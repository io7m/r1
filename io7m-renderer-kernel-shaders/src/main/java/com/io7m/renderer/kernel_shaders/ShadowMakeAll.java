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
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.io7m.parasol.Compiler;

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
    throws IOException
  {
    if (args.length != 2) {
      System.err.println("usage: out-parasol-directory out-glsl-directory");
      System.exit(1);
    }

    final File out_parasol_dir = new File(args[0]);
    final File out_glsl_dir = new File(args[1]);
    final File out_batch = new File(out_parasol_dir, "batch-shadow.txt");

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

    final List<ShadowLabel> shadowLabels = new ArrayList<ShadowLabel>();
    for (final ShadowLabel l : ShadowLabel.values()) {
      shadowLabels.add(l);
    }

    ShadowMakeAll.makeSources(shadowLabels, out_parasol_dir);
    ShadowMakeAll.makeBatch(shadowLabels, out_batch);

    final String[] sources = ShadowMakeAll.getSourcesList(out_parasol_dir);
    ShadowMakeAll.makeCompileSources(
      out_parasol_dir,
      sources,
      out_batch,
      out_glsl_dir);
  }

  public static void makeBatch(
    final @Nonnull List<ShadowLabel> shadowLabels,
    final @Nonnull File file)
    throws IOException
  {
    final FileWriter writer = new FileWriter(file);
    for (final ShadowLabel l : shadowLabels) {
      final String code = l.getCode();
      writer.append("shadow_" + code);
      writer.append(" : com.io7m.renderer.kernel.Shadow_" + code + ".p");
      writer.append("\n");
    }

    writer.close();
  }

  private static void makeCompileSources(
    final @Nonnull File source_dir,
    final @Nonnull String[] sources,
    final @Nonnull File batch,
    final @Nonnull File out_dir)
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
  }

  public static void makeSources(
    final @Nonnull List<ShadowLabel> shadowLabels,
    final @Nonnull File dir)
    throws IOException
  {
    if (dir.isDirectory() == false) {
      throw new IOException(dir + " is not a directory");
    }

    for (final ShadowLabel l : shadowLabels) {
      final String code = l.getCode();

      final File file = new File(dir, "Shadow_" + code + ".p");
      System.err.println("info: writing: " + file);

      final FileWriter writer = new FileWriter(file);
      try {
        writer.append(ShadowShaders.moduleShadow(l));
      } finally {
        writer.flush();
        writer.close();
      }
    }
  }
}
