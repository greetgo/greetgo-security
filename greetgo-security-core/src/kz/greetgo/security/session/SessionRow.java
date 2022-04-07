package kz.greetgo.security.session;

import java.util.Date;

public class SessionRow implements SessionParams {
  public final String token;
  public final Object sessionData;
  public final Date   insertedAt;
  public final Date   lastTouchedAt;

  public SessionRow(String token, Object sessionData, Date insertedAt, Date lastTouchedAt) {
    this.token         = token;
    this.sessionData   = sessionData;
    this.insertedAt    = insertedAt;
    this.lastTouchedAt = lastTouchedAt;
  }

  @Override
  public Date insertedAt() {
    return insertedAt;
  }

  @Override
  public Date lastTouchedAt() {
    return lastTouchedAt;
  }
}
