package kz.greetgo.security.session;

import kz.greetgo.security.errors.SerializedClassChanged;
import kz.greetgo.security.session.cache.Cache;
import kz.greetgo.security.session.cache.CacheBuilder;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

class SessionServiceImpl implements SessionService {
  private final SessionServiceBuilder builder;

  public SessionServiceImpl(SessionServiceBuilder builder) {
    this.builder     = builder;
    lastTouchedCache = new CacheBuilder<String, Date>()
      .loader(builder.storage::loadLastTouchedAt)
      .refreshTimeoutSec(builder.lastTouchedCacheTimeoutSec)
      .maxSize(builder.lastTouchedCacheSize)
      .build();
  }

  static class SessionCache {
    final Object                sessionData;
    final String                token;
    final AtomicReference<Date> lastTouchedAt;

    public SessionCache(Object sessionData, String token, Date lastTouchedAt) {
      this.sessionData   = sessionData;
      this.token         = token;
      this.lastTouchedAt = new AtomicReference<>(lastTouchedAt);
    }
  }

  final ConcurrentMap<String, SessionCache> sessionCacheMap   = new ConcurrentHashMap<>();
  final ConcurrentMap<String, String>       removedSessionIds = new ConcurrentHashMap<>();

  @Override
  public Map<String, String> statisticsInfo() {
    HashMap<String, String> ret = new HashMap<>();
    ret.put("Sessions cache size", "" + sessionCacheMap.size());
    ret.put("Removed sessions map size", "" + removedSessionIds.size());
    return ret;
  }

  @Override
  public SessionIdentity createSession(Object sessionData) {
    String          sessionIdPart = SessionGenId.generate(builder.sessionIdLength);
    String          sessionSalt   = builder.saltGenerator.generateSalt(sessionIdPart);
    String          sessionId     = new SessionId(sessionSalt, sessionIdPart).toString();
    String          token         = SessionGenId.generate(builder.tokenLength);
    SessionIdentity identity      = new SessionIdentity(sessionId, token);

    builder.storage.insertSession(identity, sessionData);

    Date lastTouchedAt = loadLastTouchedAt(identity.id);

    SessionCache sessionCache = new SessionCache(sessionData, token, lastTouchedAt);

    sessionCacheMap.put(sessionId, sessionCache);

    return identity;
  }

  private final Cache<String, Date> lastTouchedCache;

  private Date loadLastTouchedAt(String sessionId) {
    return lastTouchedCache.get(sessionId);
  }

  private static <T> T cast(Object object) {
    //noinspection unchecked
    return (T) object;
  }

  @Override
  public <T> T getSessionData(String sessionId) {

    if (removedSessionIds.containsKey(sessionId)) {
      return null;
    }

    {
      SessionCache sessionCache = sessionCacheMap.get(sessionId);
      if (sessionCache != null) {
        if (sessionLeft(sessionId, sessionCache.sessionData, sessionCache.token)) {
          return null;
        }
        return cast(sessionCache.sessionData);
      }
    }

    return cast(loadSession(sessionId).map(row -> row.sessionData).orElse(null));

  }

  private Optional<SessionRow> loadSession(String sessionId) {

    try {

      SessionRow sessionRow = builder.storage.loadSession(sessionId);
      if (sessionRow == null) {
        return Optional.empty();
      }

      if (sessionLeft(sessionId, sessionRow.sessionData, sessionRow.token)) {
        return Optional.empty();
      }

      sessionCacheMap.put(sessionId, sessionRow.toCacheRecord());
      return Optional.of(sessionRow);

    } catch (SerializedClassChanged e) {

      return Optional.empty();

    }

  }

  private final ConcurrentMap<String, Long> sessionLastValidatedMillis_map = new ConcurrentHashMap<>();

  private boolean sessionLeft(String sessionId, Object sessionData, String token) {
    return !sessionValidated(sessionId, sessionData, token);
  }

  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  private boolean sessionValidated(String sessionId, Object sessionData, String token) {
    SessionValidator<Object> sessionValidator = builder.sessionValidator;
    if (sessionValidator == null) {
      return true;
    }

    var lastValidatedMillis = sessionLastValidatedMillis_map.get(sessionId);
    if (lastValidatedMillis != null && (System.currentTimeMillis() - lastValidatedMillis <= builder.validateSessionDelayMillis.getAsLong())) {
      return true;
    }

    try {
      sessionValidator.validate(sessionId, sessionData, token);
      sessionLastValidatedMillis_map.put(sessionId, System.currentTimeMillis());
      return true;
    } catch (Exception e) {
      builder.sessionLog.sessionValidateError(e);

      builder.storage.remove(sessionId);
      sessionCacheMap.remove(sessionId);
      sessionLastValidatedMillis_map.remove(sessionId);

      return false;
    }
  }

  @Override
  public boolean verifyId(String sessionId) {
    if (sessionId == null) {
      return false;
    }

    SessionId s = SessionId.parse(sessionId);

    if (s == null) {
      return false;
    }

    if (s.part == null || s.part.isEmpty()) {
      return false;
    }

    if (s.salt == null || s.salt.isEmpty()) {
      return false;
    }

    String saltExpected = builder.saltGenerator.generateSalt(s.part);

    return Objects.equals(saltExpected, s.salt);

  }

  @Override
  public boolean verifyToken(String sessionId, String token) {
    SessionCache cache = sessionCacheMap.get(sessionId);
    if (cache != null) {

      if (sessionLeft(sessionId, cache.sessionData, cache.token)) {
        return false;
      }

      return Objects.equals(cache.token, token);
    }

    return loadSession(sessionId)
      .filter(row -> row.token != null && row.token.equals(token))
      .isPresent();
  }

  @Override
  public Optional<String> getToken(String sessionId) {
    SessionCache cache = sessionCacheMap.get(sessionId);
    if (cache != null) {

      if (sessionLeft(sessionId, cache.sessionData, cache.token)) {
        return Optional.empty();
      }

      return Optional.ofNullable(cache.token);
    }

    return loadSession(sessionId).map(x -> x.token);
  }

  @Override
  public void removeSession(String sessionId) {
    if (!verifyId(sessionId)) {
      return;
    }
    sessionCacheMap.remove(sessionId);
    builder.storage.remove(sessionId);
    removedSessionIds.put(sessionId, sessionId);
  }


  @Override
  public void removeOldSessions(int hoursOld) {

    builder.storage.removeSessionsOlderThan(builder.oldSessionAgeInHours);

    Calendar calendar = new GregorianCalendar();
    calendar.add(Calendar.HOUR, -hoursOld);

    Set<String> removingIds = sessionCacheMap.entrySet().stream()
                                             .filter(s -> s.getValue().lastTouchedAt.get().before(calendar.getTime()))
                                             .map(Map.Entry::getKey)
                                             .collect(Collectors.toSet());

    removingIds.forEach(sessionCacheMap::remove);
    removingIds.forEach(id -> removedSessionIds.put(id, id));
  }

  @Override
  public void idle() {
    throw new RuntimeException("22.02.2022 15:43: Not impl yet: SessionServiceImpl.idle");
  }

}
