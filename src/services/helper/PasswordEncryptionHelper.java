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
 * <b>This code is not my own</b>. It borrows heavily from
 * https://gist.github.com/jtan189/3804290
 *
 */
public class PasswordEncryptionHelper {
	private static final String ENCRYPTION_ALGORITHM = "PBKDF2WithHmacSHA512";

	private static final int BYTES_IN_SALT = 512;
	private static final int BYTES_IN_HASH = 512;
	private static final int ITERATIONS = 5000;

	private static final int SALT_INDEX = 0;
	private static final int ITERATION_INDEX = 1;
	private static final int HASH_INDEX = 2;

	private PasswordEncryptionHelper() {
		// Uninstantiable...
	}

	/**
	 * Takes in a given password and encrypts it using the
	 * "PBKDF2WithHmacSHA512" algorithm
	 * 
	 * @param password
	 *            The password to encrypt
	 * 
	 * @return A colon-delimited String containing the salt, iterations, and
	 *         hashed password
	 */
	public static String encryptPassword(char[] password) throws NoSuchAlgorithmException, InvalidKeySpecException {
		// Generate a random salt
		SecureRandom random = new SecureRandom();
		byte[] salt = new byte[BYTES_IN_SALT];
		random.nextBytes(salt);

		// Create the hashed password
		byte[] hash = createHash(password, salt, ITERATIONS, BYTES_IN_HASH);

		// Return the salt, iterations, and hashed password in a colon delimited
		// String
		return toHex(salt) + ":" + ITERATIONS + ":" + toHex(hash);
	}

	/**
	 * Given a password, salt, iteration #, and byte #, returns a byte array
	 * containing the encrypted password
	 * 
	 * @param password
	 *            The password to encrypt
	 * @param salt
	 *            The salt to use to encrypt the password
	 * @param iterations
	 *            The iteration count
	 * @param bytes
	 *            The number of bytes to use for the encrypted password
	 *            (key_length/8)
	 * 
	 * @return A byte array containing the encrypted password
	 */
	private static byte[] createHash(char[] password, byte[] salt, int iterations, int bytes)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		PBEKeySpec keySpec = new PBEKeySpec(password, salt, iterations, bytes * 8);
		SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(ENCRYPTION_ALGORITHM);

		return secretKeyFactory.generateSecret(keySpec).getEncoded();
	}

	/**
	 * Takes in a non-encrypted password and checks if it equals a given
	 * encrypted password
	 * 
	 * @param password
	 *            A non-encrypted password
	 * @param goodHash
	 *            An encrypted password
	 * 
	 * @return <code>true</code> if the non-encrypted password, when encrypted,
	 *         is equal to the encrypted password
	 */
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

	/**
	 * Checks if two byte arrays are equal
	 * 
	 * @param a
	 *            The first byte array
	 * @param b
	 *            The second byte array
	 * 
	 * @return <code>true</code> if the byte array's contents are 100% equal,
	 *         otherwise <code>false</code>
	 */
	private static boolean slowEquals(byte[] a, byte[] b) {
		int diff = a.length ^ b.length;

		for (int i = 0; i < a.length && i < b.length; i++) {
			diff |= a[i] ^ b[i];
		}

		return diff == 0;
	}

	/**
	 * Converts hex to a byte array
	 * 
	 * @param hex
	 *            The hex to convert
	 * 
	 * @return The byte array equivalent of the given hex value
	 */
	private static byte[] fromHex(String hex) {
		byte[] binary = new byte[hex.length() / 2];
		for (int i = 0; i < binary.length; i++) {
			binary[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
		}
		return binary;
	}

	/**
	 * Converts a given byte array to hex
	 * 
	 * @param array
	 *            The byte array to convert to hex
	 * @return The hex equivalent of the given byte array
	 */
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
