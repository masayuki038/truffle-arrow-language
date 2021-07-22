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

package net.wrap_trap.truffle_arrow.language.truffle.node.builtins;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.NodeInfo;
import net.wrap_trap.truffle_arrow.language.truffle.TruffleArrowContext;
import net.wrap_trap.truffle_arrow.language.truffle.TruffleArrowLanguage;
import net.wrap_trap.truffle_arrow.language.truffle.node.arrays.VectorSchemaRootContainer;
import net.wrap_trap.truffle_arrow.language.truffle.node.type.ArrayWrapper;
import net.wrap_trap.truffle_arrow.language.truffle.node.type.MapWrapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@NodeInfo(shortName = "store")
public abstract class Store extends TruffleArrowBuiltin {

  @Specialization
  @CompilerDirectives.TruffleBoundary
  public Object store(VectorSchemaRootContainer vectorSchemaRoots, ArrayWrapper array,
                      @CachedLibrary(limit = "3") InteropLibrary interop,
                      @CachedContext(TruffleArrowLanguage.class) TruffleArrowContext context) {
    Object[] values = array.getArray();
    List<Object> params = IntStream.range(0, values.length)
                            .mapToObj(i -> values[i]).collect(Collectors.toList());
    vectorSchemaRoots.addValues(params);
    return vectorSchemaRoots;
  }

  @Specialization
  @CompilerDirectives.TruffleBoundary
  public Object store(VectorSchemaRootContainer vectorSchemaRoots, MapWrapper map,
                      @CachedLibrary(limit = "3") InteropLibrary interop,
                      @CachedContext(TruffleArrowLanguage.class) TruffleArrowContext context) {
    Map<Object, Object> value = map.getMap();
    vectorSchemaRoots.addValues(value);
    return vectorSchemaRoots;
  }
}
