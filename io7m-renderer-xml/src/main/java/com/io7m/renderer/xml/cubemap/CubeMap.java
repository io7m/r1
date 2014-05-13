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

import nu.xom.Attribute;
import nu.xom.Element;

import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.types.RXMLException;
import com.io7m.renderer.xml.RXMLUtilities;

@EqualityStructural public final class CubeMap
{
  public static final URI XML_URI;
  public static final int XML_VERSION;

  static {
    try {
      XML_URI = new URI("http://schemas.io7m.com/renderer/1.0.0/cube-maps");
      XML_VERSION = 1;
    } catch (final URISyntaxException e) {
      throw new UnreachableCodeException();
    }
  }

  public Element toXML()
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

  public static CubeMap fromXML(
    final Element e)
    throws RXMLException
  {
    NullCheck.notNull(e, "Element");

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

  private final String name;
  private final String positive_z;
  private final String negative_z;
  private final String positive_y;
  private final String negative_y;
  private final String positive_x;
  private final String negative_x;

  public CubeMap(
    final String in_name,
    final String pz,
    final String nz,
    final String py,
    final String ny,
    final String px,
    final String nx)
  {
    NullCheck.notNull(in_name, "Name");
    NullCheck.notNull(pz, "Positive Z");
    NullCheck.notNull(py, "Positive Y");
    NullCheck.notNull(px, "Positive X");
    NullCheck.notNull(nz, "Negative Z");
    NullCheck.notNull(ny, "Negative Y");
    NullCheck.notNull(nx, "Negative X");

    this.name = in_name;
    this.positive_z = pz;
    this.negative_z = nz;
    this.positive_y = py;
    this.negative_y = ny;
    this.positive_x = px;
    this.negative_x = nx;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.name.hashCode();
    result = (prime * result) + this.negative_x.hashCode();
    result = (prime * result) + this.negative_y.hashCode();
    result = (prime * result) + this.negative_z.hashCode();
    result = (prime * result) + this.positive_x.hashCode();
    result = (prime * result) + this.positive_y.hashCode();
    result = (prime * result) + this.positive_z.hashCode();
    return result;
  }

  @Override public boolean equals(
    final @Nullable Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final CubeMap other = (CubeMap) obj;

    return this.name.equals(other.name)
      && this.negative_x.equals(other.negative_x)
      && this.negative_y.equals(other.negative_y)
      && this.negative_z.equals(other.negative_z)
      && this.positive_x.equals(other.positive_x)
      && this.positive_y.equals(other.positive_y)
      && this.positive_z.equals(other.positive_z);
  }

  public String getName()
  {
    return this.name;
  }

  public String getNegativeX()
  {
    return this.negative_x;
  }

  public String getNegativeY()
  {
    return this.negative_y;
  }

  public String getNegativeZ()
  {
    return this.negative_z;
  }

  public String getPositiveX()
  {
    return this.positive_x;
  }

  public String getPositiveY()
  {
    return this.positive_y;
  }

  public String getPositiveZ()
  {
    return this.positive_z;
  }
}
