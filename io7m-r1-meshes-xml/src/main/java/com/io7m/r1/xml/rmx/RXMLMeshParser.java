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

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ValidityException;

import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.VectorM3F;
import com.io7m.r1.meshes.RMeshParserEventsType;
import com.io7m.r1.types.RSpaceObjectType;
import com.io7m.r1.types.RSpaceTextureType;
import com.io7m.r1.types.RVectorI2F;
import com.io7m.r1.types.RVectorI3F;
import com.io7m.r1.types.RVectorI4F;
import com.io7m.r1.types.RXMLException;
import com.io7m.r1.xml.RXMLUtilities;

/**
 * A mesh parser implementation that parses a document and delivers events to
 * a given {@link RMeshParserEventsType} interface.
 * 
 * @param <E>
 *          The type of exceptions raised by the event interface.
 */

@EqualityReference public final class RXMLMeshParser<E extends Throwable>
{
  /**
   * Construct a new parser from the given document, which is expected to be
   * schema-valid.
   * 
   * @param <E>
   *          The type of exceptions raised by the parser.
   * @param d
   *          The document.
   * @param events
   *          The parser events interface.
   * @return A parser.
   * 
   * 
   * @throws E
   *           If required.
   * @throws RXMLException
   *           If required.
   */

  public static <E extends Throwable> RXMLMeshParser<E> parseFromDocument(
    final Document d,
    final RMeshParserEventsType<E> events)
    throws E,
      RXMLException
  {
    NullCheck.notNull(d, "Document");
    final Element r = d.getRootElement();
    assert r != null;
    return new RXMLMeshParser<E>(r, events);
  }

  /**
   * Construct a new parser from the given element, which is expected to be
   * schema-valid.
   * 
   * @param <E>
   *          The type of exceptions raised by the parser.
   * @param e
   *          The element.
   * @param events
   *          The parser events interface.
   * @return A parser.
   * 
   * 
   * @throws E
   *           If required.
   * @throws RXMLException
   *           If required.
   */

  public static <E extends Throwable> RXMLMeshParser<E> parseFromElement(
    final Element e,
    final RMeshParserEventsType<E> events)
    throws E,
      RXMLException
  {
    NullCheck.notNull(e, "Element");
    return new RXMLMeshParser<E>(e, events);
  }

  private static <E extends Throwable> void parseTriangles(
    final Element e,
    final RMeshParserEventsType<E> events)
    throws E,

      RXMLException
  {
    assert "triangles".equals(e.getLocalName());

    final int count =
      RXMLUtilities.getAttributeInteger(RXMLUtilities.getAttribute(
        e,
        "count",
        RXMLConstants.MESHES_URI));

    final Elements ets =
      RXMLUtilities.getChildren(e, "tri", RXMLConstants.MESHES_URI);

    final int size = ets.size();
    if (size != count) {
      final StringBuilder message = new StringBuilder();
      message.append("Expected ");
      message.append(count);
      message.append(" triangles, but ");
      message.append(size);
      message.append(" were provided");
      throw RXMLException.validityException(new ValidityException(message
        .toString()));
    }

    events.eventMeshTrianglesStarted(count);

    for (int index = 0; index < size; ++index) {
      final Element t = ets.get(index);
      assert t != null;

      final int v0 =
        RXMLUtilities.getAttributeInteger(RXMLUtilities.getAttribute(
          t,
          "v0",
          RXMLConstants.MESHES_URI));
      final int v1 =
        RXMLUtilities.getAttributeInteger(RXMLUtilities.getAttribute(
          t,
          "v1",
          RXMLConstants.MESHES_URI));
      final int v2 =
        RXMLUtilities.getAttributeInteger(RXMLUtilities.getAttribute(
          t,
          "v2",
          RXMLConstants.MESHES_URI));

      events.eventMeshTriangle(index, v0, v1, v2);
    }

    events.eventMeshTrianglesEnded();
  }

  private static RVectorI3F<RSpaceObjectType> parseVertexPosition(
    final Element v)
    throws RXMLException
  {
    assert "v".equals(v.getLocalName());

    final Element p =
      RXMLUtilities.getChild(v, "p", RXMLConstants.MESHES_URI);
    return RXMLUtilities.getElementAttributesVector3f(
      p,
      RXMLConstants.MESHES_URI);
  }

  private static <E extends Throwable> void parseVertices(
    final Element ev,
    final RMeshParserEventsType<E> events)
    throws E,

      RXMLException
  {
    assert "vertices".equals(ev.getLocalName());

    final VectorM3F bounds_lower =
      new VectorM3F(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
    final VectorM3F bounds_upper =
      new VectorM3F(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);

    final int count =
      RXMLUtilities.getAttributeInteger(RXMLUtilities.getAttribute(
        ev,
        "count",
        RXMLConstants.MESHES_URI));

    final Elements evs =
      RXMLUtilities.getChildren(ev, "v", RXMLConstants.MESHES_URI);

    final int size = evs.size();
    if (size != count) {
      final StringBuilder message = new StringBuilder();
      message.append("Expected ");
      message.append(count);
      message.append(" vertices, but ");
      message.append(size);
      message.append(" were provided");
      throw RXMLException.validityException(new ValidityException(message
        .toString()));
    }

    events.eventMeshVerticesStarted(count);

    for (int index = 0; index < size; ++index) {
      final Element v = evs.get(index);
      assert v != null;

      events.eventMeshVertexStarted(index);

      final RVectorI3F<RSpaceObjectType> position =
        RXMLMeshParser.parseVertexPosition(v);

      bounds_lower.set3F(
        Math.min(position.getXF(), bounds_lower.getXF()),
        Math.min(position.getYF(), bounds_lower.getYF()),
        Math.min(position.getZF(), bounds_lower.getZF()));

      bounds_upper.set3F(
        Math.max(position.getXF(), bounds_upper.getXF()),
        Math.max(position.getYF(), bounds_upper.getYF()),
        Math.max(position.getZF(), bounds_upper.getZF()));

      events.eventMeshVertexPosition(index, position);

      {
        final Element n =
          RXMLUtilities.getChild(v, "n", RXMLConstants.MESHES_URI);
        final RVectorI3F<RSpaceObjectType> vn =
          RXMLUtilities.getElementAttributesVector3f(
            n,
            RXMLConstants.MESHES_URI);
        events.eventMeshVertexNormal(index, vn);
      }

      {
        final Element t =
          RXMLUtilities.getChild(v, "t4", RXMLConstants.MESHES_URI);
        final RVectorI4F<RSpaceObjectType> vt =
          RXMLUtilities.getElementAttributesVector4f(
            t,
            RXMLConstants.MESHES_URI);
        events.eventMeshVertexTangent4f(index, vt);
      }

      {
        final Element u =
          RXMLUtilities.getChild(v, "u", RXMLConstants.MESHES_URI);
        final RVectorI2F<RSpaceTextureType> vu =
          RXMLUtilities.getElementAttributesVector2f(
            u,
            RXMLConstants.MESHES_URI);
        events.eventMeshVertexUV(index, vu);
      }

      events.eventMeshVertexEnded(index);
    }

    events.eventMeshVerticesEnded(
      new RVectorI3F<RSpaceObjectType>(bounds_lower.getXF(), bounds_lower
        .getYF(), bounds_lower.getZF()),
      new RVectorI3F<RSpaceObjectType>(bounds_upper.getXF(), bounds_upper
        .getYF(), bounds_upper.getZF()));
  }

  private final RMeshParserEventsType<E> events;

  private RXMLMeshParser(
    final Element e,
    final RMeshParserEventsType<E> in_events)
    throws E,
      RXMLException
  {
    this.events = NullCheck.notNull(in_events, "Parser events");

    try {
      this.events.eventMeshStarted();

      RXMLUtilities.checkIsElement(e, "mesh", RXMLConstants.MESHES_URI);

      final Attribute na =
        RXMLUtilities.getAttribute(e, "name", RXMLConstants.MESHES_URI);
      final String nav = na.getValue();
      assert nav != null;
      in_events.eventMeshName(nav);

      final Element ev =
        RXMLUtilities.getChild(e, "vertices", RXMLConstants.MESHES_URI);
      RXMLMeshParser.parseVertices(ev, in_events);

      final Element etr =
        RXMLUtilities.getChild(e, "triangles", RXMLConstants.MESHES_URI);
      RXMLMeshParser.parseTriangles(etr, in_events);
    } catch (final RXMLException x) {
      in_events.eventError(x);
      throw x;
    } finally {
      in_events.eventMeshEnded();
    }
  }
}
