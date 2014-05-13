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

package com.io7m.renderer.xml.normal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.SortedSet;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import nu.xom.Serializer;
import nu.xom.ValidityException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import com.io7m.jlog.Log;
import com.io7m.jlog.LogPolicyProperties;
import com.io7m.jlog.LogPolicyType;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.Nullable;
import com.io7m.jproperties.JPropertyException;
import com.io7m.renderer.types.RXMLException;
import com.io7m.renderer.xml.collada.ColladaDocument;
import com.io7m.renderer.xml.collada.ColladaGeometry;
import com.io7m.renderer.xml.collada.ColladaGeometryID;

public final class ColladaToRMX
{
  private static final String PROGRAM = "collada-to-rmx";

  private static class ExportConfig
  {
    public ExportConfig(
      final File in_input,
      final @Nullable File in_output,
      final String in_mesh_name,
      final boolean in_tangents,
      final boolean in_bitangents,
      final @Nullable String in_output_mesh_name)
    {
      this.input = in_input;
      this.output = in_output;
      this.mesh_name = in_mesh_name;
      this.tangents = in_tangents || in_bitangents;
      this.bitangents = in_bitangents;
      this.output_mesh_name = in_output_mesh_name;
    }

    final File             input;
    final @Nullable File   output;
    final String           mesh_name;
    final boolean          tangents;
    final boolean          bitangents;
    final @Nullable String output_mesh_name;
  }

  public static void main(
    final String args[])
    throws JPropertyException
  {
    final Options o = ColladaToRMX.makeOptions();

    try {
      final PosixParser parser = new PosixParser();
      final CommandLine line = parser.parse(o, args);
      final LogUsableType log = ColladaToRMX.getLog(line.hasOption("debug"));

      if (line.hasOption("help")) {
        ColladaToRMX.showHelp(o);
        System.exit(0);
      } else if (line.hasOption("list")) {
        ColladaToRMX.commandListFile(
          log,
          new File(line.getOptionValue("input")));
      } else if (line.hasOption("export")) {
        final File input = new File(line.getOptionValue("input"));

        File output;
        if (line.hasOption("output")) {
          output = new File(line.getOptionValue("output"));
        } else {
          output = null;
        }

        final String mesh_name = line.getOptionValue("export");
        final String output_mesh_name = line.getOptionValue("name");
        final boolean tangents = line.hasOption("tangents");
        final boolean bitangents = line.hasOption("bitangents");

        assert mesh_name != null;
        final ExportConfig config =
          new ExportConfig(
            input,
            output,
            mesh_name,
            tangents,
            bitangents,
            output_mesh_name);
        ColladaToRMX.commandExportFile(log, config);
      }

    } catch (final ParseException e) {
      System.err.println("error: " + e.getMessage());
      ColladaToRMX.showHelp(o);
    } catch (final ValidityException e) {
      System.err.println("fatal: XML validity error: " + e.getMessage());
    } catch (final ParsingException e) {
      System.err.println("fatal: XML well-formedness error: "
        + e.getMessage());
    } catch (final IOException e) {
      System.err.println("fatal: i/o error: " + e.getMessage());
    } catch (final RXMLException e) {
      System.err.println("fatal: XML validity error: " + e.getMessage());
    }
  }

  private static void commandExportFile(
    final LogUsableType log,
    final ExportConfig config)
    throws ValidityException,
      ParsingException,
      IOException,
      RXMLException
  {
    final Document doc = ColladaToRMX.getDocument(config.input);
    doc.setBaseURI(config.input.toURI().toString());

    final ColladaGeometryID gid = new ColladaGeometryID(config.mesh_name);
    final ColladaDocument cdoc = ColladaDocument.newDocument(doc, log);
    final ColladaGeometry geo = cdoc.getGeometry(gid);

    if (geo == null) {
      throw new NoSuchElementException("Mesh '" + gid + "' does not exist");
    }

    final MeshBasicColladaImporter importer =
      new MeshBasicColladaImporter(log);

    MeshBasic basic = null;
    final String m_name = config.output_mesh_name;
    if (m_name != null) {
      log.debug("Using mesh name '" + m_name + "'");
      basic = importer.newMeshFromColladaGeometryWithName(cdoc, geo, m_name);
    } else {
      basic = importer.newMeshFromColladaGeometry(cdoc, geo);
    }

    Element x = null;
    if (config.tangents) {
      log.debug("Generating tangents");
      final MeshTangents tan = MeshTangents.makeWithTangents(basic);
      final MeshTangentsRMXExporter exporter =
        new MeshTangentsRMXExporter(log);
      if (config.bitangents) {
        log.debug("Including bitangents in output mesh");
      } else {
        log.debug("Excluding bitangents from output mesh");
      }
      x = exporter.toXML(tan, config.bitangents);
    } else {
      final MeshBasicRMXExporter exporter = new MeshBasicRMXExporter(log);
      x = exporter.toXML(basic);
    }

    @SuppressWarnings("resource") final OutputStream output_stream =
      (config.output != null)
        ? new FileOutputStream(config.output)
        : System.out;

    final Serializer s = new Serializer(output_stream);
    s.setIndent(2);
    s.setLineSeparator("\n");
    s.setMaxLength(80);
    s.write(new Document(x));

    output_stream.flush();
    if (config.output != null) {
      output_stream.close();
    }
  }

  private static void commandListFile(
    final LogUsableType log,
    final File input)
    throws ValidityException,
      ParsingException,
      IOException,
      RXMLException
  {
    final Document doc = ColladaToRMX.getDocument(input);
    doc.setBaseURI(input.toURI().toString());

    final ColladaDocument cdoc = ColladaDocument.newDocument(doc, log);
    final SortedSet<ColladaGeometryID> geoms = cdoc.getGeometryIDs();
    for (final ColladaGeometryID g : geoms) {
      System.out.println(g);
    }
  }

  private static Document getDocument(
    final File input)
    throws ValidityException,
      ParsingException,
      IOException
  {
    final FileInputStream stream = new FileInputStream(input);
    final Builder b = new Builder();
    final Document doc = b.build(stream);
    assert doc != null;
    stream.close();
    return doc;
  }

  public static LogUsableType getLog(
    final boolean debug)
    throws JPropertyException
  {
    final Properties p = new Properties();
    p.setProperty("com.io7m.renderer.level.collada-to-rmx", "LOG_DEBUG");
    p.setProperty(
      "com.io7m.renderer.logs.collada-to-rmx",
      Boolean.toString(debug));

    final LogPolicyType policy =
      LogPolicyProperties.newPolicy(p, "com.io7m.renderer");
    return Log.newLog(policy, "collada-to-rmx");
  }

  private static Options makeOptions()
  {
    final Options os = new Options();
    os.addOption("h", "help", false, "Show this message");

    final OptionGroup g = new OptionGroup();

    {
      final Option o =
        new Option("l", "list", false, "List meshes in COLLADA file");
      g.addOption(o);
    }

    {
      final Option o =
        new Option("n", "name", true, "Set the name of the exported RMX mesh");
      o.setArgName("name");
      os.addOption(o);
    }

    {
      final Option o =
        new Option("d", "debug", false, "Enable debug logging");
      os.addOption(o);
    }

    {
      final Option o =
        new Option(
          "t",
          "tangents",
          false,
          "Generate and output per-vertex tangents");
      os.addOption(o);
    }

    {
      final Option o =
        new Option(
          "b",
          "bitangents",
          false,
          "Generate and output per-vertex bitangents (implies tangent generation)");
      os.addOption(o);
    }

    {
      final Option o =
        new Option("i", "input", true, "Use the given COLLADA file as input");
      o.setArgName("file.dae");
      o.setRequired(true);
      os.addOption(o);
    }

    {
      final Option o =
        new Option(
          "o",
          "output",
          true,
          "Export the RMX mesh to the given file (default is stdout)");
      o.setArgName("file.rmx");
      os.addOption(o);
    }

    {
      final Option o =
        new Option("e", "export", true, "Export named mesh from COLLADA file");
      o.setArgName("mesh-id");
      g.addOption(o);
    }

    g.setRequired(true);
    os.addOptionGroup(g);
    return os;
  }

  private static void showHelp(
    final Options o)
  {
    final HelpFormatter formatter = new HelpFormatter();
    final PrintWriter pw = new PrintWriter(System.err);

    formatter.printHelp(
      pw,
      120,
      ColladaToRMX.PROGRAM,
      null,
      o,
      4,
      4,
      null,
      true);
    pw.flush();
  }
}
