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

import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.dsl.UnsupportedSpecializationException;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RootNode;
import net.wrap_trap.truffle_arrow.language.truffle.node.ExprReadLocal;
import net.wrap_trap.truffle_arrow.language.truffle.node.ReturnException;
import net.wrap_trap.truffle_arrow.language.truffle.node.Statements;
import net.wrap_trap.truffle_arrow.language.truffle.node.arrays.VectorSchemaRootContainer;


public class TruffleArrowRootNode extends RootNode {

  @Child
  private Statements statements;

  public TruffleArrowRootNode(TruffleLanguage<?> language, FrameDescriptor frameDescriptor, Statements statements) {
    super(language, frameDescriptor);
    this.statements = statements;
  }

  @Override
  public Object execute(VirtualFrame frame) {
    try {
      statements.executeVoid(frame);
    } catch (ReturnException e) {
      Object result = e.getResult();
      if (result instanceof VectorSchemaRootContainer) {
        VectorSchemaRootContainer container = (VectorSchemaRootContainer) result;
        container.setRowCounts();
        return new Result(container.getVectorSchemaRoots());
      }
      return e.getResult();
    } catch (UnsupportedSpecializationException e) {
      Node caused = e.getNode();
      if (caused instanceof ExprReadLocal) {
        throw new LocalVariableNotFoundException((ExprReadLocal) caused);
      }
      throw e;
    }
    return true;
  }
}
