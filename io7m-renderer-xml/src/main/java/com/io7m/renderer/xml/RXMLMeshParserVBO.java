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

package com.io7m.renderer.xml;

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
import com.io7m.jcanephora.CursorWritableIndex;
import com.io7m.jcanephora.IndexBuffer;
import com.io7m.jcanephora.IndexBufferWritableData;
import com.io7m.jcanephora.JCGLArrayBuffers;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLIndexBuffers;
import com.io7m.jcanephora.UsageHint;
import com.io7m.renderer.RSpaceObject;
import com.io7m.renderer.RSpaceTangent;
import com.io7m.renderer.RSpaceTexture;
import com.io7m.renderer.RVectorI2F;
import com.io7m.renderer.RVectorI3F;
import com.io7m.renderer.kernel.KMeshAttributes;

/**
 * An implementation of the {@link RXMLMeshParserEvents} interface that
 * produces an array buffer and index buffer whilst parsing.
 */

public final class RXMLMeshParserVBO<G extends JCGLArrayBuffers & JCGLIndexBuffers>
{
  private class Events implements RXMLMeshParserEvents<JCGLException>
  {
    private @CheckForNull RXMLMeshType              mtype;
    private @CheckForNull ArrayBufferTypeDescriptor type;
    private final @Nonnull G                        gl;
    private final @Nonnull UsageHint                usage;
    private @CheckForNull ArrayBufferWritableData   array_data;
    private @CheckForNull IndexBufferWritableData   indices_data;
    private @CheckForNull CursorWritableIndex       cursor_index;
    private @CheckForNull CursorWritable3f          cursor_pos;
    private @CheckForNull CursorWritable3f          cursor_normal;
    private @CheckForNull CursorWritable2f          cursor_uv;
    private @CheckForNull CursorWritable3f          cursor_tangent;

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
        this.cursor_index.canWrite() == false,
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

      if (mt.hasTangent()) {
        a.add(KMeshAttributes.ATTRIBUTE_TANGENT);
      }

      if (mt.hasUV()) {
        a.add(KMeshAttributes.ATTRIBUTE_UV);
      }

      final ArrayBufferAttributeDescriptor[] ab =
        new ArrayBufferAttributeDescriptor[a.size()];
      a.toArray(ab);

      this.type = new ArrayBufferTypeDescriptor(ab);
      this.mtype = mt;
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

    @Override public void eventMeshVertexTangent(
      final int index,
      final @Nonnull RVectorI3F<RSpaceTangent> tangent)
      throws ConstraintError
    {
      Constraints.constrainArbitrary(
        RXMLMeshParserVBO.this.parsing,
        "Parsing in progress");
      this.cursor_tangent.put3f(tangent.x, tangent.y, tangent.z);
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

    @Override public void eventMeshVerticesEnded()
      throws JCGLException,
        ConstraintError
    {
      Constraints.constrainArbitrary(
        RXMLMeshParserVBO.this.parsing,
        "Parsing in progress");

      if (this.cursor_pos != null) {
        Constraints.constrainArbitrary(
          this.cursor_pos.canWrite() == false,
          "Position attributes completely assigned");
      }
      if (this.cursor_normal != null) {
        Constraints.constrainArbitrary(
          this.cursor_normal.canWrite() == false,
          "Normal attributes completely assigned");
      }
      if (this.cursor_tangent != null) {
        Constraints.constrainArbitrary(
          this.cursor_tangent.canWrite() == false,
          "Tangent attributes completely assigned");
      }
      if (this.cursor_uv != null) {
        Constraints.constrainArbitrary(
          this.cursor_uv.canWrite() == false,
          "UV attributes completely assigned");
      }

      this.gl.arrayBufferUpdate(this.array_data);
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

      if (this.mtype.hasTangent()) {
        this.cursor_tangent =
          this.array_data.getCursor3f(KMeshAttributes.ATTRIBUTE_TANGENT
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

  private final @Nonnull Events                        events;
  private final @Nonnull RXMLMeshParser<JCGLException> parser;
  protected boolean                                    parsing;
  protected boolean                                    success;
  protected boolean                                    error;
  protected @CheckForNull ArrayBuffer                  array;
  protected @CheckForNull IndexBuffer                  indices;
  protected @CheckForNull String                       name;

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
