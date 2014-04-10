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

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jcache.LRUCacheAbstract;
import com.io7m.jcache.LRUCacheType;
import com.io7m.renderer.types.RException;

/**
 * Shader caches.
 */

public final class KShaderCache extends
  LRUCacheAbstract<String, KProgram, RException> implements KShaderCacheType
{
  /**
   * Wrap the given cache and expose a {@link KShaderCacheType} interface.
   * 
   * @param c
   *          The cache
   * @return A cache
   * @throws ConstraintError
   *           If <code>c == null</code>
   */

  public static @Nonnull KShaderCacheType wrap(
    final @Nonnull LRUCacheType<String, KProgram, RException> c)
    throws ConstraintError
  {
    return new KShaderCache(c);
  }

  private KShaderCache(
    final @Nonnull LRUCacheType<String, KProgram, RException> c)
    throws ConstraintError
  {
    super(c);
  }
}