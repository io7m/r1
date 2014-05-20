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

package com.io7m.renderer.kernel.types;

import com.io7m.jcanephora.JCGLException;
import com.io7m.renderer.types.RException;

/**
 * A generic mesh-with-material visitor, returning values of type
 * <code>A</code> and raising exceptions of type <code>E</code>.
 * 
 * @param <A>
 *          The return value type of the implementing visitor
 * @param <E>
 *          The type of exceptions raised by the implementing visitor
 */

public interface KMeshWithMaterialVisitorType<A, E extends Throwable>
{
  /**
   * Visit a mesh with an opaque alpha-to-depth material.
   * 
   * @param i
   *          The instance
   * @return A value of type <code>A</code>
   * 
   * @throws RException
   *           If required
   * @throws JCGLException
   *           If required
   * @throws E
   *           If required
   */

  A meshWithMaterialOpaqueAlphaDepth(
    final KMeshWithMaterialOpaqueAlphaDepth i)
    throws E,
      RException,
      JCGLException;

  /**
   * Visit a mesh with a regular opaque material.
   * 
   * @param i
   *          The instance
   * @return A value of type <code>A</code>
   * 
   * @throws RException
   *           If required
   * @throws JCGLException
   *           If required
   * @throws E
   *           If required
   */

  A meshWithMaterialOpaqueRegular(
    final KMeshWithMaterialOpaqueRegular i)
    throws E,
      RException,
      JCGLException;

  /**
   * Visit a mesh with a refractive material.
   * 
   * @param i
   *          The instance
   * @return A value of type <code>A</code>
   * 
   * @throws RException
   *           If required
   * @throws JCGLException
   *           If required
   * @throws E
   *           If required
   */

  A meshWithMaterialTranslucentRefractive(
    final KMeshWithMaterialTranslucentRefractive i)
    throws E,
      RException,
      JCGLException;

  /**
   * Visit a mesh with a regular translucent material.
   * 
   * @param i
   *          The instance
   * @return A value of type <code>A</code>
   * 
   * @throws RException
   *           If required
   * @throws JCGLException
   *           If required
   * @throws E
   *           If required
   */

  A meshWithMaterialTranslucentRegular(
    final KMeshWithMaterialTranslucentRegular i)
    throws E,
      RException,
      JCGLException;

  /**
   * Visit a mesh with a specular-only material.
   * 
   * @param i
   *          The instance
   * @return A value of type <code>A</code>
   * 
   * @throws RException
   *           If required
   * @throws JCGLException
   *           If required
   * @throws E
   *           If required
   */

  A meshWithMaterialTranslucentSpecularOnly(
    final KMeshWithMaterialTranslucentSpecularOnly i)
    throws E,
      RException,
      JCGLException;
}
