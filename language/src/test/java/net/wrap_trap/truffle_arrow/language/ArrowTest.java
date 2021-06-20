package net.wrap_trap.truffle_arrow.language;

import org.apache.arrow.vector.ipc.SeekableReadChannel;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.apache.arrow.vector.ipc.ArrowFileReader;
import org.apache.arrow.vector.ipc.ArrowReader;


public class ArrowTest {

  @BeforeAll
  public static void setupOnce() throws ClassNotFoundException, IOException {
    TestUtils.generateTestFile("target/classes/all_fields.arrow", TestDataType.CASE1);
  }

  @AfterAll
  public static void teardownOnce() throws IOException {
    new File("target/classes/all_fields.arrow").delete();
  }

//  public void test() throws IOException {
//    FileInputStream fileInputStream = new FileInputStream("target/classes/all_fields.arrow");
//    try (ArrowFileReader reader = new ArrowFileReader(fileInputStream.getChannel(), ArrowUtils.createAllocator("test"))) {
//      reader.
//    }
//  }
}
