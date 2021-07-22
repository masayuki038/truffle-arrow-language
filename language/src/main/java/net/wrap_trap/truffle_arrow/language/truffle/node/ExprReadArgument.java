package net.wrap_trap.truffle_arrow.language.truffle.node;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.BranchProfile;

public class ExprReadArgument extends ExprBase {
  private final int index;

  private final BranchProfile outOfBoundsTaken = BranchProfile.create();

  public ExprReadArgument(int index) {
    this.index = index;
  }

  @Override
  public Object executeGeneric(VirtualFrame frame) {
    Object[] args = frame.getArguments();
    if (index < args.length) {
      return args[index];
    } else {
      outOfBoundsTaken.enter();
      return SqlNull.INSTANCE;
    }
  }
}
