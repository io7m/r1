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

package com.io7m.r1.examples.viewer_new;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLContext;

import com.io7m.jcamera.JCameraFPSStyle;
import com.io7m.jcamera.JCameraFPSStyle.Context;
import com.io7m.jcamera.JCameraFPSStyleIntegrator;
import com.io7m.jcamera.JCameraFPSStyleIntegratorType;
import com.io7m.jcamera.JCameraFPSStyleSnapshot;
import com.io7m.jcamera.JCameraFPSStyleType;
import com.io7m.jcamera.JCameraInput;
import com.io7m.jcamera.JCameraMouseRegion;
import com.io7m.jcamera.JCameraRotationCoefficients;
import com.io7m.jcamera.JCameraScreenOrigin;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jcanephora.TextureCubeStaticUsableType;
import com.io7m.jcanephora.TextureLoaderType;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.texload.imageio.TextureLoaderImageIO;
import com.io7m.jfunctional.Unit;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.jproperties.JPropertyException;
import com.io7m.jranges.RangeInclusiveL;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.parameterized.PMatrixI4x4F;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.r1.examples.ExampleRendererConstructorType;
import com.io7m.r1.examples.ExampleRendererDebugType;
import com.io7m.r1.examples.ExampleRendererDeferredType;
import com.io7m.r1.examples.ExampleRendererType;
import com.io7m.r1.examples.ExampleRendererVisitorType;
import com.io7m.r1.examples.ExampleSceneBuilderType;
import com.io7m.r1.examples.ExampleSceneType;
import com.io7m.r1.examples.ExampleSceneUtilities;
import com.io7m.r1.examples.ExampleTypeEnum;
import com.io7m.r1.examples.ExampleViewType;
import com.io7m.r1.examples.tools.EMeshCache;
import com.io7m.r1.examples.tools.ETexture2DCache;
import com.io7m.r1.examples.tools.ETextureCubeCache;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.exceptions.RExceptionBuilderInvalid;
import com.io7m.r1.exceptions.RExceptionInstanceAlreadyVisible;
import com.io7m.r1.exceptions.RExceptionLightGroupAlreadyAdded;
import com.io7m.r1.kernel.KFramebufferDeferredType;
import com.io7m.r1.kernel.KImageSinkBlitRGBA;
import com.io7m.r1.kernel.KImageSinkRGBAType;
import com.io7m.r1.kernel.KShaderCacheSetType;
import com.io7m.r1.kernel.types.KCamera;
import com.io7m.r1.kernel.types.KInstanceOpaqueType;
import com.io7m.r1.kernel.types.KInstanceTranslucentLitType;
import com.io7m.r1.kernel.types.KInstanceTranslucentUnlitType;
import com.io7m.r1.kernel.types.KLightTranslucentType;
import com.io7m.r1.kernel.types.KLightWithShadowType;
import com.io7m.r1.kernel.types.KMeshReadableType;
import com.io7m.r1.kernel.types.KUnitQuadCache;
import com.io7m.r1.kernel.types.KUnitQuadCacheType;
import com.io7m.r1.kernel.types.KVisibleSet;
import com.io7m.r1.kernel.types.KVisibleSetBuilderWithCreateType;
import com.io7m.r1.kernel.types.KVisibleSetLightGroupBuilderType;
import com.io7m.r1.spaces.RSpaceEyeType;
import com.io7m.r1.spaces.RSpaceWorldType;
import com.jogamp.newt.opengl.GLWindow;

/**
 * Basic example runner for scene types.
 */

public final class VExampleRunnerScene implements VExampleRunnerSceneType
{
  private @Nullable ETexture2DCache                   cache_2d;
  private @Nullable ETextureCubeCache                 cache_cube;
  private @Nullable EMeshCache                        cache_mesh;
  private @Nullable KUnitQuadCacheType                cache_quad;
  private final JCameraFPSStyleType                   camera;
  private final Context                               camera_context;
  private final JCameraInput                          camera_input;
  private final JCameraFPSStyleIntegratorType         camera_integrator;
  private JCameraFPSStyleSnapshot                     camera_snap_curr;
  private JCameraFPSStyleSnapshot                     camera_snap_prev;
  private final VExampleConfig                        config;
  private final ExampleSceneType                      example;
  private @Nullable KFramebufferDeferredType          framebuffer;
  private @Nullable JCGLImplementationType            gi;
  private @Nullable GL                                gl;
  private @Nullable TextureLoaderType                 loader;
  private final LogUsableType                         log;
  private final MatrixM4x4F                           matrix_view_temporary;
  private final AtomicReference<JCameraMouseRegion>   mouse_region;
  private final AtomicBoolean                         mouse_view;
  private @Nullable ExampleRendererType               renderer;
  private final ExampleRendererConstructorType        renderer_cons;
  private @Nullable AreaInclusive                     screen_area;
  private @Nullable KShaderCacheSetType               shader_cache;
  private @Nullable KImageSinkRGBAType<AreaInclusive> sink;
  private double                                      time_accum;
  private long                                        time_then;
  private final AtomicInteger                         view_index;
  private final AtomicBoolean                         want_save;
  private final @Nullable GLWindow                    window;

  /**
   * Construct an example runner.
   *
   * @param in_config
   *          The configuration
   * @param in_example
   *          The example
   * @param in_renderer_cons
   *          The renderer
   * @param in_window
   *          The window, if one is in use
   * @param in_log
   *          A log interface
   */

  public VExampleRunnerScene(
    final VExampleConfig in_config,
    final ExampleSceneType in_example,
    final ExampleRendererConstructorType in_renderer_cons,
    final @Nullable GLWindow in_window,
    final LogUsableType in_log)
  {
    this.example = NullCheck.notNull(in_example, "Example");
    this.renderer_cons = NullCheck.notNull(in_renderer_cons);
    this.log = NullCheck.notNull(in_log);
    this.config = NullCheck.notNull(in_config, "Config");
    this.want_save = new AtomicBoolean(false);

    this.camera = JCameraFPSStyle.newCamera();
    this.camera.cameraSetPosition3f(0.0f, 2.0f, 3.0f);

    this.camera_input = JCameraInput.newInput();
    this.camera_context = new JCameraFPSStyle.Context();

    this.camera_integrator =
      JCameraFPSStyleIntegrator.newIntegrator(this.camera, this.camera_input);
    this.camera_integrator.integratorAngularSetDragHorizontal(0.000000001f);
    this.camera_integrator.integratorAngularSetDragVertical(0.000000001f);
    this.camera_integrator
      .integratorAngularSetAccelerationHorizontal((float) ((Math.PI / 12) / (1.0 / 60.0)));
    this.camera_integrator
      .integratorAngularSetAccelerationVertical((float) ((Math.PI / 12) / (1.0 / 60.0)));

    this.camera_integrator
      .integratorLinearSetAcceleration((float) (3.0 / (1.0 / 60.0)));
    this.camera_integrator.integratorLinearSetMaximumSpeed(3.0f);
    this.camera_integrator.integratorLinearSetDrag(0.000000001f);

    this.camera_snap_prev = this.camera.cameraMakeSnapshot();
    this.camera_snap_curr = this.camera_snap_prev;

    this.mouse_view = new AtomicBoolean(false);
    this.matrix_view_temporary = new MatrixM4x4F();
    this.window = in_window;
    this.mouse_region =
      new AtomicReference<JCameraMouseRegion>(JCameraMouseRegion.newRegion(
        JCameraScreenOrigin.SCREEN_ORIGIN_TOP_LEFT,
        640.0f,
        480.0f));
    this.view_index = new AtomicInteger(0);

    this.time_then = System.nanoTime();
  }

  @Override public void display(
    final @Nullable GLAutoDrawable drawable)
  {
    assert drawable != null;
    final ETexture2DCache c2d = VExampleRunnerScene.this.cache_2d;
    assert c2d != null;
    final EMeshCache cm = VExampleRunnerScene.this.cache_mesh;
    assert cm != null;
    final ETextureCubeCache cc = VExampleRunnerScene.this.cache_cube;
    assert cc != null;
    final KUnitQuadCacheType qc = this.cache_quad;
    assert qc != null;
    final JCGLImplementationType g = this.gi;
    assert g != null;
    final KShaderCacheSetType sc = this.shader_cache;
    assert sc != null;

    final List<ExampleViewType> viewpoints = this.example.exampleViewpoints();
    final ExampleViewType view = viewpoints.get(this.view_index.get());
    assert view != null;

    final KCamera current_camera = this.getCamera(view.getCamera());

    final KVisibleSetBuilderWithCreateType builder =
      KVisibleSet.newBuilder(current_camera);

    final ExampleSceneBuilderType example_builder =
      new ExampleSceneBuilderType() {
        @Override public TextureCubeStaticUsableType cubeTextureClamped(
          final String name)
          throws RException
        {
          try {
            return cc.loadCubeClamped(name);
          } catch (final IOException e) {
            throw new RuntimeException(e);
          } catch (final JPropertyException e) {
            throw new RuntimeException(e);
          }
        }

        @Override public TextureCubeStaticUsableType cubeTextureRepeated(
          final String name)
          throws RException
        {
          try {
            return cc.loadCubeRepeat(name);
          } catch (final IOException e) {
            throw new RuntimeException(e);
          } catch (final JPropertyException e) {
            throw new RuntimeException(e);
          }
        }

        @Override public KMeshReadableType mesh(
          final String name)
          throws RException
        {
          return cm.loadMesh(name);
        }

        @Override public Texture2DStaticUsableType texture(
          final String name)
          throws RException
        {
          try {
            return c2d.loadTexture(name);
          } catch (final IOException e) {
            throw new RuntimeException(e);
          }
        }

        @Override public Texture2DStaticUsableType textureClamped(
          final String name)
          throws RException
        {
          try {
            return c2d.loadTextureClamped(name);
          } catch (final IOException e) {
            throw new RuntimeException(e);
          }
        }

        @Override public void visibleOpaqueAddUnlit(
          final KInstanceOpaqueType instance)
          throws RExceptionBuilderInvalid,
            RExceptionInstanceAlreadyVisible
        {
          builder.visibleOpaqueAddUnlit(instance);
        }

        @Override public
          KVisibleSetLightGroupBuilderType
          visibleOpaqueNewLightGroup(
            final String name)
            throws RExceptionLightGroupAlreadyAdded,
              RExceptionBuilderInvalid
        {
          return builder.visibleOpaqueNewLightGroup(name);
        }

        @Override public void visibleShadowsAddCaster(
          final KLightWithShadowType light,
          final KInstanceOpaqueType instance)
          throws RExceptionBuilderInvalid
        {
          builder.visibleShadowsAddCaster(light, instance);
        }

        @Override public void visibleShadowsAddLight(
          final KLightWithShadowType light)
          throws RExceptionBuilderInvalid
        {
          builder.visibleShadowsAddLight(light);
        }

        @Override public void visibleTranslucentsAddLit(
          final KInstanceTranslucentLitType instance,
          final Set<KLightTranslucentType> lights)
          throws RExceptionBuilderInvalid
        {
          builder.visibleTranslucentsAddLit(instance, lights);
        }

        @Override public void visibleTranslucentsAddUnlit(
          final KInstanceTranslucentUnlitType instance)
          throws RExceptionBuilderInvalid
        {
          builder.visibleTranslucentsAddUnlit(instance);
        }
      };

    try {
      this.example.exampleScene(example_builder);
      final KVisibleSet visible = builder.visibleCreate();
      final KFramebufferDeferredType fb =
        VExampleRunnerScene.this.framebuffer;
      assert fb != null;
      final ExampleRendererType r = this.renderer;
      assert r != null;

      fb.deferredFramebufferClear(
        g.getGLCommon(),
        ExampleSceneUtilities.RGBA_NOTHING,
        1.0f,
        0);

      r.rendererAccept(new ExampleRendererVisitorType<Unit>() {
        @Override public Unit visitDebug(
          final ExampleRendererDebugType rd)
          throws RException
        {
          rd.rendererDebugEvaluate(fb, visible);
          return Unit.unit();
        }

        @Override public Unit visitDeferred(
          final ExampleRendererDeferredType rd)
          throws RException
        {
          rd.rendererDeferredEvaluateFull(fb, visible);
          return Unit.unit();
        }
      });

      assert this.sink != null;
      assert this.screen_area != null;
      this.sink.sinkEvaluateRGBA(this.screen_area, fb);
    } catch (final RException e) {
      throw new RuntimeException(e);
    }

    if (this.mouse_view.get()) {
      final JCameraMouseRegion mr = this.mouse_region.get();
      final GLWindow w = this.window;
      assert w != null;
      w.warpPointer((int) mr.getCenterX(), (int) mr.getCenterY());
      w.confinePointer(true);
      w.setPointerVisible(false);
    } else {
      final GLWindow w = this.window;
      if (w != null) {
        w.confinePointer(false);
        w.setPointerVisible(true);
      }
    }

    if (this.want_save.get()) {
      this.doSaveImage();
      this.want_save.set(false);
    }
  }

  @Override public void dispose(
    final @Nullable GLAutoDrawable drawable)
  {
    assert drawable != null;

    this.log.debug("disposing");

    try {
      final JCGLImplementationType g = this.gi;
      if (g != null) {
        if (this.framebuffer != null) {
          this.framebuffer.delete(g);
        }
      }
    } catch (final RException e) {
      throw new RuntimeException(e);
    }
  }

  private void doSaveImage()
  {
    final KFramebufferDeferredType fb = this.framebuffer;
    if (fb != null) {
      final AreaInclusive area = fb.kFramebufferGetArea();
      final int width = (int) area.getRangeX().getInterval();
      final int height = (int) area.getRangeY().getInterval();

      final GL g = this.gl;
      assert g != null;
      final ExampleRendererType r = this.renderer;
      assert r != null;

      VSaveFramebuffer.saveSceneImage(
        this.config,
        g,
        width,
        height,
        ExampleTypeEnum.EXAMPLE_SCENE,
        this.example.exampleGetName(),
        r.exampleRendererGetName(),
        this.view_index.get(),
        this.log);
    }
  }

  private KCamera getCamera(
    final KCamera view_camera)
  {
    if (this.mouse_view.get()) {
      /**
       * Integrate the camera as many times as necessary for each rendering
       * frame interval.
       */

      final long time_now = System.nanoTime();
      final long time_diff = time_now - this.time_then;
      final double time_diff_s = time_diff / 1000000000.0;
      this.time_accum = this.time_accum + time_diff_s;
      this.time_then = time_now;

      final float delta = 1.0f / 60.0f;
      while (this.time_accum >= delta) {
        this.camera_integrator.integrate(delta);
        this.camera_snap_prev = this.camera_snap_curr;
        this.camera_snap_curr = this.camera.cameraMakeSnapshot();
        this.time_accum -= delta;
      }

      /**
       * Determine how far the current time is between the current camera
       * state and the next, and use that value to interpolate between the two
       * saved states.
       */

      final float alpha = (float) (this.time_accum / delta);
      final JCameraFPSStyleSnapshot snap_interpolated =
        JCameraFPSStyleSnapshot.interpolate(
          this.camera_snap_prev,
          this.camera_snap_curr,
          alpha);

      /**
       * Make a view matrix from the snapshot.
       */

      JCameraFPSStyle.cameraMakeViewMatrix(
        this.camera_context,
        snap_interpolated,
        this.matrix_view_temporary);

      final PMatrixI4x4F<RSpaceWorldType, RSpaceEyeType> m =
        PMatrixI4x4F.newFromReadableUntyped(this.matrix_view_temporary);

      return KCamera.newCamera(m, view_camera.getProjection());
    }

    return view_camera;
  }

  @Override public void init(
    final @Nullable GLAutoDrawable drawable)
  {
    try {
      this.log.debug("initializing");

      assert drawable != null;
      final GLContext ctx = drawable.getContext();
      assert ctx != null;
      final JCGLImplementationType g =
        VGLImplementation.makeGLImplementation(ctx, this.log);
      assert g != null;

      this.gl = drawable.getGL();
      this.gi = g;
      this.loader =
        TextureLoaderImageIO
          .newTextureLoaderWithAlphaPremultiplication(this.log);
      this.cache_2d = new ETexture2DCache(g, this.loader, this.log);
      this.cache_cube = new ETextureCubeCache(g, this.loader, this.log);
      this.cache_mesh = new EMeshCache(g, this.log);

      final KUnitQuadCacheType cq =
        KUnitQuadCache.newCache(g.getGLCommon(), this.log);
      this.cache_quad = cq;
      final KShaderCacheSetType sc =
        VShaderCaches.newCachesFromArchives(g, this.log);
      this.shader_cache = sc;

      this.renderer =
        this.renderer_cons.newRenderer(this.log, this.shader_cache, g);
      this.sink = KImageSinkBlitRGBA.newSink(g, sc.getShaderImageCache(), cq);
      this.screen_area =
        new AreaInclusive(
          new RangeInclusiveL(0, drawable.getWidth() - 1),
          new RangeInclusiveL(0, drawable.getHeight() - 1));

    } catch (final RException e) {
      throw new RuntimeException(e);
    } catch (final FilesystemError e) {
      throw new RuntimeException(e);
    }
  }

  @Override public void requestImageSave()
  {
    this.want_save.set(true);
  }

  @Override public void reshape(
    final @Nullable GLAutoDrawable drawable,
    final int x,
    final int y,
    final int width,
    final int height)
  {
    try {
      this.log.debug("reshaping");

      final JCGLImplementationType g = this.gi;
      if (g != null) {
        final KFramebufferDeferredType old_fb = this.framebuffer;
        final KFramebufferDeferredType new_fb =
          VExampleFramebuffer.makeFramebuffer(this.config, g, width, height);
        this.framebuffer = new_fb;

        if (old_fb != null) {
          old_fb.delete(g);
        }
      }

      final JCameraMouseRegion region =
        JCameraMouseRegion.newRegion(
          JCameraScreenOrigin.SCREEN_ORIGIN_TOP_LEFT,
          width,
          height);
      this.mouse_region.set(region);

      this.screen_area =
        new AreaInclusive(
          new RangeInclusiveL(0, width - 1),
          new RangeInclusiveL(0, height - 1));

    } catch (final RException e) {
      throw new RuntimeException(e);
    }
  }

  @Override public int viewGetCount()
  {
    return this.example.exampleViewpoints().size();
  }

  @Override public int viewGetIndex()
  {
    return this.view_index.get();
  }

  @Override public void viewMouseMoved(
    final int x,
    final int y)
  {
    if (this.mouse_view.get()) {
      final AtomicReference<JCameraMouseRegion> ref = this.mouse_region;
      final JCameraMouseRegion mr = ref.get();

      final JCameraRotationCoefficients coefficients =
        new JCameraRotationCoefficients();
      mr.getCoefficients(x, y, coefficients);

      final JCameraInput input = this.camera_input;
      input.addRotationAroundHorizontal(coefficients.getHorizontal());
      input.addRotationAroundVertical(coefficients.getVertical());
    }
  }

  @Override public void viewMouseToggle()
  {
    if (this.window != null) {
      this.mouse_view.set(!this.mouse_view.get());
      this.log.debug("mouse view: " + (this.mouse_view.get() ? "on" : "off"));
    } else {
      this.mouse_view.set(false);
    }
  }

  @Override public void viewNext()
  {
    final List<ExampleViewType> views = this.example.exampleViewpoints();
    this.view_index.set((this.view_index.get() + 1) % views.size());
    this.log.debug("view index: " + this.view_index.get());
  }

  @Override public void viewPrevious()
  {
    final List<ExampleViewType> views = this.example.exampleViewpoints();

    if (this.view_index.get() == 0) {
      this.view_index.set(views.size() - 1);
    } else {
      this.view_index.set(this.view_index.get() - 1);
    }
    this.log.debug("view index: " + this.view_index.get());
  }

  @Override public void viewSetMovingBackward(
    final boolean b)
  {
    this.camera_input.setMovingBackward(b);
  }

  @Override public void viewSetMovingDown(
    final boolean b)
  {
    this.camera_input.setMovingDown(b);
  }

  @Override public void viewSetMovingForward(
    final boolean b)
  {
    this.camera_input.setMovingForward(b);
  }

  @Override public void viewSetMovingLeft(
    final boolean b)
  {
    this.camera_input.setMovingLeft(b);
  }

  @Override public void viewSetMovingRight(
    final boolean b)
  {
    this.camera_input.setMovingRight(b);
  }

  @Override public void viewSetMovingUp(
    final boolean b)
  {
    this.camera_input.setMovingUp(b);
  }
}
