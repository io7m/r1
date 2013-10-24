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

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jvvfs.PathVirtual;
import com.io7m.renderer.RSpaceRGBA;
import com.io7m.renderer.RVectorI4F;
import com.io7m.renderer.xml.RXMLException;
import com.io7m.renderer.xml.RXMLUtilities;

public final class SBMaterialAlbedoDescription
{
  public static @Nonnull SBMaterialAlbedoDescription fromXML(
    final @Nonnull Element e)
    throws RXMLException,
      ConstraintError
  {
    final URI uri = SBSceneDescription.SCENE_XML_URI;
    RXMLUtilities.checkIsElement(e, "albedo", uri);
    final Element ec = RXMLUtilities.getChild(e, "colour", uri);
    final Element em = RXMLUtilities.getChild(e, "mix", uri);
    final Element et = RXMLUtilities.getChild(e, "texture", uri);

    final PathVirtual texture =
      (et.getValue().length() == 0) ? null : PathVirtual.ofString(et
        .getValue());

    final RVectorI4F<RSpaceRGBA> colour =
      RXMLUtilities.getElementAttributesRGBA(ec, uri);

    final float mix = RXMLUtilities.getElementFloat(em);

    return new SBMaterialAlbedoDescription(colour, mix, texture);
  }

  private final @Nonnull RVectorI4F<RSpaceRGBA> colour;
  private final float                           mix;
  private final @CheckForNull PathVirtual       texture;

  public SBMaterialAlbedoDescription(
    final @Nonnull RVectorI4F<RSpaceRGBA> colour,
    final float mix,
    final @CheckForNull PathVirtual texture)
    throws ConstraintError
  {
    this.colour = Constraints.constrainNotNull(colour, "Colour");
    this.mix = mix;
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
    final SBMaterialAlbedoDescription other =
      (SBMaterialAlbedoDescription) obj;
    if (!this.colour.equals(other.colour)) {
      return false;
    }
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

  public @Nonnull RVectorI4F<RSpaceRGBA> getColour()
  {
    return this.colour;
  }

  public float getMix()
  {
    return this.mix;
  }

  public @CheckForNull PathVirtual getTexture()
  {
    return this.texture;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.colour.hashCode();
    result = (prime * result) + Float.floatToIntBits(this.mix);
    result =
      (prime * result)
        + ((this.texture == null) ? 0 : this.texture.hashCode());
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("SBMaterialAlbedoDescription [colour=");
    builder.append(this.colour);
    builder.append(", mix=");
    builder.append(this.mix);
    builder.append(", texture=");
    builder.append(this.texture);
    builder.append("]");
    return builder.toString();
  }

  @SuppressWarnings("boxing") @Nonnull Element toXML()
  {
    final String uri = SBSceneDescription.SCENE_XML_URI.toString();
    final Element e = new Element("s:albedo", uri);

    final Element ec = new Element("s:colour", uri);
    RXMLUtilities.putElementAttributesRGBA(
      ec,
      this.colour,
      "s",
      SBSceneDescription.SCENE_XML_URI);

    final Element ed = new Element("s:texture", uri);
    if (this.texture != null) {
      ed.appendChild(this.texture.toString());
    }

    final Element em = new Element("s:mix", uri);
    em.appendChild(String.format("%.6f", this.mix));

    e.appendChild(ec);
    e.appendChild(ed);
    e.appendChild(em);
    return e;
  }
}
