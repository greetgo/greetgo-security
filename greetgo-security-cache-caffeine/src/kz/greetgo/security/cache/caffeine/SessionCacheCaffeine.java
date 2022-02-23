package kz.greetgo.security.cache.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import kz.greetgo.security.session.SessionCache;
import kz.greetgo.security.session.SessionRow;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class SessionCacheCaffeine implements SessionCache {

  public static class Builder {
    long maximumSize    = 100_000;
    long lifeTimeMillis = 1_000;

    private Builder() {}

    public Builder maximumSize(long maximumSize) {
      this.maximumSize = maximumSize;
      return this;
    }

    public Builder lifeTimeMillis(long lifeTimeMillis) {
      this.lifeTimeMillis = lifeTimeMillis;
      return this;
    }

    public SessionCacheCaffeine build() {
      return new SessionCacheCaffeine(this);
    }
  }

  private final Cache<String, Optional<SessionRow>> cache;

  private SessionCacheCaffeine(Builder builder) {
    if (builder.maximumSize == 0) {
      cache = null;
    } else {
      cache = Caffeine.newBuilder()
                      .maximumSize(builder.maximumSize)
                      .expireAfterWrite(builder.lifeTimeMillis, TimeUnit.MILLISECONDS)
                      .build();
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public Optional<SessionRow> get(String sessionId, Supplier<Optional<SessionRow>> direct) {
    var cache = this.cache;
    return cache == null ? direct.get() : cache.get(sessionId, x -> direct.get());
  }

  @Override
  public void invalidate(String sessionId) {
    var cache = this.cache;
    if (cache == null) {
      return;
    }
    cache.invalidate(sessionId);
  }
}
