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
import nu.xom.Elements;
import nu.xom.ValidityException;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.renderer.RSpaceTexture;
import com.io7m.renderer.RVectorI3F;
import com.io7m.renderer.xml.RXMLException;
import com.io7m.renderer.xml.RXMLException.RXMLExceptionValidityError;
import com.io7m.renderer.xml.RXMLUtilities;

@Immutable public final class SBMaterialDescription
{
  static @Nonnull KMatrix3x3F<KMatrixUV> matrix3x3Parse(
    final @Nonnull Element e,
    final @Nonnull URI uri)
    throws RXMLException,
      ConstraintError,
      RXMLExceptionValidityError
  {
    final Elements eus = RXMLUtilities.getChildren(e, "column", uri);
    if (eus.size() != 3) {
      throw new RXMLException.RXMLExceptionValidityError(
        new ValidityException("Expected exactly three uv-matrix columns"));
    }

    final Element ec0 = eus.get(0);
    final RVectorI3F<RSpaceTexture> c0 =
      RXMLUtilities.getElementAttributesMatrixColumn3(ec0, uri);
    final Element ec1 = eus.get(1);
    final RVectorI3F<RSpaceTexture> c1 =
      RXMLUtilities.getElementAttributesMatrixColumn3(ec1, uri);
    final Element ec2 = eus.get(2);
    final RVectorI3F<RSpaceTexture> c2 =
      RXMLUtilities.getElementAttributesMatrixColumn3(ec2, uri);
    final KMatrix3x3F<KMatrixUV> uvm = new KMatrix3x3F<KMatrixUV>(c0, c1, c2);
    return uvm;
  }

  static void matrix3x3Write(
    final @Nonnull String uri,
    final @Nonnull KMatrix3x3F<KMatrixUV> uvm,
    final @Nonnull Element euv)
  {
    for (int c = 0; c < 3; ++c) {
      final Element emc = new Element("s:column", uri);
      final RVectorI3F<RSpaceTexture> col =
        new RVectorI3F<RSpaceTexture>(
          uvm.getRowColumnF(0, c),
          uvm.getRowColumnF(1, c),
          uvm.getRowColumnF(2, c));

      RXMLUtilities.putElementAttributesMatrixColumn3(
        emc,
        col,
        "s",
        SBSceneDescription.SCENE_XML_URI);
      euv.appendChild(emc);
    }
  }

  public static @Nonnull SBMaterialDescription fromXML(
    final @Nonnull Element e)
    throws RXMLException,
      ConstraintError
  {
    final URI uri = SBSceneDescription.SCENE_XML_URI;

    RXMLUtilities.checkIsElement(e, "material", uri);

    final Element ed = RXMLUtilities.getChild(e, "albedo", uri);
    final Element ea = RXMLUtilities.getChild(e, "alpha", uri);
    final Element em = RXMLUtilities.getChild(e, "emissive", uri);
    final Element es = RXMLUtilities.getChild(e, "specular", uri);
    final Element en = RXMLUtilities.getChild(e, "normal", uri);
    final Element ee = RXMLUtilities.getChild(e, "environment", uri);
    final Element emu = RXMLUtilities.getChild(e, "uv-matrix", uri);

    return new SBMaterialDescription(
      SBMaterialAlphaDescription.fromXML(ea),
      SBMaterialAlbedoDescription.fromXML(ed),
      SBMaterialEmissiveDescription.fromXML(em),
      SBMaterialSpecularDescription.fromXML(es),
      SBMaterialEnvironmentDescription.fromXML(ee),
      SBMaterialNormalDescription.fromXML(en),
      SBMaterialDescription.matrix3x3Parse(emu, uri));
  }

  private final @Nonnull SBMaterialAlphaDescription       alpha;
  private final @Nonnull SBMaterialAlbedoDescription      albedo;
  private final @Nonnull SBMaterialEmissiveDescription    emissive;
  private final @Nonnull SBMaterialEnvironmentDescription environment;
  private final @Nonnull SBMaterialNormalDescription      normal;
  private final @Nonnull SBMaterialSpecularDescription    specular;
  private final @Nonnull KMatrix3x3F<KMatrixUV>           uv_matrix;

  SBMaterialDescription(
    final @Nonnull SBMaterialAlphaDescription alpha,
    final @Nonnull SBMaterialAlbedoDescription albedo,
    final @Nonnull SBMaterialEmissiveDescription emissive,
    final @Nonnull SBMaterialSpecularDescription specular,
    final @Nonnull SBMaterialEnvironmentDescription environment,
    final @Nonnull SBMaterialNormalDescription normal,
    final @Nonnull KMatrix3x3F<KMatrixUV> uv_matrix)
    throws ConstraintError
  {
    this.alpha = Constraints.constrainNotNull(alpha, "Alpha");
    this.albedo = Constraints.constrainNotNull(albedo, "Albedo");
    this.emissive = Constraints.constrainNotNull(emissive, "Emissive");
    this.specular = Constraints.constrainNotNull(specular, "Specular");
    this.environment =
      Constraints.constrainNotNull(environment, "Environment");
    this.normal = Constraints.constrainNotNull(normal, "Normal");
    this.uv_matrix = Constraints.constrainNotNull(uv_matrix, "UV matrix");
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
    if (this.alpha == null) {
      if (other.alpha != null) {
        return false;
      }
    } else if (!this.alpha.equals(other.alpha)) {
      return false;
    }
    if (this.albedo == null) {
      if (other.albedo != null) {
        return false;
      }
    } else if (!this.albedo.equals(other.albedo)) {
      return false;
    }
    if (this.environment == null) {
      if (other.environment != null) {
        return false;
      }
    } else if (!this.environment.equals(other.environment)) {
      return false;
    }
    if (this.normal == null) {
      if (other.normal != null) {
        return false;
      }
    } else if (!this.normal.equals(other.normal)) {
      return false;
    }
    if (this.specular == null) {
      if (other.specular != null) {
        return false;
      }
    } else if (!this.specular.equals(other.specular)) {
      return false;
    }
    return true;
  }

  public @Nonnull SBMaterialEmissiveDescription getEmissive()
  {
    return this.emissive;
  }

  public @Nonnull SBMaterialAlbedoDescription getAlbedo()
  {
    return this.albedo;
  }

  public @Nonnull SBMaterialEnvironmentDescription getEnvironment()
  {
    return this.environment;
  }

  public @Nonnull SBMaterialNormalDescription getNormal()
  {
    return this.normal;
  }

  public @Nonnull SBMaterialSpecularDescription getSpecular()
  {
    return this.specular;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.alpha.hashCode();
    result = (prime * result) + this.albedo.hashCode();
    result = (prime * result) + this.emissive.hashCode();
    result = (prime * result) + this.environment.hashCode();
    result = (prime * result) + this.normal.hashCode();
    result = (prime * result) + this.specular.hashCode();
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("SBMaterialDescription [alpha=");
    builder.append(this.alpha);
    builder.append(", albedo=");
    builder.append(this.albedo);
    builder.append(", specular=");
    builder.append(this.specular);
    builder.append(", environment=");
    builder.append(this.environment);
    builder.append(", normal=");
    builder.append(this.normal);
    builder.append("]");
    return builder.toString();
  }

  @Nonnull Element toXML()
  {
    final String uri = SBSceneDescription.SCENE_XML_URI.toString();
    final Element e = new Element("s:material", uri);

    final Element euv = new Element("s:uv-matrix", uri);
    SBMaterialDescription.matrix3x3Write(uri, this.uv_matrix, euv);

    e.appendChild(this.alpha.toXML());
    e.appendChild(this.albedo.toXML());
    e.appendChild(this.emissive.toXML());
    e.appendChild(this.normal.toXML());
    e.appendChild(this.specular.toXML());
    e.appendChild(this.environment.toXML());
    e.appendChild(euv);

    return e;
  }

  public @Nonnull SBMaterialAlphaDescription getAlpha()
  {
    return this.alpha;
  }

  public @Nonnull KMatrix3x3F<KMatrixUV> getUVMatrix()
  {
    return this.uv_matrix;
  }
}
