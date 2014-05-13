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

import com.io7m.jnull.NullCheck;
import com.io7m.jvvfs.PathVirtual;
import com.io7m.renderer.kernel.types.KMesh;

final class SBMesh implements Comparable<SBMesh>
{
  private final PathVirtual path;
  private final KMesh       mesh;

  SBMesh(
    final PathVirtual in_path,
    final KMesh in_mesh)
  {
    NullCheck.notNull(in_path, "Path");
    NullCheck.notNull(in_mesh, "Mesh");

    this.path = in_path;
    this.mesh = in_mesh;
  }

  @Override public int compareTo(
    final SBMesh o)
  {
    return this.path.compareTo(this.path);
  }

  public KMesh getMesh()
  {
    return this.mesh;
  }

  public PathVirtual getPath()
  {
    return this.path;
  }
}
