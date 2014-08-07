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

package com.io7m.r1.examples;

import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jcanephora.TextureCubeStaticUsableType;
import com.io7m.r1.kernel.types.KMeshReadableType;
import com.io7m.r1.kernel.types.KSceneBuilderType;
import com.io7m.r1.types.RException;

/**
 * The type of example scene builders.
 */

public interface ExampleSceneBuilderType extends KSceneBuilderType
{
  /**
   * Load the cube texture with the given name.
   *
   * @param name
   *          The name
   * @return An allocated texture
   * @throws RException
   *           If an error occurs
   */

  TextureCubeStaticUsableType cubeTexture(
    final String name)
    throws RException;

  /**
   * Load the mesh with the given name.
   *
   * @param name
   *          The name
   * @return An allocated mesh
   * @throws RException
   *           If an error occurs
   */

  KMeshReadableType mesh(
    final String name)
    throws RException;

  /**
   * Load the texture with the given name.
   *
   * @param name
   *          The name
   * @return An allocated texture
   * @throws RException
   *           If an error occurs
   */

  Texture2DStaticUsableType texture(
    final String name)
    throws RException;

  /**
   * Load the texture with the given name, with the wrapping mode set to
   * {@link com.io7m.jcanephora.TextureWrapS#TEXTURE_WRAP_CLAMP_TO_EDGE}.
   *
   * @param name
   *          The name
   * @return An allocated texture
   * @throws RException
   *           If an error occurs
   */

  Texture2DStaticUsableType textureClamped(
    final String name)
    throws RException;
}
