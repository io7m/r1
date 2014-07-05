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

package com.io7m.renderer.meshes;

import com.io7m.jcanephora.ArrayBufferType;
import com.io7m.jcanephora.ArrayBufferUpdateUnmapped;
import com.io7m.jcanephora.ArrayBufferUpdateUnmappedType;
import com.io7m.jcanephora.ArrayDescriptor;
import com.io7m.jcanephora.ArrayDescriptorBuilderType;
import com.io7m.jcanephora.CursorType;
import com.io7m.jcanephora.CursorWritable2fType;
import com.io7m.jcanephora.CursorWritable3fType;
import com.io7m.jcanephora.CursorWritable4fType;
import com.io7m.jcanephora.CursorWritableIndexType;
import com.io7m.jcanephora.IndexBufferType;
import com.io7m.jcanephora.IndexBufferUpdateUnmapped;
import com.io7m.jcanephora.IndexBufferUpdateUnmappedType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLExceptionAttributeDuplicate;
import com.io7m.jcanephora.UsageHint;
import com.io7m.jcanephora.api.JCGLArrayBuffersType;
import com.io7m.jcanephora.api.JCGLIndexBuffersType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.kernel.types.KMeshAttributes;
import com.io7m.renderer.types.RSpaceObjectType;
import com.io7m.renderer.types.RSpaceTextureType;
import com.io7m.renderer.types.RVectorI2F;
import com.io7m.renderer.types.RVectorI3F;
import com.io7m.renderer.types.RVectorI4F;

/**
 * An implementation of the {@link MeshParserEventsType} interface that
 * produces an array buffer and index buffer whilst parsing.
 *
 * @param <G>
 *          The type of OpenGL interfaces.
 */

@EqualityReference public final class MeshParserEventsVBO<G extends JCGLArrayBuffersType & JCGLIndexBuffersType> implements
  MeshParserEventsType<JCGLException>
{
  private static void checkCursorCompletelyAssigned(
    final CursorType cpos,
    final String c_name)
  {
    if (cpos.isValid()) {
      throw new IllegalStateException("Cursor '"
        + c_name
        + "' not completely assigned");
    }
  }

  /**
   * Construct a new mesh parser.
   *
   * @param <G>
   *          The precise type of OpenGL interface
   * @param g
   *          The OpenGL interface
   * @param hint
   *          A usage hint
   * @return A mesh parser
   */

  public static
    <G extends JCGLArrayBuffersType & JCGLIndexBuffersType>
    MeshParserEventsVBO<G>
    newEvents(
      final G g,
      final UsageHint hint)
  {
    return new MeshParserEventsVBO<G>(g, hint);
  }

  private @Nullable ArrayBufferType               array;
  private @Nullable ArrayBufferUpdateUnmappedType array_data;
  private @Nullable RVectorI3F<RSpaceObjectType>  bounds_lower;
  private @Nullable RVectorI3F<RSpaceObjectType>  bounds_upper;
  private @Nullable CursorWritableIndexType       cursor_index;
  private @Nullable CursorWritable3fType          cursor_normal;
  private @Nullable CursorWritable3fType          cursor_pos;
  private @Nullable CursorWritable4fType          cursor_tangent4f;
  private @Nullable CursorWritable2fType          cursor_uv;
  private boolean                                 error;
  private final G                                 gl;
  private @Nullable IndexBufferType               indices;
  private @Nullable IndexBufferUpdateUnmappedType indices_data;
  private @Nullable String                        name;
  private boolean                                 parsing;
  private final ArrayDescriptor                   type;
  private final UsageHint                         usage;

  private MeshParserEventsVBO(
    final G g,
    final UsageHint hint)
  {
    this.gl = NullCheck.notNull(g, "OpenGL interface");
    this.usage = NullCheck.notNull(hint, "Usage hint");

    try {
      final ArrayDescriptorBuilderType b = ArrayDescriptor.newBuilder();
      b.addAttribute(KMeshAttributes.ATTRIBUTE_POSITION);
      b.addAttribute(KMeshAttributes.ATTRIBUTE_NORMAL);
      b.addAttribute(KMeshAttributes.ATTRIBUTE_TANGENT4);
      b.addAttribute(KMeshAttributes.ATTRIBUTE_UV);
      this.type = b.build();
      this.parsing = true;
    } catch (final JCGLExceptionAttributeDuplicate e) {
      throw new UnreachableCodeException(e);
    }
  }

  private void checkParsing()
  {
    if (this.parsing == false) {
      throw new IllegalStateException("Parsing is not currently in progress");
    }
  }

  private void checkParsingDone()
  {
    if (this.parsing) {
      throw new IllegalStateException("Parsing is not yet complete");
    }
    if (this.error) {
      throw new IllegalStateException("Parsing failed; array not available");
    }
  }

  @Override public void eventError(
    final Exception e)
    throws JCGLException
  {
    this.checkParsing();
    this.error = true;

    final ArrayBufferType a = this.array;
    if (a != null) {
      this.gl.arrayBufferDelete(a);
    }
    final IndexBufferType i = this.indices;
    if (i != null) {
      this.gl.indexBufferDelete(i);
    }
  }

  @Override public void eventMeshEnded()
    throws JCGLException
  {
    this.parsing = false;
  }

  @Override public void eventMeshName(
    final String in_name)
    throws JCGLException
  {
    this.checkParsing();
    this.name = in_name;
  }

  @Override public void eventMeshStarted()
    throws JCGLException
  {
    if (this.parsing) {
      throw new IllegalStateException("Parsing is already in progress");
    }

    this.parsing = true;
  }

  @Override public void eventMeshTriangle(
    final int index,
    final int v0,
    final int v1,
    final int v2)
    throws JCGLException
  {
    this.checkParsing();

    final CursorWritableIndexType ci = this.cursor_index;
    assert ci != null;
    ci.putIndex(v0);
    ci.putIndex(v1);
    ci.putIndex(v2);
  }

  @Override public void eventMeshTrianglesEnded()
    throws JCGLException
  {
    this.checkParsing();

    final CursorWritableIndexType ci = this.cursor_index;
    assert ci != null;
    if (ci.isValid()) {
      throw new IllegalStateException(
        "Index buffer has not been completely assigned");
    }

    final IndexBufferUpdateUnmappedType idata = this.indices_data;
    assert idata != null;
    this.gl.indexBufferUpdate(idata);
  }

  @Override public void eventMeshTrianglesStarted(
    final int count)
    throws JCGLException
  {
    this.checkParsing();

    final ArrayBufferType a = this.array;
    assert a != null;

    final IndexBufferType i =
      this.gl.indexBufferAllocate(a, count * 3, UsageHint.USAGE_STATIC_DRAW);
    assert i != null;

    this.indices = i;
    this.indices_data = IndexBufferUpdateUnmapped.newReplacing(i);
    this.cursor_index = this.indices_data.getCursor();
  }

  @Override public void eventMeshVertexEnded(
    final int index)
    throws JCGLException
  {
    this.checkParsing();
  }

  @Override public void eventMeshVertexNormal(
    final int index,
    final RVectorI3F<RSpaceObjectType> normal)
    throws JCGLException
  {
    this.checkParsing();

    final CursorWritable3fType c = this.cursor_normal;
    assert c != null;
    c.put3f(normal.getXF(), normal.getYF(), normal.getZF());
  }

  @Override public void eventMeshVertexPosition(
    final int index,
    final RVectorI3F<RSpaceObjectType> position)
    throws JCGLException
  {
    this.checkParsing();

    final CursorWritable3fType c = this.cursor_pos;
    assert c != null;
    c.put3f(position.getXF(), position.getYF(), position.getZF());
  }

  @Override public void eventMeshVertexStarted(
    final int index)
    throws JCGLException
  {
    this.checkParsing();
  }

  @Override public void eventMeshVertexTangent4f(
    final int index,
    final RVectorI4F<RSpaceObjectType> tangent)
    throws JCGLException
  {
    this.checkParsing();

    final CursorWritable4fType c = this.cursor_tangent4f;
    assert c != null;
    c.put4f(
      tangent.getXF(),
      tangent.getYF(),
      tangent.getZF(),
      tangent.getWF());
  }

  @Override public void eventMeshVertexUV(
    final int index,
    final RVectorI2F<RSpaceTextureType> uv)
    throws JCGLException
  {
    this.checkParsing();

    final CursorWritable2fType c = this.cursor_uv;
    assert c != null;
    c.put2f(uv.getXF(), uv.getYF());
  }

  @Override public void eventMeshVerticesEnded(
    final RVectorI3F<RSpaceObjectType> lower,
    final RVectorI3F<RSpaceObjectType> upper)
    throws JCGLException
  {
    this.checkParsing();

    final CursorWritable3fType cpos = this.cursor_pos;
    if (cpos != null) {
      MeshParserEventsVBO.checkCursorCompletelyAssigned(
        cpos,
        "Position attribute");
    }
    final CursorWritable3fType cnor = this.cursor_normal;
    if (cnor != null) {
      MeshParserEventsVBO.checkCursorCompletelyAssigned(
        cnor,
        "Normal attribute");
    }
    final CursorWritable4fType ctan4 = this.cursor_tangent4f;
    if (ctan4 != null) {
      MeshParserEventsVBO.checkCursorCompletelyAssigned(
        ctan4,
        "Tangent4 attribute");
    }
    final CursorWritable2fType cuv = this.cursor_uv;
    if (cuv != null) {
      MeshParserEventsVBO.checkCursorCompletelyAssigned(
        cuv,
        "UV attribute attribute");
    }

    final ArrayBufferType a = this.array;
    assert a != null;

    this.gl.arrayBufferBind(a);
    try {
      final ArrayBufferUpdateUnmappedType ad = this.array_data;
      assert ad != null;
      this.gl.arrayBufferUpdate(ad);
    } finally {
      this.gl.arrayBufferUnbind();
    }

    this.bounds_lower = lower;
    this.bounds_upper = upper;
  }

  @Override public void eventMeshVerticesStarted(
    final int count)
    throws JCGLException
  {
    this.checkParsing();

    final ArrayDescriptor at = this.type;
    assert at != null;

    final ArrayBufferType a =
      this.gl.arrayBufferAllocate(count, at, this.usage);
    final ArrayBufferUpdateUnmappedType ad =
      ArrayBufferUpdateUnmapped.newUpdateReplacingAll(a);

    this.array_data = ad;
    this.array = a;

    this.cursor_pos =
      this.array_data.getCursor3f(KMeshAttributes.ATTRIBUTE_POSITION
        .getName());
    this.cursor_normal =
      ad.getCursor3f(KMeshAttributes.ATTRIBUTE_NORMAL.getName());
    this.cursor_uv = ad.getCursor2f(KMeshAttributes.ATTRIBUTE_UV.getName());
    this.cursor_tangent4f =
      ad.getCursor4f(KMeshAttributes.ATTRIBUTE_TANGENT4.getName());
  }

  /**
   * @return The array.
   */

  public ArrayBufferType getArray()
  {
    this.checkParsingDone();
    final ArrayBufferType r = this.array;
    assert r != null;
    return r;
  }

  /**
   * @return The lower bounds of the mesh data.
   */

  public RVectorI3F<RSpaceObjectType> getBoundsLower()
  {
    this.checkParsingDone();
    final RVectorI3F<RSpaceObjectType> r = this.bounds_lower;
    assert r != null;
    return r;
  }

  /**
   * @return The upper bounds of the mesh data.
   */

  public RVectorI3F<RSpaceObjectType> getBoundsUpper()
  {
    this.checkParsingDone();
    final RVectorI3F<RSpaceObjectType> r = this.bounds_upper;
    assert r != null;
    return r;
  }

  /**
   * @return The index buffer.
   */

  public IndexBufferType getIndices()
  {
    this.checkParsingDone();
    final IndexBufferType r = this.indices;
    assert r != null;
    return r;
  }

  /**
   * @return The mesh name.
   */

  public String getName()
  {
    this.checkParsingDone();
    final String r = this.name;
    assert r != null;
    return r;
  }
}
