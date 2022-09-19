import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.javadoc.*;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javadoc.ProgramElementDocImpl;

/**
 * 类说明：打印类及其字段、方法的注释<br>
 * 使用javadoc实现<br>
 * 需要在工程中加载jdk中的包$JAVA_HOME/lib/tools.jar
 * <p>
 * <p>
 * 文档工具https://cf.qiniu.io/pages/viewpage.action?pageId=72910016
 */
public class Doclet {
    //跟新文档可替换本地路径前缀
    private static String projectPath = "/Users/manjiale/dev/QNLiveKit_Android/";

    public static void main(String[] args) {
        //java源文件的路径
        ArrayList<String> sources = new ArrayList<>();

        sources.add(projectPath + "qlivesdk/src/main/java/com/qlive/sdk/QLive.java");
        sources.add(projectPath + "qlivesdk/src/main/java/com/qlive/sdk/QLiveUIKit.java");
        sources.add(projectPath + "qlivesdk/src/main/java/com/qlive/sdk/QUserInfo.java");

        sources.add(projectPath + "liveroom-core/src/main/java/com/qlive/core/QTokenGetter.java");
        sources.add(projectPath + "liveroom-core/src/main/java/com/qlive/core/QLiveConfig.java");
        sources.add(projectPath + "liveroom-core/src/main/java/com/qlive/core/QRooms.java");
        sources.add(projectPath + "liveroom-core/src/main/java/com/qlive/core/QLiveStatus.java");
        sources.add(projectPath + "liveroom-core/src/main/java/com/qlive/core/QLiveCallBack.java");
        sources.add(projectPath + "liveroom-core/src/main/java/com/qlive/core/QClientType.java");
        sources.add(projectPath + "liveroom-core/src/main/java/com/qlive/core/QClientLifeCycleListener.java");
        sources.add(projectPath + "liveroom-core/src/main/java/com/qlive/core/QInvitationHandler.java");
        sources.add(projectPath + "liveroom-core/src/main/java/com/qlive/core/QInvitationHandlerListener.java");

        //java been
        sources.add(projectPath + "liveroom-core/src/main/java/com/qlive/core/been/QCreateRoomParam.java");
        sources.add(projectPath + "service/danmakuservice/src/main/java/com/qlive/danmakuservice/QDanmaku.java");
        sources.add(projectPath + "liveroom-core/src/main/java/com/qlive/core/been/QExtension.java");
        sources.add(projectPath + "liveroom-core/src/main/java/com/qlive/core/been/QInvitation.java");
        sources.add(projectPath + "liveroom-core/src/main/java/com/qlive/core/been/QLiveRoomInfo.java");
        sources.add(projectPath + "liveroom-core/src/main/java/com/qlive/core/been/QLiveRoomInfo.java");
        sources.add(projectPath + "liveroom-core/src/main/java/com/qlive/core/been/QLiveUser.java");
        sources.add(projectPath + "service/linkmicservice/src/main/java/com/qlive/linkmicservice/QMicLinker.java");
        sources.add(projectPath + "service/pkservice/src/main/java/com/qlive/pkservice/QPKSession.java");
        sources.add(projectPath + "service/publicchatservice/src/main/java/com/qlive/pubchatservice/QPublicChat.java");


        sources.add(projectPath + "liveroom-libs/comp_avparam/src/main/java/com/qlive/avparam/QPlayerEventListener.java");
        sources.add(projectPath + "liveroom-libs/comp_avparam/src/main/java/com/qlive/avparam/QBeautySetting.java");
        sources.add(projectPath + "liveroom-libs/comp_avparam/src/main/java/com/qlive/avparam/QBeautySetting.java");
        sources.add(projectPath + "liveroom-libs/comp_avparam/src/main/java/com/qlive/avparam/QConnectionStatusLister.java");
        sources.add(projectPath + "liveroom-libs/comp_avparam/src/main/java/com/qlive/avparam/QPlayerRenderView.java");
        sources.add(projectPath + "liveroom-libs/comp_avparam/src/main/java/com/qlive/avparam/QMicrophoneParam.java");
        sources.add(projectPath + "liveroom-libs/comp_avparam/src/main/java/com/qlive/avparam/QCameraParam.java");
        sources.add(projectPath + "liveroom-libs/comp_avparam/src/main/java/com/qlive/avparam/QMixStreaming.java");

        //推流
        sources.add(projectPath + "service/publicchatservice/src/main/java/com/qlive/pubchatservice/QPublicChat.java");
        sources.add(projectPath + "liveroom-pushclient/src/main/java/com/qlive/pushclient/QPusherClient.java");
        sources.add(projectPath + "liveroom-libs/comp_rtclive/src/main/java/com/qlive/rtclive/QPushRenderView.java");


        //拉流端
        sources.add(projectPath + "liveroom-pullclient/src/main/java/com/qlive/playerclient/QPlayerClient.java");


        sources.add(projectPath + "liveroom-core/src/main/java/com/qlive/core/QLiveStatusListener.java");

        sources.add(projectPath + "service/linkmicservice/src/main/java/com/qlive/linkmicservice/QLinkMicService.java");
        sources.add(projectPath + "service/linkmicservice/src/main/java/com/qlive/linkmicservice/QLinkMicServiceListener.java");
        sources.add(projectPath + "service/linkmicservice/src/main/java/com/qlive/linkmicservice/QAnchorHostMicHandler.java");
        sources.add(projectPath + "service/linkmicservice/src/main/java/com/qlive/linkmicservice/QLinkMicMixStreamAdapter.java");
        sources.add(projectPath + "service/linkmicservice/src/main/java/com/qlive/linkmicservice/QAudienceMicHandler.java");

        sources.add(projectPath + "liveroom-libs/comp_avparam/src/main/java/com/qlive/avparam/QAudioFrameListener.java");
        sources.add(projectPath + "liveroom-libs/comp_avparam/src/main/java/com/qlive/avparam/QVideoFrameListener.java");

        sources.add(projectPath + "service/pkservice/src/main/java/com/qlive/pkservice/QPKService.java");
        sources.add(projectPath + "service/pkservice/src/main/java/com/qlive/pkservice/QPKServiceListener.java");
        sources.add(projectPath + "service/pkservice/src/main/java/com/qlive/pkservice/QPKMixStreamAdapter.java");


        sources.add(projectPath + "service/publicchatservice/src/main/java/com/qlive/pubchatservice/QPublicChatService.java");
        sources.add(projectPath + "service/publicchatservice/src/main/java/com/qlive/pubchatservice/QPublicChatServiceLister.java");

        sources.add(projectPath + "service/roomservice/src/main/java/com/qlive/roomservice/QRoomService.java");
        sources.add(projectPath + "service/roomservice/src/main/java/com/qlive/roomservice/QRoomServiceListener.java");

        sources.add(projectPath + "service/roomservice/src/main/java/com/qlive/roomservice/QRoomService.java");
        sources.add(projectPath + "service/roomservice/src/main/java/com/qlive/roomservice/QRoomService.java");

        sources.add(projectPath + "service/danmakuservice/src/main/java/com/qlive/danmakuservice/QDanmakuService.java");
        sources.add(projectPath + "service/danmakuservice/src/main/java/com/qlive/danmakuservice/QDanmakuServiceListener.java");

        sources.add(projectPath + "service/chatservice/src/main/java/com/qlive/chatservice/QChatRoomService.java");
        sources.add(projectPath + "service/chatservice/src/main/java/com/qlive/chatservice/QChatRoomServiceListener.java");


        sources.add(projectPath + "liveroom-uikit/src/main/java/com/qlive/uikit/RoomPage.java");
        sources.add(projectPath + "liveroom-uikit/src/main/java/com/qlive/uikit/RoomListPage.java");
        sources.add(projectPath + "doc/docbuid/src/QLiveFuncComponent.java");
        sources.add(projectPath + "doc/docbuid/src/QLiveComponent.java");
        sources.add(projectPath + "doc/docbuid/src/QLiveUIKitContext.java");


        sources.add(projectPath + "service/shoppingservice/src/main/java/com/qlive/shoppingservice/QItem.java");
        sources.add(projectPath + "service/shoppingservice/src/main/java/com/qlive/shoppingservice/QItemStatus.java");
        sources.add(projectPath + "service/shoppingservice/src/main/java/com/qlive/shoppingservice/QOrderParam.java");
        sources.add(projectPath + "service/shoppingservice/src/main/java/com/qlive/shoppingservice/QSingleOrderParam.java");
        sources.add(projectPath + "service/shoppingservice/src/main/java/com/qlive/shoppingservice/QShoppingService.java");
        sources.add(projectPath + "service/shoppingservice/src/main/java/com/qlive/shoppingservice/QShoppingServiceListener.java");

        //打印
        try {
            println(sources);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 打印类及其字段、方法的注释
     *
     * @param sources java源文件路径
     */
    private static HashMap<String, String> links = new HashMap<>();

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

    public static void println(ArrayList<String> sources) throws NoSuchFieldException, IllegalAccessException {
        ArrayList<String> list = new ArrayList<>();
        list.add("-doclet");
        list.add(Doclet.class.getName());
        list.addAll(sources);
        com.sun.tools.javadoc.Main.execute(list.toArray(new String[list.size()]));

        ClassDoc[] classes = Doclet.root.classes();

        int DocIndex = 12045;
        for (ClassDoc classDoc : classes) {
            if (classDoc.name().equals("QMixStreaming")) {
                continue;
            }
            if (classDoc.name().equals("QMixStreaming.TrackMergeOption")) {
                continue;
            }
            if (classDoc.name().equals("QMicrophoneParam")) {
                DocIndex = 12073;
            }
            if (classDoc.name().equals("QMixStreaming.CameraMergeOption")) {
                DocIndex = 12078;
            }
            if (classDoc.name().equals("QMixStreaming.MicrophoneMergeOption")) {
                DocIndex = 12080;
            }

            if (classDoc.name().equals("RoomPage")) {
                DocIndex = 12105;
            }
            if (classDoc.name().equals("QItem")) {
                DocIndex = 12128;
            }
            if (classDoc.name().equals("QItem.RecordInfo")){
                DocIndex =    12186;
            }
            if (classDoc.name().equals("QItemStatus")){
                DocIndex =    12129;
            }

            links.put(classDoc.name(), "https://developer.qiniu.com/lowcode/api/" + String.valueOf(DocIndex++) + "/" + classDoc.name().replace(".", "_"));
        }

        StringBuffer sb = new StringBuffer();

        for (ClassDoc classDoc : classes) {
            System.out.println(classDoc.name() + "  !");
            DocFormat format = new DocFormat();
            format.name = classDoc.name();
            format.home = false;

            format.describe = new DocFormat.Describe();
            format.describe.content.add(getClassType(classDoc) + " " + classDoc.qualifiedName());
            format.describe.content.add(classDoc.commentText());

            format.reflect = links;

            sb.append("\n//" + classDoc.commentText() + "\n");
            sb.append((classDoc.name()) + "{\n");
//            DocFormat.BlockItem classItem = new DocFormat.BlockItem();
//            classItem.name = classDoc.qualifiedName();
//            classItem.desc.add( classDoc.commentText());

            FieldDoc[] fields = classDoc.fields();
            DocFormat.BlockItem filedItem = new DocFormat.BlockItem();


            filedItem.name = "字段";
            for (FieldDoc field : fields) {
                DocFormat.ElementItem i = new DocFormat.ElementItem();
                i.name = field.name();
                i.desc.add(field.commentText());
                i.sign = field.modifiers() + " " + checkLinker(field.type().simpleTypeName()) + " " + field.name();
                filedItem.elements.add(i);

                sb.append("\t").append(field.modifiers() + " " + (field.type().simpleTypeName()) + " " + field.name() + ";//" + i.desc + "\n");
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

                    Class<ProgramElementDocImpl> clz = ProgramElementDocImpl.class;
                    Field ageField = clz.getDeclaredField("tree");
                    ageField.setAccessible(true);
                    JCTree ageValue = (JCTree) ageField.get(method);
                    String mn = ageValue.toString();
                    elementItem.sign = mn;
                    //  elementItem.sign = method.modifiers() + " " + method.returnType() + " " + method.name() + " " + method.signature();
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
                    for (ParamTag tag : tags) {
                        DocFormat.ParameterItem parameterItem = new DocFormat.ParameterItem();
                        parameterItem.name = tag.parameterName();
                        parameterItem.desc = tag.parameterComment();
                        parameterItem.type = checkLinker(parameters[index].type().simpleTypeName());
                        elementItem.parameters.add(parameterItem);
                        index++;
                        sb.append("@param-").append(parameterItem.name).append(":").append(parameterItem.desc).append('\t');
                    }
                    if (tags.length > 0) {
                        sb.append("\n");
                    }
                    //   sb.append("\t").append((method.modifiers() + " " + method.returnType().simpleTypeName() + " " + method.name() + " " + method.flatSignature())).append(replaceBlank(method.commentText())).append(replaceBlank(method.commentText())).append("\n");
                    sb.append("\t").append(replaceBlank2(mn)).append(replaceBlank(method.commentText())).append("\n");

                    methodItem.elements.add(elementItem);
                }

                if (methods.length > 0) {
                    format.blocks.add(methodItem);
                }
            }

            if (classDoc.name().equals("QLiveUIKitContext")) {
                DocFormat.BlockItem filedItemKit = new DocFormat.BlockItem();
                filedItemKit.name = "字段";

                filedItemKit.elements.add(new DocFormat.ElementItem("androidContext", "val androidContext: Context", "安卓上下文"));
                filedItemKit.elements.add(new DocFormat.ElementItem("fragmentManager", "val fragmentManager: FragmentManager,", "安卓FragmentManager 用于显示弹窗"));
                filedItemKit.elements.add(new DocFormat.ElementItem("currentActivity", "val currentActivity: Activity", "当前所在的Activity"));
                filedItemKit.elements.add(new DocFormat.ElementItem("lifecycleOwner", "val lifecycleOwner: LifecycleOwner", "当前页面的安卓LifecycleOwner"));
                filedItemKit.elements.add(new DocFormat.ElementItem("leftRoomActionCall", " val leftRoomActionCall: (resultCall: QLiveCallBack<Void>) -> Unit", "离开房间操作 在任意UI组件中可以操作离开房间"));
                filedItemKit.elements.add(new DocFormat.ElementItem("createAndJoinRoomActionCall", "val createAndJoinRoomActionCall: (param: QCreateRoomParam, resultCall: QLiveCallBack<Void>) -> Unit", "创建并且加入房间操作 在任意UI组件中可创建并且加入房间"));
                filedItemKit.elements.add(new DocFormat.ElementItem("getPlayerRenderViewCall", "val getPlayerRenderViewCall: () -> QPlayerRenderView?", "获取当前播放器预览窗口 在任意UI组件中如果要对预览窗口变化可直接获取"));
                filedItemKit.elements.add(new DocFormat.ElementItem("getPusherRenderViewCall", " val getPusherRenderViewCall: () -> QPushRenderView?", "获取推流预览窗口  在任意UI组件中如果要对预览窗口变化可直接获取"));


                format.blocks.add(filedItemKit);


                DocFormat.ElementItem getLiveFuncComponent = new DocFormat.ElementItem();

                getLiveFuncComponent.name = "getLiveFuncComponent";
                getLiveFuncComponent.desc.add("获得某个功能组件的对象");
                getLiveFuncComponent.returns = checkLinker("T : QLiveFuncComponent");
                getLiveFuncComponent.sign="fun <T : QLiveFuncComponent> getLiveFuncComponent(serviceClass: Class<T>): T?";
                DocFormat.ParameterItem parameterItem = new DocFormat.ParameterItem();
                parameterItem.name = "serviceClass";
                parameterItem.desc = "具体的功能组件类";
                parameterItem.type = "Class<T>";
                getLiveFuncComponent.parameters.add(parameterItem);

                DocFormat.BlockItem methodI = new DocFormat.BlockItem();
                methodI.name = "方法";
                methodI.elements.add(getLiveFuncComponent);


                format.blocks.add(methodI);

                for (DocFormat.ElementItem e : filedItemKit.elements) {
                    sb.append("\t").append(e.sign + ";//" + e.desc + "\n");
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

        for (ClassDoc classDoc : classes) {
            if (links.get(classDoc.name()) == null) {
                continue;
            }
            DocFormat.BlockItem classItem = new DocFormat.BlockItem();
            classItem.name = "";
            classItem.desc.add(checkLinker(classDoc.name()));
            classItem.desc.add(classDoc.commentText());
            format.blocks.add(classItem);
        }
        System.out.println(format.toJson());

        System.out.println("```java");
        System.out.println(sb.toString());
        System.out.println("```");
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
        String dest = str.replaceAll("\n","");

        return dest;
    }
}
