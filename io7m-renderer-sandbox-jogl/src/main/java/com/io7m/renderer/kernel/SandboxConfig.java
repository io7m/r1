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
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.PropertyUtils;
import com.io7m.jaux.PropertyUtils.ValueIncorrectType;
import com.io7m.jaux.PropertyUtils.ValueNotFound;

public final class SandboxConfig
{
  public static @Nonnull SandboxConfig fromProperties(
    final @Nonnull Properties props)
    throws ConstraintError,
      ValueNotFound,
      ValueIncorrectType
  {
    return new SandboxConfig(props);
  }

  private static Set<String> split(
    final @Nonnull String text)
  {
    final HashSet<String> names = new HashSet<String>();
    final String[] segments = text.split("\\s+");
    for (final String name : segments) {
      names.add(name.trim());
    }
    return names;
  }

  private final @Nonnull Set<String>     hide_extensions;
  private final boolean                  opengl_debug;
  private final boolean                  opengl_trace;
  private final @Nonnull SBOpenGLProfile profile;
  private final @Nonnull Properties      props;
  private final long                     restrict_units;
  private final @Nonnull File            shader_archive_debug_file;
  private final @Nonnull File            shader_archive_depth_file;
  private final @Nonnull File            shader_archive_forward_file;
  private final @Nonnull File            shader_archive_postprocessing_file;
  private final @Nonnull File            shader_archive_shadow_file;

  public SandboxConfig(
    final @Nonnull Properties props)
    throws ConstraintError,
      ValueNotFound,
      ValueIncorrectType
  {
    this.props = Constraints.constrainNotNull(props, "Properties");

    this.shader_archive_debug_file =
      new File(PropertyUtils.getString(
        props,
        "com.io7m.renderer.sandbox.shaders.debug"));
    this.shader_archive_depth_file =
      new File(PropertyUtils.getString(
        props,
        "com.io7m.renderer.sandbox.shaders.depth"));
    this.shader_archive_shadow_file =
      new File(PropertyUtils.getString(
        props,
        "com.io7m.renderer.sandbox.shaders.shadow"));
    this.shader_archive_forward_file =
      new File(PropertyUtils.getString(
        props,
        "com.io7m.renderer.sandbox.shaders.forward"));
    this.shader_archive_postprocessing_file =
      new File(PropertyUtils.getString(
        props,
        "com.io7m.renderer.sandbox.shaders.postprocessing"));

    this.opengl_debug =
      PropertyUtils.getOptionalBoolean(
        props,
        "com.io7m.renderer.sandbox.opengl.debug",
        true);
    this.opengl_trace =
      PropertyUtils.getOptionalBoolean(
        props,
        "com.io7m.renderer.sandbox.opengl.trace",
        false);
    this.hide_extensions =
      SandboxConfig.split(PropertyUtils.getOptionalString(
        props,
        "com.io7m.renderer.sandbox.opengl.hide_extensions",
        ""));
    this.restrict_units =
      PropertyUtils.getOptionalInteger(
        props,
        "com.io7m.renderer.sandbox.opengl.texture_units",
        -1);
    this.profile =
      SBOpenGLProfile.fromString(PropertyUtils.getString(
        props,
        "com.io7m.renderer.sandbox.opengl.profile"));
  }

  public @Nonnull Set<String> getOpenGLHideExtensions()
  {
    return Collections.unmodifiableSet(this.hide_extensions);
  }

  public @Nonnull SBOpenGLProfile getOpenGLProfile()
  {
    return this.profile;
  }

  public long getOpenGLRestrictTextureUnits()
  {
    return this.restrict_units;
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

  public @Nonnull File getShaderArchiveShadowFile()
  {
    return this.shader_archive_shadow_file;
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
