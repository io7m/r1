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

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * A single batch, with the given non-translucent instances having their
 * shadows calculated from the perspective of the given light
 * <code>light</code>.
 */

@Immutable final class KBatchOpaqueShadow
{
  public static @Nonnull KBatchOpaqueShadow newBatch(
    final @Nonnull KLight light,
    final @Nonnull List<KMeshInstance> instances)
  {
    return new KBatchOpaqueShadow(light, instances);
  }

  private final @Nonnull List<KMeshInstance>              instances;
  private final @Nonnull KMeshInstanceShadowMaterialLabel label;
  private final @Nonnull KLight                           light;

  private KBatchOpaqueShadow(
    final @Nonnull KLight light,
    final @Nonnull List<KMeshInstance> instances)
  {
    this.light = light;
    this.label = KMeshInstanceShadowMaterialLabel.SHADOW_OPAQUE;
    this.instances = instances;
  }

  @Override public boolean equals(
    final Object obj)
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
    final KBatchOpaqueShadow other = (KBatchOpaqueShadow) obj;
    if (!this.instances.equals(other.instances)) {
      return false;
    }
    if (!this.label.equals(other.label)) {
      return false;
    }
    if (!this.light.equals(other.light)) {
      return false;
    }
    return true;
  }

  public @Nonnull List<KMeshInstance> getInstances()
  {
    return Collections.unmodifiableList(this.instances);
  }

  public @Nonnull KMeshInstanceShadowMaterialLabel getLabel()
  {
    return this.label;
  }

  public @Nonnull KLight getLight()
  {
    return this.light;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.instances.hashCode();
    result = (prime * result) + this.label.hashCode();
    result = (prime * result) + this.light.hashCode();
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[KBatchOpaqueShadow ");
    builder.append(this.light);
    builder.append(" ");
    builder.append(this.label);
    builder.append(" ");
    builder.append(this.instances);
    builder.append("]");
    return builder.toString();
  }
}
