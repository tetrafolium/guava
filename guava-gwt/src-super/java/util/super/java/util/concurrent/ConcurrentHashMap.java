/*
 * Copyright (C) 2009 The Guava Authors
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

package java.util.concurrent;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Minimal emulation of {@link java.util.concurrent.ConcurrentHashMap}.
 * Note that the javascript interpreter is <a
 * href="http://code.google.com/docreader/#p=google-web-toolkit-doc-1-5&t=DevGuideJavaCompatibility">
 * single-threaded</a>, it is essentially a {@link java.util.HashMap},
 * implementing the new methods introduced by {@link ConcurrentMap}.
 *
 * @author Hayward Chan
 */
public class ConcurrentHashMap<K, V>
    extends AbstractMap<K, V> implements ConcurrentMap<K, V> {

  private final Map<K, V> backingMap;

  public ConcurrentHashMap() {
    this.backingMap = new HashMap<K, V>();
  }

  public ConcurrentHashMap(final int initialCapacity) {
    this.backingMap = new HashMap<K, V>(initialCapacity);
  }

  public ConcurrentHashMap(final int initialCapacity, final float loadFactor) {
    this.backingMap = new HashMap<K, V>(initialCapacity, loadFactor);
  }

  public ConcurrentHashMap(final Map<? extends K, ? extends V> t) {
    this.backingMap = new HashMap<K, V>(t);
  }

  public V putIfAbsent(final K key, final V value) {
    if (!containsKey(key)) {
      return put(key, value);
    } else {
      return get(key);
    }
  }

  public boolean remove(final Object key, final Object value) {
    if (containsKey(key) && get(key).equals(value)) {
      remove(key);
      return true;
    } else {
      return false;
    }
  }

  public boolean replace(final K key, final V oldValue, final V newValue) {
    if (oldValue == null || newValue == null) {
      throw new NullPointerException();
    } else if (containsKey(key) && get(key).equals(oldValue)) {
      put(key, newValue);
      return true;
    } else {
      return false;
    }
  }

  public V replace(final K key, final V value) {
    if (value == null) {
      throw new NullPointerException();
    } else if (containsKey(key)) {
      return put(key, value);
    } else {
      return null;
    }
  }

  @Override public boolean containsKey(final Object key) {
    if (key == null) {
      throw new NullPointerException();
    }
    return backingMap.containsKey(key);
  }

  @Override public V get(final Object key) {
    if (key == null) {
      throw new NullPointerException();
    }
    return backingMap.get(key);
  }

  @Override public V put(final K key, final V value) {
    if (key == null || value == null) {
      throw new NullPointerException();
    }
    return backingMap.put(key, value);
  }

  @Override public boolean containsValue(final Object value) {
    if (value == null) {
      throw new NullPointerException();
    }
    return backingMap.containsValue(value);
  }

  @Override public V remove(final Object key) {
    if (key == null) {
      throw new NullPointerException();
    }
    return backingMap.remove(key);
  }

  @Override public Set<Entry<K, V>> entrySet() {
    return backingMap.entrySet();
  }

  public boolean contains(final Object value) {
    return containsValue(value);
  }

  public Enumeration<V> elements() {
    return Collections.enumeration(values());
  }

  public Enumeration<K> keys() {
    return Collections.enumeration(keySet());
  }
}
