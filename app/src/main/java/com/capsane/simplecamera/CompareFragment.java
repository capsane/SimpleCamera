package com.capsane.simplecamera;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;


import java.util.LinkedList;

import static com.capsane.simplecamera.Constants.ARG_IMAGE;
import static com.capsane.simplecamera.Constants.ARG_LOC_NUM;
import static com.capsane.simplecamera.Constants.ARG_NUMBER;
import static com.capsane.simplecamera.Constants.ARG_TYPE;
import static com.capsane.simplecamera.Constants.BUTTON_LOC1;
import static com.capsane.simplecamera.Constants.BUTTON_LOC2;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnCompareFragmentListener} interface
 * to handle interaction events.
 * Use the {@link CompareFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CompareFragment extends Fragment implements View.OnClickListener{

    private static final String TAG = "CompareFragment";

    private static SoundPool soundPool;

    private static int soundYes, soundNo;
    private static int streamYes, streamNo;

    // compare result
    private static int totalCountScene, totalCountObject, goodCount;
    private static double totalAvgDis, goodAvgDis;

    private int mPhotoType;
    private int mPhotoNumber;
    private byte[] mBytes;
    private int mLocNum;

    private ImageView imageView1;
    private ImageView imageView2;
    private Button buttonCompare;
    private TextView textView;


    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnCompareFragmentListener mListener;

    public CompareFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CompareFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CompareFragment newInstance(int param1, int param2) {
        CompareFragment fragment = new CompareFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, param1);
        args.putInt(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        // FIXME: 可能会出错
        soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        soundYes = soundPool.load(getContext(), R.raw.yes, 1);
        soundNo = soundPool.load(getContext(), R.raw.no, 1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_compare, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        textView = view.findViewById(R.id.tv_compare_result);
        imageView1 = view.findViewById(R.id.compare_iv1);
        imageView2 = view.findViewById(R.id.compare_iv2);
        buttonCompare = view.findViewById(R.id.button_compare);
        imageView1.setOnClickListener(this);
        imageView2.setOnClickListener(this);
        buttonCompare.setOnClickListener(this);
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        Log.e(TAG, "onCreateAnimation: ");
        refresh();
        return super.onCreateAnimation(transit, enter, nextAnim);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCompareFragmentListener) {
            mListener = (OnCompareFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnCompareFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnCompareFragmentListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(int photoType, int photoNumber, int code);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.compare_iv1:
                Log.e(TAG, "onClick: loc1");
                mListener.onFragmentInteraction(mPhotoType, mPhotoNumber, BUTTON_LOC1);


                break;
            case R.id.compare_iv2:
                Log.e(TAG, "onClick: loc2");
                mListener.onFragmentInteraction(mPhotoType, mPhotoNumber, BUTTON_LOC2);

                break;
            case R.id.button_compare:

                boolean result = compare();
                if (result) {
                    // sound
                    showToast("原件");
                    streamYes = soundPool.play(soundYes, 0.5f, 0.5f, 1, 0, 1.0f);
                } else {
                    // sound
                    showToast("不匹配");
                    streamNo = soundPool.play(soundNo, 0.5f, 0.5f, 1, 0, 1.0f);
                }

                String text = "图1特征数:" + totalCountScene + ", 图2特征数:"
                        + totalCountObject + ", 匹配特征数:" + goodCount + "\n全部平均距离:"
                        + (int)totalAvgDis + ", 匹配平均距离:" + (int)goodAvgDis;

                textView.setText(text);
                break;
        }
    }

    private void refresh() {
        // capsane：　添加loc1, loc2
        if (getArguments() != null) {
            mPhotoType = getArguments().getInt(ARG_TYPE);
            mPhotoNumber = getArguments().getInt(ARG_NUMBER);
            mBytes = getArguments().getByteArray(ARG_IMAGE);
            mLocNum = getArguments().getInt(ARG_LOC_NUM);
            Log.e(TAG, "refresh: type: " + mPhotoType + " number:" + mPhotoNumber);
            switch (mLocNum) {
                case 1:
                    // rotate the photo
                    Glide.with(getContext()).load(mBytes).transform(new RotateTransformation(getContext(), 90)).into(imageView1);
                    Log.e(TAG, "imageView1: "+ imageView1.getWidth() + ", " + imageView1.getHeight());
                    break;
                case 2:
                    Glide.with(getContext()).load(mBytes).transform(new RotateTransformation(getContext(), 90)).into(imageView2);
                    Log.e(TAG, "imageView2: "+ imageView2.getWidth() + ", " + imageView2.getHeight());
                    break;
                default:
                    Log.e(TAG, "wrong mLocNum: " + mLocNum);
            }
        } else {
            Log.e(TAG, "refresh: EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEError");
        }
    }

    private boolean compare() {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;

        imageView1.setDrawingCacheEnabled(true);
        imageView2.setDrawingCacheEnabled(true);
        Bitmap bitmap1 = imageView1.getDrawingCache();
        Bitmap bitmap2 = imageView2.getDrawingCache();

        Mat mat1 = new Mat();
        Mat mat2 = new Mat();
//
        Utils.bitmapToMat(bitmap1, mat1);
        Utils.bitmapToMat(bitmap2, mat2);
        Log.e(TAG, "Raw Mat1: " + mat1.size());
        Log.e(TAG, "Raw Mat2: " + mat2.size());

        mat1 = OpenCVActivity.blur(mat1, 3);
        mat2 = OpenCVActivity.blur(mat2, 3);
        Log.e(TAG, "Blur Mat1: " + mat1.size());
        Log.e(TAG, "Blur Mat2: " + mat2.size());

        findObject(mat1, mat2, 2);

        // 清空缓存
        imageView1.setDrawingCacheEnabled(false);
        imageView2.setDrawingCacheEnabled(false);

        // judge

        int minCount = Math.min(totalCountObject, totalCountScene);

        if ((totalCountObject*1.0/totalCountScene) > 1.5 ||
                (totalCountScene*1.0)/totalCountObject > 1.5) {
            return false;
        } else if (goodCount*1.0/minCount < 0.10) {
            return false;
        }
        return true;
    }

    public static Mat findObject(Mat sceneMat, Mat objectMat, int accuracy) {
        // keyPoints
        MatOfKeyPoint mkpObject =new MatOfKeyPoint(), mkpScene =new MatOfKeyPoint();
        // 从图片中提取特征点，保存在Mat中，即图片的特征向量
        FeatureDetector featureDetector = FeatureDetector.create(FeatureDetector.ORB);
        featureDetector.detect(objectMat, mkpObject);
        featureDetector.detect(sceneMat, mkpScene);

        totalCountObject = (int)mkpObject.size().height;
        totalCountScene = (int)mkpScene.size().height;

        Log.e(TAG, "mkpObject: " + mkpObject.size());
        Log.e(TAG, "mkpScene: " + mkpScene.size());
        String text0 = mkpObject.size() + ", " + mkpScene.size();

        // 提取特征描述子
        DescriptorExtractor descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        Mat dspObject = new Mat(), dspScene = new Mat();
        descriptorExtractor.compute(objectMat, mkpObject, dspObject);
        descriptorExtractor.compute(sceneMat, mkpScene, dspScene);

        // 特征点匹配
        MatOfDMatch matOfDMatch = new MatOfDMatch();
        DescriptorMatcher descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);
        descriptorMatcher.match(dspObject, dspScene, matOfDMatch);
        Log.e(TAG, "matOfDMatch.size: " + matOfDMatch.size());


        // find the max and min distance
        DMatch[] result = matOfDMatch.toArray();
        float minDis = 10000.0f, maxDis = 0.0f, sumDis = 0.0f;
        for (DMatch dMatch : result) {
            if (dMatch.distance < minDis) {
                minDis = dMatch.distance;
            }
            else if (dMatch.distance > maxDis) {
                maxDis = dMatch.distance;
            }
            sumDis += dMatch.distance;
        }

        totalAvgDis = sumDis/result.length;

        Log.e(TAG, "total matches: " + result.length);
        Log.e(TAG, "total sum: " + sumDis + ", avg: " + sumDis/result.length);
        Log.e(TAG, "total min: " + minDis + ", max: " + maxDis);

        // 选取匹配较好的特征点,gm
        maxDis = 0.0f;
        sumDis = 0.0f;
        LinkedList<DMatch> goodMatches = new LinkedList<>();
        for (int i = 0; i < dspObject.rows(); i++) {
            if (result[i].distance <= 200) {
                goodMatches.addLast(result[i]);
                if (result[i].distance < minDis) {
                    minDis = result[i].distance;
                }
                else if (result[i].distance > maxDis) {
                    maxDis = result[i].distance;
                }
                sumDis += result[i].distance;
            }
        }

        MatOfDMatch gm = new MatOfDMatch();
        gm.fromList(goodMatches);

        goodCount = goodMatches.size();
        goodAvgDis = sumDis/goodMatches.size();

        Log.e(TAG,"good matches: " + goodMatches.size());
        Log.e(TAG, "sum: " + sumDis + ", avg: " + sumDis/goodMatches.size());
        Log.e(TAG, "min: " + minDis + ", max: " + maxDis);

        // 画出特征点匹配图
        Mat matchPic = new Mat();
//        Features2d.drawMatches(objectMat, mkpObject, sceneMat, mkpScene, matOfDMatch, matchPic);
        Features2d.drawMatches(objectMat, mkpObject, sceneMat, mkpScene, gm, matchPic, new Scalar(0,0,255),
                new Scalar(255,0,0), new MatOfByte(), 2);

        // 添加匹配结果信息
        double count = gm.size().height;
        String text1 = "count: " + gm.size() + " accuracy: "+ accuracy;
        String text2 = "distance: " + (int)(sumDis/count) + " " + (int)minDis + " " + (int)maxDis;

        Log.e(TAG, "result: " + text1 + "\n" + text2);
        Core.putText(matchPic, text0, new Point(20, matchPic.height()-80), Core.FONT_ITALIC, 0.6, new Scalar(255));
        Core.putText(matchPic, text1, new Point(20, matchPic.height()-50), Core.FONT_ITALIC, 0.6, new Scalar(255));
        Core.putText(matchPic, text2, new Point(20, matchPic.height()-20), Core.FONT_ITALIC, 0.6, new Scalar(255));

//        Highgui.imwrite(scene + "_ORB"+gm.size()+"." + picType, matchPic);

        Bitmap dstBitmap = Bitmap.createBitmap(matchPic.width(), matchPic.height(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(matchPic, dstBitmap); //convert mat to bitmap
//        imageView.setImageBitmap(dstBitmap);

        return matchPic;
    }


    private void showToast(final String text) {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}
