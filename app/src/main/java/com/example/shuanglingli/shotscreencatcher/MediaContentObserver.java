package com.example.shuanglingli.shotscreencatcher;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;

public class MediaContentObserver {

    // 内部存储器内容观察者
    private ContentObserver mInternalObserver;
    //外部存储器内容观察者
    private ContentObserver mExternalObserver;
    //多媒体数据观察事件监听
    private MediaContentChangeListener listener;
    //context 实例只能由构造时传入，保证每个实例只为一个 context 服务
    private Context context;

    public MediaContentObserver(Context context, Handler handler) {
        this.context = context;
        mInternalObserver = new ContentObserver(handler) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                if (listener != null) {
                    listener.onMediaContentChange(MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                }
            }
        };

        mExternalObserver = new ContentObserver(handler) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                if (listener != null) {
                    listener.onMediaContentChange(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                }
            }
        };

    }

    public void setChangeListener(MediaContentChangeListener listener) {
        this.listener = listener;
        if (context != null) {
            context.getContentResolver().registerContentObserver(
                    MediaStore.Images.Media.INTERNAL_CONTENT_URI,
                    false,
                    mInternalObserver);

            context.getContentResolver().registerContentObserver(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    false,
                    mExternalObserver);
        }
    }


    public void removeChangeListener() {
        this.listener = null;
        if (context != null) {
            context.getContentResolver().unregisterContentObserver(mInternalObserver);
            context.getContentResolver().unregisterContentObserver(mExternalObserver);
        }
        context = null;
    }


    interface MediaContentChangeListener {
        void onMediaContentChange(Uri uri);
    }
}
