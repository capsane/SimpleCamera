package com.capsane.simplecamera;

/**
 * Created by capsane on 18-7-10.
 *
 */

class Constants {

    // 宏观, 微观采样数量
    static final int MACRO_PICTURE_NUMBER = 1;
    static final int MICRO_PICTURE_NUMBER = 2;

    // 摄像头Id
    static final String CAMERA_ID_INSIDE = "1";
    static final String CAMERA_ID_OUTSIDE = "0";

    // 微喷墨点拍摄、宏观拍摄、局部拍摄
    static final int FRAGMENT_TYPE_POINT = 1;
    static final int FRAGMENT_TYPE_MACRO = 2;
    static final int FRAGMENT_TYPE_MICRO = 3;
    static final int FRAGMENT_TYPE_LOC = 4;
    static final int FRAGMENT_TYPE_Comp = 7;

    // fragment type(photo type)
    static final String ARG_TYPE = "FragmentType";
    static final String ARG_NUMBER = "PhotoNumber";
    static final String ARG_IMAGE = "image";
    static final String ARG_LOC_NUM = "LocationNumber";

    // Button code
    static final int BUTTON_RETURN = 0;
    static final int BUTTON_NEXT = 1;
    static final int BUTTON_LOC1 = 10;
    static final int BUTTON_LOC2 = 20;


}
