package emu.nebula.util;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.asn1.nist.NISTNamedCurves;
import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.agreement.ECDHBasicAgreement;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.*;

// Official Name: AeadTool
public class AeadHelper {
    private static final ThreadLocal<SecureRandom> random = new ThreadLocal<>() {
        @Override
        protected SecureRandom initialValue() {
            return new SecureRandom();
        }
    };
    
    public static final byte[] serverGarbleKey = "xNdVF^XTa6T3HCUATMQ@sKMLzAw&%L!3".getBytes(StandardCharsets.US_ASCII); // Global
    public static final byte[] serverMetaKey = "ma5Dn2FhC*Xhxy%c".getBytes(StandardCharsets.US_ASCII); // Global
                    
    public static byte[] generateBytes(int size) {
        byte[] iv = new byte[size];
        random.get().nextBytes(iv);
        return iv; 
    }
    
    // AES CBC

    public static byte[] encryptCBC(byte[] messageData) throws Exception {
        byte[] iv = generateBytes(16);
        
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        
        cipher.init(
            Cipher.ENCRYPT_MODE,
            new SecretKeySpec(serverMetaKey, "AES"),
            new IvParameterSpec(iv)
        );

        byte[] encrypted = cipher.doFinal(messageData);
        byte[] data = new byte[encrypted.length + iv.length];
        
        System.arraycopy(iv, 0, data, 0, iv.length);
        System.arraycopy(encrypted, 0, data, iv.length, encrypted.length);
        
        return data;
    }
    
    public static byte[] decryptCBC(byte[] messageData) throws Exception {
        byte[] iv = new byte[16];
        byte[] data = new byte[messageData.length - iv.length];
        
        System.arraycopy(messageData, 0, iv, 0, iv.length);
        System.arraycopy(messageData, iv.length, data, 0, data.length);
        
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        
        cipher.init(
            Cipher.DECRYPT_MODE,
            new SecretKeySpec(serverMetaKey, "AES"),
            new IvParameterSpec(iv)
        );

        return cipher.doFinal(data);
    }
    
    // AES GCM
    
    public static byte[] encryptGCM(byte[] messageData, byte[] key) throws Exception {
        byte[] iv = generateBytes(12);
        
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        
        cipher.init(
            Cipher.ENCRYPT_MODE, 
            new SecretKeySpec(key, "AES"), 
            new GCMParameterSpec(128, iv)
        );
        cipher.updateAAD(iv);

        byte[] encrypted = cipher.doFinal(messageData);
        byte[] data = new byte[encrypted.length + iv.length];
        
        System.arraycopy(iv, 0, data, 0, iv.length);
        System.arraycopy(encrypted, 0, data, iv.length, encrypted.length);
        
        return data;
    }
    
    public static byte[] decryptGCM(byte[] messageData, byte[] key) throws Exception {
        byte[] iv = new byte[12];
        byte[] data = new byte[messageData.length - iv.length];
        System.arraycopy(messageData, 0, iv, 0, iv.length);
        System.arraycopy(messageData, iv.length, data, 0, data.length);
        
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        
        cipher.init(
            Cipher.DECRYPT_MODE, 
            new SecretKeySpec(key, "AES"), 
            new GCMParameterSpec(128, iv)
        );
        cipher.updateAAD(iv);

        return cipher.doFinal(data);
    }
    
    // Chacha20
    
    public static byte[] encryptChaCha(byte[] messageData, byte[] key) throws Exception {
        byte[] iv = generateBytes(12);

        Cipher cipher = Cipher.getInstance("ChaCha20-Poly1305/None/NoPadding");
        
        cipher.init(
            Cipher.ENCRYPT_MODE, 
            new SecretKeySpec(key, "ChaCha20"),
            new IvParameterSpec(iv)
        );
        cipher.updateAAD(iv);
        
        byte[] encrypted = cipher.doFinal(messageData);
        byte[] data = new byte[encrypted.length + iv.length];
        
        System.arraycopy(iv, 0, data, 0, iv.length);
        System.arraycopy(encrypted, 0, data, iv.length, encrypted.length);
        
        return data;
    }
    
    public static byte[] decryptChaCha(byte[] messageData, byte[] key) throws Exception {
        byte[] iv = new byte[12];
        byte[] data = new byte[messageData.length - iv.length];
        System.arraycopy(messageData, 0, iv, 0, iv.length);
        System.arraycopy(messageData, iv.length, data, 0, data.length);

        Cipher cipher = Cipher.getInstance("ChaCha20-Poly1305/None/NoPadding");
        
        cipher.init(
            Cipher.DECRYPT_MODE, 
            new SecretKeySpec(key, "ChaCha20"),
            new IvParameterSpec(iv)
        );
        cipher.updateAAD(iv);
        
        return cipher.doFinal(data);
    }
    
    // XOR
    
    public static byte[] encryptBasic(byte[] messageData, byte[] key3) {
        byte[] data = new byte[messageData.length];
        System.arraycopy(messageData, 0, data, 0, data.length);
        
        for (int i = 0; i < data.length; i++) {
            data[i] ^= key3[i % key3.length];
            
            int b = data[i];
            
            byte v7 = (byte) (b << 1);
            byte v1 = (byte) ((b >> 7) & 0b00000001);
            
            data[i] = (byte) (v1 | v7);
            
            data[i] ^= data.length;
        }
        
        return data;
    }
    
    public static byte[] decryptBasic(byte[] messageData, byte[] key3) {
        byte[] data = new byte[messageData.length];
        System.arraycopy(messageData, 0, data, 0, data.length);
        
        for (int i = 0; i < data.length; i++) {
            data[i] ^= data.length;
            
            int b = data[i];
            
            byte v1 = (byte) (b << 7);
            byte v7 = (byte) ((b >> 1) & 0b01111111);
            
            data[i] = (byte) (v7 | v1);
            
            data[i] ^= key3[i % key3.length];
        }
        
        return data;
    }
    
    // ECDH
    
    public static AsymmetricCipherKeyPair generateECDHKEyPair() {
        var generator = new ECKeyPairGenerator();
        
        var p = NISTNamedCurves.getByName("P-256");
        var domainParams = new ECDomainParameters(p.getCurve(), p.getG(), p.getN(), p.getH());
        var genParams = new ECKeyGenerationParameters(domainParams, random.get());
        
        generator.init(genParams);
        return generator.generateKeyPair();
    }
    
    public static byte[] generateKey(byte[] clientPublic, byte[] serverPublic, byte[] serverPrivate) {
        // Setup
        byte[] ikm = new byte[32];
        byte[] salt = serverPublic;
        byte[] info = new byte[serverPublic.length];
        
        // Create info
        for (int i = 0; i < info.length; i++) {
            int c = clientPublic[i] & 0xff;
            int s = serverPublic[i] & 0xff;
            if (c > s) {
                s = (s << 1);
            } else {
                s = ((s >> 1) & 0b01111111);
            }
            info[i] = (byte) (s ^ c);
        }
        
        var sharedKey = calcECDHSharedKey(clientPublic, serverPrivate);
        int count = Math.min(sharedKey.length, 32);
        System.arraycopy(sharedKey, 0, ikm, 32 - count, count);
        
        // Generator
        var generator = new HKDFBytesGenerator(new SHA256Digest());
        var genParams = new HKDFParameters(ikm, salt, info);
        
        byte[] output = new byte[32];
        
        generator.init(genParams);
        generator.generateBytes(output, 0, output.length);
        
        return output;
    }
    
    public static byte[] calcECDHSharedKey(byte[] clientPublic, byte[] serverPrivate) {
        var p = SECNamedCurves.getByName("secp256r1");
        var domainParams = new ECDomainParameters(p.getCurve(), p.getG(), p.getN(), p.getH(), p.getSeed());
        
        var clientPoint = p.getCurve().decodePoint(clientPublic);
        var clientParams = new ECPublicKeyParameters(clientPoint, domainParams);
        
        var serverInteger = new BigInteger(serverPrivate);
        var serverParams = new ECPrivateKeyParameters(serverInteger, domainParams);
        
        var agreement = new ECDHBasicAgreement();
        agreement.init(serverParams);
        
        var result = agreement.calculateAgreement(clientParams);
        
        return getUnsignedByteArray(result);
    }

    public static byte[] getUnsignedByteArray(BigInteger b) {
        byte[] signedByteArray = b.toByteArray();
        byte[] unsignedByteArray;
        
        if (signedByteArray[0] == 0 && signedByteArray.length > 1) {
            // Remove the leading zero byte
            unsignedByteArray = new byte[signedByteArray.length - 1];
            System.arraycopy(signedByteArray, 1, unsignedByteArray, 0, unsignedByteArray.length);
        } else {
            // No leading zero byte to remove, or it's a negative number
            // which is not directly representable as an unsigned byte array without special handling
            unsignedByteArray = signedByteArray;
        }
        
        return unsignedByteArray;
    }
}
