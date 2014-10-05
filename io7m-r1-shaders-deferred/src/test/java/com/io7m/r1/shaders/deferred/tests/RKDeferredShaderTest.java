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

package com.io7m.r1.shaders.deferred.tests;

import org.junit.Test;

import com.io7m.r1.kernel.types.KMaterialAlbedoUntextured;
import com.io7m.r1.kernel.types.KMaterialDepthConstant;
import com.io7m.r1.kernel.types.KMaterialEmissiveNone;
import com.io7m.r1.kernel.types.KMaterialEnvironmentNone;
import com.io7m.r1.kernel.types.KMaterialNormalVertex;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegular;
import com.io7m.r1.kernel.types.KMaterialSpecularNone;
import com.io7m.r1.shaders.deferred.RKDeferredShader;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RMatrixI3x3F;
import com.io7m.r1.types.RTransformTextureType;

@SuppressWarnings("static-method") public final class RKDeferredShaderTest
{
  @Test public void testShader()
    throws RException
  {
    final StringBuilder b = new StringBuilder();
    final RMatrixI3x3F<RTransformTextureType> id = RMatrixI3x3F.identity();
    final KMaterialOpaqueRegular m =
      KMaterialOpaqueRegular.newMaterial(
        id,
        KMaterialAlbedoUntextured.white(),
        KMaterialDepthConstant.constant(),
        KMaterialEmissiveNone.none(),
        KMaterialEnvironmentNone.none(),
        KMaterialNormalVertex.vertex(),
        KMaterialSpecularNone.none());

    RKDeferredShader.fragmentShaderGeometryRegular(
      b,
      m.materialRegularGetAlbedo(),
      m.materialOpaqueGetDepth(),
      m.materialGetEmissive(),
      m.materialRegularGetEnvironment(),
      m.materialGetNormal(),
      m.materialRegularGetSpecular(),
      m.materialRequiresUVCoordinates());

    System.out.println(b.toString());
  }
}
