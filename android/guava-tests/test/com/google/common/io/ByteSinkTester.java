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

package com.google.common.io;

import static com.google.common.io.SourceSinkFactory.ByteSinkFactory;
import static com.google.common.io.SourceSinkFactory.CharSinkFactory;
import static org.junit.Assert.assertArrayEquals;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Map;
import junit.framework.TestSuite;

/**
 * A generator of {@code TestSuite} instances for testing {@code ByteSink} implementations.
 * Generates tests of a all methods on a {@code ByteSink} given various inputs written to it as well
 * as sub-suites for testing the {@code CharSink} view in the same way.
 *
 * @author Colin Decker
 */
@AndroidIncompatible // Android doesn't understand tests that lack default constructors.
public class ByteSinkTester extends SourceSinkTester<ByteSink, byte[], ByteSinkFactory> {

  private static final ImmutableList<Method> testMethods
      = getTestMethods(ByteSinkTester.class);

  static TestSuite tests(final String name, final ByteSinkFactory factory) {
    TestSuite suite = new TestSuite(name);
    for (Map.Entry<String, String> entry : TEST_STRINGS.entrySet()) {
      String desc = entry.getKey();
      TestSuite stringSuite = suiteForString(name, factory, entry.getValue(), desc);
      suite.addTest(stringSuite);
    }
    return suite;
  }

  private static TestSuite suiteForString(final String name, final ByteSinkFactory factory,
      final String string, final String desc) {
    byte[] bytes = string.getBytes(Charsets.UTF_8);
    TestSuite suite = suiteForBytes(name, factory, desc, bytes);
    CharSinkFactory charSinkFactory = SourceSinkFactories.asCharSinkFactory(factory);
    suite.addTest(CharSinkTester.suiteForString(name + ".asCharSink[Charset]", charSinkFactory,
        string, desc));
    return suite;
  }

  private static TestSuite suiteForBytes(final String name, final ByteSinkFactory factory,
      final String desc, final byte[] bytes) {
    TestSuite suite = new TestSuite(name + " [" + desc + "]");
    for (final Method method : testMethods) {
      suite.addTest(new ByteSinkTester(factory, bytes, name, desc, method));
    }
    return suite;
  }

  private ByteSink sink;

  ByteSinkTester(final ByteSinkFactory factory, final byte[] data, final String suiteName,
      final String caseDesc, final Method method) {
    super(factory, data, suiteName, caseDesc, method);
  }

  @Override
  protected void setUp() throws Exception {
    sink = factory.createSink();
  }

  public void testOpenStream() throws IOException {
    OutputStream out = sink.openStream();
    try {
      ByteStreams.copy(new ByteArrayInputStream(data), out);
    } finally {
      out.close();
    }

    assertContainsExpectedBytes();
  }

  public void testOpenBufferedStream() throws IOException {
    OutputStream out = sink.openBufferedStream();
    try {
      ByteStreams.copy(new ByteArrayInputStream(data), out);
    } finally {
      out.close();
    }

    assertContainsExpectedBytes();
  }

  public void testWrite() throws IOException {
    sink.write(data);

    assertContainsExpectedBytes();
  }

  public void testWriteFrom_inputStream() throws IOException {
    sink.writeFrom(new ByteArrayInputStream(data));

    assertContainsExpectedBytes();
  }

  private void assertContainsExpectedBytes() throws IOException {
    assertArrayEquals(expected, factory.getSinkContents());
  }
}
