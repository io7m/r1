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

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * The set of {@link KMeshInstance} objects and {@link KLight} objects that
 * overlap the view frustum of the given {@link KCamera}.
 */

@Immutable final class KScene
{
  private final @Nonnull KCamera                   camera;
  private final @Nonnull Collection<KLight>        lights;
  private final @Nonnull Collection<KMeshInstance> meshes;

  KScene(
    final @Nonnull KCamera camera,
    final @Nonnull Collection<KLight> first,
    final @Nonnull Collection<KMeshInstance> second)
  {
    this.camera = camera;
    this.lights = first;
    this.meshes = second;
  }

  @Nonnull KCamera getCamera()
  {
    return this.camera;
  }

  @Nonnull Collection<KLight> getLights()
  {
    return this.lights;
  }

  @Nonnull Collection<KMeshInstance> getMeshes()
  {
    return this.meshes;
  }
}
