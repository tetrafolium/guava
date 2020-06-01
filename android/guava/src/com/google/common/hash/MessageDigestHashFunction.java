/*
 * Copyright (C) 2011 The Guava Authors
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
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * {@link HashFunction} adapter for {@link MessageDigest} instances.
 *
 * @author Kevin Bourrillion
 * @author Dimitris Andreou
 */
final class MessageDigestHashFunction extends AbstractHashFunction implements Serializable {
  private final MessageDigest prototype;
  private final int bytes;
  private final boolean supportsClone;
  private final String toString;

  MessageDigestHashFunction(final String algorithmName, final String toString) {
    this.prototype = getMessageDigest(algorithmName);
    this.bytes = prototype.getDigestLength();
    this.toString = checkNotNull(toString);
    this.supportsClone = supportsClone(prototype);
  }

  MessageDigestHashFunction(final String algorithmName, final int bytes, final String toString) {
    this.toString = checkNotNull(toString);
    this.prototype = getMessageDigest(algorithmName);
    int maxLength = prototype.getDigestLength();
    checkArgument(
        bytes >= 4 && bytes <= maxLength, "bytes (%s) must be >= 4 and < %s", bytes, maxLength);
    this.bytes = bytes;
    this.supportsClone = supportsClone(prototype);
  }

  private static boolean supportsClone(final MessageDigest digest) {
    try {
      digest.clone();
      return true;
    } catch (CloneNotSupportedException e) {
      return false;
    }
  }

  @Override
  public int bits() {
    return bytes * Byte.SIZE;
  }

  @Override
  public String toString() {
    return toString;
  }

  private static MessageDigest getMessageDigest(final String algorithmName) {
    try {
      return MessageDigest.getInstance(algorithmName);
    } catch (NoSuchAlgorithmException e) {
      throw new AssertionError(e);
    }
  }

  @Override
  public Hasher newHasher() {
    if (supportsClone) {
      try {
        return new MessageDigestHasher((MessageDigest) prototype.clone(), bytes);
      } catch (CloneNotSupportedException e) {
        // falls through
      }
    }
    return new MessageDigestHasher(getMessageDigest(prototype.getAlgorithm()), bytes);
  }

  private static final class SerializedForm implements Serializable {
    private final String algorithmName;
    private final int bytes;
    private final String toString;

    private SerializedForm(final String algorithmName, final int bytes, final String toString) {
      this.algorithmName = algorithmName;
      this.bytes = bytes;
      this.toString = toString;
    }

    private Object readResolve() {
      return new MessageDigestHashFunction(algorithmName, bytes, toString);
    }

    private static final long serialVersionUID = 0;
  }

  Object writeReplace() {
    return new SerializedForm(prototype.getAlgorithm(), bytes, toString);
  }

  /**
   * Hasher that updates a message digest.
   */
  private static final class MessageDigestHasher extends AbstractByteHasher {
    private final MessageDigest digest;
    private final int bytes;
    private boolean done;

    private MessageDigestHasher(final MessageDigest digest, final int bytes) {
      this.digest = digest;
      this.bytes = bytes;
    }

    @Override
    protected void update(final byte b) {
      checkNotDone();
      digest.update(b);
    }

    @Override
    protected void update(final byte[] b, final int off, final int len) {
      checkNotDone();
      digest.update(b, off, len);
    }

    @Override
    protected void update(final ByteBuffer bytes) {
      checkNotDone();
      digest.update(bytes);
    }

    private void checkNotDone() {
      checkState(!done, "Cannot re-use a Hasher after calling hash() on it");
    }

    @Override
    public HashCode hash() {
      checkNotDone();
      done = true;
      return (bytes == digest.getDigestLength())
          ? HashCode.fromBytesNoCopy(digest.digest())
          : HashCode.fromBytesNoCopy(Arrays.copyOf(digest.digest(), bytes));
    }
  }
}
