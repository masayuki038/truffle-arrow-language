package net.wrap_trap.truffle_arrow.language.truffle.node;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;

@NodeChild("target")
abstract public class ExprNot extends ExprBase {
  @Specialization
  boolean executeBoolean(boolean value) {
    return !value;
  }
}