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

public interface OneMulti {

    class LaunchMode {
        /**
         * A_B_C_D => D -> B => A_B
         * A_B_C => C -> D => A_B_C_D
         * A_B_C => C -> C => A_B_C
         */
        public static int FLAG_FRAGMENT_CLEAR_TOP = 0x10000000;

        /**
         * A_B_C_D => D -> B => B
         * A_B_C => C -> D => D
         * A_B_C => C -> C => C
         */
        public static int FLAG_FRAGMENT_CLEAR_ALL = 0x20000000;

        /**
         *
         */
        public static int FLAG_FRAGMENT_SINGLE_TOP = 0x30000000;

        /**
         *
         */
        public static int FLAG_FRAGMENT_SINGLE_INSTANCE = 0x40000000;

        /**
         *
         */
        public static int FLAG_FRAGMENT_NO_HISTORY = 0x50000000;


    }

    String FRAGMENT_CLASS = "ONE_MULTI_FRAGMENT_CLASS";

    int[] StartAnimations = new int[] {R.anim.slide_right_in, R.anim.slide_left_out};
    int[] BackAnimations = new int[] {R.anim.back_left_in, R.anim.back_right_out};
}
