package com.capsane.simplecamera;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.OpenCVLoader;

import java.util.HashMap;
import java.util.Map;

import static com.capsane.simplecamera.Constants.ARG_NUMBER;
import static com.capsane.simplecamera.Constants.ARG_TYPE;
import static com.capsane.simplecamera.Constants.BUTTON_LOC1;
import static com.capsane.simplecamera.Constants.BUTTON_LOC2;
import static com.capsane.simplecamera.Constants.BUTTON_NEXT;
import static com.capsane.simplecamera.Constants.BUTTON_RETURN;
import static com.capsane.simplecamera.Constants.FRAGMENT_TYPE_Comp;
import static com.capsane.simplecamera.Constants.FRAGMENT_TYPE_LOC;
import static com.capsane.simplecamera.Constants.FRAGMENT_TYPE_MACRO;
import static com.capsane.simplecamera.Constants.FRAGMENT_TYPE_MICRO;
import static com.capsane.simplecamera.Constants.FRAGMENT_TYPE_POINT;


public class LaunchCameraActivity extends AppCompatActivity implements
        CameraFragment.OnCameraFragmentListener, PhotoFragment.onPhotoFragmentListener,
        CompareFragment.OnCompareFragmentListener, MicroLocationFragment.OnMicroLocationListener,
        View.OnClickListener {

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

        Button buttonCompare = findViewById(R.id.btn_menu_compare);
        buttonCompare.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_13, getApplicationContext(), mLoaderCallback);
        Log.e(TAG, "onResume success load OpenCV...");
    }

    //OpenCV库加载并初始化成功后的回调函数
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            Log.e(TAG, "onManagerConnected: ");
            switch (status) {
                case BaseLoaderCallback.SUCCESS:
                    Log.i(TAG, "成功加载");
                    break;
                default:
                    super.onManagerConnected(status);
                    Log.i(TAG, "加载失败");
                    break;
            }
        }
    };


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
        } else if (photoType == FRAGMENT_TYPE_Comp) {
            // 跳转到比对页面，填充待比对图片
            tempTag = "" + FRAGMENT_TYPE_Comp;
            FragmentTransaction mFragmentTransaction = getSupportFragmentManager().beginTransaction();
            if (fragmentMap.containsKey(tempTag)) {
                CompareFragment compareFragment = (CompareFragment)fragmentMap.get(tempTag);
                compareFragment.setArguments(bundle);
                mFragmentTransaction.replace(R.id.container, compareFragment);
                mFragmentTransaction.show(compareFragment);
                mFragmentTransaction.commit();
            } else {
                Log.e(TAG, "map中没有tempTag = 4 的 compareFragment");
            }

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


    @Override
    public void onFragmentInteraction(int photoType, int photoNumber, int code) {
        tempTag = "" + FRAGMENT_TYPE_Comp;
        switch (code) {
            // 比对图片1
            case BUTTON_LOC1:
                Log.e(TAG, "比对图片1: ");
                FragmentTransaction mFragmentTransaction = getSupportFragmentManager().beginTransaction();
                // TODO: hide
                if (fragmentMap.containsKey(tempTag)) {
                    mFragmentTransaction.hide(fragmentMap.get(tempTag));
                }

                mFragmentTransaction.add(R.id.container, CameraFragment.newInstance(FRAGMENT_TYPE_Comp, photoNumber, 1));
                mFragmentTransaction.commit();
                break;
            case BUTTON_LOC2:
                Log.e(TAG, "比对图片2: ");
                mFragmentTransaction = getSupportFragmentManager().beginTransaction();
                // TODO: hide
                if (fragmentMap.containsKey(tempTag)) {
                    mFragmentTransaction.hide(fragmentMap.get(tempTag));
                }

                mFragmentTransaction.add(R.id.container, CameraFragment.newInstance(FRAGMENT_TYPE_Comp, photoNumber, 2));
                mFragmentTransaction.commit();
                break;


        }
    }

    @Override
    public void onClick(View view) {
        // TODO: Menu button
        Log.e(TAG, "onClick: ");
        switch (view.getId()) {
            // 快速比对窗口tempTag = 4
            case R.id.btn_menu_compare:
                Log.e(TAG, "onClick: 比对");
                // 启动快速比对fragment
                tempTag = "" + FRAGMENT_TYPE_Comp;
                FragmentTransaction mFragmentTransaction = getSupportFragmentManager().beginTransaction();

                if (!fragmentMap.containsKey(tempTag)) {
                    CompareFragment compareFragment = CompareFragment.newInstance(500, 0);
                    fragmentMap.put(tempTag, compareFragment);
                }
                mFragmentTransaction.replace(R.id.container, fragmentMap.get(tempTag), tempTag);
                mFragmentTransaction.commit();
                break;

        }
    }


    private void hideAllFragmentsExcept(String tag) {

        if (fragmentMap.isEmpty()) {
            return;
        }

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        for (Map.Entry<String, Fragment> entry : fragmentMap.entrySet()) {
            String key = entry.getKey();
            Fragment fragment = entry.getValue();
            if (!key.equals(tag)) {
                fragmentTransaction.hide(fragment);
            }
        }
        fragmentTransaction.commit();
    }

}
