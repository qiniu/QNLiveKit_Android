package com.qlive.uiwidghtbeauty.utils;


import static com.qlive.uiwidghtbeauty.utils.Constants.*;

import android.content.Context;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

public class LoadResourcesTask extends AsyncTask<String, Void, Boolean> {

    public interface ILoadResourcesCallback {
        Context getContext();

        void onStartTask();

        void onEndTask(boolean result);
    }

    private ILoadResourcesCallback mCallback;

    public LoadResourcesTask(ILoadResourcesCallback callback) {
        mCallback = (callback);
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        Context context = mCallback.getContext();
        FileUtils.copyStickerFiles(context, NEW_ENGINE);
        FileUtils.copyStickerFiles(context, MAKEUP_EYE);
        FileUtils.copyStickerFiles(context, MAKEUP_BROW);
        FileUtils.copyStickerFiles(context, MAKEUP_BLUSH);
        FileUtils.copyStickerFiles(context, MAKEUP_HIGHLIGHT);
        FileUtils.copyStickerFiles(context, MAKEUP_LIP);
        FileUtils.copyStickerFiles(context, MAKEUP_EYELINER);
        FileUtils.copyStickerFiles(context, MAKEUP_EYELASH);
        FileUtils.copyStickerFiles(context, MAKEUP_EYEBALL);
        FileUtils.copyStickerFiles(context, MAKEUP_STYLE);
        FileUtils.copyFilterFiles(context, FILTER_PORTRAIT);
        FileUtils.copyFilterFiles(context, FILTER_SCENERY);
        FileUtils.copyFilterFiles(context, FILTER_STILL_LIFE);
        FileUtils.copyFilterFiles(context, FILTER_FOOD);
        FileUtils.copyFileIfNeed(context, LICENSE_FILE);
        return true;
    }

    @Override
    protected void onPreExecute() {
        mCallback.onStartTask();
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        mCallback.onEndTask(result);
    }
}
