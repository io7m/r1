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

package com.io7m.r1.shaders.forward.tests;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.reflections.Reflections;

import com.io7m.r1.kernel.types.KMaterialAlbedoType;
import com.io7m.r1.kernel.types.KMaterialDepthType;
import com.io7m.r1.kernel.types.KMaterialEnvironmentType;
import com.io7m.r1.kernel.types.KMaterialNormalType;
import com.io7m.r1.kernel.types.KMaterialRefractiveType;
import com.io7m.r1.kernel.types.KMaterialSpecularNotNoneType;
import com.io7m.r1.kernel.types.KMaterialSpecularType;
import com.io7m.r1.shaders.forward.RKFMaterialCases;

@SuppressWarnings("static-method") public final class RKMaterialCasesTest
{
  private static Reflections getReflections()
  {
    return new Reflections("com.io7m.r1.kernel.types");
  }

  @Test public void testAlbedoCases()
  {
    final Reflections r = RKMaterialCasesTest.getReflections();
    final Set<Class<? extends KMaterialAlbedoType>> st =
      r.getSubTypesOf(KMaterialAlbedoType.class);

    final RKFMaterialCases c = new RKFMaterialCases();
    Assert.assertEquals(st.size(), c.getCasesAlbedo().size());
  }

  @Test public void testDepthCases()
  {
    final Reflections r = RKMaterialCasesTest.getReflections();
    final Set<Class<? extends KMaterialDepthType>> st =
      r.getSubTypesOf(KMaterialDepthType.class);

    final RKFMaterialCases c = new RKFMaterialCases();
    Assert.assertEquals(st.size(), c.getCasesDepth().size());
  }

  @Test public void testNormalCases()
  {
    final Reflections r = RKMaterialCasesTest.getReflections();
    final Set<Class<? extends KMaterialNormalType>> st =
      r.getSubTypesOf(KMaterialNormalType.class);

    final RKFMaterialCases c = new RKFMaterialCases();
    Assert.assertEquals(st.size(), c.getCasesNormal().size());
  }

  @Test public void testEnvironmentCases()
  {
    final Reflections r = RKMaterialCasesTest.getReflections();
    final Set<Class<? extends KMaterialEnvironmentType>> st =
      r.getSubTypesOf(KMaterialEnvironmentType.class);

    final RKFMaterialCases c = new RKFMaterialCases();
    Assert.assertEquals(st.size(), c.getCasesEnvironment().size());
  }

  @Test public void testRefractiveCases()
  {
    final Reflections r = RKMaterialCasesTest.getReflections();
    final Set<Class<? extends KMaterialRefractiveType>> st =
      r.getSubTypesOf(KMaterialRefractiveType.class);

    final RKFMaterialCases c = new RKFMaterialCases();
    Assert.assertEquals(st.size(), c.getCasesRefractive().size());
  }

  @Test public void testSpecularCases()
  {
    final Reflections r = RKMaterialCasesTest.getReflections();
    final Set<Class<? extends KMaterialSpecularType>> st =
      r.getSubTypesOf(KMaterialSpecularType.class);
    st.remove(KMaterialSpecularNotNoneType.class);

    final RKFMaterialCases c = new RKFMaterialCases();
    Assert.assertEquals(st.size(), c.getCasesSpecular().size());
  }
}