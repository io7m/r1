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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.functional.Unit;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jcanephora.JCGLImplementationVisitor;
import com.io7m.jcanephora.JCGLInterfaceGL2;
import com.io7m.jcanephora.JCGLInterfaceGL3;
import com.io7m.jcanephora.JCGLInterfaceGLES2;
import com.io7m.jcanephora.JCGLInterfaceGLES3;
import com.io7m.jcanephora.SpatialCursorReadable1f;
import com.io7m.jcanephora.SpatialCursorReadable2f;
import com.io7m.jcanephora.SpatialCursorReadable3f;
import com.io7m.jcanephora.SpatialCursorReadable4f;
import com.io7m.jcanephora.Texture2DReadableData;
import com.io7m.jcanephora.Texture2DStaticUsable;
import com.io7m.jcanephora.TextureType;
import com.io7m.jlog.Level;
import com.io7m.jlog.Log;
import com.io7m.jtensors.VectorM2F;
import com.io7m.jtensors.VectorM3F;
import com.io7m.jtensors.VectorM4F;

public final class SBTextureUtilities
{
  public static void textureDump2DStatic(
    final @Nonnull JCGLImplementation gi,
    final @Nonnull Texture2DStaticUsable t,
    final @Nonnull File file,
    final @Nonnull Log log)
    throws JCGLException,
      ConstraintError,
      FileNotFoundException,
      IOException
  {
    if (log.enabled(Level.LOG_DEBUG)) {
      final StringBuilder message = new StringBuilder();
      message.append("Dump ");
      message.append(t.getWidth());
      message.append("x");
      message.append(t.getHeight());
      message.append(" ");
      message.append(t.getType());
      message.append(" ");
      message.append(file);
      log.debug(message.toString());
    }

    gi
      .implementationAccept(new JCGLImplementationVisitor<Unit, IOException>() {
        @Override public Unit implementationIsGL2(
          final @Nonnull JCGLInterfaceGL2 gl)
          throws JCGLException,
            ConstraintError,
            FileNotFoundException,
            IOException
        {
          final Texture2DReadableData r = gl.texture2DStaticGetImage(t);
          SBTextureUtilities.textureDumpAsRGB(t.getType(), r, file);
          return Unit.unit();
        }

        @Override public Unit implementationIsGL3(
          final @Nonnull JCGLInterfaceGL3 gl)
          throws JCGLException,
            ConstraintError,
            FileNotFoundException,
            IOException
        {
          final Texture2DReadableData r = gl.texture2DStaticGetImage(t);
          SBTextureUtilities.textureDumpAsRGB(t.getType(), r, file);
          return Unit.unit();
        }

        @Override public Unit implementationIsGLES2(
          final @Nonnull JCGLInterfaceGLES2 gl)
          throws JCGLException,
            ConstraintError
        {
          log.debug("Cannot dump textures on this implementation");
          return Unit.unit();
        }

        @Override public Unit implementationIsGLES3(
          final @Nonnull JCGLInterfaceGLES3 gl)
          throws JCGLException,
            ConstraintError
        {
          log.debug("Cannot dump textures on this implementation");
          return Unit.unit();
        }
      });
  }

  public static void textureDumpAsRGB(
    final @Nonnull TextureType t,
    final @Nonnull Texture2DReadableData r,
    final @Nonnull File file)
    throws FileNotFoundException,
      IOException,
      ConstraintError
  {
    final AreaInclusive area = r.getArea();
    final int width = (int) area.getRangeX().getInterval();
    final int height = (int) area.getRangeY().getInterval();
    final BufferedImage image =
      new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

    switch (t) {
      case TEXTURE_TYPE_RG_16F_4BPP:
      case TEXTURE_TYPE_RG_16I_4BPP:
      case TEXTURE_TYPE_RG_16U_4BPP:
      case TEXTURE_TYPE_RG_16_4BPP:
      case TEXTURE_TYPE_RG_32I_8BPP:
      case TEXTURE_TYPE_RG_32U_8BPP:
      case TEXTURE_TYPE_RG_8I_2BPP:
      case TEXTURE_TYPE_RG_8U_2BPP:
      case TEXTURE_TYPE_RG_8_2BPP:
      case TEXTURE_TYPE_RG_32F_8BPP:
      {
        SBTextureUtilities.textureDumpAsRGB_RG(r, width, height, image);
        break;
      }
      case TEXTURE_TYPE_R_16F_2BPP:
      case TEXTURE_TYPE_R_16I_2BPP:
      case TEXTURE_TYPE_R_16U_2BPP:
      case TEXTURE_TYPE_R_16_2BPP:
      case TEXTURE_TYPE_R_32F_4BPP:
      case TEXTURE_TYPE_R_32I_4BPP:
      case TEXTURE_TYPE_R_32U_4BPP:
      case TEXTURE_TYPE_R_8I_1BPP:
      case TEXTURE_TYPE_R_8U_1BPP:
      case TEXTURE_TYPE_R_8_1BPP:
      case TEXTURE_TYPE_DEPTH_24_4BPP:
      case TEXTURE_TYPE_DEPTH_24_STENCIL_8_4BPP:
      case TEXTURE_TYPE_DEPTH_32F_4BPP:
      case TEXTURE_TYPE_DEPTH_16_2BPP:
      {
        SBTextureUtilities.textureDumpAsRGB_R(r, width, height, image);
        break;
      }
      case TEXTURE_TYPE_RGBA_1010102_4BPP:
      case TEXTURE_TYPE_RGBA_16F_8BPP:
      case TEXTURE_TYPE_RGBA_16I_8BPP:
      case TEXTURE_TYPE_RGBA_16U_8BPP:
      case TEXTURE_TYPE_RGBA_16_8BPP:
      case TEXTURE_TYPE_RGBA_32F_16BPP:
      case TEXTURE_TYPE_RGBA_32I_16BPP:
      case TEXTURE_TYPE_RGBA_32U_16BPP:
      case TEXTURE_TYPE_RGBA_4444_2BPP:
      case TEXTURE_TYPE_RGBA_5551_2BPP:
      case TEXTURE_TYPE_RGBA_8I_4BPP:
      case TEXTURE_TYPE_RGBA_8U_4BPP:
      case TEXTURE_TYPE_RGBA_8_4BPP:
      {
        SBTextureUtilities.textureDumpAsRGB_RGBA(r, width, height, image);
        break;
      }
      case TEXTURE_TYPE_RGB_16F_6BPP:
      case TEXTURE_TYPE_RGB_16I_6BPP:
      case TEXTURE_TYPE_RGB_16U_6BPP:
      case TEXTURE_TYPE_RGB_16_6BPP:
      case TEXTURE_TYPE_RGB_32F_12BPP:
      case TEXTURE_TYPE_RGB_32I_12BPP:
      case TEXTURE_TYPE_RGB_32U_12BPP:
      case TEXTURE_TYPE_RGB_565_2BPP:
      case TEXTURE_TYPE_RGB_8I_3BPP:
      case TEXTURE_TYPE_RGB_8U_3BPP:
      case TEXTURE_TYPE_RGB_8_3BPP:
      {
        SBTextureUtilities.textureDumpAsRGB_RGB(r, width, height, image);
        break;
      }
    }

    ImageIO.write(image, "PNG", file);
  }

  private static void textureDumpAsRGB_R(
    final @Nonnull Texture2DReadableData r,
    final int width,
    final int height,
    final @Nonnull BufferedImage image)
    throws ConstraintError
  {
    final SpatialCursorReadable1f c = r.getCursor1f();

    for (int y = 0; y < height; ++y) {
      for (int x = 0; x < width; ++x) {
        c.seekTo(x, y);
        final float k = c.get1f();
        final int rgb = (int) (k * 255.0);
        image.setRGB(x, (height - 1) - y, rgb);
      }
    }
  }

  private static void textureDumpAsRGB_RG(
    final @Nonnull Texture2DReadableData r,
    final int width,
    final int height,
    final @Nonnull BufferedImage image)
    throws ConstraintError
  {
    final SpatialCursorReadable2f c = r.getCursor2f();
    final VectorM2F rg = new VectorM2F();

    for (int y = 0; y < height; ++y) {
      for (int x = 0; x < width; ++x) {
        c.seekTo(x, y);
        c.get2f(rg);
        int rgb = 0;
        rgb |= ((int) (rg.x * 255.0)) << 16;
        rgb |= ((int) (rg.y * 255.0)) << 8;
        image.setRGB(x, (height - 1) - y, rgb);
      }
    }
  }

  private static void textureDumpAsRGB_RGB(
    final @Nonnull Texture2DReadableData r,
    final int width,
    final int height,
    final @Nonnull BufferedImage image)
    throws ConstraintError
  {
    final SpatialCursorReadable3f c = r.getCursor3f();
    final VectorM3F k = new VectorM3F();

    for (int y = 0; y < height; ++y) {
      for (int x = 0; x < width; ++x) {
        c.seekTo(x, y);
        c.get3f(k);
        int rgb = 0;
        rgb |= ((int) (k.x * 255.0)) << 16;
        rgb |= ((int) (k.y * 255.0)) << 8;
        rgb |= ((int) (k.z * 255.0));
        image.setRGB(x, (height - 1) - y, rgb);
      }
    }
  }

  private static void textureDumpAsRGB_RGBA(
    final @Nonnull Texture2DReadableData r,
    final int width,
    final int height,
    final @Nonnull BufferedImage image)
    throws ConstraintError
  {
    final SpatialCursorReadable4f c = r.getCursor4f();
    final VectorM4F k = new VectorM4F();

    for (int y = 0; y < height; ++y) {
      for (int x = 0; x < width; ++x) {
        c.seekTo(x, y);
        c.get4f(k);
        int rgb = 0;
        rgb |= ((int) (k.w * 255.0)) << 24;
        rgb |= ((int) (k.x * 255.0)) << 16;
        rgb |= ((int) (k.y * 255.0)) << 8;
        rgb |= ((int) (k.z * 255.0));
        image.setRGB(x, (height - 1) - y, rgb);
      }
    }
  }

  public static void textureDumpTimestampedTemporary(
    final @Nonnull Texture2DStaticUsable t,
    final @Nonnull Texture2DReadableData r,
    final @Nonnull String name)
    throws FileNotFoundException,
      IOException,
      ConstraintError
  {
    final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
    final String actual =
      String.format("%s-%s.png", name, fmt.format(cal.getTime()));
    final File out_file =
      new File(System.getProperty("java.io.tmpdir"), actual);
    SBTextureUtilities.textureDumpAsRGB(t.getType(), r, out_file);
  }

  public static void textureDumpTimestampedTemporary2DStatic(
    final @Nonnull JCGLImplementation gi,
    final @Nonnull Texture2DStaticUsable t,
    final @Nonnull String name,
    final @Nonnull Log log)
    throws FileNotFoundException,
      IOException,
      JCGLException,
      ConstraintError
  {
    final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
    final String actual =
      String.format("%s-%s.png", name, fmt.format(cal.getTime()));
    final File out_file =
      new File(System.getProperty("java.io.tmpdir"), actual);
    SBTextureUtilities.textureDump2DStatic(gi, t, out_file, log);
  }
}
