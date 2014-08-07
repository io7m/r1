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

package com.io7m.r1.examples;

import java.io.InputStream;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jfunctional.Some;
import com.io7m.jfunctional.Unit;
import com.io7m.jlog.Log;
import com.io7m.jlog.LogLevel;
import com.io7m.jlog.LogPolicyAllOn;
import com.io7m.jlog.LogType;
import com.io7m.jlog.LogUsableType;
import com.io7m.r1.examples.scenes.DLSimple0;
import com.io7m.r1.kernel.KRendererDeferred;
import com.io7m.r1.kernel.KRendererType;

@SuppressWarnings("static-method") public final class ExampleImagesTest
{
  private static LogType getLog()
  {
    return Log.newLog(LogPolicyAllOn.newPolicy(LogLevel.LOG_DEBUG), "tests");
  }

  @Test public void testGetImage()
    throws Exception
  {
    final LogUsableType log = ExampleImagesTest.getLog();
    final ExampleImages<String> e =
      new ExampleImages<String>(new ExampleImageLoaderType<String>() {
        @Override public String loadImage(
          final String name,
          final InputStream _)
          throws Exception
        {
          return name;
        }
      }, log);

    final Some<String> r =
      (Some<String>) e.getImage(KRendererDeferred.class, DLSimple0.class, 0);
    final String expected =
      String.format(
        "/%s/%s/%s/0.png",
        "com/io7m/r1/examples/results",
        DLSimple0.class.getCanonicalName(),
        KRendererDeferred.class.getCanonicalName());
    Assert.assertEquals(expected, r.get());
  }

  @Test public void testRendererList()
    throws Exception
  {
    final LogUsableType log = ExampleImagesTest.getLog();
    final ExampleImages<Unit> e =
      new ExampleImages<Unit>(new ExampleImageLoaderType<Unit>() {
        @Override public Unit loadImage(
          final String _,
          final InputStream __)
          throws Exception
        {
          return Unit.unit();
        }
      }, log);

    final List<Class<? extends KRendererType>> r =
      e.getSceneRenderers(DLSimple0.class);
    Assert.assertEquals(1, r.size());
    Assert.assertTrue(r.contains(KRendererDeferred.class));
  }
}
