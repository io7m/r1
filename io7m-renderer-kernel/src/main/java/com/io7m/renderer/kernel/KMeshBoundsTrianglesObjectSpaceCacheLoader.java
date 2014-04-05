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

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jcache.JCacheLoader;
import com.io7m.renderer.kernel.types.KMeshBounds;
import com.io7m.renderer.kernel.types.KMeshBoundsTriangles;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RSpaceObjectType;

final class KMeshBoundsTrianglesObjectSpaceCacheLoader implements
  JCacheLoader<KMeshBounds<RSpaceObjectType>, KMeshBoundsTriangles<RSpaceObjectType>, RException>
{
  public static @Nonnull
    KMeshBoundsTrianglesObjectSpaceCacheLoader
    newLoader()
  {
    return new KMeshBoundsTrianglesObjectSpaceCacheLoader();
  }

  private KMeshBoundsTrianglesObjectSpaceCacheLoader()
  {

  }

  @Override public void cacheValueClose(
    final @Nonnull KMeshBoundsTriangles<RSpaceObjectType> v)
    throws RException
  {
    // Nothing
  }

  @Override public KMeshBoundsTriangles<RSpaceObjectType> cacheValueLoad(
    final @Nonnull KMeshBounds<RSpaceObjectType> bounds)
    throws RException
  {
    try {
      return KMeshBoundsTriangles.newBoundsTriangles(bounds);
    } catch (final ConstraintError e) {
      throw new UnreachableCodeException();
    }
  }

  @Override public BigInteger cacheValueSizeOf(
    final @Nonnull KMeshBoundsTriangles<RSpaceObjectType> v)
  {
    return BigInteger.ONE;
  }
}
