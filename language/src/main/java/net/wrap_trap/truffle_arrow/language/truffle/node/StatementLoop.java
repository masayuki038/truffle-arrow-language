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
import org.apache.arrow.vector.*;
import org.apache.arrow.vector.ipc.ArrowFileReader;


@NodeInfo(shortName = "loop")
public class StatementLoop extends StatementBase {

  @Child
  private ExprStringLiteral dirPath;

  @Child
  private Statements statements;

  public StatementLoop(ExprStringLiteral dirPath, Statements statements) {
    this.dirPath = dirPath;
    this.statements = statements;
  }

  @Override
  public void executeVoid(VirtualFrame frame) {
    String path = this.dirPath.executeString(frame);
    try {
      List<VectorSchemaRoot> loaded = this.loadArrowFile(path);
      loaded.stream().forEach(v -> this.loop(frame, v));
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

  protected void loop(VirtualFrame frame, VectorSchemaRoot vectorSchemaRoot) {
    List<FieldVector> fieldVectors = vectorSchemaRoot.getFieldVectors();
    FrameDescriptor descriptor = frame.getFrameDescriptor();
    int i;
    for (i = 0; i < vectorSchemaRoot.getRowCount(); i++) {
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
    }
  }
}