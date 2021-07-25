package net.wrap_trap.truffle_arrow.language.truffle.node;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.NodeInfo;

@NodeInfo
@NodeChild("receiver")
@NodeChild("name")
@NodeChild("value")
public abstract class StatementWriteProperty extends ExprBase {

  @Specialization(limit = "3")
  protected Object write(Object receiver, String name, Object value,
                         @CachedLibrary("receiver") InteropLibrary objectLibrary) {
    try {
      objectLibrary.writeMember(receiver, name, value);
    } catch (UnsupportedMessageException | UnknownIdentifierException | UnsupportedTypeException e) {
      throw new UnsupportedOperationException(e);
    }
    return value;
  }
}
