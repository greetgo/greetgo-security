package kz.greetgo.security.password;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

import static java.nio.charset.StandardCharsets.UTF_8;

public class PasswordEncoderBuilder {

  private int iterations  = 4;
  private int memory      = 128 * 128;
  private int parallelism = 10;

  private boolean built = false;

  PasswordEncoderBuilder() {}

  public static PasswordEncoderBuilder newBuilder() {
    return new PasswordEncoderBuilder();
  }

  private void checkBuilt() {
    if (built) {
      throw new RuntimeException("RJ7gBe1nxE :: Already built");
    }
  }

  public PasswordEncoderBuilder iterations(int iterations) {
    this.iterations = iterations;
    return this;
  }

  public PasswordEncoderBuilder memory(int memory) {
    this.memory = memory;
    return this;
  }

  public PasswordEncoderBuilder parallelism(int parallelism) {
    this.parallelism = parallelism;
    return this;
  }

  public PasswordEncoder build() {
    checkBuilt();
    Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
    built = true;
    return new PasswordEncoder() {
      @Override
      public String encode(String password) {
        return argon2.hash(iterations, memory, parallelism, passwordToBytes(password));
      }

      @Override
      public boolean verify(String password, String encodedPassword) {
        if (encodedPassword == null) {
          return false;
        }
        return argon2.verify(encodedPassword, passwordToBytes(password));
      }
    };
  }

  private byte[] passwordToBytes(String password) {
    if (password == null) {
      password = "";
    }
    return password.getBytes(UTF_8);
  }
}
