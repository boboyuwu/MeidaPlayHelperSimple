package com.boboyu.mediaplayerlibrary.helper;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.boboyu.mediaplayerlibrary.constants.Keys;
import com.boboyu.mediaplayerlibrary.interfaces.OnMediaStateChageListener;
import com.boboyu.mediaplayerlibrary.services.MediaService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wubo on 2016/10/18.
 */

public class MediaPlayerHelper implements OnCompletionListener{
    private static final String TAG = "MediaPlayerHelper";
    private static volatile MediaPlayerHelper mediaPlayerHelper;
    private MediaPlayer mediaPlayer;
    private Context mContext;
    private final Uri mUri;
    private boolean isServiceToggleOn;

    //播放的三个状态
    public static final int MEDIAPLAYER_STOP = 1000;
    public static final int MEDIAPLAYER_PLAY = 2000;
    public static final int MEDIAPLAYER_PAUSE = 3000;
    //当前播放的状态
    private int playerStatus = MEDIAPLAYER_STOP;

    //跟新播放进度
    private static final int LOOP_UPDATE_PROGRESS = 100;
    //跟新播放状态(播放、暂停、停止三个状态)
    private static final int UPDATE_PLAY_BUTTON_STATUS = 200;
    //播放完成
    private static final int PLAY_COMPLETION = 300;

    //private OnMediaStateChageListener mOnMediaStateChageListener;

    private ArrayList<OnMediaStateChageListener> mediaStateObserver= new ArrayList();
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOOP_UPDATE_PROGRESS:
                   notifyObservables(LOOP_UPDATE_PROGRESS,msg);
                    break;
                case PLAY_COMPLETION:
                    notifyObservables(PLAY_COMPLETION,msg);
                    break;
                //播放各个状态控制    播放  暂停   停止等
                case UPDATE_PLAY_BUTTON_STATUS:
                    notifyObservables(UPDATE_PLAY_BUTTON_STATUS,msg);
                    break;
            }
        }
    };

    private void notifyObservables(int opt,Message msg){
        switch (opt){
            case LOOP_UPDATE_PROGRESS:
               /* Log.d(TAG,"LOOP_UPDATE_PROGRESS ："+getCurrentPosition());*/
                if (mediaStateObserver != null) {
                    for (OnMediaStateChageListener onMediaStateChageListener : mediaStateObserver) {
                        if(onMediaStateChageListener!=null){
                            onMediaStateChageListener.onProgressChange(getCurrentPosition());
                        }
                    }
                    Message message = mHandler.obtainMessage();
                    message.what = LOOP_UPDATE_PROGRESS;
                    mHandler.sendMessage(message);
                }
                break;
            case UPDATE_PLAY_BUTTON_STATUS:
                int status = (int) msg.obj;
                switch (status){
                    case MEDIAPLAYER_PLAY:
                        if (mediaStateObserver != null) {
                            for (OnMediaStateChageListener onMediaStateChageListener : mediaStateObserver) {
                                if (onMediaStateChageListener != null) {
                                    onMediaStateChageListener.onPlayChange(MediaPlayerStatus.PLAYING);
                                }
                            }
                        }
                        break;
                    case MEDIAPLAYER_PAUSE:
                        if (mediaStateObserver != null) {
                            for (OnMediaStateChageListener onMediaStateChageListener : mediaStateObserver) {
                                if (onMediaStateChageListener != null) {
                                    onMediaStateChageListener.onPlayChange(MediaPlayerStatus.PAUSE);
                                }
                            }
                        }
                        break;

                    case MEDIAPLAYER_STOP:
                        if (mediaStateObserver != null) {
                            for (OnMediaStateChageListener onMediaStateChageListener : mediaStateObserver) {
                                if (onMediaStateChageListener != null) {
                                    onMediaStateChageListener.onPlayChange(MediaPlayerStatus.STOP);
                                }
                            }
                        }
                        break;
                }
                break;

            case PLAY_COMPLETION:
                if (mediaStateObserver != null) {
                    MediaPlayer mp = (MediaPlayer) msg.obj;
                    for (OnMediaStateChageListener onMediaStateChageListener : mediaStateObserver) {
                        if (onMediaStateChageListener != null) {
                            onMediaStateChageListener.onPlayCompletion(mp);
                        }
                    }
                }
                break;

        }
    }

    private MediaPlayerHelper(Context context, String uri) {
        mContext = context;
        mUri = Uri.parse(uri);
        mediaPlayer = MediaPlayer.create(context, mUri);
        mediaPlayer.setOnCompletionListener(this);
    }

    private MediaPlayerHelper(Context context, Uri uri) {
        mContext = context;
        mUri = uri;
        mediaPlayer = MediaPlayer.create(mContext, mUri);
        mediaPlayer.setOnCompletionListener(this);
    }

    public static MediaPlayerHelper builder(Context context, String uri) {
        if (mediaPlayerHelper == null) {
            synchronized (MediaPlayerHelper.class) {
                if (mediaPlayerHelper == null) {
                        mediaPlayerHelper = new MediaPlayerHelper(context.getApplicationContext(), uri);
                        return mediaPlayerHelper;
                }
            }
        }
        return mediaPlayerHelper;
    }


    public static MediaPlayerHelper builder(Context context, Uri uri) {

        if (mediaPlayerHelper == null) {
            synchronized (MediaPlayerHelper.class) {
                if (mediaPlayerHelper == null) {
                        mediaPlayerHelper = new MediaPlayerHelper(context.getApplicationContext(), uri);
                        return mediaPlayerHelper;
                }
            }
        }
        return mediaPlayerHelper;
    }

    public void startOrPause() {
        if (mediaPlayer == null) {
            // 在create之后实际就已经prapre准备好了
            mediaPlayer = MediaPlayer.create(mContext, mUri);
        }
        if (playerStatus == MEDIAPLAYER_STOP) {
            //mediaPlayer.setOnPreparedListener(this);
            Log.d(TAG,"play");
            //是否开启通知
            initMediaService();
            mediaPlayer.start();
            playerStatus = MEDIAPLAYER_PLAY;
            sendPlayStatusMessage(playerStatus);
            startProgressTask();
         } else if(playerStatus == MEDIAPLAYER_PLAY){
            Log.d(TAG,"pause");
            mediaPlayer.pause();
            playerStatus = MEDIAPLAYER_PAUSE;
            sendPlayStatusMessage(playerStatus);
            stopProgressTask();
        }else if(playerStatus == MEDIAPLAYER_PAUSE){
            Log.d(TAG,"resume");
            initMediaService();
            mediaPlayer.start();
            playerStatus = MEDIAPLAYER_PLAY;
            sendPlayStatusMessage(playerStatus);
            startProgressTask();

        }
    }

    private void initMediaService() {
        Log.d(TAG,"isServiceToggleOn:"+isServiceToggleOn);
        if(isServiceToggleOn){
            Intent intent;
            if (!isServiceWork(mContext.getApplicationContext(), MediaService.class.getName())) {
                intent = new Intent(mContext, MediaService.class);
                intent.putExtra(Keys.URI, mUri.toString());
                intent.putExtra(Keys.ID, "215432");
                mContext.startService(intent);
                return;
            }
        }
    }

    /**
     * 判断某个服务是否正在运行的方法
     * @param mContext
     * @param serviceName 是包名+服务的类名（例如：net.loonggg.testbackstage.TestService）
     * @return true代表正在运行，false代表服务没有正在运行
     */
    public boolean isServiceWork(Context mContext, String serviceName) {
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager) mContext
                .getSystemService(mContext.ACTIVITY_SERVICE);
        List<RunningServiceInfo> myList = myAM.getRunningServices(40);
        if (myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).service.getClassName().toString();
            if (mName.equals(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }




    public void start(){
        Log.d(TAG,"start");
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(mContext, mUri);
        }
        //mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.start();
        playerStatus = MEDIAPLAYER_PLAY;
        sendPlayStatusMessage(playerStatus);
        startProgressTask();
    }

    public void pause(){
        Log.d(TAG,"pause");
        if(mediaPlayer!=null&& mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            playerStatus = MEDIAPLAYER_PAUSE;
            sendPlayStatusMessage(playerStatus);
            stopProgressTask();
        }
    }

    private void sendPlayStatusMessage(int status) {
        Message message = mHandler.obtainMessage();
        message.what = UPDATE_PLAY_BUTTON_STATUS;
        message.obj=status;
        mHandler.sendMessage(message);
    }

    public void stop() {
        if (mediaPlayer != null) {
            playerStatus = MEDIAPLAYER_STOP;
            mediaPlayer.stop();
            sendPlayStatusMessage(playerStatus);
            try {
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            stopProgressTask();
        }
    }

    //这种情况出现在当Activity界面关闭并且通知也关闭情况下彻底释放资源
    public void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            //mediaPlayer.reset();
            stop();
            mediaPlayer.release();
            mediaPlayer=null;
            mediaPlayerHelper=null;
        }
    }

    public boolean removeObservable(OnMediaStateChageListener onMediaStateChageListener){
        return mediaStateObserver.remove(onMediaStateChageListener);
    }



    public int getDuration() {
        if (mediaPlayer != null) {
            return mediaPlayer.getDuration();
        }
        return 0;
    }

    public int getCurrentPosition() {
        synchronized (MediaPlayerHelper.class) {
            if (mediaPlayer == null || !isPlaying()) {
                return 0;
            }
            return mediaPlayer.getCurrentPosition();
        }
    }

    public boolean isPlaying() {
        boolean isPlaying = false;
        if(mediaPlayer!=null){
            try {
                isPlaying = mediaPlayer.isPlaying();
            } catch (IllegalStateException e) {
                isPlaying = false;
            }
        }
        return isPlaying;
    }


    public void startProgressTask() {
        //----------定时器记录播放进度---------//
        Message message = mHandler.obtainMessage();
        message.what = LOOP_UPDATE_PROGRESS;
        mHandler.sendMessage(message);
    }


    public void stopProgressTask() {
        mHandler.removeMessages(LOOP_UPDATE_PROGRESS);
    }


    public void setMediaStateChangeListener(OnMediaStateChageListener onMediaStateChageListener) {
        //mOnMediaStateChageListener = onMediaStateChageListener;
        if(!mediaStateObserver.contains(onMediaStateChageListener))
        mediaStateObserver.add(onMediaStateChageListener);
    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        playerStatus = MEDIAPLAYER_STOP;
        //mediaPlayer.reset();
        Message message = mHandler.obtainMessage();
        message.what = PLAY_COMPLETION;
        message.obj=mp;
        mHandler.sendMessage(message);
        stopProgressTask();
    }


    public void isMediaServiceOpen(boolean isServiceToggleOn){
        MediaPlayerHelper.this.isServiceToggleOn=isServiceToggleOn;
    }

}
