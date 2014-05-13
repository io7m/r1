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

package com.io7m.renderer.kernel.sandbox;

import java.io.File;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import com.io7m.jnull.NullCheck;
import com.io7m.jproperties.JProperties;
import com.io7m.jproperties.JPropertyException;

public final class SandboxConfig
{
  public static SandboxConfig fromProperties(
    final Properties props)
    throws JPropertyException
  {
    return new SandboxConfig(props);
  }

  private static Set<String> split(
    final String text)
  {
    final HashSet<String> names = new HashSet<String>();
    final String[] segments = text.split("\\s+");
    for (final String name : segments) {
      names.add(name.trim());
    }
    return names;
  }

  private final Set<String>     hide_extensions;
  private final boolean         opengl_debug;
  private final boolean         opengl_trace;
  private final SBOpenGLProfile profile;
  private final Properties      props;
  private final long            restrict_units;
  private final File            shader_archive_debug_file;
  private final File            shader_archive_depth_file;
  private final File            shader_archive_forward_file;
  private final File            shader_archive_postprocessing_file;

  public SandboxConfig(
    final Properties in_props)
    throws JPropertyException
  {
    this.props = NullCheck.notNull(in_props, "Properties");

    this.shader_archive_debug_file =
      new File(JProperties.getString(
        in_props,
        "com.io7m.renderer.sandbox.shaders.debug"));
    this.shader_archive_depth_file =
      new File(JProperties.getString(
        in_props,
        "com.io7m.renderer.sandbox.shaders.depth"));
    this.shader_archive_forward_file =
      new File(JProperties.getString(
        in_props,
        "com.io7m.renderer.sandbox.shaders.forward"));
    this.shader_archive_postprocessing_file =
      new File(JProperties.getString(
        in_props,
        "com.io7m.renderer.sandbox.shaders.postprocessing"));

    this.opengl_debug =
      JProperties.getBooleanOptional(
        in_props,
        "com.io7m.renderer.sandbox.opengl.debug",
        true);
    this.opengl_trace =
      JProperties.getBooleanOptional(
        in_props,
        "com.io7m.renderer.sandbox.opengl.trace",
        false);
    this.hide_extensions =
      SandboxConfig.split(JProperties.getStringOptional(
        in_props,
        "com.io7m.renderer.sandbox.opengl.hide_extensions",
        ""));
    this.restrict_units =
      JProperties.getBigIntegerOptional(
        in_props,
        "com.io7m.renderer.sandbox.opengl.texture_units",
        BigInteger.valueOf(-1)).intValue();
    this.profile =
      SBOpenGLProfile.fromString(JProperties.getString(
        in_props,
        "com.io7m.renderer.sandbox.opengl.profile"));
  }

  public Set<String> getOpenGLHideExtensions()
  {
    return Collections.unmodifiableSet(this.hide_extensions);
  }

  public SBOpenGLProfile getOpenGLProfile()
  {
    return this.profile;
  }

  public long getOpenGLRestrictTextureUnits()
  {
    return this.restrict_units;
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

  public boolean isOpenGLDebug()
  {
    return this.opengl_debug;
  }

  public boolean isOpenGLTrace()
  {
    return this.opengl_trace;
  }
}
