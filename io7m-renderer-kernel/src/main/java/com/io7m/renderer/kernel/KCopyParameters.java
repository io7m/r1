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

import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;

/**
 * Parameters for copying framebuffers in postprocessors.
 */

@EqualityStructural public final class KCopyParameters
{
  private final boolean       blit;
  private final AreaInclusive source_select;
  private final AreaInclusive target_select;

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
   */

  public KCopyParameters(
    final AreaInclusive in_source_select,
    final AreaInclusive in_target_select,
    final boolean in_blit)
  {
    this.source_select =
      NullCheck.notNull(in_source_select, "Source selection");
    this.target_select =
      NullCheck.notNull(in_target_select, "Target selection");
    this.blit = in_blit;
  }

  @Override public boolean equals(
    final @Nullable Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final KCopyParameters other = (KCopyParameters) obj;
    return (this.blit == other.blit)
      && (this.source_select.equals(other.source_select))
      && (this.target_select.equals(other.target_select));
  }

  /**
   * @return The source area
   */

  public AreaInclusive getSourceSelect()
  {
    return this.source_select;
  }

  /**
   * @return The target area
   */

  public AreaInclusive getTargetSelect()
  {
    return this.target_select;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + (this.blit ? 1231 : 1237);
    result = (prime * result) + this.source_select.hashCode();
    result = (prime * result) + this.target_select.hashCode();
    return result;
  }

  /**
   * @return <code>true</code> if blitting is requested
   */

  public boolean useBlitting()
  {
    return this.blit;
  }
}
