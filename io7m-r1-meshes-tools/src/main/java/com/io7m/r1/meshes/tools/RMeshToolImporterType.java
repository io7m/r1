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

package com.io7m.r1.meshes.tools;

import java.io.File;
import java.io.PrintWriter;

import com.io7m.jfunctional.OptionType;
import com.io7m.jlog.LogUsableType;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.meshes.RMeshTangents;

/**
 * The type of importers.
 */

public interface RMeshToolImporterType
{
  /**
   * @return The humanly-readable name of the format the importer supports.
   */

  String importerGetHumanName();

  /**
   * @return A short token uniquely identifying the importer format.
   */

  String importerGetShortName();

  /**
   * @return The file name suffix that identifies the format the importer
   *         supports.
   */

  String importerGetSuffix();

  /**
   * Import a mesh with the given name from the given file.
   *
   * @param file
   *          The file.
   * @param mesh_name
   *          The name of the mesh to import.
   * @param mesh_change_name
   *          The name that will replace that of the imported mesh.
   * @param log
   *          A log interface.
   *
   * @return A mesh.
   * @throws RException
   *           On errors.
   */

  RMeshTangents importFile(
    final File file,
    final String mesh_name,
    final OptionType<String> mesh_change_name,
    final LogUsableType log)
    throws RException;

  /**
   * Display information about all meshes contained within the given file.
   *
   * @param file
   *          The file.
   * @param output
   *          The output writer to which information will be written.
   * @param log
   *          A log interface.
   *
   * @throws RException
   *           On errors.
   */

  void showFile(
    final File file,
    final PrintWriter output,
    final LogUsableType log)
    throws RException;
}
