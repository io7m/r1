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

package com.io7m.renderer.kernel;

import java.net.URI;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ValidityException;

import com.io7m.renderer.RSpace;
import com.io7m.renderer.RSpaceRGB;
import com.io7m.renderer.RVectorI3F;

final class SBXMLUtilities
{
  static @Nonnull Element checkIsElement(
    final @Nonnull Element e,
    final @Nonnull String name,
    final @Nonnull URI uri)
    throws ValidityException
  {
    if (e.getNamespaceURI().equals(uri.toString()) == false) {
      throw new ValidityException("Incorrect namespace (expected '"
        + uri
        + "')");
    }
    if (e.getLocalName().equals(name) == false) {
      throw new ValidityException("Incorrect name (expected '" + name + "')");
    }
    return e;
  }

  static @Nonnull Element getChild(
    final @Nonnull Element e,
    final @Nonnull String name,
    final @Nonnull URI uri)
    throws ValidityException
  {
    final Elements es = e.getChildElements(name, uri.toString());
    if (es.size() == 0) {
      throw new ValidityException(
        "Expected at least one child element with name '" + name + "'");
    }
    return SBXMLUtilities.checkIsElement(es.get(0), name, uri);
  }

  static float getFloat(
    final @Nonnull Element e)
    throws ValidityException
  {
    try {
      return Float.parseFloat(e.getValue());
    } catch (final NumberFormatException x) {
      throw new ValidityException("Expected a real number but got '"
        + e.getValue()
        + "'");
    }
  }

  static @Nonnull Integer getInteger(
    final @Nonnull Element e)
    throws ValidityException
  {
    try {
      return Integer.valueOf(e.getValue());
    } catch (final NumberFormatException x) {
      throw new ValidityException("Expected an integer but got '"
        + e.getValue()
        + "'");
    }
  }

  static @Nonnull String getNonEmptyString(
    final @Nonnull Element e)
    throws ValidityException
  {
    if (e.getValue().length() == 0) {
      throw new ValidityException("Expected a non-empty string");
    }
    return e.getValue();
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

  static @Nonnull RVectorI3F<RSpaceRGB> getRGB(
    final @Nonnull Element e,
    final @Nonnull URI uri)
    throws ValidityException
  {
    final Element er = SBXMLUtilities.getChild(e, "r", uri);
    final Element eg = SBXMLUtilities.getChild(e, "g", uri);
    final Element eb = SBXMLUtilities.getChild(e, "b", uri);
    return new RVectorI3F<RSpaceRGB>(
      SBXMLUtilities.getFloat(er),
      SBXMLUtilities.getFloat(eg),
      SBXMLUtilities.getFloat(eb));
  }

  static @Nonnull <T extends RSpace> RVectorI3F<T> getVector3f(
    final @Nonnull Element e,
    final @Nonnull URI uri)
    throws ValidityException
  {
    final Element ex = SBXMLUtilities.getChild(e, "x", uri);
    final Element ey = SBXMLUtilities.getChild(e, "y", uri);
    final Element ez = SBXMLUtilities.getChild(e, "z", uri);
    return new RVectorI3F<T>(
      SBXMLUtilities.getFloat(ex),
      SBXMLUtilities.getFloat(ey),
      SBXMLUtilities.getFloat(ez));
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
