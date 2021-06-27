package net.wrap_trap.truffle_arrow.language;

import java.util.HashMap;
import java.util.Map;


public enum FieldType {
  INT("INT"),
  BIGINT("BIGINT"),
  DOUBLE("DOUBLE"),
  STRING("STRING");

  private String typeName;
  private static final Map<String, FieldType> MAP = new HashMap<>();

  FieldType(String typeName) {
    this.typeName = typeName;
  }

  static {
    MAP.put(INT.toString(), INT);
    MAP.put(BIGINT.toString(), BIGINT);
    MAP.put(DOUBLE.toString(), DOUBLE);
    MAP.put(STRING.toString(), STRING);
  }

  @Override
  public String toString() {
    return typeName;
  }

  public static FieldType of(String typeName) {
    return MAP.get(typeName);
  }
}
