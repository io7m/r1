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

package com.io7m.r1.shaders.forward;

import java.util.ArrayList;
import java.util.List;

import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.Texture2DStaticType;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jcanephora.TextureFormat;
import com.io7m.jcanephora.TextureUsableVisitorType;
import com.io7m.jcanephora.TextureWrapS;
import com.io7m.jcanephora.TextureWrapT;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jranges.RangeInclusiveL;
import com.io7m.jtensors.parameterized.PMatrixM4x4F;
import com.io7m.jtensors.parameterized.PVectorI3F;
import com.io7m.junreachable.UnimplementedCodeException;
import com.io7m.r1.kernel.types.KLightDirectional;
import com.io7m.r1.kernel.types.KLightSphereWithoutShadow;
import com.io7m.r1.kernel.types.KLightTranslucentType;
import com.io7m.r1.kernel.types.KProjectionFrustum;
import com.io7m.r1.spaces.RSpaceClipType;
import com.io7m.r1.spaces.RSpaceEyeType;
import com.io7m.r1.spaces.RSpaceRGBType;
import com.io7m.r1.spaces.RSpaceWorldType;

@EqualityReference public final class RKFLightCases
{
  private static List<KLightTranslucentType> makeLightCases()
  {
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

      @Override public <A, E extends Exception> A textureUsableAccept(
        final TextureUsableVisitorType<A, E> v)
        throws E
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }
    };

    final ArrayList<KLightTranslucentType> cases =
      new ArrayList<KLightTranslucentType>();
    final PVectorI3F<RSpaceWorldType> v =
      new PVectorI3F<RSpaceWorldType>(0.0f, 0.0f, 0.0f);
    final PVectorI3F<RSpaceRGBType> c =
      new PVectorI3F<RSpaceRGBType>(1.0f, 1.0f, 1.0f);

    final KProjectionFrustum projection =
      KProjectionFrustum.newProjection(
        new PMatrixM4x4F<RSpaceEyeType, RSpaceClipType>(),
        -1.0f,
        1.0f,
        -1.0f,
        1.0f,
        1.0f,
        100.0f);

    {
      final KLightTranslucentType l = KLightDirectional.newLight(v, c, 1.0f);
      cases.add(l);
    }

    {
      final KLightTranslucentType l =
        KLightSphereWithoutShadow.newLight(c, 1.0f, v, 1.0f, 1.0f);
      cases.add(l);
    }

    return cases;
  }

  private final List<KLightTranslucentType> light_cases;

  public RKFLightCases()
  {
    this.light_cases = RKFLightCases.makeLightCases();
  }

  public List<KLightTranslucentType> getCases()
  {
    return this.light_cases;
  }
}
