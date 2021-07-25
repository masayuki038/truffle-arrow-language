package net.wrap_trap.truffle_arrow.language.truffle;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

@ExportLibrary(InteropLibrary.class)
@SuppressWarnings("static-method")
public final class TruffleArrowType implements TruffleObject {
  public static final TruffleArrowType OBJECT = new TruffleArrowType("Object", (l, v) -> l.hasMembers(v));

  @CompilerDirectives.CompilationFinal(dimensions = 1) public static final TruffleArrowType[] PRECEDENCE = new TruffleArrowType[]{OBJECT};

  private final String name;
  private final TypeCheck isInstance;

  private TruffleArrowType(String name, TypeCheck isInstance) {
    this.name = name;
    this.isInstance = isInstance;
  }

  public boolean isInstance(Object value, InteropLibrary interop) {
    CompilerAsserts.partialEvaluationConstant(this);
    return isInstance.check(interop, value);
  }

  @ExportMessage
  boolean hasLanguage() {
    return true;
  }

  @ExportMessage
  Class<? extends TruffleLanguage<?>> getLanguage() {
    return TruffleArrowLanguage.class;
  }

  @ExportMessage
  boolean isMetaObject() {
    return true;
  }

  @ExportMessage(name = "getMetaQualifiedName")
  @ExportMessage(name = "getMetaSimpleName")
  public Object getName() {
    return name;
  }

  @ExportMessage(name = "toDisplayString")
  Object toDisplayString(@SuppressWarnings("unused") boolean allowSideEffects) {
    return name;
  }

  @Override
  public String toString() {
    return "SLType[" + name + "]";
  }

  @ExportMessage
  static class IsMetaInstance {

    @Specialization(guards = "type == cachedType", limit = "3")
    static boolean doCached(@SuppressWarnings("unused") TruffleArrowType type, Object value,
                            @Cached("type") TruffleArrowType cachedType,
                            @CachedLibrary("value") InteropLibrary valueLib) {
      return cachedType.isInstance.check(valueLib, value);
    }

    @CompilerDirectives.TruffleBoundary
    @Specialization(replaces = "doCached")
    static boolean doGeneric(TruffleArrowType type, Object value) {
      return type.isInstance.check(InteropLibrary.getFactory().getUncached(), value);
    }
  }

  @FunctionalInterface
  interface TypeCheck {
    boolean check(InteropLibrary lib, Object value);
  }
}