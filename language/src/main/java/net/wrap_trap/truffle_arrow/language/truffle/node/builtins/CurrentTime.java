package net.wrap_trap.truffle_arrow.language.truffle.node.builtins;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.NodeInfo;
import net.wrap_trap.truffle_arrow.language.truffle.TruffleArrowContext;
import net.wrap_trap.truffle_arrow.language.truffle.TruffleArrowLanguage;

@NodeInfo(shortName = "current_time")
public abstract class CurrentTime extends TruffleArrowBuiltin {
  @Specialization
  @CompilerDirectives.TruffleBoundary
  public Long currentTime(Object value,
                     @CachedLibrary(limit = "3") InteropLibrary interop,
                     @CachedContext(TruffleArrowLanguage.class) TruffleArrowContext context) {
    return System.currentTimeMillis();
  }
}
