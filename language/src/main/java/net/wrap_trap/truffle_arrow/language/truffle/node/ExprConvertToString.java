package net.wrap_trap.truffle_arrow.language.truffle.node;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;

@NodeInfo
public class ExprConvertToString extends ExprBase {

  @Child
  private ExprBase expr;

  public ExprConvertToString(ExprBase expr) {
    this.expr = expr;
  }

  @Override
  public Object executeGeneric(VirtualFrame frame) {
    Object o = this.expr.executeGeneric(frame);
    return o.toString();
  }
}
