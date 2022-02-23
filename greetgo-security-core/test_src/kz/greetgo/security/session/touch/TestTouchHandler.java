package kz.greetgo.security.session.touch;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TestTouchHandler implements TouchHandler {

  public final Map<String, Date> updatedSessions = new HashMap<>();

  @Override
  public void updateLastModifiedAt(String sessionId, Date lastModifiedAt) {
    updatedSessions.put(sessionId, lastModifiedAt);
  }

}
