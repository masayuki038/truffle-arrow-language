package net.wrap_trap.truffle_arrow.language.truffle;

import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectLibrary;

import java.util.HashMap;
import java.util.Map;

public class TruffleUtils {

  public static Map<Object, Object> toMap(DynamicObject dynamicObject, DynamicObjectLibrary objLib) {
    Map<Object, Object> map = new HashMap<>();
    for (Object key: objLib.getKeyArray(dynamicObject)) {
      map.put(key, objLib.getOrDefault(dynamicObject, key, null));
    }
    return map;
  }
}
