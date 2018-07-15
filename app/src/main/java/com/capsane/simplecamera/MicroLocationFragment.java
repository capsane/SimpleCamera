package com.capsane.simplecamera;

import android.content.Context;
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

import com.bumptech.glide.Glide;

import static com.capsane.simplecamera.Constants.ARG_IMAGE;
import static com.capsane.simplecamera.Constants.ARG_LOC_NUM;
import static com.capsane.simplecamera.Constants.ARG_NUMBER;
import static com.capsane.simplecamera.Constants.ARG_TYPE;
import static com.capsane.simplecamera.Constants.BUTTON_LOC1;
import static com.capsane.simplecamera.Constants.BUTTON_LOC2;
import static com.capsane.simplecamera.Constants.BUTTON_RETURN;
import static com.capsane.simplecamera.Constants.BUTTON_NEXT;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnMicroLocationListener} interface
 * to handle interaction events.
 * Use the {@link MicroLocationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MicroLocationFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "MicroLocationFragment";

    private int mPhotoType;
    private int mPhotoNumber;
    private byte[] mBytes;
    private int mLocNum;

    private ImageView ivMicro;
    private ImageView ivLoc1;
    private ImageView ivLoc2;


    private OnMicroLocationListener mListener;

    public MicroLocationFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param type Parameter 1.
     * @param number Parameter 2.
     * @return A new instance of fragment MicroLocationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MicroLocationFragment newInstance(int type, int number) {
        MicroLocationFragment fragment = new MicroLocationFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TYPE, type);
        args.putInt(ARG_NUMBER, number);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPhotoType = getArguments().getInt(ARG_TYPE);
            mPhotoNumber = getArguments().getInt(ARG_NUMBER);
            mBytes = getArguments().getByteArray(ARG_IMAGE);
            mLocNum = getArguments().getInt(ARG_LOC_NUM);
            Log.e(TAG, "onCreate: type: " + mPhotoType + " number:" + mPhotoNumber + "mbytes.len: " + mBytes.length);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e(TAG, "onCreateView: ");
        return inflater.inflate(R.layout.fragment_micro_location, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView tvTitle = view.findViewById(R.id.loc_title);
        ivMicro = view.findViewById(R.id.image_micro);
        ivLoc1 = view.findViewById(R.id.image_loc1);
        ivLoc2 = view.findViewById(R.id.image_loc2);

        ivLoc1.setOnClickListener(this);
        ivLoc2.setOnClickListener(this);

        Button buttonReturn = view.findViewById(R.id.button_loc_return);
        Button buttonNext = view.findViewById(R.id.button_loc_next);
        buttonReturn.setOnClickListener(this);
        buttonNext.setOnClickListener(this);

        tvTitle.setText("微观" + mPhotoNumber);
        Log.e(TAG, "onViewCreated: mLocNum: " + mLocNum);
        switch (mLocNum) {
            case 1:
                Glide.with(getContext()).load(mBytes).into(ivLoc1);
                break;
            case 2:
                Glide.with(getContext()).load(mBytes).into(ivLoc2);
                break;
            default:
                Glide.with(getContext()).load(mBytes).into(ivMicro);
        }



    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        Log.e(TAG, "onCreateAnimation: do nothing");
        return super.onCreateAnimation(transit, enter, nextAnim);
    }

    @Override
    public void onResume() {
        Log.e(TAG, "onResume: ");
        super.onResume();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        Log.e(TAG, "onHiddenChanged: ");
        refresh();
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onAttach(Context context) {
        Log.e(TAG, "onAttach: ");
        super.onAttach(context);
        if (context instanceof OnMicroLocationListener) {
            mListener = (OnMicroLocationListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnMicroLocationListener");
        }
    }

    @Override
    public void onDetach() {
        Log.e(TAG, "onDetach: ");
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
    public interface OnMicroLocationListener {
        void onMicroLocationInteraction(int photoType, int photoNumber, int code);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_loc_return:
                mListener.onMicroLocationInteraction(mPhotoType, mPhotoNumber, BUTTON_RETURN);
                break;
            case R.id.button_loc_next:
                mListener.onMicroLocationInteraction(mPhotoType, mPhotoNumber, BUTTON_NEXT);
                break;

            case R.id.image_loc1:
                mListener.onMicroLocationInteraction(mPhotoType, mPhotoNumber, BUTTON_LOC1);
                break;
            case R.id.image_loc2:
                mListener.onMicroLocationInteraction(mPhotoType, mPhotoNumber, BUTTON_LOC2);
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
            Log.e(TAG, "refresh: type: " + mPhotoType + " number:" + mPhotoNumber + "mbytes.len: " + mBytes.length);
            switch (mLocNum) {
                case 1:
                    Glide.with(getContext()).load(mBytes).into(ivLoc1);
                    break;
                case 2:
                    Glide.with(getContext()).load(mBytes).into(ivLoc2);
                    break;
                default:
                    // FIXME: 如果重新拍摄micro,则同时重置loc
                    Glide.with(getContext()).load(mBytes).into(ivMicro);
                    Glide.with(getContext()).load(R.mipmap.take_picture).into(ivLoc1);
                    Glide.with(getContext()).load(R.mipmap.take_picture).into(ivLoc2);
            }
        } else {
            Log.e(TAG, "refresh: EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEError");
        }
    }

}
