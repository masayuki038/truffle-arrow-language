package net.wrap_trap.truffle_arrow.language.truffle;

import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.dsl.UnsupportedSpecializationException;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RootNode;
import net.wrap_trap.truffle_arrow.language.truffle.node.ExprReadLocal;
import net.wrap_trap.truffle_arrow.language.truffle.node.ReturnException;
import net.wrap_trap.truffle_arrow.language.truffle.node.Statements;
import org.apache.arrow.vector.VectorSchemaRoot;

import java.util.List;


public class TruffleArrowRootNode extends RootNode {

  @Child
  private Statements statements;

  public TruffleArrowRootNode(TruffleLanguage<?> language, FrameDescriptor frameDescriptor, Statements statements) {
    super(language, frameDescriptor);
    this.statements = statements;
  }

  @Override
  public Object execute(VirtualFrame frame) {
    try {
      statements.executeVoid(frame);
    } catch (ReturnException e) {
      Object result = e.getResult();
      if (result instanceof List) {
        return new Result((List<VectorSchemaRoot>) result);
      }
      return e.getResult();
    } catch (UnsupportedSpecializationException e) {
      Node caused = e.getNode();
      if (caused instanceof ExprReadLocal) {
        throw new LocalVariableNotFoundException((ExprReadLocal) caused);
      }
      throw e;
    }
    return true;
  }
}
