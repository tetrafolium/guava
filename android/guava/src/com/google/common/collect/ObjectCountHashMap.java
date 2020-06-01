/*
 * Copyright (C) 2017 The Guava Authors
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

import static com.google.common.collect.CollectPreconditions.checkPositive;
import static com.google.common.collect.Hashing.smearedHash;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Multiset.Entry;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.j2objc.annotations.WeakOuter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * ObjectCountHashMap is an implementation of {@code AbstractObjectCountMap} that uses arrays to
 * store key objects and count values. Comparing to using a traditional {@code HashMap}
 * implementation which stores keys and count values as map entries, {@code ObjectCountHashMap}
 * minimizes object allocation and reduces memory footprint.
 *
 * <p>In the absence of element deletions, this will iterate over elements in insertion order.
 */
@GwtCompatible(serializable = true, emulated = true)
class ObjectCountHashMap<K> extends AbstractObjectCountMap<K> {

  /** Creates an empty {@code ObjectCountHashMap} instance. */
  public static <K> ObjectCountHashMap<K> create() {
    return new ObjectCountHashMap<K>();
  }

  /**
   * Creates a {@code ObjectCountHashMap} instance, with a high enough "initial capacity" that it
   * <i>should</i> hold {@code expectedSize} elements without growth.
   *
   * @param expectedSize the number of elements you expect to add to the returned set
   * @return a new, empty {@code ObjectCountHashMap} with enough capacity to hold {@code
   *     expectedSize} elements without resizing
   * @throws IllegalArgumentException if {@code expectedSize} is negative
   */
  public static <K> ObjectCountHashMap<K> createWithExpectedSize(final int expectedSize) {
    return new ObjectCountHashMap<K>(expectedSize);
  }

  private static final int MAXIMUM_CAPACITY = 1 << 30;

  static final float DEFAULT_LOAD_FACTOR = 1.0f;

  /** Bitmask that selects the low 32 bits. */
  private static final long NEXT_MASK = (1L << 32) - 1;

  /** Bitmask that selects the high 32 bits. */
  private static final long HASH_MASK = ~NEXT_MASK;

  static final int DEFAULT_SIZE = 3;

  // used to indicate blank table entries
  static final int UNSET = -1;

  /**
   * The hashtable. Its values are indexes to the keys, values, and entries arrays.
   *
   * <p>Currently, the UNSET value means "null pointer", and any non negative value x is the actual
   * index.
   *
   * <p>Its size must be a power of two.
   */
  private transient int[] table;

  /**
   * Contains the logical entries, in the range of [0, size()). The high 32 bits of each long is the
   * smeared hash of the element, whereas the low 32 bits is the "next" pointer (pointing to the
   * next entry in the bucket chain). The pointers in [size(), entries.length) are all "null"
   * (UNSET).
   */
  @VisibleForTesting transient long[] entries;

  /** The load factor. */
  private transient float loadFactor;

  /** When we have this many elements, resize the hashtable. */
  private transient int threshold;

  /** Constructs a new empty instance of {@code ObjectCountHashMap}. */
  ObjectCountHashMap() {
    init(DEFAULT_SIZE, DEFAULT_LOAD_FACTOR);
  }

  ObjectCountHashMap(final AbstractObjectCountMap<K> map) {
    init(map.size(), DEFAULT_LOAD_FACTOR);
    for (int i = map.firstIndex(); i != -1; i = map.nextIndex(i)) {
      put(map.getKey(i), map.getValue(i));
    }
  }

  /**
   * Constructs a new instance of {@code ObjectCountHashMap} with the specified capacity.
   *
   * @param capacity the initial capacity of this {@code ObjectCountHashMap}.
   */
  ObjectCountHashMap(final int capacity) {
    this(capacity, DEFAULT_LOAD_FACTOR);
  }

  ObjectCountHashMap(final int expectedSize, final float loadFactor) {
    init(expectedSize, loadFactor);
  }

  void init(final int expectedSize, final float loadFactor) {
    Preconditions.checkArgument(expectedSize >= 0, "Initial capacity must be non-negative");
    Preconditions.checkArgument(loadFactor > 0, "Illegal load factor");
    int buckets = Hashing.closedTableSize(expectedSize, loadFactor);
    this.table = newTable(buckets);
    this.loadFactor = loadFactor;

    this.keys = new Object[expectedSize];
    this.values = new int[expectedSize];

    this.entries = newEntries(expectedSize);
    this.threshold = Math.max(1, (int) (buckets * loadFactor));
  }

  private static int[] newTable(final int size) {
    int[] array = new int[size];
    Arrays.fill(array, UNSET);
    return array;
  }

  private static long[] newEntries(final int size) {
    long[] array = new long[size];
    Arrays.fill(array, UNSET);
    return array;
  }

  private int hashTableMask() {
    return table.length - 1;
  }

  private static int getHash(final long entry) {
    return (int) (entry >>> 32);
  }

  /** Returns the index, or UNSET if the pointer is "null" */
  private static int getNext(final long entry) {
    return (int) entry;
  }

  /** Returns a new entry value by changing the "next" index of an existing entry */
  private static long swapNext(final long entry, final int newNext) {
    return (HASH_MASK & entry) | (NEXT_MASK & newNext);
  }

  @CanIgnoreReturnValue
  @Override
  public int put(final @Nullable K key, final int value) {
    checkPositive(value, "count");
    long[] entries = this.entries;
    Object[] keys = this.keys;
    int[] values = this.values;

    int hash = smearedHash(key);
    int tableIndex = hash & hashTableMask();
    int newEntryIndex = this.size; // current size, and pointer to the entry to be appended
    int next = table[tableIndex];
    if (next == UNSET) {
      table[tableIndex] = newEntryIndex;
    } else {
      int last;
      long entry;
      do {
        last = next;
        entry = entries[next];
        if (getHash(entry) == hash && Objects.equal(key, keys[next])) {
          int oldValue = values[next];

          values[next] = value;
          return oldValue;
        }
        next = getNext(entry);
      } while (next != UNSET);
      entries[last] = swapNext(entry, newEntryIndex);
    }
    if (newEntryIndex == Integer.MAX_VALUE) {
      throw new IllegalStateException("Cannot contain more than Integer.MAX_VALUE elements!");
    }
    int newSize = newEntryIndex + 1;
    resizeMeMaybe(newSize);
    insertEntry(newEntryIndex, key, value, hash);
    this.size = newSize;
    if (newEntryIndex >= threshold) {
      resizeTable(2 * table.length);
    }
    modCount++;
    return 0;
  }

  /**
   * Creates a fresh entry with the specified object at the specified position in the entry array.
   */
  void insertEntry(final int entryIndex, final @Nullable K key, final int value, final int hash) {
    this.entries[entryIndex] = ((long) hash << 32) | (NEXT_MASK & UNSET);
    this.keys[entryIndex] = key;
    this.values[entryIndex] = value;
  }

  /** Returns currentSize + 1, after resizing the entries storage if necessary. */
  private void resizeMeMaybe(final int newSize) {
    int entriesSize = entries.length;
    if (newSize > entriesSize) {
      int newCapacity = entriesSize + Math.max(1, entriesSize >>> 1);
      if (newCapacity < 0) {
        newCapacity = Integer.MAX_VALUE;
      }
      if (newCapacity != entriesSize) {
        resizeEntries(newCapacity);
      }
    }
  }

  /**
   * Resizes the internal entries array to the specified capacity, which may be greater or less than
   * the current capacity.
   */
  void resizeEntries(final int newCapacity) {
    this.keys = Arrays.copyOf(keys, newCapacity);
    this.values = Arrays.copyOf(values, newCapacity);
    long[] entries = this.entries;
    int oldCapacity = entries.length;
    entries = Arrays.copyOf(entries, newCapacity);
    if (newCapacity > oldCapacity) {
      Arrays.fill(entries, oldCapacity, newCapacity, UNSET);
    }
    this.entries = entries;
  }

  private void resizeTable(final int newCapacity) { // newCapacity always a power of two
    int[] oldTable = table;
    int oldCapacity = oldTable.length;
    if (oldCapacity >= MAXIMUM_CAPACITY) {
      threshold = Integer.MAX_VALUE;
      return;
    }
    int newThreshold = 1 + (int) (newCapacity * loadFactor);
    int[] newTable = newTable(newCapacity);
    long[] entries = this.entries;

    int mask = newTable.length - 1;
    for (int i = 0; i < size; i++) {
      long oldEntry = entries[i];
      int hash = getHash(oldEntry);
      int tableIndex = hash & mask;
      int next = newTable[tableIndex];
      newTable[tableIndex] = i;
      entries[i] = ((long) hash << 32) | (NEXT_MASK & next);
    }

    this.threshold = newThreshold;
    this.table = newTable;
  }

  @Override
  int indexOf(final @Nullable Object key) {
    int hash = smearedHash(key);
    int next = table[hash & hashTableMask()];
    while (next != UNSET) {
      long entry = entries[next];
      if (getHash(entry) == hash && Objects.equal(key, keys[next])) {
        return next;
      }
      next = getNext(entry);
    }
    return -1;
  }

  @Override
  public boolean containsKey(final @Nullable Object key) {
    return indexOf(key) != -1;
  }

  @Override
  public int get(final @Nullable Object key) {
    int index = indexOf(key);
    return (index == -1) ? 0 : values[index];
  }

  @CanIgnoreReturnValue
  @Override
  public int remove(final @Nullable Object key) {
    return remove(key, smearedHash(key));
  }

  @CanIgnoreReturnValue
  @Override
  int removeEntry(final int entryIndex) {
    return remove(keys[entryIndex], getHash(entries[entryIndex]));
  }

  private int remove(final @Nullable Object key, final int hash) {
    int tableIndex = hash & hashTableMask();
    int next = table[tableIndex];
    if (next == UNSET) { // empty bucket
      return 0;
    }
    int last = UNSET;
    do {
      if (getHash(entries[next]) == hash) {
        if (Objects.equal(key, keys[next])) {
          int oldValue = values[next];

          if (last == UNSET) {
            // we need to update the root link from table[]
            table[tableIndex] = getNext(entries[next]);
          } else {
            // we need to update the link from the chain
            entries[last] = swapNext(entries[last], getNext(entries[next]));
          }

          moveLastEntry(next);
          size--;
          modCount++;
          return oldValue;
        }
      }
      last = next;
      next = getNext(entries[next]);
    } while (next != UNSET);
    return 0;
  }

  /**
   * Moves the last entry in the entry array into {@code dstIndex}, and nulls out its old position.
   */
  void moveLastEntry(final int dstIndex) {
    int srcIndex = size() - 1;
    if (dstIndex < srcIndex) {
      // move last entry to deleted spot
      keys[dstIndex] = keys[srcIndex];
      values[dstIndex] = values[srcIndex];
      keys[srcIndex] = null;
      values[srcIndex] = 0;

      // move the last entry to the removed spot, just like we moved the element
      long lastEntry = entries[srcIndex];
      entries[dstIndex] = lastEntry;
      entries[srcIndex] = UNSET;

      // also need to update whoever's "next" pointer was pointing to the last entry place
      // reusing "tableIndex" and "next"; these variables were no longer needed
      int tableIndex = getHash(lastEntry) & hashTableMask();
      int lastNext = table[tableIndex];
      if (lastNext == srcIndex) {
        // we need to update the root pointer
        table[tableIndex] = dstIndex;
      } else {
        // we need to update a pointer in an entry
        int previous;
        long entry;
        do {
          previous = lastNext;
          lastNext = getNext(entry = entries[lastNext]);
        } while (lastNext != srcIndex);
        // here, entries[previous] points to the old entry location; update it
        entries[previous] = swapNext(entry, dstIndex);
      }
    } else {
      keys[dstIndex] = null;
      values[dstIndex] = 0;
      entries[dstIndex] = UNSET;
    }
  }

  @Override
  Set<Entry<K>> createEntrySet() {
    return new HashEntrySetView();
  }

  @WeakOuter
  class HashEntrySetView extends EntrySetView {

    @Override
    public Iterator<Entry<K>> iterator() {
      return new Itr<Entry<K>>() {
        @Override
        Entry<K> getOutput(final int entry) {
          return new MapEntry(entry);
        }
      };
    }
  }

  @Override
  public void clear() {
    modCount++;
    Arrays.fill(keys, 0, size, null);
    Arrays.fill(values, 0, size, 0);
    Arrays.fill(table, UNSET);
    Arrays.fill(entries, UNSET);
    this.size = 0;
  }
}
