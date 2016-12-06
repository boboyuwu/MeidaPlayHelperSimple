package com.boboyu.myossimple.activitys;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.boboyu.mediaplayerlibrary.helper.MediaPlayerHelper;
import com.boboyu.mediaplayerlibrary.helper.MediaPlayerStatus;
import com.boboyu.mediaplayerlibrary.interfaces.OnMediaStateChageListener;
import com.boboyu.myossimple.R;


/**
 * Created by wubo on 2016/12/5.
 */

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";
    private MediaPlayerHelper mBuilder;
    private ProgressBar mProgress;
    private ImageButton mIb_play;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //这里的内容可以自己布局定制  这里介绍基本使用方法
        mIb_play = (ImageButton) findViewById(R.id.img_play);
        TextView title = (TextView) findViewById(R.id.title);
        TextView from = (TextView) findViewById(R.id.from);
        mProgress = (ProgressBar) findViewById(R.id.progress);
        String mp3 = "http://7xqhmn.media1.z0.glb.clouddn.com/femorning-20161206.mp3";
        mBuilder = MediaPlayerHelper.builder(this, Uri.parse(mp3));
        mBuilder.setMediaStateChangeListener(onMediaStateChageListener);
        //如果需要启动通知栏请设置这一句
        mBuilder.isMediaServiceOpen(true);
        mProgress.setMax(mBuilder.getDuration());
        title.setText("测试音频");
        from.setText("来自xx的测试音频");


        mIb_play.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBuilder != null) {
                    mBuilder.startOrPause();
                }else{
                    mIb_play.setImageResource(R.drawable.play);
                }
            }
        });
    }

    OnMediaStateChageListener onMediaStateChageListener = new OnMediaStateChageListener() {
        //进度条改变的值
        @Override
        public void onProgressChange(int position) {
            mProgress.setProgress(position);
        }

        //播放完成状态
        @Override
        public void onPlayCompletion(MediaPlayer mp) {
            mIb_play.setImageResource(R.drawable.ic_play_arrow_white);
            mProgress.setProgress(0);
        }

        //播放状态改变
        @Override
        public void onPlayChange(MediaPlayerStatus status) {
            switch (status.getStatu() /** 或者直接status也行 */) {
                case PLAYING:
                    //这里跟新按q钮
                    mIb_play.setImageResource(R.drawable.pause);
                    Toast.makeText(MainActivity.this, "PLAYING", Toast.LENGTH_SHORT).show();
                    break;

                case PAUSE:
                    mIb_play.setImageResource(R.drawable.play);
                    Toast.makeText(MainActivity.this, "PAUSE", Toast.LENGTH_SHORT).show();
                    break;

                case STOP:
                    mIb_play.setImageResource(R.drawable.play);
                    Toast.makeText(MainActivity.this, "STOP", Toast.LENGTH_SHORT).show();
                    break;

            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBuilder.removeObservable(onMediaStateChageListener);
        mBuilder.releaseMediaPlayer();
    }
}
