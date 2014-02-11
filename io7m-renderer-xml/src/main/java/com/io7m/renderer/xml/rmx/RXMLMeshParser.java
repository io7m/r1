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

import java.util.EnumSet;

import javax.annotation.Nonnull;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ValidityException;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jtensors.VectorM3F;
import com.io7m.renderer.types.RSpaceObject;
import com.io7m.renderer.types.RSpaceTexture;
import com.io7m.renderer.types.RVectorI2F;
import com.io7m.renderer.types.RVectorI3F;
import com.io7m.renderer.types.RVectorI4F;
import com.io7m.renderer.types.RXMLException;
import com.io7m.renderer.xml.RXMLUtilities;

public final class RXMLMeshParser<E extends Throwable>
{
  private static void checkVersion(
    final @Nonnull Element e)
    throws RXMLException
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
      throw RXMLException.validityException(new ValidityException(message
        .toString()));
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
      throw RXMLException.validityException(new ValidityException(message
        .toString()));
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
    throws ConstraintError
  {
    assert e.getLocalName().equals("type");

    final EnumSet<RXMLMeshAttribute> attributes =
      EnumSet.noneOf(RXMLMeshAttribute.class);

    final Element en =
      RXMLUtilities.getOptionalChild(
        e,
        "attribute-normal-3f",
        RXMLConstants.MESHES_URI);

    if (en != null) {
      attributes.add(RXMLMeshAttribute.NORMAL_3F);
    }

    final Element et =
      RXMLUtilities.getOptionalChild(
        e,
        "attribute-tangent-4f",
        RXMLConstants.MESHES_URI);

    if (et != null) {
      attributes.add(RXMLMeshAttribute.TANGENT_4F);
    }

    /**
     * If bitangent-3f is present, then tangent-3f must be too, if the
     * document is schema-valid.
     */

    final Element eb =
      RXMLUtilities.getOptionalChild(
        e,
        "attribute-bitangent-3f",
        RXMLConstants.MESHES_URI);

    if (eb != null) {
      attributes.add(RXMLMeshAttribute.TANGENT_3F_BITANGENT_3F);
    }

    final Element eu =
      RXMLUtilities.getOptionalChild(
        e,
        "attribute-uv-2f",
        RXMLConstants.MESHES_URI);

    if (eu != null) {
      attributes.add(RXMLMeshAttribute.UV_2F);
    }

    return new RXMLMeshType(attributes);
  }

  private static RVectorI3F<RSpaceObject> parseVertexPosition(
    final @Nonnull Element v)
    throws RXMLException
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

      events.eventMeshVertexStarted(index);

      final RVectorI3F<RSpaceObject> position =
        RXMLMeshParser.parseVertexPosition(v);

      bounds_lower.x = Math.min(position.x, bounds_lower.x);
      bounds_lower.y = Math.min(position.y, bounds_lower.y);
      bounds_lower.z = Math.min(position.z, bounds_lower.z);
      bounds_upper.x = Math.max(position.x, bounds_upper.x);
      bounds_upper.y = Math.max(position.y, bounds_upper.y);
      bounds_upper.z = Math.max(position.z, bounds_upper.z);

      events.eventMeshVertexPosition(index, position);

      if (type.hasNormal()) {
        final Element n =
          RXMLUtilities.getChild(v, "n", RXMLConstants.MESHES_URI);
        final RVectorI3F<RSpaceObject> vn =
          RXMLUtilities.getElementAttributesVector3f(
            n,
            RXMLConstants.MESHES_URI);
        events.eventMeshVertexNormal(index, vn);
      }

      if (type.hasTangent3f()) {
        final Element t =
          RXMLUtilities.getChild(v, "t3", RXMLConstants.MESHES_URI);
        final RVectorI3F<RSpaceObject> vt =
          RXMLUtilities.getElementAttributesVector3f(
            t,
            RXMLConstants.MESHES_URI);
        events.eventMeshVertexTangent3f(index, vt);
      }

      if (type.hasTangent4f()) {
        final Element t =
          RXMLUtilities.getChild(v, "t4", RXMLConstants.MESHES_URI);
        final RVectorI4F<RSpaceObject> vt =
          RXMLUtilities.getElementAttributesVector4f(
            t,
            RXMLConstants.MESHES_URI);
        events.eventMeshVertexTangent4f(index, vt);
      }

      if (type.hasBitangent()) {
        final Element t =
          RXMLUtilities.getChild(v, "b", RXMLConstants.MESHES_URI);
        final RVectorI3F<RSpaceObject> vb =
          RXMLUtilities.getElementAttributesVector3f(
            t,
            RXMLConstants.MESHES_URI);
        events.eventMeshVertexBitangent(index, vb);
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

    events.eventMeshVerticesEnded(new RVectorI3F<RSpaceObject>(
      bounds_lower.x,
      bounds_lower.y,
      bounds_lower.z), new RVectorI3F<RSpaceObject>(
      bounds_upper.x,
      bounds_upper.y,
      bounds_upper.z));
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
