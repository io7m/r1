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
import com.io7m.renderer.kernel.types.KInstanceTranslucentRegular;
import com.io7m.renderer.kernel.types.KLight;
import com.io7m.renderer.kernel.types.KLightLabel;
import com.io7m.renderer.kernel.types.KMaterialAlbedoLabel;
import com.io7m.renderer.kernel.types.KMaterialDepthLabel;
import com.io7m.renderer.kernel.types.KMaterialEmissiveLabel;
import com.io7m.renderer.kernel.types.KMaterialEnvironmentLabel;
import com.io7m.renderer.kernel.types.KMaterialForwardRegularLabel;
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
  KMaterialForwardOpaqueLabelCache
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

  private final @Nonnull LRUCacheTrivial<KInstanceOpaque, KMaterialAlbedoLabel, ConstraintError>                  albedo_opaque_cache;
  private final @Nonnull LRUCacheTrivial<KInstanceTranslucentRegular, KMaterialAlbedoLabel, ConstraintError>      albedo_translucent_regular_cache;
  private final @Nonnull LRUCacheConfig                                                                           cache_config;
  private final @Nonnull KGraphicsCapabilities                                                                    capabilities;
  private final @Nonnull LRUCacheTrivial<KInstanceOpaque, KMaterialDepthLabel, ConstraintError>                   depth_cache;
  private final @Nonnull LRUCacheTrivial<KInstanceOpaque, KMaterialEmissiveLabel, ConstraintError>                emissive_opaque_cache;
  private final @Nonnull LRUCacheTrivial<KInstanceTranslucentRegular, KMaterialEmissiveLabel, ConstraintError>    emissive_translucent_regular_cache;
  private final @Nonnull LRUCacheTrivial<KInstanceOpaque, KMaterialEnvironmentLabel, ConstraintError>             environment_opaque_cache;
  private final @Nonnull LRUCacheTrivial<KInstanceTranslucentRegular, KMaterialEnvironmentLabel, ConstraintError> environment_translucent_regular_cache;
  private final @Nonnull LRUCacheTrivial<KInstanceOpaque, KMaterialForwardRegularLabel, ConstraintError>           forward_opaque_cache;
  private final @Nonnull LRUCacheTrivial<KLight, KLightLabel, ConstraintError>                                    light_cache;
  private final @Nonnull LRUCacheTrivial<KInstance, KMaterialNormalLabel, ConstraintError>                        normal_cache;
  private final @Nonnull LRUCacheTrivial<KInstanceOpaque, KMaterialSpecularLabel, ConstraintError>                specular_opaque_cache;
  private final @Nonnull LRUCacheTrivial<KInstanceTranslucentRegular, KMaterialSpecularLabel, ConstraintError>    specular_translucent_regular_cache;

  private KLabelDecider(
    final @Nonnull KGraphicsCapabilities capabilities,
    final @Nonnull LRUCacheConfig cache_config)
    throws ConstraintError
  {
    this.capabilities =
      Constraints.constrainNotNull(capabilities, "Capabilities");
    this.cache_config =
      Constraints.constrainNotNull(cache_config, "Cache config");

    this.albedo_opaque_cache =
      LRUCacheTrivial
        .newCache(
          new JCacheLoader<KInstanceOpaque, KMaterialAlbedoLabel, ConstraintError>() {
            @Override public void cacheValueClose(
              final @Nonnull KMaterialAlbedoLabel v)
              throws ConstraintError
            {
              // Nothing
            }

            @Override public KMaterialAlbedoLabel cacheValueLoad(
              final @Nonnull KInstanceOpaque instance)
              throws ConstraintError
            {
              return KMaterialAlbedoLabel.fromInstanceOpaque(instance);
            }

            @Override public @Nonnull BigInteger cacheValueSizeOf(
              final @Nonnull KMaterialAlbedoLabel v)
            {
              return BigInteger.ONE;
            }
          },
          this.cache_config);

    this.albedo_translucent_regular_cache =
      LRUCacheTrivial
        .newCache(
          new JCacheLoader<KInstanceTranslucentRegular, KMaterialAlbedoLabel, ConstraintError>() {
            @Override public void cacheValueClose(
              final @Nonnull KMaterialAlbedoLabel v)
              throws ConstraintError
            {
              // Nothing
            }

            @Override public KMaterialAlbedoLabel cacheValueLoad(
              final @Nonnull KInstanceTranslucentRegular instance)
              throws ConstraintError
            {
              return KMaterialAlbedoLabel
                .fromInstanceTranslucentRegular(instance);
            }

            @Override public @Nonnull BigInteger cacheValueSizeOf(
              final @Nonnull KMaterialAlbedoLabel v)
            {
              return BigInteger.ONE;
            }
          }, this.cache_config);

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
                  KLabelDecider.this.albedo_opaque_cache.cacheGetLU(instance);
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
          },
          this.cache_config);

    this.emissive_opaque_cache =
      LRUCacheTrivial
        .newCache(
          new JCacheLoader<KInstanceOpaque, KMaterialEmissiveLabel, ConstraintError>() {
            @Override public void cacheValueClose(
              final @Nonnull KMaterialEmissiveLabel v)
              throws ConstraintError
            {
              // Nothing
            }

            @Override public KMaterialEmissiveLabel cacheValueLoad(
              final @Nonnull KInstanceOpaque instance)
              throws ConstraintError
            {
              return KMaterialEmissiveLabel.fromInstanceOpaque(instance);
            }

            @Override public @Nonnull BigInteger cacheValueSizeOf(
              final @Nonnull KMaterialEmissiveLabel v)
            {
              return BigInteger.ONE;
            }
          },
          this.cache_config);

    this.emissive_translucent_regular_cache =
      LRUCacheTrivial
        .newCache(
          new JCacheLoader<KInstanceTranslucentRegular, KMaterialEmissiveLabel, ConstraintError>() {
            @Override public void cacheValueClose(
              final @Nonnull KMaterialEmissiveLabel v)
              throws ConstraintError
            {
              // Nothing
            }

            @Override public KMaterialEmissiveLabel cacheValueLoad(
              final @Nonnull KInstanceTranslucentRegular instance)
              throws ConstraintError
            {
              return KMaterialEmissiveLabel
                .fromInstanceTranslucentRegular(instance);
            }

            @Override public @Nonnull BigInteger cacheValueSizeOf(
              final @Nonnull KMaterialEmissiveLabel v)
            {
              return BigInteger.ONE;
            }
          }, this.cache_config);

    this.environment_opaque_cache =
      LRUCacheTrivial
        .newCache(
          new JCacheLoader<KInstanceOpaque, KMaterialEnvironmentLabel, ConstraintError>() {
            @Override public void cacheValueClose(
              final @Nonnull KMaterialEnvironmentLabel v)
              throws ConstraintError
            {
              // Nothing
            }

            @SuppressWarnings("synthetic-access") @Override public
              KMaterialEnvironmentLabel
              cacheValueLoad(
                final @Nonnull KInstanceOpaque instance)
                throws ConstraintError
            {
              try {
                return KMaterialEnvironmentLabel.fromInstanceOpaque(
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

    this.environment_translucent_regular_cache =
      LRUCacheTrivial
        .newCache(
          new JCacheLoader<KInstanceTranslucentRegular, KMaterialEnvironmentLabel, ConstraintError>() {
            @Override public void cacheValueClose(
              final @Nonnull KMaterialEnvironmentLabel v)
              throws ConstraintError
            {
              // Nothing
            }

            @SuppressWarnings("synthetic-access") @Override public
              KMaterialEnvironmentLabel
              cacheValueLoad(
                final @Nonnull KInstanceTranslucentRegular instance)
                throws ConstraintError
            {
              try {
                return KMaterialEnvironmentLabel
                  .fromInstanceTranslucentRegular(
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

    this.specular_opaque_cache =
      LRUCacheTrivial
        .newCache(
          new JCacheLoader<KInstanceOpaque, KMaterialSpecularLabel, ConstraintError>() {
            @Override public void cacheValueClose(
              final @Nonnull KMaterialSpecularLabel v)
              throws ConstraintError
            {
              // Nothing
            }

            @SuppressWarnings("synthetic-access") @Override public
              KMaterialSpecularLabel
              cacheValueLoad(
                final @Nonnull KInstanceOpaque instance)
                throws ConstraintError
            {
              try {
                return KMaterialSpecularLabel.fromInstanceOpaque(
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

    this.specular_translucent_regular_cache =
      LRUCacheTrivial
        .newCache(
          new JCacheLoader<KInstanceTranslucentRegular, KMaterialSpecularLabel, ConstraintError>() {
            @Override public void cacheValueClose(
              final @Nonnull KMaterialSpecularLabel v)
              throws ConstraintError
            {
              // Nothing
            }

            @SuppressWarnings("synthetic-access") @Override public
              KMaterialSpecularLabel
              cacheValueLoad(
                final @Nonnull KInstanceTranslucentRegular instance)
                throws ConstraintError
            {
              try {
                return KMaterialSpecularLabel.fromInstanceTranslucentRegular(
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

    this.forward_opaque_cache =
      LRUCacheTrivial
        .newCache(
          new JCacheLoader<KInstanceOpaque, KMaterialForwardRegularLabel, ConstraintError>() {
            @Override public void cacheValueClose(
              final @Nonnull KMaterialForwardRegularLabel v)
              throws ConstraintError
            {
              // Nothing
            }

            @SuppressWarnings("synthetic-access") @Override public
              KMaterialForwardRegularLabel
              cacheValueLoad(
                final @Nonnull KInstanceOpaque instance)
                throws ConstraintError
            {
              try {
                final KMaterialAlbedoLabel albedo =
                  KLabelDecider.this.albedo_opaque_cache.cacheGetLU(instance);
                final KMaterialEmissiveLabel emissive =
                  KLabelDecider.this.emissive_opaque_cache
                    .cacheGetLU(instance);
                final KMaterialEnvironmentLabel environment =
                  KLabelDecider.this.environment_opaque_cache
                    .cacheGetLU(instance);
                final KMaterialNormalLabel normal =
                  KLabelDecider.this.normal_cache.cacheGetLU(instance);
                final KMaterialSpecularLabel specular =
                  KLabelDecider.this.specular_opaque_cache
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

  @Override public @Nonnull KMaterialAlbedoLabel getAlbedoLabelOpaque(
    final @Nonnull KInstanceOpaque instance)
    throws ConstraintError
  {
    try {
      return this.albedo_opaque_cache.cacheGetLU(instance);
    } catch (final JCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public @Nonnull
    KMaterialAlbedoLabel
    getAlbedoLabelTranslucentRegular(
      final @Nonnull KInstanceTranslucentRegular instance)
      throws ConstraintError
  {
    try {
      return this.albedo_translucent_regular_cache.cacheGetLU(instance);
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

  @Override public @Nonnull KMaterialEmissiveLabel getEmissiveLabelOpaque(
    final @Nonnull KInstanceOpaque instance)
    throws ConstraintError
  {
    try {
      return this.emissive_opaque_cache.cacheGetLU(instance);
    } catch (final JCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public @Nonnull
    KMaterialEmissiveLabel
    getEmissiveLabelTranslucentRegular(
      final @Nonnull KInstanceTranslucentRegular instance)
      throws ConstraintError
  {
    try {
      return this.emissive_translucent_regular_cache.cacheGetLU(instance);
    } catch (final JCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public @Nonnull
    KMaterialEnvironmentLabel
    getEnvironmentLabelOpaque(
      final @Nonnull KInstanceOpaque instance)
      throws ConstraintError
  {
    try {
      return this.environment_opaque_cache.cacheGetLU(instance);
    } catch (final JCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public @Nonnull
    KMaterialEnvironmentLabel
    getEnvironmentLabelTranslucentRegular(
      final @Nonnull KInstanceTranslucentRegular instance)
      throws ConstraintError
  {
    try {
      return this.environment_translucent_regular_cache.cacheGetLU(instance);
    } catch (final JCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public KMaterialForwardRegularLabel getForwardLabelOpaque(
    final @Nonnull KInstanceOpaque instance)
    throws ConstraintError
  {
    try {
      return this.forward_opaque_cache.cacheGetLU(instance);
    } catch (final JCacheException x) {
      throw new UnreachableCodeException(x);
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
    return this.albedo_opaque_cache
      .cacheSize()
      .add(this.albedo_translucent_regular_cache.cacheSize())
      .add(this.depth_cache.cacheSize())
      .add(this.emissive_opaque_cache.cacheSize())
      .add(this.emissive_translucent_regular_cache.cacheSize())
      .add(this.environment_opaque_cache.cacheSize())
      .add(this.environment_translucent_regular_cache.cacheSize())
      .add(this.forward_opaque_cache.cacheSize())
      .add(this.normal_cache.cacheSize())
      .add(this.specular_opaque_cache.cacheSize())
      .add(this.specular_translucent_regular_cache.cacheSize())
      .add(this.light_cache.cacheSize());
  }

  @Override public @Nonnull KMaterialSpecularLabel getSpecularLabelOpaque(
    final @Nonnull KInstanceOpaque instance)
    throws ConstraintError
  {
    try {
      return this.specular_opaque_cache.cacheGetLU(instance);
    } catch (final JCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public @Nonnull
    KMaterialSpecularLabel
    getSpecularLabelTranslucentRegular(
      final @Nonnull KInstanceTranslucentRegular instance)
      throws ConstraintError
  {
    try {
      return this.specular_translucent_regular_cache.cacheGetLU(instance);
    } catch (final JCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }
}
