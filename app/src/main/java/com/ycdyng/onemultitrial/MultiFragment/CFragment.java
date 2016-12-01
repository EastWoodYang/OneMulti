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

public class CFragment extends MultiFragment implements View.OnClickListener {

    private TextView mTitleTextView;
    private Button mStartFragmentDButton;
    private Button mStartFragmentDForResultButton;
    private Button mSetResultAndFinishFragmentCButton;
    private Button mFinishFragmentCButton;
    private TextView mInfoTextView;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.fragment_c;
    }

    @Override
    protected int getThemeResId() {
        return R.style.CustomAppTheme_2;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTargetFragment();
    }

    @Override
    protected void onCreateView(View rootView, ViewGroup container, Bundle savedInstanceState) {
        mTitleTextView = (TextView) rootView.findViewById(R.id.title_text_view);
        mStartFragmentDButton = (Button) rootView.findViewById(R.id.start_fragment_d_button);
        mStartFragmentDForResultButton = (Button) rootView.findViewById(R.id.start_fragment_d_for_result_button);
        mSetResultAndFinishFragmentCButton = (Button) rootView.findViewById(R.id.set_result_and_finish_fragment_c_button);
        mFinishFragmentCButton = (Button) rootView.findViewById(R.id.finish_fragment_c_button);
        mInfoTextView = (TextView) rootView.findViewById(R.id.info_text_view);

        mStartFragmentDButton.setOnClickListener(this);
        mStartFragmentDForResultButton.setOnClickListener(this);

        if(getTargetFragment() != null) {
            mTitleTextView.setText("Fragment C (should setResult)");
            mSetResultAndFinishFragmentCButton.setVisibility(View.VISIBLE);
            mSetResultAndFinishFragmentCButton.setOnClickListener(this);
        } else {
            mTitleTextView.setText("Fragment C");
            mSetResultAndFinishFragmentCButton.setVisibility(View.GONE);
        }

        mFinishFragmentCButton.setOnClickListener(this);

        mTitleTextView.postDelayed(new Runnable() {
            @Override
            public void run() {
                setInfo();
            }
        }, 250);
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
            case R.id.start_fragment_d_button:
                Intent startDFragmentIntent = new Intent(getActivity(), DFragment.class);
                startFragment(startDFragmentIntent);
                break;

            case R.id.start_fragment_d_for_result_button:
                Intent startDFragmentForResultIntent = new Intent(getActivity(), DFragment.class);
                startFragmentForResult(startDFragmentForResultIntent, 102);
                break;

            case R.id.set_result_and_finish_fragment_c_button:
                //setResult(RESULT_OK);
                setResult(RESULT_OK, null);
                finish();
                break;

            case R.id.finish_fragment_c_button:
                finish();
                break;

        }
    }

    @Override
    public void onFragmentResult(int requestCode, int resultCode, Intent data) {
        super.onFragmentResult(requestCode, resultCode, data);
        Toast.makeText(getContext(), "onFragmentResult: requestCode " + requestCode + ", resultCode " + resultCode, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d("BFragment", "setUserVisibleHint: " + isVisibleToUser);
    }

}
