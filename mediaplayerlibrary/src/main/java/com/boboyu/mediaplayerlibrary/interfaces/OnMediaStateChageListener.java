package com.boboyu.mediaplayerlibrary.interfaces;

import android.media.MediaPlayer;

import com.boboyu.mediaplayerlibrary.helper.MediaPlayerStatus;

/**
 * Created by wubo on 2016/11/2.
 */
public interface OnMediaStateChageListener {
    void onProgressChange(int position);
    void onPlayCompletion(MediaPlayer mp);
    void onPlayChange(MediaPlayerStatus status);
}
