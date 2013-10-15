/*
 * Copyright © 2013 <code@io7m.com> http://io7m.com
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;

@Immutable final class KBatches
{
  private final @Nonnull ArrayList<KBatchUnlit> batches_opaque_unlit;
  private final @Nonnull ArrayList<KBatchLit>   batches_opaque_lit;
  private final @Nonnull ArrayList<KBatchUnlit> batches_translucent_unlit;
  private final @Nonnull ArrayList<KBatchLit>   batches_translucent_lit;

  KBatches(
    final @Nonnull ArrayList<KBatchUnlit> opaque_unlit,
    final @Nonnull ArrayList<KBatchLit> opaque_lit,
    final @Nonnull ArrayList<KBatchUnlit> translucent_unlit,
    final @Nonnull ArrayList<KBatchLit> translucent_lit)
    throws ConstraintError
  {
    this.batches_opaque_unlit =
      Constraints.constrainNotNull(opaque_unlit, "Opaque unlit");
    this.batches_opaque_lit =
      Constraints.constrainNotNull(opaque_lit, "Opaque lit");
    this.batches_translucent_unlit =
      Constraints.constrainNotNull(translucent_unlit, "Translucent unlit");
    this.batches_translucent_lit =
      Constraints.constrainNotNull(translucent_lit, "Translucent lit");
  }

  @Nonnull List<KBatchLit> getBatchesOpaqueLit()
  {
    return Collections.unmodifiableList(this.batches_opaque_lit);
  }

  @Nonnull List<KBatchUnlit> getBatchesOpaqueUnlit()
  {
    return Collections.unmodifiableList(this.batches_opaque_unlit);
  }

  @Nonnull List<KBatchLit> getBatchesTranslucentLit()
  {
    return Collections.unmodifiableList(this.batches_translucent_lit);
  }

  @Nonnull List<KBatchUnlit> getBatchesTranslucentUnlit()
  {
    return Collections.unmodifiableList(this.batches_translucent_unlit);
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[KBatches ");
    builder.append(this.batches_opaque_unlit);
    builder.append(" ");
    builder.append(this.batches_opaque_lit);
    builder.append(" ");
    builder.append(this.batches_translucent_unlit);
    builder.append(" ");
    builder.append(this.batches_translucent_lit);
    builder.append("]");
    return builder.toString();
  }

}
