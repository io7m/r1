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

package com.io7m.renderer.kernel_shaders;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Properties;

import javax.annotation.Nonnull;
import javax.xml.parsers.ParserConfigurationException;

import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.apache.commons.cli.ParseException;
import org.xml.sax.SAXException;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.functional.Pair;
import com.io7m.jlog.Log;
import com.io7m.jparasol.CompilerError;
import com.io7m.jparasol.frontend.Frontend;
import com.io7m.jparasol.xml.Batch;
import com.io7m.jparasol.xml.PGLSLCompactor;

public final class ShadowMakeAll
{
  private static @Nonnull String[] getSourcesList(
    final @Nonnull File out_dir)
  {
    return out_dir.list(new FilenameFilter() {
      @Override public boolean accept(
        final @Nonnull File dir,
        final @Nonnull String name)
      {
        return name.endsWith(".p");
      }
    });
  }

  public static void main(
    final String args[])
    throws IOException,
      ValidityException,
      NoSuchAlgorithmException,
      ParsingException,
      SAXException,
      ParserConfigurationException,
      ConstraintError,
      ParseException,
      CompilerError
  {
    if (args.length != 4) {
      final String message =
        "usage: out-batch out-parasol-directory out-glsl-directory out-glsl-compacted-directory";
      System.err.println(message);
      throw new IllegalArgumentException(message);
    }

    final File out_batch = new File(args[0]);
    final File out_parasol_dir = new File(args[1]);
    final File out_glsl_dir = new File(args[2]);
    final File out_glsl_compact_dir = new File(args[3]);

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

    final String[] sources = ShadowMakeAll.getSourcesList(out_parasol_dir);
    ShadowMakeAll.makeCompileSources(
      out_parasol_dir,
      sources,
      out_batch,
      out_glsl_dir,
      out_glsl_compact_dir);
  }

  private static void makeCompileSources(
    final @Nonnull File source_dir,
    final @Nonnull String[] sources,
    final @Nonnull File batch,
    final @Nonnull File out_dir,
    final @Nonnull File out_compact_dir)
    throws ValidityException,
      NoSuchAlgorithmException,
      ParsingException,
      IOException,
      SAXException,
      ParserConfigurationException,
      ConstraintError,
      ParseException,
      CompilerError
  {
    final ArrayList<String> argslist = new ArrayList<String>();

    argslist.add("--require-es");
    argslist.add("[,]");
    argslist.add("--require-full");
    argslist.add("[,]");
    argslist.add("--compile-batch");
    argslist.add(out_dir.toString());
    argslist.add(batch.toString());

    for (final String s : sources) {
      final File f = new File(source_dir, s);
      argslist.add(f.toString());
    }

    final String[] args = new String[argslist.size()];
    argslist.toArray(args);
    Frontend.run(Frontend.getLog(false), args);

    {
      final Batch b = Batch.fromFile(out_dir, batch);
      for (final Pair<String, String> k : b.getTargets()) {
        final File program_in = new File(out_dir, k.first);
        System.out.println("info: compact " + program_in);
        final File program_out = new File(out_compact_dir, k.first);
        PGLSLCompactor.newCompactor(
          program_in,
          program_out,
          ShadowMakeAll.getLog());
      }
    }
  }

  private static @Nonnull Log getLog()
  {
    final Properties props = new Properties();
    props.setProperty("com.io7m.parasol.logs.compactor", "false");
    return new Log(props, "com.io7m.parasol", "compactor");
  }
}
