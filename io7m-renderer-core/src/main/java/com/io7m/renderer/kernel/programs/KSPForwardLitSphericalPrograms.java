
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
import com.io7m.renderer.kernel.KRenderingCapabilities.NormalCapability;
import com.io7m.renderer.kernel.KRenderingCapabilities.SpecularCapability;
import com.io7m.renderer.kernel.KRenderingCapabilities.TextureCapability;
import com.io7m.renderer.kernel.KShadingProgramLightSpherical;

public enum KSPForwardLitSphericalPrograms
{
  /*
   * Spherical.
   */

  KSP_FORWARD_LIT_SPHERICAL("fwd_LS", new KRenderingCapabilities(
    TextureCapability.TEXTURE_CAP_NONE,
    NormalCapability.NORMAL_CAP_VERTEX,
    SpecularCapability.SPECULAR_CAP_CONSTANT)),

  KSP_FORWARD_LIT_SPHERICAL_TEXTURED(
    "fwd_LS_T",
    new KRenderingCapabilities(
      TextureCapability.TEXTURE_CAP_DIFFUSE,
      NormalCapability.NORMAL_CAP_VERTEX,
      SpecularCapability.SPECULAR_CAP_CONSTANT)),

  /*
   * Spherical, with specular terms.
   */

  KSP_FORWARD_LIT_SPHERICAL_SPECULAR(
    "fwd_LS_S",
    new KRenderingCapabilities(
      TextureCapability.TEXTURE_CAP_NONE,
      NormalCapability.NORMAL_CAP_VERTEX,
      SpecularCapability.SPECULAR_CAP_CONSTANT)),

  KSP_FORWARD_LIT_SPHERICAL_SPECULARMAPPED(
    "fwd_LS_SM",
    new KRenderingCapabilities(
      TextureCapability.TEXTURE_CAP_NONE,
      NormalCapability.NORMAL_CAP_VERTEX,
      SpecularCapability.SPECULAR_CAP_MAPPED)),

  KSP_FORWARD_LIT_SPHERICAL_TEXTURED_SPECULAR(
    "fwd_LS_T_S",
    new KRenderingCapabilities(
      TextureCapability.TEXTURE_CAP_DIFFUSE,
      NormalCapability.NORMAL_CAP_VERTEX,
      SpecularCapability.SPECULAR_CAP_CONSTANT)),

  KSP_FORWARD_LIT_SPHERICAL_TEXTURED_SPECULARMAPPED(
    "fwd_LS_T_SM",
    new KRenderingCapabilities(
      TextureCapability.TEXTURE_CAP_DIFFUSE,
      NormalCapability.NORMAL_CAP_VERTEX,
      SpecularCapability.SPECULAR_CAP_MAPPED)),

  /*
   * Spherical, with normal mapping.
   */

  KSP_FORWARD_LIT_SPHERICAL_NORMALMAPPED(
    "fwd_LS_N",
    new KRenderingCapabilities(
      TextureCapability.TEXTURE_CAP_NONE,
      NormalCapability.NORMAL_CAP_MAPPED,
      SpecularCapability.SPECULAR_CAP_CONSTANT)),

  KSP_FORWARD_LIT_SPHERICAL_TEXTURED_NORMALMAPPED(
    "fwd_LS_T_N",
    new KRenderingCapabilities(
      TextureCapability.TEXTURE_CAP_DIFFUSE,
      NormalCapability.NORMAL_CAP_MAPPED,
      SpecularCapability.SPECULAR_CAP_CONSTANT)),

  /*
   * Spherical, with normal mapping, with specular terms.
   */

  KSP_FORWARD_LIT_SPHERICAL_SPECULAR_NORMALMAPPED(
    "fwd_LS_S_N",
    new KRenderingCapabilities(
      TextureCapability.TEXTURE_CAP_NONE,
      NormalCapability.NORMAL_CAP_MAPPED,
      SpecularCapability.SPECULAR_CAP_CONSTANT)),

  KSP_FORWARD_LIT_SPHERICAL_SPECULARMAPPED_NORMALMAPPED(
    "fwd_LS_SM_N",
    new KRenderingCapabilities(
      TextureCapability.TEXTURE_CAP_NONE,
      NormalCapability.NORMAL_CAP_MAPPED,
      SpecularCapability.SPECULAR_CAP_MAPPED)),

  KSP_FORWARD_LIT_SPHERICAL_TEXTURED_SPECULAR_NORMALMAPPED(
    "fwd_LS_T_S_N",
    new KRenderingCapabilities(
      TextureCapability.TEXTURE_CAP_DIFFUSE,
      NormalCapability.NORMAL_CAP_MAPPED,
      SpecularCapability.SPECULAR_CAP_CONSTANT)),

  KSP_FORWARD_LIT_SPHERICAL_TEXTURED_SPECULARMAPPED_NORMALMAPPED(
    "fwd_LS_T_SM_N",
    new KRenderingCapabilities(
      TextureCapability.TEXTURE_CAP_DIFFUSE,
      NormalCapability.NORMAL_CAP_MAPPED,
      SpecularCapability.SPECULAR_CAP_MAPPED))

  ;

  private final @Nonnull String                    name;
  private final @Nonnull KRenderingCapabilities required;

  private KSPForwardLitSphericalPrograms(
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

  public static @Nonnull KShadingProgramLightSpherical make(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull FSCapabilityRead fs,
    final @Nonnull Log log,
    final @Nonnull KSPForwardLitSphericalPrograms p)
    throws JCGLException,
      JCGLCompileException,
      JCGLUnsupportedException,
      FilesystemError,
      IOException,
      ConstraintError
  {
    switch (p) {
      case KSP_FORWARD_LIT_SPHERICAL:
        return new KSPF_LS(gc, fs, log);
      case KSP_FORWARD_LIT_SPHERICAL_NORMALMAPPED:
        return new KSPF_LS_NM(gc, fs, log);
      case KSP_FORWARD_LIT_SPHERICAL_SPECULAR:
        return new KSPF_LS_S(gc, fs, log);
      case KSP_FORWARD_LIT_SPHERICAL_SPECULARMAPPED:
        return new KSPF_LS_SM(gc, fs, log);
      case KSP_FORWARD_LIT_SPHERICAL_SPECULARMAPPED_NORMALMAPPED:
        return new KSPF_LS_SM_NM(gc, fs, log);
      case KSP_FORWARD_LIT_SPHERICAL_SPECULAR_NORMALMAPPED:
        return new KSPF_LS_S_NM(gc, fs, log);
      case KSP_FORWARD_LIT_SPHERICAL_TEXTURED:
        return new KSPF_LS_T(gc, fs, log);
      case KSP_FORWARD_LIT_SPHERICAL_TEXTURED_NORMALMAPPED:
        return new KSPF_LS_T_NM(gc, fs, log);
      case KSP_FORWARD_LIT_SPHERICAL_TEXTURED_SPECULAR:
        return new KSPF_LS_T_S(gc, fs, log);
      case KSP_FORWARD_LIT_SPHERICAL_TEXTURED_SPECULARMAPPED:
        return new KSPF_LS_T_SM(gc, fs, log);
      case KSP_FORWARD_LIT_SPHERICAL_TEXTURED_SPECULARMAPPED_NORMALMAPPED:
        return new KSPF_LS_T_SM_NM(gc, fs, log);
      case KSP_FORWARD_LIT_SPHERICAL_TEXTURED_SPECULAR_NORMALMAPPED:
        return new KSPF_LS_T_S_NM(gc, fs, log);
    }

    throw new UnreachableCodeException();
  }

  public @Nonnull KRenderingCapabilities getRequiredCapabilities()
  {
    return this.required;
  }
}
