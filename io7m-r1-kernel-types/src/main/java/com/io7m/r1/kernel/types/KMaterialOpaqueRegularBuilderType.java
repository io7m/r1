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

package com.io7m.r1.kernel.types;

import com.io7m.r1.types.RMatrixI3x3F;
import com.io7m.r1.types.RTransformTextureType;

/**
 * The type of mutable builders for {@link KMaterialOpaqueRegular}.
 */

public interface KMaterialOpaqueRegularBuilderType extends
  KMaterialOpaqueBuilderType<KMaterialOpaqueRegular>
{
  /**
   * Set the albedo of the material.
   * 
   * @param albedo
   *          The albedo properties.
   */

  void setAlbedo(
    final KMaterialAlbedoType albedo);

  /**
   * Set the depth properties of the material.
   * 
   * @param depth
   *          The depth properties.
   */

  void setDepth(
    final KMaterialDepthType depth);

  /**
   * Set the emissive properties of the material.
   * 
   * @param emissive
   *          The emissive properties.
   */

  void setEmissive(
    final KMaterialEmissiveType emissive);

  /**
   * Set the environment properties of the material.
   * 
   * @param environment
   *          The environment properties.
   */

  void setEnvironment(
    final KMaterialEnvironmentType environment);

  /**
   * Set the normal properties of the material.
   * 
   * @param normal
   *          The normal properties.
   */

  void setNormal(
    final KMaterialNormalType normal);

  /**
   * Set the specular properties of the material.
   * 
   * @param specular
   *          The specular properties.
   */

  void setSpecular(
    final KMaterialSpecularType specular);

  /**
   * Set the UV matrix of the material.
   * 
   * @param uv_matrix
   *          The UV matrix.
   */

  void setUVMatrix(
    final RMatrixI3x3F<RTransformTextureType> uv_matrix);
}