package kz.greetgo.security.session;

import kz.greetgo.security.crypto.Crypto;
import kz.greetgo.security.util.SessionDot;
import kz.greetgo.security.util.SkipListener;
import kz.greetgo.security.util.TestSessionStorage;
import kz.greetgo.util.RND;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static kz.greetgo.security.SecurityBuilders.newCryptoBuilder;
import static kz.greetgo.security.SecurityBuilders.newSessionServiceBuilder;
import static org.assertj.core.api.Assertions.assertThat;

@Listeners(SkipListener.class)
public class SessionServiceTest {

  TestSessionStorage sessionStorage;
  SessionService     sessionService;
  SessionService     sessionService2;
  SessionServiceImpl impl;
  SessionServiceImpl impl2;
  final SaltGenerator saltGenerator = str -> "S" + str.substring(0, 5) + "S";

  @BeforeMethod
  public void createSessionService() {
    sessionStorage = new TestSessionStorage();

    sessionService = newSessionServiceBuilder().setSessionIdLength(17)
                                               .setTokenLength(17)
                                               .setStorage(sessionStorage)
                                               .setSaltGenerator(saltGenerator)
                                               .build();

    impl = (SessionServiceImpl) sessionService;

    sessionService2 = newSessionServiceBuilder().setSessionIdLength(17)
                                                .setTokenLength(17)
                                                .setStorage(sessionStorage)
                                                .setSaltGenerator(saltGenerator)
                                                .build();

    impl2 = (SessionServiceImpl) sessionService2;
  }

  @Test
  public void createSession() {
    String sessionData = "SESSION DATA " + RND.str(10);

    //
    //
    SessionIdentity identity = sessionService.createSession(sessionData);
    //
    //

    assertThat(identity).isNotNull();
    assertThat(identity.id).isNotNull();
    assertThat(identity.token).isNotNull();
    assertThat(identity.id).isNotEqualTo(identity.token);

    SessionDot sessionDot = sessionStorage.sessionMap.get(identity.id);
    assertThat(sessionDot).isNotNull();
    assertThat(sessionDot.id).isEqualTo(identity.id);
    assertThat(sessionDot.token).isEqualTo(identity.token);
    assertThat(sessionDot.sessionData).isEqualTo(sessionData);
  }

  @Test
  public void getSessionData() {

    String          sessionData = "SESSION DATA " + RND.str(10);
    SessionIdentity identity    = sessionService.createSession(sessionData);

    //
    //
    Object actual = sessionService.getSessionData(identity.id);
    //
    //

    assertThat(actual).isEqualTo(sessionData);

  }


  @Test
  public void getSessionData_fromStorage() {
    sessionStorage.loadSessionCount = 0;

    String          sessionData = "SESSION DATA " + RND.str(10);
    SessionIdentity identity    = sessionService.createSession(sessionData);

    //
    //
    Object actual = sessionService.getSessionData(identity.id);
    //
    //

    assertThat(actual).isEqualTo(sessionData);

    assertThat(sessionStorage.loadSessionCount).isEqualTo(1);
  }

  @Test
  public void getSessionData_inRemoved() {
    String sessionData = "SESSION DATA " + RND.str(10);

    SessionIdentity identity = sessionService.createSession(sessionData);

    //
    //
    Object actual = sessionService.getSessionData(identity.id);
    //
    //

    assertThat(actual).isNull();
  }

  @Test
  public void getSessionData_fromCache() {
    String          sessionData = "SESSION DATA " + RND.str(10);
    SessionIdentity identity    = sessionService.createSession(sessionData);

    sessionService.getSessionData(identity.id);

    sessionStorage.loadSessionCount = 0;

    //
    //
    Object actual = sessionService.getSessionData(identity.id);
    //
    //

    assertThat(actual).isEqualTo(sessionData);

    assertThat(sessionStorage.loadSessionCount).isZero();
  }

  @Test
  public void getSessionData_absent() {
    sessionStorage.loadSessionCount = 0;

    //
    //
    Object actual = sessionService.getSessionData(RND.str(10));
    //
    //

    assertThat(actual).isNull();

    assertThat(sessionStorage.loadSessionCount).isEqualTo(1);
  }

  @Test(invocationCount = 10)
  public void verifyId() {
    String id   = RND.str(10);
    String salt = saltGenerator.generateSalt(id);

    String sessionId = new SessionId(salt, id).toString();

    System.out.println("UozSw5rt28 :: sessionId = " + sessionId);

    //
    //
    boolean verificationResult = sessionService.verifyId(sessionId);
    //
    //

    assertThat(verificationResult).isTrue();
  }

  @Test
  public void verifyId_leftId1() {
    //
    //
    boolean verificationResult = sessionService.verifyId(RND.str(10));
    //
    //

    assertThat(verificationResult).isFalse();
  }

  @Test
  public void verifyId_leftId2() {
    //
    //
    boolean verificationResult = sessionService.verifyId(RND.str(10) + "-");
    //
    //

    assertThat(verificationResult).isFalse();
  }

  @Test
  public void verifyId_leftId3() {
    //
    //
    boolean verificationResult = sessionService.verifyId("-" + RND.str(10));
    //
    //

    assertThat(verificationResult).isFalse();
  }


  @Test
  public void verifyId_null() {
    //
    //
    boolean verificationResult = sessionService.verifyId(null);
    //
    //

    assertThat(verificationResult).isFalse();
  }

  @Test
  public void verifyToken() {
    SessionIdentity identity = sessionService.createSession(null);

    sessionStorage.loadSessionCount = 0;

    //
    //
    boolean verificationResult = sessionService.verifyToken(identity.id, identity.token);
    //
    //

    assertThat(verificationResult).isTrue();
    assertThat(sessionStorage.loadSessionCount).isEqualTo(1);
  }

  @Test
  public void verifyToken_fromCache() {
    SessionIdentity identity = sessionService.createSession(null);

    sessionStorage.loadSessionCount = 0;

    //
    //
    boolean verificationResult = sessionService.verifyToken(identity.id, identity.token);
    //
    //

    assertThat(verificationResult).isTrue();
    assertThat(sessionStorage.loadSessionCount).isZero();
  }

  @Test
  public void verifyToken_noSession() {
    SessionIdentity identity = sessionService.createSession(null);

    sessionStorage.loadSessionCount = 0;

    sessionStorage.sessionMap.clear();

    //
    //
    boolean verificationResult = sessionService.verifyToken(identity.id, identity.token);
    //
    //

    assertThat(verificationResult).isFalse();
    assertThat(sessionStorage.loadSessionCount).isEqualTo(1);
  }

  @Test
  public void verifyToken_anotherToken() {
    SessionIdentity identity1 = sessionService.createSession(null);
    SessionIdentity identity2 = sessionService.createSession(null);

    sessionStorage.loadSessionCount = 0;

    //
    //
    boolean verificationResult = sessionService.verifyToken(identity1.id, identity2.token);
    //
    //

    assertThat(verificationResult).isFalse();
    assertThat(sessionStorage.loadSessionCount).isEqualTo(1);
  }

  @Test
  public void verifyToken_leftToken_noCache() {
    SessionIdentity identity = sessionService.createSession(null);

    sessionStorage.loadSessionCount = 0;

    //
    //
    boolean verificationResult = sessionService.verifyToken(identity.id, RND.str(10));
    //
    //

    assertThat(verificationResult).isFalse();
    assertThat(sessionStorage.loadSessionCount).isEqualTo(1);
  }

  @Test
  public void verifyToken_leftToken() {
    SessionIdentity identity = sessionService.createSession(null);

    sessionStorage.loadSessionCount = 0;

    //
    //
    boolean verificationResult = sessionService.verifyToken(identity.id, RND.str(10));
    //
    //

    assertThat(verificationResult).isFalse();
    assertThat(sessionStorage.loadSessionCount).isZero();
  }

  public static Date nowAddHours(int hours) {
    Calendar calendar = new GregorianCalendar();
    calendar.add(Calendar.HOUR, hours);
    return calendar.getTime();
  }

  @Test
  public void removeSession() {
    SessionIdentity identity = sessionService.createSession(null);

    assertThat(sessionStorage.sessionMap).containsKey(identity.id);

    //
    //
    sessionService.removeSession(identity.id);
    //
    //

    assertThat(sessionStorage.sessionMap).doesNotContainKey(identity.id);
  }

  @Test
  public void removeSession_noSession_leftId() {
    String sessionId = RND.str(10);

    //
    //
    sessionService.removeSession(sessionId);
    //
    //

    assertThat(1).isEqualTo(2);
  }

  @Test
  public void removeSession_noSession_goodId() {
    SessionIdentity identity = sessionService.createSession(null);

    sessionStorage.sessionMap.remove(identity.id);

    //
    //
    sessionService.removeSession(identity.id);
    //
    //

    assertThat(1).isEqualTo(2);
  }

  private void setLastTouchedByInDb(String sessionId, Date value) {
    sessionStorage.sessionMap.get(sessionId).lastTouchedAt = value;
  }

  @Test
  public void removeOldSessions() {
    String youngId = sessionService.createSession(null).id;
    String oldId   = sessionService.createSession(null).id;

    setLastTouchedByInDb(youngId, nowAddHours(-7 + 1));
    setLastTouchedByInDb(oldId, nowAddHours(-7 - 1));

    //
    //
    sessionService.removeOldSessions(7);
    //
    //

    assertThat(sessionStorage.sessionMap).containsKey(youngId);

    assertThat(sessionStorage.sessionMap).doesNotContainKey(oldId);
  }

  @Test
  public void saltGeneratorFromCrypto() {
    Path dir = Paths.get("build/test_data/saltGeneratorFromCrypto");

    Crypto crypto = newCryptoBuilder().inFiles(dir.resolve("pri.key").toFile(), dir.resolve("pub.key").toFile())
                                      .build();

    SessionService sessionService = newSessionServiceBuilder().setSessionIdLength(17)
                                                              .setTokenLength(17)
                                                              .setStorage(sessionStorage)
                                                              .setSaltGeneratorOnCrypto(crypto, 17, RND.byteArray(100))
                                                              .build();

    SessionIdentity identity = sessionService.createSession(null);
//    System.out.println(identity);
    assertThat(identity).isNotNull();
  }

}
