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

package com.io7m.r1.examples.tools;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.Texture2DStaticType;
import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jcanephora.TextureLoaderType;
import com.io7m.jcanephora.TextureWrapS;
import com.io7m.jcanephora.TextureWrapT;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;

/**
 * A trivial 2D texture cache.
 */

public final class ETexture2DCache
{
  private final JCGLImplementationType                 gi;
  private final LogUsableType                          glog;
  private final TextureLoaderType                      texture_loader;
  private final Map<String, Texture2DStaticUsableType> textures;

  /**
   * Initialize the cache.
   *
   * @param in_gi
   *          A GL interface.
   * @param in_texture_loader
   *          A texture loader.
   * @param in_log
   *          A log interface.
   */

  public ETexture2DCache(
    final JCGLImplementationType in_gi,
    final TextureLoaderType in_texture_loader,
    final LogUsableType in_log)
  {
    this.gi = NullCheck.notNull(in_gi, "GL");
    this.glog = NullCheck.notNull(in_log, "Log").with("cube-cache");
    this.texture_loader =
      NullCheck.notNull(in_texture_loader, "Texture loader");
    this.textures = new HashMap<String, Texture2DStaticUsableType>();
  }

  private Texture2DStaticUsableType loadActual(
    final String name,
    final TextureWrapS wrap_s,
    final TextureWrapT wrap_t)
    throws IOException,
      JCGLException
  {
    final StringBuilder message = new StringBuilder();
    message.setLength(0);
    message.append("Loading texture from ");
    message.append(name);
    this.glog.debug(message.toString());

    final String file = String.format("/com/io7m/r1/examples/%s", name);
    final InputStream stream =
      ETexture2DCache.class.getResourceAsStream(file);

    if (stream == null) {
      throw new FileNotFoundException(String.format(
        "Unable to open texture: %s",
        file));
    }

    try {
      final TextureLoaderType tl = this.texture_loader;
      assert tl != null;

      final JCGLImplementationType g = this.gi;
      assert g != null;

      final Texture2DStaticType t =
        tl.load2DStaticInferred(
          g,
          wrap_s,
          wrap_t,
          TextureFilterMinification.TEXTURE_FILTER_LINEAR,
          TextureFilterMagnification.TEXTURE_FILTER_LINEAR,
          stream,
          name);
      this.textures.put(name, t);
      return t;
    } finally {
      stream.close();
    }
  }

  /**
   * Load a cube texture with the given name.
   *
   * @param name
   *          The name.
   * @return A cube texture.
   * @throws IOException
   *           On I/O errors.
   * @throws JCGLException
   *           On GL errors.
   */

  public Texture2DStaticUsableType loadTexture(
    final String name)
    throws IOException,
      JCGLException
  {
    if (this.textures.containsKey(name)) {
      return this.textures.get(name);
    }

    final TextureWrapS wrap_s = TextureWrapS.TEXTURE_WRAP_REPEAT;
    final TextureWrapT wrap_t = TextureWrapT.TEXTURE_WRAP_REPEAT;

    return this.loadActual(name, wrap_s, wrap_t);
  }

  /**
   * Load a cube texture with the given name, with the wrapping modes set to
   * clamp to edge.
   *
   * @param name
   *          The name.
   * @return A cube texture.
   * @throws IOException
   *           On I/O errors.
   * @throws JCGLException
   *           On GL errors.
   */

  public Texture2DStaticUsableType loadTextureClamped(
    final String name)
    throws IOException,
      JCGLException
  {
    if (this.textures.containsKey(name)) {
      return this.textures.get(name);
    }

    final TextureWrapS wrap_s = TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE;
    final TextureWrapT wrap_t = TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE;

    return this.loadActual(name, wrap_s, wrap_t);
  }
}
