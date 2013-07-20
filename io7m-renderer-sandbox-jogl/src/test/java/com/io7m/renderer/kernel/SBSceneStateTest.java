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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Serializer;

import org.junit.Test;

import com.io7m.jaux.functional.Pair;
import com.io7m.jcanephora.Texture2DStatic;
import com.io7m.jlog.Log;
import com.io7m.renderer.RSpaceRGB;
import com.io7m.renderer.RSpaceWorld;
import com.io7m.renderer.RVectorI3F;
import com.io7m.renderer.kernel.SBSceneState.SBSceneNormalized;

public final class SBSceneStateTest
{
  private Log makeLog()
  {
    final Properties props = new Properties();
    props.setProperty("com.io7m.renderer.logs.tests", "true");
    return new Log(props, "com.io7m.renderer", "tests");
  }

  @Test public void testNormalization()
    throws IOException
  {
    final Log log = this.makeLog();
    final SBSceneState state = new SBSceneState(log);

    state.textureAdd(
      new File("/x/y/z/file.txt"),
      new Pair<BufferedImage, Texture2DStatic>(null, null));
    state.textureAdd(
      new File("/a/b/c/file.txt"),
      new Pair<BufferedImage, Texture2DStatic>(null, null));
    state.textureAdd(
      new File("/a/b/z/file.txt"),
      new Pair<BufferedImage, Texture2DStatic>(null, null));

    {
      final SortedSet<SBMesh> objects = new TreeSet<SBMesh>();
      objects.add(new SBMesh("m0", null, null));
      objects.add(new SBMesh("m1", null, null));
      objects.add(new SBMesh("m2", null, null));
      state.meshAdd(new File("/x/z.so0"), objects);
    }

    {
      final SortedSet<SBMesh> objects = new TreeSet<SBMesh>();
      objects.add(new SBMesh("m0", null, null));
      objects.add(new SBMesh("m1", null, null));
      objects.add(new SBMesh("m2", null, null));
      state.meshAdd(new File("/z/z.so0"), objects);
    }

    state.lightAdd(new KLight.KDirectional(
      Integer.valueOf(23),
      new RVectorI3F<RSpaceWorld>(1, 2, 3),
      new RVectorI3F<RSpaceRGB>(4, 5, 6),
      64));

    {
      final Integer id = Integer.valueOf(64);
      final File model = new File("/z/z.so0");
      final String model_object = "m1";
      final RVectorI3F<RSpaceWorld> position =
        new RVectorI3F<RSpaceWorld>(10, 10, -10);
      final RVectorI3F<SBDegrees> orientation =
        new RVectorI3F<SBDegrees>(20, 20, -20);
      final File diffuse_texture = new File("/x/y/z/file.txt");
      final File normal_texture = new File("/a/b/c/file.txt");
      final File specular_texture = new File("/a/b/z/file.txt");

      state.objectAdd(new SBObjectDescription(
        id,
        model,
        model_object,
        position,
        orientation,
        diffuse_texture,
        normal_texture,
        specular_texture));
    }

    final SBSceneNormalized normal =
      new SBSceneState.SBSceneNormalized(log, state);

    final Element e = normal.toXML();
    final Serializer s = new Serializer(System.out);
    s.setIndent(2);
    s.setMaxLength(80);
    s.write(new Document(e));
  }
}
