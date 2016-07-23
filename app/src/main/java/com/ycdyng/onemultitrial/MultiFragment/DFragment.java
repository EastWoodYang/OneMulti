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

public class DFragment extends MultiFragment implements View.OnClickListener {

    private TextView mTitleTextView;
    private Button mStartFragmentAButton;
    private Button mStartFragmentAForResultButton;
    private Button mSetResultAndFinishFragmentDButton;
    private Button mFinishFragmentDButton;
    private Button mStartFragmentBWithFlagClearTopButton;
    private Button mStartFragmentBWithFlagClearAllButton;
    private Button mStartFragmentBWithFlagSingleTopButton;
    private Button mStartFragmentBWithFlagSingleInstanceButton;

    private TextView mInfoTextView;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_d;
    }

    @Override
    protected int getThemeResId() {
        return R.style.CustomAppTheme_3;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTargetFragment();
    }

    @Override
    protected void onCreateView(View rootView, ViewGroup container, Bundle savedInstanceState) {
        mTitleTextView = (TextView) rootView.findViewById(R.id.title_text_view);
        mStartFragmentAButton = (Button) rootView.findViewById(R.id.start_fragment_a_button);
        mStartFragmentAForResultButton = (Button) rootView.findViewById(R.id.start_fragment_a_for_result_button);
        mSetResultAndFinishFragmentDButton = (Button) rootView.findViewById(R.id.set_result_and_finish_fragment_d_button);
        mFinishFragmentDButton = (Button) rootView.findViewById(R.id.finish_fragment_d_button);
        mStartFragmentBWithFlagClearTopButton = (Button) rootView.findViewById(R.id.start_fragment_b_with_flag_clear_top);
        mStartFragmentBWithFlagClearAllButton = (Button) rootView.findViewById(R.id.start_fragment_b_with_flag_clear_all);
        mStartFragmentBWithFlagSingleTopButton = (Button) rootView.findViewById(R.id.start_fragment_b_with_flag_single_top);
        mStartFragmentBWithFlagSingleInstanceButton = (Button) rootView.findViewById(R.id.start_fragment_b_with_flag_single_instance);
        mInfoTextView = (TextView) rootView.findViewById(R.id.info_text_view);

        mStartFragmentAButton.setOnClickListener(this);
        mStartFragmentAForResultButton.setOnClickListener(this);

        if(getTargetFragment() != null) {
            mTitleTextView.setText("Fragment D (should setResult)");
            mSetResultAndFinishFragmentDButton.setVisibility(View.VISIBLE);
            mSetResultAndFinishFragmentDButton.setOnClickListener(this);

            mStartFragmentBWithFlagClearTopButton.setVisibility(View.GONE);
            mStartFragmentBWithFlagClearAllButton.setVisibility(View.GONE);
            mStartFragmentBWithFlagSingleTopButton.setVisibility(View.GONE);
            mStartFragmentBWithFlagSingleInstanceButton.setVisibility(View.GONE);
        } else {
            mTitleTextView.setText("Fragment D");
            mSetResultAndFinishFragmentDButton.setVisibility(View.GONE);

            mStartFragmentBWithFlagClearTopButton.setVisibility(View.VISIBLE);
            mStartFragmentBWithFlagClearAllButton.setVisibility(View.VISIBLE);
            mStartFragmentBWithFlagSingleTopButton.setVisibility(View.VISIBLE);
            mStartFragmentBWithFlagSingleInstanceButton.setVisibility(View.VISIBLE);
            mStartFragmentBWithFlagClearTopButton.setOnClickListener(this);
            mStartFragmentBWithFlagClearAllButton.setOnClickListener(this);
            mStartFragmentBWithFlagSingleTopButton.setOnClickListener(this);
            mStartFragmentBWithFlagSingleInstanceButton.setOnClickListener(this);
        }

        mFinishFragmentDButton.setOnClickListener(this);

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
            case R.id.start_fragment_a_button:
                Intent startAFragmentIntent = new Intent(getActivity(), AFragment.class);
                startFragment(startAFragmentIntent);
                break;

            case R.id.start_fragment_a_for_result_button:
                Intent startAFragmentForResultIntent = new Intent(getActivity(), AFragment.class);
                startFragmentForResult(startAFragmentForResultIntent, 103);
                break;

            case R.id.set_result_and_finish_fragment_d_button:
                //setResult(RESULT_OK);
                setResult(RESULT_OK, null);
                finish();
                break;

            case R.id.finish_fragment_d_button:
                finish();
                break;

            case R.id.start_fragment_b_with_flag_clear_top:
                Intent startBFragmentWithFlagClearTopIntent = new Intent(getActivity(), BFragment.class);
                startBFragmentWithFlagClearTopIntent.setFlags(LaunchMode.FLAG_FRAGMENT_CLEAR_TOP);
                startFragment(startBFragmentWithFlagClearTopIntent);
                break;

            case R.id.start_fragment_b_with_flag_clear_all:
                Intent startBFragmentWithFlagClearAllIntent = new Intent(getActivity(), BFragment.class);
                startBFragmentWithFlagClearAllIntent.setFlags(LaunchMode.FLAG_FRAGMENT_CLEAR_ALL);
                startFragment(startBFragmentWithFlagClearAllIntent);
                break;

            case R.id.start_fragment_b_with_flag_single_instance:
                Intent startBFragmentWithFlagSingleInstanceIntent = new Intent(getActivity(), BFragment.class);
                startBFragmentWithFlagSingleInstanceIntent.setFlags(LaunchMode.FLAG_FRAGMENT_SINGLE_INSTANCE);
                startFragment(startBFragmentWithFlagSingleInstanceIntent);
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
