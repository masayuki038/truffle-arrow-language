package net.wrap_trap.truffle_arrow.language.truffle.node;

import com.oracle.truffle.api.frame.VirtualFrame;


public class ExprStringLiteral extends ExprBase {

  final String value;

  public ExprStringLiteral(String value) {
    this.value = value;
  }

  @Override
  String executeString(VirtualFrame vf) {
    return value;
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

