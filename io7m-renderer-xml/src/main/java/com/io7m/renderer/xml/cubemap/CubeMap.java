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

package com.io7m.renderer.xml.cubemap;

import java.net.URI;
import java.net.URISyntaxException;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import nu.xom.Attribute;
import nu.xom.Element;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.renderer.types.RXMLException;
import com.io7m.renderer.xml.RXMLUtilities;

@Immutable public final class CubeMap
{
  public static final @Nonnull URI XML_URI;
  public static final int          XML_VERSION;

  static {
    try {
      XML_URI = new URI("http://schemas.io7m.com/renderer/1.0.0/cube-maps");
      XML_VERSION = 1;
    } catch (final URISyntaxException e) {
      throw new UnreachableCodeException();
    }
  }

  public @Nonnull Element toXML()
  {
    final String uri = CubeMap.XML_URI.toString();
    final Element e = new Element("c:cube-map", uri);

    RXMLUtilities.putElementAttributeInteger(
      e,
      "c",
      "version",
      CubeMap.XML_VERSION,
      CubeMap.XML_URI);
    RXMLUtilities.putElementAttributeString(
      e,
      "c",
      "name",
      this.name,
      CubeMap.XML_URI);

    RXMLUtilities.putElementString(
      e,
      "c",
      "positive-z",
      this.positive_z,
      CubeMap.XML_URI);
    RXMLUtilities.putElementString(
      e,
      "c",
      "positive-y",
      this.positive_y,
      CubeMap.XML_URI);
    RXMLUtilities.putElementString(
      e,
      "c",
      "positive-x",
      this.positive_x,
      CubeMap.XML_URI);
    RXMLUtilities.putElementString(
      e,
      "c",
      "negative-z",
      this.negative_z,
      CubeMap.XML_URI);
    RXMLUtilities.putElementString(
      e,
      "c",
      "negative-y",
      this.negative_y,
      CubeMap.XML_URI);
    RXMLUtilities.putElementString(
      e,
      "c",
      "negative-x",
      this.negative_x,
      CubeMap.XML_URI);

    return e;
  }

  public static @Nonnull CubeMap fromXML(
    final @Nonnull Element e)
    throws RXMLException,
      ConstraintError
  {
    Constraints.constrainNotNull(e, "Element");

    final Attribute an =
      RXMLUtilities.getAttribute(e, "name", CubeMap.XML_URI);

    final Element pz =
      RXMLUtilities.getChild(e, "positive-z", CubeMap.XML_URI);
    final Element nz =
      RXMLUtilities.getChild(e, "negative-z", CubeMap.XML_URI);
    final Element py =
      RXMLUtilities.getChild(e, "positive-y", CubeMap.XML_URI);
    final Element ny =
      RXMLUtilities.getChild(e, "negative-y", CubeMap.XML_URI);
    final Element px =
      RXMLUtilities.getChild(e, "positive-x", CubeMap.XML_URI);
    final Element nx =
      RXMLUtilities.getChild(e, "negative-x", CubeMap.XML_URI);

    return new CubeMap(
      RXMLUtilities.getAttributeNonEmptyString(an),
      RXMLUtilities.getElementNonEmptyString(pz),
      RXMLUtilities.getElementNonEmptyString(nz),
      RXMLUtilities.getElementNonEmptyString(py),
      RXMLUtilities.getElementNonEmptyString(ny),
      RXMLUtilities.getElementNonEmptyString(px),
      RXMLUtilities.getElementNonEmptyString(nx));
  }

  private final @Nonnull String name;
  private final @Nonnull String positive_z;
  private final @Nonnull String negative_z;
  private final @Nonnull String positive_y;
  private final @Nonnull String negative_y;
  private final @Nonnull String positive_x;
  private final @Nonnull String negative_x;

  public CubeMap(
    final @Nonnull String name,
    final @Nonnull String pz,
    final @Nonnull String nz,
    final @Nonnull String py,
    final @Nonnull String ny,
    final @Nonnull String px,
    final @Nonnull String nx)
    throws ConstraintError
  {
    Constraints.constrainNotNull(name, "Name");
    Constraints.constrainNotNull(pz, "Positive Z");
    Constraints.constrainNotNull(py, "Positive Y");
    Constraints.constrainNotNull(px, "Positive X");
    Constraints.constrainNotNull(nz, "Negative Z");
    Constraints.constrainNotNull(ny, "Negative Y");
    Constraints.constrainNotNull(nx, "Negative X");

    this.name = name;
    this.positive_z = pz;
    this.negative_z = nz;
    this.positive_y = py;
    this.negative_y = ny;
    this.positive_x = px;
    this.negative_x = nx;
  }

  public @Nonnull String getName()
  {
    return this.name;
  }

  public @Nonnull String getNegativeX()
  {
    return this.negative_x;
  }

  public @Nonnull String getNegativeY()
  {
    return this.negative_y;
  }

  public @Nonnull String getNegativeZ()
  {
    return this.negative_z;
  }

  public @Nonnull String getPositiveX()
  {
    return this.positive_x;
  }

  public @Nonnull String getPositiveY()
  {
    return this.positive_y;
  }

  public @Nonnull String getPositiveZ()
  {
    return this.positive_z;
  }
}
