package com.xiaomi.mimobile.milpatest;

public class TextUtil {


    public static byte[] hexStringToBytes(String hex) {
        byte[] data = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            data[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
        }
        return data;
    }

    public static byte[] decStringToBytes(String dec) {
        byte[] data = new byte[dec.length() / 2];
        for (int i = 0; i < dec.length(); i += 2) {
            data[i / 2] = (byte) Integer.parseInt(dec.substring(i, i + 2), 10);
        }
        return data;
    }

    public static String bytesToHexString(byte[] bytes) {
        return bytesToHexString(bytes, 0);
    }

    public static String bytesToHexString(byte[] bytes, int start) {
        return bytesToHexString(bytes, start, bytes.length);
    }

    public static String bytesToHexString(byte[] bytes, int start, int end) {
        if (start > end || end > bytes.length) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < end; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

}
