/*
 * Copyright © 2014 <code@io7m.com> http://io7m.com
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

package com.io7m.renderer.kernel.types;

import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jnull.NullCheck;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RExceptionMaterialMissingAlbedoTexture;
import com.io7m.renderer.types.RExceptionMaterialMissingSpecularTexture;

/**
 * Functions to verify that materials are correctly constructed.
 */

@EqualityReference public final class KMaterialVerification
{
  /**
   * If alpha-to-depth rendering is specified, check that an albedo texture is
   * provided.
   */

  private static void checkDepthMaterial(
    final KMaterialAlbedoType in_albedo,
    final KMaterialDepthType in_depth)
    throws RException
  {
    if (KMaterialVerification.isValidDepthMaterial(in_albedo, in_depth) == false) {
      throw new RExceptionMaterialMissingAlbedoTexture(
        "An albedo texture is required for alpha-to-depth materials");
    }
  }

  /**
   * If specular-mapped environment mapping is specified, check that a
   * specular map is provided.
   */

  private static void checkEnvironmentSpecular(
    final KMaterialEnvironmentType in_environment,
    final KMaterialSpecularType in_specular)
    throws RException
  {
    if (KMaterialVerification.isValidEnvironmentSpecular(
      in_environment,
      in_specular) == false) {
      throw new RExceptionMaterialMissingSpecularTexture(
        "A specular texture is required for specular-mapped environment mapping");
    }
  }

  /**
   * If alpha-to-depth rendering is specified, check that an albedo texture is
   * provided.
   * 
   * @param in_albedo
   *          The albedo.
   * @param in_depth
   *          The depth type.
   * @return <code>true</code> if valid.
   */

  public static boolean isValidDepthMaterial(
    final KMaterialAlbedoType in_albedo,
    final KMaterialDepthType in_depth)
  {
    if (in_depth instanceof KMaterialDepthAlpha) {
      return in_albedo instanceof KMaterialAlbedoTextured;
    }
    return true;
  }

  /**
   * If specular-mapped environment is specified, check that a specular
   * texture is provided.
   * 
   * @param in_environment
   *          The environment.
   * @param in_specular
   *          The specular type.
   * @return <code>true</code> if valid.
   */

  public static boolean isValidEnvironmentSpecular(
    final KMaterialEnvironmentType in_environment,
    final KMaterialSpecularType in_specular)
  {
    if (in_environment instanceof KMaterialEnvironmentReflectionMapped) {
      return in_specular instanceof KMaterialSpecularMapped;
    }
    return true;
  }

  /**
   * Check whether or not the given combination of material properties is
   * valid.
   * 
   * @param in_depth
   *          The material's depth rendering properties
   * @param in_normal
   *          The material's normal mapping properties
   * @param in_albedo
   *          The material's albedo properties
   * @param in_emissive
   *          The material's emissive properties
   * @param in_environment
   *          The material's environment mapping properties
   * @param in_specular
   *          The material's specularity properties
   * 
   * @throws RExceptionMaterialMissingAlbedoTexture
   *           If one or more material properties require an albedo texture,
   *           but one was not provided.
   * @throws RExceptionMaterialMissingSpecularTexture
   *           If one or more material properties require a specular texture,
   *           but one was not provided.
   * @throws RException
   *           If an error occurs.
   */

  public static void materialVerifyOpaqueRegular(
    final KMaterialAlbedoType in_albedo,
    final KMaterialDepthType in_depth,
    final KMaterialEmissiveType in_emissive,
    final KMaterialEnvironmentType in_environment,
    final KMaterialNormalType in_normal,
    final KMaterialSpecularType in_specular)
    throws RException,
      RExceptionMaterialMissingAlbedoTexture,
      RExceptionMaterialMissingSpecularTexture
  {
    NullCheck.notNull(in_albedo, "Albedo");
    NullCheck.notNull(in_emissive, "Emissive");
    NullCheck.notNull(in_environment, "Environment");
    NullCheck.notNull(in_depth, "Depth");
    NullCheck.notNull(in_normal, "Normal");
    NullCheck.notNull(in_specular, "Specular");

    KMaterialVerification.checkDepthMaterial(in_albedo, in_depth);
    KMaterialVerification.checkEnvironmentSpecular(
      in_environment,
      in_specular);
  }

  /**
   * Check whether or not the given combination of material properties is
   * valid.
   * 
   * @param in_normal
   *          The material's normal mapping properties
   * @param in_refractive
   *          The material's refractive properties
   */

  public static void materialVerifyTranslucentRefractive(
    final KMaterialNormalType in_normal,
    final KMaterialRefractiveType in_refractive)
  {
    NullCheck.notNull(in_normal, "Normal");
    NullCheck.notNull(in_refractive, "Refractive");
  }

  /**
   * Check whether or not the given combination of material properties is
   * valid.
   * 
   * @param in_alpha
   *          The material's alpha properties
   * @param in_normal
   *          The material's normal mapping properties
   * @param in_albedo
   *          The material's albedo properties
   * @param in_emissive
   *          The material's emissive properties
   * @param in_environment
   *          The material's environment mapping properties
   * @param in_specular
   *          The material's specularity properties
   * 
   * @throws RExceptionMaterialMissingSpecularTexture
   *           If one or more material properties require a specular texture,
   *           but one was not provided.
   * @throws RException
   *           If an error occurs.
   */

  public static void materialVerifyTranslucentRegular(
    final KMaterialAlbedoType in_albedo,
    final KMaterialAlphaType in_alpha,
    final KMaterialEmissiveType in_emissive,
    final KMaterialEnvironmentType in_environment,
    final KMaterialNormalType in_normal,
    final KMaterialSpecularType in_specular)
    throws RException,
      RExceptionMaterialMissingSpecularTexture
  {
    NullCheck.notNull(in_albedo, "Albedo");
    NullCheck.notNull(in_alpha, "Alpha");
    NullCheck.notNull(in_emissive, "Emissive");
    NullCheck.notNull(in_environment, "Environment");
    NullCheck.notNull(in_normal, "Normal");
    NullCheck.notNull(in_specular, "Specular");

    KMaterialVerification.checkEnvironmentSpecular(
      in_environment,
      in_specular);
  }

  /**
   * Check whether or not the given combination of material properties is
   * valid.
   * 
   * @param in_alpha
   *          The material's alpha properties
   * @param in_normal
   *          The material's normal mapping properties
   * @param in_specular
   *          The material's specular properties
   */

  public static void materialVerifyTranslucentSpecularOnly(
    final KMaterialAlphaType in_alpha,
    final KMaterialNormalType in_normal,
    final KMaterialSpecularType in_specular)
  {
    NullCheck.notNull(in_alpha, "Alpha");
    NullCheck.notNull(in_normal, "Normal");
    NullCheck.notNull(in_specular, "Specular");
  }

  private KMaterialVerification()
  {
    throw new UnreachableCodeException();
  }
}
