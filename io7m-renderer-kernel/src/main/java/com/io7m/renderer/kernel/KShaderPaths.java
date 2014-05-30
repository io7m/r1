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

import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.jvvfs.PathVirtual;

/**
 * Paths for determining the location of shaders inside a shader filesystem.
 */

public final class KShaderPaths
{
  private KShaderPaths()
  {
    throw new UnreachableCodeException();
  }

  /**
   * The path to forward rendering opaque+unlit shaders.
   */

  public static final PathVirtual PATH_FORWARD_OPAQUE_UNLIT;

  /**
   * The path to forward rendering opaque+lit shaders.
   */

  public static final PathVirtual PATH_FORWARD_OPAQUE_LIT;

  /**
   * The path to forward rendering translucent+unlit shaders.
   */

  public static final PathVirtual PATH_FORWARD_TRANSLUCENT_UNLIT;

  /**
   * The path to forward rendering translucent+lit shaders.
   */

  public static final PathVirtual PATH_FORWARD_TRANSLUCENT_LIT;

  /**
   * The path to depth shaders.
   */

  public static final PathVirtual PATH_DEPTH;

  /**
   * The path to depth variance shaders.
   */

  public static final PathVirtual PATH_DEPTH_VARIANCE;

  /**
   * The path to postprocessing shaders.
   */

  public static final PathVirtual PATH_POSTPROCESSING;

  static {
    try {
      PATH_FORWARD_OPAQUE_UNLIT =
        PathVirtual.ofString("/shaders/forward/opaque/unlit");
      PATH_FORWARD_OPAQUE_LIT =
        PathVirtual.ofString("/shaders/forward/opaque/lit");

      PATH_FORWARD_TRANSLUCENT_UNLIT =
        PathVirtual.ofString("/shaders/forward/translucent/unlit");
      PATH_FORWARD_TRANSLUCENT_LIT =
        PathVirtual.ofString("/shaders/forward/translucent/lit");

      PATH_DEPTH = PathVirtual.ofString("/shaders/depth");
      PATH_DEPTH_VARIANCE = PathVirtual.ofString("/shaders/depth_variance");

      PATH_POSTPROCESSING = PathVirtual.ofString("/shaders/postprocessing");
    } catch (final FilesystemError e) {
      throw new UnreachableCodeException(e);
    }
  }
}
