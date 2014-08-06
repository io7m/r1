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

package com.io7m.renderer.shaders.deferred;

import java.util.ArrayList;
import java.util.List;

import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.Texture2DStaticType;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jcanephora.TextureFormat;
import com.io7m.jcanephora.TextureWrapS;
import com.io7m.jcanephora.TextureWrapT;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jranges.RangeInclusiveL;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.junreachable.UnimplementedCodeException;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.kernel.types.KLightDirectional;
import com.io7m.renderer.kernel.types.KLightProjectiveWithShadowBasic;
import com.io7m.renderer.kernel.types.KLightProjectiveWithShadowBasicBuilderType;
import com.io7m.renderer.kernel.types.KLightProjectiveWithShadowVariance;
import com.io7m.renderer.kernel.types.KLightProjectiveWithShadowVarianceBuilderType;
import com.io7m.renderer.kernel.types.KLightProjectiveWithoutShadow;
import com.io7m.renderer.kernel.types.KLightProjectiveWithoutShadowBuilderType;
import com.io7m.renderer.kernel.types.KLightSphereWithoutShadow;
import com.io7m.renderer.kernel.types.KLightType;
import com.io7m.renderer.kernel.types.KProjectionFrustum;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RSpaceRGBType;
import com.io7m.renderer.types.RSpaceWorldType;
import com.io7m.renderer.types.RVectorI3F;

@EqualityReference public final class RKDLightCases
{
  private static List<KLightType> makeLightCases()
  {
    try {
      final Texture2DStaticType t = new Texture2DStaticType() {
        @Override public long resourceGetSizeBytes()
        {
          // TODO Auto-generated method stub
          throw new UnimplementedCodeException();
        }

        @Override public boolean resourceIsDeleted()
        {
          // TODO Auto-generated method stub
          throw new UnimplementedCodeException();
        }

        @Override public int getGLName()
        {
          // TODO Auto-generated method stub
          throw new UnimplementedCodeException();
        }

        @Override public TextureWrapT textureGetWrapT()
        {
          // TODO Auto-generated method stub
          throw new UnimplementedCodeException();
        }

        @Override public TextureWrapS textureGetWrapS()
        {
          // TODO Auto-generated method stub
          throw new UnimplementedCodeException();
        }

        @Override public int textureGetWidth()
        {
          // TODO Auto-generated method stub
          throw new UnimplementedCodeException();
        }

        @Override public RangeInclusiveL textureGetRangeY()
        {
          // TODO Auto-generated method stub
          throw new UnimplementedCodeException();
        }

        @Override public RangeInclusiveL textureGetRangeX()
        {
          // TODO Auto-generated method stub
          throw new UnimplementedCodeException();
        }

        @Override public String textureGetName()
        {
          // TODO Auto-generated method stub
          throw new UnimplementedCodeException();
        }

        @Override public
          TextureFilterMinification
          textureGetMinificationFilter()
        {
          // TODO Auto-generated method stub
          throw new UnimplementedCodeException();
        }

        @Override public
          TextureFilterMagnification
          textureGetMagnificationFilter()
        {
          // TODO Auto-generated method stub
          throw new UnimplementedCodeException();
        }

        @Override public int textureGetHeight()
        {
          // TODO Auto-generated method stub
          throw new UnimplementedCodeException();
        }

        @Override public TextureFormat textureGetFormat()
        {
          // TODO Auto-generated method stub
          throw new UnimplementedCodeException();
        }

        @Override public AreaInclusive textureGetArea()
        {
          // TODO Auto-generated method stub
          throw new UnimplementedCodeException();
        }
      };

      final ArrayList<KLightType> cases = new ArrayList<KLightType>();
      final RVectorI3F<RSpaceWorldType> v =
        new RVectorI3F<RSpaceWorldType>(0.0f, 0.0f, 0.0f);
      final RVectorI3F<RSpaceRGBType> c =
        new RVectorI3F<RSpaceRGBType>(1.0f, 1.0f, 1.0f);

      final KProjectionFrustum projection =
        KProjectionFrustum.newProjection(
          new MatrixM4x4F(),
          -1.0f,
          1.0f,
          -1.0f,
          1.0f,
          1.0f,
          100.0f);

      {
        final KLightType l = KLightDirectional.newLight(v, c, 1.0f);
        cases.add(l);
      }

      {
        final KLightType l =
          KLightSphereWithoutShadow.newLight(c, 1.0f, v, 1.0f, 1.0f);
        cases.add(l);
      }

      {
        final KLightProjectiveWithoutShadowBuilderType b =
          KLightProjectiveWithoutShadow.newBuilder(t, projection);
        cases.add(b.build());
      }

      {
        final KLightProjectiveWithShadowBasicBuilderType b =
          KLightProjectiveWithShadowBasic.newBuilder(t, projection);
        cases.add(b.build());
      }

      {
        final KLightProjectiveWithShadowVarianceBuilderType b =
          KLightProjectiveWithShadowVariance.newBuilder(t, projection);
        cases.add(b.build());
      }

      return cases;
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private final List<KLightType> light_cases;

  public RKDLightCases()
  {
    this.light_cases = RKDLightCases.makeLightCases();
  }

  public List<KLightType> getCases()
  {
    return this.light_cases;
  }
}
