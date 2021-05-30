package net.wrap_trap.truffle_arrow.language.truffle.node;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.nodes.UnexpectedResultException;

@NodeInfo(shortName = "loop")
public class StatementLoop extends StatementBase {

  @Child
  private ExprStringNode dirPath;

  @Child
  private Statements statements;

  public StatementLoop(ExprStringNode dirPath, Statements statements) {
    this.dirPath = dirPath;
    this.statements = statements;
  }

  @Override
  public void executeVoid(VirtualFrame frame) {
    String path = this.dirPath.executeString(frame);
    // loop
    this.statements.executeVoid(frame);
  }
}
