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

package com.io7m.renderer.xml.rmx;

import java.net.URI;
import java.net.URISyntaxException;

import javax.annotation.Nonnull;

import com.io7m.jaux.UnreachableCodeException;

public final class RXMLConstants
{
  public static final @Nonnull URI MESHES_URI;
  public static final int          MESHES_VERSION;

  static {
    try {
      MESHES_URI = new URI("http://schemas.io7m.com/renderer/1.0.0/meshes");
      MESHES_VERSION = 1;
    } catch (final URISyntaxException e) {
      throw new UnreachableCodeException();
    }
  }
}
