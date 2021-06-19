package net.wrap_trap.truffle_arrow.language.truffle.node.type;

public class FieldDef {

  private String name;
  private String type;

  public FieldDef(String name, String type) {
    this.name = name;
    this.type = type;
  }

  public String getName() {
    return this.name;
  }

  public String getType() {
    return this.type;
  }
}
