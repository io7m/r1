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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jlucache.LRUCacheConfig;
import com.io7m.jlucache.LRUCacheTrivial;
import com.io7m.jlucache.LUCacheException;
import com.io7m.jlucache.LUCacheLoader;

final class KLabelDecider implements
  KLightLabelCache,
  KMaterialAlbedoLabelCache,
  KMaterialAlphaLabelCache,
  KMaterialDepthLabelCache,
  KMaterialEmissiveLabelCache,
  KMaterialEnvironmentLabelCache,
  KMaterialNormalLabelCache,
  KMaterialSpecularLabelCache,
  KMaterialForwardLabelCache,
  KMaterialShadowLabelCache
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
    final long cache_labels)
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
  private final @Nonnull LRUCacheTrivial<KMeshInstance, KMaterialNormalLabel, ConstraintError>      normal_cache;
  private final @Nonnull LRUCacheTrivial<ShadowCacheKey, KMaterialShadowLabel, ConstraintError>     shadow_cache;
  private final @Nonnull LRUCacheTrivial<KMeshInstance, KMaterialSpecularLabel, ConstraintError>    specular_cache;
  private final @Nonnull LRUCacheTrivial<KLight, KLightLabel, ConstraintError>                      light_cache;

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
          new LUCacheLoader<KMeshInstance, KMaterialAlbedoLabel, ConstraintError>() {
            @Override public void luCacheClose(
              final @Nonnull KMaterialAlbedoLabel v)
              throws ConstraintError
            {
              // Nothing
            }

            @Override public KMaterialAlbedoLabel luCacheLoadFrom(
              final @Nonnull KMeshInstance instance)
              throws ConstraintError
            {
              return KMaterialAlbedoLabel.fromInstance(instance);
            }

            @Override public long luCacheSizeOf(
              final @Nonnull KMaterialAlbedoLabel v)
            {
              return 1;
            }
          },
          this.cache_config);

    this.alpha_cache =
      LRUCacheTrivial
        .newCache(
          new LUCacheLoader<KMeshInstance, KMaterialAlphaLabel, ConstraintError>() {
            @Override public void luCacheClose(
              final @Nonnull KMaterialAlphaLabel v)
              throws ConstraintError
            {
              // Nothing
            }

            @Override public KMaterialAlphaLabel luCacheLoadFrom(
              final @Nonnull KMeshInstance instance)
              throws ConstraintError
            {
              return KMaterialAlphaLabel.fromMaterial(instance.getMaterial());
            }

            @Override public long luCacheSizeOf(
              final @Nonnull KMaterialAlphaLabel v)
            {
              return 1;
            }
          },
          this.cache_config);

    this.depth_cache =
      LRUCacheTrivial
        .newCache(
          new LUCacheLoader<KMeshInstance, KMaterialDepthLabel, ConstraintError>() {
            @Override public void luCacheClose(
              final @Nonnull KMaterialDepthLabel v)
              throws ConstraintError
            {
              // Nothing
            }

            @Override public KMaterialDepthLabel luCacheLoadFrom(
              final @Nonnull KMeshInstance instance)
              throws ConstraintError
            {
              try {
                final KMaterialAlbedoLabel albedo =
                  KLabelDecider.this.albedo_cache.luCacheGet(instance);
                return KMaterialDepthLabel.fromMaterial(
                  albedo,
                  instance.getMaterial());
              } catch (final LUCacheException e) {
                throw new UnreachableCodeException(e);
              }
            }

            @Override public long luCacheSizeOf(
              final @Nonnull KMaterialDepthLabel v)
            {
              return 1;
            }
          },
          this.cache_config);

    this.emissive_cache =
      LRUCacheTrivial
        .newCache(
          new LUCacheLoader<KMeshInstance, KMaterialEmissiveLabel, ConstraintError>() {
            @Override public void luCacheClose(
              final @Nonnull KMaterialEmissiveLabel v)
              throws ConstraintError
            {
              // Nothing
            }

            @Override public KMaterialEmissiveLabel luCacheLoadFrom(
              final @Nonnull KMeshInstance instance)
              throws ConstraintError
            {
              return KMaterialEmissiveLabel.fromInstance(instance);
            }

            @Override public long luCacheSizeOf(
              final @Nonnull KMaterialEmissiveLabel v)
            {
              return 1;
            }
          },
          this.cache_config);

    this.environment_cache =
      LRUCacheTrivial
        .newCache(
          new LUCacheLoader<KMeshInstance, KMaterialEnvironmentLabel, ConstraintError>() {
            @Override public void luCacheClose(
              final @Nonnull KMaterialEnvironmentLabel v)
              throws ConstraintError
            {
              // Nothing
            }

            @SuppressWarnings("synthetic-access") @Override public
              KMaterialEnvironmentLabel
              luCacheLoadFrom(
                final @Nonnull KMeshInstance instance)
                throws ConstraintError
            {
              try {
                return KMaterialEnvironmentLabel.fromInstance(
                  instance,
                  KLabelDecider.this.normal_cache.luCacheGet(instance));
              } catch (final LUCacheException x) {
                throw new UnreachableCodeException(x);
              }
            }

            @Override public long luCacheSizeOf(
              final @Nonnull KMaterialEnvironmentLabel v)
            {
              return 1;
            }
          }, this.cache_config);

    this.normal_cache =
      LRUCacheTrivial
        .newCache(
          new LUCacheLoader<KMeshInstance, KMaterialNormalLabel, ConstraintError>() {
            @Override public void luCacheClose(
              final @Nonnull KMaterialNormalLabel v)
              throws ConstraintError
            {
              // Nothing
            }

            @Override public KMaterialNormalLabel luCacheLoadFrom(
              final @Nonnull KMeshInstance instance)
              throws ConstraintError
            {
              return KMaterialNormalLabel.fromInstance(instance);
            }

            @Override public long luCacheSizeOf(
              final @Nonnull KMaterialNormalLabel v)
            {
              return 1;
            }
          },
          this.cache_config);

    this.specular_cache =
      LRUCacheTrivial
        .newCache(
          new LUCacheLoader<KMeshInstance, KMaterialSpecularLabel, ConstraintError>() {
            @Override public void luCacheClose(
              final @Nonnull KMaterialSpecularLabel v)
              throws ConstraintError
            {
              // Nothing
            }

            @SuppressWarnings("synthetic-access") @Override public
              KMaterialSpecularLabel
              luCacheLoadFrom(
                final @Nonnull KMeshInstance instance)
                throws ConstraintError
            {
              try {
                return KMaterialSpecularLabel.fromInstance(
                  instance,
                  KLabelDecider.this.normal_cache.luCacheGet(instance));
              } catch (final LUCacheException x) {
                throw new UnreachableCodeException(x);
              }
            }

            @Override public long luCacheSizeOf(
              final @Nonnull KMaterialSpecularLabel v)
            {
              return 1;
            }
          }, this.cache_config);

    this.forward_cache =
      LRUCacheTrivial
        .newCache(
          new LUCacheLoader<KMeshInstance, KMaterialForwardLabel, ConstraintError>() {
            @Override public void luCacheClose(
              final @Nonnull KMaterialForwardLabel v)
              throws ConstraintError
            {
              // Nothing
            }

            @SuppressWarnings("synthetic-access") @Override public
              KMaterialForwardLabel
              luCacheLoadFrom(
                final @Nonnull KMeshInstance instance)
                throws ConstraintError
            {
              try {
                final KMaterialAlphaLabel alpha =
                  KLabelDecider.this.alpha_cache.luCacheGet(instance);
                final KMaterialAlbedoLabel albedo =
                  KLabelDecider.this.albedo_cache.luCacheGet(instance);
                final KMaterialEmissiveLabel emissive =
                  KLabelDecider.this.emissive_cache.luCacheGet(instance);
                final KMaterialEnvironmentLabel environment =
                  KLabelDecider.this.environment_cache.luCacheGet(instance);
                final KMaterialNormalLabel normal =
                  KLabelDecider.this.normal_cache.luCacheGet(instance);
                final KMaterialSpecularLabel specular =
                  KLabelDecider.this.specular_cache.luCacheGet(instance);

                return new KMaterialForwardLabel(
                  alpha,
                  albedo,
                  emissive,
                  environment,
                  normal,
                  specular);
              } catch (final LUCacheException x) {
                throw new UnreachableCodeException(x);
              }
            }

            @Override public long luCacheSizeOf(
              final @Nonnull KMaterialForwardLabel v)
            {
              return 1;
            }
          }, this.cache_config);

    this.shadow_cache =
      LRUCacheTrivial
        .newCache(
          new LUCacheLoader<ShadowCacheKey, KMaterialShadowLabel, ConstraintError>() {
            @Override public void luCacheClose(
              final @Nonnull KMaterialShadowLabel v)
              throws ConstraintError
            {
              // Nothing
            }

            @SuppressWarnings("synthetic-access") @Override public
              KMaterialShadowLabel
              luCacheLoadFrom(
                final @Nonnull ShadowCacheKey key)
                throws ConstraintError
            {
              try {
                final KMaterialDepthLabel caster =
                  KLabelDecider.this.depth_cache.luCacheGet(key.instance);

                return KMaterialShadowLabel.fromShadow(
                  KLabelDecider.this.capabilities,
                  caster,
                  key.shadow);
              } catch (final LUCacheException x) {
                throw new UnreachableCodeException(x);
              }
            }

            @Override public long luCacheSizeOf(
              final @Nonnull KMaterialShadowLabel v)
            {
              return 1;
            }
          }, this.cache_config);

    this.light_cache =
      LRUCacheTrivial.newCache(
        new LUCacheLoader<KLight, KLightLabel, ConstraintError>() {
          @Override public void luCacheClose(
            final @Nonnull KLightLabel v)
            throws ConstraintError
          {
            // Nothing
          }

          @Override public KLightLabel luCacheLoadFrom(
            final @Nonnull KLight key)
            throws ConstraintError
          {
            return KLightLabel.fromLight(capabilities, key);
          }

          @Override public long luCacheSizeOf(
            final @Nonnull KLightLabel v)
          {
            return 1;
          }
        },
        this.cache_config);
  }

  @Override public @Nonnull KMaterialAlbedoLabel getAlbedoLabel(
    final @Nonnull KMeshInstance instance)
    throws ConstraintError
  {
    try {
      return this.albedo_cache.luCacheGet(instance);
    } catch (final LUCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public @Nonnull KMaterialAlphaLabel getAlphaLabel(
    final @Nonnull KMeshInstance instance)
    throws ConstraintError
  {
    try {
      return this.alpha_cache.luCacheGet(instance);
    } catch (final LUCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public @Nonnull KMaterialEmissiveLabel getEmissiveLabel(
    final @Nonnull KMeshInstance instance)
    throws ConstraintError
  {
    try {
      return this.emissive_cache.luCacheGet(instance);
    } catch (final LUCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public @Nonnull KMaterialEnvironmentLabel getEnvironmentLabel(
    final @Nonnull KMeshInstance instance)
    throws ConstraintError
  {
    try {
      return this.environment_cache.luCacheGet(instance);
    } catch (final LUCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public @Nonnull KMaterialForwardLabel getForwardLabel(
    final @Nonnull KMeshInstance instance)
    throws ConstraintError
  {
    try {
      return this.forward_cache.luCacheGet(instance);
    } catch (final LUCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public @Nonnull KMaterialNormalLabel getNormalLabel(
    final @Nonnull KMeshInstance instance)
    throws ConstraintError
  {
    try {
      return this.normal_cache.luCacheGet(instance);
    } catch (final LUCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public @Nonnull KMaterialShadowLabel getShadowLabel(
    final @Nonnull KMeshInstance instance,
    final @Nonnull KShadow shadow)
    throws ConstraintError
  {
    try {
      return this.shadow_cache
        .luCacheGet(new ShadowCacheKey(instance, shadow));
    } catch (final LUCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public @Nonnull KMaterialSpecularLabel getSpecularLabel(
    final @Nonnull KMeshInstance instance)
    throws ConstraintError
  {
    try {
      return this.specular_cache.luCacheGet(instance);
    } catch (final LUCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public @Nonnull KLightLabel getLightLabel(
    final @Nonnull KLight light)
    throws ConstraintError
  {
    try {
      return this.light_cache.luCacheGet(light);
    } catch (final LUCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public @Nonnull KMaterialDepthLabel getDepthLabel(
    final @Nonnull KMeshInstance instance)
    throws ConstraintError
  {
    try {
      return this.depth_cache.luCacheGet(instance);
    } catch (final LUCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }

}
