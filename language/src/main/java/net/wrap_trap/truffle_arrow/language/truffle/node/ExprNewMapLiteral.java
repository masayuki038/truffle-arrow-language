package net.wrap_trap.truffle_arrow.language.truffle.node;

import com.oracle.truffle.api.frame.VirtualFrame;

import java.util.TreeMap;
import java.util.Map;

public class ExprNewMapLiteral extends ExprBase {

  final Map<Object, Object> value;

  public ExprNewMapLiteral() {
    this.value = new TreeMap<>();
  }

  @Override
  public Object executeGeneric(VirtualFrame virtualFrame) {
    return value;
  }

  @Override
  public String toString() {
    return "" + value;
  }
}
