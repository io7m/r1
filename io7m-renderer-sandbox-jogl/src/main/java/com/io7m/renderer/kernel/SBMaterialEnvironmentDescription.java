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

public final class SBMaterialEnvironmentDescription
{
  public static @Nonnull SBMaterialEnvironmentDescription fromXML(
    final @Nonnull Element e)
    throws RXMLException,
      ConstraintError
  {
    final URI uri = SBSceneDescription.SCENE_XML_URI;
    RXMLUtilities.checkIsElement(e, "environment", uri);
    final Element em = RXMLUtilities.getChild(e, "mix", uri);
    final Element et = RXMLUtilities.getChild(e, "texture", uri);

    final PathVirtual texture =
      (et.getValue().length() == 0) ? null : PathVirtual.ofString(et
        .getValue());

    final float mix = RXMLUtilities.getElementFloat(em);
    return new SBMaterialEnvironmentDescription(texture, mix);
  }

  private final @CheckForNull PathVirtual texture;
  private final float                     mix;

  SBMaterialEnvironmentDescription(
    final @CheckForNull PathVirtual texture,
    final float mix)
  {
    this.texture = texture;
    this.mix = mix;
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
    final SBMaterialEnvironmentDescription other =
      (SBMaterialEnvironmentDescription) obj;
    if (Float.floatToIntBits(this.mix) != Float.floatToIntBits(other.mix)) {
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

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + Float.floatToIntBits(this.mix);
    result =
      (prime * result)
        + ((this.texture == null) ? 0 : this.texture.hashCode());
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("SBMaterialEnvironmentDescription [texture=");
    builder.append(this.texture);
    builder.append(", mix=");
    builder.append(this.mix);
    builder.append("]");
    return builder.toString();
  }

  @SuppressWarnings("boxing") @Nonnull Element toXML()
  {
    final String uri = SBSceneDescription.SCENE_XML_URI.toString();
    final Element e = new Element("s:environment", uri);

    final Element et = new Element("s:texture", uri);
    if (this.texture != null) {
      et.appendChild(this.texture.toString());
    }

    final Element ei = new Element("s:mix", uri);
    ei.appendChild(String.format("%.6f", this.mix));

    e.appendChild(ei);
    e.appendChild(et);
    return e;
  }

  public @CheckForNull PathVirtual getTexture()
  {
    return this.texture;
  }

  public float getMix()
  {
    return this.mix;
  }
}
