package kz.greetgo.security.crypto;

import java.security.SecureRandom;

/**
 * <p>
 * Encrypt and decrypt data. Sign and verify signature of data. Random generation.
 * </p>
 * <p>
 * Key pair will generate automatically and saved into key storage on first using
 * </p>
 * <p>
 * To create implementation use {@link CryptoBuilder}
 * </p>
 */
public interface Crypto {
  /**
   * Encrypt data using public key
   *
   * @param bytes data to encrypt
   * @return encrypted data
   */
  byte[] encrypt(byte[] bytes);

  /**
   * Encrypt data using public key
   * <p>
   * Max number of bytes is limited by {@link CryptoSource#getBlockSize()}
   *
   * @param bytes data to encrypt
   * @return encrypted data
   */
  byte[] encryptBlock(byte[] bytes);

  /**
   * Decrypt encrypted data using private key
   * <p>
   * Encryption must be made by method {@link #encrypt(byte[])}
   *
   * @param encryptedBytes encrypted data
   * @return decrypted (original) data
   */
  byte[] decrypt(byte[] encryptedBytes);

  /**
   * Decrypt encrypted data using private key
   * <p>
   * Encryption must be made by method {@link #encryptBlock(byte[])}
   *
   * @param encryptedBytes encrypted data
   * @return decrypted (original) data
   */
  byte[] decryptBlock(byte[] encryptedBytes);

  /**
   * Sign data
   *
   * @param bytes the data to sign
   * @return signature
   */
  byte[] sign(byte[] bytes);

  /**
   * Verifies signature
   *
   * @param bytes     checking data
   * @param signature verifying signature
   * @return verification result: <code>true</code> - verification is OK, otherwise - verification wrong
   */
  boolean verifySignature(byte[] bytes, byte[] signature);

  /**
   * Gives security random generator
   *
   * @return security random generator
   */
  SecureRandom rnd();

  /**
   * Generate hash
   *
   * @param sourceBytes source data
   * @return hash
   */
  byte[] makeHash(byte[] sourceBytes);
}
