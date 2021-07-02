package net.wrap_trap.truffle_arrow.language;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

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
      "return $a + $b * 3;";
    Context ctx = Context.create("ta");
    assertThat(ctx.eval("ta", SAMPLE).asInt(), is(9));
  }

  @Test
  public void testEqual1() {
    String SAMPLE =
      "$a = 1;\n" +
      "if ($a == 1) {\n" +
      "  echo \"$a == 1\";\n" +
      "  return \"foo\";\n" +
      "}\n" +
      "return \"bar\";";
    Context ctx = Context.create("ta");
    assertThat(ctx.eval("ta", SAMPLE).asString(), is("foo"));
  }

  @Test
  public void testEqual2() {
    String SAMPLE =
      "$a = 1;\n" +
      "if ($a == 2) {\n" +
      "  echo \"$a == 2\";\n" +
      "  return \"foo\";\n" +
      "}\n" +
      "return \"bar\";";
    Context ctx = Context.create("ta");
    assertThat(ctx.eval("ta", SAMPLE).asString(), is("bar"));
  }

  @Test
  public void testPushedVariables() throws IOException {
    String script =
      "$out = arrays(F_INT:INT, F_BIGINT:BIGINT);" +
      "loop (\"target/all_fields.arrow\") {\n" +
      "  echo $F_INT;\n" +
      "  echo $F_BIGINT;\n" +
      "  $F_BIGINT;\n" +
      "  store($out, $F_INT, $F_BIGINT);\n" +
      "}\n" +
      "echo $out;" +
      "return $out;";
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
      "$out = arrays(F_BIGINT:BIGINT, a:STRING);" +
      "loop (\"target/all_fields.arrow\") {\n" +
        "  echo $F_BIGINT;\n" +
        "  $a = \"hoge\";\n" +
        "  $b = 1;\n" +
        "  store($out, $F_BIGINT, $a);" +
        "}\n" +
        "return $out;";
    Context ctx = Context.create("ta");
    List<List<Object>> rows = ctx.eval("ta", script).as(List.class);
    for (int i = 0; i < 10; i ++) {
      List<Object> row = rows.get(i);
      assertThat(row.get(0), is(10L - i)); // F_BIGINT
      assertThat(row.get(1), is("hoge"));
    }
  }

  @Test
  public void testSum() {
    String script =
      "$sum = 0;" +
        "loop (\"target/all_fields.arrow\") {\n" +
        "  echo $F_INT;\n" +
        "  $sum = $sum + $F_INT;\n" +
        "}\n" +
        "return $sum;";
    Context ctx = Context.create("ta");
    long sum = ctx.eval("ta", script).asLong();
    assertThat(sum, is(45L));
  }


  @Test
  public void testCount() {
    String script =
      "$cnt = 0;" +
        "$out = arrays(cnt:INT);" +
        "loop (\"target/all_fields.arrow\") {\n" +
        "  echo $F_INT;\n" +
        "  $cnt = $cnt + 1;\n" +
        "}\n" +
        "store($out, $cnt);\n" +
        "return $out;";
    Context ctx = Context.create("ta");
    List<List<Object>> rows = ctx.eval("ta", script).as(List.class);
    assertThat(rows.size(), is(1));
    assertThat(rows.get(0).get(0), is(10));
  }

  @Test
  public void testMap() {
    String script =
      "$map = {};" +
        "$out = arrays(key:INT, value:INT);" +
        "loop (\"target/all_fields.arrow\") {\n" +
        "  $tmp = $F_INT / 2;\n" +
        "  $map[$tmp] = get($map[$tmp], 0) + 1;\n" +
        "}\n" +
        "store($out, $map);" +
        "return $out;";
    Context ctx = Context.create("ta");
    List<List<Object>> rows = ctx.eval("ta", script).as(List.class);
    assertThat(rows.size(), is(5));
    assertThat(rows.get(0).get(0), is(0));
    assertThat(rows.get(0).get(1), is(2));
    assertThat(rows.get(4).get(0), is(4));
    assertThat(rows.get(4).get(1), is(2));
  }

  @Test
  public void testFilterAndMap() {
    String script =
      "$map = {};" +
        "$out = arrays(key:INT, value:INT);" +
        "loop (\"target/all_fields.arrow\") {\n" +
        "  if ($F_INT < 5) {\n" +
        "    $map[$F_INT] = get($map[$F_INT], 0) + 1;\n" +
        "  }\n" +
        "}\n" +
        "store($out, $map);" +
        "return $out;";
    Context ctx = Context.create("ta");
    List<List<Object>> rows = ctx.eval("ta", script).as(List.class);
    assertThat(rows.size(), is(5));
    assertThat(rows.get(0).get(0), is(0));
    assertThat(rows.get(0).get(1), is(1));
    assertThat(rows.get(4).get(0), is(4));
    assertThat(rows.get(4).get(1), is(1));
  }

  @Test
  public void testRefIllegalVariableName() {
    String script =
      "loop (\"target/all_fields.arrow\") {\n" +
        "  echo $F_BIGIN;\n" +
        "}\n";
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
      "$out = arrays(F_BIGINT:BIGIN);\n" +
      "loop (\"target/all_fields.arrow\") {\n" +
        "  echo $F_BIGINT;\n" +
        "}\n";
    try {
      Context ctx = Context.create("ta");
      ctx.eval("ta", script);
      fail();
    } catch (PolyglotException e) {
      assertThat(e.getMessage(), containsString("F_BIGINT"));
    }
  }
}


