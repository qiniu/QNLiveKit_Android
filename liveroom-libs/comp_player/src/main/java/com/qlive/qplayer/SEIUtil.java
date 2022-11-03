package com.qlive.qplayer;

import com.pili.pldroid.player.PLOnVideoFrameListener;

import java.util.Arrays;

class SEIUtil {

    public static String parseSEI(byte[] data, int size, int width, int height, int format) {
        String content = "";
        if (format == PLOnVideoFrameListener.VIDEO_FORMAT_SEI && size > 0) {
            // If the RTMP stream is from Qiniu
            // Add &addtssei=true to the end of URL to enable SEI timestamp.
            // Format of the byte array:
            // 0:       SEI TYPE                    This is part of h.264 standard.
            // 1:       unregistered user data      This is part of h.264 standard.
            // 2:       payload length              This is part of h.264 standard.
            // 3-18:    uuid                        This is part of h.264 standard.
            // 19-22:   ts64                        Magic string to mark this stream is from Qiniu
            // 23-30:   timestamp                   The timestamp
            // 31:      0x80                        Magic hex in ffmpeg
            int index = 2;
            int payloadSize = 0;
            do {
                payloadSize += data[index] & 0xFF;
            } while (data[index++] == (byte) 0xFF);
            String uuid = bytesToHex(Arrays.copyOfRange(data, index, index + 16));
            int length = payloadSize - 16;
            int start_index = index + 16;
            if (start_index >= 0 && length > 0 && (start_index + length) <= data.length) {
                content = new String(data, index + 16, length);
            }
        }
        return content;
    }

    private static String bytesToHex(byte[] bytes) {
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

}
