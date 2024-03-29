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
import org.apache.arrow.vector.VectorSchemaRoot;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;


@ExportLibrary(InteropLibrary.class)
public class Result implements TruffleObject {

  int size;
  Map<Long, VectorSchemaRoot> resultMap;

  public Result(List<VectorSchemaRoot> result) {
    resultMap = new TreeMap<>();
    size = 0;
    for (VectorSchemaRoot v : result) {
      int rowCount = v.getRowCount();
      if (rowCount > 0) {
        size += rowCount;
        resultMap.put(Long.valueOf(size), v);
      }
    }
  }

  @ExportMessage
  boolean hasArrayElements() {
    return true;
  }

  @ExportMessage
  @CompilerDirectives.TruffleBoundary
  Object readArrayElement(long index) throws UnsupportedMessageException, InvalidArrayIndexException {
    long offset = 0;
    for (long boundary : resultMap.keySet()) {
      if (index < boundary) {
        return new Row((int) (index - offset), resultMap.get(boundary));
      }
      offset = boundary;
    }
    throw InvalidArrayIndexException.create(index);
  }

  @ExportMessage
  @CompilerDirectives.TruffleBoundary
  Object writeArrayElement(long index, Object value) throws UnsupportedMessageException, InvalidArrayIndexException {
    throw UnsupportedMessageException.create();
  }

  @ExportMessage
  @CompilerDirectives.TruffleBoundary
  long getArraySize() {
    return size;
  }

  @ExportMessage(name = "isArrayElementReadable")
  @ExportMessage(name = "isArrayElementModifiable")
  @ExportMessage(name = "isArrayElementInsertable")
  boolean isArrayElementReadable(long index) {
    return index >= 0 && index < getArraySize();
  }
}

