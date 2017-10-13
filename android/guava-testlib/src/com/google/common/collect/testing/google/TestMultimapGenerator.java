/*
 * Copyright (C) 2012 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.common.collect.testing.google;

import com.google.common.annotations.GwtCompatible;
import com.google.common.collect.Multimap;
import com.google.common.collect.testing.SampleElements;
import com.google.common.collect.testing.TestContainerGenerator;
import java.util.Collection;
import java.util.Map;

/**
 * Creates multimaps, containing sample elements, to be tested.
 *
 * @author Louis Wasserman
 */
@GwtCompatible
public interface TestMultimapGenerator<K, V, M extends Multimap<K, V>>
  extends TestContainerGenerator<M, Map.Entry<K, V>> {

  K[] createKeyArray(int length);

  V[] createValueArray(int length);

  SampleElements<K> sampleKeys();

  SampleElements<V> sampleValues();

  Collection<V> createCollection(Iterable<? extends V> values);
}
