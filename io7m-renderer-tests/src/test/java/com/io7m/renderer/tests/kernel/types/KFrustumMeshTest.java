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

package com.io7m.renderer.tests.kernel.types;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jcanephora.ArrayBufferUsableType;
import com.io7m.jcanephora.IndexBufferUsableType;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.renderer.kernel.types.KFrustumMesh;
import com.io7m.renderer.kernel.types.KProjectionFOV;
import com.io7m.renderer.kernel.types.KProjectionFrustum;
import com.io7m.renderer.kernel.types.KProjectionOrthographic;
import com.io7m.renderer.tests.FakeJCGLArrayAndIndexBuffers;

@SuppressWarnings("static-method") public final class KFrustumMeshTest
{
  @Test public void testFOV()
    throws Exception
  {
    final FakeJCGLArrayAndIndexBuffers g = new FakeJCGLArrayAndIndexBuffers();

    final MatrixM4x4F mat = new MatrixM4x4F();
    final KProjectionFOV p =
      KProjectionFOV.newProjection(
        mat,
        (float) Math.toRadians(90.0f),
        1.0f,
        1.0f,
        10.0f);
    final KFrustumMesh m = KFrustumMesh.newFromFOV(g, p);

    Assert.assertEquals(-1.0f, m.getNearXMinimum(), 0.0f);
    Assert.assertEquals(1.0f, m.getNearXMaximum(), 0.0f);
    Assert.assertEquals(-1.0f, m.getNearYMinimum(), 0.0f);
    Assert.assertEquals(1.0f, m.getNearYMaximum(), 0.0f);
    Assert.assertEquals(-1.0f, m.getNearZ(), 0.0f);

    Assert.assertEquals(-10.0f, m.getFarXMinimum(), 0.0f);
    Assert.assertEquals(10.0f, m.getFarXMaximum(), 0.0f);
    Assert.assertEquals(-10.0f, m.getFarYMinimum(), 0.0f);
    Assert.assertEquals(10.0f, m.getFarYMaximum(), 0.0f);
    Assert.assertEquals(-10.0f, m.getFarZ(), 0.0f);

    final IndexBufferUsableType mi = m.getIndices();
    final ArrayBufferUsableType ma = m.getArray();
    Assert.assertEquals(
      KFrustumMesh.getFrustumMeshSizeBytes(),
      ma.resourceGetSizeBytes() + mi.resourceGetSizeBytes());
  }

  @Test public void testFrustum()
    throws Exception
  {
    final FakeJCGLArrayAndIndexBuffers g = new FakeJCGLArrayAndIndexBuffers();

    final MatrixM4x4F mat = new MatrixM4x4F();
    final KProjectionFrustum p =
      KProjectionFrustum.newProjection(
        mat,
        -1.0f,
        1.0f,
        -2.0f,
        2.0f,
        1.0f,
        10.0f);
    final KFrustumMesh m = KFrustumMesh.newFromFrustum(g, p);

    Assert.assertEquals(-1.0f, m.getNearXMinimum(), 0.0f);
    Assert.assertEquals(1.0f, m.getNearXMaximum(), 0.0f);
    Assert.assertEquals(-2.0f, m.getNearYMinimum(), 0.0f);
    Assert.assertEquals(2.0f, m.getNearYMaximum(), 0.0f);
    Assert.assertEquals(-1.0f, m.getNearZ(), 0.0f);

    Assert.assertEquals(-10.0f, m.getFarXMinimum(), 0.0f);
    Assert.assertEquals(10.0f, m.getFarXMaximum(), 0.0f);
    Assert.assertEquals(-20.0f, m.getFarYMinimum(), 0.0f);
    Assert.assertEquals(20.0f, m.getFarYMaximum(), 0.0f);
    Assert.assertEquals(-10.0f, m.getFarZ(), 0.0f);

    final IndexBufferUsableType mi = m.getIndices();
    final ArrayBufferUsableType ma = m.getArray();
    Assert.assertEquals(
      KFrustumMesh.getFrustumMeshSizeBytes(),
      ma.resourceGetSizeBytes() + mi.resourceGetSizeBytes());
  }

  @Test public void testOrthographic()
    throws Exception
  {
    final FakeJCGLArrayAndIndexBuffers g = new FakeJCGLArrayAndIndexBuffers();

    final MatrixM4x4F mat = new MatrixM4x4F();
    final KProjectionOrthographic p =
      KProjectionOrthographic.newProjection(
        mat,
        -2.0f,
        2.0f,
        -3.0f,
        3.0f,
        1.0f,
        10.0f);
    final KFrustumMesh m = KFrustumMesh.newFromOrthographic(g, p);

    Assert.assertEquals(-2.0f, m.getNearXMinimum(), 0.0f);
    Assert.assertEquals(2.0f, m.getNearXMaximum(), 0.0f);
    Assert.assertEquals(-3.0f, m.getNearYMinimum(), 0.0f);
    Assert.assertEquals(3.0f, m.getNearYMaximum(), 0.0f);
    Assert.assertEquals(-1.0f, m.getNearZ(), 0.0f);

    Assert.assertEquals(-2.0f, m.getFarXMinimum(), 0.0f);
    Assert.assertEquals(2.0f, m.getFarXMaximum(), 0.0f);
    Assert.assertEquals(-3.0f, m.getFarYMinimum(), 0.0f);
    Assert.assertEquals(3.0f, m.getFarYMaximum(), 0.0f);
    Assert.assertEquals(-10.0f, m.getFarZ(), 0.0f);

    final IndexBufferUsableType mi = m.getIndices();
    final ArrayBufferUsableType ma = m.getArray();
    Assert.assertEquals(
      KFrustumMesh.getFrustumMeshSizeBytes(),
      ma.resourceGetSizeBytes() + mi.resourceGetSizeBytes());
  }
}
