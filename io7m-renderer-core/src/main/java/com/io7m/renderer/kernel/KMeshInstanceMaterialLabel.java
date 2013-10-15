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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jcanephora.ArrayBuffer;

@Immutable public final class KMeshInstanceMaterialLabel
{
  public static enum Albedo
  {
    ALBEDO_TEXTURED("BT"),
    ALBEDO_COLOURED("BC");

    final @Nonnull String code;

    private Albedo(
      final @Nonnull String code)
    {
      this.code = code;
    }

    public @Nonnull String getCode()
    {
      return this.code;
    }
  }

  public static enum Alpha
  {
    ALPHA_OPAQUE("AO"),
    ALPHA_TRANSLUCENT("AT");

    final @Nonnull String code;

    private Alpha(
      final @Nonnull String code)
    {
      this.code = code;
    }

    public @Nonnull String getCode()
    {
      return this.code;
    }
  }

  public static enum Emissive
  {
    EMISSIVE_NONE(""),
    EMISSIVE_CONSTANT("MC"),
    EMISSIVE_MAPPED("MM");

    final @Nonnull String code;

    private Emissive(
      final @Nonnull String code)
    {
      this.code = code;
    }

    public @Nonnull String getCode()
    {
      return this.code;
    }
  }

  public static enum Environment
  {
    ENVIRONMENT_NONE(""),
    ENVIRONMENT_REFLECTIVE("EL"),
    ENVIRONMENT_REFRACTIVE("ER"),
    ENVIRONMENT_REFLECTIVE_REFRACTIVE("ELR");

    final @Nonnull String code;

    private Environment(
      final @Nonnull String code)
    {
      this.code = code;
    }

    public @Nonnull String getCode()
    {
      return this.code;
    }
  }

  public static enum Normal
  {
    NORMAL_NONE(""),
    NORMAL_VERTEX("NV"),
    NORMAL_MAPPED("NM");

    final @Nonnull String code;

    private Normal(
      final @Nonnull String code)
    {
      this.code = code;
    }

    public @Nonnull String getCode()
    {
      return this.code;
    }
  }

  public static enum Specular
  {
    SPECULAR_NONE(""),
    SPECULAR_CONSTANT("SC"),
    SPECULAR_MAPPED("SM");

    final @Nonnull String code;

    private Specular(
      final @Nonnull String code)
    {
      this.code = code;
    }

    public @Nonnull String getCode()
    {
      return this.code;
    }
  }

  static @Nonnull KMeshInstanceMaterialLabel label(
    final @Nonnull KMesh mesh,
    final @Nonnull KMaterial material)
    throws ConstraintError
  {
    return new KMeshInstanceMaterialLabel(
      KMeshInstanceMaterialLabel.labelAlpha(mesh, material),
      KMeshInstanceMaterialLabel.labelAlbedo(mesh, material),
      KMeshInstanceMaterialLabel.labelEmissive(mesh, material),
      KMeshInstanceMaterialLabel.labelEnvironment(mesh, material),
      KMeshInstanceMaterialLabel.labelNormal(mesh, material),
      KMeshInstanceMaterialLabel.labelSpecular(mesh, material));
  }

  static @Nonnull Albedo labelAlbedo(
    final @Nonnull KMesh mesh,
    final @Nonnull KMaterial material)
    throws ConstraintError
  {
    Constraints.constrainNotNull(mesh, "Mesh");
    Constraints.constrainNotNull(material, "Material");

    final ArrayBuffer a = mesh.getArrayBuffer();

    if (a.hasAttribute(KMeshAttributes.ATTRIBUTE_UV.getName())) {
      if (material.getDiffuse().getTexture().isSome()) {
        if (material.getDiffuse().getMix() == 0.0) {
          return Albedo.ALBEDO_COLOURED;
        }
        return Albedo.ALBEDO_TEXTURED;
      }
    }

    return Albedo.ALBEDO_COLOURED;
  }

  static @Nonnull Alpha labelAlpha(
    final @Nonnull KMesh mesh,
    final @Nonnull KMaterial material)
    throws ConstraintError
  {
    Constraints.constrainNotNull(mesh, "Mesh");
    Constraints.constrainNotNull(material, "Material");

    if (material.getAlpha().isTranslucent()) {
      return Alpha.ALPHA_TRANSLUCENT;
    }

    return Alpha.ALPHA_OPAQUE;
  }

  static @Nonnull Emissive labelEmissive(
    final @Nonnull KMesh mesh,
    final @Nonnull KMaterial material)
    throws ConstraintError
  {
    Constraints.constrainNotNull(mesh, "Mesh");
    Constraints.constrainNotNull(material, "Material");

    final ArrayBuffer a = mesh.getArrayBuffer();
    final KMaterialEmissive e = material.getEmissive();

    if (e.getEmission() == 0.0) {
      return Emissive.EMISSIVE_NONE;
    }
    if (e.getTexture().isSome()
      && a.hasAttribute(KMeshAttributes.ATTRIBUTE_UV.getName())) {
      return Emissive.EMISSIVE_MAPPED;
    }

    return Emissive.EMISSIVE_CONSTANT;
  }

  static @Nonnull Environment labelEnvironment(
    final @Nonnull KMesh mesh,
    final @Nonnull KMaterial material)
    throws ConstraintError
  {
    Constraints.constrainNotNull(mesh, "Mesh");
    Constraints.constrainNotNull(material, "Material");

    switch (KMeshInstanceMaterialLabel.labelNormal(mesh, material)) {
      case NORMAL_NONE:
        return Environment.ENVIRONMENT_NONE;
      case NORMAL_MAPPED:
      case NORMAL_VERTEX:
      {
        final KMaterialEnvironment e = material.getEnvironment();
        if (e.getTexture().isSome()) {
          if (e.getMix() > 0.0) {
            if (e.getReflectionMix() == 0.0) {
              return Environment.ENVIRONMENT_REFRACTIVE;
            }
            if (e.getReflectionMix() == 1.0) {
              return Environment.ENVIRONMENT_REFLECTIVE;
            }
            return Environment.ENVIRONMENT_REFLECTIVE_REFRACTIVE;
          }
        }

        return Environment.ENVIRONMENT_NONE;
      }
    }

    throw new UnreachableCodeException();
  }

  static @Nonnull Normal labelNormal(
    final @Nonnull KMesh mesh,
    final @Nonnull KMaterial material)
    throws ConstraintError
  {
    Constraints.constrainNotNull(mesh, "Mesh");
    Constraints.constrainNotNull(material, "Material");

    final ArrayBuffer a = mesh.getArrayBuffer();

    if (a.hasAttribute(KMeshAttributes.ATTRIBUTE_NORMAL.getName())) {
      if (a.hasAttribute(KMeshAttributes.ATTRIBUTE_UV.getName())) {
        if (a.hasAttribute(KMeshAttributes.ATTRIBUTE_TANGENT4.getName())) {
          if (material.getNormal().getTexture().isSome()) {
            return Normal.NORMAL_MAPPED;
          }
        }
      }
      return Normal.NORMAL_VERTEX;
    }

    return Normal.NORMAL_NONE;
  }

  static @Nonnull Specular labelSpecular(
    final @Nonnull KMesh mesh,
    final @Nonnull KMaterial material)
    throws ConstraintError
  {
    Constraints.constrainNotNull(mesh, "Mesh");
    Constraints.constrainNotNull(material, "Material");

    final ArrayBuffer a = mesh.getArrayBuffer();

    switch (KMeshInstanceMaterialLabel.labelNormal(mesh, material)) {
      case NORMAL_NONE:
        return Specular.SPECULAR_NONE;
      case NORMAL_MAPPED:
      case NORMAL_VERTEX:
      {
        final KMaterialSpecular s = material.getSpecular();
        if (s.getIntensity() == 0.0) {
          return Specular.SPECULAR_NONE;
        }
        if (s.getTexture().isSome()
          && a.hasAttribute(KMeshAttributes.ATTRIBUTE_UV.getName())) {
          return Specular.SPECULAR_MAPPED;
        }
        return Specular.SPECULAR_CONSTANT;
      }
    }

    throw new UnreachableCodeException();
  }

  private static @Nonnull String makeLabelCode(
    final @Nonnull Alpha a,
    final @Nonnull Albedo d,
    final @Nonnull Emissive m,
    final @Nonnull Environment e,
    final @Nonnull Normal n,
    final @Nonnull Specular s)
  {
    final StringBuilder b = new StringBuilder();
    b.append(a.code);
    b.append("_");
    b.append(d.code);
    if (m.code.isEmpty() == false) {
      b.append("_");
      b.append(m.code);
    }
    if (e.code.isEmpty() == false) {
      b.append("_");
      b.append(e.code);
    }
    if (n.code.isEmpty() == false) {
      b.append("_");
      b.append(n.code);
    }
    if (s.code.isEmpty() == false) {
      b.append("_");
      b.append(s.code);
    }
    return b.toString();
  }

  private final @Nonnull Alpha       alpha;
  private final @Nonnull Albedo      albedo;
  private final @Nonnull Emissive    emissive;
  private final @Nonnull Environment environment;
  private final @Nonnull Normal      normal;
  private final @Nonnull Specular    specular;
  private final @Nonnull String      code;

  KMeshInstanceMaterialLabel(
    final @Nonnull Alpha alpha,
    final @Nonnull Albedo albedo,
    final @Nonnull Emissive emissive,
    final @Nonnull Environment environment,
    final @Nonnull Normal normal,
    final @Nonnull Specular specular)
    throws ConstraintError
  {
    this.alpha = Constraints.constrainNotNull(alpha, "Alpha");
    this.albedo = Constraints.constrainNotNull(albedo, "Albedo");
    this.emissive = Constraints.constrainNotNull(emissive, "Emissive");
    this.environment =
      Constraints.constrainNotNull(environment, "Environment");
    this.normal = Constraints.constrainNotNull(normal, "Normal");
    this.specular = Constraints.constrainNotNull(specular, "Specular");

    this.code =
      KMeshInstanceMaterialLabel.makeLabelCode(
        alpha,
        albedo,
        emissive,
        environment,
        normal,
        specular);
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
    final KMeshInstanceMaterialLabel other = (KMeshInstanceMaterialLabel) obj;
    if (this.alpha != other.alpha) {
      return false;
    }
    if (this.albedo != other.albedo) {
      return false;
    }
    if (this.emissive != other.emissive) {
      return false;
    }
    if (this.environment != other.environment) {
      return false;
    }
    if (this.normal != other.normal) {
      return false;
    }
    if (this.specular != other.specular) {
      return false;
    }
    return true;
  }

  public @Nonnull String getCode()
  {
    return this.code;
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
}
