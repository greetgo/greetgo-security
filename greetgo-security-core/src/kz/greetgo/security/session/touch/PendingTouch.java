package kz.greetgo.security.session.touch;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class PendingTouch implements AutoCloseable {

  private final TouchHandler   touchHandler;
  private final Supplier<Date> nowSupplier;
  private final Supplier<Long> delayMsSupplier;

  public PendingTouch(TouchHandler touchHandler, Supplier<Date> nowSupplier, Supplier<Long> delayMsSupplier) {
    this.touchHandler    = touchHandler;
    this.nowSupplier     = nowSupplier;
    this.delayMsSupplier = delayMsSupplier;
  }

  private static class Date2 {
    final Date                  firstTouchedAt;
    final AtomicReference<Date> lastTouchedAt;

    private Date2(Date touchedAt) {
      this.firstTouchedAt = touchedAt;
      lastTouchedAt       = new AtomicReference<>(touchedAt);
    }

    @Override
    public String toString() {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      return "Date2{" + sdf.format(firstTouchedAt) + " - " + sdf.format(lastTouchedAt.get()) + '}';
    }
  }

  private final ConcurrentHashMap<String, Date2> pendingMap = new ConcurrentHashMap<>();

  public void touch(String sessionId) {
    pendingMap
      .computeIfAbsent(sessionId, id -> new Date2(nowSupplier.get()))
      .lastTouchedAt
      .set(nowSupplier.get());
  }

  public void idle() {

    Date border = new Date(nowSupplier.get().getTime() - delayMsSupplier.get());

    while (true) {

      Map.Entry<String, Date2> e = pendingMap.entrySet()
                                             .stream()
                                             .filter(x -> border.after(x.getValue().firstTouchedAt))
                                             .findAny()
                                             .orElse(null);

      if (e == null) {
        return;
      }

      pendingMap.remove(e.getKey());

      touchHandler.updateLastModifiedAt(e.getKey(), e.getValue().lastTouchedAt.get());
    }
  }

  @Override
  public void close()  {
    while (true) {

      Map.Entry<String, Date2> e = pendingMap.entrySet()
                                             .stream()
                                             .findAny()
                                             .orElse(null);

      if (e == null) {
        return;
      }

      pendingMap.remove(e.getKey());

      touchHandler.updateLastModifiedAt(e.getKey(), e.getValue().lastTouchedAt.get());
    }
  }
}
