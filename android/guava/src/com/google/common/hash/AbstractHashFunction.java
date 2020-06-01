/*
 * Copyright (C) 2017 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.common.hash;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkPositionIndexes;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/** 
 * Skeleton implementation of {@link HashFunction} in terms of {@link #newHasher()}.
 * 
 * TODO(lowasser): make public 
 */
abstract class AbstractHashFunction implements HashFunction {
  @Override
  public <T> HashCode hashObject(final T instance, final Funnel<? super T> funnel) {
    return newHasher().putObject(instance, funnel).hash();
  }

  @Override
  public HashCode hashUnencodedChars(final CharSequence input) {
    int len = input.length();
    return newHasher(len * 2).putUnencodedChars(input).hash();
  }

  @Override
  public HashCode hashString(final CharSequence input, final Charset charset) {
    return newHasher().putString(input, charset).hash();
  }

  @Override
  public HashCode hashInt(final int input) {
    return newHasher(4).putInt(input).hash();
  }

  @Override
  public HashCode hashLong(final long input) {
    return newHasher(8).putLong(input).hash();
  }

  @Override
  public HashCode hashBytes(final byte[] input) {
    return hashBytes(input, 0, input.length);
  }

  @Override
  public HashCode hashBytes(final byte[] input, final int off, final int len) {
    checkPositionIndexes(off, off + len, input.length);
    return newHasher(len).putBytes(input, off, len).hash();
  }

  @Override
  public HashCode hashBytes(final ByteBuffer input) {
    return newHasher(input.remaining()).putBytes(input).hash();
  }

  @Override
  public Hasher newHasher(final int expectedInputSize) {
    checkArgument(
        expectedInputSize >= 0, "expectedInputSize must be >= 0 but was %s", expectedInputSize);
    return newHasher();
  }
}
