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
import org.jparsec.internal.util.Lists;


@NodeInfo(shortName = "loop")
public class StatementLoad extends StatementBase {

  @Child
  private ExprStringLiteral dirPath;

  @Child
  private Statements statements;

  public StatementLoad(ExprStringLiteral dirPath, Statements statements) {
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
    FrameDescriptor descriptor = frame.getFrameDescriptor();

    List<FieldVector> fieldVectors = Lists.arrayList();
    for (FieldVector fieldVector: vectorSchemaRoot.getFieldVectors()) {
      if (descriptor.findFrameSlot(fieldVector.getName()) != null) {
        fieldVectors.add(fieldVector);
      }
    }

    int i;
    for (i = 0; i < vectorSchemaRoot.getRowCount(); i++) {
      for (int j = 0; j < fieldVectors.size(); j++) {
        FieldVector fieldVector = fieldVectors.get(j);
        FrameSlot slot = descriptor.findFrameSlot(fieldVector.getName());
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