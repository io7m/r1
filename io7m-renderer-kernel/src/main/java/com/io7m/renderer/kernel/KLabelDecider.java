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
import com.io7m.renderer.kernel.types.KInstanceOpaqueType;
import com.io7m.renderer.kernel.types.KInstanceRegularType;
import com.io7m.renderer.kernel.types.KInstanceTranslucentRefractive;
import com.io7m.renderer.kernel.types.KInstanceTranslucentRegular;
import com.io7m.renderer.kernel.types.KInstanceTranslucentSpecularOnly;
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

  private final LRUCacheType<KInstanceRegularType, KMaterialAlbedoLabel, UnreachableCodeException>                                     albedo_cache;
  private final LRUCacheType<KInstanceTranslucentRegular, KMaterialAlphaOpacityType, UnreachableCodeException>                         alpha_regular_cache;
  private final LRUCacheType<KInstanceTranslucentSpecularOnly, KMaterialAlphaOpacityType, UnreachableCodeException>                    alpha_specular_only_cache;
  private final LRUCacheConfig                                                                                                         cache_config;
  private final KGraphicsCapabilitiesType                                                                                              capabilities;
  private final LRUCacheType<KInstanceOpaqueType, KMaterialDepthLabel, UnreachableCodeException>                                       depth_cache;
  private final LRUCacheType<KInstanceRegularType, KMaterialEmissiveLabel, UnreachableCodeException>                                   emissive_cache;
  private final LRUCacheType<KInstanceRegularType, KMaterialEnvironmentLabel, UnreachableCodeException>                                environment_cache;
  private final LRUCacheType<KInstanceOpaqueType, KMaterialForwardOpaqueUnlitLabel, UnreachableCodeException>                          forward_opaque_unlit_cache;
  private final LRUCacheType<KInstanceRegularType, KMaterialForwardRegularLabel, UnreachableCodeException>                             forward_regular_cache;
  private final LRUCacheType<KInstanceTranslucentRefractive, KMaterialForwardTranslucentRefractiveLabel, UnreachableCodeException>     forward_translucent_refractive_cache;
  private final LRUCacheType<KInstanceTranslucentRegular, KMaterialForwardTranslucentRegularUnlitLabel, UnreachableCodeException>      forward_translucent_regular_unlit_cache;
  private final LRUCacheType<KInstanceTranslucentSpecularOnly, KMaterialForwardTranslucentSpecularOnlyLabel, UnreachableCodeException> forward_translucent_specular_only_cache;
  private final LRUCacheType<KLightType, KLightLabel, UnreachableCodeException>                                                        light_cache;
  private final LRUCacheType<KInstanceType, KMaterialNormalLabel, UnreachableCodeException>                                            normal_cache;
  private final LRUCacheType<KInstanceTranslucentRefractive, KMaterialRefractiveLabel, UnreachableCodeException>                       refractive_cache;
  private final LRUCacheType<KInstanceTranslucentSpecularOnly, KMaterialSpecularLabel, UnreachableCodeException>                       specular_only_cache;
  private final LRUCacheType<KInstanceRegularType, KMaterialSpecularLabel, UnreachableCodeException>                                   specular_regular_cache;

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
    final KInstanceRegularType instance)
  {
    try {
      return this.albedo_cache.cacheGetLU(instance);
    } catch (final JCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public KMaterialAlphaOpacityType getAlphaLabelTranslucentRegular(
    final KInstanceTranslucentRegular instance)
  {
    try {
      return this.alpha_regular_cache.cacheGetLU(instance);
    } catch (final JCacheException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private KMaterialAlphaOpacityType getAlphaLabelTranslucentSpecularOnly(
    final KInstanceTranslucentSpecularOnly instance)
  {
    try {
      return this.alpha_specular_only_cache.cacheGetLU(instance);
    } catch (final JCacheException e) {
      throw new UnreachableCodeException(e);
    }
  }

  @Override public KMaterialDepthLabel getDepthLabel(
    final KInstanceOpaqueType instance)
  {
    try {
      return this.depth_cache.cacheGetLU(instance);
    } catch (final JCacheException e) {
      throw new UnreachableCodeException(e);
    }
  }

  @Override public KMaterialEmissiveLabel getEmissiveLabelRegular(
    final KInstanceRegularType instance)
  {
    try {
      return this.emissive_cache.cacheGetLU(instance);
    } catch (final JCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public KMaterialEnvironmentLabel getEnvironmentLabelRegular(
    final KInstanceRegularType instance)
  {
    try {
      return this.environment_cache.cacheGetLU(instance);
    } catch (final JCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public KMaterialForwardOpaqueLitLabel getForwardLabelOpaqueLit(
    final KLightType light,
    final KInstanceOpaqueType instance)
  {
    return KMaterialForwardOpaqueLitLabel.newLabel(
      this.getLightLabel(light),
      this.getForwardLabelRegular(instance));
  }

  @Override public
    KMaterialForwardOpaqueUnlitLabel
    getForwardLabelOpaqueUnlit(
      final KInstanceOpaqueType instance)
  {
    try {
      return this.forward_opaque_unlit_cache.cacheGetLU(instance);
    } catch (final JCacheException e) {
      throw new UnreachableCodeException(e);
    }
  }

  @Override public KMaterialForwardRegularLabel getForwardLabelRegular(
    final KInstanceRegularType instance)
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
      final KInstanceTranslucentSpecularOnly instance)
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
      final KInstanceTranslucentRefractive instance)
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
      final KInstanceTranslucentRegular instance)
  {
    return KMaterialForwardTranslucentRegularLitLabel.newLabel(
      this.getLightLabel(light),
      this.getForwardLabelRegular(instance),
      this.getAlphaLabelTranslucentRegular(instance));
  }

  @Override public
    KMaterialForwardTranslucentRegularUnlitLabel
    getForwardLabelTranslucentRegularUnlit(
      final KInstanceTranslucentRegular instance)
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
      final KInstanceTranslucentSpecularOnly instance)
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
    final KInstanceTranslucentRefractive instance)
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
    final KInstanceRegularType instance)
  {
    try {
      return this.specular_regular_cache.cacheGetLU(instance);
    } catch (final JCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }

  private
    LRUCacheTrivial<KInstanceRegularType, KMaterialAlbedoLabel, UnreachableCodeException>
    newAlbedoCache()
  {
    return LRUCacheTrivial
      .newCache(
        new JCacheLoaderType<KInstanceRegularType, KMaterialAlbedoLabel, UnreachableCodeException>() {
          @Override public void cacheValueClose(
            final KMaterialAlbedoLabel v)
          {
            // Nothing
          }

          @Override public KMaterialAlbedoLabel cacheValueLoad(
            final KInstanceRegularType instance)
          {
            return KMaterialAlbedoLabel.fromInstanceRegular(instance);
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
    LRUCacheTrivial<KInstanceTranslucentRegular, KMaterialAlphaOpacityType, UnreachableCodeException>
    newAlphaRegularCache()
  {
    return LRUCacheTrivial
      .newCache(
        new JCacheLoaderType<KInstanceTranslucentRegular, KMaterialAlphaOpacityType, UnreachableCodeException>() {
          @Override public void cacheValueClose(
            final KMaterialAlphaOpacityType v)
          {
            // Nothing
          }

          @Override public KMaterialAlphaOpacityType cacheValueLoad(
            final KInstanceTranslucentRegular instance)
          {
            try {
              final KMaterialNormalLabel normal =
                KLabelDecider.this.normal_cache.cacheGetLU(instance);
              return KMaterialAlphaOpacityType.fromInstanceRegular(
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
    LRUCacheType<KInstanceTranslucentSpecularOnly, KMaterialAlphaOpacityType, UnreachableCodeException>
    newAlphaSpecularOnlyCache()
  {
    return LRUCacheTrivial
      .newCache(
        new JCacheLoaderType<KInstanceTranslucentSpecularOnly, KMaterialAlphaOpacityType, UnreachableCodeException>() {
          @Override public void cacheValueClose(
            final KMaterialAlphaOpacityType v)
          {
            // Nothing
          }

          @Override public KMaterialAlphaOpacityType cacheValueLoad(
            final KInstanceTranslucentSpecularOnly instance)
          {
            try {
              final KMaterialNormalLabel normal =
                KLabelDecider.this.normal_cache.cacheGetLU(instance);
              return KMaterialAlphaOpacityType.fromInstanceSpecularOnly(
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
    LRUCacheTrivial<KInstanceOpaqueType, KMaterialDepthLabel, UnreachableCodeException>
    newDepthCache()
  {
    return LRUCacheTrivial
      .newCache(
        new JCacheLoaderType<KInstanceOpaqueType, KMaterialDepthLabel, UnreachableCodeException>() {
          @Override public void cacheValueClose(
            final KMaterialDepthLabel v)
          {
            // Nothing
          }

          @Override public KMaterialDepthLabel cacheValueLoad(
            final KInstanceOpaqueType instance)
          {
            try {
              final KMaterialAlbedoLabel albedo =
                KLabelDecider.this.albedo_cache.cacheGetLU(instance);
              return KMaterialDepthLabel.fromInstanceOpaque(albedo, instance);
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
    LRUCacheTrivial<KInstanceRegularType, KMaterialEmissiveLabel, UnreachableCodeException>
    newEmissiveCache()
  {
    return LRUCacheTrivial
      .newCache(
        new JCacheLoaderType<KInstanceRegularType, KMaterialEmissiveLabel, UnreachableCodeException>() {
          @Override public void cacheValueClose(
            final KMaterialEmissiveLabel v)
          {
            // Nothing
          }

          @Override public KMaterialEmissiveLabel cacheValueLoad(
            final KInstanceRegularType instance)
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
    LRUCacheTrivial<KInstanceRegularType, KMaterialEnvironmentLabel, UnreachableCodeException>
    newEnvironmentCache()
  {
    return LRUCacheTrivial
      .newCache(
        new JCacheLoaderType<KInstanceRegularType, KMaterialEnvironmentLabel, UnreachableCodeException>() {
          @Override public void cacheValueClose(
            final KMaterialEnvironmentLabel v)
          {
            // Nothing
          }

          @Override public KMaterialEnvironmentLabel cacheValueLoad(
            final KInstanceRegularType instance)
          {
            try {
              return KMaterialEnvironmentLabel.fromInstanceRegular(
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
    LRUCacheTrivial<KInstanceOpaqueType, KMaterialForwardOpaqueUnlitLabel, UnreachableCodeException>
    newForwardOpaqueUnlitCache()
  {
    return LRUCacheTrivial
      .newCache(
        new JCacheLoaderType<KInstanceOpaqueType, KMaterialForwardOpaqueUnlitLabel, UnreachableCodeException>() {
          @Override public void cacheValueClose(
            final KMaterialForwardOpaqueUnlitLabel v)
          {
            // Nothing
          }

          @Override public KMaterialForwardOpaqueUnlitLabel cacheValueLoad(
            final KInstanceOpaqueType instance)
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
    LRUCacheTrivial<KInstanceRegularType, KMaterialForwardRegularLabel, UnreachableCodeException>
    newForwardRegularCache()
  {
    return LRUCacheTrivial
      .newCache(
        new JCacheLoaderType<KInstanceRegularType, KMaterialForwardRegularLabel, UnreachableCodeException>() {
          @Override public void cacheValueClose(
            final KMaterialForwardRegularLabel v)
          {
            // Nothing
          }

          @Override public KMaterialForwardRegularLabel cacheValueLoad(
            final KInstanceRegularType instance)
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
            return KMaterialNormalLabel.fromInstance(instance);
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
    LRUCacheTrivial<KInstanceTranslucentRefractive, KMaterialRefractiveLabel, UnreachableCodeException>
    newRefractiveCache()
  {
    return LRUCacheTrivial
      .newCache(
        new JCacheLoaderType<KInstanceTranslucentRefractive, KMaterialRefractiveLabel, UnreachableCodeException>() {
          @Override public void cacheValueClose(
            final KMaterialRefractiveLabel v)
          {
            // Nothing
          }

          @Override public KMaterialRefractiveLabel cacheValueLoad(
            final KInstanceTranslucentRefractive instance)
          {
            return KMaterialRefractiveLabel.fromInstance(instance);
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
    LRUCacheTrivial<KInstanceRegularType, KMaterialSpecularLabel, UnreachableCodeException>
    newSpecularCache()
  {
    return LRUCacheTrivial
      .newCache(
        new JCacheLoaderType<KInstanceRegularType, KMaterialSpecularLabel, UnreachableCodeException>() {
          @Override public void cacheValueClose(
            final KMaterialSpecularLabel v)
          {
            // Nothing
          }

          @Override public KMaterialSpecularLabel cacheValueLoad(
            final KInstanceRegularType instance)
          {
            try {
              return KMaterialSpecularLabel.fromInstanceRegular(
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
    LRUCacheType<KInstanceTranslucentSpecularOnly, KMaterialSpecularLabel, UnreachableCodeException>
    newSpecularOnlyCache()
  {
    return LRUCacheTrivial
      .newCache(
        new JCacheLoaderType<KInstanceTranslucentSpecularOnly, KMaterialSpecularLabel, UnreachableCodeException>() {
          @Override public void cacheValueClose(
            final KMaterialSpecularLabel v)
          {
            // Nothing
          }

          @Override public KMaterialSpecularLabel cacheValueLoad(
            final KInstanceTranslucentSpecularOnly instance)
          {
            try {
              return KMaterialSpecularLabel.fromInstanceSpecularOnly(
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
    LRUCacheTrivial<KInstanceTranslucentRefractive, KMaterialForwardTranslucentRefractiveLabel, UnreachableCodeException>
    newTranslucentRefractiveCache()
  {
    return LRUCacheTrivial
      .newCache(
        new JCacheLoaderType<KInstanceTranslucentRefractive, KMaterialForwardTranslucentRefractiveLabel, UnreachableCodeException>() {
          @Override public void cacheValueClose(
            final KMaterialForwardTranslucentRefractiveLabel v)
          {
            // Nothing
          }

          @Override public
            KMaterialForwardTranslucentRefractiveLabel
            cacheValueLoad(
              final KInstanceTranslucentRefractive instance)
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
    LRUCacheTrivial<KInstanceTranslucentRegular, KMaterialForwardTranslucentRegularUnlitLabel, UnreachableCodeException>
    newTranslucentRegularUnlitCache()
  {
    return LRUCacheTrivial
      .newCache(
        new JCacheLoaderType<KInstanceTranslucentRegular, KMaterialForwardTranslucentRegularUnlitLabel, UnreachableCodeException>() {
          @Override public void cacheValueClose(
            final KMaterialForwardTranslucentRegularUnlitLabel v)
          {
            // Nothing
          }

          @Override public
            KMaterialForwardTranslucentRegularUnlitLabel
            cacheValueLoad(
              final KInstanceTranslucentRegular instance)
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
    LRUCacheType<KInstanceTranslucentSpecularOnly, KMaterialForwardTranslucentSpecularOnlyLabel, UnreachableCodeException>
    newTranslucentSpecularOnlyCache()
  {
    return LRUCacheTrivial
      .newCache(
        new JCacheLoaderType<KInstanceTranslucentSpecularOnly, KMaterialForwardTranslucentSpecularOnlyLabel, UnreachableCodeException>() {
          @Override public void cacheValueClose(
            final KMaterialForwardTranslucentSpecularOnlyLabel v)
          {
            // Nothing
          }

          @Override public
            KMaterialForwardTranslucentSpecularOnlyLabel
            cacheValueLoad(
              final KInstanceTranslucentSpecularOnly instance)
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
