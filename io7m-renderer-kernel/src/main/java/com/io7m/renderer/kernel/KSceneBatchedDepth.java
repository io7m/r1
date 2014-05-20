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

import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jnull.NullCheck;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.kernel.types.KInstanceOpaqueType;
import com.io7m.renderer.kernel.types.KMaterialDepthLabel;

@EqualityReference final class KSceneBatchedDepth
{
  interface BuilderType
  {
    void sceneAddInstance(
      final KInstanceOpaqueType instance);

      Map<KMaterialDepthLabel, List<KInstanceOpaqueType>>
      sceneCreate();
  }

  @EqualityReference private static final class KSceneBatchedDepthBuilder implements
    BuilderType
  {
    private final KMaterialDepthLabelCacheType                                   cache;
    private final Map<KMaterialDepthLabel, List<KInstanceOpaqueType>> in_progress;
    private boolean                                                              valid;

    public KSceneBatchedDepthBuilder(
      final KMaterialDepthLabelCacheType in_cache)
    {
      this.valid = true;
      this.cache = NullCheck.notNull(in_cache, "Depth label cache");
      this.in_progress =
        new HashMap<KMaterialDepthLabel, List<KInstanceOpaqueType>>();
    }

    @Override public void sceneAddInstance(
      final KInstanceOpaqueType instance)
    {
      NullCheck.notNull(instance, "Instance");

      if (this.valid == false) {
        throw new IllegalStateException("Builder has already been used");
      }

      final KMaterialDepthLabel depth_label =
        this.cache.getDepthLabel(instance.instanceGetMeshWithMaterial());
      List<KInstanceOpaqueType> depth_batch;
      if (this.in_progress.containsKey(depth_label)) {
        depth_batch = this.in_progress.get(depth_label);
      } else {
        depth_batch = new ArrayList<KInstanceOpaqueType>();
      }

      depth_batch.add(instance);
      this.in_progress.put(depth_label, depth_batch);
    }

    @Override public
      Map<KMaterialDepthLabel, List<KInstanceOpaqueType>>
      sceneCreate()
    {
      this.valid = false;
      return this.in_progress;
    }
  }

  public static BuilderType newBuilder(
    final KMaterialDepthLabelCacheType cache)
  {
    return new KSceneBatchedDepthBuilder(cache);
  }

  private KSceneBatchedDepth()
  {
    throw new UnreachableCodeException();
  }
}
