package kz.greetgo.security.session.touch;

import java.util.Date;

public interface TouchHandler {

  void updateLastModifiedAt(String sessionId, Date lastModifiedAt);

}
