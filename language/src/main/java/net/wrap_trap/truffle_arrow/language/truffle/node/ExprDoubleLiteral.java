package net.wrap_trap.truffle_arrow.language.truffle.node;

import com.oracle.truffle.api.frame.VirtualFrame;


public class ExprDoubleLiteral extends ExprBase {

  final double value;

  public ExprDoubleLiteral(double value) {
    this.value = value;
  }

  @Override
  double executeDouble(VirtualFrame vf) {
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
