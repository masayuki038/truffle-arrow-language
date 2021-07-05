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

package net.wrap_trap.truffle_arrow.language.launcher;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;


public final class Main {

  private static final String TA = "ta";

  /**
   * The main entry point.
   */
  public static void main(String[] args) throws IOException {
    Source source;
    Map<String, String> options = new HashMap<>();
    String file = null;
    for (String arg : args) {
      if (parseOption(options, arg)) {
        continue;
      } else {
        if (file == null) {
          file = arg;
        }
      }
    }

    if (file == null) {
      // @formatter:off
      source = Source.newBuilder(TA, new InputStreamReader(System.in), "<stdin>").build();
      // @formatter:on
    } else {
      source = Source.newBuilder(TA, new File(file)).build();
    }

    System.exit(executeSource(source, System.in, System.out, options));
  }

  private static int executeSource(Source source, InputStream in, PrintStream out, Map<String, String> options) {
    Context context;
    PrintStream err = System.err;
    try {
      context = Context.newBuilder(TA).in(in).out(out).options(options).build();
    } catch (IllegalArgumentException e) {
      err.println(e.getMessage());
      return 1;
    }
    out.println("== running on " + context.getEngine());

    try {
      Value result = context.eval(source);

      if (!result.isNull()) {
        out.println(result.toString());
      }
      return 0;
    } catch (PolyglotException ex) {
      if (ex.isInternalError()) {
        // for internal errors we print the full stack trace
        ex.printStackTrace();
      } else {
        err.println(ex.getMessage());
      }
      return 1;
    } finally {
      context.close();
    }
  }

  private static boolean parseOption(Map<String, String> options, String arg) {
    if (arg.length() <= 2 || !arg.startsWith("--")) {
      return false;
    }
    int eqIdx = arg.indexOf('=');
    String key;
    String value;
    if (eqIdx < 0) {
      key = arg.substring(2);
      value = null;
    } else {
      key = arg.substring(2, eqIdx);
      value = arg.substring(eqIdx + 1);
    }

    if (value == null) {
      value = "true";
    }
    int index = key.indexOf('.');
    String group = key;
    if (index >= 0) {
      group = group.substring(0, index);
    }
    options.put(key, value);
    return true;
  }
}
