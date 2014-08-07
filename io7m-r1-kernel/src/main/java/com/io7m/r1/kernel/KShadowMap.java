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

import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jnull.NullCheck;
import com.io7m.r1.kernel.types.KShadowMapBasicDescription;
import com.io7m.r1.kernel.types.KShadowMapVarianceDescription;
import com.io7m.r1.types.RException;

@EqualityReference abstract class KShadowMap implements KShadowMapType
{
  @EqualityReference static final class KShadowMapBasic extends KShadowMap
  {
    private final KShadowMapBasicDescription description;
    private final KFramebufferDepth          framebuffer;

    KShadowMapBasic(
      final KShadowMapBasicDescription in_description,
      final KFramebufferDepth in_framebuffer)
    {
      this.description = NullCheck.notNull(in_description, "Description");
      this.framebuffer = NullCheck.notNull(in_framebuffer, "Framebuffer");
    }

    public KShadowMapBasicDescription getDescription()
    {
      return this.description;
    }

    public KFramebufferDepth getFramebuffer()
    {
      return this.framebuffer;
    }

    @Override public
      <A, E extends Throwable, V extends KShadowMapVisitorType<A, E>>
      A
      kShadowMapAccept(
        final V v)
        throws E,
          RException
    {
      return v.shadowMapVisitBasic(this);
    }

    @Override public void kShadowMapDelete(
      final JCGLImplementationType g)
      throws RException
    {
      this.framebuffer.kFramebufferDelete(g);
    }

    @Override public long kShadowMapGetSizeBytes()
    {
      return this.framebuffer.kFramebufferGetSizeBytes();
    }

    @Override public boolean resourceIsDeleted()
    {
      return this.framebuffer.resourceIsDeleted();
    }
  }

  @EqualityReference static final class KShadowMapVariance extends KShadowMap
  {
    private final KShadowMapVarianceDescription description;
    private final KFramebufferDepthVariance     framebuffer;

    KShadowMapVariance(
      final KShadowMapVarianceDescription in_description,
      final KFramebufferDepthVariance in_framebuffer)
    {
      this.description = NullCheck.notNull(in_description, "Description");
      this.framebuffer = NullCheck.notNull(in_framebuffer, "Framebuffer");
    }

    public KShadowMapVarianceDescription getDescription()
    {
      return this.description;
    }

    public KFramebufferDepthVariance getFramebuffer()
    {
      return this.framebuffer;
    }

    @Override public
      <A, E extends Throwable, V extends KShadowMapVisitorType<A, E>>
      A
      kShadowMapAccept(
        final V v)
        throws E,
          RException
    {
      return v.shadowMapVisitVariance(this);
    }

    @Override public void kShadowMapDelete(
      final JCGLImplementationType g)
      throws RException
    {
      this.framebuffer.kFramebufferDelete(g);
    }

    @Override public long kShadowMapGetSizeBytes()
    {
      return this.framebuffer.kFramebufferGetSizeBytes();
    }

    @Override public boolean resourceIsDeleted()
    {
      return this.framebuffer.resourceIsDeleted();
    }
  }
}
