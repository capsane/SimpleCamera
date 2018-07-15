package com.capsane.simplecamera;

import android.content.Intent;
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

import static com.capsane.simplecamera.Constants.ARG_LOC_NUM;
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

    // 保存当前所处的menu
    private static int mMenuState;
    // 由menu跳转，不需要refresh
    private static boolean needRefresh = true;

    private static String tempTag;

    // TODO: 使用getFragmentById代替
    private static Map<String, Fragment> fragmentMap;

    // 两个通用的cameraFragment
    private static CameraFragment cameraFragmentInside;
    private static CameraFragment cameraFragmentOutside;

    private static Bundle tempBundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);

        fragmentMap = new HashMap<>();

        // 初始化两个cameraFragment
        cameraFragmentInside = CameraFragment.newInstance(FRAGMENT_TYPE_MACRO, -1, -1);
        cameraFragmentOutside = CameraFragment.newInstance(FRAGMENT_TYPE_POINT, -1, -1);


        setContentView(R.layout.activity_launch_camera);
        if (null == savedInstanceState) {

            Bundle bundle = new Bundle();
            bundle.putInt(ARG_TYPE, FRAGMENT_TYPE_POINT);
            bundle.putInt(ARG_NUMBER, 1);
            bundle.putInt(ARG_LOC_NUM, -1);
            cameraFragmentOutside.setArguments(bundle);

            // first添加cameraOut
            getSupportFragmentManager().beginTransaction().add(R.id.container, cameraFragmentOutside).commit();
            if (!fragmentMap.containsKey("out")) {
                Log.e(TAG, "put out");
                fragmentMap.put("out", cameraFragmentOutside);
            }
        }

        Button buttonPoint = findViewById(R.id.btn_menu_point);
        Button buttonMacro = findViewById(R.id.btn_menu_macro);
        Button buttonMicro = findViewById(R.id.btn_menu_micro);
        Button buttonRecord = findViewById(R.id.btn_menu_record);
        Button buttonCompare = findViewById(R.id.btn_menu_compare);
        buttonPoint.setOnClickListener(this);
        buttonMacro.setOnClickListener(this);
        buttonMicro.setOnClickListener(this);
        buttonRecord.setOnClickListener(this);
        buttonCompare.setOnClickListener(this);


    }

    @Override
    public void onResume() {
        super.onResume();
        // TODO：直接放在onCreate中吧
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_13, getApplicationContext(), mLoaderCallback);
        Log.e(TAG, "onResume success load OpenCV...");
    }

    //OpenCV库加载并初始化成功后的回调函数
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case BaseLoaderCallback.SUCCESS:
                    Log.e(TAG, "OpenCV成功加载");
                    break;
                default:
                    super.onManagerConnected(status);
                    Log.e(TAG, "OpenCV加载失败");
                    break;
            }
        }
    };


    // 相机拍摄到照片之后
    @Override
    public void onCameraInteraction(Bundle bundle) {
        int photoType = bundle.getInt(ARG_TYPE);
        int photoNumber = bundle.getInt(ARG_NUMBER);
        tempTag = "" + photoType + "" + photoNumber;
        Log.e(TAG, "Camera拍摄的图片tag: " + tempTag);

        if (photoType == FRAGMENT_TYPE_MICRO) {
            //拍摄的是微观图像
            if (!fragmentMap.containsKey(tempTag)) {
                MicroLocationFragment microLocationFragment = MicroLocationFragment.newInstance(100, 0);
                fragmentMap.put(tempTag, microLocationFragment);
                Log.e(TAG, "134 fragmentMap.put(" + tempTag + ")");
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, fragmentMap.get(tempTag), tempTag)
                        .commit();
            }
            fragmentMap.get(tempTag).setArguments(bundle);
            hideAllFragmentsExcept(tempTag);

        } else if (photoType == FRAGMENT_TYPE_LOC) {
            // 拍摄的是微观的定位图像，更新对应的微观+定位
            tempTag = "" + (photoType - 1) + "" + photoNumber;
            MicroLocationFragment microLocationFragment = (MicroLocationFragment) fragmentMap.get(tempTag);
            // Argument之间传递数据
            microLocationFragment.setArguments(bundle);
            hideAllFragmentsExcept(tempTag);

        } else if (photoType == FRAGMENT_TYPE_Comp) {
            // 拍摄的是比对图像
            tempTag = "" + FRAGMENT_TYPE_Comp;
            if (fragmentMap.containsKey(tempTag)) {
                CompareFragment compareFragment = (CompareFragment) fragmentMap.get(tempTag);
                compareFragment.setArguments(bundle);
                Log.e(TAG, "add3");
                // bug: already added
                hideAllFragmentsExcept(tempTag);
            } else {
                Log.e(TAG, "map中没有tempTag = 4 的 compareFragment");
            }

        } else {
            // 拍摄的是墨点图像/宏观图像; !注意重新拍摄的情况
            if (!fragmentMap.containsKey(tempTag)) {
                PhotoFragment photoFragment = PhotoFragment.newInstance(200, 0);
                fragmentMap.put(tempTag, photoFragment);
                Log.e(TAG, "169 fragmentMap.put(" + tempTag + ")");
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, fragmentMap.get(tempTag), tempTag)
                        .commit();
            }
            // FIXME: 这里重新设置了bundle啊！！覆盖了上面的
            fragmentMap.get(tempTag).setArguments(bundle);
            hideAllFragmentsExcept(tempTag);
        }
    }


    // 图像的点击事件
    @Override
    public void onPhotoInteraction(int photoType, int photoNumber, int buttonCode, Bundle bundle) {

        // FIXME: 导航回来时，bundle可能不存在

        switch (photoType) {
            // 微喷墨点，跳转到宏观拍摄
            case FRAGMENT_TYPE_POINT:
                switch (buttonCode) {
                    case BUTTON_RETURN:
                        break;
                    case BUTTON_NEXT:
                        break;
                }
                Log.e(TAG, "onPhotoInteraction: add with new CameraFragment");

                // second添加cameraIn
                // FIXME: 从menu导航回来之后，点击报错, 因为已经add过了

                if (!fragmentMap.containsKey("in")) {
                    getSupportFragmentManager().beginTransaction().add(R.id.container, cameraFragmentInside).commit();

                    Log.e(TAG, "put in");
                    fragmentMap.put("in", cameraFragmentInside);
                }


                tempBundle = new Bundle();
                tempBundle.putInt(ARG_TYPE, FRAGMENT_TYPE_MACRO);
                tempBundle.putInt(ARG_NUMBER, 1);
                tempBundle.putInt(ARG_LOC_NUM, -1);
                cameraFragmentInside.setArguments(tempBundle);

                // 隐藏微喷墨点photo
                hideAllFragmentsExcept("in");

                break;

            // 宏观图片
            case FRAGMENT_TYPE_MACRO:
                switch (buttonCode) {
                    // 重新拍摄
                    case BUTTON_RETURN:

                        // 宏观重新拍摄
                        tempBundle = new Bundle();
                        tempBundle.putInt(ARG_TYPE, FRAGMENT_TYPE_MACRO);
                        tempBundle.putInt(ARG_NUMBER, photoNumber);
                        tempBundle.putInt(ARG_LOC_NUM, -1);
                        cameraFragmentInside.setArguments(tempBundle);
                        hideAllFragmentsExcept("in");

                        break;
                    // 下一步，宏观位置放置
                    case BUTTON_NEXT:
                        if (photoNumber < Constants.MACRO_PICTURE_NUMBER) {
                            Log.e(TAG, "BUTTON_NEXT: photoNumber < Constants.MACRO_PICTURE_NUMBER");
                            tempBundle = new Bundle();
                            tempBundle.putInt(ARG_TYPE, FRAGMENT_TYPE_MACRO);
                            tempBundle.putInt(ARG_NUMBER, photoNumber + 1);
                            tempBundle.putInt(ARG_LOC_NUM, -1);
                            cameraFragmentInside.setArguments(tempBundle);

                            hideAllFragmentsExcept("in");

                        } else {
                            Log.e(TAG, "BUTTON_NEXT: photoNumber >= Constants.MACRO_PICTURE_NUMBER");
                            tempBundle = new Bundle();
                            tempBundle.putInt(ARG_TYPE, FRAGMENT_TYPE_MICRO);
                            tempBundle.putInt(ARG_NUMBER, 1);
                            tempBundle.putInt(ARG_LOC_NUM, -1);
                            cameraFragmentOutside.setArguments(tempBundle);

                            hideAllFragmentsExcept("out");

                        }
                        break;
                }
                break;

            //　定位图
            case FRAGMENT_TYPE_LOC:

                break;

        }
    }


    // 微观+定位的点击事件
    @Override
    public void onMicroLocationInteraction(int photoType, int photoNumber, int code) {

        // 拍摄loc: 31, 32, 33
        tempTag = "" + (3) + "" + photoNumber;

        switch (code) {
            // 重新拍摄
            case BUTTON_RETURN:


                tempBundle = new Bundle();
                tempBundle.putInt(ARG_TYPE, FRAGMENT_TYPE_MICRO);
                tempBundle.putInt(ARG_NUMBER, photoNumber);
                tempBundle.putInt(ARG_LOC_NUM, -1);
                cameraFragmentOutside.setArguments(tempBundle);
                hideAllFragmentsExcept("out");
                break;

            // 拍摄下一个微观点
            case BUTTON_NEXT:
                if (photoNumber < Constants.MICRO_PICTURE_NUMBER) {
                    tempBundle = new Bundle();
                    tempBundle.putInt(ARG_TYPE, FRAGMENT_TYPE_MICRO);
                    tempBundle.putInt(ARG_NUMBER, photoNumber + 1);
                    tempBundle.putInt(ARG_LOC_NUM, -1);
                    cameraFragmentOutside.setArguments(tempBundle);
                    hideAllFragmentsExcept("out");

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
                tempBundle = new Bundle();
                tempBundle.putInt(ARG_TYPE, FRAGMENT_TYPE_LOC);
                tempBundle.putInt(ARG_NUMBER, photoNumber);
                tempBundle.putInt(ARG_LOC_NUM, 1);
                cameraFragmentInside.setArguments(tempBundle);

                hideAllFragmentsExcept("in");
                break;

            // 拍摄定位点2
            case BUTTON_LOC2:
                Log.e(TAG, "拍摄loc2: " + tempTag);
                tempBundle = new Bundle();
                tempBundle.putInt(ARG_TYPE, FRAGMENT_TYPE_LOC);
                tempBundle.putInt(ARG_NUMBER, photoNumber);
                tempBundle.putInt(ARG_LOC_NUM, 2);
                cameraFragmentInside.setArguments(tempBundle);
                hideAllFragmentsExcept("in");

                break;
        }


    }


    // 比对的点击事件
    @Override
    public void onCompareInteraction(int photoType, int photoNumber, int code) {
        tempTag = "" + FRAGMENT_TYPE_Comp;
        switch (code) {
            // 比对图片1
            case BUTTON_LOC1:
                Log.e(TAG, "比对图片1: ");
                tempBundle = new Bundle();
                tempBundle.putInt(ARG_TYPE, FRAGMENT_TYPE_Comp);
                tempBundle.putInt(ARG_NUMBER, photoNumber);
                tempBundle.putInt(ARG_LOC_NUM, 1);
                cameraFragmentOutside.setArguments(tempBundle);

                hideAllFragmentsExcept("out");

                break;
            case BUTTON_LOC2:
                Log.e(TAG, "比对图片2: ");
                tempBundle = new Bundle();
                tempBundle.putInt(ARG_TYPE, FRAGMENT_TYPE_Comp);
                tempBundle.putInt(ARG_NUMBER, photoNumber);
                tempBundle.putInt(ARG_LOC_NUM, 2);
                cameraFragmentOutside.setArguments(tempBundle);

                hideAllFragmentsExcept("out");

                break;


        }
    }

    @Override
    public void onClick(View view) {
        // TODO: Menu button, 不需要refresh
        Log.e(TAG, "onClick: ");
        switch (view.getId()) {
            case R.id.btn_menu_point:
                if (mMenuState != 11) {
                    hideAllFragmentsExcept("11");
                }
                break;
            case R.id.btn_menu_macro:
                hideAllFragmentsExcept("21");
                break;

            case R.id.btn_menu_micro:
                hideAllFragmentsExcept("31");
                break;
            // 快速比对窗口tempTag = 4
            case R.id.btn_menu_compare:
                Log.e(TAG, "onClick: 比对");
                // 启动快速比对fragment
                tempTag = "" + FRAGMENT_TYPE_Comp;
                FragmentTransaction mFragmentTransaction = getSupportFragmentManager().beginTransaction();

                if (!fragmentMap.containsKey(tempTag)) {
                    CompareFragment compareFragment = CompareFragment.newInstance(500, 0);
                    fragmentMap.put(tempTag, compareFragment);
                    Log.e(TAG, "fragmentMap.put(" + tempTag + ")");
                }

                Log.e(TAG, "add7");
                hideAllFragmentsExcept(tempTag);
                mFragmentTransaction.add(R.id.container, fragmentMap.get(tempTag), tempTag);
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
                Log.e(TAG, "sorry! hide " + key);
                fragmentTransaction.hide(fragment);
            } else {
                Log.e(TAG, "bingo! show " + key);
                fragmentTransaction.show(fragment);
            }
        }
        fragmentTransaction.commit();
    }

}
