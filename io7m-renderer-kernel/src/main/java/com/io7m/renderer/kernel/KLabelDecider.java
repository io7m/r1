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

import java.math.BigInteger;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jcache.JCacheException;
import com.io7m.jcache.JCacheLoaderType;
import com.io7m.jcache.LRUCacheConfig;
import com.io7m.jcache.LRUCacheTrivial;
import com.io7m.jcache.LRUCacheType;
import com.io7m.renderer.kernel.types.KGraphicsCapabilities;
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

public final class KLabelDecider implements KForwardLabelDeciderType
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
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   */

  public static @Nonnull KLabelDecider newDecider(
    final @Nonnull KGraphicsCapabilities capabilities,
    final @Nonnull BigInteger cache_labels)
    throws ConstraintError
  {
    final LRUCacheConfig config =
      LRUCacheConfig.empty().withMaximumCapacity(cache_labels);
    return new KLabelDecider(capabilities, config);
  }

  private final @Nonnull LRUCacheType<KInstanceRegularType, KMaterialAlbedoLabel, ConstraintError>                                     albedo_cache;
  private final @Nonnull LRUCacheType<KInstanceTranslucentRegular, KMaterialAlphaOpacityType, ConstraintError>                         alpha_regular_cache;
  private final @Nonnull LRUCacheType<KInstanceTranslucentSpecularOnly, KMaterialAlphaOpacityType, ConstraintError>                    alpha_specular_only_cache;
  private final @Nonnull LRUCacheConfig                                                                                                cache_config;
  private final @Nonnull KGraphicsCapabilities                                                                                         capabilities;
  private final @Nonnull LRUCacheType<KInstanceOpaqueType, KMaterialDepthLabel, ConstraintError>                                       depth_cache;
  private final @Nonnull LRUCacheType<KInstanceRegularType, KMaterialEmissiveLabel, ConstraintError>                                   emissive_cache;
  private final @Nonnull LRUCacheType<KInstanceRegularType, KMaterialEnvironmentLabel, ConstraintError>                                environment_cache;
  private final @Nonnull LRUCacheType<KInstanceOpaqueType, KMaterialForwardOpaqueUnlitLabel, ConstraintError>                          forward_opaque_unlit_cache;
  private final @Nonnull LRUCacheType<KInstanceRegularType, KMaterialForwardRegularLabel, ConstraintError>                             forward_regular_cache;
  private final @Nonnull LRUCacheType<KInstanceTranslucentRefractive, KMaterialForwardTranslucentRefractiveLabel, ConstraintError>     forward_translucent_refractive_cache;
  private final @Nonnull LRUCacheType<KInstanceTranslucentRegular, KMaterialForwardTranslucentRegularUnlitLabel, ConstraintError>      forward_translucent_regular_unlit_cache;
  private final @Nonnull LRUCacheType<KInstanceTranslucentSpecularOnly, KMaterialForwardTranslucentSpecularOnlyLabel, ConstraintError> forward_translucent_specular_only_cache;
  private final @Nonnull LRUCacheType<KLightType, KLightLabel, ConstraintError>                                                        light_cache;
  private final @Nonnull LRUCacheType<KInstanceType, KMaterialNormalLabel, ConstraintError>                                            normal_cache;
  private final @Nonnull LRUCacheType<KInstanceTranslucentRefractive, KMaterialRefractiveLabel, ConstraintError>                       refractive_cache;
  private final @Nonnull LRUCacheType<KInstanceTranslucentSpecularOnly, KMaterialSpecularLabel, ConstraintError>                       specular_only_cache;
  private final @Nonnull LRUCacheType<KInstanceRegularType, KMaterialSpecularLabel, ConstraintError>                                   specular_regular_cache;

  private KLabelDecider(
    final @Nonnull KGraphicsCapabilities in_capabilities,
    final @Nonnull LRUCacheConfig in_cache_config)
    throws ConstraintError
  {
    this.capabilities =
      Constraints.constrainNotNull(in_capabilities, "Capabilities");
    this.cache_config =
      Constraints.constrainNotNull(in_cache_config, "Cache config");

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

  @Override public @Nonnull KMaterialAlbedoLabel getAlbedoLabelRegular(
    final @Nonnull KInstanceRegularType instance)
    throws ConstraintError
  {
    try {
      return this.albedo_cache.cacheGetLU(instance);
    } catch (final JCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public @Nonnull
    KMaterialAlphaOpacityType
    getAlphaLabelTranslucentRegular(
      final @Nonnull KInstanceTranslucentRegular instance)
      throws ConstraintError
  {
    try {
      return this.alpha_regular_cache.cacheGetLU(instance);
    } catch (final JCacheException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private KMaterialAlphaOpacityType getAlphaLabelTranslucentSpecularOnly(
    final @Nonnull KInstanceTranslucentSpecularOnly instance)
    throws ConstraintError
  {
    try {
      return this.alpha_specular_only_cache.cacheGetLU(instance);
    } catch (final JCacheException e) {
      throw new UnreachableCodeException(e);
    }
  }

  @Override public KMaterialDepthLabel getDepthLabel(
    final @Nonnull KInstanceOpaqueType instance)
    throws ConstraintError
  {
    try {
      return this.depth_cache.cacheGetLU(instance);
    } catch (final JCacheException e) {
      throw new UnreachableCodeException(e);
    }
  }

  @Override public @Nonnull KMaterialEmissiveLabel getEmissiveLabelRegular(
    final @Nonnull KInstanceRegularType instance)
    throws ConstraintError
  {
    try {
      return this.emissive_cache.cacheGetLU(instance);
    } catch (final JCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public @Nonnull
    KMaterialEnvironmentLabel
    getEnvironmentLabelRegular(
      final @Nonnull KInstanceRegularType instance)
      throws ConstraintError
  {
    try {
      return this.environment_cache.cacheGetLU(instance);
    } catch (final JCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public KMaterialForwardOpaqueLitLabel getForwardLabelOpaqueLit(
    final @Nonnull KLightType light,
    final @Nonnull KInstanceOpaqueType instance)
    throws ConstraintError
  {
    return KMaterialForwardOpaqueLitLabel.newLabel(
      this.getLightLabel(light),
      this.getForwardLabelRegular(instance));
  }

  @Override public
    KMaterialForwardOpaqueUnlitLabel
    getForwardLabelOpaqueUnlit(
      final @Nonnull KInstanceOpaqueType instance)
      throws ConstraintError
  {
    try {
      return this.forward_opaque_unlit_cache.cacheGetLU(instance);
    } catch (final JCacheException e) {
      throw new UnreachableCodeException(e);
    }
  }

  @Override public KMaterialForwardRegularLabel getForwardLabelRegular(
    final @Nonnull KInstanceRegularType instance)
    throws ConstraintError
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
      final @Nonnull KInstanceTranslucentSpecularOnly instance)
      throws ConstraintError
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
      final @Nonnull KInstanceTranslucentRefractive instance)
      throws ConstraintError
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
      final @Nonnull KLightType light,
      final @Nonnull KInstanceTranslucentRegular instance)
      throws ConstraintError
  {
    return KMaterialForwardTranslucentRegularLitLabel.newLabel(
      this.getLightLabel(light),
      this.getForwardLabelRegular(instance),
      this.getAlphaLabelTranslucentRegular(instance));
  }

  @Override public
    KMaterialForwardTranslucentRegularUnlitLabel
    getForwardLabelTranslucentRegularUnlit(
      final @Nonnull KInstanceTranslucentRegular instance)
      throws ConstraintError
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
      final @Nonnull KLightType light,
      final @Nonnull KInstanceTranslucentSpecularOnly instance)
      throws ConstraintError
  {
    return KMaterialForwardTranslucentSpecularOnlyLitLabel.newLabel(
      this.getLightLabel(light),
      this.getForwardLabelSpecularOnly(instance),
      this.getAlphaLabelTranslucentSpecularOnly(instance));
  }

  @Override public @Nonnull KLightLabel getLightLabel(
    final @Nonnull KLightType light)
    throws ConstraintError
  {
    try {
      return this.light_cache.cacheGetLU(light);
    } catch (final JCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public @Nonnull KMaterialNormalLabel getNormalLabel(
    final @Nonnull KInstanceType instance)
    throws ConstraintError
  {
    try {
      return this.normal_cache.cacheGetLU(instance);
    } catch (final JCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public KMaterialRefractiveLabel getRefractiveLabel(
    final @Nonnull KInstanceTranslucentRefractive instance)
    throws ConstraintError
  {
    try {
      return this.refractive_cache.cacheGetLU(instance);
    } catch (final JCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public @Nonnull BigInteger getSize()
  {
    return this.albedo_cache
      .cacheSize()
      .add(this.alpha_regular_cache.cacheSize())
      .add(this.depth_cache.cacheSize())
      .add(this.emissive_cache.cacheSize())
      .add(this.environment_cache.cacheSize())
      .add(this.forward_opaque_unlit_cache.cacheSize())
      .add(this.forward_regular_cache.cacheSize())
      .add(this.forward_translucent_refractive_cache.cacheSize())
      .add(this.forward_translucent_regular_unlit_cache.cacheSize())
      .add(this.light_cache.cacheSize())
      .add(this.normal_cache.cacheSize())
      .add(this.refractive_cache.cacheSize())
      .add(this.specular_regular_cache.cacheSize());
  }

  @Override public @Nonnull KMaterialSpecularLabel getSpecularLabelRegular(
    final @Nonnull KInstanceRegularType instance)
    throws ConstraintError
  {
    try {
      return this.specular_regular_cache.cacheGetLU(instance);
    } catch (final JCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }

  private
    LRUCacheTrivial<KInstanceRegularType, KMaterialAlbedoLabel, ConstraintError>
    newAlbedoCache()
      throws ConstraintError
  {
    return LRUCacheTrivial
      .newCache(
        new JCacheLoaderType<KInstanceRegularType, KMaterialAlbedoLabel, ConstraintError>() {
          @Override public void cacheValueClose(
            final @Nonnull KMaterialAlbedoLabel v)
            throws ConstraintError
          {
            // Nothing
          }

          @Override public KMaterialAlbedoLabel cacheValueLoad(
            final @Nonnull KInstanceRegularType instance)
            throws ConstraintError
          {
            return KMaterialAlbedoLabel.fromInstanceRegular(instance);
          }

          @Override public @Nonnull BigInteger cacheValueSizeOf(
            final @Nonnull KMaterialAlbedoLabel v)
          {
            return BigInteger.ONE;
          }
        },
        this.cache_config);
  }

  private
    LRUCacheTrivial<KInstanceTranslucentRegular, KMaterialAlphaOpacityType, ConstraintError>
    newAlphaRegularCache()
      throws ConstraintError
  {
    return LRUCacheTrivial
      .newCache(
        new JCacheLoaderType<KInstanceTranslucentRegular, KMaterialAlphaOpacityType, ConstraintError>() {
          @Override public void cacheValueClose(
            final @Nonnull KMaterialAlphaOpacityType v)
            throws ConstraintError
          {
            // Nothing
          }

          @SuppressWarnings("synthetic-access") @Override public
            KMaterialAlphaOpacityType
            cacheValueLoad(
              final @Nonnull KInstanceTranslucentRegular instance)
              throws ConstraintError
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

          @Override public @Nonnull BigInteger cacheValueSizeOf(
            final @Nonnull KMaterialAlphaOpacityType v)
          {
            return BigInteger.ONE;
          }
        }, this.cache_config);
  }

  private
    LRUCacheType<KInstanceTranslucentSpecularOnly, KMaterialAlphaOpacityType, ConstraintError>
    newAlphaSpecularOnlyCache()
      throws ConstraintError
  {
    return LRUCacheTrivial
      .newCache(
        new JCacheLoaderType<KInstanceTranslucentSpecularOnly, KMaterialAlphaOpacityType, ConstraintError>() {
          @Override public void cacheValueClose(
            final @Nonnull KMaterialAlphaOpacityType v)
            throws ConstraintError
          {
            // Nothing
          }

          @SuppressWarnings("synthetic-access") @Override public
            KMaterialAlphaOpacityType
            cacheValueLoad(
              final @Nonnull KInstanceTranslucentSpecularOnly instance)
              throws ConstraintError
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

          @Override public @Nonnull BigInteger cacheValueSizeOf(
            final @Nonnull KMaterialAlphaOpacityType v)
          {
            return BigInteger.ONE;
          }
        }, this.cache_config);
  }

  private
    LRUCacheTrivial<KInstanceOpaqueType, KMaterialDepthLabel, ConstraintError>
    newDepthCache()
      throws ConstraintError
  {
    return LRUCacheTrivial
      .newCache(
        new JCacheLoaderType<KInstanceOpaqueType, KMaterialDepthLabel, ConstraintError>() {
          @Override public void cacheValueClose(
            final @Nonnull KMaterialDepthLabel v)
            throws ConstraintError
          {
            // Nothing
          }

          @SuppressWarnings("synthetic-access") @Override public
            KMaterialDepthLabel
            cacheValueLoad(
              final @Nonnull KInstanceOpaqueType instance)
              throws ConstraintError
          {
            try {
              final KMaterialAlbedoLabel albedo =
                KLabelDecider.this.albedo_cache.cacheGetLU(instance);
              return KMaterialDepthLabel.fromInstanceOpaque(albedo, instance);
            } catch (final JCacheException e) {
              throw new UnreachableCodeException(e);
            }
          }

          @Override public @Nonnull BigInteger cacheValueSizeOf(
            final @Nonnull KMaterialDepthLabel v)
          {
            return BigInteger.ONE;
          }
        },
        this.cache_config);
  }

  private
    LRUCacheTrivial<KInstanceRegularType, KMaterialEmissiveLabel, ConstraintError>
    newEmissiveCache()
      throws ConstraintError
  {
    return LRUCacheTrivial
      .newCache(
        new JCacheLoaderType<KInstanceRegularType, KMaterialEmissiveLabel, ConstraintError>() {
          @Override public void cacheValueClose(
            final @Nonnull KMaterialEmissiveLabel v)
            throws ConstraintError
          {
            // Nothing
          }

          @Override public KMaterialEmissiveLabel cacheValueLoad(
            final @Nonnull KInstanceRegularType instance)
            throws ConstraintError
          {
            return KMaterialEmissiveLabel.fromInstanceRegular(instance);
          }

          @Override public @Nonnull BigInteger cacheValueSizeOf(
            final @Nonnull KMaterialEmissiveLabel v)
          {
            return BigInteger.ONE;
          }
        },
        this.cache_config);
  }

  private
    LRUCacheTrivial<KInstanceRegularType, KMaterialEnvironmentLabel, ConstraintError>
    newEnvironmentCache()
      throws ConstraintError
  {
    return LRUCacheTrivial
      .newCache(
        new JCacheLoaderType<KInstanceRegularType, KMaterialEnvironmentLabel, ConstraintError>() {
          @Override public void cacheValueClose(
            final @Nonnull KMaterialEnvironmentLabel v)
            throws ConstraintError
          {
            // Nothing
          }

          @SuppressWarnings("synthetic-access") @Override public
            KMaterialEnvironmentLabel
            cacheValueLoad(
              final @Nonnull KInstanceRegularType instance)
              throws ConstraintError
          {
            try {
              return KMaterialEnvironmentLabel.fromInstanceRegular(
                KLabelDecider.this.normal_cache.cacheGetLU(instance),
                instance);
            } catch (final JCacheException x) {
              throw new UnreachableCodeException(x);
            }
          }

          @Override public @Nonnull BigInteger cacheValueSizeOf(
            final @Nonnull KMaterialEnvironmentLabel v)
          {
            return BigInteger.ONE;
          }
        }, this.cache_config);
  }

  private
    LRUCacheTrivial<KInstanceOpaqueType, KMaterialForwardOpaqueUnlitLabel, ConstraintError>
    newForwardOpaqueUnlitCache()
      throws ConstraintError
  {
    return LRUCacheTrivial
      .newCache(
        new JCacheLoaderType<KInstanceOpaqueType, KMaterialForwardOpaqueUnlitLabel, ConstraintError>() {
          @Override public void cacheValueClose(
            final @Nonnull KMaterialForwardOpaqueUnlitLabel v)
            throws ConstraintError
          {
            // Nothing
          }

          @SuppressWarnings("synthetic-access") @Override public
            KMaterialForwardOpaqueUnlitLabel
            cacheValueLoad(
              final @Nonnull KInstanceOpaqueType instance)
              throws ConstraintError
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
            final @Nonnull KMaterialForwardOpaqueUnlitLabel v)
          {
            return BigInteger.ONE;
          }
        }, this.cache_config);
  }

  private
    LRUCacheTrivial<KInstanceRegularType, KMaterialForwardRegularLabel, ConstraintError>
    newForwardRegularCache()
      throws ConstraintError
  {
    return LRUCacheTrivial
      .newCache(
        new JCacheLoaderType<KInstanceRegularType, KMaterialForwardRegularLabel, ConstraintError>() {
          @Override public void cacheValueClose(
            final @Nonnull KMaterialForwardRegularLabel v)
            throws ConstraintError
          {
            // Nothing
          }

          @SuppressWarnings("synthetic-access") @Override public
            KMaterialForwardRegularLabel
            cacheValueLoad(
              final @Nonnull KInstanceRegularType instance)
              throws ConstraintError
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
            final @Nonnull KMaterialForwardRegularLabel v)
          {
            return BigInteger.ONE;
          }
        },
        this.cache_config);
  }

  private
    LRUCacheTrivial<KLightType, KLightLabel, ConstraintError>
    newLightCache(
      final KGraphicsCapabilities in_capabilities)
      throws ConstraintError
  {
    return LRUCacheTrivial.newCache(
      new JCacheLoaderType<KLightType, KLightLabel, ConstraintError>() {
        @Override public void cacheValueClose(
          final @Nonnull KLightLabel v)
          throws ConstraintError
        {
          // Nothing
        }

        @Override public KLightLabel cacheValueLoad(
          final @Nonnull KLightType key)
          throws ConstraintError
        {
          return KLightLabel.fromLight(in_capabilities, key);
        }

        @Override public @Nonnull BigInteger cacheValueSizeOf(
          final @Nonnull KLightLabel v)
        {
          return BigInteger.ONE;
        }
      },
      this.cache_config);
  }

  private
    LRUCacheTrivial<KInstanceType, KMaterialNormalLabel, ConstraintError>
    newNormalCache()
      throws ConstraintError
  {
    return LRUCacheTrivial
      .newCache(
        new JCacheLoaderType<KInstanceType, KMaterialNormalLabel, ConstraintError>() {
          @Override public void cacheValueClose(
            final @Nonnull KMaterialNormalLabel v)
            throws ConstraintError
          {
            // Nothing
          }

          @Override public KMaterialNormalLabel cacheValueLoad(
            final @Nonnull KInstanceType instance)
            throws ConstraintError
          {
            return KMaterialNormalLabel.fromInstance(instance);
          }

          @Override public @Nonnull BigInteger cacheValueSizeOf(
            final @Nonnull KMaterialNormalLabel v)
          {
            return BigInteger.ONE;
          }
        },
        this.cache_config);
  }

  private
    LRUCacheTrivial<KInstanceTranslucentRefractive, KMaterialRefractiveLabel, ConstraintError>
    newRefractiveCache()
      throws ConstraintError
  {
    return LRUCacheTrivial
      .newCache(
        new JCacheLoaderType<KInstanceTranslucentRefractive, KMaterialRefractiveLabel, ConstraintError>() {
          @Override public void cacheValueClose(
            final @Nonnull KMaterialRefractiveLabel v)
            throws ConstraintError
          {
            // Nothing
          }

          @Override public KMaterialRefractiveLabel cacheValueLoad(
            final @Nonnull KInstanceTranslucentRefractive instance)
            throws ConstraintError
          {
            return KMaterialRefractiveLabel.fromInstance(instance);
          }

          @Override public @Nonnull BigInteger cacheValueSizeOf(
            final @Nonnull KMaterialRefractiveLabel v)
          {
            return BigInteger.ONE;
          }
        },
        this.cache_config);
  }

  private
    LRUCacheTrivial<KInstanceRegularType, KMaterialSpecularLabel, ConstraintError>
    newSpecularCache()
      throws ConstraintError
  {
    return LRUCacheTrivial
      .newCache(
        new JCacheLoaderType<KInstanceRegularType, KMaterialSpecularLabel, ConstraintError>() {
          @Override public void cacheValueClose(
            final @Nonnull KMaterialSpecularLabel v)
            throws ConstraintError
          {
            // Nothing
          }

          @SuppressWarnings("synthetic-access") @Override public
            KMaterialSpecularLabel
            cacheValueLoad(
              final @Nonnull KInstanceRegularType instance)
              throws ConstraintError
          {
            try {
              return KMaterialSpecularLabel.fromInstanceRegular(
                KLabelDecider.this.normal_cache.cacheGetLU(instance),
                instance);
            } catch (final JCacheException x) {
              throw new UnreachableCodeException(x);
            }
          }

          @Override public @Nonnull BigInteger cacheValueSizeOf(
            final @Nonnull KMaterialSpecularLabel v)
          {
            return BigInteger.ONE;
          }
        }, this.cache_config);
  }

  private
    LRUCacheType<KInstanceTranslucentSpecularOnly, KMaterialSpecularLabel, ConstraintError>
    newSpecularOnlyCache()
      throws ConstraintError
  {
    return LRUCacheTrivial
      .newCache(
        new JCacheLoaderType<KInstanceTranslucentSpecularOnly, KMaterialSpecularLabel, ConstraintError>() {
          @Override public void cacheValueClose(
            final @Nonnull KMaterialSpecularLabel v)
            throws ConstraintError
          {
            // Nothing
          }

          @SuppressWarnings("synthetic-access") @Override public
            KMaterialSpecularLabel
            cacheValueLoad(
              final @Nonnull KInstanceTranslucentSpecularOnly instance)
              throws ConstraintError
          {
            try {
              return KMaterialSpecularLabel.fromInstanceSpecularOnly(
                KLabelDecider.this.normal_cache.cacheGetLU(instance),
                instance);
            } catch (final JCacheException x) {
              throw new UnreachableCodeException(x);
            }
          }

          @Override public @Nonnull BigInteger cacheValueSizeOf(
            final @Nonnull KMaterialSpecularLabel v)
          {
            return BigInteger.ONE;
          }
        }, this.cache_config);
  }

  private
    LRUCacheTrivial<KInstanceTranslucentRefractive, KMaterialForwardTranslucentRefractiveLabel, ConstraintError>
    newTranslucentRefractiveCache()
      throws ConstraintError
  {
    return LRUCacheTrivial
      .newCache(
        new JCacheLoaderType<KInstanceTranslucentRefractive, KMaterialForwardTranslucentRefractiveLabel, ConstraintError>() {
          @Override public void cacheValueClose(
            final @Nonnull KMaterialForwardTranslucentRefractiveLabel v)
            throws ConstraintError
          {
            // Nothing
          }

          @SuppressWarnings("synthetic-access") @Override public
            KMaterialForwardTranslucentRefractiveLabel
            cacheValueLoad(
              final @Nonnull KInstanceTranslucentRefractive instance)
              throws ConstraintError
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
            final @Nonnull KMaterialForwardTranslucentRefractiveLabel v)
          {
            return BigInteger.ONE;
          }
        }, this.cache_config);
  }

  private
    LRUCacheTrivial<KInstanceTranslucentRegular, KMaterialForwardTranslucentRegularUnlitLabel, ConstraintError>
    newTranslucentRegularUnlitCache()
      throws ConstraintError
  {
    return LRUCacheTrivial
      .newCache(
        new JCacheLoaderType<KInstanceTranslucentRegular, KMaterialForwardTranslucentRegularUnlitLabel, ConstraintError>() {
          @Override public void cacheValueClose(
            final @Nonnull KMaterialForwardTranslucentRegularUnlitLabel v)
            throws ConstraintError
          {
            // Nothing
          }

          @SuppressWarnings("synthetic-access") @Override public
            KMaterialForwardTranslucentRegularUnlitLabel
            cacheValueLoad(
              final @Nonnull KInstanceTranslucentRegular instance)
              throws ConstraintError
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
            final @Nonnull KMaterialForwardTranslucentRegularUnlitLabel v)
          {
            return BigInteger.ONE;
          }
        }, this.cache_config);
  }

  private
    LRUCacheType<KInstanceTranslucentSpecularOnly, KMaterialForwardTranslucentSpecularOnlyLabel, ConstraintError>
    newTranslucentSpecularOnlyCache()
      throws ConstraintError
  {
    return LRUCacheTrivial
      .newCache(
        new JCacheLoaderType<KInstanceTranslucentSpecularOnly, KMaterialForwardTranslucentSpecularOnlyLabel, ConstraintError>() {
          @Override public void cacheValueClose(
            final @Nonnull KMaterialForwardTranslucentSpecularOnlyLabel v)
            throws ConstraintError
          {
            // Nothing
          }

          @SuppressWarnings("synthetic-access") @Override public
            KMaterialForwardTranslucentSpecularOnlyLabel
            cacheValueLoad(
              final @Nonnull KInstanceTranslucentSpecularOnly instance)
              throws ConstraintError
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
            final @Nonnull KMaterialForwardTranslucentSpecularOnlyLabel v)
          {
            return BigInteger.ONE;
          }
        }, this.cache_config);
  }
}
