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
import javax.annotation.concurrent.Immutable;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jcanephora.AreaInclusive;

/**
 * Parameters for copying framebuffers in postprocessors.
 */

@Immutable public final class KCopyParameters
{
  private final boolean                blit;
  private final @Nonnull AreaInclusive source_select;
  private final @Nonnull AreaInclusive target_select;

  /**
   * Construct new parameters.
   * 
   * @param in_source_select
   *          The inclusive area that will be copied from the source.
   * @param in_target_select
   *          The inclusive area that will contain the data copied on the
   *          target.
   * @param in_blit
   *          If blitting should be used (may be ignored if not supported)
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   */

  public KCopyParameters(
    final @Nonnull AreaInclusive in_source_select,
    final @Nonnull AreaInclusive in_target_select,
    final boolean in_blit)
    throws ConstraintError
  {
    this.source_select =
      Constraints.constrainNotNull(in_source_select, "Source selection");
    this.target_select =
      Constraints.constrainNotNull(in_target_select, "Target selection");
    this.blit = in_blit;
  }

  /**
   * @return The source area
   */

  public @Nonnull AreaInclusive getSourceSelect()
  {
    return this.source_select;
  }

  /**
   * @return The target area
   */

  public @Nonnull AreaInclusive getTargetSelect()
  {
    return this.target_select;
  }

  /**
   * @return <code>true</code> if blitting is requested
   */

  public boolean useBlitting()
  {
    return this.blit;
  }
}
