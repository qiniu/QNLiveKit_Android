package com.softsugar.library.sdk.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by sensetime on 16-6-20.
 */
public class SignUtils {

    private static String SIGN = "sign";

    private static String UTF_8 = "UTF-8";
    public static String prepareParams(Map<String,Object> params, boolean needEncode) throws UnsupportedEncodingException
    {
        StringBuilder sb = new StringBuilder();
        if(params!=null && !params.isEmpty()) {
            Iterator<Map.Entry<String, Object>> iterator = params.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<String, Object> next = iterator.next();
                String key = next.getKey();
                String value = next.getValue() == null ? "" : next.getValue()
                        .toString();


                if (needEncode) {
                    value = URLEncoder.encode(value, UTF_8);
                }

                if (!key.equals(SIGN) ) {
                        sb.append(key).append('=')
                                .append(value);

                        // 拼接时，不包括最后一个&字符
                        if (iterator.hasNext()) {
                            sb.append('&');
                        }
                }
            }
        }
        return sb.toString();
    }

    public static String formatString(Map<String,Object> params)
    {
        StringBuilder sb = new StringBuilder();
        if(params!=null && !params.isEmpty()){
            for(Map.Entry<String, Object> entry : params.entrySet()){
                try {
                    sb.append(entry.getKey()).append('=')
                            .append(URLEncoder.encode((String)entry.getValue(), UTF_8)).append('&');
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            sb.deleteCharAt(sb.length()-1);
        }
        return sb.toString();
    }

    public static String getSignString(Map<String,Object> params, String signKey, boolean needEncode) throws UnsupportedEncodingException{
        String data = prepareParams(params,needEncode);
        String src = data + signKey;
        return encryptToSHA(src);
    }

    public static boolean check(String sign, String data, String signKey) throws UnsupportedEncodingException {
        if(sign == null || sign.isEmpty() || sign.equals("")) {
            return true;
        }

        String encryptStr = data + signKey;
        String signResult = SignUtils.encryptToSHA(encryptStr);

        if (signResult.equals(sign)) {
            return true;
        }

        return false;
    }

    public static String byte2hex(byte[] b) {
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = (Integer.toHexString(b[n] & 0XFF));
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
        }
        return hs;
    }

    //SHA1 加密实例
    public static String encryptToSHA(String info) {
        byte[] digesta = null;
        try {
            MessageDigest alga = MessageDigest.getInstance("SHA-1");

            alga.update(info.getBytes(UTF_8));
            digesta = alga.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String rs = byte2hex(digesta);
        return rs;
    }

    public static Map<String, Object> sortMapByKey(Map<String, Object> map){
        Map<String, Object> sortMap = new TreeMap<String, Object>();
        Set<String> keySet = map.keySet();
        Iterator<String> iter = keySet.iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            sortMap.put(key,map.get(key));
        }
        return sortMap;
    }
}

