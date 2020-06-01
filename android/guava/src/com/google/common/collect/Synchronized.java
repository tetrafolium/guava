/*
 * Copyright (C) 2007 The Guava Authors
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

package com.google.common.collect;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.VisibleForTesting;
import com.google.j2objc.annotations.RetainedWith;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Queue;
import java.util.RandomAccess;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import javax.annotation.Nullable;

/**
 * Synchronized collection views. The returned synchronized collection views are
 * serializable if the backing collection and the mutex are serializable.
 *
 * <p>If {@code null} is passed as the {@code mutex} parameter to any of this
 * class's top-level methods or inner class constructors, the created object
 * uses itself as the synchronization mutex.
 *
 * <p>This class should be used by other collection classes only.
 *
 * @author Mike Bostock
 * @author Jared Levy
 */
@GwtCompatible(emulated = true)
final class Synchronized {
  private Synchronized() { }

  static class SynchronizedObject implements Serializable {
    final Object delegate;
    final Object mutex;

    SynchronizedObject(final Object delegate, final @Nullable Object mutex) {
      this.delegate = checkNotNull(delegate);
      this.mutex = (mutex == null) ? this : mutex;
    }

    Object delegate() {
      return delegate;
    }

    // No equals and hashCode; see ForwardingObject for details.

    @Override
    public String toString() {
      synchronized (mutex) {
        return delegate.toString();
      }
    }

    // Serialization invokes writeObject only when it's private.
    // The SynchronizedObject subclasses don't need a writeObject method since
    // they don't contain any non-transient member variables, while the
    // following writeObject() handles the SynchronizedObject members.

    @GwtIncompatible // java.io.ObjectOutputStream
    private void writeObject(final ObjectOutputStream stream) throws IOException {
      synchronized (mutex) {
        stream.defaultWriteObject();
      }
    }

    @GwtIncompatible // not needed in emulated source
    private static final long serialVersionUID = 0;
  }

  private static <E> Collection<E> collection(final Collection<E> collection, final @Nullable Object mutex) {
    return new SynchronizedCollection<E>(collection, mutex);
  }

  @VisibleForTesting
  static class SynchronizedCollection<E> extends SynchronizedObject implements Collection<E> {
    private SynchronizedCollection(final Collection<E> delegate, final @Nullable Object mutex) {
      super(delegate, mutex);
    }

    @SuppressWarnings("unchecked")
    @Override
    Collection<E> delegate() {
      return (Collection<E>) super.delegate();
    }

    @Override
    public boolean add(final E e) {
      synchronized (mutex) {
        return delegate().add(e);
      }
    }

    @Override
    public boolean addAll(final Collection<? extends E> c) {
      synchronized (mutex) {
        return delegate().addAll(c);
      }
    }

    @Override
    public void clear() {
      synchronized (mutex) {
        delegate().clear();
      }
    }

    @Override
    public boolean contains(final Object o) {
      synchronized (mutex) {
        return delegate().contains(o);
      }
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
      synchronized (mutex) {
        return delegate().containsAll(c);
      }
    }

    @Override
    public boolean isEmpty() {
      synchronized (mutex) {
        return delegate().isEmpty();
      }
    }

    @Override
    public Iterator<E> iterator() {
      return delegate().iterator(); // manually synchronized
    }

    @Override
    public boolean remove(final Object o) {
      synchronized (mutex) {
        return delegate().remove(o);
      }
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
      synchronized (mutex) {
        return delegate().removeAll(c);
      }
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
      synchronized (mutex) {
        return delegate().retainAll(c);
      }
    }

    @Override
    public int size() {
      synchronized (mutex) {
        return delegate().size();
      }
    }

    @Override
    public Object[] toArray() {
      synchronized (mutex) {
        return delegate().toArray();
      }
    }

    @Override
    public <T> T[] toArray(final T[] a) {
      synchronized (mutex) {
        return delegate().toArray(a);
      }
    }

    private static final long serialVersionUID = 0;
  }

  @VisibleForTesting
  static <E> Set<E> set(final Set<E> set, final @Nullable Object mutex) {
    return new SynchronizedSet<E>(set, mutex);
  }

  static class SynchronizedSet<E> extends SynchronizedCollection<E> implements Set<E> {

    SynchronizedSet(final Set<E> delegate, final @Nullable Object mutex) {
      super(delegate, mutex);
    }

    @Override
    Set<E> delegate() {
      return (Set<E>) super.delegate();
    }

    @Override
    public boolean equals(final Object o) {
      if (o == this) {
        return true;
      }
      synchronized (mutex) {
        return delegate().equals(o);
      }
    }

    @Override
    public int hashCode() {
      synchronized (mutex) {
        return delegate().hashCode();
      }
    }

    private static final long serialVersionUID = 0;
  }

  private static <E> SortedSet<E> sortedSet(final SortedSet<E> set, final @Nullable Object mutex) {
    return new SynchronizedSortedSet<E>(set, mutex);
  }

  static class SynchronizedSortedSet<E> extends SynchronizedSet<E> implements SortedSet<E> {
    SynchronizedSortedSet(final SortedSet<E> delegate, final @Nullable Object mutex) {
      super(delegate, mutex);
    }

    @Override
    SortedSet<E> delegate() {
      return (SortedSet<E>) super.delegate();
    }

    @Override
    public Comparator<? super E> comparator() {
      synchronized (mutex) {
        return delegate().comparator();
      }
    }

    @Override
    public SortedSet<E> subSet(final E fromElement, final E toElement) {
      synchronized (mutex) {
        return sortedSet(delegate().subSet(fromElement, toElement), mutex);
      }
    }

    @Override
    public SortedSet<E> headSet(final E toElement) {
      synchronized (mutex) {
        return sortedSet(delegate().headSet(toElement), mutex);
      }
    }

    @Override
    public SortedSet<E> tailSet(final E fromElement) {
      synchronized (mutex) {
        return sortedSet(delegate().tailSet(fromElement), mutex);
      }
    }

    @Override
    public E first() {
      synchronized (mutex) {
        return delegate().first();
      }
    }

    @Override
    public E last() {
      synchronized (mutex) {
        return delegate().last();
      }
    }

    private static final long serialVersionUID = 0;
  }

  private static <E> List<E> list(final List<E> list, final @Nullable Object mutex) {
    return (list instanceof RandomAccess)
        ? new SynchronizedRandomAccessList<E>(list, mutex)
        : new SynchronizedList<E>(list, mutex);
  }

  private static class SynchronizedList<E> extends SynchronizedCollection<E> implements List<E> {
    SynchronizedList(final List<E> delegate, final @Nullable Object mutex) {
      super(delegate, mutex);
    }

    @Override
    List<E> delegate() {
      return (List<E>) super.delegate();
    }

    @Override
    public void add(final int index, final E element) {
      synchronized (mutex) {
        delegate().add(index, element);
      }
    }

    @Override
    public boolean addAll(final int index, final Collection<? extends E> c) {
      synchronized (mutex) {
        return delegate().addAll(index, c);
      }
    }

    @Override
    public E get(final int index) {
      synchronized (mutex) {
        return delegate().get(index);
      }
    }

    @Override
    public int indexOf(final Object o) {
      synchronized (mutex) {
        return delegate().indexOf(o);
      }
    }

    @Override
    public int lastIndexOf(final Object o) {
      synchronized (mutex) {
        return delegate().lastIndexOf(o);
      }
    }

    @Override
    public ListIterator<E> listIterator() {
      return delegate().listIterator(); // manually synchronized
    }

    @Override
    public ListIterator<E> listIterator(final int index) {
      return delegate().listIterator(index); // manually synchronized
    }

    @Override
    public E remove(final int index) {
      synchronized (mutex) {
        return delegate().remove(index);
      }
    }

    @Override
    public E set(final int index, final E element) {
      synchronized (mutex) {
        return delegate().set(index, element);
      }
    }

    @Override
    public List<E> subList(final int fromIndex, final int toIndex) {
      synchronized (mutex) {
        return list(delegate().subList(fromIndex, toIndex), mutex);
      }
    }

    @Override
    public boolean equals(final Object o) {
      if (o == this) {
        return true;
      }
      synchronized (mutex) {
        return delegate().equals(o);
      }
    }

    @Override
    public int hashCode() {
      synchronized (mutex) {
        return delegate().hashCode();
      }
    }

    private static final long serialVersionUID = 0;
  }

  private static class SynchronizedRandomAccessList<E> extends SynchronizedList<E>
      implements RandomAccess {
    SynchronizedRandomAccessList(final List<E> list, final @Nullable Object mutex) {
      super(list, mutex);
    }

    private static final long serialVersionUID = 0;
  }

  static <E> Multiset<E> multiset(final Multiset<E> multiset, final @Nullable Object mutex) {
    if (multiset instanceof SynchronizedMultiset || multiset instanceof ImmutableMultiset) {
      return multiset;
    }
    return new SynchronizedMultiset<E>(multiset, mutex);
  }

  private static class SynchronizedMultiset<E> extends SynchronizedCollection<E>
      implements Multiset<E> {
    transient Set<E> elementSet;
    transient Set<Entry<E>> entrySet;

    SynchronizedMultiset(final Multiset<E> delegate, final @Nullable Object mutex) {
      super(delegate, mutex);
    }

    @Override
    Multiset<E> delegate() {
      return (Multiset<E>) super.delegate();
    }

    @Override
    public int count(final Object o) {
      synchronized (mutex) {
        return delegate().count(o);
      }
    }

    @Override
    public int add(final E e, final int n) {
      synchronized (mutex) {
        return delegate().add(e, n);
      }
    }

    @Override
    public int remove(final Object o, final int n) {
      synchronized (mutex) {
        return delegate().remove(o, n);
      }
    }

    @Override
    public int setCount(final E element, final int count) {
      synchronized (mutex) {
        return delegate().setCount(element, count);
      }
    }

    @Override
    public boolean setCount(final E element, final int oldCount, final int newCount) {
      synchronized (mutex) {
        return delegate().setCount(element, oldCount, newCount);
      }
    }

    @Override
    public Set<E> elementSet() {
      synchronized (mutex) {
        if (elementSet == null) {
          elementSet = typePreservingSet(delegate().elementSet(), mutex);
        }
        return elementSet;
      }
    }

    @Override
    public Set<Entry<E>> entrySet() {
      synchronized (mutex) {
        if (entrySet == null) {
          entrySet = typePreservingSet(delegate().entrySet(), mutex);
        }
        return entrySet;
      }
    }

    @Override
    public boolean equals(final Object o) {
      if (o == this) {
        return true;
      }
      synchronized (mutex) {
        return delegate().equals(o);
      }
    }

    @Override
    public int hashCode() {
      synchronized (mutex) {
        return delegate().hashCode();
      }
    }

    private static final long serialVersionUID = 0;
  }

  static <K, V> Multimap<K, V> multimap(final Multimap<K, V> multimap, final @Nullable Object mutex) {
    if (multimap instanceof SynchronizedMultimap || multimap instanceof ImmutableMultimap) {
      return multimap;
    }
    return new SynchronizedMultimap<>(multimap, mutex);
  }

  private static class SynchronizedMultimap<K, V> extends SynchronizedObject
      implements Multimap<K, V> {
    transient Set<K> keySet;
    transient Collection<V> valuesCollection;
    transient Collection<Map.Entry<K, V>> entries;
    transient Map<K, Collection<V>> asMap;
    transient Multiset<K> keys;

    @SuppressWarnings("unchecked")
    @Override
    Multimap<K, V> delegate() {
      return (Multimap<K, V>) super.delegate();
    }

    SynchronizedMultimap(final Multimap<K, V> delegate, final @Nullable Object mutex) {
      super(delegate, mutex);
    }

    @Override
    public int size() {
      synchronized (mutex) {
        return delegate().size();
      }
    }

    @Override
    public boolean isEmpty() {
      synchronized (mutex) {
        return delegate().isEmpty();
      }
    }

    @Override
    public boolean containsKey(final Object key) {
      synchronized (mutex) {
        return delegate().containsKey(key);
      }
    }

    @Override
    public boolean containsValue(final Object value) {
      synchronized (mutex) {
        return delegate().containsValue(value);
      }
    }

    @Override
    public boolean containsEntry(final Object key, final Object value) {
      synchronized (mutex) {
        return delegate().containsEntry(key, value);
      }
    }

    @Override
    public Collection<V> get(final K key) {
      synchronized (mutex) {
        return typePreservingCollection(delegate().get(key), mutex);
      }
    }

    @Override
    public boolean put(final K key, final V value) {
      synchronized (mutex) {
        return delegate().put(key, value);
      }
    }

    @Override
    public boolean putAll(final K key, final Iterable<? extends V> values) {
      synchronized (mutex) {
        return delegate().putAll(key, values);
      }
    }

    @Override
    public boolean putAll(final Multimap<? extends K, ? extends V> multimap) {
      synchronized (mutex) {
        return delegate().putAll(multimap);
      }
    }

    @Override
    public Collection<V> replaceValues(final K key, final Iterable<? extends V> values) {
      synchronized (mutex) {
        return delegate().replaceValues(key, values); // copy not synchronized
      }
    }

    @Override
    public boolean remove(final Object key, final Object value) {
      synchronized (mutex) {
        return delegate().remove(key, value);
      }
    }

    @Override
    public Collection<V> removeAll(final Object key) {
      synchronized (mutex) {
        return delegate().removeAll(key); // copy not synchronized
      }
    }

    @Override
    public void clear() {
      synchronized (mutex) {
        delegate().clear();
      }
    }

    @Override
    public Set<K> keySet() {
      synchronized (mutex) {
        if (keySet == null) {
          keySet = typePreservingSet(delegate().keySet(), mutex);
        }
        return keySet;
      }
    }

    @Override
    public Collection<V> values() {
      synchronized (mutex) {
        if (valuesCollection == null) {
          valuesCollection = collection(delegate().values(), mutex);
        }
        return valuesCollection;
      }
    }

    @Override
    public Collection<Map.Entry<K, V>> entries() {
      synchronized (mutex) {
        if (entries == null) {
          entries = typePreservingCollection(delegate().entries(), mutex);
        }
        return entries;
      }
    }

    @Override
    public Map<K, Collection<V>> asMap() {
      synchronized (mutex) {
        if (asMap == null) {
          asMap = new SynchronizedAsMap<>(delegate().asMap(), mutex);
        }
        return asMap;
      }
    }

    @Override
    public Multiset<K> keys() {
      synchronized (mutex) {
        if (keys == null) {
          keys = multiset(delegate().keys(), mutex);
        }
        return keys;
      }
    }

    @Override
    public boolean equals(final Object o) {
      if (o == this) {
        return true;
      }
      synchronized (mutex) {
        return delegate().equals(o);
      }
    }

    @Override
    public int hashCode() {
      synchronized (mutex) {
        return delegate().hashCode();
      }
    }

    private static final long serialVersionUID = 0;
  }

  static <K, V> ListMultimap<K, V> listMultimap(
      final ListMultimap<K, V> multimap, final @Nullable Object mutex) {
    if (multimap instanceof SynchronizedListMultimap || multimap instanceof ImmutableListMultimap) {
      return multimap;
    }
    return new SynchronizedListMultimap<>(multimap, mutex);
  }

  private static class SynchronizedListMultimap<K, V> extends SynchronizedMultimap<K, V>
      implements ListMultimap<K, V> {
    SynchronizedListMultimap(final ListMultimap<K, V> delegate, final @Nullable Object mutex) {
      super(delegate, mutex);
    }

    @Override
    ListMultimap<K, V> delegate() {
      return (ListMultimap<K, V>) super.delegate();
    }

    @Override
    public List<V> get(final K key) {
      synchronized (mutex) {
        return list(delegate().get(key), mutex);
      }
    }

    @Override
    public List<V> removeAll(final Object key) {
      synchronized (mutex) {
        return delegate().removeAll(key); // copy not synchronized
      }
    }

    @Override
    public List<V> replaceValues(final K key, final Iterable<? extends V> values) {
      synchronized (mutex) {
        return delegate().replaceValues(key, values); // copy not synchronized
      }
    }

    private static final long serialVersionUID = 0;
  }

  static <K, V> SetMultimap<K, V> setMultimap(final SetMultimap<K, V> multimap, final @Nullable Object mutex) {
    if (multimap instanceof SynchronizedSetMultimap || multimap instanceof ImmutableSetMultimap) {
      return multimap;
    }
    return new SynchronizedSetMultimap<>(multimap, mutex);
  }

  private static class SynchronizedSetMultimap<K, V> extends SynchronizedMultimap<K, V>
      implements SetMultimap<K, V> {
    transient Set<Map.Entry<K, V>> entrySet;

    SynchronizedSetMultimap(final SetMultimap<K, V> delegate, final @Nullable Object mutex) {
      super(delegate, mutex);
    }

    @Override
    SetMultimap<K, V> delegate() {
      return (SetMultimap<K, V>) super.delegate();
    }

    @Override
    public Set<V> get(final K key) {
      synchronized (mutex) {
        return set(delegate().get(key), mutex);
      }
    }

    @Override
    public Set<V> removeAll(final Object key) {
      synchronized (mutex) {
        return delegate().removeAll(key); // copy not synchronized
      }
    }

    @Override
    public Set<V> replaceValues(final K key, final Iterable<? extends V> values) {
      synchronized (mutex) {
        return delegate().replaceValues(key, values); // copy not synchronized
      }
    }

    @Override
    public Set<Map.Entry<K, V>> entries() {
      synchronized (mutex) {
        if (entrySet == null) {
          entrySet = set(delegate().entries(), mutex);
        }
        return entrySet;
      }
    }

    private static final long serialVersionUID = 0;
  }

  static <K, V> SortedSetMultimap<K, V> sortedSetMultimap(
      final SortedSetMultimap<K, V> multimap, final @Nullable Object mutex) {
    if (multimap instanceof SynchronizedSortedSetMultimap) {
      return multimap;
    }
    return new SynchronizedSortedSetMultimap<>(multimap, mutex);
  }

  private static class SynchronizedSortedSetMultimap<K, V> extends SynchronizedSetMultimap<K, V>
      implements SortedSetMultimap<K, V> {
    SynchronizedSortedSetMultimap(final SortedSetMultimap<K, V> delegate, final @Nullable Object mutex) {
      super(delegate, mutex);
    }

    @Override
    SortedSetMultimap<K, V> delegate() {
      return (SortedSetMultimap<K, V>) super.delegate();
    }

    @Override
    public SortedSet<V> get(final K key) {
      synchronized (mutex) {
        return sortedSet(delegate().get(key), mutex);
      }
    }

    @Override
    public SortedSet<V> removeAll(final Object key) {
      synchronized (mutex) {
        return delegate().removeAll(key); // copy not synchronized
      }
    }

    @Override
    public SortedSet<V> replaceValues(final K key, final Iterable<? extends V> values) {
      synchronized (mutex) {
        return delegate().replaceValues(key, values); // copy not synchronized
      }
    }

    @Override
    public Comparator<? super V> valueComparator() {
      synchronized (mutex) {
        return delegate().valueComparator();
      }
    }

    private static final long serialVersionUID = 0;
  }

  private static <E> Collection<E> typePreservingCollection(
      final Collection<E> collection, final @Nullable Object mutex) {
    if (collection instanceof SortedSet) {
      return sortedSet((SortedSet<E>) collection, mutex);
    }
    if (collection instanceof Set) {
      return set((Set<E>) collection, mutex);
    }
    if (collection instanceof List) {
      return list((List<E>) collection, mutex);
    }
    return collection(collection, mutex);
  }

  private static <E> Set<E> typePreservingSet(final Set<E> set, final @Nullable Object mutex) {
    if (set instanceof SortedSet) {
      return sortedSet((SortedSet<E>) set, mutex);
    } else {
      return set(set, mutex);
    }
  }

  private static class SynchronizedAsMapEntries<K, V>
      extends SynchronizedSet<Map.Entry<K, Collection<V>>> {
    SynchronizedAsMapEntries(final Set<Map.Entry<K, Collection<V>>> delegate, final @Nullable Object mutex) {
      super(delegate, mutex);
    }

    @Override
    public Iterator<Map.Entry<K, Collection<V>>> iterator() {
      // Must be manually synchronized.
      return new TransformedIterator<Map.Entry<K, Collection<V>>, Map.Entry<K, Collection<V>>>(
          super.iterator()) {
        @Override
        Map.Entry<K, Collection<V>> transform(final Map.Entry<K, Collection<V>> entry) {
          return new ForwardingMapEntry<K, Collection<V>>() {
            @Override
            protected Map.Entry<K, Collection<V>> delegate() {
              return entry;
            }

            @Override
            public Collection<V> getValue() {
              return typePreservingCollection(entry.getValue(), mutex);
            }
          };
        }
      };
    }

    // See Collections.CheckedMap.CheckedEntrySet for details on attacks.

    @Override
    public Object[] toArray() {
      synchronized (mutex) {
        return ObjectArrays.toArrayImpl(delegate());
      }
    }

    @Override
    public <T> T[] toArray(final T[] array) {
      synchronized (mutex) {
        return ObjectArrays.toArrayImpl(delegate(), array);
      }
    }

    @Override
    public boolean contains(final Object o) {
      synchronized (mutex) {
        return Maps.containsEntryImpl(delegate(), o);
      }
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
      synchronized (mutex) {
        return Collections2.containsAllImpl(delegate(), c);
      }
    }

    @Override
    public boolean equals(final Object o) {
      if (o == this) {
        return true;
      }
      synchronized (mutex) {
        return Sets.equalsImpl(delegate(), o);
      }
    }

    @Override
    public boolean remove(final Object o) {
      synchronized (mutex) {
        return Maps.removeEntryImpl(delegate(), o);
      }
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
      synchronized (mutex) {
        return Iterators.removeAll(delegate().iterator(), c);
      }
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
      synchronized (mutex) {
        return Iterators.retainAll(delegate().iterator(), c);
      }
    }

    private static final long serialVersionUID = 0;
  }

  @VisibleForTesting
  static <K, V> Map<K, V> map(final Map<K, V> map, final @Nullable Object mutex) {
    return new SynchronizedMap<>(map, mutex);
  }

  private static class SynchronizedMap<K, V> extends SynchronizedObject implements Map<K, V> {
    transient Set<K> keySet;
    transient Collection<V> values;
    transient Set<Map.Entry<K, V>> entrySet;

    SynchronizedMap(final Map<K, V> delegate, final @Nullable Object mutex) {
      super(delegate, mutex);
    }

    @SuppressWarnings("unchecked")
    @Override
    Map<K, V> delegate() {
      return (Map<K, V>) super.delegate();
    }

    @Override
    public void clear() {
      synchronized (mutex) {
        delegate().clear();
      }
    }

    @Override
    public boolean containsKey(final Object key) {
      synchronized (mutex) {
        return delegate().containsKey(key);
      }
    }

    @Override
    public boolean containsValue(final Object value) {
      synchronized (mutex) {
        return delegate().containsValue(value);
      }
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
      synchronized (mutex) {
        if (entrySet == null) {
          entrySet = set(delegate().entrySet(), mutex);
        }
        return entrySet;
      }
    }

    @Override
    public V get(final Object key) {
      synchronized (mutex) {
        return delegate().get(key);
      }
    }

    @Override
    public boolean isEmpty() {
      synchronized (mutex) {
        return delegate().isEmpty();
      }
    }

    @Override
    public Set<K> keySet() {
      synchronized (mutex) {
        if (keySet == null) {
          keySet = set(delegate().keySet(), mutex);
        }
        return keySet;
      }
    }

    @Override
    public V put(final K key, final V value) {
      synchronized (mutex) {
        return delegate().put(key, value);
      }
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> map) {
      synchronized (mutex) {
        delegate().putAll(map);
      }
    }

    @Override
    public V remove(final Object key) {
      synchronized (mutex) {
        return delegate().remove(key);
      }
    }

    @Override
    public int size() {
      synchronized (mutex) {
        return delegate().size();
      }
    }

    @Override
    public Collection<V> values() {
      synchronized (mutex) {
        if (values == null) {
          values = collection(delegate().values(), mutex);
        }
        return values;
      }
    }

    @Override
    public boolean equals(final Object o) {
      if (o == this) {
        return true;
      }
      synchronized (mutex) {
        return delegate().equals(o);
      }
    }

    @Override
    public int hashCode() {
      synchronized (mutex) {
        return delegate().hashCode();
      }
    }

    private static final long serialVersionUID = 0;
  }

  static <K, V> SortedMap<K, V> sortedMap(final SortedMap<K, V> sortedMap, final @Nullable Object mutex) {
    return new SynchronizedSortedMap<>(sortedMap, mutex);
  }

  static class SynchronizedSortedMap<K, V> extends SynchronizedMap<K, V>
      implements SortedMap<K, V> {

    SynchronizedSortedMap(final SortedMap<K, V> delegate, final @Nullable Object mutex) {
      super(delegate, mutex);
    }

    @Override
    SortedMap<K, V> delegate() {
      return (SortedMap<K, V>) super.delegate();
    }

    @Override
    public Comparator<? super K> comparator() {
      synchronized (mutex) {
        return delegate().comparator();
      }
    }

    @Override
    public K firstKey() {
      synchronized (mutex) {
        return delegate().firstKey();
      }
    }

    @Override
    public SortedMap<K, V> headMap(final K toKey) {
      synchronized (mutex) {
        return sortedMap(delegate().headMap(toKey), mutex);
      }
    }

    @Override
    public K lastKey() {
      synchronized (mutex) {
        return delegate().lastKey();
      }
    }

    @Override
    public SortedMap<K, V> subMap(final K fromKey, final K toKey) {
      synchronized (mutex) {
        return sortedMap(delegate().subMap(fromKey, toKey), mutex);
      }
    }

    @Override
    public SortedMap<K, V> tailMap(final K fromKey) {
      synchronized (mutex) {
        return sortedMap(delegate().tailMap(fromKey), mutex);
      }
    }

    private static final long serialVersionUID = 0;
  }

  static <K, V> BiMap<K, V> biMap(final BiMap<K, V> bimap, final @Nullable Object mutex) {
    if (bimap instanceof SynchronizedBiMap || bimap instanceof ImmutableBiMap) {
      return bimap;
    }
    return new SynchronizedBiMap<>(bimap, mutex, null);
  }

  @VisibleForTesting
  static class SynchronizedBiMap<K, V> extends SynchronizedMap<K, V>
      implements BiMap<K, V>, Serializable {
    private transient Set<V> valueSet;
    @RetainedWith
    private transient BiMap<V, K> inverse;

    private SynchronizedBiMap(
        final BiMap<K, V> delegate, final @Nullable Object mutex, final @Nullable BiMap<V, K> inverse) {
      super(delegate, mutex);
      this.inverse = inverse;
    }

    @Override
    BiMap<K, V> delegate() {
      return (BiMap<K, V>) super.delegate();
    }

    @Override
    public Set<V> values() {
      synchronized (mutex) {
        if (valueSet == null) {
          valueSet = set(delegate().values(), mutex);
        }
        return valueSet;
      }
    }

    @Override
    public V forcePut(final K key, final V value) {
      synchronized (mutex) {
        return delegate().forcePut(key, value);
      }
    }

    @Override
    public BiMap<V, K> inverse() {
      synchronized (mutex) {
        if (inverse == null) {
          inverse = new SynchronizedBiMap<>(delegate().inverse(), mutex, this);
        }
        return inverse;
      }
    }

    private static final long serialVersionUID = 0;
  }

  private static class SynchronizedAsMap<K, V> extends SynchronizedMap<K, Collection<V>> {
    transient Set<Map.Entry<K, Collection<V>>> asMapEntrySet;
    transient Collection<Collection<V>> asMapValues;

    SynchronizedAsMap(final Map<K, Collection<V>> delegate, final @Nullable Object mutex) {
      super(delegate, mutex);
    }

    @Override
    public Collection<V> get(final Object key) {
      synchronized (mutex) {
        Collection<V> collection = super.get(key);
        return (collection == null) ? null : typePreservingCollection(collection, mutex);
      }
    }

    @Override
    public Set<Map.Entry<K, Collection<V>>> entrySet() {
      synchronized (mutex) {
        if (asMapEntrySet == null) {
          asMapEntrySet = new SynchronizedAsMapEntries<>(delegate().entrySet(), mutex);
        }
        return asMapEntrySet;
      }
    }

    @Override
    public Collection<Collection<V>> values() {
      synchronized (mutex) {
        if (asMapValues == null) {
          asMapValues = new SynchronizedAsMapValues<V>(delegate().values(), mutex);
        }
        return asMapValues;
      }
    }

    @Override
    public boolean containsValue(final Object o) {
      // values() and its contains() method are both synchronized.
      return values().contains(o);
    }

    private static final long serialVersionUID = 0;
  }

  private static class SynchronizedAsMapValues<V> extends SynchronizedCollection<Collection<V>> {
    SynchronizedAsMapValues(final Collection<Collection<V>> delegate, final @Nullable Object mutex) {
      super(delegate, mutex);
    }

    @Override
    public Iterator<Collection<V>> iterator() {
      // Must be manually synchronized.
      return new TransformedIterator<Collection<V>, Collection<V>>(super.iterator()) {
        @Override
        Collection<V> transform(final Collection<V> from) {
          return typePreservingCollection(from, mutex);
        }
      };
    }

    private static final long serialVersionUID = 0;
  }

  @GwtIncompatible // NavigableSet
  @VisibleForTesting
  static class SynchronizedNavigableSet<E> extends SynchronizedSortedSet<E>
      implements NavigableSet<E> {
    SynchronizedNavigableSet(final NavigableSet<E> delegate, final @Nullable Object mutex) {
      super(delegate, mutex);
    }

    @Override
    NavigableSet<E> delegate() {
      return (NavigableSet<E>) super.delegate();
    }

    @Override
    public E ceiling(final E e) {
      synchronized (mutex) {
        return delegate().ceiling(e);
      }
    }

    @Override
    public Iterator<E> descendingIterator() {
      return delegate().descendingIterator(); // manually synchronized
    }

    transient NavigableSet<E> descendingSet;

    @Override
    public NavigableSet<E> descendingSet() {
      synchronized (mutex) {
        if (descendingSet == null) {
          NavigableSet<E> dS = Synchronized.navigableSet(delegate().descendingSet(), mutex);
          descendingSet = dS;
          return dS;
        }
        return descendingSet;
      }
    }

    @Override
    public E floor(final E e) {
      synchronized (mutex) {
        return delegate().floor(e);
      }
    }

    @Override
    public NavigableSet<E> headSet(final E toElement, final boolean inclusive) {
      synchronized (mutex) {
        return Synchronized.navigableSet(delegate().headSet(toElement, inclusive), mutex);
      }
    }

    @Override
    public E higher(final E e) {
      synchronized (mutex) {
        return delegate().higher(e);
      }
    }

    @Override
    public E lower(final E e) {
      synchronized (mutex) {
        return delegate().lower(e);
      }
    }

    @Override
    public E pollFirst() {
      synchronized (mutex) {
        return delegate().pollFirst();
      }
    }

    @Override
    public E pollLast() {
      synchronized (mutex) {
        return delegate().pollLast();
      }
    }

    @Override
    public NavigableSet<E> subSet(
        final E fromElement, final boolean fromInclusive, final E toElement, final boolean toInclusive) {
      synchronized (mutex) {
        return Synchronized.navigableSet(
            delegate().subSet(fromElement, fromInclusive, toElement, toInclusive), mutex);
      }
    }

    @Override
    public NavigableSet<E> tailSet(final E fromElement, final boolean inclusive) {
      synchronized (mutex) {
        return Synchronized.navigableSet(delegate().tailSet(fromElement, inclusive), mutex);
      }
    }

    @Override
    public SortedSet<E> headSet(final E toElement) {
      return headSet(toElement, false);
    }

    @Override
    public SortedSet<E> subSet(final E fromElement, final E toElement) {
      return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<E> tailSet(final E fromElement) {
      return tailSet(fromElement, true);
    }

    private static final long serialVersionUID = 0;
  }

  @GwtIncompatible // NavigableSet
  static <E> NavigableSet<E> navigableSet(final NavigableSet<E> navigableSet, final @Nullable Object mutex) {
    return new SynchronizedNavigableSet<E>(navigableSet, mutex);
  }

  @GwtIncompatible // NavigableSet
  static <E> NavigableSet<E> navigableSet(final NavigableSet<E> navigableSet) {
    return navigableSet(navigableSet, null);
  }

  @GwtIncompatible // NavigableMap
  static <K, V> NavigableMap<K, V> navigableMap(final NavigableMap<K, V> navigableMap) {
    return navigableMap(navigableMap, null);
  }

  @GwtIncompatible // NavigableMap
  static <K, V> NavigableMap<K, V> navigableMap(
      final NavigableMap<K, V> navigableMap, final @Nullable Object mutex) {
    return new SynchronizedNavigableMap<>(navigableMap, mutex);
  }

  @GwtIncompatible // NavigableMap
  @VisibleForTesting
  static class SynchronizedNavigableMap<K, V> extends SynchronizedSortedMap<K, V>
      implements NavigableMap<K, V> {

    SynchronizedNavigableMap(final NavigableMap<K, V> delegate, final @Nullable Object mutex) {
      super(delegate, mutex);
    }

    @Override
    NavigableMap<K, V> delegate() {
      return (NavigableMap<K, V>) super.delegate();
    }

    @Override
    public Entry<K, V> ceilingEntry(final K key) {
      synchronized (mutex) {
        return nullableSynchronizedEntry(delegate().ceilingEntry(key), mutex);
      }
    }

    @Override
    public K ceilingKey(final K key) {
      synchronized (mutex) {
        return delegate().ceilingKey(key);
      }
    }

    transient NavigableSet<K> descendingKeySet;

    @Override
    public NavigableSet<K> descendingKeySet() {
      synchronized (mutex) {
        if (descendingKeySet == null) {
          return descendingKeySet = Synchronized.navigableSet(delegate().descendingKeySet(), mutex);
        }
        return descendingKeySet;
      }
    }

    transient NavigableMap<K, V> descendingMap;

    @Override
    public NavigableMap<K, V> descendingMap() {
      synchronized (mutex) {
        if (descendingMap == null) {
          return descendingMap = navigableMap(delegate().descendingMap(), mutex);
        }
        return descendingMap;
      }
    }

    @Override
    public Entry<K, V> firstEntry() {
      synchronized (mutex) {
        return nullableSynchronizedEntry(delegate().firstEntry(), mutex);
      }
    }

    @Override
    public Entry<K, V> floorEntry(final K key) {
      synchronized (mutex) {
        return nullableSynchronizedEntry(delegate().floorEntry(key), mutex);
      }
    }

    @Override
    public K floorKey(final K key) {
      synchronized (mutex) {
        return delegate().floorKey(key);
      }
    }

    @Override
    public NavigableMap<K, V> headMap(final K toKey, final boolean inclusive) {
      synchronized (mutex) {
        return navigableMap(delegate().headMap(toKey, inclusive), mutex);
      }
    }

    @Override
    public Entry<K, V> higherEntry(final K key) {
      synchronized (mutex) {
        return nullableSynchronizedEntry(delegate().higherEntry(key), mutex);
      }
    }

    @Override
    public K higherKey(final K key) {
      synchronized (mutex) {
        return delegate().higherKey(key);
      }
    }

    @Override
    public Entry<K, V> lastEntry() {
      synchronized (mutex) {
        return nullableSynchronizedEntry(delegate().lastEntry(), mutex);
      }
    }

    @Override
    public Entry<K, V> lowerEntry(final K key) {
      synchronized (mutex) {
        return nullableSynchronizedEntry(delegate().lowerEntry(key), mutex);
      }
    }

    @Override
    public K lowerKey(final K key) {
      synchronized (mutex) {
        return delegate().lowerKey(key);
      }
    }

    @Override
    public Set<K> keySet() {
      return navigableKeySet();
    }

    transient NavigableSet<K> navigableKeySet;

    @Override
    public NavigableSet<K> navigableKeySet() {
      synchronized (mutex) {
        if (navigableKeySet == null) {
          return navigableKeySet = Synchronized.navigableSet(delegate().navigableKeySet(), mutex);
        }
        return navigableKeySet;
      }
    }

    @Override
    public Entry<K, V> pollFirstEntry() {
      synchronized (mutex) {
        return nullableSynchronizedEntry(delegate().pollFirstEntry(), mutex);
      }
    }

    @Override
    public Entry<K, V> pollLastEntry() {
      synchronized (mutex) {
        return nullableSynchronizedEntry(delegate().pollLastEntry(), mutex);
      }
    }

    @Override
    public NavigableMap<K, V> subMap(
        final K fromKey, final boolean fromInclusive, final K toKey, final boolean toInclusive) {
      synchronized (mutex) {
        return navigableMap(delegate().subMap(fromKey, fromInclusive, toKey, toInclusive), mutex);
      }
    }

    @Override
    public NavigableMap<K, V> tailMap(final K fromKey, final boolean inclusive) {
      synchronized (mutex) {
        return navigableMap(delegate().tailMap(fromKey, inclusive), mutex);
      }
    }

    @Override
    public SortedMap<K, V> headMap(final K toKey) {
      return headMap(toKey, false);
    }

    @Override
    public SortedMap<K, V> subMap(final K fromKey, final K toKey) {
      return subMap(fromKey, true, toKey, false);
    }

    @Override
    public SortedMap<K, V> tailMap(final K fromKey) {
      return tailMap(fromKey, true);
    }

    private static final long serialVersionUID = 0;
  }

  @GwtIncompatible // works but is needed only for NavigableMap
  private static <K, V> Entry<K, V> nullableSynchronizedEntry(
      final @Nullable Entry<K, V> entry, final @Nullable Object mutex) {
    if (entry == null) {
      return null;
    }
    return new SynchronizedEntry<>(entry, mutex);
  }

  @GwtIncompatible // works but is needed only for NavigableMap
  private static class SynchronizedEntry<K, V> extends SynchronizedObject implements Entry<K, V> {

    SynchronizedEntry(final Entry<K, V> delegate, final @Nullable Object mutex) {
      super(delegate, mutex);
    }

    @SuppressWarnings("unchecked") // guaranteed by the constructor
    @Override
    Entry<K, V> delegate() {
      return (Entry<K, V>) super.delegate();
    }

    @Override
    public boolean equals(final Object obj) {
      synchronized (mutex) {
        return delegate().equals(obj);
      }
    }

    @Override
    public int hashCode() {
      synchronized (mutex) {
        return delegate().hashCode();
      }
    }

    @Override
    public K getKey() {
      synchronized (mutex) {
        return delegate().getKey();
      }
    }

    @Override
    public V getValue() {
      synchronized (mutex) {
        return delegate().getValue();
      }
    }

    @Override
    public V setValue(final V value) {
      synchronized (mutex) {
        return delegate().setValue(value);
      }
    }

    private static final long serialVersionUID = 0;
  }

  static <E> Queue<E> queue(final Queue<E> queue, final @Nullable Object mutex) {
    return (queue instanceof SynchronizedQueue) ? queue : new SynchronizedQueue<E>(queue, mutex);
  }

  private static class SynchronizedQueue<E> extends SynchronizedCollection<E> implements Queue<E> {

    SynchronizedQueue(final Queue<E> delegate, final @Nullable Object mutex) {
      super(delegate, mutex);
    }

    @Override
    Queue<E> delegate() {
      return (Queue<E>) super.delegate();
    }

    @Override
    public E element() {
      synchronized (mutex) {
        return delegate().element();
      }
    }

    @Override
    public boolean offer(final E e) {
      synchronized (mutex) {
        return delegate().offer(e);
      }
    }

    @Override
    public E peek() {
      synchronized (mutex) {
        return delegate().peek();
      }
    }

    @Override
    public E poll() {
      synchronized (mutex) {
        return delegate().poll();
      }
    }

    @Override
    public E remove() {
      synchronized (mutex) {
        return delegate().remove();
      }
    }

    private static final long serialVersionUID = 0;
  }

  static <E> Deque<E> deque(final Deque<E> deque, final @Nullable Object mutex) {
    return new SynchronizedDeque<E>(deque, mutex);
  }

  private static final class SynchronizedDeque<E> extends SynchronizedQueue<E> implements Deque<E> {

    SynchronizedDeque(final Deque<E> delegate, final @Nullable Object mutex) {
      super(delegate, mutex);
    }

    @Override
    Deque<E> delegate() {
      return (Deque<E>) super.delegate();
    }

    @Override
    public void addFirst(final E e) {
      synchronized (mutex) {
        delegate().addFirst(e);
      }
    }

    @Override
    public void addLast(final E e) {
      synchronized (mutex) {
        delegate().addLast(e);
      }
    }

    @Override
    public boolean offerFirst(final E e) {
      synchronized (mutex) {
        return delegate().offerFirst(e);
      }
    }

    @Override
    public boolean offerLast(final E e) {
      synchronized (mutex) {
        return delegate().offerLast(e);
      }
    }

    @Override
    public E removeFirst() {
      synchronized (mutex) {
        return delegate().removeFirst();
      }
    }

    @Override
    public E removeLast() {
      synchronized (mutex) {
        return delegate().removeLast();
      }
    }

    @Override
    public E pollFirst() {
      synchronized (mutex) {
        return delegate().pollFirst();
      }
    }

    @Override
    public E pollLast() {
      synchronized (mutex) {
        return delegate().pollLast();
      }
    }

    @Override
    public E getFirst() {
      synchronized (mutex) {
        return delegate().getFirst();
      }
    }

    @Override
    public E getLast() {
      synchronized (mutex) {
        return delegate().getLast();
      }
    }

    @Override
    public E peekFirst() {
      synchronized (mutex) {
        return delegate().peekFirst();
      }
    }

    @Override
    public E peekLast() {
      synchronized (mutex) {
        return delegate().peekLast();
      }
    }

    @Override
    public boolean removeFirstOccurrence(final Object o) {
      synchronized (mutex) {
        return delegate().removeFirstOccurrence(o);
      }
    }

    @Override
    public boolean removeLastOccurrence(final Object o) {
      synchronized (mutex) {
        return delegate().removeLastOccurrence(o);
      }
    }

    @Override
    public void push(final E e) {
      synchronized (mutex) {
        delegate().push(e);
      }
    }

    @Override
    public E pop() {
      synchronized (mutex) {
        return delegate().pop();
      }
    }

    @Override
    public Iterator<E> descendingIterator() {
      synchronized (mutex) {
        return delegate().descendingIterator();
      }
    }

    private static final long serialVersionUID = 0;
  }
  
  static <R, C, V> Table<R, C, V> table(final Table<R, C, V> table, final Object mutex) {
    return new SynchronizedTable<>(table, mutex);
  }

  private static final class SynchronizedTable<R, C, V> extends SynchronizedObject
      implements Table<R, C, V> {

    SynchronizedTable(final Table<R, C, V> delegate, final Object mutex) {
      super(delegate, mutex);
    }

    @SuppressWarnings("unchecked")
    @Override
    Table<R, C, V> delegate() {
      return (Table<R, C, V>) super.delegate();
    }

    @Override
    public boolean contains(final @Nullable Object rowKey, final @Nullable Object columnKey) {
      synchronized (mutex) {
        return delegate().contains(rowKey, columnKey);
      }
    }

    @Override
    public boolean containsRow(final @Nullable Object rowKey) {
      synchronized (mutex) {
        return delegate().containsRow(rowKey);
      }
    }

    @Override
    public boolean containsColumn(final @Nullable Object columnKey) {
      synchronized (mutex) {
        return delegate().containsColumn(columnKey);
      }
    }

    @Override
    public boolean containsValue(final @Nullable Object value) {
      synchronized (mutex) {
        return delegate().containsValue(value);
      }
    }

    @Override
    public V get(final @Nullable Object rowKey, final @Nullable Object columnKey) {
      synchronized (mutex) {
        return delegate().get(rowKey, columnKey);
      }
    }

    @Override
    public boolean isEmpty() {
      synchronized (mutex) {
        return delegate().isEmpty();
      }
    }

    @Override
    public int size() {
      synchronized (mutex) {
        return delegate().size();
      }
    }

    @Override
    public void clear() {
      synchronized (mutex) {
        delegate().clear();
      }
    }

    @Override
    public V put(final @Nullable R rowKey, final @Nullable C columnKey, final @Nullable V value) {
      synchronized (mutex) {
        return delegate().put(rowKey, columnKey, value);
      }
    }

    @Override
    public void putAll(final Table<? extends R, ? extends C, ? extends V> table) {
      synchronized (mutex) {
        delegate().putAll(table);
      }
    }

    @Override
    public V remove(final @Nullable Object rowKey, final @Nullable Object columnKey) {
      synchronized (mutex) {
        return delegate().remove(rowKey, columnKey);
      }
    }

    @Override
    public Map<C, V> row(final @Nullable R rowKey) {
      synchronized (mutex) {
        return map(delegate().row(rowKey), mutex);
      }
    }

    @Override
    public Map<R, V> column(final @Nullable C columnKey) {
      synchronized (mutex) {
        return map(delegate().column(columnKey), mutex);
      }
    }

    @Override
    public Set<Cell<R, C, V>> cellSet() {
      synchronized (mutex) {
        return set(delegate().cellSet(), mutex);
      }
    }

    @Override
    public Set<R> rowKeySet() {
      synchronized (mutex) {
        return set(delegate().rowKeySet(), mutex);
      }
    }

    @Override
    public Set<C> columnKeySet() {
      synchronized (mutex) {
        return set(delegate().columnKeySet(), mutex);
      }
    }

    @Override
    public Collection<V> values() {
      synchronized (mutex) {
        return collection(delegate().values(), mutex);
      }
    }

    @Override
    public Map<R, Map<C, V>> rowMap() {
      synchronized (mutex) {
        return map(
            Maps.transformValues(
                delegate().rowMap(),
                new com.google.common.base.Function<Map<C, V>, Map<C, V>>() {
                  @Override
                  public Map<C, V> apply(final Map<C, V> t) {
                    return map(t, mutex);
                  }
                }),
            mutex);
      }
    }

    @Override
    public Map<C, Map<R, V>> columnMap() {
      synchronized (mutex) {
        return map(
            Maps.transformValues(
                delegate().columnMap(),
                new com.google.common.base.Function<Map<R, V>, Map<R, V>>() {
                  @Override
                  public Map<R, V> apply(final Map<R, V> t) {
                    return map(t, mutex);
                  }
                }),
            mutex);
      }
    }

    @Override
    public int hashCode() {
      synchronized (mutex) {
        return delegate().hashCode();
      }
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
      if (this == obj) {
        return true;
      }
      synchronized (mutex) {
        return delegate().equals(obj);
      }
    }
  }
}
