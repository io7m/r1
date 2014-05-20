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

import java.util.HashSet;
import java.util.Set;

import nu.xom.IllegalAddException;

import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.junreachable.UnreachableCodeException;

/**
 * Labels for forward-rendering refractive translucent objects.
 */

@EqualityStructural public final class KMaterialForwardTranslucentRefractiveLabel implements
  KTexturesRequiredType,
  KMaterialLabelImpliesUVType,
  KLabelType
{
  /**
   * @return The set of all possible translucent refractive labels.
   */

  public static Set<KMaterialForwardTranslucentRefractiveLabel> allLabels()
  {
    final Set<KMaterialForwardTranslucentRefractiveLabel> s =
      new HashSet<KMaterialForwardTranslucentRefractiveLabel>();

    for (final KMaterialRefractiveLabel r : KMaterialRefractiveLabel.values()) {
      assert r != null;

      for (final KMaterialNormalLabel n : KMaterialNormalLabel.values()) {
        assert n != null;

        switch (n) {
          case NORMAL_NONE:
            break;
          case NORMAL_MAPPED:
          case NORMAL_VERTEX:
            s.add(KMaterialForwardTranslucentRefractiveLabel.newLabel(r, n));
            break;
        }
      }
    }

    return s;
  }

  private static String makeLabelCode(
    final KMaterialRefractiveLabel refractive,
    final KMaterialNormalLabel normal)
  {
    switch (normal) {
      case NORMAL_NONE:
      {
        throw new UnreachableCodeException();
      }
      case NORMAL_MAPPED:
      case NORMAL_VERTEX:
      {
        final String r =
          String.format(
            "fwd_U_%s_%s",
            refractive.labelGetCode(),
            normal.labelGetCode());
        assert r != null;
        return r;
      }
    }

    throw new UnreachableCodeException();
  }

  /**
   * Create a new refractive forward-rendering label.
   * 
   * @param in_refractive
   *          The refractive properties
   * @param in_normal
   *          The normal label
   * @return A new label
   */

  public static KMaterialForwardTranslucentRefractiveLabel newLabel(
    final KMaterialRefractiveLabel in_refractive,
    final KMaterialNormalLabel in_normal)
  {
    return new KMaterialForwardTranslucentRefractiveLabel(
      in_refractive,
      in_normal);
  }

  private final String                   code;
  private final KMaterialNormalLabel     normal;
  private final KMaterialRefractiveLabel refractive;

  private KMaterialForwardTranslucentRefractiveLabel(
    final KMaterialRefractiveLabel in_refractive,
    final KMaterialNormalLabel in_normal)
  {
    this.refractive = NullCheck.notNull(in_refractive, "Refractive");
    this.normal = NullCheck.notNull(in_normal, "Normal");

    if (this.normal == KMaterialNormalLabel.NORMAL_NONE) {
      throw new IllegalAddException("No normal vectors provided");
    }

    this.code =
      KMaterialForwardTranslucentRefractiveLabel.makeLabelCode(
        in_refractive,
        in_normal);
  }

  @Override public boolean equals(
    final @Nullable Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final KMaterialForwardTranslucentRefractiveLabel other =
      (KMaterialForwardTranslucentRefractiveLabel) obj;
    return this.code.equals(other.code)
      && (this.normal == other.normal)
      && (this.refractive == other.refractive);
  }

  /**
   * @return The refractive label
   */

  public KMaterialRefractiveLabel getRefractive()
  {
    return this.refractive;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.code.hashCode();
    result = (prime * result) + this.normal.hashCode();
    result = (prime * result) + this.refractive.hashCode();
    return result;
  }

  @Override public String labelGetCode()
  {
    return this.code;
  }

  /**
   * @return The normal-mapping label
   */

  public KMaterialNormalLabel labelGetNormal()
  {
    return this.normal;
  }

  @Override public boolean labelImpliesUV()
  {
    return this.normal == KMaterialNormalLabel.NORMAL_MAPPED;
  }

  @Override public int texturesGetRequired()
  {
    /**
     * At most one texture for the normal map, and one for the scene that will
     * be refracted.
     */
    return this.normal.texturesGetRequired() + 1;
  }
}
