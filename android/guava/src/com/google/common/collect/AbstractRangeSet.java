/*
 * Copyright (C) 2011 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.common.collect;

import com.google.common.annotations.GwtIncompatible;
import javax.annotation.Nullable;

/**
 * A skeletal implementation of {@code RangeSet}.
 *
 * @author Louis Wasserman
 */
@GwtIncompatible
abstract class AbstractRangeSet<C extends Comparable> implements RangeSet<C> {
  AbstractRangeSet() { }

  @Override
  public boolean contains(final C value) {
    return rangeContaining(value) != null;
  }

  @Override
  public abstract Range<C> rangeContaining(C value);

  @Override
  public boolean isEmpty() {
    return asRanges().isEmpty();
  }

  @Override
  public void add(final Range<C> range) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void remove(final Range<C> range) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear() {
    remove(Range.<C>all());
  }

  @Override
  public boolean enclosesAll(final RangeSet<C> other) {
    return enclosesAll(other.asRanges());
  }

  @Override
  public boolean enclosesAll(final Iterable<Range<C>> ranges) {
    for (Range<C> range : ranges) {
      if (!encloses(range)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public void addAll(final RangeSet<C> other) {
    addAll(other.asRanges());
  }

  @Override
  public void addAll(final Iterable<Range<C>> ranges) {
    for (Range<C> range : ranges) {
      add(range);
    }
  }

  @Override
  public void removeAll(final RangeSet<C> other) {
    removeAll(other.asRanges());
  }

  @Override
  public void removeAll(final Iterable<Range<C>> ranges) {
    for (Range<C> range : ranges) {
      remove(range);
    }
  }

  @Override
  public boolean intersects(final Range<C> otherRange) {
    return !subRangeSet(otherRange).isEmpty();
  }

  @Override
  public abstract boolean encloses(Range<C> otherRange);

  @Override
  public boolean equals(final @Nullable Object obj) {
    if (obj == this) {
      return true;
    } else if (obj instanceof RangeSet) {
      RangeSet<?> other = (RangeSet<?>) obj;
      return this.asRanges().equals(other.asRanges());
    }
    return false;
  }

  @Override
  public final int hashCode() {
    return asRanges().hashCode();
  }

  @Override
  public final String toString() {
    return asRanges().toString();
  }
}
