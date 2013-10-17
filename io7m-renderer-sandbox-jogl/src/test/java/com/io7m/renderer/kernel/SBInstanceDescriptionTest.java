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

import javax.annotation.Nonnull;

import net.java.quickcheck.Generator;
import net.java.quickcheck.QuickCheck;
import net.java.quickcheck.characteristic.AbstractCharacteristic;
import net.java.quickcheck.generator.support.IntegerGenerator;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jaux.AlmostEqualFloat;
import com.io7m.jaux.AlmostEqualFloat.ContextRelative;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jtensors.VectorI4F;
import com.io7m.renderer.RSpaceRGBA;
import com.io7m.renderer.RSpaceWorld;

public class SBInstanceDescriptionTest
{
  private static class MaterialGenerator implements
    Generator<SBMaterialDescription>
  {
    private final @Nonnull SBNonEmptyStringGenerator        string_gen;
    private final @Nonnull IntegerGenerator                 int_gen;
    private final @Nonnull SBVectorI4FGenerator<RSpaceRGBA> rgba_gen;
    private final @Nonnull PathVirtualGenerator             path_gen;

    public MaterialGenerator()
    {
      this.string_gen = new SBNonEmptyStringGenerator();
      this.int_gen = new IntegerGenerator();
      this.rgba_gen = new SBVectorI4FGenerator<RSpaceRGBA>();
      this.path_gen = new PathVirtualGenerator();
    }

    @Override public SBMaterialDescription next()
    {
      try {
        final SBMaterialAlbedoDescription sd_diff =
          new SBMaterialAlbedoDescription(
            this.rgba_gen.next(),
            (float) Math.random(),
            this.path_gen.next());

        final SBMaterialSpecularDescription sd_spec =
          new SBMaterialSpecularDescription(
            this.path_gen.next(),
            (float) Math.random(),
            (float) Math.random());

        final SBMaterialEnvironmentDescription sd_envi =
          new SBMaterialEnvironmentDescription(
            this.path_gen.next(),
            (float) Math.random(),
            (float) Math.random(),
            (float) Math.random());

        final SBMaterialNormalDescription sd_norm =
          new SBMaterialNormalDescription(this.path_gen.next());

        final SBMaterialAlphaDescription sd_alph =
          new SBMaterialAlphaDescription(
            (Math.random() % 2) == 0,
            (float) Math.random());

        final SBMaterialEmissiveDescription sd_emis =
          new SBMaterialEmissiveDescription(
            (float) Math.random(),
            this.path_gen.next());

        return new SBMaterialDescription(
          sd_alph,
          sd_diff,
          sd_emis,
          sd_spec,
          sd_envi,
          sd_norm);

      } catch (final ConstraintError x) {
        throw new UnreachableCodeException();
      }
    }
  }

  private static class InstanceGenerator implements
    Generator<SBInstanceDescription>
  {
    private final @Nonnull SBNonEmptyStringGenerator         string_gen;
    private final @Nonnull IntegerGenerator                  int_gen;
    private final @Nonnull SBVectorI3FGenerator<RSpaceWorld> pos_gen;
    private final @Nonnull SBVectorI3FGenerator<SBDegrees>   ori_gen;
    private final @Nonnull MaterialGenerator                 mat_gen;
    private final @Nonnull PathVirtualGenerator              path_gen;

    public InstanceGenerator()
    {
      this.string_gen = new SBNonEmptyStringGenerator();
      this.int_gen = new IntegerGenerator();
      this.pos_gen = new SBVectorI3FGenerator<RSpaceWorld>();
      this.ori_gen = new SBVectorI3FGenerator<SBDegrees>();
      this.mat_gen = new MaterialGenerator();
      this.path_gen = new PathVirtualGenerator();
    }

    @Override public SBInstanceDescription next()
    {
      return new SBInstanceDescription(
        this.int_gen.next(),
        this.pos_gen.next(),
        this.ori_gen.next(),
        this.path_gen.next(),
        this.mat_gen.next());
    }
  }

  @SuppressWarnings("static-method") @Test public
    void
    testSerializationRoundTrip()
  {
    QuickCheck.forAllVerbose(
      new InstanceGenerator(),
      new AbstractCharacteristic<SBInstanceDescription>() {
        @SuppressWarnings("boxing") @Override protected void doSpecify(
          final @Nonnull SBInstanceDescription desc)
          throws Throwable
        {
          final ContextRelative ctx = new ContextRelative();
          ctx.setMaxAbsoluteDifference(0.000001f);

          final SBInstanceDescription desc_r =
            SBInstanceDescription.fromXML(desc.toXML());

          Assert.assertEquals(desc.getID(), desc_r.getID());
          Assert.assertEquals(desc.getOrientation(), desc_r.getOrientation());
          Assert.assertEquals(desc.getPosition(), desc_r.getPosition());
          Assert.assertEquals(desc.getMesh(), desc_r.getMesh());

          final SBMaterialDescription mat = desc.getMaterial();
          final SBMaterialDescription mat_r = desc_r.getMaterial();

          {
            final SBMaterialAlbedoDescription aa = mat.getAlbedo();
            final SBMaterialAlbedoDescription ar = mat_r.getAlbedo();

            Assert.assertTrue(VectorI4F.almostEqual(
              ctx,
              aa.getColour(),
              ar.getColour()));
            Assert.assertTrue(AlmostEqualFloat.almostEqual(
              ctx,
              aa.getMix(),
              ar.getMix()));
            Assert.assertEquals(aa.getTexture(), ar.getTexture());
          }

          {
            final SBMaterialAlphaDescription aa = mat.getAlpha();
            final SBMaterialAlphaDescription ar = mat_r.getAlpha();

            Assert.assertEquals(aa.isTranslucent(), ar.isTranslucent());
            Assert.assertTrue(AlmostEqualFloat.almostEqual(
              ctx,
              aa.getOpacity(),
              ar.getOpacity()));
          }

          {
            final SBMaterialEmissiveDescription me = mat.getEmissive();
            final SBMaterialEmissiveDescription mr = mat_r.getEmissive();

            Assert.assertTrue(AlmostEqualFloat.almostEqual(
              ctx,
              me.getEmission(),
              mr.getEmission()));
            Assert.assertEquals(me.getTexture(), mr.getTexture());
          }

          {
            final SBMaterialEnvironmentDescription ee = mat.getEnvironment();
            final SBMaterialEnvironmentDescription er =
              mat_r.getEnvironment();

            Assert.assertTrue(AlmostEqualFloat.almostEqual(
              ctx,
              ee.getMix(),
              er.getMix()));
            Assert.assertTrue(AlmostEqualFloat.almostEqual(
              ctx,
              ee.getReflectionMix(),
              er.getReflectionMix()));
            Assert.assertTrue(AlmostEqualFloat.almostEqual(
              ctx,
              ee.getRefractionIndex(),
              er.getRefractionIndex()));
            Assert.assertEquals(ee.getTexture(), er.getTexture());
          }

          {
            final SBMaterialNormalDescription ne = mat.getNormal();
            final SBMaterialNormalDescription nr = mat_r.getNormal();

            Assert.assertEquals(ne.getTexture(), nr.getTexture());
          }

          {
            final SBMaterialSpecularDescription se = mat.getSpecular();
            final SBMaterialSpecularDescription sr = mat_r.getSpecular();

            Assert.assertTrue(AlmostEqualFloat.almostEqual(
              ctx,
              se.getExponent(),
              sr.getExponent()));
            Assert.assertTrue(AlmostEqualFloat.almostEqual(
              ctx,
              se.getIntensity(),
              sr.getIntensity()));
            Assert.assertEquals(se.getTexture(), sr.getTexture());
          }
        }
      });
  }
}
