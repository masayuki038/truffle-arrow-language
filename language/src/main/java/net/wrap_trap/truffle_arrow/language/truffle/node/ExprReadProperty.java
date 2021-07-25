package net.wrap_trap.truffle_arrow.language.truffle.node;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.NodeInfo;

@NodeInfo(shortName = ".")
@NodeChild("receiverNode")
@NodeChild("nameNode")
public abstract class ExprReadProperty extends ExprBase {

  @Specialization(guards = "objects.hasMembers(receiver)", limit = "3")
  protected Object readObject(Object receiver, String name,
                              @CachedLibrary("receiver") InteropLibrary objects) {
    try {
      return objects.readMember(receiver, name);
    } catch (UnsupportedMessageException | UnknownIdentifierException e) {
      throw new UnsupportedOperationException(e);
    }
  }
}