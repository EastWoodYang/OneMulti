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

package com.ycdyng.onemultitrial.OneActivity;

import android.os.Bundle;

import com.ycdyng.onemulti.MultiFragment;
import com.ycdyng.onemulti.OneActivity;
import com.ycdyng.onemultitrial.MultiFragment.AFragment;

public class AActivity extends OneActivity {

    @Override
    protected Class<? extends MultiFragment> getDefaultFragment() {
        return AFragment.class;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

}
