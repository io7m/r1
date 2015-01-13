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

package com.io7m.r1.kernel;

import java.math.BigInteger;

import com.io7m.jcache.JCacheLoaderType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.parameterized.PMatrixM4x4F;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.kernel.types.KProjectionType;
import com.io7m.r1.spaces.RSpaceClipType;
import com.io7m.r1.spaces.RSpaceEyeType;

/**
 * A cache loader that constructs and caches view rays for projections.
 */

@EqualityReference public final class KViewRaysCacheLoader implements
  JCacheLoaderType<KProjectionType, KViewRays, RException>
{
  /**
   * Construct a new cache loader.
   *
   * @param context
   *          Preallocated temporary matrix storage
   *
   * @return A new cache loader
   */

  public static
    JCacheLoaderType<KProjectionType, KViewRays, RException>
    newLoader(
      final PMatrixM4x4F.Context context)
  {
    return new KViewRaysCacheLoader(context);
  }

  private final PMatrixM4x4F.Context                        context;
  private final PMatrixM4x4F<RSpaceClipType, RSpaceEyeType> matrix;

  private KViewRaysCacheLoader(
    final PMatrixM4x4F.Context in_context)
  {
    this.context = NullCheck.notNull(in_context, "Context");
    this.matrix = new PMatrixM4x4F<RSpaceClipType, RSpaceEyeType>();
  }

  @Override public void cacheValueClose(
    final KViewRays v)
    throws RException
  {
    // Nothing
  }

  @Override public KViewRays cacheValueLoad(
    final KProjectionType p)
    throws RException
  {
    @SuppressWarnings("unchecked") final PMatrixM4x4F<RSpaceEyeType, RSpaceClipType> vi_temp =
      (PMatrixM4x4F<RSpaceEyeType, RSpaceClipType>) (PMatrixM4x4F<?, ?>) this.matrix;
    p.projectionGetMatrix().makeMatrixM4x4F(vi_temp);
    PMatrixM4x4F.invertInPlaceWithContext(this.context, this.matrix);
    return KViewRays.newRays(this.context, this.matrix);
  }

  @Override public BigInteger cacheValueSizeOf(
    final KViewRays v)
  {
    final BigInteger r = BigInteger.ONE;
    assert r != null;
    return r;
  }
}
