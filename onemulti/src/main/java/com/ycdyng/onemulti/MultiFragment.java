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

package com.ycdyng.onemulti;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.EditText;

import java.lang.ref.WeakReference;

public abstract class MultiFragment extends Fragment implements OneMulti{

    private static final String TAG = "MultiFragment";

    /** Standard fragment result: operation canceled. */
    public static final int RESULT_CANCELED    = 0;
    /** Standard fragment result: operation succeeded. */
    public static final int RESULT_OK           = -1;

    public OneActivity mActivity;
    public Context mContext;

    int launchMode;

    int mResultCode = RESULT_CANCELED;
    Intent mResultData = null;

    private WeakReference<OnFragmentTransactionListener> mOnFragmentTransactionListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, getClass().getSimpleName() + ": onAttach");
        mContext = context;
        if(context instanceof OneActivity) {
            mActivity = (OneActivity) context;
        } else {
            throw new ClassCastException(mActivity.toString() + " must extends MultiFragment");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, getClass().getSimpleName() + ": onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Log.d(TAG, getClass().getSimpleName() + ": onCreateView");
        View view = null;
        int layoutResId = getLayoutResId();
        if(layoutResId <= 0) {
            throw new IllegalStateException("Layout resource id can't be zero");
        }
        int themeResId = getThemeResId();
        if(themeResId <= 0) {
            view = inflater.inflate(layoutResId, container, false);
        } else {
            final ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(getActivity(), themeResId);
            LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
            view = localInflater.inflate(layoutResId, container, false);
        }
        view.setClickable(true);
        onCreateView(view, container, savedInstanceState);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, getClass().getSimpleName() + ": onViewCreated");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, getClass().getSimpleName() + ": onActivityCreated");
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Log.d(TAG, getClass().getSimpleName() + ": onViewStateRestored");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, getClass().getSimpleName() + ": onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, getClass().getSimpleName() + ": onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, getClass().getSimpleName() + ": onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, getClass().getSimpleName() + ": onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, getClass().getSimpleName() + ": onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, getClass().getSimpleName() + ": onDestroy");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, getClass().getSimpleName() + ": onDetach");
    }

    protected abstract int getLayoutResId();

    protected abstract void onCreateView(View view, ViewGroup container, Bundle savedInstanceState);

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if(nextAnim != 0) {
            Animation animation = AnimationUtils.loadAnimation(getActivity(), nextAnim);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    OnFragmentTransactionListener listener = mOnFragmentTransactionListener.get();
                    if(listener != null) {
                        listener.onTransactionEnd();
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            // NOTE: the animation must be added to an animation set in order for the listener
            // to work on the exit animation
            AnimationSet animSet = new AnimationSet(true);
            animSet.addAnimation(animation);
            return animSet;
        }
        return null;
    }

    protected int getThemeResId() {
        return R.style.AppTheme;
    }

    protected final void startFragment(Intent intent) {
        mActivity.startFragment(intent);
    }

    protected final void startFragmentWithoutAnimation(Intent intent) {
        mActivity.startFragment(intent, false);
    }

    protected final void startFragmentForResult(Intent intent, int requestCode) {
        mActivity.startFragmentForResult(intent, requestCode);
    }

    /**
     * Call this to set the result that current fragment will return to its
     * caller.
     */
    public final void setResult(int resultCode) {
        mResultCode = resultCode;
        mResultData = null;
        synchronized (this) {
            mActivity.setFragmentResult(mResultCode);
        }
    }

    /**
     * Call this to set the result that current fragment will return to its
     * caller.
     */
    public final void setResult(int resultCode, Intent data) {
        mResultCode = resultCode;
        mResultData = data;
        synchronized (this) {
            mActivity.setFragmentResult(mResultCode, mResultData);
        }
    }

    public void finish() {
        mActivity.finishFragment();
    }

    public void finishActivity() {
        synchronized (this) {
            mActivity.finishActivity();
        }
    }

    public void showSoftInput(EditText editText) {
        mActivity.showSoftInput(editText);
    }

    public void hiddenSoftInput() {
        mActivity.hiddenSoftInput();
    }

    public boolean backPressed() {
        long currentTime = System.currentTimeMillis();
        if ((currentTime - mActivity.touchTime) >= mActivity.waitTime) {
            mActivity.touchTime = currentTime;
            return onBackPressed();
        } else {
            return true;
        }
    }

    public void onFragmentResume() {

    }

    protected boolean onBackPressed() {
        return false;
    }

    public interface OnFragmentTransactionListener {
        void onTransactionEnd();
    }

    public void setOnFragmentTransactionListener(OnFragmentTransactionListener listener) {
        this.mOnFragmentTransactionListener = new WeakReference<OnFragmentTransactionListener>(listener);
    }

    public int getLaunchMode() {
        return launchMode;
    }

    public void setLaunchMode(int launchMode) {
        this.launchMode = launchMode;
    }
}