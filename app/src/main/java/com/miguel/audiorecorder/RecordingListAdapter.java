package com.miguel.audiorecorder;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class RecordingListAdapter extends RecyclerView.Adapter<RecordingListAdapter.RecordingViewHolder> {

    private Context mContext;
    private ArrayList<File> mFileList;
    private OnRecordingClickedListener mListener;

    public RecordingListAdapter(Context context ,ArrayList<File> mFileList, OnRecordingClickedListener listener) {
        this.mContext = context;
        this.mFileList = mFileList;
        this.mListener = listener;
    }

    public class RecordingViewHolder extends RecyclerView.ViewHolder{
        TextView mRecordingTitle, mRecordingDate, mRecordingSize;
        CardView mRecordingCardView;
        public RecordingViewHolder(@NonNull View itemView) {
            super(itemView);

            mRecordingTitle = itemView.findViewById(R.id.recording_item_title);
            mRecordingDate = itemView.findViewById(R.id.recording_item_date);
            mRecordingSize = itemView.findViewById(R.id.recording_item_size);
            mRecordingCardView = itemView.findViewById(R.id.recording_item_card_view);

            itemView.setOnClickListener(v -> mListener.onClickListener(mFileList.get(getAdapterPosition())));
        }
    }
    @NonNull
    @Override
    public RecordingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recording_item, parent, false);
        return new RecordingViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordingViewHolder holder, int position) {
        File recording = mFileList.get(position);

        holder.mRecordingTitle.setText(recording.getName());
        holder.mRecordingDate.setText(TimeUtils.getTimeAgo(recording.lastModified()));
        holder.mRecordingSize.setText(TimeUtils.getDuration(mContext, recording.getAbsolutePath()));
    }
    @Override
    public int getItemCount() {
        return mFileList.size();
    }
    public interface OnRecordingClickedListener{
        void onClickListener(File file);
    }
    public void deleteFile(int position){
        mFileList.remove(position);
        notifyItemRemoved(position);
    }
    public void addFile(File file, int position){
        mFileList.add(position, file);
        notifyItemInserted(position);
    }
}
