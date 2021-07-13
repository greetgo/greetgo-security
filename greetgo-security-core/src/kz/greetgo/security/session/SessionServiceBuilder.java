package kz.greetgo.security.session;

import kz.greetgo.security.crypto.Crypto;

import java.util.function.IntSupplier;
import java.util.function.LongSupplier;

import static java.util.Objects.requireNonNull;

public class SessionServiceBuilder {
  SessionStorage storage;
  SaltGenerator  saltGenerator;

  int oldSessionAgeInHours = 24;
  int sessionIdLength      = 15;
  int tokenLength          = 15;

  LongSupplier lastTouchedCacheTimeoutSec = () -> 30;
  IntSupplier  lastTouchedCacheSize       = () -> 1_000_000;
  LongSupplier validateSessionDelayMillis = () -> 10_000;//10 seconds

  SessionValidator<Object> sessionValidator = null;

  SessionLog sessionLog = Throwable::printStackTrace;

  private SessionServiceBuilder() {}

  public static SessionServiceBuilder newBuilder() {
    return new SessionServiceBuilder();
  }

  public <T> SessionServiceBuilder sessionValidator(SessionValidator<T> sessionValidator) {
    checkBuilt();
    //noinspection unchecked
    this.sessionValidator = (SessionValidator<Object>) sessionValidator;
    return this;
  }

  public SessionServiceBuilder sessionLog(SessionLog sessionLog) {
    checkBuilt();
    this.sessionLog = requireNonNull(sessionLog, "CMNjR6SkFq :: sessionLog");
    return this;
  }

  public SessionServiceBuilder setValidateSessionDelayMillis(LongSupplier validateSessionDelayMillis) {
    checkBuilt();
    this.validateSessionDelayMillis = requireNonNull(validateSessionDelayMillis,
                                                     "mj2y4oz57T :: validateSessionDelayMillis");
    return this;
  }

  public SessionServiceBuilder setStorage(SessionStorage storage) {
    checkBuilt();
    this.storage = storage;
    return this;
  }

  private boolean built = false;

  private void checkBuilt() {
    if (built) {
      throw new RuntimeException("00vy8Th268 :: Already built");
    }
  }

  public SessionServiceBuilder setSaltGenerator(SaltGenerator saltGenerator) {
    checkBuilt();
    this.saltGenerator = saltGenerator;
    return this;
  }

  public SessionServiceBuilder setSaltGeneratorOnCrypto(Crypto crypto, int saltLength, byte[] saltMixture) {
    checkBuilt();
    this.saltGenerator = new SaltGeneratorCryptoBridge(crypto, saltLength, saltMixture);
    return this;
  }

  public SessionServiceBuilder setOldSessionAgeInHours(int oldSessionAgeInHours) {
    checkBuilt();
    this.oldSessionAgeInHours = oldSessionAgeInHours;
    return this;
  }

  public SessionServiceBuilder setSessionIdLength(int sessionIdLength) {
    checkBuilt();
    this.sessionIdLength = sessionIdLength;
    return this;
  }

  public SessionServiceBuilder setTokenLength(int tokenLength) {
    checkBuilt();
    this.tokenLength = tokenLength;
    return this;
  }

  public SessionServiceBuilder setLastTouchedCacheTimeoutSec(LongSupplier lastTouchedCacheTimeoutSec) {
    checkBuilt();
    this.lastTouchedCacheTimeoutSec = requireNonNull(lastTouchedCacheTimeoutSec);
    return this;
  }

  public SessionServiceBuilder setLastTouchedCacheTimeoutSec(long dbCacheTimeoutSec) {
    checkBuilt();
    return setLastTouchedCacheTimeoutSec(() -> dbCacheTimeoutSec);
  }

  public SessionServiceBuilder setLastTouchedCacheSize(IntSupplier lastTouchedCacheSize) {
    checkBuilt();
    this.lastTouchedCacheSize = requireNonNull(lastTouchedCacheSize);
    return this;
  }

  public SessionServiceBuilder setLastTouchedCacheSize(int lastTouchedCacheSize) {
    checkBuilt();
    return setLastTouchedCacheSize(() -> lastTouchedCacheSize);
  }

  public SessionService build() {
    built = true;
    if (storage == null) {
      throw new RuntimeException("lNUic2LKvg :: No sessionStorage");
    }
    if (saltGenerator == null) {
      throw new RuntimeException("h1tmyAyHG2 :: No saltGenerator");
    }
    return new SessionServiceImpl(this);
  }
}
