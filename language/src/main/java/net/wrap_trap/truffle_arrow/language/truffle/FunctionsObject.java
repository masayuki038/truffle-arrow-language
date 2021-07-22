package net.wrap_trap.truffle_arrow.language.truffle;

import java.util.HashMap;
import java.util.Map;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.profiles.BranchProfile;

@ExportLibrary(InteropLibrary.class)
@SuppressWarnings("static-method")
final class FunctionsObject implements TruffleObject {

  final Map<String, TruffleArrowFunction> functions = new HashMap<>();

  FunctionsObject() {
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
  boolean hasMembers() {
    return true;
  }

  @ExportMessage
  @TruffleBoundary
  Object readMember(String member) throws UnknownIdentifierException {
    Object value = functions.get(member);
    if (value != null) {
      return value;
    }
    throw UnknownIdentifierException.create(member);
  }

  @ExportMessage
  @TruffleBoundary
  boolean isMemberReadable(String member) {
    return functions.containsKey(member);
  }

  @ExportMessage
  @TruffleBoundary
  Object getMembers(@SuppressWarnings("unused") boolean includeInternal) {
    return new FunctionNamesObject(functions.keySet().toArray());
  }

  @ExportMessage
  boolean hasMetaObject() {
    return true;
  }

  @ExportMessage
  Object getMetaObject() {
    return null;
  }

  @ExportMessage
  boolean isScope() {
    return true;
  }

  @ExportMessage
  @TruffleBoundary
  Object toDisplayString(@SuppressWarnings("unused") boolean allowSideEffects) {
    return "global";
  }

  public static boolean isInstance(TruffleObject obj) {
    return obj instanceof FunctionsObject;
  }

  @ExportLibrary(InteropLibrary.class)
  static final class FunctionNamesObject implements TruffleObject {

    private final Object[] names;

    FunctionNamesObject(Object[] names) {
      this.names = names;
    }

    @ExportMessage
    boolean hasArrayElements() {
      return true;
    }

    @ExportMessage
    boolean isArrayElementReadable(long index) {
      return index >= 0 && index < names.length;
    }

    @ExportMessage
    long getArraySize() {
      return names.length;
    }

    @ExportMessage
    Object readArrayElement(long index, @Cached BranchProfile error) throws InvalidArrayIndexException {
      if (!isArrayElementReadable(index)) {
        error.enter();
        throw InvalidArrayIndexException.create(index);
      }
      return names[(int) index];
    }
  }
}