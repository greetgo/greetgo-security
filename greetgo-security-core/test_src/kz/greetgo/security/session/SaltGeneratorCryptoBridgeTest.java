package kz.greetgo.security.session;

import kz.greetgo.security.crypto.Crypto;
import kz.greetgo.security.crypto.CryptoSourceConfigDefault;
import kz.greetgo.util.RND;
import org.testng.annotations.Test;

import java.io.File;

import static kz.greetgo.security.SecurityBuilders.newCryptoBuilder;
import static org.assertj.core.api.Assertions.assertThat;

public class SaltGeneratorCryptoBridgeTest {

  @Test
  public void generateSalt() {
    String       suffix  = RND.intStr(19);
    final String keysDir = "build/test_data/SaltGeneratorCryptoBridgeTest/keys/";

    File privateKeyFile = new File(keysDir + suffix + ".private.key");
    File publicKeyFile  = new File(keysDir + suffix + ".public.key");

    Crypto crypto = newCryptoBuilder().setKeySize(1024)
                                      .inFiles(privateKeyFile, publicKeyFile)
                                      .setConfig(new CryptoSourceConfigDefault())
                                      .build();

    SaltGeneratorCryptoBridge saltGenerator = new SaltGeneratorCryptoBridge(crypto);

    String str1 = RND.str(10);
    String str2 = RND.str(100);

    String salt1  = saltGenerator.generateSalt(str1);
    String salt11 = saltGenerator.generateSalt(str1);
    String salt2  = saltGenerator.generateSalt(str2);
    String salt22 = saltGenerator.generateSalt(str2);

    System.out.println("pL5QeWwNYT :: str1 = " + str1);
    System.out.println("pL5QeWwNYT ::     salt1  = " + salt1);
    System.out.println("pL5QeWwNYT ::     salt11 = " + salt11);
    System.out.println("ciM67PEqT7 :: str2 = " + str2);
    System.out.println("ciM67PEqT7 ::     salt2  = " + salt2);
    System.out.println("ciM67PEqT7 ::     salt22 = " + salt22);

    assertThat(salt1).isNotEqualTo(salt2);
    assertThat(salt1).isNotEqualTo(salt11);
    assertThat(salt2).isNotEqualTo(salt22);
  }

  @Test
  public void generateSalt__validateSalt() {
    String       suffix  = RND.intStr(19);
    final String keysDir = "build/test_data/SaltGeneratorCryptoBridgeTest/keys/";

    File privateKeyFile = new File(keysDir + suffix + ".private.key");
    File publicKeyFile  = new File(keysDir + suffix + ".public.key");

    Crypto crypto = newCryptoBuilder().setKeySize(1024)
                                      .inFiles(privateKeyFile, publicKeyFile)
                                      .setConfig(new CryptoSourceConfigDefault())
                                      .build();

    SaltGeneratorCryptoBridge saltGenerator = new SaltGeneratorCryptoBridge(crypto);

    String str1 = RND.str(10);
    String str2 = RND.str(100);

    String salt1  = saltGenerator.generateSalt(str1);
    String salt11 = saltGenerator.generateSalt(str1);
    String salt2  = saltGenerator.generateSalt(str2);
    String salt22 = saltGenerator.generateSalt(str2);

    System.out.println("ES6Oar1IZ0 :: str1 = " + str1);
    System.out.println("ES6Oar1IZ0 ::     salt1  = " + salt1);
    System.out.println("ES6Oar1IZ0 ::     salt11 = " + salt11);
    System.out.println("ES6Oar1IZ0 :: str2 = " + str2);
    System.out.println("ES6Oar1IZ0 ::     salt2  = " + salt2);
    System.out.println("ES6Oar1IZ0 ::     salt22 = " + salt22);

    assertThat(saltGenerator.validateSalt(str1, salt1)).isTrue();
    assertThat(saltGenerator.validateSalt(str1, salt11)).isTrue();
    assertThat(saltGenerator.validateSalt(str2, salt2)).isTrue();
    assertThat(saltGenerator.validateSalt(str2, salt22)).isTrue();

    assertThat(salt1).isNotEqualTo(salt11);
    assertThat(salt2).isNotEqualTo(salt22);

  }

}
