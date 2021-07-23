/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.wrap_trap.truffle_arrow.language.truffle;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.source.SourceSection;
import com.oracle.truffle.api.utilities.CyclicAssumption;
import com.oracle.truffle.api.utilities.TriState;

import java.util.logging.Level;

@ExportLibrary(InteropLibrary.class)
@SuppressWarnings("static-method")
public class TruffleArrowFunction implements TruffleObject {

  public static final int INLINE_CACHE_SIZE = 2;

  private static final TruffleLogger LOG = TruffleLogger.getLogger(TruffleArrowLanguage.ID, TruffleArrowFunction.class);

  private final String name;

  private RootCallTarget callTarget;

  private final CyclicAssumption callTargetStable;

  protected TruffleArrowFunction(RootCallTarget callTarget) {
    this.name = callTarget.getRootNode().getName();
    this.callTargetStable = new CyclicAssumption(name);
    setCallTarget(callTarget);
  }

  public String getName() {
    return name;
  }

  protected void setCallTarget(RootCallTarget callTarget) {
    boolean wasNull = this.callTarget == null;
    this.callTarget = callTarget;
    LOG.log(Level.FINE, "Installed call target for: {0}", name);
    if (!wasNull) {
      callTargetStable.invalidate();
    }
  }

  public RootCallTarget getCallTarget() {
    return callTarget;
  }

  public Assumption getCallTargetStable() {
    return callTargetStable.getAssumption();
  }

  @Override
  public String toString() {
    return name;
  }

  @ExportMessage
  boolean hasLanguage() {
    return true;
  }

  @ExportMessage
  Class<? extends TruffleLanguage<?>> getLanguage() {
    return TruffleArrowLanguage.class;
  }

  @SuppressWarnings("static-method")
  @ExportMessage
  @CompilerDirectives.TruffleBoundary
  SourceSection getSourceLocation() {
    return getCallTarget().getRootNode().getSourceSection();
  }

  @SuppressWarnings("static-method")
  @ExportMessage
  boolean hasSourceLocation() {
    return true;
  }

  @ExportMessage
  boolean isExecutable() {
    return true;
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
  @SuppressWarnings("unused")
  static final class IsIdenticalOrUndefined {
    @Specialization
    static TriState doTruffleArrowFunction(TruffleArrowFunction receiver, TruffleArrowFunction other) {
      return receiver == other ? TriState.TRUE : TriState.FALSE;
    }

    @Fallback
    static TriState doOther(TruffleArrowFunction receiver, Object other) {
      return TriState.UNDEFINED;
    }
  }

  @ExportMessage
  @CompilerDirectives.TruffleBoundary
  static int identityHashCode(TruffleArrowFunction receiver) {
    return System.identityHashCode(receiver);
  }

  @ExportMessage
  Object toDisplayString(@SuppressWarnings("unused") boolean allowSideEffects) {
    return name;
  }

  @ReportPolymorphism
  @ExportMessage
  abstract static class Execute {
    @Specialization(limit = "INLINE_CACHE_SIZE",
      guards = "function.getCallTarget() == cachedTarget",
      assumptions = "callTargetStable")
    @SuppressWarnings("unused")
    protected static Object doDirect(TruffleArrowFunction function, Object[] arguments,
                                     @Cached("function.getCallTargetStable()") Assumption callTargetStable,
                                     @Cached("function.getCallTarget()") RootCallTarget cachedTarget,
                                     @Cached("create(cachedTarget)") DirectCallNode callNode) {

      return callNode.call(arguments);
    }

    @Specialization(replaces = "doDirect")
    protected static Object doIndirect(TruffleArrowFunction function, Object[] arguments,
                                       @Cached IndirectCallNode callNode) {
      return callNode.call(function.getCallTarget(), arguments);
    }
  }
}
