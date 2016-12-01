package com.ycdyng.onemulti;

public class LaunchMode {
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
