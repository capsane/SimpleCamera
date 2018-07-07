package com.capsane.simplecamera;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link onPhotoFragmentListener} interface
 * to handle interaction events.
 * Use the {@link PhotoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PhotoFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "PhotoFragment";

    // 微喷墨点拍摄、宏观拍摄、局部拍摄
    public static final int PHOTO_TYPE_POINT = 1;
    public static final int PHOTO_TYPE_MACRO = 2;
    public static final int PHOTO_TYPE_MICRO = 3;
    public static final int PHOTO_TYPE_LOC = 4;

    public static final int BUTTON_RETURN = 0;
    public static final int BUTTON_NEXT = 1;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "FragmentType";
    private static final String ARG_PARAM2 = "PhotoNumber";

    // TODO: Rename and change types of parameters
    private int mPhotoType;
    private int mPhotoNumber;
    private byte[] mBytes;

    private onPhotoFragmentListener mListener;

    private Button returnButton;
    private Button nextButton;

    public PhotoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param photoType Parameter 1.
     * @param photoNumber Parameter 2.
     * @return A new instance of fragment PhotoFragment.
     */
    public static PhotoFragment newInstance(int photoType, int photoNumber) {
        PhotoFragment fragment = new PhotoFragment();
        // 为了向Activity传递数据
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, photoType);
        args.putInt(ARG_PARAM2, photoNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPhotoType = getArguments().getInt(CameraFragment.ARG_PARAM1);
            Log.e(TAG, "onCreate: " + mPhotoType);
            mPhotoNumber = getArguments().getInt(CameraFragment.ARG_PARAM2);
            mBytes = getArguments().getByteArray("image");
            if (mBytes != null) {
                Log.e(TAG, "bytes.length: " + mBytes.length);
            } else {
                Log.e(TAG, "bytes.length 为0!!!" );
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_photo, container, false);
    }



    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.e(TAG, "onViewCreated");
        ImageView imageView = view.findViewById(R.id.image_view);
        Glide.with(getContext()).load(mBytes).into(imageView);
        returnButton = view.findViewById(R.id.button_return);
        returnButton.setOnClickListener(this);
        nextButton = view.findViewById(R.id.button_next);
        nextButton.setOnClickListener(this);
        TextView tvTitle = view.findViewById(R.id.photo_fragment_title);
        Log.e(TAG, "onViewCreated: mPhotoType: " + mPhotoType);
        switch (mPhotoType) {
            case PHOTO_TYPE_POINT:
                returnButton.setText(R.string.noPoint);
                nextButton.setText(R.string.hasPoint);
                tvTitle.setText(R.string.title_point);
                break;

            case PHOTO_TYPE_MACRO:
                tvTitle.setText("宏观" + mPhotoNumber);
                break;

            case PHOTO_TYPE_MICRO:
                tvTitle.setText("微观" + mPhotoNumber);
                break;

        }


    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof onPhotoFragmentListener) {
            mListener = (onPhotoFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement onPhotoFragmentListener");
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
    public interface onPhotoFragmentListener {
        void onPhotoInteraction(int photoType, int photoNumber, int code);
    }


    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.button_return:
                Log.e(TAG, "button_return");
                mListener.onPhotoInteraction(mPhotoType, mPhotoNumber, BUTTON_RETURN);
                break;

            case R.id.button_next:
                Log.e(TAG, "button_next");
                mListener.onPhotoInteraction(mPhotoType, mPhotoNumber, BUTTON_NEXT);
                break;
        }
    }
}
