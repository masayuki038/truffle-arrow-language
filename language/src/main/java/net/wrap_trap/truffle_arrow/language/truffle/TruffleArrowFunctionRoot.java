package net.wrap_trap.truffle_arrow.language.truffle;

import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.source.SourceSection;
import net.wrap_trap.truffle_arrow.language.truffle.node.ExprBase;

public class TruffleArrowFunctionRoot extends RootNode {

  @Child
  private ExprBase exprBase;
  private final String name;
  private final SourceSection sourceSection;

  public TruffleArrowFunctionRoot(TruffleLanguage<?> language, FrameDescriptor frameDescriptor, SourceSection sourceSection, String name, ExprBase exprBase) {
    super(language, frameDescriptor);
    this.sourceSection = sourceSection;
    this.name = name;
    this.exprBase = exprBase;
  }

  @Override
  public Object execute(VirtualFrame frame) {
    return this.exprBase.executeGeneric(frame);
  }

  @Override
  public String getName() {
    return this.name;
  }
}
