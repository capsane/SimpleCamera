package com.capsane.simplecamera;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import static com.capsane.simplecamera.Constants.ARG_NUMBER;
import static com.capsane.simplecamera.Constants.ARG_TYPE;
import static com.capsane.simplecamera.Constants.BUTTON_LOC1;
import static com.capsane.simplecamera.Constants.BUTTON_LOC2;
import static com.capsane.simplecamera.Constants.BUTTON_NEXT;
import static com.capsane.simplecamera.Constants.BUTTON_RETURN;
import static com.capsane.simplecamera.Constants.FRAGMENT_TYPE_LOC;
import static com.capsane.simplecamera.Constants.FRAGMENT_TYPE_MACRO;
import static com.capsane.simplecamera.Constants.FRAGMENT_TYPE_MICRO;
import static com.capsane.simplecamera.Constants.FRAGMENT_TYPE_POINT;


public class LaunchCameraActivity extends AppCompatActivity implements
        CameraFragment.OnCameraFragmentListener, PhotoFragment.onPhotoFragmentListener,
        MicroLocationFragment.OnMicroLocationListener {

    private static final String TAG = "LaunchCameraActivity";

    private static String tempTag;

    private static Map<String, Fragment> fragmentMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fragmentMap = new HashMap<>();

        setContentView(R.layout.activity_launch_camera);
        if (null == savedInstanceState) {
            // 墨点
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, CameraFragment.newInstance(
                            FRAGMENT_TYPE_POINT, 1, -1))
                    .commit();
        }
    }

    // OnFragmentInteractionListener接口的回调，用于Activity和Fragment之间的通讯
    @Override
    public void onCameraInteraction(Bundle bundle) {
        int photoType = bundle.getInt(ARG_TYPE);
        int photoNumber = bundle.getInt(ARG_NUMBER);
        tempTag = ""+photoType+""+photoNumber;
        Log.e(TAG, "tag: " + tempTag);

        if (photoType == FRAGMENT_TYPE_MICRO) {
            // Camera -> Loc, add to Micro

            FragmentTransaction mFragmentTransaction = getSupportFragmentManager().beginTransaction();
            Log.e(TAG, "tag: " + tempTag);

            if (!fragmentMap.containsKey(tempTag)) {
                MicroLocationFragment microLocationFragment = MicroLocationFragment.newInstance(100, 0);
                fragmentMap.put(tempTag, microLocationFragment);
            }
            fragmentMap.get(tempTag).setArguments(bundle);
            mFragmentTransaction.replace(R.id.container, fragmentMap.get(tempTag), tempTag);
            mFragmentTransaction.commit();

        } else if (photoType == FRAGMENT_TYPE_LOC) {
            // 跳转到Loc页面，填充Loc，注意这里需要回到3x
            tempTag = ""+(photoType-1)+""+photoNumber;
            FragmentTransaction mFragmentTransaction = getSupportFragmentManager().beginTransaction();
            MicroLocationFragment microLocationFragment = (MicroLocationFragment)fragmentMap.get(tempTag);
            Log.e(TAG, "onCameraInteraction: " + microLocationFragment.toString());
            microLocationFragment.setArguments(bundle);

            mFragmentTransaction.replace(R.id.container, microLocationFragment);
            mFragmentTransaction.show(microLocationFragment);
            mFragmentTransaction.commit();
        } else {
            // photo页面replace cameraFragment
            if (!fragmentMap.containsKey(tempTag)) {
                PhotoFragment photoFragment = PhotoFragment.newInstance(200, 0);
                fragmentMap.put(tempTag, photoFragment);
            }
            // FIXME: 这里重新设置了bundle啊！！覆盖了上面的
            fragmentMap.get(tempTag).setArguments(bundle);
            FragmentTransaction mFragmentTransaction = getSupportFragmentManager().beginTransaction();
            Log.e(TAG, "tag: " + tempTag);
            mFragmentTransaction.replace(R.id.container, fragmentMap.get(tempTag), tempTag);
            mFragmentTransaction.commit();
        }
    }

    @Override
    public void onPhotoInteraction(int photoType, int photoNumber, int buttonCode, Bundle bundle) {
        switch (photoType) {
            // 微喷墨点，跳转到宏观拍摄
            case FRAGMENT_TYPE_POINT:
                switch (buttonCode) {
                    case BUTTON_RETURN:
                        break;
                    case BUTTON_NEXT:
                        break;
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, CameraFragment.newInstance(FRAGMENT_TYPE_MACRO, 1, -1))
                        .commit();
                break;

            // 宏观图片，跳转到微观取证，首先是宏观位置比对
            case FRAGMENT_TYPE_MACRO:
                switch (buttonCode) {
                    // 重新拍摄
                    case BUTTON_RETURN:
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, CameraFragment.newInstance(FRAGMENT_TYPE_MACRO, photoNumber, -1))
                                .commit();
                        break;
                    // 下一步，宏观位置放置
                    case BUTTON_NEXT:
                        if (photoNumber < Constants.MACRO_PICTURE_NUMBER) {
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.container, CameraFragment.newInstance(FRAGMENT_TYPE_MACRO,
                                            photoNumber + 1, -1))
                                    .commit();
                        } else {
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.container, CameraFragment.newInstance(FRAGMENT_TYPE_MICRO, 1, -1))
                                    .commit();
                        }
                        break;
                }
                break;

//            // 微观图像采集完之后
//            case PhotoFragment.FRAGMENT_TYPE_MICRO:
//                switch (buttonCode) {
//                    // 重新拍摄
//                    case PhotoFragment.BUTTON_RETURN:
//                        Log.e(TAG, "微观图像采集完成，重新拍摄");
//                        getSupportFragmentManager().beginTransaction()
//                                .replace(R.id.container, CameraFragment.newInstance(CameraFragment.FRAGMENT_TYPE_MICRO, photoNumber, -1))
//                                .commit();
//                        break;
//                    // 微观图拍完之后，跳转到PHOTO_TYPE_LOC页面
//                    case PhotoFragment.BUTTON_NEXT:
//                        Log.e(TAG, "微观图像采集完成，跳转？");
//                        MicroLocationFragment photoFragment = MicroLocationFragment.newInstance(200, 0);
//                        photoFragment.setArguments(bundle);
//                        FragmentTransaction mFragmentTransaction = getSupportFragmentManager().beginTransaction();
//                        mFragmentTransaction.replace(R.id.container, photoFragment);
//                        mFragmentTransaction.commit();
//                        break;
//                }
//                break;


            //　定位图
            case FRAGMENT_TYPE_LOC:

                break;

        }
    }

    @Override
    public void onMicroLocationInteraction(int photoType, int photoNumber, int code) {

        // 拍摄loc: 31, 32, 33
        tempTag = "" + (3) + "" + photoNumber;

        switch (code) {
            // 重新拍摄
            case BUTTON_RETURN:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, CameraFragment.newInstance(FRAGMENT_TYPE_MICRO, photoNumber,
                                -1))
                        .commit();
                break;

            // 拍摄下一个微观点
            case BUTTON_NEXT:
                if (photoNumber < Constants.MICRO_PICTURE_NUMBER) {
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.container, CameraFragment.newInstance(FRAGMENT_TYPE_MICRO,
                                    photoNumber + 1, -1))
                            .commit();
                } else {
                    Log.e(TAG, "onMicroLocationInteraction: jump jump jump");
                    // FIXME:

                    Log.e(TAG, fragmentMap.keySet().toString());

                    Intent intent = new Intent();
                    intent.setClass(LaunchCameraActivity.this, RecordActivity.class);
                    // 防止崩溃
                    getSupportFragmentManager().beginTransaction().
                            hide(fragmentMap.get(tempTag)).
                            commit();
                    startActivity(intent);
                }
                break;

            // 拍摄定位点1
            case BUTTON_LOC1:
                Log.e(TAG, "拍摄loc1： " + tempTag);
                FragmentTransaction mFragmentTransaction = getSupportFragmentManager().beginTransaction();
                // TODO: hide
                if (fragmentMap.containsKey(tempTag)) {
                    mFragmentTransaction.hide(fragmentMap.get(tempTag));
                }

                mFragmentTransaction.add(R.id.container, CameraFragment.newInstance(FRAGMENT_TYPE_LOC, photoNumber, 1));
                mFragmentTransaction.commit();
                break;

            // 拍摄定位点2
            case BUTTON_LOC2:
                Log.e(TAG, "拍摄loc2: " + tempTag);
                mFragmentTransaction = getSupportFragmentManager().beginTransaction();
                // TODO: hide
                if (fragmentMap.containsKey(tempTag)) {
                    mFragmentTransaction.hide(fragmentMap.get(tempTag));
                }

                mFragmentTransaction.add(R.id.container, CameraFragment.newInstance(FRAGMENT_TYPE_LOC, photoNumber, 2));
                mFragmentTransaction.commit();
                break;
        }


    }
}
