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
import com.oracle.truffle.api.nodes.NodeInfo;

import net.wrap_trap.truffle_arrow.language.ArrowUtils;
import net.wrap_trap.truffle_arrow.language.FieldType;
import net.wrap_trap.truffle_arrow.language.truffle.node.arrays.VectorSchemaRootContainerImpl;
import net.wrap_trap.truffle_arrow.language.truffle.node.type.FieldDef;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.vector.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@NodeInfo(shortName = "get")
public class ExprGet extends ExprBase {

  private ExprBase expr;
  private ExprBase orElse;

  public ExprGet(ExprBase expr, ExprBase orElse) {
    this.expr = expr;
    this.orElse = orElse;
  }

  @Override
  Object executeGeneric(VirtualFrame frame) {
    Object value = this.expr.executeGeneric(frame);
    if (value == null) {
      return this.orElse.executeGeneric(frame);
    }
    return value;
  }
}
