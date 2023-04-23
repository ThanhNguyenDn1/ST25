package com.st.st25nfc.generic.readDataST;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.st.st25sdk.Helper;

import org.spongycastle.util.encoders.Base64Encoder;
import org.spongycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;

public class CoverViewmodel extends ViewModel {
    public String getDexFromBuffer(int indexBuffers, byte[] mBuffer, int indexBit) {
        String bitsOfLock = "";
        for (int i = indexBuffers * 4; i < indexBuffers * 4 + 4; i++) {
            byte b1 = mBuffer[i];
            String s1 = String.format("%8s", Integer.toBinaryString(b1 & 0xFF)).replace(' ', '0');
            bitsOfLock = bitsOfLock.concat(s1);
        }

        return bitsOfLock.charAt(indexBit) + "";
    }

    public String getDexFromBuffer(int indexBuffers, byte[] mBuffer, int indexBitStart, int indexBitEnd) {

        String bitsOfLock = "";
        for (int i = indexBuffers * 4; i < indexBuffers * 4 + 4; i++) {
            byte b1 = mBuffer[i];
            String s1 = String.format("%8s", Integer.toBinaryString(b1 & 0xFF)).replace(' ', '0');
            bitsOfLock = bitsOfLock.concat(s1);
        }
        String data = bitsOfLock.substring(indexBitStart, indexBitEnd + 1);


        try {
            return convertBinaryToDecimal2(data) + "";
        } catch (Exception io) {
            Log.e("22222", "getDexFromBuffer: " + io);
            return "err";
        }

    }

    public String getDexFromBuffer(int indexBuffers, int indexBufferEnd, byte[] mBuffer) {

        String bitsOfLock = "";
        for (int i = indexBuffers * 4; i < indexBuffers * 4 + 4 * (indexBufferEnd - indexBuffers + 1); i++) {
            byte b1 = mBuffer[i];
            String s1 = String.format("%8s", Integer.toBinaryString(b1 & 0xFF)).replace(' ', '0');
            bitsOfLock = bitsOfLock.concat(s1);
        }

        try {
            return convertBinaryToDecimal2(bitsOfLock) + "";
        } catch (Exception io) {
            Log.e("22222", "getDexFromBuffer: " + io);
            return "err";
        }

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

    private int convertBinaryToDecimal(String num) {
        int decimalNumber = 0;
        int remainder = 0;
        for (int i = 0; i < num.length(); i++) {
            remainder = Integer.parseInt( num.charAt(i)+"") % 10;
            decimalNumber += remainder* Math.pow(2, (num.length()-1-i));
        }
        return decimalNumber;
       // 0 0 0 1 1 1
    }

    private String convertBinaryToDecimal2(String num) {
        int soDu= num.length()%8;
        String decimalNumber="";
        if (soDu != 0) {
            while (num.length()%8!=0){
                num = "0".concat(num);
            }
        }
        int i=7;
        while(i<num.length()){
            decimalNumber = decimalNumber.concat(Integer.parseInt(num.substring(i-7, i+1),2)+"");
            i+=8;
        }

        return Integer.parseInt(decimalNumber)+"";
        // 0 0 0 1 1 1
    }


    //data type dec or binary
    public byte[] setDataToBuffer(int indexBuffers, byte[] mBuffer, boolean isBinary, String data, int indexBitStart, int indexBitEnd) {
        byte[] buffer = mBuffer;
        try {
            String bitsOfLock = "";
            for (int i = indexBuffers * 4; i < indexBuffers * 4 + 4; i++) {
                byte b1 = mBuffer[i];
                String s1 = String.format("%8s", Integer.toBinaryString(b1 & 0xFF)).replace(' ', '0');
                bitsOfLock = bitsOfLock.concat(s1);
            }
            if (isBinary) {
                bitsOfLock = bitsOfLock.substring(0, indexBitStart) + data + bitsOfLock.substring(indexBitEnd + 1);
            }
            {
                int num = Integer.parseInt(data);
                String dataBitNew = "";
                int rem = 0;
                while (num != 0) {
                    rem = num % 2;
                    num /= 2;
                    dataBitNew = (rem + "").concat(dataBitNew);
                }
                while (dataBitNew.length() < indexBitEnd - indexBitStart + 1) {
                    dataBitNew = "0".concat(dataBitNew);
                }


                bitsOfLock = bitsOfLock.substring(0, indexBitStart) + dataBitNew + bitsOfLock.substring(indexBitEnd + 1);
            }
            int j = 0;
            for (int i = indexBuffers * 4; i < indexBuffers * 4 + 4; i++) {
                String subString = bitsOfLock.substring(j * 8, (j + 1) * 8);
                byte b1 = (byte) Integer.parseInt(subString, 2);
                mBuffer[i] = b1;
                j++;
            }
            return mBuffer;
        } catch (Exception e) {
            return buffer;
        }
    }


    public String getServer(int indexBlockStart, int indexBlockEnd, byte[] mBuffer) {
        byte[] data=new byte[(indexBlockEnd-indexBlockStart+1)*4];
        int j=0;
        for (int i = indexBlockStart * 4; i < indexBlockStart * 4 + 4 * (indexBlockEnd - indexBlockStart + 1); i++) {
            data[j]=mBuffer[i];
            ++j;
        }
        return new String(data, StandardCharsets.UTF_8);
    }

    public String getHex(int indexBlockStart, int indexBlockEnd, byte[] mBuffer) {
        byte[] data=new byte[(indexBlockEnd-indexBlockStart+1)*4];
        String dataHex="";
        int j=0;
        for (int i = indexBlockStart * 4; i < indexBlockStart * 4 + 4 * (indexBlockEnd - indexBlockStart + 1); i++) {
            data[j]=mBuffer[i];
            dataHex=dataHex.concat(Helper.convertByteToHexString(mBuffer[i]).toUpperCase());
            ++j;
        }
        return dataHex;
    }
}
