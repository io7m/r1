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

package com.io7m.renderer.kernel.examples.viewer;

import java.io.File;
import java.util.Properties;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.PropertyUtils;
import com.io7m.jaux.PropertyUtils.ValueNotFound;

final class ViewerConfig
{
  public static @Nonnull ViewerConfig fromProperties(
    final @Nonnull Properties props)
    throws ConstraintError,
      ValueNotFound
  {
    return new ViewerConfig(props);
  }

  private final @Nonnull Properties props;
  private final @Nonnull File       replacement_results_directory;
  private final @Nonnull File       shader_archive_debug_file;
  private final @Nonnull File       shader_archive_depth_file;
  private final @Nonnull File       shader_archive_forward_file;
  private final @Nonnull File       shader_archive_postprocessing_file;

  public ViewerConfig(
    final @Nonnull Properties in_props)
    throws ConstraintError,
      ValueNotFound
  {
    this.props = Constraints.constrainNotNull(in_props, "Properties");

    this.shader_archive_debug_file =
      new File(PropertyUtils.getString(
        in_props,
        "com.io7m.renderer.kernel.examples.viewer.shaders.debug"));
    this.shader_archive_depth_file =
      new File(PropertyUtils.getString(
        in_props,
        "com.io7m.renderer.kernel.examples.viewer.shaders.depth"));
    this.shader_archive_forward_file =
      new File(PropertyUtils.getString(
        in_props,
        "com.io7m.renderer.kernel.examples.viewer.shaders.forward"));
    this.shader_archive_postprocessing_file =
      new File(PropertyUtils.getString(
        in_props,
        "com.io7m.renderer.kernel.examples.viewer.shaders.postprocessing"));

    this.replacement_results_directory =
      new File(
        PropertyUtils
          .getString(
            in_props,
            "com.io7m.renderer.kernel.examples.viewer.replacement-results-directory"));
  }

  public @Nonnull File getReplacementResultsDirectory()
  {
    return this.replacement_results_directory;
  }

  public @Nonnull File getShaderArchiveDebugFile()
  {
    return this.shader_archive_debug_file;
  }

  public @Nonnull File getShaderArchiveDepthFile()
  {
    return this.shader_archive_depth_file;
  }

  public @Nonnull File getShaderArchiveForwardFile()
  {
    return this.shader_archive_forward_file;
  }

  public @Nonnull File getShaderArchivePostprocessingFile()
  {
    return this.shader_archive_postprocessing_file;
  }
}
