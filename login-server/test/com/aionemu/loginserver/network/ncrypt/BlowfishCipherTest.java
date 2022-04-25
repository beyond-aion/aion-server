package com.aionemu.loginserver.network.ncrypt;

import java.security.GeneralSecurityException;

import org.junit.jupiter.api.Test;

/**
 * Simple test to check blowfish cipher encryption and decryption results.
 * 
 * @author EvilSpirit
 */
public class BlowfishCipherTest {

	/**
	 * Initial key
	 */
	private static final byte[] INITIAL_KEY = { (byte) 0x6b, (byte) 0x60, (byte) 0xcb, (byte) 0x5b, (byte) 0x82, (byte) 0xce, (byte) 0x90, (byte) 0xb1,
		(byte) 0xcc, (byte) 0x2b, (byte) 0x6c, (byte) 0x55, (byte) 0x6c, (byte) 0x6c, (byte) 0x6c, (byte) 0x6c };

	/**
	 * A test for a new blowfish cipher
	 */
	@Test
	public void testNewBlowfishCipher() {
		BlowfishCipher cipher = new BlowfishCipher(INITIAL_KEY);
		String s[] = "BA 00 59 B2 52 B8 E5 73 49 AC 26 48 5B D7 A3 42 62 23 DD B1 FD 13 E9 10 DD DB 74 48 24 B9 0D 3C 52 18 B3 22 D4 E3 0C F0 49 77 14 F7 04 4E B1 8B B9 B2 A7 29 63 A0 03 D7 F2 83 B7 F3 C2 FC F7 82 08 D8 29 B1 5A 6A 92 BD 12 38 95 76 F5 BA 17 8E 8C AC B5 02 E8 D4 F9 75 75 BF 60 BE 13 25 1D 79 1C 1D 72 3C D9 95 E5 3A 4C 44 7C CA 37 76 0B 10 F5 9C 32 CD A0 D5 8D 6B EA 0E 62 D2 41 18 AE 4F C3 98 0C 7C 0E 16 76 94 BF 2B 51 B9 38 80 B5 4D 1B 2C 06 62 FA 80 D8 A4 0B 1C B8 3B 77 54 95 F9 A6 6E B5 28 C9 6E 93 76 9F 3B 78 8E 34 7D 99 67 2C 27 63 8F BF 93 40 B0 24 09"
			.split(" ");
		byte[] bytes = new byte[s.length];

		System.out.println("Length: " + bytes.length);

		for (int i = 0; i < s.length; i++) {
			bytes[i] = Integer.decode("0x" + s[i]).byteValue();
		}

		cipher.decipher(bytes);
		cipher.cipher(bytes);

		for (byte b : bytes) {
			System.out.println("> " + Integer.toHexString(b & 0xFF).toUpperCase());
		}
	}

	/**
	 * A test created for blowfish keygen performance
	 */
	@Test
	public void testBlowfishKeygenPerformance() {
		try {
			KeyGen.init();
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < 10; i++) {
			KeyGen.generateBlowfishKey();
		}
	}
}
