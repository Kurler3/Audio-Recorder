package com.miguel.audiorecorder;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TimeUtils {
    public static String getTimeAgo(long timeCreated){
        Date now = new Date();

        long seconds = TimeUnit.MILLISECONDS.toSeconds(now.getTime() - timeCreated);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(now.getTime() - timeCreated);
        long hours = TimeUnit.MILLISECONDS.toHours(now.getTime() - timeCreated);
        long days = TimeUnit.MILLISECONDS.toDays(now.getTime() - timeCreated);

        if(seconds < 60) return "just now";
        else if(minutes==1) return "a minute ago";
        else if(minutes > 1 && minutes < 60) return minutes + " minutes ago";
        else if(hours==1) return "an hour ago";
        else if(hours > 1 && hours < 24) return hours + " hours ago";
        else if(days==1) return "one day ago";
        else return days + " days ago";
    }
    public static String getDuration(Context context,String path){
        Uri uri = Uri.parse(path);
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(context,uri);

        String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(Integer.parseInt(durationStr));

        if(seconds<60){
            return seconds + "s";
        }
        int minutes = (int) seconds / 60;
        return minutes + "m:" + seconds + "s";
    }
}
