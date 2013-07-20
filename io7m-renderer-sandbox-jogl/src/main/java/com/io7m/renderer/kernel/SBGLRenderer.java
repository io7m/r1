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

import java.io.File;
import java.io.InputStream;
import java.util.Queue;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Function;
import com.io7m.jaux.functional.Pair;
import com.io7m.jaux.functional.Unit;
import com.io7m.jcanephora.GLException;
import com.io7m.jcanephora.GLImplementationJOGL;
import com.io7m.jcanephora.GLInterfaceCommon;
import com.io7m.jcanephora.GLUnsupportedException;
import com.io7m.jcanephora.Texture2DStatic;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jcanephora.TextureLoader;
import com.io7m.jcanephora.TextureLoaderImageIO;
import com.io7m.jcanephora.TextureWrapS;
import com.io7m.jcanephora.TextureWrapT;
import com.io7m.jlog.Log;
import com.io7m.jsom0.Model;
import com.io7m.jsom0.ModelObjectVBO;
import com.io7m.jsom0.NameNormalAttribute;
import com.io7m.jsom0.NamePositionAttribute;
import com.io7m.jsom0.NameUVAttribute;
import com.io7m.jsom0.parser.ModelObjectParser;
import com.io7m.jsom0.parser.ModelObjectParserVBOImmediate;
import com.io7m.jsom0.parser.ModelParser;

final class SBGLRenderer implements GLEventListener
{
  private class MeshDeleteFuture extends FutureTask<Void>
  {
    MeshDeleteFuture(
      final @Nonnull SBMesh mesh)
    {
      super(new Callable<Void>() {
        @SuppressWarnings("synthetic-access") @Override public @Nonnull
          Void
          call()
            throws Exception
        {
          SBGLRenderer.this.log.debug("Deleting mesh " + mesh);

          if (SBGLRenderer.this.gi != null) {
            final GLInterfaceCommon gl = SBGLRenderer.this.gi.getGLCommon();

            try {
              gl.arrayBufferDelete(mesh.getArrayBuffer());
              gl.indexBufferDelete(mesh.getIndexBuffer());
            } catch (final ConstraintError e) {
              throw new UnreachableCodeException();
            }
          }

          throw new GLException(-1, "OpenGL not ready!");
        }
      });
    }
  }

  private class MeshLoadFuture extends
    FutureTask<Pair<File, SortedSet<SBMesh>>>
  {
    MeshLoadFuture(
      final @Nonnull File file,
      final @Nonnull InputStream stream)
    {
      super(new Callable<Pair<File, SortedSet<SBMesh>>>() {
        @SuppressWarnings("synthetic-access") @Override public @Nonnull
          Pair<File, SortedSet<SBMesh>>
          call()
            throws Exception
        {
          SBGLRenderer.this.log.debug("Loading " + file);

          if (SBGLRenderer.this.gi != null) {
            final GLInterfaceCommon gl = SBGLRenderer.this.gi.getGLCommon();

            try {
              final NamePositionAttribute name_position_attribute =
                new NamePositionAttribute("position");
              final NameNormalAttribute name_normal_attribute =
                new NameNormalAttribute("normal");
              final NameUVAttribute name_uv_attribute =
                new NameUVAttribute("uv");

              final ModelObjectParser<ModelObjectVBO, GLException> op =
                new ModelObjectParserVBOImmediate<GLInterfaceCommon>(
                  file.toString(),
                  stream,
                  name_position_attribute,
                  name_normal_attribute,
                  name_uv_attribute,
                  SBGLRenderer.this.log,
                  gl);

              final ModelParser<ModelObjectVBO, GLException> mp =
                new ModelParser<ModelObjectVBO, GLException>(op);

              final Model<ModelObjectVBO> m = mp.model();
              if (SBGLRenderer.this.meshes.containsKey(file)) {
                SBGLRenderer.this.log.debug("Reloading " + file);
              }
              SBGLRenderer.this.meshes.put(file, m);

              final TreeSet<SBMesh> objects = new TreeSet<SBMesh>();
              m.forEachObject(new Function<ModelObjectVBO, Unit>() {
                @Override public Unit call(
                  final ModelObjectVBO x)
                {
                  objects.add(new SBMesh(x.getName(), x.getArrayBuffer(), x
                    .getIndexBuffer()));
                  return Unit.value;
                }
              });

              SBGLRenderer.this.log.debug("Loaded " + file);
              return new Pair<File, SortedSet<SBMesh>>(file, objects);
            } catch (final ConstraintError e) {
              throw new UnreachableCodeException();
            }
          }

          throw new GLException(-1, "OpenGL not ready!");
        }
      });
    }
  }

  private class TextureDeleteFuture extends FutureTask<Void>
  {
    TextureDeleteFuture(
      final @Nonnull Texture2DStatic texture)
    {
      super(new Callable<Void>() {
        @SuppressWarnings("synthetic-access") @Override public @Nonnull
          Void
          call()
            throws Exception
        {
          SBGLRenderer.this.log.debug("Deleting " + texture);

          if (SBGLRenderer.this.gi != null) {
            final GLInterfaceCommon gl = SBGLRenderer.this.gi.getGLCommon();
            try {
              gl.texture2DStaticDelete(texture);
            } catch (final ConstraintError e) {
              throw new UnreachableCodeException();
            }
          }

          throw new GLException(-1, "OpenGL not ready!");
        }
      });
    }
  }

  private class TextureLoadFuture extends FutureTask<Texture2DStatic>
  {
    TextureLoadFuture(
      final @Nonnull File file,
      final @Nonnull InputStream stream)
    {
      super(new Callable<Texture2DStatic>() {
        @SuppressWarnings("synthetic-access") @Override public @Nonnull
          Texture2DStatic
          call()
            throws Exception
        {
          SBGLRenderer.this.log.debug("Loading " + file);

          if (SBGLRenderer.this.gi != null) {
            final GLInterfaceCommon gl = SBGLRenderer.this.gi.getGLCommon();
            try {
              final Texture2DStatic t =
                SBGLRenderer.this.texture_loader.load2DStaticInferredGLES2(
                  gl,
                  TextureWrapS.TEXTURE_WRAP_REPEAT,
                  TextureWrapT.TEXTURE_WRAP_REPEAT,
                  TextureFilterMinification.TEXTURE_FILTER_NEAREST,
                  TextureFilterMagnification.TEXTURE_FILTER_NEAREST,
                  stream,
                  file.toString());
              if (SBGLRenderer.this.textures.containsKey(file)) {
                SBGLRenderer.this.log.debug("Reloading " + file);
                final Texture2DStatic u =
                  SBGLRenderer.this.textures.get(file);
                gl.texture2DStaticDelete(u);
              }
              SBGLRenderer.this.textures.put(file, t);
              SBGLRenderer.this.log.debug("Loaded " + file);
              return t;
            } catch (final ConstraintError e) {
              throw new UnreachableCodeException();
            }
          }

          throw new GLException(-1, "OpenGL not ready!");
        }
      });
    }
  }

  private static <A, B extends FutureTask<A>> void processQueue(
    final @Nonnull Queue<B> q)
  {
    for (;;) {
      final FutureTask<?> f = q.poll();
      if (f == null) {
        break;
      }
      f.run();
    }
  }

  private final @Nonnull Log                                            log;
  private @CheckForNull GLImplementationJOGL                            gi;
  private final @Nonnull TextureLoader                                  texture_loader;
  private final @Nonnull ConcurrentLinkedQueue<TextureLoadFuture>       texture_load_queue;

  private final @Nonnull ConcurrentLinkedQueue<TextureDeleteFuture>     texture_delete_queue;

  private final @Nonnull ConcurrentLinkedQueue<MeshLoadFuture>          mesh_load_queue;

  private final @Nonnull ConcurrentLinkedQueue<MeshDeleteFuture>        mesh_delete_queue;

  private final @Nonnull ConcurrentHashMap<File, Texture2DStatic>       textures;

  private final @Nonnull ConcurrentHashMap<File, Model<ModelObjectVBO>> meshes;

  public SBGLRenderer(
    final @Nonnull Log log)
  {
    this.log = new Log(log, "gl");
    this.texture_loader = new TextureLoaderImageIO();
    this.texture_load_queue = new ConcurrentLinkedQueue<TextureLoadFuture>();
    this.texture_delete_queue =
      new ConcurrentLinkedQueue<TextureDeleteFuture>();
    this.textures = new ConcurrentHashMap<File, Texture2DStatic>();
    this.mesh_load_queue = new ConcurrentLinkedQueue<MeshLoadFuture>();
    this.mesh_delete_queue = new ConcurrentLinkedQueue<MeshDeleteFuture>();
    this.meshes = new ConcurrentHashMap<File, Model<ModelObjectVBO>>();
  }

  @Override public void display(
    final @Nonnull GLAutoDrawable drawable)
  {
    if (this.gi == null) {
      return;
    }

    try {
      final GLInterfaceCommon gl = this.gi.getGLCommon();
      gl.colorBufferClear3f(0.0f, 0.0f, 1.0f);

      SBGLRenderer.processQueue(this.mesh_delete_queue);
      SBGLRenderer.processQueue(this.texture_delete_queue);
      SBGLRenderer.processQueue(this.texture_load_queue);
      SBGLRenderer.processQueue(this.mesh_load_queue);

    } catch (final GLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final ConstraintError e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override public void dispose(
    final @Nonnull GLAutoDrawable drawable)
  {
    // TODO Auto-generated method stub
  }

  @Override public void init(
    final @Nonnull GLAutoDrawable drawable)
  {
    this.log.debug("initialized");
    try {
      this.gi = new GLImplementationJOGL(drawable.getContext(), this.log);

    } catch (final GLException e) {
      this.log.critical(e.getMessage());
    } catch (final GLUnsupportedException e) {
      this.log.critical(e.getMessage());
    } catch (final ConstraintError e) {
      this.log.critical(e.getMessage());
    }
  }

  void meshDelete(
    final @Nonnull SBMesh m)
  {
    final MeshDeleteFuture f = new MeshDeleteFuture(m);
    this.mesh_delete_queue.add(f);
  }

  Future<Pair<File, SortedSet<SBMesh>>> meshLoad(
    final @Nonnull File file,
    final @Nonnull InputStream stream)
  {
    final MeshLoadFuture f = new MeshLoadFuture(file, stream);
    this.mesh_load_queue.add(f);
    return f;
  }

  @Override public void reshape(
    final @Nonnull GLAutoDrawable drawable,
    final int x,
    final int y,
    final int width,
    final int height)
  {
    // TODO Auto-generated method stub
  }

  void textureDelete(
    final @Nonnull Texture2DStatic t)
  {
    final TextureDeleteFuture f = new TextureDeleteFuture(t);
    this.texture_delete_queue.add(f);
  }

  Future<Texture2DStatic> textureLoad(
    final @Nonnull File file,
    final @Nonnull InputStream stream)
  {
    final TextureLoadFuture f = new TextureLoadFuture(file, stream);
    this.texture_load_queue.add(f);
    return f;
  }
}
