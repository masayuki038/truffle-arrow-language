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

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;

public class GroupByRunner {
  public static void main(String[] args) throws ClassNotFoundException, IOException, InstantiationException, IllegalAccessException {
    new GroupByRunner().run(args[0], args[1], Integer.parseInt(args[2]));
  }

  public void run(String sourcePath, String dataFilePath, int column) throws ClassNotFoundException, IllegalAccessException, InstantiationException, IOException {
    File f = new File(sourcePath);
    JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
    if (javac.run(null, null, null, f.getAbsolutePath()) != 0) {
      throw new RuntimeException("compilation Failed");
    }
    Class<?> clazz = Class.forName("net.wrap_trap.truffle_arrow_language.test_java_code.GroupBy");
    GroupBy main = (GroupBy) clazz.newInstance();
    main.run(dataFilePath, column);
  }
}
