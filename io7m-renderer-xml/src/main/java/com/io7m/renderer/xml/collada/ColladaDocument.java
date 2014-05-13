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

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Nodes;
import nu.xom.XPathContext;

import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.jranges.RangeCheckException;
import com.io7m.junreachable.UnimplementedCodeException;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.types.RXMLException;
import com.io7m.renderer.xml.RXMLUtilities;
import com.io7m.renderer.xml.collada.ColladaSource.ColladaSourceArray2F;
import com.io7m.renderer.xml.collada.ColladaSource.ColladaSourceArray3F;
import com.io7m.renderer.xml.collada.ColladaSource.ColladaVertices;
import com.io7m.renderer.xml.collada.ColladaSource.Type;

public final class ColladaDocument
{
  public static final URI COLLADA_URI;

  static {
    try {
      COLLADA_URI = new URI("http://www.collada.org/2005/11/COLLADASchema");
    } catch (final URISyntaxException e) {
      throw new UnreachableCodeException();
    }
  }

  private static SortedSet<ColladaSourceID> geometryLoadSourceIDs(
    final Elements e)
    throws RXMLException
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

  private static ColladaVertices geometryLoadVertices(
    final Element e,
    final LogUsableType log,
    final XPathContext xpc)
    throws RXMLException
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

  private static ColladaAxis getNearestAxis(
    final Element e,
    final LogUsableType log,
    final XPathContext xpc)
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

  private static Element getVerticesElementForGeometry(
    final Element eg)
    throws RXMLException
  {
    final Element em =
      RXMLUtilities.getChild(eg, "mesh", ColladaDocument.COLLADA_URI);
    final Element ev =
      RXMLUtilities.getChild(em, "vertices", ColladaDocument.COLLADA_URI);
    return ev;
  }

  public static ColladaDocument newDocument(
    final Document document,
    final LogUsableType log)
    throws RXMLException
  {
    return new ColladaDocument(document, log);
  }

  private static ColladaInput parseInput(
    final Element ie)
    throws RXMLException
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

  private static ArrayList<ColladaInput> parseInputs(
    final Element e)
    throws RXMLException
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

  private static ColladaSource sourceLoadFloatArray2f(
    final ColladaSourceID id,
    final Element ef,
    final int element_count,
    final ColladaAxis axis)
  {
    final String[] efs = ef.getValue().split("\\s+");
    final ColladaSourceArray2F source =
      new ColladaSource.ColladaSourceArray2F(id, axis);
    for (int index = 0; index < efs.length; index += 2) {
      final float x = Float.valueOf(efs[index + 0]).floatValue();
      final float y = Float.valueOf(efs[index + 1]).floatValue();
      source.put2f(x, y);
    }

    if (element_count == source.getArray2f().size() == false) {
      @SuppressWarnings("boxing") final String r =
        String.format(
          "Expected %d elements but got %d",
          element_count,
          source.getArray2f().size());
      assert r != null;
      throw new RangeCheckException(r);
    }

    return source;
  }

  private static ColladaSource sourceLoadFloatArray3f(
    final ColladaSourceID id,
    final Element ef,
    final ColladaAxis axis,
    final int element_count)
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

    if (element_count == source.getArray3f().size() == false) {
      @SuppressWarnings("boxing") final String r =
        String.format(
          "Expected %d elements but got %d",
          element_count,
          source.getArray3f().size());
      assert r != null;
      throw new RangeCheckException(r);
    }

    return source;
  }

  private final XPathContext                                xpc;
  private final StringBuilder                               message;
  private final LogUsableType                               log;
  private final HashMap<ColladaSourceID, ColladaSource>     sources;
  private final HashMap<ColladaGeometryID, ColladaGeometry> geometries;
  private final Document                                    document;
  private final LogUsableType                               log_source;
  private final LogUsableType                               log_geometry;

  private ColladaDocument(
    final Document in_document,
    final LogUsableType in_log)
    throws RXMLException
  {
    this.document = NullCheck.notNull(in_document, "Document");
    this.message = new StringBuilder();
    this.xpc = new XPathContext("c", ColladaDocument.COLLADA_URI.toString());

    this.log = NullCheck.notNull(in_log, "Log").with("collada-parser");
    this.log_source = this.log.with("source");
    this.log_geometry = this.log.with("geometry");

    this.log.debug("Initialized document '" + in_document.getBaseURI() + "'");
    this.sources = new HashMap<ColladaSourceID, ColladaSource>();
    this.geometries = new HashMap<ColladaGeometryID, ColladaGeometry>();
    this.sourcesLoad();
    this.geometriesLoad();
  }

  private void geometriesLoad()
    throws RXMLException
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
      final String r = this.message.toString();
      assert r != null;
      this.log_geometry.debug(r);
    }

    for (int index = 0; index < nodes.size(); ++index) {
      final Element e = (Element) nodes.get(index);
      this.geometryLoad(e);
    }
  }

  private void geometryAdd(
    final ColladaGeometryID id,
    final ColladaGeometry cg)
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
    final Element e)
    throws RXMLException
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
      final String r = this.message.toString();
      assert r != null;
      this.log_geometry.debug(r);
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

  private ColladaPolylist geometryLoadPolylist(
    final Element e)
    throws RXMLException
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
        final String r = this.message.toString();
        assert r != null;
        this.log_geometry.debug(r);
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

      if (polygons.size() != pcount) {
        @SuppressWarnings("boxing") final String s =
          String.format(
            "Incorrect number of polygons parsed. Expected %d but got %d",
            pcount,
            polygons.size());
        assert s != null;
        throw new RangeCheckException(s);
      }

      return new ColladaPolylist(inputs, polygons);
    } catch (final NumberFormatException x) {
      this.message.setLength(0);
      this.message
        .append("Could not parse element in indices array of polylist");
      final String r = this.message.toString();
      assert r != null;
      throw new RXMLException.RXMLExceptionNumberFormatError(x, r);
    }
  }

  public @Nullable ColladaGeometry getGeometry(
    final ColladaGeometryID id)
  {
    NullCheck.notNull(id, "ID");
    return this.geometries.get(id);
  }

  private Element getGeometryElement(
    final ColladaGeometryID id)
  {
    final StringBuilder query = new StringBuilder();
    query.append("/c:COLLADA/c:library_geometries/c:geometry[@id='");
    query.append(id.getActual());
    query.append("']");

    final Nodes nodes = this.document.query(query.toString(), this.xpc);

    if (nodes.size() != 1) {
      throw new IllegalArgumentException(
        "Expected exactly one geometry with id " + id);
    }

    return (Element) nodes.get(0);
  }

  public SortedSet<ColladaGeometryID> getGeometryIDs()
  {
    return new TreeSet<ColladaGeometryID>(this.geometries.keySet());
  }

  public @Nullable ColladaSource getSource(
    final ColladaSourceID id)
  {
    NullCheck.notNull(id, "ID");
    return this.sources.get(id);
  }

  public SortedSet<ColladaSourceID> getSourceIDs()
  {
    return new TreeSet<ColladaSourceID>(this.sources.keySet());
  }

  private void sourceAdd(
    final ColladaSourceID id,
    final ColladaSource source)
  {
    {
      this.message.setLength(0);
      this.message.append("Loaded source ");
      this.message.append(id);
      this.message.append(": ");
      this.message.append(source);
      final String r = this.message.toString();
      assert r != null;
      this.log_source.debug(r);
    }

    this.sources.put(id, source);
  }

  private ColladaSource sourceLoadFloatArray(
    final ColladaSourceID id,
    final Element es,
    final ColladaAxis axis)
    throws RXMLException
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
        final String r = this.message.toString();
        assert r != null;
        this.log_source.debug(r);
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
        final String r = this.message.toString();
        assert r != null;
        this.log_source.debug(r);
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
      final String r = this.message.toString();
      assert r != null;
      throw new RXMLException.RXMLExceptionNumberFormatError(x, r);
    }
  }

  private void sourcesLoad()
    throws RXMLException
  {
    final Nodes nodes = this.document.query("/c:COLLADA//c:source", this.xpc);

    {
      this.message.setLength(0);
      this.message.append("Loading ");
      this.message.append(nodes.size());
      this.message.append(" sources");
      final String r = this.message.toString();
      assert r != null;
      this.log_source.debug(r);
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
        final String r = this.message.toString();
        assert r != null;
        this.log_source.debug(r);
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
