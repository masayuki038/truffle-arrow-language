package net.wrap_trap.truffle_arrow.language;

import org.graalvm.polyglot.Context;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TruffleArrowLanguageTest {
  @BeforeClass
  public static void setupOnce() throws IOException {
    TestUtils.generateTestFile("target/all_fields.arrow", TestDataType.CASE4);
  }

  @AfterClass
  public static void teardownOnce() {
    new File("target/all_fields.arrow").delete();
  }

  private final static String SAMPLE =
      "$a = 1;\n" +
      "$b = 2;\n" +
      "return $a + $b;";

  @Test
  public void testScript() {
    Context ctx = Context.create("ta");
    assertThat(ctx.eval("ta", SAMPLE).asDouble(), is(3d));
  }

  private final static String SAMPLE2 =
      "loop (\"target/all_fields.arrow\") {\n" +
      "  echo $F_BIGINT;\n" +
      "  return $F_BIGINT;\n" +
      "}\n";

  @Test
  public void testPushedVariable() throws IOException {
    Context ctx = Context.create("ta");
    assertThat(ctx.eval("ta", SAMPLE2).asLong(), is(10L));
  }
}


