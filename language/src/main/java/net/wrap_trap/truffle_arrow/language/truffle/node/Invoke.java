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
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.instrumentation.Tag;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.NodeInfo;
import net.wrap_trap.truffle_arrow.language.truffle.node.type.ArrayWrapper;
import net.wrap_trap.truffle_arrow.language.truffle.node.type.MapWrapper;

import java.util.Map;

@NodeInfo(shortName = "invoke")
public final class Invoke extends ExprBase {

  @Child private ExprBase functionNode;
  @Children private final ExprBase[] argumentNodes;
  @Child private InteropLibrary library;

  public Invoke(ExprBase functionNode, ExprBase[] argumentNodes) {
    this.functionNode = functionNode;
    this.argumentNodes = argumentNodes;
    this.library = InteropLibrary.getFactory().createDispatched(3);
  }

  @ExplodeLoop
  @Override
  public Object executeGeneric(VirtualFrame frame) {
    Object function = functionNode.executeGeneric(frame);
    CompilerAsserts.compilationConstant(argumentNodes.length);

    Object[] argumentValues = new Object[argumentNodes.length];
    for (int i = 0; i < argumentNodes.length; i++) {
      Object arg = argumentNodes[i].executeGeneric(frame);
      if (arg instanceof Object[]) {
        arg = new ArrayWrapper((Object[]) arg);
      } else if (arg instanceof Map) {
        arg = new MapWrapper((Map) arg);
      }
      argumentValues[i] = arg;
    }

    try {
      return library.execute(function, argumentValues);
    } catch (ArityException | UnsupportedTypeException | UnsupportedMessageException e) {
      throw new IllegalArgumentException("Failed to execute: " + function);
    }
  }

  @Override
  public boolean hasTag(Class<? extends Tag> tag) {
    if (tag == StandardTags.CallTag.class) {
      return true;
    }
    return super.hasTag(tag);
  }
}
