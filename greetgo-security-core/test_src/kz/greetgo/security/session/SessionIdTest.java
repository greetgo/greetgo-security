package kz.greetgo.security.session;

import kz.greetgo.util.RND;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SessionIdTest {
  @Test
  public void parse_toString() {

    SessionId id1 = new SessionId(RND.str(10), RND.str(10));

    SessionId id2 = SessionId.parse(id1.toString());

    assertThat(id2.salt).isEqualTo(id1.salt);
    assertThat(id2.part).isEqualTo(id1.part);
  }
}
