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

import java.math.BigInteger;

import com.io7m.jcache.JCacheLoaderType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.renderer.kernel.types.KMeshBounds;
import com.io7m.renderer.kernel.types.KMeshReadableType;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RSpaceObjectType;

/**
 * A cache loader that caches the results of calculating the object-space
 * bounds for a given mesh.
 */

@EqualityReference public final class KMeshBoundsObjectSpaceCacheLoader implements
  JCacheLoaderType<KMeshReadableType, KMeshBounds<RSpaceObjectType>, RException>
{
  /**
   * Construct a new cache loader.
   * 
   * @return A cache loader
   */

  public static KMeshBoundsObjectSpaceCacheLoader newLoader()
  {
    return new KMeshBoundsObjectSpaceCacheLoader();
  }

  private KMeshBoundsObjectSpaceCacheLoader()
  {

  }

  @Override public void cacheValueClose(
    final KMeshBounds<RSpaceObjectType> v)
    throws RException
  {
    // Nothing
  }

  @Override public KMeshBounds<RSpaceObjectType> cacheValueLoad(
    final KMeshReadableType key)
    throws RException
  {
    return KMeshBounds.fromMeshObjectSpace(key);
  }

  @Override public BigInteger cacheValueSizeOf(
    final KMeshBounds<RSpaceObjectType> v)
  {
    return BigInteger.ONE;
  }
}
