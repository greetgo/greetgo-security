package kz.greetgo.security.session;

import java.util.Optional;
import java.util.function.Supplier;

public class NoSessionCache implements SessionCache {

  @Override
  public Optional<SessionRow> get(String sessionId, Supplier<Optional<SessionRow>> direct) {
    return direct.get();
  }

  @Override
  public void invalidate(String sessionId) {}

}
