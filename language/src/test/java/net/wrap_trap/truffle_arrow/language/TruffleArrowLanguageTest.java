package net.wrap_trap.truffle_arrow.language;

import net.wrap_trap.truffle_arrow.language.truffle.Row;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class TruffleArrowLanguageTest {

  @BeforeClass
  public static void setupOnce() throws IOException {
    TestUtils.generateTestFile("target/all_fields.arrow", TestDataType.CASE4);
  }

  @AfterClass
  public static void teardownOnce() {
    new File("target/all_fields.arrow").delete();
  }

  @Test
  public void testScript() {
    String SAMPLE =
      "$a = 1;\n" +
      "$b = 2;\n" +
      "return $a + $b;";
    Context ctx = Context.create("ta");
    assertThat(ctx.eval("ta", SAMPLE).asDouble(), is(3d));
  }

  @Test
  public void testPushedVariables() throws IOException {
    String script =
      "$ret = loop (\"target/all_fields.arrow\") {\n" +
      "  echo $F_INT;\n" +
      "  echo $F_BIGINT;\n" +
      "  $F_BIGINT;\n" +
      "} yield (F_INT:INT, F_BIGINT:BIGINT);\n" +
      "echo $ret;" +
      "return $ret;";
    Context ctx = Context.create("ta");
    List<List<Object>> rows = ctx.eval("ta", script).as(List.class);
    assertThat(rows.size(), is(10));
    for (int i = 0; i < 10; i ++) {
      List<Object> row = rows.get(i);
      assertThat(row.get(0), is(i)); // F_INT
      assertThat(row.get(1), is(10L - i)); // F_BIGINT
    }
  }

  @Test
  public void testAddNewVariableToOutputs() {
    String script =
      "return loop (\"target/all_fields.arrow\") {\n" +
        "  echo $F_BIGINT;\n" +
        "  $a = \"hoge\";\n" +
        "  $b = 1;\n" +
        "} yield (F_BIGINT:BIGINT, a:STRING);\n";
    Context ctx = Context.create("ta");
    List<List<Object>> rows = ctx.eval("ta", script).as(List.class);
    for (int i = 0; i < 10; i ++) {
      List<Object> row = rows.get(i);
      assertThat(row.get(0), is(10L - i)); // F_BIGINT
      assertThat(row.get(1), is("hoge"));
    }
  }

  @Test
  public void testRefIllegalVariableName() {
    String script =
      "loop (\"target/all_fields.arrow\") {\n" +
        "  echo $F_BIGIN;\n" +
        "} yield (F_BIGIN:BIGINT)\n";
    try {
      Context ctx = Context.create("ta");
      ctx.eval("ta", script);
      fail();
    } catch (PolyglotException e) {
      assertThat(e.getMessage(), containsString("Failed to reference a local variable: F_BIGIN"));
    }
  }
  @Test
  public void testRefIllegalVariableType() {
    String script =
      "loop (\"target/all_fields.arrow\") {\n" +
        "  echo $F_BIGINT;\n" +
        "} yield (F_BIGINT:BIGIN)\n";
    try {
      Context ctx = Context.create("ta");
      ctx.eval("ta", script);
      fail();
    } catch (PolyglotException e) {
      assertThat(e.getMessage(), containsString("F_BIGINT"));
    }
  }
}


