package net.wrap_trap.truffle_arrow.language.truffle;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.interop.TruffleObject;

import java.util.*;

public class TruffleArrowFunctionRegistry {

  private final TruffleArrowLanguage language;
  private final FunctionsObject functionsObject = new FunctionsObject();
  private final Map<Map<String, RootCallTarget>, Void> registeredFunctions = new IdentityHashMap<>();

  public TruffleArrowFunctionRegistry(TruffleArrowLanguage language) {
    this.language = language;
  }

  @CompilerDirectives.TruffleBoundary
  public TruffleArrowFunction lookup(String name, boolean createIfNotPresent) {
    return functionsObject.functions.get(name);
  }

  TruffleArrowFunction register(String name, RootCallTarget callTarget) {
    TruffleArrowFunction result = functionsObject.functions.get(name);
    if (result == null) {
      result = new TruffleArrowFunction(callTarget);
      functionsObject.functions.put(name, result);
    } else {
      result.setCallTarget(callTarget);
    }
    return result;
  }

  @CompilerDirectives.TruffleBoundary
  public void register(Map<String, RootCallTarget> newFunctions) {
    if (registeredFunctions.containsKey(newFunctions)) {
      return;
    }
    for (Map.Entry<String, RootCallTarget> entry : newFunctions.entrySet()) {
      register(entry.getKey(), entry.getValue());
    }
    registeredFunctions.put(newFunctions, null);
  }

  public TruffleArrowFunction getFunction(String name) {
    return functionsObject.functions.get(name);
  }

  /**
   * Returns the sorted list of all functions, for printing purposes only.
   */
  public List<TruffleArrowFunction> getFunctions() {
    List<TruffleArrowFunction> result = new ArrayList<>(functionsObject.functions.values());
    Collections.sort(result, new Comparator<TruffleArrowFunction>() {
      public int compare(TruffleArrowFunction f1, TruffleArrowFunction f2) {
        return f1.toString().compareTo(f2.toString());
      }
    });
    return result;
  }

  public TruffleObject getFunctionsObject() {
    return functionsObject;
  }
}
