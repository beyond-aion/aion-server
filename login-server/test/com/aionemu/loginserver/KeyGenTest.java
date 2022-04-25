package com.aionemu.loginserver;

import java.security.GeneralSecurityException;
import java.security.KeyPairGenerator;
import java.security.spec.RSAKeyGenParameterSpec;

import javax.crypto.Cipher;

import org.junit.jupiter.api.Test;

import com.aionemu.loginserver.network.ncrypt.EncryptedRSAKeyPair;
import com.aionemu.loginserver.network.ncrypt.KeyGen;

/**
 * This test is for KeyGen initialization and performance checking
 */
public class KeyGenTest {

	/**
	 * A test for keygen init
	 */
	@Test
	public void testKeyGenInit() {
		try {
			KeyGen.init();
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testRSAPreInit() throws Exception {
		EncryptedRSAKeyPair[] RSAKeyPairs = new EncryptedRSAKeyPair[10];

		KeyPairGenerator rsaKeyPairGenerator = KeyPairGenerator.getInstance("RSA");

		RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(1024, RSAKeyGenParameterSpec.F4);

		rsaKeyPairGenerator.initialize(spec);

		for (int i = 0; i < 10; i++) {
			RSAKeyPairs[i] = new EncryptedRSAKeyPair(rsaKeyPairGenerator.generateKeyPair());
		}

		long t1 = System.currentTimeMillis();
		for (int i = 0; i < 10; i++) {
			Cipher pRsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
			pRsaCipher.init(Cipher.DECRYPT_MODE, RSAKeyPairs[i].getRSAKeyPair().getPrivate());
		}
		long t2 = System.currentTimeMillis();
		System.out.println("RSA init time: " + (t2 - t1));

		byte[] data = new byte[128];

		t1 = System.currentTimeMillis();
		for (int i = 0; i < 10; i++) {
			Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
			rsaCipher.init(Cipher.DECRYPT_MODE, RSAKeyPairs[0].getRSAKeyPair().getPrivate());
			rsaCipher.doFinal(data, 0, 128);
		}
		t2 = System.currentTimeMillis();
		System.out.println("RSA decryption time: " + (t2 - t1));

	}

}
