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

package net.wrap_trap.truffle_arrow.language.truffle.node.arrays;

import com.oracle.truffle.api.interop.TruffleObject;
import net.wrap_trap.truffle_arrow.language.ArrowFieldType;
import net.wrap_trap.truffle_arrow.language.truffle.node.SqlNull;
import net.wrap_trap.truffle_arrow.language.truffle.node.type.ArrowTimeSec;
import org.apache.arrow.vector.*;
import org.apache.arrow.vector.util.Text;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;


public class VectorSchemaRootContainerImpl
  implements VectorSchemaRootContainer, TruffleObject {

  private List<VectorSchemaRoot> vectorSchemaRoots;
  private List<FieldVector> fieldVectors;
  private int[] indices = new int[1];
  private int current = 0;

  public VectorSchemaRootContainerImpl(VectorSchemaRoot vectorSchemaRoot) {
    this.vectorSchemaRoots = Arrays.asList(vectorSchemaRoot);
    this.fieldVectors = vectorSchemaRoot.getFieldVectors();
  }

  @Override
  public void addValues(List<Object> values) {
    for (int i = 0; i < this.fieldVectors.size(); i ++) {
      addValue(values.get(i), this.fieldVectors.get(i), false);
    }
    this.indices[current] ++;
  }

  @Override
  public void addValues(Map<Object, Object> map) {
    for (Map.Entry<Object, Object> e: map.entrySet()) {
      addValue(e.getKey(), this.fieldVectors.get(0), true);
      addValue(e.getValue(), this.fieldVectors.get(1), false);
      this.indices[current] ++;
    }
  }

  private Object convert(Object input, Function<String, Object> converter) {
    if (input instanceof String) { // a key of DynamicObject
      String str = (String) input;
      if (str.equals(SqlNull.INSTANCE.toString())) {
        return SqlNull.INSTANCE;
      } else {
        return converter.apply(str);
      }
    }
    return input; // a key of Map
  }

  private void addValue(Object input, FieldVector fieldVector, boolean isKey) {
    int index = this.indices[current];
    ArrowFieldType type = ArrowFieldType.of(fieldVector.getField().getFieldType().getType());
    Object value;
    switch (type) {
      case INT:
        if (isKey) {
          value = convert(input, Integer::parseInt);
        } else {
          value = input;
        }
        IntVector intVector = (IntVector) fieldVector;
        if (!(value instanceof SqlNull)) {
          intVector.set(index, (int) value);
        } else {
          intVector.setNull(index);
        }
        break;
      case DATE:
        if (isKey) {
          value = convert(input, Integer::parseInt);
        } else {
          value = input;
        }
        DateDayVector dateDayVector = (DateDayVector) fieldVector;
        if (!(value instanceof SqlNull)) {
          dateDayVector.set(index, (int) value);
        } else {
          dateDayVector.setNull(index);
        }
        break;
      case TIME:
        if (isKey) {
         throw new UnsupportedOperationException("Can't specify an ArrowTimeSec as key");
        } else {
          value = input;
        }
        TimeSecVector timeSecVector = (TimeSecVector) fieldVector;
        if (!(value instanceof SqlNull)) {
          timeSecVector.set(index, ((ArrowTimeSec) value).timeSec());
        } else {
          timeSecVector.setNull(index);
        }
        break;
      case TIMESTAMP:
        if (isKey) {
          value = convert(input, Long::parseLong);
        } else {
          value = input;
        }
        TimeStampSecTZVector timezoneVector = (TimeStampSecTZVector) fieldVector;
        if (!(value instanceof SqlNull)) {
          timezoneVector.set(index, (long) value);
        } else {
          timezoneVector.setNull(index);
        }
        break;
      case LONG:
        if (isKey) {
          value = convert(input, Long::parseLong);
        } else {
          value = input;
        }
        BigIntVector bigIntVector = (BigIntVector) fieldVector;
        if (!(value instanceof SqlNull)) {
          bigIntVector.set(index, (long) value);
        } else {
          bigIntVector.setNull(index);
        }
        break;
      case DOUBLE:
        if (isKey) {
          value = convert(input, Double::parseDouble);
        } else {
          value = input;
        }
        Float8Vector float8Vector = (Float8Vector) fieldVector;
        if (!(value instanceof SqlNull)) {
          float8Vector.set(index, (double) value);
        } else {
          float8Vector.setNull(index);
        }
        break;
      case STRING:
        value = input;
        VarCharVector varCharVector = (VarCharVector) fieldVector;
        if (!(value instanceof SqlNull)) {
          varCharVector.set(index, new Text((String) value));
        } else {
          varCharVector.setNull(index);
        }
        break;
      default:
        throw new IllegalArgumentException("Unexpected ArrowFieldType: " + type);
    }
  }

  @Override
  public List<VectorSchemaRoot> getVectorSchemaRoots() {
    return this.vectorSchemaRoots;
  }

  @Override
  public void setRowCounts() {
    for (int i = 0; i < this.vectorSchemaRoots.size(); i ++) {
      this.vectorSchemaRoots.get(i).setRowCount(this.indices[i]);
    }
  }
}
