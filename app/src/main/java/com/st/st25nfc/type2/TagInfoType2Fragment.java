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

package com.st.st25nfc.type2;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.st.st25nfc.R;
import com.st.st25nfc.generic.MainActivity;
import com.st.st25nfc.generic.STFragment;
import com.st.st25sdk.Helper;
import com.st.st25sdk.NFCTag;
import com.st.st25sdk.STException;
import com.st.st25sdk.type2.Type2Tag;
import com.st.st25sdk.type2.st25tn.ST25TNTag;

public class TagInfoType2Fragment extends STFragment {

    private Type2Tag mType2Tag;

    private TextView mUidView;
    private TextView mProductVersionTextView;
    private TextView mProductCodeTextView;
    private TextView mManufacturerNameView;
    private TextView mTagNameView;
    private TextView mTagDescriptionView;
    private TextView mTagTypeView;
    private TextView mTagSizeView;
    private TextView mTLVsAreaSizeView;
    private TextView mT2TAreaSizeView;
    private TextView mTechListView;


    public static TagInfoType2Fragment newInstance(Context context) {
        TagInfoType2Fragment f = new TagInfoType2Fragment();
        /* If needed, pass some argument to the fragment
        Bundle args = new Bundle();
        args.putInt("index", index);
        f.setArguments(args);
        */

        // Set the title of this fragment
        f.setTitle(context.getResources().getString(R.string.tag_info));

        return f;
    }

    public TagInfoType2Fragment() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_type2_tag_info, container, false);
        mView = view;

        NFCTag nfcTag = MainActivity.getTag();
        if ((nfcTag == null) || (!(nfcTag instanceof Type2Tag))) {
            showToast(R.string.invalid_tag);
            return null;
        }

        mType2Tag = (Type2Tag) nfcTag;

        mTagNameView = mView.findViewById(R.id.model_header);
        mTagTypeView = mView.findViewById(R.id.model_type);
        mTagDescriptionView = mView.findViewById(R.id.model_description);
        mProductVersionTextView = mView.findViewById(R.id.productVersionTextView);
        mProductCodeTextView = mView.findViewById(R.id.productCodeTextView);
        LinearLayout productVersionLayout = mView.findViewById(R.id.productVersionLayout);
        LinearLayout productCodeLayout = mView.findViewById(R.id.productVersionLayout);

        mManufacturerNameView = mView.findViewById(R.id.manufacturer_name);
        mUidView = mView.findViewById(R.id.uid);
        mTagSizeView = mView.findViewById(R.id.memory_size);
        mTechListView = mView.findViewById(R.id.tech_list);
        mTLVsAreaSizeView = mView.findViewById(R.id.tlvsAreaTextView);
        mT2TAreaSizeView = mView.findViewById(R.id.t2tAreaTextView);

        if (mType2Tag instanceof ST25TNTag) {
            productVersionLayout.setVisibility(View.VISIBLE);
            productCodeLayout.setVisibility(View.VISIBLE);
        } else {
            productVersionLayout.setVisibility(View.GONE);
            productCodeLayout.setVisibility(View.GONE);
        }

        initView();

        return (View) view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private class FillViewTask extends STFragment.FillViewTask {
        String mManufacturerName;
        String mUid;
        String mTagName;
        String mTagDescription;
        String mProductVersion;
        String mProductCode;
        String mTagType;
        String mTagSize;
        String mTLVsAreaSize;
        String mT2TAreaSize;
        String mTechList;

        public FillViewTask() {
        }

        @Override
        protected Integer doInBackground(NFCTag... param) {

            if (myTag != null) {
                try {
                    mTagName = myTag.getName();
                    mTagDescription = myTag.getDescription();
                    mTagType = myTag.getTypeDescription();
                    mManufacturerName = ": " + myTag.getManufacturerName();
                    mUid = ": " + Helper.convertHexByteArrayToString(myTag.getUid()).toUpperCase();
                    mTagSize = ": " + String.valueOf(myTag.getMemSizeInBytes()) + " bytes";
                    mTLVsAreaSize = ": " + String.valueOf(mType2Tag.getTlvsAreaSizeInBytes()) + " bytes";
                    mT2TAreaSize = ": " + String.valueOf(mType2Tag.getT2TAreaSizeInBytes()) + " bytes";
                    mTechList = ": " + TextUtils.join("\n ", myTag.getTechList());

                    if (mType2Tag instanceof ST25TNTag) {
                        ST25TNTag st25TNTag = (ST25TNTag) mType2Tag;
                        byte productVersion = st25TNTag.getProductVersion();
                        int version = (productVersion >> 4);
                        int subVersion = (productVersion & 0x0F);
                        mProductCode = "0x" + String.format("%04x", st25TNTag.getProductCode()).toUpperCase();
                        mProductVersion = version + "." + subVersion;
                    }

                } catch (STException e) {
                    return -1;

                }
            }

            return 0;
        }


        @Override
        protected void onPostExecute(Integer result) {

            if (result == 0) {
                if (mManufacturerNameView != null && mUidView != null) {
                    mTagNameView.setText(mTagName);
                    mTagTypeView.setText(mTagType);
                    mTagDescriptionView.setText(mTagDescription);

                    if (mType2Tag instanceof ST25TNTag) {
                        mProductCodeTextView.setText(mProductCode);
                        mProductVersionTextView.setText(mProductVersion);
                    }

                    mManufacturerNameView.setText(mManufacturerName);
                    mUidView.setText(mUid);
                    mTagSizeView.setText(mTagSize);
                    mTechListView.setText(mTechList);
                    mTLVsAreaSizeView.setText(mTLVsAreaSize);
                    mT2TAreaSizeView.setText(mT2TAreaSize);
                }
            }
            return;

        }
    }

    @Override
    public void fillView() {
        new FillViewTask().execute(myTag);
    }
}
