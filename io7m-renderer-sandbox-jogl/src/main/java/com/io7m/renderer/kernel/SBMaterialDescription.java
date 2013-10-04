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
import javax.annotation.concurrent.Immutable;

import nu.xom.Element;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.renderer.RSpaceRGBA;
import com.io7m.renderer.RVectorI4F;
import com.io7m.renderer.xml.RXMLException;
import com.io7m.renderer.xml.RXMLUtilities;

@Immutable public final class SBMaterialDescription
{
  private final @Nonnull RVectorI4F<RSpaceRGBA> diffuse;
  private final @CheckForNull String            texture_diffuse;
  private final @CheckForNull String            texture_normal;
  private final @CheckForNull String            texture_specular;
  private final float                           specular_intensity;
  private final float                           specular_exponent;

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[SBMaterialDescription ");
    builder.append(this.diffuse);
    builder.append(" ");
    builder.append(this.texture_diffuse);
    builder.append(" ");
    builder.append(this.texture_normal);
    builder.append(" ");
    builder.append(this.texture_specular);
    builder.append(" ");
    builder.append(this.specular_intensity);
    builder.append(" ");
    builder.append(this.specular_exponent);
    builder.append("]");
    return builder.toString();
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result =
      (prime * result)
        + ((this.diffuse == null) ? 0 : this.diffuse.hashCode());
    result = (prime * result) + Float.floatToIntBits(this.specular_exponent);
    result = (prime * result) + Float.floatToIntBits(this.specular_intensity);
    result =
      (prime * result)
        + ((this.texture_diffuse == null) ? 0 : this.texture_diffuse
          .hashCode());
    result =
      (prime * result)
        + ((this.texture_normal == null) ? 0 : this.texture_normal.hashCode());
    result =
      (prime * result)
        + ((this.texture_specular == null) ? 0 : this.texture_specular
          .hashCode());
    return result;
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
    final SBMaterialDescription other = (SBMaterialDescription) obj;
    if (this.diffuse == null) {
      if (other.diffuse != null) {
        return false;
      }
    } else if (!this.diffuse.equals(other.diffuse)) {
      return false;
    }
    if (Float.floatToIntBits(this.specular_exponent) != Float
      .floatToIntBits(other.specular_exponent)) {
      return false;
    }
    if (Float.floatToIntBits(this.specular_intensity) != Float
      .floatToIntBits(other.specular_intensity)) {
      return false;
    }
    if (this.texture_diffuse == null) {
      if (other.texture_diffuse != null) {
        return false;
      }
    } else if (!this.texture_diffuse.equals(other.texture_diffuse)) {
      return false;
    }
    if (this.texture_normal == null) {
      if (other.texture_normal != null) {
        return false;
      }
    } else if (!this.texture_normal.equals(other.texture_normal)) {
      return false;
    }
    if (this.texture_specular == null) {
      if (other.texture_specular != null) {
        return false;
      }
    } else if (!this.texture_specular.equals(other.texture_specular)) {
      return false;
    }
    return true;
  }

  SBMaterialDescription(
    final @Nonnull RVectorI4F<RSpaceRGBA> diffuse,
    final @CheckForNull String texture_diffuse,
    final @CheckForNull String texture_normal,
    final @CheckForNull String texture_specular,
    final float specular_intensity,
    final float specular_exponent)
  {
    this.diffuse =
      new RVectorI4F<RSpaceRGBA>(
        diffuse.getXF(),
        diffuse.getYF(),
        diffuse.getZF(),
        diffuse.getWF());

    this.texture_diffuse = texture_diffuse;
    this.texture_normal = texture_normal;
    this.texture_specular = texture_specular;
    this.specular_intensity = specular_intensity;
    this.specular_exponent = specular_exponent;
  }

  public @Nonnull RVectorI4F<RSpaceRGBA> getDiffuse()
  {
    return this.diffuse;
  }

  public float getSpecularExponent()
  {
    return this.specular_exponent;
  }

  public float getSpecularIntensity()
  {
    return this.specular_intensity;
  }

  public @CheckForNull String getTextureDiffuse()
  {
    return this.texture_diffuse;
  }

  public @CheckForNull String getTextureNormal()
  {
    return this.texture_normal;
  }

  public @CheckForNull String getTextureSpecular()
  {
    return this.texture_specular;
  }

  @SuppressWarnings("boxing") @Nonnull Element toXML()
  {
    final String uri = SBSceneDescription.SCENE_XML_URI.toString();
    final Element e = new Element("s:material", uri);

    final Element edc = new Element("s:diffuse-colour", uri);
    RXMLUtilities.putElementAttributesRGBA(
      edc,
      this.diffuse,
      "s",
      SBSceneDescription.SCENE_XML_URI);

    final Element ed = new Element("s:texture-diffuse", uri);
    if (this.texture_diffuse != null) {
      ed.appendChild(this.texture_diffuse);
    }
    final Element en = new Element("s:texture-normal", uri);
    if (this.texture_normal != null) {
      en.appendChild(this.texture_normal);
    }
    final Element es = new Element("s:texture-specular", uri);
    if (this.texture_specular != null) {
      es.appendChild(this.texture_specular);
    }

    final Element ese = new Element("s:specular-exponent", uri);
    ese.appendChild(String.format("%.6f", this.specular_exponent));
    final Element esi = new Element("s:specular-intensity", uri);
    esi.appendChild(String.format("%.6f", this.specular_intensity));

    e.appendChild(edc);
    e.appendChild(ed);
    e.appendChild(en);
    e.appendChild(es);
    e.appendChild(ese);
    e.appendChild(esi);
    return e;
  }

  public static @Nonnull SBMaterialDescription fromXML(
    final @Nonnull Element e)
    throws RXMLException,
      ConstraintError
  {
    final URI uri = SBSceneDescription.SCENE_XML_URI;

    RXMLUtilities.checkIsElement(e, "material", uri);

    final Element edc = RXMLUtilities.getChild(e, "diffuse-colour", uri);
    final Element ed = RXMLUtilities.getChild(e, "texture-diffuse", uri);
    final Element en = RXMLUtilities.getChild(e, "texture-normal", uri);
    final Element es = RXMLUtilities.getChild(e, "texture-specular", uri);
    final Element ese = RXMLUtilities.getChild(e, "specular-exponent", uri);
    final Element esi = RXMLUtilities.getChild(e, "specular-intensity", uri);

    final String texture_diffuse =
      (ed.getValue().length() == 0) ? null : ed.getValue();
    final String texture_normal =
      (en.getValue().length() == 0) ? null : en.getValue();
    final String texture_specular =
      (es.getValue().length() == 0) ? null : es.getValue();

    final float specular_exponent = RXMLUtilities.getElementFloat(ese);
    final float specular_intensity = RXMLUtilities.getElementFloat(esi);

    final RVectorI4F<RSpaceRGBA> diffuse_colour =
      RXMLUtilities.getElementAttributesRGBA(edc, uri);

    return new SBMaterialDescription(
      diffuse_colour,
      texture_diffuse,
      texture_normal,
      texture_specular,
      specular_intensity,
      specular_exponent);
  }
}
