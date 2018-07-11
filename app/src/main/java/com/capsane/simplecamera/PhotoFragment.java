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

import static com.capsane.simplecamera.Constants.BUTTON_NEXT;
import static com.capsane.simplecamera.Constants.BUTTON_RETURN;
import static com.capsane.simplecamera.Constants.FRAGMENT_TYPE_MACRO;
import static com.capsane.simplecamera.Constants.FRAGMENT_TYPE_MICRO;
import static com.capsane.simplecamera.Constants.FRAGMENT_TYPE_POINT;


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

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_TYPE = "FragmentType";
    private static final String ARG_NUMBER = "PhotoNumber";
    private static final String ARG_IMAGE = "image";

    // TODO: Rename and change types of parameters
    private int mPhotoType;
    private int mPhotoNumber;
    private byte[] mBytes;

    private onPhotoFragmentListener mListener;

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
        args.putInt(ARG_TYPE, photoType);
        args.putInt(ARG_NUMBER, photoNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPhotoType = getArguments().getInt(ARG_TYPE);
            Log.e(TAG, "onCreate: " + mPhotoType);
            mPhotoNumber = getArguments().getInt(ARG_NUMBER);
            mBytes = getArguments().getByteArray(ARG_IMAGE);
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
        Button returnButton = view.findViewById(R.id.button_return);
        returnButton.setOnClickListener(this);
        Button nextButton = view.findViewById(R.id.button_next);
        nextButton.setOnClickListener(this);
        TextView tvTitle = view.findViewById(R.id.photo_fragment_title);
        Log.e(TAG, "onViewCreated: mPhotoType: " + mPhotoType);
        switch (mPhotoType) {
            case FRAGMENT_TYPE_POINT:
                returnButton.setText(R.string.noPoint);
                nextButton.setText(R.string.hasPoint);
                tvTitle.setText(R.string.title_point);
                break;

            case FRAGMENT_TYPE_MACRO:
                tvTitle.setText("宏观" + mPhotoNumber);
                break;

            case FRAGMENT_TYPE_MICRO:
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
        void onPhotoInteraction(int photoType, int photoNumber, int code, Bundle bundle);
    }


    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.button_return:
                Log.e(TAG, "button_return");
                mListener.onPhotoInteraction(mPhotoType, mPhotoNumber, BUTTON_RETURN, null);
                break;

            case R.id.button_next:
                Log.e(TAG, "button_next");
                Log.e(TAG, "onClick: type:" + mPhotoType + " number:" + mPhotoNumber);
                //
                if (mPhotoType == FRAGMENT_TYPE_MICRO) {
                    Bundle bundle = new Bundle();
                    bundle.putByteArray("image", mBytes);
                }

                mListener.onPhotoInteraction(mPhotoType, mPhotoNumber, BUTTON_NEXT, null);
                break;
        }
    }
}
