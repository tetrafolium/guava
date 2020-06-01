/*
 * Copyright (C) 2010 The Guava Authors
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

package com.google.common.collect.testing;

import com.google.common.annotations.GwtIncompatible;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A wrapper around {@code TreeSet} that aggressively checks to see if elements
 * are mutually comparable. This implementation passes the navigable set test
 * suites.
 *
 * @author Louis Wasserman
 */
@GwtIncompatible
public final class SafeTreeSet<E> implements Serializable, NavigableSet<E> {
  @SuppressWarnings("unchecked")
  private static final Comparator<Object> NATURAL_ORDER =
      new Comparator<Object>() {
        @Override
        public int compare(final Object o1, final Object o2) {
          return ((Comparable<Object>) o1).compareTo(o2);
        }
      };

  private final NavigableSet<E> delegate;

  public SafeTreeSet() {
    this(new TreeSet<E>());
  }

  public SafeTreeSet(final Collection<? extends E> collection) {
    this(new TreeSet<E>(collection));
  }

  public SafeTreeSet(final Comparator<? super E> comparator) {
    this(new TreeSet<E>(comparator));
  }

  public SafeTreeSet(final SortedSet<E> set) {
    this(new TreeSet<E>(set));
  }

  private SafeTreeSet(final NavigableSet<E> delegate) {
    this.delegate = delegate;
    for (E e : this) {
      checkValid(e);
    }
  }

  @Override
  public boolean add(final E element) {
    return delegate.add(checkValid(element));
  }

  @Override
  public boolean addAll(final Collection<? extends E> collection) {
    for (E e : collection) {
      checkValid(e);
    }
    return delegate.addAll(collection);
  }

  @Override
  public E ceiling(final E e) {
    return delegate.ceiling(checkValid(e));
  }

  @Override
  public void clear() {
    delegate.clear();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Comparator<? super E> comparator() {
    Comparator<? super E> comparator = delegate.comparator();
    if (comparator == null) {
      comparator = (Comparator<? super E>) NATURAL_ORDER;
    }
    return comparator;
  }

  @Override
  public boolean contains(final Object object) {
    return delegate.contains(checkValid(object));
  }

  @Override
  public boolean containsAll(final Collection<?> c) {
    return delegate.containsAll(c);
  }

  @Override
  public Iterator<E> descendingIterator() {
    return delegate.descendingIterator();
  }

  @Override
  public NavigableSet<E> descendingSet() {
    return new SafeTreeSet<E>(delegate.descendingSet());
  }

  @Override
  public E first() {
    return delegate.first();
  }

  @Override
  public E floor(final E e) {
    return delegate.floor(checkValid(e));
  }

  @Override
  public SortedSet<E> headSet(final E toElement) {
    return headSet(toElement, false);
  }

  @Override
  public NavigableSet<E> headSet(final E toElement, final boolean inclusive) {
    return new SafeTreeSet<E>(delegate.headSet(checkValid(toElement), inclusive));
  }

  @Override
  public E higher(final E e) {
    return delegate.higher(checkValid(e));
  }

  @Override
  public boolean isEmpty() {
    return delegate.isEmpty();
  }

  @Override
  public Iterator<E> iterator() {
    return delegate.iterator();
  }

  @Override
  public E last() {
    return delegate.last();
  }

  @Override
  public E lower(final E e) {
    return delegate.lower(checkValid(e));
  }

  @Override
  public E pollFirst() {
    return delegate.pollFirst();
  }

  @Override
  public E pollLast() {
    return delegate.pollLast();
  }

  @Override
  public boolean remove(final Object object) {
    return delegate.remove(checkValid(object));
  }

  @Override
  public boolean removeAll(final Collection<?> c) {
    return delegate.removeAll(c);
  }

  @Override
  public boolean retainAll(final Collection<?> c) {
    return delegate.retainAll(c);
  }

  @Override
  public int size() {
    return delegate.size();
  }

  @Override
  public NavigableSet<E> subSet(
      final E fromElement, final boolean fromInclusive, final E toElement, final boolean toInclusive) {
    return new SafeTreeSet<E>(
        delegate.subSet(
            checkValid(fromElement), fromInclusive, checkValid(toElement), toInclusive));
  }

  @Override
  public SortedSet<E> subSet(final E fromElement, final E toElement) {
    return subSet(fromElement, true, toElement, false);
  }

  @Override
  public SortedSet<E> tailSet(final E fromElement) {
    return tailSet(fromElement, true);
  }

  @Override
  public NavigableSet<E> tailSet(final E fromElement, final boolean inclusive) {
    return new SafeTreeSet<E>(delegate.tailSet(checkValid(fromElement), inclusive));
  }

  @Override
  public Object[] toArray() {
    return delegate.toArray();
  }

  @Override
  public <T> T[] toArray(final T[] a) {
    return delegate.toArray(a);
  }

  private <T> T checkValid(final T t) {
    // a ClassCastException is what's supposed to happen!
    @SuppressWarnings("unchecked")
    E e = (E) t;
    comparator().compare(e, e);
    return t;
  }

  @Override
  public boolean equals(final Object obj) {
    return delegate.equals(obj);
  }

  @Override
  public int hashCode() {
    return delegate.hashCode();
  }

  @Override
  public String toString() {
    return delegate.toString();
  }

  private static final long serialVersionUID = 0L;
}
