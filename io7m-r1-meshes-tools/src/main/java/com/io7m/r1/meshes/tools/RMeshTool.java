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

package com.io7m.r1.meshes.tools;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import com.io7m.jfunctional.None;
import com.io7m.jfunctional.Option;
import com.io7m.jfunctional.OptionPartialVisitorType;
import com.io7m.jfunctional.OptionType;
import com.io7m.jfunctional.Some;
import com.io7m.jlog.Log;
import com.io7m.jlog.LogPolicyProperties;
import com.io7m.jlog.LogPolicyType;
import com.io7m.jlog.LogUsableType;
import com.io7m.jproperties.JPropertyException;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.meshes.RMeshTangents;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RExceptionMeshTypeUnknown;

/**
 * Command line mesh conversion.
 */

public final class RMeshTool
{
  /**
   * Retrieve a log interface.
   *
   * @param debug
   *          <code>true</code> if debugging should be enabled.
   * @return A log interface.
   */

  public static LogUsableType getLog(
    final boolean debug)
  {
    try {
      final Properties p = new Properties();
      p.setProperty("com.io7m.r1.level", debug ? "LOG_DEBUG" : "LOG_INFO");
      p.setProperty("com.io7m.r1.logs.meshtool", Boolean.toString(debug));

      final LogPolicyType policy =
        LogPolicyProperties.newPolicy(p, "com.io7m.r1");
      return Log.newLog(policy, "meshtool");
    } catch (final JPropertyException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static Options makeOptions()
  {
    final Options os = new Options();
    final OptionGroup g = new OptionGroup();

    {
      OptionBuilder.withLongOpt("help");
      OptionBuilder.withDescription("Show help message");
      os.addOption(OptionBuilder.create());
    }

    {
      OptionBuilder.withLongOpt("show");
      OptionBuilder
        .withDescription("Show information about meshes in the input file");
      OptionBuilder.withArgName("file");
      OptionBuilder.hasArg();
      g.addOption(OptionBuilder.create());
    }

    {
      OptionBuilder.withLongOpt("convert");
      OptionBuilder
        .withDescription("Convert a named mesh in the input file to the specified format and write to the output file");
      OptionBuilder.hasArgs(3);
      OptionBuilder.withValueSeparator(' ');
      OptionBuilder.withArgName("mesh-name> <input-file> <output-file");
      g.addOption(OptionBuilder.create());
    }

    {
      OptionBuilder.withLongOpt("export-name");
      OptionBuilder.withDescription("Set the name of the exported mesh");
      OptionBuilder.withArgName("name");
      OptionBuilder.hasArg();
      os.addOption(OptionBuilder.create());
    }

    {
      OptionBuilder.withLongOpt("debug");
      os.addOption(OptionBuilder.create());
    }

    {
      OptionBuilder.withLongOpt("input-format");
      OptionBuilder.withArgName("format");
      OptionBuilder.hasArg();
      OptionBuilder
        .withDescription("Assume the input file is of the given format");
      os.addOption(OptionBuilder.create());
    }

    {
      OptionBuilder.withLongOpt("output-format");
      OptionBuilder.withArgName("format");
      OptionBuilder.hasArg();
      OptionBuilder
        .withDescription("Require that the output file be of the given format");
      os.addOption(OptionBuilder.create());
    }

    os.addOptionGroup(g);
    return os;
  }

  private final Map<String, RMeshToolExporterType> exporter_suffixes;
  private final Map<String, RMeshToolExporterType> exporters;
  private final Map<String, RMeshToolImporterType> importer_suffixes;
  private final Map<String, RMeshToolImporterType> importers;
  private final Options                            options;

  /**
   * Construct a mesh tool.
   */

  public RMeshTool()
  {
    this.options = RMeshTool.makeOptions();

    this.importers = new HashMap<String, RMeshToolImporterType>();
    this.importers.put("collada", new RMeshToolImporterCOLLADA());
    this.importers.put("rmb", new RMeshToolImporterRMB());
    this.importers.put("rmbz", new RMeshToolImporterRMBZ());
    this.importers.put("rmx", new RMeshToolImporterRMX());
    this.importers.put("rmxz", new RMeshToolImporterRMXZ());

    this.importer_suffixes = new HashMap<String, RMeshToolImporterType>();
    for (final RMeshToolImporterType i : this.importers.values()) {
      assert i != null;
      this.importer_suffixes.put(i.importerGetSuffix(), i);
    }

    this.exporters = new HashMap<String, RMeshToolExporterType>();
    this.exporters.put("rmb", new RMeshToolExporterRMB());
    this.exporters.put("rmbz", new RMeshToolExporterRMBZ());
    this.exporters.put("rmx", new RMeshToolExporterRMX());
    this.exporters.put("rmxz", new RMeshToolExporterRMXZ());

    this.exporter_suffixes = new HashMap<String, RMeshToolExporterType>();
    for (final RMeshToolExporterType i : this.exporters.values()) {
      assert i != null;
      this.exporter_suffixes.put(i.exporterGetSuffix(), i);
    }
  }

  private void commandConvert(
    final LogUsableType log,
    final CommandLine line)
    throws RException
  {
    final String[] args = line.getOptionValues("convert");
    assert args.length == 3;

    final String mesh_name = args[0];
    final File file_in = new File(args[1]);
    final File file_out = new File(args[2]);

    final OptionType<String> want_in =
      Option.of(line.getOptionValue("input-format"));
    final OptionType<String> want_out =
      Option.of(line.getOptionValue("output-format"));

    final RMeshToolImporterType importer =
      this.importerForSuffix(want_in, file_in.toString());
    final RMeshToolExporterType exporter =
      this.exporterForSuffix(want_out, file_out.toString());

    final OptionType<String> mesh_change_name =
      Option.of(line.getOptionValue("export-name"));
    final RMeshTangents mesh =
      importer.importFile(file_in, mesh_name, mesh_change_name, log);
    exporter.exportFile(file_out, mesh, log);
  }

  private void commandShow(
    final LogUsableType log,
    final CommandLine line)
    throws RException
  {
    final PrintWriter out = new PrintWriter(System.out);

    try {
      final String[] args = line.getOptionValues("show");
      assert args.length == 1;
      final String name = args[0];
      final File file = new File(name);

      final OptionType<String> want =
        Option.of(line.getOptionValue("input-format"));
      final RMeshToolImporterType importer =
        this.importerForSuffix(want, name);
      importer.showFile(file, out, log);
      out.flush();
    } finally {
      out.close();
    }
  }

  private RMeshToolExporterType exporterForSuffix(
    final OptionType<String> want,
    final String name)
    throws RException
  {
    final Map<String, RMeshToolExporterType> is = this.exporters;

    return want
      .acceptPartial(new OptionPartialVisitorType<String, RMeshToolExporterType, RException>() {
        @Override public RMeshToolExporterType none(
          final None<String> _)
          throws RException
        {
          for (final String exporter_name : is.keySet()) {
            assert exporter_name != null;
            final RMeshToolExporterType exporter = is.get(exporter_name);
            assert exporter != null;

            if (name.endsWith("." + exporter.exporterGetSuffix())) {
              return exporter;
            }
          }

          final String rs =
            String.format(
              "Could not infer the type of exporter from the filename '%s'",
              name);
          assert rs != null;
          throw new RExceptionMeshTypeUnknown(rs);
        }

        @Override public RMeshToolExporterType some(
          final Some<String> s)
          throws RException
        {
          final String type = s.get();
          if (is.containsKey(type)) {
            final RMeshToolExporterType r = is.get(type);
            assert r != null;
            return r;
          }

          final String rs =
            String.format(
              "There is no supported exporter for the type '%s'",
              type);
          assert rs != null;
          throw new RExceptionMeshTypeUnknown(rs);
        }
      });
  }

  private RMeshToolImporterType importerForSuffix(
    final OptionType<String> want,
    final String name)
    throws RException
  {
    final Map<String, RMeshToolImporterType> is = this.importers;

    return want
      .acceptPartial(new OptionPartialVisitorType<String, RMeshToolImporterType, RException>() {
        @Override public RMeshToolImporterType none(
          final None<String> _)
          throws RException
        {
          for (final String importer_name : is.keySet()) {
            assert importer_name != null;
            final RMeshToolImporterType importer = is.get(importer_name);
            assert importer != null;

            if (name.endsWith("." + importer.importerGetSuffix())) {
              return importer;
            }
          }

          final String rs =
            String.format(
              "Could not infer the type of importer from the filename '%s'",
              name);
          assert rs != null;
          throw new RExceptionMeshTypeUnknown(rs);
        }

        @Override public RMeshToolImporterType some(
          final Some<String> s)
          throws RException
        {
          final String type = s.get();
          if (is.containsKey(type)) {
            final RMeshToolImporterType r = is.get(type);
            assert r != null;
            return r;
          }

          final String rs =
            String.format(
              "There is no supported importer for the type '%s'",
              type);
          assert rs != null;
          throw new RExceptionMeshTypeUnknown(rs);
        }
      });
  }

  /**
   * Run the tool.
   *
   * @param args
   *          Command line arguments.
   * @throws Exception
   *           On errors.
   */

  public void run(
    final String[] args)
    throws Exception
  {
    if (args.length == 0) {
      this.showHelp();
      return;
    }

    final LogUsableType log = RMeshTool.getLog(false);
    this.runWithLog(args, log);
  }

  @SuppressWarnings({ "null" }) private void runWithLog(
    final String[] args,
    final LogUsableType log)
    throws RException
  {
    try {
      final PosixParser parser = new PosixParser();
      final CommandLine line = parser.parse(this.options, args);
      final LogUsableType rlog = RMeshTool.getLog(line.hasOption("debug"));

      if (line.hasOption("help")) {
        this.showHelp();
        return;
      } else if (line.hasOption("show")) {
        this.commandShow(rlog, line);
      } else if (line.hasOption("convert")) {
        this.commandConvert(rlog, line);
      }

    } catch (final ParseException e) {
      System.err.println("error: " + e.getMessage());
      this.showHelp();
    } catch (final RException e) {
      log.error(String.format("%s", e.getMessage()));
      throw e;
    }
  }

  private void showHelp()
  {
    final HelpFormatter formatter = new HelpFormatter();
    final PrintWriter pw = new PrintWriter(System.err);

    pw.println("meshtool: [options] --show input-file");
    pw.println("          [options] --convert name input-file output-file");
    pw.println();
    formatter.printOptions(pw, 120, this.options, 0, 2);
    pw.println();
    pw.println("Supported import formats: ");
    pw.println();

    for (final String name : this.importers.keySet()) {
      final RMeshToolImporterType i = this.importers.get(name);
      pw.printf(
        "   %-8s : %-12s (filename suffix '%s')\n",
        i.importerGetShortName(),
        i.importerGetHumanName(),
        i.importerGetSuffix());
    }

    pw.flush();
  }
}
