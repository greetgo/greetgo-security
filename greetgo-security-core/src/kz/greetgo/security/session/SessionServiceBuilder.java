package kz.greetgo.security.session;

import kz.greetgo.security.crypto.Crypto;

import java.util.Date;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class SessionServiceBuilder {
  SessionStorage storage;
  SaltGenerator  saltGenerator;

  SessionCache sessionCache = new NoSessionCache();

  int sessionIdLength = 15;
  int tokenLength     = 15;

  long delayTouchSyncMs = 4700;

  SessionValidator<Object> sessionValidator = null;

  SessionLog sessionLog = Throwable::printStackTrace;

  Supplier<Date> nowSupplier = Date::new;

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

  public SessionServiceBuilder setDelayTouchSyncMs(long delayTouchSyncMs) {
    this.delayTouchSyncMs = delayTouchSyncMs;
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

  public SessionServiceBuilder setNowSupplier(Supplier<Date> nowSupplier) {
    checkBuilt();
    this.nowSupplier = nowSupplier;
    return this;
  }

  public SessionServiceBuilder setSaltGeneratorOnCrypto(Crypto crypto, int saltLength, byte[] saltMixture) {
    checkBuilt();
    this.saltGenerator = new SaltGeneratorCryptoBridge(crypto, saltLength, saltMixture);
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
