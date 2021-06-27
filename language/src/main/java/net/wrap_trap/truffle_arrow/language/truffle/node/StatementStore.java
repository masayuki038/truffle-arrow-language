package net.wrap_trap.truffle_arrow.language.truffle.node;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;
import net.wrap_trap.truffle_arrow.language.truffle.node.arrays.VectorSchemaRootContainer;

import java.util.List;
import java.util.stream.Collectors;


@NodeInfo(shortName = "echo")
public class StatementStore extends StatementBase {

  private List<ExprBase> variables;

  public StatementStore(List<ExprBase> variables) {
    this.variables = variables;
  }

  @Override
  void executeVoid(VirtualFrame frame) {
    List<Object> values = this.variables.stream().map(v -> v.executeGeneric(frame)).collect(Collectors.toList());
    Object first = values.get(0);
    if (!(first instanceof VectorSchemaRootContainer)) {
      throw new IllegalArgumentException("The first parameter of 'store' should be specified a return value of 'arrays'");
    }
    VectorSchemaRootContainer vectorSchemaRoots = (VectorSchemaRootContainer) first;
    vectorSchemaRoots.addValues(values.subList(1, values.size()));
  }
}
