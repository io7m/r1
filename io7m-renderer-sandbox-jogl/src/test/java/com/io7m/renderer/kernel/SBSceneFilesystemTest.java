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

package com.io7m.renderer.kernel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jlog.Log;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.jvvfs.PathVirtual;
import com.io7m.renderer.xml.RXMLException;

public class SBSceneFilesystemTest
{
  @SuppressWarnings("static-method") @Test public void testCopying()
    throws FilesystemError,
      ConstraintError,
      IOException,
      ValidityException,
      ParsingException,
      RXMLException
  {
    final Log log = TestUtilities.getLog();

    final SBSceneFilesystem s = SBSceneFilesystem.filesystemEmpty(log);

    final File td =
      SBIOUtilities.makeTemporaryDirectory(
        log,
        "sandbox-test/sandbox-test-",
        100);
    final File td_out =
      SBIOUtilities.makeTemporaryDirectory(
        log,
        "sandbox-test/sandbox-test-out-",
        100);

    final File mf = new File(td, "sphere_16_8_mesh.rmx");
    final File tf = new File(td, "error.png");

    final File cx = new File(td, "cube.xml");
    final File pz = new File(td, "positive_z.png");
    final File nz = new File(td, "negative_z.png");
    final File py = new File(td, "positive_y.png");
    final File ny = new File(td, "negative_y.png");
    final File px = new File(td, "positive_x.png");
    final File nx = new File(td, "negative_x.png");

    SBSceneFilesystemTest.copy("/sphere_16_8_mesh.rmx", mf);
    SBSceneFilesystemTest.copy("/error.png", tf);
    SBSceneFilesystemTest.copy("/sky0/cube.xml", cx);
    SBSceneFilesystemTest.copy("/sky0/positive_z.png", pz);
    SBSceneFilesystemTest.copy("/sky0/negative_z.png", nz);
    SBSceneFilesystemTest.copy("/sky0/positive_y.png", py);
    SBSceneFilesystemTest.copy("/sky0/negative_y.png", ny);
    SBSceneFilesystemTest.copy("/sky0/positive_x.png", px);
    SBSceneFilesystemTest.copy("/sky0/negative_x.png", nx);

    s.filesystemCopyInMesh(mf);
    s.filesystemCopyInMesh(mf);
    s.filesystemCopyInTexture2D(tf);
    s.filesystemCopyInTexture2D(tf);
    s.filesystemCopyInTextureCube(cx);
    s.filesystemCopyInTextureCube(cx);

    final InputStream mfs =
      s.filesystemOpenFile(PathVirtual.ofString("/meshes/sphere_16_8_mesh.rmx"));
    mfs.close();

    {
      final File meshes = new File(s.root(), "meshes");
      final File textures = new File(s.root(), "textures");
      final File cubemaps = new File(s.root(), "cubemaps");
      Assert.assertEquals(2, meshes.list().length);
      Assert.assertEquals(2, textures.list().length);
      Assert.assertEquals(2, cubemaps.list().length);
    }

    final File out_file = new File(td_out, "scene.zs");
    s.filesystemSave(SBSceneDescription.empty(), out_file);
    Assert.assertTrue(out_file.isFile());
  }

  private static void copy(
    final String name,
    final File f)
    throws FileNotFoundException,
      IOException
  {
    SBIOUtilities.copyStreamOut(
      SBSceneFilesystemTest.class.getResourceAsStream(name),
      f);
  }
}
