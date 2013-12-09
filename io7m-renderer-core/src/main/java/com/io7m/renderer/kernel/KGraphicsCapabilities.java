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

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.functional.Option;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLExtensionDepthTexture;
import com.io7m.jcanephora.JCGLExtensionESDepthTexture;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jcanephora.JCGLInterfaceGL2;
import com.io7m.jcanephora.JCGLInterfaceGL3;
import com.io7m.jcanephora.JCGLInterfaceGLES2;
import com.io7m.jcanephora.JCGLInterfaceGLES3;

/**
 * The capabilities of the graphics system upon which the renderers are
 * currently executing.
 */

public final class KGraphicsCapabilities
{
  private static boolean decideDepthTextures(
    final JCGLImplementation gi)
  {
    {
      final Option<JCGLInterfaceGL3> o = gi.getGL3();
      if (o.isSome()) {
        return true;
      }
    }

    {
      final Option<JCGLInterfaceGLES3> o = gi.getGLES3();
      if (o.isSome()) {
        return true;
      }
    }

    {
      final Option<JCGLInterfaceGL2> o = gi.getGL2();
      if (o.isSome()) {
        final JCGLInterfaceGL2 g = ((Option.Some<JCGLInterfaceGL2>) o).value;
        final Option<JCGLExtensionDepthTexture> e = g.extensionDepthTexture();

        if (e.isSome()) {
          return true;
        }
      }
    }

    {
      final Option<JCGLInterfaceGLES2> o = gi.getGLES2();

      if (o.isSome()) {
        final JCGLInterfaceGLES2 g =
          ((Option.Some<JCGLInterfaceGLES2>) o).value;

        final Option<JCGLExtensionESDepthTexture> e =
          g.extensionDepthTexture();

        if (e.isSome()) {
          return true;
        }
      }
    }

    return false;
  }

  public static @Nonnull KGraphicsCapabilities getCapabilities(
    final @Nonnull JCGLImplementation gi)
    throws JCGLException,
      ConstraintError
  {
    Constraints.constrainNotNull(gi, "OpenGL implementation");

    final int tu_count = gi.getGLCommon().textureGetUnits().size();
    final boolean depth_support =
      KGraphicsCapabilities.decideDepthTextures(gi);

    return new KGraphicsCapabilities(tu_count, depth_support);
  }

  private final boolean depth_textures;
  private final int     texture_units;

  private KGraphicsCapabilities(
    final int texture_units,
    final boolean depth_textures)
  {
    this.texture_units = texture_units;
    this.depth_textures = depth_textures;
  }

  /**
   * <p>
   * Return <code>true</code> if rendering to depth textures is supported.
   * This is supported on all but a small number of OpenGL ES 2 and OpenGL 2.1
   * implementations (and is in fact required by all subsequent OpenGL
   * standards)
   * </p>
   */

  public boolean getSupportsDepthTextures()
  {
    return this.depth_textures;
  }

  /**
   * <p>
   * Return the number of available texture units.
   * </p>
   * <p>
   * The number available is guaranteed to be at least:
   * </p>
   * <ul>
   * <li>2 on OpenGL 2.1</li>
   * <li>8 on OpenGL ES 2</li>
   * <li>16 on OpenGL ES 3</li>
   * <li>16 on OpenGL >= 3.0</li>
   * </ul>
   */

  public int getTextureUnits()
  {
    return this.texture_units;
  }

}
