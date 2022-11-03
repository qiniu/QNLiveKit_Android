package com.qlive.uikitktv;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class Utils {

    public static void doCopy(Context context, String assetsPath, String desPath) throws IOException {
        String[] srcFiles = context.getAssets().list(assetsPath);//for directory
        for (String srcFileName : srcFiles) {
            String outFileName = desPath + File.separator + srcFileName;
            if (new File(outFileName).exists()) {
                continue;
            }
            String inFileName = assetsPath + File.separator + srcFileName;
            if (assetsPath.equals("")) {// for first time
                inFileName = srcFileName;
            }
            Log.e("tag", "========= assets: " + assetsPath + "  filename: " + srcFileName + " infile: " + inFileName + " outFile: " + outFileName);
            try {
                InputStream inputStream = context.getAssets().open(inFileName);
                copyAndClose(inputStream, new FileOutputStream(outFileName));
            } catch (IOException e) {//if directory fails exception
                e.printStackTrace();
                new File(outFileName).mkdir();
                doCopy(context, inFileName, outFileName);
            }
        }
    }

    private static void closeQuietly(OutputStream out) {
        try {
            if (out != null) out.close();
            ;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void closeQuietly(InputStream is) {
        try {
            if (is != null) {
                is.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void copyAndClose(InputStream is, OutputStream out) throws IOException {
        copy(is, out);
        closeQuietly(is);
        closeQuietly(out);
    }

    private static void copy(InputStream is, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int n = 0;
        while (-1 != (n = is.read(buffer))) {
            out.write(buffer, 0, n);
        }
    }
}


