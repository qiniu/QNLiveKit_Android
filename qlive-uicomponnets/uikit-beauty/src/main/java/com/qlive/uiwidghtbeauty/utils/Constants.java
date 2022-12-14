package com.qlive.uiwidghtbeauty.utils;

import com.sensetime.stmobile.params.STEffectBeautyType;

public class Constants {

    public final static String GROUP_2D = "46";
    public final static String GROUP_3D = "48";
    public final static String GROUP_HAND = "47";
    public final static String GROUP_BG = "49";

    public final static String GROUP_LIP = "50";
    public final static String GROUP_EYEBALL = "51";
    public final static String GROUP_BLUSH = "52";
    public final static String GROUP_BROW = "53";
    public final static String GROUP_HIGHLIGHT = "54";
    public final static String GROUP_EYE = "55";
    public final static String GROUP_EYELINER = "56";
    public final static String GROUP_EYELASH = "57";
    public final static String GROUP_STYLE = "58";

    public static final String BASE_BEAUTY = "baseBeauty";
    public static final String PROFESSIONAL_BEAUTY = "professionalBeauty";
    public static final String MICRO_BEAUTY = "microBeauty";
    public static final String ADJUST_BEAUTY = "adjustBeauty";

    public static final String MAKEUP_BLUSH = "makeup_blush";
    public static final String MAKEUP_BROW = "makeup_brow";
    public static final String MAKEUP_EYE = "makeup_eye";
    public static final String MAKEUP_LIP = "makeup_lip";
    public static final String MAKEUP_HIGHLIGHT = "makeup_highlight";
    public static final String MAKEUP_EYELINER = "makeup_eyeliner";
    public static final String MAKEUP_EYELASH = "makeup_eyelash";
    public static final String MAKEUP_EYEBALL = "makeup_eyeball";
    public static final String MAKEUP_STYLE = "makeup_style";

    public static final String FILTER_PORTRAIT = "filter_portrait";
    public static final String FILTER_SCENERY = "filter_scenery";
    public static final String FILTER_STILL_LIFE = "filter_still_life";
    public static final String FILTER_FOOD = "filter_food";

    public static final String NEW_ENGINE = "newEngine";
    public static final String STICKER_NEW_ENGINE = "sticker_new_engine";

    public static final String ORIGINAL = "original";
    public static final String LICENSE_FILE = "SenseME.lic";

    public static final int[] BEAUTY_TYPES = {
            // ????????????
            STEffectBeautyType.EFFECT_BEAUTY_BASE_WHITTEN, // ??????
            STEffectBeautyType.EFFECT_BEAUTY_BASE_REDDEN, // ??????
            STEffectBeautyType.EFFECT_BEAUTY_BASE_FACE_SMOOTH, // ??????
            // ??????
            STEffectBeautyType.EFFECT_BEAUTY_RESHAPE_SHRINK_FACE, // ??????
            STEffectBeautyType.EFFECT_BEAUTY_RESHAPE_ENLARGE_EYE, // ??????
            STEffectBeautyType.EFFECT_BEAUTY_RESHAPE_SHRINK_JAW, // ??????
            STEffectBeautyType.EFFECT_BEAUTY_RESHAPE_NARROW_FACE, // ??????
            STEffectBeautyType.EFFECT_BEAUTY_RESHAPE_ROUND_EYE, // ??????
            // ?????????
            STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_THIN_FACE, // ?????????
            STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_CHIN_LENGTH, // ??????
            STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_HAIRLINE_HEIGHT, // ??????
            STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_APPLE_MUSLE, // ?????????
            STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_NARROW_NOSE, // ?????????
            STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_NOSE_LENGTH, // ??????
            STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_PROFILE_RHINOPLASTY, // ????????????
            STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_MOUTH_SIZE, // ??????
            STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_PHILTRUM_LENGTH, // ?????????
            STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_EYE_DISTANCE, // ??????
            STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_EYE_ANGLE, // ????????????
            STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_OPEN_CANTHUS, // ?????????
            STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_BRIGHT_EYE, // ??????
            STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_REMOVE_DARK_CIRCLES, // ????????????
            STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_REMOVE_NASOLABIAL_FOLDS, // ????????????
            STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_WHITE_TEETH, // ??????
            STEffectBeautyType.EFFECT_BEAUTY_PLASTIC_SHRINK_CHEEKBONE, // ?????????
            // ??????
            STEffectBeautyType.EFFECT_BEAUTY_TONE_CONTRAST, // ?????????
            STEffectBeautyType.EFFECT_BEAUTY_TONE_SATURATION, // ?????????
    };

    // ????????????
    public static final int BEAUTY_BASE_WHITTEN = 0;
    public static final int BEAUTY_BASE_REDDEN = 1;
    public static final int BEAUTY_BASE_FACE_SMOOTH = 2;
    // ??????
    public static final int BEAUTY_RESHAPE_SHRINK_FACE = 3;
    public static final int BEAUTY_RESHAPE_ENLARGE_EYE = 4;
    public static final int BEAUTY_RESHAPE_SHRINK_JAW = 5;
    public static final int BEAUTY_RESHAPE_NARROW_FACE = 6;
    public static final int BEAUTY_RESHAPE_ROUND_EYE = 7;
    // ?????????
    public static final int BEAUTY_PLASTIC_THIN_FACE = 8;
    public static final int BEAUTY_PLASTIC_CHIN_LENGTH = 9;
    public static final int BEAUTY_PLASTIC_HAIRLINE_HEIGHT = 10;
    public static final int BEAUTY_PLASTIC_APPLE_MUSLE = 11;
    public static final int BEAUTY_PLASTIC_NARROW_NOSE = 12;
    public static final int BEAUTY_PLASTIC_NOSE_LENGTH = 13;
    public static final int BEAUTY_PLASTIC_PROFILE_RHINOPLASTY = 14;
    public static final int BEAUTY_PLASTIC_MOUTH_SIZE = 15;
    public final static int BEAUTY_PLASTIC_PHILTRUM_LENGTH = 16;
    public final static int BEAUTY_PLASTIC_EYE_DISTANCE = 17;
    public final static int BEAUTY_PLASTIC_EYE_ANGLE = 18;
    public final static int BEAUTY_PLASTIC_OPEN_CANTHUS = 19;
    public final static int BEAUTY_PLASTIC_BRIGHT_EYE = 20;
    public final static int BEAUTY_PLASTIC_REMOVE_DARK_CIRCLES = 21;
    public final static int BEAUTY_PLASTIC_REMOVE_NASOLABIAL_FOLDS = 22;
    public final static int BEAUTY_PLASTIC_WHITE_TEETH = 23;
    public final static int BEAUTY_PLASTIC_SHRINK_CHEEKBONE = 24;
    // ??????
    public final static int BEAUTY_TONE_CONTRAST = 25;
    public final static int BEAUTY_TONE_SATURATION = 26;

    public static final int ST_MAKEUP_LIP = STEffectBeautyType.EFFECT_BEAUTY_MAKEUP_LIP;
    public static final int ST_MAKEUP_HIGHLIGHT = STEffectBeautyType.EFFECT_BEAUTY_MAKEUP_NOSE;
    public static final int ST_MAKEUP_BLUSH = STEffectBeautyType.EFFECT_BEAUTY_MAKEUP_CHEEK;
    public static final int ST_MAKEUP_BROW = STEffectBeautyType.EFFECT_BEAUTY_MAKEUP_EYE_BROW;
    public static final int ST_MAKEUP_EYE = STEffectBeautyType.EFFECT_BEAUTY_MAKEUP_EYE_SHADOW;
    public static final int ST_MAKEUP_EYELINER = STEffectBeautyType.EFFECT_BEAUTY_MAKEUP_EYE_LINE;
    public static final int ST_MAKEUP_EYELASH = STEffectBeautyType.EFFECT_BEAUTY_MAKEUP_EYE_LASH;
    public static final int ST_MAKEUP_EYEBALL = STEffectBeautyType.EFFECT_BEAUTY_MAKEUP_EYE_BALL;
    public static final int ST_MAKEUP_STYLE = STEffectBeautyType.EFFECT_BEAUTY_MAKEUP_ALL;

    public static final int MAKEUP_TYPE_COUNT = 9;
}
