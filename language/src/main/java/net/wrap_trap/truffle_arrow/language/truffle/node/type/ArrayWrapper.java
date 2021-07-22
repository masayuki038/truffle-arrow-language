package net.wrap_trap.truffle_arrow.language.truffle.node.type;

import com.oracle.truffle.api.interop.TruffleObject;

public class ArrayWrapper implements TruffleObject {

  private final Object[] array;

  public ArrayWrapper(Object[] array) {
    this.array = array;
  }

  public Object[] getArray() {
    return this.array;
  }
}
