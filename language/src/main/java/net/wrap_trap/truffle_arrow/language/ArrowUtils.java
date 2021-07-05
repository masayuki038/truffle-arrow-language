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

package net.wrap_trap.truffle_arrow.language;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ArrowUtils {

  private static final Logger log = LoggerFactory.getLogger(ArrowUtils.class);
  private static final String CONFIG_ALLOCATOR_SIZE = "allocator.initial.size";
  private static final String CONFIG_ALLOCATOR_DEBUG_LOG = "allocator.debug-log";

  private static RootAllocator rootAllocator = new RootAllocator(Long.MAX_VALUE);

  public static BufferAllocator createAllocator(String desc) {
    Config config = ConfigFactory.load();
    int size = config.getInt(CONFIG_ALLOCATOR_SIZE);
    boolean enableDebugLog = config.getBoolean(CONFIG_ALLOCATOR_DEBUG_LOG);

    if (enableDebugLog) {
      log.debug(String.format("createAllocator [%s] (before) %s", desc, rootAllocator.toVerboseString()));
    }
    BufferAllocator newBuffer = rootAllocator.newChildAllocator(
      Thread.currentThread().getName(), size, Integer.MAX_VALUE);
    if (enableDebugLog) {
      log.debug(String.format("createAllocator [%s] (after) %s", desc, rootAllocator.toVerboseString()));
    }
    return newBuffer;
  }
}

