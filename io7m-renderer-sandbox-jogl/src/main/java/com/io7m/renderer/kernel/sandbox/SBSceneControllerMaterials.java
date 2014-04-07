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

import java.util.Collection;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.renderer.types.RException;

interface SBSceneControllerMaterials extends
  SBSceneChangeListenerRegistration
{
  public void sceneMaterialPut(
    final @Nonnull SBMaterial material)
    throws ConstraintError;

  public boolean sceneMaterialExists(
    final @Nonnull Integer id)
    throws ConstraintError;

  public @Nonnull Integer sceneMaterialFreshID();

  public @CheckForNull SBMaterial sceneMaterialGet(
    final @Nonnull Integer id)
    throws ConstraintError;

  public void sceneMaterialRemove(
    final @Nonnull Integer id)
    throws ConstraintError;

  public @Nonnull Collection<SBMaterial> sceneMaterialsGetAll();

  void sceneMaterialPutByDescription(
    final @Nonnull Integer id,
    final @Nonnull SBMaterialDescription desc)
    throws ConstraintError,
      RException;
}
