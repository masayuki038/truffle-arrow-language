package net.wrap_trap.truffle_arrow.language.truffle.node.builtins;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.UnsupportedSpecializationException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import net.wrap_trap.truffle_arrow.language.truffle.node.ExprBase;

@NodeChild(value = "arguments", type = ExprBase[].class)
@GenerateNodeFactory
public abstract class TruffleArrowBuiltin extends ExprBase {

  @Override
  public final Object executeGeneric(VirtualFrame frame) {
    try {
      return execute(frame);
    } catch (UnsupportedSpecializationException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public final boolean executeBoolean(VirtualFrame frame) throws UnexpectedResultException {
    return super.executeBoolean(frame);
  }

  @Override
  public final long executeLong(VirtualFrame frame) throws UnexpectedResultException {
    return super.executeLong(frame);
  }

  @Override
  public final void executeVoid(VirtualFrame frame) {
    super.executeVoid(frame);
  }

  protected abstract Object execute(VirtualFrame frame);
}
