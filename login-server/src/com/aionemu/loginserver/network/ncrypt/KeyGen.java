package com.aionemu.loginserver.network.ncrypt;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.RSAKeyGenParameterSpec;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import com.aionemu.commons.utils.Rnd;

/**
 * Key generator. It generates keys or keyPairs for Blowfish and RSA
 * 
 * @author -Nemesiss-
 */
public class KeyGen {

	private static KeyGenerator blowfishKeyGen;
	private static final EncryptedRSAKeyPair[] encryptedRSAKeyPairs = new EncryptedRSAKeyPair[10];

	public static void init() {
		try {
			blowfishKeyGen = KeyGenerator.getInstance("Blowfish");
			KeyPairGenerator rsaKeyPairGenerator = KeyPairGenerator.getInstance("RSA");
			RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(1024, RSAKeyGenParameterSpec.F4);
			rsaKeyPairGenerator.initialize(spec);
			for (int i = 0; i < encryptedRSAKeyPairs.length; i++) {
				encryptedRSAKeyPairs[i] = new EncryptedRSAKeyPair(rsaKeyPairGenerator.generateKeyPair());
			}
		} catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
			throw new Error(e);
		}
	}

	public static SecretKey generateBlowfishKey() {
		return blowfishKeyGen.generateKey();
	}

	public static EncryptedRSAKeyPair getEncryptedRSAKeyPair() {
		return Rnd.get(encryptedRSAKeyPairs);
	}
}
