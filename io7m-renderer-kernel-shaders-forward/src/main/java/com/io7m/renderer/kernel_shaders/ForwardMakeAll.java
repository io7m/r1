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
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Set;

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
import com.io7m.renderer.kernel.types.KMaterialForwardOpaqueLitLabel;
import com.io7m.renderer.kernel.types.KMaterialForwardOpaqueUnlitLabel;
import com.io7m.renderer.kernel.types.KMaterialForwardTranslucentRegularLitLabel;
import com.io7m.renderer.kernel.types.KMaterialForwardTranslucentRegularUnlitLabel;

public final class ForwardMakeAll
{
  private static @Nonnull Log getLog()
  {
    final Properties props = new Properties();
    props.setProperty("com.io7m.parasol.logs.compactor", "false");
    return new Log(props, "com.io7m.parasol", "compactor");
  }

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
      ConstraintError,
      ValidityException,
      NoSuchAlgorithmException,
      ParsingException,
      SAXException,
      ParserConfigurationException,
      ParseException,
      CompilerError
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

    final Set<KMaterialForwardOpaqueUnlitLabel> opaque_unlit =
      KMaterialForwardOpaqueUnlitLabel.allLabels();
    ForwardMakeAll.makeSourcesOpaqueUnlit(opaque_unlit, out_parasol_dir);
    final Set<KMaterialForwardOpaqueLitLabel> opaque_lit =
      KMaterialForwardOpaqueLitLabel.allLabels();
    ForwardMakeAll.makeSourcesOpaqueLit(opaque_lit, out_parasol_dir);

    final Set<KMaterialForwardTranslucentRegularUnlitLabel> translucent_regular_unlit =
      KMaterialForwardTranslucentRegularUnlitLabel.allLabels();
    ForwardMakeAll.makeSourcesTranslucentRegularUnlit(
      translucent_regular_unlit,
      out_parasol_dir);
    final Set<KMaterialForwardTranslucentRegularLitLabel> translucent_regular_lit =
      KMaterialForwardTranslucentRegularLitLabel.allLabels();
    ForwardMakeAll.makeSourcesTranslucentRegularLit(
      translucent_regular_lit,
      out_parasol_dir);

    ForwardMakeAll.makeBatch(
      opaque_unlit,
      opaque_lit,
      translucent_regular_unlit,
      translucent_regular_lit,
      out_batch);

    final String[] sources = ForwardMakeAll.getSourcesList(out_parasol_dir);
    ForwardMakeAll.makeCompileSources(
      out_parasol_dir,
      sources,
      out_batch,
      out_glsl_dir,
      out_glsl_compact_dir);
  }

  public static
    void
    makeBatch(
      final Set<KMaterialForwardOpaqueUnlitLabel> opaque_unlit,
      final Set<KMaterialForwardOpaqueLitLabel> opaque_lit,
      final Set<KMaterialForwardTranslucentRegularUnlitLabel> translucent_regular_unlit,
      final Set<KMaterialForwardTranslucentRegularLitLabel> translucent_regular_lit,
      final @Nonnull File file)
      throws IOException
  {
    final FileWriter writer = new FileWriter(file);

    for (final KMaterialForwardOpaqueUnlitLabel l : opaque_unlit) {
      final String code = l.labelGetCode();
      final String module = TitleCase.toTitleCase(code);
      writer.append(code);
      writer.append(" : com.io7m.renderer.kernel." + module + ".p");
      writer.append("\n");
    }

    for (final KMaterialForwardOpaqueLitLabel l : opaque_lit) {
      final String code = l.labelGetCode();
      final String module = TitleCase.toTitleCase(code);
      writer.append(code);
      writer.append(" : com.io7m.renderer.kernel." + module + ".p");
      writer.append("\n");
    }

    for (final KMaterialForwardTranslucentRegularUnlitLabel l : translucent_regular_unlit) {
      final String code = l.labelGetCode();
      final String module = TitleCase.toTitleCase(code);
      writer.append(code);
      writer.append(" : com.io7m.renderer.kernel." + module + ".p");
      writer.append("\n");
    }

    for (final KMaterialForwardTranslucentRegularLitLabel l : translucent_regular_lit) {
      final String code = l.labelGetCode();
      final String module = TitleCase.toTitleCase(code);
      writer.append(code);
      writer.append(" : com.io7m.renderer.kernel." + module + ".p");
      writer.append("\n");
    }

    writer.close();
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
        final File program_out = new File(out_compact_dir, k.first);
        System.out.println("info: compact " + program_in);
        PGLSLCompactor.newCompactor(
          program_in,
          program_out,
          ForwardMakeAll.getLog());
      }
    }
  }

  private static void makeSourcesOpaqueLit(
    final @Nonnull Set<KMaterialForwardOpaqueLitLabel> opaque_lit,
    final @Nonnull File dir)
    throws IOException
  {
    if (dir.isDirectory() == false) {
      throw new IOException(dir + " is not a directory");
    }

    for (final KMaterialForwardOpaqueLitLabel l : opaque_lit) {
      final String code = TitleCase.toTitleCase(l.labelGetCode());

      final File file = new File(dir, code + ".p");
      System.err.println("info: writing: " + file);

      final FileWriter writer = new FileWriter(file);
      try {
        writer.append(ForwardShaders.moduleForwardOpaqueLit(l));
      } finally {
        writer.flush();
        writer.close();
      }
    }
  }

  private static void makeSourcesOpaqueUnlit(
    final @Nonnull Set<KMaterialForwardOpaqueUnlitLabel> opaque_unlit,
    final @Nonnull File dir)
    throws IOException
  {
    if (dir.isDirectory() == false) {
      throw new IOException(dir + " is not a directory");
    }

    for (final KMaterialForwardOpaqueUnlitLabel l : opaque_unlit) {
      final String code = TitleCase.toTitleCase(l.labelGetCode());

      final File file = new File(dir, code + ".p");
      System.err.println("info: writing: " + file);

      final FileWriter writer = new FileWriter(file);
      try {
        writer.append(ForwardShaders.moduleForwardOpaqueUnlit(l));
      } finally {
        writer.flush();
        writer.close();
      }
    }
  }

  private static
    void
    makeSourcesTranslucentRegularLit(
      final @Nonnull Set<KMaterialForwardTranslucentRegularLitLabel> opaque_lit,
      final @Nonnull File dir)
      throws IOException
  {
    if (dir.isDirectory() == false) {
      throw new IOException(dir + " is not a directory");
    }

    for (final KMaterialForwardTranslucentRegularLitLabel l : opaque_lit) {
      final String code = TitleCase.toTitleCase(l.labelGetCode());

      final File file = new File(dir, code + ".p");
      System.err.println("info: writing: " + file);

      final FileWriter writer = new FileWriter(file);
      try {
        writer.append(ForwardShaders.moduleForwardTranslucentRegularLit(l));
      } finally {
        writer.flush();
        writer.close();
      }
    }
  }

  private static
    void
    makeSourcesTranslucentRegularUnlit(
      final @Nonnull Set<KMaterialForwardTranslucentRegularUnlitLabel> opaque_unlit,
      final @Nonnull File dir)
      throws IOException
  {
    if (dir.isDirectory() == false) {
      throw new IOException(dir + " is not a directory");
    }

    for (final KMaterialForwardTranslucentRegularUnlitLabel l : opaque_unlit) {
      final String code = TitleCase.toTitleCase(l.labelGetCode());

      final File file = new File(dir, code + ".p");
      System.err.println("info: writing: " + file);

      final FileWriter writer = new FileWriter(file);
      try {
        writer.append(ForwardShaders.moduleForwardTranslucentRegularUnlit(l));
      } finally {
        writer.flush();
        writer.close();
      }
    }
  }
}
