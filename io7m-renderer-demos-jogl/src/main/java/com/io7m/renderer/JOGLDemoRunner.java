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

package com.io7m.renderer;

import java.util.HashMap;
import java.util.Properties;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.Nonnull;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLCapabilitiesImmutable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.functional.PartialFunction;
import com.io7m.jcanephora.GLCompileException;
import com.io7m.jcanephora.GLException;
import com.io7m.jcanephora.GLImplementationJOGL;
import com.io7m.jcanephora.TextureLoader;
import com.io7m.jcanephora.TextureLoaderImageIO;
import com.io7m.jlog.Log;
import com.io7m.jtensors.VectorM2I;
import com.io7m.jvvfs.Filesystem;
import com.io7m.jvvfs.PathVirtual;
import com.io7m.renderer.kernel.DemoKRendererFlat;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.FPSAnimator;

final class JOGLDemoRunner implements GLEventListener, KeyListener
{
  private enum Command
  {
    COMMAND_NEXT,
    COMMAND_PREVIOUS
  }

  private static final int FRAMES_PER_SECOND = 60;

  static void fatal(
    final Throwable e)
  {
    e.printStackTrace();
    System.exit(1);
  }

  @SuppressWarnings("unused") public static void main(
    final String args[])
  {
    try {
      new JOGLDemoRunner();
    } catch (final Throwable e) {
      JOGLDemoRunner.fatal(e);
    }
  }

  private final @Nonnull Log                                                           log;
  private final @Nonnull Log                                                           log_demos;

  protected final @Nonnull GLWindow                                                    window;
  private final @Nonnull FPSAnimator                                                   animator;
  private @Nonnull GLImplementationJOGL                                                gl_implementation;
  private final @Nonnull Filesystem                                                    filesystem;
  private final @Nonnull VectorM2I                                                     window_position;
  private final @Nonnull VectorM2I                                                     window_size;

  private @Nonnull DemoConfig                                                          config;
  private final @Nonnull HashMap<String, PartialFunction<DemoConfig, Demo, Throwable>> demos;
  private final @Nonnull TreeSet<String>                                               demos_names_sorted;
  private @Nonnull String                                                              demo_name_current;
  private @Nonnull Demo                                                                demo_current;

  private final @Nonnull ConcurrentLinkedQueue<Command>                                command_queue;
  private @Nonnull TextureLoader                                                       texture_loader;
  private int                                                                          frame =
                                                                                               0;

  JOGLDemoRunner()
    throws Throwable
  {
    this.command_queue = new ConcurrentLinkedQueue<JOGLDemoRunner.Command>();

    this.demos_names_sorted = new TreeSet<String>();
    this.demos =
      new HashMap<String, PartialFunction<DemoConfig, Demo, Throwable>>();
    this.demosInitialize();

    this.window_position = new VectorM2I(0, 0);
    this.window_size = new VectorM2I(640, 480);

    final Properties p = new Properties();
    p.setProperty("com.io7m.renderer.demos.logs.main.filesystem", "false");
    this.log = new Log(p, "com.io7m.renderer.demos", "main");
    this.log_demos = new Log(this.log, "demos");

    this.filesystem = Filesystem.makeWithoutArchiveDirectory(this.log);
    this.filesystem.mountClasspathArchive(Demo.class, PathVirtual.ROOT);
    this.filesystem.mountClasspathArchive(Version.class, PathVirtual.ROOT);

    final GLProfile profile = GLProfile.getDefault();
    final GLCapabilities requested_caps = new GLCapabilities(profile);
    requested_caps.setStencilBits(8);
    requested_caps.setDepthBits(24);
    requested_caps.setRedBits(8);
    requested_caps.setBlueBits(8);
    requested_caps.setGreenBits(8);

    this.window = GLWindow.create(requested_caps);
    this.window.setSize(this.window_size.x, this.window_size.y);
    this.window.setTitle(this.getClass().getName());
    this.window.addWindowListener(new WindowAdapter() {
      @Override public void windowDestroyNotify(
        final WindowEvent e)
      {
        System.exit(0);
      }
    });

    this.window.addGLEventListener(this);
    this.window.addKeyListener(this);

    this.animator =
      new FPSAnimator(this.window, JOGLDemoRunner.FRAMES_PER_SECOND);
    this.animator.setUpdateFPSFrames(
      JOGLDemoRunner.FRAMES_PER_SECOND,
      System.err);
    this.animator.start();

    this.window.setVisible(true);

    {
      final GLCapabilitiesImmutable actual_caps =
        this.window.getChosenGLCapabilities();
      this.log.info("Actual capabilities: " + actual_caps);

      if (actual_caps.getStencilBits() < 8) {
        this.log
          .critical("At least 8 bits of stencil buffer are required, got "
            + actual_caps.getStencilBits());
        System.exit(1);
      }

      if (actual_caps.getDepthBits() < 16) {
        this.log
          .critical("At least 16 bits of depth buffer are required, got "
            + actual_caps.getDepthBits());
        System.exit(1);
      }
    }
  }

  private void demoFirst()
  {
    try {
      this.demo_name_current = this.demos_names_sorted.first();
      this.log.debug("First: " + this.demo_name_current);
      this.demo_current =
        this.demos.get(this.demo_name_current).call(this.config);
    } catch (final Throwable x) {
      JOGLDemoRunner.fatal(x);
    }
  }

  private boolean demoHasNext()
  {
    return this.demos_names_sorted.higher(this.demo_name_current) != null;
  }

  private boolean demoHasPrevious()
  {
    return this.demos_names_sorted.lower(this.demo_name_current) != null;
  }

  private void demoNext()
  {
    try {
      this.demoShutdown();
      this.demo_name_current =
        this.demos_names_sorted.higher(this.demo_name_current);
      this.log.debug("Demo: " + this.demo_name_current);
      this.demo_current =
        this.demos.get(this.demo_name_current).call(this.config);
    } catch (final Throwable x) {
      JOGLDemoRunner.fatal(x);
    }
  }

  private void demoPrevious()
  {
    try {
      this.demoShutdown();
      this.demo_name_current =
        this.demos_names_sorted.lower(this.demo_name_current);
      this.log.debug("Demo: " + this.demo_name_current);
      this.demo_current =
        this.demos.get(this.demo_name_current).call(this.config);
    } catch (final Throwable x) {
      JOGLDemoRunner.fatal(x);
    }
  }

  private void demoShutdown()
  {
    try {
      this.log.debug("Stopping: " + this.demo_name_current);
      this.demo_current.shutdown();
      System.gc();
    } catch (final Throwable x) {
      JOGLDemoRunner.fatal(x);
    }
  }

  private void demosInitialize()
  {
    this.demos.put(
      DemoKRendererFlat.getName(),
      new PartialFunction<DemoConfig, Demo, Throwable>() {
        @Override public Demo call(
          final DemoConfig c)
          throws Throwable
        {
          JOGLDemoRunner.this.window.setTitle(DemoKRendererFlat.getName());
          return new DemoKRendererFlat(c);
        }
      });

    this.demos.put(
      DemoBlank.getName(),
      new PartialFunction<DemoConfig, Demo, Throwable>() {
        @Override public Demo call(
          final DemoConfig c)
          throws Throwable
        {
          JOGLDemoRunner.this.window.setTitle(DemoBlank.getName());
          return new DemoBlank(c);
        }
      });

    for (final String name : this.demos.keySet()) {
      this.demos_names_sorted.add(name);
    }
  }

  @Override public void display(
    final GLAutoDrawable drawable)
  {
    try {
      while (this.command_queue.peek() != null) {
        switch (this.command_queue.poll()) {
          case COMMAND_NEXT:
          {
            if (this.demoHasNext()) {
              this.demoNext();
            } else {
              this.demoShutdown();
              this.demoFirst();
            }
            break;
          }
          case COMMAND_PREVIOUS:
          {
            if (this.demoHasPrevious()) {
              this.demoPrevious();
            }
            break;
          }
        }
      }

      if (this.demo_current.hasShutDown() == false) {
        this.demo_current.display(++this.frame);
      }
    } catch (final GLException e) {
      JOGLDemoRunner.fatal(e);
    } catch (final GLCompileException e) {
      JOGLDemoRunner.fatal(e);
    } catch (final ConstraintError e) {
      JOGLDemoRunner.fatal(e);
    }
  }

  @Override public void dispose(
    final GLAutoDrawable drawable)
  {
    // Nothing.
  }

  @Override public void init(
    final GLAutoDrawable drawable)
  {
    try {
      this.texture_loader = new TextureLoaderImageIO();
      this.gl_implementation =
        new GLImplementationJOGL(drawable.getContext(), this.log);

      this.config =
        new DemoConfig(
          this.gl_implementation,
          this.texture_loader,
          this.log_demos,
          this.filesystem,
          this.window_position,
          this.window_size);

      this.demoFirst();

    } catch (final GLException e) {
      JOGLDemoRunner.fatal(e);
    } catch (final ConstraintError e) {
      JOGLDemoRunner.fatal(e);
    } catch (final Throwable e) {
      JOGLDemoRunner.fatal(e);
    }
  }

  @Override public void keyPressed(
    final KeyEvent e)
  {
    // Nothing
  }

  @Override public void keyReleased(
    final KeyEvent e)
  {
    try {
      switch (e.getKeyChar()) {
        case 'n':
        {
          this.command_queue.add(Command.COMMAND_NEXT);
          break;
        }
        case 'p':
        {
          this.command_queue.add(Command.COMMAND_PREVIOUS);
          break;
        }
      }

    } catch (final Throwable x) {
      JOGLDemoRunner.fatal(x);
    }
  }

  @Override public void reshape(
    final GLAutoDrawable drawable,
    final int x,
    final int y,
    final int w,
    final int h)
  {
    this.log.info("reshape " + w + "x" + h);

    this.window_position.x = x;
    this.window_position.y = y;
    this.window_size.x = w;
    this.window_size.y = h;

    try {
      this.demo_current.reshape(this.window_position, this.window_size);
    } catch (final GLException e) {
      JOGLDemoRunner.fatal(e);
    } catch (final GLCompileException e) {
      JOGLDemoRunner.fatal(e);
    } catch (final ConstraintError e) {
      JOGLDemoRunner.fatal(e);
    }
  }
}
