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

/**
 * A class to allow {@link Network} implementations to be backed by a provided delegate. This is not
 * currently planned to be released as a general-purpose forwarding class.
 *
 * @author James Sexton
 * @author Joshua O'Madadhain
 */
abstract class ForwardingNetwork<N, E> extends AbstractNetwork<N, E> {

  protected abstract Network<N, E> delegate();

  @Override
  public Set<N> nodes() {
    return delegate().nodes();
  }

  @Override
  public Set<E> edges() {
    return delegate().edges();
  }

  @Override
  public boolean isDirected() {
    return delegate().isDirected();
  }

  @Override
  public boolean allowsParallelEdges() {
    return delegate().allowsParallelEdges();
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
  public ElementOrder<E> edgeOrder() {
    return delegate().edgeOrder();
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
  public Set<E> incidentEdges(final N node) {
    return delegate().incidentEdges(node);
  }

  @Override
  public Set<E> inEdges(final N node) {
    return delegate().inEdges(node);
  }

  @Override
  public Set<E> outEdges(final N node) {
    return delegate().outEdges(node);
  }

  @Override
  public EndpointPair<N> incidentNodes(final E edge) {
    return delegate().incidentNodes(edge);
  }

  @Override
  public Set<E> adjacentEdges(final E edge) {
    return delegate().adjacentEdges(edge);
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
  public Set<E> edgesConnecting(final N nodeU, final N nodeV) {
    return delegate().edgesConnecting(nodeU, nodeV);
  }

  @Override
  public Optional<E> edgeConnecting(final N nodeU, final N nodeV) {
    return delegate().edgeConnecting(nodeU, nodeV);
  }

  @Override
  public E edgeConnectingOrNull(final N nodeU, final N nodeV) {
    return delegate().edgeConnectingOrNull(nodeU, nodeV);
  }

  @Override
  public boolean hasEdgeConnecting(final N nodeU, final N nodeV) {
    return delegate().hasEdgeConnecting(nodeU, nodeV);
  }
}
