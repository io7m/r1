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

import com.io7m.renderer.types.RException;

/**
 * The type of postprocessors that operate on RGBA values.
 * 
 * @param <C>
 *          The type of renderer-specific configuration values
 */

public interface KPostprocessorRGBAType<C> extends KPostprocessorType
{
  /**
   * <p>
   * Evaluate the postprocessor, reading from <code>input</code> and writing
   * to <code>output</code>.
   * </p>
   * <p>
   * All postprocessors must be able to handle the case that
   * <code>input == output</code>, possibly by performing operations on
   * temporary internal framebuffers.
   * </p>
   * 
   * @param config
   *          The postprocessor-specific config values
   * @param input
   *          The input framebuffer
   * @param output
   *          The output framebuffer
   * 
   * @throws RException
   *           If any error occurs
   */

  void postprocessorEvaluateRGBA(
    final C config,
    final KFramebufferRGBAUsableType input,
    final KFramebufferRGBAUsableType output)
    throws RException;
}
