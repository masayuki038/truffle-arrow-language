package net.wrap_trap.truffle_arrow.language.truffle.node.arrays;

import net.wrap_trap.truffle_arrow.language.ArrowFieldType;
import net.wrap_trap.truffle_arrow.language.truffle.node.SqlNull;
import net.wrap_trap.truffle_arrow.language.truffle.node.type.ArrowTimeSec;
import org.apache.arrow.vector.*;
import org.apache.arrow.vector.util.Text;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class VectorSchemaRootContainerImpl implements VectorSchemaRootContainer {

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
    if (values.size() == 1 && (values.get(0) instanceof Map)) {
      addValuesAsMap((Map<Object, Object>) values.get(0));
    } else {
      addValuesAsList(values);
    }
  }

  private void addValuesAsMap(Map<Object, Object> map) {
    if (this.fieldVectors.size() != 2) {
      throw new IllegalArgumentException("Invalid fields on storing from map: " + this.fieldVectors);
    }
    for (Map.Entry<Object, Object> e: map.entrySet()) {
      addValue(e.getKey(), this.fieldVectors.get(0));
      addValue(e.getValue(), this.fieldVectors.get(1));
      this.indices[current] ++;
    }
  }

  private void addValuesAsList(List<Object> values) {
    for (int i = 0; i < this.fieldVectors.size(); i ++) {
      addValue(values.get(i), this.fieldVectors.get(i));
    }
    this.indices[current] ++;
  }

  private void addValue(Object value, FieldVector fieldVector) {
    int index = this.indices[current];
    ArrowFieldType type = ArrowFieldType.of(fieldVector.getField().getFieldType().getType());
    switch (type) {
      case INT:
        IntVector intVector = (IntVector) fieldVector;
        if (!(value instanceof SqlNull)) {
          intVector.set(index, (int) value);
        } else {
          intVector.setNull(index);
        }
        break;
      case DATE:
        DateDayVector dateDayVector = (DateDayVector) fieldVector;
        if (!(value instanceof SqlNull)) {
          dateDayVector.set(index, (int) value);
        } else {
          dateDayVector.setNull(index);
        }
        break;
      case TIME:
        TimeSecVector timeSecVector = (TimeSecVector) fieldVector;
        if (!(value instanceof SqlNull)) {
          timeSecVector.set(index, ((ArrowTimeSec) value).timeSec());
        } else {
          timeSecVector.setNull(index);
        }
        break;
      case TIMESTAMP:
        TimeStampSecTZVector timezoneVector = (TimeStampSecTZVector) fieldVector;
        if (!(value instanceof SqlNull)) {
          timezoneVector.set(index, (long) value);
        } else {
          timezoneVector.setNull(index);
        }
        break;
      case LONG:
        BigIntVector bigIntVector = (BigIntVector) fieldVector;
        if (!(value instanceof SqlNull)) {
          bigIntVector.set(index, (long) value);
        } else {
          bigIntVector.setNull(index);
        }
        break;
      case DOUBLE:
        Float8Vector float8Vector = (Float8Vector) fieldVector;
        if (!(value instanceof SqlNull)) {
          float8Vector.set(index, (double) value);
        } else {
          float8Vector.setNull(index);
        }
        break;
      case STRING:
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
