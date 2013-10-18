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

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jvvfs.PathVirtual;
import com.io7m.renderer.xml.RXMLException;
import com.io7m.renderer.xml.RXMLUtilities;

public final class SBMaterialEmissiveDescription
{
  public static @Nonnull SBMaterialEmissiveDescription fromXML(
    final @Nonnull Element e)
    throws RXMLException,
      ConstraintError
  {
    final URI uri = SBSceneDescription.SCENE_XML_URI;
    RXMLUtilities.checkIsElement(e, "emissive", uri);
    final Element em = RXMLUtilities.getChild(e, "emission", uri);
    final Element et = RXMLUtilities.getChild(e, "texture", uri);

    final PathVirtual texture =
      (et.getValue().length() == 0) ? null : PathVirtual.ofString(et
        .getValue());

    final float emission = RXMLUtilities.getElementFloat(em);
    return new SBMaterialEmissiveDescription(emission, texture);
  }

  private final float                     emission;
  private final @CheckForNull PathVirtual texture;

  public SBMaterialEmissiveDescription(
    final float emission,
    final @CheckForNull PathVirtual texture)
  {
    this.emission = emission;
    this.texture = texture;
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
    final SBMaterialEmissiveDescription other =
      (SBMaterialEmissiveDescription) obj;
    if (Float.floatToIntBits(this.emission) != Float
      .floatToIntBits(other.emission)) {
      return false;
    }
    if (this.texture == null) {
      if (other.texture != null) {
        return false;
      }
    } else if (!this.texture.equals(other.texture)) {
      return false;
    }
    return true;
  }

  public float getEmission()
  {
    return this.emission;
  }

  public @CheckForNull PathVirtual getTexture()
  {
    return this.texture;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + Float.floatToIntBits(this.emission);
    result =
      (prime * result)
        + ((this.texture == null) ? 0 : this.texture.hashCode());
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("SBMaterialEmissiveDescription [emission=");
    builder.append(this.emission);
    builder.append(", texture=");
    builder.append(this.texture);
    builder.append("]");
    return builder.toString();
  }

  @SuppressWarnings("boxing") @Nonnull Element toXML()
  {
    final String uri = SBSceneDescription.SCENE_XML_URI.toString();
    final Element e = new Element("s:emissive", uri);

    final Element ed = new Element("s:texture", uri);
    if (this.texture != null) {
      ed.appendChild(this.texture.toString());
    }

    final Element em = new Element("s:emission", uri);
    em.appendChild(String.format("%.6f", this.emission));

    e.appendChild(ed);
    e.appendChild(em);
    return e;
  }
}