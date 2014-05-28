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

package com.io7m.renderer.kernel_shaders.forward;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.cli.ParseException;

import com.io7m.jfunctional.Pair;
import com.io7m.jfunctional.Unit;
import com.io7m.jlog.Log;
import com.io7m.jlog.LogPolicyProperties;
import com.io7m.jlog.LogPolicyType;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.Nullable;
import com.io7m.jparasol.CompilerError;
import com.io7m.jparasol.frontend.Frontend;
import com.io7m.jparasol.xml.Batch;
import com.io7m.jparasol.xml.PGLSLCompactor;
import com.io7m.jproperties.JPropertyException;
import com.io7m.renderer.kernel.types.KMaterialOpaqueRegular;
import com.io7m.renderer.kernel.types.KMaterialTranslucentRefractive;
import com.io7m.renderer.kernel.types.KMaterialTranslucentRegular;
import com.io7m.renderer.kernel.types.KMaterialTranslucentSpecularOnly;

public final class ForwardMakeAll
{
  private static void compactSources(
    final File batch,
    final File out_dir,
    final File out_compact_dir)
    throws IOException,
      InterruptedException,
      ExecutionException,
      TimeoutException
  {
    final Batch b = Batch.fromFile(out_dir, batch);

    final List<Future<Unit>> futures = new ArrayList<Future<Unit>>();
    final ExecutorService exec =
      Executors.newFixedThreadPool(ForwardMakeAll.getThreadCount());

    try {
      final List<Pair<String, String>> targets = b.getTargets();
      for (int index = 0; index < targets.size(); ++index) {
        final Pair<String, String> k = targets.get(index);

        futures.add(exec.submit(new Callable<Unit>() {
          @SuppressWarnings("synthetic-access") @Override public Unit call()
            throws Exception
          {
            final File program_in = new File(out_dir, k.getLeft());
            final File program_out = new File(out_compact_dir, k.getLeft());
            System.out.println("info: compact " + program_in);
            PGLSLCompactor.newCompactor(
              program_in,
              program_out,
              ForwardMakeAll.getLog());
            return Unit.unit();
          }
        }));
      }

      for (int index = 0; index < futures.size(); ++index) {
        final Future<Unit> f = futures.get(index);
        f.get(10, TimeUnit.SECONDS);
      }

      System.out.println("info: compactions completed");
    } finally {
      exec.shutdown();
    }
  }

  private static LogUsableType getLog()
    throws JPropertyException
  {
    final Properties props = new Properties();
    props.setProperty("com.io7m.parasol.level", "LOG_INFO");
    props.setProperty("com.io7m.parasol.logs.compactor", "false");
    final LogPolicyType policy =
      LogPolicyProperties.newPolicy(props, "com.io7m.parasol");
    return Log.newLog(policy, "compactor");
  }

  private static int getThreadCount()
  {
    return Runtime.getRuntime().availableProcessors() * 2;
  }

  public static void main(
    final String args[])
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

    final List<KMaterialOpaqueRegular> opaque_unlit =
      new RKUnlitOpaqueRegularCases().getCases();
    ForwardMakeAll.makeSourcesUnlitOpaque(opaque_unlit, out_parasol_dir);

    final List<RKLitCase<KMaterialOpaqueRegular>> opaque_lit =
      new RKLitOpaqueRegularCases().getCases();
    ForwardMakeAll.makeSourcesLitOpaqueRegular(opaque_lit, out_parasol_dir);

    final List<KMaterialTranslucentRegular> translucent_regular_unlit =
      new RKUnlitTranslucentRegularCases().getCases();
    ForwardMakeAll.makeSourcesTranslucentRegularUnlit(
      translucent_regular_unlit,
      out_parasol_dir);

    final List<RKLitCase<KMaterialTranslucentRegular>> translucent_regular_lit =
      new RKLitTranslucentRegularCases().getCases();
    ForwardMakeAll.makeSourcesLitTranslucentRegular(
      translucent_regular_lit,
      out_parasol_dir);

    final List<KMaterialTranslucentRefractive> translucent_refractive =
      new RKUnlitTranslucentRefractiveCases().getCases();
    ForwardMakeAll.makeSourcesTranslucentRefractive(
      translucent_refractive,
      out_parasol_dir);

    final List<RKLitCase<KMaterialTranslucentSpecularOnly>> translucent_specular =
      new RKLitTranslucentSpecularOnlyCases().getCases();
    ForwardMakeAll.makeSourcesTranslucentSpecularOnly(
      translucent_specular,
      out_parasol_dir);

    ForwardMakeAll.makeBatch(
      opaque_unlit,
      opaque_lit,
      translucent_regular_unlit,
      translucent_regular_lit,
      translucent_refractive,
      translucent_specular,
      out_batch);

    ForwardMakeAll.makeSourcesList(out_parasol_dir, out_sources);
    ForwardMakeAll.makeCompileSources(
      out_sources,
      out_batch,
      out_glsl_dir,
      out_glsl_compact_dir);
  }

  public static
    void
    makeBatch(
      final List<KMaterialOpaqueRegular> opaque_unlit,
      final List<RKLitCase<KMaterialOpaqueRegular>> opaque_lit,
      final List<KMaterialTranslucentRegular> translucent_regular_unlit,
      final List<RKLitCase<KMaterialTranslucentRegular>> translucent_regular_lit,
      final List<KMaterialTranslucentRefractive> translucent_refractive,
      final List<RKLitCase<KMaterialTranslucentSpecularOnly>> translucent_specular,
      final File file)
      throws IOException
  {
    final FileWriter writer = new FileWriter(file);

    for (final KMaterialOpaqueRegular l : opaque_unlit) {
      final String code = RKForwardShaderCodes.fromUnlitOpaqueRegular(l);
      writer.append(code);
      writer.append(" : com.io7m.renderer.kernel." + code + ".p");
      writer.append("\n");
    }

    for (final RKLitCase<KMaterialOpaqueRegular> l : opaque_lit) {
      final String code = RKForwardShaderCodes.fromLitOpaqueRegularCase(l);
      writer.append(code);
      writer.append(" : com.io7m.renderer.kernel." + code + ".p");
      writer.append("\n");
    }

    for (final KMaterialTranslucentRegular l : translucent_regular_unlit) {
      final String code = RKForwardShaderCodes.fromUnlitTranslucentRegular(l);
      writer.append(code);
      writer.append(" : com.io7m.renderer.kernel." + code + ".p");
      writer.append("\n");
    }

    for (final RKLitCase<KMaterialTranslucentRegular> l : translucent_regular_lit) {
      final String code = RKForwardShaderCodes.fromLitTranslucentRegular(l);
      writer.append(code);
      writer.append(" : com.io7m.renderer.kernel." + code + ".p");
      writer.append("\n");
    }

    for (final KMaterialTranslucentRefractive l : translucent_refractive) {
      final String code = RKForwardShaderCodes.fromUnlitTranslucentRefractive(l);
      writer.append(code);
      writer.append(" : com.io7m.renderer.kernel." + code + ".p");
      writer.append("\n");
    }

    for (final RKLitCase<KMaterialTranslucentSpecularOnly> l : translucent_specular) {
      final String code = RKForwardShaderCodes.fromLitTranslucentSpecularOnly(l);
      writer.append(code);
      writer.append(" : com.io7m.renderer.kernel." + code + ".p");
      writer.append("\n");
    }

    writer.close();
  }

  private static void makeCompileSources(
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
    final ArrayList<String> argslist = new ArrayList<String>();

    argslist.add("--threads");
    argslist.add(Integer.toString(ForwardMakeAll.getThreadCount()));
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

    ForwardMakeAll.compactSources(batch, out_dir, out_compact_dir);
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

  private static void makeSourcesLitOpaqueRegular(
    final List<RKLitCase<KMaterialOpaqueRegular>> opaque_lit,
    final File dir)
    throws IOException
  {
    if (dir.isDirectory() == false) {
      throw new IOException(dir + " is not a directory");
    }

    for (final RKLitCase<KMaterialOpaqueRegular> c : opaque_lit) {
      final String scode = RKForwardShaderCodes.fromUnlitOpaqueRegular(c);

      final File file = new File(dir, scode + ".p");
      System.err.println("info: writing: " + file);

      final FileWriter writer = new FileWriter(file);
      try {
        writer.append(ForwardShader.moduleLitOpaqueRegular(
          c.getCapabilities(),
          c.getLight(),
          c.getMaterial()));
      } finally {
        writer.flush();
        writer.close();
      }
    }
  }

  private static void makeSourcesLitTranslucentRegular(
    final List<RKLitCase<KMaterialTranslucentRegular>> translucent_lit,
    final File dir)
    throws IOException
  {
    if (dir.isDirectory() == false) {
      throw new IOException(dir + " is not a directory");
    }

    for (final RKLitCase<KMaterialTranslucentRegular> c : translucent_lit) {
      final String scode = RKForwardShaderCodes.fromLitTranslucentRegular(c);

      final File file = new File(dir, scode + ".p");
      System.err.println("info: writing: " + file);

      final FileWriter writer = new FileWriter(file);
      try {
        writer.append(ForwardShader.moduleLitTranslucentRegular(
          c.getCapabilities(),
          c.getLight(),
          c.getMaterial()));
      } finally {
        writer.flush();
        writer.close();
      }
    }
  }

  private static void makeSourcesTranslucentRefractive(
    final List<KMaterialTranslucentRefractive> refractive,
    final File dir)
    throws IOException
  {
    if (dir.isDirectory() == false) {
      throw new IOException(dir + " is not a directory");
    }

    for (final KMaterialTranslucentRefractive m : refractive) {
      final String code = RKForwardShaderCodes.fromUnlitTranslucentRefractive(m);

      final File file = new File(dir, code + ".p");
      System.err.println("info: writing: " + file);

      final FileWriter writer = new FileWriter(file);
      try {
        writer.append(ForwardShader.moduleUnlitTranslucentRefractive(m));
      } finally {
        writer.flush();
        writer.close();
      }
    }
  }

  private static void makeSourcesTranslucentRegularUnlit(
    final List<KMaterialTranslucentRegular> translucent_unlit,
    final File dir)
    throws IOException
  {
    if (dir.isDirectory() == false) {
      throw new IOException(dir + " is not a directory");
    }

    for (final KMaterialTranslucentRegular m : translucent_unlit) {
      final String code = RKForwardShaderCodes.fromUnlitTranslucentRegular(m);

      final File file = new File(dir, code + ".p");
      System.err.println("info: writing: " + file);

      final FileWriter writer = new FileWriter(file);
      try {
        writer.append(ForwardShader.moduleUnlitTranslucentRegular(m));
      } finally {
        writer.flush();
        writer.close();
      }
    }
  }

  static
    void
    makeSourcesTranslucentSpecularOnly(
      final List<RKLitCase<KMaterialTranslucentSpecularOnly>> translucent_specular,
      final File dir)
      throws IOException
  {
    if (dir.isDirectory() == false) {
      throw new IOException(dir + " is not a directory");
    }

    for (final RKLitCase<KMaterialTranslucentSpecularOnly> c : translucent_specular) {
      final String scode = RKForwardShaderCodes.fromLitTranslucentSpecularOnly(c);

      final File file = new File(dir, scode + ".p");
      System.err.println("info: writing: " + file);

      final FileWriter writer = new FileWriter(file);
      try {
        writer.append(ForwardShader.moduleLitTranslucentSpecularOnly(
          c.getCapabilities(),
          c.getLight(),
          c.getMaterial()));
      } finally {
        writer.flush();
        writer.close();
      }
    }
  }

  private static void makeSourcesUnlitOpaque(
    final List<KMaterialOpaqueRegular> opaque_unlit,
    final File dir)
    throws IOException
  {
    if (dir.isDirectory() == false) {
      throw new IOException(dir + " is not a directory");
    }

    for (final KMaterialOpaqueRegular m : opaque_unlit) {
      final String code = RKForwardShaderCodes.fromUnlitOpaqueRegular(m);

      final File file = new File(dir, code + ".p");
      System.err.println("info: writing: " + file);

      final FileWriter writer = new FileWriter(file);
      try {
        writer.append(ForwardShader.moduleUnlitOpaqueRegular(m));
      } finally {
        writer.flush();
        writer.close();
      }
    }
  }
}
