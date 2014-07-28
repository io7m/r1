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
import com.io7m.jcanephora.Texture2DStaticType;
import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jcanephora.TextureWrapS;
import com.io7m.jcanephora.TextureWrapT;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLImplementationVisitorType;
import com.io7m.jcanephora.api.JCGLInterfaceGL2Type;
import com.io7m.jcanephora.api.JCGLInterfaceGL3Type;
import com.io7m.jcanephora.api.JCGLInterfaceGLES2Type;
import com.io7m.jcanephora.api.JCGLInterfaceGLES3Type;
import com.io7m.junreachable.UnreachableCodeException;

public final class RFakeTextures2DStatic
{
  public static Generator<Texture2DStaticUsableType> generator(
    final JCGLImplementationType g,
    final Generator<String> name_gen)
  {
    return new Generator<Texture2DStaticUsableType>() {
      @Override public Texture2DStaticUsableType next()
      {
        try {
          return RFakeTextures2DStatic.makeActual(g, name_gen.next());
        } catch (final JCGLException e) {
          throw new UnreachableCodeException(e);
        }
      }
    };
  }

  private static Texture2DStaticType make(
    final JCGLImplementationType g)
  {
    try {
      return RFakeTextures2DStatic.makeActual(g, "any");
    } catch (final JCGLException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static Texture2DStaticType makeActual(
    final JCGLImplementationType g,
    final String name)
    throws JCGLException
  {
    return g
      .implementationAccept(new JCGLImplementationVisitorType<Texture2DStaticType, UnreachableCodeException>() {
        @Override public Texture2DStaticType implementationIsGL2(
          final JCGLInterfaceGL2Type gl)
          throws JCGLException
        {
          return gl.texture2DStaticAllocateRGB8(
            name,
            256,
            256,
            TextureWrapS.TEXTURE_WRAP_REPEAT,
            TextureWrapT.TEXTURE_WRAP_REPEAT,
            TextureFilterMinification.TEXTURE_FILTER_LINEAR,
            TextureFilterMagnification.TEXTURE_FILTER_LINEAR);
        }

        @Override public Texture2DStaticType implementationIsGL3(
          final JCGLInterfaceGL3Type gl)
          throws JCGLException
        {
          return gl.texture2DStaticAllocateRGB8(
            name,
            256,
            256,
            TextureWrapS.TEXTURE_WRAP_REPEAT,
            TextureWrapT.TEXTURE_WRAP_REPEAT,
            TextureFilterMinification.TEXTURE_FILTER_LINEAR,
            TextureFilterMagnification.TEXTURE_FILTER_LINEAR);
        }

        @Override public Texture2DStaticType implementationIsGLES2(
          final JCGLInterfaceGLES2Type gl)
          throws JCGLException
        {
          return gl.texture2DStaticAllocateRGB565(
            name,
            256,
            256,
            TextureWrapS.TEXTURE_WRAP_REPEAT,
            TextureWrapT.TEXTURE_WRAP_REPEAT,
            TextureFilterMinification.TEXTURE_FILTER_LINEAR,
            TextureFilterMagnification.TEXTURE_FILTER_LINEAR);
        }

        @Override public Texture2DStaticType implementationIsGLES3(
          final JCGLInterfaceGLES3Type gl)
          throws JCGLException
        {
          return gl.texture2DStaticAllocateRGB8(
            name,
            256,
            256,
            TextureWrapS.TEXTURE_WRAP_REPEAT,
            TextureWrapT.TEXTURE_WRAP_REPEAT,
            TextureFilterMinification.TEXTURE_FILTER_LINEAR,
            TextureFilterMagnification.TEXTURE_FILTER_LINEAR);
        }
      });
  }

  public static Texture2DStaticType newAnything()
  {
    final JCGLImplementationType g =
      RFakeGL.newFakeGL30(RFakeShaderControllers.newNull());
    return RFakeTextures2DStatic.make(g);
  }

  public static Texture2DStaticUsableType newAnything(
    final JCGLImplementationType g)
  {
    try {
      return RFakeTextures2DStatic.makeActual(g, "any");
    } catch (final JCGLException e) {
      throw new UnreachableCodeException(e);
    }
  }

  public static Texture2DStaticType newWithName(
    final JCGLImplementationType g,
    final String name)
  {
    try {
      return RFakeTextures2DStatic.makeActual(g, name);
    } catch (final JCGLException e) {
      throw new UnreachableCodeException(e);
    }
  }
}
