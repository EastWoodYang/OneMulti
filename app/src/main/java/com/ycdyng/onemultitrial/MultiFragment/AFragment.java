/*
 * Copyright 2016 EastWood Yang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ycdyng.onemultitrial.MultiFragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ycdyng.onemulti.MultiFragment;
import com.ycdyng.onemultitrial.R;

import java.util.List;

public class AFragment extends MultiFragment implements View.OnClickListener {

    private TextView mTitleTextView;
    private Button mStartFragmentBButton;
    private Button mStartFragmentBForResultButton;
    private Button mStartFragmentCAndFinishFragmentAButton;
    private Button mFinishFragmentAButton;
    private Button mFinishActivityAButton;
    private TextView mInfoTextView;

    private long mWaitTime = 2000;
    private long mTouchTime = 0;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_a;
    }

    @Override
    protected int getThemeResId() {
        return R.style.CustomAppTheme_0;
    }

    @Override
    protected void onCreateView(View rootView, ViewGroup container, Bundle savedInstanceState) {
        mTitleTextView = (TextView) rootView.findViewById(R.id.title_text_view);
        mStartFragmentBButton = (Button) rootView.findViewById(R.id.start_fragment_b_button);
        mStartFragmentBForResultButton = (Button) rootView.findViewById(R.id.start_fragment_b_for_result_button);
        mStartFragmentCAndFinishFragmentAButton = (Button) rootView.findViewById(R.id.start_fragment_c_and_finish_fragment_a_button);
        mFinishFragmentAButton = (Button) rootView.findViewById(R.id.finish_fragment_a_button);
        mFinishActivityAButton = (Button) rootView.findViewById(R.id.finish_activity_button);
        mInfoTextView = (TextView) rootView.findViewById(R.id.info_text_view);

        mStartFragmentBButton.setOnClickListener(this);
        mStartFragmentBForResultButton.setOnClickListener(this);
        mStartFragmentCAndFinishFragmentAButton.setOnClickListener(this);

        if(getTargetFragment() != null) {
            mTitleTextView.setText("Fragment A (should setResult)");
            mStartFragmentCAndFinishFragmentAButton.setVisibility(View.GONE);
        } else {
            mTitleTextView.setText("Fragment A");
            mStartFragmentCAndFinishFragmentAButton.setVisibility(View.VISIBLE);
            mStartFragmentCAndFinishFragmentAButton.setOnClickListener(this);
        }

        mFinishFragmentAButton.setOnClickListener(this);
        mFinishActivityAButton.setOnClickListener(this);

        setInfo();
    }

    private void setInfo() {
        String info = "";
        List<Fragment> currentFragments = getActivity().getSupportFragmentManager().getFragments();
        info += "name           added visible detached removed hidden";
        for (int i = 0; i < currentFragments.size(); i++) {
            Fragment fragment = currentFragments.get(i);
            if(fragment != null) {
                info += "\n" + fragment.getClass().getSimpleName() + "   " + fragment.isAdded() + "     " + fragment.isVisible() + "    " + fragment.isDetached() + "         " + fragment.isRemoving() + "        " + fragment.isHidden();
            }
        }
        mInfoTextView.setText(info);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_fragment_b_button:
                if(Build.VERSION.SDK_INT >= 23) {
                    if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 10001);
                        return;
                    }
                }
                Intent startBIntent = new Intent(getActivity(), BFragment.class);
                startFragment(startBIntent);
                break;

            case R.id.start_fragment_b_for_result_button:
                Intent startBForResultIntent = new Intent(getActivity(), BFragment.class);
                startFragmentForResult(startBForResultIntent, 100);
                break;

            case R.id.start_fragment_c_and_finish_fragment_a_button:
                Intent startCAndFinishIntent = new Intent(getActivity(), CFragment.class);
                startFragment(startCAndFinishIntent);
                finish();
                break;

            case R.id.finish_fragment_a_button:
                finish();
                break;

            case R.id.finish_activity_button:
                finishActivity();
                 break;
        }
    }

    @Override
    public boolean backPressed() {
        long currentTime = System.currentTimeMillis();
        if ((currentTime - mTouchTime) >= mWaitTime) {
            mTouchTime = currentTime;
            Toast.makeText(mContext, "Push back again !", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            finishActivity();
            return true;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Toast.makeText(getContext(), "onActivityResult: requestCode " + requestCode + ", resultCode " + resultCode, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 10001: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent startBIntent = new Intent(getActivity(), BFragment.class);
                    startFragment(startBIntent);
                }
                return;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d("AFragment", "setUserVisibleHint: " + isVisibleToUser);
    }

}
