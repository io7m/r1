package com.io7m.renderer.kernel_shaders;

import javax.annotation.Nonnull;

import com.io7m.jaux.UnimplementedCodeException;

enum ShadowLabel
{
  SHADOW_BASIC_OPAQUE("BO"),
  SHADOW_BASIC_TRANSLUCENT("BT"),
  SHADOW_BASIC_TRANSLUCENT_TEXTURED("BX"),

  SHADOW_BASIC_OPAQUE_PACKED4444("BOP4"),
  SHADOW_BASIC_TRANSLUCENT_PACKED4444("BTP4"),
  SHADOW_BASIC_TRANSLUCENT_TEXTURED_PACKED4444("BXP4")

  ;

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
      case SHADOW_BASIC_OPAQUE:
      case SHADOW_BASIC_TRANSLUCENT:
      case SHADOW_BASIC_OPAQUE_PACKED4444:
      case SHADOW_BASIC_TRANSLUCENT_PACKED4444:
        return false;
      case SHADOW_BASIC_TRANSLUCENT_TEXTURED:
      case SHADOW_BASIC_TRANSLUCENT_TEXTURED_PACKED4444:
        return true;
    }

    throw new UnimplementedCodeException();
  }
}
