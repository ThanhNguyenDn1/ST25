package com.st.st25nfc.generic.readDataST;

import androidx.lifecycle.ViewModel;

public class CoverViewmodel extends ViewModel {
    public String getDexFromBuffer(int indexBuffers, byte[] mBuffer, int indexBit) {
        String bitsOfLock = "";
        for (int i = indexBuffers * 4 + 1; i <= indexBuffers * 4 + 4; i++) {
            byte b1 = mBuffer[i];
            String s1 = String.format("%8s", Integer.toBinaryString(b1 & 0xFF)).replace(' ', '0');
            bitsOfLock.concat(s1);
        }

        return bitsOfLock.charAt(indexBit) + "";
    }

    public String getDexFromBuffer(int indexBuffers, byte[] mBuffer, int indexBitStart, int indexBitEnd) {
        String bitsOfLock = "";
        for (int i = indexBuffers * 4 + 1; i <= indexBuffers * 4 + 4; i++) {
            byte b1 = mBuffer[i];
            String s1 = String.format("%8s", Integer.toBinaryString(b1 & 0xFF)).replace(' ', '0');
            bitsOfLock.concat(s1);
        }
        String data = bitsOfLock.substring(indexBitStart, indexBitEnd);
        return convertBinaryToDecimal(Long.parseLong(data)) + "";
    }


    private int convertBinaryToDecimal(long num) {
        int decimalNumber = 0, i = 0;
        long remainder;

        while (num != 0) {
            remainder = num % 10;
            num /= 10;
            decimalNumber += remainder * Math.pow(2, i);
            ++i;
        }

        return decimalNumber;
    }


}
