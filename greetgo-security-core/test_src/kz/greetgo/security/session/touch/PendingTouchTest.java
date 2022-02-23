package kz.greetgo.security.session.touch;

import org.testng.annotations.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.assertj.core.api.Assertions.assertThat;

public class PendingTouchTest {

  @Test
  public void touch__idle__timeWentLessThenDelay() throws ParseException {

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    TestTouchHandler touchHandler = new TestTouchHandler();
    Calendar         time         = new GregorianCalendar();

    PendingTouch pendingTouch = new PendingTouch(touchHandler, time::getTime, () -> 100_000L);

    time.setTime(sdf.parse("2020-01-01 11:00:12"));
    pendingTouch.touch("s1");

    time.setTime(sdf.parse("2020-01-01 11:00:17"));
    pendingTouch.touch("s1");

    time.setTime(sdf.parse("2020-01-01 11:00:15"));
    pendingTouch.touch("s1");

    time.setTime(sdf.parse("2020-01-01 11:00:20"));
    pendingTouch.idle();

    assertThat(touchHandler.updatedSessions).isEmpty();
  }

  @Test
  public void touch__idle__timeWentMoreThenDelay() throws ParseException {

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    TestTouchHandler touchHandler = new TestTouchHandler();
    Calendar         time         = new GregorianCalendar();

    PendingTouch pendingTouch = new PendingTouch(touchHandler, time::getTime, () -> 10_000L);

    time.setTime(sdf.parse("2020-01-01 11:00:10"));
    pendingTouch.touch("s1");

    time.setTime(sdf.parse("2020-01-01 11:00:15"));
    pendingTouch.touch("s1");

    time.setTime(sdf.parse("2020-01-01 11:00:19"));
    pendingTouch.touch("s1");

    assertThat(touchHandler.updatedSessions).isEmpty();

    time.setTime(sdf.parse("2020-01-01 11:00:21"));
    pendingTouch.idle();

    assertThat(touchHandler.updatedSessions).containsEntry("s1", sdf.parse("2020-01-01 11:00:19"));
  }

  @Test
  public void touch__idle__someSessionIds() throws ParseException {

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    TestTouchHandler touchHandler = new TestTouchHandler();
    Calendar         time         = new GregorianCalendar();

    PendingTouch pendingTouch = new PendingTouch(touchHandler, time::getTime, () -> 10_000L);

    time.setTime(sdf.parse("2020-01-01 11:00:10"));
    pendingTouch.touch("s1");

    time.setTime(sdf.parse("2020-01-01 11:00:13"));
    pendingTouch.touch("s2");

    time.setTime(sdf.parse("2020-01-01 11:00:19"));
    pendingTouch.touch("s3");

    assertThat(touchHandler.updatedSessions).isEmpty();

    time.setTime(sdf.parse("2022-01-01 12:00:00"));
    pendingTouch.idle();

    assertThat(touchHandler.updatedSessions).containsEntry("s1", sdf.parse("2020-01-01 11:00:10"));
    assertThat(touchHandler.updatedSessions).containsEntry("s2", sdf.parse("2020-01-01 11:00:13"));
    assertThat(touchHandler.updatedSessions).containsEntry("s3", sdf.parse("2020-01-01 11:00:19"));
  }

}
