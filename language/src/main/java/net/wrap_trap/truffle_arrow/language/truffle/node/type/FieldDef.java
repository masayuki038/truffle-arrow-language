package net.wrap_trap.truffle_arrow.language.truffle.node.type;

import net.wrap_trap.truffle_arrow.language.FieldType;

public class FieldDef {

  private String name;
  private FieldType type;

  public FieldDef(String name, FieldType type) {
    this.name = name;
    this.type = type;
  }

  public String getName() {
    return this.name;
  }

  public FieldType getType() {
    return this.type;
  }
}
