package models;

import play.Logger;
import play.db.ebean.Model;

import java.io.*;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;


/**
 * Created by Riboni1989 on 03/04/14.
 */
public class SecurityInfoShare extends Model {

    private PublicKey publicKey;
    private PrivateKey privateKey;
    private String algorithm = "RSA";

    public boolean createKey(){
        try{
            String saveKey = null;
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(algorithm);
            keyPairGenerator.initialize(4096);
            KeyPair keyPair = keyPairGenerator.genKeyPair();

            byte[] pubKey = keyPair.getPublic().getEncoded();
            saveKey = new String(pubKey, "ISO-8859-1");

            File file = new File("/Users/Riboni1989/Desktop/Lavoro/LucaG/authentication/files/publicKey.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(saveKey);
            bw.close();

            byte[] privateKey = keyPair.getPrivate().getEncoded();
            saveKey = new String(privateKey, "ISO-8859-1");

            file = new File("/Users/Riboni1989/Desktop/Lavoro/LucaG/authentication/files/privateKey.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            fw = new FileWriter(file.getAbsoluteFile());
            bw = new BufferedWriter(fw);
            bw.write(saveKey);
            bw.close();

        }
        catch(Exception e){
            Logger.error("Error in creating keys");
            return false;
        }
        return true;
    }

    public boolean loadKey(){
        int value =0;
        String key= new String();
        try{
            File file = new File("/Users/Riboni1989/Desktop/Lavoro/LucaG/authentication/files/publicKey.txt");
            if (!file.exists()) {
                Logger.error("publicKey file dosn't exists");
                return false;
            }
            FileReader fr = new FileReader(file.getAbsoluteFile());
            BufferedReader br = new BufferedReader(fr);

            while((value = br.read()) != -1){
                char c = (char)value;
                key += c;
            }
            br.close();

            this.publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(key.getBytes("ISO-8859-1")));

            key= new String();

            file = new File("/Users/Riboni1989/Desktop/Lavoro/LucaG/authentication/files/privateKey.txt");
            if (!file.exists()) {
                Logger.error("publicKey file dosn't exists");
                return false;
            }
            fr = new FileReader(file.getAbsoluteFile());
            br = new BufferedReader(fr);

            while((value = br.read()) != -1){
                char c = (char)value;
                key += c;
            }
            br.close();

            this.privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(key.getBytes("ISO-8859-1")));
        }
        catch(Exception e){
            Logger.error("Error in retrieving keys");
        }
        return true;
    }

    public boolean setSpecificPublicKey(String key){
        try{
        this.publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(key.getBytes("ISO-8859-1")));
        }
        catch(Exception e){
            Logger.error("Error during setting specific public key");
            return false;
        }
        return true;
    }

    public String getPublicKey(){
        String result = null;
        try{
            result = new String( this.publicKey.getEncoded(), "ISO-8859-1");
        }
        catch(Exception e){
            Logger.error("Error in getting public key");
            return "no key available";
        }
        return result;
    }

    public String encrypt(String text) {
        byte[] cipherText;
        String result = null;
        try {
            final Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, this.publicKey);
            cipherText = cipher.doFinal(text.getBytes());
            result =new String(cipherText, "ISO-8859-1");
        } catch (Exception e) {
            Logger.error("Error in encrypting message");
            return "error in encryption";
        }
        return result;
    }

    public String decrypt(String text) {
        try {
            byte[] encoded = text.getBytes("ISO-8859-1");
            final Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, this.privateKey);
            byte[] decrypted = cipher.doFinal(encoded);
            text =new String(decrypted, "ISO-8859-1");
        } catch (Exception e) {
            Logger.error("Error in decrypting message");
            return "error in decryption";
        }
        return text;
    }
}
