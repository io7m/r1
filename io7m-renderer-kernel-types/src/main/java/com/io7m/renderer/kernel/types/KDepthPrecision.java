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

package com.io7m.renderer.kernel.types;

/**
 * A hint regarding the desired precision of a depth buffer. The
 * implementation is free to ignore the hint if the current graphics hardware
 * cannot provide a particular precision.
 */

public enum KDepthPrecision
{
  /**
   * Request a 16-bit depth buffer.
   */

  DEPTH_PRECISION_16,

  /**
   * Request a 24-bit depth buffer.
   */

  DEPTH_PRECISION_24,

  /**
   * Request a 32-bit floating point depth buffer.
   */

  DEPTH_PRECISION_32F
}
