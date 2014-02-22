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

package com.io7m.renderer.xml.rmx;

import java.util.ArrayList;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import nu.xom.Document;
import nu.xom.Element;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jcanephora.ArrayBuffer;
import com.io7m.jcanephora.ArrayBufferAttributeDescriptor;
import com.io7m.jcanephora.ArrayBufferTypeDescriptor;
import com.io7m.jcanephora.ArrayBufferWritableData;
import com.io7m.jcanephora.CursorWritable2f;
import com.io7m.jcanephora.CursorWritable3f;
import com.io7m.jcanephora.CursorWritable4f;
import com.io7m.jcanephora.CursorWritableIndex;
import com.io7m.jcanephora.IndexBuffer;
import com.io7m.jcanephora.IndexBufferWritableData;
import com.io7m.jcanephora.JCGLArrayBuffers;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLIndexBuffers;
import com.io7m.jcanephora.UsageHint;
import com.io7m.renderer.kernel.types.KMeshAttributes;
import com.io7m.renderer.types.RSpaceObject;
import com.io7m.renderer.types.RSpaceTexture;
import com.io7m.renderer.types.RVectorI2F;
import com.io7m.renderer.types.RVectorI3F;
import com.io7m.renderer.types.RVectorI4F;
import com.io7m.renderer.types.RXMLException;

/**
 * An implementation of the {@link RXMLMeshParserEvents} interface that
 * produces an array buffer and index buffer whilst parsing.
 */

public final class RXMLMeshParserVBO<G extends JCGLArrayBuffers & JCGLIndexBuffers>
{
  private class Events implements RXMLMeshParserEvents<JCGLException>
  {
    private @CheckForNull ArrayBufferWritableData   array_data;
    private @CheckForNull CursorWritable3f          cursor_bitangent;
    private @CheckForNull CursorWritableIndex       cursor_index;
    private @CheckForNull CursorWritable3f          cursor_normal;
    private @CheckForNull CursorWritable3f          cursor_pos;
    private @CheckForNull CursorWritable3f          cursor_tangent3f;
    private @CheckForNull CursorWritable4f          cursor_tangent4f;
    private @CheckForNull CursorWritable2f          cursor_uv;
    private final @Nonnull G                        gl;
    private @CheckForNull IndexBufferWritableData   indices_data;
    private @CheckForNull RXMLMeshType              mtype;
    private @CheckForNull ArrayBufferTypeDescriptor type;
    private final @Nonnull UsageHint                usage;

    public Events(
      final @Nonnull G g,
      final @Nonnull UsageHint hint)
      throws ConstraintError
    {
      this.gl = Constraints.constrainNotNull(g, "OpenGL interface");
      this.usage = Constraints.constrainNotNull(hint, "Usage hint");
    }

    @Override public void eventError(
      final @Nonnull RXMLException e)
      throws JCGLException,
        ConstraintError
    {
      Constraints.constrainArbitrary(
        RXMLMeshParserVBO.this.parsing,
        "Parsing in progress");
      RXMLMeshParserVBO.this.error = true;

      if (RXMLMeshParserVBO.this.array != null) {
        this.gl.arrayBufferDelete(RXMLMeshParserVBO.this.array);
      }
      if (RXMLMeshParserVBO.this.indices != null) {
        this.gl.indexBufferDelete(RXMLMeshParserVBO.this.indices);
      }
    }

    @Override public void eventMeshEnded()
      throws JCGLException,
        ConstraintError
    {
      Constraints.constrainArbitrary(
        RXMLMeshParserVBO.this.parsing,
        "Parsing in progress");
      RXMLMeshParserVBO.this.parsing = false;
      RXMLMeshParserVBO.this.success = !RXMLMeshParserVBO.this.error;
    }

    @Override public void eventMeshName(
      final @Nonnull String mesh_name)
      throws JCGLException,
        ConstraintError
    {
      Constraints.constrainArbitrary(
        RXMLMeshParserVBO.this.parsing,
        "Parsing in progress");
      RXMLMeshParserVBO.this.name = mesh_name;
    }

    @Override public void eventMeshStarted()
      throws JCGLException,
        ConstraintError
    {
      Constraints.constrainArbitrary(
        RXMLMeshParserVBO.this.parsing == false,
        "Parsing not in progress");
      RXMLMeshParserVBO.this.parsing = true;
    }

    @Override public void eventMeshTriangle(
      final int index,
      final int v0,
      final int v1,
      final int v2)
      throws ConstraintError
    {
      Constraints.constrainArbitrary(
        RXMLMeshParserVBO.this.parsing,
        "Parsing in progress");
      this.cursor_index.putIndex(v0);
      this.cursor_index.putIndex(v1);
      this.cursor_index.putIndex(v2);
    }

    @Override public void eventMeshTrianglesEnded()
      throws JCGLException,
        ConstraintError
    {
      Constraints.constrainArbitrary(
        RXMLMeshParserVBO.this.parsing,
        "Parsing in progress");
      Constraints.constrainArbitrary(
        this.cursor_index.isValid() == false,
        "Index buffer completely assigned");

      this.gl.indexBufferUpdate(this.indices_data);
    }

    @Override public void eventMeshTrianglesStarted(
      final int count)
      throws JCGLException,
        ConstraintError
    {
      Constraints.constrainArbitrary(
        RXMLMeshParserVBO.this.parsing,
        "Parsing in progress");
      RXMLMeshParserVBO.this.indices =
        this.gl.indexBufferAllocate(RXMLMeshParserVBO.this.array, count * 3);
      this.indices_data =
        new IndexBufferWritableData(RXMLMeshParserVBO.this.indices);
      this.cursor_index = this.indices_data.getCursor();
    }

    @Override public void eventMeshType(
      final @Nonnull RXMLMeshType mt)
      throws JCGLException,
        ConstraintError
    {
      Constraints.constrainArbitrary(
        RXMLMeshParserVBO.this.parsing,
        "Parsing in progress");

      final ArrayList<ArrayBufferAttributeDescriptor> a =
        new ArrayList<ArrayBufferAttributeDescriptor>();

      a.add(KMeshAttributes.ATTRIBUTE_POSITION);

      if (mt.hasNormal()) {
        a.add(KMeshAttributes.ATTRIBUTE_NORMAL);
      }

      if (mt.hasTangent3f()) {
        a.add(KMeshAttributes.ATTRIBUTE_TANGENT3);
      }

      if (mt.hasTangent4f()) {
        a.add(KMeshAttributes.ATTRIBUTE_TANGENT4);
      }

      if (mt.hasBitangent()) {
        a.add(KMeshAttributes.ATTRIBUTE_BITANGENT);
      }

      if (mt.hasUV()) {
        a.add(KMeshAttributes.ATTRIBUTE_UV);
      }

      this.type = new ArrayBufferTypeDescriptor(a);
      this.mtype = mt;
    }

    @Override public void eventMeshVertexBitangent(
      final int index,
      final @Nonnull RVectorI3F<RSpaceObject> bitangent)
      throws JCGLException,
        ConstraintError
    {
      Constraints.constrainArbitrary(
        RXMLMeshParserVBO.this.parsing,
        "Parsing in progress");
      this.cursor_bitangent.put3f(bitangent.x, bitangent.y, bitangent.z);
    }

    @Override public void eventMeshVertexEnded(
      final int index)
      throws ConstraintError
    {
      Constraints.constrainArbitrary(
        RXMLMeshParserVBO.this.parsing,
        "Parsing in progress");
    }

    @Override public void eventMeshVertexNormal(
      final int index,
      final @Nonnull RVectorI3F<RSpaceObject> normal)
      throws ConstraintError
    {
      Constraints.constrainArbitrary(
        RXMLMeshParserVBO.this.parsing,
        "Parsing in progress");
      this.cursor_normal.put3f(normal.x, normal.y, normal.z);
    }

    @Override public void eventMeshVertexPosition(
      final int index,
      final @Nonnull RVectorI3F<RSpaceObject> position)
      throws ConstraintError
    {
      Constraints.constrainArbitrary(
        RXMLMeshParserVBO.this.parsing,
        "Parsing in progress");
      this.cursor_pos.put3f(position.x, position.y, position.z);
    }

    @Override public void eventMeshVertexStarted(
      final int index)
      throws ConstraintError
    {
      Constraints.constrainArbitrary(
        RXMLMeshParserVBO.this.parsing,
        "Parsing in progress");
    }

    @Override public void eventMeshVertexTangent3f(
      final int index,
      final @Nonnull RVectorI3F<RSpaceObject> tangent)
      throws ConstraintError
    {
      Constraints.constrainArbitrary(
        RXMLMeshParserVBO.this.parsing,
        "Parsing in progress");
      this.cursor_tangent3f.put3f(tangent.x, tangent.y, tangent.z);
    }

    @Override public void eventMeshVertexTangent4f(
      final int index,
      final @Nonnull RVectorI4F<RSpaceObject> tangent)
      throws ConstraintError
    {
      Constraints.constrainArbitrary(
        RXMLMeshParserVBO.this.parsing,
        "Parsing in progress");
      this.cursor_tangent4f.put4f(tangent.x, tangent.y, tangent.z, tangent.w);
    }

    @Override public void eventMeshVertexUV(
      final int index,
      final @Nonnull RVectorI2F<RSpaceTexture> uv)
      throws ConstraintError
    {
      Constraints.constrainArbitrary(
        RXMLMeshParserVBO.this.parsing,
        "Parsing in progress");
      this.cursor_uv.put2f(uv.x, uv.y);
    }

    @SuppressWarnings("synthetic-access") @Override public
      void
      eventMeshVerticesEnded(
        final @Nonnull RVectorI3F<RSpaceObject> lower,
        final @Nonnull RVectorI3F<RSpaceObject> upper)
        throws JCGLException,
          ConstraintError
    {
      Constraints.constrainArbitrary(
        RXMLMeshParserVBO.this.parsing,
        "Parsing in progress");

      if (this.cursor_pos != null) {
        Constraints.constrainArbitrary(
          this.cursor_pos.isValid() == false,
          "Position attributes completely assigned");
      }
      if (this.cursor_normal != null) {
        Constraints.constrainArbitrary(
          this.cursor_normal.isValid() == false,
          "KMaterialNormalLabel attributes completely assigned");
      }
      if (this.cursor_tangent3f != null) {
        Constraints.constrainArbitrary(
          this.cursor_tangent3f.isValid() == false,
          "Tangent3f attributes completely assigned");
      }
      if (this.cursor_tangent4f != null) {
        Constraints.constrainArbitrary(
          this.cursor_tangent4f.isValid() == false,
          "Tangent4f attributes completely assigned");
      }
      if (this.cursor_bitangent != null) {
        Constraints.constrainArbitrary(
          this.cursor_bitangent.isValid() == false,
          "Bitangent attributes completely assigned");
      }
      if (this.cursor_uv != null) {
        Constraints.constrainArbitrary(
          this.cursor_uv.isValid() == false,
          "UV attributes completely assigned");
      }

      this.gl.arrayBufferBind(RXMLMeshParserVBO.this.array);
      try {
        this.gl.arrayBufferUpdate(this.array_data);
      } finally {
        this.gl.arrayBufferUnbind();
      }

      RXMLMeshParserVBO.this.bounds_lower = lower;
      RXMLMeshParserVBO.this.bounds_upper = upper;
    }

    @Override public void eventMeshVerticesStarted(
      final int count)
      throws JCGLException,
        ConstraintError
    {
      Constraints.constrainArbitrary(
        RXMLMeshParserVBO.this.parsing,
        "Parsing in progress");

      RXMLMeshParserVBO.this.array =
        this.gl.arrayBufferAllocate(count, this.type, this.usage);
      this.array_data =
        new ArrayBufferWritableData(RXMLMeshParserVBO.this.array);

      this.cursor_pos =
        this.array_data.getCursor3f(KMeshAttributes.ATTRIBUTE_POSITION
          .getName());

      if (this.mtype.hasNormal()) {
        this.cursor_normal =
          this.array_data.getCursor3f(KMeshAttributes.ATTRIBUTE_NORMAL
            .getName());
      }

      if (this.mtype.hasUV()) {
        this.cursor_uv =
          this.array_data.getCursor2f(KMeshAttributes.ATTRIBUTE_UV.getName());
      }

      if (this.mtype.hasTangent3f()) {
        this.cursor_tangent3f =
          this.array_data.getCursor3f(KMeshAttributes.ATTRIBUTE_TANGENT3
            .getName());
      }

      if (this.mtype.hasTangent4f()) {
        this.cursor_tangent4f =
          this.array_data.getCursor4f(KMeshAttributes.ATTRIBUTE_TANGENT4
            .getName());
      }

      if (this.mtype.hasBitangent()) {
        this.cursor_bitangent =
          this.array_data.getCursor3f(KMeshAttributes.ATTRIBUTE_BITANGENT
            .getName());
      }
    }
  }

  public static
    <G extends JCGLArrayBuffers & JCGLIndexBuffers>
    RXMLMeshParserVBO<G>
    parseFromDocument(
      final @Nonnull Document d,
      final @Nonnull G g,
      final @Nonnull UsageHint hint)
      throws ConstraintError,
        RXMLException,
        JCGLException
  {
    Constraints.constrainNotNull(d, "Document");
    Constraints.constrainNotNull(g, "OpenGL interface");
    Constraints.constrainNotNull(hint, "Usage hint");
    return new RXMLMeshParserVBO<G>(g, hint, d.getRootElement());
  }

  public static
    <G extends JCGLArrayBuffers & JCGLIndexBuffers>
    RXMLMeshParserVBO<G>
    parseFromElement(
      final @Nonnull Element e,
      final @Nonnull G g,
      final @Nonnull UsageHint hint)
      throws ConstraintError,
        RXMLException,
        JCGLException
  {
    Constraints.constrainNotNull(e, "Element");
    Constraints.constrainNotNull(g, "OpenGL interface");
    Constraints.constrainNotNull(hint, "Usage hint");
    return new RXMLMeshParserVBO<G>(g, hint, e);
  }

  protected @CheckForNull ArrayBuffer                  array;
  private @Nonnull RVectorI3F<RSpaceObject>            bounds_lower;
  private @Nonnull RVectorI3F<RSpaceObject>            bounds_upper;
  protected boolean                                    error;
  private final @Nonnull Events                        events;
  protected @CheckForNull IndexBuffer                  indices;
  protected @CheckForNull String                       name;
  private final @Nonnull RXMLMeshParser<JCGLException> parser;
  protected boolean                                    parsing;
  protected boolean                                    success;

  private RXMLMeshParserVBO(
    final @Nonnull G g,
    final @Nonnull UsageHint hint,
    final @Nonnull Element e)
    throws ConstraintError,
      JCGLException,
      RXMLException
  {
    this.parsing = false;
    this.success = false;
    this.error = false;
    this.events = new Events(g, hint);
    this.parser = RXMLMeshParser.parseFromElement(e, this.events);
  }

  /**
   * Retrieve the array buffer produced from a successful parse.
   * 
   * @throws ConstraintError
   *           Iff parsing was not successful.
   */

  public @Nonnull ArrayBuffer getArrayBuffer()
    throws ConstraintError
  {
    Constraints.constrainArbitrary(this.success, "Parsing was successful");
    return this.array;
  }

  /**
   * Retrieve the lower bounds of all the vertices in the parsed mesh.
   * 
   * @throws ConstraintError
   *           Iff parsing was not successful.
   */

  public final @Nonnull RVectorI3F<RSpaceObject> getBoundsLower()
    throws ConstraintError
  {
    Constraints.constrainArbitrary(this.success, "Parsing was successful");
    return this.bounds_lower;
  }

  /**
   * Retrieve the upper bounds of all the vertices in the parsed mesh.
   * 
   * @throws ConstraintError
   *           Iff parsing was not successful.
   */

  public final @Nonnull RVectorI3F<RSpaceObject> getBoundsUpper()
    throws ConstraintError
  {
    Constraints.constrainArbitrary(this.success, "Parsing was successful");
    return this.bounds_upper;
  }

  /**
   * Retrieve the index buffer produced from a successful parse.
   * 
   * @throws ConstraintError
   *           Iff parsing was not successful.
   */

  public @Nonnull IndexBuffer getIndexBuffer()
    throws ConstraintError
  {
    Constraints.constrainArbitrary(this.success, "Parsing was successful");
    return this.indices;
  }

  /**
   * Retrieve the name of the parsed mesh.
   * 
   * @throws ConstraintError
   *           Iff parsing was not successful.
   */

  public @Nonnull String getName()
    throws ConstraintError
  {
    Constraints.constrainArbitrary(this.success, "Parsing was successful");
    return this.name;
  }
}
