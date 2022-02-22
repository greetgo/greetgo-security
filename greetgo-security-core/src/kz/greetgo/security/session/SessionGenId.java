package kz.greetgo.security.session;

import java.security.SecureRandom;
import java.util.Random;

public class SessionGenId {

  @SuppressWarnings("SpellCheckingInspection")
  private static final String ENG     = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final String DEG     = "0123456789";
  private static final char[] ALL     = (ENG.toLowerCase() + ENG.toUpperCase() + DEG).toCharArray();
  private static final int    ALL_LEN = ALL.length;

  private final static ThreadLocal<Random> random = ThreadLocal.withInitial(SecureRandom::new);

  public static String generate(int length) {

    int[] randomIndexes = random.get()
                                .ints()
                                .limit(length)
                                .map(i -> i < 0 ? -i : i)
                                .map(i -> i % ALL_LEN)
                                .toArray();

    char[] chars = new char[length];

    for (int i = 0; i < length; i++) {
      chars[i] = ALL[randomIndexes[i]];
    }

    return new String(chars);
  }


}
