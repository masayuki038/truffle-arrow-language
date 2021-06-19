package net.wrap_trap.truffle_arrow.language.truffle.node;

import com.oracle.truffle.api.frame.VirtualFrame;
import net.wrap_trap.truffle_arrow.language.truffle.node.type.FieldDef;

public class ExprFieldDef extends ExprBase {

  final String name;
  final String type;

  public ExprFieldDef(String name, String type) {
    this.name = name;
    this.type = type;
  }

  @Override
  public Object executeGeneric(VirtualFrame frame) {
    return new FieldDef(this.name, this.type);
  }

  @Override
  void executeVoid(VirtualFrame frame) { }

  @Override
  public String toString() {
    return "FieldDef name: " + this.name + " type: " + this.type;
  }
}
