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

package com.st.st25nfc.type4;

import android.os.Bundle;
import com.google.android.material.navigation.NavigationView;
import androidx.fragment.app.FragmentManager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.st.st25nfc.R;
import com.st.st25nfc.generic.MainActivity;
import com.st.st25nfc.generic.PwdDialogFragment;
import com.st.st25nfc.generic.STFragmentActivity;
import com.st.st25sdk.STException;
import com.st.st25sdk.type4a.STType4GpoInterface;
import com.st.st25sdk.type4a.STType4Tag;

import java.util.ArrayList;
import java.util.List;


public class GpoConfigActivity extends STFragmentActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Set here the Toolbar to use for this activity
    private int toolbar_res = R.menu.toolbar_empty;

    static final String TAG = "GpoConfigActivity";
    private STType4Tag myTag;

    FragmentManager mFragmentManager;

    ArrayList<String> mGPOStates;
    private RadioButton[] mGpoRadioButton;

    private boolean mGpoFeaturesAvailable;
    private Boolean mGpoRFChangeEnable;
    private boolean mGPOConfRetrievalOnGoing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.default_layout);

        // Inflate content of FrameLayout
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_content);
        View childView = getLayoutInflater().inflate(R.layout.fragment_gpo_conf, null);
        frameLayout.addView(childView);

        myTag = (STType4Tag) MainActivity.getTag();
        if (myTag == null) {
            showToast(R.string.invalid_tag);
            goBackToMainActivity();
            return;
        }

        mFragmentManager = getSupportFragmentManager();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        mMenu.inflateMenu(navigationView);
        try {
            if (((STType4GpoInterface) myTag) instanceof STType4GpoInterface) {
                Button sendInterruptTagButton = (Button) findViewById(R.id.sendInterruptTagButton);
                sendInterruptTagButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendInterrupt();
                    }
                });
                Button bEnablestategpo = (Button) findViewById(R.id.bEnablestategpo);
                bEnablestategpo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendStateControlCommand();
                    }
                });

                mGpoFeaturesAvailable = true;
                enableGPOFeatures(mGpoFeaturesAvailable);

                // build dynamic GPO configuration
                buildDynamicGPOConfiguration();


            } else {
                mGpoFeaturesAvailable = false;
                enableGPOFeatures(false);
            }
        } catch (Exception e) {
            // in case of the Tag is not GPO enabled
            mGpoFeaturesAvailable = false;
            enableGPOFeatures(false);
        }
    }

    private void enableGPOFeatures(boolean enable) {
        int visibility = enable?View.VISIBLE:View.INVISIBLE;

        // Features
        Button updateTagButton = (Button) findViewById(R.id.updateTagButton);
        updateTagButton.setVisibility(visibility);
        Button sendInterruptTagButton = (Button) findViewById(R.id.sendInterruptTagButton);
        sendInterruptTagButton.setVisibility(visibility);
        Button bEnablestategpo = (Button) findViewById(R.id.bEnablestategpo);
        bEnablestategpo.setVisibility(visibility);

        // Descriptions
        if (enable) {
            TextView headerNotAvailable = (TextView) findViewById(R.id.gpoNotAvailableView);
            headerNotAvailable.setVisibility(View.INVISIBLE);
            TextView header = (TextView) findViewById(R.id.gpoConfHeaderTextView);
            header.setVisibility(View.VISIBLE);
        } else {
            TextView headerNotAvailable = (TextView) findViewById(R.id.gpoNotAvailableView);
            headerNotAvailable.setVisibility(View.VISIBLE);
            TextView header = (TextView) findViewById(R.id.gpoConfHeaderTextView);
            header.setVisibility(View.INVISIBLE);
        }
    }

    private void createRadioButton(ArrayList<String> gpoStates, boolean writeEnable) {
        RadioGroup rgp = (RadioGroup) findViewById(R.id.gpoRadioGroup);
        rgp.setOrientation(LinearLayout.VERTICAL);
        mGpoRadioButton = new RadioButton[gpoStates.size()];
        for (int i = 0; i < gpoStates.size(); i++) {
            mGpoRadioButton[i] = new RadioButton(this);
            mGpoRadioButton[i].setId(i);
            mGpoRadioButton[i].setText("- " + mGpoRadioButton[i].getId() + " " + gpoStates.get(i));
            rgp.addView(mGpoRadioButton[i]);
        }
        rgp.setEnabled(writeEnable);
        setCheckableRadioButton(rgp,writeEnable);

    }

    private void setCheckableRadioButton(RadioGroup rgp, boolean checked) {
        for(int i = 0; i < rgp.getChildCount(); i++){
            ((RadioButton)rgp.getChildAt(i)).setEnabled(checked);
        }
    }

    private void updateRadioButtonStatus(byte gpo, RadioButton[] gpoRb) {
        try {
            STType4GpoInterface.GpoMode mode = ((STType4GpoInterface) myTag).getGpoMode(gpo);
            List<STType4GpoInterface.GpoMode> listOfModes = ((STType4GpoInterface) myTag).getSupportedGpoModes();
            int index = listOfModes.indexOf(mode);
            gpoRb[index].setChecked(true);
        } catch (STException e) {
            e.printStackTrace();
            return;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGpoFeaturesAvailable && !mGPOConfRetrievalOnGoing) getCurrentGpoConf();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds read_list_items to the action bar if it is present.
        getMenuInflater().inflate(toolbar_res, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long

        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    private void buildDynamicGPOConfiguration() {
        mGPOConfRetrievalOnGoing = true;
        new Thread(new Runnable() {
            public void run() {
                mGpoRFChangeEnable = ((STType4GpoInterface) myTag).isGpoConfigurableByRf();
                try {
                    List<STType4GpoInterface.GpoMode> gpoModeList = ((STType4GpoInterface) myTag).getSupportedGpoModes();
                    mGPOStates = new ArrayList<>(gpoModeList.size());
                    for (Object object : gpoModeList) {
                        mGPOStates.add(object != null ? object.toString() : null);
                    }

                } catch (STException e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {
                    public void run() {
                        createRadioButton(mGPOStates, mGpoRFChangeEnable);
                        Button updateTagButton = (Button) findViewById(R.id.updateTagButton);
                        updateTagButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                changeTagGpoConf();
                            }
                        });
                        updateTagButton.setEnabled(mGpoRFChangeEnable);
                        updateTagButton.setVisibility(mGpoRFChangeEnable ? View.VISIBLE : View.INVISIBLE);

                        if (mGpoFeaturesAvailable) getCurrentGpoConf();
                        mGPOConfRetrievalOnGoing = false;
                    }
                });
            }
        }).start();
    }

    private void getCurrentGpoConf() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    final byte gpoField = ((STType4GpoInterface)myTag).getGpo();
                    final RadioButton[] gpoStatus = mGpoRadioButton;
                    // Post an action to UI Thead to update the widgets
                    runOnUiThread(new Runnable() {
                        public void run() {
                            updateRadioButtonStatus(gpoField, gpoStatus);
                         }
                    });

                } catch (STException e) {
                    switch (e.getError()) {
                        case TAG_NOT_IN_THE_FIELD:
                            showToast(R.string.tag_not_in_the_field);
                            break;
                        default:
                            e.printStackTrace();
                            showToast(R.string.error_while_reading_the_tag);
                    }
                }
            }
        }).start();
    }

    private void changeTagGpoConf() {
        new Thread(new Runnable() {
            public void run() {
                Log.v(TAG, "changeTagGpoConf");
                RadioGroup rgp = (RadioGroup) findViewById(R.id.gpoRadioGroup);
                int selection = rgp.getCheckedRadioButtonId();
                try {
                    STType4GpoInterface.GpoMode mode = ((STType4GpoInterface)myTag).getSupportedGpoModes().get(selection);
                    ((STType4GpoInterface)myTag).setGpoMode(mode);
                    showToast(R.string.tag_updated_with_gpo_mode, mode.toString());
                } catch (STException e) {
                    switch (e.getError()) {
                        case TAG_NOT_IN_THE_FIELD:
                            showToast(R.string.tag_not_in_the_field);
                            break;
                        case CONFIG_PASSWORD_NEEDED:
                            displayPasswordDialogBox();
                            break;
                        default:
                            e.printStackTrace();
                            showToast(R.string.error_while_updating_the_tag);
                    }
                }
            }
        }).start();
    }

    private void sendInterrupt() {
        new Thread(new Runnable() {
            public void run() {
                Log.v(TAG, "sendInterrupt");
                try {
                    if (!((STType4GpoInterface)myTag).isGpoInInterruptedMode()) {
                        showToast(R.string.tag_not_in_a_correct_configuration_mode);
                    } else {
                        ((STType4GpoInterface)myTag).sendInterruptCommand();
                        showToast(R.string.tag_updated);
                    }
                } catch (STException e) {
                    switch (e.getError()) {
                        case TAG_NOT_IN_THE_FIELD:
                            showToast(R.string.tag_not_in_the_field);
                            break;
                        case CONFIG_PASSWORD_NEEDED:
                            displayPasswordDialogBox();
                            break;
                        default:
                            e.printStackTrace();
                            showToast(R.string.error_while_updating_the_tag);
                    }
                }

            }
        }).start();
    }

    private void sendStateControlCommand() {
        new Thread(new Runnable() {
            public void run() {
                Log.v(TAG, "sendStateControlCommand");
                // Check that GPO is correctly configured
                try {
                    if (!((STType4GpoInterface)myTag).isGpoInStateControlMode()) {
                        showToast(R.string.tag_not_in_a_correct_configuration_mode);
                    } else {

                        int value = 0;
                        // Select the value
                        boolean _GPOTogglecheck = ((Switch) findViewById(R.id.tbEnablestategpo)).isChecked();
                        value = _GPOTogglecheck == true ? 1 : 0;
                        ((STType4GpoInterface)myTag).setStateControlCommand(value);

                        showToast(R.string.tag_updated);
                    }
                } catch (STException e) {
                    switch (e.getError()) {
                        case TAG_NOT_IN_THE_FIELD:
                            showToast(R.string.tag_not_in_the_field);
                            break;
                        case CONFIG_PASSWORD_NEEDED:
                            displayPasswordDialogBox();
                            break;
                        default:
                            e.printStackTrace();
                            showToast(R.string.error_while_updating_the_tag);
                    }
                }

            }
        }).start();
    }


    private void displayPasswordDialogBox() {
        Log.v(TAG, "displayPasswordDialogBox");
    }

    public void onPwdDialogFinish(int result) {
        Log.v(TAG, "onPwdDialogFinish. result = " + result);
        if (result == PwdDialogFragment.RESULT_OK) {
            // Config password has been entered successfully so we can now retry to change the memory configuration
            changeTagGpoConf();
        } else {
            Log.e(TAG, "Action failed! Tag not updated!");
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        return mMenu.selectItem(this, item);
    }
}
