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

package com.io7m.r1.meshes;

import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jnull.NullCheck;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.types.RExceptionMeshNameInvalid;

/**
 * Functions to check the validity of mesh names.
 */

@EqualityReference public final class RMeshNames
{
  /**
   * The status of a mesh name.
   */

  public static enum Status
  {
    /**
     * The mesh name invalid due to being empty.
     */

    MESH_NAME_EMPTY,

    /**
     * The mesh name is valid.
     */

    MESH_NAME_OK,

    /**
     * The mesh name invalid due to being too long.
     */

    MESH_NAME_TOO_LONG
  }

  /**
   * Examine a potential mesh name and throw an exception if the name is not
   * valid.
   *
   * @param name
   *          The name.
   * @return The name.
   * @throws RExceptionMeshNameInvalid
   *           If the name is not valid.
   */

  public static String checkMeshName(
    final String name)
    throws RExceptionMeshNameInvalid
  {
    NullCheck.notNull(name, "Name");

    final Status status = RMeshNames.examineMeshName(name);
    switch (status) {
      case MESH_NAME_EMPTY:
      {
        throw new RExceptionMeshNameInvalid("Name is empty");
      }
      case MESH_NAME_OK:
      {
        return name;
      }
      case MESH_NAME_TOO_LONG:
      {
        throw new RExceptionMeshNameInvalid(
          "Name is too long (must be fewer than 65536 bytes when encoded to UTF-8)");
      }
    }

    throw new UnreachableCodeException();
  }

  /**
   * Examine a potential mesh name and return a value indicating whether or
   * not it would be valid.
   *
   * @param name
   *          The name.
   * @return A status value.
   */

  public static Status examineMeshName(
    final String name)
  {
    NullCheck.notNull(name, "Name");

    if (name.isEmpty()) {
      return Status.MESH_NAME_EMPTY;
    }

    if (name.getBytes().length >= 65536) {
      return Status.MESH_NAME_TOO_LONG;
    }

    return Status.MESH_NAME_OK;
  }

  private RMeshNames()
  {
    throw new UnreachableCodeException();
  }
}
