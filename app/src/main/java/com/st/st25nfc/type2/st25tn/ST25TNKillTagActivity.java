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

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;
import com.st.st25nfc.R;
import com.st.st25nfc.generic.MainActivity;
import com.st.st25nfc.generic.PwdDialogFragment;
import com.st.st25nfc.generic.STFragmentActivity;
import com.st.st25nfc.generic.util.UIHelper;
import com.st.st25sdk.NFCTag;
import com.st.st25sdk.STException;
import com.st.st25sdk.type2.st25tn.ST25TNTag;

import static com.st.st25nfc.type2.st25tn.ST25TNKillTagActivity.Action.LOCK_KILL_FEATURE;
import static com.st.st25nfc.type2.st25tn.ST25TNKillTagActivity.Action.LOCK_KILL_PASSWORD;
import static com.st.st25nfc.type2.st25tn.ST25TNKillTagActivity.Action.READ_LOCK_STATUS;
import static com.st.st25sdk.type2.st25tn.ST25TNTag.ST25TNLocks.*;
import static com.st.st25nfc.type2.st25tn.ST25TNKillTagActivity.Action.KILL_TAG;
import static com.st.st25nfc.type2.st25tn.ST25TNKillTagActivity.Action.SET_KILL_PWD;
import static com.st.st25nfc.type2.st25tn.ST25TNKillTagActivity.ActionStatus.ACTION_FAILED;
import static com.st.st25nfc.type2.st25tn.ST25TNKillTagActivity.ActionStatus.ACTION_CANCELED;
import static com.st.st25nfc.type2.st25tn.ST25TNKillTagActivity.ActionStatus.ACTION_SUCCESSFUL;
import static com.st.st25nfc.type2.st25tn.ST25TNKillTagActivity.ActionStatus.TAG_NOT_IN_THE_FIELD;


public class ST25TNKillTagActivity extends STFragmentActivity implements PwdDialogFragment.PwdDialogListener, NavigationView.OnNavigationItemSelectedListener {

    enum Action {
        KILL_TAG,
        SET_KILL_PWD,
        READ_LOCK_STATUS,
        LOCK_KILL_FEATURE,
        LOCK_KILL_PASSWORD
    };

    enum ActionStatus {
        ACTION_SUCCESSFUL,
        ACTION_FAILED,
        ACTION_CANCELED,
        TAG_NOT_IN_THE_FIELD,
        CONFIG_PASSWORD_NEEDED
    };

    // Set here the Toolbar to use for this activity
    private int toolbar_res = R.menu.toolbar_empty;

    static final String TAG = "KillActivity";
    private Handler mHandler;
    FragmentManager mFragmentManager;
    private Action mCurrentAction;
    private ST25TNTag mST25TNTag;
    private PwdDialogFragment mPwdDialogFragment;

    private Button mChangeKillPasswordButton;
    private Button mKillButton;

    private CheckBox mKillTagLockCheckbox;
    private CheckBox mKillPasswordLockCheckbox;

    boolean mIsKillLocked = false;
    boolean mIsKillPasswordLocked = false;

    // Kill Password Length in bytes
    private final int KILL_PASSWORD_LENGTH = 4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.default_layout);

        // Inflate content of FrameLayout
        FrameLayout frameLayout = findViewById(R.id.frame_content);
        View childView = getLayoutInflater().inflate(R.layout.fragment_st25tn_kill_tag, null);
        frameLayout.addView(childView);

        TextView warningTextView = findViewById(R.id.warningTextView);

        NFCTag nfcTag = MainActivity.getTag();
        if (nfcTag instanceof ST25TNTag) {
            mST25TNTag = (ST25TNTag) nfcTag;
        } else {
            showToast(R.string.invalid_tag);
            goBackToMainActivity();
            return;
        }

        mHandler = new Handler();
        mFragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        mMenu.inflateMenu(navigationView);

        mKillButton = (Button) findViewById(R.id.killButton);
        mKillButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeAsynchronousAction(KILL_TAG);

            }
        });

        mChangeKillPasswordButton = (Button) findViewById(R.id.changeKillPasswordButton);
        mChangeKillPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeAsynchronousAction(SET_KILL_PWD);
            }
        });

        /*
        Button lockKillPasswordButton = (Button) findViewById(R.id.lockKillPasswordButton);
        lockKillPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askConfirmation();
            }
        });

         */

        mKillTagLockCheckbox = findViewById(R.id.killTagLockCheckbox);
        mKillTagLockCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked && !mIsKillLocked) {
                        askLockKillFeatureConfirmation();
                    }
                    updateUI();
                }
            }
        );

        mKillPasswordLockCheckbox = findViewById(R.id.killPasswordLockCheckbox);
        mKillPasswordLockCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked && !mIsKillPasswordLocked) {
                        askLockKillPasswordConfirmation();
                    }
                    updateUI();
                }
            }
        );
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!STFragmentActivity.tagChanged(this, mST25TNTag)) {
            // The same tag has been taped again
            executeAsynchronousAction(READ_LOCK_STATUS);
        }
    }

    private void askLockKillFeatureConfirmation() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle(getString(R.string.confirmation_needed));

        // set dialog message
        alertDialogBuilder
                .setMessage(getString(R.string.type2_lock_kill_feature_question))
                .setCancelable(true)

                .setPositiveButton(getString(R.string.lock_kill_feature),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                        lockKillFeature();
                    }
                })
                .setNegativeButton(getString(R.string.cancel),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                        mKillTagLockCheckbox.setChecked(false);
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private void askLockKillPasswordConfirmation() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle(getString(R.string.confirmation_needed));

        // set dialog message
        alertDialogBuilder
                .setMessage(getString(R.string.type2_lock_kill_pwd_question))
                .setCancelable(true)

                .setPositiveButton(getString(R.string.lock_password),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                        lockKillPassword();
                    }
                })
                .setNegativeButton(getString(R.string.cancel),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                        mKillPasswordLockCheckbox.setChecked(false);
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private void lockKillFeature() {
        executeAsynchronousAction(LOCK_KILL_FEATURE);
    }

    private void lockKillPassword() {
        executeAsynchronousAction(LOCK_KILL_PASSWORD);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_empty, menu);

        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long

        // as you specify a parent activity in AndroidManifest.xml.


        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        return mMenu.selectItem(this, item);
    }

    private void updateUI() {

        if (mIsKillLocked) {
            mKillTagLockCheckbox.setEnabled(false);
            mKillButton.setEnabled(false);
            mKillButton.setBackgroundResource(R.drawable.light_grey_round_area);
        } else {
            mKillTagLockCheckbox.setEnabled(true);
            mKillButton.setEnabled(true);
            mKillButton.setBackgroundResource(R.drawable.light_blue_round_area);
        }

        if (mIsKillPasswordLocked) {
            mKillPasswordLockCheckbox.setEnabled(false);
            mChangeKillPasswordButton.setEnabled(false);
            mChangeKillPasswordButton.setBackgroundResource(R.drawable.light_grey_round_area);
        } else {
            mKillPasswordLockCheckbox.setEnabled(true);
            mChangeKillPasswordButton.setEnabled(true);
            mChangeKillPasswordButton.setBackgroundResource(R.drawable.light_blue_round_area);
        }

    }

    @Override
    public void onPwdDialogFinish(int result, byte[] password) {

    }

    private void executeAsynchronousAction(Action action) {
        Log.d(TAG, "Starting background action " + action);
        mCurrentAction = action;
        new myAsyncTask(action).execute();
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
                UIHelper.displayCircularProgressBar(ST25TNKillTagActivity.this, getString(R.string.please_wait));

                switch (mAction) {
                    case READ_LOCK_STATUS:
                        mIsKillLocked = mST25TNTag.isLocked(BLOCK_30H_KILL_KEYHOLE);
                        mIsKillPasswordLocked = mST25TNTag.isLocked(BLOCK_2FH_KILL_PASSWORD);
                        result = ACTION_SUCCESSFUL;
                        break;

                    case LOCK_KILL_FEATURE:
                        mST25TNTag.lock(BLOCK_30H_KILL_KEYHOLE);
                        mIsKillLocked = true;
                        result = ACTION_SUCCESSFUL;
                        break;
                    case LOCK_KILL_PASSWORD:
                        mST25TNTag.lock(BLOCK_2FH_KILL_PASSWORD);
                        mIsKillPasswordLocked = true;
                        result = ACTION_SUCCESSFUL;
                        break;

                    case KILL_TAG:
                        mPwdDialogFragment = PwdDialogFragment.newInstance(getString(R.string.enter_kill_pwd),
                                mFragmentManager,
                                ST25TNKillTagActivity.this,
                                KILL_PASSWORD_LENGTH);

                        // Get the password typed by the user
                        byte[] password = mPwdDialogFragment.getPassword();
                        if (password != null) {
                            mST25TNTag.kill(password);
                            result = ACTION_SUCCESSFUL;
                        } else {
                            result = ACTION_CANCELED;
                        }
                        break;

                    case SET_KILL_PWD:
                        mPwdDialogFragment = PwdDialogFragment.newInstance(getString(R.string.enter_new_kill_pwd),
                                mFragmentManager,
                                ST25TNKillTagActivity.this,
                                KILL_PASSWORD_LENGTH);

                        // Get the password typed by the user
                        password = mPwdDialogFragment.getPassword();
                        if (password != null) {
                            mST25TNTag.setKillPassword(password);
                            result = ACTION_SUCCESSFUL;
                        } else {
                            result = ACTION_CANCELED;
                        }
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

                    default:
                        e.printStackTrace();
                        result = ACTION_FAILED;
                        break;
                }
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
                        case READ_LOCK_STATUS:
                            if (mIsKillLocked) {
                                mKillTagLockCheckbox.setChecked(true);
                            }
                            if (mIsKillPasswordLocked) {
                                mKillPasswordLockCheckbox.setChecked(true);
                            }
                            updateUI();
                            break;
                        case KILL_TAG:
                            showToast(R.string.tag_killed);
                            break;
                        case SET_KILL_PWD:
                            showToast(R.string.kill_password_updated);
                            break;
                        case LOCK_KILL_FEATURE:
                        case LOCK_KILL_PASSWORD:
                            showToast(R.string.tag_updated);
                            updateUI();
                            break;
                    }
                    break;

                case ACTION_CANCELED:
                    break;

                case ACTION_FAILED:
                    showToast(R.string.command_failed);
                    break;

                case TAG_NOT_IN_THE_FIELD:
                    showToast(R.string.tag_not_in_the_field);
                    break;
            }

            return;
        }
    }

}
