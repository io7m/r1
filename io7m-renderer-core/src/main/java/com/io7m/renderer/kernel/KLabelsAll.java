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

package com.io7m.renderer.kernel;

import java.util.TreeSet;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.renderer.kernel.KLight.Type;

final class KLabelsAll
{
  public static void main(
    final String args[])
    throws ConstraintError
  {
    final TreeSet<String> shaders = new TreeSet<String>();
    final StringBuilder buf = new StringBuilder();

    for (final KMaterialAlbedoLabel b : KMaterialAlbedoLabel.values()) {
      for (final KMaterialAlphaLabel a : KMaterialAlphaLabel.values()) {
        for (final KMaterialEmissiveLabel m : KMaterialEmissiveLabel.values()) {
          buf.setLength(0);
          buf.append("U_");
          buf.append(a.code);
          buf.append("_");
          buf.append(b.code);
          switch (m) {
            case EMISSIVE_CONSTANT:
            case EMISSIVE_MAPPED:
            {
              buf.append("_");
              buf.append(m.code);
              break;
            }
            case EMISSIVE_NONE:
            {
              break;
            }
          }
          shaders.add(buf.toString());
        }
      }
    }

    for (final KMaterialAlbedoLabel b : KMaterialAlbedoLabel.values()) {
      for (final KMaterialAlphaLabel a : KMaterialAlphaLabel.values()) {
        for (final KMaterialEmissiveLabel m : KMaterialEmissiveLabel.values()) {
          for (final KMaterialEnvironmentLabel e : KMaterialEnvironmentLabel
            .values()) {
            for (final KMaterialNormalLabel n : KMaterialNormalLabel.values()) {
              for (final KMaterialSpecularLabel s : KMaterialSpecularLabel
                .values()) {
                final KMeshInstanceForwardMaterialLabel label =
                  new KMeshInstanceForwardMaterialLabel(a, b, m, e, n, s);

                for (final Type l : KLight.Type.values()) {
                  buf.setLength(0);
                  buf.append(l.getCode());
                  buf.append("_");
                  buf.append(label.getCode());
                  shaders.add(buf.toString());
                }
              }
            }
          }
        }
      }
    }

    for (final String s : shaders) {
      System.out.println(s);
    }

    System.err.println(shaders.size() + " shaders");
  }
}
