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

package com.io7m.r1.documentation.examples;

import com.io7m.jfunctional.Unit;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.kernel.types.KMaterialAlbedoTextured;
import com.io7m.r1.kernel.types.KMaterialAlbedoPropertiesType;
import com.io7m.r1.kernel.types.KMaterialAlbedoUntextured;
import com.io7m.r1.kernel.types.KMaterialAlbedoVisitorType;
import com.io7m.r1.types.RException;

/**
 * An example of pattern matching on materials.
 */

public final class Match0
{
  private Match0()
  {
    throw new UnreachableCodeException();
  }

  public static void whichAlbedoType(
    final KMaterialAlbedoType m)
    throws RException
  {
    m
      .albedoAccept(new KMaterialAlbedoVisitorType<Unit, UnreachableCodeException>() {
        @Override public Unit textured(
          final KMaterialAlbedoTextured mt)
        {
          System.out.println("Textured");
          return Unit.unit();
        }

        @Override public Unit untextured(
          final KMaterialAlbedoUntextured mu)
        {
          System.out.println("Untextured");
          return Unit.unit();
        }
      });
  }
}
