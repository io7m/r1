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

package com.io7m.renderer.xml.collada;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Nodes;
import nu.xom.XPathContext;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnimplementedCodeException;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jlog.Log;
import com.io7m.renderer.xml.RXMLException;
import com.io7m.renderer.xml.RXMLUtilities;
import com.io7m.renderer.xml.collada.ColladaSource.ColladaSourceArray2F;
import com.io7m.renderer.xml.collada.ColladaSource.ColladaSourceArray3F;
import com.io7m.renderer.xml.collada.ColladaSource.ColladaVertices;
import com.io7m.renderer.xml.collada.ColladaSource.Type;

public final class ColladaDocument
{
  public static final @Nonnull URI COLLADA_URI;

  static {
    try {
      COLLADA_URI = new URI("http://www.collada.org/2005/11/COLLADASchema");
    } catch (final URISyntaxException e) {
      throw new UnreachableCodeException();
    }
  }

  private static @Nonnull SortedSet<ColladaSourceID> geometryLoadSourceIDs(
    final @Nonnull Elements e)
    throws ConstraintError,
      RXMLException
  {
    final TreeSet<ColladaSourceID> source_ids =
      new TreeSet<ColladaSourceID>();
    for (int index = 0; index < e.size(); ++index) {
      final Element es = e.get(index);
      RXMLUtilities.checkIsElement(es, "source", ColladaDocument.COLLADA_URI);

      final ColladaSourceID sid =
        new ColladaSourceID(
          RXMLUtilities.getAttributeNonEmptyString(RXMLUtilities
            .getAttributeInDefaultNamespace(es, "id")));
      source_ids.add(sid);
    }
    return source_ids;
  }

  private static @Nonnull ColladaVertices geometryLoadVertices(
    final @Nonnull Element e,
    final @Nonnull Log log,
    final @Nonnull XPathContext xpc)
    throws RXMLException,
      ConstraintError
  {
    RXMLUtilities.checkIsElement(e, "vertices", ColladaDocument.COLLADA_URI);

    final ColladaAxis axis = ColladaDocument.getNearestAxis(e, log, xpc);

    final ColladaSourceID si =
      new ColladaSourceID(
        RXMLUtilities.getAttributeNonEmptyString(RXMLUtilities
          .getAttributeInDefaultNamespace(e, "id")));

    final Elements ei =
      RXMLUtilities.getChildren(e, "input", ColladaDocument.COLLADA_URI);

    final ArrayList<ColladaInput> inputs = new ArrayList<ColladaInput>();
    for (int index = 0; index < ei.size(); ++index) {
      final Element ie = ei.get(index);
      final ColladaInput i = ColladaDocument.parseInput(ie);
      inputs.add(i);
    }

    return new ColladaVertices(si, inputs, axis);
  }

  private static @Nonnull ColladaAxis getNearestAxis(
    final @Nonnull Element e,
    final @Nonnull Log log,
    final @Nonnull XPathContext xpc)
  {
    final Nodes nodes = e.query("ancestor::node()/c:asset/c:up_axis", xpc);
    final String name = nodes.get(0).getValue();

    log.debug("Retrieved axis " + name);

    if (name.equals("UP_X")) {
      return ColladaAxis.COLLADA_AXIS_X_UP;
    }
    if (name.equals("UP_Y")) {
      return ColladaAxis.COLLADA_AXIS_Y_UP;
    }
    return ColladaAxis.COLLADA_AXIS_Z_UP;
  }

  private static @Nonnull Element getVerticesElementForGeometry(
    final @Nonnull Element eg)
    throws RXMLException,
      ConstraintError
  {
    final Element em =
      RXMLUtilities.getChild(eg, "mesh", ColladaDocument.COLLADA_URI);
    final Element ev =
      RXMLUtilities.getChild(em, "vertices", ColladaDocument.COLLADA_URI);
    return ev;
  }

  public static @Nonnull ColladaDocument newDocument(
    final @Nonnull Document document,
    final @Nonnull Log log)
    throws ConstraintError,
      RXMLException
  {
    return new ColladaDocument(document, log);
  }

  private static @Nonnull ColladaInput parseInput(
    final @Nonnull Element ie)
    throws RXMLException,
      ConstraintError
  {
    RXMLUtilities.checkIsElement(ie, "input", ColladaDocument.COLLADA_URI);

    final ColladaSourceID source =
      new ColladaSourceID(RXMLUtilities
        .getAttributeNonEmptyString(
          RXMLUtilities.getAttributeInDefaultNamespace(ie, "source"))
        .substring(1));
    final ColladaSemantic s =
      ColladaSemantic.fromName(RXMLUtilities
        .getAttributeNonEmptyString(RXMLUtilities
          .getAttributeInDefaultNamespace(ie, "semantic")));

    final Attribute oa = ie.getAttribute("offset");
    final int offset =
      (oa != null) ? RXMLUtilities.getAttributeInteger(oa) : 0;

    return new ColladaInput(source, s, offset);
  }

  private static @Nonnull ArrayList<ColladaInput> parseInputs(
    final @Nonnull Element e)
    throws RXMLException,
      ConstraintError
  {
    final ArrayList<ColladaInput> inputs = new ArrayList<ColladaInput>();
    final Elements eis =
      RXMLUtilities.getChildren(e, "input", ColladaDocument.COLLADA_URI);
    for (int index = 0; index < eis.size(); ++index) {
      final Element ei = eis.get(index);
      final ColladaInput i = ColladaDocument.parseInput(ei);
      inputs.add(i);
    }
    return inputs;
  }

  private static @Nonnull ColladaSource sourceLoadFloatArray2f(
    final @Nonnull ColladaSourceID id,
    final @Nonnull Element ef,
    final int element_count,
    final @Nonnull ColladaAxis axis)
    throws ConstraintError
  {
    final String[] efs = ef.getValue().split("\\s+");
    final ColladaSourceArray2F source =
      new ColladaSource.ColladaSourceArray2F(id, axis);
    for (int index = 0; index < efs.length; index += 2) {
      final float x = Float.valueOf(efs[index + 0]).floatValue();
      final float y = Float.valueOf(efs[index + 1]).floatValue();
      source.put2f(x, y);
    }

    Constraints.constrainArbitrary(element_count == source
      .getArray2f()
      .size(), "Correct number of source elements read");

    return source;
  }

  private static @Nonnull ColladaSource sourceLoadFloatArray3f(
    final @Nonnull ColladaSourceID id,
    final @Nonnull Element ef,
    final @Nonnull ColladaAxis axis,
    final int element_count)
    throws ConstraintError
  {
    final String[] efs = ef.getValue().split("\\s+");
    final ColladaSourceArray3F source =
      new ColladaSource.ColladaSourceArray3F(id, axis);
    for (int index = 0; index < efs.length; index += 3) {
      final float x = Float.valueOf(efs[index + 0]).floatValue();
      final float y = Float.valueOf(efs[index + 1]).floatValue();
      final float z = Float.valueOf(efs[index + 2]).floatValue();
      source.put3f(x, y, z);
    }

    Constraints.constrainArbitrary(element_count == source
      .getArray3f()
      .size(), "Correct number of source elements read");

    return source;
  }

  private final @Nonnull XPathContext                                xpc;
  private final @Nonnull StringBuilder                               message;
  private final @Nonnull Log                                         log;
  private final @Nonnull HashMap<ColladaSourceID, ColladaSource>     sources;
  private final @Nonnull HashMap<ColladaGeometryID, ColladaGeometry> geometries;
  private final @Nonnull Document                                    document;
  private final @Nonnull Log                                         log_source;
  private final @Nonnull Log                                         log_geometry;

  private ColladaDocument(
    final @Nonnull Document document,
    final @Nonnull Log log)
    throws ConstraintError,
      RXMLException
  {
    this.document = Constraints.constrainNotNull(document, "Document");
    this.message = new StringBuilder();
    this.xpc = new XPathContext("c", ColladaDocument.COLLADA_URI.toString());
    this.log = new Log(log, "collada-parser");
    this.log_source = new Log(this.log, "source");
    this.log_geometry = new Log(this.log, "geometry");

    this.log.debug("Initialized document '" + document.getBaseURI() + "'");
    this.sources = new HashMap<ColladaSourceID, ColladaSource>();
    this.geometries = new HashMap<ColladaGeometryID, ColladaGeometry>();
    this.sourcesLoad();
    this.geometriesLoad();
  }

  private void geometriesLoad()
    throws ConstraintError,
      RXMLException
  {
    final Nodes nodes =
      this.document.query(
        "/c:COLLADA/c:library_geometries/c:geometry",
        this.xpc);

    {
      this.message.setLength(0);
      this.message.append("Loading ");
      this.message.append(nodes.size());
      this.message.append(" geometries");
      this.log_geometry.debug(this.message.toString());
    }

    for (int index = 0; index < nodes.size(); ++index) {
      final Element e = (Element) nodes.get(index);
      this.geometryLoad(e);
    }
  }

  private void geometryAdd(
    final @Nonnull ColladaGeometryID id,
    final @Nonnull ColladaGeometry cg)
  {
    {
      this.message.setLength(0);
      this.message.append("Loaded geometry ");
      this.message.append(id);
      this.message.append(": ");
      this.message.append(cg);
      this.log_geometry.debug(this.message.toString());
    }

    this.geometries.put(id, cg);
  }

  private void geometryLoad(
    final @Nonnull Element e)
    throws ConstraintError,
      RXMLException
  {
    RXMLUtilities.checkIsElement(e, "geometry", ColladaDocument.COLLADA_URI);

    final ColladaGeometryID id =
      new ColladaGeometryID(
        RXMLUtilities.getAttributeNonEmptyString(RXMLUtilities
          .getAttributeInDefaultNamespace(e, "id")));

    {
      this.message.setLength(0);
      this.message.append("Loading geometry ");
      this.message.append(id);
      this.log_geometry.debug(this.message.toString());
    }

    final ColladaAxis axis =
      ColladaDocument.getNearestAxis(e, this.log_geometry, this.xpc);
    final Element em =
      RXMLUtilities.getChild(e, "mesh", ColladaDocument.COLLADA_URI);
    final Elements ess =
      RXMLUtilities.getChildren(em, "source", ColladaDocument.COLLADA_URI);
    final SortedSet<ColladaSourceID> source_ids =
      ColladaDocument.geometryLoadSourceIDs(ess);
    final Element ev =
      RXMLUtilities.getChild(em, "vertices", ColladaDocument.COLLADA_URI);
    final ColladaSource.ColladaVertices v =
      ColladaDocument.geometryLoadVertices(ev, this.log_geometry, this.xpc);
    this.sourceAdd(v.getID(), v);

    /**
     * XXX: This implementation only supports a single polylist.
     */

    final Element ep =
      RXMLUtilities.getChild(em, "polylist", ColladaDocument.COLLADA_URI);
    final ColladaPolylist p = this.geometryLoadPolylist(ep);

    final ColladaGeometry cg =
      new ColladaGeometry.ColladaMesh(id, this, source_ids, p, axis);
    this.geometryAdd(id, cg);
  }

  private @Nonnull ColladaPolylist geometryLoadPolylist(
    final @Nonnull Element e)
    throws RXMLException,
      ConstraintError
  {
    try {
      RXMLUtilities
        .checkIsElement(e, "polylist", ColladaDocument.COLLADA_URI);

      final ArrayList<ColladaInput> inputs = ColladaDocument.parseInputs(e);

      final int pcount =
        RXMLUtilities.getAttributeInteger(RXMLUtilities
          .getAttributeInDefaultNamespace(e, "count"));

      {
        this.message.setLength(0);
        this.message.append("Expecting ");
        this.message.append(pcount);
        this.message.append(" polygons");
        this.log_geometry.debug(this.message.toString());
      }

      /**
       * XXX: The vcount and p elements are optional. This seems to be a bug
       * in the COLLADA specification (what is the point of a polylist element
       * that doesn't specify polygons?).
       */

      final Element vc =
        RXMLUtilities.getChild(e, "vcount", ColladaDocument.COLLADA_URI);
      final Element p =
        RXMLUtilities.getChild(e, "p", ColladaDocument.COLLADA_URI);

      final String[] vcs = vc.getValue().split("\\s+");
      final String[] ps = p.getValue().split("\\s+");

      /**
       * Each vcount element specifies the number of vertices in the current
       * polygon. The number of inputs gives the number of indices per vertex.
       */

      final int indices_per_vertex = inputs.size();
      final ArrayList<ColladaPoly> polygons = new ArrayList<ColladaPoly>();

      int poly_offset = 0;
      for (int index = 0; index < vcs.length; ++index) {
        final int vcount = Integer.valueOf(vcs[index]).intValue();

        final ArrayList<ColladaVertex> vertices =
          new ArrayList<ColladaVertex>();
        for (int v = 0; v < vcount; ++v) {
          final int vertex_offset = poly_offset + (v * indices_per_vertex);
          final ArrayList<Integer> indices = new ArrayList<Integer>();
          for (int i = 0; i < indices_per_vertex; ++i) {
            final Integer value = Integer.valueOf(ps[vertex_offset + i]);
            indices.add(value);
          }
          vertices.add(new ColladaVertex(indices));
        }

        polygons.add(new ColladaPoly(vertices));
        poly_offset += (vcount * indices_per_vertex);
      }

      Constraints.constrainArbitrary(
        polygons.size() == pcount,
        "Correct number of polygons parsed");

      return new ColladaPolylist(inputs, polygons);
    } catch (final NumberFormatException x) {
      this.message.setLength(0);
      this.message
        .append("Could not parse element in indices array of polylist");
      throw new RXMLException.RXMLExceptionNumberFormatError(
        x,
        this.message.toString());
    }
  }

  public @CheckForNull ColladaGeometry getGeometry(
    final @Nonnull ColladaGeometryID id)
    throws ConstraintError
  {
    Constraints.constrainNotNull(id, "ID");
    return this.geometries.get(id);
  }

  private @Nonnull Element getGeometryElement(
    final @Nonnull ColladaGeometryID id)
    throws ConstraintError
  {
    final StringBuilder query = new StringBuilder();
    query.append("/c:COLLADA/c:library_geometries/c:geometry[@id='");
    query.append(id.getActual());
    query.append("']");

    final Nodes nodes = this.document.query(query.toString(), this.xpc);
    Constraints.constrainArbitrary(
      nodes.size() == 1,
      "Exactly one geometry with id " + id + " exists");

    return (Element) nodes.get(0);
  }

  public @Nonnull SortedSet<ColladaGeometryID> getGeometryIDs()
  {
    return new TreeSet<ColladaGeometryID>(this.geometries.keySet());
  }

  public @CheckForNull ColladaSource getSource(
    final @Nonnull ColladaSourceID id)
    throws ConstraintError
  {
    Constraints.constrainNotNull(id, "ID");
    return this.sources.get(id);
  }

  public @Nonnull SortedSet<ColladaSourceID> getSourceIDs()
  {
    return new TreeSet<ColladaSourceID>(this.sources.keySet());
  }

  private void sourceAdd(
    final @Nonnull ColladaSourceID id,
    final @Nonnull ColladaSource source)
  {
    {
      this.message.setLength(0);
      this.message.append("Loaded source ");
      this.message.append(id);
      this.message.append(": ");
      this.message.append(source);
      this.log_source.debug(this.message.toString());
    }

    this.sources.put(id, source);
  }

  private @Nonnull ColladaSource sourceLoadFloatArray(
    final @Nonnull ColladaSourceID id,
    final @Nonnull Element es,
    final @Nonnull ColladaAxis axis)
    throws RXMLException,
      ConstraintError
  {
    try {
      final Element et =
        RXMLUtilities.getChild(
          es,
          "technique_common",
          ColladaDocument.COLLADA_URI);
      final Element ef =
        RXMLUtilities
          .getChild(es, "float_array", ColladaDocument.COLLADA_URI);
      final Element ea =
        RXMLUtilities.getChild(et, "accessor", ColladaDocument.COLLADA_URI);

      final int element_count =
        RXMLUtilities.getAttributeInteger(RXMLUtilities
          .getAttributeInDefaultNamespace(ea, "count"));

      {
        this.message.setLength(0);
        this.message.append("Expecting ");
        this.message.append(element_count);
        this.message.append(" elements");
        this.log_source.debug(this.message.toString());
      }

      final int param_count =
        RXMLUtilities
          .getChildren(ea, "param", ColladaDocument.COLLADA_URI)
          .size();

      {
        this.message.setLength(0);
        this.message.append("Array consists of groups of ");
        this.message.append(param_count);
        this.message.append(" values");
        this.log_source.debug(this.message.toString());
      }

      Type type;
      switch (param_count) {
        case 2:
        {
          type = ColladaSource.Type.SOURCE_TYPE_VECTOR_2F;
          break;
        }
        case 3:
        {
          type = ColladaSource.Type.SOURCE_TYPE_VECTOR_3F;
          break;
        }
        default:
        {
          throw new UnimplementedCodeException();
        }
      }

      switch (type) {
        case SOURCE_TYPE_VECTOR_2F:
        {
          return ColladaDocument.sourceLoadFloatArray2f(
            id,
            ef,
            element_count,
            axis);
        }
        case SOURCE_TYPE_VECTOR_3F:
        {
          return ColladaDocument.sourceLoadFloatArray3f(
            id,
            ef,
            axis,
            element_count);
        }
        case SOURCE_VERTICES:
        {
          throw new UnreachableCodeException();
        }
      }

      throw new UnreachableCodeException();

    } catch (final NumberFormatException x) {
      this.message.setLength(0);
      this.message
        .append("Could not parse element in float array of source '");
      this.message.append(id);
      this.message.append("'");
      throw new RXMLException.RXMLExceptionNumberFormatError(
        x,
        this.message.toString());
    }
  }

  private void sourcesLoad()
    throws ConstraintError,
      RXMLException
  {
    final Nodes nodes = this.document.query("/c:COLLADA//c:source", this.xpc);

    {
      this.message.setLength(0);
      this.message.append("Loading ");
      this.message.append(nodes.size());
      this.message.append(" sources");
      this.log_source.debug(this.message.toString());
    }

    for (int index = 0; index < nodes.size(); ++index) {
      final Element es = (Element) nodes.get(index);

      final ColladaSourceID id =
        new ColladaSourceID(
          RXMLUtilities.getAttributeNonEmptyString(RXMLUtilities
            .getAttributeInDefaultNamespace(es, "id")));

      final ColladaAxis axis =
        ColladaDocument.getNearestAxis(es, this.log_source, this.xpc);

      {
        this.message.setLength(0);
        this.message.append("Loading source ");
        this.message.append(id);
        this.log_source.debug(this.message.toString());
      }

      if (es.getChildElements(
        "float_array",
        ColladaDocument.COLLADA_URI.toString()).size() == 1) {
        final ColladaSource source = this.sourceLoadFloatArray(id, es, axis);
        this.sourceAdd(id, source);
      } else {
        throw new UnimplementedCodeException();
      }
    }
  }
}
