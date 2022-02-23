package kz.greetgo.security.session;

import java.util.Optional;
import java.util.function.Supplier;

public interface SessionCache {

  Optional<SessionRow> get(String sessionId, Supplier<Optional<SessionRow>> direct);

  void invalidate(String sessionId);

}
