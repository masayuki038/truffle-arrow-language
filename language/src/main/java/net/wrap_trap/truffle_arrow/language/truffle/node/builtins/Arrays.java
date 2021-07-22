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

package net.wrap_trap.truffle_arrow.language.truffle.node.builtins;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.NodeInfo;
import net.wrap_trap.truffle_arrow.language.ArrowUtils;
import net.wrap_trap.truffle_arrow.language.FieldType;
import net.wrap_trap.truffle_arrow.language.truffle.TruffleArrowContext;
import net.wrap_trap.truffle_arrow.language.truffle.TruffleArrowLanguage;
import net.wrap_trap.truffle_arrow.language.truffle.node.arrays.VectorSchemaRootContainerImpl;
import net.wrap_trap.truffle_arrow.language.truffle.node.type.ArrayWrapper;
import net.wrap_trap.truffle_arrow.language.truffle.node.type.FieldDef;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.vector.*;
import org.jparsec.internal.util.Lists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NodeInfo(shortName = "arrays")
public abstract class Arrays extends TruffleArrowBuiltin {

  private static final int INIT_ROW_COUNT = 1024;

  @Specialization
  @CompilerDirectives.TruffleBoundary
  public Object echo(ArrayWrapper arrayWrapper,
                     @CachedLibrary(limit = "3") InteropLibrary interop,
                     @CachedContext(TruffleArrowLanguage.class) TruffleArrowContext context) {

    List<FieldDef> fieldDefs = Lists.arrayList();
    for (Object o: arrayWrapper.getArray()) {
      if (!(o instanceof FieldDef)) {
        throw new IllegalArgumentException("Expected FieldDef. But specified: " + o);
      }
      fieldDefs.add((FieldDef) o);
    }

    Map<String, FieldVector> fieldVectorMap = new HashMap<>();
    VectorSchemaRoot out = createVectorSchemaRoot(
      fieldDefs, ArrowUtils.createAllocator("out"), INIT_ROW_COUNT);
    for (FieldVector fieldVector: out.getFieldVectors()) {
      fieldVectorMap.put(fieldVector.getField().getName(), fieldVector);
    }
    return new VectorSchemaRootContainerImpl(out);
  }

  public static VectorSchemaRoot createVectorSchemaRoot(List<FieldDef> fieldDefs, BufferAllocator allocator, int initialCapacity) {
    List<FieldVector> fieldVectors = new ArrayList<>();
    for(FieldDef field: fieldDefs) {
      String name = field.getName();
      FieldType type = field.getType();
      FieldVector fieldVector;
      // TODO handle DATE / TIME / TIMESTAMP
      switch (type) {
        case INT:
          fieldVector = new IntVector(name, allocator);
          break;
        case BIGINT:
          fieldVector = new BigIntVector(name, allocator);
          break;
        case DOUBLE:
          fieldVector = new Float8Vector(name, allocator);
          break;
        case STRING:
          fieldVector = new VarCharVector(name, allocator);
          break;
        default:
          throw new IllegalArgumentException(
            "Unexpected field type. field: %s, type: %s".format(name, type));
      }
      fieldVector.setInitialCapacity(initialCapacity);
      fieldVector.allocateNew();
      fieldVectors.add(fieldVector);
    }
    return new VectorSchemaRoot(fieldVectors);
  }
}
