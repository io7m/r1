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

/**
 * <p>
 * The type of framebuffer configurations suitable for forward rendering, with
 * a depth buffer that can be sampled (for shadow mapping and similar
 * techniques).
 * </p>
 * <p>
 * Implementation note: On platforms that support depth textures, a single
 * framebuffer will be allocated with an RGBA texture color attachment, and a
 * depth-texture depth attachment. In this case,
 * {@link #kFramebufferGetColorFramebuffer()} ==
 * {@link #kFramebufferGetDepthPassFramebuffer()}. On platforms that do not
 * support depth textures, two framebuffers <tt>F0</tt> and <tt>F1</tt> will
 * be allocated. <tt>F0</tt> will consist of an RGBA texture color attachment
 * and a depth renderbuffer <tt>R</tt>, and <tt>F1</tt> will consist of an
 * RGBA texture to which packed depth values will be encoded, and the same
 * depth renderbuffer <tt>R</tt>. Consult
 * {@link #kFramebufferGetDepthIsPackedColour()}.
 * </p>
 */

public interface KFramebufferForwardType extends
  KFramebufferRGBAType,
  KFramebufferDepthType,
  KFramebufferForwardUsableType
{
  // No extras.
}
