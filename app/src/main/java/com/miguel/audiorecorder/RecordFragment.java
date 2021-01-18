package com.miguel.audiorecorder;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RecordFragment extends Fragment implements View.OnClickListener {
    public static String TAG = "recordFrag";

    private int RECORD_AUDIO_CODE = 2;

    private NavController mNavController;

    private ImageButton mRecordingsListBtn;
    private ImageButton mRecordBtn;
    private Chronometer mChronometer;
    private TextView mRecordFileNameText;

    private MediaRecorder mMediaRecorder;
    private boolean isRecording = false;

    private String mRecordFile;

    public RecordFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_record, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mNavController = Navigation.findNavController(view);
        mRecordingsListBtn = view.findViewById(R.id.record_list_btn);
        mRecordBtn = view.findViewById(R.id.record_btn);
        mChronometer = view.findViewById(R.id.record_timer);
        mRecordFileNameText = view.findViewById(R.id.record_file_name);

        mRecordingsListBtn.setOnClickListener(this);
        mRecordBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.record_list_btn:
                if(isRecording){
                     new AlertDialog.Builder(getContext())
                            .setTitle("Audio is Still Recording")
                            .setMessage("If you leave this page now recording will stop.")
                            .setPositiveButton("Ok", (dialog, which) -> {
                                stopRecording();
                                isRecording = false;
                                mNavController.navigate(R.id.action_recordFragment_to_recordListFragment);
                            })
                            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                            .create().show();
                }else{
                    mNavController.navigate(R.id.action_recordFragment_to_recordListFragment);
                }
                break;
            case R.id.record_btn:
                HandleRecording();
                break;
        }
    }
    private void HandleRecording(){
        if(isRecording){
            stopRecording();
            Log.d(TAG, "stop recording");
            mRecordBtn.setImageDrawable(getResources().getDrawable(R.drawable.record_btn_stopped, null));
            isRecording = false;
        }else{
            if(checkPermissions()){
                startRecording();
                Log.d(TAG, "started Recording");
                mRecordBtn.setImageDrawable(getResources().getDrawable(R.drawable.record_btn_recording, null));
                isRecording = true;
            }
        }
    }

    private void startRecording() {
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.start();

        String recordPath = getActivity().getExternalFilesDir("/").getAbsolutePath();

        SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.CHINA);

        Date now = new Date();
        // TO-DO: need to do this dynamically
        mRecordFile = "recording_" + format.format(now) + ".3gp";

        mRecordFileNameText.setText("Recording file: " + mRecordFile);

        mMediaRecorder = new MediaRecorder();

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mMediaRecorder.setOutputFile(recordPath + "/" + mRecordFile);

        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mMediaRecorder.start();
    }

    private void stopRecording() {
        mChronometer.stop();
        mRecordFileNameText.setText(getResources().getString(R.string.press_mic_text));

        mMediaRecorder.stop();
        mMediaRecorder.release();
        mMediaRecorder = null;

        Snackbar snackbar = Snackbar.make(getView(), "Recording" + mRecordFile + "saved", Snackbar.LENGTH_LONG);
        snackbar.setAction("Ok", v -> snackbar.dismiss());
        snackbar.show();

        Log.d(TAG, "Media recorder released. File saved");
    }

    private boolean checkPermissions(){
        if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED){
            return true;
        }
        /** If there is no permission, then check if should be requested */
        requestRecordingPermission();
        return false;
    }
    private void requestRecordingPermission(){
        /** Since there is no permission, request it */
        if(ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.RECORD_AUDIO)){
            new AlertDialog.Builder(getContext())
                    .setTitle("Recording Permission")
                    .setMessage("In order to record audio you need to first accept the permission")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_CODE);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        }else{
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_CODE);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(isRecording){
            stopRecording();
            isRecording = false;
        }
    }
}