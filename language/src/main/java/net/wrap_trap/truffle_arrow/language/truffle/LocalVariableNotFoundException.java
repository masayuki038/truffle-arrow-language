package net.wrap_trap.truffle_arrow.language.truffle;

import net.wrap_trap.truffle_arrow.language.truffle.node.ExprReadLocal;


public class LocalVariableNotFoundException extends RuntimeException {

  private ExprReadLocal exprReadLocal;

  public LocalVariableNotFoundException(ExprReadLocal exprReadLocal) {
    this.exprReadLocal = exprReadLocal;
  }

  @Override
  public String getMessage() {
    return "Failed to reference a local variable: " + exprReadLocal.getSlot().getIdentifier();
  }
}
