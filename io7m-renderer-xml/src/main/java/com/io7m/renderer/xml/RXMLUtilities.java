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

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.renderer.RSpace;
import com.io7m.renderer.RSpaceRGB;
import com.io7m.renderer.RVectorI2F;
import com.io7m.renderer.RVectorI3F;

final class RXMLUtilities
{
  static @Nonnull Element checkIsElement(
    final @Nonnull Element e,
    final @Nonnull String name,
    final @Nonnull URI uri)
    throws RXMLException,
      ConstraintError
  {
    if (e.getNamespaceURI().equals(uri.toString()) == false) {
      final StringBuilder message = new StringBuilder();
      message.append("Incorrect namespace (expected '");
      message.append(uri);
      message.append("')");
      throw new RXMLException.RXMLExceptionValidityError(
        new ValidityException(message.toString()));
    }
    if (e.getLocalName().equals(name) == false) {
      final StringBuilder message = new StringBuilder();
      message.append("Incorrect name (expected '");
      message.append(name);
      message.append("')");
      throw new RXMLException.RXMLExceptionValidityError(
        new ValidityException(message.toString()));
    }
    return e;
  }

  static @Nonnull Attribute getAttribute(
    final @Nonnull Element e,
    final @Nonnull String name,
    final @Nonnull URI uri)
    throws RXMLException,
      ConstraintError
  {
    final Attribute a = e.getAttribute(name, uri.toString());
    if (a == null) {
      final StringBuilder message = new StringBuilder();
      message.append("Expected an attribute '");
      message.append(name);
      message.append("'");
      throw new RXMLException.RXMLExceptionValidityError(
        new ValidityException(message.toString()));
    }
    return a;
  }

  public static boolean getAttributeBoolean(
    final @Nonnull Attribute a)
  {
    return Boolean.valueOf(a.getValue()).booleanValue();
  }

  static float getAttributeFloat(
    final @Nonnull Attribute a)
    throws RXMLException,
      ConstraintError
  {
    try {
      return Float.valueOf(a.getValue()).floatValue();
    } catch (final NumberFormatException x) {
      final StringBuilder message = new StringBuilder();
      message.append("Expected a floating point value but got '");
      message.append(a.getValue());
      message.append("'");
      throw new RXMLException.RXMLExceptionValidityError(
        new ValidityException(message.toString()));
    }
  }

  static int getAttributeInteger(
    final @Nonnull Attribute a)
    throws RXMLException,
      ConstraintError
  {
    try {
      return Integer.valueOf(a.getValue()).intValue();
    } catch (final NumberFormatException x) {
      final StringBuilder message = new StringBuilder();
      message.append("Expected an integer but got '");
      message.append(a.getValue());
      message.append("'");
      throw new RXMLException.RXMLExceptionValidityError(
        new ValidityException(message.toString()));
    }
  }

  static @Nonnull Element getChild(
    final @Nonnull Element e,
    final @Nonnull String name,
    final @Nonnull URI uri)
    throws RXMLException,
      ConstraintError
  {
    final Elements es = e.getChildElements(name, uri.toString());
    if (es.size() == 0) {
      final StringBuilder message = new StringBuilder();
      message.append("Expected at least one child element with name '");
      message.append(name);
      message.append("'");
      throw new RXMLException.RXMLExceptionValidityError(
        new ValidityException(message.toString()));
    }
    return RXMLUtilities.checkIsElement(es.get(0), name, uri);
  }

  static @Nonnull Elements getChildren(
    final @Nonnull Element e,
    final @Nonnull String name,
    final @Nonnull URI uri)
  {
    return e.getChildElements(name, uri.toString());
  }

  static @Nonnull
    <T extends RSpace>
    RVectorI2F<T>
    getElementAttributesVector2f(
      final @Nonnull Element e,
      final @Nonnull URI uri)
      throws RXMLException,
        ConstraintError
  {
    final Attribute ax = RXMLUtilities.getAttribute(e, "x", uri);
    final Attribute ay = RXMLUtilities.getAttribute(e, "y", uri);
    return new RVectorI2F<T>(
      RXMLUtilities.getAttributeFloat(ax),
      RXMLUtilities.getAttributeFloat(ay));
  }

  static @Nonnull
    <T extends RSpace>
    RVectorI3F<T>
    getElementAttributesVector3f(
      final @Nonnull Element e,
      final @Nonnull URI uri)
      throws RXMLException,
        ConstraintError
  {
    final Attribute ax = RXMLUtilities.getAttribute(e, "x", uri);
    final Attribute ay = RXMLUtilities.getAttribute(e, "y", uri);
    final Attribute az = RXMLUtilities.getAttribute(e, "z", uri);
    return new RVectorI3F<T>(
      RXMLUtilities.getAttributeFloat(ax),
      RXMLUtilities.getAttributeFloat(ay),
      RXMLUtilities.getAttributeFloat(az));
  }

  static float getElementFloat(
    final @Nonnull Element e)
    throws RXMLException,
      ConstraintError
  {
    try {
      return Float.parseFloat(e.getValue());
    } catch (final NumberFormatException x) {
      final StringBuilder message = new StringBuilder();
      message.append("Expected a floating point value but got '");
      message.append(e.getValue());
      message.append("'");
      throw new RXMLException.RXMLExceptionValidityError(
        new ValidityException(message.toString()));
    }
  }

  static int getElementInteger(
    final @Nonnull Element e)
    throws RXMLException,
      ConstraintError
  {
    try {
      return Integer.valueOf(e.getValue()).intValue();
    } catch (final NumberFormatException x) {
      final StringBuilder message = new StringBuilder();
      message.append("Expected an integer but got '");
      message.append(e.getValue());
      message.append("'");
      throw new RXMLException.RXMLExceptionValidityError(
        new ValidityException(message.toString()));
    }
  }

  static @Nonnull String getElementNonEmptyString(
    final @Nonnull Element e)
    throws RXMLException,
      ConstraintError
  {
    if (e.getValue().length() == 0) {
      final StringBuilder message = new StringBuilder();
      message.append("Expected a non-empty string");
      throw new RXMLException.RXMLExceptionValidityError(
        new ValidityException(message.toString()));
    }
    return e.getValue();
  }

  static @Nonnull RVectorI3F<RSpaceRGB> getElementRGB(
    final @Nonnull Element e,
    final @Nonnull URI uri)
    throws RXMLException,
      ConstraintError
  {
    final Element er = RXMLUtilities.getChild(e, "r", uri);
    final Element eg = RXMLUtilities.getChild(e, "g", uri);
    final Element eb = RXMLUtilities.getChild(e, "b", uri);
    return new RVectorI3F<RSpaceRGB>(
      RXMLUtilities.getElementFloat(er),
      RXMLUtilities.getElementFloat(eg),
      RXMLUtilities.getElementFloat(eb));
  }

  static @Nonnull <T extends RSpace> RVectorI3F<T> getElementVector3f(
    final @Nonnull Element e,
    final @Nonnull URI uri)
    throws RXMLException,
      ConstraintError
  {
    final Element ex = RXMLUtilities.getChild(e, "x", uri);
    final Element ey = RXMLUtilities.getChild(e, "y", uri);
    final Element ez = RXMLUtilities.getChild(e, "z", uri);
    return new RVectorI3F<T>(
      RXMLUtilities.getElementFloat(ex),
      RXMLUtilities.getElementFloat(ey),
      RXMLUtilities.getElementFloat(ez));
  }

  static @CheckForNull Element getOptionalChild(
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

  static boolean isElement(
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
}
