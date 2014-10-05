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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.SortedSet;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import com.io7m.jfunctional.None;
import com.io7m.jfunctional.OptionPartialVisitorType;
import com.io7m.jfunctional.OptionType;
import com.io7m.jfunctional.Some;
import com.io7m.jlog.LogUsableType;
import com.io7m.r1.meshes.RMeshBasic;
import com.io7m.r1.meshes.RMeshTangents;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RExceptionIO;
import com.io7m.r1.types.RExceptionMeshNonexistent;
import com.io7m.r1.types.RXMLException;
import com.io7m.r1.xml.collada.RColladaDocument;
import com.io7m.r1.xml.collada.RColladaGeometry;
import com.io7m.r1.xml.collada.RColladaGeometryID;
import com.io7m.r1.xml.collada.RColladaPolylist;
import com.io7m.r1.xml.collada.RColladaGeometry.RColladaMesh;
import com.io7m.r1.xml.collada.tools.RColladaToMeshBasic;

/**
 * COLLADA mesh importer.
 */

public final class RMeshToolImporterCOLLADA implements RMeshToolImporterType
{
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

  /**
   * Construct an importer.
   */

  public RMeshToolImporterCOLLADA()
  {
    // Nothing.
  }

  @Override public String importerGetHumanName()
  {
    return "COLLADA 1.4";
  }

  @Override public String importerGetShortName()
  {
    return "collada";
  }

  @Override public String importerGetSuffix()
  {
    return "dae";
  }

  @Override public RMeshTangents importFile(
    final File input,
    final String mesh_name,
    final OptionType<String> mesh_change_name,
    final LogUsableType log)
    throws RException
  {
    try {
      final Document doc = RMeshToolImporterCOLLADA.getDocument(input);
      doc.setBaseURI(input.toURI().toString());

      final RColladaDocument cdoc = RColladaDocument.newDocument(doc, log);
      final RColladaGeometryID geom_name = new RColladaGeometryID(mesh_name);
      final RColladaGeometry geo = cdoc.getGeometry(geom_name);

      if (geo == null) {
        final String r = String.format("Mesh %s does not exist", mesh_name);
        assert r != null;
        throw new RExceptionMeshNonexistent(r);
      }

      final RColladaToMeshBasic importer = new RColladaToMeshBasic(log);

      final RMeshBasic basic =
        mesh_change_name
          .acceptPartial(new OptionPartialVisitorType<String, RMeshBasic, RException>() {
            @Override public RMeshBasic none(
              final None<String> _)
              throws RException
            {
              return importer.newMeshFromColladaGeometry(cdoc, geo);
            }

            @Override public RMeshBasic some(
              final Some<String> s)
              throws RException
            {
              return importer.newMeshFromColladaGeometryWithName(
                cdoc,
                geo,
                s.get());
            }
          });

      return RMeshTangents.makeWithTangents(basic);
    } catch (final ValidityException e) {
      throw RXMLException.validityException(e);
    } catch (final ParsingException e) {
      throw RXMLException.parsingException(e);
    } catch (final IOException e) {
      throw RExceptionIO.fromIOException(e);
    }
  }

  @Override public void showFile(
    final File file,
    final PrintWriter output,
    final LogUsableType log)
    throws RException
  {
    try {
      final Document doc = RMeshToolImporterCOLLADA.getDocument(file);
      doc.setBaseURI(file.toURI().toString());

      final RColladaDocument cdoc = RColladaDocument.newDocument(doc, log);
      final SortedSet<RColladaGeometryID> geoms = cdoc.getGeometryIDs();
      for (final RColladaGeometryID gid : geoms) {
        assert gid != null;
        final RColladaGeometry g = cdoc.getGeometry(gid);
        assert g != null;

        switch (g.getType()) {
          case GEOMETRY_MESH:
          {
            final RColladaMesh m = (RColladaMesh) g;
            final RColladaPolylist polys = m.getPolylist();

            /*
             * TODO: Presumably there is a reasonable way to get the number of
             * vertices without doing a full import of each mesh?
             */

            output.printf(
              "%s : %d triangles : ? vertices\n",
              gid.getActual(),
              polys.getPolygons().size());
            break;
          }
        }
      }
    } catch (final ValidityException e) {
      throw RXMLException.validityException(e);
    } catch (final ParsingException e) {
      throw RXMLException.parsingException(e);
    } catch (final IOException e) {
      throw RExceptionIO.fromIOException(e);
    }
  }
}
