package com.qlive.uikitcore;

import com.qlive.core.QLiveService;

import java.util.HashMap;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

class JavaGenericsEliminateUtil {
    static <T extends UIEvent> void registerEventAction(HashMap<String, Function1<UIEvent, Unit>> map, Class<T> clz, Function1<T, Unit> call) {
        map.put(UIEvent.Companion.getAction(clz), (Function1<UIEvent, Unit>) call);
    }
    static <T extends QLiveFuncComponent> T getLiveComponent(List<BaseComponent<?>> comps , Class<T> serviceClass){
       for (BaseComponent<?> c:comps){
           if(c.getClass().getCanonicalName().equals(serviceClass.getCanonicalName())
            && c instanceof QLiveFuncComponent
           ){
              return (T) c;
           }
       }
       return null;
    }
}
