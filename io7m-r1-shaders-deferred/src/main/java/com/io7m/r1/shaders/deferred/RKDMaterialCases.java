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

package com.io7m.r1.shaders.deferred;

import java.util.ArrayList;
import java.util.List;

import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.Texture2DStaticType;
import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jcanephora.TextureCubeStaticType;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jcanephora.TextureFormat;
import com.io7m.jcanephora.TextureUsableVisitorType;
import com.io7m.jcanephora.TextureWrapR;
import com.io7m.jcanephora.TextureWrapS;
import com.io7m.jcanephora.TextureWrapT;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jranges.RangeInclusiveL;
import com.io7m.junreachable.UnimplementedCodeException;
import com.io7m.r1.kernel.types.KMaterialDefaultsUsableType;
import com.io7m.r1.kernel.types.KMaterialDepthAlpha;
import com.io7m.r1.kernel.types.KMaterialDepthConstant;
import com.io7m.r1.kernel.types.KMaterialDepthType;
import com.io7m.r1.kernel.types.KMaterialEnvironmentNone;
import com.io7m.r1.kernel.types.KMaterialEnvironmentReflection;
import com.io7m.r1.kernel.types.KMaterialEnvironmentReflectionMapped;
import com.io7m.r1.kernel.types.KMaterialEnvironmentType;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegular;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegularBuilderType;

@EqualityReference public final class RKDMaterialCases
{
  private static List<KMaterialOpaqueRegular> makeCasesGeometryOpaqueRegular(
    final List<KMaterialDepthType> cases_depth,
    final List<KMaterialEnvironmentType> cases_env,
    final KMaterialDefaultsUsableType defaults)
  {
    final List<KMaterialOpaqueRegular> cases =
      new ArrayList<KMaterialOpaqueRegular>();

    for (final KMaterialDepthType d : cases_depth) {
      assert d != null;
      for (final KMaterialEnvironmentType ev : cases_env) {
        assert ev != null;

        final KMaterialOpaqueRegularBuilderType b =
          KMaterialOpaqueRegular.newBuilder(defaults);
        b.setDepthType(d);
        b.setEnvironment(ev);
        cases.add(b.build());
      }
    }

    return cases;
  }

  private static List<KMaterialDepthType> makeDepthCases()
  {
    final List<KMaterialDepthType> cases =
      new ArrayList<KMaterialDepthType>();
    cases.add(KMaterialDepthAlpha.alpha(1.0f));
    cases.add(KMaterialDepthConstant.constant());
    return cases;
  }

  private static List<KMaterialEnvironmentType> makeEnvironmentCases(
    final TextureCubeStaticType t)
  {
    final List<KMaterialEnvironmentType> cases =
      new ArrayList<KMaterialEnvironmentType>();
    cases.add(KMaterialEnvironmentNone.none());
    cases.add(KMaterialEnvironmentReflection.reflection(1.0f, t));
    cases.add(KMaterialEnvironmentReflectionMapped.reflectionMapped(1.0f, t));
    return cases;
  }

  private final List<KMaterialDepthType>       cases_depth;
  private final List<KMaterialEnvironmentType> cases_env;
  private final List<KMaterialOpaqueRegular>   cases_geom_opaque_regular;

  public RKDMaterialCases()
  {
    this(new Texture2DStaticType() {
      @Override public int getGLName()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

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

      @Override public AreaInclusive textureGetArea()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public TextureFormat textureGetFormat()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public int textureGetHeight()
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

      @Override public
        TextureFilterMinification
        textureGetMinificationFilter()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public String textureGetName()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public RangeInclusiveL textureGetRangeX()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public RangeInclusiveL textureGetRangeY()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public int textureGetWidth()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public TextureWrapS textureGetWrapS()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public TextureWrapT textureGetWrapT()
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
    }, new TextureCubeStaticType() {

      @Override public int getGLName()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

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

      @Override public AreaInclusive textureGetArea()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public TextureFormat textureGetFormat()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public int textureGetHeight()
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

      @Override public
        TextureFilterMinification
        textureGetMinificationFilter()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public String textureGetName()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public RangeInclusiveL textureGetRangeX()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public RangeInclusiveL textureGetRangeY()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public int textureGetWidth()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public TextureWrapR textureGetWrapR()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public TextureWrapS textureGetWrapS()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public TextureWrapT textureGetWrapT()
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
    });
  }

  public RKDMaterialCases(
    final Texture2DStaticType t2d,
    final TextureCubeStaticType tc)
  {
    this.cases_depth = RKDMaterialCases.makeDepthCases();
    this.cases_env = RKDMaterialCases.makeEnvironmentCases(tc);

    this.cases_geom_opaque_regular =
      RKDMaterialCases.makeCasesGeometryOpaqueRegular(
        this.cases_depth,
        this.cases_env,
        new KMaterialDefaultsUsableType() {
          @Override public Texture2DStaticUsableType getEmptyAlbedoTexture()
          {
            return t2d;
          }

          @Override public
            Texture2DStaticUsableType
            getEmptyEmissiveTexture()
          {
            return t2d;
          }

          @Override public
            Texture2DStaticUsableType
            getEmptySpecularTexture()
          {
            return t2d;
          }

          @Override public Texture2DStaticUsableType getFlatNormalTexture()
          {
            return t2d;
          }
        });
  }

  public List<KMaterialDepthType> getCasesDepth()
  {
    return this.cases_depth;
  }

  public List<KMaterialEnvironmentType> getCasesEnvironment()
  {
    return this.cases_env;
  }

  public List<KMaterialOpaqueRegular> getCasesGeometryOpaqueRegular()
  {
    return this.cases_geom_opaque_regular;
  }
}
