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

package com.io7m.renderer.tests;

import net.java.quickcheck.Generator;

import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.TextureCubeStaticType;
import com.io7m.jcanephora.TextureCubeStaticUsableType;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jcanephora.TextureWrapR;
import com.io7m.jcanephora.TextureWrapS;
import com.io7m.jcanephora.TextureWrapT;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLImplementationVisitorType;
import com.io7m.jcanephora.api.JCGLInterfaceGL2Type;
import com.io7m.jcanephora.api.JCGLInterfaceGL3Type;
import com.io7m.jcanephora.api.JCGLInterfaceGLES2Type;
import com.io7m.jcanephora.api.JCGLInterfaceGLES3Type;
import com.io7m.junreachable.UnreachableCodeException;

public final class RFakeTexturesCubeStatic
{
  public static Generator<TextureCubeStaticUsableType> generator(
    final JCGLImplementationType g,
    final Generator<String> name_gen)
  {
    return new Generator<TextureCubeStaticUsableType>() {
      @Override public TextureCubeStaticUsableType next()
      {
        try {
          return RFakeTexturesCubeStatic.makeActual(g, name_gen.next());
        } catch (final JCGLException e) {
          throw new UnreachableCodeException(e);
        }
      }
    };
  }

  private static TextureCubeStaticType make(
    final JCGLImplementationType g)
  {
    try {
      return RFakeTexturesCubeStatic.makeActual(g, "any");
    } catch (final JCGLException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static TextureCubeStaticType makeActual(
    final JCGLImplementationType g,
    final String name)
    throws JCGLException
  {
    return g
      .implementationAccept(new JCGLImplementationVisitorType<TextureCubeStaticType, UnreachableCodeException>() {
        @Override public TextureCubeStaticType implementationIsGL2(
          final JCGLInterfaceGL2Type gl)
          throws JCGLException
        {
          return gl.textureCubeStaticAllocateRGB8(
            name,
            256,
            TextureWrapR.TEXTURE_WRAP_REPEAT,
            TextureWrapS.TEXTURE_WRAP_REPEAT,
            TextureWrapT.TEXTURE_WRAP_REPEAT,
            TextureFilterMinification.TEXTURE_FILTER_LINEAR,
            TextureFilterMagnification.TEXTURE_FILTER_LINEAR);
        }

        @Override public TextureCubeStaticType implementationIsGL3(
          final JCGLInterfaceGL3Type gl)
          throws JCGLException
        {
          return gl.textureCubeStaticAllocateRGB8(
            name,
            256,
            TextureWrapR.TEXTURE_WRAP_REPEAT,
            TextureWrapS.TEXTURE_WRAP_REPEAT,
            TextureWrapT.TEXTURE_WRAP_REPEAT,
            TextureFilterMinification.TEXTURE_FILTER_LINEAR,
            TextureFilterMagnification.TEXTURE_FILTER_LINEAR);
        }

        @Override public TextureCubeStaticType implementationIsGLES2(
          final JCGLInterfaceGLES2Type gl)
          throws JCGLException
        {
          return gl.textureCubeStaticAllocateRGB565(
            name,
            256,
            TextureWrapR.TEXTURE_WRAP_REPEAT,
            TextureWrapS.TEXTURE_WRAP_REPEAT,
            TextureWrapT.TEXTURE_WRAP_REPEAT,
            TextureFilterMinification.TEXTURE_FILTER_LINEAR,
            TextureFilterMagnification.TEXTURE_FILTER_LINEAR);
        }

        @Override public TextureCubeStaticType implementationIsGLES3(
          final JCGLInterfaceGLES3Type gl)
          throws JCGLException
        {
          return gl.textureCubeStaticAllocateRGB8(
            name,
            256,
            TextureWrapR.TEXTURE_WRAP_REPEAT,
            TextureWrapS.TEXTURE_WRAP_REPEAT,
            TextureWrapT.TEXTURE_WRAP_REPEAT,
            TextureFilterMinification.TEXTURE_FILTER_LINEAR,
            TextureFilterMagnification.TEXTURE_FILTER_LINEAR);
        }
      });
  }

  public static TextureCubeStaticType newAnything()
  {
    final JCGLImplementationType g =
      RFakeGL.newFakeGL30(RFakeShaderControllers.newNull());
    return RFakeTexturesCubeStatic.make(g);
  }

  public static TextureCubeStaticType newAnything(
    final JCGLImplementationType g)
  {
    return RFakeTexturesCubeStatic.make(g);
  }
}
