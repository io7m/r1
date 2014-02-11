/*
 * Copyright © 2013 <code@io7m.com> http://io7m.com
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
import com.io7m.jcache.JCacheLoader;
import com.io7m.jcache.LRUCacheConfig;
import com.io7m.jcache.LRUCacheTrivial;
import com.io7m.renderer.kernel.types.KGraphicsCapabilities;
import com.io7m.renderer.kernel.types.KInstance;
import com.io7m.renderer.kernel.types.KInstanceOpaque;
import com.io7m.renderer.kernel.types.KInstanceRegular;
import com.io7m.renderer.kernel.types.KInstanceTranslucentRegular;
import com.io7m.renderer.kernel.types.KLight;
import com.io7m.renderer.kernel.types.KLightLabel;
import com.io7m.renderer.kernel.types.KMaterialAlbedoLabel;
import com.io7m.renderer.kernel.types.KMaterialDepthLabel;
import com.io7m.renderer.kernel.types.KMaterialEmissiveLabel;
import com.io7m.renderer.kernel.types.KMaterialEnvironmentLabel;
import com.io7m.renderer.kernel.types.KMaterialForwardOpaqueLitLabel;
import com.io7m.renderer.kernel.types.KMaterialForwardOpaqueUnlitLabel;
import com.io7m.renderer.kernel.types.KMaterialForwardRegularLabel;
import com.io7m.renderer.kernel.types.KMaterialForwardTranslucentRegularLitLabel;
import com.io7m.renderer.kernel.types.KMaterialForwardTranslucentRegularUnlitLabel;
import com.io7m.renderer.kernel.types.KMaterialNormalLabel;
import com.io7m.renderer.kernel.types.KMaterialSpecularLabel;

final class KLabelDecider implements
  KLightLabelCache,
  KMaterialAlbedoLabelCache,
  KMaterialDepthLabelCache,
  KMaterialEmissiveLabelCache,
  KMaterialEnvironmentLabelCache,
  KMaterialNormalLabelCache,
  KMaterialSpecularLabelCache,
  KMaterialForwardRegularLabelCache,
  KMaterialForwardOpaqueUnlitLabelCache,
  KMaterialForwardOpaqueLitLabelCache,
  KMaterialForwardTranslucentRegularLitLabelCache,
  KMaterialForwardTranslucentRegularUnlitLabelCache
{
  public static @Nonnull KLabelDecider newDecider(
    final @Nonnull KGraphicsCapabilities capabilities,
    final @Nonnull BigInteger cache_labels)
    throws ConstraintError
  {
    final LRUCacheConfig config =
      LRUCacheConfig.empty().withMaximumCapacity(cache_labels);
    return new KLabelDecider(capabilities, config);
  }

  private final @Nonnull LRUCacheTrivial<KInstanceRegular, KMaterialAlbedoLabel, ConstraintError>                                    albedo_cache;
  private final @Nonnull LRUCacheConfig                                                                                              cache_config;
  private final @Nonnull KGraphicsCapabilities                                                                                       capabilities;
  private final @Nonnull LRUCacheTrivial<KInstanceOpaque, KMaterialDepthLabel, ConstraintError>                                      depth_cache;
  private final @Nonnull LRUCacheTrivial<KInstanceRegular, KMaterialEmissiveLabel, ConstraintError>                                  emissive_cache;
  private final @Nonnull LRUCacheTrivial<KInstanceRegular, KMaterialEnvironmentLabel, ConstraintError>                               environment_cache;
  private final @Nonnull LRUCacheTrivial<KInstanceOpaque, KMaterialForwardOpaqueUnlitLabel, ConstraintError>                         forward_opaque_unlit_cache;
  private final @Nonnull LRUCacheTrivial<KInstanceRegular, KMaterialForwardRegularLabel, ConstraintError>                            forward_regular_cache;
  private final @Nonnull LRUCacheTrivial<KInstanceTranslucentRegular, KMaterialForwardTranslucentRegularUnlitLabel, ConstraintError> forward_translucent_regular_unlit_cache;
  private final @Nonnull LRUCacheTrivial<KLight, KLightLabel, ConstraintError>                                                       light_cache;
  private final @Nonnull LRUCacheTrivial<KInstance, KMaterialNormalLabel, ConstraintError>                                           normal_cache;
  private final @Nonnull LRUCacheTrivial<KInstanceRegular, KMaterialSpecularLabel, ConstraintError>                                  specular_cache;

  private KLabelDecider(
    final @Nonnull KGraphicsCapabilities capabilities,
    final @Nonnull LRUCacheConfig cache_config)
    throws ConstraintError
  {
    this.capabilities =
      Constraints.constrainNotNull(capabilities, "Capabilities");
    this.cache_config =
      Constraints.constrainNotNull(cache_config, "Cache config");

    this.albedo_cache =
      LRUCacheTrivial
        .newCache(
          new JCacheLoader<KInstanceRegular, KMaterialAlbedoLabel, ConstraintError>() {
            @Override public void cacheValueClose(
              final @Nonnull KMaterialAlbedoLabel v)
              throws ConstraintError
            {
              // Nothing
            }

            @Override public KMaterialAlbedoLabel cacheValueLoad(
              final @Nonnull KInstanceRegular instance)
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

    this.depth_cache =
      LRUCacheTrivial
        .newCache(
          new JCacheLoader<KInstanceOpaque, KMaterialDepthLabel, ConstraintError>() {
            @Override public void cacheValueClose(
              final @Nonnull KMaterialDepthLabel v)
              throws ConstraintError
            {
              // Nothing
            }

            @SuppressWarnings("synthetic-access") @Override public
              KMaterialDepthLabel
              cacheValueLoad(
                final @Nonnull KInstanceOpaque instance)
                throws ConstraintError
            {
              try {
                final KMaterialAlbedoLabel albedo =
                  KLabelDecider.this.albedo_cache.cacheGetLU(instance);
                return KMaterialDepthLabel.fromInstanceOpaque(
                  albedo,
                  instance);
              } catch (final JCacheException e) {
                throw new UnreachableCodeException(e);
              }
            }

            @Override public @Nonnull BigInteger cacheValueSizeOf(
              final @Nonnull KMaterialDepthLabel v)
            {
              return BigInteger.ONE;
            }
          }, this.cache_config);

    this.emissive_cache =
      LRUCacheTrivial
        .newCache(
          new JCacheLoader<KInstanceRegular, KMaterialEmissiveLabel, ConstraintError>() {
            @Override public void cacheValueClose(
              final @Nonnull KMaterialEmissiveLabel v)
              throws ConstraintError
            {
              // Nothing
            }

            @Override public KMaterialEmissiveLabel cacheValueLoad(
              final @Nonnull KInstanceRegular instance)
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

    this.environment_cache =
      LRUCacheTrivial
        .newCache(
          new JCacheLoader<KInstanceRegular, KMaterialEnvironmentLabel, ConstraintError>() {
            @Override public void cacheValueClose(
              final @Nonnull KMaterialEnvironmentLabel v)
              throws ConstraintError
            {
              // Nothing
            }

            @SuppressWarnings("synthetic-access") @Override public
              KMaterialEnvironmentLabel
              cacheValueLoad(
                final @Nonnull KInstanceRegular instance)
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

    this.normal_cache =
      LRUCacheTrivial.newCache(
        new JCacheLoader<KInstance, KMaterialNormalLabel, ConstraintError>() {
          @Override public void cacheValueClose(
            final @Nonnull KMaterialNormalLabel v)
            throws ConstraintError
          {
            // Nothing
          }

          @Override public KMaterialNormalLabel cacheValueLoad(
            final @Nonnull KInstance instance)
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

    this.specular_cache =
      LRUCacheTrivial
        .newCache(
          new JCacheLoader<KInstanceRegular, KMaterialSpecularLabel, ConstraintError>() {
            @Override public void cacheValueClose(
              final @Nonnull KMaterialSpecularLabel v)
              throws ConstraintError
            {
              // Nothing
            }

            @SuppressWarnings("synthetic-access") @Override public
              KMaterialSpecularLabel
              cacheValueLoad(
                final @Nonnull KInstanceRegular instance)
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

    this.forward_opaque_unlit_cache =
      LRUCacheTrivial
        .newCache(
          new JCacheLoader<KInstanceOpaque, KMaterialForwardOpaqueUnlitLabel, ConstraintError>() {
            @Override public void cacheValueClose(
              final @Nonnull KMaterialForwardOpaqueUnlitLabel v)
              throws ConstraintError
            {
              // Nothing
            }

            @SuppressWarnings("synthetic-access") @Override public
              KMaterialForwardOpaqueUnlitLabel
              cacheValueLoad(
                final @Nonnull KInstanceOpaque instance)
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

    this.forward_translucent_regular_unlit_cache =
      LRUCacheTrivial
        .newCache(
          new JCacheLoader<KInstanceTranslucentRegular, KMaterialForwardTranslucentRegularUnlitLabel, ConstraintError>() {
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
                return KMaterialForwardTranslucentRegularUnlitLabel.newLabel(
                  albedo,
                  environment,
                  normal);
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

    this.forward_regular_cache =
      LRUCacheTrivial
        .newCache(
          new JCacheLoader<KInstanceRegular, KMaterialForwardRegularLabel, ConstraintError>() {
            @Override public void cacheValueClose(
              final @Nonnull KMaterialForwardRegularLabel v)
              throws ConstraintError
            {
              // Nothing
            }

            @SuppressWarnings("synthetic-access") @Override public
              KMaterialForwardRegularLabel
              cacheValueLoad(
                final @Nonnull KInstanceRegular instance)
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
                  KLabelDecider.this.specular_cache.cacheGetLU(instance);
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
          }, this.cache_config);

    this.light_cache =
      LRUCacheTrivial.newCache(
        new JCacheLoader<KLight, KLightLabel, ConstraintError>() {
          @Override public void cacheValueClose(
            final @Nonnull KLightLabel v)
            throws ConstraintError
          {
            // Nothing
          }

          @Override public KLightLabel cacheValueLoad(
            final @Nonnull KLight key)
            throws ConstraintError
          {
            return KLightLabel.fromLight(capabilities, key);
          }

          @Override public @Nonnull BigInteger cacheValueSizeOf(
            final @Nonnull KLightLabel v)
          {
            return BigInteger.ONE;
          }
        },
        this.cache_config);
  }

  @Override public @Nonnull KMaterialAlbedoLabel getAlbedoLabelRegular(
    final @Nonnull KInstanceRegular instance)
    throws ConstraintError
  {
    try {
      return this.albedo_cache.cacheGetLU(instance);
    } catch (final JCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public KMaterialDepthLabel getDepthLabel(
    final @Nonnull KInstanceOpaque instance)
    throws ConstraintError
  {
    try {
      return this.depth_cache.cacheGetLU(instance);
    } catch (final JCacheException e) {
      throw new UnreachableCodeException(e);
    }
  }

  @Override public @Nonnull KMaterialEmissiveLabel getEmissiveLabelRegular(
    final @Nonnull KInstanceRegular instance)
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
      final @Nonnull KInstanceRegular instance)
      throws ConstraintError
  {
    try {
      return this.environment_cache.cacheGetLU(instance);
    } catch (final JCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public KMaterialForwardOpaqueLitLabel getForwardLabelOpaqueLit(
    final @Nonnull KLight light,
    final @Nonnull KInstanceOpaque instance)
    throws ConstraintError
  {
    return KMaterialForwardOpaqueLitLabel.newLabel(
      this.getLightLabel(light),
      this.getForwardLabelRegular(instance));
  }

  @Override public
    KMaterialForwardOpaqueUnlitLabel
    getForwardLabelOpaqueUnlit(
      final @Nonnull KInstanceOpaque instance)
      throws ConstraintError
  {
    try {
      return this.forward_opaque_unlit_cache.cacheGetLU(instance);
    } catch (final JCacheException e) {
      throw new UnreachableCodeException(e);
    }
  }

  @Override public KMaterialForwardRegularLabel getForwardLabelRegular(
    final @Nonnull KInstanceRegular instance)
    throws ConstraintError
  {
    try {
      return this.forward_regular_cache.cacheGetLU(instance);
    } catch (final JCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public
    KMaterialForwardTranslucentRegularLitLabel
    getForwardLabelTranslucentRegularLit(
      final @Nonnull KLight light,
      final @Nonnull KInstanceTranslucentRegular instance)
      throws ConstraintError
  {
    return KMaterialForwardTranslucentRegularLitLabel.newLabel(
      this.getLightLabel(light),
      this.getForwardLabelRegular(instance));
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

  @Override public @Nonnull KLightLabel getLightLabel(
    final @Nonnull KLight light)
    throws ConstraintError
  {
    try {
      return this.light_cache.cacheGetLU(light);
    } catch (final JCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public @Nonnull KMaterialNormalLabel getNormalLabel(
    final @Nonnull KInstance instance)
    throws ConstraintError
  {
    try {
      return this.normal_cache.cacheGetLU(instance);
    } catch (final JCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }

  public @Nonnull BigInteger getSize()
  {
    return this.albedo_cache
      .cacheSize()
      .add(this.depth_cache.cacheSize())
      .add(this.emissive_cache.cacheSize())
      .add(this.environment_cache.cacheSize())
      .add(this.forward_translucent_regular_unlit_cache.cacheSize())
      .add(this.forward_opaque_unlit_cache.cacheSize())
      .add(this.forward_regular_cache.cacheSize())
      .add(this.normal_cache.cacheSize())
      .add(this.specular_cache.cacheSize())
      .add(this.light_cache.cacheSize());
  }

  @Override public @Nonnull KMaterialSpecularLabel getSpecularLabelRegular(
    final @Nonnull KInstanceRegular instance)
    throws ConstraintError
  {
    try {
      return this.specular_cache.cacheGetLU(instance);
    } catch (final JCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }
}
