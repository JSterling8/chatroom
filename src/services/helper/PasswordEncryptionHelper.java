package services.helper;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * 
 * Encrypts and decrypts passwords using the PBKDF2 algorithm.
 * 
 * <b>This code is not my own</b>.  It borrows heavily from https://gist.github.com/jtan189/3804290  
 *
 */
public class PasswordEncryptionHelper {
	private static final String ENCRYPTION_ALGORITHM = "PBKDF2WithHmacSHA1";

	private static final int BYTES_IN_SALT = 128;
	private static final int BYTES_IN_HASH = 128;
	private static final int ITERATIONS = 5000;

	private static final int SALT_INDEX = 0;
	private static final int ITERATION_INDEX = 1;
	private static final int HASH_INDEX = 2;

	private PasswordEncryptionHelper() {
	}

	public static String encryptPassword(char[] password) throws NoSuchAlgorithmException, InvalidKeySpecException {
		// Generate a random salt
		SecureRandom random = new SecureRandom();
		byte[] salt = new byte[BYTES_IN_SALT];
		random.nextBytes(salt);

		byte[] hash = createHash(password, salt, ITERATIONS, BYTES_IN_HASH);

		return toHex(salt) + ":" + ITERATIONS + ":" + toHex(hash);
	}

	private static byte[] createHash(char[] password, byte[] salt, int iterations, int bytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
		PBEKeySpec keySpec = new PBEKeySpec(password, salt, iterations, bytes * 8);
		SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(ENCRYPTION_ALGORITHM);

		return secretKeyFactory.generateSecret(keySpec).getEncoded();
	}

	public static boolean validatePassword(char[] password, String goodHash)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		// Decode the hash into its 3 parameters
		String[] params = goodHash.split(":");
		
		int iterations = Integer.parseInt(params[ITERATION_INDEX]);
		byte[] salt = fromHex(params[SALT_INDEX]);
		byte[] hash = fromHex(params[HASH_INDEX]);
		
		// Compute the hash of the provided password, using the same salt,
		// iteration count, and hash length
		byte[] testHash = createHash(password, salt, iterations, hash.length);

		return slowEquals(hash, testHash);
	}

	private static boolean slowEquals(byte[] a, byte[] b) {
		int diff = a.length ^ b.length;
		
		for (int i = 0; i < a.length && i < b.length; i++) {
			diff |= a[i] ^ b[i];
		}
		
		return diff == 0;
	}

	private static byte[] fromHex(String hex) {
		byte[] binary = new byte[hex.length() / 2];
		for (int i = 0; i < binary.length; i++) {
			binary[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
		}
		return binary;
	}

	private static String toHex(byte[] array) {
		BigInteger bi = new BigInteger(1, array);
		String hex = bi.toString(16);
		
		int paddingLength = (array.length * 2) - hex.length();
		
		if (paddingLength > 0) {
			return String.format("%0" + paddingLength + "d", 0) + hex;
		} else {
			return hex;
		}
	}
}
