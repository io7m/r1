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
import java.util.Set;

import nu.xom.ParsingException;
import nu.xom.ValidityException;

import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jcanephora.TextureCubeStaticUsableType;
import com.io7m.r1.examples.tools.EMeshCache;
import com.io7m.r1.examples.tools.ETexture2DCache;
import com.io7m.r1.examples.tools.ETextureCubeCache;
import com.io7m.r1.kernel.types.KInstanceOpaqueType;
import com.io7m.r1.kernel.types.KInstanceTranslucentLitType;
import com.io7m.r1.kernel.types.KInstanceTranslucentUnlitType;
import com.io7m.r1.kernel.types.KLightTranslucentType;
import com.io7m.r1.kernel.types.KLightWithShadowType;
import com.io7m.r1.kernel.types.KMeshReadableType;
import com.io7m.r1.kernel.types.KVisibleSetBuilderWithCreateType;
import com.io7m.r1.kernel.types.KVisibleSetLightGroupBuilderType;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RExceptionBuilderInvalid;
import com.io7m.r1.types.RExceptionIO;
import com.io7m.r1.types.RExceptionInstanceAlreadyVisible;
import com.io7m.r1.types.RExceptionJCGL;
import com.io7m.r1.types.RExceptionLightGroupAlreadyAdded;
import com.io7m.r1.types.RXMLException;

/**
 * The default implementation of an example scene builder.
 */

public final class ExampleSceneBuilder implements ExampleSceneBuilderType
{
  private final ETextureCubeCache                cube_cache;
  private final EMeshCache                       mesh_cache;
  private final KVisibleSetBuilderWithCreateType scene_builder;
  private final ETexture2DCache                  texture2d_cache;

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
    final KVisibleSetBuilderWithCreateType in_scene_builder,
    final ETextureCubeCache in_cube_cache,
    final EMeshCache in_mesh_cache,
    final ETexture2DCache in_texture2d_cache)
  {
    this.scene_builder = in_scene_builder;
    this.cube_cache = in_cube_cache;
    this.mesh_cache = in_mesh_cache;
    this.texture2d_cache = in_texture2d_cache;
  }

  @Override public TextureCubeStaticUsableType cubeTextureClamped(
    final String name)
    throws RException
  {
    try {
      final ETextureCubeCache cc = this.cube_cache;
      assert cc != null;
      return cc.loadCubeClamped(name);
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

  @Override public TextureCubeStaticUsableType cubeTextureRepeated(
    final String name)
    throws RException
  {
    try {
      final ETextureCubeCache cc = this.cube_cache;
      assert cc != null;
      return cc.loadCubeRepeat(name);
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

  @Override public void visibleOpaqueAddUnlit(
    final KInstanceOpaqueType instance)
    throws RExceptionBuilderInvalid,
      RExceptionInstanceAlreadyVisible
  {
    this.scene_builder.visibleOpaqueAddUnlit(instance);
  }

  @Override public
    KVisibleSetLightGroupBuilderType
    visibleOpaqueNewLightGroup(
      final String name)
      throws RExceptionLightGroupAlreadyAdded,
        RExceptionBuilderInvalid
  {
    return this.scene_builder.visibleOpaqueNewLightGroup(name);
  }

  @Override public void visibleShadowsAddCaster(
    final KLightWithShadowType light,
    final KInstanceOpaqueType instance)
    throws RExceptionBuilderInvalid
  {
    this.scene_builder.visibleShadowsAddCaster(light, instance);
  }

  @Override public void visibleShadowsAddLight(
    final KLightWithShadowType light)
    throws RExceptionBuilderInvalid
  {
    this.scene_builder.visibleShadowsAddLight(light);
  }

  @Override public void visibleTranslucentsAddLit(
    final KInstanceTranslucentLitType instance,
    final Set<KLightTranslucentType> lights)
    throws RExceptionBuilderInvalid
  {
    this.scene_builder.visibleTranslucentsAddLit(instance, lights);
  }

  @Override public void visibleTranslucentsAddUnlit(
    final KInstanceTranslucentUnlitType instance)
    throws RExceptionBuilderInvalid
  {
    this.scene_builder.visibleTranslucentsAddUnlit(instance);
  }
}
