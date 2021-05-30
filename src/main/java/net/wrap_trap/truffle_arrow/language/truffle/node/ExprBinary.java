package net.wrap_trap.truffle_arrow.language.truffle.node;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.UnexpectedResultException;

@NodeChildren({ @NodeChild("leftNode"), @NodeChild("rightNode") })
public abstract class ExprBinary extends ExprBase {
}