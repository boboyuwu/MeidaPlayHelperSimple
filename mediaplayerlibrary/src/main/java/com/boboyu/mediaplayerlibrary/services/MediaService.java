package com.boboyu.mediaplayerlibrary.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.boboyu.mediaplayerlibrary.R;
import com.boboyu.mediaplayerlibrary.helper.MediaPlayerHelper;
import com.boboyu.mediaplayerlibrary.helper.MediaPlayerStatus;
import com.boboyu.mediaplayerlibrary.interfaces.OnMediaStateChageListener;
import com.boboyu.mediaplayerlibrary.utils.CollapseStatusBarUtils;

public class MediaService extends Service implements OnMediaStateChageListener {
    public static final int TYPE_Customer1 = 1                                                                                                  ;

    private static final int CommandPlay = 11;
    private static final int CommandNext = 22;
    private static final int CommandClose = 33;

    private static final int StatusStop = 111;
    private static final int StatusPlay = 222;
    private static final int CommandJump = 333;
    private boolean isServiceFirstStart=true;
    private static final String TAG = "MediaService";


    NotificationManager manger;
    MediaPlayerHelper mediaPlayer;
    int playerStatus = StatusStop;
    private String mUri;
    private RemoteViews mRemoteViews;
    private Notification mNotification;
    private NotificationCompat.Builder mBuilder;
    private long mNewsId;

    @Override
    public void onCreate() {
        mRemoteViews = new RemoteViews(getPackageName(), R.layout.notification_customer);
        manger = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("Notification");
        mBuilder.setContentText("自定义通知栏示例");
        mBuilder.setSmallIcon(R.drawable.ic_launcher);
        //builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.push));
        mBuilder.setAutoCancel(false);
        mBuilder.setOngoing(true);
        mBuilder.setShowWhen(false);

        mBuilder.setContent(mRemoteViews);
        mNotification = mBuilder.build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return super.onStartCommand(intent, flags, startId);
        }
        int command = intent.getIntExtra("command", 0);
        mUri = intent.getStringExtra("uri");

       /* mNewsId = intent.getLongExtra("newsid",0);*/

        mediaPlayer = MediaPlayerHelper.builder(this, mUri);
        mediaPlayer.setMediaStateChangeListener(this);
        playerStatus=mediaPlayer.isPlaying()?StatusPlay:StatusStop;
        Log.d(TAG,"isMediaPlaying:"+playerStatus);
        if (command == 0 ) {
            command = CommandPlay;
        }
        if (command == CommandClose ) {
            playerStatus = StatusStop;
            manger.cancel(TYPE_Customer1);
        }else if(command==CommandJump){
            CollapseStatusBarUtils.collapseStatusBar(getApplicationContext());
          /*  Bundle bundle = new Bundle();
            bundle.putLong(Keys.NEWS_ID, mNewsId);
            bundle.putBoolean(Keys.IS_NEWS, true);
            Intent jump2NewsDetail=new Intent(Intent.ACTION_MAIN);
            jump2NewsDetail.addCategory(Intent.CATEGORY_LAUNCHER);
            jump2NewsDetail.setClass(this,FeMorningNewsDetailActivity.class);
            jump2NewsDetail.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            startActivity(jump2NewsDetail);*/
        }else{
            sendCustomerNotification(command);
        }
        setMediaPlayer(command, playerStatus);


        return super.onStartCommand(intent, flags, startId);
    }

    private void sendCustomerNotification(int command) {


        mRemoteViews.setTextViewText(R.id.title, "通知栏标题");
        mRemoteViews.setTextViewText(R.id.text, "通知栏内容");
        /*if(command==CommandNext){
            remoteViews.setImageViewResource(R.id.btn1,R.drawable.ic_pause_white);
        }else */
        if (command == CommandPlay) {
            if (playerStatus == StatusStop) {
                mRemoteViews.setImageViewResource(R.id.btn1, R.drawable.ic_pause_white);
            } else {
                mRemoteViews.setImageViewResource(R.id.btn1, R.drawable.ic_play_arrow_white);
            }
        }
        Intent Intent1 = new Intent(this, MediaService.class);
        Intent1.putExtra("command", CommandPlay);
        Intent1.putExtra("uri", mUri);
        Intent1.putExtra("newsid",mNewsId);
        PendingIntent PIntent1 = PendingIntent.getService(this, 5, Intent1, 0);
        mRemoteViews.setOnClickPendingIntent(R.id.btn1, PIntent1);

        Intent Intent2 = new Intent(this,MediaService.class);
        Intent2.putExtra("command",CommandJump);
        Intent2.putExtra("uri", mUri);
        Intent2.putExtra("newsid",mNewsId);
        PendingIntent PIntent4 =   PendingIntent.getService(this,6,Intent2,0);
        mRemoteViews.setOnClickPendingIntent(R.id.status_bar_latest_event_content,PIntent4);

        Intent Intent3 = new Intent(this, MediaService.class);
        Intent3.putExtra("command", CommandClose);
        Intent3.putExtra("uri", mUri);
        Intent3.putExtra("newsid",mNewsId);
        PendingIntent PIntent3 = PendingIntent.getService(this, 7, Intent3, 0);
        mRemoteViews.setOnClickPendingIntent(R.id.btn3, PIntent3);


        manger.notify(TYPE_Customer1, mNotification);
    }

    private void setMediaPlayer(int command, int status) {
        Log.d(TAG,"==== command ===="+command+"  status ===="+status);
        if(isServiceFirstStart){
            //我们需要防止第一次进来的情况 开启服务是耗时的虽然逻辑先开启服务但是helper里面先start
            isServiceFirstStart=false;
            return;
        }
        switch (command) {
            case CommandPlay:
                if (status == StatusStop) {
                    if(!mediaPlayer.isPlaying()){
                        mediaPlayer.start();
                        playerStatus = StatusPlay;
                    }

                } else {
                    if(mediaPlayer.isPlaying()){
                        mediaPlayer.pause();
                        playerStatus = StatusStop;
                    }
                }
                break;
            case CommandClose:
                manger.cancel(TYPE_Customer1);
                stopSelf();
                break;

        }
    }

    @Override
    public void onDestroy() {
        mediaPlayer.removeObservable(this);
        super.onDestroy();
    }

    @Override
    public void onProgressChange(int position) {

    }

    @Override
    public void onPlayCompletion(MediaPlayer mp) {
        playerStatus = StatusStop;
        mRemoteViews.setImageViewResource(R.id.btn1, R.drawable.ic_play_arrow_white);
        manger.notify(TYPE_Customer1, mNotification);
    }

    @Override
    public void onPlayChange(MediaPlayerStatus status) {
        if(MediaPlayerStatus.PLAYING.equals(status)){
            playerStatus = StatusPlay;
            mRemoteViews.setImageViewResource(R.id.btn1, R.drawable.ic_pause_white);
            manger.notify(TYPE_Customer1, mNotification);
        }else if(MediaPlayerStatus.PAUSE.equals(status)){
            playerStatus = StatusStop;
            mRemoteViews.setImageViewResource(R.id.btn1, R.drawable.ic_play_arrow_white);
            manger.notify(TYPE_Customer1, mNotification);
        }else if(MediaPlayerStatus.STOP.equals(status)){
           /* mediaPlayer.releaseMediaPlayer();
            playerStatus = StatusStop;*/
            manger.cancel(TYPE_Customer1);
            playerStatus = StatusStop;
            stopSelf();
        }
    }
}
