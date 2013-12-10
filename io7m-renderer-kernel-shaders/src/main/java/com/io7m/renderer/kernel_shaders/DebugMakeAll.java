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

package com.io7m.renderer.kernel_shaders;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Properties;

import javax.annotation.Nonnull;
import javax.xml.parsers.ParserConfigurationException;

import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.xml.sax.SAXException;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.functional.Pair;
import com.io7m.jlog.Log;
import com.io7m.parasol.Compiler;
import com.io7m.parasol.xml.Batch;
import com.io7m.parasol.xml.PGLSLCompactor;

public final class DebugMakeAll
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
      ConstraintError
  {
    if (args.length != 3) {
      System.err
        .println("usage: out-parasol-directory out-glsl-directory out-glsl-compact-directory");
      System.exit(1);
    }

    final File out_parasol_dir = new File(args[0]);
    final File out_glsl_dir = new File(args[1]);
    final File out_glsl_compact_dir = new File(args[2]);
    final File out_batch = new File(out_parasol_dir, "batch-debug.txt");

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

    DebugMakeAll.makeSources(out_parasol_dir);
    DebugMakeAll.makeBatch(out_batch);

    final String[] sources = DebugMakeAll.getSourcesList(out_parasol_dir);
    DebugMakeAll.makeCompileSources(
      out_parasol_dir,
      sources,
      out_batch,
      out_glsl_dir,
      out_glsl_compact_dir);
  }

  public static void makeBatch(
    final @Nonnull File file)
    throws IOException,
      ConstraintError
  {
    final PrintWriter writer = new PrintWriter(new FileWriter(file));

    final InputStream stream =
      DebugMakeAll.class
        .getResourceAsStream("/com/io7m/renderer/kernel_shaders/debug-batch.txt");

    try {
      final Batch batch = Batch.fromStream(new File("/"), stream);
      for (final Pair<String, String> t : batch.getTargets()) {
        writer.append(t.first);
        writer.append(" : ");
        writer.append(t.second);
        writer.println();
      }
      writer.flush();
      writer.close();
    } finally {
      stream.close();
    }
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
      ConstraintError
  {
    final ArrayList<String> argslist = new ArrayList<String>();

    argslist.add("--Yno-comments");
    argslist.add("--require-es");
    argslist.add("[100,300]");
    argslist.add("--require-full");
    argslist.add("[110,430]");
    argslist.add("--compile-batch");
    argslist.add(out_dir.toString());
    argslist.add(batch.toString());

    for (final String s : sources) {
      final File f = new File(source_dir, s);
      argslist.add(f.toString());
    }

    final String[] args = new String[argslist.size()];
    argslist.toArray(args);
    Compiler.run(Compiler.getLog(false), args);

    {
      final Batch b = Batch.fromFile(out_dir, batch);
      for (final Pair<String, String> k : b.getTargets()) {
        final File program_in = new File(out_dir, k.first);
        System.out.println("info: compact " + program_in);
        final File program_out = new File(out_compact_dir, k.first);
        PGLSLCompactor.newCompactor(
          program_in,
          program_out,
          DebugMakeAll.getLog());
      }
    }
  }

  private static @Nonnull Log getLog()
  {
    final Properties props = new Properties();
    props.setProperty("com.io7m.parasol.logs.compactor", "false");
    return new Log(props, "com.io7m.parasol", "compactor");
  }

  public static void makeSources(
    final @Nonnull File dir)
    throws IOException
  {
    if (dir.isDirectory() == false) {
      throw new IOException(dir + " is not a directory");
    }
  }
}