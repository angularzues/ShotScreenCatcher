package com.example.shuanglingli.shotscreencatcher;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;

public class ScreenshotObserver {

    private Context context;
    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private MediaContentObserver mediaContentObserver;
    private ScreenshotListener listener;
    private static final String EXTERNAL_CONTENT_URI_MATCHER =
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString();
    private long preScreenShotTime;

    /**
     * 读取媒体数据库时需要读取的列
     */
    private static final String[] MEDIA_PROJECTIONS = {
            MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_ADDED
    };

    private static final String[] KEYWORDS = {
            "screenshot", "screen_shot", "screen-shot", "screen shot",
            "screencapture", "screen_capture", "screen-capture", "screen capture",
            "screencap", "screen_cap", "screen-cap", "screen cap"
    };


    public ScreenshotObserver(Context context) {
        this.context = context;
        mHandlerThread = new HandlerThread("Screenshot_Manager");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
    }


    public void setScreenshotListener(ScreenshotListener listener) {
        this.listener = listener;
        mediaContentObserver = new MediaContentObserver(context, mHandler);
        mediaContentObserver.setChangeListener(new MediaContentObserver.MediaContentChangeListener() {
            @Override
            public void onMediaContentChange(Uri uri) {
                handleMediaContentChange(uri);
            }
        });
    }

    public void removeScreenshotListener() {
        if (mediaContentObserver != null) {
            this.mediaContentObserver.removeChangeListener();
            this.mediaContentObserver = null;
        }

        try {
            if (mHandlerThread != null && mHandlerThread.isAlive()) {
                mHandlerThread.quit();
            }
            this.mHandlerThread = null;
        } catch (Throwable e) {

        }

        this.context = null;
        this.mHandler = null;
        this.listener = null;
    }

    private void handleMediaContentChange(Uri contentUri) {
        Cursor cursor = null;
        try {

            //查询更新的数据
            if (contentUri.toString().startsWith(EXTERNAL_CONTENT_URI_MATCHER)) {
                cursor = context.getContentResolver().query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        MEDIA_PROJECTIONS,
                        null,
                        null,
                        MediaStore.Images.ImageColumns.DATE_ADDED + " desc limit 1"
                );
            } else {
                // 数据改变时查询数据库中最后加入的一条数据
                cursor = context.getContentResolver().query(
                        MediaStore.Images.Media.INTERNAL_CONTENT_URI,
                        MEDIA_PROJECTIONS,
                        null,
                        null,
                        MediaStore.Images.ImageColumns.DATE_ADDED + " desc limit 1"
                );
            }

            if (cursor == null) {
                return;
            }

            if (!cursor.moveToFirst()) {
                cursor.close();
                return;
            }

            // 获取更新数据索引和数据更新时间索引
            int dataIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            int dateTakenIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_ADDED);

            // 获取行数据
            String data = cursor.getString(dataIndex);
            long dateTaken = cursor.getLong(dateTakenIndex);

            // 检测是否是截屏事件
            if (checkIsScreenShot(data, dateTaken)) {
                if (listener != null) {
                    listener.onScreenshot(contentUri);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }


    /**
     * 判断是否是截屏
     */
    private boolean checkIsScreenShot(String data, long dateTaken) {
        if (System.currentTimeMillis() / 1000 - dateTaken > 2 || dateTaken - preScreenShotTime < 1) {
            return false;
        }
        preScreenShotTime = dateTaken;
        data = data.toLowerCase();
        // 判断图片路径是否含有指定的关键字之一, 如果有, 则认为当前截屏了
        for (String keyWork : KEYWORDS) {
            if (data.contains(keyWork)) {
                return true;
            }
        }
        return false;
    }


    public interface ScreenshotListener {
        void onScreenshot(Uri contentUri);
    }


}
