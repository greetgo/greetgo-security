package kz.greetgo.security.session;

import kz.greetgo.security.crypto.Crypto;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

import static kz.greetgo.security.util.ByteUtil.copyToLength;
import static kz.greetgo.security.util.ByteUtil.xorBytes;

public class SaltGeneratorCryptoBridge implements SaltGenerator {
  private final Crypto crypto;
  private final int    rndLength;


  public SaltGeneratorCryptoBridge(Crypto crypto) {
    this(crypto, 32);
  }

  public SaltGeneratorCryptoBridge(Crypto crypto, int rndLength) {
    this.crypto    = crypto;
    this.rndLength = rndLength;
  }

  @Override
  public String generateSalt(String str) {
    byte[] beginBytes = (str == null ? "" : str).getBytes(StandardCharsets.UTF_8);

    byte[] rndBytes = new byte[rndLength];
    crypto.rnd().nextBytes(rndBytes);

    byte[] sourceBytes = xorBytes(rndBytes, copyToLength(beginBytes, rndBytes.length));

    byte[] encryptedSourceBytes = crypto.encryptBlock(sourceBytes);

    byte[] resultBytes = new byte[rndBytes.length + encryptedSourceBytes.length];

    System.arraycopy(rndBytes, 0, resultBytes, 0, rndLength);
    System.arraycopy(encryptedSourceBytes, 0, resultBytes, rndLength, encryptedSourceBytes.length);

    String salt = Base64.getEncoder().encodeToString(resultBytes);
    return salt.replace('/', '$').replace('+', '~').replace('=', '@');
  }

  @Override
  public boolean validateSalt(String str, String salt) {

    if (salt == null) {
      return false;
    }

    String saltBase64 = salt.replace('$', '/').replace('~', '+').replace('@', '=');

    byte[] resultBytes;
    try {
      resultBytes = Base64.getDecoder().decode(saltBase64);
    } catch (Exception e) {
      return false;
    }

    if (resultBytes.length <= rndLength) {
      return false;
    }

    byte[] rndBytes             = new byte[rndLength];
    byte[] encryptedSourceBytes = new byte[resultBytes.length - rndLength];

    System.arraycopy(resultBytes, 0, rndBytes, 0, rndLength);
    System.arraycopy(resultBytes, rndLength, encryptedSourceBytes, 0, encryptedSourceBytes.length);

    byte[] sourceBytes;
    try {
      sourceBytes = crypto.decryptBlock(encryptedSourceBytes);
    } catch (Exception e) {
      return false;
    }

    byte[] beginBytes   = (str == null ? "" : str).getBytes(StandardCharsets.UTF_8);
    byte[] sourceBytes2 = xorBytes(rndBytes, copyToLength(beginBytes, rndBytes.length));

    return Arrays.equals(sourceBytes, sourceBytes2);
  }
}
