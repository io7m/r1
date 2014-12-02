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

package com.io7m.r1.xml.collada.tools;

import java.util.List;

import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.jranges.RangeCheckException;
import com.io7m.jtensors.MatrixM3x3F;
import com.io7m.jtensors.VectorI2F;
import com.io7m.jtensors.VectorI3F;
import com.io7m.jtensors.parameterized.PVectorI2F;
import com.io7m.jtensors.parameterized.PVectorI3F;
import com.io7m.r1.meshes.RMeshBasic;
import com.io7m.r1.types.RExceptionMeshMissingNormals;
import com.io7m.r1.types.RExceptionMeshMissingPositions;
import com.io7m.r1.types.RExceptionMeshMissingUVs;
import com.io7m.r1.types.RExceptionMeshNameInvalid;
import com.io7m.r1.types.RSpaceObjectType;
import com.io7m.r1.types.RSpaceTextureType;
import com.io7m.r1.xml.collada.RColladaAxis;
import com.io7m.r1.xml.collada.RColladaDocument;
import com.io7m.r1.xml.collada.RColladaGeometry;
import com.io7m.r1.xml.collada.RColladaGeometry.RColladaMesh;
import com.io7m.r1.xml.collada.RColladaGeometryID;
import com.io7m.r1.xml.collada.RColladaInput;
import com.io7m.r1.xml.collada.RColladaPoly;
import com.io7m.r1.xml.collada.RColladaPolylist;
import com.io7m.r1.xml.collada.RColladaSemantic;
import com.io7m.r1.xml.collada.RColladaSource;
import com.io7m.r1.xml.collada.RColladaSource.ColladaSourceArray2F;
import com.io7m.r1.xml.collada.RColladaSource.ColladaSourceArray3F;
import com.io7m.r1.xml.collada.RColladaSource.ColladaVertices;
import com.io7m.r1.xml.collada.RColladaVertex;

/**
 * An importer that can load a {@link RMeshBasic} from a COLLADA document.
 */

@EqualityReference public final class RColladaToMeshBasic
{
  @EqualityReference static class Offsets
  {
    private static int getNormalOffset(
      final List<RColladaInput> inputs)
    {
      for (int index = 0; index < inputs.size(); ++index) {
        if (inputs.get(index).getSemantic() == RColladaSemantic.SEMANTIC_NORMAL) {
          return index;
        }
      }
      return -1;
    }

    private static int getPositionOffset(
      final List<RColladaInput> inputs)
    {
      for (int index = 0; index < inputs.size(); ++index) {
        if (inputs.get(index).getSemantic() == RColladaSemantic.SEMANTIC_VERTEX) {
          return index;
        }
      }
      return -1;
    }

    private static int getUVOffset(
      final List<RColladaInput> inputs)
    {
      for (int index = 0; index < inputs.size(); ++index) {
        if (inputs.get(index).getSemantic() == RColladaSemantic.SEMANTIC_TEXCOORD) {
          return index;
        }
      }
      return -1;
    }

    // CHECKSTYLE_VISIBILITY:OFF
    final int position_index;
    final int normal_index;
    final int texture_index;

    // CHECKSTYLE_VISIBILITY:ON

    Offsets(
      final RColladaGeometryID geom_id,
      final List<RColladaInput> inputs)
      throws RExceptionMeshMissingPositions,
        RExceptionMeshMissingNormals,
        RExceptionMeshMissingUVs
    {
      this.position_index = Offsets.getPositionOffset(inputs);
      if (this.position_index < 0) {
        final StringBuilder m = new StringBuilder();
        m.append("The mesh '");
        m.append(geom_id.getActual());
        m.append("' is missing required vertex position data");
        final String r = m.toString();
        assert r != null;
        throw new RExceptionMeshMissingPositions(r);
      }

      this.normal_index = Offsets.getNormalOffset(inputs);
      if (this.normal_index < 0) {
        final StringBuilder m = new StringBuilder();
        m.append("The mesh '");
        m.append(geom_id.getActual());
        m.append("' is missing required vertex normal vectors");
        final String r = m.toString();
        assert r != null;
        throw new RExceptionMeshMissingNormals(r);
      }

      this.texture_index = Offsets.getUVOffset(inputs);
      if (this.texture_index < 0) {
        final StringBuilder m = new StringBuilder();
        m.append("The mesh '");
        m.append(geom_id.getActual());
        m.append("' is missing required vertex UV coordinates");
        final String r = m.toString();
        assert r != null;
        throw new RExceptionMeshMissingUVs(r);
      }
    }
  }

  private static void loadColladaPolygons(
    final RMeshBasic m,
    final Offsets offsets,
    final List<RColladaPoly> polygons,
    final LogUsableType log)
  {
    for (final RColladaPoly poly : polygons) {
      final List<RColladaVertex> vertices = poly.getVertices();

      if (vertices.size() != 3) {
        throw new RangeCheckException("Polygons must have three vertices");
      }

      final RColladaVertex v0 = vertices.get(0);
      final RColladaVertex v1 = vertices.get(1);
      final RColladaVertex v2 = vertices.get(2);

      final int normal_v0 =
        v0.getIndices().get(offsets.normal_index).intValue();
      final int normal_v1 =
        v1.getIndices().get(offsets.normal_index).intValue();
      final int normal_v2 =
        v2.getIndices().get(offsets.normal_index).intValue();

      final int position_v0 =
        v0.getIndices().get(offsets.position_index).intValue();
      final int position_v1 =
        v1.getIndices().get(offsets.position_index).intValue();
      final int position_v2 =
        v2.getIndices().get(offsets.position_index).intValue();

      final int uv_v0 = v0.getIndices().get(offsets.texture_index).intValue();
      final int uv_v1 = v1.getIndices().get(offsets.texture_index).intValue();
      final int uv_v2 = v2.getIndices().get(offsets.texture_index).intValue();

      final int v0i = m.vertexAdd(position_v0, normal_v0, uv_v0);
      final int v1i = m.vertexAdd(position_v1, normal_v1, uv_v1);
      final int v2i = m.vertexAdd(position_v2, normal_v2, uv_v2);
      m.triangleAdd(v0i, v1i, v2i);
    }

    log.debug("Loaded " + m.verticesGet().size() + " vertices");
    log.debug("Loaded " + m.trianglesGet().size() + " triangles");
  }

  private static void loadColladaTexCoords(
    final RMeshBasic m,
    final ColladaSourceArray2F source)
  {
    final List<VectorI2F> a = source.getArray2f();
    for (final VectorI2F v : a) {
      m.uvAdd(new PVectorI2F<RSpaceTextureType>(v.getXF(), v.getYF()));
    }
  }

  private final LogUsableType log;
  private final MatrixM3x3F   matrix;

  /**
   * Construct a new importer.
   *
   * @param in_log
   *          A log interface.
   */

  public RColladaToMeshBasic(
    final LogUsableType in_log)
  {
    this.log =
      NullCheck.notNull(in_log, "Log interface").with(
        "mesh-basic-collada-importer");
    this.matrix = new MatrixM3x3F();
  }

  private void loadColladaNormals(
    final RMeshBasic m,
    final ColladaSourceArray3F source)
  {
    final RColladaAxis source_axis = source.getAxis();
    if (source_axis != RColladaAxis.COLLADA_AXIS_Y_UP) {
      this.log.debug("Converting normals from axis "
        + source_axis
        + " to "
        + RColladaAxis.COLLADA_AXIS_Y_UP);
    }

    final List<VectorI3F> a = source.getArray3f();
    for (final VectorI3F v : a) {
      final PVectorI3F<RSpaceObjectType> vec =
        new PVectorI3F<RSpaceObjectType>(v.getXF(), v.getYF(), v.getZF());

      if (source_axis != RColladaAxis.COLLADA_AXIS_Y_UP) {
        m.normalAdd(RColladaAxis.convertAxes(
          this.matrix,
          source_axis,
          vec,
          RColladaAxis.COLLADA_AXIS_Y_UP));
      } else {
        m.normalAdd(vec);
      }
    }
  }

  private void loadColladaPositions(
    final RMeshBasic m,
    final ColladaSourceArray3F source)
  {
    final RColladaAxis source_axis = source.getAxis();
    if (source_axis != RColladaAxis.COLLADA_AXIS_Y_UP) {
      this.log.debug("Converting positions from axis "
        + source_axis
        + " to "
        + RColladaAxis.COLLADA_AXIS_Y_UP);
    }

    final List<VectorI3F> a = source.getArray3f();
    for (final VectorI3F v : a) {
      final PVectorI3F<RSpaceObjectType> vec =
        new PVectorI3F<RSpaceObjectType>(v.getXF(), v.getYF(), v.getZF());

      if (source_axis != RColladaAxis.COLLADA_AXIS_Y_UP) {
        m.positionAdd(RColladaAxis.convertAxes(
          this.matrix,
          source_axis,
          vec,
          RColladaAxis.COLLADA_AXIS_Y_UP));
      } else {
        m.positionAdd(vec);
      }
    }
  }

  private void loadColladaSources(
    final RColladaDocument doc,
    final RMeshBasic m,
    final List<RColladaInput> inputs)
  {
    for (final RColladaInput input : inputs) {
      switch (input.getSemantic()) {
        case SEMANTIC_VERTEX:
        {
          this.loadSourceVertexPositions(doc, m, input);
          break;
        }
        case SEMANTIC_NORMAL:
        {
          this.loadSourceVertexNormals(doc, m, input);
          break;
        }
        case SEMANTIC_TEXCOORD:
        {
          this.loadSourceVertexUVCoordinates(doc, m, input);
          break;
        }
        case SEMANTIC_BINORMAL:
        case SEMANTIC_COLOR:
        case SEMANTIC_CONTINUITY:
        case SEMANTIC_IMAGE:
        case SEMANTIC_INPUT:
        case SEMANTIC_INTERPOLATION:
        case SEMANTIC_INV_BIND_MATRIX:
        case SEMANTIC_IN_TANGENT:
        case SEMANTIC_JOINT:
        case SEMANTIC_LINEAR_STEPS:
        case SEMANTIC_MORPH_TARGET:
        case SEMANTIC_MORPH_WEIGHT:
        case SEMANTIC_OUTPUT:
        case SEMANTIC_OUT_TANGENT:
        case SEMANTIC_POSITION:
        case SEMANTIC_TANGENT:
        case SEMANTIC_TEXBINORMAL:
        case SEMANTIC_TEXTANGENT:
        case SEMANTIC_UV:
        case SEMANTIC_WEIGHT:
        {
          break;
        }
      }
    }
  }

  private void loadSourceVertexUVCoordinates(
    final RColladaDocument doc,
    final RMeshBasic m,
    final RColladaInput input)
  {
    final ColladaSourceArray2F source =
      (RColladaSource.ColladaSourceArray2F) doc.getSource(input.getSource());
    assert source != null;
    RColladaToMeshBasic.loadColladaTexCoords(m, source);

    this.log.debug("Loaded " + m.uvsGet().size() + " texture coordinates");
  }

  private void loadSourceVertexNormals(
    final RColladaDocument doc,
    final RMeshBasic m,
    final RColladaInput input)
  {
    final ColladaSourceArray3F source =
      (RColladaSource.ColladaSourceArray3F) doc.getSource(input.getSource());
    assert source != null;
    this.loadColladaNormals(m, source);
    this.log.debug("Loaded " + m.normalsGet().size() + " normals");
  }

  private void loadSourceVertexPositions(
    final RColladaDocument doc,
    final RMeshBasic m,
    final RColladaInput input)
  {
    final ColladaVertices vertices =
      (RColladaSource.ColladaVertices) doc.getSource(input.getSource());
    assert vertices != null;

    boolean got_position = false;
    for (final RColladaInput vertex_input : vertices.getInputs()) {
      if (vertex_input.getSemantic() == RColladaSemantic.SEMANTIC_POSITION) {
        got_position = true;
        final ColladaSourceArray3F source =
          (RColladaSource.ColladaSourceArray3F) doc.getSource(vertex_input
            .getSource());
        assert source != null;

        this.loadColladaPositions(m, source);
      }
    }

    if (got_position == false) {
      throw new IllegalArgumentException(
        "Source does not have POSITION input");
    }

    this.log.debug("Loaded " + m.positionsGet().size() + " positions");
  }

  /**
   * Load the mesh from the given geometry and document.
   *
   * @param doc
   *          The document.
   * @param geom
   *          The geometry.
   * @return A mesh.
   * @throws RExceptionMeshMissingUVs
   *           If the geometry does not have UV coordinates.
   * @throws RExceptionMeshMissingNormals
   *           If the geometry does not have normal vectors.
   * @throws RExceptionMeshMissingPositions
   *           If the geometry does not have vertex positions.
   * @throws RExceptionMeshNameInvalid
   *           If the name of the geometry is not valid for use in a mesh.
   */

  public RMeshBasic newMeshFromColladaGeometry(
    final RColladaDocument doc,
    final RColladaGeometry geom)
    throws RExceptionMeshMissingPositions,
      RExceptionMeshMissingNormals,
      RExceptionMeshMissingUVs,
      RExceptionMeshNameInvalid
  {
    NullCheck.notNull(doc, "Document");
    NullCheck.notNull(geom, "Geometry");
    return this.newMeshFromColladaGeometryWithName(doc, geom, geom
      .getID()
      .getActual());
  }

  /**
   * Load the mesh from the given geometry and document, naming it
   * <code>name</code>.
   *
   * @param name
   *          The resulting mesh name.
   * @param doc
   *          The document.
   * @param geom
   *          The geometry.
   * @return A mesh.
   * @throws RExceptionMeshMissingUVs
   *           If the geometry does not have UV coordinates.
   * @throws RExceptionMeshMissingNormals
   *           If the geometry does not have normal vectors.
   * @throws RExceptionMeshMissingPositions
   *           If the geometry does not have vertex positions.
   * @throws RExceptionMeshNameInvalid
   *           If the name of the geometry is not valid for use in a mesh.
   */

  public RMeshBasic newMeshFromColladaGeometryWithName(
    final RColladaDocument doc,
    final RColladaGeometry geom,
    final String name)
    throws RExceptionMeshMissingPositions,
      RExceptionMeshMissingNormals,
      RExceptionMeshMissingUVs,
      RExceptionMeshNameInvalid
  {
    NullCheck.notNull(doc, "Document");
    NullCheck.notNull(geom, "Geometry");
    NullCheck.notNull(name, "Name");

    if (geom.getDocument() != doc) {
      throw new IllegalArgumentException(
        "Geometry did not come from the given document");
    }

    final RMeshBasic m = RMeshBasic.newMesh(name);
    switch (geom.getType()) {
      case GEOMETRY_MESH:
      {
        final RColladaMesh cm = (RColladaMesh) geom;
        final RColladaPolylist pl = cm.getPolylist();
        final List<RColladaInput> inputs = pl.getInputs();
        final Offsets offsets = new Offsets(cm.getID(), inputs);
        final List<RColladaPoly> polys = pl.getPolygons();
        this.loadColladaSources(doc, m, inputs);
        RColladaToMeshBasic.loadColladaPolygons(m, offsets, polys, this.log);
        break;
      }
    }

    return m;
  }
}
