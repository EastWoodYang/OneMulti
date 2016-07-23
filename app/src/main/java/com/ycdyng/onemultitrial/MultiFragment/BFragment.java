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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ycdyng.onemulti.MultiFragment;
import com.ycdyng.onemultitrial.R;

import java.util.List;

public class BFragment extends MultiFragment implements View.OnClickListener {

    private TextView mTitleTextView;
    private Button mStartFragmentCButton;
    private Button mStartFragmentCForResultButton;
    private Button mStartFragmentCWithFlagNoHistoryButton;
    private Button mSetResultAndFinishFragmentBButton;
    private Button mFinishFragmentBButton;
    private TextView mInfoTextView;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_b;
    }

    @Override
    protected int getThemeResId() {
        return R.style.CustomAppTheme_1;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTargetFragment();
    }

    @Override
    protected void onCreateView(View rootView, ViewGroup container, Bundle savedInstanceState) {
        mTitleTextView = (TextView) rootView.findViewById(R.id.title_text_view);
        mStartFragmentCButton = (Button) rootView.findViewById(R.id.start_fragment_c_button);
        mStartFragmentCForResultButton = (Button) rootView.findViewById(R.id.start_fragment_c_for_result_button);
        mStartFragmentCWithFlagNoHistoryButton = (Button) rootView.findViewById(R.id.start_fragment_c_with_flag_no_history);
        mSetResultAndFinishFragmentBButton = (Button) rootView.findViewById(R.id.set_result_and_finish_fragment_b_button);
        mFinishFragmentBButton = (Button) rootView.findViewById(R.id.finish_fragment_b_button);
        mInfoTextView = (TextView) rootView.findViewById(R.id.info_text_view);

        mStartFragmentCButton.setOnClickListener(this);
        mStartFragmentCForResultButton.setOnClickListener(this);
        mStartFragmentCWithFlagNoHistoryButton.setOnClickListener(this);

        if(getTargetFragment() != null) {
            mTitleTextView.setText("Fragment B (should setResult)");
            mSetResultAndFinishFragmentBButton.setVisibility(View.VISIBLE);
            mSetResultAndFinishFragmentBButton.setOnClickListener(this);
        } else {
            mTitleTextView.setText("Fragment B");
            mSetResultAndFinishFragmentBButton.setVisibility(View.GONE);
        }

        mFinishFragmentBButton.setOnClickListener(this);

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
            case R.id.start_fragment_c_button:
                Intent startFragmentCIntent = new Intent(getActivity(), CFragment.class);
                startFragment(startFragmentCIntent);
                break;

            case R.id.start_fragment_c_for_result_button:
                Intent startFragmentCForResultIntent = new Intent(getActivity(), CFragment.class);
                startFragmentForResult(startFragmentCForResultIntent, 101);
                break;

            case R.id.start_fragment_c_with_flag_no_history:
                Intent startFragmentCWithFlagNoHistoryIntent = new Intent(getActivity(), CFragment.class);
                startFragmentCWithFlagNoHistoryIntent.setFlags(LaunchMode.FLAG_FRAGMENT_NO_HISTORY);
                startFragment(startFragmentCWithFlagNoHistoryIntent);
                break;

            case R.id.set_result_and_finish_fragment_b_button:
                //setResult(RESULT_OK);
                setResult(RESULT_OK, null);
                finish();
                break;

            case R.id.finish_fragment_b_button:
                finish();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Toast.makeText(getContext(), "onActivityResult: requestCode " + requestCode + ", resultCode " + resultCode, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d("BFragment", "setUserVisibleHint: " + isVisibleToUser);
    }

}
