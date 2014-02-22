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

package com.io7m.renderer.xml;

import java.net.URI;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ValidityException;

import com.io7m.jtensors.QuaternionI4F;
import com.io7m.jtensors.VectorReadable3F;
import com.io7m.jtensors.VectorReadable4F;
import com.io7m.renderer.types.RMatrixI3x3F;
import com.io7m.renderer.types.RMatrixI4x4F;
import com.io7m.renderer.types.RSpace;
import com.io7m.renderer.types.RSpaceRGB;
import com.io7m.renderer.types.RSpaceRGBA;
import com.io7m.renderer.types.RTransform;
import com.io7m.renderer.types.RVectorI2F;
import com.io7m.renderer.types.RVectorI3F;
import com.io7m.renderer.types.RVectorI4F;
import com.io7m.renderer.types.RVectorReadable3F;
import com.io7m.renderer.types.RVectorReadable4F;
import com.io7m.renderer.types.RXMLException;

public final class RXMLUtilities
{
  public static @Nonnull Element checkIsElement(
    final @Nonnull Element e,
    final @Nonnull String name,
    final @Nonnull URI uri)
    throws RXMLException
  {
    if (e.getNamespaceURI().equals(uri.toString()) == false) {
      final StringBuilder message = new StringBuilder();
      message.append("Incorrect namespace (expected '");
      message.append(uri);
      message.append("')");
      throw RXMLException.validityException(new ValidityException(message
        .toString()));
    }
    if (e.getLocalName().equals(name) == false) {
      final StringBuilder message = new StringBuilder();
      message.append("Incorrect name (expected '");
      message.append(name);
      message.append("')");
      throw RXMLException.validityException(new ValidityException(message
        .toString()));
    }
    return e;
  }

  public static @Nonnull Attribute getAttribute(
    final @Nonnull Element e,
    final @Nonnull String name,
    final @Nonnull URI uri)
    throws RXMLException
  {
    final Attribute a = e.getAttribute(name, uri.toString());
    if (a == null) {
      final StringBuilder message = new StringBuilder();
      message.append("Expected an attribute '");
      message.append(name);
      message.append("'");
      throw RXMLException.validityException(new ValidityException(message
        .toString()));
    }
    return a;
  }

  public static boolean getAttributeBoolean(
    final @Nonnull Attribute a)
  {
    return Boolean.valueOf(a.getValue()).booleanValue();
  }

  public static float getAttributeFloat(
    final @Nonnull Attribute a)
    throws RXMLException
  {
    try {
      return Float.valueOf(a.getValue()).floatValue();
    } catch (final NumberFormatException x) {
      final StringBuilder message = new StringBuilder();
      message.append("Expected a floating point value but got '");
      message.append(a.getValue());
      message.append("'");
      throw RXMLException.validityException(new ValidityException(message
        .toString()));
    }
  }

  public static @Nonnull Attribute getAttributeInDefaultNamespace(
    final @Nonnull Element e,
    final @Nonnull String name)
    throws RXMLException
  {
    final Attribute a = e.getAttribute(name);
    if (a == null) {
      final StringBuilder message = new StringBuilder();
      message.append("Expected an attribute '");
      message.append(name);
      message.append("'");
      throw RXMLException.validityException(new ValidityException(message
        .toString()));
    }
    return a;
  }

  public static int getAttributeInteger(
    final @Nonnull Attribute a)
    throws RXMLException
  {
    try {
      return Integer.valueOf(a.getValue()).intValue();
    } catch (final NumberFormatException x) {
      final StringBuilder message = new StringBuilder();
      message.append("Expected an integer but got '");
      message.append(a.getValue());
      message.append("'");
      throw RXMLException.validityException(new ValidityException(message
        .toString()));
    }
  }

  public static @Nonnull String getAttributeNonEmptyString(
    final @Nonnull Attribute a)
    throws RXMLException
  {
    if (a.getValue().length() == 0) {
      final StringBuilder message = new StringBuilder();
      message.append("Expected a non-empty string");
      throw RXMLException.validityException(new ValidityException(message
        .toString()));
    }
    return a.getValue();
  }

  public static @Nonnull Element getChild(
    final @Nonnull Element e,
    final @Nonnull String name,
    final @Nonnull URI uri)
    throws RXMLException
  {
    final Elements es = e.getChildElements(name, uri.toString());
    if (es.size() == 0) {
      final StringBuilder message = new StringBuilder();
      message.append("Expected at least one child element with name '");
      message.append(name);
      message.append("'");
      throw RXMLException.validityException(new ValidityException(message
        .toString()));
    }
    return RXMLUtilities.checkIsElement(es.get(0), name, uri);
  }

  public static @Nonnull Elements getChildren(
    final @Nonnull Element e,
    final @Nonnull String name,
    final @Nonnull URI uri)
  {
    return e.getChildElements(name, uri.toString());
  }

  public static @Nonnull
    <T extends RSpace>
    RVectorI3F<T>
    getElementAttributesMatrixColumn3(
      final @Nonnull Element e,
      final @Nonnull URI uri)
      throws RXMLException
  {
    final Attribute a0 = RXMLUtilities.getAttribute(e, "r0", uri);
    final Attribute a1 = RXMLUtilities.getAttribute(e, "r1", uri);
    final Attribute a2 = RXMLUtilities.getAttribute(e, "r2", uri);
    return new RVectorI3F<T>(
      RXMLUtilities.getAttributeFloat(a0),
      RXMLUtilities.getAttributeFloat(a1),
      RXMLUtilities.getAttributeFloat(a2));
  }

  public static @Nonnull
    <T extends RSpace>
    RVectorI4F<T>
    getElementAttributesMatrixColumn4(
      final @Nonnull Element e,
      final @Nonnull URI uri)
      throws RXMLException
  {
    final Attribute a0 = RXMLUtilities.getAttribute(e, "r0", uri);
    final Attribute a1 = RXMLUtilities.getAttribute(e, "r1", uri);
    final Attribute a2 = RXMLUtilities.getAttribute(e, "r2", uri);
    final Attribute a3 = RXMLUtilities.getAttribute(e, "r3", uri);
    return new RVectorI4F<T>(
      RXMLUtilities.getAttributeFloat(a0),
      RXMLUtilities.getAttributeFloat(a1),
      RXMLUtilities.getAttributeFloat(a2),
      RXMLUtilities.getAttributeFloat(a3));
  }

  public static @Nonnull QuaternionI4F getElementAttributesQuaternion4f(
    final @Nonnull Element e,
    final @Nonnull URI uri)
    throws RXMLException
  {
    final Attribute ax = RXMLUtilities.getAttribute(e, "x", uri);
    final Attribute ay = RXMLUtilities.getAttribute(e, "y", uri);
    final Attribute az = RXMLUtilities.getAttribute(e, "z", uri);
    final Attribute aw = RXMLUtilities.getAttribute(e, "w", uri);
    return new QuaternionI4F(
      RXMLUtilities.getAttributeFloat(ax),
      RXMLUtilities.getAttributeFloat(ay),
      RXMLUtilities.getAttributeFloat(az),
      RXMLUtilities.getAttributeFloat(aw));
  }

  public static @Nonnull RVectorI3F<RSpaceRGB> getElementAttributesRGB(
    final @Nonnull Element e,
    final @Nonnull URI uri)
    throws RXMLException
  {
    final Attribute ax = RXMLUtilities.getAttribute(e, "r", uri);
    final Attribute ay = RXMLUtilities.getAttribute(e, "g", uri);
    final Attribute az = RXMLUtilities.getAttribute(e, "b", uri);
    return new RVectorI3F<RSpaceRGB>(
      RXMLUtilities.getAttributeFloat(ax),
      RXMLUtilities.getAttributeFloat(ay),
      RXMLUtilities.getAttributeFloat(az));
  }

  public static @Nonnull RVectorI4F<RSpaceRGBA> getElementAttributesRGBA(
    final @Nonnull Element e,
    final @Nonnull URI uri)
    throws RXMLException
  {
    final Attribute ax = RXMLUtilities.getAttribute(e, "r", uri);
    final Attribute ay = RXMLUtilities.getAttribute(e, "g", uri);
    final Attribute az = RXMLUtilities.getAttribute(e, "b", uri);
    final Attribute aw = RXMLUtilities.getAttribute(e, "a", uri);
    return new RVectorI4F<RSpaceRGBA>(
      RXMLUtilities.getAttributeFloat(ax),
      RXMLUtilities.getAttributeFloat(ay),
      RXMLUtilities.getAttributeFloat(az),
      RXMLUtilities.getAttributeFloat(aw));
  }

  public static @Nonnull
    <T extends RSpace>
    RVectorI2F<T>
    getElementAttributesVector2f(
      final @Nonnull Element e,
      final @Nonnull URI uri)
      throws RXMLException
  {
    final Attribute ax = RXMLUtilities.getAttribute(e, "x", uri);
    final Attribute ay = RXMLUtilities.getAttribute(e, "y", uri);
    return new RVectorI2F<T>(
      RXMLUtilities.getAttributeFloat(ax),
      RXMLUtilities.getAttributeFloat(ay));
  }

  public static @Nonnull
    <T extends RSpace>
    RVectorI3F<T>
    getElementAttributesVector3f(
      final @Nonnull Element e,
      final @Nonnull URI uri)
      throws RXMLException
  {
    final Attribute ax = RXMLUtilities.getAttribute(e, "x", uri);
    final Attribute ay = RXMLUtilities.getAttribute(e, "y", uri);
    final Attribute az = RXMLUtilities.getAttribute(e, "z", uri);
    return new RVectorI3F<T>(
      RXMLUtilities.getAttributeFloat(ax),
      RXMLUtilities.getAttributeFloat(ay),
      RXMLUtilities.getAttributeFloat(az));
  }

  public static @Nonnull
    <T extends RSpace>
    RVectorI4F<T>
    getElementAttributesVector4f(
      final @Nonnull Element e,
      final @Nonnull URI uri)
      throws RXMLException
  {
    final Attribute ax = RXMLUtilities.getAttribute(e, "x", uri);
    final Attribute ay = RXMLUtilities.getAttribute(e, "y", uri);
    final Attribute az = RXMLUtilities.getAttribute(e, "z", uri);
    final Attribute aw = RXMLUtilities.getAttribute(e, "w", uri);
    return new RVectorI4F<T>(
      RXMLUtilities.getAttributeFloat(ax),
      RXMLUtilities.getAttributeFloat(ay),
      RXMLUtilities.getAttributeFloat(az),
      RXMLUtilities.getAttributeFloat(aw));
  }

  public static boolean getElementBoolean(
    final @Nonnull Element e)
    throws RXMLException
  {
    final String v = e.getValue();
    if (v.equals("true")) {
      return true;
    } else if (v.equals("false")) {
      return false;
    }

    final StringBuilder message = new StringBuilder();
    message.append("Expected a boolean value but got '");
    message.append(v);
    message.append("'");
    throw RXMLException.validityException(new ValidityException(message
      .toString()));
  }

  public static float getElementFloat(
    final @Nonnull Element e)
    throws RXMLException
  {
    try {
      return Float.parseFloat(e.getValue());
    } catch (final NumberFormatException x) {
      final StringBuilder message = new StringBuilder();
      message.append("Expected a floating point value but got '");
      message.append(e.getValue());
      message.append("'");
      throw RXMLException.validityException(new ValidityException(message
        .toString()));
    }
  }

  public static double getElementDouble(
    final @Nonnull Element e)
    throws RXMLException
  {
    try {
      return Double.parseDouble(e.getValue());
    } catch (final NumberFormatException x) {
      final StringBuilder message = new StringBuilder();
      message.append("Expected a floating point value but got '");
      message.append(e.getValue());
      message.append("'");
      throw RXMLException.validityException(new ValidityException(message
        .toString()));
    }
  }

  public static int getElementInteger(
    final @Nonnull Element e)
    throws RXMLException
  {
    try {
      return Integer.valueOf(e.getValue()).intValue();
    } catch (final NumberFormatException x) {
      final StringBuilder message = new StringBuilder();
      message.append("Expected an integer but got '");
      message.append(e.getValue());
      message.append("'");
      throw RXMLException.validityException(new ValidityException(message
        .toString()));
    }
  }

  public static @Nonnull
    <T extends RTransform>
    RMatrixI3x3F<T>
    getElementMatrixI3x3F(
      final @Nonnull Element e,
      final @Nonnull URI uri)
      throws RXMLException
  {
    final Elements eus = RXMLUtilities.getChildren(e, "column", uri);
    if (eus.size() != 3) {
      throw RXMLException.validityException(new ValidityException(
        "Expected exactly three matrix columns"));
    }

    final Element ec0 = eus.get(0);
    final RVectorI3F<RSpace> c0 =
      RXMLUtilities.getElementAttributesMatrixColumn3(ec0, uri);
    final Element ec1 = eus.get(1);
    final RVectorI3F<RSpace> c1 =
      RXMLUtilities.getElementAttributesMatrixColumn3(ec1, uri);
    final Element ec2 = eus.get(2);
    final RVectorI3F<RSpace> c2 =
      RXMLUtilities.getElementAttributesMatrixColumn3(ec2, uri);

    final RMatrixI3x3F<T> m = RMatrixI3x3F.newFromColumns(c0, c1, c2);
    return m;
  }

  public static @Nonnull
    <T extends RTransform>
    RMatrixI4x4F<T>
    getElementMatrixI4x4F(
      final @Nonnull Element e,
      final @Nonnull URI uri)
      throws RXMLException
  {
    final Elements eus = RXMLUtilities.getChildren(e, "column", uri);
    if (eus.size() != 4) {
      throw RXMLException.validityException(new ValidityException(
        "Expected exactly four matrix columns"));
    }

    final Element ec0 = eus.get(0);
    final RVectorI4F<RSpace> c0 =
      RXMLUtilities.getElementAttributesMatrixColumn4(ec0, uri);
    final Element ec1 = eus.get(1);
    final RVectorI4F<RSpace> c1 =
      RXMLUtilities.getElementAttributesMatrixColumn4(ec1, uri);
    final Element ec2 = eus.get(2);
    final RVectorI4F<RSpace> c2 =
      RXMLUtilities.getElementAttributesMatrixColumn4(ec2, uri);
    final Element ec3 = eus.get(3);
    final RVectorI4F<RSpace> c3 =
      RXMLUtilities.getElementAttributesMatrixColumn4(ec3, uri);

    final RMatrixI4x4F<T> m = RMatrixI4x4F.newFromColumns(c0, c1, c2, c3);
    return m;
  }

  public static @Nonnull String getElementNonEmptyString(
    final @Nonnull Element e)
    throws RXMLException
  {
    if (e.getValue().length() == 0) {
      final StringBuilder message = new StringBuilder();
      message.append("Expected a non-empty string");
      throw RXMLException.validityException(new ValidityException(message
        .toString()));
    }
    return e.getValue();
  }

  public static @Nonnull RVectorI3F<RSpaceRGB> getElementRGB(
    final @Nonnull Element e,
    final @Nonnull URI uri)
    throws RXMLException
  {
    final Element er = RXMLUtilities.getChild(e, "r", uri);
    final Element eg = RXMLUtilities.getChild(e, "g", uri);
    final Element eb = RXMLUtilities.getChild(e, "b", uri);
    return new RVectorI3F<RSpaceRGB>(
      RXMLUtilities.getElementFloat(er),
      RXMLUtilities.getElementFloat(eg),
      RXMLUtilities.getElementFloat(eb));
  }

  public static @Nonnull <T extends RSpace> RVectorI3F<T> getElementVector3f(
    final @Nonnull Element e,
    final @Nonnull URI uri)
    throws RXMLException
  {
    final Element ex = RXMLUtilities.getChild(e, "x", uri);
    final Element ey = RXMLUtilities.getChild(e, "y", uri);
    final Element ez = RXMLUtilities.getChild(e, "z", uri);
    return new RVectorI3F<T>(
      RXMLUtilities.getElementFloat(ex),
      RXMLUtilities.getElementFloat(ey),
      RXMLUtilities.getElementFloat(ez));
  }

  public static @CheckForNull Element getOptionalChild(
    final @Nonnull Element e,
    final @Nonnull String name,
    final @Nonnull URI uri)
  {
    final Elements es = e.getChildElements(name, uri.toString());
    if (es.size() == 0) {
      return null;
    }
    return es.get(0);
  }

  public static boolean isElement(
    final @Nonnull Element e,
    final @Nonnull String name,
    final @Nonnull URI uri)
  {
    if (e.getNamespaceURI().equals(uri.toString()) == false) {
      return false;
    }
    if (e.getLocalName().equals(name) == false) {
      return false;
    }
    return true;
  }

  public static void putElementAttributeInteger(
    final @Nonnull Element e,
    final @Nonnull String prefix,
    final @Nonnull String name,
    final int value,
    final @Nonnull URI uri)
  {
    final Attribute a =
      new Attribute(
        prefix + ":" + name,
        uri.toString(),
        Integer.toString(value));
    e.addAttribute(a);
  }

  public static void putElementAttributesMatrixColumn3(
    final @Nonnull Element e,
    final @Nonnull VectorReadable3F column,
    final @Nonnull String prefix,
    final @Nonnull URI uri)
  {
    final Attribute a0 =
      new Attribute(prefix + ":r0", uri.toString(), Float.toString(column
        .getXF()));
    final Attribute a1 =
      new Attribute(prefix + ":r1", uri.toString(), Float.toString(column
        .getYF()));
    final Attribute a2 =
      new Attribute(prefix + ":r2", uri.toString(), Float.toString(column
        .getZF()));

    e.addAttribute(a0);
    e.addAttribute(a1);
    e.addAttribute(a2);
  }

  public static void putElementAttributesMatrixColumn4(
    final @Nonnull Element e,
    final @Nonnull VectorReadable4F column,
    final @Nonnull String prefix,
    final @Nonnull URI uri)
  {
    final Attribute a0 =
      new Attribute(prefix + ":r0", uri.toString(), Float.toString(column
        .getXF()));
    final Attribute a1 =
      new Attribute(prefix + ":r1", uri.toString(), Float.toString(column
        .getYF()));
    final Attribute a2 =
      new Attribute(prefix + ":r2", uri.toString(), Float.toString(column
        .getZF()));
    final Attribute a3 =
      new Attribute(prefix + ":r3", uri.toString(), Float.toString(column
        .getWF()));

    e.addAttribute(a0);
    e.addAttribute(a1);
    e.addAttribute(a2);
    e.addAttribute(a3);
  }

  public static void putElementAttributesRGB(
    final @Nonnull Element e,
    final @Nonnull RVectorReadable3F<RSpaceRGB> rgb,
    final @Nonnull String prefix,
    final @Nonnull URI uri)
  {
    final Attribute ar =
      new Attribute(
        prefix + ":r",
        uri.toString(),
        Float.toString(rgb.getXF()));
    final Attribute ag =
      new Attribute(
        prefix + ":g",
        uri.toString(),
        Float.toString(rgb.getYF()));
    final Attribute ab =
      new Attribute(
        prefix + ":b",
        uri.toString(),
        Float.toString(rgb.getZF()));

    e.addAttribute(ar);
    e.addAttribute(ag);
    e.addAttribute(ab);
  }

  public static void putElementAttributesRGBA(
    final @Nonnull Element e,
    final @Nonnull RVectorReadable4F<RSpaceRGBA> rgba,
    final @Nonnull String prefix,
    final @Nonnull URI uri)
  {
    final Attribute ar =
      new Attribute(prefix + ":r", uri.toString(), Float.toString(rgba
        .getXF()));
    final Attribute ag =
      new Attribute(prefix + ":g", uri.toString(), Float.toString(rgba
        .getYF()));
    final Attribute ab =
      new Attribute(prefix + ":b", uri.toString(), Float.toString(rgba
        .getZF()));
    final Attribute aa =
      new Attribute(prefix + ":a", uri.toString(), Float.toString(rgba
        .getWF()));

    e.addAttribute(ar);
    e.addAttribute(ag);
    e.addAttribute(ab);
    e.addAttribute(aa);
  }

  public static void putElementAttributeString(
    final @Nonnull Element e,
    final @Nonnull String prefix,
    final @Nonnull String name,
    final @Nonnull String value,
    final @Nonnull URI uri)
  {
    final Attribute a =
      new Attribute(prefix + ":" + name, uri.toString(), value);
    e.addAttribute(a);
  }

  public static <T extends RSpace> void putElementAttributesVector3f(
    final @Nonnull Element e,
    final @Nonnull RVectorReadable3F<T> v,
    final @Nonnull String prefix,
    final @Nonnull URI uri)
  {
    final Attribute ax =
      new Attribute(prefix + ":x", uri.toString(), Float.toString(v.getXF()));
    final Attribute ay =
      new Attribute(prefix + ":y", uri.toString(), Float.toString(v.getYF()));
    final Attribute az =
      new Attribute(prefix + ":z", uri.toString(), Float.toString(v.getZF()));

    e.addAttribute(ax);
    e.addAttribute(ay);
    e.addAttribute(az);
  }

  public static <T extends RSpace> void putElementAttributesVector4f(
    final @Nonnull Element e,
    final @Nonnull RVectorReadable4F<T> v,
    final @Nonnull String prefix,
    final @Nonnull URI uri)
  {
    final Attribute ax =
      new Attribute(prefix + ":x", uri.toString(), Float.toString(v.getXF()));
    final Attribute ay =
      new Attribute(prefix + ":y", uri.toString(), Float.toString(v.getYF()));
    final Attribute az =
      new Attribute(prefix + ":z", uri.toString(), Float.toString(v.getZF()));
    final Attribute aw =
      new Attribute(prefix + ":w", uri.toString(), Float.toString(v.getWF()));

    e.addAttribute(ax);
    e.addAttribute(ay);
    e.addAttribute(az);
    e.addAttribute(aw);
  }

  @SuppressWarnings("boxing") public static void putElementDouble(
    final @Nonnull Element e,
    final @Nonnull String prefix,
    final @Nonnull String name,
    final double value,
    final @Nonnull URI uri)
  {
    RXMLUtilities.putElementString(
      e,
      prefix,
      name,
      String.format("%.15f", value),
      uri);
  }

  public static <T extends RTransform> void putElementMatrixI3x3F(
    final @Nonnull Element e,
    final @Nonnull String prefix,
    final @Nonnull RMatrixI3x3F<T> m,
    final @Nonnull URI uri)
  {
    for (int c = 0; c < 3; ++c) {
      final Element emc = new Element("s:column", uri.toString());
      final RVectorI3F<RSpace> col =
        new RVectorI3F<RSpace>(
          m.getRowColumnF(0, c),
          m.getRowColumnF(1, c),
          m.getRowColumnF(2, c));

      RXMLUtilities.putElementAttributesMatrixColumn3(emc, col, prefix, uri);
      e.appendChild(emc);
    }
  }

  public static <T extends RTransform> void putElementMatrixI4x4F(
    final @Nonnull Element e,
    final @Nonnull String prefix,
    final @Nonnull RMatrixI4x4F<T> m,
    final @Nonnull URI uri)
  {
    for (int c = 0; c < 4; ++c) {
      final Element emc = new Element("s:column", uri.toString());
      final RVectorI4F<RSpace> col =
        new RVectorI4F<RSpace>(
          m.getRowColumnF(0, c),
          m.getRowColumnF(1, c),
          m.getRowColumnF(2, c),
          m.getRowColumnF(3, c));

      RXMLUtilities.putElementAttributesMatrixColumn4(emc, col, prefix, uri);
      e.appendChild(emc);
    }
  }

  public static void putElementString(
    final @Nonnull Element e,
    final @Nonnull String prefix,
    final @Nonnull String name,
    final @Nonnull String value,
    final @Nonnull URI uri)
  {
    final Element en = new Element(prefix + ":" + name, uri.toString());
    en.appendChild(value);
    e.appendChild(en);
  }

  public static boolean hasChild(
    final @Nonnull Element e,
    final @Nonnull String name,
    final @Nonnull URI uri)
  {
    final Elements es = e.getChildElements(name, uri.toString());
    return es.size() == 1;
  }

  public static boolean hasChildren(
    final @Nonnull Element e,
    final @Nonnull String name,
    final @Nonnull URI uri)
  {
    final Elements es = e.getChildElements(name, uri.toString());
    return es.size() >= 1;
  }
}
