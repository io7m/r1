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

package com.io7m.r1.examples;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nu.xom.ParsingException;
import nu.xom.ValidityException;

import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jcanephora.TextureCubeStaticUsableType;
import com.io7m.r1.examples.tools.EMeshCache;
import com.io7m.r1.examples.tools.ETexture2DCache;
import com.io7m.r1.examples.tools.ETextureCubeCache;
import com.io7m.r1.kernel.types.KCamera;
import com.io7m.r1.kernel.types.KInstanceOpaqueType;
import com.io7m.r1.kernel.types.KInstanceTranslucentLitType;
import com.io7m.r1.kernel.types.KInstanceTranslucentUnlitType;
import com.io7m.r1.kernel.types.KInstanceType;
import com.io7m.r1.kernel.types.KLightTranslucentType;
import com.io7m.r1.kernel.types.KLightType;
import com.io7m.r1.kernel.types.KLightWithShadowType;
import com.io7m.r1.kernel.types.KMeshReadableType;
import com.io7m.r1.kernel.types.KSceneBuilderWithCreateType;
import com.io7m.r1.kernel.types.KSceneLightGroupBuilderType;
import com.io7m.r1.kernel.types.KTranslucentType;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RExceptionIO;
import com.io7m.r1.types.RExceptionInstanceAlreadyLit;
import com.io7m.r1.types.RExceptionJCGL;
import com.io7m.r1.types.RExceptionLightGroupAlreadyAdded;
import com.io7m.r1.types.RXMLException;

/**
 * The default implementation of an example scene builder.
 */

public final class ExampleSceneBuilder implements ExampleSceneBuilderType
{
  private final ETextureCubeCache           cube_cache;
  private final EMeshCache                  mesh_cache;
  private final KSceneBuilderWithCreateType scene_builder;
  private final ETexture2DCache             texture2d_cache;

  /**
   * Construct an example scene builder.
   *
   * @param in_scene_builder
   *          The base scene builder.
   * @param in_cube_cache
   *          A cube map cache.
   * @param in_mesh_cache
   *          A mesh cache.
   * @param in_texture2d_cache
   *          A texture cache.
   */

  public ExampleSceneBuilder(
    final KSceneBuilderWithCreateType in_scene_builder,
    final ETextureCubeCache in_cube_cache,
    final EMeshCache in_mesh_cache,
    final ETexture2DCache in_texture2d_cache)
  {
    this.scene_builder = in_scene_builder;
    this.cube_cache = in_cube_cache;
    this.mesh_cache = in_mesh_cache;
    this.texture2d_cache = in_texture2d_cache;
  }

  @Override public TextureCubeStaticUsableType cubeTexture(
    final String name)
    throws RException
  {
    try {
      final ETextureCubeCache cc = this.cube_cache;
      assert cc != null;
      return cc.loadCube(name);
    } catch (final ValidityException e) {
      throw RXMLException.validityException(e);
    } catch (final IOException e) {
      throw RExceptionIO.fromIOException(e);
    } catch (final JCGLException e) {
      throw RExceptionJCGL.fromJCGLException(e);
    } catch (final ParsingException e) {
      throw RXMLException.parsingException(e);
    }
  }

  @Override public KMeshReadableType mesh(
    final String name)
    throws RException
  {
    final EMeshCache mc = this.mesh_cache;
    assert mc != null;
    return mc.loadMesh(name);
  }

  @Override public void sceneAddOpaqueUnlit(
    final KInstanceOpaqueType instance)
    throws RExceptionInstanceAlreadyLit
  {
    this.scene_builder.sceneAddOpaqueUnlit(instance);
  }

  @Override public void sceneAddShadowCaster(
    final KLightWithShadowType light,
    final KInstanceOpaqueType instance)
  {
    this.scene_builder.sceneAddShadowCaster(light, instance);
  }

  @Override public void sceneAddTranslucentLit(
    final KInstanceTranslucentLitType instance,
    final Set<KLightTranslucentType> lights)
  {
    this.scene_builder.sceneAddTranslucentLit(instance, lights);
  }

  @Override public void sceneAddTranslucentUnlit(
    final KInstanceTranslucentUnlitType instance)
  {
    this.scene_builder.sceneAddTranslucentUnlit(instance);
  }

  @Override public KCamera sceneGetCamera()
  {
    return this.scene_builder.sceneGetCamera();
  }

  @Override public Set<KInstanceType> sceneGetInstances()
  {
    return this.scene_builder.sceneGetInstances();
  }

  @Override public
    Set<KInstanceOpaqueType>
    sceneGetInstancesOpaqueLitVisible()
  {
    return this.scene_builder.sceneGetInstancesOpaqueLitVisible();
  }

  @Override public
    Map<KLightWithShadowType, Set<KInstanceOpaqueType>>
    sceneGetInstancesOpaqueShadowCastingByLight()
  {
    return this.scene_builder.sceneGetInstancesOpaqueShadowCastingByLight();
  }

  @Override public Set<KInstanceOpaqueType> sceneGetInstancesOpaqueUnlit()
  {
    return this.scene_builder.sceneGetInstancesOpaqueUnlit();
  }

  @Override public Set<KLightType> sceneGetLights()
  {
    return this.scene_builder.sceneGetLights();
  }

  @Override public Set<KLightWithShadowType> sceneGetLightsShadowCasting()
  {
    return this.scene_builder.sceneGetLightsShadowCasting();
  }

  @Override public List<KTranslucentType> sceneGetTranslucents()
  {
    return this.scene_builder.sceneGetTranslucents();
  }

  @Override public KSceneLightGroupBuilderType sceneNewLightGroup(
    final String name)
    throws RExceptionLightGroupAlreadyAdded
  {
    return this.scene_builder.sceneNewLightGroup(name);
  }

  @Override public Texture2DStaticUsableType texture(
    final String name)
    throws RException
  {
    try {
      final ETexture2DCache tc = this.texture2d_cache;
      assert tc != null;
      return tc.loadTexture(name);
    } catch (final IOException e) {
      throw RExceptionIO.fromIOException(e);
    } catch (final JCGLException e) {
      throw RExceptionJCGL.fromJCGLException(e);
    }
  }

  @Override public Texture2DStaticUsableType textureClamped(
    final String name)
    throws RException
  {
    try {
      final ETexture2DCache tc = this.texture2d_cache;
      assert tc != null;
      return tc.loadTextureClamped(name);
    } catch (final IOException e) {
      throw RExceptionIO.fromIOException(e);
    } catch (final JCGLException e) {
      throw RExceptionJCGL.fromJCGLException(e);
    }
  }
}
