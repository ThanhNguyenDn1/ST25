package com.st.st25nfc.generic.readDataST;

import static com.st.st25sdk.MultiAreaInterface.AREA1;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.st.st25nfc.R;
import com.st.st25nfc.databinding.ActivityCovertBinding;
import com.st.st25nfc.generic.ReadFragmentActivity;
import com.st.st25nfc.generic.STFragmentActivity;
import com.st.st25nfc.generic.util.UIHelper;
import com.st.st25sdk.MultiAreaInterface;
import com.st.st25sdk.NFCTag;
import com.st.st25sdk.STException;
import com.st.st25sdk.STLog;
import com.st.st25sdk.type2.Type2Tag;
import com.st.st25sdk.type4a.FileControlTlvType4;
import com.st.st25sdk.type4a.STType4Tag;
import com.st.st25sdk.type4a.Type4Tag;
import com.st.st25sdk.type5.STType5Tag;
import com.st.st25sdk.type5.Type5Tag;

import java.util.ArrayList;

public class CovertActivity extends STFragmentActivity {
    private ActivityCovertBinding mBinding;
    private CoverViewmodel mViewmodel;

    //old

    // Set here the Toolbar to use for this activity
    private int toolbar_res = R.menu.toolbar_empty;

    // The data are now read by Byte but we will still format the display by raw of 4 Bytes
    private final int NBR_OF_BYTES_PER_RAW = 4;

    private int mStartAddress;
    private int mNumberOfBytes;

    private static final String TAG = "ReadFragmentActivity";
    private ContentViewAsync mContentView;
    private boolean mIsAreaProtectedInWrite;
    private boolean mIsAreaProtectedInRead;


    // For type 4 read in case of several area
    // Default value
    private int mAreaId = AREA1;
    private boolean mUnitInBytes;

    private byte[] mReadPassword;
    private byte[] mWritePassword;
    private byte[] mBuffer;


    enum ActionStatus {
        ACTION_SUCCESSFUL, ACTION_FAILED, TAG_WRITE_PROTECTED, AREA_PASSWORD_NEEDED, TAG_NOT_IN_THE_FIELD
    }

    ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityCovertBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        setData();
        setView();
        handlerEvent();
    }

    private void handlerEvent() {
        mBinding.icdReadWrite.btnRead.setOnClickListener(view -> {
            if (startSmartTagReadGetParametersFromUI()) {
                startSmartTagRead();
            }
        });
        mBinding.icdLoadUpdate.btnLoad.setOnClickListener(view -> {
            loadDataView();
        });

        mBinding.icdReadWrite.btnWrite.setOnClickListener(view -> {
            writeData();
        });
    }

    private void writeData() {
        getViewMain();


        //write
        AsyncTaskWriteDataMessage mTaskWriteDataMessage = new AsyncTaskWriteDataMessage(getTag(),mBuffer,0);
        mTaskWriteDataMessage.execute();
    }

    private void loadDataView() {
        setViewMain();
        setViewRTC();
        setView3G();
        setViewSensor();



    }

    private void setViewSensor() {
        mBinding.icdSenserSettings.edtIntervalGetPressure.setText(mViewmodel.getDexFromBuffer(28, mBuffer, 0, 16));
        mBinding.icdSenserSettings.edtHighPressureAlarm.setText(mViewmodel.getDexFromBuffer(31, mBuffer, 16, 23));
        mBinding.icdSenserSettings.edtLowPressureAlarm.setText(mViewmodel.getDexFromBuffer(31, mBuffer, 8, 15));
        mBinding.icdSenserSettings.edtIntervalCheckPressureAlarm.setText(mViewmodel.getDexFromBuffer(38, mBuffer, 18, 31));
        mBinding.icdSenserSettings.edtROCPulseCounter.setText(mViewmodel.getDexFromBuffer(31, mBuffer, 0, 7));
        mBinding.icdSenserSettings.edtROCPulseInterval.setText(mViewmodel.getDexFromBuffer(38, mBuffer, 4, 17));

    }

    private void setView3G() {
        mBinding.icd3GSettings.edtEnable.setText(mViewmodel.getDexFromBuffer(15, mBuffer, 0));
        mBinding.icd3GSettings.edtInterval.setText(mViewmodel.getDexFromBuffer(15, mBuffer, 17, 29));
        mBinding.icd3GSettings.edtPort.setText(mViewmodel.getDexFromBuffer(15, mBuffer, 1, 16));
        mBinding.icd3GSettings.edtServer.setText(mViewmodel.getDexFromBuffer(15, mBuffer, 1, 16));

        String mobilePhones[] = {"Vietel", "Mobifone", "Vietnammobile", "Viettel Data"};
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, mobilePhones);
        adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        mBinding.icd3GSettings.spnAPN.setAdapter(adapter);
        mBinding.icd3GSettings.spnAPN.setSelection(Integer.parseInt(mViewmodel.getDexFromBuffer(15, mBuffer, 30, 31)));


    }

    private void setViewRTC() {
        mBinding.icdRTCSettings.edtAMPM.setText(mViewmodel.getDexFromBuffer(2, mBuffer, 21));
        mBinding.icdRTCSettings.edt1224.setText(mViewmodel.getDexFromBuffer(2, mBuffer, 21));//TODO
        mBinding.icdRTCSettings.edtWeek.setText(mViewmodel.getDexFromBuffer(2, mBuffer, 0,2));
        mBinding.icdRTCSettings.edtHour.setText(mViewmodel.getDexFromBuffer(2, mBuffer, 3,7));
        mBinding.icdRTCSettings.edtMinute.setText(mViewmodel.getDexFromBuffer(2, mBuffer, 8,13));
        mBinding.icdRTCSettings.edtSecond.setText(mViewmodel.getDexFromBuffer(2, mBuffer, 14,19));
        mBinding.icdRTCSettings.edtDay.setText(mViewmodel.getDexFromBuffer(1, mBuffer, 11,15));
        mBinding.icdRTCSettings.edtMonth.setText(mViewmodel.getDexFromBuffer(1, mBuffer, 7,10));
        mBinding.icdRTCSettings.edtYear.setText(mViewmodel.getDexFromBuffer(1, mBuffer, 0,6));
        mBinding.icdRTCSettings.edtYear.setText(mViewmodel.getDexFromBuffer(1, mBuffer, 0,6));
    }

    private void setViewMain() {
        mBinding.icdMainSettings.edtDefaultIDYear.setText(mViewmodel.getDexFromBuffer(4, mBuffer, 0,7));
        mBinding.icdMainSettings.edtDefaultIDMonth.setText(mViewmodel.getDexFromBuffer(3, mBuffer,24,31));
        mBinding.icdMainSettings.edtDefaultID.setText(mViewmodel.getDexFromBuffer(3,  mBuffer,0,23));
        mBinding.icdMainSettings.edtCountry.setText(mViewmodel.getDexFromBuffer(39,  mBuffer,0,7));
        mBinding.icdMainSettings.edtHardware.setText(mViewmodel.getDexFromBuffer(39,  mBuffer,24,31));
        mBinding.icdMainSettings.edtFirmWare.setText(mViewmodel.getDexFromBuffer(39,  mBuffer,8,15));
        mBinding.icdMainSettings.edtSW.setText(mViewmodel.getDexFromBuffer(39,  mBuffer,16,23));
        mBinding.icdMainSettings.edtCustomID.setText(mViewmodel.getDexFromBuffer(13,mBuffer, 0, 19));//TODO
        mBinding.icdMainSettings.edtType.setText(mViewmodel.getDexFromBuffer(13,mBuffer, 20, 23));//TODO
        mBinding.icdMainSettings.edtPulseInput1.setText(mViewmodel.getDexFromBuffer(0, mBuffer,0));
        mBinding.icdMainSettings.edtPulseInput2.setText(mViewmodel.getDexFromBuffer(1, mBuffer,1));
        mBinding.icdMainSettings.edtPulseM3.setText(mViewmodel.getDexFromBuffer(0,mBuffer,3, 16));
        mBinding.icdMainSettings.edtDigit.setText(mViewmodel.getDexFromBuffer(0,mBuffer,17, 20));
        mBinding.icdMainSettings.edtSVD.setText(mViewmodel.getDexFromBuffer(0, mBuffer, 26,31));
        mBinding.icdMainSettings.edtDecimal.setText(mViewmodel.getDexFromBuffer(14, mBuffer, 23,24));
        mBinding.icdMainSettings.edtPressure.setText(mViewmodel.getDexFromBuffer(14, mBuffer, 2));
        mBinding.icdMainSettings.edtAction.setText("");//TODO
        mBinding.icdMainSettings.edtBatVolt.setText(mViewmodel.getDexFromBuffer(12, mBuffer, 0,15));
    }

    private void getViewMain() {
        mBuffer = mViewmodel.setDataToBuffer(4, mBuffer, false, mBinding.icdMainSettings.edtDefaultIDYear.getText().toString(), 0, 7);
        mBuffer = mViewmodel.setDataToBuffer(3, mBuffer, false, mBinding.icdMainSettings.edtDefaultIDMonth.getText().toString(), 24, 31);
        mBuffer = mViewmodel.setDataToBuffer(3, mBuffer, false, mBinding.icdMainSettings.edtDefaultID.getText().toString(), 0, 23);
        mBuffer = mViewmodel.setDataToBuffer(39, mBuffer, false, mBinding.icdMainSettings.edtCountry.getText().toString(), 0, 7);
        mBuffer = mViewmodel.setDataToBuffer(39, mBuffer, false, mBinding.icdMainSettings.edtHardware.getText().toString(), 24, 31);
        mBuffer = mViewmodel.setDataToBuffer(39, mBuffer, false, mBinding.icdMainSettings.edtFirmWare.getText().toString(), 8, 15);
        mBuffer = mViewmodel.setDataToBuffer(39, mBuffer, false, mBinding.icdMainSettings.edtSW.getText().toString(), 16, 23);
        mBuffer = mViewmodel.setDataToBuffer(13, mBuffer, false, mBinding.icdMainSettings.edtCustomID.getText().toString(), 0, 19);
        mBuffer = mViewmodel.setDataToBuffer(13, mBuffer, false, mBinding.icdMainSettings.edtType.getText().toString(), 20, 23);
        mBuffer = mViewmodel.setDataToBuffer(0, mBuffer, true, mBinding.icdMainSettings.edtPulseInput1.getText().toString(), 0, 0);
        mBuffer = mViewmodel.setDataToBuffer(1, mBuffer, true, mBinding.icdMainSettings.edtPulseInput2.getText().toString(), 1, 1);
        mBuffer = mViewmodel.setDataToBuffer(0, mBuffer, false, mBinding.icdMainSettings.edtPulseM3.getText().toString(), 3, 16);
        mBuffer = mViewmodel.setDataToBuffer(0, mBuffer, false, mBinding.icdMainSettings.edtDigit.getText().toString(), 17, 20);
        mBuffer = mViewmodel.setDataToBuffer(0, mBuffer, false, mBinding.icdMainSettings.edtSVD.getText().toString(), 26, 31);
        mBuffer = mViewmodel.setDataToBuffer(14, mBuffer, false, mBinding.icdMainSettings.edtDecimal.getText().toString(), 23, 24);
        mBuffer = mViewmodel.setDataToBuffer(14, mBuffer, true, mBinding.icdMainSettings.edtPressure.getText().toString(), 2, 2);
        // mBuffer = mViewmodel.setDataToBuffer(14, mBuffer, true,   mBinding.icdMainSettings.edtAction.getText().toString(), 2,2);TODO
        mBuffer = mViewmodel.setDataToBuffer(12, mBuffer, false, mBinding.icdMainSettings.edtBatVolt.getText().toString(), 0, 15);

    }

    private void setView() {
    }

    private void setData() {
        mViewmodel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication())).get(CoverViewmodel.class);
        if (super.getTag() == null) {
            showToast(R.string.invalid_tag);
            goBackToMainActivity();
            return;
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        mUnitInBytes = false;


        // Retrieve parameters for start and UI configuration
        Intent mIntent = getIntent();
        // define the start address and numbers of bytes to read for display
        int startAddress = mIntent.getIntExtra("start_address", -1);
        int nbOfBytes = mIntent.getIntExtra("nb_of_bytes", -1);
        // define the data as an array of bytes to display
        byte[] data = mIntent.getByteArrayExtra("data");
        // define the file id for the display - Type4 Tags needed - replace start address and numbers of bytes
        int areaFileID = mIntent.getIntExtra("areaFileID", -1);
        if (areaFileID != -1) mAreaId = areaFileID;
        // information message
        String information = mIntent.getStringExtra("information");
        if (information == null) information = getString(R.string.hexadecimal_dump);

        // Manage the dump display for a Type4 Tag


        // Manage an hexa dump - no tag dependency
        if (data != null) {
            // UI setting
            configureUIItemsForHexaDumpOnly(information);
            // Displaying
            startDisplaying(data);
        } else if (areaFileID != -1 && getTag() instanceof Type4Tag) {
            // UI setting
            configureUIItemsForHexaDumpOnly(information);
            // start reading ...
            mAreaId = areaFileID;
            startType4ReadingAndDisplaying((Type4Tag) getTag(), areaFileID);
        } else if (startAddress >= 0 && nbOfBytes >= 0 && getTag() instanceof Type5Tag) { // manage the dump with start address and number of bytes - Type5
            mStartAddress = startAddress;
            mNumberOfBytes = nbOfBytes;
            // UI setting
            configureUIItemsForHexaDumpOnly(information);
            // inform user that a read will be performed
//            Snackbar snackbar = Snackbar.make(mChildView , "", Snackbar.LENGTH_LONG);
//            snackbar.setAction(getString(R.string.reading_x_bytes_starting_y_address,mNumberOfBytes,mStartAddress), this);
//
//            snackbar.setActionTextColor(getResources().getColor(R.color.white));
//            snackbar.show();
            // start reading ...
            startType5ReadingAndDisplaying(mStartAddress, mNumberOfBytes);
        } else if (startAddress >= 0 && nbOfBytes >= 0 && getTag() instanceof Type2Tag) { // manage the dump with start address and number of bytes - Type2
            mStartAddress = startAddress;
            mNumberOfBytes = nbOfBytes;
            // UI setting
            configureUIItemsForHexaDumpOnly(information);
            // inform user that a read will be performed
//            Snackbar snackbar = Snackbar.make(mChildView , "", Snackbar.LENGTH_LONG);
//            snackbar.setAction(getString(R.string.reading_x_bytes_starting_y_address,mNumberOfBytes,mStartAddress), this);
//
//            snackbar.setActionTextColor(getResources().getColor(R.color.white));
//            snackbar.show();
            // start reading ...
            startType2ReadingAndDisplaying(mStartAddress, mNumberOfBytes);
        } else {
            // default behaviour - user have to enter read parameters
            // Manage UI for Tag Type4
            if (getTag() instanceof Type4Tag) {
                // display layout for type4
                displayType4ReadSelectionParameters();
                fillType4SpinnerForSelection((Type4Tag) getTag());
            } else {
                displayType5ReadSelectionParameters();
            }
        }

    }


    private void fillType4SpinnerForSelection(Type4Tag tag) {
        ArrayList<String> stringArrayList = new ArrayList<String>();

        try {
            int numberOfFiles = tag.getNbrOfFiles();
            for (int i = 0; i < numberOfFiles; i++) {
                stringArrayList.add(getString(R.string.area_number_to_name) + (i + 1));
            }
        } catch (STException e) {
            e.printStackTrace();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, stringArrayList);
    }

    private void removeType4ReadSelectionParameters() {

    }

    private void displayType4ReadSelectionParameters() {

    }

    private void removeType5ReadSelectionParameters() {

    }

    private void displayType5ReadSelectionParameters() {

    }

    private void configureUIItemsForHexaDumpOnly(String information) {

    }

    private int getMemoryAreaSizeInBytes(Type4Tag myTag, int area) {
        int memoryAreaSizeInBytes = 0;
        try {
            if (myTag instanceof STType4Tag) {

                int fileId = UIHelper.getType4FileIdFromArea(area);
                FileControlTlvType4 controlTlv = ((STType4Tag) myTag).getCCFileTlv(fileId);

                memoryAreaSizeInBytes = controlTlv.getMaxFileSize();

            } else {
                if (myTag instanceof MultiAreaInterface) {
                    memoryAreaSizeInBytes = ((MultiAreaInterface) myTag).getAreaSizeInBytes(area);
                } else {
                    memoryAreaSizeInBytes = myTag.getMemSizeInBytes();
                }
            }
        } catch (STException e) {
            e.printStackTrace();
        }
        return memoryAreaSizeInBytes;
    }

    @Override
    public void onBackPressed() {

        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    class ContentViewAsync extends AsyncTask<Void, Integer, Boolean> {
        byte mBuffer[] = null;
        NFCTag mTag;
        // Default value
        int mArea = AREA1;

        public ContentViewAsync(NFCTag myTag, int myArea) {
            mTag = myTag;
            mArea = myArea;
        }

        public ContentViewAsync(byte[] buffer) {
            mBuffer = buffer;
        }

        private ContentViewAsync(NFCTag myTag, int myArea, byte[] buffer) {
            mTag = myTag;
            mArea = myArea;
            mBuffer = buffer;
        }

        protected Boolean doInBackground(Void... arg0) {
            if (mBuffer == null) {
                try {
                    if (UIHelper.isAType4Tag(mTag)) {
                        // Tag type 4
                        int size = getMemoryAreaSizeInBytes(((Type4Tag) mTag), mArea);
                        mNumberOfBytes = size;
                        mStartAddress = 0;

                        int fileId = UIHelper.getType4FileIdFromArea(mArea);

                        // inform user that a read will be performed
                        //snackBarUiThread();
                        if (mReadPassword != null && mTag instanceof STType4Tag) {
                            mBuffer = ((STType4Tag) mTag).readBytes(fileId, 0, size, mReadPassword);
                        } else {
                            mBuffer = ((Type4Tag) mTag).readBytes(fileId, 0, size);
                        }
                        mReadPassword = null;
                        int nbrOfBytesRead = 0;
                        if (mBuffer != null) {
                            nbrOfBytesRead = mBuffer.length;
                        }
                        if (nbrOfBytesRead != mNumberOfBytes) {
                            showToast(R.string.error_during_read_operation, nbrOfBytesRead);
                        }
                    } else if (UIHelper.isAType5Tag(mTag)) {
                        if (mArea == -1) {
                            mAreaId = getAreaIdFromAddressInBytesForType5Tag(mStartAddress);
                        }
                        if (mAreaId == -1) {
                            // An issue occured retrieving AreaId from Address
                            // Address is probably invalid
                            showToast(R.string.invalid_value);
                            return false;

                        } else {
                            // Type 5
                            mBuffer = getTag().readBytes(mStartAddress, mNumberOfBytes);
                            // Warning: readBytes() may return less bytes than requested
                            int nbrOfBytesRead = 0;
                            if (mBuffer != null) {
                                nbrOfBytesRead = mBuffer.length;
                            }
                            if (nbrOfBytesRead != mNumberOfBytes) {
                                showToast(R.string.error_during_read_operation, nbrOfBytesRead);
                            }
                        }
                    } else if (UIHelper.isAType2Tag(mTag)) {
                        if (mArea == -1) {
                            mAreaId = getAreaIdFromAddressInBytesForType2Tag(mStartAddress);
                        }
                        if (mAreaId == -1) {
                            // An issue occured retrieving AreaId from Address
                            // Address is probably invalid
                            showToast(R.string.invalid_value);
                            return false;

                        } else {
                            mBuffer = getTag().readBytes(mStartAddress, mNumberOfBytes);
                            // Warning: readBytes() may return less bytes than requested
                            int nbrOfBytesRead = 0;
                            if (mBuffer != null) {
                                nbrOfBytesRead = mBuffer.length;
                            }
                            if (nbrOfBytesRead != mNumberOfBytes) {
                                showToast(R.string.error_during_read_operation, nbrOfBytesRead);
                            }
                        }
                    } else {
                        // An issue occured retrieving AreaId from Address
                        // Tag type not yet handled
                        showToast(R.string.invalid_value);
                        return false;
                    }
                } catch (STException e) {
                    mReadPassword = null;

                    switch (e.getError()) {
                        case TAG_NOT_IN_THE_FIELD:
                            showToast(R.string.tag_not_in_the_field);
                            break;
                        case PASSWORD_NEEDED:
                        case ISO15693_BLOCK_IS_LOCKED:
                        case ISO15693_BLOCK_PROTECTED:
                        case WRONG_SECURITY_STATUS:
                            showToast(R.string.area_protected_in_read);
                            if (mTag instanceof Type5Tag) {
                                // Type 5
                                // Check that targeted bytes not in several areas
                                if (!isTargetedBytesInOneArea(mStartAddress, mNumberOfBytes)) {
                                    // display information message that write on two areas protected....
                                    return false;
                                }
                            }
                            mIsAreaProtectedInRead = true;
                            //showReadPasswordDialog(mAreaId);

                            break;
                        default:
                            showToast(R.string.Command_failed);
                    }
                    Log.e(TAG, e.getMessage());
                    return false;
                }

            } else {
                // buffer already initialized by constructor - no need to read Tag.
                // Nothing to do
            }
            return true;
        }

        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (mBuffer != null && result == true) {
                CovertActivity.this.mBuffer = mBuffer;
                setDataView(mBuffer);
                Toast.makeText(CovertActivity.this, "" + mBuffer.length, Toast.LENGTH_SHORT).show();
                //TODO
            }

        }

        public void selfRestart() {
            mContentView = new ContentViewAsync(mTag, mAreaId, mBuffer);
            mContentView.execute();
        }
    }
// FOR PWD


    private int convertItemToBytesUnit(int value) {
        return value * NBR_OF_BYTES_PER_RAW;
    }


    private boolean startSmartTagReadGetParametersFromUI() {
        Boolean ret = true;
        try {
            if (mUnitInBytes) {
                //mStartAddress = Integer.parseInt(mStartAddressEditText.getText().toString());
            } else {
                int valInBlock = 0;//Integer.parseInt(mStartAddressEditText.getText().toString());
                mStartAddress = convertItemToBytesUnit(valInBlock);
            }
        } catch (Exception e) {
            STLog.e("Bad Start Address" + e.getMessage());
            showToast(R.string.bad_start_address);
            ret = false;
        }

        try {
            if (mUnitInBytes) {
                //mNumberOfBytes = Integer.parseInt(mNbrOfBytesEditText.getText().toString());
            } else {
                //int valInBlock = Integer.parseInt(mNbrOfBytesEditText.getText().toString());
                mNumberOfBytes = convertItemToBytesUnit(40);
            }
        } catch (Exception e) {
            STLog.e("Bad Numbers of Bytes" + e.getMessage());
            showToast(R.string.bad_number_of_bytes);
            ret = false;
        }

        // Hide Soft Keyboard
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }

        return ret;
    }

    private void startSmartTagRead() {
        if (getTag() instanceof Type5Tag) {
            startType5ReadingAndDisplaying(mStartAddress, mNumberOfBytes);
        } else if (getTag() instanceof Type2Tag) {
            startType2ReadingAndDisplaying(mStartAddress, mNumberOfBytes);
        } else if (getTag() instanceof Type4Tag) {
            // by defaut - read first area
            startType4ReadingAndDisplaying(getTag(), mAreaId);
        }
    }

    private int getAreaIdFromAddressInBytesForType5Tag(int address) {
        int ret = AREA1;
        if (getTag() instanceof MultiAreaInterface && getTag() instanceof STType5Tag) {
            MultiAreaInterface tag = (MultiAreaInterface) getTag();
            try {
                ret = tag.getAreaFromByteAddress(address);
            } catch (STException e) {
                ret = -1;
            }
        }
        return ret;
    }

    private int getAreaIdFromAddressInBytesForType2Tag(int address) {
        // only one AREA for this type of Tag for time being
        int ret = AREA1;
        return ret;
    }

    private boolean isTargetedBytesInOneArea(int address, int numberOfBytes) {
        boolean ret = true;
        if (getTag() instanceof MultiAreaInterface && getTag() instanceof STType5Tag) {
            MultiAreaInterface tag = (MultiAreaInterface) getTag();
            try {
                ret = (tag.getAreaFromByteAddress(address) == tag.getAreaFromByteAddress(address + numberOfBytes - 1));
            } catch (STException e) {
                // bad address or address and length over capacity
                ret = false;
            }
        }
        return ret;
    }

    private void startType5ReadingAndDisplaying(int startAddress, int numberOfBytes) {
        mStartAddress = startAddress;
        mNumberOfBytes = numberOfBytes;
        mContentView = new ContentViewAsync(getTag(), -1);
        mContentView.execute();

    }

    private void startType2ReadingAndDisplaying(int startAddress, int numberOfBytes) {
        mStartAddress = startAddress;
        mNumberOfBytes = numberOfBytes;
        mContentView = new ContentViewAsync(getTag(), -1);
        mContentView.execute();
    }

    private void startType4ReadingAndDisplaying(NFCTag tag, int area) {
        mContentView = new ContentViewAsync(tag, area);
        mContentView.execute();
    }

    private void startDisplaying(byte[] data) {
        mContentView = new ContentViewAsync(data);
        mContentView.execute();
    }


    public void onPause() {
        if (mContentView != null) mContentView.cancel(true);

        super.onPause();
    }

    public void setDataView(byte[] mBuffer) {
        mBinding.icdReadWrite.edtWaterIndex.setText(mViewmodel.getDexFromBuffer(6, mBuffer, 0,31));
        mBinding.icdReadWrite.edtPressure.setText(mViewmodel.getDexFromBuffer(29, mBuffer, 0,31));
    }

    protected class AsyncTaskWriteDataMessage extends AsyncTask<Void, Void, ActionStatus> {
        byte[] mMessageData;
        int mMemoryOffsetData;
        NFCTag mNFCTag;
        int mRetrievedAreaID = mAreaId;

        public AsyncTaskWriteDataMessage(NFCTag tag, byte[] data, int offset) {
            mMessageData = data;
            mMemoryOffsetData = offset;
            mNFCTag = tag;
        }

        @Override
        protected ActionStatus doInBackground(Void... param) {
            ActionStatus result = ActionStatus.ACTION_FAILED;

            if (mMessageData != null ) {
                UIHelper.displayCircularProgressBar(CovertActivity.this, getString(R.string.please_wait));
                try {
                    if (UIHelper.isAType4Tag(mNFCTag)) {
                        // Tag type 4
                        int fileId = UIHelper.getType4FileIdFromArea(mRetrievedAreaID);
                        if (mWritePassword != null && mNFCTag instanceof STType4Tag) {
                            ((STType4Tag) mNFCTag).writeBytes(fileId,mMemoryOffsetData, mMessageData, mWritePassword);
                        } else {
                            ((Type4Tag) mNFCTag).writeBytes(fileId,mMemoryOffsetData, mMessageData);
                        }
                        result = ActionStatus.ACTION_SUCCESSFUL;
                    } else if (UIHelper.isAType5Tag(mNFCTag)){
                        // Type 5
                        // retrieve the AreaID
                        mRetrievedAreaID = getAreaIdFromAddressInBytesForType5Tag(mStartAddress+mMemoryOffsetData);
                        if (mRetrievedAreaID == -1) {
                            // An issue occured retrieving AreaId from Address
                            // Address is probably invalid or write overlap capacity
                            //snackBarUiThreadWithMessage(getString(R.string.invalid_value));
                        } else {
                            mNFCTag.writeBytes(mStartAddress+mMemoryOffsetData, mMessageData);
                            result = ActionStatus.ACTION_SUCCESSFUL;
                        }
                    } else if (UIHelper.isAType2Tag(mNFCTag)){
                        // Type 2
                        // retrieve the AreaID
                        mRetrievedAreaID = getAreaIdFromAddressInBytesForType2Tag(mStartAddress+mMemoryOffsetData);
                        if (mRetrievedAreaID == -1) {
                            // An issue occured retrieving AreaId from Address
                            // Address is probably invalid or write overlap capacity
                            //snackBarUiThreadWithMessage(getString(R.string.invalid_value));
                        } else {
                            mNFCTag.writeBytes(mStartAddress+mMemoryOffsetData, mMessageData);
                            result = ActionStatus.ACTION_SUCCESSFUL;

                        }
                    } else {
                        // Tag not yet handled
                        //snackBarUiThreadWithMessage(getString(R.string.invalid_value));
                        Log.e(TAG, "Tag type not Handled");

                    }
                    if (result == ActionStatus.ACTION_SUCCESSFUL) {
                        mWritePassword = null;
                        UIHelper.invalidateCache(mNFCTag);
                    }
                    UIHelper.dismissCircularProgressBar();

                }catch (STException e) {
                    UIHelper.dismissCircularProgressBar();

                    switch (e.getError()) {
                        case WRONG_SECURITY_STATUS:
                        case ISO15693_BLOCK_PROTECTED:
                        case ISO15693_BLOCK_IS_LOCKED:
                            result = ActionStatus.TAG_WRITE_PROTECTED;
                            break;

                        case TAG_NOT_IN_THE_FIELD:
                            result = ActionStatus.TAG_NOT_IN_THE_FIELD;
                            break;

                        default:
                            e.printStackTrace();
                            break;
                    }
                }

            } else {
                // define error
            }
            return result;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(ActionStatus actionStatus) {
            UIHelper.dismissCircularProgressBar();
            switch (actionStatus) {
                case ACTION_SUCCESSFUL:
                    showToast(R.string.tag_updated);
                    startSmartTagRead();
                    break;

                case TAG_WRITE_PROTECTED:
                    showToast(R.string.write_permission);
                    if(UIHelper.isAType5Tag(mNFCTag))  {
                        // Type 5
                        // Check that targeted bytes not in several areas
                        if (!isTargetedBytesInOneArea(mStartAddress+mMemoryOffsetData, mMessageData.length)) {
                            // display information message that write on two areas protected....
                            return;
                        }
                    }
                    mIsAreaProtectedInWrite = true;
                    //showWritePasswordDialog(mRetrievedAreaID);
                    break;

                case TAG_NOT_IN_THE_FIELD:
                    showToast(R.string.tag_not_in_the_field);
                    break;

                case ACTION_FAILED:
                default:
                    showToast(R.string.command_failed);
                    break;
            }
        }
    }
}