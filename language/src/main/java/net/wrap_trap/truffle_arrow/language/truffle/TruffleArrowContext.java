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

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.dsl.NodeFactory;
import net.wrap_trap.truffle_arrow.language.truffle.node.builtins.ArraysFactory;
import net.wrap_trap.truffle_arrow.language.truffle.node.builtins.EchoFactory;
import net.wrap_trap.truffle_arrow.language.truffle.node.builtins.StoreFactory;
import net.wrap_trap.truffle_arrow.language.truffle.node.builtins.TruffleArrowBuiltin;

public class TruffleArrowContext {

  private final TruffleArrowFunctionRegistry functionRegistry;
  private TruffleArrowLanguage language;

  public TruffleArrowContext(TruffleArrowLanguage language) {
    this.language = language;
    this.functionRegistry = new TruffleArrowFunctionRegistry(language);
    installBuiltins();
  }

  public TruffleArrowFunctionRegistry getFunctionRegistry() {
    return functionRegistry;
  }

  private void installBuiltins() {
    installBuiltin(EchoFactory.getInstance());
    installBuiltin(StoreFactory.getInstance());
    installBuiltin(ArraysFactory.getInstance());
  }

  public void installBuiltin(NodeFactory<? extends TruffleArrowBuiltin> factory) {
    RootCallTarget target = language.lookupBuiltin(factory);
    String rootName = target.getRootNode().getName();
    getFunctionRegistry().register(rootName, target);
  }
}
