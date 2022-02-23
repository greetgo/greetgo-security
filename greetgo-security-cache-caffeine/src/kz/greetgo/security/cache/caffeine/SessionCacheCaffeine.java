package kz.greetgo.security.cache.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import kz.greetgo.security.session.SessionCache;
import kz.greetgo.security.session.SessionRow;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

public class SessionCacheCaffeine implements SessionCache {

  private static class Params {
    final long maximumSize;
    final long lifeTimeMillis;

    Params(long maximumSize, long lifeTimeMillis) {
      this.maximumSize    = maximumSize;
      this.lifeTimeMillis = lifeTimeMillis;
    }

    boolean eq(Params params) {
      if (this == params) {
        return true;
      }
      if (params == null) {
        return false;
      }
      return maximumSize == params.maximumSize
        && lifeTimeMillis == params.lifeTimeMillis;
    }

    Cache<String, Optional<SessionRow>> createCache() {
      return maximumSize == 0 ? null : Caffeine.newBuilder()
                                               .maximumSize(maximumSize)
                                               .expireAfterWrite(lifeTimeMillis, TimeUnit.MILLISECONDS)
                                               .build();
    }
  }

  public static class Builder {
    LongSupplier maximumSize    = () -> 100_000;
    LongSupplier lifeTimeMillis = () -> 1_000;

    private Builder() {}

    public Builder maximumSize(LongSupplier maximumSize) {
      this.maximumSize = maximumSize;
      return this;
    }

    public Builder lifeTimeMillis(LongSupplier lifeTimeMillis) {
      this.lifeTimeMillis = lifeTimeMillis;
      return this;
    }

    public Params getParams() {
      return new Params(maximumSize.getAsLong(), lifeTimeMillis.getAsLong());
    }

    public SessionCacheCaffeine build() {
      return new SessionCacheCaffeine(this);
    }
  }

  private static class CacheWithParams {
    private final Cache<String, Optional<SessionRow>> cache;

    private final Params params;

    private CacheWithParams(Params params, Cache<String, Optional<SessionRow>> cache) {
      this.params = params;
      this.cache  = cache;
    }

    public void cleanUp() {
      if (cache != null) {
        cache.cleanUp();
      }
    }
  }

  private final AtomicReference<CacheWithParams> cacheRef = new AtomicReference<>(null);

  private final Builder builder;

  private Cache<String, Optional<SessionRow>> getCache() {

    Params currentParams = builder.getParams();

    CacheWithParams current = this.cacheRef.get();
    if (current == null) {
      Cache<String, Optional<SessionRow>> cache = currentParams.createCache();

      CacheWithParams old = cacheRef.getAndSet(new CacheWithParams(currentParams, cache));
      if (old != null) {
        old.cleanUp();
      }
      return cache;
    }

    if (current.params.eq(currentParams)) {
      return current.cache;
    }

    {
      Cache<String, Optional<SessionRow>> cache = currentParams.createCache();

      CacheWithParams old = cacheRef.getAndSet(new CacheWithParams(currentParams, cache));
      if (old != null) {
        old.cleanUp();
      }

      return cache;
    }

  }

  private SessionCacheCaffeine(Builder builder) {
    this.builder = builder;
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public Optional<SessionRow> get(String sessionId, Supplier<Optional<SessionRow>> direct) {
    var cache = getCache();
    return cache == null ? direct.get() : cache.get(sessionId, x -> direct.get());
  }

  @Override
  public void invalidate(String sessionId) {
    var cache = getCache();
    if (cache == null) {
      return;
    }
    cache.invalidate(sessionId);
  }
}
