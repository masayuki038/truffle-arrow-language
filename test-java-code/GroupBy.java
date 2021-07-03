package net.wrap_trap.truffle_arrow_language.test_java_code;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.arrow.vector.*;

public class GroupBy {

  public void run(String path, int column) throws IOException {
    Map<Object, Integer> ret = new HashMap<>();

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
    System.out.println(ret);
  }

  public static void main(String[] args) throws IOException {
    new GroupBy().run("D:\\tmp\\truffle-arrow\\ontime_backup.arrow", 4);
  }
}
