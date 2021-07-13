package kz.greetgo.security.session;

public interface SessionValidator<SessionUserData> {

  void validate(String sessionId, SessionUserData sessionData, String token) throws Exception;

}
