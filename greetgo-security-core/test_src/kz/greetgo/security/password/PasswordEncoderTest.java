package kz.greetgo.security.password;

import org.testng.annotations.Test;

import static kz.greetgo.security.SecurityBuilders.newPasswordEncoderBuilder;
import static org.assertj.core.api.Assertions.assertThat;

public class PasswordEncoderTest {
  @Test
  public void encode_verify() {

    PasswordEncoder passwordEncoder = newPasswordEncoderBuilder().iterations(10)
                                                                 .memory(64 * 64)
                                                                 .parallelism(8)
                                                                 .build();

    //
    //
    String encodedPassword1 = passwordEncoder.encode("111");
    String encodedPassword2 = passwordEncoder.encode("111");
    //
    //

    System.out.println("3fDToBHu95 :: encodedPassword1 = " + encodedPassword1);
    System.out.println("3fDToBHu95 :: encodedPassword2 = " + encodedPassword2);

    assertThat(encodedPassword1).isNotEqualTo(encodedPassword2);

    {
      boolean verifyResult = passwordEncoder.verify("111", encodedPassword2);
      assertThat(verifyResult).isTrue();
    }
    {
      boolean verifyResult = passwordEncoder.verify("111", encodedPassword1);
      assertThat(verifyResult).isTrue();
    }
    {
      boolean verifyResult = passwordEncoder.verify("222", encodedPassword1);
      assertThat(verifyResult).isFalse();
    }
  }

  @Test
  public void encode_verify_nullAndEmpty() {

    PasswordEncoder passwordEncoder = newPasswordEncoderBuilder().iterations(4)
                                                                 .memory(64 * 64)
                                                                 .parallelism(8)
                                                                 .build();

    //
    //
    String encodedPassword1 = passwordEncoder.encode(null);
    String encodedPassword2 = passwordEncoder.encode("");
    //
    //

    assertThat(encodedPassword1).isNotEqualTo(encodedPassword2);

    {
      boolean verifyResult = passwordEncoder.verify(null, encodedPassword1);
      assertThat(verifyResult).isTrue();
    }
    {
      boolean verifyResult = passwordEncoder.verify("222", encodedPassword1);
      assertThat(verifyResult).isFalse();
    }
    {
      boolean verifyResult = passwordEncoder.verify("", encodedPassword1);
      assertThat(verifyResult).isTrue();
    }
  }
}
