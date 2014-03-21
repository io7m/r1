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

import javax.annotation.Nonnull;

import com.io7m.jcache.JCacheLoader;
import com.io7m.renderer.kernel.types.KMesh;
import com.io7m.renderer.kernel.types.KMeshBounds;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RSpaceObject;

final class KMeshBoundsObjectSpaceCacheLoader implements
  JCacheLoader<KMesh, KMeshBounds<RSpaceObject>, RException>
{
  public static @Nonnull KMeshBoundsObjectSpaceCacheLoader newLoader()
  {
    return new KMeshBoundsObjectSpaceCacheLoader();
  }

  private KMeshBoundsObjectSpaceCacheLoader()
  {

  }

  @Override public void cacheValueClose(
    final @Nonnull KMeshBounds<RSpaceObject> v)
    throws RException
  {
    // Nothing
  }

  @Override public KMeshBounds<RSpaceObject> cacheValueLoad(
    final @Nonnull KMesh key)
    throws RException
  {
    return KMeshBounds.fromMeshObjectSpace(key);
  }

  @Override public BigInteger cacheValueSizeOf(
    final @Nonnull KMeshBounds<RSpaceObject> v)
  {
    return BigInteger.ONE;
  }
}