package net.wrap_trap.truffle_arrow.language;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

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
      "loop (\"target/all_fields.arrow\") {\n" +
      "  echo $F_INT;\n" +
      "  echo $F_BIGINT;\n" +
      "  return $F_BIGINT;\n" +
      "} yield (\"F_INT\", \"F_BIGINT\")\n";
    Context ctx = Context.create("ta");
    assertThat(ctx.eval("ta", script).asLong(), is(10L));
  }

  @Test
  public void testRefIllegalVariable() {
    String script =
      "loop (\"target/all_fields.arrow\") {\n" +
      "  echo $F_BIGIN;\n" +
      "} yield (\"F_BIGIN\")\n";
    try {
      Context ctx = Context.create("ta");
      ctx.eval("ta", script);
      fail();
    } catch (PolyglotException e) {
      assertThat(e.getMessage(), containsString("Failed to reference a local variable: F_BIGIN"));
    }
  }

  @Test
  public void testAddNewVariable() {
    String script =
      "loop (\"target/all_fields.arrow\") {\n" +
        "  echo $F_BIGINT;\n" +
        "  $a = \"hoge\";\n" +
        "  $b = 1;\n" +
        "} yield (\"F_BIGINT\", \"a\")\n";
    Context ctx = Context.create("ta");
    ctx.eval("ta", script);
  }
}


