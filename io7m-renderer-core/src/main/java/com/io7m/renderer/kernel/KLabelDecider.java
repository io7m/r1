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
import javax.annotation.concurrent.Immutable;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jcache.JCacheException;
import com.io7m.jcache.JCacheLoader;
import com.io7m.jcache.LRUCacheConfig;
import com.io7m.jcache.LRUCacheTrivial;

final class KLabelDecider implements
  KLightLabelCache,
  KMaterialAlbedoLabelCache,
  KMaterialAlphaLabelCache,
  KMaterialDepthLabelCache,
  KMaterialEmissiveLabelCache,
  KMaterialEnvironmentLabelCache,
  KMaterialNormalLabelCache,
  KMaterialSpecularLabelCache,
  KMaterialForwardLabelCache
{
  @Immutable private static final class ShadowCacheKey
  {
    private final @Nonnull KMeshInstance instance;
    private final @Nonnull KShadow       shadow;

    public ShadowCacheKey(
      final @Nonnull KMeshInstance instance,
      final @Nonnull KShadow shadow)
      throws ConstraintError
    {
      this.instance = Constraints.constrainNotNull(instance, "Instance");
      this.shadow = Constraints.constrainNotNull(shadow, "Shadow");
    }

    @Override public boolean equals(
      final Object obj)
    {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (this.getClass() != obj.getClass()) {
        return false;
      }
      final ShadowCacheKey other = (ShadowCacheKey) obj;
      if (!this.instance.equals(other.instance)) {
        return false;
      }
      if (!this.shadow.equals(other.shadow)) {
        return false;
      }
      return true;
    }

    @Override public int hashCode()
    {
      final int prime = 31;
      int result = 1;
      result = (prime * result) + this.instance.hashCode();
      result = (prime * result) + this.shadow.hashCode();
      return result;
    }
  }

  public static @Nonnull KLabelDecider newDecider(
    final @Nonnull KGraphicsCapabilities capabilities,
    final @Nonnull BigInteger cache_labels)
    throws ConstraintError
  {
    final LRUCacheConfig config =
      LRUCacheConfig.empty().withMaximumCapacity(cache_labels);
    return new KLabelDecider(capabilities, config);
  }

  private final @Nonnull LRUCacheTrivial<KMeshInstance, KMaterialAlbedoLabel, ConstraintError>      albedo_cache;
  private final @Nonnull LRUCacheTrivial<KMeshInstance, KMaterialAlphaLabel, ConstraintError>       alpha_cache;
  private final @Nonnull LRUCacheConfig                                                             cache_config;
  private final @Nonnull KGraphicsCapabilities                                                      capabilities;
  private final @Nonnull LRUCacheTrivial<KMeshInstance, KMaterialDepthLabel, ConstraintError>       depth_cache;
  private final @Nonnull LRUCacheTrivial<KMeshInstance, KMaterialEmissiveLabel, ConstraintError>    emissive_cache;
  private final @Nonnull LRUCacheTrivial<KMeshInstance, KMaterialEnvironmentLabel, ConstraintError> environment_cache;
  private final @Nonnull LRUCacheTrivial<KMeshInstance, KMaterialForwardLabel, ConstraintError>     forward_cache;
  private final @Nonnull LRUCacheTrivial<KLight, KLightLabel, ConstraintError>                      light_cache;
  private final @Nonnull LRUCacheTrivial<KMeshInstance, KMaterialNormalLabel, ConstraintError>      normal_cache;
  private final @Nonnull LRUCacheTrivial<KMeshInstance, KMaterialSpecularLabel, ConstraintError>    specular_cache;

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
          new JCacheLoader<KMeshInstance, KMaterialAlbedoLabel, ConstraintError>() {
            @Override public void cacheValueClose(
              final @Nonnull KMaterialAlbedoLabel v)
              throws ConstraintError
            {
              // Nothing
            }

            @Override public KMaterialAlbedoLabel cacheValueLoad(
              final @Nonnull KMeshInstance instance)
              throws ConstraintError
            {
              return KMaterialAlbedoLabel.fromInstance(instance);
            }

            @Override public @Nonnull BigInteger cacheValueSizeOf(
              final @Nonnull KMaterialAlbedoLabel v)
            {
              return BigInteger.ONE;
            }
          },
          this.cache_config);

    this.alpha_cache =
      LRUCacheTrivial
        .newCache(
          new JCacheLoader<KMeshInstance, KMaterialAlphaLabel, ConstraintError>() {
            @Override public void cacheValueClose(
              final @Nonnull KMaterialAlphaLabel v)
              throws ConstraintError
            {
              // Nothing
            }

            @Override public KMaterialAlphaLabel cacheValueLoad(
              final @Nonnull KMeshInstance instance)
              throws ConstraintError
            {
              return KMaterialAlphaLabel.fromMaterial(instance.getMaterial());
            }

            @Override public @Nonnull BigInteger cacheValueSizeOf(
              final @Nonnull KMaterialAlphaLabel v)
            {
              return BigInteger.ONE;
            }
          },
          this.cache_config);

    this.depth_cache =
      LRUCacheTrivial
        .newCache(
          new JCacheLoader<KMeshInstance, KMaterialDepthLabel, ConstraintError>() {
            @Override public void cacheValueClose(
              final @Nonnull KMaterialDepthLabel v)
              throws ConstraintError
            {
              // Nothing
            }

            @Override public KMaterialDepthLabel cacheValueLoad(
              final @Nonnull KMeshInstance instance)
              throws ConstraintError
            {
              try {
                final KMaterialAlbedoLabel albedo =
                  KLabelDecider.this.albedo_cache.cacheGetLU(instance);
                return KMaterialDepthLabel.fromMaterial(
                  albedo,
                  instance.getMaterial());
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

    this.emissive_cache =
      LRUCacheTrivial
        .newCache(
          new JCacheLoader<KMeshInstance, KMaterialEmissiveLabel, ConstraintError>() {
            @Override public void cacheValueClose(
              final @Nonnull KMaterialEmissiveLabel v)
              throws ConstraintError
            {
              // Nothing
            }

            @Override public KMaterialEmissiveLabel cacheValueLoad(
              final @Nonnull KMeshInstance instance)
              throws ConstraintError
            {
              return KMaterialEmissiveLabel.fromInstance(instance);
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
          new JCacheLoader<KMeshInstance, KMaterialEnvironmentLabel, ConstraintError>() {
            @Override public void cacheValueClose(
              final @Nonnull KMaterialEnvironmentLabel v)
              throws ConstraintError
            {
              // Nothing
            }

            @SuppressWarnings("synthetic-access") @Override public
              KMaterialEnvironmentLabel
              cacheValueLoad(
                final @Nonnull KMeshInstance instance)
                throws ConstraintError
            {
              try {
                return KMaterialEnvironmentLabel.fromInstance(
                  instance,
                  KLabelDecider.this.normal_cache.cacheGetLU(instance));
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
      LRUCacheTrivial
        .newCache(
          new JCacheLoader<KMeshInstance, KMaterialNormalLabel, ConstraintError>() {
            @Override public void cacheValueClose(
              final @Nonnull KMaterialNormalLabel v)
              throws ConstraintError
            {
              // Nothing
            }

            @Override public KMaterialNormalLabel cacheValueLoad(
              final @Nonnull KMeshInstance instance)
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
          new JCacheLoader<KMeshInstance, KMaterialSpecularLabel, ConstraintError>() {
            @Override public void cacheValueClose(
              final @Nonnull KMaterialSpecularLabel v)
              throws ConstraintError
            {
              // Nothing
            }

            @SuppressWarnings("synthetic-access") @Override public
              KMaterialSpecularLabel
              cacheValueLoad(
                final @Nonnull KMeshInstance instance)
                throws ConstraintError
            {
              try {
                return KMaterialSpecularLabel.fromInstance(
                  instance,
                  KLabelDecider.this.normal_cache.cacheGetLU(instance));
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

    this.forward_cache =
      LRUCacheTrivial
        .newCache(
          new JCacheLoader<KMeshInstance, KMaterialForwardLabel, ConstraintError>() {
            @Override public void cacheValueClose(
              final @Nonnull KMaterialForwardLabel v)
              throws ConstraintError
            {
              // Nothing
            }

            @SuppressWarnings("synthetic-access") @Override public
              KMaterialForwardLabel
              cacheValueLoad(
                final @Nonnull KMeshInstance instance)
                throws ConstraintError
            {
              try {
                final KMaterialAlphaLabel alpha =
                  KLabelDecider.this.alpha_cache.cacheGetLU(instance);
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

                return new KMaterialForwardLabel(
                  alpha,
                  albedo,
                  emissive,
                  environment,
                  normal,
                  specular);
              } catch (final JCacheException x) {
                throw new UnreachableCodeException(x);
              }
            }

            @Override public @Nonnull BigInteger cacheValueSizeOf(
              final @Nonnull KMaterialForwardLabel v)
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

  @Override public @Nonnull KMaterialAlbedoLabel getAlbedoLabel(
    final @Nonnull KMeshInstance instance)
    throws ConstraintError
  {
    try {
      return this.albedo_cache.cacheGetLU(instance);
    } catch (final JCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public @Nonnull KMaterialAlphaLabel getAlphaLabel(
    final @Nonnull KMeshInstance instance)
    throws ConstraintError
  {
    try {
      return this.alpha_cache.cacheGetLU(instance);
    } catch (final JCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public @Nonnull KMaterialDepthLabel getDepthLabel(
    final @Nonnull KMeshInstance instance)
    throws ConstraintError
  {
    try {
      return this.depth_cache.cacheGetLU(instance);
    } catch (final JCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public @Nonnull KMaterialEmissiveLabel getEmissiveLabel(
    final @Nonnull KMeshInstance instance)
    throws ConstraintError
  {
    try {
      return this.emissive_cache.cacheGetLU(instance);
    } catch (final JCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public @Nonnull KMaterialEnvironmentLabel getEnvironmentLabel(
    final @Nonnull KMeshInstance instance)
    throws ConstraintError
  {
    try {
      return this.environment_cache.cacheGetLU(instance);
    } catch (final JCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public @Nonnull KMaterialForwardLabel getForwardLabel(
    final @Nonnull KMeshInstance instance)
    throws ConstraintError
  {
    try {
      return this.forward_cache.cacheGetLU(instance);
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
    final @Nonnull KMeshInstance instance)
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
      .add(this.alpha_cache.cacheSize())
      .add(this.depth_cache.cacheSize())
      .add(this.emissive_cache.cacheSize())
      .add(this.environment_cache.cacheSize())
      .add(this.forward_cache.cacheSize())
      .add(this.normal_cache.cacheSize())
      .add(this.specular_cache.cacheSize())
      .add(this.light_cache.cacheSize());
  }

  @Override public @Nonnull KMaterialSpecularLabel getSpecularLabel(
    final @Nonnull KMeshInstance instance)
    throws ConstraintError
  {
    try {
      return this.specular_cache.cacheGetLU(instance);
    } catch (final JCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }
}
