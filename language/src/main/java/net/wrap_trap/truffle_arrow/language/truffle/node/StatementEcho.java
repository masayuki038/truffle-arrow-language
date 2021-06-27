package net.wrap_trap.truffle_arrow.language.truffle.node;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeInfo;


@NodeInfo(shortName = "echo")
public class StatementEcho extends StatementBase {

  @Node.Child
  private ExprBase expr;

  public StatementEcho(ExprBase expr) {
    this.expr = expr;
  }

  @Override
  void executeVoid(VirtualFrame frame) {
    System.out.println(this.expr.executeGeneric(frame));
  }
}
