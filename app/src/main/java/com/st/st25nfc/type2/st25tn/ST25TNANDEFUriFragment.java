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

package com.st.st25nfc.type2.st25tn;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;

import com.st.st25nfc.R;
import com.st.st25nfc.generic.MainActivity;
import com.st.st25nfc.generic.STFragmentActivity;
import com.st.st25nfc.generic.ndef.NDEFActivity;
import com.st.st25nfc.generic.ndef.NDEFEditorFragment;
import com.st.st25nfc.generic.ndef.NDEFRecordFragment;
import com.st.st25nfc.generic.util.UIHelper;
import com.st.st25sdk.Helper;
import com.st.st25sdk.NFCTag;
import com.st.st25sdk.STException;
import com.st.st25sdk.ndef.NDEFMsg;
import com.st.st25sdk.ndef.UriRecord;
import com.st.st25sdk.type2.Type2Tag;
import com.st.st25sdk.type2.st25tn.ST25TNTag;

import java.io.UnsupportedEncodingException;
import java.util.Collection;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.st.st25nfc.generic.ndef.NDEFEditorFragment.VIEW_NDEF_RECORD;
import static com.st.st25nfc.type2.st25tn.ST25TNANDEFUriFragment.ActionStatus.ACTION_FAILED;
import static com.st.st25nfc.type2.st25tn.ST25TNANDEFUriFragment.ActionStatus.ACTION_SUCCESSFUL;
import static com.st.st25nfc.type2.st25tn.ST25TNANDEFUriFragment.ActionStatus.EDITION_NOT_ALLOWED;
import static com.st.st25nfc.type2.st25tn.ST25TNANDEFUriFragment.ActionStatus.INVALID_SEPARATOR;
import static com.st.st25nfc.type2.st25tn.ST25TNANDEFUriFragment.ActionStatus.TAG_NOT_IN_THE_FIELD;
import static com.st.st25sdk.type2.st25tn.ST25TNTag.ST25TNMemoryConfiguration.EXTENDED_TLVS_AREA_192_BYTES;
import static com.st.st25sdk.type2.st25tn.ST25TNTag.ST25TNMemoryConfiguration.EXTENDED_TLVS_AREA_208_BYTES;
import static com.st.st25sdk.type2.st25tn.ST25TNTag.ST25TNLocks.*;

public class ST25TNANDEFUriFragment extends NDEFRecordFragment implements AdapterView.OnItemSelectedListener, NDEFActivity.WriteNdefMessageHook {

    final static String TAG = "TNANDEFUriFragment";

    enum Action {
        READ_CURRENT_CONFIGURATION,
        WRITE_NEW_CONFIGURATION,
        WRITE_CUSTOM_MSG,
        DELETE_RECORD
    };

    enum ActionStatus {
        ACTION_SUCCESSFUL,
        ACTION_FAILED,
        TAG_NOT_IN_THE_FIELD,
        AREA_PASSWORD_NEEDED,
        EDITION_NOT_ALLOWED,
        ANDEF_CONFIGURATION_IS_LOCKED,
        ANDEF_SIGNATURE_IS_LOCKED,
        INVALID_SEPARATOR
    };

    private ST25TNTag mST25TNTag;
    FragmentManager mFragmentManager;

    private UriRecord mUriRecord;
    private EditText mUriEditText;
    private Spinner mUriSpinner;
    private int mRecordAction;
    private boolean mIsNdefRecordEditable;
    boolean mIsCurrentConfigurationRead = false;
    private boolean mIsAndefCustomMsgAndSeparatorLocked = false;

    private Action mCurrentAction;

    private NDEFActivity mNDEFActivity;

    CheckBox mLockAndefCustomMsgAndSeparatorCheckbox;
    CheckBox mAndefCustomMsgCheckbox;
    CheckBox mAndefUtcCheckbox;
    TextView mSeparatorTextView;

    LinearLayout mAndefLayout;
    LinearLayout mCustomMsgLayout;
    LinearLayout mAndefUtcLayout;

    ImageButton mCustomMsgImageButton;
    ImageButton mAndefUtcImageButton;

    EditText mSeparatorCharEditText;

    TextView mGeneratedUrlTextView;

    private boolean mIsAndefEnabled = false;

    private byte[] mNdefBytes;
    private byte[] mCurrentPayload;
    private int mAndefStartOffset = 0;

    String mAndefSeparatorString = "";

    String mCustomMsg = "";
    String mCustomMsgToWrite = "";

    private boolean mAddAndefCustomMsg = false;
    private boolean mAddAndefUtc = false;

    private View mView;


    public static ST25TNANDEFUriFragment newInstance(Context context) {
        ST25TNANDEFUriFragment f = new ST25TNANDEFUriFragment();
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

        View view = inflater.inflate(R.layout.fragment_st25tnandef_uri, container, false);
        mView = view;

        Bundle bundle = getArguments();
        if (bundle == null) {
            Log.e(TAG, "Fatal error! Arguments are missing!");
            return null;
        }

        mFragmentManager = getActivity().getSupportFragmentManager();
        mNDEFActivity = (NDEFActivity) getActivity();

        NFCTag nfcTag = MainActivity.getTag();
        if (nfcTag instanceof ST25TNTag) {
            mST25TNTag = (ST25TNTag) nfcTag;
        } else {
            showToast(R.string.invalid_tag);
            ((STFragmentActivity) getActivity()).goBackToMainActivity();
        }

        NDEFMsg ndefMsg = (NDEFMsg) bundle.getSerializable(NDEFRecordFragment.NDEFKey);
        int recordNbr = bundle.getInt(NDEFRecordFragment.RecordNbrKey);
        mUriRecord = (UriRecord) ndefMsg.getNDEFRecord(recordNbr);

        // Get the IDs of all the Widgets
        getWidgetsIds(view);

        mRecordAction = bundle.getInt(NDEFEditorFragment.EditorKey);

        initFragmentWidgets();

        if(mRecordAction == VIEW_NDEF_RECORD) {
            // We are displaying an existing record. By default it is not editable
            ndefRecordEditable(false);

        } else {
            // We are adding a new TextRecord or editing an existing record
            ndefRecordEditable(true);
        }

        mAndefLayout.setVisibility(VISIBLE);

        mAndefCustomMsgCheckbox.setChecked(false);
        mAndefUtcCheckbox.setChecked(false);
        mSeparatorTextView.setVisibility(GONE);

        mCustomMsgImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayCustomMsgDialogBox();
            }
        });

        mAndefUtcImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayFieldContent("ANDEF Unique Tap Code field : 3 ASCII characters (not displayable)");
            }
        });

        readCurrentConfiguration();

        // Now that the UI is initialized, we can set all the Listeners
        setListeners();

        mNDEFActivity.hookWriteActions(this);

        return view;
    }

    private void displayCustomMsgDialogBox() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final AlertDialog dialog = builder.create();

        // set title
        //alertDialog.setTitle(getString(R.string.XXX));

        dialog.setCancelable(false);

        // inflate XML content
        View dialogView = getLayoutInflater().inflate(R.layout.fragment_st25tn_custom_msg, null);
        dialog.setView(dialogView);

        final EditText asciiCustomMsgEditText = dialogView.findViewById(R.id.asciiCustomMsgEditText);
        final Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        final Button updateTagButton = dialogView.findViewById(R.id.updateTagButton);
        final Button restoreDefaultButton = dialogView.findViewById(R.id.restoreDefaultButton);

        restoreDefaultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String defaultCustomMsg = mST25TNTag.getDefaultAndefCustomMsg();
                    asciiCustomMsgEditText.setText(defaultCustomMsg);
                } catch (STException e) {
                    e.printStackTrace();
                }
            }
        });

        asciiCustomMsgEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                String asciiText = asciiCustomMsgEditText.getText().toString();
                String hexData = Helper.convertAsciiStringToHexString(asciiText);

                boolean isUpdatePossible = false;
                if (mIsNdefRecordEditable) {
                    if (asciiText.length() == 14) {
                        if (!Helper.isStringInST25AsciiTable(asciiText)) {
                            displayWarningForNonPortableCharacters();
                        }
                        isUpdatePossible = true;
                    }
                }

                if (isUpdatePossible) {
                    updateTagButton.setEnabled(true);
                    updateTagButton.setTextColor(getResources().getColor(R.color.st_light_blue));
                } else {
                    updateTagButton.setEnabled(false);
                    updateTagButton.setTextColor(getResources().getColor(R.color.st_middle_grey));
                }

                if (mIsNdefRecordEditable) {
                    asciiCustomMsgEditText.setEnabled(true);
                    restoreDefaultButton.setEnabled(true);
                    restoreDefaultButton.setBackgroundResource(R.drawable.light_blue_round_area);
                } else {
                    asciiCustomMsgEditText.setEnabled(false);
                    restoreDefaultButton.setEnabled(false);
                    restoreDefaultButton.setBackgroundResource(R.drawable.light_grey_round_area);
                }

            }
        });

        asciiCustomMsgEditText.setText(mCustomMsg);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        updateTagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String asciiText = asciiCustomMsgEditText.getText().toString();
                dialog.cancel();
                mCustomMsgToWrite = asciiText;
                executeAsynchronousAction(Action.WRITE_CUSTOM_MSG);
            }
        });

        if (mIsAndefCustomMsgAndSeparatorLocked) {
            updateTagButton.setEnabled(false);
            updateTagButton.setTextColor(getResources().getColor(R.color.st_middle_grey));

            restoreDefaultButton.setEnabled(false);
            restoreDefaultButton.setBackgroundResource(R.drawable.light_grey_round_area);

            asciiCustomMsgEditText.setClickable(false);
            asciiCustomMsgEditText.setFocusable(false);
            asciiCustomMsgEditText.setFocusableInTouchMode(false);
        }

        // show the dialog box
        dialog.show();
    }

    private void getWidgetsIds(View view ) {
        mUriEditText = view.findViewById(R.id.ndef_fragment_uri_text);
        mUriSpinner = view.findViewById(R.id.ndef_fragment_uri_title);

        mGeneratedUrlTextView = view.findViewById(R.id.generatedUrlTextView);

        mCustomMsgLayout = view.findViewById(R.id.customMsgLayout);
        mAndefUtcLayout = view.findViewById(R.id.andefUtcLayout);

        mSeparatorCharEditText = view.findViewById(R.id.separatorCharEditText);

        mAndefLayout = view.findViewById(R.id.andefLayout);

        mAndefCustomMsgCheckbox = view.findViewById(R.id.customMsgCheckbox);
        mAndefUtcCheckbox = view.findViewById(R.id.andefUtcCheckbox);
        mSeparatorTextView = view.findViewById(R.id.separatorTextView);

        mLockAndefCustomMsgAndSeparatorCheckbox = view.findViewById(R.id.lockAndefCustomMsgAndSeparatorCheckbox);

        mCustomMsgImageButton = view.findViewById(R.id.customMsgImageButton);
        mAndefUtcImageButton = view.findViewById(R.id.andefUtcImageButton);

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void initFragmentWidgets() {
        Collection<String> spinnerList = UriRecord.getUriCodesList();
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this.getActivity(), R.layout.spinner_text_view){
            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position%2 == 1) {
                    // Set the item background color
                    tv.setBackgroundColor(getResources().getColor(R.color.st_light_grey));
                }
                else {
                    // Set the alternate item background color
                    tv.setBackgroundColor(getResources().getColor(R.color.st_very_light_blue));
                }
                return view;
            }
        };
        spinnerAdapter.addAll(spinnerList);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_text_view);
        mUriSpinner.setAdapter(spinnerAdapter);
        mUriSpinner.setOnItemSelectedListener(this);

        setContent();
    }

    /**
     * The content from the NDEF Record is displayed in the Fragment
     */
    public void setContent() {
        String uri = mUriRecord.getContent();
        mUriEditText.setText(uri);

        UriRecord.NdefUriIdCode uriCode = mUriRecord.getUriID();
        mUriSpinner.setSelection(UriRecord.getUriCodePositionInList(uriCode));
    }

    /**
     * The content from the fragment is saved into the NDEF Record
     */
    @Override
    public void updateContent() {
        UriRecord.NdefUriIdCode uriID = UriRecord.getUriCodeFromStr((String) mUriSpinner.getSelectedItem());
        mUriRecord.setUriID(uriID);

        String text = mUriEditText.getText().toString();
        mUriRecord.setContent(text);
    }


    @Override
    public void ndefRecordEditable(boolean editable) {
        mIsNdefRecordEditable = editable;

        if (editable) {
            mUriEditText.setBackgroundColor(getResources().getColor(R.color.st_very_light_blue));
            mUriSpinner.setBackgroundColor(getResources().getColor(R.color.st_very_light_blue));

        } else {
            mUriEditText.setBackgroundColor(getResources().getColor(R.color.st_light_grey));
            mUriSpinner.setBackgroundColor(getResources().getColor(R.color.st_light_grey));
        }

        mUriEditText.setClickable(editable);
        mUriEditText.setFocusable(editable);
        mUriEditText.setFocusableInTouchMode(editable);

        mUriSpinner.setClickable(editable);
        mUriSpinner.setFocusable(editable);
        mUriSpinner.setFocusableInTouchMode(editable);
        mUriSpinner.setEnabled(editable);

        mAndefCustomMsgCheckbox.setEnabled(editable);
        mAndefUtcCheckbox.setEnabled(editable);

        // mSeparatorCharEditText will be enabled if 'editable' is true AND mIsAndefCustomMsgAndSeparatorLocked is false
        if (editable && !mIsAndefCustomMsgAndSeparatorLocked) {
            mSeparatorCharEditText.setClickable(true);
            mSeparatorCharEditText.setFocusable(true);
            mSeparatorCharEditText.setFocusableInTouchMode(true);
            mSeparatorCharEditText.setBackgroundColor(getResources().getColor(R.color.st_very_light_blue));

            mLockAndefCustomMsgAndSeparatorCheckbox.setEnabled(true);
        } else {
            mSeparatorCharEditText.setClickable(false);
            mSeparatorCharEditText.setFocusable(false);
            mSeparatorCharEditText.setFocusableInTouchMode(false);
            mSeparatorCharEditText.setBackgroundColor(getResources().getColor(R.color.st_light_grey));

            mLockAndefCustomMsgAndSeparatorCheckbox.setEnabled(false);
        }

        if(!editable) {
            // The Fragment is no more editable. Reload its content
            if (mIsCurrentConfigurationRead) {
                readCurrentConfiguration();
            }
            setContent();
        }
    }


    private void displayNotImplemented() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        // set dialog message
        alertDialogBuilder
                .setMessage(R.string.not_implemented_yet)
                .setCancelable(true)
                .setPositiveButton(getString(R.string.ok),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog dialog = alertDialogBuilder.create();

        // show it
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.st_light_blue));
    }


    private void displayKeepPhoneOnTagMessage() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        // set dialog message
        alertDialogBuilder
                .setMessage(R.string.please_keep_the_phone_on_the_tag)
                .setCancelable(true)
                .setPositiveButton(getString(R.string.ok),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                        // Restart the same action
                        executeAsynchronousAction(mCurrentAction);
                    }
                });

        // create alert dialog
        AlertDialog dialog = alertDialogBuilder.create();

        // show it
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.st_light_blue));
    }


    private int getNumberOfAndefItemSelected() {
        int nbrOfItems = 0;

        if (mAndefCustomMsgCheckbox.isChecked()) nbrOfItems++;
        if (mAndefUtcCheckbox.isChecked()) nbrOfItems++;

        return nbrOfItems;
    }

    private void removeAndefDataFromUri() {
        if ((mNdefBytes == null) || (mCurrentPayload == null)) {
            return;
        }

        int payloadOffsetInUriRecord = Helper.findSubArrayPosition(mNdefBytes, mCurrentPayload);
        if (payloadOffsetInUriRecord != -1) {

            // NB: The first byte of the payload is the prefix ID. Skip it
            payloadOffsetInUriRecord++;

            // Before the UriRecord, there are 16 Bytes of T2T Header + 2 Bytes for the NDEF Header
            int payloadOffsetInMemory = payloadOffsetInUriRecord + Type2Tag.T2T_MEMORY_HEADER_NUMBER_OF_BYTES + 2;

            if (mAndefStartOffset < payloadOffsetInMemory) {
                displayInvalidAndefStartOffsetWarning();
                return;
            }

            int nbrOfBytesToKeep = mAndefStartOffset - payloadOffsetInMemory;
            String uri = mUriRecord.getContent();
            String uriWithoutAndef = uri.substring(0, nbrOfBytesToKeep);
            Log.i(TAG, uriWithoutAndef);

            mUriEditText.setText(uriWithoutAndef);
        }
    }

    private void checkIfQuestionMarkNeeded(boolean isChecked) {
        if (isChecked && !mAddAndefCustomMsg && !mAddAndefUtc) {
            String uriText = mUriEditText.getText().toString();
            if (uriText.indexOf("?") == -1) {
                mUriEditText.setText(uriText+"?data=");
            }
        }

        if (isChecked) {
            if (mST25TNTag.getMemoryConfiguration() == EXTENDED_TLVS_AREA_208_BYTES) {
                UIHelper.displayMessage(getActivity(), R.string.custom_msg_warning_when_using_extended_mode2);
            }
        }
    }

    private void proposeToAddQuestionMark() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        // set dialog message
        alertDialogBuilder
                .setMessage(R.string.add_question_mark)
                .setCancelable(true)
                .setNegativeButton(getString(R.string.no),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton(getString(R.string.yes),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                        String uriText = mUriEditText.getText().toString();
                        mUriEditText.setText(uriText + '?');
                    }
                });

        // create alert dialog
        AlertDialog dialog = alertDialogBuilder.create();

        // show it
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.st_light_blue));
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.st_light_blue));

    }

    private void updateUI() {

        if (mAndefCustomMsgCheckbox.isChecked()) {
            mCustomMsgLayout.setVisibility(VISIBLE);
        } else {
            mCustomMsgLayout.setVisibility(GONE);
        }

        if (mAndefUtcCheckbox.isChecked()) {
            mAndefUtcLayout.setVisibility(VISIBLE);
        } else {
            mAndefUtcLayout.setVisibility(GONE);
        }

        if (mAndefCustomMsgCheckbox.isChecked() && mAndefUtcCheckbox.isChecked()) {
            mSeparatorTextView.setVisibility(VISIBLE);
        } else {
            mSeparatorTextView.setVisibility(GONE);
        }

        if (mLockAndefCustomMsgAndSeparatorCheckbox.isChecked()) {
            mSeparatorTextView.setEnabled(false);
        }

        int numberOfAndefItems = getNumberOfAndefItemSelected();
        generateURL(numberOfAndefItems);
    }

    private void generateURL(int numberOfAndefItems) {
        String url = mUriSpinner.getSelectedItem().toString() + mUriEditText.getText();
        String andefData = "";

        // Custom Msg
        if(mAndefCustomMsgCheckbox.isChecked()) {
            andefData += mCustomMsg;
        }

        if (numberOfAndefItems > 1) {
            // A separator is needed
            String separator = mSeparatorCharEditText.getText().toString();
            andefData += separator;
        }

        // Unique Tap Code
        if(mAndefUtcCheckbox.isChecked()) {
            andefData += "XXX"; // ANDEF Unique Tap Code is not displayable. We display "XXX" instead
        }

        url += andefData;
        mGeneratedUrlTextView.setText(url);
    }

    private void setListeners() {

        mUriSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                updateUI();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }

        });

        mUriEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                updateUI();
            }
        });

        mSeparatorCharEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                String txt = mSeparatorCharEditText.getText().toString();
                mSeparatorTextView.setText(txt);
                updateUI();
            }
        });


        mAndefCustomMsgCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                                   @Override
                                                   public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                       checkIfQuestionMarkNeeded(isChecked);
                                                       updateUI();
                                                   }
                                               }
        );

        mAndefUtcCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                                    @Override
                                                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                        checkIfQuestionMarkNeeded(isChecked);
                                                        updateUI();
                                                    }
                                                }
        );

    }

    @Override
    public boolean writeNdefMessage() {
        executeAsynchronousAction(Action.WRITE_NEW_CONFIGURATION);
        return true;
    }

    @Override
    public boolean deleteNdefMessage() {
        if (mIsAndefEnabled) {
            executeAsynchronousAction(Action.DELETE_RECORD);
            return true;
        } else {
            // ANDEF is not enabled. Let NDEFActivity write the NDEF
            return false;
        }
    }

    private void readCurrentConfiguration() {
        mIsCurrentConfigurationRead = true;
        executeAsynchronousAction(Action.READ_CURRENT_CONFIGURATION);
    }

    private void executeAsynchronousAction(Action action) {
        Log.d(TAG, "Starting background action " + action);
        mCurrentAction = action;
        new myAsyncTask(action).execute();
    }

    private void displayInvalidAndefStartOffsetWarning() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        // set dialog message
        alertDialogBuilder
                .setMessage(R.string.invalid_andef_start_address)
                .setCancelable(true)
                .setPositiveButton(getString(R.string.ok),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog dialog = alertDialogBuilder.create();

        // show it
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.st_light_blue));
    }

    private void displayWarningForNonPortableCharacters() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        // set dialog message
        alertDialogBuilder
                .setMessage(R.string.warning_about_ascii_characters)
                .setCancelable(true)
                .setPositiveButton(getString(R.string.ok),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog dialog = alertDialogBuilder.create();

        // show it
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.st_light_blue));
    }

    private void displayFieldContent(final String fieldContent) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        // set dialog message
        alertDialogBuilder
                .setMessage(fieldContent)
                .setCancelable(true)
                .setPositiveButton(getString(R.string.ok),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog dialog = alertDialogBuilder.create();

        // show it
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.st_light_blue));
    }

    private class myAsyncTask extends AsyncTask<Void, Void, ActionStatus> {
        Action mAction;

        public myAsyncTask(Action action) {
            mAction = action;
        }

        @Override
        protected ActionStatus doInBackground(Void... param) {
            ActionStatus result;

            try {
                if (mAction == Action.WRITE_NEW_CONFIGURATION) {
                    // Check that edition is alllowed
                    if (!mIsNdefRecordEditable) {
                        return EDITION_NOT_ALLOWED;
                    }
                }

                UIHelper.displayCircularProgressBar(mNDEFActivity, getString(R.string.please_wait));

                switch (mAction) {
                    case READ_CURRENT_CONFIGURATION:

                        mAddAndefCustomMsg = mST25TNTag.isAndefCustomMsgEnabled();
                        mAddAndefUtc = mST25TNTag.isUniqueTapCodeEnabled();
                        mIsAndefEnabled = mAddAndefCustomMsg || mAddAndefUtc;

                        mAndefSeparatorString = mST25TNTag.getAndefSeparator();
                        mAndefStartOffset = mST25TNTag.getAndefStartAddressInBytes();

                        mCustomMsg = mST25TNTag.getAndefCustomMsg();

                        // Check Lock status of block 0x3C to 0x3F
                        boolean areBlocks3C3DLocked = mST25TNTag.isLocked(BLOCKS_3CH_TO_3DH);
                        boolean areBlocks3E3FLocked = mST25TNTag.isLocked(BLOCKS_3EH_TO_3FH);
                        mIsAndefCustomMsgAndSeparatorLocked = areBlocks3C3DLocked || areBlocks3E3FLocked;

                        if(mIsAndefEnabled) {
                            NDEFMsg ndefMsg;
                            try {
                                ndefMsg = mST25TNTag.readNdefMessage();
                            } catch (STException e) {
                                switch(e.getError()) {
                                    case INVALID_CCFILE:
                                    case INVALID_NDEF_DATA:
                                        // Create an empty NDEF message
                                        ndefMsg = new NDEFMsg();
                                        break;
                                    default:
                                        // Other exceptions are escalated
                                        throw e;
                                }
                            }

                            if (ndefMsg != null) {
                                mNdefBytes = ndefMsg.serialize();
                                mCurrentPayload = mUriRecord.getPayload();
                            } else {
                                mNdefBytes = null;
                                mCurrentPayload = null;
                            }

                        }

                        result = ActionStatus.ACTION_SUCCESSFUL;
                        break;

                    case WRITE_CUSTOM_MSG:
                        if (!mCustomMsgToWrite.equals(mCustomMsg)) {
                            mST25TNTag.setAndefCustomMsg(mCustomMsgToWrite);
                            mCustomMsg = mCustomMsgToWrite;
                        }
                        result = ActionStatus.ACTION_SUCCESSFUL;
                        break;

                    case WRITE_NEW_CONFIGURATION:
                        // Write the ANDEF configuration
                        if (mAndefCustomMsgCheckbox.isChecked() || mAndefUtcCheckbox.isChecked()) {
                            if ((mAndefCustomMsgCheckbox.isChecked() != mAddAndefCustomMsg)) {
                                mST25TNTag.enableAndefCustomMsg(mAndefCustomMsgCheckbox.isChecked());
                                mAddAndefCustomMsg = mAndefCustomMsgCheckbox.isChecked();
                            }

                            if ((mAndefUtcCheckbox.isChecked() != mAddAndefUtc)) {
                                mST25TNTag.enableUniqueTapCode(mAndefUtcCheckbox.isChecked());
                                mAddAndefUtc = mAndefUtcCheckbox.isChecked();
                            }

                            mIsAndefEnabled = true;

                            String newSeparator = mSeparatorCharEditText.getText().toString();
                            if (newSeparator.length() != 1) {
                                return INVALID_SEPARATOR;
                            }
                            if (!mAndefSeparatorString.equals(newSeparator)) {
                                mST25TNTag.setAndefSeparator(newSeparator);
                            }

                        } else {
                            // ANDEF is disabled
                            if(mIsCurrentConfigurationRead) {
                                mST25TNTag.enableAndefCustomMsg(false);
                                mAddAndefCustomMsg = false;

                                mST25TNTag.enableUniqueTapCode(false);
                                mAddAndefUtc = false;

                                mIsAndefEnabled = false;
                            }
                        }

                        if (mLockAndefCustomMsgAndSeparatorCheckbox.isChecked()) {
                            mST25TNTag.lock(BLOCKS_3CH_TO_3DH);
                            mST25TNTag.lock(BLOCKS_3EH_TO_3FH);
                            mIsAndefCustomMsgAndSeparatorLocked = true;
                        }

                        // Update mUriRecord and write it
                        updateContent();
                        if(mIsAndefEnabled) {
                            mST25TNTag.writeAndefUri(mUriRecord);
                        } else {
                            NDEFMsg ndefMsg = new NDEFMsg();
                            ndefMsg.addRecord(mUriRecord);
                            mST25TNTag.writeNdefMessage(ndefMsg);
                        }
                        result = ACTION_SUCCESSFUL;
                        break;
                    case DELETE_RECORD:
                        mST25TNTag.enableAndefCustomMsg(false);
                        mST25TNTag.enableUniqueTapCode(false);

                        NDEFMsg emptyNDEFMsg = new NDEFMsg();
                        mST25TNTag.writeNdefMessage(emptyNDEFMsg);
                        result = ACTION_SUCCESSFUL;
                        break;
                    default:
                        result = ACTION_FAILED;
                        break;
                }

            } catch (STException e) {
                switch (e.getError()) {
                    case TAG_NOT_IN_THE_FIELD:
                        result = TAG_NOT_IN_THE_FIELD;
                        break;

                    case ISO15693_BLOCK_IS_LOCKED:
                        result = ActionStatus.AREA_PASSWORD_NEEDED;
                        break;

                    default:
                        e.printStackTrace();
                        result = ACTION_FAILED;
                        break;
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                result = ACTION_FAILED;
            } catch (Exception e) {
                e.printStackTrace();
                result = ACTION_FAILED;
            }

            return result;
        }

        @Override
        protected void onPostExecute(ActionStatus actionStatus) {

            UIHelper.dismissCircularProgressBar();

            switch(actionStatus) {
                case ACTION_SUCCESSFUL:
                    switch (mAction) {
                        case READ_CURRENT_CONFIGURATION:
                            mSeparatorCharEditText.setText(mAndefSeparatorString);
                            mAndefCustomMsgCheckbox.setChecked(mAddAndefCustomMsg);
                            mAndefUtcCheckbox.setChecked(mAddAndefUtc);
                            mSeparatorTextView.setText(mAndefSeparatorString);
                            mLockAndefCustomMsgAndSeparatorCheckbox.setChecked(mIsAndefCustomMsgAndSeparatorLocked);

                            if (mIsAndefEnabled) {
                                // If ANDEF is currently enabled in the tag, we should truncate the URI to remove the ANDEF bytes
                                removeAndefDataFromUri();
                            }
                            updateUI();
                            break;
                        case WRITE_NEW_CONFIGURATION:
                            updateUI();
                            mNDEFActivity.finish();
                            showToast(R.string.tag_updated);
                            break;
                        case WRITE_CUSTOM_MSG:
                            updateUI();
                            showToast(R.string.tag_updated);
                            break;
                        case DELETE_RECORD:
                            mNDEFActivity.finish();
                            showToast(R.string.tag_updated);
                            break;
                    }
                    break;

                case INVALID_SEPARATOR:
                    UIHelper.displayMessage(getActivity(), R.string.invalid_separator);
                    break;

                case ANDEF_CONFIGURATION_IS_LOCKED:
                    UIHelper.displayMessage(getActivity(), R.string.andef_configuration_is_locked);
                    break;

                case ANDEF_SIGNATURE_IS_LOCKED:
                    UIHelper.displayMessage(getActivity(), R.string.andef_signature_configuration_is_locked);
                    break;

                case AREA_PASSWORD_NEEDED:
                    mNDEFActivity.showWritePasswordDialog();
                    break;

                case ACTION_FAILED:
                    showToast(R.string.command_failed);
                    break;

                case TAG_NOT_IN_THE_FIELD:
                    updateUI();
                    displayKeepPhoneOnTagMessage();
                    break;

                case EDITION_NOT_ALLOWED:
                    showToast(R.string.edition_not_activated);
                    break;
            }

            return;
        }
    }

}


