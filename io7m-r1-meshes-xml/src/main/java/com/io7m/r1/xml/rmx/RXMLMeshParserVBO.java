/*
 * Copyright © 2014 <code@io7m.com> http://io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.r1.xml.rmx;

import nu.xom.Document;
import nu.xom.Element;

import com.io7m.jcanephora.ArrayBufferType;
import com.io7m.jcanephora.IndexBufferType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.UsageHint;
import com.io7m.jcanephora.api.JCGLArrayBuffersType;
import com.io7m.jcanephora.api.JCGLIndexBuffersType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.parameterized.PVectorI3F;
import com.io7m.r1.meshes.RMeshParserEventsVBO;
import com.io7m.r1.types.RSpaceObjectType;
import com.io7m.r1.xml.RXMLException;

/**
 * The glue between a {@link RXMLMeshParser} and a
 * {@link RMeshParserEventsVBO} for producing vertex buffer objects directly
 * from XML meshes.
 *
 * @param <G>
 *          The precise type of required OpenGL interfaces
 */

@EqualityReference public final class RXMLMeshParserVBO<G extends JCGLArrayBuffersType & JCGLIndexBuffersType>
{
  /**
   * Construct a parser for the given document, which is expected to be
   * schema-valid.
   *
   * @param <G>
   *          The precise type of OpenGL interfaces.
   * @param d
   *          The document.
   * @param g
   *          The OpenGL interface.
   * @param hint
   *          The usage hit for the loaded meshes.
   * @return A parser.
   * @throws RXMLException
   *           On XML errors.
   * @throws JCGLException
   *           On OpenGL errors.
   */

  public static
    <G extends JCGLArrayBuffersType & JCGLIndexBuffersType>
    RXMLMeshParserVBO<G>
    parseFromDocument(
      final Document d,
      final G g,
      final UsageHint hint)
      throws RXMLException,
        JCGLException
  {
    NullCheck.notNull(d, "Document");
    NullCheck.notNull(g, "OpenGL interface");
    NullCheck.notNull(hint, "Usage hint");
    final Element root = d.getRootElement();
    assert root != null;
    return new RXMLMeshParserVBO<G>(g, hint, root);
  }

  /**
   * Construct a parser for the given element, which is expected to be
   * schema-valid.
   *
   * @param <G>
   *          The precise type of OpenGL interfaces.
   * @param e
   *          The element.
   * @param g
   *          The OpenGL interface.
   * @param hint
   *          The usage hit for the loaded meshes.
   * @return A parser.
   * @throws RXMLException
   *           On XML errors.
   * @throws JCGLException
   *           On OpenGL errors.
   */

  public static
    <G extends JCGLArrayBuffersType & JCGLIndexBuffersType>
    RXMLMeshParserVBO<G>
    parseFromElement(
      final Element e,
      final G g,
      final UsageHint hint)
      throws RXMLException,
        JCGLException
  {
    NullCheck.notNull(e, "Element");
    NullCheck.notNull(g, "OpenGL interface");
    NullCheck.notNull(hint, "Usage hint");
    return new RXMLMeshParserVBO<G>(g, hint, e);
  }

  private final RMeshParserEventsVBO<G>                                   events;
  @SuppressWarnings("unused") private final RXMLMeshParser<JCGLException> parser;

  private RXMLMeshParserVBO(
    final G g,
    final UsageHint hint,
    final Element e)
    throws JCGLException,
      RXMLException
  {
    this.events = RMeshParserEventsVBO.newEvents(g, hint);
    this.parser = RXMLMeshParser.parseFromElement(e, this.events);
  }

  /**
   * @return The array buffer produced from a successful parse.
   */

  public ArrayBufferType getArrayBuffer()
  {
    return this.events.getArray();
  }

  /**
   * @return The lower bounds of all the vertices in the parsed mesh.
   */

  public PVectorI3F<RSpaceObjectType> getBoundsLower()
  {
    return this.events.getBoundsLower();
  }

  /**
   * @return The upper bounds of all the vertices in the parsed mesh.
   */

  public PVectorI3F<RSpaceObjectType> getBoundsUpper()
  {
    return this.events.getBoundsUpper();
  }

  /**
   * @return The index buffer produced from a successful parse.
   */

  public IndexBufferType getIndexBuffer()
  {
    return this.events.getIndices();
  }

  /**
   * @return The name of the parsed mesh.
   */

  public String getName()
  {
    return this.events.getName();
  }
}
