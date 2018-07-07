package com.capsane.simplecamera;

import android.net.Uri;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class LaunchCameraActivity extends AppCompatActivity implements
        CameraFragment.OnCameraFragmentListener, PhotoFragment.onPhotoFragmentListener,
        MicroLocationFragment.OnMicroLocationListener {

    private static final String TAG = "LaunchCameraActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch_camera);
        // getSupportFragmentManager().beginTransaction()获得FragmentTransaction对象，
        // 调用add或者replace方法加载Fragment. add(要传入的容器， fragment对象)
        // 然后commit()提交事务，当然还有remove等方法
         if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, CameraFragment.newInstance(CameraFragment.FRAGMENT_TYPE_POINT,1))
                    .commit();
        }
    }

    // OnFragmentInteractionListener接口的回调，用于Activity和Fragment之间的通讯
    @Override
    public void onCameraInteraction(Bundle bundle) {
        // TODO: 跳转到PhotoFragment
        Log.e(TAG, "onCameraInteraction, callback");
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        PhotoFragment photoFragment = PhotoFragment.newInstance(200, 0);
        // FIXME: 这里重新设置了bundle啊！！覆盖了上面的
        photoFragment.setArguments(bundle);
        fragmentTransaction.replace(R.id.container, photoFragment);
        fragmentTransaction.commit();

    }

    @Override
    public void onPhotoInteraction(int photoType, int photoNumber, int buttonCode) {
        Log.e(TAG, "onPhotoInteraction, callback");

        switch (photoType) {
            // 微喷墨点，跳转到宏观拍摄
            case PhotoFragment.PHOTO_TYPE_POINT:
                switch (buttonCode) {
                    case PhotoFragment.BUTTON_RETURN:
                        Log.e(TAG, "onPhotoInteraction: 左无");
                        break;
                    case PhotoFragment.BUTTON_NEXT:
                        Log.e(TAG, "onPhotoInteraction: 右有");
                        break;
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, CameraFragment.newInstance(CameraFragment.FRAGMENT_TYPE_MACRO, 1))
                        .commit();
                break;

            // 宏观图片，跳转到微观取证，首先是宏观位置比对
            case PhotoFragment.PHOTO_TYPE_MACRO:
                switch (buttonCode) {
                    // 重新拍摄
                    case PhotoFragment.BUTTON_RETURN:
                        Log.e(TAG, "onPhotoInteraction: 左重新拍摄");
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, CameraFragment.newInstance(CameraFragment.FRAGMENT_TYPE_MACRO, photoNumber))
                                .commit();
                        break;
                    // 下一步，宏观位置放置
                    case PhotoFragment.BUTTON_NEXT:
                        Log.e(TAG, "onPhotoInteraction: 右下一步: " + photoNumber);
                        if (photoNumber < 3) {
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.container, CameraFragment.newInstance(CameraFragment.FRAGMENT_TYPE_MACRO,
                                            photoNumber+1))
                                    .commit();
                        } else {
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.container, CameraFragment.newInstance(CameraFragment.FRAGMENT_TYPE_MICRO, 1))
                                    .commit();
                        }
                        break;
                }
                break;

            // 微观图像采集完之后
            case PhotoFragment.PHOTO_TYPE_MICRO:
                switch (buttonCode) {
                    // 重新拍摄
                    case PhotoFragment.BUTTON_RETURN:
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, CameraFragment.newInstance(CameraFragment.FRAGMENT_TYPE_MICRO, photoNumber))
                                .commit();
                        break;
                    // 定位图
                    case PhotoFragment.BUTTON_NEXT:
                        if (photoNumber < 3) {
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.container, CameraFragment.newInstance(CameraFragment.FRAGMENT_TYPE_MICRO, photoNumber+1))
                                    .commit();
                        }
                        break;
                }



                break;

            //　定位图
            case PhotoFragment.PHOTO_TYPE_LOC:

                break;

        }
    }

    @Override
    public void onMicroLocationInteraction(Uri uri) {

    }
}
