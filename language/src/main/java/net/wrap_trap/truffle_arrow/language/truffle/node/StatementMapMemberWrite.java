package net.wrap_trap.truffle_arrow.language.truffle.node;

import com.oracle.truffle.api.frame.VirtualFrame;

import java.util.Map;

public class StatementMapMemberWrite extends StatementBase {

  final ExprReadLocal mapVariable;
  final ExprBase member;
  final ExprBase value;

  public StatementMapMemberWrite(ExprReadLocal mapVariable, ExprBase member, ExprBase value) {
    this.mapVariable = mapVariable;
    this.member = member;
    this.value = value;
  }

  @Override
  void executeVoid(VirtualFrame frame) {
    Object o = this.mapVariable.readObject(frame);
    if (!(o instanceof Map)) {
      throw new IllegalArgumentException("Map expected, but: " + o);
    }
    Map<Object, Object> map = (Map<Object, Object>) o;
    map.put(this.member.executeGeneric(frame), this.value.executeGeneric(frame));
  }
}
