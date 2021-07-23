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
import java.util.HashMap;
import java.util.Map;

public class GroupByRunner {
  public static void main(String[] args) throws ClassNotFoundException, IOException, InstantiationException, IllegalAccessException {
    new GroupByRunner().run(args[0], args[1], args[2], args[3], Integer.parseInt(args[4]), Integer.parseInt(args[5]));
  }

  public void run(String sourcePath, String classesDir, String classFqdn, String dataFilePath, int column, int num) throws ClassNotFoundException, IllegalAccessException, InstantiationException, IOException {
    File f = new File(sourcePath);
    JavaCompiler javac = ToolProvider.getSystemJavaCompiler();

    String[] args = {
      "-d", new File(classesDir).getAbsolutePath(),
      f.getAbsolutePath()
    };

    if (javac.run(null, null, null, args) != 0) {
      throw new RuntimeException("compilation Failed");
    }
    Class<?> clazz = Class.forName(classFqdn);
    GroupBy main = (GroupBy) clazz.newInstance();
    Map<Object, Integer> ret = new HashMap<>();
    for (int i = 0; i < num; i ++) {
      long start = System.currentTimeMillis();
      main.run(dataFilePath, column, ret);
      long end = System.currentTimeMillis();
      System.out.println((i + 1) + ": " + (end - start) + " ms");
    }
    System.out.println(ret);
  }
}
