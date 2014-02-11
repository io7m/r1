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

import java.util.List;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jlog.Log;
import com.io7m.jtensors.MatrixM3x3F;
import com.io7m.jtensors.VectorI2F;
import com.io7m.jtensors.VectorI3F;
import com.io7m.renderer.types.RSpaceObject;
import com.io7m.renderer.types.RSpaceTexture;
import com.io7m.renderer.types.RVectorI2F;
import com.io7m.renderer.types.RVectorI3F;
import com.io7m.renderer.xml.collada.ColladaAxis;
import com.io7m.renderer.xml.collada.ColladaDocument;
import com.io7m.renderer.xml.collada.ColladaGeometry;
import com.io7m.renderer.xml.collada.ColladaGeometry.ColladaMesh;
import com.io7m.renderer.xml.collada.ColladaInput;
import com.io7m.renderer.xml.collada.ColladaPoly;
import com.io7m.renderer.xml.collada.ColladaPolylist;
import com.io7m.renderer.xml.collada.ColladaSemantic;
import com.io7m.renderer.xml.collada.ColladaSource;
import com.io7m.renderer.xml.collada.ColladaSource.ColladaSourceArray2F;
import com.io7m.renderer.xml.collada.ColladaSource.ColladaSourceArray3F;
import com.io7m.renderer.xml.collada.ColladaSource.ColladaVertices;
import com.io7m.renderer.xml.collada.ColladaVertex;

public class MeshBasicColladaImporter
{
  static class Offsets
  {
    private static int getNormalOffset(
      final @Nonnull List<ColladaInput> inputs)
    {
      for (int index = 0; index < inputs.size(); ++index) {
        if (inputs.get(index).getSemantic() == ColladaSemantic.SEMANTIC_NORMAL) {
          return index;
        }
      }
      return -1;
    }

    private static int getPositionOffset(
      final @Nonnull List<ColladaInput> inputs)
    {
      for (int index = 0; index < inputs.size(); ++index) {
        if (inputs.get(index).getSemantic() == ColladaSemantic.SEMANTIC_VERTEX) {
          return index;
        }
      }
      return -1;
    }

    private static int getUVOffset(
      final @Nonnull List<ColladaInput> inputs)
    {
      for (int index = 0; index < inputs.size(); ++index) {
        if (inputs.get(index).getSemantic() == ColladaSemantic.SEMANTIC_TEXCOORD) {
          return index;
        }
      }
      return -1;
    }

    final int position_index;
    final int normal_index;
    final int texture_index;

    Offsets(
      final @Nonnull List<ColladaInput> inputs)
      throws ConstraintError
    {
      this.position_index = Offsets.getPositionOffset(inputs);
      Constraints.constrainRange(
        this.position_index,
        0,
        Integer.MAX_VALUE,
        "Mesh has position attribute");
      this.normal_index = Offsets.getNormalOffset(inputs);
      Constraints.constrainRange(
        this.normal_index,
        0,
        Integer.MAX_VALUE,
        "Mesh has normal attribute");
      this.texture_index = Offsets.getUVOffset(inputs);
    }
  }

  private static void loadColladaPolygons(
    final @Nonnull MeshBasic m,
    final @Nonnull Offsets offsets,
    final @Nonnull List<ColladaPoly> polygons,
    final @Nonnull Log log)
    throws ConstraintError
  {
    for (final ColladaPoly poly : polygons) {
      final List<ColladaVertex> vertices = poly.getVertices();
      Constraints
        .constrainRange(vertices.size(), 3, 3, "Polygon is triangle");

      final ColladaVertex v0 = vertices.get(0);
      final ColladaVertex v1 = vertices.get(1);
      final ColladaVertex v2 = vertices.get(2);

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

      int uv_v0 = -1;
      int uv_v1 = -1;
      int uv_v2 = -1;
      if (offsets.texture_index >= 0) {
        uv_v0 = v0.getIndices().get(offsets.texture_index).intValue();
        uv_v1 = v1.getIndices().get(offsets.texture_index).intValue();
        uv_v2 = v2.getIndices().get(offsets.texture_index).intValue();
      }

      final int v0i = m.vertexAdd(position_v0, normal_v0, uv_v0);
      final int v1i = m.vertexAdd(position_v1, normal_v1, uv_v1);
      final int v2i = m.vertexAdd(position_v2, normal_v2, uv_v2);
      m.triangleAdd(v0i, v1i, v2i);
    }

    log.debug("Loaded " + m.verticesGet().size() + " vertices");
    log.debug("Loaded " + m.trianglesGet().size() + " triangles");
  }

  private static void loadColladaTexCoords(
    final @Nonnull MeshBasic m,
    final @Nonnull ColladaSourceArray2F source)
    throws ConstraintError
  {
    final List<VectorI2F> a = source.getArray2f();
    for (final VectorI2F v : a) {
      m.uvAdd(new RVectorI2F<RSpaceTexture>(v.x, v.y));
    }
  }

  private final @Nonnull Log         log;
  private final @Nonnull MatrixM3x3F matrix;

  public MeshBasicColladaImporter(
    final @Nonnull Log log)
    throws ConstraintError
  {
    this.log =
      new Log(
        Constraints.constrainNotNull(log, "Log interface"),
        "mesh-basic-collada-importer");
    this.matrix = new MatrixM3x3F();
  }

  private void loadColladaNormals(
    final @Nonnull MeshBasic m,
    final @Nonnull ColladaSourceArray3F source)
    throws ConstraintError
  {
    final ColladaAxis source_axis = source.getAxis();
    if (source_axis != ColladaAxis.COLLADA_AXIS_Y_UP) {
      this.log.debug("Converting normals from axis "
        + source_axis
        + " to "
        + ColladaAxis.COLLADA_AXIS_Y_UP);
    }

    final List<VectorI3F> a = source.getArray3f();
    for (final VectorI3F v : a) {
      final RVectorI3F<RSpaceObject> vec =
        new RVectorI3F<RSpaceObject>(v.x, v.y, v.z);

      if (source_axis != ColladaAxis.COLLADA_AXIS_Y_UP) {
        m.normalAdd(ColladaAxis.convertAxes(
          this.matrix,
          source_axis,
          vec,
          ColladaAxis.COLLADA_AXIS_Y_UP));
      } else {
        m.normalAdd(vec);
      }
    }
  }

  private void loadColladaPositions(
    final @Nonnull MeshBasic m,
    final @Nonnull ColladaSourceArray3F source)
    throws ConstraintError
  {
    final ColladaAxis source_axis = source.getAxis();
    if (source_axis != ColladaAxis.COLLADA_AXIS_Y_UP) {
      this.log.debug("Converting positions from axis "
        + source_axis
        + " to "
        + ColladaAxis.COLLADA_AXIS_Y_UP);
    }

    final List<VectorI3F> a = source.getArray3f();
    for (final VectorI3F v : a) {
      final RVectorI3F<RSpaceObject> vec =
        new RVectorI3F<RSpaceObject>(v.x, v.y, v.z);

      if (source_axis != ColladaAxis.COLLADA_AXIS_Y_UP) {
        m.positionAdd(ColladaAxis.convertAxes(
          this.matrix,
          source_axis,
          vec,
          ColladaAxis.COLLADA_AXIS_Y_UP));
      } else {
        m.positionAdd(vec);
      }
    }
  }

  private void loadColladaSources(
    final @Nonnull ColladaDocument doc,
    final @Nonnull MeshBasic m,
    final @Nonnull List<ColladaInput> inputs)
    throws ConstraintError
  {
    for (final ColladaInput input : inputs) {
      switch (input.getSemantic()) {
        case SEMANTIC_VERTEX:
        {
          final ColladaVertices vertices =
            (ColladaSource.ColladaVertices) doc.getSource(input.getSource());

          boolean got_position = false;
          for (final ColladaInput vertex_input : vertices.getInputs()) {
            if (vertex_input.getSemantic() == ColladaSemantic.SEMANTIC_POSITION) {
              got_position = true;
              final ColladaSourceArray3F source =
                (ColladaSource.ColladaSourceArray3F) doc
                  .getSource(vertex_input.getSource());

              this.loadColladaPositions(m, source);
            }
          }

          Constraints.constrainArbitrary(
            got_position,
            "Source had POSITION input");

          this.log.debug("Loaded " + m.positionsGet().size() + " positions");
          break;
        }
        case SEMANTIC_NORMAL:
        {
          final ColladaSourceArray3F source =
            (ColladaSource.ColladaSourceArray3F) doc.getSource(input
              .getSource());

          this.loadColladaNormals(m, source);

          this.log.debug("Loaded " + m.normalsGet().size() + " normals");
          break;
        }
        case SEMANTIC_TEXCOORD:
        {
          final ColladaSourceArray2F source =
            (ColladaSource.ColladaSourceArray2F) doc.getSource(input
              .getSource());

          MeshBasicColladaImporter.loadColladaTexCoords(m, source);

          this.log.debug("Loaded "
            + m.uvsGet().size()
            + " texture coordinates");
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

  public @Nonnull MeshBasic newMeshFromColladaGeometry(
    final @Nonnull ColladaDocument doc,
    final @Nonnull ColladaGeometry geom)
    throws ConstraintError
  {
    Constraints.constrainNotNull(doc, "Document");
    Constraints.constrainNotNull(geom, "Geometry");
    return this.newMeshFromColladaGeometryWithName(doc, geom, geom
      .getID()
      .getActual());
  }

  public @Nonnull MeshBasic newMeshFromColladaGeometryWithName(
    final @Nonnull ColladaDocument doc,
    final @Nonnull ColladaGeometry geom,
    final @Nonnull String name)
    throws ConstraintError
  {
    Constraints.constrainNotNull(doc, "Document");
    Constraints.constrainNotNull(geom, "Geometry");
    Constraints.constrainNotNull(name, "Name");
    Constraints.constrainArbitrary(
      geom.getDocument() == doc,
      "Geometry came from the given document");

    final MeshBasic m = MeshBasic.newMesh(name);
    switch (geom.getType()) {
      case GEOMETRY_MESH:
      {
        final ColladaMesh cm = (ColladaMesh) geom;
        final ColladaPolylist pl = cm.getPolylist();
        final List<ColladaInput> inputs = pl.getInputs();
        final Offsets offsets = new Offsets(inputs);
        m.setHasUV(offsets.texture_index >= 0);
        final List<ColladaPoly> polys = pl.getPolygons();
        this.loadColladaSources(doc, m, inputs);
        MeshBasicColladaImporter.loadColladaPolygons(
          m,
          offsets,
          polys,
          this.log);
        break;
      }
    }

    return m;
  }
}
