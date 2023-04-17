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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
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
import com.st.st25sdk.type2.st25tn.ST25TNTag.ST25TNLocks;

import java.util.EnumMap;
import java.util.Map;

import static com.st.st25sdk.type2.st25tn.ST25TNTag.ST25TNLocks.*;
import static com.st.st25nfc.type2.st25tn.ST25TNLockActivity.ActionStatus.ACTION_FAILED;
import static com.st.st25nfc.type2.st25tn.ST25TNLockActivity.ActionStatus.ACTION_SUCCESSFUL;
import static com.st.st25nfc.type2.st25tn.ST25TNLockActivity.ActionStatus.TAG_NOT_IN_THE_FIELD;
import static com.st.st25sdk.STException.STExceptionCode.BAD_PARAMETER;


public class ST25TNLockActivity extends STFragmentActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Local class used to store a status about each locable block
    class LockStatus {
        boolean enabled;
        boolean isLocked;
    }

    static final String TAG = "ST25TNLockActivity";
    private ST25TNTag mST25TNTag;
    FragmentManager mFragmentManager;
    private boolean mUpdateCCFileValue = false;

    /////////////////////////////////////////
    // Values currently set in the tags

    private Map<ST25TNLocks, LockStatus> mCurrentLocksMap;

    private int mCurrentStatLock0;
    private int mCurrentStatLock1;
    private int mCurrentSysLock;
    private int mCurrentDynLock0;
    private int mCurrentDynLock1;
    private int mCurrentDynLock2;

    /////////////////////////////////////////
    // New values entered by the user

    private Map<ST25TNLocks, LockStatus> mNewLocksMap;

    /////////////////////////////////////////
    private CheckBox mStatLock0Bit0Checkbox;
    private CheckBox mStatLock0Bit1Checkbox;
    private CheckBox mStatLock0Bit2Checkbox;
    private CheckBox mStatLock0Bit3Checkbox;
    private CheckBox mStatLock0Bit4Checkbox;
    private CheckBox mStatLock0Bit5Checkbox;
    private CheckBox mStatLock0Bit6Checkbox;
    private CheckBox mStatLock0Bit7Checkbox;

    private CheckBox mStatLock1Bit0Checkbox;
    private CheckBox mStatLock1Bit1Checkbox;
    private CheckBox mStatLock1Bit2Checkbox;
    private CheckBox mStatLock1Bit3Checkbox;
    private CheckBox mStatLock1Bit4Checkbox;
    private CheckBox mStatLock1Bit5Checkbox;
    private CheckBox mStatLock1Bit6Checkbox;
    private CheckBox mStatLock1Bit7Checkbox;

    private CheckBox mSysLockBit0Checkbox;
    private CheckBox mSysLockBit1Checkbox;
    private CheckBox mSysLockBit2Checkbox;
    private CheckBox mSysLockBit3Checkbox;
    private CheckBox mSysLockBit4Checkbox;

    private CheckBox mDynLock0Bit0Checkbox;
    private CheckBox mDynLock0Bit1Checkbox;
    private CheckBox mDynLock0Bit2Checkbox;
    private CheckBox mDynLock0Bit3Checkbox;
    private CheckBox mDynLock0Bit4Checkbox;
    private CheckBox mDynLock0Bit5Checkbox;
    private CheckBox mDynLock0Bit6Checkbox;
    private CheckBox mDynLock0Bit7Checkbox;

    private CheckBox mDynLock1Bit0Checkbox;
    private CheckBox mDynLock1Bit1Checkbox;
    private CheckBox mDynLock1Bit2Checkbox;
    private CheckBox mDynLock1Bit3Checkbox;
    private CheckBox mDynLock1Bit4Checkbox;
    private CheckBox mDynLock1Bit5Checkbox;

    private CheckBox mDynLock2Bit2Checkbox;
    private CheckBox mDynLock2Bit3Checkbox;
    private CheckBox mDynLock2Bit4Checkbox;
    private CheckBox mDynLock2Bit5Checkbox;
    private CheckBox mDynLock2Bit6Checkbox;
    private CheckBox mDynLock2Bit7Checkbox;

    private TextView mStatLock0ValueTextView;
    private TextView mStatLock1ValueTextView;
    private TextView mDynLock0ValueTextView;
    private TextView mDynLock1ValueTextView;
    private TextView mDynLock2ValueTextView;
    private TextView mSysLockValueTextView;


    enum Action {
        READ_LOCKS,
        WRITE_LOCKS
    }

    enum ActionStatus {
        ACTION_SUCCESSFUL,
        ACTION_FAILED,
        TAG_NOT_IN_THE_FIELD
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.default_layout);

        // Inflate content of FrameLayout
        FrameLayout frameLayout=(FrameLayout) findViewById(R.id.frame_content);
        View childView = getLayoutInflater().inflate(R.layout.st25tn_lock, null);
        frameLayout.addView(childView);

        mST25TNTag = (ST25TNTag) MainActivity.getTag();
        if (mST25TNTag == null) {
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

        mStatLock0Bit0Checkbox = findViewById(R.id.statLock0Bit0Checkbox);
        mStatLock0Bit1Checkbox = findViewById(R.id.statLock0Bit1Checkbox);
        mStatLock0Bit2Checkbox = findViewById(R.id.statLock0Bit2Checkbox);
        mStatLock0Bit3Checkbox = findViewById(R.id.statLock0Bit3Checkbox);
        mStatLock0Bit4Checkbox = findViewById(R.id.statLock0Bit4Checkbox);
        mStatLock0Bit5Checkbox = findViewById(R.id.statLock0Bit5Checkbox);
        mStatLock0Bit6Checkbox = findViewById(R.id.statLock0Bit6Checkbox);
        mStatLock0Bit7Checkbox = findViewById(R.id.statLock0Bit7Checkbox);

        mStatLock1Bit0Checkbox = findViewById(R.id.statLock1Bit0Checkbox);
        mStatLock1Bit1Checkbox = findViewById(R.id.statLock1Bit1Checkbox);
        mStatLock1Bit2Checkbox = findViewById(R.id.statLock1Bit2Checkbox);
        mStatLock1Bit3Checkbox = findViewById(R.id.statLock1Bit3Checkbox);
        mStatLock1Bit4Checkbox = findViewById(R.id.statLock1Bit4Checkbox);
        mStatLock1Bit5Checkbox = findViewById(R.id.statLock1Bit5Checkbox);
        mStatLock1Bit6Checkbox = findViewById(R.id.statLock1Bit6Checkbox);
        mStatLock1Bit7Checkbox = findViewById(R.id.statLock1Bit7Checkbox);

        mSysLockBit0Checkbox = findViewById(R.id.sysLockBit0Checkbox);
        mSysLockBit1Checkbox = findViewById(R.id.sysLockBit1Checkbox);
        mSysLockBit2Checkbox = findViewById(R.id.sysLockBit2Checkbox);
        mSysLockBit3Checkbox = findViewById(R.id.sysLockBit3Checkbox);
        mSysLockBit4Checkbox = findViewById(R.id.sysLockBit4Checkbox);

        mDynLock0Bit0Checkbox = findViewById(R.id.dynLock0Bit0Checkbox);
        mDynLock0Bit1Checkbox = findViewById(R.id.dynLock0Bit1Checkbox);
        mDynLock0Bit2Checkbox = findViewById(R.id.dynLock0Bit2Checkbox);
        mDynLock0Bit3Checkbox = findViewById(R.id.dynLock0Bit3Checkbox);
        mDynLock0Bit4Checkbox = findViewById(R.id.dynLock0Bit4Checkbox);
        mDynLock0Bit5Checkbox = findViewById(R.id.dynLock0Bit5Checkbox);
        mDynLock0Bit6Checkbox = findViewById(R.id.dynLock0Bit6Checkbox);
        mDynLock0Bit7Checkbox = findViewById(R.id.dynLock0Bit7Checkbox);

        mDynLock1Bit0Checkbox = findViewById(R.id.dynLock1Bit0Checkbox);
        mDynLock1Bit1Checkbox = findViewById(R.id.dynLock1Bit1Checkbox);
        mDynLock1Bit2Checkbox = findViewById(R.id.dynLock1Bit2Checkbox);
        mDynLock1Bit3Checkbox = findViewById(R.id.dynLock1Bit3Checkbox);
        mDynLock1Bit4Checkbox = findViewById(R.id.dynLock1Bit4Checkbox);
        mDynLock1Bit5Checkbox = findViewById(R.id.dynLock1Bit5Checkbox);

        mDynLock2Bit2Checkbox = findViewById(R.id.dynLock2Bit2Checkbox);
        mDynLock2Bit3Checkbox = findViewById(R.id.dynLock2Bit3Checkbox);
        mDynLock2Bit4Checkbox = findViewById(R.id.dynLock2Bit4Checkbox);
        mDynLock2Bit5Checkbox = findViewById(R.id.dynLock2Bit5Checkbox);
        mDynLock2Bit6Checkbox = findViewById(R.id.dynLock2Bit6Checkbox);
        mDynLock2Bit7Checkbox = findViewById(R.id.dynLock2Bit7Checkbox);

        mStatLock0ValueTextView = findViewById(R.id.statLock0ValueTextView);
        mStatLock1ValueTextView = findViewById(R.id.statLock1ValueTextView);
        mDynLock0ValueTextView = findViewById(R.id.dynLock0ValueTextView);
        mDynLock1ValueTextView = findViewById(R.id.dynLock1ValueTextView);
        mDynLock2ValueTextView = findViewById(R.id.dynLock2ValueTextView);
        mSysLockValueTextView = findViewById(R.id.sysLockValueTextView);

        Button selectAllButton = findViewById(R.id.selectAllButton);
        selectAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectAllLocks();
            }
        });

        // Create a Map Table used to store the current status of each lock
        mCurrentLocksMap = new EnumMap<>(ST25TNLocks.class);
        mNewLocksMap = new EnumMap<>(ST25TNLocks.class);

        for (ST25TNLocks lock : ST25TNLocks.values()) {
            LockStatus lockStatus = new LockStatus();
            lockStatus.enabled = false;
            lockStatus.isLocked = false;
            mCurrentLocksMap.put(lock, lockStatus);
        }

        copyLocksMap(mCurrentLocksMap, mNewLocksMap);

        try {
            for (ST25TNLocks lock : ST25TNLocks.values()) {
                CheckBox checkBox = getCheckBoxFromLockValue(lock);
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                              @Override
                              public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                  try {
                                      ST25TNLocks lock = getLockValueFromCheckBox(checkBox);
                                      LockStatus lockStatus = mNewLocksMap.get(lock);
                                      lockStatus.isLocked = isChecked;

                                      if (isChecked) {
                                          if (lock == BLOCK_02H_EXTENDED_LOCK0) {
                                              disableBits3To7OfStatLock0(mNewLocksMap);
                                          } else if (lock == BLOCK_02H_EXTENDED_LOCK1) {
                                              disableBits4To7OfStatLock0AndBits0To1OfStatLock1(mNewLocksMap);
                                          } else if (lock == BLOCK_02H_EXTENDED_LOCK2) {
                                              disableBits2To7OfStatLock1(mNewLocksMap);
                                          } else if (lock == BLOCK_2CH_DYNLOCK_SYSLOCK) {
                                              disableDynLocksAndSysLock(mNewLocksMap);
                                          }
                                      } else {
                                          if (lock == BLOCK_02H_EXTENDED_LOCK0) {
                                              restoreBits3To7OfStatLock0();
                                          } else if (lock == BLOCK_02H_EXTENDED_LOCK1) {
                                              restoreBits4To7OfStatLock0AndBits0To1OfStatLock1();
                                          } else if (lock == BLOCK_02H_EXTENDED_LOCK2) {
                                              restoreBits2To7OfStatLock1();
                                          } else if (lock == BLOCK_2CH_DYNLOCK_SYSLOCK) {
                                              restoreDynLocksAndSysLock();
                                          }
                                      }

                                      updateDisplay();

                                  } catch (STException e1) {
                                      e1.printStackTrace();
                                  }
                              }
                          }
                );
            }
        } catch (STException e) {
            e.printStackTrace();
        }

        updateDisplay();

        readCurrentLocks();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void readCurrentLocks() {
        new myAsyncTask(Action.READ_LOCKS).execute();
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
                if (areSomeLocksChanged()) {
                    if (areAllLocksSelected()) {
                        // All the Locks are selected. Ask if CCFile should be changed
                        askIfCCFileShouldBeUpdated();
                    } else {
                        // Some Locks are not set
                        mUpdateCCFileValue = false;
                        askConfirmationBeforeWriting();
                    }
                } else {
                    UIHelper.displayMessage(ST25TNLockActivity.this, R.string.no_change);
                }
                return true;

            case R.id.action_refresh:
                readCurrentLocks();
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

    private boolean areSomeLocksChanged() {
        for (ST25TNLocks lock : ST25TNLocks.values()) {
            LockStatus newLockStatus = mNewLocksMap.get(lock);
            LockStatus currentLockStatus = mCurrentLocksMap.get(lock);

            if (newLockStatus.isLocked != currentLockStatus.isLocked) {
                return true;
            }
        }
        return false;
    }

    private boolean areAllLocksSelected() {
        for (ST25TNLocks lock : ST25TNLocks.values()) {
            LockStatus newLockStatus = mNewLocksMap.get(lock);
            if (!newLockStatus.isLocked) {
                return false;
            }
        }

        return true;
    }

    private void askIfCCFileShouldBeUpdated() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set dialog message
        alertDialogBuilder
                .setMessage(R.string.ask_to_set_the_tag_as_read_only)
                .setCancelable(true)
                .setNegativeButton(getString(R.string.no),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton(getString(R.string.yes),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                        mUpdateCCFileValue = true;
                        askConfirmationBeforeWriting();
                    }
                });

        // create alert dialog
        AlertDialog dialog = alertDialogBuilder.create();

        // show it
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.st_light_blue));
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.st_light_blue));

    }

    private void askConfirmationBeforeWriting() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set dialog message
        alertDialogBuilder
                .setMessage(R.string.warning_irreversible_action_do_you_want_to_continue)
                .setCancelable(true)
                .setNegativeButton(getString(R.string.no),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton(getString(R.string.yes),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                        new myAsyncTask(Action.WRITE_LOCKS).execute();
                    }
                });

        // create alert dialog
        AlertDialog dialog = alertDialogBuilder.create();

        // show it
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.st_light_blue));
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.st_light_blue));

    }

    private void selectAllLocks() {
        // The 4 special bits should be processed last
        for (ST25TNLocks lock : ST25TNLocks.values()) {
            if ((lock != BLOCK_02H_EXTENDED_LOCK0) &&
                (lock != BLOCK_02H_EXTENDED_LOCK1) &&
                (lock != BLOCK_02H_EXTENDED_LOCK2) &&
                (lock != BLOCK_2CH_DYNLOCK_SYSLOCK) ) {
                lock(lock);
            }
        }

        lock(BLOCK_02H_EXTENDED_LOCK0);
        lock(BLOCK_02H_EXTENDED_LOCK1);
        lock(BLOCK_02H_EXTENDED_LOCK2);
        lock(BLOCK_2CH_DYNLOCK_SYSLOCK);

        updateDisplay();
    }

    private void lock(ST25TNLocks lock) {
        LockStatus newLockStatus = mNewLocksMap.get(lock);
        if (newLockStatus.enabled) {
            newLockStatus.isLocked = true;
        }
    }

    private void disableLock(Map<ST25TNLocks, LockStatus> map, ST25TNLocks lock) {
        LockStatus newLockStatus = map.get(lock);
        if (newLockStatus.enabled) {
            newLockStatus.enabled = false;
        }
    }

    private void restoreEnabledStatus(ST25TNLocks lock) {
        LockStatus currentLockStatus = mCurrentLocksMap.get(lock);
        LockStatus newLockStatus = mNewLocksMap.get(lock);

        newLockStatus.enabled = currentLockStatus.enabled;
    }

    private void disableBits3To7OfStatLock0(Map<ST25TNLocks, LockStatus> map) {
        disableLock(map, BLOCK_03H_CC_FILE);
    }

    private void disableBits4To7OfStatLock0AndBits0To1OfStatLock1(Map<ST25TNLocks, LockStatus> map) {
        // STATLOCK_0
        disableLock(map, BLOCK_04H);
        disableLock(map, BLOCK_05H);
        disableLock(map, BLOCK_06H);
        disableLock(map, BLOCK_07H);

        // STATLOCK_1
        disableLock(map, BLOCK_08H);
        disableLock(map, BLOCK_09H);
    }

    private void disableBits2To7OfStatLock1(Map<ST25TNLocks, LockStatus> map) {
        disableLock(map, BLOCK_0AH);
        disableLock(map, BLOCK_0BH);
        disableLock(map, BLOCK_0CH);
        disableLock(map, BLOCK_0DH);
        disableLock(map, BLOCK_0EH);
        disableLock(map, BLOCK_0FH);
    }

    private void disableDynLocksAndSysLock(Map<ST25TNLocks, LockStatus> map) {
        // SYSLOCK
        disableLock(map, BLOCK_2CH_DYNLOCK_SYSLOCK);
        disableLock(map, BLOCK_2DH_PRODUCT_IDENTIFICATION);
        disableLock(map, BLOCK_2EH_ANDEF_CFG);
        disableLock(map, BLOCK_2FH_KILL_PASSWORD);
        disableLock(map, BLOCK_30H_KILL_KEYHOLE);

        // DYNLOCK_0
        disableLock(map, BLOCKS_10H_TO_11H);
        disableLock(map, BLOCKS_12H_TO_13H);
        disableLock(map, BLOCKS_14H_TO_15H);
        disableLock(map, BLOCKS_16H_TO_17H);
        disableLock(map, BLOCKS_18H_TO_19H);
        disableLock(map, BLOCKS_1AH_TO_1BH);
        disableLock(map, BLOCKS_1CH_TO_1DH);
        disableLock(map, BLOCKS_1EH_TO_1FH);

        // DYNLOCK_1
        disableLock(map, BLOCKS_20H_TO_21H);
        disableLock(map, BLOCKS_22H_TO_23H);
        disableLock(map, BLOCKS_24H_TO_25H);
        disableLock(map, BLOCKS_26H_TO_27H);
        disableLock(map, BLOCKS_28H_TO_29H);
        disableLock(map, BLOCKS_2AH_TO_2BH);

        // DYNLOCK_2
        disableLock(map, BLOCKS_34H_TO_35H);
        disableLock(map, BLOCKS_36H_TO_37H);
        disableLock(map, BLOCKS_38H_TO_39H);
        disableLock(map, BLOCKS_3AH_TO_3BH);
        disableLock(map, BLOCKS_3CH_TO_3DH);
        disableLock(map, BLOCKS_3EH_TO_3FH);
    }


    private void restoreBits3To7OfStatLock0() {
        restoreEnabledStatus(BLOCK_03H_CC_FILE);
    }

    private void restoreBits4To7OfStatLock0AndBits0To1OfStatLock1() {
        // STATLOCK_0
        restoreEnabledStatus(BLOCK_04H);
        restoreEnabledStatus(BLOCK_05H);
        restoreEnabledStatus(BLOCK_06H);
        restoreEnabledStatus(BLOCK_07H);

        // STATLOCK_1
        restoreEnabledStatus(BLOCK_08H);
        restoreEnabledStatus(BLOCK_09H);
    }

    private void restoreBits2To7OfStatLock1() {
        restoreEnabledStatus(BLOCK_0AH);
        restoreEnabledStatus(BLOCK_0BH);
        restoreEnabledStatus(BLOCK_0CH);
        restoreEnabledStatus(BLOCK_0DH);
        restoreEnabledStatus(BLOCK_0EH);
        restoreEnabledStatus(BLOCK_0FH);
    }

    private void restoreDynLocksAndSysLock() {
        // SYSLOCK
        restoreEnabledStatus(BLOCK_2CH_DYNLOCK_SYSLOCK);
        restoreEnabledStatus(BLOCK_2DH_PRODUCT_IDENTIFICATION);
        restoreEnabledStatus(BLOCK_2EH_ANDEF_CFG);
        restoreEnabledStatus(BLOCK_2FH_KILL_PASSWORD);
        restoreEnabledStatus(BLOCK_30H_KILL_KEYHOLE);

        // DYNLOCK_0
        restoreEnabledStatus(BLOCKS_10H_TO_11H);
        restoreEnabledStatus(BLOCKS_12H_TO_13H);
        restoreEnabledStatus(BLOCKS_14H_TO_15H);
        restoreEnabledStatus(BLOCKS_16H_TO_17H);
        restoreEnabledStatus(BLOCKS_18H_TO_19H);
        restoreEnabledStatus(BLOCKS_1AH_TO_1BH);
        restoreEnabledStatus(BLOCKS_1CH_TO_1DH);
        restoreEnabledStatus(BLOCKS_1EH_TO_1FH);

        // DYNLOCK_1
        restoreEnabledStatus(BLOCKS_20H_TO_21H);
        restoreEnabledStatus(BLOCKS_22H_TO_23H);
        restoreEnabledStatus(BLOCKS_24H_TO_25H);
        restoreEnabledStatus(BLOCKS_26H_TO_27H);
        restoreEnabledStatus(BLOCKS_28H_TO_29H);
        restoreEnabledStatus(BLOCKS_2AH_TO_2BH);

        // DYNLOCK_2
        restoreEnabledStatus(BLOCKS_34H_TO_35H);
        restoreEnabledStatus(BLOCKS_36H_TO_37H);
        restoreEnabledStatus(BLOCKS_38H_TO_39H);
        restoreEnabledStatus(BLOCKS_3AH_TO_3BH);
        restoreEnabledStatus(BLOCKS_3CH_TO_3DH);
        restoreEnabledStatus(BLOCKS_3EH_TO_3FH);
    }

    private void updateDisplay() {

        try {
            for (ST25TNLocks lock : ST25TNLocks.values()) {
                CheckBox checkBox = getCheckBoxFromLockValue(lock);

                LockStatus currentLockStatus = mCurrentLocksMap.get(lock);
                LockStatus newLockStatus = mNewLocksMap.get(lock);

                checkBox.setChecked(newLockStatus.isLocked);
                checkBox.setEnabled(newLockStatus.enabled);

                // Trick using the "activated" state to get alternate colors
                if (currentLockStatus.enabled) {
                    if (newLockStatus.enabled) {
                        checkBox.setActivated(true);
                    } else {
                        checkBox.setActivated(false);
                    }
                } else {
                    checkBox.setActivated(true);
                }
            }
        } catch (STException e) {
            e.printStackTrace();
        }

        int newStatLock0Value = getNewStatLock0Value();
        int newStatLock1Value = getNewStatLock1Value();
        int newDynLock0Value = getNewDynLock0Value();
        int newDynLock1Value = getNewDynLock1Value();
        int newDynLock2Value = getNewDynLock2Value();
        int newSysLockValue = getNewSysLockValue();

        mStatLock0ValueTextView.setText("0x" + String.format("%02x", newStatLock0Value).toUpperCase());
        mStatLock0ValueTextView.setTextColor(getResources().getColor(newStatLock0Value == mCurrentStatLock0 ? R.color.st_dark_blue : R.color.red));

        mStatLock1ValueTextView.setText("0x" + String.format("%02x", newStatLock1Value).toUpperCase());
        mStatLock1ValueTextView.setTextColor(getResources().getColor(newStatLock1Value == mCurrentStatLock1 ? R.color.st_dark_blue : R.color.red));

        mDynLock0ValueTextView.setText("0x" + String.format("%02x", newDynLock0Value).toUpperCase());
        mDynLock0ValueTextView.setTextColor(getResources().getColor(newDynLock0Value == mCurrentDynLock0 ? R.color.st_dark_blue : R.color.red));

        mDynLock1ValueTextView.setText("0x" + String.format("%02x", newDynLock1Value).toUpperCase());
        mDynLock1ValueTextView.setTextColor(getResources().getColor(newDynLock1Value == mCurrentDynLock1 ? R.color.st_dark_blue : R.color.red));

        mDynLock2ValueTextView.setText("0x" + String.format("%02x", newDynLock2Value).toUpperCase());
        mDynLock2ValueTextView.setTextColor(getResources().getColor(newDynLock2Value == mCurrentDynLock2 ? R.color.st_dark_blue : R.color.red));

        mSysLockValueTextView.setText("0x" + String.format("%02x", newSysLockValue).toUpperCase());
        mSysLockValueTextView.setTextColor(getResources().getColor(newSysLockValue == mCurrentSysLock ? R.color.st_dark_blue : R.color.red));
    }

    private CheckBox getCheckBoxFromLockValue(ST25TNLocks lock) throws STException {
        CheckBox checkbox;

        switch(lock) {
            // STATLOCK_0
            case BLOCK_02H_EXTENDED_LOCK0:
                checkbox = mStatLock0Bit0Checkbox;
                break;
            case BLOCK_02H_EXTENDED_LOCK1:
                checkbox = mStatLock0Bit1Checkbox;
                break;
            case BLOCK_02H_EXTENDED_LOCK2:
                checkbox = mStatLock0Bit2Checkbox;
                break;
            case BLOCK_03H_CC_FILE:
                checkbox = mStatLock0Bit3Checkbox;
                break;
            case BLOCK_04H:
                checkbox = mStatLock0Bit4Checkbox;
                break;
            case BLOCK_05H:
                checkbox = mStatLock0Bit5Checkbox;
                break;
            case BLOCK_06H:
                checkbox = mStatLock0Bit6Checkbox;
                break;
            case BLOCK_07H:
                checkbox = mStatLock0Bit7Checkbox;
                break;

            // STATLOCK_1
            case BLOCK_08H:
                checkbox = mStatLock1Bit0Checkbox;
                break;
            case BLOCK_09H:
                checkbox = mStatLock1Bit1Checkbox;
                break;
            case BLOCK_0AH:
                checkbox = mStatLock1Bit2Checkbox;
                break;
            case BLOCK_0BH:
                checkbox = mStatLock1Bit3Checkbox;
                break;
            case BLOCK_0CH:
                checkbox = mStatLock1Bit4Checkbox;
                break;
            case BLOCK_0DH:
                checkbox = mStatLock1Bit5Checkbox;
                break;
            case BLOCK_0EH:
                checkbox = mStatLock1Bit6Checkbox;
                break;
            case BLOCK_0FH:
                checkbox = mStatLock1Bit7Checkbox;
                break;

            // DYNLOCK_0
            case BLOCKS_10H_TO_11H:
                checkbox = mDynLock0Bit0Checkbox;
                break;
            case BLOCKS_12H_TO_13H:
                checkbox = mDynLock0Bit1Checkbox;
                break;
            case BLOCKS_14H_TO_15H:
                checkbox = mDynLock0Bit2Checkbox;
                break;
            case BLOCKS_16H_TO_17H:
                checkbox = mDynLock0Bit3Checkbox;
                break;
            case BLOCKS_18H_TO_19H:
                checkbox = mDynLock0Bit4Checkbox;
                break;
            case BLOCKS_1AH_TO_1BH:
                checkbox = mDynLock0Bit5Checkbox;
                break;
            case BLOCKS_1CH_TO_1DH:
                checkbox = mDynLock0Bit6Checkbox;
                break;
            case BLOCKS_1EH_TO_1FH:
                checkbox = mDynLock0Bit7Checkbox;
                break;

            // DYNLOCK_1
            case BLOCKS_20H_TO_21H:
                checkbox = mDynLock1Bit0Checkbox;
                break;
            case BLOCKS_22H_TO_23H:
                checkbox = mDynLock1Bit1Checkbox;
                break;
            case BLOCKS_24H_TO_25H:
                checkbox = mDynLock1Bit2Checkbox;
                break;
            case BLOCKS_26H_TO_27H:
                checkbox = mDynLock1Bit3Checkbox;
                break;
            case BLOCKS_28H_TO_29H:
                checkbox = mDynLock1Bit4Checkbox;
                break;
            case BLOCKS_2AH_TO_2BH:
                checkbox = mDynLock1Bit5Checkbox;
                break;

            // SYSLOCK
            case BLOCK_2CH_DYNLOCK_SYSLOCK:
                checkbox = mSysLockBit0Checkbox;
                break;
            case BLOCK_2DH_PRODUCT_IDENTIFICATION:
                checkbox = mSysLockBit1Checkbox;
                break;
            case BLOCK_2EH_ANDEF_CFG:
                checkbox = mSysLockBit2Checkbox;
                break;
            case BLOCK_2FH_KILL_PASSWORD:
                checkbox = mSysLockBit3Checkbox;
                break;
            case BLOCK_30H_KILL_KEYHOLE:
                checkbox = mSysLockBit4Checkbox;
                break;

            // DYNLOCK_2
            case BLOCKS_34H_TO_35H:
                checkbox = mDynLock2Bit2Checkbox;
                break;
            case BLOCKS_36H_TO_37H:
                checkbox = mDynLock2Bit3Checkbox;
                break;
            case BLOCKS_38H_TO_39H:
                checkbox = mDynLock2Bit4Checkbox;
                break;
            case BLOCKS_3AH_TO_3BH:
                checkbox = mDynLock2Bit5Checkbox;
                break;
            case BLOCKS_3CH_TO_3DH:
                checkbox = mDynLock2Bit6Checkbox;
                break;
            case BLOCKS_3EH_TO_3FH:
                checkbox = mDynLock2Bit7Checkbox;
                break;

            default:
                throw new STException(BAD_PARAMETER);
        }

        return checkbox;
    }

    private ST25TNLocks getLockValueFromCheckBox(CheckBox checkbox) throws STException {
        ST25TNLocks lock;

        // STATLOCK_0
        if (checkbox == mStatLock0Bit0Checkbox) {
            lock = BLOCK_02H_EXTENDED_LOCK0;
        } else if (checkbox == mStatLock0Bit1Checkbox) {
            lock = BLOCK_02H_EXTENDED_LOCK1;
        } else if (checkbox == mStatLock0Bit2Checkbox) {
            lock = BLOCK_02H_EXTENDED_LOCK2;
        } else if (checkbox == mStatLock0Bit3Checkbox) {
            lock = BLOCK_03H_CC_FILE;
        } else if (checkbox == mStatLock0Bit4Checkbox) {
            lock = BLOCK_04H;
        } else if (checkbox == mStatLock0Bit5Checkbox) {
            lock = BLOCK_05H;
        } else if (checkbox == mStatLock0Bit6Checkbox) {
            lock = BLOCK_06H;
        } else if (checkbox == mStatLock0Bit7Checkbox) {
            lock = BLOCK_07H;

        // STATLOCK_1
        } else if (checkbox == mStatLock1Bit0Checkbox) {
            lock = BLOCK_08H;
        } else if (checkbox == mStatLock1Bit1Checkbox) {
            lock = BLOCK_09H;
        } else if (checkbox == mStatLock1Bit2Checkbox) {
            lock = BLOCK_0AH;
        } else if (checkbox == mStatLock1Bit3Checkbox) {
            lock = BLOCK_0BH;
        } else if (checkbox == mStatLock1Bit4Checkbox) {
            lock = BLOCK_0CH;
        } else if (checkbox == mStatLock1Bit5Checkbox) {
            lock = BLOCK_0DH;
        } else if (checkbox == mStatLock1Bit6Checkbox) {
            lock = BLOCK_0EH;
        } else if (checkbox == mStatLock1Bit7Checkbox) {
            lock = BLOCK_0FH;

        // DYNLOCK_0
        } else if (checkbox == mDynLock0Bit0Checkbox) {
            lock = BLOCKS_10H_TO_11H;
        } else if (checkbox == mDynLock0Bit1Checkbox) {
            lock = BLOCKS_12H_TO_13H;
        } else if (checkbox == mDynLock0Bit2Checkbox) {
            lock = BLOCKS_14H_TO_15H;
        } else if (checkbox == mDynLock0Bit3Checkbox) {
            lock = BLOCKS_16H_TO_17H;
        } else if (checkbox == mDynLock0Bit4Checkbox) {
            lock = BLOCKS_18H_TO_19H;
        } else if (checkbox == mDynLock0Bit5Checkbox) {
            lock = BLOCKS_1AH_TO_1BH;
        } else if (checkbox == mDynLock0Bit6Checkbox) {
            lock = BLOCKS_1CH_TO_1DH;
        } else if (checkbox == mDynLock0Bit7Checkbox) {
            lock = BLOCKS_1EH_TO_1FH;

        // DYNLOCK1
        } else if (checkbox == mDynLock1Bit0Checkbox) {
            lock = BLOCKS_20H_TO_21H;
        } else if (checkbox == mDynLock1Bit1Checkbox) {
            lock = BLOCKS_22H_TO_23H;
        } else if (checkbox == mDynLock1Bit2Checkbox) {
            lock = BLOCKS_24H_TO_25H;
        } else if (checkbox == mDynLock1Bit3Checkbox) {
            lock = BLOCKS_26H_TO_27H;
        } else if (checkbox == mDynLock1Bit4Checkbox) {
            lock = BLOCKS_28H_TO_29H;
        } else if (checkbox == mDynLock1Bit5Checkbox) {
            lock = BLOCKS_2AH_TO_2BH;

        // SYSLOCK
        } else if (checkbox == mSysLockBit0Checkbox) {
            lock = BLOCK_2CH_DYNLOCK_SYSLOCK;
        } else if (checkbox == mSysLockBit1Checkbox) {
            lock = BLOCK_2DH_PRODUCT_IDENTIFICATION;
        } else if (checkbox == mSysLockBit2Checkbox) {
            lock = BLOCK_2EH_ANDEF_CFG;
        } else if (checkbox == mSysLockBit3Checkbox) {
            lock = BLOCK_2FH_KILL_PASSWORD;
        } else if (checkbox == mSysLockBit4Checkbox) {
            lock = BLOCK_30H_KILL_KEYHOLE;

        // DYNLOCK_2
        } else if (checkbox == mDynLock2Bit2Checkbox) {
            lock = BLOCKS_34H_TO_35H;
        } else if (checkbox == mDynLock2Bit3Checkbox) {
            lock = BLOCKS_36H_TO_37H;
        } else if (checkbox == mDynLock2Bit4Checkbox) {
            lock = BLOCKS_38H_TO_39H;
        } else if (checkbox == mDynLock2Bit5Checkbox) {
            lock = BLOCKS_3AH_TO_3BH;
        } else if (checkbox == mDynLock2Bit6Checkbox) {
            lock = BLOCKS_3CH_TO_3DH;
        } else if (checkbox == mDynLock2Bit7Checkbox) {
            lock = BLOCKS_3EH_TO_3FH;
        } else {
            throw new STException(BAD_PARAMETER);
        }

        return lock;
    }

    public int getNewStatLock0Value() {
        int newValue = 0;

        if (mStatLock0Bit0Checkbox.isChecked()) {
            newValue |= 0x01;
        }
        if (mStatLock0Bit1Checkbox.isChecked()) {
            newValue |= 0x02;
        }
        if (mStatLock0Bit2Checkbox.isChecked()) {
            newValue |= 0x04;
        }
        if (mStatLock0Bit3Checkbox.isChecked()) {
            newValue |= 0x08;
        }
        if (mStatLock0Bit4Checkbox.isChecked()) {
            newValue |= 0x10;
        }
        if (mStatLock0Bit5Checkbox.isChecked()) {
            newValue |= 0x20;
        }
        if (mStatLock0Bit6Checkbox.isChecked()) {
            newValue |= 0x40;
        }
        if (mStatLock0Bit7Checkbox.isChecked()) {
            newValue |= 0x80;
        }

        return newValue;
    }

    public int getNewStatLock1Value() {
        int newValue = 0;

        if (mStatLock1Bit0Checkbox.isChecked()) {
            newValue |= 0x01;
        }
        if (mStatLock1Bit1Checkbox.isChecked()) {
            newValue |= 0x02;
        }
        if (mStatLock1Bit2Checkbox.isChecked()) {
            newValue |= 0x04;
        }
        if (mStatLock1Bit3Checkbox.isChecked()) {
            newValue |= 0x08;
        }
        if (mStatLock1Bit4Checkbox.isChecked()) {
            newValue |= 0x10;
        }
        if (mStatLock1Bit5Checkbox.isChecked()) {
            newValue |= 0x20;
        }
        if (mStatLock1Bit6Checkbox.isChecked()) {
            newValue |= 0x40;
        }
        if (mStatLock1Bit7Checkbox.isChecked()) {
            newValue |= 0x80;
        }

        return newValue;
    }

    public int getNewDynLock0Value() {
        int newValue = 0;

        if (mDynLock0Bit0Checkbox.isChecked()) {
            newValue |= 0x01;
        }
        if (mDynLock0Bit1Checkbox.isChecked()) {
            newValue |= 0x02;
        }
        if (mDynLock0Bit2Checkbox.isChecked()) {
            newValue |= 0x04;
        }
        if (mDynLock0Bit3Checkbox.isChecked()) {
            newValue |= 0x08;
        }
        if (mDynLock0Bit4Checkbox.isChecked()) {
            newValue |= 0x10;
        }
        if (mDynLock0Bit5Checkbox.isChecked()) {
            newValue |= 0x20;
        }
        if (mDynLock0Bit6Checkbox.isChecked()) {
            newValue |= 0x40;
        }
        if (mDynLock0Bit7Checkbox.isChecked()) {
            newValue |= 0x80;
        }

        return newValue;
    }


    public int getNewDynLock1Value() {
        int newValue = 0;

        if (mDynLock1Bit0Checkbox.isChecked()) {
            newValue |= 0x01;
        }
        if (mDynLock1Bit1Checkbox.isChecked()) {
            newValue |= 0x02;
        }
        if (mDynLock1Bit2Checkbox.isChecked()) {
            newValue |= 0x04;
        }
        if (mDynLock1Bit3Checkbox.isChecked()) {
            newValue |= 0x08;
        }
        if (mDynLock1Bit4Checkbox.isChecked()) {
            newValue |= 0x10;
        }
        if (mDynLock1Bit5Checkbox.isChecked()) {
            newValue |= 0x20;
        }

        return newValue;
    }

    public int getNewDynLock2Value() {
        int newValue = 0;

        if (mDynLock2Bit2Checkbox.isChecked()) {
            newValue |= 0x04;
        }
        if (mDynLock2Bit3Checkbox.isChecked()) {
            newValue |= 0x08;
        }
        if (mDynLock2Bit4Checkbox.isChecked()) {
            newValue |= 0x10;
        }
        if (mDynLock2Bit5Checkbox.isChecked()) {
            newValue |= 0x20;
        }
        if (mDynLock2Bit6Checkbox.isChecked()) {
            newValue |= 0x40;
        }
        if (mDynLock2Bit7Checkbox.isChecked()) {
            newValue |= 0x80;
        }

        return newValue;
    }

    public int getNewSysLockValue() {
        int newValue = 0;

        if (mSysLockBit0Checkbox.isChecked()) {
            newValue |= 0x01;
        }
        if (mSysLockBit1Checkbox.isChecked()) {
            newValue |= 0x02;
        }
        if (mSysLockBit2Checkbox.isChecked()) {
            newValue |= 0x04;
        }
        if (mSysLockBit3Checkbox.isChecked()) {
            newValue |= 0x08;
        }
        if (mSysLockBit4Checkbox.isChecked()) {
            newValue |= 0x10;
        }

        return newValue;
    }

    /**
     * Copy Lock Map
     * @param src
     * @param dst
     */
    public void copyLocksMap(Map<ST25TNLocks, LockStatus> src,
                             Map<ST25TNLocks, LockStatus> dst) {

        for (ST25TNLocks lock : ST25TNLocks.values()) {
            LockStatus srcLockStatus = src.get(lock);

            LockStatus dstLockStatus = new LockStatus();
            dstLockStatus.isLocked = srcLockStatus.isLocked;
            dstLockStatus.enabled = srcLockStatus.enabled;

            dst.put(lock, dstLockStatus);
        }
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
                    case READ_LOCKS:
                        mCurrentStatLock0 = mST25TNTag.getStatLock0() & 0xFF;
                        mCurrentStatLock1 = mST25TNTag.getStatLock1() & 0xFF;
                        mCurrentSysLock = mST25TNTag.getSysLock() & 0xFF;
                        mCurrentDynLock0 = mST25TNTag.getDynLock0() & 0xFF;
                        mCurrentDynLock1 = mST25TNTag.getDynLock1() & 0xFF;
                        mCurrentDynLock2 = mST25TNTag.getDynLock2() & 0xFF;

                        for (ST25TNLocks lock : ST25TNLocks.values()) {
                            boolean isLocked = mST25TNTag.isLocked(lock);
                            LockStatus newLockStatus = new LockStatus();
                            newLockStatus.isLocked = isLocked;
                            newLockStatus.enabled = !isLocked;
                            mCurrentLocksMap.put(lock, newLockStatus);
                        }

                        if (!mCurrentLocksMap.get(BLOCK_02H_EXTENDED_LOCK0).enabled) {
                            disableBits3To7OfStatLock0(mCurrentLocksMap);
                        } else if (!mCurrentLocksMap.get(BLOCK_02H_EXTENDED_LOCK1).enabled) {
                            disableBits4To7OfStatLock0AndBits0To1OfStatLock1(mCurrentLocksMap);
                        } else if (!mCurrentLocksMap.get(BLOCK_02H_EXTENDED_LOCK2).enabled) {
                            disableBits2To7OfStatLock1(mCurrentLocksMap);
                        } else if (!mCurrentLocksMap.get(BLOCK_2CH_DYNLOCK_SYSLOCK).enabled) {
                            disableDynLocksAndSysLock(mCurrentLocksMap);
                        }

                        result = ACTION_SUCCESSFUL;
                        break;
                    case WRITE_LOCKS:
                        if (mUpdateCCFileValue) {
                            byte[] ccFile = mST25TNTag.readCCFile();
                            ccFile[3] = 0x0F;
                            mST25TNTag.writeCCFile(ccFile);
                        }

                        for (ST25TNLocks lock : ST25TNLocks.values()) {
                            // Process all the locks excepted the bits locking the StatLock,
                            // SysLock and DynLock bits which should be set last
                            if ((lock != BLOCK_02H_EXTENDED_LOCK0) &&
                                (lock != BLOCK_02H_EXTENDED_LOCK1) &&
                                (lock != BLOCK_02H_EXTENDED_LOCK2) &&
                                (lock != BLOCK_2CH_DYNLOCK_SYSLOCK) ) {
                                writeLock(lock);
                            }
                        }

                        writeLock(BLOCK_02H_EXTENDED_LOCK0);
                        writeLock(BLOCK_02H_EXTENDED_LOCK1);
                        writeLock(BLOCK_02H_EXTENDED_LOCK2);
                        writeLock(BLOCK_2CH_DYNLOCK_SYSLOCK);

                        mCurrentStatLock0 = mST25TNTag.getStatLock0() & 0xFF;
                        mCurrentStatLock1 = mST25TNTag.getStatLock1() & 0xFF;
                        mCurrentSysLock = mST25TNTag.getSysLock() & 0xFF;
                        mCurrentDynLock0 = mST25TNTag.getDynLock0() & 0xFF;
                        mCurrentDynLock1 = mST25TNTag.getDynLock1() & 0xFF;
                        mCurrentDynLock2 = mST25TNTag.getDynLock2() & 0xFF;

                        result = ACTION_SUCCESSFUL;
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

        private void writeLock(ST25TNLocks lock) throws STException {
            LockStatus newLockStatus = mNewLocksMap.get(lock);
            LockStatus currentLockStatus = mCurrentLocksMap.get(lock);

            if (newLockStatus.isLocked != currentLockStatus.isLocked) {
                mST25TNTag.lock(lock);
                currentLockStatus.isLocked = newLockStatus.isLocked;
                // The lock is irreversible so this lock is now disabled
                newLockStatus.enabled = false;
                currentLockStatus.enabled = false;
            }
        }

        @Override
        protected void onPostExecute(ActionStatus actionStatus) {

            switch(actionStatus) {
                case ACTION_SUCCESSFUL:
                    switch(mAction) {
                        case READ_LOCKS:
                            copyLocksMap(mCurrentLocksMap, mNewLocksMap);
                            updateDisplay();
                            break;
                        case WRITE_LOCKS:
                            // New Locks become current Locks
                            copyLocksMap(mNewLocksMap, mCurrentLocksMap);
                            showToast(R.string.tag_updated);
                            updateDisplay();
                            break;
                    }
                    break;

                case ACTION_FAILED:
                    showToast(R.string.command_failed);
                    break;

                case TAG_NOT_IN_THE_FIELD:
                    if (mAction == Action.READ_LOCKS) {
                        UIHelper.displayMessage(ST25TNLockActivity.this, R.string.tag_not_in_the_field_in_lock_screen);
                    } else {
                        showToast(R.string.tag_not_in_the_field);
                    }
                    break;
            }

            return;
        }
    }

}
