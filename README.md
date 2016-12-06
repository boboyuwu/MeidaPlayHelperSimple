封装了一个音乐播放器的库,改良了一下觉得还是挺稳定的.


依赖方式1：

  allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	    }
 
 依赖方式2：
 
 dependencies {
	        compile 'com.github.boboyuwu:MeidaPlayHelperSimple:v1.0'
	}
 
 
使用方法在simple里面已经写得很清楚了.
MediaPlayerHelper mBuilder = MediaPlayerHelper.builder(this, Uri.parse(mp3));
mBuilder.setMediaStateChangeListener(onMediaStateChageListener);
设置回调跟新不同的UI界面
 //播放状态改变    分别是正在播放状态、暂停状态、停止状态(停止状态当我们destroy activity时候确定彻底不用这个helper了release释放的时候会回调这个状态)
        
        
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
 
如果需要添加通知栏功能请设置这一句,不设置默认没有这个功能
mBuilder.isMediaServiceOpen(true);


请在Activity的onDestroy(){  
    mBuilder.removeObservable(onMediaStateChageListener);    //加上你所设置的回调接口防止内存泄漏
    mBuilder.releaseMediaPlayer();            //添加这个释放所有播放资源
}

功能不停跟新中....
好用请star O(∩_∩)O~
