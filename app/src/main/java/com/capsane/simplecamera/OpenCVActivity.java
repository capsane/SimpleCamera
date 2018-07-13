package com.capsane.simplecamera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
//FIXME: opencv3中为DMatch : import org.opencv.core.DMatch;
//FIXME: opencv2中为：       import org.opencv.features2d.DMatch;
import org.opencv.core.Core;
import org.opencv.core.Point;
import org.opencv.features2d.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;

import java.util.LinkedList;

public class OpenCVActivity extends AppCompatActivity implements View.OnClickListener {


    private Button button;
    private ImageView imageView;

    private Bitmap srcBitmap;
    private Bitmap dstBitmap;
    private static boolean flag = true;
    private static boolean isFirst = true;
    private static final String TAG = "OpenCV Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_cv);

        imageView = findViewById(R.id.img);
        button = findViewById(R.id.btn_gray);
        button.setOnClickListener(new ProcessClickListener());

        Button buttonOrb = findViewById(R.id.btn_orb);
        buttonOrb.setOnClickListener(this);

        Button buttonBrisk = findViewById(R.id.btn_brisk);
        buttonBrisk.setOnClickListener(this);

        Button buttonMatch = findViewById(R.id.btn_match);
        buttonMatch.setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        //load OpenCV engine and init OpenCV library
        //Current OpenCV Library version: public static final String OPENCV_VERSION = "3.4.1";
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


    public Bitmap process() {
        Mat srcMat = new Mat();
        Mat dstMat = new Mat();
        // 图片输入
        srcBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pic1);
        dstBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);
        Utils.bitmapToMat(srcBitmap, srcMat);//convert original bitmap to Mat, R G B.
        Log.e(TAG, "process: src: " + srcMat.size() + ":" + srcMat.channels());
        // 1. gray
        Imgproc.cvtColor(srcMat, dstMat, Imgproc.COLOR_RGB2GRAY);
        Log.e(TAG, "process: dst: " + dstMat.size() + ":" + dstMat.channels());
        //
        Utils.matToBitmap(dstMat, dstBitmap); //convert mat to bitmap
        return dstBitmap;
    }

    // sift特征描述子
    public Mat sift(Bitmap srcBitmap) {
        Mat srcMat = new Mat();
        srcBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.picturea);
        dstBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);

        Bitmap dstBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);

        Utils.bitmapToMat(srcBitmap, srcMat);
        Log.e(TAG, "sift: " + srcMat.size() + ":" + srcMat.channels());

        MatOfKeyPoint mkpSift = new MatOfKeyPoint();
        FeatureDetector featureDetectorSift = FeatureDetector.create(FeatureDetector.SIFT);
        featureDetectorSift.detect(srcMat, mkpSift);
        // 画出特征点
        Mat picWithKeyPoints = new Mat();
        Features2d.drawKeypoints(srcMat, mkpSift, picWithKeyPoints);
        // 保存,描绘特征点图片
        Utils.matToBitmap(picWithKeyPoints, dstBitmap); //convert mat to bitmap
        imageView.setImageBitmap(dstBitmap);

        // 提取特征描述子
        DescriptorExtractor deSIFT = DescriptorExtractor.create(DescriptorExtractor.SIFT);
        Mat descSift = new Mat();
        deSIFT.compute(srcMat, mkpSift, descSift);
        Log.e(TAG, "" + descSift.size());
        return descSift;

    }

    // sift特征描述子
    public Mat orb(Bitmap srcBitmap) {
        Mat srcMat = new Mat();
        Mat srcMat3 = new Mat();
        Utils.bitmapToMat(srcBitmap, srcMat);
        Log.e(TAG, "raw mat: " + srcMat.size() + ":" + srcMat.channels());
        Imgproc.cvtColor(srcMat, srcMat3, Imgproc.COLOR_RGBA2RGB);
        Log.e(TAG, "raw mat3: " + srcMat3.size() + ":" + srcMat3.channels());

        // 提取特征点
        MatOfKeyPoint mkpORB = new MatOfKeyPoint();
        FeatureDetector featureDetectorSift = FeatureDetector.create(FeatureDetector.ORB);
        featureDetectorSift.detect(srcMat3, mkpORB);
        Log.e(TAG, "mkpORB: " + mkpORB.size());

        // 画出特征点
        Mat picWithKeyPoints = new Mat();
        Features2d.drawKeypoints(srcMat3, mkpORB, picWithKeyPoints);
        Log.e(TAG, "picWithKeyPoints: " + picWithKeyPoints.size());

        // 保存,描绘特征点图片
        Bitmap dstBitmap = Bitmap.createBitmap(picWithKeyPoints.width(), picWithKeyPoints.height(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(picWithKeyPoints, dstBitmap); //convert mat to bitmap
        imageView.setImageBitmap(dstBitmap);

        // 提取特征描述子
        DescriptorExtractor deSIFT = DescriptorExtractor.create(DescriptorExtractor.ORB);
        Mat descORB = new Mat();
        deSIFT.compute(srcMat3, mkpORB, descORB);
        Log.e(TAG, "descORB： " + descORB.size());
        return descORB;

    }

    // sift特征描述子
    public Mat brisk(Bitmap srcBitmap) {
        Mat srcMat = new Mat();
        Mat srcMat3 = new Mat();
        Utils.bitmapToMat(srcBitmap, srcMat);
        Log.e(TAG, "brisk: initial" + srcMat.size() + ":" + srcMat.channels());
        Imgproc.cvtColor(srcMat, srcMat3, Imgproc.COLOR_RGBA2RGB);

        MatOfKeyPoint mkpSift = new MatOfKeyPoint();
        FeatureDetector featureDetectorSift = FeatureDetector.create(FeatureDetector.BRISK);
        featureDetectorSift.detect(srcMat3, mkpSift);

        // 画出特征点
        Mat picWithKeyPoints = new Mat();
        Features2d.drawKeypoints(srcMat3, mkpSift, picWithKeyPoints);

        // 保存,描绘特征点图片
        Log.e(TAG, "save: width:" + srcBitmap.getWidth() + " height:" + srcBitmap.getHeight());
        Bitmap dstBitmap = Bitmap.createBitmap(picWithKeyPoints.width(), picWithKeyPoints.height(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(picWithKeyPoints, dstBitmap); //convert mat to bitmap
        imageView.setImageBitmap(dstBitmap);

        // 提取特征描述子
        DescriptorExtractor deSIFT = DescriptorExtractor.create(DescriptorExtractor.BRISK);
        Mat descSift = new Mat();
        deSIFT.compute(srcMat3, mkpSift, descSift);
        Log.e(TAG, "brisk: 特征数量： " + descSift.size());
        return descSift;

    }


    /**
     * 高斯模糊
     * @param image 图像源
     * @param width 模糊半径
     * */
    public static Mat blur(Mat image, int width) {
        Log.e(TAG, "blur: " + width);
        Mat dst = new Mat();
        Imgproc.GaussianBlur(image, dst, new Size(width, width), 0);
        return dst;
    }


    /**
     * 降采样
     * @param image 图像源
     * @param min 缩小倍数
     */
    public static Mat resize(Mat image, int min) {
        Log.e(TAG, "resize: " + min);
        Mat dstMat = new Mat();
        int width = image.cols(), height = image.rows();
        Size size = new Size(width/min, height/min);
        Imgproc.resize(image, dstMat, size);
        return dstMat;
    }

    /**
     * 图像匹配
     * @param sceneMat 场景图像
     * @param objectMat 目标图像
     * @param accuracy 匹配精度
     * */
    public static Mat findObject(Mat sceneMat, Mat objectMat, int accuracy) {
        // keyPoints
        MatOfKeyPoint mkpObject =new MatOfKeyPoint(), mkpScene =new MatOfKeyPoint();
        // 从图片中提取特征点，保存在Mat中，即图片的特征向量
        FeatureDetector featureDetector = FeatureDetector.create(FeatureDetector.ORB);
        featureDetector.detect(objectMat, mkpObject);
        featureDetector.detect(sceneMat, mkpScene);

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





    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_gray:

                break;

            case R.id.btn_orb:
                BitmapFactory.Options options = new BitmapFactory.Options();
//                options.inJustDecodeBounds = true;  // 不生成bitmap,只获取尺寸
                options.inScaled = false;
                Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(), R.drawable.a4, options);
                Mat mat = new Mat();
                Utils.bitmapToMat(bitmap1, mat);
                Log.e(TAG, "Raw Mat: " + mat.size());
                orb(bitmap1);
                break;


            case R.id.btn_brisk:
                options = new BitmapFactory.Options();
                options.inScaled = false;
                bitmap1 = BitmapFactory.decodeResource(getResources(), R.drawable.a4, options);
                mat = new Mat();
                Utils.bitmapToMat(bitmap1, mat);
                Log.e(TAG, "Raw Mat: " + mat.size());
                brisk(bitmap1);
                break;

            case R.id.btn_match:
                options = new BitmapFactory.Options();
                options.inScaled = false;
                bitmap1 = BitmapFactory.decodeResource(getResources(), R.drawable.a4, options);
                Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.b4, options);
                Mat mat1 = new Mat();
                Mat mat2 = new Mat();

                Utils.bitmapToMat(bitmap1, mat1);
                Utils.bitmapToMat(bitmap2, mat2);
                Log.e(TAG, "Raw Mat1: " + mat1.size());
                Log.e(TAG, "Raw Mat2: " + mat2.size());

                findObject(mat1, mat2, 2);

                break;
        }
    }


    public class ProcessClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            if (isFirst) {
                process();
                isFirst = false;
            }
            if (flag) {
                imageView.setImageBitmap(dstBitmap);
                button.setText("查看原图");
                flag = false;
            } else {
                imageView.setImageBitmap(srcBitmap);
                button.setText("灰度化");
                flag = true;
            }
        }
    }

}
