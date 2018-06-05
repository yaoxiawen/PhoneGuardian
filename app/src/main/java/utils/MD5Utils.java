package utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Utils {
    public static String encode(String s){
        MessageDigest instance = null;
        try {
            instance = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] digest = instance.digest(s.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : digest){
            int i = b & 0xff;
            String hexString = Integer.toHexString(i);
            if (hexString.length()<2){
                hexString = "0"+hexString;
            }
            sb.append(hexString);
        }
        return sb.toString();
    }
}
