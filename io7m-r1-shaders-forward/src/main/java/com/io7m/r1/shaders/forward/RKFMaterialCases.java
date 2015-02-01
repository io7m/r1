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
import com.io7m.jtensors.parameterized.PVectorI4F;
import com.io7m.junreachable.UnimplementedCodeException;
import com.io7m.r1.kernel.types.KMaterialAlphaConstant;
import com.io7m.r1.kernel.types.KMaterialAlphaOneMinusDot;
import com.io7m.r1.kernel.types.KMaterialAlphaType;
import com.io7m.r1.kernel.types.KMaterialDefaultsUsableType;
import com.io7m.r1.kernel.types.KMaterialDepthAlpha;
import com.io7m.r1.kernel.types.KMaterialDepthConstant;
import com.io7m.r1.kernel.types.KMaterialDepthType;
import com.io7m.r1.kernel.types.KMaterialEnvironmentNone;
import com.io7m.r1.kernel.types.KMaterialEnvironmentReflection;
import com.io7m.r1.kernel.types.KMaterialEnvironmentReflectionMapped;
import com.io7m.r1.kernel.types.KMaterialEnvironmentType;
import com.io7m.r1.kernel.types.KMaterialRefractiveMaskedDeltaTextured;
import com.io7m.r1.kernel.types.KMaterialRefractiveMaskedNormals;
import com.io7m.r1.kernel.types.KMaterialRefractiveType;
import com.io7m.r1.kernel.types.KMaterialRefractiveUnmaskedDeltaTextured;
import com.io7m.r1.kernel.types.KMaterialRefractiveUnmaskedNormals;
import com.io7m.r1.kernel.types.KMaterialTranslucentRefractive;
import com.io7m.r1.kernel.types.KMaterialTranslucentRefractiveBuilderType;
import com.io7m.r1.kernel.types.KMaterialTranslucentRegular;
import com.io7m.r1.kernel.types.KMaterialTranslucentRegularBuilderType;
import com.io7m.r1.kernel.types.KMaterialTranslucentSpecularOnly;
import com.io7m.r1.kernel.types.KMaterialTranslucentSpecularOnlyBuilderType;
import com.io7m.r1.spaces.RSpaceRGBAType;

@EqualityReference public final class RKFMaterialCases
{
  private static List<KMaterialAlphaType> makeAlphaCases()
  {
    final List<KMaterialAlphaType> cases =
      new ArrayList<KMaterialAlphaType>();
    cases.add(KMaterialAlphaConstant.constant(1.0f));
    cases.add(KMaterialAlphaOneMinusDot.oneMinusDot(1.0f));
    return cases;
  }

  private static
    List<KMaterialTranslucentRegular>
    makeCasesLitTranslucentRegular(
      final List<KMaterialAlphaType> cases_alpha,
      final List<KMaterialEnvironmentType> cases_env,
      final KMaterialDefaultsUsableType defaults)
  {
    final List<KMaterialTranslucentRegular> cases =
      new ArrayList<KMaterialTranslucentRegular>();
    final KMaterialTranslucentRegularBuilderType b =
      KMaterialTranslucentRegular.newBuilder(defaults);

    for (final KMaterialAlphaType al : cases_alpha) {
      assert al != null;
      for (final KMaterialEnvironmentType ev : cases_env) {
        assert ev != null;
        b.setAlpha(al);
        b.setEnvironment(ev);
        cases.add(b.build());
      }
    }

    return cases;
  }

  private static
    List<KMaterialTranslucentSpecularOnly>
    makeCasesLitTranslucentSpecularOnly(
      final List<KMaterialAlphaType> cases_alpha,
      final KMaterialDefaultsUsableType defaults)
  {
    final List<KMaterialTranslucentSpecularOnly> cases =
      new ArrayList<KMaterialTranslucentSpecularOnly>();
    final KMaterialTranslucentSpecularOnlyBuilderType mb =
      KMaterialTranslucentSpecularOnly.newBuilder(defaults);

    for (final KMaterialAlphaType al : cases_alpha) {
      assert al != null;

      mb.setAlpha(al);
      cases.add(mb.build());
    }

    return cases;
  }

  private static
    List<KMaterialTranslucentRefractive>
    makeCasesUnlitTranslucentRefractive(
      final List<KMaterialRefractiveType> cases_refractive,
      final KMaterialDefaultsUsableType defaults)
  {
    final List<KMaterialTranslucentRefractive> cases =
      new ArrayList<KMaterialTranslucentRefractive>();

    final KMaterialTranslucentRefractiveBuilderType b =
      KMaterialTranslucentRefractive.newBuilder(defaults);

    for (final KMaterialRefractiveType r : cases_refractive) {
      assert r != null;
      b.setRefractive(r);
      cases.add(b.build());
    }

    return cases;
  }

  private static
    List<KMaterialTranslucentRegular>
    makeCasesUnlitTranslucentRegular(
      final List<KMaterialAlphaType> cases_alpha,
      final List<KMaterialEnvironmentType> cases_env,
      final KMaterialDefaultsUsableType defaults)
  {
    final List<KMaterialTranslucentRegular> cases =
      new ArrayList<KMaterialTranslucentRegular>();

    final KMaterialTranslucentRegularBuilderType b =
      KMaterialTranslucentRegular.newBuilder(defaults);

    for (final KMaterialAlphaType al : cases_alpha) {
      assert al != null;
      for (final KMaterialEnvironmentType ev : cases_env) {
        assert ev != null;
        b.setAlpha(al);
        b.setEnvironment(ev);
        final KMaterialTranslucentRegular m = b.build();
        cases.add(m);
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

  private static List<KMaterialRefractiveType> makeRefractiveCases(
    final Texture2DStaticType t)
  {
    final List<KMaterialRefractiveType> cases =
      new ArrayList<KMaterialRefractiveType>();
    final PVectorI4F<RSpaceRGBAType> color =
      new PVectorI4F<RSpaceRGBAType>(1.0f, 1.0f, 1.0f, 1.0f);

    cases.add(KMaterialRefractiveMaskedNormals.create(1.0f, color));
    cases.add(KMaterialRefractiveMaskedDeltaTextured.create(1.0f, t, color));
    cases.add(KMaterialRefractiveUnmaskedNormals.create(1.0f, color));
    cases
      .add(KMaterialRefractiveUnmaskedDeltaTextured.create(1.0f, t, color));
    return cases;
  }

  private final List<KMaterialAlphaType>               cases_alpha;
  private final List<KMaterialDepthType>               cases_depth;
  private final List<KMaterialEnvironmentType>         cases_env;
  private final List<KMaterialTranslucentRegular>      cases_lit_translucent_regular;
  private final List<KMaterialTranslucentSpecularOnly> cases_lit_translucent_specular_only;
  private final List<KMaterialRefractiveType>          cases_refractive;
  private final List<KMaterialTranslucentRefractive>   cases_unlit_translucent_refractive;
  private final List<KMaterialTranslucentRegular>      cases_unlit_translucent_regular;

  public RKFMaterialCases(
    final Texture2DStaticType t,
    final TextureCubeStaticType tc)
  {
    this.cases_alpha = RKFMaterialCases.makeAlphaCases();
    this.cases_depth = RKFMaterialCases.makeDepthCases();
    this.cases_env = RKFMaterialCases.makeEnvironmentCases(tc);
    this.cases_refractive = RKFMaterialCases.makeRefractiveCases(t);

    final KMaterialDefaultsUsableType defaults =
      new KMaterialDefaultsUsableType() {
        @Override public Texture2DStaticUsableType getFlatNormalTexture()
        {
          return t;
        }

        @Override public Texture2DStaticUsableType getEmptySpecularTexture()
        {
          return t;
        }

        @Override public Texture2DStaticUsableType getEmptyEmissiveTexture()
        {
          return t;
        }

        @Override public Texture2DStaticUsableType getEmptyAlbedoTexture()
        {
          return t;
        }
      };

    this.cases_lit_translucent_regular =
      RKFMaterialCases.makeCasesLitTranslucentRegular(
        this.cases_alpha,
        this.cases_env,
        defaults);

    this.cases_unlit_translucent_regular =
      RKFMaterialCases.makeCasesUnlitTranslucentRegular(
        this.cases_alpha,
        this.cases_env,
        defaults);

    this.cases_unlit_translucent_refractive =
      RKFMaterialCases.makeCasesUnlitTranslucentRefractive(
        this.cases_refractive,
        defaults);

    this.cases_lit_translucent_specular_only =
      RKFMaterialCases.makeCasesLitTranslucentSpecularOnly(
        this.cases_alpha,
        defaults);
  }

  public RKFMaterialCases()
  {
    this(new Texture2DStaticType() {
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
    }, new TextureCubeStaticType() {
      @Override public boolean resourceIsDeleted()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public long resourceGetSizeBytes()
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

      @Override public TextureWrapR textureGetWrapR()
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

  public List<KMaterialTranslucentRegular> getCasesLitTranslucentRegular()
  {
    return this.cases_lit_translucent_regular;
  }

  public
    List<KMaterialTranslucentSpecularOnly>
    getCasesLitTranslucentSpecularOnly()
  {
    return this.cases_lit_translucent_specular_only;
  }

  public List<KMaterialRefractiveType> getCasesRefractive()
  {
    return this.cases_refractive;
  }

  public
    List<KMaterialTranslucentRefractive>
    getCasesUnlitTranslucentRefractive()
  {
    return this.cases_unlit_translucent_refractive;
  }

  public List<KMaterialTranslucentRegular> getCasesUnlitTranslucentRegular()
  {
    return this.cases_unlit_translucent_regular;
  }
}
