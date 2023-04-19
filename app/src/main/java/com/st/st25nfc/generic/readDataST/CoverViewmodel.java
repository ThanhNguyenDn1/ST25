package com.st.st25nfc.generic.readDataST;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.lifecycle.ViewModel;

import com.google.android.material.snackbar.Snackbar;
import com.st.st25nfc.R;
import com.st.st25nfc.generic.ReadFragmentActivity;
import com.st.st25nfc.generic.util.NumberUtils;
import com.st.st25sdk.Helper;
import com.st.st25sdk.NFCTag;
import com.st.st25sdk.STLog;
import com.st.st25sdk.type2.Type2Tag;
import com.st.st25sdk.type4a.Type4Tag;
import com.st.st25sdk.type5.Type5Tag;

public class CoverViewmodel extends ViewModel {


    public String getDexFromBuffer(int indexBuffers, int countByte, byte[] mBuffer){
        int startIndex=indexBuffers*4+1;
        String data="";
        for(int i =startIndex;i<startIndex+countByte; i++){

           String datahex= Helper.convertByteToHexString(mBuffer[i]).toUpperCase();
            data=data.concat(NumberUtils.hexToDec(datahex).toString());
        }
        return data;
    }


}
