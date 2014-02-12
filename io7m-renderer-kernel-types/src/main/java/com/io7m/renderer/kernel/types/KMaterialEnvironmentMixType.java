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

/**
 * The factor used to control the amount of environment map mixed into the
 * surface albedo.
 */

public enum KMaterialEnvironmentMixType
{
  /**
   * The mix factor is taken directly from the material mix value.
   */

  ENVIRONMENT_MIX_CONSTANT,

  /**
   * The mix factor is derived from the dot product of the view vector and
   * surface normal, multiplied by the material mix value.
   */

  ENVIRONMENT_MIX_DOT_PRODUCT,

  /**
   * The mix factor is sampled from the specular map, multiplied by the
   * material mix value.
   */

  ENVIRONMENT_MIX_MAPPED
}
