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

package com.io7m.r1.examples.viewer;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.media.nativewindow.WindowClosingProtocol.WindowClosingMode;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;

import nu.xom.ParsingException;
import nu.xom.ValidityException;

import com.io7m.jcache.JCacheException;
import com.io7m.jcamera.JCameraFPSStyle;
import com.io7m.jcamera.JCameraFPSStyleIntegrator;
import com.io7m.jcamera.JCameraFPSStyleIntegratorType;
import com.io7m.jcamera.JCameraFPSStyleType;
import com.io7m.jcamera.JCameraInput;
import com.io7m.jcamera.JCameraMouseRegion;
import com.io7m.jcamera.JCameraRotationCoefficients;
import com.io7m.jcamera.JCameraScreenOrigin;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.ArrayBufferUsableType;
import com.io7m.jcanephora.FaceSelection;
import com.io7m.jcanephora.IndexBufferUsableType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.Primitives;
import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jcanephora.TextureCubeStaticUsableType;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jcanephora.TextureLoaderType;
import com.io7m.jcanephora.TextureUnitType;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jcanephora.batchexec.JCBExecutorProcedureType;
import com.io7m.jcanephora.batchexec.JCBExecutorType;
import com.io7m.jcanephora.batchexec.JCBProgramProcedureType;
import com.io7m.jcanephora.batchexec.JCBProgramType;
import com.io7m.jcanephora.texload.imageio.TextureLoaderImageIO;
import com.io7m.jfunctional.Unit;
import com.io7m.jlog.LogType;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.Nullable;
import com.io7m.jranges.RangeInclusiveL;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.VectorI4F;
import com.io7m.jtensors.VectorReadable4FType;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.r1.examples.ExampleClasses;
import com.io7m.r1.examples.ExampleRendererConstructorDebugType;
import com.io7m.r1.examples.ExampleRendererConstructorDeferredType;
import com.io7m.r1.examples.ExampleRendererConstructorForwardType;
import com.io7m.r1.examples.ExampleRendererConstructorType;
import com.io7m.r1.examples.ExampleRendererConstructorVisitorType;
import com.io7m.r1.examples.ExampleRendererDebugType;
import com.io7m.r1.examples.ExampleRendererDeferredType;
import com.io7m.r1.examples.ExampleRendererType;
import com.io7m.r1.examples.ExampleRendererVisitorType;
import com.io7m.r1.examples.ExampleRenderers;
import com.io7m.r1.examples.ExampleSceneBuilderType;
import com.io7m.r1.examples.ExampleSceneType;
import com.io7m.r1.examples.ExampleSceneUtilities;
import com.io7m.r1.examples.ExampleViewType;
import com.io7m.r1.examples.tools.EMeshCache;
import com.io7m.r1.examples.tools.ETexture2DCache;
import com.io7m.r1.examples.tools.ETextureCubeCache;
import com.io7m.r1.kernel.KFramebufferDeferred;
import com.io7m.r1.kernel.KFramebufferDeferredType;
import com.io7m.r1.kernel.KFramebufferRGBAUsableType;
import com.io7m.r1.kernel.KProgramType;
import com.io7m.r1.kernel.KShaderCachePostprocessingType;
import com.io7m.r1.kernel.KShadingProgramCommon;
import com.io7m.r1.kernel.types.KCamera;
import com.io7m.r1.kernel.types.KDepthPrecision;
import com.io7m.r1.kernel.types.KFramebufferDepthDescription;
import com.io7m.r1.kernel.types.KFramebufferForwardDescription;
import com.io7m.r1.kernel.types.KFramebufferRGBADescription;
import com.io7m.r1.kernel.types.KInstanceOpaqueType;
import com.io7m.r1.kernel.types.KInstanceTranslucentLitType;
import com.io7m.r1.kernel.types.KInstanceTranslucentUnlitType;
import com.io7m.r1.kernel.types.KLightTranslucentType;
import com.io7m.r1.kernel.types.KLightWithShadowType;
import com.io7m.r1.kernel.types.KMeshReadableType;
import com.io7m.r1.kernel.types.KRGBAPrecision;
import com.io7m.r1.kernel.types.KUnitQuad;
import com.io7m.r1.kernel.types.KUnitQuadCache;
import com.io7m.r1.kernel.types.KUnitQuadCacheType;
import com.io7m.r1.kernel.types.KUnitQuadUsableType;
import com.io7m.r1.kernel.types.KVisibleSet;
import com.io7m.r1.kernel.types.KVisibleSetBuilderWithCreateType;
import com.io7m.r1.kernel.types.KVisibleSetLightGroupBuilderType;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RExceptionBuilderInvalid;
import com.io7m.r1.types.RExceptionInstanceAlreadyVisible;
import com.io7m.r1.types.RExceptionJCGL;
import com.io7m.r1.types.RExceptionLightGroupAlreadyAdded;
import com.io7m.r1.types.RMatrixI4x4F;
import com.io7m.r1.types.RTransformViewType;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.FPSAnimator;

@SuppressWarnings("synthetic-access") final class ViewerSingleMainWindow implements
  Runnable
{
  protected static final int FRAMES_PER_SECOND = 60;

  private final class ViewerSingleMainMouseListener extends MouseAdapter
  {
    public ViewerSingleMainMouseListener()
    {
      this.coefficients = new JCameraRotationCoefficients();
    }

    private final JCameraRotationCoefficients coefficients;

    @Override public void mouseMoved(
      final @Nullable MouseEvent e)
    {
      assert e != null;

      final Runner r = ViewerSingleMainWindow.this.runner;
      if (r == null) {
        return;
      }

      if (r.mouseViewIsEnabled()) {
        final JCameraMouseRegion mr =
          ViewerSingleMainWindow.this.mouse_region;

        final float x = e.getX();
        final float y = e.getY();

        mr.getCoefficients(x, y, this.coefficients);

        final JCameraInput input = r.getFPSCameraInput();
        input.addRotationAroundHorizontal(this.coefficients.getHorizontal());
        input.addRotationAroundVertical(this.coefficients.getVertical());
      }
    }
  }

  private final class ViewerSingleMainKeyListener implements KeyListener
  {
    private final GLWindow window;

    public ViewerSingleMainKeyListener(
      final GLWindow in_window)
    {
      this.window = in_window;
    }

    @Override public void keyPressed(
      final @Nullable KeyEvent e)
    {
      assert e != null;

      final Runner r = ViewerSingleMainWindow.this.runner;
      if (r == null) {
        return;
      }

      final JCameraInput input = r.getFPSCameraInput();
      final short code = e.getKeyCode();

      switch (code) {
        case KeyEvent.VK_A:
        {
          input.setMovingLeft(true);
          break;
        }
        case KeyEvent.VK_D:
        {
          input.setMovingRight(true);
          break;
        }
        case KeyEvent.VK_W:
        {
          input.setMovingForward(true);
          break;
        }
        case KeyEvent.VK_S:
        {
          input.setMovingBackward(true);
          break;
        }
        case KeyEvent.VK_F:
        {
          input.setMovingUp(true);
          break;
        }
        case KeyEvent.VK_V:
        {
          input.setMovingDown(true);
          break;
        }
      }
    }

    @Override public void keyReleased(
      final @Nullable KeyEvent e)
    {
      assert e != null;

      final Runner r = ViewerSingleMainWindow.this.runner;
      if (r == null) {
        return;
      }

      final JCameraInput input = r.getFPSCameraInput();
      final short code = e.getKeyCode();
      switch (code) {
        case KeyEvent.VK_A:
        {
          input.setMovingLeft(false);
          break;
        }
        case KeyEvent.VK_D:
        {
          input.setMovingRight(false);
          break;
        }
        case KeyEvent.VK_W:
        {
          input.setMovingForward(false);
          break;
        }
        case KeyEvent.VK_S:
        {
          input.setMovingBackward(false);
          break;
        }
        case KeyEvent.VK_F:
        {
          input.setMovingUp(false);
          break;
        }
        case KeyEvent.VK_V:
        {
          input.setMovingDown(false);
          break;
        }
        case KeyEvent.VK_N:
        {
          r.nextViewIndex();
          break;
        }
        case KeyEvent.VK_P:
        {
          r.previousViewIndex();
          break;
        }
        case KeyEvent.VK_M:
        {
          if (r.mouseViewIsEnabled()) {
            r.mouseViewDisable();
            this.window.confinePointer(false);
            this.window.setPointerVisible(true);
          } else {
            r.mouseViewEnable();
            input.setRotationHorizontal(0.0f);
            input.setRotationVertical(0.0f);
            this.window.warpPointer(
              this.window.getWidth() / 2,
              this.window.getHeight() / 2);
            this.window.confinePointer(true);
            this.window.setPointerVisible(false);
          }
          break;
        }
      }
    }
  }

  private static final class Runner
  {
    private static void drawQuad(
      final JCGLInterfaceCommonType gc,
      final KUnitQuadUsableType quad,
      final JCBProgramType p)
      throws JCGLException,
        JCGLException
    {
      final ArrayBufferUsableType array = quad.getArray();
      final IndexBufferUsableType indices = quad.getIndices();

      gc.arrayBufferBind(array);

      try {
        KShadingProgramCommon.bindAttributePosition(p, array);
        KShadingProgramCommon.bindAttributeUV(p, array);
        KShadingProgramCommon.putMatrixUV(
          p,
          ExampleSceneUtilities.IDENTITY_UV_M);

        p.programExecute(new JCBProgramProcedureType<JCGLException>() {
          @Override public void call()
            throws JCGLException
          {
            gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
          }
        });

      } finally {
        gc.arrayBufferUnbind();
      }
    }

    private final ETexture2DCache               cache_2d;
    private final ETextureCubeCache             cache_cube;
    private final EMeshCache                    cache_mesh;
    private final ExampleSceneType              example;
    private final JCameraFPSStyleType           fps_camera;
    private final JCameraFPSStyleIntegratorType fps_camera_integrator;
    private final KFramebufferDeferredType      framebuffer;
    private final JCGLImplementationType        gi;
    private final TextureLoaderType             loader;
    private LogUsableType                       log;
    private final MatrixM4x4F                   matrix_view_temporary;
    private final MatrixM4x4F.Context           matrix_context;
    private boolean                             mouse_view;
    private final KUnitQuad                     quad;
    private final ExampleRendererType           renderer;
    private final VShaderCaches                 shader_caches;
    private int                                 view_index;
    private JCameraInput                        fps_camera_input;

    Runner(
      final GLAutoDrawable drawable,
      final JCGLImplementationType in_gi,
      final ExampleSceneType in_example,
      final ExampleRendererConstructorType in_renderer_cons,
      final VShaderCaches in_shader_caches,
      final LogUsableType in_log)
      throws JCGLException,
        RException
    {
      this.gi = in_gi;
      this.example = in_example;
      this.loader =
        TextureLoaderImageIO
          .newTextureLoaderWithAlphaPremultiplication(in_log);
      this.cache_cube = new ETextureCubeCache(in_gi, this.loader, in_log);
      this.cache_2d = new ETexture2DCache(in_gi, this.loader, in_log);
      this.cache_mesh = new EMeshCache(in_gi, in_log);
      this.quad = KUnitQuad.newQuad(in_gi.getGLCommon(), in_log);
      this.shader_caches = in_shader_caches;
      this.mouse_view = false;
      this.log = in_log;

      this.matrix_view_temporary = new MatrixM4x4F();
      this.matrix_context = new MatrixM4x4F.Context();

      this.fps_camera = JCameraFPSStyle.newCamera();
      this.fps_camera.cameraSetPosition3f(0.0f, 2.0f, 3.0f);

      this.fps_camera_input = JCameraInput.newInput();

      this.fps_camera_integrator =
        JCameraFPSStyleIntegrator.newIntegrator(
          this.fps_camera,
          this.fps_camera_input);

      this.fps_camera_integrator
        .integratorAngularSetDragHorizontal(0.000000001f);
      this.fps_camera_integrator
        .integratorAngularSetDragVertical(0.000000001f);
      this.fps_camera_integrator
        .integratorAngularSetAccelerationHorizontal((float) ((Math.PI / 12) / (1.0 / 60.0)));
      this.fps_camera_integrator
        .integratorAngularSetAccelerationVertical((float) ((Math.PI / 12) / (1.0 / 60.0)));

      this.fps_camera_integrator
        .integratorLinearSetAcceleration((float) (3.0 / (1.0 / 60.0)));
      this.fps_camera_integrator.integratorLinearSetMaximumSpeed(3.0f);
      this.fps_camera_integrator.integratorLinearSetDrag(0.000000001f);

      final KUnitQuadCacheType quad_cache =
        KUnitQuadCache.newCache(in_gi.getGLCommon(), in_log);

      this.renderer =
        in_renderer_cons
          .matchConstructor(new ExampleRendererConstructorVisitorType<ExampleRendererType, RException>() {
            @Override public ExampleRendererType debug(
              final ExampleRendererConstructorDebugType c)
              throws RException,
                JCGLException
            {
              return c.newRenderer(
                in_log,
                quad_cache,
                in_shader_caches.getShaderDepthCache(),
                in_shader_caches.getShaderDebugCache(),
                in_shader_caches.getShaderPostprocessingCache(),
                in_gi);
            }

            @Override public ExampleRendererType deferred(
              final ExampleRendererConstructorDeferredType c)
              throws RException,
                JCGLException
            {
              return c.newRenderer(
                in_log,
                in_shader_caches.getShaderDebugCache(),
                in_shader_caches.getShaderForwardTranslucentLitCache(),
                in_shader_caches.getShaderForwardTranslucentUnlitCache(),
                in_shader_caches.getShaderDepthCache(),
                in_shader_caches.getShaderDepthVarianceCache(),
                in_shader_caches.getShaderPostprocessingCache(),
                in_shader_caches.getShaderDeferredGeoCache(),
                in_shader_caches.getShaderDeferredLightCache(),
                in_gi);
            }

            @Override public ExampleRendererType forward(
              final ExampleRendererConstructorForwardType c)
              throws RException,
                JCGLException
            {
              return c.newRenderer(
                in_log,
                in_shader_caches.getShaderForwardTranslucentLitCache(),
                in_shader_caches.getShaderForwardTranslucentUnlitCache(),
                in_shader_caches.getShaderDepthCache(),
                in_shader_caches.getShaderDepthVarianceCache(),
                in_shader_caches.getShaderPostprocessingCache(),
                in_gi);
            }
          });

      this.framebuffer =
        this.renderer
          .rendererAccept(new ExampleRendererVisitorType<KFramebufferDeferredType>() {
            private AreaInclusive makeArea()
            {
              final RangeInclusiveL range_x =
                new RangeInclusiveL(0, drawable.getWidth() - 1);
              final RangeInclusiveL range_y =
                new RangeInclusiveL(0, drawable.getHeight() - 1);
              final AreaInclusive area = new AreaInclusive(range_x, range_y);
              return area;
            }

            private KFramebufferDeferredType makeDeferred()
              throws RException
            {
              final AreaInclusive area = this.makeArea();

              final KFramebufferRGBADescription rgba_description =
                KFramebufferRGBADescription.newDescription(
                  area,
                  TextureFilterMagnification.TEXTURE_FILTER_LINEAR,
                  TextureFilterMinification.TEXTURE_FILTER_LINEAR,
                  KRGBAPrecision.RGBA_PRECISION_8);

              final KFramebufferDepthDescription depth_description =
                KFramebufferDepthDescription.newDescription(
                  area,
                  TextureFilterMagnification.TEXTURE_FILTER_NEAREST,
                  TextureFilterMinification.TEXTURE_FILTER_NEAREST,
                  KDepthPrecision.DEPTH_PRECISION_24);

              return KFramebufferDeferred.newFramebuffer(
                Runner.this.gi,
                KFramebufferForwardDescription.newDescription(
                  rgba_description,
                  depth_description));
            }

            @Override public KFramebufferDeferredType visitDebug(
              final ExampleRendererDebugType r)
              throws RException
            {
              return this.makeDeferred();
            }

            @Override public KFramebufferDeferredType visitDeferred(
              final ExampleRendererDeferredType r)
              throws RException
            {
              return this.makeDeferred();
            }
          });
    }

    public JCameraInput getFPSCameraInput()
    {
      return this.fps_camera_integrator.integratorGetInput();
    }

    public void mouseViewDisable()
    {
      this.log.debug("mouse view disabled");
      this.mouse_view = false;
    }

    public void mouseViewEnable()
    {
      this.log.debug("mouse view enabled");
      this.mouse_view = true;
    }

    public boolean mouseViewIsEnabled()
    {
      return this.mouse_view;
    }

    public void nextViewIndex()
    {
      final List<ExampleViewType> views = this.example.exampleViewpoints();
      this.view_index = (this.view_index + 1) % views.size();
      this.log.debug("view index: " + this.view_index);
    }

    public void previousViewIndex()
    {
      final List<ExampleViewType> views = this.example.exampleViewpoints();

      if (this.view_index == 0) {
        this.view_index = views.size() - 1;
      } else {
        this.view_index = this.view_index - 1;
      }
      this.log.debug("view index: " + this.view_index);
    }

    private void renderSceneResults(
      final KFramebufferRGBAUsableType fb)
      throws JCGLException,
        RException,
        JCacheException
    {
      final JCGLImplementationType g = this.gi;
      assert g != null;
      final JCGLInterfaceCommonType gc = g.getGLCommon();

      final KShaderCachePostprocessingType sc =
        this.shader_caches.getShaderPostprocessingCache();
      assert sc != null;
      final KProgramType kp = sc.cacheGetLU("copy_rgba");
      gc.framebufferDrawUnbind();

      try {
        gc.blendingDisable();
        gc.colorBufferMask(true, true, true, true);
        gc.cullingDisable();

        if (gc.depthBufferGetBits() > 0) {
          gc.depthBufferTestDisable();
          gc.depthBufferWriteDisable();
        }

        if (gc.stencilBufferGetBits() > 0) {
          gc.stencilBufferDisable();
          gc.stencilBufferMask(FaceSelection.FACE_FRONT_AND_BACK, 0);
        }

        gc.viewportSet(fb.kFramebufferGetArea());

        final JCBExecutorType e = kp.getExecutable();
        e.execRun(new JCBExecutorProcedureType<JCGLException>() {
          @Override public void call(
            final JCBProgramType p)
            throws JCGLException
          {
            final List<TextureUnitType> units = gc.textureGetUnits();
            final TextureUnitType unit = units.get(0);
            assert unit != null;

            gc.texture2DStaticBind(unit, fb.rgbaGetTexture());
            p.programUniformPutTextureUnit("t_image", unit);
            Runner.drawQuad(gc, Runner.this.quad, p);
          }
        });

      } finally {
        gc.framebufferDrawUnbind();
      }
    }

    public void run()
      throws RException
    {
      final List<ExampleViewType> viewpoints =
        this.example.exampleViewpoints();
      final ExampleViewType view = viewpoints.get(this.view_index);

      final KCamera view_camera = view.getCamera();
      final KVisibleSetBuilderWithCreateType scene_builder;
      if (this.mouse_view) {
        this.fps_camera_integrator
          .integrate(1.0f / ViewerSingleMainWindow.FRAMES_PER_SECOND);
        this.fps_camera.cameraMakeViewMatrix(
          this.matrix_context,
          this.matrix_view_temporary);

        final RMatrixI4x4F<RTransformViewType> m =
          RMatrixI4x4F.newFromReadable(this.matrix_view_temporary);

        final KCamera kc = KCamera.newCamera(m, view_camera.getProjection());
        scene_builder = KVisibleSet.newBuilder(kc);
      } else {
        scene_builder = KVisibleSet.newBuilder(view_camera);
      }

      final ExampleSceneBuilderType b = new ExampleSceneBuilderType() {
        @Override public TextureCubeStaticUsableType cubeTextureClamped(
          final String name)
          throws RException
        {
          try {
            final ETextureCubeCache cc = Runner.this.cache_cube;
            assert cc != null;
            return cc.loadCubeClamped(name);
          } catch (final ValidityException e) {
            throw new RuntimeException(e);
          } catch (final IOException e) {
            throw new RuntimeException(e);
          } catch (final JCGLException e) {
            throw new RuntimeException(e);
          } catch (final ParsingException e) {
            throw new RuntimeException(e);
          }
        }

        @Override public TextureCubeStaticUsableType cubeTextureRepeated(
          final String name)
          throws RException
        {
          try {
            return Runner.this.cache_cube.loadCubeRepeat(name);
          } catch (final ValidityException e) {
            throw new RuntimeException(e);
          } catch (final IOException e) {
            throw new RuntimeException(e);
          } catch (final JCGLException e) {
            throw new RuntimeException(e);
          } catch (final ParsingException e) {
            throw new RuntimeException(e);
          }
        }

        @Override public KMeshReadableType mesh(
          final String name)
          throws RException
        {
          return Runner.this.cache_mesh.loadMesh(name);
        }

        @Override public Texture2DStaticUsableType texture(
          final String name)
          throws RException
        {
          try {
            return Runner.this.cache_2d.loadTexture(name);
          } catch (final IOException e) {
            throw new RuntimeException(e);
          } catch (final JCGLException e) {
            throw new RuntimeException(e);
          }
        }

        @Override public Texture2DStaticUsableType textureClamped(
          final String name)
          throws RException
        {
          try {
            return Runner.this.cache_2d.loadTextureClamped(name);
          } catch (final IOException e) {
            throw new RuntimeException(e);
          } catch (final JCGLException e) {
            throw new RuntimeException(e);
          }
        }

        @Override public void visibleOpaqueAddUnlit(
          final KInstanceOpaqueType instance)
          throws RExceptionBuilderInvalid,
            RExceptionInstanceAlreadyVisible
        {
          scene_builder.visibleOpaqueAddUnlit(instance);
        }

        @Override public
          KVisibleSetLightGroupBuilderType
          visibleOpaqueNewLightGroup(
            final String name)
            throws RExceptionLightGroupAlreadyAdded,
              RExceptionBuilderInvalid
        {
          return scene_builder.visibleOpaqueNewLightGroup(name);
        }

        @Override public void visibleShadowsAddCaster(
          final KLightWithShadowType light,
          final KInstanceOpaqueType instance)
          throws RExceptionBuilderInvalid
        {
          scene_builder.visibleShadowsAddCaster(light, instance);
        }

        @Override public void visibleShadowsAddLight(
          final KLightWithShadowType light)
          throws RExceptionBuilderInvalid
        {
          scene_builder.visibleShadowsAddLight(light);
        }

        @Override public void visibleTranslucentsAddLit(
          final KInstanceTranslucentLitType instance,
          final Set<KLightTranslucentType> lights)
          throws RExceptionBuilderInvalid
        {
          scene_builder.visibleTranslucentsAddLit(instance, lights);
        }

        @Override public void visibleTranslucentsAddUnlit(
          final KInstanceTranslucentUnlitType instance)
          throws RExceptionBuilderInvalid
        {
          scene_builder.visibleTranslucentsAddUnlit(instance);
        }
      };

      this.example.exampleScene(b);

      this.renderer.rendererAccept(new ExampleRendererVisitorType<Unit>() {
        @Override public Unit visitDebug(
          final ExampleRendererDebugType rd)
          throws RException
        {
          try {
            final KVisibleSet sc = scene_builder.visibleCreate();

            final KFramebufferDeferredType fb = Runner.this.framebuffer;
            assert fb != null;
            fb.deferredFramebufferClear(
              Runner.this.gi.getGLCommon(),
              ViewerSingleMainWindow.CLEAR_COLOR,
              ViewerSingleMainWindow.CLEAR_DEPTH,
              ViewerSingleMainWindow.CLEAR_STENCIL);

            rd.rendererDebugEvaluate(fb, sc);
            Runner.this.renderSceneResults(fb);

            return Unit.unit();
          } catch (final JCGLException e) {
            throw RExceptionJCGL.fromJCGLException(e);
          } catch (final JCacheException e) {
            throw new UnreachableCodeException(e);
          }
        }

        @Override public Unit visitDeferred(
          final ExampleRendererDeferredType rd)
          throws RException
        {
          try {
            final KVisibleSet sc = scene_builder.visibleCreate();

            final KFramebufferDeferredType fb = Runner.this.framebuffer;
            assert fb != null;
            fb.deferredFramebufferClear(
              Runner.this.gi.getGLCommon(),
              ViewerSingleMainWindow.CLEAR_COLOR,
              ViewerSingleMainWindow.CLEAR_DEPTH,
              ViewerSingleMainWindow.CLEAR_STENCIL);

            rd.rendererDeferredEvaluateFull(fb, sc);
            Runner.this.renderSceneResults(fb);

            return Unit.unit();
          } catch (final JCGLException e) {
            throw RExceptionJCGL.fromJCGLException(e);
          } catch (final JCacheException e) {
            throw new UnreachableCodeException(e);
          }
        }
      });
    }
  }

  static final VectorReadable4FType            CLEAR_COLOR;
  static final float                           CLEAR_DEPTH;
  static final int                             CLEAR_STENCIL;

  static {
    CLEAR_STENCIL = 0;
    CLEAR_COLOR = new VectorI4F(0.0f, 0.0f, 0.0f, 0.0f);
    CLEAR_DEPTH = 1.0f;
  }

  private final ViewerConfig                   config;
  private final ExampleSceneType               example;
  private final LogType                        log;
  private final ExampleRendererConstructorType renderer;
  private @Nullable Runner                     runner;
  private final JCameraMouseRegion             mouse_region;

  public ViewerSingleMainWindow(
    final ViewerConfig in_config,
    final LogType in_log,
    final String renderer_name,
    final String example_name)
    throws ClassNotFoundException,
      InstantiationException,
      IllegalAccessException
  {
    this.config = in_config;
    this.log = in_log;
    this.renderer = ExampleRenderers.getRenderer(renderer_name);
    this.example = ExampleClasses.getScene(example_name);
    this.mouse_region =
      JCameraMouseRegion.newRegion(
        JCameraScreenOrigin.SCREEN_ORIGIN_TOP_LEFT,
        640.0f,
        480.0f);
  }

  @Override public void run()
  {
    final GLProfile pro = VGLImplementation.getGLProfile(this.log);
    final GLCapabilities caps = new GLCapabilities(pro);
    final GLWindow window = GLWindow.create(caps);

    window.setSize(640, 480);
    window.addGLEventListener(new GLEventListener() {
      @Override public void display(
        @Nullable final GLAutoDrawable drawable)
      {
        try {
          assert drawable != null;

          final Runner r = ViewerSingleMainWindow.this.runner;
          assert r != null;
          r.run();

          if (r.mouseViewIsEnabled()) {
            final JCameraMouseRegion mr =
              ViewerSingleMainWindow.this.mouse_region;
            window.warpPointer((int) mr.getCenterX(), (int) mr.getCenterY());
          }

        } catch (final Exception e) {
          throw new RuntimeException(e);
        }
      }

      @Override public void dispose(
        @Nullable final GLAutoDrawable drawable)
      {

      }

      @Override public void init(
        @Nullable final GLAutoDrawable drawable)
      {
        try {
          assert drawable != null;

          final GLContext ctx = drawable.getContext();
          assert ctx != null;

          final JCGLImplementationType gi =
            VGLImplementation.makeGLImplementation(
              ctx,
              ViewerSingleMainWindow.this.log);

          final VShaderCaches caches =
            VShaderCaches.newCachesFromArchives(
              gi,
              ViewerSingleMainWindow.this.config,
              ViewerSingleMainWindow.this.log);

          ViewerSingleMainWindow.this.runner =
            new Runner(
              drawable,
              gi,
              ViewerSingleMainWindow.this.example,
              ViewerSingleMainWindow.this.renderer,
              caches,
              ViewerSingleMainWindow.this.log);
        } catch (final JCGLException e) {
          throw new RuntimeException(e);
        } catch (final RException e) {
          throw new RuntimeException(e);
        } catch (final FilesystemError e) {
          throw new RuntimeException(e);
        }
      }

      @Override public void reshape(
        @Nullable final GLAutoDrawable drawable,
        final int x,
        final int y,
        final int width,
        final int height)
      {
        final JCameraMouseRegion mr =
          ViewerSingleMainWindow.this.mouse_region;
        mr.setHeight(height);
        mr.setWidth(width);
      }
    });

    window.addWindowListener(new WindowAdapter() {
      @Override public void windowDestroyed(
        final @Nullable WindowEvent e)
      {
        ViewerSingleMainWindow.this.log.info("Exiting...");
        System.exit(0);
      }
    });

    window.addMouseListener(new ViewerSingleMainMouseListener());
    window.addKeyListener(new ViewerSingleMainKeyListener(window));
    window.setDefaultCloseOperation(WindowClosingMode.DISPOSE_ON_CLOSE);
    window.setVisible(true);

    final FPSAnimator anim =
      new FPSAnimator(ViewerSingleMainWindow.FRAMES_PER_SECOND);
    anim.setUpdateFPSFrames(
      ViewerSingleMainWindow.FRAMES_PER_SECOND,
      System.err);
    anim.add(window);
    anim.start();
  }
}
