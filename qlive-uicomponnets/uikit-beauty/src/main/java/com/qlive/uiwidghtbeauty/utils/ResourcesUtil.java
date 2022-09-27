package com.qlive.uiwidghtbeauty.utils;

import android.content.Context;
import android.graphics.BitmapFactory;


import com.qlive.uiwidghtbeauty.R;
import com.qlive.uiwidghtbeauty.model.BeautyItem;
import com.qlive.uiwidghtbeauty.model.FilterItem;
import com.qlive.uiwidghtbeauty.model.MakeupItem;

import java.util.ArrayList;
import java.util.HashMap;

public class ResourcesUtil {

    public static float[] sBeautifyParams = {
            // 美颜
            0.36f, 0.74f, 0.5f,
            // 美型
            0.5f, 0.5f, 0.5f, 0f, 0f,
            // 微整形
            0.5f, 0.5f, 0.5f, 0.5f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f,
            // 调整
            0f, 0f};

    public static String getMakeupNameOfType(int type) {
        String name = Constants.MAKEUP_EYELASH;
        if (type == Constants.ST_MAKEUP_BROW) {
            name = Constants.MAKEUP_BROW;
        } else if (type == Constants.ST_MAKEUP_EYE) {
            name = Constants.MAKEUP_EYE;
        } else if (type == Constants.ST_MAKEUP_BLUSH) {
            name = Constants.MAKEUP_BLUSH;
        } else if (type == Constants.ST_MAKEUP_LIP) {
            name = Constants.MAKEUP_LIP;
        } else if (type == Constants.ST_MAKEUP_HIGHLIGHT) {
            name = Constants.MAKEUP_HIGHLIGHT;
        } else if (type == Constants.ST_MAKEUP_EYELINER) {
            name = Constants.MAKEUP_EYELINER;
        } else if (type == Constants.ST_MAKEUP_EYELASH) {
            name = Constants.MAKEUP_EYELASH;
        } else if (type == Constants.ST_MAKEUP_EYEBALL) {
            name = Constants.MAKEUP_EYEBALL;
        } else if (type == Constants.ST_MAKEUP_STYLE) {
            name = Constants.MAKEUP_STYLE;
        }
        return name;
    }

    public static int calculateBeautyIndex(int beautyOptionPosition, int selectPosition) {
        switch (beautyOptionPosition) {
            case 0:
                switch (selectPosition) {
                    case 0:
                        return Constants.BEAUTY_BASE_WHITTEN;
                    case 1:
                        return Constants.BEAUTY_BASE_REDDEN;
                    case 2:
                        return Constants.BEAUTY_BASE_FACE_SMOOTH;
                }
                break;
            case 1:
                switch (selectPosition) {
                    case 0:
                        return Constants.BEAUTY_RESHAPE_SHRINK_FACE;
                    case 1:
                        return Constants.BEAUTY_RESHAPE_ENLARGE_EYE;
                    case 2:
                        return Constants.BEAUTY_RESHAPE_SHRINK_JAW;
                    case 3:
                        return Constants.BEAUTY_RESHAPE_NARROW_FACE;
                    case 4:
                        return Constants.BEAUTY_RESHAPE_ROUND_EYE;
                }
                break;
            case 2:
                switch (selectPosition) {
                    case 0:
                        return Constants.BEAUTY_PLASTIC_THIN_FACE;
                    case 1:
                        return Constants.BEAUTY_PLASTIC_CHIN_LENGTH;
                    case 2:
                        return Constants.BEAUTY_PLASTIC_HAIRLINE_HEIGHT;
                    case 3:
                        return Constants.BEAUTY_PLASTIC_APPLE_MUSLE;
                    case 4:
                        return Constants.BEAUTY_PLASTIC_NARROW_NOSE;
                    case 5:
                        return Constants.BEAUTY_PLASTIC_NOSE_LENGTH;
                    case 6:
                        return Constants.BEAUTY_PLASTIC_PROFILE_RHINOPLASTY;
                    case 7:
                        return Constants.BEAUTY_PLASTIC_MOUTH_SIZE;
                    case 8:
                        return Constants.BEAUTY_PLASTIC_PHILTRUM_LENGTH;
                    case 9:
                        return Constants.BEAUTY_PLASTIC_EYE_DISTANCE;
                    case 10:
                        return Constants.BEAUTY_PLASTIC_EYE_ANGLE;
                    case 11:
                        return Constants.BEAUTY_PLASTIC_OPEN_CANTHUS;
                    case 12:
                        return Constants.BEAUTY_PLASTIC_BRIGHT_EYE;
                    case 13:
                        return Constants.BEAUTY_PLASTIC_REMOVE_DARK_CIRCLES;
                    case 14:
                        return Constants.BEAUTY_PLASTIC_REMOVE_NASOLABIAL_FOLDS;
                    case 15:
                        return Constants.BEAUTY_PLASTIC_WHITE_TEETH;
                    case 16:
                        return Constants.BEAUTY_PLASTIC_SHRINK_CHEEKBONE;
                }
                break;
            case 5:
                switch (selectPosition) {
                    case 0:
                        return Constants.BEAUTY_TONE_CONTRAST;
                    case 1:
                        return Constants.BEAUTY_TONE_SATURATION;
                }
                break;
        }
        return -1;
    }

    public static ArrayList<BeautyItem> getBeautyBaseItemList(Context context) {
        ArrayList<BeautyItem> beautyBaseItemList = new ArrayList<>();
        beautyBaseItemList.add(new BeautyItem("美白",FileUtils.getResourcesUri(context, R.drawable.beauty_whiten_unselected),FileUtils.getResourcesUri(context, R.drawable.beauty_whiten_selected)));
        beautyBaseItemList.add(new BeautyItem("红润", FileUtils.getResourcesUri(context, R.drawable.beauty_redden_unselected),FileUtils.getResourcesUri(context, R.drawable.beauty_redden_selected)));
        beautyBaseItemList.add(new BeautyItem("磨皮",FileUtils.getResourcesUri(context, R.drawable.beauty_smooth_unselected),FileUtils.getResourcesUri(context, R.drawable.beauty_smooth_selected)));
        beautyBaseItemList.get(0).setProgress((int) (sBeautifyParams[0] * 100));
        beautyBaseItemList.get(1).setProgress((int) (sBeautifyParams[1] * 100));
        beautyBaseItemList.get(2).setProgress((int) (sBeautifyParams[2] * 100));
        return beautyBaseItemList;
    }

    public static ArrayList<BeautyItem> getAdjustBeautyItemList(Context context) {
        ArrayList<BeautyItem> adjustBeautyItemList = new ArrayList<>();
        adjustBeautyItemList.add(new BeautyItem("对比度",FileUtils.getResourcesUri(context, R.drawable.beauty_contrast_unselected),FileUtils.getResourcesUri(context, R.drawable.beauty_contrast_selected)));
        adjustBeautyItemList.add(new BeautyItem("饱和度",FileUtils.getResourcesUri(context, R.drawable.beauty_saturation_unselected),FileUtils.getResourcesUri(context, R.drawable.beauty_saturation_selected)));
        adjustBeautyItemList.get(0).setProgress((int) (sBeautifyParams[25] * 100));
        adjustBeautyItemList.get(1).setProgress((int) (sBeautifyParams[26] * 100));
        return adjustBeautyItemList;
    }

    public static HashMap<String, ArrayList<FilterItem>> getFilterListMap(Context context) {
        HashMap<String, ArrayList<FilterItem>> filterItemListMap = new HashMap<>();
        filterItemListMap.put(Constants.FILTER_PORTRAIT, FileUtils.getFilterFiles(context, Constants.FILTER_PORTRAIT));
        filterItemListMap.put(Constants.FILTER_SCENERY, FileUtils.getFilterFiles(context, Constants.FILTER_SCENERY));
        filterItemListMap.put(Constants.FILTER_STILL_LIFE, FileUtils.getFilterFiles(context, Constants.FILTER_STILL_LIFE));
        filterItemListMap.put(Constants.FILTER_FOOD, FileUtils.getFilterFiles(context, Constants.FILTER_FOOD));
        return filterItemListMap;
    }

    public static HashMap<String, ArrayList<MakeupItem>> getMakeupListMap(Context context) {
        HashMap<String, ArrayList<MakeupItem>> makeupLists = new HashMap<>();
        makeupLists.put(Constants.MAKEUP_LIP, FileUtils.getMakeupFiles(context, Constants.MAKEUP_LIP));
        makeupLists.put(Constants.MAKEUP_HIGHLIGHT, FileUtils.getMakeupFiles(context, Constants.MAKEUP_HIGHLIGHT));
        makeupLists.put(Constants.MAKEUP_BLUSH, FileUtils.getMakeupFiles(context, Constants.MAKEUP_BLUSH));
        makeupLists.put(Constants.MAKEUP_BROW, FileUtils.getMakeupFiles(context, Constants.MAKEUP_BROW));
        makeupLists.put(Constants.MAKEUP_EYE, FileUtils.getMakeupFiles(context, Constants.MAKEUP_EYE));
        makeupLists.put(Constants.MAKEUP_EYELINER, FileUtils.getMakeupFiles(context, Constants.MAKEUP_EYELINER));
        makeupLists.put(Constants.MAKEUP_EYELASH, FileUtils.getMakeupFiles(context, Constants.MAKEUP_EYELASH));
        makeupLists.put(Constants.MAKEUP_EYEBALL, FileUtils.getMakeupFiles(context, Constants.MAKEUP_EYEBALL));
        makeupLists.put(Constants.MAKEUP_STYLE, FileUtils.getMakeupFiles(context, Constants.MAKEUP_STYLE));
        return makeupLists;
    }

    public static HashMap<String, Integer> getMakeupOptionIndexMap() {
        HashMap<String, Integer> makeupOptionIndex = new HashMap<>();
        makeupOptionIndex.put(Constants.MAKEUP_EYE, 1);
        makeupOptionIndex.put(Constants.MAKEUP_BLUSH, 2);
        makeupOptionIndex.put(Constants.MAKEUP_LIP, 3);
        makeupOptionIndex.put(Constants.MAKEUP_HIGHLIGHT, 4);
        makeupOptionIndex.put(Constants.MAKEUP_BROW, 5);
        makeupOptionIndex.put(Constants.MAKEUP_EYELINER, 6);
        makeupOptionIndex.put(Constants.MAKEUP_EYELASH, 7);
        makeupOptionIndex.put(Constants.MAKEUP_EYEBALL, 8);
        makeupOptionIndex.put(Constants.MAKEUP_STYLE, 9);
        return makeupOptionIndex;
    }

    public static ArrayList<BeautyItem> getMicroBeautyItemList(Context context) {
        ArrayList<BeautyItem> microBeautyItem = new ArrayList<>();
        microBeautyItem.add(new BeautyItem("瘦脸型",FileUtils.getResourcesUri(context, R.drawable.beauty_thin_face_unselected),FileUtils.getResourcesUri(context, R.drawable.beauty_thin_face_selected)));
        microBeautyItem.add(new BeautyItem("下巴",FileUtils.getResourcesUri(context, R.drawable.beauty_chin_unselected),FileUtils.getResourcesUri(context, R.drawable.beauty_chin_selected)));
        microBeautyItem.add(new BeautyItem("额头",FileUtils.getResourcesUri(context, R.drawable.beauty_forehead_unselected),FileUtils.getResourcesUri(context, R.drawable.beauty_forehead_selected)));
        microBeautyItem.add(new BeautyItem("苹果肌",FileUtils.getResourcesUri(context, R.drawable.beauty_apple_musle_unselected),FileUtils.getResourcesUri(context, R.drawable.beauty_apple_musle_selected)));
        microBeautyItem.add(new BeautyItem("瘦鼻翼",FileUtils.getResourcesUri(context, R.drawable.beauty_thin_nose_unselected),FileUtils.getResourcesUri(context, R.drawable.beauty_thin_nose_selected)));
        microBeautyItem.add(new BeautyItem("长鼻",FileUtils.getResourcesUri(context, R.drawable.beauty_long_nose_unselected),FileUtils.getResourcesUri(context, R.drawable.beauty_long_nose_selected)));
        microBeautyItem.add(new BeautyItem("侧脸隆鼻",FileUtils.getResourcesUri(context, R.drawable.beauty_profile_rhinoplasty_unselected),FileUtils.getResourcesUri(context, R.drawable.beauty_profile_rhinoplasty_selected)));
        microBeautyItem.add(new BeautyItem("嘴型",FileUtils.getResourcesUri(context, R.drawable.beauty_mouth_type_unselected),FileUtils.getResourcesUri(context, R.drawable.beauty_mouth_type_selected)));
        microBeautyItem.add(new BeautyItem("缩人中",FileUtils.getResourcesUri(context, R.drawable.beauty_philtrum_unselected),FileUtils.getResourcesUri(context, R.drawable.beauty_philtrum_selected)));
        microBeautyItem.add(new BeautyItem("眼距",FileUtils.getResourcesUri(context, R.drawable.beauty_eye_distance_unselected),FileUtils.getResourcesUri(context, R.drawable.beauty_eye_distance_selected)));
        microBeautyItem.add(new BeautyItem("眼睛角度",FileUtils.getResourcesUri(context, R.drawable.beauty_eye_angle_unselected),FileUtils.getResourcesUri(context, R.drawable.beauty_eye_angle_selected)));
        microBeautyItem.add(new BeautyItem("开眼角",FileUtils.getResourcesUri(context, R.drawable.beauty_open_canthus_unselected),FileUtils.getResourcesUri(context, R.drawable.beauty_open_canthus_selected)));
        microBeautyItem.add(new BeautyItem("亮眼",FileUtils.getResourcesUri(context, R.drawable.beauty_bright_eye_unselected),FileUtils.getResourcesUri(context, R.drawable.beauty_bright_eye_selected)));
        microBeautyItem.add(new BeautyItem("祛黑眼圈",FileUtils.getResourcesUri(context, R.drawable.beauty_remove_dark_circles_unselected),FileUtils.getResourcesUri(context, R.drawable.beauty_remove_dark_circles_selected)));
        microBeautyItem.add(new BeautyItem("祛法令纹",FileUtils.getResourcesUri(context, R.drawable.beauty_remove_nasolabial_folds_unselected),FileUtils.getResourcesUri(context, R.drawable.beauty_remove_nasolabial_folds_selected)));
        microBeautyItem.add(new BeautyItem("白牙",FileUtils.getResourcesUri(context, R.drawable.beauty_white_teeth_unselected),FileUtils.getResourcesUri(context, R.drawable.beauty_white_teeth_selected)));
        microBeautyItem.add(new BeautyItem("瘦颧骨",FileUtils.getResourcesUri(context, R.drawable.beauty_thin_cheekbone_unselected),FileUtils.getResourcesUri(context, R.drawable.beauty_thin_cheekbone_selected)));
        for (int i = 0; i < 16; i++) {
            microBeautyItem.get(i).setProgress((int) (sBeautifyParams[i + 8] * 100));
        }
        return microBeautyItem;
    }

    public static ArrayList<BeautyItem> getProfessionalBeautyItemList(Context context) {
        ArrayList<BeautyItem> professionalBeautyItem = new ArrayList<>();
        professionalBeautyItem.add(new BeautyItem("瘦脸",FileUtils.getResourcesUri(context, R.drawable.beauty_shrink_face_unselected),FileUtils.getResourcesUri(context, R.drawable.beauty_shrink_face_selected)));
        professionalBeautyItem.add(new BeautyItem("大眼",FileUtils.getResourcesUri(context, R.drawable.beauty_enlargeeye_unselected),FileUtils.getResourcesUri(context, R.drawable.beauty_enlargeeye_selected)));
        professionalBeautyItem.add(new BeautyItem("小脸",FileUtils.getResourcesUri(context, R.drawable.beauty_small_face_unselected),FileUtils.getResourcesUri(context, R.drawable.beauty_small_face_selected)));
        professionalBeautyItem.add(new BeautyItem("窄脸",FileUtils.getResourcesUri(context, R.drawable.beauty_narrow_face_unselected),FileUtils.getResourcesUri(context, R.drawable.beauty_narrow_face_selected)));
        professionalBeautyItem.add(new BeautyItem("圆眼",FileUtils.getResourcesUri(context, R.drawable.beauty_round_eye_unselected),FileUtils.getResourcesUri(context, R.drawable.beauty_round_eye_selected)));
        professionalBeautyItem.get(0).setProgress((int) (sBeautifyParams[3] * 100));
        professionalBeautyItem.get(1).setProgress((int) (sBeautifyParams[4] * 100));
        professionalBeautyItem.get(2).setProgress((int) (sBeautifyParams[5] * 100));
        professionalBeautyItem.get(3).setProgress((int) (sBeautifyParams[6] * 100));
        professionalBeautyItem.get(4).setProgress((int) (sBeautifyParams[7] * 100));
        return professionalBeautyItem;
    }
}
