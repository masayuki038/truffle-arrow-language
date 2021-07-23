For benchmarking
================
This project is for comparing to the performance between truffle-arrow-language and Java implementation. This includes a Java Implementation processing Apache Arrow data.

How to run
===========
```shell
java -cp /path/to/dir:/path/to/test-java-code.jar net.wrap_trap.truffle_arrow_language.test_java_code.GroupByRunner [/path/to/${*.java}] [/path/to/class-dir(javac -d)] [class name] [/path/to/${*.arrow}] [Column Number] [Repeat]
```

- Example
```shell
java -cp .:./0.2.0/test-java-code.jar net.wrap_trap.truffle_arrow_language.test_java_code.GroupByRunner ./GroupBy10.java . net.wrap_trap.truffle_arrow_language.test_java_code.GroupBy10 ./ontime_backup.arrow 4 10
```

- Sample Class

```java
package net.wrap_trap.truffle_arrow_language.test_java_code;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.*;
import org.apache.arrow.vector.ipc.ArrowFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroupBy10 implements GroupBy {

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
      v.close();
    });
  }
}
```