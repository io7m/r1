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
import com.io7m.renderer.kernel.types.KInstanceTransformedOpaque;
import com.io7m.renderer.kernel.types.KMaterialDepthLabel;

final class KSceneBatchedDepth
{
  static interface Builder
  {
    public void sceneAddInstance(
      final @Nonnull KInstanceTransformedOpaque instance)
      throws ConstraintError;

    public @Nonnull
      Map<KMaterialDepthLabel, List<KInstanceTransformedOpaque>>
      sceneCreate();
  }

  private static final class KSceneBatchedDepthBuilder implements Builder
  {
    private final @Nonnull KMaterialDepthLabelCache                                   cache;
    private final @Nonnull Map<KMaterialDepthLabel, List<KInstanceTransformedOpaque>> in_progress;
    private boolean                                                                   valid;

    public KSceneBatchedDepthBuilder(
      final @Nonnull KMaterialDepthLabelCache cache)
      throws ConstraintError
    {
      this.valid = true;
      this.cache = Constraints.constrainNotNull(cache, "Depth label cache");
      this.in_progress =
        new HashMap<KMaterialDepthLabel, List<KInstanceTransformedOpaque>>();
    }

    @Override public void sceneAddInstance(
      final @Nonnull KInstanceTransformedOpaque instance)
      throws ConstraintError
    {
      Constraints.constrainNotNull(instance, "Instance");
      Constraints.constrainArbitrary(this.valid, "Builder is still valid");

      final KMaterialDepthLabel depth_label =
        this.cache.getDepthLabel(instance.instanceGet());
      List<KInstanceTransformedOpaque> depth_batch;
      if (this.in_progress.containsKey(depth_label)) {
        depth_batch = this.in_progress.get(depth_label);
      } else {
        depth_batch = new ArrayList<KInstanceTransformedOpaque>();
      }

      depth_batch.add(instance);
      this.in_progress.put(depth_label, depth_batch);
    }

    @Override public @Nonnull
      Map<KMaterialDepthLabel, List<KInstanceTransformedOpaque>>
      sceneCreate()
    {
      this.valid = false;
      return this.in_progress;
    }
  }

  public static @Nonnull Builder newBuilder(
    final @Nonnull KMaterialDepthLabelCache cache)
    throws ConstraintError
  {
    return new KSceneBatchedDepthBuilder(cache);
  }

  private KSceneBatchedDepth()
  {
    throw new UnreachableCodeException();
  }
}
