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

package com.io7m.renderer.kernel.sandbox;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jlog.Log;
import com.io7m.renderer.kernel.KFramebufferRGBACacheType;
import com.io7m.renderer.kernel.KFramebufferRGBAUsableType;
import com.io7m.renderer.kernel.KShaderCacheType;
import com.io7m.renderer.types.RException;

public interface SBKPostprocessor
{
  void postprocessorInitialize(
    final @Nonnull JCGLImplementation gi,
    final @Nonnull KFramebufferRGBACacheType rgba_cache,
    final @Nonnull KShaderCacheType shader_cache,
    final @Nonnull Log log)
    throws RException,
      ConstraintError;

  void postprocessorRun(
    final @Nonnull KFramebufferRGBAUsableType input,
    final @Nonnull KFramebufferRGBAUsableType output)
    throws RException,
      ConstraintError;

  void postprocessorClose();
}
