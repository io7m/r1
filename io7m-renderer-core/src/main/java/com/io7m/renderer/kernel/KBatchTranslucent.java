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
 * A single translucent instance, lit by all of the given lights.
 */

@Immutable final class KBatchTranslucent
{
  public static @Nonnull KBatchTranslucent newBatch(
    final @Nonnull KMeshInstance instance,
    final @Nonnull List<KLight> lights)
  {
    return new KBatchTranslucent(instance, lights);
  }

  private final @Nonnull KMeshInstance instance;
  private final @Nonnull List<KLight>  lights;

  private KBatchTranslucent(
    final @Nonnull KMeshInstance instance,
    final @Nonnull List<KLight> lights)
  {
    this.instance = instance;
    this.lights = lights;
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
    final KBatchTranslucent other = (KBatchTranslucent) obj;
    if (!this.instance.equals(other.instance)) {
      return false;
    }
    if (!this.lights.equals(other.lights)) {
      return false;
    }
    return true;
  }

  public @Nonnull KMeshInstance getInstance()
  {
    return this.instance;
  }

  public @Nonnull List<KLight> getLights()
  {
    return Collections.unmodifiableList(this.lights);
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.instance.hashCode();
    result = (prime * result) + this.lights.hashCode();
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[KBatchTranslucent ");
    builder.append(this.instance);
    builder.append(" ");
    builder.append(this.lights);
    builder.append("]");
    return builder.toString();
  }

}
