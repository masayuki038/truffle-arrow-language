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

package net.wrap_trap.truffle_arrow.language.truffle.node;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;
import net.wrap_trap.truffle_arrow.language.truffle.TruffleArrowContext;
import net.wrap_trap.truffle_arrow.language.truffle.TruffleArrowFunction;
import net.wrap_trap.truffle_arrow.language.truffle.TruffleArrowLanguage;

@NodeInfo(shortName = "func")
public final class FunctionLiteral extends ExprBase {

  private final String functionName;

  @CompilerDirectives.CompilationFinal
  private TruffleArrowFunction cachedFunction;

  @CompilerDirectives.CompilationFinal
  private TruffleArrowLanguage language;

  @CompilerDirectives.CompilationFinal
  private TruffleLanguage.ContextReference<TruffleArrowContext> contextRef;

  public FunctionLiteral(String functionName) {
    this.functionName = functionName;
  }

  @Override
  public TruffleArrowFunction executeGeneric(VirtualFrame frame) {
    TruffleLanguage.ContextReference<TruffleArrowContext> contextReference = contextRef;
    if (contextReference == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      contextReference = contextRef = lookupContextReference(TruffleArrowLanguage.class);
    }
    TruffleArrowLanguage l = language;
    if (l == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      l = language = lookupLanguageReference(TruffleArrowLanguage.class).get();
    }
    CompilerAsserts.partialEvaluationConstant(l);

    TruffleArrowFunction function = this.cachedFunction;
    if (function == null) {
      CompilerDirectives.transferToInterpreterAndInvalidate();
      this.cachedFunction = function = contextReference.get().getFunctionRegistry().lookup(functionName, true);
    }
    return function;
  }
}
