package kz.greetgo.security.session;

import java.util.Date;

public interface SessionParams {
  Date insertedAt();

  Date lastTouchedAt();
}
