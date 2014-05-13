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

import com.io7m.jnull.NullCheck;
import com.io7m.jproperties.JProperties;
import com.io7m.jproperties.JPropertyNonexistent;

final class ViewerConfig
{
  public static ViewerConfig fromProperties(
    final Properties props)
    throws JPropertyNonexistent
  {
    return new ViewerConfig(props);
  }

  private final Properties props;
  private final File       replacement_results_directory;
  private final File       shader_archive_debug_file;
  private final File       shader_archive_depth_file;
  private final File       shader_archive_forward_file;
  private final File       shader_archive_postprocessing_file;

  public ViewerConfig(
    final Properties in_props)
    throws JPropertyNonexistent
  {
    this.props = NullCheck.notNull(in_props, "Properties");

    this.shader_archive_debug_file =
      new File(JProperties.getString(
        in_props,
        "com.io7m.renderer.kernel.examples.viewer.shaders.debug"));
    this.shader_archive_depth_file =
      new File(JProperties.getString(
        in_props,
        "com.io7m.renderer.kernel.examples.viewer.shaders.depth"));
    this.shader_archive_forward_file =
      new File(JProperties.getString(
        in_props,
        "com.io7m.renderer.kernel.examples.viewer.shaders.forward"));
    this.shader_archive_postprocessing_file =
      new File(JProperties.getString(
        in_props,
        "com.io7m.renderer.kernel.examples.viewer.shaders.postprocessing"));

    this.replacement_results_directory =
      new File(
        JProperties
          .getString(
            in_props,
            "com.io7m.renderer.kernel.examples.viewer.replacement-results-directory"));
  }

  public File getReplacementResultsDirectory()
  {
    return this.replacement_results_directory;
  }

  public File getShaderArchiveDebugFile()
  {
    return this.shader_archive_debug_file;
  }

  public File getShaderArchiveDepthFile()
  {
    return this.shader_archive_depth_file;
  }

  public File getShaderArchiveForwardFile()
  {
    return this.shader_archive_forward_file;
  }

  public File getShaderArchivePostprocessingFile()
  {
    return this.shader_archive_postprocessing_file;
  }
}
