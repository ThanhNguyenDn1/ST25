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

import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.st.st25nfc.R;
import com.st.st25nfc.generic.AppLauncherActivity;
import com.st.st25nfc.generic.CheckSignatureActivity;
import com.st.st25nfc.generic.PreferredApplicationActivity;
import com.st.st25nfc.generic.RegistersActivity;
import com.st.st25nfc.generic.ST25Menu;
import com.st.st25nfc.generic.ndef.NDEFEditorFragment;
import com.st.st25nfc.generic.util.UIHelper;
import com.st.st25sdk.NFCTag;
import com.st.st25sdk.SignatureInterface;
import com.st.st25sdk.type2.st25tn.ST25TNTag;

public class ST25TNMenu extends ST25Menu {
    public ST25TNMenu(NFCTag tag) {
        super(tag);

        ST25TNTag st25TNTag = (ST25TNTag) tag;

        mMenuResource.add(R.menu.menu_nfc_forum);
        mMenuResource.add(R.menu.menu_st25tn);

        if (tag instanceof SignatureInterface) {
            mMenuResource.add(R.menu.menu_signature);
        }

        if (!st25TNTag.isST25TN512()) {
            mMenuResource.add(R.menu.menu_st25tn_memory_configuration);
        }
    }

    @Override
    public boolean selectItem(Activity activity, MenuItem item) {
        Intent intent;
        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.preferred_application:
                intent = new Intent(activity, PreferredApplicationActivity.class);
                activity.startActivityForResult(intent, 1);
                break;
            case R.id.about:
                UIHelper.displayAboutDialogBox(activity);
                break;
            case R.id.product_name:
            // Nfc forum
            case R.id.tag_info:
                //Set tab 0 of ST25TNActivity
                intent = new Intent(activity, ST25TNActivity.class);
                intent.putExtra("select_tab", 0);
                activity.startActivityForResult(intent, 1);
                break;
            case R.id.nfc_ndef_editor:
                intent = new Intent(activity, ST25TNActivity.class);
                intent.putExtra("select_tab", 1);
                activity.startActivityForResult(intent, 1);
                break;
            case R.id.cc_file:
                intent = new Intent(activity, ST25TNActivity.class);
                intent.putExtra("select_tab", 2);
                activity.startActivityForResult(intent, 1);
                break;
            case R.id.memory_configuration:
                intent = new Intent(activity, ST25TNChangeMemConfActivity.class);
                activity.startActivityForResult(intent, 1);
                break;
            // Product features
            case R.id.memory_dump:
                intent = new Intent(activity, ST25TNActivity.class);
                intent.putExtra("select_tab", 4);
                activity.startActivityForResult(intent, 1);
                break;
            case R.id.app_launcher:
                intent = new Intent(activity, AppLauncherActivity.class);
                activity.startActivityForResult(intent, 1);
                break;
            case R.id.signature_menu:
                intent = new Intent(activity,  CheckSignatureActivity.class);
                activity.startActivityForResult(intent, 1);
                break;
            case R.id.kill_menu:
                intent = new Intent(activity,  ST25TNKillTagActivity.class);
                activity.startActivityForResult(intent, 1);
                break;
            case R.id.lock_menu:
                intent = new Intent(activity,  ST25TNLockActivity.class);
                activity.startActivityForResult(intent, 1);
                break;
            case R.id.andef_menu:
                NDEFEditorFragment.setDisplayAndefContent();
                intent = new Intent(activity, ST25TNActivity.class);
                intent.putExtra("select_tab", 1);
                activity.startActivityForResult(intent, 1);
                break;
            case R.id.register_management:
                intent = new Intent(activity, RegistersActivity.class);
                activity.startActivityForResult(intent, 1);
                break;
             default:
                break;
        }

        DrawerLayout drawer = (DrawerLayout) activity.findViewById(R.id.drawer);
        drawer.closeDrawer(GravityCompat.START);
        return true;

    }

}
