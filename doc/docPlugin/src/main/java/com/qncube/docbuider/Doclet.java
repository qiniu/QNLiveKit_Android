package com.qncube.docbuider;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.javadoc.*;

public class Doclet {
    private static final HashMap<String, String> links = new LinkedHashMap<>();

    private static void initLink() {
        links.clear();
        links.put("QLive", "https://developer.qiniu.com/lowcode/api/12045/QLive");
        links.put("QLiveUIKit", "https://developer.qiniu.com/lowcode/api/12046/QLiveUIKit");
        links.put("QUserInfo", "https://developer.qiniu.com/lowcode/api/12047/QUserInfo");
        links.put("QTokenGetter", "https://developer.qiniu.com/lowcode/api/12048/QTokenGetter");
        links.put("QLiveConfig", "https://developer.qiniu.com/lowcode/api/12049/QLiveConfig");
        links.put("QRooms", "https://developer.qiniu.com/lowcode/api/12050/QRooms");
        links.put("QLiveStatus", "https://developer.qiniu.com/lowcode/api/12051/QLiveStatus");
        links.put("QLiveCallBack", "https://developer.qiniu.com/lowcode/api/12052/QLiveCallBack");
        links.put("QClientType", "https://developer.qiniu.com/lowcode/api/12053/QClientType");
        links.put("QClientLifeCycleListener", "https://developer.qiniu.com/lowcode/api/12054/QClientLifeCycleListener");
        links.put("QInvitationHandler", "https://developer.qiniu.com/lowcode/api/12055/QInvitationHandler");
        links.put("QInvitationHandlerListener", "https://developer.qiniu.com/lowcode/api/12056/QInvitationHandlerListener");
        links.put("QCreateRoomParam", "https://developer.qiniu.com/lowcode/api/12057/QCreateRoomParam");
        links.put("QDanmaku", "https://developer.qiniu.com/lowcode/api/12058/QDanmaku");
        links.put("QExtension", "https://developer.qiniu.com/lowcode/api/12059/QExtension");
        links.put("QInvitation", "https://developer.qiniu.com/lowcode/api/12060/QInvitation");
        links.put("QLiveRoomInfo", "https://developer.qiniu.com/lowcode/api/12061/QLiveRoomInfo");
        links.put("QLiveUser", "https://developer.qiniu.com/lowcode/api/12062/QLiveUser");
        links.put("QMicLinker", "https://developer.qiniu.com/lowcode/api/12063/QMicLinker");
        links.put("QPKSession", "https://developer.qiniu.com/lowcode/api/12064/QPKSession");
        links.put("QPublicChat", "https://developer.qiniu.com/lowcode/api/12065/QPublicChat");
        links.put("QPlayerEventListener", "https://developer.qiniu.com/lowcode/api/12066/QPlayerEventListener");
        links.put("QBeautySetting", "https://developer.qiniu.com/lowcode/api/12067/QBeautySetting");
        links.put("QConnectionStatusLister", "https://developer.qiniu.com/lowcode/api/12068/QConnectionStatusLister");
        links.put("QPlayerRenderView", "https://developer.qiniu.com/lowcode/api/12069/QPlayerRenderView");
        links.put("QMicrophoneParam", "https://developer.qiniu.com/lowcode/api/12073/QMicrophoneParam");
        links.put("QCameraParam", "https://developer.qiniu.com/lowcode/api/12074/QCameraParam");
        links.put("QMixStreaming.MixStreamParams", "https://developer.qiniu.com/lowcode/api/12075/QMixStreaming_MixStreamParams");
        links.put("QMixStreaming.TranscodingLiveStreamingImage", "https://developer.qiniu.com/lowcode/api/12076/QMixStreaming_TranscodingLiveStreamingImage");
        links.put("QMixStreaming.CameraMergeOption", "https://developer.qiniu.com/lowcode/api/12078/QMixStreaming_CameraMergeOption");
        links.put("QMixStreaming.MergeOption", "https://developer.qiniu.com/lowcode/api/12079/QMixStreaming_MergeOption");
        links.put("QMixStreaming.MicrophoneMergeOption", "https://developer.qiniu.com/lowcode/api/12080/QMixStreaming_MicrophoneMergeOption");
        links.put("QPusherClient", "https://developer.qiniu.com/lowcode/api/12081/QPusherClient");
        links.put("QPushRenderView", "https://developer.qiniu.com/lowcode/api/12082/QPushRenderView");
        links.put("QPlayerClient", "https://developer.qiniu.com/lowcode/api/12083/QPlayerClient");
        links.put("QLiveStatusListener", "https://developer.qiniu.com/lowcode/api/12084/QLiveStatusListener");
        links.put("QLinkMicService", "https://developer.qiniu.com/lowcode/api/12085/QLinkMicService");
        links.put("QLinkMicServiceListener", "https://developer.qiniu.com/lowcode/api/12086/QLinkMicServiceListener");
        links.put("QAnchorHostMicHandler", "https://developer.qiniu.com/lowcode/api/12087/QAnchorHostMicHandler");
        links.put("QLinkMicMixStreamAdapter", "https://developer.qiniu.com/lowcode/api/12088/QLinkMicMixStreamAdapter");
        links.put("QAudienceMicHandler", "https://developer.qiniu.com/lowcode/api/12089/QAudienceMicHandler");
        links.put("QAudienceMicHandler.LinkMicHandlerListener", "https://developer.qiniu.com/lowcode/api/12090/QAudienceMicHandler_LinkMicHandlerListener");
        links.put("QAudioFrameListener", "https://developer.qiniu.com/lowcode/api/12091/QAudioFrameListener");
        links.put("QVideoFrameListener", "https://developer.qiniu.com/lowcode/api/12092/QVideoFrameListener");
        links.put("QPKService", "https://developer.qiniu.com/lowcode/api/12093/QPKService");
        links.put("QPKServiceListener", "https://developer.qiniu.com/lowcode/api/12094/QPKServiceListener");
        links.put("QPKMixStreamAdapter", "https://developer.qiniu.com/lowcode/api/12095/QPKMixStreamAdapter");
        links.put("QPublicChatService", "https://developer.qiniu.com/lowcode/api/12096/QPublicChatService");
        links.put("QPublicChatServiceLister", "https://developer.qiniu.com/lowcode/api/12097/QPublicChatServiceLister");
        links.put("QRoomService", "https://developer.qiniu.com/lowcode/api/12098/QRoomService");
        links.put("QRoomServiceListener", "https://developer.qiniu.com/lowcode/api/12099/QRoomServiceListener");
        links.put("QDanmakuService", "https://developer.qiniu.com/lowcode/api/12100/QDanmakuService");
        links.put("QDanmakuServiceListener", "https://developer.qiniu.com/lowcode/api/12101/QDanmakuServiceListener");
        links.put("QChatRoomService", "https://developer.qiniu.com/lowcode/api/12102/QChatRoomService");
        links.put("QChatRoomServiceListener", "https://developer.qiniu.com/lowcode/api/12103/QChatRoomServiceListener");
        links.put("RoomPage", "https://developer.qiniu.com/lowcode/api/12105/RoomPage");
        links.put("RoomListPage", "https://developer.qiniu.com/lowcode/api/12106/RoomListPage");
        links.put("QLiveFuncComponent", "https://developer.qiniu.com/lowcode/api/12107/QLiveFuncComponent");
        links.put("QLiveComponent", "https://developer.qiniu.com/lowcode/api/12108/QLiveComponent");
        links.put("QLiveUIKitContext", "https://developer.qiniu.com/lowcode/api/12109/QLiveUIKitContext");
        links.put("QItem", "https://developer.qiniu.com/lowcode/api/12128/QItem");
        links.put("QItem.RecordInfo", "https://developer.qiniu.com/lowcode/api/12186/QItem_RecordInfo");
        links.put("QItemStatus", "https://developer.qiniu.com/lowcode/api/12129/QItemStatus");
        links.put("QOrderParam", "https://developer.qiniu.com/lowcode/api/12130/QOrderParam");
        links.put("QSingleOrderParam", "https://developer.qiniu.com/lowcode/api/12131/QSingleOrderParam");
        links.put("QShoppingService", "https://developer.qiniu.com/lowcode/api/12132/QShoppingService");
        links.put("QShoppingServiceListener", "https://developer.qiniu.com/lowcode/api/12133/QShoppingServiceListener");
        links.put("QGift", "https://developer.qiniu.com/lowcode/api/12277/qgift");
        links.put("QGiftMsg", "https://developer.qiniu.com/lowcode/api/12278/qgiftmsg");
        links.put("QGiftService", "https://developer.qiniu.com/lowcode/api/12279/qgiftservice");
        links.put("QGiftServiceListener", "https://developer.qiniu.com/lowcode/api/12280/qgiftservicelistener");
        links.put("QLike", "https://developer.qiniu.com/lowcode/api/12281/qlike");
        links.put("QLikeResponse", "https://developer.qiniu.com/lowcode/api/12282/qlikeresponse");
        links.put("QLikeService", "https://developer.qiniu.com/lowcode/api/12283/qlikeservice");
        links.put("QLikeServiceListener", "https://developer.qiniu.com/lowcode/api/12284/qlikeservicelistener");

        links.put("QKTVMusic", "https://developer.qiniu.com/lowcode/api/12345/qktvmusic");
        links.put("QKTVService", "https://developer.qiniu.com/lowcode/api/12346/qktvservice");
        links.put("QKTVServiceListener", "https://developer.qiniu.com/lowcode/api/12347/qktvservicelistener");
    }

    private static String checkLinker(String name) {
        if (name.equals("MicrophoneMergeOption")) {
            name = "QMixStreaming.MicrophoneMergeOption";
        }
        if (name.equals("TranscodingLiveStreamingImage")) {
            name = "QMixStreaming.TranscodingLiveStreamingImage";
        }
        if (name.equals("CameraMergeOption")) {
            name = "QMixStreaming.CameraMergeOption";
        }
        if (name.equals("MergeOption")) {
            name = "QMixStreaming.MergeOption";
        }

        if (name.equals("LinkMicHandlerListener")) {
            name = "QAudienceMicHandler.LinkMicHandlerListener";
        }
        if (name.equals("MixStreamParams")) {
            name = "QMixStreaming.MixStreamParams";
        }
        if (name.equals("RecordInfo")) {
            name = "QItem.RecordInfo";
        }
        if (links.get(name) == null) {
            return name;
        } else {
            return "{{" + name + "}}";
        }
    }

    public static void println(ArrayList<String> sources, String outDir) throws NoSuchFieldException, IllegalAccessException {
        initLink();
        ArrayList<String> list = new ArrayList<>();
        list.add("-doclet");
        list.add(Doclet.class.getName());
        list.addAll(sources);
        System.out.println("开始调用doc");
        com.sun.tools.javadoc.Main.execute(list.toArray(new String[list.size()]));

        ClassDoc[] classes = Doclet.root.classes();
        System.out.println("结束调用doc " + classes.length);
        StringBuilder sb = new StringBuilder();

        LinkedList<String> classNameList = new LinkedList<String>();
        for (Map.Entry<String, String> entry : links.entrySet()) {
            classNameList.add(entry.getKey());
        }
        LinkedList<ClassDoc> classDocsList = new LinkedList<ClassDoc>(Arrays.asList(classes));
        classDocsList.sort(Comparator.comparingInt(classDoc -> classNameList.indexOf(classDoc.name()))
        );

        for (ClassDoc classDoc : classDocsList) {
            boolean isBuildDoc = links.containsKey(classDoc.name());
            System.out.println("是否生成文档 " + classDoc.name() + "  " + isBuildDoc);
            if (!isBuildDoc) {
                continue;
            }
            System.out.println(classDoc.name() + "  !");
            DocFormat format = new DocFormat();
            format.name = classDoc.name();
            format.home = false;

            format.describe = new DocFormat.Describe();
            format.describe.content.add(getClassType(classDoc) + " " + classDoc.qualifiedName());
            format.describe.content.add(classDoc.commentText());

            format.reflect = links;

            sb.append("\n//" + replaceBlank2(classDoc.commentText()) + "\n");
            sb.append((classDoc.name()) + "{\n");

            FieldDoc[] fields = classDoc.fields();
            DocFormat.BlockItem filedItem = new DocFormat.BlockItem();

            filedItem.name = "字段";

            for (FieldDoc field : fields) {
                DocFormat.ElementItem i = new DocFormat.ElementItem();
                i.name = field.name();
                i.desc.add(field.commentText());
                i.sign = field.modifiers() + " " + checkLinker(field.type().simpleTypeName()) + " " + field.name();
                filedItem.elements.add(i);
                for (String desc : i.desc) {
                    sb.append("\t").append("//" + replaceBlank2(desc)).append("\n");
                }
                sb.append("\t").append(field.modifiers() + " " + (field.type().simpleTypeName()) + " " + field.name() + "\n");
            }
            if (filedItem.elements.size() > 0) {
                format.blocks.add(filedItem);
            }

            DocFormat.BlockItem methodItem = new DocFormat.BlockItem();
            methodItem.name = "方法";
            if (!classDoc.isEnum()) {
                MethodDoc[] methods = classDoc.methods();
                for (MethodDoc method : methods) {

                    DocFormat.ElementItem elementItem = new DocFormat.ElementItem();
                    elementItem.name = method.name();
                    elementItem.desc.add(method.commentText());
                    elementItem.returns = checkLinker(method.returnType().simpleTypeName());
                    //  elementItem.sign = method.toString();
//                    Class<ProgramElementDocImpl> clz = ProgramElementDocImpl.class;
//                    Field ageField = clz.getDeclaredField("tree");
//                    ageField.setAccessible(true);
//                    JCTree ageValue = (JCTree) ageField.get(method);
//                    String mn = ageValue.toString();
//                    elementItem.sign = mn;

                    Parameter[] parameters = method.parameters();
                    ParamTag[] tags = method.paramTags();

                    if (method.name().equals("auth")) {
                        if (elementItem.note == null) {
                            elementItem.note = new ArrayList<>();
                        }
                        elementItem.note.add("认证成功后才能使用qlive的功能");
                    }

                    int index = 0;

                    sb.append("\n");
                    sb.append("\t").append("//").append(replaceBlank(method.commentText())).append("\n");
                    if (tags.length > 0) {
                        sb.append("\t").append("//");
                    }
                    StringBuilder paramsMap = new StringBuilder();
                    for (ParamTag tag : tags) {
                        DocFormat.ParameterItem parameterItem = new DocFormat.ParameterItem();
                        parameterItem.name = tag.parameterName();
                        parameterItem.desc = tag.parameterComment();
                        parameterItem.type = checkLinker(parameters[index].type().simpleTypeName());
                        elementItem.parameters.add(parameterItem);

                        sb.append("@param-").append(parameterItem.name).append(":").append(parameterItem.desc).append('\t');
                        paramsMap.append(parameters[index].type().simpleTypeName()).append(" ").append(parameterItem.name);
                        index++;
                        if (index != tags.length) {
                            paramsMap.append(",");
                        }
                    }
                    String mn = method.modifiers() + " " + method.returnType() + " " + method.name() + "(" + paramsMap.toString() + ")";
                    elementItem.sign = mn;
                    if (tags.length > 0) {
                        sb.append("\n");
                    }
                    //   sb.append("\t").append((method.modifiers() + " " + method.returnType().simpleTypeName() + " " + method.name() + " " + method.flatSignature())).append(replaceBlank(method.commentText())).append(replaceBlank(method.commentText())).append("\n");
                    sb.append("\t").append(mn).append("\n");

                    methodItem.elements.add(elementItem);
                }

                if (methods.length > 0) {
                    format.blocks.add(methodItem);
                }
            }
            if (format.blocks.size() > 0) {
                System.out.println(links.get(classDoc.name()));
                System.out.println(format.toJson());
            }
            sb.append("}\n");
        }
        System.out.println("api概览");
        DocFormat format = new DocFormat();
        format.name = "api概览";
        format.home = true;
        format.reflect = links;

        for (ClassDoc classDoc : classDocsList) {
            if (links.get(classDoc.name()) == null) {
                continue;
            }
            DocFormat.BlockItem classItem = new DocFormat.BlockItem();
            classItem.name = "";
            classItem.desc.add(checkLinker(classDoc.name()));
            classItem.desc.add(classDoc.commentText());
            format.blocks.add(classItem);
        }

//        writeToFile(format.toJson(), outDir + "/doc.txt");
//        writeToFile(sb.toString(), outDir + "/md.txt");

        System.out.println(format.toJson());

        System.out.println(sb.toString());
    }

    private static void writeToFile(String str, String outPath) {

        try {
            File file = new File(outPath);
            if (file.exists()) {
                file.delete();
            }
            FileWriter fileWritter = new FileWriter(file.getName(), true);
            BufferedWriter bufferWritter = new BufferedWriter(fileWritter);

            bufferWritter.write(str + "\n");

            bufferWritter.flush();
            bufferWritter.close();
            fileWritter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getClassType(ClassDoc classDoc) {
        if (classDoc.isEnum()) {
            return "enum";
        }
        if (classDoc.isInterface()) {
            return "interface";
        }
        if (classDoc.isClass()) {
            return "class";
        }
        return "";
    }

    /**
     * 文档根节点
     */
    private static RootDoc root;

    /**
     * javadoc调用入口
     *
     * @param root
     * @return
     */
    public static boolean start(RootDoc root) {
        Doclet.root = root;
        return true;
    }

    public static String replaceBlank(String str) {
        String dest = "";
        if (str != null) {
            Pattern p = Pattern.compile("\\s*|\\t|\\r|\\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }

    public static String replaceBlank2(String str) {
        String dest = str.replaceAll("\n", " ");
        return dest;
    }
}
