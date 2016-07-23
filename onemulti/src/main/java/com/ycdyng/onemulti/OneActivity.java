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
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public abstract class OneActivity extends AppCompatActivity implements OneMulti{

    static final String TAG = "OneActivity";
    static final boolean DEBUG = true;

    int mResultCode = RESULT_CANCELED;
    Intent mResultData = null;

    FragmentManager mFragmentManager;
    FragmentTransaction mCurTransaction = null;
    Fragment mCurrentPrimaryItem = null;

    public InputMethodManager mInputMethodManager = null;

    public long waitTime = 250;
    public long touchTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        mFragmentManager = getSupportFragmentManager();
//        mFragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
//            @Override
//            public void onBackStackChanged() {
//                FragmentManager fragmentManager = getSupportFragmentManager();
//                if (fragmentManager != null) {
//                    MultiFragment multiFragment = (MultiFragment) fragmentManager.findFragmentById(android.R.id.content);
//                    multiFragment.onFragmentResume();
//                }
//            }
//        });

        String className = null;
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(FRAGMENT_CLASS)) {
            className =  intent.getStringExtra(FRAGMENT_CLASS);
        } else {
            Class cls = getDefaultFragment();
            if(cls == null) {
                throw new IllegalArgumentException("Default Fragment class argument can not be null");
            }
            className = cls.getName();
        }
        initFragment(className, intent == null ? null : intent.getExtras());
    }

    protected abstract Class<? extends MultiFragment> getDefaultFragment();

    public void initFragment(String className, Bundle bundle) {
        mCurTransaction = mFragmentManager.beginTransaction();
        Fragment fragment = getFragment(className);
        fragment.setArguments(bundle);
        String name = makeFragmentName(getClass().getSimpleName(), className);
        if (DEBUG) Log.v(TAG, "Adding item #" + name);
        mCurTransaction.add(android.R.id.content, fragment, name);

        fragment.setMenuVisibility(true);
        fragment.setUserVisibleHint(true);
        mCurrentPrimaryItem = fragment;
        mCurTransaction.commitNow();
        mCurTransaction = null;
    }

    public void startFragment(Intent intent) {
        startFragment(intent, CustomAnimations);
    }

    public void startFragment(Intent intent, boolean anim) {
        int[] customAnimations = null;
        if(anim) customAnimations = CustomAnimations;
        startFragment(intent, customAnimations);
    }

    private void startFragment(Intent intent, int[] customAnimations) {

//        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
//        if(anim != null) {
//            if(anim.length == 4) {
//                fragmentTransaction.setCustomAnimations(anim[0], anim[1], anim[2], anim[3]);
//            } else if(anim.length == 2) {
//                fragmentTransaction.setCustomAnimations(anim[0], anim[1]);
//            }
//        }
//
//        if(fragment instanceof MultiFragment) {
//            MultiFragment baseOneMultiFragment = (MultiFragment) fragment;
//            baseOneMultiFragment.setOnFragmentTransactionListener(mOnFragmentTransactionListener);
//        } else {
//            throw new IllegalArgumentException(fragment.getClass().getName()  + " is inaccessible");
//        }

        clearAvailIndices();
        mCurTransaction = mFragmentManager.beginTransaction();
        String name = makeFragmentName(getClass().getSimpleName(), intent.getComponent().getClassName());
        int currentAddedFragmentCount = getAddedFragmentCount();
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
                    mCurTransaction.attach(fragment);
                } else {
                    if(mCurrentPrimaryItem != null) {
                        mCurTransaction.detach(mCurrentPrimaryItem);
                    }
                    fragment = getFragment(intent.getComponent().getClassName());
                    fragment.setArguments(intent.getExtras());
                    if (DEBUG) Log.v(TAG, "Adding item #" + name);
                    mCurTransaction.add(android.R.id.content, fragment, name);
                }
            }
        } else if(intent.getFlags() == LaunchMode.FLAG_FRAGMENT_CLEAR_ALL) {
            // remove all
            List<Fragment> availableFragments = getAvailableFragments();
            for (int i = availableFragments.size() - 1; i >= 0 ; i--) {
                mCurTransaction.remove(availableFragments.get(i));
            }
            // add new
            fragment = getFragment(intent.getComponent().getClassName());
            fragment.setArguments(intent.getExtras());
            if (DEBUG) Log.v(TAG, "Adding item #" + name);
            mCurTransaction.add(android.R.id.content, fragment, name);
        } else if(intent.getFlags() == LaunchMode.FLAG_FRAGMENT_SINGLE_INSTANCE) {
            // Do we already have this fragment?
            fragment = mFragmentManager.findFragmentByTag(name);
            if(fragment != null) {  // Yes, we have
                if(mCurrentPrimaryItem != null) {
                    mCurTransaction.detach(mCurrentPrimaryItem);
                }
                mCurTransaction.attach(fragment);
            } else {
                if(mCurrentPrimaryItem != null) {
                    if(mCurrentPrimaryItem instanceof MultiFragment && ((MultiFragment) mCurrentPrimaryItem).getLaunchMode() == LaunchMode.FLAG_FRAGMENT_NO_HISTORY) {
                        mCurTransaction.remove(mCurrentPrimaryItem);
                    } else {
                        mCurTransaction.detach(mCurrentPrimaryItem);
                    }
                }
                fragment = getFragment(intent.getComponent().getClassName());
                fragment.setArguments(intent.getExtras());
                if (DEBUG) Log.v(TAG, "Adding item #" + name);
                mCurTransaction.add(android.R.id.content, fragment, name);
            }
        } else if(intent.getFlags() == LaunchMode.FLAG_FRAGMENT_SINGLE_TOP) {
            if(mCurrentPrimaryItem.getClass().getName().equals(intent.getComponent().getClassName())) {
                mCurTransaction.attach(mCurrentPrimaryItem);
            } else {
                if(mCurrentPrimaryItem != null) {
                    if(mCurrentPrimaryItem instanceof MultiFragment && ((MultiFragment) mCurrentPrimaryItem).getLaunchMode() == LaunchMode.FLAG_FRAGMENT_NO_HISTORY) {
                        mCurTransaction.remove(mCurrentPrimaryItem);
                    } else {
                        mCurTransaction.detach(mCurrentPrimaryItem);
                    }
                }
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
            }
        } else {
            if(mCurrentPrimaryItem != null) {
                if(mCurrentPrimaryItem instanceof MultiFragment && ((MultiFragment) mCurrentPrimaryItem).getLaunchMode() == LaunchMode.FLAG_FRAGMENT_NO_HISTORY) {
                    mCurTransaction.remove(mCurrentPrimaryItem);
                } else {
                    mCurTransaction.detach(mCurrentPrimaryItem);
                }
            }
            fragment = getFragment(intent.getComponent().getClassName());
            fragment.setArguments(intent.getExtras());
            if (DEBUG) Log.v(TAG, "Adding item #" + name);
            mCurTransaction.add(android.R.id.content, fragment, name);
            if(intent.getFlags() == LaunchMode.FLAG_FRAGMENT_NO_HISTORY && fragment instanceof MultiFragment) {
                ((MultiFragment) fragment).setLaunchMode(LaunchMode.FLAG_FRAGMENT_NO_HISTORY);
            }
        }

        if (fragment != null && fragment != mCurrentPrimaryItem) {
            fragment.setMenuVisibility(false);
            fragment.setUserVisibleHint(false);
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
            mCurTransaction.commitNow();
            mCurTransaction = null;
        }
    }

    public final void startFragmentForResult(Intent intent, int requestCode) {
        startFragmentForResult(intent, requestCode, CustomAnimations);
    }

    public final void startFragmentForResult(Intent intent, int requestCode, boolean anim) {
        int[] customAnimations = null;
        if(anim) customAnimations = CustomAnimations;
        startFragmentForResult(intent, requestCode, customAnimations);
    }

    private final void startFragmentForResult(Intent intent, int requestCode, int[] customAnimations) {
        clearAvailIndices();
        mCurTransaction = mFragmentManager.beginTransaction();

        // Do we already have this fragment?
        String name = makeFragmentName(getClass().getSimpleName(), intent.getComponent().getClassName());
        Fragment fragment = getFragment(intent.getComponent().getClassName());
        if(fragment == null) return;
        fragment.setTargetFragment(mCurrentPrimaryItem, requestCode);
        fragment.setArguments(intent.getExtras());
        if (DEBUG) Log.v(TAG, "Adding item #" + name);
        mCurTransaction.add(android.R.id.content, fragment, name);

        if (fragment != mCurrentPrimaryItem) {
            fragment.setMenuVisibility(false);
            fragment.setUserVisibleHint(false);
        }

        if (fragment != mCurrentPrimaryItem) {
            if (mCurrentPrimaryItem != null) {
                mCurrentPrimaryItem.setMenuVisibility(false);
                mCurrentPrimaryItem.setUserVisibleHint(false);
            }
            fragment.setMenuVisibility(true);
            fragment.setUserVisibleHint(true);
            mCurrentPrimaryItem = fragment;
        }

        mCurTransaction.commitNow();
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
        if (mCurTransaction == null) {
            int availableFragmentCount = getAvailableFragmentCount();
            if(availableFragmentCount <= 1) {
                finishActivity();
            } else {
                Fragment targetFragment = mCurrentPrimaryItem.getTargetFragment();
                if(targetFragment != null && targetFragment.isVisible()) {
                    targetFragment.onActivityResult(mCurrentPrimaryItem.getTargetRequestCode(), mResultCode, mResultData);
                    mCurTransaction = mFragmentManager.beginTransaction();
                    mCurTransaction.remove(mCurrentPrimaryItem);
                    if (targetFragment != mCurrentPrimaryItem) {
                        if (mCurrentPrimaryItem != null) {
                            mCurrentPrimaryItem.setMenuVisibility(false);
                            mCurrentPrimaryItem.setUserVisibleHint(false);
                        }
                        targetFragment.setMenuVisibility(true);
                        targetFragment.setUserVisibleHint(true);
                        mCurrentPrimaryItem = targetFragment;
                    }
                    mCurTransaction.commitNow();
                    mCurTransaction = null;
                } else {
                    Fragment detachedFragment = getDetachedFragment();
                    mCurTransaction = mFragmentManager.beginTransaction();
                    int detachedFragmentIndex = getFragmentIndex(detachedFragment);
                    int currentPrimaryItemIndex = getFragmentIndex(mCurrentPrimaryItem);
                    if(detachedFragmentIndex < currentPrimaryItemIndex) {
                        mCurTransaction.remove(mCurrentPrimaryItem);
                    } else {
                        mCurTransaction.detach(mCurrentPrimaryItem);
                    }
                    mCurTransaction.attach(detachedFragment);
                    if (detachedFragment != mCurrentPrimaryItem) {
                        if (mCurrentPrimaryItem != null) {
                            mCurrentPrimaryItem.setMenuVisibility(false);
                            mCurrentPrimaryItem.setUserVisibleHint(false);
                        }
                        detachedFragment.setMenuVisibility(true);
                        detachedFragment.setUserVisibleHint(true);
                        mCurrentPrimaryItem = detachedFragment;
                    }
                    mCurTransaction.commitNow();
                    mCurTransaction = null;
                }
            }
        } else {
            if(mCurrentPrimaryItem != null) {
                mCurTransaction.remove(getVisibleFragment());
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

    private FragmentTransaction initCustomAnimations(FragmentTransaction fragmentTransaction, int[] anim) {
        if(fragmentTransaction != null && anim != null) {
            if(anim.length == 4) {
                fragmentTransaction.setCustomAnimations(anim[0], anim[1], anim[2], anim[3]);
            } else if(anim.length == 2) {
                fragmentTransaction.setCustomAnimations(anim[0], anim[1]);
            }
        }
        return fragmentTransaction;
    }

    private String makeFragmentName(String activity, String fragment) {
        return "android:one_multi:" + activity + ":" + fragment;
    }

    private Fragment getDetachedFragment() {
        List<Fragment> detachedFragments = new ArrayList<>();
        FragmentManager fragmentManager = getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        for (int i = fragments.size() - 1; i >= 0; i--) {
            Fragment fragment = fragments.get(i);
            if(fragment != null && fragment.isDetached()) {
                detachedFragments.add(fragment);
            }
        }
        Fragment detachedFragment = null;
        int detachedFragmentIndex = 0;
        for (int i = detachedFragments.size() - 1; i >= 0; i--) {
            if(detachedFragment == null) {
                detachedFragment = detachedFragments.get(i);
                detachedFragmentIndex = getFragmentIndex(detachedFragment);
            } else {
                Fragment fragment = detachedFragments.get(i);
                int fragmentIndex = getFragmentIndex(fragment);
                if(fragmentIndex > detachedFragmentIndex) {
                    detachedFragment = fragment;
                    detachedFragmentIndex = fragmentIndex;
                }
            }
        }
        return detachedFragment;
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

    private int getAvailableFragmentCount() {
        return getAvailableFragments().size();
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
            fragment.onActivityResult(requestCode, resultCode, data);
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
//        super.onBackPressed();
        if(mCurrentPrimaryItem != null) {
            MultiFragment fragment = (MultiFragment) mCurrentPrimaryItem;
            if (!fragment.backPressed()) {
                finishFragment();
            }
        }
    }

    private MultiFragment.OnFragmentTransactionListener mOnFragmentTransactionListener = new MultiFragment.OnFragmentTransactionListener() {
        @Override
        public void onTransactionEnd() {
            FragmentManager fragmentManager = getSupportFragmentManager();
            if (fragmentManager != null) {
                MultiFragment baseOneMultiFragment = (MultiFragment) fragmentManager.findFragmentById(android.R.id.content);
                if(baseOneMultiFragment != null) {
                    baseOneMultiFragment.onFragmentResume();
                }
            }
        }
    };

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