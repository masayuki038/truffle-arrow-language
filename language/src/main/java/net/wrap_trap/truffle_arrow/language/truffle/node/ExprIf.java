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

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.oracle.truffle.api.profiles.ConditionProfile;


@NodeInfo(shortName = "if")
public final class ExprIf extends ExprBase {

  @Node.Child
  private ExprBase conditionNode;

  @Node.Child
  private Statements thenPart;

  @Node.Child
  private Statements elsePart;

  private final ConditionProfile condition = ConditionProfile.createCountingProfile();

  public ExprIf(ExprBase conditionNode, Statements thenPart, Statements elsePart) {
    this.conditionNode = conditionNode;
    this.thenPart = thenPart;
    this.elsePart = elsePart;
  }

  @Override
  public Object executeGeneric(VirtualFrame frame) {
    if (condition.profile(evaluateCondition(frame))) {
      this.thenPart.executeVoid(frame);
    } else if (this.elsePart != null) {
      this.elsePart.executeVoid(frame);
    }
    return null;
  }

  private boolean evaluateCondition(VirtualFrame frame) {
    try {
      return conditionNode.executeBoolean(frame);
    } catch (UnexpectedResultException ex) {
      throw new IllegalStateException(ex);
    }
  }
}
