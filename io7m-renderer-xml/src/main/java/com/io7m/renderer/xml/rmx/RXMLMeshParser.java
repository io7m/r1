/*
 * Copyright © 2013 <code@io7m.com> http://io7m.com
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

package com.io7m.renderer.xml.rmx;

import javax.annotation.Nonnull;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ValidityException;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.renderer.RSpaceObject;
import com.io7m.renderer.RSpaceTangent;
import com.io7m.renderer.RSpaceTexture;
import com.io7m.renderer.RVectorI2F;
import com.io7m.renderer.RVectorI3F;
import com.io7m.renderer.xml.RXMLException;
import com.io7m.renderer.xml.RXMLUtilities;

public final class RXMLMeshParser<E extends Throwable>
{
  private static void checkVersion(
    final @Nonnull Element e)
    throws RXMLException,
      ConstraintError
  {
    final Attribute a =
      RXMLUtilities.getAttribute(e, "version", RXMLConstants.MESHES_URI);
    final int version = RXMLUtilities.getAttributeInteger(a);
    if (version != RXMLConstants.MESHES_VERSION) {
      final StringBuilder message = new StringBuilder();
      message.append("Unexpected version ");
      message.append(version);
      message.append(", supported version is ");
      message.append(RXMLConstants.MESHES_VERSION);
      throw new RXMLException.RXMLExceptionValidityError(
        new ValidityException(message.toString()));
    }
  }

  public static <E extends Throwable> RXMLMeshParser<E> parseFromDocument(
    final @Nonnull Document d,
    final @Nonnull RXMLMeshParserEvents<E> events)
    throws ConstraintError,
      E,
      RXMLException
  {
    Constraints.constrainNotNull(d, "Document");
    return new RXMLMeshParser<E>(d.getRootElement(), events);
  }

  public static <E extends Throwable> RXMLMeshParser<E> parseFromElement(
    final @Nonnull Element e,
    final @Nonnull RXMLMeshParserEvents<E> events)
    throws ConstraintError,
      E,
      RXMLException
  {
    Constraints.constrainNotNull(e, "Element");
    return new RXMLMeshParser<E>(e, events);
  }

  private static <E extends Throwable> void parseTriangles(
    final @Nonnull Element e,
    final @Nonnull RXMLMeshParserEvents<E> events)
    throws E,
      ConstraintError,
      RXMLException
  {
    assert e.getLocalName().equals("triangles");

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
      throw new RXMLException.RXMLExceptionValidityError(
        new ValidityException(message.toString()));
    }

    events.eventMeshTrianglesStarted(count);

    for (int index = 0; index < size; ++index) {
      final Element t = ets.get(index);
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

  private static @Nonnull RXMLMeshType parseType(
    final @Nonnull Element e)
  {
    assert e.getLocalName().equals("type");

    final Element en =
      RXMLUtilities.getOptionalChild(
        e,
        "attribute-normal-3f",
        RXMLConstants.MESHES_URI);

    final Element et =
      RXMLUtilities.getOptionalChild(
        e,
        "attribute-tangent-3f",
        RXMLConstants.MESHES_URI);

    final Element eu =
      RXMLUtilities.getOptionalChild(
        e,
        "attribute-uv-2f",
        RXMLConstants.MESHES_URI);

    return new RXMLMeshType(en != null, et != null, eu != null);
  }

  private static RVectorI3F<RSpaceObject> parseVertexPosition(
    final @Nonnull Element v)
    throws RXMLException,
      ConstraintError
  {
    assert v.getLocalName().equals("v");

    final Element p =
      RXMLUtilities.getChild(v, "p", RXMLConstants.MESHES_URI);
    return RXMLUtilities.getElementAttributesVector3f(
      p,
      RXMLConstants.MESHES_URI);
  }

  private static <E extends Throwable> void parseVertices(
    final @Nonnull Element ev,
    final @Nonnull RXMLMeshType type,
    final @Nonnull RXMLMeshParserEvents<E> events)
    throws E,
      ConstraintError,
      RXMLException
  {
    assert ev.getLocalName().equals("vertices");

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
      throw new RXMLException.RXMLExceptionValidityError(
        new ValidityException(message.toString()));
    }

    events.eventMeshVerticesStarted(count);

    for (int index = 0; index < size; ++index) {
      final Element v = evs.get(index);

      events.eventMeshVertexStarted(index);
      events.eventMeshVertexPosition(
        index,
        RXMLMeshParser.parseVertexPosition(v));

      if (type.hasNormal()) {
        final Element n =
          RXMLUtilities.getChild(v, "n", RXMLConstants.MESHES_URI);
        final RVectorI3F<RSpaceObject> vn =
          RXMLUtilities.getElementAttributesVector3f(
            n,
            RXMLConstants.MESHES_URI);
        events.eventMeshVertexNormal(index, vn);
      }

      if (type.hasTangent()) {
        final Element t =
          RXMLUtilities.getChild(v, "t", RXMLConstants.MESHES_URI);
        final RVectorI3F<RSpaceTangent> vt =
          RXMLUtilities.getElementAttributesVector3f(
            t,
            RXMLConstants.MESHES_URI);
        events.eventMeshVertexTangent(index, vt);
      }

      if (type.hasUV()) {
        final Element u =
          RXMLUtilities.getChild(v, "u", RXMLConstants.MESHES_URI);
        final RVectorI2F<RSpaceTexture> vu =
          RXMLUtilities.getElementAttributesVector2f(
            u,
            RXMLConstants.MESHES_URI);
        events.eventMeshVertexUV(index, vu);
      }

      events.eventMeshVertexEnded(index);
    }

    events.eventMeshVerticesEnded();
  }

  private final @Nonnull RXMLMeshParserEvents<E> events;

  private RXMLMeshParser(
    final @Nonnull Element e,
    final @Nonnull RXMLMeshParserEvents<E> events)
    throws ConstraintError,
      E,
      RXMLException
  {
    this.events = Constraints.constrainNotNull(events, "Parser events");

    try {
      this.events.eventMeshStarted();

      RXMLUtilities.checkIsElement(e, "mesh", RXMLConstants.MESHES_URI);
      RXMLMeshParser.checkVersion(e);

      final Attribute na =
        RXMLUtilities.getAttribute(e, "name", RXMLConstants.MESHES_URI);
      events.eventMeshName(na.getValue());

      final Element et =
        RXMLUtilities.getChild(e, "type", RXMLConstants.MESHES_URI);
      final RXMLMeshType mt = RXMLMeshParser.parseType(et);
      events.eventMeshType(mt);

      final Element ev =
        RXMLUtilities.getChild(e, "vertices", RXMLConstants.MESHES_URI);
      RXMLMeshParser.parseVertices(ev, mt, events);

      final Element etr =
        RXMLUtilities.getChild(e, "triangles", RXMLConstants.MESHES_URI);
      RXMLMeshParser.parseTriangles(etr, events);
    } catch (final RXMLException x) {
      events.eventError(x);
      throw x;
    } finally {
      events.eventMeshEnded();
    }
  }
}
