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

package com.io7m.renderer.kernel.programs;

import java.io.IOException;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jcanephora.JCGLCompileException;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLInterfaceCommon;
import com.io7m.jcanephora.JCGLUnsupportedException;
import com.io7m.jlog.Log;
import com.io7m.jvvfs.FSCapabilityRead;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.renderer.kernel.KRenderingCapabilities;
import com.io7m.renderer.kernel.KRenderingCapabilities.EnvironmentCapability;
import com.io7m.renderer.kernel.KRenderingCapabilities.NormalCapability;
import com.io7m.renderer.kernel.KRenderingCapabilities.SpecularCapability;
import com.io7m.renderer.kernel.KRenderingCapabilities.TextureCapability;
import com.io7m.renderer.kernel.KShadingProgram;

public enum KSPForwardUnlitPrograms
{
  KSP_FORWARD_UNLIT("fwd_U", new KRenderingCapabilities(
    TextureCapability.TEXTURE_CAP_NONE,
    NormalCapability.NORMAL_CAP_NONE,
    SpecularCapability.SPECULAR_CAP_NONE,
    EnvironmentCapability.ENVIRONMENT_NONE)),

  KSP_FORWARD_UNLIT_ENVIRONMENTMAPPED("fwd_U_E", new KRenderingCapabilities(
    TextureCapability.TEXTURE_CAP_NONE,
    NormalCapability.NORMAL_CAP_NONE,
    SpecularCapability.SPECULAR_CAP_NONE,
    EnvironmentCapability.ENVIRONMENT_MAPPED)),

  KSP_FORWARD_UNLIT_TEXTURED("fwd_U_T", new KRenderingCapabilities(
    TextureCapability.TEXTURE_CAP_DIFFUSE,
    NormalCapability.NORMAL_CAP_NONE,
    SpecularCapability.SPECULAR_CAP_NONE,
    EnvironmentCapability.ENVIRONMENT_NONE)),

  KSP_FORWARD_UNLIT_TEXTURED_ENVIRONMENTMAPPED(
    "fwd_U_T_E",
    new KRenderingCapabilities(
      TextureCapability.TEXTURE_CAP_DIFFUSE,
      NormalCapability.NORMAL_CAP_NONE,
      SpecularCapability.SPECULAR_CAP_NONE,
      EnvironmentCapability.ENVIRONMENT_MAPPED))

  ;

  public static @Nonnull KShadingProgram make(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull FSCapabilityRead fs,
    final @Nonnull Log log,
    final @Nonnull KSPForwardUnlitPrograms p)
    throws JCGLException,
      JCGLCompileException,
      JCGLUnsupportedException,
      FilesystemError,
      IOException,
      ConstraintError
  {
    switch (p) {
      case KSP_FORWARD_UNLIT:
        return KSPF_U.make(gc, fs, log);
      case KSP_FORWARD_UNLIT_TEXTURED:
        return KSPF_U_T.make(gc, fs, log);
      case KSP_FORWARD_UNLIT_ENVIRONMENTMAPPED:
        return KSPF_U_T_E.make(gc, fs, log);
      case KSP_FORWARD_UNLIT_TEXTURED_ENVIRONMENTMAPPED:
        return KSPF_U_E.make(gc, fs, log);
    }

    throw new UnreachableCodeException();
  }

  private final @Nonnull String                 name;
  private final @Nonnull KRenderingCapabilities required;

  private KSPForwardUnlitPrograms(
    final @Nonnull String name,
    final @Nonnull KRenderingCapabilities required)
  {
    this.name = name;
    this.required = required;
  }

  public @Nonnull String getName()
  {
    return this.name;
  }

  public @Nonnull KRenderingCapabilities getRequiredCapabilities()
  {
    return this.required;
  }
}
