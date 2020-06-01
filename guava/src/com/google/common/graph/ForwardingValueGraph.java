/*
 * Copyright (C) 2016 The Guava Authors
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

package com.google.common.graph;

import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * A class to allow {@link ValueGraph} implementations to be backed by a provided delegate. This is
 * not currently planned to be released as a general-purpose forwarding class.
 *
 * @author James Sexton
 * @author Joshua O'Madadhain
 */
abstract class ForwardingValueGraph<N, V> extends AbstractValueGraph<N, V> {

  protected abstract ValueGraph<N, V> delegate();

  @Override
  public Set<N> nodes() {
    return delegate().nodes();
  }

  /**
   * Defer to {@link AbstractValueGraph#edges()} (based on {@link #successors(Object)}) for full
   * edges() implementation.
   */
  @Override
  protected long edgeCount() {
    return delegate().edges().size();
  }

  @Override
  public boolean isDirected() {
    return delegate().isDirected();
  }

  @Override
  public boolean allowsSelfLoops() {
    return delegate().allowsSelfLoops();
  }

  @Override
  public ElementOrder<N> nodeOrder() {
    return delegate().nodeOrder();
  }

  @Override
  public Set<N> adjacentNodes(final N node) {
    return delegate().adjacentNodes(node);
  }

  @Override
  public Set<N> predecessors(final N node) {
    return delegate().predecessors(node);
  }

  @Override
  public Set<N> successors(final N node) {
    return delegate().successors(node);
  }

  @Override
  public int degree(final N node) {
    return delegate().degree(node);
  }

  @Override
  public int inDegree(final N node) {
    return delegate().inDegree(node);
  }

  @Override
  public int outDegree(final N node) {
    return delegate().outDegree(node);
  }

  @Override
  public boolean hasEdgeConnecting(final N nodeU, final N nodeV) {
    return delegate().hasEdgeConnecting(nodeU, nodeV);
  }

  @Override
  public Optional<V> edgeValue(final N nodeU, final N nodeV) {
    return delegate().edgeValue(nodeU, nodeV);
  }

  @Override
  @Nullable
  public V edgeValueOrDefault(final N nodeU, final N nodeV, final @Nullable V defaultValue) {
    return delegate().edgeValueOrDefault(nodeU, nodeV, defaultValue);
  }
}
