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

package com.io7m.renderer.tests.kernel;

import java.util.ArrayList;
import java.util.List;

import com.io7m.jcanephora.CubeMapFaceLH;
import com.io7m.jcanephora.CubeMapFaceRH;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLExceptionRuntime;
import com.io7m.jcanephora.Texture2DStaticType;
import com.io7m.jcanephora.Texture2DStaticUpdateType;
import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jcanephora.TextureCubeStaticType;
import com.io7m.jcanephora.TextureCubeStaticUpdateType;
import com.io7m.jcanephora.TextureCubeStaticUsableType;
import com.io7m.jcanephora.TextureUnitType;
import com.io7m.jcanephora.api.JCGLTextureUnitsType;
import com.io7m.jcanephora.api.JCGLTextures2DStaticCommonType;
import com.io7m.jcanephora.api.JCGLTexturesCubeStaticCommonType;
import com.io7m.jnull.NonNull;
import com.io7m.jnull.NullCheck;

public final class KJCGLTextureFake implements
  JCGLTextures2DStaticCommonType,
  JCGLTexturesCubeStaticCommonType,
  JCGLTextureUnitsType
{
  private final List<Boolean>                  bound;
  private final @NonNull List<TextureUnitType> units;

  public KJCGLTextureFake(
    final @NonNull List<TextureUnitType> in_units)
  {
    this.units = NullCheck.notNullAll(in_units, "Units");
    this.bound = new ArrayList<Boolean>(in_units.size());

    for (int index = 0; index < in_units.size(); ++index) {
      this.bound.add(Boolean.FALSE);
    }
  }

  boolean isBound(
    final int index)
  {
    return this.bound.get(index).booleanValue();
  }

  @Override public void texture2DStaticBind(
    final TextureUnitType unit,
    final Texture2DStaticUsableType texture)
    throws JCGLException
  {
    System.err.printf("texture2DStaticBind: %d\n", unit.unitGetIndex());
    this.bound.set(unit.unitGetIndex(), Boolean.TRUE);
  }

  @Override public void texture2DStaticDelete(
    final Texture2DStaticType texture)
    throws JCGLException
  {
    // Nothing
  }

  @Override public boolean texture2DStaticIsBound(
    final TextureUnitType unit,
    final Texture2DStaticUsableType texture)
    throws JCGLException
  {
    return this.bound.get(unit.unitGetIndex()).booleanValue();
  }

  @Override public void texture2DStaticUnbind(
    final TextureUnitType unit)
    throws JCGLException
  {
    System.err.printf("texture2DStaticUnbind: %d\n", unit.unitGetIndex());
    this.bound.set(unit.unitGetIndex(), Boolean.FALSE);
  }

  @Override public void texture2DStaticUpdate(
    final Texture2DStaticUpdateType data)
    throws JCGLException
  {
    // Nothing
  }

  @Override public void textureCubeStaticBind(
    final TextureUnitType unit,
    final TextureCubeStaticUsableType texture)
    throws JCGLException
  {
    System.err.printf("textureCubeStaticBind: %d\n", unit.unitGetIndex());
    this.bound.set(unit.unitGetIndex(), Boolean.TRUE);
  }

  @Override public void textureCubeStaticDelete(
    final TextureCubeStaticType texture)
    throws JCGLException
  {
    // Nothing
  }

  @Override public boolean textureCubeStaticIsBound(
    final TextureUnitType unit,
    final TextureCubeStaticUsableType texture)
    throws JCGLException
  {
    return this.bound.get(unit.unitGetIndex()).booleanValue();
  }

  @Override public void textureCubeStaticUnbind(
    final TextureUnitType unit)
    throws JCGLException
  {
    System.err.printf("textureCubeStaticUnbind: %d\n", unit.unitGetIndex());
    this.bound.set(unit.unitGetIndex(), Boolean.FALSE);
  }

  @Override public void textureCubeStaticUpdateLH(
    final CubeMapFaceLH face,
    final TextureCubeStaticUpdateType data)
    throws JCGLException
  {
    // Nothing
  }

  @Override public void textureCubeStaticUpdateRH(
    final CubeMapFaceRH face,
    final TextureCubeStaticUpdateType data)
    throws JCGLException
  {
    // Nothing
  }

  @Override public int textureGetMaximumSize()
    throws JCGLExceptionRuntime
  {
    return 8192;
  }

  @Override public List<TextureUnitType> textureGetUnits()
    throws JCGLExceptionRuntime
  {
    return this.units;
  }
}
