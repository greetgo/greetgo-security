package kz.greetgo.security.cache.caffeine;

import kz.greetgo.security.session.SessionRow;
import kz.greetgo.util.RND;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class SessionCacheCaffeineTest {

  @Test
  public void caching() {

    SessionCacheCaffeine cache = SessionCacheCaffeine.builder()
                                                     .maximumSize(10_000)
                                                     .lifeTimeMillis(800)
                                                     .build();

    SessionRow row1 = new SessionRow(RND.str(10), "WOW1", new Date(), new Date());
    SessionRow row2 = new SessionRow(RND.str(10), "WOW2", new Date(), new Date());

    Optional<SessionRow> outRow1 = cache.get("x", () -> Optional.of(row1));
    Optional<SessionRow> outRow2 = cache.get("x", () -> Optional.of(row2));

    assertThat(outRow1.orElseThrow().token).describedAs("Must be `row1`")
                                           .isEqualTo(row1.token);

    assertThat(outRow2.orElseThrow().token).describedAs("Must be `row1` (also 1, not 2)")
                                           .isEqualTo(row1.token);

  }
}
