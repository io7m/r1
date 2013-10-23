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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import nu.xom.Element;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.renderer.xml.RXMLException;
import com.io7m.renderer.xml.RXMLUtilities;

@Immutable public final class SBFrustum
{
  public static @Nonnull SBFrustum fromXML(
    final @Nonnull Element e)
    throws RXMLException,
      ConstraintError
  {
    final URI uri = SBSceneDescription.SCENE_XML_URI;
    RXMLUtilities.checkIsElement(e, "frustum", uri);
    final Element efov = RXMLUtilities.getChild(e, "fov", uri);
    final Element elen = RXMLUtilities.getChild(e, "length", uri);
    final Element easp = RXMLUtilities.getChild(e, "aspect", uri);
    return new SBFrustum(
      RXMLUtilities.getElementFloat(elen),
      RXMLUtilities.getElementFloat(efov),
      RXMLUtilities.getElementFloat(easp));
  }

  private final float length;
  private final float horizontal_fov;
  private final float aspect;

  SBFrustum(
    final float length,
    final float horizontal_fov,
    final float aspect)
  {
    this.length = length;
    this.horizontal_fov = horizontal_fov;
    this.aspect = aspect;
  }

  @Override public boolean equals(
    final Object obj)
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
    final SBFrustum other = (SBFrustum) obj;
    if (Float.floatToIntBits(this.aspect) != Float
      .floatToIntBits(other.aspect)) {
      return false;
    }
    if (Float.floatToIntBits(this.horizontal_fov) != Float
      .floatToIntBits(other.horizontal_fov)) {
      return false;
    }
    if (Float.floatToIntBits(this.length) != Float
      .floatToIntBits(other.length)) {
      return false;
    }
    return true;
  }

  public float getAspectRatio()
  {
    return this.aspect;
  }

  public float getHorizontalFOV()
  {
    return this.horizontal_fov;
  }

  public float getMaximumLength()
  {
    return this.length;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + Float.floatToIntBits(this.aspect);
    result = (prime * result) + Float.floatToIntBits(this.horizontal_fov);
    result = (prime * result) + Float.floatToIntBits(this.length);
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[SBFrustum [length");
    builder.append(this.length);
    builder.append("] [horizontal_fov ");
    builder.append(this.horizontal_fov);
    builder.append("] [aspect ");
    builder.append(this.aspect);
    builder.append("]]");
    return builder.toString();
  }

  public @Nonnull Element toXML()
  {
    final URI uri = SBSceneDescription.SCENE_XML_URI;
    final Element e = new Element("s:frustum", uri.toString());
    final Element efov = new Element("s:fov", uri.toString());
    final Element elen = new Element("s:length", uri.toString());
    final Element easp = new Element("s:aspect", uri.toString());
    efov.appendChild(Float.toString(this.horizontal_fov));
    easp.appendChild(Float.toString(this.aspect));
    elen.appendChild(Float.toString(this.length));
    e.appendChild(efov);
    e.appendChild(easp);
    e.appendChild(elen);
    return e;
  }

}
