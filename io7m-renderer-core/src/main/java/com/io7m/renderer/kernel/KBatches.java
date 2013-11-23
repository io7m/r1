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
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;

@Immutable final class KBatches
{
  public static @Nonnull KBatches newBatches(
    final @Nonnull Map<KLight, List<KBatchOpaqueLit>> opaque_lit,
    final @Nonnull List<KBatchOpaqueUnlit> opaque_unlit,
    final @Nonnull List<KBatchTranslucent> translucent,
    final @Nonnull List<KLight> shadow_lights,
    final @Nonnull Map<KLight, KBatchOpaqueShadow> shadow_opaque,
    final @Nonnull Map<KLight, KBatchTranslucentShadow> shadow_translucent)
    throws ConstraintError
  {
    return new KBatches(
      opaque_lit,
      opaque_unlit,
      translucent,
      shadow_lights,
      shadow_opaque,
      shadow_translucent);
  }

  private final @Nonnull Map<KLight, List<KBatchOpaqueLit>>   batches_opaque_lit;
  private final @Nonnull List<KBatchOpaqueUnlit>              batches_opaque_unlit;
  private final @Nonnull List<KBatchTranslucent>              batches_translucent;
  private final @Nonnull List<KLight>                         shadow_lights;
  private final @Nonnull Map<KLight, KBatchOpaqueShadow>      shadow_opaque;
  private final @Nonnull Map<KLight, KBatchTranslucentShadow> shadow_translucent;

  private KBatches(
    final @Nonnull Map<KLight, List<KBatchOpaqueLit>> opaque_lit,
    final @Nonnull List<KBatchOpaqueUnlit> opaque_unlit,
    final @Nonnull List<KBatchTranslucent> translucent,
    final @Nonnull List<KLight> shadow_lights,
    final @Nonnull Map<KLight, KBatchOpaqueShadow> shadow_opaque,
    final @Nonnull Map<KLight, KBatchTranslucentShadow> shadow_translucent)
    throws ConstraintError
  {
    this.batches_opaque_unlit =
      Constraints.constrainNotNull(opaque_unlit, "Opaque unlit");
    this.batches_opaque_lit =
      Constraints.constrainNotNull(opaque_lit, "Opaque lit");
    this.batches_translucent =
      Constraints.constrainNotNull(translucent, "Translucent");
    this.shadow_lights =
      Constraints.constrainNotNull(shadow_lights, "Shadow lights");
    this.shadow_opaque =
      Constraints.constrainNotNull(shadow_opaque, "Opaque shadow");
    this.shadow_translucent =
      Constraints.constrainNotNull(shadow_translucent, "Translucent shadow");
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
    final KBatches other = (KBatches) obj;
    if (!this.batches_opaque_lit.equals(other.batches_opaque_lit)) {
      return false;
    }
    if (!this.batches_opaque_unlit.equals(other.batches_opaque_unlit)) {
      return false;
    }
    if (!this.batches_translucent.equals(other.batches_translucent)) {
      return false;
    }
    if (!this.shadow_lights.equals(other.shadow_lights)) {
      return false;
    }
    if (!this.shadow_opaque.equals(other.shadow_opaque)) {
      return false;
    }
    if (!this.shadow_translucent.equals(other.shadow_translucent)) {
      return false;
    }
    return true;
  }

  public @Nonnull Map<KLight, List<KBatchOpaqueLit>> getBatchesOpaqueLit()
  {
    return Collections.unmodifiableMap(this.batches_opaque_lit);
  }

  public @Nonnull List<KBatchOpaqueUnlit> getBatchesOpaqueUnlit()
  {
    return Collections.unmodifiableList(this.batches_opaque_unlit);
  }

  public @Nonnull Map<KLight, KBatchOpaqueShadow> getBatchesShadowOpaque()
  {
    return Collections.unmodifiableMap(this.shadow_opaque);
  }

  public @Nonnull
    Map<KLight, KBatchTranslucentShadow>
    getBatchesShadowTranslucent()
  {
    return Collections.unmodifiableMap(this.shadow_translucent);
  }

  public @Nonnull List<KBatchTranslucent> getBatchesTranslucent()
  {
    return Collections.unmodifiableList(this.batches_translucent);
  }

  public @Nonnull List<KLight> getShadowLights()
  {
    return this.shadow_lights;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.batches_opaque_lit.hashCode();
    result = (prime * result) + this.batches_opaque_unlit.hashCode();
    result = (prime * result) + this.batches_translucent.hashCode();
    result = (prime * result) + this.shadow_lights.hashCode();
    result = (prime * result) + this.shadow_opaque.hashCode();
    result = (prime * result) + this.shadow_translucent.hashCode();
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[KBatches batches_opaque_lit=");
    builder.append(this.batches_opaque_lit);
    builder.append(" batches_opaque_unlit=");
    builder.append(this.batches_opaque_unlit);
    builder.append(" batches_translucent=");
    builder.append(this.batches_translucent);
    builder.append(" shadow_lights=");
    builder.append(this.shadow_lights);
    builder.append(" shadow_opaque=");
    builder.append(this.shadow_opaque);
    builder.append(" shadow_translucent=");
    builder.append(this.shadow_translucent);
    builder.append("]");
    return builder.toString();
  }
}
