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

package com.io7m.renderer.shaders.forward.translucent.unlit;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.cli.ParseException;

import com.io7m.jlog.Log;
import com.io7m.jlog.LogLevel;
import com.io7m.jlog.LogPolicyAllOn;
import com.io7m.jlog.LogPolicyType;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.Nullable;
import com.io7m.jparasol.CompilerError;
import com.io7m.jparasol.frontend.Frontend;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.kernel.types.KMaterialTranslucentRefractive;
import com.io7m.renderer.kernel.types.KMaterialTranslucentRegular;
import com.io7m.renderer.shaders.core.EasyCompactor;
import com.io7m.renderer.shaders.forward.RKForwardShader;
import com.io7m.renderer.shaders.forward.RKForwardShaderCodes;
import com.io7m.renderer.shaders.forward.RKFUnlitTranslucentRefractiveCases;
import com.io7m.renderer.shaders.forward.RKFUnlitTranslucentRegularCases;

/**
 * Generate all opaque+unlit shaders.
 */

public final class MakeTranslucentUnlitMain
{
  private static LogUsableType getLog()
  {
    final LogPolicyType policy = LogPolicyAllOn.newPolicy(LogLevel.LOG_INFO);
    return Log.newLog(policy, "make-opaque-unlit-main");
  }

  private static int getThreadCount()
  {
    return Runtime.getRuntime().availableProcessors() * 2;
  }

  /**
   * Main program.
   * 
   * @param args
   *          Command line arguments.
   * @throws IOException
   *           On I/O errors.
   * @throws ParseException
   *           On XML errors.
   * @throws CompilerError
   *           On compilation errors.
   * @throws InterruptedException
   *           On concurrency/execution errors.
   * @throws ExecutionException
   *           On concurrency/execution errors.
   * @throws TimeoutException
   *           On concurrency/execution errors.
   */

  public static void main(
    final String[] args)
    throws IOException,
      ParseException,
      CompilerError,
      InterruptedException,
      ExecutionException,
      TimeoutException
  {
    if (args.length != 3) {
      final String message =
        "usage: out-parasol-directory out-glsl-directory out-glsl-compacted-directory";
      System.err.println(message);
      throw new IllegalArgumentException(message);
    }

    final LogUsableType log = MakeTranslucentUnlitMain.getLog();

    final File out_parasol_dir = new File(args[0]);
    final File out_glsl_dir = new File(args[1]);
    final File out_glsl_compact_dir = new File(args[2]);
    final File out_batch = new File(out_parasol_dir, "batch-forward.txt");
    final File out_sources = new File(out_parasol_dir, "source-list.txt");

    if (out_parasol_dir.mkdirs() == false) {
      if (out_parasol_dir.isDirectory() == false) {
        throw new IOException("Could not create " + out_parasol_dir);
      }
    }
    if (out_glsl_dir.mkdirs() == false) {
      if (out_glsl_dir.isDirectory() == false) {
        throw new IOException("Could not create " + out_glsl_dir);
      }
    }
    if (out_glsl_compact_dir.mkdirs() == false) {
      if (out_glsl_compact_dir.isDirectory() == false) {
        throw new IOException("Could not create " + out_glsl_compact_dir);
      }
    }

    final List<KMaterialTranslucentRegular> translucent_regular =
      new RKFUnlitTranslucentRegularCases().getCases();
    final List<KMaterialTranslucentRefractive> translucent_refractive =
      new RKFUnlitTranslucentRefractiveCases().getCases();

    MakeTranslucentUnlitMain.makeSourcesUnlitTranslucentRegular(
      log,
      translucent_regular,
      out_parasol_dir);

    MakeTranslucentUnlitMain.makeSourcesUnlitTranslucentRefractive(
      log,
      translucent_refractive,
      out_parasol_dir);

    MakeTranslucentUnlitMain.generateBatches(
      log,
      out_batch,
      translucent_regular,
      translucent_refractive);

    MakeTranslucentUnlitMain.makeSourcesList(out_parasol_dir, out_sources);
    MakeTranslucentUnlitMain.makeCompileSources(
      log,
      out_sources,
      out_batch,
      out_glsl_dir,
      out_glsl_compact_dir);
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

      final String code =
        RKForwardShaderCodes.fromUnlitTranslucentRefractive(m);
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

  private static void generateBatches(
    final LogUsableType log,
    final File out_batch,
    final List<KMaterialTranslucentRegular> translucent_regular,
    final List<KMaterialTranslucentRefractive> translucent_refractive)
    throws IOException
  {
    log.info("Generating batch: " + out_batch);
    final FileWriter writer = new FileWriter(out_batch);

    for (final KMaterialTranslucentRegular l : translucent_regular) {
      assert l != null;
      final String code = RKForwardShaderCodes.fromUnlitTranslucentRegular(l);
      writer.append(code);
      writer.append(" : ");
      writer
        .append(RKForwardShader.PACKAGE_FORWARD_TRANSLUCENT_UNLIT_REGULAR);
      writer.append(".");
      writer.append(code);
      writer.append(".p");
      writer.append("\n");
    }

    for (final KMaterialTranslucentRefractive l : translucent_refractive) {
      assert l != null;
      final String code =
        RKForwardShaderCodes.fromUnlitTranslucentRefractive(l);
      writer.append(code);
      writer.append(" : ");
      writer
        .append(RKForwardShader.PACKAGE_FORWARD_TRANSLUCENT_UNLIT_REFRACTIVE);
      writer.append(".");
      writer.append(code);
      writer.append(".p");
      writer.append("\n");
    }

    writer
      .append("refraction_mask : com.io7m.renderer.core.Refraction.mask\n");
    writer.close();
  }

  private static void makeCompileSources(
    final LogUsableType log,
    final File out_sources,
    final File batch,
    final File out_dir,
    final File out_compact_dir)
    throws IOException,
      ParseException,
      CompilerError,
      InterruptedException,
      ExecutionException,
      TimeoutException
  {
    final List<String> argslist = new ArrayList<String>();

    argslist.add("--threads");
    argslist.add(Integer.toString(MakeTranslucentUnlitMain.getThreadCount()));
    argslist.add("--require-es");
    argslist.add("[,]");
    argslist.add("--require-full");
    argslist.add("[,]");
    argslist.add("--compile-batch");
    argslist.add(out_dir.toString());
    argslist.add(batch.toString());
    argslist.add(out_sources.toString());

    final String[] args = new String[argslist.size()];
    argslist.toArray(args);
    Frontend.run(Frontend.getLog(false), args);

    EasyCompactor.compact(
      log,
      MakeTranslucentUnlitMain.getThreadCount(),
      batch,
      out_dir,
      out_compact_dir);
  }

  private static void makeSourcesList(
    final File out_parasol_dir,
    final File out_sources)
    throws IOException
  {
    final String[] sources = out_parasol_dir.list(new FilenameFilter() {
      @Override public boolean accept(
        final @Nullable File dir,
        final @Nullable String name)
      {
        assert name != null;
        return name.endsWith(".p");
      }
    });

    final FileWriter writer = new FileWriter(out_sources);
    for (final String s : sources) {
      writer.write(String.format("%s/%s\n", out_parasol_dir, s));
    }
    writer.flush();
    writer.close();
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

      final String code = RKForwardShaderCodes.fromUnlitTranslucentRegular(m);
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

  private MakeTranslucentUnlitMain()
  {
    throw new UnreachableCodeException();
  }
}
