package net.wrap_trap.truffle_arrow.language.truffle.node;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;

import net.wrap_trap.truffle_arrow.language.ArrowFieldType;
import net.wrap_trap.truffle_arrow.language.ArrowUtils;
import net.wrap_trap.truffle_arrow.language.truffle.node.type.ArrowTimeSec;
import org.apache.arrow.vector.FieldVector;
import org.apache.arrow.vector.VectorSchemaRoot;
import org.apache.arrow.vector.ipc.ArrowFileReader;


@NodeInfo(shortName = "loop")
public class StatementLoop extends StatementBase {

  @Child
  private ExprStringNode dirPath;

  @Child
  private Statements statements;

  public StatementLoop(ExprStringNode dirPath, Statements statements) {
    this.dirPath = dirPath;
    this.statements = statements;
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
          ArrowFieldType type = ArrowFieldType.of(fieldVector.getField().getFieldType().getType());
          switch (type) {
            case INT:
            case DATE:
              descriptor.setFrameSlotKind(slot, FrameSlotKind.Int);
              frame.setInt(slot, (int) value);
              break;
            case TIME:
              descriptor.setFrameSlotKind(slot, FrameSlotKind.Object);
              frame.setObject(slot, new ArrowTimeSec((int) value));
              break;
            case LONG:
            case TIMESTAMP:
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
        this.statements.executeVoid(frame);
      }
    }
  }
}