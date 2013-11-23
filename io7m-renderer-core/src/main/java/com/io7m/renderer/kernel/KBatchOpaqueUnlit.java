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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * A single batch, with the given instances being rendered with a
 * non-translucent material <code>label</code>, without being influenced by
 * any lights (the objects will appear at full brightness).
 */

@Immutable final class KBatchOpaqueUnlit
{
  public static @Nonnull KBatchOpaqueUnlit newBatch(
    final @Nonnull KMeshInstanceForwardMaterialLabel label,
    final @Nonnull ArrayList<KMeshInstance> instances)
  {
    return new KBatchOpaqueUnlit(label, instances);
  }

  private final @Nonnull ArrayList<KMeshInstance>          instances;
  private final @Nonnull KMeshInstanceForwardMaterialLabel label;

  private KBatchOpaqueUnlit(
    final @Nonnull KMeshInstanceForwardMaterialLabel label,
    final @Nonnull ArrayList<KMeshInstance> instances)
  {
    this.label = label;
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
    final KBatchOpaqueUnlit other = (KBatchOpaqueUnlit) obj;
    if (!this.instances.equals(other.instances)) {
      return false;
    }
    if (!this.label.equals(other.label)) {
      return false;
    }
    return true;
  }

  public @Nonnull List<KMeshInstance> getInstances()
  {
    return Collections.unmodifiableList(this.instances);
  }

  public @Nonnull KMeshInstanceForwardMaterialLabel getLabel()
  {
    return this.label;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.instances.hashCode();
    result = (prime * result) + this.label.hashCode();
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[KBatchOpaqueUnlit ");
    builder.append(this.label);
    builder.append(" ");
    builder.append(this.instances);
    builder.append("]");
    return builder.toString();
  }
}