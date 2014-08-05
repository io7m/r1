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

package com.io7m.renderer.tests.kernel.types;

import net.java.quickcheck.Generator;
import net.java.quickcheck.generator.support.StringGenerator;

import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.kernel.types.KLightType;
import com.io7m.renderer.kernel.types.KProjectionType;
import com.io7m.renderer.kernel.types.KShadowType;
import com.io7m.renderer.tests.QuaternionI4FGenerator;
import com.io7m.renderer.tests.RFakeTextures2DStatic;
import com.io7m.renderer.tests.types.RVectorI3FGenerator;
import com.io7m.renderer.types.RSpaceRGBType;
import com.io7m.renderer.types.RSpaceWorldType;
import com.io7m.renderer.types.RVectorI3F;

public final class KLightGenerator implements Generator<KLightType>
{
  private final KLightDirectionalGenerator dir_gen;
  private final KLightSphereGenerator      sph_gen;
  private final KLightProjectiveGenerator  pro_gen;

  public KLightGenerator(
    final JCGLImplementationType g)
  {
    final Generator<RVectorI3F<RSpaceRGBType>> in_colour_gen =
      new RVectorI3FGenerator<RSpaceRGBType>();
    final Generator<RVectorI3F<RSpaceWorldType>> in_direction_gen =
      new RVectorI3FGenerator<RSpaceWorldType>();
    final Generator<RVectorI3F<RSpaceWorldType>> in_position_gen =
      new RVectorI3FGenerator<RSpaceWorldType>();
    final Generator<QuaternionI4F> in_quat_gen = new QuaternionI4FGenerator();
    final Generator<KProjectionType> in_proj_gen = new KProjectionGenerator();
    final Generator<String> name_gen = new StringGenerator();
    final Generator<Texture2DStaticUsableType> in_tex_gen =
      RFakeTextures2DStatic.generator(g, name_gen);
    final Generator<KShadowType> in_shad_gen = new KShadowGenerator();

    this.dir_gen =
      new KLightDirectionalGenerator(in_colour_gen, in_direction_gen);
    this.sph_gen = new KLightSphereGenerator(in_colour_gen, in_position_gen);
    this.pro_gen =
      new KLightProjectiveGenerator(
        in_colour_gen,
        in_position_gen,
        in_quat_gen,
        in_proj_gen,
        in_tex_gen,
        in_shad_gen);
  }

  @Override public KLightType next()
  {
    final int r = (int) (Math.random() * 3);
    switch (r) {
      case 0:
      {
        return this.dir_gen.next();
      }
      case 1:
      {
        return this.sph_gen.next();
      }
      case 2:
      {
        return this.pro_gen.next();
      }
    }

    throw new UnreachableCodeException();
  }

}
