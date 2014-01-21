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

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.renderer.RException;
import com.io7m.renderer.kernel.KShadowMapDescription.KShadowMapBasicDescription;
import com.io7m.renderer.kernel.KShadowMapDescription.KShadowMapVarianceDescription;

abstract class KShadowMap implements KShadowMapType
{
  static final class KShadowMapBasic extends KShadowMap
  {
    private final @Nonnull KShadowMapBasicDescription description;
    private final @Nonnull KFramebufferDepth          framebuffer;

    KShadowMapBasic(
      final @Nonnull KShadowMapBasicDescription description,
      final @Nonnull KFramebufferDepth framebuffer)
      throws ConstraintError
    {
      this.description =
        Constraints.constrainNotNull(description, "Description");
      this.framebuffer =
        Constraints.constrainNotNull(framebuffer, "Framebuffer");
    }

    public @Nonnull KShadowMapBasicDescription getDescription()
    {
      return this.description;
    }

    public @Nonnull KFramebufferDepth getFramebuffer()
    {
      return this.framebuffer;
    }

    @Override public
      <A, E extends Throwable, V extends KShadowMapVisitor<A, E>>
      A
      kShadowMapAccept(
        final @Nonnull V v)
        throws E,
          RException
    {
      return v.shadowMapVisitBasic(this);
    }

    @Override public void kShadowMapDelete(
      final @Nonnull JCGLImplementation g)
      throws RException,
        ConstraintError
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

  static final class KShadowMapVariance extends KShadowMap
  {
    private final @Nonnull KShadowMapVarianceDescription description;
    private final @Nonnull KFramebufferDepthVariance     framebuffer;

    KShadowMapVariance(
      final @Nonnull KShadowMapVarianceDescription description,
      final @Nonnull KFramebufferDepthVariance framebuffer)
      throws ConstraintError
    {
      this.description =
        Constraints.constrainNotNull(description, "Description");
      this.framebuffer =
        Constraints.constrainNotNull(framebuffer, "Framebuffer");
    }

    public @Nonnull KShadowMapVarianceDescription getDescription()
    {
      return this.description;
    }

    public @Nonnull KFramebufferDepthVariance getFramebuffer()
    {
      return this.framebuffer;
    }

    @Override public
      <A, E extends Throwable, V extends KShadowMapVisitor<A, E>>
      A
      kShadowMapAccept(
        final @Nonnull V v)
        throws E,
          RException
    {
      return v.shadowMapVisitVariance(this);
    }

    @Override public void kShadowMapDelete(
      final @Nonnull JCGLImplementation g)
      throws RException,
        ConstraintError
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
