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

package com.io7m.r1.shaders.deferred.geometry;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipOutputStream;

import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jfunctional.Pair;
import com.io7m.jlog.Log;
import com.io7m.jlog.LogPolicyProperties;
import com.io7m.jlog.LogPolicyType;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.Nullable;
import com.io7m.jparasol.core.GVersionES;
import com.io7m.jparasol.core.GVersionFull;
import com.io7m.jparasol.frontend.Compiler;
import com.io7m.jparasol.frontend.CompilerBatch;
import com.io7m.jparasol.frontend.CopyZip;
import com.io7m.jparasol.glsl.serialization.GSerializerType;
import com.io7m.jparasol.glsl.serialization.GSerializerZip;
import com.io7m.jparasol.lexer.Position;
import com.io7m.jparasol.typed.ast.TASTShaderNameFlat;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegular;
import com.io7m.r1.shaders.deferred.RKDMaterialCases;
import com.io7m.r1.shaders.deferred.RKDeferredShader;

/**
 * Generate all geometry-pass shaders.
 */

@SuppressWarnings("resource") @EqualityReference public final class RShadersDeferredGeometryMakeAll
{
  /**
   * Main program.
   *
   * @param args
   *          Command line arguments.
   * @throws Exception
   *           On errors.
   */

  public static void main(
    final String[] args)
    throws Exception
  {
    final ExecutorService exec;

    if (args.length != 2) {
      final String message = "usage: out-parasol-directory out-archive";
      System.err.println(message);
      throw new IllegalArgumentException(message);
    }

    final File out_parasol_dir = new File(args[0]);
    final File out_archive = new File(args[1]);

    final Properties p = new Properties();
    p.setProperty("com.io7m.r1.level", "LOG_DEBUG");
    p.setProperty("com.io7m.r1.logs.generator", "true");
    p.setProperty("com.io7m.r1.logs.generator.pipeline", "false");
    p.setProperty("com.io7m.r1.logs.generator.gpipeline", "false");
    p.setProperty("com.io7m.r1.logs.generator.compactor", "false");
    p.setProperty("com.io7m.r1.logs.generator.serializer-zip", "false");

    final LogPolicyType policy =
      LogPolicyProperties.newPolicy(p, "com.io7m.r1");
    final LogUsableType log = Log.newLog(policy, "generator");

    log.debug("parasol directory: " + out_parasol_dir);
    log.debug("archive: " + out_archive);

    if (out_parasol_dir.mkdirs() == false) {
      if (out_parasol_dir.isDirectory() == false) {
        throw new IOException("Could not create " + out_parasol_dir);
      }
    }

    final List<KMaterialOpaqueRegular> cases_opaque =
      new RKDMaterialCases().getCasesGeometryOpaqueRegular();

    RShadersDeferredGeometryMakeAll.makeSources(
      log,
      cases_opaque,
      out_parasol_dir);

    final CompilerBatch batch = CompilerBatch.newBatch();

    {
      final Pair<File, Position> meta =
        Pair.pair(new File("<stdin>"), Position.ZERO);

      for (final KMaterialOpaqueRegular c : cases_opaque) {
        assert c != null;
        final String code = c.materialGetCode();
        final String name =
          String.format(
            "%s.%s.p",
            RKDeferredShader.PACKAGE_DEFERRED_GEOMETRY_REGULAR,
            code);
        assert name != null;

        batch.addShaderWithOutputName(
          TASTShaderNameFlat.parse(name, meta),
          code);
      }
    }

    final List<File> sources =
      RShadersDeferredGeometryMakeAll.makeSourcesList(out_parasol_dir);

    exec =
      Executors
        .newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    assert exec != null;

    try {
      final ZipOutputStream archive_stream =
        CopyZip.copyZip(log, out_archive);
      final GSerializerType serializer =
        GSerializerZip.newSerializer(archive_stream, log);

      final Compiler c = Compiler.newCompiler(log, exec);
      c.setCompacting(true);
      c.setGeneratingCode(true);

      final SortedSet<GVersionES> es =
        new TreeSet<GVersionES>(GVersionES.ALL);
      es.remove(GVersionES.GLSL_ES_100);

      c.setRequiredES(es);
      c.setRequiredFull(GVersionFull.ALL);
      c.setSerializer(serializer);
      c.runForFiles(batch, sources);

      serializer.close();

      log.debug("done");
    } finally {
      exec.shutdown();
    }
  }

  private static List<File> makeSourcesList(
    final File out_parasol_dir)
  {
    final String[] sources = out_parasol_dir.list(new FilenameFilter() {
      @Override public boolean accept(
        final @Nullable File dir,
        final @Nullable String name)
      {
        assert dir != null;
        assert name != null;
        return name.endsWith(".p");
      }
    });

    final List<File> files = new ArrayList<File>();
    for (final String s : sources) {
      files.add(new File(out_parasol_dir, s));
    }

    return files;
  }

  private static void makeSources(
    final LogUsableType log,
    final List<KMaterialOpaqueRegular> cases_opaque,
    final File dir)
    throws IOException
  {
    if (dir.isDirectory() == false) {
      throw new IOException(dir + " is not a directory");
    }

    for (final KMaterialOpaqueRegular c : cases_opaque) {
      assert c != null;

      final String code = c.materialGetCode();
      final File file = new File(dir, code + ".p");
      log.info("Generating " + file);

      final FileWriter writer = new FileWriter(file);
      try {
        writer.append(RKDeferredShader.moduleGeometryOpaqueRegular(c));
      } finally {
        writer.flush();
        writer.close();
      }
    }
  }

  private RShadersDeferredGeometryMakeAll()
  {
    throw new UnreachableCodeException();
  }
}
