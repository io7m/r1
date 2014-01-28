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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;

import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.pcollections.HashTreePSet;
import org.pcollections.MapPSet;
import org.xml.sax.SAXException;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Option;
import com.io7m.jaux.functional.Pair;
import com.io7m.jcanephora.CMFKNegativeX;
import com.io7m.jcanephora.CMFKNegativeY;
import com.io7m.jcanephora.CMFKNegativeZ;
import com.io7m.jcanephora.CMFKPositiveX;
import com.io7m.jcanephora.CMFKPositiveY;
import com.io7m.jcanephora.CMFKPositiveZ;
import com.io7m.jcanephora.CubeMapFaceInputStream;
import com.io7m.jcanephora.Texture2DStatic;
import com.io7m.jcanephora.TextureCubeStatic;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jcanephora.TextureWrapR;
import com.io7m.jcanephora.TextureWrapS;
import com.io7m.jcanephora.TextureWrapT;
import com.io7m.jlog.Log;
import com.io7m.jparasol.xml.PGLSLMetaXML;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.jtensors.QuaternionM4F;
import com.io7m.jtensors.VectorI3F;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.jvvfs.PathVirtual;
import com.io7m.renderer.RMatrixI4x4F;
import com.io7m.renderer.RTransformProjection;
import com.io7m.renderer.kernel.KLight.KProjective;
import com.io7m.renderer.kernel.SBLight.SBLightDirectional;
import com.io7m.renderer.kernel.SBLight.SBLightProjective;
import com.io7m.renderer.kernel.SBLight.SBLightSpherical;
import com.io7m.renderer.kernel.SBLightDescription.SBLightDescriptionDirectional;
import com.io7m.renderer.kernel.SBLightDescription.SBLightDescriptionProjective;
import com.io7m.renderer.kernel.SBLightDescription.SBLightDescriptionSpherical;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.mapper.CannotResolveClassException;

interface SBSceneChangeListener
{
  public void sceneChanged();
}

interface SBSceneChangeListenerRegistration
{
  public void sceneChangeListenerAdd(
    final @Nonnull SBSceneChangeListener listener);
}

public final class SBSceneController implements
  SBSceneControllerIO,
  SBSceneControllerLights,
  SBSceneControllerMeshes,
  SBSceneControllerInstances,
  SBSceneControllerRenderer,
  SBSceneControllerRendererControl,
  SBSceneControllerShaders,
  SBSceneControllerTextures
{
  private static class SceneAndFilesystem
  {
    final @Nonnull SBScene           scene;
    final @Nonnull SBSceneFilesystem filesystem;

    SceneAndFilesystem(
      final @Nonnull SBScene scene,
      final @Nonnull SBSceneFilesystem filesystem)
    {
      this.scene = scene;
      this.filesystem = filesystem;
    }
  }

  private final @Nonnull SBGLRenderer                        renderer;
  private final @Nonnull Log                                 log;
  private final @Nonnull LinkedList<SBSceneChangeListener>   listeners;
  private final @Nonnull AtomicReference<SceneAndFilesystem> state_current;
  private final @Nonnull ExecutorService                     exec_pool;
  private final @Nonnull Map<String, SBShader>               shaders;

  public SBSceneController(
    final @Nonnull SBGLRenderer renderer,
    final @Nonnull Log log)
    throws FilesystemError,
      IOException,
      ConstraintError
  {
    this.renderer = renderer;
    this.log = new Log(log, "control");
    this.listeners = new LinkedList<SBSceneChangeListener>();
    this.shaders = new ConcurrentHashMap<String, SBShader>();
    this.state_current =
      new AtomicReference<SceneAndFilesystem>(new SceneAndFilesystem(
        SBScene.empty(),
        SBSceneFilesystem.filesystemEmpty(log)));
    this.exec_pool = Executors.newCachedThreadPool();
  }

  private @Nonnull SceneAndFilesystem internalIOSceneLoad(
    final @Nonnull File file)
    throws FileNotFoundException,
      FilesystemError,
      IOException,
      ConstraintError,
      InterruptedException,
      ExecutionException
  {
    final SBSceneFilesystem fs =
      SBSceneFilesystem.filesystemLoadScene(this.log, file);
    final InputStream xms =
      fs.filesystemOpenFile(PathVirtual.ofString("/scene.xml"));

    try {
      final XStream stream = new XStream();
      final SBSceneDescription sd;

      try {
        sd = (SBSceneDescription) stream.fromXML(xms);
      } catch (final CannotResolveClassException x) {
        final StringBuilder m = new StringBuilder();
        m
          .append("Could not parse the given scene. It is likely of an unsupported version.");
        throw new IOException(m.toString(), x);
      }

      if (sd.getSchemaVersion() != SBSceneDescription.SCENE_XML_VERSION) {
        final StringBuilder m = new StringBuilder();
        m.append("Supported scene version is ");
        m.append(SBSceneDescription.SCENE_XML_VERSION);
        m.append(" but the loaded scene is of version ");
        m.append(sd.getSchemaVersion());
        throw new IOException(m.toString());
      }

      SBScene scene = SBScene.empty();

      for (final SBTexture2DDescription desc : sd.getTextures2D()) {
        final SBTexture2D<?> t =
          this.internalTexture2DLoadFromPath(
            fs,
            desc.getPath(),
            desc.getWrapModeS(),
            desc.getWrapModeT(),
            desc.getTextureMin(),
            desc.getTextureMag());
        scene = scene.texture2DAdd(t);
      }

      for (final SBTextureCubeDescription desc : sd.getTexturesCube()) {
        final SBTextureCube t =
          this.internalTextureCubeLoadFromPath(
            fs,
            desc.getPath(),
            desc.getWrapModeR(),
            desc.getWrapModeS(),
            desc.getWrapModeT(),
            desc.getTextureMin(),
            desc.getTextureMag());
        scene = scene.textureCubeAdd(t);
      }

      for (final PathVirtual name : sd.getMeshes()) {
        final SBMesh m = this.internalMeshLoadFromPath(fs, name);
        scene = scene.meshAdd(m);
      }

      for (final SBLightDescription light : sd.getLights()) {
        switch (light.getType()) {
          case LIGHT_DIRECTIONAL:
          {
            final SBLightDescriptionDirectional dd =
              (SBLightDescriptionDirectional) light;
            scene = scene.lightAdd(new SBLightDirectional(dd));
            break;
          }
          case LIGHT_PROJECTIVE:
          {
            final SBLightDescriptionProjective dd =
              (SBLightDescriptionProjective) light;
            final SBTexture2D<SBTexture2DKindAlbedo> t =
              scene.texture2DGet(dd.getTexture());

            scene = scene.lightAdd(new SBLightProjective(dd, dd.getLight(t)));
            break;
          }
          case LIGHT_SPHERE:
          {
            final SBLightDescriptionSpherical dd =
              (SBLightDescriptionSpherical) light;
            scene = scene.lightAdd(new SBLightSpherical(dd));
            break;
          }
        }

      }

      for (final SBInstanceDescription i : sd.getInstanceDescriptions()) {
        scene = scene.instanceAddByDescription(i);
      }

      return new SceneAndFilesystem(scene, fs);

    } finally {
      xms.close();
    }
  }

  private void internalIOSceneSave(
    final @Nonnull File file)
    throws IOException
  {
    final SceneAndFilesystem saf = this.state_current.get();
    saf.filesystem.filesystemSave(saf.scene.makeDescription(), file);
  }

  private @Nonnull SBMesh internalMeshLoad(
    final @Nonnull SBSceneFilesystem fs,
    final @Nonnull File file)
    throws FileNotFoundException,
      FilesystemError,
      IOException,
      ConstraintError,
      InterruptedException,
      ExecutionException
  {
    final PathVirtual path = fs.filesystemCopyInMesh(file);
    return this.internalMeshLoadFromPath(fs, path);
  }

  private @Nonnull SBMesh internalMeshLoadFromPath(
    final @Nonnull SBSceneFilesystem fs,
    final @Nonnull PathVirtual path)
    throws FilesystemError,
      ConstraintError,
      InterruptedException,
      ExecutionException,
      IOException
  {
    final InputStream stream = fs.filesystemOpenFile(path);
    try {
      final Future<SBMesh> future = this.renderer.meshLoad(path, stream);
      final SBMesh mesh = future.get();
      return mesh;
    } finally {
      stream.close();
    }
  }

  private @Nonnull SBShader internalShaderLoad(
    final @Nonnull File file)
    throws ConstraintError,
      IOException,
      InterruptedException,
      ExecutionException,
      KXMLException
  {
    final FileInputStream stream = new FileInputStream(file);
    try {
      final PGLSLMetaXML meta = PGLSLMetaXML.fromStream(stream, this.log);
      final Future<SBShader> f =
        this.renderer.shaderLoad(file.getParentFile(), meta);
      return f.get();
    } catch (final SAXException x) {
      throw KXMLException.saxException(x);
    } catch (final ParserConfigurationException x) {
      throw KXMLException.parserConfigurationException(x);
    } catch (final ParsingException x) {
      throw KXMLException.parsingException(x);
    } finally {
      stream.close();
    }
  }

  private void internalStateChangedNotifyListeners()
  {
    for (final SBSceneChangeListener l : this.listeners) {
      l.sceneChanged();
    }
  }

  private void internalStateUpdate(
    final @Nonnull SceneAndFilesystem state)
  {
    this.log.debug("state updated");
    this.state_current.set(state);
    this.internalStateChangedNotifyListeners();
  }

  private void internalStateUpdateSceneOnly(
    final @Nonnull SBScene scene)
  {
    this.log.debug("scene state updated");

    final SceneAndFilesystem saf = this.state_current.get();
    this.state_current.set(new SceneAndFilesystem(scene, saf.filesystem));
    this.internalStateChangedNotifyListeners();
  }

  private @Nonnull
    <T extends SBTexture2DKind>
    SBTexture2D<T>
    internalTexture2DLoad(
      final @Nonnull SBSceneFilesystem fs,
      final @Nonnull File file,
      final @Nonnull TextureWrapS wrap_s,
      final @Nonnull TextureWrapT wrap_t,
      final @Nonnull TextureFilterMinification filter_min,
      final @Nonnull TextureFilterMagnification filter_mag)
      throws FileNotFoundException,
        FilesystemError,
        IOException,
        ConstraintError,
        InterruptedException,
        ExecutionException
  {
    final PathVirtual path = fs.filesystemCopyInTexture2D(file);
    return this.internalTexture2DLoadFromPath(
      fs,
      path,
      wrap_s,
      wrap_t,
      filter_min,
      filter_mag);
  }

  private @Nonnull
    <T extends SBTexture2DKind>
    SBTexture2D<T>
    internalTexture2DLoadFromPath(
      final @Nonnull SBSceneFilesystem fs,
      final @Nonnull PathVirtual path,
      final @Nonnull TextureWrapS wrap_s,
      final @Nonnull TextureWrapT wrap_t,
      final @Nonnull TextureFilterMinification filter_min,
      final @Nonnull TextureFilterMagnification filter_mag)
      throws FilesystemError,
        ConstraintError,
        IOException,
        InterruptedException,
        ExecutionException
  {
    final InputStream image_io_stream = fs.filesystemOpenFile(path);

    try {
      final InputStream gl_stream = fs.filesystemOpenFile(path);

      try {
        final BufferedImage image = ImageIO.read(image_io_stream);
        if (image == null) {
          throw new IOException("Unable to parse image");
        }

        final Future<Texture2DStatic> future =
          this.renderer.texture2DLoad(
            path,
            gl_stream,
            wrap_s,
            wrap_t,
            filter_min,
            filter_mag);
        final Texture2DStatic texture = future.get();

        return new SBTexture2D<T>(new SBTexture2DDescription(
          path,
          wrap_s,
          wrap_t,
          filter_min,
          filter_mag), texture, image);
      } finally {
        gl_stream.close();
      }
    } finally {
      image_io_stream.close();
    }
  }

  private @Nonnull SBTextureCube internalTextureCubeLoad(
    final @Nonnull SBSceneFilesystem fs,
    final @Nonnull File file,
    final @Nonnull TextureWrapR wrap_r,
    final @Nonnull TextureWrapS wrap_s,
    final @Nonnull TextureWrapT wrap_t,
    final @Nonnull TextureFilterMinification filter_min,
    final @Nonnull TextureFilterMagnification filter_mag)
    throws FileNotFoundException,
      FilesystemError,
      IOException,
      ConstraintError,
      InterruptedException,
      ExecutionException,
      ValidityException,
      ParsingException,
      com.io7m.renderer.xml.RXMLException
  {
    final PathVirtual path = fs.filesystemCopyInTextureCube(file);
    return this.internalTextureCubeLoadFromPath(
      fs,
      path,
      wrap_r,
      wrap_s,
      wrap_t,
      filter_min,
      filter_mag);
  }

  private @Nonnull SBTextureCube internalTextureCubeLoadFromPath(
    final @Nonnull SBSceneFilesystem fs,
    final @Nonnull PathVirtual path,
    final @Nonnull TextureWrapR wrap_r,
    final @Nonnull TextureWrapS wrap_s,
    final @Nonnull TextureWrapT wrap_t,
    final @Nonnull TextureFilterMinification filter_min,
    final @Nonnull TextureFilterMagnification filter_mag)
    throws FilesystemError,
      ConstraintError,
      IOException,
      InterruptedException,
      ExecutionException
  {
    CubeMapFaceInputStream<CMFKPositiveZ> spz = null;
    CubeMapFaceInputStream<CMFKPositiveY> spy = null;
    CubeMapFaceInputStream<CMFKPositiveX> spx = null;
    CubeMapFaceInputStream<CMFKNegativeZ> snz = null;
    CubeMapFaceInputStream<CMFKNegativeY> sny = null;
    CubeMapFaceInputStream<CMFKNegativeX> snx = null;

    InputStream ispz = null;
    InputStream ispy = null;
    InputStream ispx = null;
    InputStream isnz = null;
    InputStream isny = null;
    InputStream isnx = null;

    try {
      final PathVirtual path_pz =
        path.appendName(SBSceneFilesystem.CUBE_MAP_POSITIVE_Z);
      final PathVirtual path_py =
        path.appendName(SBSceneFilesystem.CUBE_MAP_POSITIVE_Y);
      final PathVirtual path_px =
        path.appendName(SBSceneFilesystem.CUBE_MAP_POSITIVE_X);
      final PathVirtual path_nz =
        path.appendName(SBSceneFilesystem.CUBE_MAP_NEGATIVE_Z);
      final PathVirtual path_ny =
        path.appendName(SBSceneFilesystem.CUBE_MAP_NEGATIVE_Y);
      final PathVirtual path_nx =
        path.appendName(SBSceneFilesystem.CUBE_MAP_NEGATIVE_X);

      ispz = fs.filesystemOpenFile(path_pz);
      ispy = fs.filesystemOpenFile(path_py);
      ispx = fs.filesystemOpenFile(path_px);
      isnz = fs.filesystemOpenFile(path_nz);
      isny = fs.filesystemOpenFile(path_ny);
      isnx = fs.filesystemOpenFile(path_nx);

      final BufferedImage ipz = ImageIO.read(ispz);
      if (ipz == null) {
        throw new IOException("Unknown image format: " + path_pz);
      }
      final BufferedImage ipy = ImageIO.read(ispy);
      if (ipy == null) {
        throw new IOException("Unknown image format: " + path_py);
      }
      final BufferedImage ipx = ImageIO.read(ispx);
      if (ipx == null) {
        throw new IOException("Unknown image format: " + path_px);
      }

      final BufferedImage inz = ImageIO.read(isnz);
      if (inz == null) {
        throw new IOException("Unknown image format: " + path_nz);
      }
      final BufferedImage iny = ImageIO.read(isny);
      if (iny == null) {
        throw new IOException("Unknown image format: " + path_ny);
      }
      final BufferedImage inx = ImageIO.read(isnx);
      if (inx == null) {
        throw new IOException("Unknown image format: " + path_nx);
      }

      spz =
        new CubeMapFaceInputStream<CMFKPositiveZ>(
          fs.filesystemOpenFile(path_pz));
      spy =
        new CubeMapFaceInputStream<CMFKPositiveY>(
          fs.filesystemOpenFile(path_py));
      spx =
        new CubeMapFaceInputStream<CMFKPositiveX>(
          fs.filesystemOpenFile(path_px));
      snz =
        new CubeMapFaceInputStream<CMFKNegativeZ>(
          fs.filesystemOpenFile(path_nz));
      sny =
        new CubeMapFaceInputStream<CMFKNegativeY>(
          fs.filesystemOpenFile(path_ny));
      snx =
        new CubeMapFaceInputStream<CMFKNegativeX>(
          fs.filesystemOpenFile(path_nx));

      final Future<TextureCubeStatic> future =
        this.renderer.textureCubeLoad(
          path,
          spz,
          snz,
          spy,
          sny,
          spx,
          snx,
          wrap_r,
          wrap_s,
          wrap_t,
          filter_min,
          filter_mag);

      final TextureCubeStatic texture = future.get();
      return new SBTextureCube(new SBTextureCubeDescription(
        path,
        wrap_r,
        wrap_s,
        wrap_t,
        filter_min,
        filter_mag), texture, ipz, inz, ipy, iny, ipx, inx);

    } finally {
      if (spz != null) {
        spz.close();
      }
      if (spy != null) {
        spy.close();
      }
      if (spx != null) {
        spx.close();
      }
      if (snz != null) {
        snz.close();
      }
      if (sny != null) {
        sny.close();
      }
      if (snx != null) {
        snx.close();
      }

      if (ispz != null) {
        ispz.close();
      }
      if (ispy != null) {
        ispy.close();
      }
      if (ispx != null) {
        ispx.close();
      }
      if (isnz != null) {
        isnz.close();
      }
      if (isny != null) {
        isny.close();
      }
      if (isnx != null) {
        isnx.close();
      }
    }
  }

  @Override public @Nonnull Future<Void> ioLoadScene(
    final @Nonnull File file)
    throws ConstraintError
  {
    Constraints.constrainNotNull(file, "File");

    final FutureTask<Void> f = new FutureTask<Void>(new Callable<Void>() {
      @SuppressWarnings("synthetic-access") @Override public Void call()
        throws Exception
      {
        try {
          final SceneAndFilesystem saf =
            SBSceneController.this.internalIOSceneLoad(file);
          SBSceneController.this.internalStateUpdate(saf);
        } catch (final ConstraintError x) {
          throw new UnreachableCodeException();
        }
        return null;
      }
    });

    this.exec_pool.execute(f);
    return f;
  }

  @Override public @Nonnull Future<Void> ioSaveScene(
    final @Nonnull File file)
    throws ConstraintError
  {
    Constraints.constrainNotNull(file, "File");

    final FutureTask<Void> f = new FutureTask<Void>(new Callable<Void>() {
      @SuppressWarnings("synthetic-access") @Override public Void call()
        throws Exception
      {
        SBSceneController.this.internalIOSceneSave(file);
        return null;
      }
    });

    this.exec_pool.execute(f);
    return f;
  }

  @Override public RMatrixI4x4F<RTransformProjection> rendererGetProjection()
  {
    return this.renderer.getProjection();
  }

  @Override public @Nonnull
    Pair<Collection<SBLight>, Collection<Pair<KMeshInstanceTransformed, SBInstance>>>
    rendererGetScene()
      throws ConstraintError
  {
    final SceneAndFilesystem saf = this.state_current.get();
    final SBScene scene = saf.scene;

    final Collection<SBLight> lights = scene.lightsGet();
    MapPSet<Pair<KMeshInstanceTransformed, SBInstance>> meshes =
      HashTreePSet.empty();

    for (final SBInstance i : scene.instancesGet()) {
      final PathVirtual mesh_path = i.getMesh();
      final SBMesh mesh = scene.meshGet(mesh_path);
      final SBMaterial mat = i.getMaterial();

      final QuaternionM4F orientation = new QuaternionM4F();
      final QuaternionI4F rotate_x =
        QuaternionI4F.makeFromAxisAngle(
          new VectorI3F(1, 0, 0),
          Math.toRadians(i.getOrientation().x));
      final QuaternionI4F rotate_y =
        QuaternionI4F.makeFromAxisAngle(
          new VectorI3F(0, 1, 0),
          Math.toRadians(i.getOrientation().y));
      final QuaternionI4F rotate_z =
        QuaternionI4F.makeFromAxisAngle(
          new VectorI3F(0, 0, 1),
          Math.toRadians(i.getOrientation().z));

      QuaternionM4F.multiplyInPlace(orientation, rotate_z);
      QuaternionM4F.multiplyInPlace(orientation, rotate_y);
      QuaternionM4F.multiplyInPlace(orientation, rotate_x);

      final Integer id = i.getID();

      final KTransform transform =
        KTransform.newOSTTransform(
          new QuaternionI4F(orientation),
          i.getScale(),
          i.getPosition());

      final SBMaterialAlphaDescription sd_alpha =
        mat.getDescription().getAlpha();
      final SBMaterialAlbedoDescription sd_albedo =
        mat.getDescription().getAlbedo();
      final SBMaterialEmissiveDescription sd_emiss =
        mat.getDescription().getEmissive();
      final SBMaterialSpecularDescription sd_spec =
        mat.getDescription().getSpecular();
      final SBMaterialEnvironmentDescription sd_envi =
        mat.getDescription().getEnvironment();

      final Option<Texture2DStatic> emissive_map =
        (mat.getEmissiveMap() != null) ? new Option.Some<Texture2DStatic>(mat
          .getEmissiveMap()
          .getTexture()) : new Option.None<Texture2DStatic>();

      final Option<Texture2DStatic> diffuse_map =
        (mat.getDiffuseMap() != null) ? new Option.Some<Texture2DStatic>(mat
          .getDiffuseMap()
          .getTexture()) : new Option.None<Texture2DStatic>();

      final Option<Texture2DStatic> normal_map =
        (mat.getNormalMap() == null)
          ? new Option.None<Texture2DStatic>()
          : new Option.Some<Texture2DStatic>(mat.getNormalMap().getTexture());

      final Option<Texture2DStatic> specular_map =
        (mat.getSpecularMap() == null)
          ? new Option.None<Texture2DStatic>()
          : new Option.Some<Texture2DStatic>(mat
            .getSpecularMap()
            .getTexture());

      final Option<TextureCubeStatic> environment_map =
        (mat.getEnvironmentMap() == null)
          ? new Option.None<TextureCubeStatic>()
          : new Option.Some<TextureCubeStatic>(mat
            .getEnvironmentMap()
            .getTexture());

      final KMaterialAlpha alpha =
        new KMaterialAlpha(
          sd_alpha.getOpacityType(),
          sd_alpha.getOpacity(),
          sd_alpha.getDepthThreshold());

      final KMaterialAlbedo diff =
        new KMaterialAlbedo(
          sd_albedo.getColour(),
          sd_albedo.getMix(),
          diffuse_map);

      final KMaterialEmissive emiss =
        new KMaterialEmissive(sd_emiss.getEmission(), emissive_map);

      final KMaterialSpecular spec =
        new KMaterialSpecular(
          sd_spec.getIntensity(),
          sd_spec.getExponent(),
          specular_map);

      final KMaterialEnvironment envi =
        new KMaterialEnvironment(
          sd_envi.getMix(),
          environment_map,
          sd_envi.getMixFromSpecularMap());

      final KMaterialNormal norm = new KMaterialNormal(normal_map);

      final KMaterial material =
        new KMaterial(alpha, diff, emiss, envi, norm, spec, mat.getUVMatrix());

      final KMesh km = mesh.getMesh();
      final KMeshInstance kmi = new KMeshInstance(id, material, km);

      final KMeshInstanceTransformed mi =
        new KMeshInstanceTransformed(kmi, transform, i.getUVMatrix());

      meshes =
        meshes.plus(new Pair<KMeshInstanceTransformed, SBInstance>(mi, i));
    }

    return new Pair<Collection<SBLight>, Collection<Pair<KMeshInstanceTransformed, SBInstance>>>(
      lights,
      meshes);
  }

  @Override public void rendererSetBackgroundColour(
    final float r,
    final float g,
    final float b)
  {
    this.renderer.setBackgroundColour(r, g, b);
  }

  @Override public void rendererSetCustomProjection(
    final @Nonnull RMatrixI4x4F<RTransformProjection> p)
  {
    this.renderer.setCustomProjection(p);
  }

  @Override public void rendererSetType(
    final @Nonnull SBKRendererType type)
  {
    this.renderer.setRenderer(type);
  }

  @Override public void rendererShowAxes(
    final boolean enabled)
  {
    this.renderer.setShowAxes(enabled);
  }

  @Override public void rendererShowGrid(
    final boolean enabled)
  {
    this.renderer.setShowGrid(enabled);
  }

  @Override public void rendererShowLightRadii(
    final boolean enabled)
  {
    this.renderer.setShowLightRadii(enabled);
  }

  @Override public void rendererShowLights(
    final boolean enabled)
  {
    this.renderer.setShowLights(enabled);
  }

  @Override public void rendererUnsetCustomProjection()
  {
    this.renderer.unsetCustomProjection();
  }

  @Override public void sceneChangeListenerAdd(
    final @Nonnull SBSceneChangeListener listener)
  {
    this.log.debug("Registered change listener " + listener);
    this.listeners.add(listener);
  }

  @Override public void sceneInstanceAdd(
    final @Nonnull SBInstance instance)
    throws ConstraintError
  {
    this.internalStateUpdateSceneOnly(this.state_current.get().scene
      .instanceAdd(instance));
  }

  @Override public void sceneInstanceAddByDescription(
    final @Nonnull SBInstanceDescription desc)
    throws ConstraintError
  {
    final SceneAndFilesystem saf = this.state_current.get();
    this.internalStateUpdateSceneOnly(saf.scene
      .instanceAddByDescription(desc));
  }

  @Override public boolean sceneInstanceExists(
    final @Nonnull Integer id)
    throws ConstraintError
  {
    return this.state_current.get().scene.instanceExists(id);
  }

  @Override public @Nonnull Integer sceneInstanceFreshID()
  {
    final Pair<SBScene, Integer> p =
      this.state_current.get().scene.instanceFreshID();
    this.internalStateUpdateSceneOnly(p.first);
    return p.second;
  }

  @Override public @Nonnull SBInstance sceneInstanceGet(
    final @Nonnull Integer id)
    throws ConstraintError
  {
    return this.state_current.get().scene.instanceGet(id);
  }

  @Override public void sceneInstanceRemove(
    final @Nonnull Integer id)
    throws ConstraintError
  {
    this.internalStateUpdateSceneOnly(this.state_current.get().scene
      .removeInstance(id));
  }

  @Override public @Nonnull Collection<SBInstance> sceneInstancesGetAll()
  {
    return this.state_current.get().scene.instancesGet();
  }

  @Override public void sceneLightAddByDescription(
    final @Nonnull SBLightDescription d)
    throws ConstraintError
  {
    switch (d.getType()) {
      case LIGHT_DIRECTIONAL:
      {
        final SBLightDescriptionDirectional dd =
          (SBLightDescriptionDirectional) d;
        final SBLightDirectional l = new SBLight.SBLightDirectional(dd);
        this.internalStateUpdateSceneOnly(this.state_current.get().scene
          .lightAdd(l));
        break;
      }
      case LIGHT_PROJECTIVE:
      {
        final SBLightDescriptionProjective dd =
          (SBLightDescriptionProjective) d;

        @SuppressWarnings("unchecked") final SBTexture2D<SBTexture2DKindAlbedo> t =
          (SBTexture2D<SBTexture2DKindAlbedo>) this.sceneTextures2DGet().get(
            dd.getTexture());

        final KProjective kp = dd.getLight(t);
        final SBLightProjective l = new SBLight.SBLightProjective(dd, kp);

        this.internalStateUpdateSceneOnly(this.state_current.get().scene
          .lightAdd(l));

        break;
      }
      case LIGHT_SPHERE:
      {
        final SBLightDescriptionSpherical dd =
          (SBLightDescriptionSpherical) d;

        this.internalStateUpdateSceneOnly(this.state_current.get().scene
          .lightAdd(new SBLight.SBLightSpherical(dd)));
        break;
      }
    }
  }

  @Override public boolean sceneLightExists(
    final @Nonnull Integer id)
    throws ConstraintError
  {
    return this.state_current.get().scene.lightExists(id);
  }

  @Override public @Nonnull Integer sceneLightFreshID()
  {
    final Pair<SBScene, Integer> p =
      this.state_current.get().scene.lightFreshID();
    this.internalStateUpdateSceneOnly(p.first);
    return p.second;
  }

  @Override public @Nonnull SBLight sceneLightGet(
    final @Nonnull Integer id)
    throws ConstraintError
  {
    return this.state_current.get().scene.lightGet(id);
  }

  @Override public void sceneLightRemove(
    final @Nonnull Integer id)
    throws ConstraintError
  {
    this.internalStateUpdateSceneOnly(this.state_current.get().scene
      .lightRemove(id));
  }

  @Override public @Nonnull Collection<SBLight> sceneLightsGetAll()
  {
    return this.state_current.get().scene.lightsGet();
  }

  @Override public @Nonnull Map<PathVirtual, SBMesh> sceneMeshesGet()
  {
    return this.state_current.get().scene.meshesGet();
  }

  @Override public @Nonnull Future<SBMesh> sceneMeshLoad(
    final @Nonnull File file)
    throws ConstraintError
  {
    Constraints.constrainNotNull(file, "File");

    final FutureTask<SBMesh> f =
      new FutureTask<SBMesh>(new Callable<SBMesh>() {
        @SuppressWarnings("synthetic-access") @Override public SBMesh call()
          throws Exception
        {
          try {
            final SceneAndFilesystem saf =
              SBSceneController.this.state_current.get();

            final SBMesh m =
              SBSceneController.this.internalMeshLoad(saf.filesystem, file);

            SBSceneController.this.internalStateUpdateSceneOnly(saf.scene
              .meshAdd(m));
            return m;
          } catch (final ConstraintError e) {
            throw new IOException(e);
          }
        }

      });

    this.exec_pool.execute(f);
    return f;
  }

  @Override public @Nonnull
    <T extends SBTexture2DKind>
    Future<SBTexture2D<T>>
    sceneTexture2DLoad(
      final @Nonnull File file,
      final @Nonnull TextureWrapS wrap_s,
      final @Nonnull TextureWrapT wrap_t,
      final @Nonnull TextureFilterMinification filter_min,
      final @Nonnull TextureFilterMagnification filter_mag)
      throws ConstraintError
  {
    Constraints.constrainNotNull(file, "File");

    final FutureTask<SBTexture2D<T>> f =
      new FutureTask<SBTexture2D<T>>(new Callable<SBTexture2D<T>>() {
        @SuppressWarnings("synthetic-access") @Override public
          SBTexture2D<T>
          call()
            throws Exception
        {
          try {
            final SceneAndFilesystem saf =
              SBSceneController.this.state_current.get();

            final SBTexture2D<T> sbt =
              SBSceneController.this.internalTexture2DLoad(
                saf.filesystem,
                file,
                wrap_s,
                wrap_t,
                filter_min,
                filter_mag);

            SBSceneController.this.internalStateUpdateSceneOnly(saf.scene
              .texture2DAdd(sbt));
            return sbt;
          } catch (final ConstraintError e) {
            throw new IOException(e);
          }
        }
      });

    this.exec_pool.execute(f);
    return f;
  }

  @Override public @Nonnull Future<SBTextureCube> sceneTextureCubeLoad(
    final @Nonnull File file,
    final @Nonnull TextureWrapR wrap_r,
    final @Nonnull TextureWrapS wrap_s,
    final @Nonnull TextureWrapT wrap_t,
    final @Nonnull TextureFilterMinification filter_min,
    final @Nonnull TextureFilterMagnification filter_mag)
    throws ConstraintError
  {
    Constraints.constrainNotNull(file, "File");

    final FutureTask<SBTextureCube> f =
      new FutureTask<SBTextureCube>(new Callable<SBTextureCube>() {
        @SuppressWarnings("synthetic-access") @Override public
          SBTextureCube
          call()
            throws Exception
        {
          try {
            final SceneAndFilesystem saf =
              SBSceneController.this.state_current.get();

            final SBTextureCube sbc =
              SBSceneController.this.internalTextureCubeLoad(
                saf.filesystem,
                file,
                wrap_r,
                wrap_s,
                wrap_t,
                filter_min,
                filter_mag);

            SBSceneController.this.internalStateUpdateSceneOnly(saf.scene
              .textureCubeAdd(sbc));
            return sbc;
          } catch (final ConstraintError e) {
            throw new IOException(e);
          }
        }
      });

    this.exec_pool.execute(f);
    return f;
  }

  @Override public @Nonnull
    Map<PathVirtual, SBTexture2D<?>>
    sceneTextures2DGet()
  {
    return this.state_current.get().scene.textures2DGet();
  }

  @Override public @Nonnull
    Map<PathVirtual, SBTextureCube>
    sceneTexturesCubeGet()
  {
    return this.state_current.get().scene.texturesCubeGet();
  }

  @Override public @Nonnull Future<SBShader> shaderLoad(
    final @Nonnull File file)
    throws ConstraintError
  {
    Constraints.constrainNotNull(file, "File");

    final FutureTask<SBShader> f =
      new FutureTask<SBShader>(new Callable<SBShader>() {
        @SuppressWarnings("synthetic-access") @Override public
          SBShader
          call()
            throws Exception
        {
          try {
            final SBShader sbt =
              SBSceneController.this.internalShaderLoad(file);

            SBSceneController.this.shaders.put(sbt.getName(), sbt);
            return sbt;
          } catch (final ConstraintError e) {
            throw new IOException(e);
          }
        }
      });

    this.exec_pool.execute(f);
    return f;
  }

  @Override public @Nonnull Map<String, SBShader> shadersGet()
  {
    return this.shaders;
  }

  @Override public Future<SBCacheStatistics> rendererGetCacheStatistics()
  {
    return this.renderer.getCacheStatistics();
  }

  @Override public void rendererPostprocessorSetType(
    final @Nonnull SBKPostprocessorType type)
  {
    this.renderer.setPostprocessor(type);
  }
}

interface SBSceneControllerInstances extends
  SBSceneChangeListenerRegistration
{
  public void sceneInstanceAdd(
    final @Nonnull SBInstance instance)
    throws ConstraintError;

  public void sceneInstanceAddByDescription(
    final @Nonnull SBInstanceDescription desc)
    throws ConstraintError;

  public boolean sceneInstanceExists(
    final @Nonnull Integer id)
    throws ConstraintError;

  public @Nonnull Integer sceneInstanceFreshID();

  public @CheckForNull SBInstance sceneInstanceGet(
    final @Nonnull Integer id)
    throws ConstraintError;

  public void sceneInstanceRemove(
    final @Nonnull Integer id)
    throws ConstraintError;

  public @Nonnull Collection<SBInstance> sceneInstancesGetAll();
}

interface SBSceneControllerIO
{
  public @Nonnull Future<Void> ioLoadScene(
    final @Nonnull File file)
    throws ConstraintError;

  public @Nonnull Future<Void> ioSaveScene(
    final @Nonnull File file)
    throws ConstraintError;
}

interface SBSceneControllerLights extends SBSceneChangeListenerRegistration
{
  public void sceneLightAddByDescription(
    final @Nonnull SBLightDescription d)
    throws ConstraintError;

  public boolean sceneLightExists(
    final @Nonnull Integer id)
    throws ConstraintError;

  public @Nonnull Integer sceneLightFreshID();

  public @CheckForNull SBLight sceneLightGet(
    final @Nonnull Integer id)
    throws ConstraintError;

  public void sceneLightRemove(
    final @Nonnull Integer id)
    throws ConstraintError;

  public @Nonnull Collection<SBLight> sceneLightsGetAll();
}

interface SBSceneControllerMeshes extends SBSceneChangeListenerRegistration
{
  public @Nonnull Map<PathVirtual, SBMesh> sceneMeshesGet();

  public @Nonnull Future<SBMesh> sceneMeshLoad(
    final @Nonnull File file)
    throws ConstraintError;
}

interface SBSceneControllerRenderer
{
  public @Nonnull
    Pair<Collection<SBLight>, Collection<Pair<KMeshInstanceTransformed, SBInstance>>>
    rendererGetScene()
      throws ConstraintError;
}

interface SBSceneControllerRendererControl
{
  public @Nonnull Future<SBCacheStatistics> rendererGetCacheStatistics();

  public @Nonnull RMatrixI4x4F<RTransformProjection> rendererGetProjection();

  public void rendererSetBackgroundColour(
    float r,
    float g,
    float b);

  public void rendererSetCustomProjection(
    final @Nonnull RMatrixI4x4F<RTransformProjection> p);

  public void rendererSetType(
    final @Nonnull SBKRendererType type);

  public void rendererPostprocessorSetType(
    final @Nonnull SBKPostprocessorType type);

  public void rendererShowAxes(
    final boolean enabled);

  public void rendererShowGrid(
    final boolean enabled);

  public void rendererShowLightRadii(
    final boolean enabled);

  public void rendererShowLights(
    final boolean enabled);

  public void rendererUnsetCustomProjection();
}

interface SBSceneControllerShaders
{
  public @Nonnull Future<SBShader> shaderLoad(
    final @Nonnull File file)
    throws ConstraintError;

  public @Nonnull Map<String, SBShader> shadersGet();
}

interface SBSceneControllerTextures extends SBSceneChangeListenerRegistration
{
  public @Nonnull
    <T extends SBTexture2DKind>
    Future<SBTexture2D<T>>
    sceneTexture2DLoad(
      final @Nonnull File file,
      final @Nonnull TextureWrapS wrap_s,
      final @Nonnull TextureWrapT wrap_t,
      final @Nonnull TextureFilterMinification filter_min,
      final @Nonnull TextureFilterMagnification filter_mag)
      throws ConstraintError;

  public @Nonnull Future<SBTextureCube> sceneTextureCubeLoad(
    final @Nonnull File file,
    final @Nonnull TextureWrapR wrap_r,
    final @Nonnull TextureWrapS wrap_s,
    final @Nonnull TextureWrapT wrap_t,
    final @Nonnull TextureFilterMinification filter_min,
    final @Nonnull TextureFilterMagnification filter_mag)
    throws ConstraintError;

  public @Nonnull Map<PathVirtual, SBTexture2D<?>> sceneTextures2DGet();

  public @Nonnull Map<PathVirtual, SBTextureCube> sceneTexturesCubeGet();
}
