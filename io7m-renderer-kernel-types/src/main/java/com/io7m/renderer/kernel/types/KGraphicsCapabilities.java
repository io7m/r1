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

package com.io7m.renderer.kernel.types;

import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.api.JCGLExtensionESDepthTextureType;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLImplementationVisitorType;
import com.io7m.jcanephora.api.JCGLInterfaceGL2Type;
import com.io7m.jcanephora.api.JCGLInterfaceGL3Type;
import com.io7m.jcanephora.api.JCGLInterfaceGLES2Type;
import com.io7m.jcanephora.api.JCGLInterfaceGLES3Type;
import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jfunctional.OptionType;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.junreachable.UnreachableCodeException;

/**
 * The capabilities of the graphics system upon which the renderers are
 * currently executing.
 */

@EqualityStructural public final class KGraphicsCapabilities implements
  KGraphicsCapabilitiesType
{
  @SuppressWarnings("boxing") private static boolean decideDepthTextures(
    final JCGLImplementationType gi)
    throws JCGLException
  {
    return gi.implementationAccept(
      new JCGLImplementationVisitorType<Boolean, UnreachableCodeException>() {
        @Override public Boolean implementationIsGL2(
          final JCGLInterfaceGL2Type gl)
          throws JCGLException
        {
          return true;
        }

        @Override public Boolean implementationIsGL3(
          final JCGLInterfaceGL3Type gl)
          throws JCGLException
        {
          return true;
        }

        @Override public Boolean implementationIsGLES2(
          final JCGLInterfaceGLES2Type gl)
          throws JCGLException
        {
          final OptionType<JCGLExtensionESDepthTextureType> ext =
            gl.extensionDepthTexture();
          final boolean r = ext.isSome();
          return r;
        }

        @Override public Boolean implementationIsGLES3(
          final JCGLInterfaceGLES3Type gl)
          throws JCGLException
        {
          return true;
        }
      }).booleanValue();
  }

  /**
   * Determine the current graphics capabilities from the given
   * implementation.
   * 
   * @param gi
   *          The OpenGL implementation
   * @return The capabilities of the implementation
   * @throws JCGLException
   *           Iff an OpenGL error occurs
   */

  public static KGraphicsCapabilities getCapabilities(
    final JCGLImplementationType gi)
    throws JCGLException
  {
    NullCheck.notNull(gi, "OpenGL implementation");

    final int tu_count = gi.getGLCommon().textureGetUnits().size();
    final boolean depth_support =
      KGraphicsCapabilities.decideDepthTextures(gi);

    return new KGraphicsCapabilities(tu_count, depth_support);
  }

  private final boolean depth_textures;
  private final int     texture_units;

  private KGraphicsCapabilities(
    final int in_texture_units,
    final boolean in_depth_textures)
  {
    this.texture_units = in_texture_units;
    this.depth_textures = in_depth_textures;
  }

  @Override public boolean equals(
    final @Nullable Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final KGraphicsCapabilities other = (KGraphicsCapabilities) obj;
    return (this.depth_textures != other.depth_textures)
      && (this.texture_units != other.texture_units);
  }

  @Override public boolean getSupportsDepthTextures()
  {
    return this.depth_textures;
  }

  @Override public int getTextureUnits()
  {
    return this.texture_units;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + (this.depth_textures ? 1231 : 1237);
    result = (prime * result) + this.texture_units;
    return result;
  }
}
