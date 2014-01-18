package com.io7m.renderer.kernel;

import javax.annotation.Nonnull;

enum KShadowType
{
  SHADOW_MAPPED_BASIC("Mapped basic"),
  SHADOW_MAPPED_VARIANCE("Mapped variance");

  private final @Nonnull String name;

  private KShadowType(
    final @Nonnull String name)
  {
    this.name = name;
  }

  @Override public @Nonnull String toString()
  {
    return this.name;
  }
}