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

import nu.xom.Element;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.renderer.xml.RXMLException;
import com.io7m.renderer.xml.RXMLUtilities;

public final class SBMaterialAlphaDescription
{
  public static @Nonnull SBMaterialAlphaDescription fromXML(
    final @Nonnull Element e)
    throws RXMLException,
      ConstraintError
  {
    final URI uri = SBSceneDescription.SCENE_XML_URI;
    RXMLUtilities.checkIsElement(e, "alpha", uri);
    final Element eo = RXMLUtilities.getChild(e, "opacity", uri);
    final Element et = RXMLUtilities.getChild(e, "translucent", uri);
    final boolean t = RXMLUtilities.getElementBoolean(et);
    final float o = RXMLUtilities.getElementFloat(eo);
    return new SBMaterialAlphaDescription(t, o);
  }

  private final boolean translucent;
  private final float   opacity;

  SBMaterialAlphaDescription(
    final boolean translucent,
    final float opacity)
  {
    this.translucent = translucent;
    this.opacity = opacity;
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
    final SBMaterialAlphaDescription other = (SBMaterialAlphaDescription) obj;
    if (Float.floatToIntBits(this.opacity) != Float
      .floatToIntBits(other.opacity)) {
      return false;
    }
    if (this.translucent != other.translucent) {
      return false;
    }
    return true;
  }

  public float getOpacity()
  {
    return this.opacity;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + Float.floatToIntBits(this.opacity);
    result = (prime * result) + (this.translucent ? 1231 : 1237);
    return result;
  }

  public boolean isTranslucent()
  {
    return this.translucent;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("SBMaterialAlphaDescription [translucent=");
    builder.append(this.translucent);
    builder.append(", opacity=");
    builder.append(this.opacity);
    builder.append("]");
    return builder.toString();
  }

  @SuppressWarnings("boxing") @Nonnull Element toXML()
  {
    final String uri = SBSceneDescription.SCENE_XML_URI.toString();
    final Element e = new Element("s:alpha", uri);

    final Element eo = new Element("s:opacity", uri);
    eo.appendChild(String.format("%.6f", this.opacity));
    final Element et = new Element("s:translucent", uri);
    et.appendChild(Boolean.toString(this.translucent));

    e.appendChild(eo);
    e.appendChild(et);
    return e;
  }
}
