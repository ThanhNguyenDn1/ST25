/*
  * @author STMicroelectronics MMY Application team
  *
  ******************************************************************************
  * @attention
  *
  * <h2><center>&copy; COPYRIGHT 2017 STMicroelectronics</center></h2>
  *
  * Licensed under ST MIX_MYLIBERTY SOFTWARE LICENSE AGREEMENT (the "License");
  * You may not use this file except in compliance with the License.
  * You may obtain a copy of the License at:
  *
  *        http://www.st.com/Mix_MyLiberty
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied,
  * AND SPECIFICALLY DISCLAIMING THE IMPLIED WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  ******************************************************************************
*/

package com.st.st25nfc.generic.ndef;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.st.st25sdk.Helper;
import com.st.st25nfc.R;
import com.st.st25sdk.STException;
import com.st.st25sdk.ndef.BtRecord;
import com.st.st25sdk.ndef.NDEFMsg;

import java.util.ArrayList;
import java.util.Set;

public class NDEFBtFragment extends NDEFRecordFragment {

    final static String TAG = "NDEFBtFragment";


    private View mView;

    private BluetoothAdapter mBtAdapter = null;
    private ArrayAdapter<String> mBtArrayAdapter;
    Set<BluetoothDevice> mPairedDevices = null;

    private ArrayList<String> mDeviceListName;
    private ArrayList<String> mDeviceListMacAddr;

    private BtRecord mBtRecord;
    private int mAction;

    private EditText mDeviceNameEditText;
    private EditText mAddrByte5EditText;
    private EditText mAddrByte4EditText;
    private EditText mAddrByte3EditText;
    private EditText mAddrByte2EditText;
    private EditText mAddrByte1EditText;
    private EditText mAddrByte0EditText;
    private ListView mBoundedDevicesListView;


    public static NDEFBtFragment newInstance(Context context) {
        NDEFBtFragment f = new NDEFBtFragment();
        /* If needed, pass some argument to the fragment
        Bundle args = new Bundle();
        args.putInt("index", index);
        f.setArguments(args);
        */
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_ndef_bt, container, false);
        mView = view;

        Bundle bundle = getArguments();
        if (bundle == null) {
            Log.e(TAG, "Fatal error! Arguments are missing!");
            return null;
        }

        NDEFMsg ndefMsg = (NDEFMsg) bundle.getSerializable(NDEFRecordFragment.NDEFKey);
        int recordNbr = bundle.getInt(NDEFRecordFragment.RecordNbrKey);
        mBtRecord = (BtRecord) ndefMsg.getNDEFRecord(recordNbr);

        initFragmentWidgets();

        mAction = bundle.getInt(NDEFEditorFragment.EditorKey);
        if(mAction == NDEFEditorFragment.VIEW_NDEF_RECORD) {
            // We are displaying an existing record. By default it is not editable
            ndefRecordEditable(false);

        } else {
            // We are adding a new TextRecord or editing an existing record
            ndefRecordEditable(true);
        }

        return mView;
    }

    private void initFragmentWidgets() {
        mDeviceNameEditText = (EditText) mView.findViewById(R.id.ndef_fragment_bt_device_name);
        mAddrByte5EditText = (EditText) mView.findViewById(R.id.addrByte5EditText);
        mAddrByte4EditText = (EditText) mView.findViewById(R.id.addrByte4EditText);
        mAddrByte3EditText = (EditText) mView.findViewById(R.id.addrByte3EditText);
        mAddrByte2EditText = (EditText) mView.findViewById(R.id.addrByte2EditText);
        mAddrByte1EditText = (EditText) mView.findViewById(R.id.addrByte1EditText);
        mAddrByte0EditText = (EditText) mView.findViewById(R.id.addrByte0EditText);

        mBoundedDevicesListView = (ListView) mView.findViewById(R.id.list_view_bounded_device);

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        mDeviceListName = new ArrayList<String>();
        mDeviceListMacAddr = new ArrayList<String>();
        mDeviceListName.add(mBtAdapter.getName());
        mDeviceListMacAddr.add(mBtAdapter.getAddress());


        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    if (device.getType() == BluetoothDevice.DEVICE_TYPE_CLASSIC || device.getType() == BluetoothDevice.DEVICE_TYPE_DUAL) {
                        if (device.getName() != null) {
                            mDeviceListName.add(device.getName());
                            mDeviceListMacAddr.add(device.getAddress());
                        }
                    }
                } else {
                    if (device.getName() != null) {
                        mDeviceListName.add(device.getName());
                        mDeviceListMacAddr.add(device.getAddress());
                    }
                }
            }

            // add a new device
            mDeviceListName.add("New BT Device");
            mDeviceListMacAddr.add("00:11:22:33:44:55");

            mBtArrayAdapter = new ArrayAdapter<String>(getContext(), R.layout.list_item, mDeviceListName);

            mBoundedDevicesListView.setAdapter(mBtArrayAdapter);
            mBoundedDevicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                    try {
                        final String item = (String) parent.getItemAtPosition(position);

                        String deviceName = mDeviceListName.get(position);
                        mDeviceNameEditText.setText(deviceName);

                        String macAddr = mDeviceListMacAddr.get(position);
                        byte[] addr = convertBTAddressStringToByteArray(macAddr);
                        setUIBTAddress(addr);
                    } catch (STException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        setContent();
    }

    private byte[] convertBTAddressStringToByteArray(String btAddress) throws STException {
        byte[] addr = Helper.convertHexStringToByteArray(btAddress.replaceAll(":", ""));

        if ((addr == null) || (addr.length != 6)) {
            throw new STException(STException.STExceptionCode.CMD_FAILED);
        }

        return addr;
    }

    private String convertBTAddressByteArrayToString(byte[] btAddress) throws STException {
        if ((btAddress == null) || (btAddress.length != 6)) {
            throw new STException(STException.STExceptionCode.BAD_PARAMETER);
        }

        String txt = String.format("%02x", btAddress[0]).toUpperCase() + ":" +
                     String.format("%02x", btAddress[1]).toUpperCase() + ":" +
                     String.format("%02x", btAddress[2]).toUpperCase() + ":" +
                     String.format("%02x", btAddress[3]).toUpperCase() + ":" +
                     String.format("%02x", btAddress[4]).toUpperCase() + ":" +
                     String.format("%02x", btAddress[5]).toUpperCase();

        return txt;
    }

    private byte[] getBTAddressFromUI() {
        byte[] addr = new byte[6];
        addr[5] = (byte) Integer.parseInt(mAddrByte5EditText.getText().toString(), 16);                 // This is the MSB
        addr[4] = (byte) Integer.parseInt(mAddrByte4EditText.getText().toString(), 16);
        addr[3] = (byte) Integer.parseInt(mAddrByte3EditText.getText().toString(), 16);                 // This is the MSB
        addr[2] = (byte) Integer.parseInt(mAddrByte2EditText.getText().toString(), 16);
        addr[1] = (byte) Integer.parseInt(mAddrByte1EditText.getText().toString(), 16);
        addr[0] = (byte) Integer.parseInt(mAddrByte0EditText.getText().toString(), 16);                 // This is the LSB

        return addr;
    }

    private void setUIBTAddress(byte[] addr) throws STException {
        if ((addr == null) || (addr.length != 6)) {
            throw new STException(STException.STExceptionCode.BAD_PARAMETER);
        }

        mAddrByte5EditText.setText(String.format("%02x", addr[5]).toUpperCase());
        mAddrByte4EditText.setText(String.format("%02x", addr[4]).toUpperCase());
        mAddrByte3EditText.setText(String.format("%02x", addr[3]).toUpperCase());
        mAddrByte2EditText.setText(String.format("%02x", addr[2]).toUpperCase());
        mAddrByte1EditText.setText(String.format("%02x", addr[1]).toUpperCase());
        mAddrByte0EditText.setText(String.format("%02x", addr[0]).toUpperCase());

    }

    /**
     * The content from the NDEF Record is displayed in the Fragment
     */
    public void setContent() {
        String deviceName = mBtRecord.getBTDeviceName();
        mDeviceNameEditText.setText(deviceName);

        try {
            byte[] addr = mBtRecord.getBTDeviceMacAddr();
            setUIBTAddress(addr);
        } catch (STException e) {
            e.printStackTrace();
        }
    }

    /**
     * The content from the fragment is saved into the NDEF Record
     */
    @Override
    public void updateContent() {
        String deviceName;
        if (mDeviceNameEditText.getText() != null) {
            deviceName = mDeviceNameEditText.getText().toString();
        } else {
            deviceName = "Device name unknown";
        }

        byte[] btAddr = getBTAddressFromUI();

        mBtRecord.setBTDeviceName(deviceName);

        if (mBtAdapter.getName().equals(deviceName)) {
            byte[] serviceClass = {(byte) 0x0C, (byte) 0x02, (byte) 0x40}; // Device class / major class / minor class little endian coding
            mBtRecord.setBTDeviceClass(serviceClass);
            byte uuidClass = (byte) 0x03; // uiid Service class 16-bit complete.
            byte[] uuid = {(byte) 0x1E, (byte) 0x11, (byte) 0x0B, (byte) 0x11}; // HFP A2DP litlle endian coding
            mBtRecord.setBTUuidClassList(uuid);
            mBtRecord.setBtUuidClass(uuidClass);

        } else {
            try {
                BluetoothDevice remoteDevice = mBtAdapter.getRemoteDevice(btAddr);
                if (remoteDevice != null) {
                    BluetoothClass deviceClass = remoteDevice.getBluetoothClass();
                    if (deviceClass != null) {
                        int Cod = deviceClass.hashCode();
                        byte[] buff = {(byte) (Cod & 0xFF), (byte) ((Cod & 0xFF00) >> 8), (byte) ((Cod & 0xFF0000) >> 16)};
                        mBtRecord.setBTDeviceClass(buff);
                        ParcelUuid[] uiids = remoteDevice.getUuids();
                        // only handle a 16 bit class uuid - full list -
                        byte[] uuid = new byte[uiids.length * 2];
                        for (int i = 0; i < uiids.length; i++) {
                            long value = (uiids[i].getUuid().getMostSignificantBits() & 0x0000FFFF00000000L) >>> 32;
                            uuid[2 * i + 1] = (byte) ((value & 0xFF00) >> 8);
                            uuid[2 * i] = (byte) (value & 0xFF);
                        }
                        byte uuidClass = (byte) 0x03; // uiid Service class 16-bit complete.
                        mBtRecord.setBTUuidClassList(uuid);
                        mBtRecord.setBtUuidClass(uuidClass);
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        mBtRecord.setBTDeviceMacAddr(btAddr);
    }


    public void ndefRecordEditable(boolean editable) {

        mDeviceNameEditText.setFocusable(editable);
        mDeviceNameEditText.setFocusableInTouchMode(editable);
        mDeviceNameEditText.setClickable(editable);

        mAddrByte5EditText.setFocusable(editable);
        mAddrByte5EditText.setFocusableInTouchMode(editable);
        mAddrByte5EditText.setClickable(editable);

        mAddrByte4EditText.setFocusable(editable);
        mAddrByte4EditText.setFocusableInTouchMode(editable);
        mAddrByte4EditText.setClickable(editable);

        mAddrByte3EditText.setFocusable(editable);
        mAddrByte3EditText.setFocusableInTouchMode(editable);
        mAddrByte3EditText.setClickable(editable);

        mAddrByte2EditText.setFocusable(editable);
        mAddrByte2EditText.setFocusableInTouchMode(editable);
        mAddrByte2EditText.setClickable(editable);

        mAddrByte1EditText.setFocusable(editable);
        mAddrByte1EditText.setFocusableInTouchMode(editable);
        mAddrByte1EditText.setClickable(editable);

        mAddrByte0EditText.setFocusable(editable);
        mAddrByte0EditText.setFocusableInTouchMode(editable);
        mAddrByte0EditText.setClickable(editable);

        if(!editable) {
            // The Fragment is no more editable. Reload its content
            setContent();
        }
    }
}


