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

import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;

import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.pcollections.HashTreePSet;
import org.pcollections.MapPSet;
import org.pcollections.PMap;
import org.pcollections.PSet;
import org.xml.sax.SAXException;

import com.io7m.jcanephora.CMFNegativeXKind;
import com.io7m.jcanephora.CMFNegativeYKind;
import com.io7m.jcanephora.CMFNegativeZKind;
import com.io7m.jcanephora.CMFPositiveXKind;
import com.io7m.jcanephora.CMFPositiveYKind;
import com.io7m.jcanephora.CMFPositiveZKind;
import com.io7m.jcanephora.CubeMapFaceInputStream;
import com.io7m.jcanephora.Texture2DStaticType;
import com.io7m.jcanephora.TextureCubeStaticType;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jcanephora.TextureWrapR;
import com.io7m.jcanephora.TextureWrapS;
import com.io7m.jcanephora.TextureWrapT;
import com.io7m.jfunctional.Pair;
import com.io7m.jfunctional.Unit;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.NullCheckException;
import com.io7m.jnull.Nullable;
import com.io7m.jparasol.xml.PGLSLMetaXML;
import com.io7m.junreachable.UnimplementedCodeException;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.jvvfs.PathVirtual;
import com.io7m.renderer.kernel.types.KMeshWithMaterialOpaqueAlphaDepth;
import com.io7m.renderer.kernel.types.KMeshWithMaterialOpaqueRegular;
import com.io7m.renderer.kernel.types.KInstanceOpaqueAlphaDepth;
import com.io7m.renderer.kernel.types.KInstanceOpaqueRegular;
import com.io7m.renderer.kernel.types.KInstanceTranslucentRefractive;
import com.io7m.renderer.kernel.types.KInstanceTranslucentRegular;
import com.io7m.renderer.kernel.types.KInstanceTranslucentSpecularOnly;
import com.io7m.renderer.kernel.types.KInstanceType;
import com.io7m.renderer.kernel.types.KMeshWithMaterialTranslucentRefractive;
import com.io7m.renderer.kernel.types.KMeshWithMaterialTranslucentRegular;
import com.io7m.renderer.kernel.types.KMeshWithMaterialTranslucentSpecularOnly;
import com.io7m.renderer.kernel.types.KLightProjective;
import com.io7m.renderer.kernel.types.KLightProjectiveBuilderType;
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
import com.io7m.renderer.kernel.types.KMaterialTranslucentSpecularOnly;
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

@SuppressWarnings("boxing") public final class SBSceneController implements
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
    final SBSceneFilesystem filesystem;
    final SBScene           scene;

    SceneAndFilesystem(
      final SBScene in_scene,
      final SBSceneFilesystem in_filesystem)
    {
      this.scene = in_scene;
      this.filesystem = in_filesystem;
    }
  }

  private static SBScene internalIOSceneLoadLights(
    final SBSceneDescription sd,
    final SBScene scene,
    final LogUsableType log)
    throws RException
  {
    final AtomicReference<SBScene> scene_temp =
      new AtomicReference<SBScene>(scene);

    final Collection<SBLightDescription> lights = sd.getLights();
    log.debug(String.format("loading %d lights", lights.size()));

    for (final SBLightDescription light : lights) {
      light
        .lightDescriptionVisitableAccept(new SBLightDescriptionVisitor<Unit, RException>() {
          @Override public Unit lightVisitDirectional(
            final SBLightDescriptionDirectional ld)
            throws RException
          {
            final SBScene s = scene_temp.get();
            scene_temp.set(s.lightAdd(new SBLightDirectional(ld)));
            return Unit.unit();
          }

          @Override public Unit lightVisitProjective(
            final SBLightDescriptionProjective ld)
            throws RException
          {
            final SBScene s = scene_temp.get();
            final SBTexture2D<SBTexture2DKindAlbedo> t =
              s.texture2DGet(ld.getTexture());

            assert t != null;
            final KLightProjectiveBuilderType b =
              KLightProjective.newBuilder();
            b.setColor(ld.getColour());
            b.setFalloff(ld.getFalloff());
            b.setIntensity(ld.getIntensity());
            b.setOrientation(ld.getOrientation());
            b.setPosition(ld.getPosition());
            b.setProjection(ld.getProjectionMatrix());
            b.setRange((float) ld.getProjection().getFar());
            b.setShadowOption(ld.getShadow());
            b.setTexture(t.getTexture());
            final KLightProjective kp = b.build();

            scene_temp.set(s.lightAdd(new SBLightProjective(ld, kp)));
            return Unit.unit();
          }

          @Override public Unit lightVisitSpherical(
            final SBLightDescriptionSpherical ld)
            throws RException
          {
            final SBScene s = scene_temp.get();
            scene_temp.set(s.lightAdd(new SBLightSpherical(ld)));
            return Unit.unit();
          }
        });
    }

    return scene_temp.get();
  }

  private static SBScene internalIOSceneLoadMaterials(
    final SBSceneDescription sd,
    final SBScene scene,
    final LogUsableType log)
    throws RException
  {
    final AtomicReference<SBScene> scene_temp =
      new AtomicReference<SBScene>(scene);

    final PMap<Integer, SBMaterialDescription> materials = sd.getMaterials();
    final String text =
      String.format("loading %d materials", materials.size());
    assert text != null;
    log.debug(text);

    for (final Integer id : materials.keySet()) {
      assert id != null;
      final SBMaterialDescription md = materials.get(id);
      assert md != null;

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

  private static KMaterialType makeKMaterial(
    final Map<PathVirtual, SBTexture2D<?>> textures_2d,
    final Map<PathVirtual, SBTextureCube> textures_cubes,
    final SBMaterial m)
    throws RException
  {
    return m
      .materialVisitableAccept(new SBMaterialVisitor<KMaterialType, RException>() {
        @Override public KMaterialType materialVisitOpaque(
          final SBMaterialOpaque mo)
          throws RException
        {
          return mo
            .materialOpaqueVisitableAccept(new SBMaterialOpaqueVisitor<KMaterialType, RException>() {
              @Override public KMaterialType materialVisitOpaqueAlphaDepth(
                final SBMaterialOpaqueAlphaToDepth moa)
                throws RException
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
                final SBMaterialOpaqueRegular mor)
                throws RException
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
          final SBMaterialTranslucent mt)
          throws RException
        {
          return mt
            .materialTranslucentVisitableAccept(new SBMaterialTranslucentVisitor<KMaterialType, RException>() {
              @Override public
                KMaterialType
                materialVisitTranslucentRefractive(
                  final SBMaterialTranslucentRefractive mtr)
                  throws RException
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
                  final SBMaterialTranslucentRegular mtr)
                  throws RException
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

              @Override public
                KMaterialType
                materialVisitTranslucentSpecularOnly(
                  final SBMaterialTranslucentSpecularOnly mtr)
                  throws RException
              {
                final SBMaterialDescriptionTranslucentSpecularOnly md =
                  mtr.materialGetDescription();

                final KMaterialAlpha in_alpha =
                  SBSceneController.makeKMaterialAlpha(md.getAlpha());

                final KMaterialNormal in_normal =
                  SBSceneController.makeKMaterialNormal(
                    textures_2d,
                    md.getNormal());

                final KMaterialSpecular in_specular =
                  SBSceneController.makeKMaterialSpecular(
                    textures_2d,
                    md.getSpecular());

                return KMaterialTranslucentSpecularOnly.newMaterial(
                  md.getUVMatrix(),
                  in_alpha,
                  in_normal,
                  in_specular);
              }
            });
        }
      });
  }

  static KMaterialAlbedo makeKMaterialAlbedo(
    final Map<PathVirtual, SBTexture2D<?>> textures,
    final SBMaterialAlbedoDescription d)
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

  private static KMaterialAlpha makeKMaterialAlpha(
    final SBMaterialAlphaDescription alpha)
  {
    return KMaterialAlpha.newAlpha(alpha.getType(), alpha.getOpacity());
  }

  static KMaterialEmissive makeKMaterialEmissive(
    final Map<PathVirtual, SBTexture2D<?>> textures,
    final SBMaterialEmissiveDescription d)
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

  static KMaterialEnvironment makeKMaterialEnvironment(
    final Map<PathVirtual, SBTextureCube> textures,
    final SBMaterialEnvironmentDescription d)
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

  static KMaterialNormal makeKMaterialNormal(
    final Map<PathVirtual, SBTexture2D<?>> textures,
    final SBMaterialNormalDescription d)
  {
    final PathVirtual p = d.getTexture();
    if (p != null) {
      final SBTexture2D<?> t = textures.get(p);
      return KMaterialNormal.newNormalMapped(t.getTexture());
    }
    return KMaterialNormal.newNormalUnmapped();
  }

  static KMaterialSpecular makeKMaterialSpecular(
    final Map<PathVirtual, SBTexture2D<?>> textures,
    final SBMaterialSpecularDescription d)
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

  private static SBMaterial materialFromDescription(
    final Integer id,
    final SBMaterialDescription desc,
    final Map<PathVirtual, SBTexture2D<?>> tx2,
    final Map<PathVirtual, SBTextureCube> txc)
    throws RException
  {
    final SBMaterial m =
      desc
        .materialDescriptionVisitableAccept(new SBMaterialDescriptionVisitor<SBMaterial, RException>() {
          @Override public SBMaterial materialDescriptionVisitOpaque(
            final SBMaterialDescriptionOpaque mo)
            throws RException
          {
            return mo
              .materialDescriptionOpaqueVisitableAccept(new SBMaterialDescriptionOpaqueVisitor<SBMaterial, RException>() {
                @Override public
                  SBMaterial
                  materialDescriptionVisitOpaqueAlphaDepth(
                    final SBMaterialDescriptionOpaqueAlphaToDepth moatd)
                    throws RException
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
                    final SBMaterialDescriptionOpaqueRegular mor)
                    throws RException
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
            final SBMaterialDescriptionTranslucent mt)
            throws RException
          {
            return mt
              .materialDescriptionTranslucentVisitableAccept(new SBMaterialDescriptionTranslucentVisitor<SBMaterial, RException>() {
                @Override public
                  SBMaterial
                  materialDescriptionVisitTranslucentRefractive(
                    final SBMaterialDescriptionTranslucentRefractive mtr)
                    throws RException
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
                    final SBMaterialDescriptionTranslucentRegular mtr)
                    throws RException
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

                @Override public
                  SBMaterial
                  materialDescriptionVisitTranslucentSpecularOnly(
                    final SBMaterialDescriptionTranslucentSpecularOnly mtr)
                    throws RException
                {
                  final SBMaterialNormalDescription normal = mtr.getNormal();
                  final SBMaterialSpecularDescription specular =
                    mtr.getSpecular();

                  @SuppressWarnings("unchecked") final SBTexture2D<SBTexture2DKindNormal> map_normal =
                    (SBTexture2D<SBTexture2DKindNormal>) (normal.getTexture() != null
                      ? tx2.get(normal.getTexture())
                      : null);
                  @SuppressWarnings("unchecked") final SBTexture2D<SBTexture2DKindSpecular> map_specular =
                    (SBTexture2D<SBTexture2DKindSpecular>) (specular
                      .getTexture() != null
                      ? tx2.get(specular.getTexture())
                      : null);

                  return new SBMaterialTranslucentSpecularOnly(
                    id,
                    mtr,
                    map_normal,
                    map_specular);
                }
              });
          }
        });
    return m;
  }

  private final ExecutorService                     exec_pool;
  private final LinkedList<SBSceneChangeListener>   listeners;
  private final LogUsableType                       log;
  private final SBGLRenderer                        renderer;
  private final Map<String, SBShader>               shaders;
  private final AtomicReference<SceneAndFilesystem> state_current;

  public SBSceneController(
    final SBGLRenderer in_renderer,
    final LogUsableType in_log)
    throws FilesystemError,
      IOException
  {
    this.renderer = in_renderer;
    this.log = in_log.with("control");
    this.listeners = new LinkedList<SBSceneChangeListener>();
    this.shaders = new ConcurrentHashMap<String, SBShader>();
    this.state_current =
      new AtomicReference<SceneAndFilesystem>(new SceneAndFilesystem(
        SBScene.empty(),
        SBSceneFilesystem.filesystemEmpty(in_log)));

    final ExecutorService pool = Executors.newCachedThreadPool();
    assert pool != null;
    this.exec_pool = pool;
  }

  private SceneAndFilesystem internalIOSceneLoad(
    final File file)
    throws FileNotFoundException,
      FilesystemError,
      IOException,
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
        assert i != null;
        scene = scene.instanceAdd(i);
      }

      return new SceneAndFilesystem(scene, fs);

    } finally {
      xms.close();
    }
  }

  private SBScene internalIOSceneLoadMeshes(
    final SBSceneFilesystem fs,
    final SBSceneDescription sd,
    final SBScene scene)
    throws FilesystemError,
      InterruptedException,
      ExecutionException,
      IOException
  {
    SBScene s = scene;

    final PSet<PathVirtual> meshes = sd.getMeshes();
    final String text = String.format("loading %d meshes", meshes.size());
    assert text != null;
    this.log.debug(text);

    for (final PathVirtual name : meshes) {
      assert name != null;
      final SBMesh m = this.internalMeshLoadFromPath(fs, name);
      s = s.meshAdd(m);
    }
    return s;
  }

  private SBScene internalIOSceneLoadTextures2D(
    final SBSceneFilesystem fs,
    final SBSceneDescription sd,
    final SBScene scene)
    throws FilesystemError,
      IOException,
      InterruptedException,
      ExecutionException
  {
    SBScene s = scene;

    final PSet<SBTexture2DDescription> textures = sd.getTextures2D();
    final String text =
      String.format("loading %d 2D textures", textures.size());
    assert text != null;
    this.log.debug(text);

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

  private SBScene internalIOSceneLoadTexturesCube(
    final SBSceneFilesystem fs,
    final SBSceneDescription sd,
    final SBScene scene)
    throws FilesystemError,
      IOException,
      InterruptedException,
      ExecutionException
  {
    SBScene s = scene;

    final PSet<SBTextureCubeDescription> cubes = sd.getTexturesCube();
    final String text =
      String.format("loading %d cube textures", cubes.size());
    assert text != null;
    this.log.debug(text);

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
    final File file)
    throws IOException
  {
    final SceneAndFilesystem saf = this.state_current.get();
    saf.filesystem.filesystemSave(saf.scene.makeDescription(), file);
  }

  private SBMesh internalMeshLoad(
    final SBSceneFilesystem fs,
    final File file)
    throws FileNotFoundException,
      FilesystemError,
      IOException,
      InterruptedException,
      ExecutionException
  {
    final PathVirtual path = fs.filesystemCopyInMesh(file);
    return this.internalMeshLoadFromPath(fs, path);
  }

  private SBMesh internalMeshLoadFromPath(
    final SBSceneFilesystem fs,
    final PathVirtual path)
    throws FilesystemError,
      InterruptedException,
      ExecutionException,
      IOException
  {
    final InputStream stream = fs.filesystemOpenFile(path);
    try {
      final Future<SBMesh> future = this.renderer.meshLoad(path, stream);
      final SBMesh mesh = future.get();
      assert mesh != null;
      return mesh;
    } finally {
      stream.close();
    }
  }

  private SBShader internalShaderLoad(
    final File file)
    throws IOException,
      InterruptedException,
      ExecutionException,
      RException
  {
    final FileInputStream stream = new FileInputStream(file);
    try {
      final PGLSLMetaXML meta = PGLSLMetaXML.fromStream(stream, this.log);
      final File parent = file.getParentFile();
      assert parent != null;
      final Future<SBShader> f = this.renderer.shaderLoad(parent, meta);
      final SBShader r = f.get();
      assert r != null;
      return r;
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
    final SceneAndFilesystem state)
  {
    this.log.debug("state updated");
    this.state_current.set(state);
    this.internalStateChangedNotifyListeners();
  }

  private void internalStateUpdateSceneOnly(
    final SBScene scene)
  {
    this.log.debug("scene state updated");

    final SceneAndFilesystem saf = this.state_current.get();
    this.state_current.set(new SceneAndFilesystem(scene, saf.filesystem));
    this.internalStateChangedNotifyListeners();
  }

  private <T extends SBTexture2DKind> SBTexture2D<T> internalTexture2DLoad(
    final SBSceneFilesystem fs,
    final File file,
    final TextureWrapS wrap_s,
    final TextureWrapT wrap_t,
    final TextureFilterMinification filter_min,
    final TextureFilterMagnification filter_mag)
    throws FileNotFoundException,
      FilesystemError,
      IOException,
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

  private
    <T extends SBTexture2DKind>
    SBTexture2D<T>
    internalTexture2DLoadFromPath(
      final SBSceneFilesystem fs,
      final PathVirtual path,
      final TextureWrapS wrap_s,
      final TextureWrapT wrap_t,
      final TextureFilterMinification filter_min,
      final TextureFilterMagnification filter_mag)
      throws FilesystemError,
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

        final Future<Texture2DStaticType> future =
          this.renderer.texture2DLoad(
            path,
            gl_stream,
            wrap_s,
            wrap_t,
            filter_min,
            filter_mag);

        final Texture2DStaticType texture = future.get();
        assert texture != null;

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

  private SBTextureCube internalTextureCubeLoad(
    final SBSceneFilesystem fs,
    final File file,
    final TextureWrapR wrap_r,
    final TextureWrapS wrap_s,
    final TextureWrapT wrap_t,
    final TextureFilterMinification filter_min,
    final TextureFilterMagnification filter_mag)
    throws FileNotFoundException,
      FilesystemError,
      IOException,
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

  private SBTextureCube internalTextureCubeLoadFromPath(
    final SBSceneFilesystem fs,
    final PathVirtual path,
    final TextureWrapR wrap_r,
    final TextureWrapS wrap_s,
    final TextureWrapT wrap_t,
    final TextureFilterMinification filter_min,
    final TextureFilterMagnification filter_mag)
    throws FilesystemError,
      IOException,
      InterruptedException,
      ExecutionException
  {
    CubeMapFaceInputStream<CMFPositiveZKind> spz = null;
    CubeMapFaceInputStream<CMFPositiveYKind> spy = null;
    CubeMapFaceInputStream<CMFPositiveXKind> spx = null;
    CubeMapFaceInputStream<CMFNegativeZKind> snz = null;
    CubeMapFaceInputStream<CMFNegativeYKind> sny = null;
    CubeMapFaceInputStream<CMFNegativeXKind> snx = null;

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
        new CubeMapFaceInputStream<CMFPositiveZKind>(
          fs.filesystemOpenFile(path_pz));
      spy =
        new CubeMapFaceInputStream<CMFPositiveYKind>(
          fs.filesystemOpenFile(path_py));
      spx =
        new CubeMapFaceInputStream<CMFPositiveXKind>(
          fs.filesystemOpenFile(path_px));
      snz =
        new CubeMapFaceInputStream<CMFNegativeZKind>(
          fs.filesystemOpenFile(path_nz));
      sny =
        new CubeMapFaceInputStream<CMFNegativeYKind>(
          fs.filesystemOpenFile(path_ny));
      snx =
        new CubeMapFaceInputStream<CMFNegativeXKind>(
          fs.filesystemOpenFile(path_nx));

      final Future<TextureCubeStaticType> future =
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

      final TextureCubeStaticType texture = future.get();
      assert texture != null;

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

  @Override public Future<Unit> ioLoadScene(
    final File file)
  {
    NullCheck.notNull(file, "File");

    final FutureTask<Unit> f = new FutureTask<Unit>(new Callable<Unit>() {
      @SuppressWarnings("synthetic-access") @Override public Unit call()
        throws Exception
      {
        final SceneAndFilesystem saf =
          SBSceneController.this.internalIOSceneLoad(file);
        SBSceneController.this.internalStateUpdate(saf);
        return Unit.unit();
      }
    });

    this.exec_pool.execute(f);
    return f;
  }

  @Override public Future<Unit> ioSaveScene(
    final File file)
  {
    NullCheck.notNull(file, "File");

    final FutureTask<Unit> f = new FutureTask<Unit>(new Callable<Unit>() {
      @SuppressWarnings("synthetic-access") @Override public Unit call()
        throws Exception
      {
        SBSceneController.this.internalIOSceneSave(file);
        return Unit.unit();
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

  @Override public
    Pair<Collection<SBLight>, Collection<Pair<KInstanceType, SBInstance>>>
    rendererGetScene()
      throws RException
  {
    final SceneAndFilesystem saf = this.state_current.get();
    final SBScene scene = saf.scene;

    final Collection<SBLight> lights = scene.lightsGet();
    MapPSet<Pair<KInstanceType, SBInstance>> meshes =
      HashTreePSet.empty();

    for (final SBInstance i : scene.instancesGet()) {
      final PathVirtual mesh_path = i.getMesh();
      final SBMesh mesh = scene.meshGet(mesh_path);
      final Integer mat_id = i.getMaterial();
      final SBMaterial mat = scene.materialGet(mat_id);

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

      assert mesh != null;
      final KMesh km = mesh.getMesh();

      final KInstanceType kit =
        kmat
          .materialAccept(new KMaterialVisitorType<KInstanceType, RException>() {
            @Override public KInstanceType materialOpaque(
              final KMaterialOpaqueType m)
              throws RException
            {
              return m
                .materialOpaqueAccept(new KMaterialOpaqueVisitorType<KInstanceType, RException>() {
                  @Override public
                    KInstanceType
                    materialOpaqueAlphaDepth(
                      final KMaterialOpaqueAlphaDepth mo)
                      throws RException
                  {
                    final KMeshWithMaterialOpaqueAlphaDepth ki =
                      KMeshWithMaterialOpaqueAlphaDepth.newInstance(
                        mo,
                        km,
                        i.getFaces());
                    return KInstanceOpaqueAlphaDepth.newInstance(
                      ki,
                      kt,
                      i.getUVMatrix());
                  }

                  @Override public
                    KInstanceType
                    materialOpaqueRegular(
                      final KMaterialOpaqueRegular mo)
                      throws RException
                  {
                    final KMeshWithMaterialOpaqueRegular ki =
                      KMeshWithMaterialOpaqueRegular.newInstance(mo, km, i.getFaces());
                    return KInstanceOpaqueRegular.newInstance(
                      ki,
                      kt,
                      i.getUVMatrix());
                  }
                });
            }

            @Override public KInstanceType materialTranslucent(
              final KMaterialTranslucentType m)
              throws RException
            {
              return m
                .materialTranslucentAccept(new KMaterialTranslucentVisitorType<KInstanceType, RException>() {
                  @Override public
                    KInstanceType
                    translucentRefractive(
                      final KMaterialTranslucentRefractive mtr)
                      throws RException
                  {
                    final KMeshWithMaterialTranslucentRefractive ki =
                      KMeshWithMaterialTranslucentRefractive.newInstance(
                        mtr,
                        km,
                        i.getFaces());
                    return KInstanceTranslucentRefractive
                      .newInstance(ki, kt, i.getUVMatrix());
                  }

                  @Override public
                    KInstanceType
                    translucentRegular(
                      final KMaterialTranslucentRegular mtr)
                      throws RException
                  {
                    final KMeshWithMaterialTranslucentRegular ki =
                      KMeshWithMaterialTranslucentRegular.newInstance(
                        mtr,
                        km,
                        i.getFaces());
                    return KInstanceTranslucentRegular
                      .newInstance(ki, kt, i.getUVMatrix());
                  }

                  @Override public
                    KInstanceType
                    translucentSpecularOnly(
                      final KMaterialTranslucentSpecularOnly mts)
                      throws RException
                  {
                    final KMeshWithMaterialTranslucentSpecularOnly ki =
                      KMeshWithMaterialTranslucentSpecularOnly.newInstance(
                        mts,
                        km,
                        i.getFaces());
                    return KInstanceTranslucentSpecularOnly
                      .newInstance(ki, kt, i.getUVMatrix());
                  }
                });
            }
          });

      meshes = meshes.plus(Pair.pair(kit, i));
    }

    final Collection<SBLight> lgen = lights;
    final Collection<Pair<KInstanceType, SBInstance>> lm = meshes;
    assert lm != null;
    return Pair.pair(lgen, lm);
  }

  @Override public void rendererSetBackgroundColour(
    final float r,
    final float g,
    final float b)
  {
    this.renderer.setBackgroundColour(r, g, b);
  }

  @Override public void rendererSetCustomProjection(
    final RMatrixI4x4F<RTransformProjectionType> p)
  {
    this.renderer.setCustomProjection(p);
  }

  @Override public void rendererSetType(
    final SBKRendererSelectionType type)
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
    final SBSceneChangeListener listener)
  {
    this.log.debug("Registered change listener " + listener);
    this.listeners.add(listener);
  }

  @Override public boolean sceneInstanceExists(
    final Integer id)
  {
    return this.state_current.get().scene.instanceExists(id);
  }

  @Override public Integer sceneInstanceFreshID()
  {
    final Pair<SBScene, Integer> p =
      this.state_current.get().scene.instanceFreshID();
    this.internalStateUpdateSceneOnly(p.getLeft());
    return p.getRight();
  }

  @Override public @Nullable SBInstance sceneInstanceGet(
    final Integer id)
  {
    return this.state_current.get().scene.instanceGet(id);
  }

  @Override public void sceneInstancePut(
    final SBInstance i)
  {
    this.internalStateUpdateSceneOnly(this.state_current.get().scene
      .instanceAdd(i));
  }

  @Override public void sceneInstanceRemove(
    final Integer id)
  {
    this.internalStateUpdateSceneOnly(this.state_current.get().scene
      .removeInstance(id));
  }

  @Override public Collection<SBInstance> sceneInstancesGetAll()
  {
    return this.state_current.get().scene.instancesGet();
  }

  @Override public boolean sceneLightExists(
    final Integer id)
  {
    return this.state_current.get().scene.lightExists(id);
  }

  @Override public Integer sceneLightFreshID()
  {
    final Pair<SBScene, Integer> p =
      this.state_current.get().scene.lightFreshID();
    this.internalStateUpdateSceneOnly(p.getLeft());
    return p.getRight();
  }

  @Override public @Nullable SBLight sceneLightGet(
    final Integer id)
  {
    return this.state_current.get().scene.lightGet(id);
  }

  @SuppressWarnings("synthetic-access") @Override public void sceneLightPut(
    final SBLightDescription d)
    throws RException
  {
    final AtomicReference<SceneAndFilesystem> state =
      SBSceneController.this.state_current;

    d
      .lightDescriptionVisitableAccept(new SBLightDescriptionVisitor<Unit, NullCheckException>() {
        @Override public Unit lightVisitDirectional(
          final SBLightDescriptionDirectional ld)
          throws RException
        {
          final SBLightDirectional l = new SBLightDirectional(ld);
          SBSceneController.this.internalStateUpdateSceneOnly(state.get().scene
            .lightAdd(l));
          return Unit.unit();
        }

        @Override public Unit lightVisitProjective(
          final SBLightDescriptionProjective ld)
          throws RException
        {
          @SuppressWarnings("unchecked") final SBTexture2D<SBTexture2DKindAlbedo> t =
            (SBTexture2D<SBTexture2DKindAlbedo>) SBSceneController.this
              .sceneTextures2DGet()
              .get(ld.getTexture());

          final KLightProjectiveBuilderType b = KLightProjective.newBuilder();
          b.setColor(ld.getColour());
          b.setFalloff(ld.getFalloff());
          b.setIntensity(ld.getIntensity());
          b.setOrientation(ld.getOrientation());
          b.setPosition(ld.getPosition());
          b.setProjection(ld.getProjectionMatrix());
          b.setRange((float) ld.getProjection().getFar());
          b.setShadowOption(ld.getShadow());
          b.setTexture(t.getTexture());
          final KLightProjective kp = b.build();

          final SBLightProjective l = new SBLightProjective(ld, kp);
          SBSceneController.this.internalStateUpdateSceneOnly(state.get().scene
            .lightAdd(l));
          return Unit.unit();
        }

        @Override public Unit lightVisitSpherical(
          final SBLightDescriptionSpherical ld)
          throws RException
        {
          final SBLightSpherical l = new SBLightSpherical(ld);
          SBSceneController.this.internalStateUpdateSceneOnly(state.get().scene
            .lightAdd(l));
          return Unit.unit();
        }
      });
  }

  @Override public void sceneLightRemove(
    final Integer id)
  {
    this.internalStateUpdateSceneOnly(this.state_current.get().scene
      .lightRemove(id));
  }

  @Override public Collection<SBLight> sceneLightsGetAll()
  {
    return this.state_current.get().scene.lightsGet();
  }

  @Override public boolean sceneMaterialExists(
    final Integer id)
  {
    return this.state_current.get().scene.materialExists(id);
  }

  @Override public Integer sceneMaterialFreshID()
  {
    final Pair<SBScene, Integer> p =
      this.state_current.get().scene.materialFreshID();
    this.internalStateUpdateSceneOnly(p.getLeft());
    return p.getRight();
  }

  @Override public @Nullable SBMaterial sceneMaterialGet(
    final Integer id)
  {
    return this.state_current.get().scene.materialGet(id);
  }

  @Override public void sceneMaterialPut(
    final SBMaterial material)
  {
    this.internalStateUpdateSceneOnly(this.state_current.get().scene
      .materialPut(material));
  }

  @Override public void sceneMaterialPutByDescription(
    final Integer id,
    final SBMaterialDescription desc)
    throws RException
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
    final Integer id)
  {
    // TODO Auto-generated method stub
    throw new UnimplementedCodeException();
  }

  @Override public Collection<SBMaterial> sceneMaterialsGetAll()
  {
    return this.state_current.get().scene.materialsGet();
  }

  @Override public Map<PathVirtual, SBMesh> sceneMeshesGet()
  {
    return this.state_current.get().scene.meshesGet();
  }

  @Override public Future<SBMesh> sceneMeshLoad(
    final File file)
  {
    NullCheck.notNull(file, "File");

    final FutureTask<SBMesh> f =
      new FutureTask<SBMesh>(new Callable<SBMesh>() {
        @SuppressWarnings("synthetic-access") @Override public SBMesh call()
          throws Exception
        {
          final SceneAndFilesystem saf =
            SBSceneController.this.state_current.get();

          final SBMesh m =
            SBSceneController.this.internalMeshLoad(saf.filesystem, file);

          SBSceneController.this.internalStateUpdateSceneOnly(saf.scene
            .meshAdd(m));
          return m;
        }

      });

    this.exec_pool.execute(f);
    return f;
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
  {
    NullCheck.notNull(file, "File");

    final FutureTask<SBTexture2D<T>> f =
      new FutureTask<SBTexture2D<T>>(new Callable<SBTexture2D<T>>() {
        @SuppressWarnings("synthetic-access") @Override public
          SBTexture2D<T>
          call()
            throws Exception
        {
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
        }
      });

    this.exec_pool.execute(f);
    return f;
  }

  @Override public Future<SBTextureCube> sceneTextureCubeLoad(
    final File file,
    final TextureWrapR wrap_r,
    final TextureWrapS wrap_s,
    final TextureWrapT wrap_t,
    final TextureFilterMinification filter_min,
    final TextureFilterMagnification filter_mag)
  {
    NullCheck.notNull(file, "File");

    final FutureTask<SBTextureCube> f =
      new FutureTask<SBTextureCube>(new Callable<SBTextureCube>() {
        @SuppressWarnings("synthetic-access") @Override public
          SBTextureCube
          call()
            throws Exception
        {
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
        }
      });

    this.exec_pool.execute(f);
    return f;
  }

  @Override public Map<PathVirtual, SBTexture2D<?>> sceneTextures2DGet()
  {
    return this.state_current.get().scene.textures2DGet();
  }

  @Override public Map<PathVirtual, SBTextureCube> sceneTexturesCubeGet()
  {
    return this.state_current.get().scene.texturesCubeGet();
  }

  @Override public Future<SBShader> shaderLoad(
    final File file)
  {
    NullCheck.notNull(file, "File");

    final FutureTask<SBShader> f =
      new FutureTask<SBShader>(new Callable<SBShader>() {
        @SuppressWarnings("synthetic-access") @Override public
          SBShader
          call()
            throws Exception
        {
          final SBShader sbt =
            SBSceneController.this.internalShaderLoad(file);

          SBSceneController.this.shaders.put(sbt.getName(), sbt);
          return sbt;
        }
      });

    this.exec_pool.execute(f);
    return f;
  }

  @Override public Map<String, SBShader> shadersGet()
  {
    return this.shaders;
  }

  @Override public void rendererPostprocessorSet(
    final SBKPostprocessor p)
  {
    this.renderer.setPostprocessor(p);
  }
}
