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

import org.graalvm.launcher.AbstractLanguageLauncher;
import org.graalvm.options.OptionCategory;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TruffleArrowLanguageLauncher extends AbstractLanguageLauncher {
  private static final String TA = "ta";
  private String inputFile;

  public static void main(String[] args) {
    new TruffleArrowLanguageLauncher().launch(args);
  }

  @Override
  protected List<String> preprocessArguments(List<String> arguments, Map<String, String> polyglotOptions) {
    List<String> uncategorized = new ArrayList<>();
    for (String arg: arguments) {
      if (arg.startsWith("-")) {
        uncategorized.add(arg);
      } else {
        this.inputFile = arg;
      }
    }
    return uncategorized;
  }

  @Override
  protected void launch(Context.Builder contextBuilder) {
    if (this.inputFile == null) {
      throw new IllegalStateException("[inputFile] required.");
    }

    try (Context context = contextBuilder.in(System.in).out(System.out).allowAllAccess(true).build()) {
      final Source source = Source.newBuilder(TA, new File(inputFile)).build();
      Value result = context.eval(source);

      if (!result.isNull()) {
        System.out.println(result.toString());
      }
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  protected String getLanguageId() {
    return TA;
  }

  @Override
  protected void printHelp(OptionCategory maxCategory) { }
}
