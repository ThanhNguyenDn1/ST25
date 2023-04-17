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

package com.st.st25nfc.type5.stlri;

import android.app.Activity;
import android.content.Intent;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.view.MenuItem;

import com.st.st25nfc.generic.AppLauncherActivity;
import com.st.st25nfc.generic.PreferredApplicationActivity;
import com.st.st25nfc.generic.util.UIHelper;
import com.st.st25nfc.type5.Type5SendCustomCmdActivity;
import com.st.st25nfc.type5.stm24lr.STM24LRChangePwdActivity;
import com.st.st25nfc.type5.stm24lr.STM24LRSectorActivity;
import com.st.st25sdk.NFCTag;
import com.st.st25nfc.R;
import com.st.st25nfc.generic.ST25Menu;

public class STLRiS64kMenu extends ST25Menu {
    public STLRiS64kMenu(NFCTag tag) {
        super(tag);
        mMenuResource.add(R.menu.menu_nfc_forum);
        mMenuResource.add(R.menu.menu_lris);
    }

    public boolean selectItem(Activity activity, MenuItem item) {
        // Handle navigation view item clicks here.

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
            case R.id.tag_info:
                //Set tab 0 of ST25DVActivity
                intent = new Intent(activity, STLRiS64kActivity.class);
                intent.putExtra("select_tab", 0);
                activity.startActivityForResult(intent, 1);
                break;
            case R.id.nfc_ndef_editor:
                intent = new Intent(activity, STLRiS64kActivity.class);
                intent.putExtra("select_tab", 1);
                activity.startActivityForResult(intent, 1);
                break;
            case R.id.cc_file:
                intent = new Intent(activity, STLRiS64kActivity.class);
                intent.putExtra("select_tab", 2);
                activity.startActivityForResult(intent, 1);
                break;
            // Product features
            case R.id.sys_file:
                intent = new Intent(activity, STLRiS64kActivity.class);
                intent.putExtra("select_tab", 3);
                activity.startActivityForResult(intent, 1);
                break;
            case R.id.memory_dump:
                intent = new Intent(activity, STLRiS64kActivity.class);
                intent.putExtra("select_tab", 4);
                activity.startActivityForResult(intent, 1);
                break;
            case R.id.app_launcher:
                intent = new Intent(activity, AppLauncherActivity.class);
                activity.startActivityForResult(intent, 1);
                break;
            case R.id.send_custom_cmd:
                intent = new Intent(activity, Type5SendCustomCmdActivity.class);
                activity.startActivityForResult(intent, 1);
                break;
            case R.id.sector_management:
                intent = new Intent(activity, STM24LRSectorActivity.class);
                activity.startActivityForResult(intent, 1);
                break;
            case R.id.passwd_management:
                intent = new Intent(activity, STM24LRChangePwdActivity.class);
                activity.startActivityForResult(intent, 1);
                break;
        }

        DrawerLayout drawer = (DrawerLayout) activity.findViewById(R.id.drawer);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
