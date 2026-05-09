package com.auction.shared;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Deterministic SHA-256 password hashing shared between the client (hashes
 * before sending) and the server (stores/compares the hash only).
 *
 * <p>Plain-text passwords never leave the client process.
 */
public final class PasswordEncoder {

  private PasswordEncoder() {}

  /**
   * Returns the lower-case hex SHA-256 digest of {@code plainText}.
   *
   * @throws IllegalStateException if SHA-256 is unavailable (never happens on
   *     any standard JVM).
   */
  public static String hash(String plainText) {
    if (plainText == null) {
      throw new IllegalArgumentException("Password must not be null");
    }
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] bytes = digest.digest(plainText.getBytes(StandardCharsets.UTF_8));
      StringBuilder hex = new StringBuilder(bytes.length * 2);
      for (byte b : bytes) {
        hex.append(String.format("%02x", b));
      }
      return hex.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 algorithm unavailable", e);
    }
  }

  /** Returns {@code true} if {@code plainText} hashes to {@code storedHash}. */
  public static boolean matches(String plainText, String storedHash) {
    return storedHash != null && storedHash.equals(hash(plainText));
  }
}
