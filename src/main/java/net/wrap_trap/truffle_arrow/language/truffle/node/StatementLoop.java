package net.wrap_trap.truffle_arrow.language.truffle.node;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;

import net.wrap_trap.truffle_arrow.language.ArrowFieldType;
import net.wrap_trap.truffle_arrow.language.ArrowUtils;
import net.wrap_trap.truffle_arrow.language.truffle.node.type.ArrowTimeSec;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.vector.*;
import org.apache.arrow.vector.ipc.ArrowFileReader;
import org.apache.arrow.vector.util.Text;


@NodeInfo(shortName = "loop")
public class StatementLoop extends StatementBase {

  @Child
  private ExprStringNode dirPath;

  @Child
  private Statements statements;

  private List<ExprStringNode> fields;

  public StatementLoop(ExprStringNode dirPath, Statements statements, List<ExprStringNode> fields) {
    this.dirPath = dirPath;
    this.statements = statements;
    this.fields = fields;
  }

  @Override
  public void executeVoid(VirtualFrame frame) {
    String path = this.dirPath.executeString(frame);
    try {
      List<VectorSchemaRoot> vectorSchemaRoots = this.loadArrowFile(path);
      for (VectorSchemaRoot vectorSchemaRoot: vectorSchemaRoots) {
        this.loop(frame, frame.getFrameDescriptor(), vectorSchemaRoot);
      }
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  private List<VectorSchemaRoot> loadArrowFile(String path) throws IOException {
    try (FileInputStream fileInputStream = new FileInputStream(path)) {
      // TODO close VectorSchemaRoot
      ArrowFileReader reader = new ArrowFileReader(fileInputStream.getChannel(), ArrowUtils.createAllocator("loadArrowFile"));
      return reader.getRecordBlocks().stream().map(block -> {
        try {
          if (!reader.loadRecordBatch(block)) {
            throw new IllegalStateException("Failed to load RecordBatch");
          }
          return reader.getVectorSchemaRoot();
        } catch (IOException e) {
          throw new IllegalStateException(e);
        }
      }).collect(Collectors.toList());
    }
  }

  protected void loop(VirtualFrame frame, FrameDescriptor descriptor, VectorSchemaRoot vectorSchemaRoot) {

    List<FieldVector> fieldVectors = vectorSchemaRoot.getFieldVectors();
    if (fieldVectors.size() == 0) {
      return;
    }

    List<String> fieldNames = this.fields.stream().map(
      field -> field.executeString(frame)).collect(Collectors.toList());

    VectorSchemaRoot out = null;
    Map<String, FieldVector> fieldVectorMap = new HashMap<>();

    for (int i = 0; i < fieldVectors.get(0).getValueCount(); i++) {
      for (int j = 0; j < fieldVectors.size(); j++) {
        FieldVector fieldVector = fieldVectors.get(j);
        FrameSlot slot = descriptor.findFrameSlot(fieldVector.getName());
        if (slot == null) {
          continue;
        }
        Object value = fieldVector.getObject(i);
        if (value == null) {
          descriptor.setFrameSlotKind(slot, FrameSlotKind.Object);
          frame.setObject(slot, SqlNull.INSTANCE);
        } else {
          // TODO handle DATE / TIME / TIMESTAMP
          ArrowFieldType type = ArrowFieldType.of(fieldVector.getField().getFieldType().getType());
          switch (type) {
            case INT:
                descriptor.setFrameSlotKind(slot, FrameSlotKind.Int);
              frame.setInt(slot, (int) value);
              break;
            case LONG:
              descriptor.setFrameSlotKind(slot, FrameSlotKind.Long);
              frame.setLong(slot, (long) value);
              break;
            case DOUBLE:
              descriptor.setFrameSlotKind(slot, FrameSlotKind.Double);
              frame.setDouble(slot, (double) value);
              break;
            case STRING:
              descriptor.setFrameSlotKind(slot, FrameSlotKind.Object);
              frame.setObject(slot, value);
              break;
            default:
              throw new IllegalArgumentException("Unexpected ArrowFieldType:" + type);
          }
        }
      }
      this.statements.executeVoid(frame);
      if (out == null) {
        out = createVectorSchemaRoot(descriptor, fieldNames, ArrowUtils.createAllocator("out"));
        for (FieldVector fieldVector: out.getFieldVectors()) {
          fieldVectorMap.put(fieldVector.getField().getName(), fieldVector);
        }
      }
      setValues(frame, descriptor, fieldVectorMap, fieldNames, i);
    }
  }

  public static VectorSchemaRoot createVectorSchemaRoot(FrameDescriptor descriptor, List<String> fields, BufferAllocator allocator) {
    List<FieldVector> fieldVectors = new ArrayList<>();
    fields.stream().forEach(name -> {
      FrameSlot slot = descriptor.findFrameSlot(name);
      if (slot == null) {
        throw new IllegalArgumentException("FrameSlot not found: " + name);
      }
      FieldVector fieldVector;
      // TODO handle DATE / TIME / TIMESTAMP
      FrameSlotKind kind = descriptor.getFrameSlotKind(slot);
      switch (kind) {
        case Int:
          fieldVector = new IntVector(name, allocator);
          break;
        case Long:
          fieldVector = new BigIntVector(name, allocator);
          break;
        case Double:
          fieldVector = new Float8Vector(name, allocator);
          break;
        case Object:
          fieldVector = new VarCharVector(name, allocator);
          break;
        default:
          throw new IllegalArgumentException(
            "Unexpected FrameSlotKind. field: %s, FrameSlotKind: %s".format(name, kind));
      }
      fieldVector.allocateNew();
      fieldVectors.add(fieldVector);
    });
    return new VectorSchemaRoot(fieldVectors);
  }

  public static void setValues(VirtualFrame frame, FrameDescriptor descriptor, Map<String, FieldVector> fieldVectorMap, List<String> fields, int index) {

    for(String field: fields) {
      FrameSlot slot = descriptor.findFrameSlot(field);
      if (slot == null) {
        throw new IllegalStateException("Field not found: " + field);
      }
      FieldVector fieldVector = fieldVectorMap.get(field);
      if (fieldVector == null) {
        throw new IllegalStateException("FieldVector not found: " + field);
      }
      ArrowFieldType type = ArrowFieldType.of(fieldVector.getField().getFieldType().getType());
      Object value = frame.getValue(slot);
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
  }
}