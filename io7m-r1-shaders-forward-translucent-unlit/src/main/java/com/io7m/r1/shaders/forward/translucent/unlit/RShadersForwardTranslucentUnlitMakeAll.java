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

package com.io7m.r1.shaders.forward.translucent.unlit;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
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
import com.io7m.jparasol.CompilerError;
import com.io7m.jparasol.UIError;
import com.io7m.jparasol.core.GVersionES;
import com.io7m.jparasol.core.GVersionFull;
import com.io7m.jparasol.frontend.Compiler;
import com.io7m.jparasol.frontend.CompilerBatch;
import com.io7m.jparasol.frontend.CopyZip;
import com.io7m.jparasol.frontend.JPBatchDuplicateOutput;
import com.io7m.jparasol.frontend.JPBatchDuplicateShader;
import com.io7m.jparasol.frontend.JPBatchInvalidOutput;
import com.io7m.jparasol.glsl.serialization.GSerializerType;
import com.io7m.jparasol.glsl.serialization.GSerializerZip;
import com.io7m.jparasol.lexer.Position;
import com.io7m.jparasol.metaserializer.JPMetaSerializerType;
import com.io7m.jparasol.metaserializer.protobuf.JPProtobufMetaSerializer;
import com.io7m.jparasol.typed.ast.TASTShaderNameFlat;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.kernel.types.KMaterialTranslucentRefractive;
import com.io7m.r1.kernel.types.KMaterialTranslucentRegular;
import com.io7m.r1.shaders.forward.RKFUnlitTranslucentRefractiveCases;
import com.io7m.r1.shaders.forward.RKFUnlitTranslucentRegularCases;
import com.io7m.r1.shaders.forward.RKForwardShader;

/**
 * Generate all opaque+unlit shaders.
 */

@EqualityReference public final class RShadersForwardTranslucentUnlitMakeAll
{
  private static CompilerBatch generateBatches(
    final List<KMaterialTranslucentRegular> translucent_regular,
    final List<KMaterialTranslucentRefractive> translucent_refractive)
    throws JPBatchInvalidOutput,
      JPBatchDuplicateShader,
      JPBatchDuplicateOutput,
      UIError
  {
    final CompilerBatch batch = CompilerBatch.newBatch();

    final Pair<File, Position> meta =
      Pair.pair(new File("<stdin>"), Position.ZERO);

    for (final KMaterialTranslucentRegular l : translucent_regular) {
      assert l != null;
      final String code = l.getCode();
      final String name =
        String.format(
          "%s.%s.p",
          RKForwardShader.PACKAGE_FORWARD_TRANSLUCENT_UNLIT_REGULAR,
          code);
      assert name != null;

      batch.addShaderWithOutputName(
        TASTShaderNameFlat.parse(name, meta),
        code);
    }

    for (final KMaterialTranslucentRefractive l : translucent_refractive) {
      assert l != null;
      final String code = l.getCode();
      final String name =
        String.format(
          "%s.%s.p",
          RKForwardShader.PACKAGE_FORWARD_TRANSLUCENT_UNLIT_REFRACTIVE,
          code);
      assert name != null;

      batch.addShaderWithOutputName(
        TASTShaderNameFlat.parse(name, meta),
        code);
    }

    batch.addShaderWithOutputName(
      TASTShaderNameFlat.parse("com.io7m.r1.core.Refraction.mask", meta),
      "refraction_mask");

    return batch;
  }

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

    try {
      final List<KMaterialTranslucentRegular> translucent_regular =
        new RKFUnlitTranslucentRegularCases().getCases();
      final List<KMaterialTranslucentRefractive> translucent_refractive =
        new RKFUnlitTranslucentRefractiveCases().getCases();

      RShadersForwardTranslucentUnlitMakeAll
        .makeSourcesUnlitTranslucentRegular(
          log,
          translucent_regular,
          out_parasol_dir);

      RShadersForwardTranslucentUnlitMakeAll
        .makeSourcesUnlitTranslucentRefractive(
          log,
          translucent_refractive,
          out_parasol_dir);

      final CompilerBatch batch =
        RShadersForwardTranslucentUnlitMakeAll.generateBatches(
          translucent_regular,
          translucent_refractive);

      final List<File> sources =
        RShadersForwardTranslucentUnlitMakeAll
          .makeSourcesList(out_parasol_dir);

      final ExecutorService e =
        Executors.newFixedThreadPool(Runtime
          .getRuntime()
          .availableProcessors() * 2);
      assert e != null;

      final ZipOutputStream archive_stream =
        CopyZip.copyZip(log, out_archive);
      final JPMetaSerializerType meta_s =
        JPProtobufMetaSerializer.newSerializer();
      final GSerializerType serializer =
        GSerializerZip.newSerializer(meta_s, archive_stream, log);

      final Compiler c = Compiler.newCompiler(log, e);
      c.setCompacting(true);
      c.setGeneratingCode(true);
      c.setRequiredES(GVersionES.ALL);
      c.setRequiredFull(GVersionFull.ALL);
      c.setSerializer(serializer);
      c.runForFiles(batch, sources);

      serializer.close();
      e.shutdown();

      log.debug("done");

    } catch (final CompilerError e) {
      System.err.printf(
        "%s: %s:%s: %s\n",
        e,
        e.getFile(),
        e.getPosition(),
        e.getMessage());
      throw e;
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

  private static void makeSourcesUnlitTranslucentRefractive(
    final LogUsableType log,
    final List<KMaterialTranslucentRefractive> translucent_refractive,
    final File dir)
    throws IOException
  {
    if (dir.isDirectory() == false) {
      throw new IOException(dir + " is not a directory");
    }

    for (final KMaterialTranslucentRefractive m : translucent_refractive) {
      assert m != null;

      final String code = m.getCode();
      final File file = new File(dir, code + ".p");
      log.info("Generating " + file);

      final FileWriter writer = new FileWriter(file);
      try {
        writer.append(RKForwardShader.moduleUnlitTranslucentRefractive(m));
      } finally {
        writer.flush();
        writer.close();
      }
    }
  }

  private static void makeSourcesUnlitTranslucentRegular(
    final LogUsableType log,
    final List<KMaterialTranslucentRegular> opaque_unlit,
    final File dir)
    throws IOException
  {
    if (dir.isDirectory() == false) {
      throw new IOException(dir + " is not a directory");
    }

    for (final KMaterialTranslucentRegular m : opaque_unlit) {
      assert m != null;

      final String code = m.getCode();
      final File file = new File(dir, code + ".p");
      log.info("Generating " + file);

      final FileWriter writer = new FileWriter(file);
      try {
        writer.append(RKForwardShader.moduleUnlitTranslucentRegular(m));
      } finally {
        writer.flush();
        writer.close();
      }
    }
  }

  private RShadersForwardTranslucentUnlitMakeAll()
  {
    throw new UnreachableCodeException();
  }
}
