package kz.greetgo.security.session;

import kz.greetgo.security.errors.SerializedClassChanged;
import kz.greetgo.security.session.touch.PendingTouch;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.Optional;

class SessionServiceImpl implements SessionService {
  private final SessionServiceBuilder builder;
  private final PendingTouch          pendingTouch;

  public SessionServiceImpl(SessionServiceBuilder builder) {
    this.builder = builder;
    pendingTouch = new PendingTouch(() -> builder.nowSupplier.get(),
                                    () -> builder.delayTouchSyncMs,
                                    (id, date) -> builder.storage.setLastTouchedAt(id, date));
  }

  @Override
  public SessionIdentity createSession(Object sessionData) {
    String          sessionIdPart = SessionGenId.generate(builder.sessionIdLength);
    String          sessionSalt   = builder.saltGenerator.generateSalt(sessionIdPart);
    String          sessionId     = new SessionId(sessionSalt, sessionIdPart).toString();
    String          token         = SessionGenId.generate(builder.tokenLength);
    SessionIdentity identity      = new SessionIdentity(sessionId, token);

    builder.storage.insertSession(identity, sessionData);
    builder.storage.setLastTouchedAt(identity.id, builder.nowSupplier.get());

    return identity;
  }

  private static <T> T cast(Object object) {
    //noinspection unchecked
    return (T) object;
  }

  @Override
  public <T> T getSessionData(String sessionId) {
    return cast(getSession(sessionId).map(row -> row.sessionData).orElse(null));
  }

  private Optional<SessionRow> getSession(String sessionId) {

    try {

      SessionRow sessionRow = builder.storage.loadSession(sessionId);
      if (sessionRow == null) {
        return Optional.empty();
      }

      if (isInvalidSession(sessionId, sessionRow.sessionData, sessionRow.token)) {
        return Optional.empty();
      }

      pendingTouch.touch(sessionId);

      return Optional.of(sessionRow);

    } catch (SerializedClassChanged e) {

      return Optional.empty();

    }

  }

  private boolean isInvalidSession(String sessionId, Object sessionData, String token) {
    return !isSessionValid(sessionId, sessionData, token);
  }

  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  private boolean isSessionValid(String sessionId, Object sessionData, String token) {
    SessionValidator<Object> sessionValidator = builder.sessionValidator;
    if (sessionValidator == null) {
      return true;
    }

    try {
      sessionValidator.validate(sessionId, sessionData, token);
      return true;
    } catch (Exception e) {
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
    return getSession(sessionId)
      .filter(row -> row.token != null && row.token.equals(token))
      .isPresent();
  }

  @Override
  public Optional<String> getToken(String sessionId) {
    return getSession(sessionId).map(x -> x.token);
  }

  @Override
  public void removeSession(String sessionId) {
    if (!verifyId(sessionId)) {
      return;
    }
    builder.storage.remove(sessionId);
  }


  @Override
  public void removeOldSessions(int hoursOld) {

    builder.storage.removeSessionsOlderThan(hoursOld);

    Calendar calendar = new GregorianCalendar();
    calendar.add(Calendar.HOUR, -hoursOld);
  }

  @Override
  public void idle() {
    pendingTouch.idle();
  }

}
