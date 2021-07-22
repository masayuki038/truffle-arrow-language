package net.wrap_trap.truffle_arrow.language.truffle.node.type;

import com.oracle.truffle.api.interop.TruffleObject;

import java.util.Map;

public class MapWrapper implements TruffleObject {
  private final Map<Object, Object> map;

  public MapWrapper(Map<Object, Object> map) {
    this.map = map;
  }

  public Map<Object, Object> getMap() {
    return map;
  }
}
