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

import java.util.concurrent.Future;

import javax.annotation.Nonnull;

import com.io7m.renderer.types.RMatrixI4x4F;
import com.io7m.renderer.types.RTransformProjection;

interface SBSceneControllerRendererControl
{
  public @Nonnull Future<SBCacheStatistics> rendererGetCacheStatistics();

  public @Nonnull RMatrixI4x4F<RTransformProjection> rendererGetProjection();

  public void rendererSetBackgroundColour(
    float r,
    float g,
    float b);

  public void rendererSetCustomProjection(
    final @Nonnull RMatrixI4x4F<RTransformProjection> p);

  public void rendererSetType(
    final @Nonnull SBKRendererType type);

  public void rendererPostprocessorSetType(
    final @Nonnull SBKPostprocessorType type);

  public void rendererShowAxes(
    final boolean enabled);

  public void rendererShowGrid(
    final boolean enabled);

  public void rendererShowLightRadii(
    final boolean enabled);

  public void rendererShowLights(
    final boolean enabled);

  public void rendererUnsetCustomProjection();
}
