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

package net.wrap_trap.truffle_arrow.language.truffle;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import net.wrap_trap.truffle_arrow.language.ArrowFieldType;
import net.wrap_trap.truffle_arrow.language.truffle.node.SqlNull;
import org.apache.arrow.vector.VectorSchemaRoot;
import org.apache.arrow.vector.FieldVector;
import org.apache.arrow.vector.util.Text;


@ExportLibrary(InteropLibrary.class)
public class Row implements TruffleObject {

  final VectorSchemaRoot vectorSchemaRoot;
  final int rowIndex;

  public Row(int rowIndex, VectorSchemaRoot vectorSchemaRoot) {
    this.rowIndex = rowIndex;
    this.vectorSchemaRoot = vectorSchemaRoot;
  }

  @ExportMessage
  boolean hasArrayElements() {
    return true;
  }

  @ExportMessage
  @CompilerDirectives.TruffleBoundary
  Object readArrayElement(long index) {
    FieldVector fieldVector = this.vectorSchemaRoot.getFieldVectors().get((int) index);
    Object o = fieldVector.getObject(this.rowIndex);
    ArrowFieldType fieldType = ArrowFieldType.of(fieldVector.getField().getType());
    if (o == null) {
      return SqlNull.INSTANCE;
    } else if (o instanceof Text) {
      return o.toString();
    } else if (o instanceof Integer && fieldType == ArrowFieldType.TIME) {
      return ((Integer) o) * 1000;
    }
    return o;
  }

  @ExportMessage
  @CompilerDirectives.TruffleBoundary
  Object writeArrayElement(long index, Object value) throws UnsupportedMessageException, InvalidArrayIndexException {
    throw UnsupportedMessageException.create();
  }

  @ExportMessage
  @CompilerDirectives.TruffleBoundary
  long getArraySize() {
    return this.vectorSchemaRoot.getFieldVectors().size();
  }

  @ExportMessage(name = "isArrayElementReadable")
  @ExportMessage(name = "isArrayElementModifiable")
  @ExportMessage(name = "isArrayElementInsertable")
  boolean isArrayElementReadable(long index) {
    return index >= 0 && index < getArraySize();
  }
}
