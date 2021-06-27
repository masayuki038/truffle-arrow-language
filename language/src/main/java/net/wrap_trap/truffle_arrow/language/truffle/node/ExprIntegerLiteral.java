package net.wrap_trap.truffle_arrow.language.truffle.node;

import com.oracle.truffle.api.frame.VirtualFrame;


public class ExprIntegerLiteral extends ExprBase {

  final int value;

  public ExprIntegerLiteral(int value) {
    this.value = value;
  }

  @Override
  int executeInteger(VirtualFrame vf) {
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
