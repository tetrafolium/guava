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

package com.google.common.collect;

import com.google.common.annotations.GwtIncompatible;
import javax.annotation.Nullable;

/**
 * Skeletal implementation of {@link ImmutableSortedSet#descendingSet()}.
 *
 * @author Louis Wasserman
 */
@GwtIncompatible
final class DescendingImmutableSortedSet<E> extends ImmutableSortedSet<E> {
  private final ImmutableSortedSet<E> forward;

  DescendingImmutableSortedSet(final ImmutableSortedSet<E> forward) {
    super(Ordering.from(forward.comparator()).reverse());
    this.forward = forward;
  }

  @Override
  public boolean contains(final @Nullable Object object) {
    return forward.contains(object);
  }

  @Override
  public int size() {
    return forward.size();
  }

  @Override
  public UnmodifiableIterator<E> iterator() {
    return forward.descendingIterator();
  }

  @Override
  ImmutableSortedSet<E> headSetImpl(final E toElement, final boolean inclusive) {
    return forward.tailSet(toElement, inclusive).descendingSet();
  }

  @Override
  ImmutableSortedSet<E> subSetImpl(
      final E fromElement, final boolean fromInclusive, final E toElement, final boolean toInclusive) {
    return forward.subSet(toElement, toInclusive, fromElement, fromInclusive).descendingSet();
  }

  @Override
  ImmutableSortedSet<E> tailSetImpl(final E fromElement, final boolean inclusive) {
    return forward.headSet(fromElement, inclusive).descendingSet();
  }

  @Override
  @GwtIncompatible("NavigableSet")
  public ImmutableSortedSet<E> descendingSet() {
    return forward;
  }

  @Override
  @GwtIncompatible("NavigableSet")
  public UnmodifiableIterator<E> descendingIterator() {
    return forward.iterator();
  }

  @Override
  @GwtIncompatible("NavigableSet")
  ImmutableSortedSet<E> createDescendingSet() {
    throw new AssertionError("should never be called");
  }

  @Override
  public E lower(final E element) {
    return forward.higher(element);
  }

  @Override
  public E floor(final E element) {
    return forward.ceiling(element);
  }

  @Override
  public E ceiling(final E element) {
    return forward.floor(element);
  }

  @Override
  public E higher(final E element) {
    return forward.lower(element);
  }

  @Override
  int indexOf(final @Nullable Object target) {
    int index = forward.indexOf(target);
    if (index == -1) {
      return index;
    } else {
      return size() - 1 - index;
    }
  }

  @Override
  boolean isPartialView() {
    return forward.isPartialView();
  }
}
