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

package com.io7m.renderer.kernel_shaders.depth_variance;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.apache.commons.cli.ParseException;
import org.xml.sax.SAXException;

import com.io7m.jfunctional.Pair;
import com.io7m.jlog.Log;
import com.io7m.jlog.LogLevel;
import com.io7m.jlog.LogPolicyAllOn;
import com.io7m.jlog.LogPolicyType;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.Nullable;
import com.io7m.jparasol.CompilerError;
import com.io7m.jparasol.frontend.Frontend;
import com.io7m.jparasol.xml.Batch;
import com.io7m.jparasol.xml.PGLSLCompactor;

public final class DepthVarianceMakeAll
{
  private static LogUsableType getLog()
  {
    final LogPolicyType policy = LogPolicyAllOn.newPolicy(LogLevel.LOG_INFO);
    return Log.newLog(policy, "make-depth-variance");
  }

  public static void main(
    final String args[])
    throws IOException,
      ValidityException,
      NoSuchAlgorithmException,
      ParsingException,
      SAXException,
      ParserConfigurationException,
      ParseException,
      CompilerError,
      InterruptedException
  {
    if (args.length != 5) {
      final String message =
        "usage: out-batch out-sources out-parasol-directory out-glsl-directory out-glsl-compacted-directory";
      System.err.println(message);
      throw new IllegalArgumentException(message);
    }

    final LogUsableType log = DepthVarianceMakeAll.getLog();

    final File out_batch = new File(args[0]);
    final File out_sources = new File(args[1]);
    final File out_parasol_dir = new File(args[2]);
    final File out_glsl_dir = new File(args[3]);
    final File out_glsl_compact_dir = new File(args[4]);

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

    DepthVarianceMakeAll.makeSourcesList(out_parasol_dir, out_sources);
    DepthVarianceMakeAll.makeCompileSources(
      log,
      out_parasol_dir,
      out_sources,
      out_batch,
      out_glsl_dir,
      out_glsl_compact_dir);
  }

  private static void makeCompileSources(
    final LogUsableType log,
    @SuppressWarnings("unused") final File source_dir,
    final File out_sources,
    final File batch,
    final File out_dir,
    final File out_compact_dir)
    throws ValidityException,
      NoSuchAlgorithmException,
      ParsingException,
      IOException,
      SAXException,
      ParserConfigurationException,
      ParseException,
      CompilerError,
      InterruptedException
  {
    final ArrayList<String> argslist = new ArrayList<String>();

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

    {
      final Batch b = Batch.fromFile(out_dir, batch);
      for (final Pair<String, String> k : b.getTargets()) {
        final File program_in = new File(out_dir, k.getLeft());
        log.info("Compact " + program_in);
        final File program_out = new File(out_compact_dir, k.getLeft());
        PGLSLCompactor.newCompactor(
          program_in,
          program_out,
          DepthVarianceMakeAll.getLog());
      }
    }
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
}
