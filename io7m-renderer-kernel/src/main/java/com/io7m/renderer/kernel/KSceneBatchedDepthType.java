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

package com.io7m.renderer.kernel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.renderer.kernel.types.KInstanceTransformedOpaqueType;
import com.io7m.renderer.kernel.types.KMaterialDepthLabel;

final class KSceneBatchedDepthType
{
  interface BuilderType
  {
    void sceneAddInstance(
      final @Nonnull KInstanceTransformedOpaqueType instance)
      throws ConstraintError;

    @Nonnull
      Map<KMaterialDepthLabel, List<KInstanceTransformedOpaqueType>>
      sceneCreate();
  }

  private static final class KSceneBatchedDepthBuilder implements BuilderType
  {
    private final @Nonnull KMaterialDepthLabelCacheType                                   cache;
    private final @Nonnull Map<KMaterialDepthLabel, List<KInstanceTransformedOpaqueType>> in_progress;
    private boolean                                                                       valid;

    public KSceneBatchedDepthBuilder(
      final @Nonnull KMaterialDepthLabelCacheType in_cache)
      throws ConstraintError
    {
      this.valid = true;
      this.cache =
        Constraints.constrainNotNull(in_cache, "Depth label cache");
      this.in_progress =
        new HashMap<KMaterialDepthLabel, List<KInstanceTransformedOpaqueType>>();
    }

    @Override public void sceneAddInstance(
      final @Nonnull KInstanceTransformedOpaqueType instance)
      throws ConstraintError
    {
      Constraints.constrainNotNull(instance, "Instance");
      Constraints.constrainArbitrary(this.valid, "Builder is still valid");

      final KMaterialDepthLabel depth_label =
        this.cache.getDepthLabel(instance.instanceGet());
      List<KInstanceTransformedOpaqueType> depth_batch;
      if (this.in_progress.containsKey(depth_label)) {
        depth_batch = this.in_progress.get(depth_label);
      } else {
        depth_batch = new ArrayList<KInstanceTransformedOpaqueType>();
      }

      depth_batch.add(instance);
      this.in_progress.put(depth_label, depth_batch);
    }

    @Override public @Nonnull
      Map<KMaterialDepthLabel, List<KInstanceTransformedOpaqueType>>
      sceneCreate()
    {
      this.valid = false;
      return this.in_progress;
    }
  }

  public static @Nonnull BuilderType newBuilder(
    final @Nonnull KMaterialDepthLabelCacheType cache)
    throws ConstraintError
  {
    return new KSceneBatchedDepthBuilder(cache);
  }

  private KSceneBatchedDepthType()
  {
    throw new UnreachableCodeException();
  }
}
