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

package com.io7m.renderer.kernel;

import java.lang.reflect.Field;
import java.math.BigInteger;

import com.io7m.jcache.JCacheException;
import com.io7m.jcache.JCacheLoaderType;
import com.io7m.jcache.LRUCacheConfig;
import com.io7m.jcache.LRUCacheTrivial;
import com.io7m.jcache.LRUCacheType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jnull.NullCheck;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.kernel.types.KGraphicsCapabilities;
import com.io7m.renderer.kernel.types.KGraphicsCapabilitiesType;
import com.io7m.renderer.kernel.types.KMeshWithMaterialOpaqueType;
import com.io7m.renderer.kernel.types.KMeshWithMaterialRegularType;
import com.io7m.renderer.kernel.types.KMeshWithMaterialTranslucentRefractive;
import com.io7m.renderer.kernel.types.KMeshWithMaterialTranslucentRegular;
import com.io7m.renderer.kernel.types.KMeshWithMaterialTranslucentSpecularOnly;
import com.io7m.renderer.kernel.types.KInstanceType;
import com.io7m.renderer.kernel.types.KLightLabel;
import com.io7m.renderer.kernel.types.KLightType;
import com.io7m.renderer.kernel.types.KMaterialAlbedoLabel;
import com.io7m.renderer.kernel.types.KMaterialAlphaOpacityType;
import com.io7m.renderer.kernel.types.KMaterialDepthLabel;
import com.io7m.renderer.kernel.types.KMaterialEmissiveLabel;
import com.io7m.renderer.kernel.types.KMaterialEnvironmentLabel;
import com.io7m.renderer.kernel.types.KMaterialForwardOpaqueLitLabel;
import com.io7m.renderer.kernel.types.KMaterialForwardOpaqueUnlitLabel;
import com.io7m.renderer.kernel.types.KMaterialForwardRegularLabel;
import com.io7m.renderer.kernel.types.KMaterialForwardTranslucentRefractiveLabel;
import com.io7m.renderer.kernel.types.KMaterialForwardTranslucentRegularLitLabel;
import com.io7m.renderer.kernel.types.KMaterialForwardTranslucentRegularUnlitLabel;
import com.io7m.renderer.kernel.types.KMaterialForwardTranslucentSpecularOnlyLabel;
import com.io7m.renderer.kernel.types.KMaterialForwardTranslucentSpecularOnlyLitLabel;
import com.io7m.renderer.kernel.types.KMaterialNormalLabel;
import com.io7m.renderer.kernel.types.KMaterialRefractiveLabel;
import com.io7m.renderer.kernel.types.KMaterialSpecularLabel;

/**
 * The default implementation of a label decider.
 */

@SuppressWarnings("synthetic-access") @EqualityReference public final class KLabelDecider implements
  KForwardLabelDeciderType
{
  /**
   * Construct a new label decider assuming the given graphics capabilities
   * and the number of labels to cache per type.
   * 
   * @param capabilities
   *          The capabilities
   * @param cache_labels
   *          The number of labels to cache per type
   * @return A new label decider
   */

  public static KLabelDecider newDecider(
    final KGraphicsCapabilities capabilities,
    final BigInteger cache_labels)
  {
    final LRUCacheConfig config =
      LRUCacheConfig.empty().withMaximumCapacity(cache_labels);
    return new KLabelDecider(capabilities, config);
  }

  private final LRUCacheType<KMeshWithMaterialRegularType, KMaterialAlbedoLabel, UnreachableCodeException>                                     albedo_cache;
  private final LRUCacheType<KMeshWithMaterialTranslucentRegular, KMaterialAlphaOpacityType, UnreachableCodeException>                         alpha_regular_cache;
  private final LRUCacheType<KMeshWithMaterialTranslucentSpecularOnly, KMaterialAlphaOpacityType, UnreachableCodeException>                    alpha_specular_only_cache;
  private final LRUCacheConfig                                                                                                         cache_config;
  private final KGraphicsCapabilitiesType                                                                                              capabilities;
  private final LRUCacheType<KMeshWithMaterialOpaqueType, KMaterialDepthLabel, UnreachableCodeException>                                       depth_cache;
  private final LRUCacheType<KMeshWithMaterialRegularType, KMaterialEmissiveLabel, UnreachableCodeException>                                   emissive_cache;
  private final LRUCacheType<KMeshWithMaterialRegularType, KMaterialEnvironmentLabel, UnreachableCodeException>                                environment_cache;
  private final LRUCacheType<KMeshWithMaterialOpaqueType, KMaterialForwardOpaqueUnlitLabel, UnreachableCodeException>                          forward_opaque_unlit_cache;
  private final LRUCacheType<KMeshWithMaterialRegularType, KMaterialForwardRegularLabel, UnreachableCodeException>                             forward_regular_cache;
  private final LRUCacheType<KMeshWithMaterialTranslucentRefractive, KMaterialForwardTranslucentRefractiveLabel, UnreachableCodeException>     forward_translucent_refractive_cache;
  private final LRUCacheType<KMeshWithMaterialTranslucentRegular, KMaterialForwardTranslucentRegularUnlitLabel, UnreachableCodeException>      forward_translucent_regular_unlit_cache;
  private final LRUCacheType<KMeshWithMaterialTranslucentSpecularOnly, KMaterialForwardTranslucentSpecularOnlyLabel, UnreachableCodeException> forward_translucent_specular_only_cache;
  private final LRUCacheType<KLightType, KLightLabel, UnreachableCodeException>                                                        light_cache;
  private final LRUCacheType<KInstanceType, KMaterialNormalLabel, UnreachableCodeException>                                            normal_cache;
  private final LRUCacheType<KMeshWithMaterialTranslucentRefractive, KMaterialRefractiveLabel, UnreachableCodeException>                       refractive_cache;
  private final LRUCacheType<KMeshWithMaterialTranslucentSpecularOnly, KMaterialSpecularLabel, UnreachableCodeException>                       specular_only_cache;
  private final LRUCacheType<KMeshWithMaterialRegularType, KMaterialSpecularLabel, UnreachableCodeException>                                   specular_regular_cache;

  private KLabelDecider(
    final KGraphicsCapabilities in_capabilities,
    final LRUCacheConfig in_cache_config)
  {
    this.capabilities = NullCheck.notNull(in_capabilities, "Capabilities");
    this.cache_config = NullCheck.notNull(in_cache_config, "Cache config");

    this.alpha_regular_cache = this.newAlphaRegularCache();
    this.alpha_specular_only_cache = this.newAlphaSpecularOnlyCache();
    this.albedo_cache = this.newAlbedoCache();
    this.depth_cache = this.newDepthCache();
    this.emissive_cache = this.newEmissiveCache();
    this.refractive_cache = this.newRefractiveCache();
    this.environment_cache = this.newEnvironmentCache();
    this.normal_cache = this.newNormalCache();
    this.specular_regular_cache = this.newSpecularCache();
    this.forward_opaque_unlit_cache = this.newForwardOpaqueUnlitCache();
    this.forward_translucent_regular_unlit_cache =
      this.newTranslucentRegularUnlitCache();
    this.forward_translucent_refractive_cache =
      this.newTranslucentRefractiveCache();
    this.forward_regular_cache = this.newForwardRegularCache();
    this.light_cache = this.newLightCache(in_capabilities);
    this.specular_only_cache = this.newSpecularOnlyCache();
    this.forward_translucent_specular_only_cache =
      this.newTranslucentSpecularOnlyCache();
  }

  @Override public KMaterialAlbedoLabel getAlbedoLabelRegular(
    final KMeshWithMaterialRegularType instance)
  {
    try {
      return this.albedo_cache.cacheGetLU(instance);
    } catch (final JCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public KMaterialAlphaOpacityType getAlphaLabelTranslucentRegular(
    final KMeshWithMaterialTranslucentRegular instance)
  {
    try {
      return this.alpha_regular_cache.cacheGetLU(instance);
    } catch (final JCacheException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private KMaterialAlphaOpacityType getAlphaLabelTranslucentSpecularOnly(
    final KMeshWithMaterialTranslucentSpecularOnly instance)
  {
    try {
      return this.alpha_specular_only_cache.cacheGetLU(instance);
    } catch (final JCacheException e) {
      throw new UnreachableCodeException(e);
    }
  }

  @Override public KMaterialDepthLabel getDepthLabel(
    final KMeshWithMaterialOpaqueType instance)
  {
    try {
      return this.depth_cache.cacheGetLU(instance);
    } catch (final JCacheException e) {
      throw new UnreachableCodeException(e);
    }
  }

  @Override public KMaterialEmissiveLabel getEmissiveLabelRegular(
    final KMeshWithMaterialRegularType instance)
  {
    try {
      return this.emissive_cache.cacheGetLU(instance);
    } catch (final JCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public KMaterialEnvironmentLabel getEnvironmentLabelRegular(
    final KMeshWithMaterialRegularType instance)
  {
    try {
      return this.environment_cache.cacheGetLU(instance);
    } catch (final JCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public KMaterialForwardOpaqueLitLabel getForwardLabelOpaqueLit(
    final KLightType light,
    final KMeshWithMaterialOpaqueType instance)
  {
    return KMaterialForwardOpaqueLitLabel.newLabel(
      this.getLightLabel(light),
      this.getForwardLabelRegular(instance));
  }

  @Override public
    KMaterialForwardOpaqueUnlitLabel
    getForwardLabelOpaqueUnlit(
      final KMeshWithMaterialOpaqueType instance)
  {
    try {
      return this.forward_opaque_unlit_cache.cacheGetLU(instance);
    } catch (final JCacheException e) {
      throw new UnreachableCodeException(e);
    }
  }

  @Override public KMaterialForwardRegularLabel getForwardLabelRegular(
    final KMeshWithMaterialRegularType instance)
  {
    try {
      return this.forward_regular_cache.cacheGetLU(instance);
    } catch (final JCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }

  private
    KMaterialForwardTranslucentSpecularOnlyLabel
    getForwardLabelSpecularOnly(
      final KMeshWithMaterialTranslucentSpecularOnly instance)
  {
    try {
      return this.forward_translucent_specular_only_cache
        .cacheGetLU(instance);
    } catch (final JCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public
    KMaterialForwardTranslucentRefractiveLabel
    getForwardLabelTranslucentRefractive(
      final KMeshWithMaterialTranslucentRefractive instance)
  {
    try {
      return this.forward_translucent_refractive_cache.cacheGetLU(instance);
    } catch (final JCacheException e) {
      throw new UnreachableCodeException(e);
    }
  }

  @Override public
    KMaterialForwardTranslucentRegularLitLabel
    getForwardLabelTranslucentRegularLit(
      final KLightType light,
      final KMeshWithMaterialTranslucentRegular instance)
  {
    return KMaterialForwardTranslucentRegularLitLabel.newLabel(
      this.getLightLabel(light),
      this.getForwardLabelRegular(instance),
      this.getAlphaLabelTranslucentRegular(instance));
  }

  @Override public
    KMaterialForwardTranslucentRegularUnlitLabel
    getForwardLabelTranslucentRegularUnlit(
      final KMeshWithMaterialTranslucentRegular instance)
  {
    try {
      return this.forward_translucent_regular_unlit_cache
        .cacheGetLU(instance);
    } catch (final JCacheException e) {
      throw new UnreachableCodeException(e);
    }
  }

  @Override public
    KMaterialForwardTranslucentSpecularOnlyLitLabel
    getForwardLabelTranslucentSpecularOnlyLit(
      final KLightType light,
      final KMeshWithMaterialTranslucentSpecularOnly instance)
  {
    return KMaterialForwardTranslucentSpecularOnlyLitLabel.newLabel(
      this.getLightLabel(light),
      this.getForwardLabelSpecularOnly(instance),
      this.getAlphaLabelTranslucentSpecularOnly(instance));
  }

  @Override public KLightLabel getLightLabel(
    final KLightType light)
  {
    try {
      return this.light_cache.cacheGetLU(light);
    } catch (final JCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public KMaterialNormalLabel getNormalLabel(
    final KInstanceType instance)
  {
    try {
      return this.normal_cache.cacheGetLU(instance);
    } catch (final JCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public KMaterialRefractiveLabel getRefractiveLabel(
    final KMeshWithMaterialTranslucentRefractive instance)
  {
    try {
      return this.refractive_cache.cacheGetLU(instance);
    } catch (final JCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public BigInteger getSize()
  {
    BigInteger size = BigInteger.ZERO;

    /**
     * Abuse of reflection. Probably safer than trying to remember to include
     * all of the caches manually...
     */

    try {
      final Field[] fields = this.getClass().getDeclaredFields();
      for (final Field f : fields) {
        final Class<?> f_type = f.getType();
        if (f_type.equals(LRUCacheType.class)) {
          final LRUCacheType<?, ?, ?> c = (LRUCacheType<?, ?, ?>) f.get(this);
          size = size.add(c.cacheSize());
        }
      }
    } catch (final IllegalAccessException e) {
      throw new UnreachableCodeException(e);
    }

    assert size != null;
    return size;
  }

  @Override public KMaterialSpecularLabel getSpecularLabelRegular(
    final KMeshWithMaterialRegularType instance)
  {
    try {
      return this.specular_regular_cache.cacheGetLU(instance);
    } catch (final JCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }

  private
    LRUCacheTrivial<KMeshWithMaterialRegularType, KMaterialAlbedoLabel, UnreachableCodeException>
    newAlbedoCache()
  {
    return LRUCacheTrivial
      .newCache(
        new JCacheLoaderType<KMeshWithMaterialRegularType, KMaterialAlbedoLabel, UnreachableCodeException>() {
          @Override public void cacheValueClose(
            final KMaterialAlbedoLabel v)
          {
            // Nothing
          }

          @Override public KMaterialAlbedoLabel cacheValueLoad(
            final KMeshWithMaterialRegularType instance)
          {
            return KMaterialAlbedoLabel.fromMeshAndMaterialRegular(instance);
          }

          @Override public BigInteger cacheValueSizeOf(
            final KMaterialAlbedoLabel v)
          {
            final BigInteger r = BigInteger.ONE;
            assert r != null;
            return r;
          }
        },
        this.cache_config);
  }

  private
    LRUCacheTrivial<KMeshWithMaterialTranslucentRegular, KMaterialAlphaOpacityType, UnreachableCodeException>
    newAlphaRegularCache()
  {
    return LRUCacheTrivial
      .newCache(
        new JCacheLoaderType<KMeshWithMaterialTranslucentRegular, KMaterialAlphaOpacityType, UnreachableCodeException>() {
          @Override public void cacheValueClose(
            final KMaterialAlphaOpacityType v)
          {
            // Nothing
          }

          @Override public KMaterialAlphaOpacityType cacheValueLoad(
            final KMeshWithMaterialTranslucentRegular instance)
          {
            try {
              final KMaterialNormalLabel normal =
                KLabelDecider.this.normal_cache.cacheGetLU(instance);
              return KMaterialAlphaOpacityType.fromMeshAndMaterialRegular(
                normal,
                instance);
            } catch (final JCacheException x) {
              throw new UnreachableCodeException(x);
            }
          }

          @Override public BigInteger cacheValueSizeOf(
            final KMaterialAlphaOpacityType v)
          {
            final BigInteger r = BigInteger.ONE;
            assert r != null;
            return r;
          }
        }, this.cache_config);
  }

  private
    LRUCacheType<KMeshWithMaterialTranslucentSpecularOnly, KMaterialAlphaOpacityType, UnreachableCodeException>
    newAlphaSpecularOnlyCache()
  {
    return LRUCacheTrivial
      .newCache(
        new JCacheLoaderType<KMeshWithMaterialTranslucentSpecularOnly, KMaterialAlphaOpacityType, UnreachableCodeException>() {
          @Override public void cacheValueClose(
            final KMaterialAlphaOpacityType v)
          {
            // Nothing
          }

          @Override public KMaterialAlphaOpacityType cacheValueLoad(
            final KMeshWithMaterialTranslucentSpecularOnly instance)
          {
            try {
              final KMaterialNormalLabel normal =
                KLabelDecider.this.normal_cache.cacheGetLU(instance);
              return KMaterialAlphaOpacityType.fromMeshAndMaterialSpecularOnly(
                normal,
                instance);
            } catch (final JCacheException x) {
              throw new UnreachableCodeException(x);
            }
          }

          @Override public BigInteger cacheValueSizeOf(
            final KMaterialAlphaOpacityType v)
          {
            final BigInteger r = BigInteger.ONE;
            assert r != null;
            return r;
          }
        }, this.cache_config);
  }

  private
    LRUCacheTrivial<KMeshWithMaterialOpaqueType, KMaterialDepthLabel, UnreachableCodeException>
    newDepthCache()
  {
    return LRUCacheTrivial
      .newCache(
        new JCacheLoaderType<KMeshWithMaterialOpaqueType, KMaterialDepthLabel, UnreachableCodeException>() {
          @Override public void cacheValueClose(
            final KMaterialDepthLabel v)
          {
            // Nothing
          }

          @Override public KMaterialDepthLabel cacheValueLoad(
            final KMeshWithMaterialOpaqueType instance)
          {
            try {
              final KMaterialAlbedoLabel albedo =
                KLabelDecider.this.albedo_cache.cacheGetLU(instance);
              return KMaterialDepthLabel.fromMeshAndMaterialOpaque(albedo, instance);
            } catch (final JCacheException e) {
              throw new UnreachableCodeException(e);
            }
          }

          @Override public BigInteger cacheValueSizeOf(
            final KMaterialDepthLabel v)
          {
            final BigInteger r = BigInteger.ONE;
            assert r != null;
            return r;
          }
        },
        this.cache_config);
  }

  private
    LRUCacheTrivial<KMeshWithMaterialRegularType, KMaterialEmissiveLabel, UnreachableCodeException>
    newEmissiveCache()
  {
    return LRUCacheTrivial
      .newCache(
        new JCacheLoaderType<KMeshWithMaterialRegularType, KMaterialEmissiveLabel, UnreachableCodeException>() {
          @Override public void cacheValueClose(
            final KMaterialEmissiveLabel v)
          {
            // Nothing
          }

          @Override public KMaterialEmissiveLabel cacheValueLoad(
            final KMeshWithMaterialRegularType instance)
          {
            return KMaterialEmissiveLabel.fromInstanceRegular(instance);
          }

          @Override public BigInteger cacheValueSizeOf(
            final KMaterialEmissiveLabel v)
          {
            final BigInteger r = BigInteger.ONE;
            assert r != null;
            return r;
          }
        },
        this.cache_config);
  }

  private
    LRUCacheTrivial<KMeshWithMaterialRegularType, KMaterialEnvironmentLabel, UnreachableCodeException>
    newEnvironmentCache()
  {
    return LRUCacheTrivial
      .newCache(
        new JCacheLoaderType<KMeshWithMaterialRegularType, KMaterialEnvironmentLabel, UnreachableCodeException>() {
          @Override public void cacheValueClose(
            final KMaterialEnvironmentLabel v)
          {
            // Nothing
          }

          @Override public KMaterialEnvironmentLabel cacheValueLoad(
            final KMeshWithMaterialRegularType instance)
          {
            try {
              return KMaterialEnvironmentLabel.fromMeshAndMaterialRegular(
                KLabelDecider.this.normal_cache.cacheGetLU(instance),
                instance);
            } catch (final JCacheException x) {
              throw new UnreachableCodeException(x);
            }
          }

          @Override public BigInteger cacheValueSizeOf(
            final KMaterialEnvironmentLabel v)
          {
            final BigInteger r = BigInteger.ONE;
            assert r != null;
            return r;
          }
        }, this.cache_config);
  }

  private
    LRUCacheTrivial<KMeshWithMaterialOpaqueType, KMaterialForwardOpaqueUnlitLabel, UnreachableCodeException>
    newForwardOpaqueUnlitCache()
  {
    return LRUCacheTrivial
      .newCache(
        new JCacheLoaderType<KMeshWithMaterialOpaqueType, KMaterialForwardOpaqueUnlitLabel, UnreachableCodeException>() {
          @Override public void cacheValueClose(
            final KMaterialForwardOpaqueUnlitLabel v)
          {
            // Nothing
          }

          @Override public KMaterialForwardOpaqueUnlitLabel cacheValueLoad(
            final KMeshWithMaterialOpaqueType instance)
          {
            try {
              final KMaterialAlbedoLabel albedo =
                KLabelDecider.this.albedo_cache.cacheGetLU(instance);
              final KMaterialEnvironmentLabel environment =
                KLabelDecider.this.environment_cache.cacheGetLU(instance);
              final KMaterialNormalLabel normal =
                KLabelDecider.this.normal_cache.cacheGetLU(instance);
              return KMaterialForwardOpaqueUnlitLabel.newLabel(
                albedo,
                environment,
                normal);
            } catch (final JCacheException x) {
              throw new UnreachableCodeException(x);
            }
          }

          @Override public BigInteger cacheValueSizeOf(
            final KMaterialForwardOpaqueUnlitLabel v)
          {
            final BigInteger r = BigInteger.ONE;
            assert r != null;
            return r;
          }
        }, this.cache_config);
  }

  private
    LRUCacheTrivial<KMeshWithMaterialRegularType, KMaterialForwardRegularLabel, UnreachableCodeException>
    newForwardRegularCache()
  {
    return LRUCacheTrivial
      .newCache(
        new JCacheLoaderType<KMeshWithMaterialRegularType, KMaterialForwardRegularLabel, UnreachableCodeException>() {
          @Override public void cacheValueClose(
            final KMaterialForwardRegularLabel v)
          {
            // Nothing
          }

          @Override public KMaterialForwardRegularLabel cacheValueLoad(
            final KMeshWithMaterialRegularType instance)
          {
            try {
              final KMaterialAlbedoLabel albedo =
                KLabelDecider.this.albedo_cache.cacheGetLU(instance);
              final KMaterialEmissiveLabel emissive =
                KLabelDecider.this.emissive_cache.cacheGetLU(instance);
              final KMaterialEnvironmentLabel environment =
                KLabelDecider.this.environment_cache.cacheGetLU(instance);
              final KMaterialNormalLabel normal =
                KLabelDecider.this.normal_cache.cacheGetLU(instance);
              final KMaterialSpecularLabel specular =
                KLabelDecider.this.specular_regular_cache
                  .cacheGetLU(instance);
              return KMaterialForwardRegularLabel.newLabel(
                albedo,
                emissive,
                environment,
                normal,
                specular);
            } catch (final JCacheException x) {
              throw new UnreachableCodeException(x);
            }
          }

          @Override public BigInteger cacheValueSizeOf(
            final KMaterialForwardRegularLabel v)
          {
            final BigInteger r = BigInteger.ONE;
            assert r != null;
            return r;
          }
        },
        this.cache_config);
  }

  private
    LRUCacheTrivial<KLightType, KLightLabel, UnreachableCodeException>
    newLightCache(
      final KGraphicsCapabilitiesType in_capabilities)
  {
    return LRUCacheTrivial
      .newCache(
        new JCacheLoaderType<KLightType, KLightLabel, UnreachableCodeException>() {
          @Override public void cacheValueClose(
            final KLightLabel v)
          {
            // Nothing
          }

          @Override public KLightLabel cacheValueLoad(
            final KLightType key)
          {
            return KLightLabel.fromLight(in_capabilities, key);
          }

          @Override public BigInteger cacheValueSizeOf(
            final KLightLabel v)
          {
            final BigInteger r = BigInteger.ONE;
            assert r != null;
            return r;
          }
        },
        this.cache_config);
  }

  private
    LRUCacheTrivial<KInstanceType, KMaterialNormalLabel, UnreachableCodeException>
    newNormalCache()
  {
    return LRUCacheTrivial
      .newCache(
        new JCacheLoaderType<KInstanceType, KMaterialNormalLabel, UnreachableCodeException>() {
          @Override public void cacheValueClose(
            final KMaterialNormalLabel v)
          {
            // Nothing
          }

          @Override public KMaterialNormalLabel cacheValueLoad(
            final KInstanceType instance)
          {
            return KMaterialNormalLabel.fromMeshAndMaterial(instance);
          }

          @Override public BigInteger cacheValueSizeOf(
            final KMaterialNormalLabel v)
          {
            final BigInteger r = BigInteger.ONE;
            assert r != null;
            return r;
          }
        },
        this.cache_config);
  }

  private
    LRUCacheTrivial<KMeshWithMaterialTranslucentRefractive, KMaterialRefractiveLabel, UnreachableCodeException>
    newRefractiveCache()
  {
    return LRUCacheTrivial
      .newCache(
        new JCacheLoaderType<KMeshWithMaterialTranslucentRefractive, KMaterialRefractiveLabel, UnreachableCodeException>() {
          @Override public void cacheValueClose(
            final KMaterialRefractiveLabel v)
          {
            // Nothing
          }

          @Override public KMaterialRefractiveLabel cacheValueLoad(
            final KMeshWithMaterialTranslucentRefractive instance)
          {
            return KMaterialRefractiveLabel.fromMeshAndMaterial(instance);
          }

          @Override public BigInteger cacheValueSizeOf(
            final KMaterialRefractiveLabel v)
          {
            final BigInteger r = BigInteger.ONE;
            assert r != null;
            return r;
          }
        },
        this.cache_config);
  }

  private
    LRUCacheTrivial<KMeshWithMaterialRegularType, KMaterialSpecularLabel, UnreachableCodeException>
    newSpecularCache()
  {
    return LRUCacheTrivial
      .newCache(
        new JCacheLoaderType<KMeshWithMaterialRegularType, KMaterialSpecularLabel, UnreachableCodeException>() {
          @Override public void cacheValueClose(
            final KMaterialSpecularLabel v)
          {
            // Nothing
          }

          @Override public KMaterialSpecularLabel cacheValueLoad(
            final KMeshWithMaterialRegularType instance)
          {
            try {
              return KMaterialSpecularLabel.fromMeshAndMaterialRegular(
                KLabelDecider.this.normal_cache.cacheGetLU(instance),
                instance);
            } catch (final JCacheException x) {
              throw new UnreachableCodeException(x);
            }
          }

          @Override public BigInteger cacheValueSizeOf(
            final KMaterialSpecularLabel v)
          {
            final BigInteger r = BigInteger.ONE;
            assert r != null;
            return r;
          }
        }, this.cache_config);
  }

  private
    LRUCacheType<KMeshWithMaterialTranslucentSpecularOnly, KMaterialSpecularLabel, UnreachableCodeException>
    newSpecularOnlyCache()
  {
    return LRUCacheTrivial
      .newCache(
        new JCacheLoaderType<KMeshWithMaterialTranslucentSpecularOnly, KMaterialSpecularLabel, UnreachableCodeException>() {
          @Override public void cacheValueClose(
            final KMaterialSpecularLabel v)
          {
            // Nothing
          }

          @Override public KMaterialSpecularLabel cacheValueLoad(
            final KMeshWithMaterialTranslucentSpecularOnly instance)
          {
            try {
              return KMaterialSpecularLabel.fromMeshAndMaterialSpecularOnly(
                KLabelDecider.this.normal_cache.cacheGetLU(instance),
                instance);
            } catch (final JCacheException x) {
              throw new UnreachableCodeException(x);
            }
          }

          @Override public BigInteger cacheValueSizeOf(
            final KMaterialSpecularLabel v)
          {
            final BigInteger r = BigInteger.ONE;
            assert r != null;
            return r;
          }
        }, this.cache_config);
  }

  private
    LRUCacheTrivial<KMeshWithMaterialTranslucentRefractive, KMaterialForwardTranslucentRefractiveLabel, UnreachableCodeException>
    newTranslucentRefractiveCache()
  {
    return LRUCacheTrivial
      .newCache(
        new JCacheLoaderType<KMeshWithMaterialTranslucentRefractive, KMaterialForwardTranslucentRefractiveLabel, UnreachableCodeException>() {
          @Override public void cacheValueClose(
            final KMaterialForwardTranslucentRefractiveLabel v)
          {
            // Nothing
          }

          @Override public
            KMaterialForwardTranslucentRefractiveLabel
            cacheValueLoad(
              final KMeshWithMaterialTranslucentRefractive instance)
          {
            try {
              final KMaterialNormalLabel normal =
                KLabelDecider.this.normal_cache.cacheGetLU(instance);
              final KMaterialRefractiveLabel refractive =
                KLabelDecider.this.refractive_cache.cacheGetLU(instance);

              return KMaterialForwardTranslucentRefractiveLabel.newLabel(
                refractive,
                normal);
            } catch (final JCacheException x) {
              throw new UnreachableCodeException(x);
            }
          }

          @Override public BigInteger cacheValueSizeOf(
            final KMaterialForwardTranslucentRefractiveLabel v)
          {
            final BigInteger r = BigInteger.ONE;
            assert r != null;
            return r;
          }
        }, this.cache_config);
  }

  private
    LRUCacheTrivial<KMeshWithMaterialTranslucentRegular, KMaterialForwardTranslucentRegularUnlitLabel, UnreachableCodeException>
    newTranslucentRegularUnlitCache()
  {
    return LRUCacheTrivial
      .newCache(
        new JCacheLoaderType<KMeshWithMaterialTranslucentRegular, KMaterialForwardTranslucentRegularUnlitLabel, UnreachableCodeException>() {
          @Override public void cacheValueClose(
            final KMaterialForwardTranslucentRegularUnlitLabel v)
          {
            // Nothing
          }

          @Override public
            KMaterialForwardTranslucentRegularUnlitLabel
            cacheValueLoad(
              final KMeshWithMaterialTranslucentRegular instance)
          {
            try {
              final KMaterialAlbedoLabel albedo =
                KLabelDecider.this.albedo_cache.cacheGetLU(instance);
              final KMaterialEnvironmentLabel environment =
                KLabelDecider.this.environment_cache.cacheGetLU(instance);
              final KMaterialNormalLabel normal =
                KLabelDecider.this.normal_cache.cacheGetLU(instance);
              final KMaterialAlphaOpacityType alpha =
                KLabelDecider.this.alpha_regular_cache.cacheGetLU(instance);

              return KMaterialForwardTranslucentRegularUnlitLabel.newLabel(
                albedo,
                environment,
                normal,
                alpha);
            } catch (final JCacheException x) {
              throw new UnreachableCodeException(x);
            }
          }

          @Override public BigInteger cacheValueSizeOf(
            final KMaterialForwardTranslucentRegularUnlitLabel v)
          {
            final BigInteger r = BigInteger.ONE;
            assert r != null;
            return r;
          }
        }, this.cache_config);
  }

  private
    LRUCacheType<KMeshWithMaterialTranslucentSpecularOnly, KMaterialForwardTranslucentSpecularOnlyLabel, UnreachableCodeException>
    newTranslucentSpecularOnlyCache()
  {
    return LRUCacheTrivial
      .newCache(
        new JCacheLoaderType<KMeshWithMaterialTranslucentSpecularOnly, KMaterialForwardTranslucentSpecularOnlyLabel, UnreachableCodeException>() {
          @Override public void cacheValueClose(
            final KMaterialForwardTranslucentSpecularOnlyLabel v)
          {
            // Nothing
          }

          @Override public
            KMaterialForwardTranslucentSpecularOnlyLabel
            cacheValueLoad(
              final KMeshWithMaterialTranslucentSpecularOnly instance)
          {
            try {
              final KMaterialNormalLabel normal =
                KLabelDecider.this.normal_cache.cacheGetLU(instance);
              final KMaterialSpecularLabel specular =
                KLabelDecider.this.specular_only_cache.cacheGetLU(instance);
              return KMaterialForwardTranslucentSpecularOnlyLabel.newLabel(
                normal,
                specular);
            } catch (final JCacheException x) {
              throw new UnreachableCodeException(x);
            }
          }

          @Override public BigInteger cacheValueSizeOf(
            final KMaterialForwardTranslucentSpecularOnlyLabel v)
          {
            final BigInteger r = BigInteger.ONE;
            assert r != null;
            return r;
          }
        }, this.cache_config);
  }
}
