package com.miguel.audiorecorder;

import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;


public class RecordListFragment extends Fragment implements RecordingListAdapter.OnRecordingClickedListener {
    private static String TAG = "listFrag";

    private ConstraintLayout mPlayerSheetLayout;
    private BottomSheetBehavior mBottomSheetBehavior;

    private ImageButton mPlayBtn, mBackwardBtn, mForwardBtn;
    private TextView mPlayerHeader, mPlayerFileName;
    private RecyclerView mRecordingsRecyclerView;
    private RecordingListAdapter mRecordingAdapter;

    private SeekBar mSeekBar;
    private Handler mSeekBarHandler;
    private Runnable mUpdateSeekBar;

    private ArrayList<File> mFileList;
    private File mDirectory;

    private boolean isPlaying = false;
    private File mFileToPlay;
    private File mFileToReplay;

    private MediaPlayer mMediaPlayer;

    public RecordListFragment() {

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_record_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDirectory = new File(getActivity().getExternalFilesDir("/").getAbsolutePath());

        // Gets all the recordings as files
        mFileList = new ArrayList<>(Arrays.asList(mDirectory.listFiles()));
        Collections.reverse(mFileList);

        mPlayerSheetLayout = view.findViewById(R.id.player_sheet_layout);

        /** Instantiating other UI elements  */
        mPlayBtn = view.findViewById(R.id.player_play_btn);
        mBackwardBtn = view.findViewById(R.id.player_backward_btn);
        mForwardBtn = view.findViewById(R.id.player_forward_btn);
        mPlayerHeader = view.findViewById(R.id.player_header_title);
        mPlayerFileName = view.findViewById(R.id.player_filename);
        mSeekBar = view.findViewById(R.id.player_seek_bar);
        //-----------------------------------------------------------------------
        // Handling recycler view stuff
        mRecordingsRecyclerView = view.findViewById(R.id.recording_list_view);
        mRecordingAdapter = new RecordingListAdapter(getContext(),mFileList, this);

        mRecordingsRecyclerView.setAdapter(mRecordingAdapter);
        mRecordingsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        //---------------------------------------------------------------------------------
        mBottomSheetBehavior = BottomSheetBehavior.from(mPlayerSheetLayout);

        mBottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if(newState == BottomSheetBehavior.STATE_HIDDEN){
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // Don't want to do anything here
            }
        });

        mPlayBtn.setOnClickListener(v -> {
            if(isPlaying){
                pauseAudio();
                isPlaying = false;
            }else{
                // Either user is asking to play again (the current finished) or to start playing
                if(mFileToPlay==null && mFileToReplay!=null){
                    playAudio(mFileToReplay);
                    isPlaying = true;
                }else if(mMediaPlayer!=null){
                    continueAudio();
                    isPlaying = true;
                }else{
                    Snackbar snackbar = Snackbar.make(getView(), "No Audio File Choosen", Snackbar.LENGTH_LONG);
                    snackbar.setAction("Ok", v1 -> snackbar.dismiss());
                    snackbar.show();
                    return;
                }
            }
        });

        /** TO-DO: implement backward and forward buttons functionality */


        /** The user can manipulate the audio playing touching the seek bar */
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if(mFileToPlay!=null){
                    pauseAudio();
                    isPlaying = false;
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(mFileToPlay!=null){
                    int progress = seekBar.getProgress();

                    mMediaPlayer.seekTo(progress);

                    continueAudio();
                    isPlaying = true;
                }
            }
        });
    }
    @Override
    public void onClickListener(File toBePlayed) {
        if (mFileToPlay==null){
            mFileToPlay = toBePlayed;
            playAudio(mFileToPlay);
            isPlaying = true;
            return;
        }
        // If it is playing and it was clicked then stop it
        if(isPlaying){
            // If the file is the same as the one playing then just stop it
            if(toBePlayed.compareTo(mFileToPlay) == 0){
                // Stop it but keep the seek bar progress and everything
                pauseAudio();
                isPlaying = false;
            }else{
                //Stop the current playing one and start the clicked one
                stopAudio();
                mFileToPlay = toBePlayed;
                playAudio(mFileToPlay);
            }
        }else{
            //If its the same file (it was paused) then just continue playing it
            if(toBePlayed.compareTo(mFileToPlay) == 0){
                // Continue playing it from where it was
                continueAudio();
            }else{
                // Restart the seek bar and start playing the new one from 0
                stopAudio();
                mFileToPlay = toBePlayed;
                playAudio(mFileToPlay);
            }
            // Either way its going to start playing
            isPlaying = true;
        }
    }
    private void pauseAudio(){
        mMediaPlayer.pause();

        // Stop updating when its paused
        mSeekBarHandler.removeCallbacks(mUpdateSeekBar);

        mPlayBtn.setImageDrawable(getResources().getDrawable(R.drawable.player_play_btn, null));
        mPlayerHeader.setText(R.string.paused);
    }
    private void continueAudio(){
        mMediaPlayer.start();

        updateSeekBar();
        mSeekBarHandler.postDelayed(mUpdateSeekBar, 0);
        mPlayBtn.setImageDrawable(getResources().getDrawable(R.drawable.player_pause_btn, null));
        mPlayerHeader.setText(R.string.playing);
    }
    private void stopAudio() {
        mMediaPlayer.stop();
        mMediaPlayer.release();
        mMediaPlayer = null;

        mFileToPlay = null;

        mSeekBarHandler.removeCallbacks(mUpdateSeekBar);
        // UI
        mPlayBtn.setImageDrawable(getResources().getDrawable(R.drawable.player_play_btn, null));
        mPlayerHeader.setText(R.string.not_playing);
        mPlayerFileName.setText("No Audio Playing");
    }

    private void playAudio(File file) {
        mFileToReplay = null;
        mMediaPlayer = new MediaPlayer();

        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        try {
            mMediaPlayer.setDataSource(file.getAbsolutePath());
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /** Change UI elements */
        mPlayBtn.setImageDrawable(getResources().getDrawable(R.drawable.player_pause_btn, null));
        mPlayerFileName.setText(file.getName());
        mPlayerHeader.setText(getResources().getString(R.string.playing));
//
        // Set a completion listener on this instance
        mMediaPlayer.setOnCompletionListener(mp -> {
            stopAudio();
            mPlayerHeader.setText(R.string.finished);
            isPlaying = false;
            mFileToReplay = file;
        });

        mSeekBar.setMax(mMediaPlayer.getDuration());

        mSeekBarHandler = new Handler();

        updateSeekBar();
        mSeekBarHandler.postDelayed(mUpdateSeekBar, 0);
    }
    private void updateSeekBar(){
        mUpdateSeekBar = new Runnable() {
            @Override
            public void run() {
                if(mMediaPlayer!=null){
                    mSeekBar.setProgress(mMediaPlayer.getCurrentPosition());
                }
                mSeekBarHandler.postDelayed(this, 50);
            }
        };
    }

    @Override
    public void onStop() {
        super.onStop();

        if(isPlaying){
            stopAudio();
            isPlaying = false;
        }
    }
}