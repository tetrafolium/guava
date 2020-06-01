/*
 * Copyright (C) 2008 The Guava Authors
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
import com.google.common.collect.Multiset.Entry;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.concurrent.LazyInit;
import com.google.j2objc.annotations.WeakOuter;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import javax.annotation.Nullable;

/**
 * A {@link Multiset} whose contents will never change, with many other important properties
 * detailed at {@link ImmutableCollection}.
 *
 * <p><b>Grouped iteration.</b> In all current implementations, duplicate elements always appear
 * consecutively when iterating. Elements iterate in order by the <i>first</i> appearance of
 * that element when the multiset was created.
 *
 * <p>See the Guava User Guide article on <a href=
 * "https://github.com/google/guava/wiki/ImmutableCollectionsExplained">
 * immutable collections</a>.
 *
 * @author Jared Levy
 * @author Louis Wasserman
 * @since 2.0
 */
@GwtCompatible(serializable = true, emulated = true)
@SuppressWarnings("serial") // we're overriding default serialization
public abstract class ImmutableMultiset<E> extends ImmutableMultisetGwtSerializationDependencies<E>
    implements Multiset<E> {
  /**
   * Returns the empty immutable multiset.
   */
  @SuppressWarnings("unchecked") // all supported methods are covariant
  public static <E> ImmutableMultiset<E> of() {
    return (ImmutableMultiset<E>) RegularImmutableMultiset.EMPTY;
  }

  /**
   * Returns an immutable multiset containing a single element.
   *
   * @throws NullPointerException if {@code element} is null
   * @since 6.0 (source-compatible since 2.0)
   */
  @SuppressWarnings("unchecked") // generic array created but never written
  public static <E> ImmutableMultiset<E> of(final E element) {
    return copyFromElements(element);
  }

  /**
   * Returns an immutable multiset containing the given elements, in order.
   *
   * @throws NullPointerException if any element is null
   * @since 6.0 (source-compatible since 2.0)
   */
  @SuppressWarnings("unchecked") //
  public static <E> ImmutableMultiset<E> of(final E e1, final E e2) {
    return copyFromElements(e1, e2);
  }

  /**
   * Returns an immutable multiset containing the given elements, in the "grouped iteration order"
   * described in the class documentation.
   *
   * @throws NullPointerException if any element is null
   * @since 6.0 (source-compatible since 2.0)
   */
  @SuppressWarnings("unchecked") //
  public static <E> ImmutableMultiset<E> of(final E e1, final E e2, final E e3) {
    return copyFromElements(e1, e2, e3);
  }

  /**
   * Returns an immutable multiset containing the given elements, in the "grouped iteration order"
   * described in the class documentation.
   *
   * @throws NullPointerException if any element is null
   * @since 6.0 (source-compatible since 2.0)
   */
  @SuppressWarnings("unchecked") //
  public static <E> ImmutableMultiset<E> of(final E e1, final E e2, final E e3, final E e4) {
    return copyFromElements(e1, e2, e3, e4);
  }

  /**
   * Returns an immutable multiset containing the given elements, in the "grouped iteration order"
   * described in the class documentation.
   *
   * @throws NullPointerException if any element is null
   * @since 6.0 (source-compatible since 2.0)
   */
  @SuppressWarnings("unchecked") //
  public static <E> ImmutableMultiset<E> of(final E e1, final E e2, final E e3, final E e4, final E e5) {
    return copyFromElements(e1, e2, e3, e4, e5);
  }

  /**
   * Returns an immutable multiset containing the given elements, in the "grouped iteration order"
   * described in the class documentation.
   *
   * @throws NullPointerException if any element is null
   * @since 6.0 (source-compatible since 2.0)
   */
  @SuppressWarnings("unchecked") //
  public static <E> ImmutableMultiset<E> of(final E e1, final E e2, final E e3, final E e4, final E e5, final E e6, final E... others) {
    return new Builder<E>().add(e1).add(e2).add(e3).add(e4).add(e5).add(e6).add(others).build();
  }

  /**
   * Returns an immutable multiset containing the given elements, in the "grouped iteration order"
   * described in the class documentation.
   *
   * @throws NullPointerException if any of {@code elements} is null
   * @since 6.0
   */
  public static <E> ImmutableMultiset<E> copyOf(final E[] elements) {
    return copyFromElements(elements);
  }

  /**
   * Returns an immutable multiset containing the given elements, in the "grouped iteration order"
   * described in the class documentation.
   *
   * @throws NullPointerException if any of {@code elements} is null
   */
  public static <E> ImmutableMultiset<E> copyOf(final Iterable<? extends E> elements) {
    if (elements instanceof ImmutableMultiset) {
      @SuppressWarnings("unchecked") // all supported methods are covariant
      ImmutableMultiset<E> result = (ImmutableMultiset<E>) elements;
      if (!result.isPartialView()) {
        return result;
      }
    }

    ImmutableMultiset.Builder<E> builder =
        new ImmutableMultiset.Builder<E>(Multisets.inferDistinctElements(elements));
    builder.addAll(elements);
    return builder.build();
  }

  private static <E> ImmutableMultiset<E> copyFromElements(final E... elements) {
    return new ImmutableMultiset.Builder<E>().add(elements).build();
  }

  static <E> ImmutableMultiset<E> copyFromEntries(
      final Collection<? extends Entry<? extends E>> entries) {
    ImmutableMultiset.Builder<E> builder = new ImmutableMultiset.Builder<E>(entries.size());
    for (Entry<? extends E> entry : entries) {
      builder.addCopies(entry.getElement(), entry.getCount());
    }
    return builder.build();
  }

  /**
   * Returns an immutable multiset containing the given elements, in the "grouped iteration order"
   * described in the class documentation.
   *
   * @throws NullPointerException if any of {@code elements} is null
   */
  public static <E> ImmutableMultiset<E> copyOf(final Iterator<? extends E> elements) {
    return new ImmutableMultiset.Builder<E>().addAll(elements).build();
  }

  ImmutableMultiset() { }

  @Override
  public UnmodifiableIterator<E> iterator() {
    final Iterator<Entry<E>> entryIterator = entrySet().iterator();
    return new UnmodifiableIterator<E>() {
      int remaining;
      E element;

      @Override
      public boolean hasNext() {
        return (remaining > 0) || entryIterator.hasNext();
      }

      @Override
      public E next() {
        if (remaining <= 0) {
          Entry<E> entry = entryIterator.next();
          element = entry.getElement();
          remaining = entry.getCount();
        }
        remaining--;
        return element;
      }
    };
  }

  @LazyInit
  private transient ImmutableList<E> asList;

  @Override
  public ImmutableList<E> asList() {
    ImmutableList<E> result = asList;
    return (result == null) ? asList = super.asList() : result;
  }

  @Override
  public boolean contains(final @Nullable Object object) {
    return count(object) > 0;
  }

  /**
   * Guaranteed to throw an exception and leave the collection unmodified.
   *
   * @throws UnsupportedOperationException always
   * @deprecated Unsupported operation.
   */
  @CanIgnoreReturnValue
  @Deprecated
  @Override
  public final int add(final E element, final int occurrences) {
    throw new UnsupportedOperationException();
  }

  /**
   * Guaranteed to throw an exception and leave the collection unmodified.
   *
   * @throws UnsupportedOperationException always
   * @deprecated Unsupported operation.
   */
  @CanIgnoreReturnValue
  @Deprecated
  @Override
  public final int remove(final Object element, final int occurrences) {
    throw new UnsupportedOperationException();
  }

  /**
   * Guaranteed to throw an exception and leave the collection unmodified.
   *
   * @throws UnsupportedOperationException always
   * @deprecated Unsupported operation.
   */
  @CanIgnoreReturnValue
  @Deprecated
  @Override
  public final int setCount(final E element, final int count) {
    throw new UnsupportedOperationException();
  }

  /**
   * Guaranteed to throw an exception and leave the collection unmodified.
   *
   * @throws UnsupportedOperationException always
   * @deprecated Unsupported operation.
   */
  @CanIgnoreReturnValue
  @Deprecated
  @Override
  public final boolean setCount(final E element, final int oldCount, final int newCount) {
    throw new UnsupportedOperationException();
  }

  @GwtIncompatible // not present in emulated superclass
  @Override
  int copyIntoArray(final Object[] dst, final int offset) {
    for (Multiset.Entry<E> entry : entrySet()) {
      Arrays.fill(dst, offset, offset + entry.getCount(), entry.getElement());
      offset += entry.getCount();
    }
    return offset;
  }

  @Override
  public boolean equals(final @Nullable Object object) {
    return Multisets.equalsImpl(this, object);
  }

  @Override
  public int hashCode() {
    return Sets.hashCodeImpl(entrySet());
  }

  @Override
  public String toString() {
    return entrySet().toString();
  }

  /** @since 21.0 (present with return type {@code Set} since 2.0) */
  @Override
  public abstract ImmutableSet<E> elementSet();

  @LazyInit
  private transient ImmutableSet<Entry<E>> entrySet;

  @Override
  public ImmutableSet<Entry<E>> entrySet() {
    ImmutableSet<Entry<E>> es = entrySet;
    return (es == null) ? (entrySet = createEntrySet()) : es;
  }

  private final ImmutableSet<Entry<E>> createEntrySet() {
    return isEmpty() ? ImmutableSet.<Entry<E>>of() : new EntrySet();
  }

  abstract Entry<E> getEntry(int index);

  @WeakOuter
  private final class EntrySet extends ImmutableSet.Indexed<Entry<E>> {
    @Override
    boolean isPartialView() {
      return ImmutableMultiset.this.isPartialView();
    }

    @Override
    Entry<E> get(final int index) {
      return getEntry(index);
    }

    @Override
    public int size() {
      return elementSet().size();
    }

    @Override
    public boolean contains(final Object o) {
      if (o instanceof Entry) {
        Entry<?> entry = (Entry<?>) o;
        if (entry.getCount() <= 0) {
          return false;
        }
        int count = count(entry.getElement());
        return count == entry.getCount();
      }
      return false;
    }

    @Override
    public int hashCode() {
      return ImmutableMultiset.this.hashCode();
    }

    // We can't label this with @Override, because it doesn't override anything
    // in the GWT emulated version.
    // TODO(cpovirk): try making all copies of this method @GwtIncompatible instead
    Object writeReplace() {
      return new EntrySetSerializedForm<E>(ImmutableMultiset.this);
    }

    private static final long serialVersionUID = 0;
  }

  static class EntrySetSerializedForm<E> implements Serializable {
    final ImmutableMultiset<E> multiset;

    EntrySetSerializedForm(final ImmutableMultiset<E> multiset) {
      this.multiset = multiset;
    }

    Object readResolve() {
      return multiset.entrySet();
    }
  }

  private static class SerializedForm implements Serializable {
    final Object[] elements;
    final int[] counts;

    SerializedForm(final Multiset<?> multiset) {
      int distinct = multiset.entrySet().size();
      elements = new Object[distinct];
      counts = new int[distinct];
      int i = 0;
      for (Entry<?> entry : multiset.entrySet()) {
        elements[i] = entry.getElement();
        counts[i] = entry.getCount();
        i++;
      }
    }

    Object readResolve() {
      ImmutableMultiset.Builder<Object> builder =
          new ImmutableMultiset.Builder<Object>(elements.length);
      for (int i = 0; i < elements.length; i++) {
        builder.addCopies(elements[i], counts[i]);
      }
      return builder.build();
    }

    private static final long serialVersionUID = 0;
  }

  // We can't label this with @Override, because it doesn't override anything
  // in the GWT emulated version.
  Object writeReplace() {
    return new SerializedForm(this);
  }

  /**
   * Returns a new builder. The generated builder is equivalent to the builder
   * created by the {@link Builder} constructor.
   */
  public static <E> Builder<E> builder() {
    return new Builder<E>();
  }

  /**
   * A builder for creating immutable multiset instances, especially {@code
   * public static final} multisets ("constant multisets"). Example:
   * <pre> {@code
   *
   *   public static final ImmutableMultiset<Bean> BEANS =
   *       new ImmutableMultiset.Builder<Bean>()
   *           .addCopies(Bean.COCOA, 4)
   *           .addCopies(Bean.GARDEN, 6)
   *           .addCopies(Bean.RED, 8)
   *           .addCopies(Bean.BLACK_EYED, 10)
   *           .build();}</pre>
   *
   * <p>Builder instances can be reused; it is safe to call {@link #build} multiple
   * times to build multiple multisets in series.
   *
   * @since 2.0
   */
  public static class Builder<E> extends ImmutableCollection.Builder<E> {
    AbstractObjectCountMap<E> contents;

    /**
     * If build() has been called on the current contents multiset, we need to copy it on any
     * future modifications, or we'll modify the already-built ImmutableMultiset.
     */
    boolean buildInvoked = false;
    /**
     * In the event of a setCount(elem, 0) call, we may need to remove elements, which destroys the
     * insertion order property of ObjectCountHashMap.  In that event, we need to convert to a
     * ObjectCountLinkedHashMap, but we need to know we did that so we can convert back.
     */
    boolean isLinkedHash = false;

    /**
     * Creates a new builder. The returned builder is equivalent to the builder
     * generated by {@link ImmutableMultiset#builder}.
     */
    public Builder() {
      this(4);
    }

    Builder(final int estimatedDistinct) {
      this.contents = ObjectCountHashMap.createWithExpectedSize(estimatedDistinct);
    }
    
    Builder(final boolean forSubtype) {
      // for ImmutableSortedMultiset not to allocate data structures not used there
      this.contents = null;
    }

    /**
     * Adds {@code element} to the {@code ImmutableMultiset}.
     *
     * @param element the element to add
     * @return this {@code Builder} object
     * @throws NullPointerException if {@code element} is null
     */
    @CanIgnoreReturnValue
    @Override
    public Builder<E> add(final E element) {
      return addCopies(element, 1);
    }

    /**
     * Adds a number of occurrences of an element to this {@code
     * ImmutableMultiset}.
     *
     * @param element the element to add
     * @param occurrences the number of occurrences of the element to add. May
     *     be zero, in which case no change will be made.
     * @return this {@code Builder} object
     * @throws NullPointerException if {@code element} is null
     * @throws IllegalArgumentException if {@code occurrences} is negative, or
     *     if this operation would result in more than {@link Integer#MAX_VALUE}
     *     occurrences of the element
     */
    @CanIgnoreReturnValue
    public Builder<E> addCopies(final E element, final int occurrences) {
      if (occurrences == 0) {
        return this;
      }
      if (buildInvoked) {
        contents = new ObjectCountHashMap<E>(contents);
        isLinkedHash = false;
      }
      buildInvoked = false;
      checkNotNull(element);
      contents.put(element, occurrences + contents.get(element));
      return this;
    }

    /**
     * Adds or removes the necessary occurrences of an element such that the
     * element attains the desired count.
     *
     * @param element the element to add or remove occurrences of
     * @param count the desired count of the element in this multiset
     * @return this {@code Builder} object
     * @throws NullPointerException if {@code element} is null
     * @throws IllegalArgumentException if {@code count} is negative
     */
    @CanIgnoreReturnValue
    public Builder<E> setCount(final E element, final int count) {
      if (count == 0 && !isLinkedHash) {
        contents = new ObjectCountLinkedHashMap<E>(contents);
        isLinkedHash = true;
        // to preserve insertion order through deletions, we have to switch to an actual linked
        // implementation at least for now, but this should be a super rare case
      } else if (buildInvoked) {
        contents = new ObjectCountHashMap<E>(contents);
        isLinkedHash = false;
      }
      buildInvoked = false;
      checkNotNull(element);
      if (count == 0) {
        contents.remove(element);
      } else {
        contents.put(checkNotNull(element), count);
      }
      return this;
    }

    /**
     * Adds each element of {@code elements} to the {@code ImmutableMultiset}.
     *
     * @param elements the elements to add
     * @return this {@code Builder} object
     * @throws NullPointerException if {@code elements} is null or contains a
     *     null element
     */
    @CanIgnoreReturnValue
    @Override
    public Builder<E> add(final E... elements) {
      super.add(elements);
      return this;
    }

    /**
     * Adds each element of {@code elements} to the {@code ImmutableMultiset}.
     *
     * @param elements the {@code Iterable} to add to the {@code
     *     ImmutableMultiset}
     * @return this {@code Builder} object
     * @throws NullPointerException if {@code elements} is null or contains a
     *     null element
     */
    @CanIgnoreReturnValue
    @Override
    public Builder<E> addAll(final Iterable<? extends E> elements) {
      if (elements instanceof Multiset) {
        Multiset<? extends E> multiset = Multisets.cast(elements);
        for (Entry<? extends E> entry : multiset.entrySet()) {
          addCopies(entry.getElement(), entry.getCount());
        }
      } else {
        super.addAll(elements);
      }
      return this;
    }

    /**
     * Adds each element of {@code elements} to the {@code ImmutableMultiset}.
     *
     * @param elements the elements to add to the {@code ImmutableMultiset}
     * @return this {@code Builder} object
     * @throws NullPointerException if {@code elements} is null or contains a
     *     null element
     */
    @CanIgnoreReturnValue
    @Override
    public Builder<E> addAll(final Iterator<? extends E> elements) {
      super.addAll(elements);
      return this;
    }

    /**
     * Returns a newly-created {@code ImmutableMultiset} based on the contents
     * of the {@code Builder}.
     */
    @Override
    public ImmutableMultiset<E> build() {
      if (contents.isEmpty()) {
        return of();
      }
      if (isLinkedHash) {
        // we need ObjectCountHashMap-backed contents, with its keys and values array in direct
        // insertion order
        contents = new ObjectCountHashMap<E>(contents);
        isLinkedHash = false;
      }
      buildInvoked = true;
      // contents is now ObjectCountHashMap, but still guaranteed to be in insertion order!
      return new RegularImmutableMultiset<E>((ObjectCountHashMap<E>) contents);
    }
  }
}
