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

import com.oracle.truffle.api.dsl.TypeSystemReference;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.instrumentation.Tag;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import net.wrap_trap.truffle_arrow.language.truffle.node.type.ArrowTimeSec;

/**
 * Base of all SQL expressions.
 */
@TypeSystemReference(SqlTypes.class)
@NodeInfo(description = "The abstract base node for all expressions")
public abstract class ExprBase extends StatementBase {
  /**
   * Compute the value of the expression. Called once for each expression in each row of a query.
   *
   * @param frame One row of data. Each FrameSlot corresponds to one column.
   * @return Result of evaluating the expression
   */
  public abstract Object executeGeneric(VirtualFrame frame);

  @Override
  public void executeVoid(VirtualFrame frame) {
    executeGeneric(frame);
  }

  public boolean executeBoolean(VirtualFrame frame) throws UnexpectedResultException {
    return SqlTypesGen.expectBoolean(executeGeneric(frame));
  }

  public long executeLong(VirtualFrame frame) throws UnexpectedResultException {
    return SqlTypesGen.expectLong(executeGeneric(frame));
  }

  public int executeInteger(VirtualFrame frame) throws UnexpectedResultException {
    return SqlTypesGen.expectInteger(executeGeneric(frame));
  }

  public double executeDouble(VirtualFrame frame) throws UnexpectedResultException {
    return SqlTypesGen.expectDouble(executeGeneric(frame));
  }

  public String executeString(VirtualFrame frame) throws UnexpectedResultException {
    return SqlTypesGen.expectString(executeGeneric(frame));
  }

  public ArrowTimeSec executeArrowTimeSec(VirtualFrame frame) throws UnexpectedResultException {
    return SqlTypesGen.expectArrowTimeSec(executeGeneric(frame));
  }

  @Override
  public boolean hasTag(Class<? extends Tag> tag) {
    return tag == StandardTags.ExpressionTag.class || super.hasTag(tag);
  }
}
