# OneMulti
#### One-Activity with Multi-Fragments

e.g.

related business in one activity 相似的业务共享一个Activity
```
├─AccountControlActivity 
│  ├─SignInFragment
│  ├─SignUpFragment
│  ├─BindMobileFragment
│  ├─ForgetPasswordFragment
│  ├─...
├─...
│  ├─...
```

# Screenshot
<img src='https://github.com/YcdYng/OneMulti/blob/master/screenshot/1.png' height='350'/>
<img src='https://github.com/YcdYng/OneMulti/blob/master/screenshot/2.png' height='350'/>
<img src='https://github.com/YcdYng/OneMulti/blob/master/screenshot/3.png' height='350'/>
<img src='https://github.com/YcdYng/OneMulti/blob/master/screenshot/4.png' height='350'/>

# Get it
available on Maven Central
```
    <dependency>
      <groupId>com.ycdyng.android</groupId>
      <artifactId>onemulti</artifactId>
      <version>1.0.4</version>
      <type>pom</type>
    </dependency>
```

or 

```
    compile 'com.ycdyng.android:onemulti:1.0.4'
```


# Usages

**One-Activity**

```
public class AccountControlActivity extends OneActivity {

    @Override
    protected Class<? extends MultiFragment> getDefaultFragment() {
        // You can return default fragment or not.
        return SignInFragment.class;
    }
    
    ...
}
```

**Multi-Fragment**

```
public class SignInFragment extends MultiFragment {

    Button signInButton;
    
    @Override
    protected int getThemeResId() {
        // Fragment Theme, like set StatusBar color
        return R.style.CustomTheme;
    }
    
    @Override
    protected int getLayoutResourceId() {
        // You must define a layout of this fragment.
        return R.layout.fragment_sign_in;
    }

    @Override
    protected void onCreateView(View view, ViewGroup container, Bundle savedInstanceState) {
        // Using findViewById
        signInButton = (Button) findViewById(R.id.sign_in_button);
        
        ...
    }
    
    ...
}
```

## switch Multi-Fragment in One-Activity

**startFragment**

```
    Intent intent = new Intent(getActivity(), SignUpFragment.class);
    startFragment(intent);
```

**startFragmentForResult**

```
    Intent intent = new Intent(getActivity(), SignUpFragment.class);
    startFragmentForResult(intent, requestCode);
```

## start Multi-Fragment between One-Activity

if AccountControlActivity has default Fragment
```
    Intent intent = new Intent(this, AccountControlActivity.class);
    startActivity(intent);
```
or not
```
    Intent intent = new Intent(this, AccountControlActivity.class);
    intent.putExtra(OneMulti.FRAGMENT_NAME, SignInFragment.class.getName());
    startActivity(intent);
```

## setResult

Call this to set the result that your fragment will return to its caller. the caller will receive result through **OnFragmentResult()**.

## finish()

If call, and the fragment will be close, then the top fragment of the history stack will be added, or activity will be closed.

For example, consider a task consisting of the fragments: A, B, C. If C calls finish(), then B will be add to activity, resulting in the stack will being: A, B.

For example, consider a task consisting of the fragments: A. If A calls finish(), then this activity will be closed.

## Fragment Launch Mode

```
    Intent intent = new Intent(getActivity(), SignUpFragment.class);
    intent.setFlag(...);
    startFragment(intent);
```

**FLAG_FRAGMENT_CLEAR_TOP**

If set, and the fragment being launched is already running in the current task, then instead of launching a new instance of that fragment, all of the other fragments on top of it will be closed and this Intent will be delivered to the (now on top) old fragment as a new Intent.

For example, consider a task consisting of the fragments: A, B, C, D. If D calls startFragment() with an Intent that resolves to the component of fragment B, then C and D will be finished and B receive the given Intent, resulting in the stack now being: A, B.

**FLAG_FRAGMENT_CLEAR_ALL**

If set, and the fragment being launched is already running in the current task, then instead of launching a new instance of that fragment, but all of the other fragments will be closed and this Intent will be delivered to the (now on top) old fragment as a new Intent.

For example, consider a task consisting of the fragments: A, B, C, D. If D calls startFragment() with an Intent that resolves to the component of fragment B, then A, C, D will be finished and B receive the given Intent, resulting in the stack now being: B.


**FLAG_FRAGMENT_SINGLE_TOP**

If set, the fragment will not be launched if it is already running at the top of the history stack.

**FLAG_FRAGMENT_SINGLE_INSTANCE**

If set, and the fragment being launched is already running in the current task, then instead of launching a new instance of that fragment

For example, consider a task consisting of the fragments: A, B, C, D. If D calls startFragment() with an Intent that resolves to the component of fragment B, then B receive the given Intent, resulting in the stack still being: A, B, C, D. if press back, resulting in the stack still being: A, B, C, D. press back again, resulting in the stack will being: A, B, C.

**FLAG_FRAGMENT_NO_HISTORY**

If set, the new fragment is not kept in the history stack. As soon as the user navigates away from it, the fragment is finished. This may also be set with the noHistory attribute.

For example, If A calls startFragment() with an Intent that resolves to the component of fragment B, then B calls startFragment() to the component of fragment c, resulting in the stack still being: A, C.

Also support startFragmentForResult().

For example, If A calls startFragmentForResult() with an Intent that resolves to the component of fragment B, then B calls startFragmentForResult() to the component of fragment c, resulting in the stack still being: A, C.

## License
```
   Copyright 2016-2017 EastWood Yang

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```
