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
import com.oracle.truffle.api.instrumentation.InstrumentableNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.source.SourceSection;
import net.wrap_trap.truffle_arrow.language.truffle.node.ExprReadLocal;
import net.wrap_trap.truffle_arrow.language.truffle.node.ReturnException;
import net.wrap_trap.truffle_arrow.language.truffle.node.StatementBase;
import net.wrap_trap.truffle_arrow.language.truffle.node.Statements;
import net.wrap_trap.truffle_arrow.language.truffle.node.arrays.VectorSchemaRootContainer;


public class TruffleArrowRootNode extends RootNode {

  @Children
  private StatementBase[] statements;

  private final SourceSection sourceSection;

  private StatementBase current;

  public TruffleArrowRootNode(TruffleLanguage<?> language, FrameDescriptor frameDescriptor, SourceSection sourceSection, Statements statements) {
    super(language, frameDescriptor);
    this.statements = statements.getStatements();
    this.sourceSection = sourceSection;
  }

  @Override
  protected boolean isInstrumentable() {
    return true;
  }

  @Override
  public Object execute(VirtualFrame frame) {
    try {
      for (StatementBase statement: this.statements) {

        this.current = statement;
        statement.executeVoid(frame);
      }
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

  @Override
  public SourceSection getSourceSection() {
    return sourceSection;
  }

  @Override
  public String getName() {
    if (this.current instanceof InstrumentableNode.WrapperNode) {
      Node delegateNode = ((InstrumentableNode.WrapperNode) this.current).getDelegateNode();
      NodeInfo nodeInfo = delegateNode.getClass().getAnnotation(NodeInfo.class);
      if (nodeInfo != null) {
        return nodeInfo.shortName();
      }
    }
    return super.getName();
  }
}
