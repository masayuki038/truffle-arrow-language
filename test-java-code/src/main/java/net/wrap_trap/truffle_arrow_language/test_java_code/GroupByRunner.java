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
