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

import com.io7m.jnull.Nullable;
import com.io7m.renderer.types.RException;

interface SBSceneControllerMaterials extends
  SBSceneChangeListenerRegistration
{
  public void sceneMaterialPut(
    final SBMaterial material);

  public boolean sceneMaterialExists(
    final Integer id);

  public Integer sceneMaterialFreshID();

  public @Nullable SBMaterial sceneMaterialGet(
    final Integer id);

  public void sceneMaterialRemove(
    final Integer id);

  public Collection<SBMaterial> sceneMaterialsGetAll();

  void sceneMaterialPutByDescription(
    final Integer id,
    final SBMaterialDescription desc)
    throws RException;
}
