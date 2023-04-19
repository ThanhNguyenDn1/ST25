package com.st.st25nfc.generic.util;

public class NumberUtils {
    public static Integer hexToDec(String hex){
        return Integer.parseInt(hex, 16);
    }

    public static String decToHex(Integer dec){
        return Integer.toHexString(dec);
    }
}
