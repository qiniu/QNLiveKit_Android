package com.softsugar.library.sdk.utils;

import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @Description
 * @Author Lu Guoqiang
 * @Time 2019/7/17 15:52
 */
public class ReadAssetsUtils {

    public static String getJson(String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        AssetManager assetManager = ContextHolder.getContext().getAssets();
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                    assetManager.open(fileName), "utf-8"));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

}
