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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import com.io7m.jcanephora.CMFNegativeXKind;
import com.io7m.jcanephora.CMFNegativeYKind;
import com.io7m.jcanephora.CMFNegativeZKind;
import com.io7m.jcanephora.CMFPositiveXKind;
import com.io7m.jcanephora.CMFPositiveYKind;
import com.io7m.jcanephora.CMFPositiveZKind;
import com.io7m.jcanephora.CubeMapFaceInputStream;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.TextureCubeStaticType;
import com.io7m.jcanephora.TextureCubeStaticUsableType;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jcanephora.TextureLoaderType;
import com.io7m.jcanephora.TextureWrapR;
import com.io7m.jcanephora.TextureWrapS;
import com.io7m.jcanephora.TextureWrapT;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.r1.types.RXMLException;
import com.io7m.r1.xml.cubemap.CubeMap;

/**
 * A trivial texture cube cache.
 */

public final class ETextureCubeCache
{
  private final Map<String, TextureCubeStaticUsableType> cube_textures;
  private final LogUsableType                            glog;
  private final TextureLoaderType                        texture_loader;
  private final JCGLImplementationType                   gi;

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

  public ETextureCubeCache(
    final JCGLImplementationType in_gi,
    final TextureLoaderType in_texture_loader,
    final LogUsableType in_log)
  {
    this.gi = NullCheck.notNull(in_gi, "GL");
    this.glog = NullCheck.notNull(in_log, "Log").with("cube-cache");
    this.texture_loader =
      NullCheck.notNull(in_texture_loader, "Texture loader");
    this.cube_textures = new HashMap<String, TextureCubeStaticUsableType>();
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
   * @throws ValidityException
   *           On XML errors.
   * @throws ParsingException
   *           On XML errors.
   * @throws RXMLException
   *           On XML errors.
   */

  public TextureCubeStaticUsableType loadCube(
    final String name)
    throws IOException,
      JCGLException,
      ValidityException,
      ParsingException,
      RXMLException
  {
    if (this.cube_textures.containsKey(name)) {
      return this.cube_textures.get(name);
    }

    final StringBuilder message = new StringBuilder();
    message.setLength(0);
    message.append("Loading texture from ");
    message.append(name);
    this.glog.debug(message.toString());

    final File file =
      new File(String.format("/com/io7m/r1/examples/%s", name));
    final InputStream stream =
      ETextureCubeCache.class.getResourceAsStream(file.toString());

    try {
      final Builder builder = new Builder();
      final Document document = builder.build(stream);
      final CubeMap cube = CubeMap.fromXML(document.getRootElement());

      final File parent = file.getParentFile();

      final CubeMapFaceInputStream<CMFPositiveZKind> positive_z =
        new CubeMapFaceInputStream<CMFPositiveZKind>(
          ETextureCubeCache.class.getResourceAsStream(new File(parent, cube
            .getPositiveZ()).toString()));
      final CubeMapFaceInputStream<CMFPositiveYKind> positive_y =
        new CubeMapFaceInputStream<CMFPositiveYKind>(
          ETextureCubeCache.class.getResourceAsStream(new File(parent, cube
            .getPositiveY()).toString()));
      final CubeMapFaceInputStream<CMFPositiveXKind> positive_x =
        new CubeMapFaceInputStream<CMFPositiveXKind>(
          ETextureCubeCache.class.getResourceAsStream(new File(parent, cube
            .getPositiveX()).toString()));
      final CubeMapFaceInputStream<CMFNegativeZKind> negative_z =
        new CubeMapFaceInputStream<CMFNegativeZKind>(
          ETextureCubeCache.class.getResourceAsStream(new File(parent, cube
            .getNegativeZ()).toString()));
      final CubeMapFaceInputStream<CMFNegativeYKind> negative_y =
        new CubeMapFaceInputStream<CMFNegativeYKind>(
          ETextureCubeCache.class.getResourceAsStream(new File(parent, cube
            .getNegativeY()).toString()));
      final CubeMapFaceInputStream<CMFNegativeXKind> negative_x =
        new CubeMapFaceInputStream<CMFNegativeXKind>(
          ETextureCubeCache.class.getResourceAsStream(new File(parent, cube
            .getNegativeX()).toString()));

      final TextureLoaderType tl = this.texture_loader;
      assert tl != null;
      final JCGLImplementationType g = this.gi;
      assert g != null;

      final TextureCubeStaticType t =
        tl.loadCubeRHStaticInferred(
          g,
          TextureWrapR.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureFilterMinification.TEXTURE_FILTER_LINEAR,
          TextureFilterMagnification.TEXTURE_FILTER_LINEAR,
          positive_z,
          negative_z,
          positive_y,
          negative_y,
          positive_x,
          negative_x,
          name);
      this.cube_textures.put(name, t);
      return t;
    } finally {
      stream.close();
    }
  }
}
