package net.wrap_trap.truffle_arrow.language.truffle.node;

import com.oracle.truffle.api.frame.VirtualFrame;

import java.util.Map;


public class ExprMapMember extends ExprBase {

  final ExprReadLocal mapVariable;
  final ExprBase member;

  public ExprMapMember(ExprReadLocal mapVariable, ExprBase member) {
    this.mapVariable = mapVariable;
    this.member = member;
  }

  @Override
  public Object executeGeneric(VirtualFrame frame) {
    Object o = this.mapVariable.readObject(frame);
    if (!(o instanceof Map)) {
      throw new IllegalArgumentException("Map expected, but: " + o);
    }
    Map<Object, Object> map = (Map<Object, Object>) o;
    return map.get(this.member.executeGeneric(frame));
  }
}
