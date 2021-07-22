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


@NodeInfo(shortName = "arrays")
public abstract class ExprArrays extends ExprBase {

  private static final int INIT_ROW_COUNT = 1024;

  private ExprNewArrayLiteral fieldDefs;

  public ExprArrays(ExprNewArrayLiteral fieldDefs) {
    this.fieldDefs = fieldDefs;
  }

//  @Override
//  public Object executeGeneric(VirtualFrame frame) {
//    List<FieldDef> fieldDefs = this.fieldDefs.stream().map(
//      field -> (FieldDef) field.executeGeneric(frame)).collect(Collectors.toList());
//
//    Map<String, FieldVector> fieldVectorMap = new HashMap<>();
//    VectorSchemaRoot out = createVectorSchemaRoot(
//      fieldDefs, ArrowUtils.createAllocator("out"), INIT_ROW_COUNT);
//    for (FieldVector fieldVector: out.getFieldVectors()) {
//      fieldVectorMap.put(fieldVector.getField().getName(), fieldVector);
//    }
//    return new VectorSchemaRootContainerImpl(out);
//  }
//
//  public static VectorSchemaRoot createVectorSchemaRoot(List<FieldDef> fields, BufferAllocator allocator, int initialCapacity) {
//    List<FieldVector> fieldVectors = new ArrayList<>();
//    fields.stream().forEach(field -> {
//      String name = field.getName();
//      FieldType type = field.getType();
//      FieldVector fieldVector;
//      // TODO handle DATE / TIME / TIMESTAMP
//      switch (type) {
//        case INT:
//          fieldVector = new IntVector(name, allocator);
//          break;
//        case BIGINT:
//          fieldVector = new BigIntVector(name, allocator);
//          break;
//        case DOUBLE:
//          fieldVector = new Float8Vector(name, allocator);
//          break;
//        case STRING:
//          fieldVector = new VarCharVector(name, allocator);
//          break;
//        default:
//          throw new IllegalArgumentException(
//            "Unexpected field type. field: %s, type: %s".format(name, type));
//      }
//      fieldVector.setInitialCapacity(initialCapacity);
//      fieldVector.allocateNew();
//      fieldVectors.add(fieldVector);
//    });
//    return new VectorSchemaRoot(fieldVectors);
//  }
}
