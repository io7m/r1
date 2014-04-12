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

package com.io7m.renderer.kernel.sandbox;

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

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;

import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.pcollections.HashTreePSet;
import org.pcollections.MapPSet;
import org.pcollections.PMap;
import org.pcollections.PSet;
import org.xml.sax.SAXException;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnimplementedCodeException;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Pair;
import com.io7m.jaux.functional.Unit;
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
import com.io7m.jvvfs.FilesystemError;
import com.io7m.jvvfs.PathVirtual;
import com.io7m.renderer.kernel.types.KInstanceOpaqueAlphaDepth;
import com.io7m.renderer.kernel.types.KInstanceOpaqueRegular;
import com.io7m.renderer.kernel.types.KInstanceTransformedOpaqueAlphaDepth;
import com.io7m.renderer.kernel.types.KInstanceTransformedOpaqueRegular;
import com.io7m.renderer.kernel.types.KInstanceTransformedTranslucentRefractive;
import com.io7m.renderer.kernel.types.KInstanceTransformedTranslucentRegular;
import com.io7m.renderer.kernel.types.KInstanceTransformedType;
import com.io7m.renderer.kernel.types.KInstanceTranslucentRefractive;
import com.io7m.renderer.kernel.types.KInstanceTranslucentRegular;
import com.io7m.renderer.kernel.types.KLightProjective;
import com.io7m.renderer.kernel.types.KMaterialAlbedo;
import com.io7m.renderer.kernel.types.KMaterialAlpha;
import com.io7m.renderer.kernel.types.KMaterialEmissive;
import com.io7m.renderer.kernel.types.KMaterialEnvironment;
import com.io7m.renderer.kernel.types.KMaterialNormal;
import com.io7m.renderer.kernel.types.KMaterialOpaqueAlphaDepth;
import com.io7m.renderer.kernel.types.KMaterialOpaqueRegular;
import com.io7m.renderer.kernel.types.KMaterialOpaqueType;
import com.io7m.renderer.kernel.types.KMaterialOpaqueVisitorType;
import com.io7m.renderer.kernel.types.KMaterialRefractive;
import com.io7m.renderer.kernel.types.KMaterialSpecular;
import com.io7m.renderer.kernel.types.KMaterialTranslucentRefractive;
import com.io7m.renderer.kernel.types.KMaterialTranslucentRegular;
import com.io7m.renderer.kernel.types.KMaterialTranslucentType;
import com.io7m.renderer.kernel.types.KMaterialTranslucentVisitorType;
import com.io7m.renderer.kernel.types.KMaterialType;
import com.io7m.renderer.kernel.types.KMaterialVisitorType;
import com.io7m.renderer.kernel.types.KMesh;
import com.io7m.renderer.kernel.types.KTransformOST;
import com.io7m.renderer.kernel.types.KTransformType;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RMatrixI4x4F;
import com.io7m.renderer.types.RTransformProjectionType;
import com.io7m.renderer.types.RXMLException;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.mapper.CannotResolveClassException;

public final class SBSceneController implements
  SBSceneControllerIO,
  SBSceneControllerLights,
  SBSceneControllerMeshes,
  SBSceneControllerMaterials,
  SBSceneControllerInstances,
  SBSceneControllerRenderer,
  SBSceneControllerRendererControl,
  SBSceneControllerShaders,
  SBSceneControllerTextures
{
  private static class SceneAndFilesystem
  {
    final @Nonnull SBSceneFilesystem filesystem;
    final @Nonnull SBScene           scene;

    SceneAndFilesystem(
      final @Nonnull SBScene in_scene,
      final @Nonnull SBSceneFilesystem in_filesystem)
    {
      this.scene = in_scene;
      this.filesystem = in_filesystem;
    }
  }

  @SuppressWarnings("boxing") private static @Nonnull
    SBScene
    internalIOSceneLoadLights(
      final @Nonnull SBSceneDescription sd,
      final @Nonnull SBScene scene,
      final @Nonnull Log log)
      throws ConstraintError,
        RException
  {
    final AtomicReference<SBScene> scene_temp =
      new AtomicReference<SBScene>(scene);

    final Collection<SBLightDescription> lights = sd.getLights();
    log.debug(String.format("loading %d lights", lights.size()));

    for (final SBLightDescription light : lights) {
      light
        .lightDescriptionVisitableAccept(new SBLightDescriptionVisitor<Unit, RException>() {
          @Override public Unit lightVisitDirectional(
            final @Nonnull SBLightDescriptionDirectional ld)
            throws ConstraintError,
              RException
          {
            final SBScene s = scene_temp.get();
            scene_temp.set(s.lightAdd(new SBLightDirectional(ld)));
            return Unit.unit();
          }

          @Override public Unit lightVisitProjective(
            final @Nonnull SBLightDescriptionProjective ld)
            throws ConstraintError,
              RException
          {
            final SBScene s = scene_temp.get();
            final SBTexture2D<SBTexture2DKindAlbedo> t =
              s.texture2DGet(ld.getTexture());

            final KLightProjective kp =
              KLightProjective.newProjective(
                ld.getID(),
                t.getTexture(),
                ld.getPosition(),
                ld.getOrientation(),
                ld.getColour(),
                ld.getIntensity(),
                (float) ld.getProjection().getFar(),
                ld.getFalloff(),
                ld.getProjectionMatrix(),
                ld.getShadow());

            scene_temp.set(s.lightAdd(new SBLightProjective(ld, kp)));
            return Unit.unit();
          }

          @Override public Unit lightVisitSpherical(
            final @Nonnull SBLightDescriptionSpherical ld)
            throws ConstraintError,
              RException
          {
            final SBScene s = scene_temp.get();
            scene_temp.set(s.lightAdd(new SBLightSpherical(ld)));
            return Unit.unit();
          }
        });
    }

    return scene_temp.get();
  }

  @SuppressWarnings("boxing") private static @Nonnull
    SBScene
    internalIOSceneLoadMaterials(
      final @Nonnull SBSceneDescription sd,
      final @Nonnull SBScene scene,
      final @Nonnull Log log)
      throws ConstraintError,
        RException
  {
    final AtomicReference<SBScene> scene_temp =
      new AtomicReference<SBScene>(scene);

    final PMap<Integer, SBMaterialDescription> materials = sd.getMaterials();
    log.debug(String.format("loading %d materials", materials.size()));

    for (final Integer id : materials.keySet()) {
      final SBMaterialDescription md = materials.get(id);
      final SBScene s = scene_temp.get();
      final Map<PathVirtual, SBTexture2D<?>> tx2 = s.textures2DGet();
      final Map<PathVirtual, SBTextureCube> txc = s.texturesCubeGet();
      scene_temp.set(s.materialPut(SBSceneController.materialFromDescription(
        id,
        md,
        tx2,
        txc)));
    }
    return scene_temp.get();
  }

  private static @Nonnull KMaterialType makeKMaterial(
    final @Nonnull Map<PathVirtual, SBTexture2D<?>> textures_2d,
    final @Nonnull Map<PathVirtual, SBTextureCube> textures_cubes,
    final @Nonnull SBMaterial m)
    throws RException,
      ConstraintError
  {
    return m
      .materialVisitableAccept(new SBMaterialVisitor<KMaterialType, RException>() {
        @Override public KMaterialType materialVisitOpaque(
          final @Nonnull SBMaterialOpaque mo)
          throws ConstraintError,
            RException
        {
          return mo
            .materialOpaqueVisitableAccept(new SBMaterialOpaqueVisitor<KMaterialType, RException>() {
              @Override public KMaterialType materialVisitOpaqueAlphaDepth(
                final @Nonnull SBMaterialOpaqueAlphaToDepth moa)
                throws ConstraintError,
                  RException
              {
                final SBMaterialDescriptionOpaqueAlphaToDepth md =
                  moa.materialGetDescription();

                final KMaterialNormal in_normal =
                  SBSceneController.makeKMaterialNormal(
                    textures_2d,
                    md.getNormal());

                final KMaterialAlbedo in_albedo =
                  SBSceneController.makeKMaterialAlbedo(
                    textures_2d,
                    md.getAlbedo());

                final KMaterialEmissive in_emissive =
                  SBSceneController.makeKMaterialEmissive(
                    textures_2d,
                    md.getEmissive());

                final KMaterialEnvironment in_environment =
                  SBSceneController.makeKMaterialEnvironment(
                    textures_cubes,
                    md.getEnvironment());

                final KMaterialSpecular in_specular =
                  SBSceneController.makeKMaterialSpecular(
                    textures_2d,
                    md.getSpecular());

                return KMaterialOpaqueAlphaDepth.newMaterial(
                  md.getUVMatrix(),
                  in_normal,
                  in_albedo,
                  in_emissive,
                  in_environment,
                  in_specular,
                  md.getAlphaThreshold());
              }

              @Override public KMaterialType materialVisitOpaqueRegular(
                final @Nonnull SBMaterialOpaqueRegular mor)
                throws ConstraintError,
                  RException
              {
                final SBMaterialDescriptionOpaqueRegular md =
                  mor.materialGetDescription();

                final KMaterialNormal in_normal =
                  SBSceneController.makeKMaterialNormal(
                    textures_2d,
                    md.getNormal());

                final KMaterialAlbedo in_albedo =
                  SBSceneController.makeKMaterialAlbedo(
                    textures_2d,
                    md.getAlbedo());

                final KMaterialEmissive in_emissive =
                  SBSceneController.makeKMaterialEmissive(
                    textures_2d,
                    md.getEmissive());

                final KMaterialEnvironment in_environment =
                  SBSceneController.makeKMaterialEnvironment(
                    textures_cubes,
                    md.getEnvironment());

                final KMaterialSpecular in_specular =
                  SBSceneController.makeKMaterialSpecular(
                    textures_2d,
                    md.getSpecular());

                return KMaterialOpaqueRegular.newMaterial(
                  md.getUVMatrix(),
                  in_normal,
                  in_albedo,
                  in_emissive,
                  in_environment,
                  in_specular);
              }
            });
        }

        @Override public KMaterialType materialVisitTranslucent(
          final @Nonnull SBMaterialTranslucent mt)
          throws ConstraintError,
            RException
        {
          return mt
            .materialTranslucentVisitableAccept(new SBMaterialTranslucentVisitor<KMaterialType, RException>() {
              @Override public
                KMaterialType
                materialVisitTranslucentRefractive(
                  final @Nonnull SBMaterialTranslucentRefractive mtr)
                  throws ConstraintError,
                    RException
              {
                final SBMaterialDescriptionTranslucentRefractive md =
                  mtr.materialGetDescription();

                final KMaterialNormal in_normal =
                  SBSceneController.makeKMaterialNormal(
                    textures_2d,
                    md.getNormal());

                final KMaterialRefractive in_refractive =
                  KMaterialRefractive.newRefractive(md
                    .getRefractive()
                    .getScale(), md.getRefractive().isMasked());

                return KMaterialTranslucentRefractive.newMaterial(
                  md.getUVMatrix(),
                  in_normal,
                  in_refractive);
              }

              @SuppressWarnings("synthetic-access") @Override public
                KMaterialType
                materialVisitTranslucentRegular(
                  final @Nonnull SBMaterialTranslucentRegular mtr)
                  throws ConstraintError,
                    RException
              {
                final SBMaterialDescriptionTranslucentRegular md =
                  mtr.materialGetDescription();

                final KMaterialAlpha in_alpha =
                  SBSceneController.makeKMaterialAlpha(md.getAlpha());

                final KMaterialNormal in_normal =
                  SBSceneController.makeKMaterialNormal(
                    textures_2d,
                    md.getNormal());

                final KMaterialAlbedo in_albedo =
                  SBSceneController.makeKMaterialAlbedo(
                    textures_2d,
                    md.getAlbedo());

                final KMaterialEmissive in_emissive =
                  SBSceneController.makeKMaterialEmissive(
                    textures_2d,
                    md.getEmissive());

                final KMaterialEnvironment in_environment =
                  SBSceneController.makeKMaterialEnvironment(
                    textures_cubes,
                    md.getEnvironment());

                final KMaterialSpecular in_specular =
                  SBSceneController.makeKMaterialSpecular(
                    textures_2d,
                    md.getSpecular());

                return KMaterialTranslucentRegular.newMaterial(
                  md.getUVMatrix(),
                  in_albedo,
                  in_alpha,
                  in_emissive,
                  in_environment,
                  in_normal,
                  in_specular);
              }
            });
        }
      });
  }

  static @Nonnull KMaterialAlbedo makeKMaterialAlbedo(
    final @Nonnull Map<PathVirtual, SBTexture2D<?>> textures,
    final @Nonnull SBMaterialAlbedoDescription d)
    throws ConstraintError
  {
    final PathVirtual p = d.getTexture();
    if (p != null) {
      final SBTexture2D<?> t = textures.get(p);
      return KMaterialAlbedo.newAlbedoTextured(
        d.getColour(),
        d.getMix(),
        t.getTexture());
    }
    return KMaterialAlbedo.newAlbedoUntextured(d.getColour());
  }

  private static @Nonnull KMaterialAlpha makeKMaterialAlpha(
    final @Nonnull SBMaterialAlphaDescription alpha)
    throws ConstraintError
  {
    return KMaterialAlpha.newAlpha(alpha.getType(), alpha.getOpacity());
  }

  static @Nonnull KMaterialEmissive makeKMaterialEmissive(
    final @Nonnull Map<PathVirtual, SBTexture2D<?>> textures,
    final @Nonnull SBMaterialEmissiveDescription d)
    throws ConstraintError
  {
    final PathVirtual p = d.getTexture();
    if (p != null) {
      final SBTexture2D<?> t = textures.get(p);
      return KMaterialEmissive.newEmissiveMapped(
        d.getEmission(),
        t.getTexture());
    }
    return KMaterialEmissive.newEmissiveUnmapped(d.getEmission());
  }

  static @Nonnull KMaterialEnvironment makeKMaterialEnvironment(
    final @Nonnull Map<PathVirtual, SBTextureCube> textures,
    final @Nonnull SBMaterialEnvironmentDescription d)
    throws ConstraintError
  {
    final PathVirtual p = d.getTexture();
    if (p != null) {
      final SBTextureCube t = textures.get(p);
      return KMaterialEnvironment.newEnvironmentMapped(
        d.getMix(),
        t.getTexture(),
        d.getMixMapped());
    }
    return KMaterialEnvironment.newEnvironmentUnmapped();
  }

  static @Nonnull KMaterialNormal makeKMaterialNormal(
    final @Nonnull Map<PathVirtual, SBTexture2D<?>> textures,
    final @Nonnull SBMaterialNormalDescription d)
    throws ConstraintError
  {
    final PathVirtual p = d.getTexture();
    if (p != null) {
      final SBTexture2D<?> t = textures.get(p);
      return KMaterialNormal.newNormalMapped(t.getTexture());
    }
    return KMaterialNormal.newNormalUnmapped();
  }

  static @Nonnull KMaterialSpecular makeKMaterialSpecular(
    final @Nonnull Map<PathVirtual, SBTexture2D<?>> textures,
    final @Nonnull SBMaterialSpecularDescription d)
    throws ConstraintError
  {
    final PathVirtual p = d.getTexture();
    if (p != null) {
      final SBTexture2D<?> t = textures.get(p);
      return KMaterialSpecular.newSpecularMapped(
        d.getColour(),
        d.getExponent(),
        t.getTexture());
    }
    return KMaterialSpecular.newSpecularUnmapped(
      d.getColour(),
      d.getExponent());
  }

  private static @Nonnull SBMaterial materialFromDescription(
    final @Nonnull Integer id,
    final @Nonnull SBMaterialDescription desc,
    final @Nonnull Map<PathVirtual, SBTexture2D<?>> tx2,
    final @Nonnull Map<PathVirtual, SBTextureCube> txc)
    throws RException,
      ConstraintError
  {
    final SBMaterial m =
      desc
        .materialDescriptionVisitableAccept(new SBMaterialDescriptionVisitor<SBMaterial, RException>() {
          @Override public SBMaterial materialDescriptionVisitOpaque(
            final @Nonnull SBMaterialDescriptionOpaque mo)
            throws ConstraintError,
              RException
          {
            return mo
              .materialDescriptionOpaqueVisitableAccept(new SBMaterialDescriptionOpaqueVisitor<SBMaterial, RException>() {
                @Override public
                  SBMaterial
                  materialDescriptionVisitOpaqueAlphaDepth(
                    final @Nonnull SBMaterialDescriptionOpaqueAlphaToDepth moatd)
                    throws ConstraintError,
                      RException
                {
                  final SBMaterialAlbedoDescription albedo =
                    moatd.getAlbedo();
                  final SBMaterialEmissiveDescription emissive =
                    moatd.getEmissive();
                  final SBMaterialNormalDescription normal =
                    moatd.getNormal();
                  final SBMaterialSpecularDescription specular =
                    moatd.getSpecular();
                  final SBMaterialEnvironmentDescription environment =
                    moatd.getEnvironment();

                  @SuppressWarnings("unchecked") final SBTexture2D<SBTexture2DKindAlbedo> map_diffuse =
                    (SBTexture2D<SBTexture2DKindAlbedo>) (albedo.getTexture() != null
                      ? tx2.get(albedo.getTexture())
                      : null);
                  @SuppressWarnings("unchecked") final SBTexture2D<SBTexture2DKindEmissive> map_emissive =
                    (SBTexture2D<SBTexture2DKindEmissive>) (emissive
                      .getTexture() != null
                      ? tx2.get(emissive.getTexture())
                      : null);
                  @SuppressWarnings("unchecked") final SBTexture2D<SBTexture2DKindNormal> map_normal =
                    (SBTexture2D<SBTexture2DKindNormal>) (normal.getTexture() != null
                      ? tx2.get(normal.getTexture())
                      : null);
                  @SuppressWarnings("unchecked") final SBTexture2D<SBTexture2DKindSpecular> map_specular =
                    (SBTexture2D<SBTexture2DKindSpecular>) (specular
                      .getTexture() != null
                      ? tx2.get(specular.getTexture())
                      : null);
                  final SBTextureCube map_environment =
                    environment.getTexture() != null ? txc.get(environment
                      .getTexture()) : null;

                  return new SBMaterialOpaqueAlphaToDepth(
                    id,
                    moatd,
                    map_diffuse,
                    map_emissive,
                    map_environment,
                    map_normal,
                    map_specular);
                }

                @Override public
                  SBMaterial
                  materialDescriptionVisitOpaqueRegular(
                    final @Nonnull SBMaterialDescriptionOpaqueRegular mor)
                    throws ConstraintError,
                      RException
                {
                  final SBMaterialAlbedoDescription albedo = mor.getAlbedo();
                  final SBMaterialEmissiveDescription emissive =
                    mor.getEmissive();
                  final SBMaterialNormalDescription normal = mor.getNormal();
                  final SBMaterialSpecularDescription specular =
                    mor.getSpecular();
                  final SBMaterialEnvironmentDescription environment =
                    mor.getEnvironment();

                  @SuppressWarnings("unchecked") final SBTexture2D<SBTexture2DKindAlbedo> map_diffuse =
                    (SBTexture2D<SBTexture2DKindAlbedo>) (albedo.getTexture() != null
                      ? tx2.get(albedo.getTexture())
                      : null);
                  @SuppressWarnings("unchecked") final SBTexture2D<SBTexture2DKindEmissive> map_emissive =
                    (SBTexture2D<SBTexture2DKindEmissive>) (emissive
                      .getTexture() != null
                      ? tx2.get(emissive.getTexture())
                      : null);
                  @SuppressWarnings("unchecked") final SBTexture2D<SBTexture2DKindNormal> map_normal =
                    (SBTexture2D<SBTexture2DKindNormal>) (normal.getTexture() != null
                      ? tx2.get(normal.getTexture())
                      : null);
                  @SuppressWarnings("unchecked") final SBTexture2D<SBTexture2DKindSpecular> map_specular =
                    (SBTexture2D<SBTexture2DKindSpecular>) (specular
                      .getTexture() != null
                      ? tx2.get(specular.getTexture())
                      : null);
                  final SBTextureCube map_environment =
                    environment.getTexture() != null ? txc.get(environment
                      .getTexture()) : null;

                  return new SBMaterialOpaqueRegular(
                    id,
                    mor,
                    map_diffuse,
                    map_emissive,
                    map_environment,
                    map_normal,
                    map_specular);
                }
              });
          }

          @Override public SBMaterial materialDescriptionVisitTranslucent(
            final @Nonnull SBMaterialDescriptionTranslucent mt)
            throws ConstraintError,
              RException
          {
            return mt
              .materialDescriptionTranslucentVisitableAccept(new SBMaterialDescriptionTranslucentVisitor<SBMaterial, RException>() {
                @Override public
                  SBMaterial
                  materialDescriptionVisitTranslucentRefractive(
                    final @Nonnull SBMaterialDescriptionTranslucentRefractive mtr)
                    throws ConstraintError,
                      RException
                {
                  final SBMaterialNormalDescription normal = mtr.getNormal();

                  @SuppressWarnings("unchecked") final SBTexture2D<SBTexture2DKindNormal> map_normal =
                    (SBTexture2D<SBTexture2DKindNormal>) (normal.getTexture() != null
                      ? tx2.get(normal.getTexture())
                      : null);

                  return new SBMaterialTranslucentRefractive(
                    id,
                    mtr,
                    map_normal);
                }

                @Override public
                  SBMaterial
                  materialDescriptionVisitTranslucentRegular(
                    final @Nonnull SBMaterialDescriptionTranslucentRegular mtr)
                    throws ConstraintError,
                      RException
                {
                  final SBMaterialAlbedoDescription albedo = mtr.getAlbedo();
                  final SBMaterialEmissiveDescription emissive =
                    mtr.getEmissive();
                  final SBMaterialNormalDescription normal = mtr.getNormal();
                  final SBMaterialSpecularDescription specular =
                    mtr.getSpecular();
                  final SBMaterialEnvironmentDescription environment =
                    mtr.getEnvironment();

                  @SuppressWarnings("unchecked") final SBTexture2D<SBTexture2DKindAlbedo> map_diffuse =
                    (SBTexture2D<SBTexture2DKindAlbedo>) (albedo.getTexture() != null
                      ? tx2.get(albedo.getTexture())
                      : null);
                  @SuppressWarnings("unchecked") final SBTexture2D<SBTexture2DKindEmissive> map_emissive =
                    (SBTexture2D<SBTexture2DKindEmissive>) (emissive
                      .getTexture() != null
                      ? tx2.get(emissive.getTexture())
                      : null);
                  @SuppressWarnings("unchecked") final SBTexture2D<SBTexture2DKindNormal> map_normal =
                    (SBTexture2D<SBTexture2DKindNormal>) (normal.getTexture() != null
                      ? tx2.get(normal.getTexture())
                      : null);
                  @SuppressWarnings("unchecked") final SBTexture2D<SBTexture2DKindSpecular> map_specular =
                    (SBTexture2D<SBTexture2DKindSpecular>) (specular
                      .getTexture() != null
                      ? tx2.get(specular.getTexture())
                      : null);
                  final SBTextureCube map_environment =
                    environment.getTexture() != null ? txc.get(environment
                      .getTexture()) : null;

                  return new SBMaterialTranslucentRegular(
                    id,
                    mtr,
                    map_diffuse,
                    map_emissive,
                    map_environment,
                    map_normal,
                    map_specular);
                }
              });
          }
        });
    return m;
  }

  private final @Nonnull ExecutorService                     exec_pool;
  private final @Nonnull LinkedList<SBSceneChangeListener>   listeners;
  private final @Nonnull Log                                 log;
  private final @Nonnull SBGLRenderer                        renderer;
  private final @Nonnull Map<String, SBShader>               shaders;
  private final @Nonnull AtomicReference<SceneAndFilesystem> state_current;

  public SBSceneController(
    final @Nonnull SBGLRenderer in_renderer,
    final @Nonnull Log in_log)
    throws FilesystemError,
      IOException,
      ConstraintError
  {
    this.renderer = in_renderer;
    this.log = new Log(in_log, "control");
    this.listeners = new LinkedList<SBSceneChangeListener>();
    this.shaders = new ConcurrentHashMap<String, SBShader>();
    this.state_current =
      new AtomicReference<SceneAndFilesystem>(new SceneAndFilesystem(
        SBScene.empty(),
        SBSceneFilesystem.filesystemEmpty(in_log)));
    this.exec_pool = Executors.newCachedThreadPool();
  }

  private @Nonnull SceneAndFilesystem internalIOSceneLoad(
    final @Nonnull File file)
    throws FileNotFoundException,
      FilesystemError,
      IOException,
      ConstraintError,
      InterruptedException,
      ExecutionException,
      RException
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
      scene = this.internalIOSceneLoadTextures2D(fs, sd, scene);
      scene = this.internalIOSceneLoadTexturesCube(fs, sd, scene);
      scene =
        SBSceneController.internalIOSceneLoadMaterials(sd, scene, this.log);
      scene = this.internalIOSceneLoadMeshes(fs, sd, scene);
      scene =
        SBSceneController.internalIOSceneLoadLights(sd, scene, this.log);

      for (final SBInstance i : sd.getInstances()) {
        scene = scene.instanceAdd(i);
      }

      return new SceneAndFilesystem(scene, fs);

    } finally {
      xms.close();
    }
  }

  @SuppressWarnings("boxing") private @Nonnull
    SBScene
    internalIOSceneLoadMeshes(
      final @Nonnull SBSceneFilesystem fs,
      final @Nonnull SBSceneDescription sd,
      final @Nonnull SBScene scene)
      throws FilesystemError,
        ConstraintError,
        InterruptedException,
        ExecutionException,
        IOException
  {
    SBScene s = scene;

    final PSet<PathVirtual> meshes = sd.getMeshes();
    this.log.debug(String.format("loading %d meshes", meshes.size()));

    for (final PathVirtual name : meshes) {
      final SBMesh m = this.internalMeshLoadFromPath(fs, name);
      s = s.meshAdd(m);
    }
    return s;
  }

  @SuppressWarnings("boxing") private @Nonnull
    SBScene
    internalIOSceneLoadTextures2D(
      final @Nonnull SBSceneFilesystem fs,
      final @Nonnull SBSceneDescription sd,
      final @Nonnull SBScene scene)
      throws FilesystemError,
        ConstraintError,
        IOException,
        InterruptedException,
        ExecutionException
  {
    SBScene s = scene;

    final PSet<SBTexture2DDescription> textures = sd.getTextures2D();
    this.log.debug(String.format("loading %d 2D textures", textures.size()));

    for (final SBTexture2DDescription desc : textures) {
      final SBTexture2D<?> t =
        this.internalTexture2DLoadFromPath(
          fs,
          desc.getPath(),
          desc.getWrapModeS(),
          desc.getWrapModeT(),
          desc.getTextureMin(),
          desc.getTextureMag());
      s = s.texture2DAdd(t);
    }
    return s;
  }

  @SuppressWarnings("boxing") private @Nonnull
    SBScene
    internalIOSceneLoadTexturesCube(
      final @Nonnull SBSceneFilesystem fs,
      final @Nonnull SBSceneDescription sd,
      final @Nonnull SBScene scene)
      throws FilesystemError,
        ConstraintError,
        IOException,
        InterruptedException,
        ExecutionException
  {
    SBScene s = scene;

    final PSet<SBTextureCubeDescription> cubes = sd.getTexturesCube();
    this.log.debug(String.format("loading %d cube textures", cubes.size()));

    for (final SBTextureCubeDescription desc : cubes) {
      final SBTextureCube t =
        this.internalTextureCubeLoadFromPath(
          fs,
          desc.getPath(),
          desc.getWrapModeR(),
          desc.getWrapModeS(),
          desc.getWrapModeT(),
          desc.getTextureMin(),
          desc.getTextureMag());
      s = s.textureCubeAdd(t);
    }
    return s;
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
      RException
  {
    final FileInputStream stream = new FileInputStream(file);
    try {
      final PGLSLMetaXML meta = PGLSLMetaXML.fromStream(stream, this.log);
      final Future<SBShader> f =
        this.renderer.shaderLoad(file.getParentFile(), meta);
      return f.get();
    } catch (final SAXException x) {
      throw RXMLException.saxException(x);
    } catch (final ParserConfigurationException x) {
      throw RXMLException.parserConfigurationException(x);
    } catch (final ParsingException x) {
      throw RXMLException.parsingException(x);
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
      RXMLException
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

  @Override public Future<SBCacheStatistics> rendererGetCacheStatistics()
  {
    return this.renderer.getCacheStatistics();
  }

  @Override public
    RMatrixI4x4F<RTransformProjectionType>
    rendererGetProjection()
  {
    return this.renderer.getProjection();
  }

  @Override public @Nonnull
    Pair<Collection<SBLight>, Collection<Pair<KInstanceTransformedType, SBInstance>>>
    rendererGetScene()
      throws ConstraintError,
        RException
  {
    final SceneAndFilesystem saf = this.state_current.get();
    final SBScene scene = saf.scene;

    final Collection<SBLight> lights = scene.lightsGet();
    MapPSet<Pair<KInstanceTransformedType, SBInstance>> meshes =
      HashTreePSet.empty();

    for (final SBInstance i : scene.instancesGet()) {
      final PathVirtual mesh_path = i.getMesh();
      final SBMesh mesh = scene.meshGet(mesh_path);
      final Integer mat_id = i.getMaterial();
      final SBMaterial mat = scene.materialGet(mat_id);
      final Integer instance_id = i.getID();

      final KTransformType kt =
        KTransformOST.newTransform(
          i.getOrientation(),
          i.getScale(),
          i.getPosition());

      final KMaterialType kmat =
        SBSceneController.makeKMaterial(
          scene.textures2DGet(),
          scene.texturesCubeGet(),
          mat);

      final KMesh km = mesh.getMesh();

      final KInstanceTransformedType kit =
        kmat
          .materialVisitableAccept(new KMaterialVisitorType<KInstanceTransformedType, RException>() {
            @Override public KInstanceTransformedType materialVisitOpaque(
              final @Nonnull KMaterialOpaqueType m)
              throws ConstraintError,
                RException
            {
              return m
                .materialOpaqueVisitableAccept(new KMaterialOpaqueVisitorType<KInstanceTransformedType, RException>() {
                  @Override public
                    KInstanceTransformedType
                    materialVisitOpaqueAlphaDepth(
                      final KMaterialOpaqueAlphaDepth mo)
                      throws ConstraintError,
                        RException
                  {
                    final KInstanceOpaqueAlphaDepth ki =
                      KInstanceOpaqueAlphaDepth.newInstance(
                        mo,
                        km,
                        i.getFaces());
                    return KInstanceTransformedOpaqueAlphaDepth.newInstance(
                      ki,
                      kt,
                      i.getUVMatrix());
                  }

                  @Override public
                    KInstanceTransformedType
                    materialVisitOpaqueRegular(
                      final @Nonnull KMaterialOpaqueRegular mo)
                      throws ConstraintError,
                        RException
                  {
                    final KInstanceOpaqueRegular ki =
                      KInstanceOpaqueRegular.newInstance(mo, km, i.getFaces());
                    return KInstanceTransformedOpaqueRegular.newInstance(
                      ki,
                      kt,
                      i.getUVMatrix());
                  }
                });
            }

            @Override public
              KInstanceTransformedType
              materialVisitTranslucent(
                final @Nonnull KMaterialTranslucentType m)
                throws ConstraintError,
                  RException
            {
              return m
                .materialTranslucentVisitableAccept(new KMaterialTranslucentVisitorType<KInstanceTransformedType, RException>() {
                  @Override public
                    KInstanceTransformedType
                    materialVisitTranslucentRefractive(
                      final @Nonnull KMaterialTranslucentRefractive mtr)
                      throws RException,
                        ConstraintError
                  {
                    final KInstanceTranslucentRefractive ki =
                      KInstanceTranslucentRefractive.newInstance(
                        mtr,
                        km,
                        i.getFaces());
                    return KInstanceTransformedTranslucentRefractive
                      .newInstance(ki, kt, i.getUVMatrix());
                  }

                  @Override public
                    KInstanceTransformedType
                    materialVisitTranslucentRegular(
                      final @Nonnull KMaterialTranslucentRegular mtr)
                      throws RException,
                        ConstraintError
                  {
                    final KInstanceTranslucentRegular ki =
                      KInstanceTranslucentRegular.newInstance(
                        mtr,
                        km,
                        i.getFaces());
                    return KInstanceTransformedTranslucentRegular
                      .newInstance(ki, kt, i.getUVMatrix());
                  }
                });
            }
          });

      meshes =
        meshes.plus(new Pair<KInstanceTransformedType, SBInstance>(kit, i));
    }

    return new Pair<Collection<SBLight>, Collection<Pair<KInstanceTransformedType, SBInstance>>>(
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
    final @Nonnull RMatrixI4x4F<RTransformProjectionType> p)
  {
    this.renderer.setCustomProjection(p);
  }

  @Override public void rendererSetType(
    final @Nonnull SBKRendererSelectionType type)
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

  @Override public void sceneInstancePut(
    final @Nonnull SBInstance i)
    throws ConstraintError
  {
    this.internalStateUpdateSceneOnly(this.state_current.get().scene
      .instanceAdd(i));
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

  @SuppressWarnings("synthetic-access") @Override public void sceneLightPut(
    final @Nonnull SBLightDescription d)
    throws ConstraintError,
      RException
  {
    final AtomicReference<SceneAndFilesystem> state =
      SBSceneController.this.state_current;

    d
      .lightDescriptionVisitableAccept(new SBLightDescriptionVisitor<Unit, ConstraintError>() {
        @Override public Unit lightVisitDirectional(
          final @Nonnull SBLightDescriptionDirectional ld)
          throws ConstraintError,
            RException,
            ConstraintError
        {
          final SBLightDirectional l = new SBLightDirectional(ld);
          SBSceneController.this.internalStateUpdateSceneOnly(state.get().scene
            .lightAdd(l));
          return Unit.unit();
        }

        @Override public Unit lightVisitProjective(
          final @Nonnull SBLightDescriptionProjective ld)
          throws ConstraintError,
            RException,
            ConstraintError
        {
          @SuppressWarnings("unchecked") final SBTexture2D<SBTexture2DKindAlbedo> t =
            (SBTexture2D<SBTexture2DKindAlbedo>) SBSceneController.this
              .sceneTextures2DGet()
              .get(ld.getTexture());

          final KLightProjective kp =
            KLightProjective.newProjective(
              ld.getID(),
              t.getTexture(),
              ld.getPosition(),
              ld.getOrientation(),
              ld.getColour(),
              ld.getIntensity(),
              (float) ld.getProjection().getFar(),
              ld.getFalloff(),
              ld.getProjectionMatrix(),
              ld.getShadow());

          final SBLightProjective l = new SBLightProjective(ld, kp);
          SBSceneController.this.internalStateUpdateSceneOnly(state.get().scene
            .lightAdd(l));
          return Unit.unit();
        }

        @Override public Unit lightVisitSpherical(
          final @Nonnull SBLightDescriptionSpherical ld)
          throws ConstraintError,
            RException,
            ConstraintError
        {
          final SBLightSpherical l = new SBLightSpherical(ld);
          SBSceneController.this.internalStateUpdateSceneOnly(state.get().scene
            .lightAdd(l));
          return Unit.unit();
        }
      });
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

  @Override public boolean sceneMaterialExists(
    final @Nonnull Integer id)
    throws ConstraintError
  {
    return this.state_current.get().scene.materialExists(id);
  }

  @Override public Integer sceneMaterialFreshID()
  {
    final Pair<SBScene, Integer> p =
      this.state_current.get().scene.materialFreshID();
    this.internalStateUpdateSceneOnly(p.first);
    return p.second;
  }

  @Override public SBMaterial sceneMaterialGet(
    final @Nonnull Integer id)
    throws ConstraintError
  {
    return this.state_current.get().scene.materialGet(id);
  }

  @Override public void sceneMaterialPut(
    final @Nonnull SBMaterial material)
    throws ConstraintError
  {
    this.internalStateUpdateSceneOnly(this.state_current.get().scene
      .materialPut(material));
  }

  @Override public void sceneMaterialPutByDescription(
    final @Nonnull Integer id,
    final @Nonnull SBMaterialDescription desc)
    throws ConstraintError,
      RException
  {
    final Map<PathVirtual, SBTexture2D<?>> tx2 =
      SBSceneController.this.sceneTextures2DGet();
    final Map<PathVirtual, SBTextureCube> txc =
      SBSceneController.this.sceneTexturesCubeGet();

    final SBMaterial m =
      SBSceneController.materialFromDescription(id, desc, tx2, txc);

    this.sceneMaterialPut(m);
  }

  @Override public void sceneMaterialRemove(
    final @Nonnull Integer id)
    throws ConstraintError
  {
    // TODO Auto-generated method stub
    throw new UnimplementedCodeException();
  }

  @Override public Collection<SBMaterial> sceneMaterialsGetAll()
  {
    return this.state_current.get().scene.materialsGet();
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

  @Override public void rendererPostprocessorSet(
    final @Nonnull SBKPostprocessor p)
  {
    this.renderer.setPostprocessor(p);
  }
}
