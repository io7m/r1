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

package com.io7m.renderer.kernel.sandbox;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnimplementedCodeException;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jcanephora.TextureWrapR;
import com.io7m.jcanephora.TextureWrapS;
import com.io7m.jcanephora.TextureWrapT;
import com.io7m.jvvfs.PathVirtual;
import com.io7m.renderer.types.RException;

/**
 * An example class that implements all of the controller interfaces, purely
 * for the purposes of launching simple example programs for each of the
 * control panel types.
 */

final class SBExampleController implements
  SBSceneControllerMaterials,
  SBSceneControllerInstances,
  SBSceneControllerTextures,
  SBSceneControllerMeshes,
  SBSceneControllerLights
{
  private final @Nonnull AtomicInteger                    instance_id_pool;
  private final @Nonnull AtomicInteger                    light_id_pool;
  private final @Nonnull AtomicInteger                    material_id_pool;
  private final @Nonnull HashMap<PathVirtual, SBMesh>     mesh_map;
  private final @Nonnull Map<PathVirtual, SBTexture2D<?>> textures;

  public SBExampleController()
    throws ConstraintError
  {
    this.mesh_map = new HashMap<PathVirtual, SBMesh>();
    this.mesh_map.put(PathVirtual.ofString("/meshes/x/r.rmx"), null);
    this.mesh_map.put(PathVirtual.ofString("/meshes/z/q.rmb"), null);
    this.mesh_map.put(PathVirtual.ofString("/meshes/w/w.rmx"), null);
    this.instance_id_pool = new AtomicInteger(0);
    this.material_id_pool = new AtomicInteger(0);
    this.light_id_pool = new AtomicInteger(0);

    this.textures = new HashMap<PathVirtual, SBTexture2D<?>>();
  }

  @Override public void sceneChangeListenerAdd(
    final SBSceneChangeListener listener)
  {
    System.out.println("Pretended to add change listener " + listener);
  }

  @Override public boolean sceneInstanceExists(
    final Integer id)
    throws ConstraintError
  {
    // TODO Auto-generated method stub
    throw new UnimplementedCodeException();
  }

  @Override public Integer sceneInstanceFreshID()
  {
    return Integer.valueOf(this.instance_id_pool.incrementAndGet());
  }

  @Override public SBInstance sceneInstanceGet(
    final Integer id)
    throws ConstraintError
  {
    // TODO Auto-generated method stub
    throw new UnimplementedCodeException();
  }

  @Override public void sceneInstancePut(
    final SBInstance desc)
    throws ConstraintError
  {
    // TODO Auto-generated method stub
    throw new UnimplementedCodeException();
  }

  @Override public void sceneInstanceRemove(
    final Integer id)
    throws ConstraintError
  {
    // TODO Auto-generated method stub
    throw new UnimplementedCodeException();
  }

  @Override public Collection<SBInstance> sceneInstancesGetAll()
  {
    // TODO Auto-generated method stub
    throw new UnimplementedCodeException();
  }

  @Override public boolean sceneLightExists(
    final Integer id)
    throws ConstraintError
  {
    // TODO Auto-generated method stub
    throw new UnimplementedCodeException();
  }

  @Override public Integer sceneLightFreshID()
  {
    return Integer.valueOf(this.light_id_pool.incrementAndGet());
  }

  @Override public SBLight sceneLightGet(
    final Integer id)
    throws ConstraintError
  {
    // TODO Auto-generated method stub
    throw new UnimplementedCodeException();
  }

  @Override public void sceneLightRemove(
    final Integer id)
    throws ConstraintError
  {
    // TODO Auto-generated method stub
    throw new UnimplementedCodeException();
  }

  @Override public Collection<SBLight> sceneLightsGetAll()
  {
    // TODO Auto-generated method stub
    throw new UnimplementedCodeException();
  }

  @Override public boolean sceneMaterialExists(
    final Integer id)
    throws ConstraintError
  {
    // TODO Auto-generated method stub
    throw new UnimplementedCodeException();
  }

  @Override public Integer sceneMaterialFreshID()
  {
    return Integer.valueOf(this.material_id_pool.incrementAndGet());
  }

  @Override public SBMaterial sceneMaterialGet(
    final Integer id)
    throws ConstraintError
  {
    // TODO Auto-generated method stub
    throw new UnimplementedCodeException();
  }

  @Override public void sceneMaterialPut(
    final SBMaterial material)
    throws ConstraintError
  {
    // TODO Auto-generated method stub
    throw new UnimplementedCodeException();
  }

  @Override public void sceneMaterialPutByDescription(
    final Integer id,
    final SBMaterialDescription desc)
    throws ConstraintError,
      RException
  {
    // TODO Auto-generated method stub
    throw new UnimplementedCodeException();
  }

  @Override public void sceneMaterialRemove(
    final Integer id)
    throws ConstraintError
  {
    // TODO Auto-generated method stub
    throw new UnimplementedCodeException();
  }

  @Override public Collection<SBMaterial> sceneMaterialsGetAll()
  {
    // TODO Auto-generated method stub
    throw new UnimplementedCodeException();
  }

  @Override public Map<PathVirtual, SBMesh> sceneMeshesGet()
  {
    return this.mesh_map;
  }

  @Override public Future<SBMesh> sceneMeshLoad(
    final File file)
    throws ConstraintError
  {
    // TODO Auto-generated method stub
    throw new UnimplementedCodeException();
  }

  @Override public
    <T extends SBTexture2DKind>
    Future<SBTexture2D<T>>
    sceneTexture2DLoad(
      final File file,
      final TextureWrapS wrap_s,
      final TextureWrapT wrap_t,
      final TextureFilterMinification filter_min,
      final TextureFilterMagnification filter_mag)
      throws ConstraintError
  {
    // TODO Auto-generated method stub
    throw new UnimplementedCodeException();
  }

  @Override public Future<SBTextureCube> sceneTextureCubeLoad(
    final File file,
    final TextureWrapR wrap_r,
    final TextureWrapS wrap_s,
    final TextureWrapT wrap_t,
    final TextureFilterMinification filter_min,
    final TextureFilterMagnification filter_mag)
    throws ConstraintError
  {
    // TODO Auto-generated method stub
    throw new UnimplementedCodeException();
  }

  @Override public Map<PathVirtual, SBTexture2D<?>> sceneTextures2DGet()
  {
    return this.textures;
  }

  @Override public Map<PathVirtual, SBTextureCube> sceneTexturesCubeGet()
  {
    // TODO Auto-generated method stub
    throw new UnimplementedCodeException();
  }

  @Override public void sceneLightPut(
    final SBLightDescription d)
    throws ConstraintError
  {
    // TODO Auto-generated method stub
    throw new UnimplementedCodeException();
  }
}
