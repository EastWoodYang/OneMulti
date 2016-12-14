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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.EditText;

public abstract class MultiFragment extends Fragment implements OneMulti {

    private static final String TAG = "MultiFragment";

    /**
     * Standard fragment result: operation canceled.
     */
    public static final int RESULT_CANCELED = 0;
    /**
     * Standard fragment result: operation succeeded.
     */
    public static final int RESULT_OK = -1;

    public OneActivity mActivity;
    public Context mContext;

    int launchMode;

    int mResultCode = RESULT_CANCELED;
    Intent mResultData = null;

    private View mRootView;

    private int mColorPrimaryDark;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (DEBUG) Log.d(TAG, getClass().getSimpleName() + ": onAttach");
        mContext = context;
        if (context instanceof OneActivity) {
            mActivity = (OneActivity) context;
        } else {
            throw new ClassCastException(mActivity.toString() + " must extends MultiFragment");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) Log.d(TAG, getClass().getSimpleName() + ": onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (DEBUG) Log.d(TAG, getClass().getSimpleName() + ": onCreateView");
        int layoutResId = getLayoutResourceId();
        if (layoutResId <= 0) {
            throw new IllegalStateException("Layout resource id can't be zero");
        }
        int themeResId = getThemeResId();
        if (themeResId != R.style.AppTheme) {
            ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(getActivity(), themeResId);
            inflater = inflater.cloneInContext(contextThemeWrapper);
        }
        mColorPrimaryDark = getColorPrimaryDark(inflater.getContext().getTheme());
        setStatusBarColor();

        mRootView = inflater.inflate(layoutResId, container, false);

        onCreateView(mRootView, container, savedInstanceState);
        return mRootView;
    }

    @Override
    public void onViewCreated(View view,Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUserVisibleHint(true);
    }

    protected abstract int getLayoutResourceId();

    protected abstract void onCreateView(View rootView, ViewGroup container, Bundle savedInstanceState);

    @Override
    public Animation onCreateAnimation(final int transit, final boolean enter, final int nextAnim) {
        if (nextAnim == 0) {
            return null;
        }
//        if(enter) {
//            if(nextAnim == StartAnimations[0]) {
//                ViewCompat.setTranslationZ(getView(), 1.0f);
//            } else {
//                ViewCompat.setTranslationZ(getView(), 0.0f);
//            }
//        } else {
//            if(nextAnim == BackAnimations[0]) {
//                ViewCompat.setTranslationZ(getView(), 1.0f);
//            } else {
//                ViewCompat.setTranslationZ(getView(), 0.0f);
//            }
//        }
        Animation animation = AnimationUtils.loadAnimation(getActivity(), nextAnim);
        animation.setAnimationListener(new Animation.AnimationListener() {

            private int mLayerType;

            @Override
            public void onAnimationStart(Animation animation) {
                if(getView() != null) {
                    mLayerType = getView().getLayerType();
                    getView().setLayerType(View.LAYER_TYPE_HARDWARE, null);
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
//                if(getView() != null) {
//                    getView().setLayerType(mLayerType, null);
//                    getView().post(new Runnable() {
//                        @Override
//                        public void run() {
//                            if(getView() != null) {
//                                ViewCompat.setTranslationZ(getView(), 0.0f);
//                            }
//                        }
//                    });
//                }
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

    protected int getThemeResId() {
        return R.style.AppTheme;
    }

    public View findViewById(int id) {
        if(mRootView != null) {
            return mRootView.findViewById(id);
        } else {
            return null;
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        mActivity.startActivityForResult(intent, requestCode);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void startActivityForResult(Intent intent, int requestCode, Bundle options) {
        mActivity.startActivityForResult(intent, requestCode, options);
    }

    protected final void startFragment(Intent intent) {
        mActivity.startFragment(intent);
    }

    protected final void startFragmentWithoutAnimation(Intent intent) {
        mActivity.startFragment(intent, false);
    }

    protected final void startFragmentBehaveBack(Intent intent) {
        mActivity.startFragment(intent, OneMulti.BackAnimations);
    }

    protected final void startFragmentForResult(Intent intent, int requestCode) {
        mActivity.startFragmentForResult(intent, requestCode);
    }

    protected final void startActivityFragment(Context context, Bundle bundle, String fragmentName) {
        Intent oneIntent = new Intent(context, OneActivity.class);
        oneIntent.putExtras(bundle);
        oneIntent.putExtra(FRAGMENT_NAME, fragmentName);
        startActivity(oneIntent);
    }

    protected final void startActivityFragmentForResult(Context context, Bundle bundle, String fragmentName, int requestCode) {
        Intent oneIntent = new Intent(context, OneActivity.class);
        oneIntent.putExtras(bundle);
        oneIntent.putExtra(FRAGMENT_NAME, fragmentName);
        startActivityForResult(oneIntent, requestCode);
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

    public int getResultCode() {
        return mResultCode;
    }

    public Intent getResultData() {
        return mResultData;
    }

    public void finish() {
        mActivity.finishFragment();
    }

    public void finishWithoutAnimation() {
        mActivity.finishFragment(false);
    }

    public void finishActivity() {
        synchronized (this) {
            mActivity.finishActivity();
        }
    }

    public void onFragmentResult(int requestCode, int resultCode, Intent data) {

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

    protected boolean onBackPressed() {
        return false;
    }

    public int getLaunchMode() {
        return launchMode;
    }

    public void setLaunchMode(int launchMode) {
        this.launchMode = launchMode;
    }

    public void setStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && mColorPrimaryDark != 0) {
            Window window = mActivity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(mColorPrimaryDark);
        }
    }

    public void setStatusBarColor(int colorPrimaryDark) {
        mColorPrimaryDark = colorPrimaryDark;
        setStatusBarColor();
    }

    public int getColorPrimaryDark(Resources.Theme theme) {
        int color = 0; // a default
        final TypedArray appearance = theme.obtainStyledAttributes(new int[] {android.R.attr.colorPrimaryDark});
        if (0 < appearance.getIndexCount()) {
            int attr = appearance.getIndex(0);
            color = appearance.getColor(attr, color);
        }
        appearance.recycle();
        return color;
    }
}