package com.io7m.renderer.kernel_shaders;

import javax.annotation.Nonnull;

import com.io7m.jaux.UnimplementedCodeException;

enum ShadowLabel
{
  SHADOW_OPAQUE("SO"),
  SHADOW_TRANSLUCENT("ST"),
  SHADOW_TRANSLUCENT_TEXTURED("SX");

  private final String name;

  private ShadowLabel(
    final @Nonnull String name)
  {
    this.name = name;
  }

  @Override public @Nonnull String toString()
  {
    return this.name;
  }

  public String getCode()
  {
    return this.name;
  }

  public boolean impliesUV()
  {
    switch (this) {
      case SHADOW_OPAQUE:
      case SHADOW_TRANSLUCENT:
        return false;
      case SHADOW_TRANSLUCENT_TEXTURED:
        return true;
    }

    throw new UnimplementedCodeException();
  }
}
