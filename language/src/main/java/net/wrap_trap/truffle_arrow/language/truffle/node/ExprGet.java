package net.wrap_trap.truffle_arrow.language.truffle.node;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;

import net.wrap_trap.truffle_arrow.language.ArrowUtils;
import net.wrap_trap.truffle_arrow.language.FieldType;
import net.wrap_trap.truffle_arrow.language.truffle.node.arrays.VectorSchemaRootContainerImpl;
import net.wrap_trap.truffle_arrow.language.truffle.node.type.FieldDef;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.vector.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@NodeInfo(shortName = "get")
public class ExprGet extends ExprBase {

  private ExprBase expr;
  private ExprBase orElse;

  public ExprGet(ExprBase expr, ExprBase orElse) {
    this.expr = expr;
    this.orElse = orElse;
  }

  @Override
  Object executeGeneric(VirtualFrame frame) {
    Object value = this.expr.executeGeneric(frame);
    if (value == null) {
      return this.orElse.executeGeneric(frame);
    }
    return value;
  }
}
