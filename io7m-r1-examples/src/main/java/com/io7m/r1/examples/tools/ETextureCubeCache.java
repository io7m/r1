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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
import com.io7m.jproperties.JPropertyException;
import com.io7m.r1.examples.ECubeMap;

/**
 * A trivial texture cube cache.
 */

public final class ETextureCubeCache
{
  private static TextureCubeStaticType loadCubeActual(
    final String name,
    final TextureWrapR wrap_r,
    final TextureWrapS wrap_s,
    final TextureWrapT wrap_t,
    final CubeMapFaceInputStream<CMFPositiveZKind> positive_z,
    final CubeMapFaceInputStream<CMFPositiveYKind> positive_y,
    final CubeMapFaceInputStream<CMFPositiveXKind> positive_x,
    final CubeMapFaceInputStream<CMFNegativeZKind> negative_z,
    final CubeMapFaceInputStream<CMFNegativeYKind> negative_y,
    final CubeMapFaceInputStream<CMFNegativeXKind> negative_x,
    final TextureLoaderType tl,
    final JCGLImplementationType g)
    throws IOException,
      JCGLException
  {
    return tl.loadCubeRHStaticInferred(
      g,
      wrap_r,
      wrap_s,
      wrap_t,
      TextureFilterMinification.TEXTURE_FILTER_LINEAR,
      TextureFilterMagnification.TEXTURE_FILTER_LINEAR,
      positive_z,
      negative_z,
      positive_y,
      negative_y,
      positive_x,
      negative_x,
      name);
  }

  @SuppressWarnings("resource") private static
    CubeMapFaceInputStream<CMFNegativeXKind>
    openNegativeX(
      final List<InputStream> streams,
      final String name)
      throws FileNotFoundException
  {
    final InputStream stream =
      ETextureCubeCache.class.getResourceAsStream(name);
    if (stream == null) {
      throw new FileNotFoundException(name);
    }
    final CubeMapFaceInputStream<CMFNegativeXKind> r =
      new CubeMapFaceInputStream<CMFNegativeXKind>(stream);
    streams.add(r);
    return r;
  }

  @SuppressWarnings("resource") private static
    CubeMapFaceInputStream<CMFNegativeYKind>
    openNegativeY(
      final List<InputStream> streams,
      final String name)
      throws FileNotFoundException
  {
    final InputStream stream =
      ETextureCubeCache.class.getResourceAsStream(name);
    if (stream == null) {
      throw new FileNotFoundException(name);
    }
    final CubeMapFaceInputStream<CMFNegativeYKind> r =
      new CubeMapFaceInputStream<CMFNegativeYKind>(stream);
    streams.add(r);
    return r;
  }

  @SuppressWarnings("resource") private static
    CubeMapFaceInputStream<CMFNegativeZKind>
    openNegativeZ(
      final List<InputStream> streams,
      final String name)
      throws FileNotFoundException
  {
    final InputStream stream =
      ETextureCubeCache.class.getResourceAsStream(name);
    if (stream == null) {
      throw new FileNotFoundException(name);
    }
    final CubeMapFaceInputStream<CMFNegativeZKind> r =
      new CubeMapFaceInputStream<CMFNegativeZKind>(stream);
    streams.add(r);
    return r;
  }

  @SuppressWarnings("resource") private static
    CubeMapFaceInputStream<CMFPositiveXKind>
    openPositiveX(
      final List<InputStream> streams,
      final String name)
      throws FileNotFoundException
  {
    final InputStream stream =
      ETextureCubeCache.class.getResourceAsStream(name);
    if (stream == null) {
      throw new FileNotFoundException(name);
    }
    final CubeMapFaceInputStream<CMFPositiveXKind> r =
      new CubeMapFaceInputStream<CMFPositiveXKind>(stream);
    streams.add(r);
    return r;
  }

  @SuppressWarnings("resource") private static
    CubeMapFaceInputStream<CMFPositiveYKind>
    openPositiveY(
      final List<InputStream> streams,
      final String name)
      throws FileNotFoundException
  {
    final InputStream stream =
      ETextureCubeCache.class.getResourceAsStream(name);
    if (stream == null) {
      throw new FileNotFoundException(name);
    }
    final CubeMapFaceInputStream<CMFPositiveYKind> r =
      new CubeMapFaceInputStream<CMFPositiveYKind>(stream);
    streams.add(r);
    return r;
  }

  @SuppressWarnings("resource") private static
    CubeMapFaceInputStream<CMFPositiveZKind>
    openPositiveZ(
      final List<InputStream> streams,
      final String name)
      throws FileNotFoundException
  {
    final InputStream stream =
      ETextureCubeCache.class.getResourceAsStream(name);
    if (stream == null) {
      throw new FileNotFoundException(name);
    }
    final CubeMapFaceInputStream<CMFPositiveZKind> r =
      new CubeMapFaceInputStream<CMFPositiveZKind>(stream);
    streams.add(r);
    return r;
  }

  private final Map<String, TextureCubeStaticUsableType> cube_textures;

  private final JCGLImplementationType                   gi;

  private final LogUsableType                            glog;

  private final TextureLoaderType                        texture_loader;

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
   * @throws JPropertyException
   *           On parse errors.
   */

  public TextureCubeStaticUsableType loadCubeClamped(
    final String name)
    throws IOException,
      JCGLException,
      JPropertyException
  {
    return this.loadCubeWithWrap(
      name,
      TextureWrapR.TEXTURE_WRAP_CLAMP_TO_EDGE,
      TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
      TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE);
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
   * @throws JPropertyException
   *           On parse errors
   */

  public TextureCubeStaticUsableType loadCubeRepeat(
    final String name)
    throws IOException,
      JCGLException,
      JPropertyException
  {
    return this.loadCubeWithWrap(
      name,
      TextureWrapR.TEXTURE_WRAP_REPEAT,
      TextureWrapS.TEXTURE_WRAP_REPEAT,
      TextureWrapT.TEXTURE_WRAP_REPEAT);
  }

  @SuppressWarnings("resource") private
    TextureCubeStaticUsableType
    loadCubeWithWrap(
      final String name,
      final TextureWrapR wrap_r,
      final TextureWrapS wrap_s,
      final TextureWrapT wrap_t)
      throws IOException,
        JCGLException,
        JPropertyException
  {
    /**
     * Store the name along with the wrapping modes in the cache, to allow for
     * textures with the same name but different wrapping modes to be cached
     * correctly.
     */

    final StringBuilder name_with_wrap = new StringBuilder();
    name_with_wrap.append(name);
    name_with_wrap.append(":");
    name_with_wrap.append(wrap_r);
    name_with_wrap.append(":");
    name_with_wrap.append(wrap_s);
    name_with_wrap.append(":");
    name_with_wrap.append(wrap_t);

    final String name_with_wrap_actual = name_with_wrap.toString();
    if (this.cube_textures.containsKey(name_with_wrap_actual)) {
      final TextureCubeStaticUsableType r =
        this.cube_textures.get(name_with_wrap_actual);
      assert r != null;
      return r;
    }

    final StringBuilder message = new StringBuilder();
    message.setLength(0);
    message.append("Loading texture from ");
    message.append(name);
    {
      final String s = message.toString();
      assert s != null;
      this.glog.debug(s);
    }

    final String file = String.format("/com/io7m/r1/examples/%s", name);
    final int last_index = file.lastIndexOf('/');
    assert last_index != -1;
    final String directory = file.substring(0, last_index);

    final List<InputStream> streams = new ArrayList<InputStream>();

    try {
      final InputStream meta_stream =
        ETextureCubeCache.class.getResourceAsStream(file.toString());
      if (meta_stream == null) {
        throw new FileNotFoundException(String.format(
          "Unable to open texture: %s",
          file));
      }
      streams.add(meta_stream);

      final Properties p = new Properties();
      p.load(meta_stream);
      final ECubeMap cube = ECubeMap.fromProperties(p);

      final String positive_z_name =
        String.format("%s/%s", directory, cube.getPositiveZ());
      final String positive_y_name =
        String.format("%s/%s", directory, cube.getPositiveY());
      final String positive_x_name =
        String.format("%s/%s", directory, cube.getPositiveX());
      final String negative_z_name =
        String.format("%s/%s", directory, cube.getNegativeZ());
      final String negative_y_name =
        String.format("%s/%s", directory, cube.getNegativeY());
      final String negative_x_name =
        String.format("%s/%s", directory, cube.getNegativeX());

      assert positive_x_name != null;
      assert positive_y_name != null;
      assert positive_z_name != null;
      assert negative_x_name != null;
      assert negative_y_name != null;
      assert negative_z_name != null;

      final CubeMapFaceInputStream<CMFPositiveZKind> positive_z =
        ETextureCubeCache.openPositiveZ(streams, positive_z_name);
      final CubeMapFaceInputStream<CMFPositiveYKind> positive_y =
        ETextureCubeCache.openPositiveY(streams, positive_y_name);
      final CubeMapFaceInputStream<CMFPositiveXKind> positive_x =
        ETextureCubeCache.openPositiveX(streams, positive_x_name);
      final CubeMapFaceInputStream<CMFNegativeZKind> negative_z =
        ETextureCubeCache.openNegativeZ(streams, negative_z_name);
      final CubeMapFaceInputStream<CMFNegativeYKind> negative_y =
        ETextureCubeCache.openNegativeY(streams, negative_y_name);
      final CubeMapFaceInputStream<CMFNegativeXKind> negative_x =
        ETextureCubeCache.openNegativeX(streams, negative_x_name);

      final TextureLoaderType tl = this.texture_loader;
      assert tl != null;
      final JCGLImplementationType g = this.gi;
      assert g != null;

      final TextureCubeStaticType t =
        ETextureCubeCache.loadCubeActual(
          name,
          TextureWrapR.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          positive_z,
          positive_y,
          positive_x,
          negative_z,
          negative_y,
          negative_x,
          tl,
          g);
      this.cube_textures.put(name_with_wrap_actual, t);

      return t;
    } finally {
      for (final InputStream s : streams) {
        s.close();
      }
    }
  }
}
