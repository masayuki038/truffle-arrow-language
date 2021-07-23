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

package net.wrap_trap.truffle_arrow_language.test_java_code;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.arrow.vector.*;

public class GroupBy1 implements GroupBy {

  public void run(String path, int column, Map<Object, Integer> ret) throws IOException {
    ArrowUtils.loadArrowFile(path).stream().forEach(v -> {
      FieldVector fieldVector = v.getFieldVectors().get(column);
      for (int i = 0; i < v.getRowCount(); i ++) {
        Object key = fieldVector.getObject(i);
        if (ret.containsKey(key)) {
          ret.put(key, ret.get(key) + 1);
        } else {
          ret.put(key, 1);
        }
      }
    });
  }

  public static void main(String[] args) throws IOException {
    Map<Object, Integer> ret = new HashMap<>();
    new GroupBy1().run("D:\\tmp\\truffle-arrow\\ontime_backup.arrow", 4, ret);
    System.out.println(ret);
  }
}
