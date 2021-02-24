package nc.noumea.mairie.webapps.core.tools.util;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Iterator;

// Classe importée depuis le code de la synchro-si.
// semble avoir été copié-collé depuis cette page: http://sloanseaman.com/wordpress/2011/08/11/pgp-encryptiondecryption-in-java/
//
public class PgpUtils {

	private static final Logger logger = LoggerFactory.getLogger(PgpUtils.class);

	public static PGPPublicKey readPublicKey(InputStream in) throws IOException, PGPException {
		in = org.bouncycastle.openpgp.PGPUtil.getDecoderStream(in);

		PGPPublicKeyRingCollection pgpPub = new PGPPublicKeyRingCollection(in);

		//
		// we just loop through the collection till we find a key suitable for encryption, in the real
		// world you would probably want to be a bit smarter about this.
		//
		PGPPublicKey key = null;

		//
		// iterate through the key rings.
		//
		Iterator<PGPPublicKeyRing> rIt = pgpPub.getKeyRings();

		while (key == null && rIt.hasNext()) {
			PGPPublicKeyRing kRing = rIt.next();
			Iterator<PGPPublicKey> kIt = kRing.getPublicKeys();
			while (key == null && kIt.hasNext()) {
				PGPPublicKey k = kIt.next();

				if (k.isEncryptionKey()) {
					key = k;
				}
			}
		}

		if (key == null) {
			throw new IllegalArgumentException("Can't find encryption key in key ring.");
		}

		return key;
	}

	public static void decryptFile(InputStream fileIn, OutputStream fileOut, InputStream key, char[] passPhrase) throws Exception {

		Security.addProvider(new BouncyCastleProvider());

		InputStream in = PGPUtil.getDecoderStream(fileIn);

		PGPObjectFactory pgpFactory = new PGPObjectFactory(in);
		PGPEncryptedDataList encData;

		Object o = pgpFactory.nextObject();

		if (o instanceof PGPEncryptedDataList) {
			encData = (PGPEncryptedDataList) o;
		} else {
			encData = (PGPEncryptedDataList) pgpFactory.nextObject();
		}

		Iterator<PGPPublicKeyEncryptedData> it = encData.getEncryptedDataObjects();
		PGPPrivateKey sKey = null;
		PGPPublicKeyEncryptedData pbe = null;

		while (sKey == null && it.hasNext()) {
			pbe = it.next();
			sKey = findSecretKey(key, pbe.getKeyID(), passPhrase);
		}

		if (sKey == null) {
			throw new IllegalArgumentException("Secret key for message not found.");
		}

		InputStream clear = pbe.getDataStream(sKey, "BC");

		PGPObjectFactory plainFact = new PGPObjectFactory(clear);

		Object message = plainFact.nextObject();

		if (message instanceof PGPCompressedData) {
			PGPCompressedData cData = (PGPCompressedData) message;
			PGPObjectFactory pgpFact = new PGPObjectFactory(cData.getDataStream());

			message = pgpFact.nextObject();

			PGPOnePassSignatureList p1 = (PGPOnePassSignatureList) message;

			// PGPOnePassSignature ops = p1.get(0);
			message = pgpFact.nextObject();
		}

		if (message instanceof PGPLiteralData) {
			PGPLiteralData ld = (PGPLiteralData) message;

			InputStream unc = ld.getInputStream();
			int ch;

			while ((ch = unc.read()) >= 0) {
				fileOut.write(ch);
			}
		} else if (message instanceof PGPOnePassSignatureList) {
			throw new PGPException("Encrypted message contains a signed message - not literal data.");
		} else {
			throw new PGPException("Message is not a simple encrypted file - type unknown.");
		}

		if (pbe.isIntegrityProtected()) {
			if (!pbe.verify()) {
				throw new PGPException("Message failed integrity check");
			}
		}

	}

	private static PGPPrivateKey findSecretKey(InputStream keyIn, long keyID, char[] pass) throws IOException, PGPException, NoSuchProviderException {
		PGPSecretKeyRingCollection pgpSec = new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(keyIn));

		PGPSecretKey pgpSecKey = pgpSec.getSecretKey(keyID);

		if (pgpSecKey == null) {
			logger.error("pas de private key");
			return null;
		}

		return pgpSecKey.extractPrivateKey(pass, "BC");
	}

	public static void encryptFile(OutputStream out, File file, PGPPublicKey encKey)
		throws IOException, NoSuchProviderException, PGPException {
		Security.addProvider(new BouncyCastleProvider());

		ByteArrayOutputStream bOut = new ByteArrayOutputStream();

		org.bouncycastle.openpgp.PGPUtil.writeFileToLiteralData(bOut,
			PGPLiteralData.BINARY, file);

		bOut.close();

		PGPEncryptedDataGenerator cPk = new PGPEncryptedDataGenerator(
			PGPEncryptedData.CAST5, true,
			new SecureRandom(), "BC");

		cPk.addMethod(encKey);

		byte[] bytes = bOut.toByteArray();

		OutputStream cOut = cPk.open(out, bytes.length);

		cOut.write(bytes);

		cOut.close();

		out.close();
	}
}