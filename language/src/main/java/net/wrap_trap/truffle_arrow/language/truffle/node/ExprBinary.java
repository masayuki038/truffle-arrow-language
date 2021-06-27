package net.wrap_trap.truffle_arrow.language.truffle.node;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;


@NodeChildren({ @NodeChild("leftNode"), @NodeChild("rightNode") })
public abstract class ExprBinary extends ExprBase {
}