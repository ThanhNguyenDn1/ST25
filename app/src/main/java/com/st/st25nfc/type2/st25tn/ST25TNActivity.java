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
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.navigation.NavigationView;
import com.st.st25nfc.R;
import com.st.st25nfc.generic.MainActivity;
import com.st.st25nfc.generic.STFragment;
import com.st.st25nfc.generic.STFragmentActivity;
import com.st.st25nfc.generic.STPagerAdapter;
import com.st.st25nfc.generic.SlidingTabLayout;
import com.st.st25nfc.generic.util.DisplayTapTagRequest;
import com.st.st25nfc.generic.util.UIHelper.STFragmentId;
import com.st.st25sdk.NFCTag;
import com.st.st25sdk.STException;
import com.st.st25sdk.ndef.NDEFMsg;
import com.st.st25sdk.ndef.UriRecord;
import com.st.st25sdk.type2.st25tn.ST25TNTag;
import com.st.st25sdk.type2.st25tn.ST25TNTag.ST25TNMemoryConfiguration;

import java.util.ArrayList;
import java.util.List;

import static com.st.st25sdk.type2.st25tn.ST25TNTag.ST25TNMemoryConfiguration.*;
import static com.st.st25nfc.type2.st25tn.ST25TNActivity.ActionStatus.*;
import static com.st.st25nfc.type2.st25tn.ST25TNActivity.Action.*;

public class ST25TNActivity extends STFragmentActivity implements NavigationView.OnNavigationItemSelectedListener,
        STFragment.STFragmentListener {

    enum Action {
        PERFORM_SAFETY_CHECKS,
        DISABLE_ANDEF,
        SET_TLVS,
        FIX_CCFILE
    };

    enum ActionStatus {
        ACTION_SUCCESSFUL,
        ACTION_FAILED,
        TAG_NOT_IN_THE_FIELD,

        SUSPICIOUS_ANDEF_CONFIGURATION,
        INCORRECT_TLVS,
        INVALID_CCFILE,
        ERROR_DURING_TLVS_PARSING
    };

    // Set here the Toolbar to use for this activity
    private int toolbar_res = R.menu.toolbar_empty;

    final static String TAG = "ST25TNActivity";
    public ST25TNTag mST25TNTag;

    STPagerAdapter mPagerAdapter;
    ViewPager mViewPager;
    private Action mCurrentAction;

    private SlidingTabLayout mSlidingTabLayout;

    private ST25TNMemoryConfiguration mNewMemoryConfiguration;

    private int mCC2DefaultMode = 0x14;
    private int mCC2ExtendedMode1 = 0x1C;
    private int mCC2ExtendedMode2 = 0x1E;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.pager_layout);

        mST25TNTag = (ST25TNTag) MainActivity.getTag();
        if (mST25TNTag == null) {
            showToast(R.string.invalid_tag);
            goBackToMainActivity();
            return;
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(mST25TNTag.getName());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        mMenu.inflateMenu(navigationView);

        List<STFragmentId> fragmentList = new ArrayList<STFragmentId>();

        fragmentList.add(STFragmentId.TAG_INFO_TYPE2_FRAGMENT_ID);
        fragmentList.add(STFragmentId.NDEF_DETAILS_FRAGMENT_ID);
        fragmentList.add(STFragmentId.CC_FILE_TYPE2_FRAGMENT_ID);
        fragmentList.add(STFragmentId.RAW_DATA_FRAGMENT_ID);

        mPagerAdapter = new STPagerAdapter(getSupportFragmentManager(), getApplicationContext(), fragmentList);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mPagerAdapter);

        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setViewPager(mViewPager);

        // Check if the activity was started with a request to select a specific tab
        Intent mIntent = getIntent();
        int tabNbr = mIntent.getIntExtra("select_tab", -1);
        if(tabNbr != -1) {
            mViewPager.setCurrentItem(tabNbr);
        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds read_list_items to the action bar if it is present.
        getMenuInflater().inflate(toolbar_res, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }




    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        return mMenu.selectItem(this, item);
    }


    public NFCTag getTag() {
        return mST25TNTag;
    }


    @Override
    public void onResume() {
        super.onResume();

        boolean tagChanged = tagChanged(this, mST25TNTag);
        if(!tagChanged) {
            byte[] uid = new byte[0];
            try {
                uid = mST25TNTag.getUid();
            } catch (STException e) {
                e.printStackTrace();
            }

            executeAsynchronousAction(PERFORM_SAFETY_CHECKS);
        }
    }


    /////////////////////////////////////////////////////////////////////////////////////

    private void executeAsynchronousAction(Action action) {
        Log.d(TAG, "Starting background action " + action);
        mCurrentAction = action;
        new myAsyncTask(action).execute();
    }


    private class myAsyncTask extends AsyncTask<Void, Void, ActionStatus> {
        Action mAction;
        int mProductCode;
        int mCC2Value;

        public myAsyncTask(Action action) {
            mAction = action;
        }

        @Override
        protected ActionStatus doInBackground(Void... param) {
            ActionStatus result;

            try {
                switch(mAction) {
                    case PERFORM_SAFETY_CHECKS:
                        if (mST25TNTag.errorDuringTlvParsing()){
                            return ActionStatus.ERROR_DURING_TLVS_PARSING;
                        }

                        mProductCode = mST25TNTag.getProductCode();

                        byte[] ccfile = mST25TNTag.readCCFile();
                        if ((ccfile == null) || (ccfile.length != 4)) {
                            return ActionStatus.ACTION_FAILED;
                        }

                        mCC2Value = ccfile[2];
                        ST25TNMemoryConfiguration memoryConfiguration = mST25TNTag.getMemoryConfiguration();
                        if (memoryConfiguration == INVALID_MEMORY_CONFIGURATION) {
                            return ActionStatus.INVALID_CCFILE;
                        }

                        if (!mST25TNTag.areTlvsCorrectForCurrentMemoryConfiguration()) {
                            return ActionStatus.INCORRECT_TLVS;
                        }

                        NDEFMsg currentNdefMsg = mST25TNTag.readNdefMessage();

                        // If ANDEF is enabled, check that the NDEF message contains an URI
                        if (mST25TNTag.isAndefEnabled()) {
                            if  ((currentNdefMsg == null) ||
                                 (currentNdefMsg.getNbrOfRecords() != 1) ||
                                 (!(currentNdefMsg.getNDEFRecord(0) instanceof UriRecord)) ) {
                                return ActionStatus.SUSPICIOUS_ANDEF_CONFIGURATION;
                            }
                        }
                        result = ActionStatus.ACTION_SUCCESSFUL;
                        break;

                    case DISABLE_ANDEF:
                        mST25TNTag.enableAndefCustomMsg(false);
                        mST25TNTag.enableUniqueTapCode(false);
                        result = ActionStatus.ACTION_SUCCESSFUL;
                        break;

                    case SET_TLVS:
                        mST25TNTag.setTLVForCurrentMemoryConfiguration();
                        result = ActionStatus.ACTION_SUCCESSFUL;
                        break;

                    case FIX_CCFILE:
                        // setMemoryConfiguration() will fix the CCFile and set the right TLVs
                        mST25TNTag.setMemoryConfiguration(mNewMemoryConfiguration);
                        result = ActionStatus.ACTION_SUCCESSFUL;
                        break;

                    default:
                        // Unknown action
                        result = ActionStatus.ACTION_FAILED;
                        break;
                }

            } catch (STException e) {
                switch (e.getError()) {
                    case TAG_NOT_IN_THE_FIELD:
                        result = TAG_NOT_IN_THE_FIELD;
                        break;

                    case INVALID_NDEF_DATA:
                        if (mAction == PERFORM_SAFETY_CHECKS) {
                            // These errors happen if the tag doesn't contain a valid CCFile or NDEF File
                            // No need to continue de Safety checks in that case
                            result = ACTION_SUCCESSFUL;
                        } else {
                            result = ACTION_FAILED;
                        }
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
                        case FIX_CCFILE:
                        case DISABLE_ANDEF:
                            showToast(R.string.tag_updated);
                            // Restart the safetyCheck;
                            executeAsynchronousAction(PERFORM_SAFETY_CHECKS);
                            break;
                        case SET_TLVS:
                            // Erase the reference to current tag to force a re-instanciation
                            MainActivity.setTag(null);
                            DisplayTapTagRequest.run(ST25TNActivity.this, mST25TNTag, getString(R.string.please_tap_the_tag_again));
                            break;
                        case PERFORM_SAFETY_CHECKS:
                            Log.v(TAG, "Safety check: No problem detected");
                            break;
                    }
                    break;

                case SUSPICIOUS_ANDEF_CONFIGURATION:
                    displayDisableAndefQuestion();
                    break;

                case INCORRECT_TLVS:
                    if (mST25TNTag.isST25TN512()) {
                        displaySt25tn512TlvWarning();
                    } else {
                        displaySt25tn01kTlvWarning();
                    }
                    break;

                case INVALID_CCFILE:
                    if (mProductCode == 0x9091) {
                        // ST25TN512: There is no other valid solution for the CCFile length
                        displayCCFileError(mCC2Value);
                    } else {
                        // ST25TN01K
                        if ((mCC2Value | mCC2DefaultMode) == mCC2DefaultMode) {
                            displayCCFileWarning(mCC2Value, mCC2DefaultMode, TLVS_AREA_160_BYTES);
                        } else if ((mCC2Value | mCC2ExtendedMode1) == mCC2ExtendedMode1) {
                            displayCCFileWarning(mCC2Value, mCC2ExtendedMode1, EXTENDED_TLVS_AREA_192_BYTES);
                        } else if ((mCC2Value | mCC2ExtendedMode2) == mCC2ExtendedMode2) {
                            displayCCFileWarning(mCC2Value, mCC2ExtendedMode2, EXTENDED_TLVS_AREA_208_BYTES);
                        } else {
                            // No solution to recover!
                            displayCCFileError(mCC2Value);
                        }
                    }

                    break;

                case ERROR_DURING_TLVS_PARSING:
                    displayErrorDuringTlvParsingMsg();
                    break;

                case ACTION_FAILED:
                    showToast(R.string.Command_failed);
                    break;

                case TAG_NOT_IN_THE_FIELD:
                    showToast(R.string.tag_not_in_the_field);
                    break;
            }

            return;
        }
    }

    private void displayDisableAndefQuestion() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        String dialogMsg = getResources().getString(R.string.warning_invalid_andef_configuration);

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
                        executeAsynchronousAction(DISABLE_ANDEF);
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog dialog = alertDialogBuilder.create();

        // show it
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.st_light_blue));
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.st_light_blue));
    }

    private void displaySt25tn512TlvWarning() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        String dialogMsg = getResources().getString(R.string.warning_invalid_tlvs_st25tn512);

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
                        executeAsynchronousAction(SET_TLVS);
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog dialog = alertDialogBuilder.create();

        // show it
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.st_light_blue));
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.st_light_blue));
    }

    private void displaySt25tn01kTlvWarning() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        String dialogMsg = getResources().getString(R.string.warning_invalid_tlvs_st25tn01K);

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
                        executeAsynchronousAction(SET_TLVS);
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog dialog = alertDialogBuilder.create();

        // show it
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.st_light_blue));
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.st_light_blue));
    }

    private void displayErrorDuringTlvParsingMsg() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        String dialogMsg = getResources().getString(R.string.error_during_tlvs_parsing);

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
                        executeAsynchronousAction(SET_TLVS);
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog dialog = alertDialogBuilder.create();

        // show it
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.st_light_blue));
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.st_light_blue));
    }


    private void displayCCFileWarning(int currentCC2, int newCC2, ST25TNMemoryConfiguration newMemoryConfiguration) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        String dialogMsg = getResources().getString(R.string.warning_invalid_ccfile_length, currentCC2) +
                "\n\n" +
                getResources().getString(R.string.do_you_want_to_fix_ccfile_length, newCC2);

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
                        mNewMemoryConfiguration = newMemoryConfiguration;
                        executeAsynchronousAction(FIX_CCFILE);
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog dialog = alertDialogBuilder.create();

        // show it
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.st_light_blue));
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.st_light_blue));
    }

    private void displayCCFileError(int currentCC2) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        String dialogMsg = getResources().getString(R.string.warning_invalid_ccfile_length, currentCC2) +
                            "\n\n" +
                            getResources().getString(R.string.no_valid_value_can_be_set);

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
        AlertDialog dialog = alertDialogBuilder.create();

        // show it
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.st_light_blue));
    }

}

