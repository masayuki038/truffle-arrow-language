package net.wrap_trap.truffle_arrow.language.truffle.node;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.Layout;
import com.oracle.truffle.api.object.Shape;
import net.wrap_trap.truffle_arrow.language.truffle.TruffleArrowObjectType;


@NodeInfo(shortName = "{}")
public abstract class ExprNewObjectLiteral extends ExprBase {
  static final Layout LAYOUT = Layout.createLayout();
  static final Shape emptyShape = LAYOUT.createShape(TruffleArrowObjectType.SINGLETON);

  @Specialization
  public Object newObject() {
    return emptyShape.newInstance();
  }
}

