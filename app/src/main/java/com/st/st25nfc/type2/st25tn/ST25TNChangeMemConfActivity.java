/*
  * @author STMicroelectronics MMY Application team
  *
  ******************************************************************************
  * @attention
  *
  * <h2><center>&copy; COPYRIGHT 2021 STMicroelectronics</center></h2>
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

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.navigation.NavigationView;
import com.st.st25nfc.R;
import com.st.st25nfc.generic.MainActivity;
import com.st.st25nfc.generic.STFragmentActivity;
import com.st.st25nfc.generic.util.UIHelper;
import com.st.st25sdk.STException;
import com.st.st25sdk.type2.st25tn.ST25TNTag;
import com.st.st25sdk.type2.st25tn.ST25TNTag.ST25TNMemoryConfiguration;

import static com.st.st25sdk.type2.st25tn.ST25TNTag.ST25TNMemoryConfiguration.*;
import static android.view.View.INVISIBLE;
import static com.st.st25nfc.type2.st25tn.ST25TNChangeMemConfActivity.ActionStatus.*;



public class ST25TNChangeMemConfActivity extends STFragmentActivity implements NavigationView.OnNavigationItemSelectedListener {

    static final String TAG = "TVANDEFChangeMemConf";
    private ST25TNTag mST25TNTag;
    FragmentManager mFragmentManager;

    private RelativeLayout mColumn2Layout;
    private int mWholeTagHeightInPixels;        // Height of the graphical representation of the tag
    private int mTotalMemorySizeInBlocks;
    private int mBlockSizeInBytes;
    private ST25TNMemoryConfiguration mCurrentMemoryConfiguration;
    private ST25TNMemoryConfiguration mSelectedMemoryConfiguration;

    private RelativeLayout mColumn3Layout;
    private TextView mT2tAreaSizeTextView;

    private TextView mT2THeaderTextView;
    private TextView mTLVSAreaTextView;
    private TextView mSystemArea1TextView;
    private TextView mSystemArea2TextView;
    private TextView mSystemArea3TextView;

    private TextView mT2THeaderSizeInBytesTextView;
    private TextView mT2THeaderSizeInBlocksTextView;

    private TextView mT2TArea2SizeInBytesTextView;
    private TextView mT2TArea2SizeInBlocksTextView;

    private TextView mSystemAreaSizeInBytesTextView;
    private TextView mSystemAreaSizeInBlocksTextView;

    private TextView mTotalSizeInBytesTextView;
    private TextView mTotalSizeInBlocksTextView;

    private RadioButton mDefaultModeRadioButton;
    private RadioButton mExtendedMode1RadioButton;
    private RadioButton mExtendedMode2RadioButton;

    private Button mCheckTlvButton;

    enum Action {
        WRITE_MEMORY_CONFIGURATION,
        CHECK_TLVS
    }

    enum ActionStatus {
        ACTION_SUCCESSFUL,
        ACTION_FAILED,
        TAG_NOT_IN_THE_FIELD,
        NO_TLV_NEEDED,
        TLVS_CORRECT,
        TLVS_INCORRECT
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.default_layout);

        // Inflate content of FrameLayout
        FrameLayout frameLayout=(FrameLayout) findViewById(R.id.frame_content);
        View childView = getLayoutInflater().inflate(R.layout.st25tn_memory_configuration, null);
        frameLayout.addView(childView);

        mST25TNTag = (ST25TNTag) MainActivity.getTag();
        if (mST25TNTag == null) {
            showToast(R.string.invalid_tag);
            goBackToMainActivity();
            return;
        }

        if (mST25TNTag.isST25TN512()) {
            showToast(R.string.invalid_tag);
            goBackToMainActivity();
            return;
        }

        mFragmentManager = getSupportFragmentManager();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        mMenu.inflateMenu(navigationView);

        mCurrentMemoryConfiguration = mST25TNTag.getMemoryConfiguration();

        mBlockSizeInBytes = mST25TNTag.getBlockSizeInBytes();

        mColumn2Layout = (RelativeLayout) findViewById(R.id.column2Layout);
        // Hide column2Layout until we have calculated all the area sizes
        mColumn2Layout.setVisibility(INVISIBLE);

        // Trick to get the height of column2Layout
        // By this way, the drawing will fit the space available on this phone
        ViewTreeObserver viewTreeObserver = mColumn2Layout.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewTreeObserver viewTreeObserver = mColumn2Layout.getViewTreeObserver();
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    viewTreeObserver.removeGlobalOnLayoutListener(this);
                } else {
                    viewTreeObserver.removeOnGlobalLayoutListener(this);
                }

                int column2LayoutHeight = mColumn2Layout.getMeasuredHeight();
                setDrawingSize(column2LayoutHeight);
            }
        });

        mT2THeaderTextView = findViewById(R.id.t2tHeaderTextView);
        mTLVSAreaTextView = findViewById(R.id.tlvsAreaTextView);
        mSystemArea1TextView = findViewById(R.id.systemArea1TextView);
        mSystemArea2TextView = findViewById(R.id.systemArea2TextView);
        mSystemArea3TextView = findViewById(R.id.systemArea3TextView);

        mColumn3Layout = findViewById(R.id.column3Layout);
        mT2tAreaSizeTextView = findViewById(R.id.t2tAreaSizeTextView);

        mT2THeaderSizeInBytesTextView = findViewById(R.id.t2tHeaderSizeInBytesTextView);
        mT2THeaderSizeInBlocksTextView = findViewById(R.id.t2tHeaderSizeInBlocksTextView);

        mT2TArea2SizeInBytesTextView = findViewById(R.id.t2tAreaSizeInBytesTextView);
        mT2TArea2SizeInBlocksTextView = findViewById(R.id.t2tAreaSizeInBlocksTextView);

        mSystemAreaSizeInBytesTextView = findViewById(R.id.systemAreaSizeInBytesTextView);
        mSystemAreaSizeInBlocksTextView = findViewById(R.id.systemAreaSizeInBlocksTextView);

        mTotalSizeInBytesTextView = findViewById(R.id.totalSizeInBytesTextView);
        mTotalSizeInBlocksTextView = findViewById(R.id.totalSizeInBlocksTextView);

        mDefaultModeRadioButton = findViewById(R.id.defaultModeRadioButton);
        mDefaultModeRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedMemoryConfiguration = TLVS_AREA_160_BYTES;
                drawAreas();
            }
        });

        mExtendedMode1RadioButton = findViewById(R.id.extendedMode1RadioButton);
        mExtendedMode1RadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedMemoryConfiguration = EXTENDED_TLVS_AREA_192_BYTES;
                drawAreas();
            }
        });

        mExtendedMode2RadioButton = findViewById(R.id.extendedMode2RadioButton);
        mExtendedMode2RadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedMemoryConfiguration = EXTENDED_TLVS_AREA_208_BYTES;
                drawAreas();
            }
        });

        mCheckTlvButton = findViewById(R.id.checkTlvButton);
        mCheckTlvButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new myAsyncTask(Action.CHECK_TLVS).execute();
            }
        });

        displayCurrentConfiguration();

    }

    @Override
    public void onResume() {
        super.onResume();

        boolean tagChanged = tagChanged(this, mST25TNTag);
        if(!tagChanged) {
            drawAreas();
        }
    }

    private void displayCurrentConfiguration() {
        mSelectedMemoryConfiguration = mCurrentMemoryConfiguration;

        switch(mCurrentMemoryConfiguration) {
            default:
            case TLVS_AREA_160_BYTES:
                mDefaultModeRadioButton.setChecked(true);
                break;
            case EXTENDED_TLVS_AREA_192_BYTES:
                mExtendedMode1RadioButton.setChecked(true);
                break;
            case EXTENDED_TLVS_AREA_208_BYTES:
                mExtendedMode2RadioButton.setChecked(true);
                break;
        }

        drawAreas();
    }

    private void setDrawingSize(int column2LayoutHeight) {

        // Areas will be drawn in "column2Layout"
        // Each area will be represented by a rectangle.
        // - Rectangle width will be fixed (arbitrary set to "80dp")
        // - Rectangle height will be relative to column2Layout's height. By this way the drawing
        //   size will fit the screen size.

        // The drawing will occupy 90% of column2Layout's height
        mWholeTagHeightInPixels = (column2LayoutHeight * 90) / 100;

        drawAreas();
    }

    private void writeMemoryConfiguration() {
        new myAsyncTask(Action.WRITE_MEMORY_CONFIGURATION).execute();
    }

    private void drawAreas() {

        if(mWholeTagHeightInPixels == 0) {
            // Areas cannot be drawn until mWholeTagHeightInPixels is known because all the sizes
            // will be relative to this size
            return;
        }

        mTotalMemorySizeInBlocks = 64;

        int t2THeaderSizeInBlocks = mST25TNTag.T2T_MEMORY_HEADER_NUMBER_OF_BYTES / mBlockSizeInBytes;
        int t2THeaderRectangleHeight = convertSizeInBlocksIntoRectangleHeight(t2THeaderSizeInBlocks);

        int systemArea1SizeInBlocks = mST25TNTag.SYSTEM_AREA1_MEMORY_SIZE_IN_BLOCKS;
        int systemArea1RectangleHeight = convertSizeInBlocksIntoRectangleHeight(systemArea1SizeInBlocks);

        int systemArea2SizeInBlocks;
        int systemArea3SizeInBlocks;
        int t2TAreaSizeInBlocks;

        switch(mSelectedMemoryConfiguration) {
            default:
            case TLVS_AREA_160_BYTES:
                t2TAreaSizeInBlocks = 160 / mBlockSizeInBytes;
                systemArea2SizeInBlocks = mST25TNTag.SYSTEM_AREA2_MEMORY_SIZE_IN_BLOCKS;
                systemArea3SizeInBlocks = mST25TNTag.SYSTEM_AREA3_MEMORY_SIZE_IN_BLOCKS;

                mSystemArea2TextView.setText("");
                mSystemArea2TextView.setBackgroundColor(getResources().getColor(R.color.st_light_green));
                mSystemArea3TextView.setText("");
                mSystemArea3TextView.setBackgroundColor(getResources().getColor(R.color.st_light_green));
                break;

            case EXTENDED_TLVS_AREA_192_BYTES:
                t2TAreaSizeInBlocks = 192 / mBlockSizeInBytes;
                systemArea2SizeInBlocks = 0;
                systemArea3SizeInBlocks = mST25TNTag.SYSTEM_AREA3_MEMORY_SIZE_IN_BLOCKS;

                mSystemArea2TextView.setText(R.string.tlvs_area);
                mSystemArea2TextView.setBackgroundColor(getResources().getColor(R.color.st_light_purple));
                mSystemArea3TextView.setText(R.string.t2t_system_area);
                mSystemArea3TextView.setBackgroundColor(getResources().getColor(R.color.st_light_green));
                break;

            case EXTENDED_TLVS_AREA_208_BYTES:
                t2TAreaSizeInBlocks = 208 / mBlockSizeInBytes;
                systemArea2SizeInBlocks = 0;
                systemArea3SizeInBlocks = 0;

                mSystemArea2TextView.setText(R.string.tlvs_area);
                mSystemArea2TextView.setBackgroundColor(getResources().getColor(R.color.st_light_purple));
                mSystemArea3TextView.setText("");
                mSystemArea3TextView.setBackgroundColor(getResources().getColor(R.color.st_light_purple));
                break;
        }
        int systemAreaSizeInBlocks = systemArea1SizeInBlocks + systemArea2SizeInBlocks + systemArea3SizeInBlocks;

        int t2TAreaRectangleHeight = convertSizeInBlocksIntoRectangleHeight(160/mBlockSizeInBytes); // DO NOT use t2TAreaSizeInBlocks here
        int systemArea2RectangleHeight = convertSizeInBlocksIntoRectangleHeight(mST25TNTag.SYSTEM_AREA2_MEMORY_SIZE_IN_BLOCKS);
        int systemArea3RectangleHeight = convertSizeInBlocksIntoRectangleHeight(mST25TNTag.SYSTEM_AREA3_MEMORY_SIZE_IN_BLOCKS);

        mT2THeaderTextView.getLayoutParams().height = t2THeaderRectangleHeight;
        mT2THeaderTextView.requestLayout();

        mTLVSAreaTextView.getLayoutParams().height = t2TAreaRectangleHeight;
        mTLVSAreaTextView.requestLayout();

        mSystemArea1TextView.getLayoutParams().height = systemArea1RectangleHeight;
        mSystemArea1TextView.requestLayout();

        mSystemArea2TextView.getLayoutParams().height = systemArea2RectangleHeight;
        mSystemArea2TextView.requestLayout();

        mSystemArea3TextView.getLayoutParams().height = systemArea3RectangleHeight;
        mSystemArea3TextView.requestLayout();

        // The content of column2Layout is ready
        mColumn2Layout.setVisibility(View.VISIBLE);

        // Update the Table
        mT2THeaderSizeInBytesTextView.setText(String.valueOf(t2THeaderSizeInBlocks * mBlockSizeInBytes));
        mT2THeaderSizeInBlocksTextView.setText(String.valueOf(t2THeaderSizeInBlocks));

        mT2TArea2SizeInBytesTextView.setText(String.valueOf(t2TAreaSizeInBlocks * mBlockSizeInBytes));
        mT2TArea2SizeInBlocksTextView.setText(String.valueOf(t2TAreaSizeInBlocks));

        mSystemAreaSizeInBytesTextView.setText(String.valueOf(systemAreaSizeInBlocks * mBlockSizeInBytes));
        mSystemAreaSizeInBlocksTextView.setText(String.valueOf(systemAreaSizeInBlocks));

        mTotalSizeInBytesTextView.setText(String.valueOf(mTotalMemorySizeInBlocks * mBlockSizeInBytes));
        mTotalSizeInBlocksTextView.setText(String.valueOf(mTotalMemorySizeInBlocks));

        // Update the column displaying the T2T Area size
        try {
            int t2tAreaSizeInBytes = mST25TNTag.getT2TAreaSizeFromMemoryConfiguration(mSelectedMemoryConfiguration);
            int t2tAreaSizeInBlocks = t2tAreaSizeInBytes / mBlockSizeInBytes;
            int t2tAreaRectangleHeight = convertSizeInBlocksIntoRectangleHeight(t2tAreaSizeInBlocks);

            mColumn3Layout.getLayoutParams().height = t2tAreaRectangleHeight;
            mColumn3Layout.requestLayout();

            LinearLayout.LayoutParams parameter = (LinearLayout.LayoutParams) mColumn3Layout.getLayoutParams();
            parameter.setMargins(0, t2THeaderRectangleHeight, 0, 0); // left, top, right, bottom
            mColumn3Layout.setLayoutParams(parameter);

            mT2tAreaSizeTextView.setText(getResources().getString(R.string.t2t_area_size_in_bytes, t2tAreaSizeInBytes));

        } catch (STException e) {
            e.printStackTrace();
        }

    }

    /**
     * This function converts a size in blocks into a rectangle Height.
     * It does a rule of three:
     *      mTotalMemorySizeInBlocks ---> mWholeTagHeightInPixels
     *      sizeInBlocks             ---> rectangleHeight
     * @param sizeInBlocks
     * @return
     */
    private int convertSizeInBlocksIntoRectangleHeight(int sizeInBlocks) {
        int rectangleHeight = (sizeInBlocks * mWholeTagHeightInPixels) / mTotalMemorySizeInBlocks;
        return rectangleHeight;
    }

    /**
     * This function converts a rectangle Height into a size in blocks.
     * It does a rule of three:
     *      mWholeTagHeightInPixels ---> mTotalMemorySizeInBlocks
     *      rectangleHeight         ---> sizeInBlocks
     * @param rectangleHeight
     * @return
     */
    private int convertRectangleHeightIntoSizeInBlocks(int rectangleHeight) {
        int sizeInBlocks = (rectangleHeight * mTotalMemorySizeInBlocks) / mWholeTagHeightInPixels;
        return sizeInBlocks;
    }


    private void displayTlvCorrectMsg() {
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(this);

        String dialogMsg;
        if (mCurrentMemoryConfiguration == EXTENDED_TLVS_AREA_192_BYTES) {
            dialogMsg = getResources().getString(R.string.current_mem_conf_extended_mode1);
        } else {
            dialogMsg = getResources().getString(R.string.current_mem_conf_extended_mode2);
        }

        dialogMsg += "\n\n" + getResources().getString(R.string.type2_tlv_description) + "\n\n" +
                     getResources().getString(R.string.type2_tlv_correct);

        // set dialog message
        alertDialogBuilder
                .setMessage(dialogMsg)
                .setCancelable(true)
                .setPositiveButton(getString(R.string.ok),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                    }
                });

        // create alert dialog
        android.app.AlertDialog dialog = alertDialogBuilder.create();

        // show it
        dialog.show();

        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.st_light_blue));
    }

    private void displayTlvIncorrectMsg() {
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(this);

        String dialogMsg;
        if (mCurrentMemoryConfiguration == EXTENDED_TLVS_AREA_192_BYTES) {
            dialogMsg = getResources().getString(R.string.current_mem_conf_extended_mode1);
        } else {
            dialogMsg = getResources().getString(R.string.current_mem_conf_extended_mode2);
        }

        dialogMsg += "\n\n" + getResources().getString(R.string.type2_tlv_description) + "\n\n" +
                getResources().getString(R.string.type2_tlv_incorrect);

        // set dialog message
        alertDialogBuilder
                .setMessage(dialogMsg)
                .setCancelable(true)
                .setNegativeButton(getString(R.string.no),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton(getString(R.string.yes),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        //executeAsynchronousAction(SET_TLVS);
                        dialog.cancel();
                    }
                });

        // create alert dialog
        android.app.AlertDialog dialog = alertDialogBuilder.create();

        // show it
        dialog.show();

        dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.st_light_blue));
        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.st_light_blue));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds read_list_items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_st25dv_mem_conf, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                if (mSelectedMemoryConfiguration != mCurrentMemoryConfiguration) {
                    askConfirmationBeforeUpdatingTheTag();
                }
                return true;

            case R.id.action_refresh:
                displayCurrentConfiguration();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        return mMenu.selectItem(this, item);
    }


    private void askConfirmationBeforeUpdatingTheTag() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ST25TNChangeMemConfActivity.this);

        // set title
        alertDialogBuilder.setTitle(getString(R.string.confirmation_needed));

        // set dialog message
        alertDialogBuilder
                .setMessage(getString(R.string.irreversible_action_memory_configuration_change))
                .setCancelable(true)
                .setPositiveButton(getString(R.string.yes),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                        writeMemoryConfiguration();
                    }
                })
                .setNegativeButton(getString(R.string.no),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();

        alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.st_light_blue));
        alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.st_light_blue));

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
                switch(mAction) {
                    case WRITE_MEMORY_CONFIGURATION:
                        mST25TNTag.setMemoryConfiguration(mSelectedMemoryConfiguration);
                        result = ACTION_SUCCESSFUL;
                        mCurrentMemoryConfiguration = mSelectedMemoryConfiguration;
                        break;
                    case CHECK_TLVS:
                        if (mCurrentMemoryConfiguration == TLVS_AREA_160_BYTES) {
                            result = NO_TLV_NEEDED;
                        } else {
                            // TLV needed
                            if (mST25TNTag.areTlvsCorrectForCurrentMemoryConfiguration()) {
                                result = TLVS_CORRECT;
                            } else {
                                result = TLVS_INCORRECT;
                            }
                        }
                        break;

                    default:
                        Log.e(TAG, "Unknown action!");
                        result = ACTION_FAILED;
                        break;
                }

            } catch (STException e) {
                switch (e.getError()) {
                    case TAG_NOT_IN_THE_FIELD:
                        result = TAG_NOT_IN_THE_FIELD;
                        break;

                    default:
                        e.printStackTrace();
                        result = ACTION_FAILED;
                        break;
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(ActionStatus actionStatus) {

            switch(actionStatus) {
                case ACTION_SUCCESSFUL:
                    switch(mAction) {
                        case WRITE_MEMORY_CONFIGURATION:
                            showToast(R.string.tag_updated);
                            drawAreas();
                            break;
                    }
                    break;

                case NO_TLV_NEEDED:
                    UIHelper.displayMessage(ST25TNChangeMemConfActivity.this, R.string.type2_no_tlv_needed);
                    break;
                case TLVS_CORRECT:
                    displayTlvCorrectMsg();
                    break;
                case TLVS_INCORRECT:
                    displayTlvIncorrectMsg();
                    break;

                case ACTION_FAILED:
                    showToast(R.string.command_failed);
                    displayCurrentConfiguration();
                    break;

                case TAG_NOT_IN_THE_FIELD:
                    showToast(R.string.tag_not_in_the_field);
                    break;
            }

            return;
        }
    }


}
