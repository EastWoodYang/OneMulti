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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class OneActivity extends FragmentActivity implements OneMulti{

    static final String TAG = "OneActivity";

    int mResultCode = RESULT_CANCELED;
    Intent mResultData = null;

    FragmentManager mFragmentManager;
    FragmentTransaction mCurTransaction = null;
    Fragment mCurrentPrimaryItem = null;

    public InputMethodManager mInputMethodManager = null;

    float mTranslationZ = 0.0f;

    public long waitTime = 250;
    public long touchTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        mFragmentManager = getSupportFragmentManager();

        String className = null;
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(FRAGMENT_NAME)) {
            className =  intent.getStringExtra(FRAGMENT_NAME);
        } else {
            Class cls = getDefaultFragment();
            if(cls != null) {
                className = cls.getName();
            }
        }
        if(className != null) {
            initFragment(className, intent == null ? null : intent.getExtras());
        }
    }

    protected Class<? extends MultiFragment> getDefaultFragment() {
        return null;
    }

    public void initFragment(String className, Bundle bundle) {
        mCurTransaction = mFragmentManager.beginTransaction();
        Fragment fragment = getFragment(className);
        fragment.setArguments(bundle);
        String name = makeFragmentName(getClass().getSimpleName(), className);
        if (DEBUG) Log.v(TAG, "Adding item #" + name);
        mCurTransaction.add(android.R.id.content, fragment, name);
        mCurTransaction.commitAllowingStateLoss();
        mCurTransaction = null;
        mCurrentPrimaryItem = fragment;
    }

    public void startFragment(Intent intent) {
        startFragment(intent, StartAnimations);
    }

    public void startFragment(Intent intent, boolean anim) {
        int[] customAnimations = null;
        if(anim) customAnimations = StartAnimations;
        startFragment(intent, customAnimations);
    }

    public void startFragment(Intent intent, int[] customAnimations) {
        hiddenSoftInput();
        clearAvailIndices();
        mCurTransaction = mFragmentManager.beginTransaction();
        if(customAnimations != null && customAnimations.length >= 2) {
            mCurTransaction.setCustomAnimations(customAnimations[0], customAnimations[1]);
        }
        mCurTransaction.setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        String name = makeFragmentName(getClass().getSimpleName(), intent.getComponent().getClassName());

        Fragment fragment = null;
        if(intent.getFlags() == LaunchMode.FLAG_FRAGMENT_CLEAR_TOP) {
            // at the top of the history stack ?
            if(mCurrentPrimaryItem.getClass().getName().equals(intent.getComponent().getClassName())) {   // Yes
                fragment = mCurrentPrimaryItem;
                mCurTransaction.attach(mCurrentPrimaryItem);
            } else {
                // Do we already have this fragment?
                fragment = mFragmentManager.findFragmentByTag(name);
                if(fragment != null) {  // Yes, we have
                    mCurTransaction.attach(fragment);

                    int attachFragmentIndex = getFragmentIndex(fragment);
                    List<Fragment> availableFragments = getAvailableFragments();
                    for (int i = availableFragments.size() - 1; i >= 0 ; i--) {
                        if(fragment != availableFragments.get(i)) {
                            Fragment tempFragment = availableFragments.get(i);
                            int index = getFragmentIndex(tempFragment);
                            if(index > attachFragmentIndex) {
                                mCurTransaction.remove(availableFragments.get(i));
                            }
                        }
                    }
                } else {
                    fragment = getFragment(intent.getComponent().getClassName());
                    fragment.setArguments(intent.getExtras());
                    if (DEBUG) Log.v(TAG, "Adding item #" + name);
                    mCurTransaction.add(android.R.id.content, fragment, name);
                    if(mCurrentPrimaryItem != null) {
                        mCurTransaction.detach(mCurrentPrimaryItem);
                    }
                }
            }
        } else if(intent.getFlags() == LaunchMode.FLAG_FRAGMENT_CLEAR_ALL) {
            // add new
            fragment = getFragment(intent.getComponent().getClassName());
            fragment.setArguments(intent.getExtras());
            if (DEBUG) Log.v(TAG, "Adding item #" + name);
            mCurTransaction.add(android.R.id.content, fragment, name);

            // remove all
            List<Fragment> availableFragments = getAvailableFragments();
            for (int i = availableFragments.size() - 1; i >= 0 ; i--) {
                mCurTransaction.remove(availableFragments.get(i));
            }
        } else if(intent.getFlags() == LaunchMode.FLAG_FRAGMENT_SINGLE_INSTANCE) {
            // Do we already have this fragment?
            fragment = mFragmentManager.findFragmentByTag(name);
            if(fragment != null) {  // Yes, we have
                mCurTransaction.attach(fragment);

                if(mCurrentPrimaryItem != null) {
                    mCurTransaction.detach(mCurrentPrimaryItem);
                }
            } else {
                fragment = getFragment(intent.getComponent().getClassName());
                fragment.setArguments(intent.getExtras());
                if (DEBUG) Log.v(TAG, "Adding item #" + name);
                mCurTransaction.add(android.R.id.content, fragment, name);

                if(mCurrentPrimaryItem != null) {
                    if(mCurrentPrimaryItem instanceof MultiFragment && ((MultiFragment) mCurrentPrimaryItem).getLaunchMode() == LaunchMode.FLAG_FRAGMENT_NO_HISTORY) {
                        mCurTransaction.remove(mCurrentPrimaryItem);
                    } else {
                        mCurTransaction.detach(mCurrentPrimaryItem);
                    }
                }
            }
        } else if(intent.getFlags() == LaunchMode.FLAG_FRAGMENT_SINGLE_TOP) {
            if(mCurrentPrimaryItem.getClass().getName().equals(intent.getComponent().getClassName())) {
                mCurTransaction.attach(mCurrentPrimaryItem);
            } else {
                // Do we already have this fragment?
                fragment = mFragmentManager.findFragmentByTag(name);
                if(fragment != null) {
                    int currentFragmentIndex = getFragmentIndex(mCurrentPrimaryItem);
                    int fragmentIndex = getFragmentIndex(fragment);
                    if(fragmentIndex > currentFragmentIndex) {
                        mCurTransaction.attach(fragment);
                    } else {
                        fragment = getFragment(intent.getComponent().getClassName());
                        fragment.setArguments(intent.getExtras());
                        if (DEBUG) Log.v(TAG, "Adding item #" + name);
                        mCurTransaction.add(android.R.id.content, fragment, name);
                    }
                } else {
                    fragment = getFragment(intent.getComponent().getClassName());
                    fragment.setArguments(intent.getExtras());
                    if (DEBUG) Log.v(TAG, "Adding item #" + name);
                    mCurTransaction.add(android.R.id.content, fragment, name);
                }

                if(mCurrentPrimaryItem != null) {
                    if(mCurrentPrimaryItem instanceof MultiFragment && ((MultiFragment) mCurrentPrimaryItem).getLaunchMode() == LaunchMode.FLAG_FRAGMENT_NO_HISTORY) {
                        mCurTransaction.remove(mCurrentPrimaryItem);
                    } else {
                        mCurTransaction.detach(mCurrentPrimaryItem);
                    }
                }
            }
        } else {
            fragment = getFragment(intent.getComponent().getClassName());
            fragment.setArguments(intent.getExtras());
            if (DEBUG) Log.v(TAG, "Adding item #" + name);
            mCurTransaction.add(android.R.id.content, fragment, name);
            if(intent.getFlags() == LaunchMode.FLAG_FRAGMENT_NO_HISTORY && fragment instanceof MultiFragment) {
                ((MultiFragment) fragment).setLaunchMode(LaunchMode.FLAG_FRAGMENT_NO_HISTORY);
            }

            if(mCurrentPrimaryItem != null) {
                if(mCurrentPrimaryItem instanceof MultiFragment && ((MultiFragment) mCurrentPrimaryItem).getLaunchMode() == LaunchMode.FLAG_FRAGMENT_NO_HISTORY) {
                    mCurTransaction.remove(mCurrentPrimaryItem);
                } else {
                    mCurTransaction.detach(mCurrentPrimaryItem);
                }
            }
        }

        if(mCurrentPrimaryItem != null && mCurrentPrimaryItem.getView() != null) {
            mCurrentPrimaryItem.getView().post(new Runnable() {
                @Override
                public void run() {
                    finishOp();
                }
            });
        } else {
            finishOp();
        }

        if (fragment != mCurrentPrimaryItem) {
            if (mCurrentPrimaryItem != null) {
                mCurrentPrimaryItem.setMenuVisibility(false);
                mCurrentPrimaryItem.setUserVisibleHint(false);
            }
            if (fragment != null) {
                fragment.setMenuVisibility(true);
                fragment.setUserVisibleHint(true);
                mCurrentPrimaryItem = fragment;
            }
        }
    }

    private void finishOp() {
        if (mCurTransaction != null) {
            mCurTransaction.commitAllowingStateLoss();
            mCurTransaction = null;
        }
    }

    public final void startFragmentForResult(Intent intent, int requestCode) {
        startFragmentForResult(intent, requestCode, StartAnimations);
    }

    public final void startFragmentForResult(Intent intent, int requestCode, boolean anim) {
        int[] customAnimations = null;
        if(anim) customAnimations = StartAnimations;
        startFragmentForResult(intent, requestCode, customAnimations);
    }

    private final void startFragmentForResult(Intent intent, int requestCode, int[] customAnimations) {
        hiddenSoftInput();
        clearAvailIndices();
        mCurTransaction = mFragmentManager.beginTransaction();
        if(customAnimations != null && customAnimations.length >= 2) {
            mCurTransaction.setCustomAnimations(customAnimations[0], customAnimations[1]);
        }

        String name = makeFragmentName(getClass().getSimpleName(), intent.getComponent().getClassName());
        Fragment fragment = getFragment(intent.getComponent().getClassName());
        fragment.setTargetFragment(mCurrentPrimaryItem, requestCode);
        fragment.setArguments(intent.getExtras());
        if(intent.getFlags() == LaunchMode.FLAG_FRAGMENT_NO_HISTORY && fragment instanceof MultiFragment) {
            ((MultiFragment) fragment).setLaunchMode(LaunchMode.FLAG_FRAGMENT_NO_HISTORY);
        }
        if (DEBUG) Log.v(TAG, "Adding item #" + name);
        mCurTransaction.add(android.R.id.content, fragment, name);
        mCurTransaction.hide(mCurrentPrimaryItem);
        if (fragment != mCurrentPrimaryItem) {
            if (mCurrentPrimaryItem != null) {
                mCurrentPrimaryItem.setMenuVisibility(false);
                mCurrentPrimaryItem.setUserVisibleHint(false);
            }
            fragment.setMenuVisibility(true);
            fragment.setUserVisibleHint(true);
            mCurrentPrimaryItem = fragment;
        }

        mCurTransaction.commitAllowingStateLoss();
        mCurTransaction = null;
    }

    public final void setFragmentResult(int resultCode) {
        mResultCode = resultCode;
        mResultData = null;
    }

    public final void setFragmentResult(int resultCode, Intent data) {
        mResultCode = resultCode;
        mResultData = data;
    }

    public final void finishFragment() {
        finishFragment(BackAnimations);
    }

    public final void finishFragment(boolean anim) {
        int[] customAnimations = null;
        if(anim) customAnimations = StartAnimations;
        finishFragment(customAnimations);
    }

    public final void finishFragment(int[] customAnimations) {
        hiddenSoftInput();
        if (mCurTransaction == null) {
            int availableFragmentCount = getAvailableFragmentCount();
            if(availableFragmentCount <= 1) {
                finishActivity();
            } else {
                mCurTransaction = mFragmentManager.beginTransaction();
                if(customAnimations != null && customAnimations.length >= 2) {
                    mCurTransaction.setCustomAnimations(customAnimations[0], customAnimations[1]);
                }
                Fragment targetFragment = mCurrentPrimaryItem.getTargetFragment();
                if(targetFragment != null && targetFragment.isHidden()) {
                    if(targetFragment instanceof MultiFragment) {
                        MultiFragment targetMultiFragment = (MultiFragment) targetFragment;
                        targetMultiFragment.setStatusBarColor();
                        if(mCurrentPrimaryItem instanceof MultiFragment) {
                            MultiFragment currentFragment = (MultiFragment) mCurrentPrimaryItem;
                            targetMultiFragment.onFragmentResult(currentFragment.getTargetRequestCode(), currentFragment.getResultCode(), currentFragment.getResultData());
                        } else {
                            targetMultiFragment.onFragmentResult(mCurrentPrimaryItem.getTargetRequestCode(), mResultCode, mResultData);
                        }
                    } else {
                        targetFragment.onActivityResult(mCurrentPrimaryItem.getTargetRequestCode(), mResultCode, mResultData);
                    }
                    mCurTransaction.remove(mCurrentPrimaryItem);
                    mCurTransaction.show(targetFragment);
                    if (targetFragment != mCurrentPrimaryItem) {
                        if (mCurrentPrimaryItem != null) {
                            mCurrentPrimaryItem.setMenuVisibility(false);
                            mCurrentPrimaryItem.setUserVisibleHint(false);
                        }
                        targetFragment.setMenuVisibility(true);
                        targetFragment.setUserVisibleHint(true);
                        mCurrentPrimaryItem = targetFragment;
                    }
                    mCurTransaction.commitAllowingStateLoss();
                    mCurTransaction = null;
                } else {
                    Fragment nextFragment = getNextFragment();
                    if(nextFragment != null) {
                        int nextFragmentIndex = getFragmentIndex(nextFragment);
                        int currentPrimaryItemIndex = getFragmentIndex(mCurrentPrimaryItem);
                        if(nextFragmentIndex < currentPrimaryItemIndex) {
                            mCurTransaction.remove(mCurrentPrimaryItem);
                        } else {
                            mCurTransaction.detach(mCurrentPrimaryItem);
                        }
                        if(nextFragment.isDetached()) {
                            mCurTransaction.attach(nextFragment);
                        } else {
                            mCurTransaction.show(nextFragment);
                        }
                        if (nextFragment != mCurrentPrimaryItem) {
                            if (mCurrentPrimaryItem != null) {
                                mCurrentPrimaryItem.setMenuVisibility(false);
                                mCurrentPrimaryItem.setUserVisibleHint(false);
                            }
                            nextFragment.setMenuVisibility(true);
                            nextFragment.setUserVisibleHint(true);
                            mCurrentPrimaryItem = nextFragment;
                        }
                        mCurTransaction.commitAllowingStateLoss();
                        mCurTransaction = null;
                    } else {
                        finishActivity();
                    }
                }
            }
        } else {
            if(mCurrentPrimaryItem != null) {
                Fragment fragment = getVisibleFragment();
                if(fragment != null) {
                    mCurTransaction.remove(fragment);
                }
            }
        }
    }

    public final void finishActivity() {
        setResult(mResultCode, mResultData);
        finish();
    }

    private Fragment getFragment(String className) {
        Class cls = null;
        try {
            cls = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(className + " not found");
        }

        try {
            return (Fragment) cls.newInstance();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(className + " is inaccessible");
        } catch (InstantiationException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(className + " cannot be instantiated");
        }
    }

    private String makeFragmentName(String activity, String fragment) {
        return "android:one_multi:" + activity + ":" + fragment;
    }

    private Fragment getNextFragment() {
        List<Fragment> nextFragments = new ArrayList<>();
        FragmentManager fragmentManager = getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        for (int i = fragments.size() - 1; i >= 0; i--) {
            Fragment fragment = fragments.get(i);
            if(fragment != null && (fragment.isDetached() || fragment.isAdded() && !fragment.isVisible())) {
                nextFragments.add(fragment);
            }
        }
        Fragment nextFragment = null;
        int nextFragmentIndex = 0;
        for (int i = nextFragments.size() - 1; i >= 0; i--) {
            if(nextFragment == null) {
                nextFragment = nextFragments.get(i);
                nextFragmentIndex = getFragmentIndex(nextFragment);
            } else {
                Fragment fragment = nextFragments.get(i);
                int fragmentIndex = getFragmentIndex(fragment);
                if(fragmentIndex > nextFragmentIndex) {
                    nextFragment = fragment;
                    nextFragmentIndex = fragmentIndex;
                }
            }
        }
        return nextFragment;
    }

    private int getFragmentIndex(Fragment fragment) {
        int fragmentIndex = 0;
        Class cls = fragment.getClass();
        do {
            cls = cls.getSuperclass();
        } while (!cls.getSimpleName().equals("Fragment"));
        try {
            Field field = cls.getDeclaredField("mIndex");
            field.setAccessible(true);
            fragmentIndex = field.getInt(fragment);
            field.setAccessible(false);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return fragmentIndex;
    }

    private int getAddedFragmentCount() {
        int addedCount = 0;
        FragmentManager fragmentManager = getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        for (int i = fragments.size() - 1; i >= 0; i--) {
            Fragment fragment = fragments.get(i);
            if(fragment != null && fragment.isAdded() && fragment.isVisible()) {
                addedCount++;
            }
        }
        return addedCount;
    }

    public float getTranslationZ() {
        return mTranslationZ += 1.0f;
    }

    private List<Fragment> getAvailableFragments() {
        List<Fragment> availableFragments = new ArrayList<>();
        FragmentManager fragmentManager = getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        for (int i = fragments.size() - 1; i >= 0; i--) {
            Fragment fragment = fragments.get(i);
            if(fragment != null && !fragment.isRemoving()) {
                availableFragments.add(fragment);
            }
        }
        return availableFragments;
    }

    public int getAvailableFragmentCount() {
        return getAvailableFragments().size();
    }

    public Fragment getFragment(Class<?> cls) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        for (int i = fragments.size() - 1; i >= 0; i--) {
            Fragment fragment = fragments.get(i);
            if(fragment != null && fragment.getClass() == cls) {
                return fragment;
            }
        }
        return null;
    }

    public Fragment getVisibleFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        for (int i = fragments.size() - 1; i >= 0; i--) {
            Fragment fragment = fragments.get(i);
            if(fragment != null && fragment.isVisible()) {
                return fragment;
            }
        }
        return null;
    }

    public void clearAvailIndices() {
        Class<?> classType = mFragmentManager.getClass();
        Field field = null;
        try {
            field = classType.getDeclaredField("mAvailIndices");
            field.setAccessible(true);
            field.set(mFragmentManager, null);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment fragment = getVisibleFragment();
        if(fragment != null) {
            if(fragment instanceof MultiFragment) {
                MultiFragment multiFragment = (MultiFragment) fragment;
                if(multiFragment.getLaunchMode() == LaunchMode.FLAG_FRAGMENT_NO_HISTORY) {
                    mCurTransaction = mFragmentManager.beginTransaction();
                    Fragment detachedFragment = getNextFragment();
                    mCurTransaction.attach(detachedFragment);
                    mCurTransaction.remove(multiFragment);
                    mCurrentPrimaryItem = detachedFragment;
                    mCurTransaction.commitAllowingStateLoss();
                    mCurTransaction = null;
                    detachedFragment.onActivityResult(requestCode, resultCode, data);
                } else {
                    multiFragment.onActivityResult(requestCode, resultCode, data);
                }
            } else {
                fragment.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode,final String[] permissions,final int[] grantResults) {
        final Fragment fragment = getVisibleFragment();
        if(fragment != null && fragment instanceof MultiFragment) {
            if(fragment.getView() != null) {
                fragment.getView().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
                    }
                }, 100);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(mCurrentPrimaryItem != null) {
            MultiFragment fragment = (MultiFragment) mCurrentPrimaryItem;
            if (!fragment.backPressed()) {
                finishFragment();
            }
        } else {
            super.onBackPressed();
        }
    }

    public void showSoftInput(EditText view){
        view.requestFocus();
        if (mInputMethodManager == null){
            mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        }
        try {
            mInputMethodManager.showSoftInput(view, InputMethodManager.SHOW_FORCED);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hiddenSoftInput() {
        if (mInputMethodManager == null){
            mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        }
        try {
            if(getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
                mInputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}