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
import net.wrap_trap.truffle_arrow.language.truffle.node.arrays.VectorSchemaRootContainer;

import java.util.List;
import java.util.stream.Collectors;


@NodeInfo(shortName = "echo")
public class StatementStore extends StatementBase {

  private List<ExprBase> variables;

  public StatementStore(List<ExprBase> variables) {
    this.variables = variables;
  }

  @Override
  public void executeVoid(VirtualFrame frame) {
    List<Object> values = this.variables.stream().map(v -> v.executeGeneric(frame)).collect(Collectors.toList());
    Object first = values.get(0);
    if (!(first instanceof VectorSchemaRootContainer)) {
      throw new IllegalArgumentException("The first parameter of 'store' should be specified a return value of 'arrays'");
    }
    VectorSchemaRootContainer vectorSchemaRoots = (VectorSchemaRootContainer) first;
    vectorSchemaRoots.addValues(values.subList(1, values.size()));
  }
}
