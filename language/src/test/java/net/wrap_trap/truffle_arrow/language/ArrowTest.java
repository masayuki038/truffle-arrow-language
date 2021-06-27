package net.wrap_trap.truffle_arrow.language;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.File;
import java.io.IOException;


public class ArrowTest {

  @BeforeAll
  public static void setupOnce() throws ClassNotFoundException, IOException {
    TestUtils.generateTestFile("target/classes/all_fields.arrow", TestDataType.CASE1);
  }

  @AfterAll
  public static void teardownOnce() throws IOException {
    new File("target/classes/all_fields.arrow").delete();
  }
}
