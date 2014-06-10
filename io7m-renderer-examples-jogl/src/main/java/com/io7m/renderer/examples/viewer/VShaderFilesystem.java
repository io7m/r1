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

package com.io7m.renderer.examples.viewer;

import java.io.File;

import com.io7m.jlog.LogUsableType;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.jvvfs.FilesystemType;
import com.io7m.renderer.kernel.KShaderPaths;

/**
 * Code to initialize the shader filesystem.
 */

public final class VShaderFilesystem
{
  private static File makeShaderArchiveNameAssembled(
    final ViewerConfig config,
    final String name)
  {
    final StringBuilder s = new StringBuilder();
    s.append("lib/io7m-renderer-shaders-");
    s.append(name);
    s.append("-");
    s.append(config.getProgramVersion());
    s.append("-shaders.zip");
    return new File(s.toString());
  }

  private static File makeShaderArchiveNameEclipse(
    final ViewerConfig config,
    final String name)
  {
    final StringBuilder s = new StringBuilder();
    final String base = System.getenv("ECLIPSE_EXEC_DIR");
    if (base == null) {
      throw new IllegalStateException("ECLIPSE_EXEC_DIR is unset");
    }

    s.append(base);
    s.append("/io7m-renderer-shaders-");
    s.append(name);
    s.append("/target/io7m-renderer-shaders-");
    s.append(name);
    s.append("-");
    s.append(config.getProgramVersion());
    s.append("-shaders.zip");
    return new File(s.toString());
  }

  /**
   * Initialize the shader filesystem based on the viewer config.
   * 
   * @param config
   *          The config.
   * @param fs
   *          The filesystem.
   * @param log
   *          A log interface.
   * @throws FilesystemError
   *           On filesystem errors.
   */

  public static void setupShaderFilesystem(
    final ViewerConfig config,
    final FilesystemType fs,
    final LogUsableType log)
    throws FilesystemError
  {
    fs.createDirectory(KShaderPaths.PATH_DEBUG);
    fs.createDirectory(KShaderPaths.PATH_DEFERRED_GEOMETRY);
    fs.createDirectory(KShaderPaths.PATH_DEFERRED_LIGHT);
    fs.createDirectory(KShaderPaths.PATH_DEPTH);
    fs.createDirectory(KShaderPaths.PATH_DEPTH_VARIANCE);
    fs.createDirectory(KShaderPaths.PATH_FORWARD_OPAQUE_LIT);
    fs.createDirectory(KShaderPaths.PATH_FORWARD_OPAQUE_UNLIT);
    fs.createDirectory(KShaderPaths.PATH_FORWARD_TRANSLUCENT_LIT);
    fs.createDirectory(KShaderPaths.PATH_FORWARD_TRANSLUCENT_UNLIT);
    fs.createDirectory(KShaderPaths.PATH_POSTPROCESSING);

    /**
     * If running from eclipse, alternate measures have to be taken to set up
     * the shader filesystem, because the program's not running from a neatly
     * arranged assembly directory.
     */

    if (config.isEclipse()) {
      log
        .info("Running under eclipse - loading shaders from target directories");

      fs.mountArchiveFromAnywhere(
        VShaderFilesystem.makeShaderArchiveNameEclipse(config, "debug"),
        KShaderPaths.PATH_DEBUG);

      fs.mountArchiveFromAnywhere(
        VShaderFilesystem.makeShaderArchiveNameEclipse(
          config,
          "deferred-geometry"),
        KShaderPaths.PATH_DEFERRED_GEOMETRY);

      fs.mountArchiveFromAnywhere(
        VShaderFilesystem.makeShaderArchiveNameEclipse(config, "depth"),
        KShaderPaths.PATH_DEPTH);
      fs.mountArchiveFromAnywhere(
        VShaderFilesystem.makeShaderArchiveNameEclipse(
          config,
          "depth_variance"),
        KShaderPaths.PATH_DEPTH_VARIANCE);
      fs.mountArchiveFromAnywhere(
        VShaderFilesystem.makeShaderArchiveNameEclipse(
          config,
          "forward-opaque-lit"),
        KShaderPaths.PATH_FORWARD_OPAQUE_LIT);
      fs.mountArchiveFromAnywhere(
        VShaderFilesystem.makeShaderArchiveNameEclipse(
          config,
          "forward-opaque-unlit"),
        KShaderPaths.PATH_FORWARD_OPAQUE_UNLIT);
      fs.mountArchiveFromAnywhere(
        VShaderFilesystem.makeShaderArchiveNameEclipse(
          config,
          "forward-translucent-lit"),
        KShaderPaths.PATH_FORWARD_TRANSLUCENT_LIT);
      fs.mountArchiveFromAnywhere(
        VShaderFilesystem.makeShaderArchiveNameEclipse(
          config,
          "forward-translucent-unlit"),
        KShaderPaths.PATH_FORWARD_TRANSLUCENT_UNLIT);
      fs.mountArchiveFromAnywhere(
        VShaderFilesystem.makeShaderArchiveNameEclipse(
          config,
          "postprocessing"),
        KShaderPaths.PATH_POSTPROCESSING);
    } else {
      fs.mountArchiveFromAnywhere(
        VShaderFilesystem.makeShaderArchiveNameAssembled(config, "debug"),
        KShaderPaths.PATH_DEBUG);

      fs.mountArchiveFromAnywhere(
        VShaderFilesystem.makeShaderArchiveNameAssembled(
          config,
          "deferred-geometry"),
        KShaderPaths.PATH_DEFERRED_GEOMETRY);
      fs.mountArchiveFromAnywhere(
        VShaderFilesystem.makeShaderArchiveNameAssembled(config, "depth"),
        KShaderPaths.PATH_DEPTH);
      fs.mountArchiveFromAnywhere(
        VShaderFilesystem.makeShaderArchiveNameAssembled(
          config,
          "depth_variance"),
        KShaderPaths.PATH_DEPTH_VARIANCE);
      fs.mountArchiveFromAnywhere(
        VShaderFilesystem.makeShaderArchiveNameAssembled(
          config,
          "forward-opaque-lit"),
        KShaderPaths.PATH_FORWARD_OPAQUE_LIT);
      fs.mountArchiveFromAnywhere(
        VShaderFilesystem.makeShaderArchiveNameAssembled(
          config,
          "forward-opaque-unlit"),
        KShaderPaths.PATH_FORWARD_OPAQUE_UNLIT);
      fs.mountArchiveFromAnywhere(
        VShaderFilesystem.makeShaderArchiveNameAssembled(
          config,
          "forward-translucent-lit"),
        KShaderPaths.PATH_FORWARD_TRANSLUCENT_LIT);
      fs.mountArchiveFromAnywhere(
        VShaderFilesystem.makeShaderArchiveNameAssembled(
          config,
          "forward-translucent-unlit"),
        KShaderPaths.PATH_FORWARD_TRANSLUCENT_UNLIT);
      fs.mountArchiveFromAnywhere(
        VShaderFilesystem.makeShaderArchiveNameAssembled(
          config,
          "postprocessing"),
        KShaderPaths.PATH_POSTPROCESSING);
    }
  }

  private VShaderFilesystem()
  {
    throw new UnreachableCodeException();
  }
}
