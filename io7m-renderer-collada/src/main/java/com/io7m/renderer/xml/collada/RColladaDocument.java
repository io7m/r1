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

package com.io7m.renderer.xml.collada;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.TreeSet;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Nodes;
import nu.xom.XPathContext;

import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.jranges.RangeCheckException;
import com.io7m.junreachable.UnimplementedCodeException;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.types.RXMLException;
import com.io7m.renderer.xml.RXMLUtilities;
import com.io7m.renderer.xml.collada.RColladaSource.ColladaSourceArray2F;
import com.io7m.renderer.xml.collada.RColladaSource.ColladaSourceArray3F;
import com.io7m.renderer.xml.collada.RColladaSource.ColladaVertices;
import com.io7m.renderer.xml.collada.RColladaSource.Type;

/**
 * A loaded COLLADA document.
 */

@EqualityReference public final class RColladaDocument
{
  /**
   * The COLLADA namespace URI.
   */

  public static final URI COLLADA_URI;

  static {
    try {
      COLLADA_URI = new URI("http://www.collada.org/2005/11/COLLADASchema");
    } catch (final URISyntaxException e) {
      throw new UnreachableCodeException();
    }
  }

  private static NavigableSet<RColladaSourceID> geometryLoadSourceIDs(
    final Elements e)
    throws RXMLException
  {
    final NavigableSet<RColladaSourceID> source_ids =
      new TreeSet<RColladaSourceID>();
    for (int index = 0; index < e.size(); ++index) {
      final Element es = e.get(index);
      assert es != null;
      RXMLUtilities.checkIsElement(es, "source", RColladaDocument.COLLADA_URI);

      final RColladaSourceID sid =
        new RColladaSourceID(
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
    RXMLUtilities.checkIsElement(e, "vertices", RColladaDocument.COLLADA_URI);

    final RColladaAxis axis = RColladaDocument.getNearestAxis(e, log, xpc);

    final RColladaSourceID si =
      new RColladaSourceID(
        RXMLUtilities.getAttributeNonEmptyString(RXMLUtilities
          .getAttributeInDefaultNamespace(e, "id")));

    final Elements ei =
      RXMLUtilities.getChildren(e, "input", RColladaDocument.COLLADA_URI);

    final List<RColladaInput> inputs = new ArrayList<RColladaInput>();
    for (int index = 0; index < ei.size(); ++index) {
      final Element ie = ei.get(index);
      assert ie != null;
      final RColladaInput i = RColladaDocument.parseInput(ie);
      inputs.add(i);
    }

    return new ColladaVertices(si, inputs, axis);
  }

  private static RColladaAxis getNearestAxis(
    final Element e,
    final LogUsableType log,
    final XPathContext xpc)
  {
    final Nodes nodes = e.query("ancestor::node()/c:asset/c:up_axis", xpc);
    final String name = nodes.get(0).getValue();

    log.debug("Retrieved axis " + name);

    if ("UP_X".equals(name)) {
      return RColladaAxis.COLLADA_AXIS_X_UP;
    }
    if ("UP_Y".equals(name)) {
      return RColladaAxis.COLLADA_AXIS_Y_UP;
    }
    return RColladaAxis.COLLADA_AXIS_Z_UP;
  }

  /**
   * Load a COLLADA document from the given document (which is expected to be
   * schema-valid with respect to the COLLADA schema).
   * 
   * @param document
   *          The document.
   * @param log
   *          A log interface.
   * @return A COLLADA document.
   * @throws RXMLException
   *           If an XML error occurs.
   */

  public static RColladaDocument newDocument(
    final Document document,
    final LogUsableType log)
    throws RXMLException
  {
    return new RColladaDocument(document, log);
  }

  private static RColladaInput parseInput(
    final Element ie)
    throws RXMLException
  {
    RXMLUtilities.checkIsElement(ie, "input", RColladaDocument.COLLADA_URI);

    final String ss =
      RXMLUtilities
        .getAttributeNonEmptyString(
          RXMLUtilities.getAttributeInDefaultNamespace(ie, "source"))
        .substring(1);
    assert ss != null;

    final RColladaSourceID source = new RColladaSourceID(ss);
    final RColladaSemantic s =
      RColladaSemantic.fromName(RXMLUtilities
        .getAttributeNonEmptyString(RXMLUtilities
          .getAttributeInDefaultNamespace(ie, "semantic")));

    final Attribute oa = ie.getAttribute("offset");
    final int offset =
      (oa != null) ? RXMLUtilities.getAttributeInteger(oa) : 0;

    return new RColladaInput(source, s, offset);
  }

  private static List<RColladaInput> parseInputs(
    final Element e)
    throws RXMLException
  {
    final List<RColladaInput> inputs = new ArrayList<RColladaInput>();
    final Elements eis =
      RXMLUtilities.getChildren(e, "input", RColladaDocument.COLLADA_URI);
    for (int index = 0; index < eis.size(); ++index) {
      final Element ei = eis.get(index);
      assert ei != null;
      final RColladaInput i = RColladaDocument.parseInput(ei);
      inputs.add(i);
    }
    return inputs;
  }

  private static RColladaSource sourceLoadFloatArray2f(
    final RColladaSourceID id,
    final Element ef,
    final int element_count,
    final RColladaAxis axis)
  {
    final String[] efs = ef.getValue().split("\\s+");
    final ColladaSourceArray2F source =
      new RColladaSource.ColladaSourceArray2F(id, axis);
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

  private static RColladaSource sourceLoadFloatArray3f(
    final RColladaSourceID id,
    final Element ef,
    final RColladaAxis axis,
    final int element_count)
  {
    final String[] efs = ef.getValue().split("\\s+");
    final ColladaSourceArray3F source =
      new RColladaSource.ColladaSourceArray3F(id, axis);
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

  private final XPathContext                            xpc;
  private final StringBuilder                           message;
  private final LogUsableType                           log;
  private final Map<RColladaSourceID, RColladaSource>     sources;
  private final Map<RColladaGeometryID, RColladaGeometry> geometries;
  private final Document                                document;
  private final LogUsableType                           log_source;
  private final LogUsableType                           log_geometry;

  private RColladaDocument(
    final Document in_document,
    final LogUsableType in_log)
    throws RXMLException
  {
    this.document = NullCheck.notNull(in_document, "Document");
    this.message = new StringBuilder();
    this.xpc = new XPathContext("c", RColladaDocument.COLLADA_URI.toString());

    this.log = NullCheck.notNull(in_log, "Log").with("collada-parser");
    this.log_source = this.log.with("source");
    this.log_geometry = this.log.with("geometry");

    this.log.debug("Initialized document '" + in_document.getBaseURI() + "'");
    this.sources = new HashMap<RColladaSourceID, RColladaSource>();
    this.geometries = new HashMap<RColladaGeometryID, RColladaGeometry>();
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
      assert e != null;
      this.geometryLoad(e);
    }
  }

  private void geometryAdd(
    final RColladaGeometryID id,
    final RColladaGeometry cg)
  {
    {
      this.message.setLength(0);
      this.message.append("Loaded geometry ");
      this.message.append(id);
      this.message.append(": ");
      this.message.append(cg);
      final String r = this.message.toString();
      assert r != null;
      this.log_geometry.debug(r);
    }

    this.geometries.put(id, cg);
  }

  private void geometryLoad(
    final Element e)
    throws RXMLException
  {
    RXMLUtilities.checkIsElement(e, "geometry", RColladaDocument.COLLADA_URI);

    final RColladaGeometryID id =
      new RColladaGeometryID(
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

    final RColladaAxis axis =
      RColladaDocument.getNearestAxis(e, this.log_geometry, this.xpc);
    final Element em =
      RXMLUtilities.getChild(e, "mesh", RColladaDocument.COLLADA_URI);
    final Elements ess =
      RXMLUtilities.getChildren(em, "source", RColladaDocument.COLLADA_URI);
    final SortedSet<RColladaSourceID> source_ids =
      RColladaDocument.geometryLoadSourceIDs(ess);
    final Element ev =
      RXMLUtilities.getChild(em, "vertices", RColladaDocument.COLLADA_URI);
    final RColladaSource.ColladaVertices v =
      RColladaDocument.geometryLoadVertices(ev, this.log_geometry, this.xpc);
    this.sourceAdd(v.getID(), v);

    /**
     * XXX: This implementation only supports a single polylist.
     */

    final Element ep =
      RXMLUtilities.getChild(em, "polylist", RColladaDocument.COLLADA_URI);
    final RColladaPolylist p = this.geometryLoadPolylist(ep);

    final RColladaGeometry cg =
      new RColladaGeometry.ColladaMesh(id, this, source_ids, p, axis);
    this.geometryAdd(id, cg);
  }

  private RColladaPolylist geometryLoadPolylist(
    final Element e)
    throws RXMLException
  {
    try {
      RXMLUtilities
        .checkIsElement(e, "polylist", RColladaDocument.COLLADA_URI);

      final List<RColladaInput> inputs = RColladaDocument.parseInputs(e);

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
        RXMLUtilities.getChild(e, "vcount", RColladaDocument.COLLADA_URI);
      final Element p =
        RXMLUtilities.getChild(e, "p", RColladaDocument.COLLADA_URI);

      final String[] vcs = vc.getValue().split("\\s+");
      final String[] ps = p.getValue().split("\\s+");

      /**
       * Each vcount element specifies the number of vertices in the current
       * polygon. The number of inputs gives the number of indices per vertex.
       */

      final int indices_per_vertex = inputs.size();
      final List<RColladaPoly> polygons = new ArrayList<RColladaPoly>();

      int poly_offset = 0;
      for (int index = 0; index < vcs.length; ++index) {
        final int vcount = Integer.valueOf(vcs[index]).intValue();

        final List<RColladaVertex> vertices = new ArrayList<RColladaVertex>();
        for (int v = 0; v < vcount; ++v) {
          final int vertex_offset = poly_offset + (v * indices_per_vertex);
          final List<Integer> indices = new ArrayList<Integer>();
          for (int i = 0; i < indices_per_vertex; ++i) {
            final Integer value = Integer.valueOf(ps[vertex_offset + i]);
            indices.add(value);
          }
          vertices.add(new RColladaVertex(indices));
        }

        polygons.add(new RColladaPoly(vertices));
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

      return new RColladaPolylist(inputs, polygons);
    } catch (final NumberFormatException x) {
      this.message.setLength(0);
      this.message
        .append("Could not parse element in indices array of polylist");
      final String r = this.message.toString();
      assert r != null;
      throw new RXMLException.RXMLExceptionNumberFormatError(x, r);
    }
  }

  /**
   * @param id
   *          The identifier.
   * @return The geometry with the given <code>id</code>, or <code>null</code>
   *         if it does not exist.
   */

  public @Nullable RColladaGeometry getGeometry(
    final RColladaGeometryID id)
  {
    NullCheck.notNull(id, "ID");
    return this.geometries.get(id);
  }

  /**
   * @return The set of geometry IDs in the document.
   */

  public NavigableSet<RColladaGeometryID> getGeometryIDs()
  {
    return new TreeSet<RColladaGeometryID>(this.geometries.keySet());
  }

  /**
   * @param id
   *          The identifier.
   * @return The source with the given <code>id</code>, or <code>null</code>
   *         if it does not exist.
   */

  public @Nullable RColladaSource getSource(
    final RColladaSourceID id)
  {
    NullCheck.notNull(id, "ID");
    return this.sources.get(id);
  }

  /**
   * @return The set of source IDs in the document.
   */

  public NavigableSet<RColladaSourceID> getSourceIDs()
  {
    return new TreeSet<RColladaSourceID>(this.sources.keySet());
  }

  private void sourceAdd(
    final RColladaSourceID id,
    final RColladaSource source)
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

  private RColladaSource sourceLoadFloatArray(
    final RColladaSourceID id,
    final Element es,
    final RColladaAxis axis)
    throws RXMLException
  {
    try {
      final Element et =
        RXMLUtilities.getChild(
          es,
          "technique_common",
          RColladaDocument.COLLADA_URI);
      final Element ef =
        RXMLUtilities
          .getChild(es, "float_array", RColladaDocument.COLLADA_URI);
      final Element ea =
        RXMLUtilities.getChild(et, "accessor", RColladaDocument.COLLADA_URI);

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
          .getChildren(ea, "param", RColladaDocument.COLLADA_URI)
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
          type = RColladaSource.Type.SOURCE_TYPE_VECTOR_2F;
          break;
        }
        case 3:
        {
          type = RColladaSource.Type.SOURCE_TYPE_VECTOR_3F;
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
          return RColladaDocument.sourceLoadFloatArray2f(
            id,
            ef,
            element_count,
            axis);
        }
        case SOURCE_TYPE_VECTOR_3F:
        {
          return RColladaDocument.sourceLoadFloatArray3f(
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
      assert es != null;

      final RColladaSourceID id =
        new RColladaSourceID(
          RXMLUtilities.getAttributeNonEmptyString(RXMLUtilities
            .getAttributeInDefaultNamespace(es, "id")));

      final RColladaAxis axis =
        RColladaDocument.getNearestAxis(es, this.log_source, this.xpc);

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
        RColladaDocument.COLLADA_URI.toString()).size() == 1) {
        final RColladaSource source = this.sourceLoadFloatArray(id, es, axis);
        this.sourceAdd(id, source);
      } else {
        throw new UnimplementedCodeException();
      }
    }
  }
}
