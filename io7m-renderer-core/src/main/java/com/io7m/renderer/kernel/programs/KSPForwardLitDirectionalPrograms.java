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
import com.io7m.renderer.kernel.KShadingProgramLightDirectional;

public enum KSPForwardLitDirectionalPrograms
{
  /*
   * Directional.
   */

  KSP_FORWARD_LIT_DIRECTIONAL("fwd_LD", new KRenderingCapabilities(
    TextureCapability.TEXTURE_CAP_NONE,
    NormalCapability.NORMAL_CAP_VERTEX,
    SpecularCapability.SPECULAR_CAP_CONSTANT,
    EnvironmentCapability.ENVIRONMENT_NONE)),

  KSP_FORWARD_LIT_DIRECTIONAL_TEXTURED(
    "fwd_LD_T",
    new KRenderingCapabilities(
      TextureCapability.TEXTURE_CAP_DIFFUSE,
      NormalCapability.NORMAL_CAP_VERTEX,
      SpecularCapability.SPECULAR_CAP_CONSTANT,
      EnvironmentCapability.ENVIRONMENT_NONE)),

  /*
   * Directional, with specular terms.
   */

  KSP_FORWARD_LIT_DIRECTIONAL_SPECULAR(
    "fwd_LD_S",
    new KRenderingCapabilities(
      TextureCapability.TEXTURE_CAP_NONE,
      NormalCapability.NORMAL_CAP_VERTEX,
      SpecularCapability.SPECULAR_CAP_CONSTANT,
      EnvironmentCapability.ENVIRONMENT_NONE)),

  KSP_FORWARD_LIT_DIRECTIONAL_SPECULARMAPPED(
    "fwd_LD_SM",
    new KRenderingCapabilities(
      TextureCapability.TEXTURE_CAP_NONE,
      NormalCapability.NORMAL_CAP_VERTEX,
      SpecularCapability.SPECULAR_CAP_MAPPED,
      EnvironmentCapability.ENVIRONMENT_NONE)),

  KSP_FORWARD_LIT_DIRECTIONAL_TEXTURED_SPECULAR(
    "fwd_LD_T_S",
    new KRenderingCapabilities(
      TextureCapability.TEXTURE_CAP_DIFFUSE,
      NormalCapability.NORMAL_CAP_VERTEX,
      SpecularCapability.SPECULAR_CAP_CONSTANT,
      EnvironmentCapability.ENVIRONMENT_NONE)),

  KSP_FORWARD_LIT_DIRECTIONAL_TEXTURED_SPECULARMAPPED(
    "fwd_LD_T_SM",
    new KRenderingCapabilities(
      TextureCapability.TEXTURE_CAP_DIFFUSE,
      NormalCapability.NORMAL_CAP_VERTEX,
      SpecularCapability.SPECULAR_CAP_MAPPED,
      EnvironmentCapability.ENVIRONMENT_NONE)),

  /*
   * Directional, with normal mapping.
   */

  KSP_FORWARD_LIT_DIRECTIONAL_NORMALMAPPED(
    "fwd_LD_N",
    new KRenderingCapabilities(
      TextureCapability.TEXTURE_CAP_NONE,
      NormalCapability.NORMAL_CAP_MAPPED,
      SpecularCapability.SPECULAR_CAP_CONSTANT,
      EnvironmentCapability.ENVIRONMENT_NONE)),

  KSP_FORWARD_LIT_DIRECTIONAL_TEXTURED_NORMALMAPPED(
    "fwd_LD_T_N",
    new KRenderingCapabilities(
      TextureCapability.TEXTURE_CAP_DIFFUSE,
      NormalCapability.NORMAL_CAP_MAPPED,
      SpecularCapability.SPECULAR_CAP_CONSTANT,
      EnvironmentCapability.ENVIRONMENT_NONE)),

  /*
   * Directional, with normal mapping, with specular terms.
   */

  KSP_FORWARD_LIT_DIRECTIONAL_SPECULAR_NORMALMAPPED(
    "fwd_LD_S_N",
    new KRenderingCapabilities(
      TextureCapability.TEXTURE_CAP_NONE,
      NormalCapability.NORMAL_CAP_MAPPED,
      SpecularCapability.SPECULAR_CAP_CONSTANT,
      EnvironmentCapability.ENVIRONMENT_NONE)),

  KSP_FORWARD_LIT_DIRECTIONAL_SPECULARMAPPED_NORMALMAPPED(
    "fwd_LD_SM_N",
    new KRenderingCapabilities(
      TextureCapability.TEXTURE_CAP_NONE,
      NormalCapability.NORMAL_CAP_MAPPED,
      SpecularCapability.SPECULAR_CAP_MAPPED,
      EnvironmentCapability.ENVIRONMENT_NONE)),

  KSP_FORWARD_LIT_DIRECTIONAL_TEXTURED_SPECULAR_NORMALMAPPED(
    "fwd_LD_T_S_N",
    new KRenderingCapabilities(
      TextureCapability.TEXTURE_CAP_DIFFUSE,
      NormalCapability.NORMAL_CAP_MAPPED,
      SpecularCapability.SPECULAR_CAP_CONSTANT,
      EnvironmentCapability.ENVIRONMENT_NONE)),

  KSP_FORWARD_LIT_DIRECTIONAL_TEXTURED_SPECULARMAPPED_NORMALMAPPED(
    "fwd_LD_T_SM_N",
    new KRenderingCapabilities(
      TextureCapability.TEXTURE_CAP_DIFFUSE,
      NormalCapability.NORMAL_CAP_MAPPED,
      SpecularCapability.SPECULAR_CAP_MAPPED,
      EnvironmentCapability.ENVIRONMENT_NONE)),

  /*
   * Directional with environment mapping.
   */

  KSP_FORWARD_LIT_DIRECTIONAL_ENVIRONMENTMAPPED(
    "fwd_LD_E",
    new KRenderingCapabilities(
      TextureCapability.TEXTURE_CAP_NONE,
      NormalCapability.NORMAL_CAP_VERTEX,
      SpecularCapability.SPECULAR_CAP_CONSTANT,
      EnvironmentCapability.ENVIRONMENT_MAPPED)),

  KSP_FORWARD_LIT_DIRECTIONAL_TEXTURED_ENVIRONMENTMAPPED(
    "fwd_LD_T_E",
    new KRenderingCapabilities(
      TextureCapability.TEXTURE_CAP_DIFFUSE,
      NormalCapability.NORMAL_CAP_VERTEX,
      SpecularCapability.SPECULAR_CAP_CONSTANT,
      EnvironmentCapability.ENVIRONMENT_MAPPED)),

  /*
   * Directional with environment mapping, with specular terms.
   */

  KSP_FORWARD_LIT_DIRECTIONAL_ENVIRONMENTMAPPED_SPECULAR(
    "fwd_LD_E_S",
    new KRenderingCapabilities(
      TextureCapability.TEXTURE_CAP_NONE,
      NormalCapability.NORMAL_CAP_VERTEX,
      SpecularCapability.SPECULAR_CAP_CONSTANT,
      EnvironmentCapability.ENVIRONMENT_MAPPED)),

  KSP_FORWARD_LIT_DIRECTIONAL_ENVIRONMENTMAPPED_SPECULARMAPPED(
    "fwd_LD_E_SM",
    new KRenderingCapabilities(
      TextureCapability.TEXTURE_CAP_NONE,
      NormalCapability.NORMAL_CAP_VERTEX,
      SpecularCapability.SPECULAR_CAP_MAPPED,
      EnvironmentCapability.ENVIRONMENT_MAPPED)),

  KSP_FORWARD_LIT_DIRECTIONAL_TEXTURED_ENVIRONMENTMAPPED_SPECULAR(
    "fwd_LD_T_E_S",
    new KRenderingCapabilities(
      TextureCapability.TEXTURE_CAP_DIFFUSE,
      NormalCapability.NORMAL_CAP_VERTEX,
      SpecularCapability.SPECULAR_CAP_CONSTANT,
      EnvironmentCapability.ENVIRONMENT_MAPPED)),

  KSP_FORWARD_LIT_DIRECTIONAL_TEXTURED_ENVIRONMENTMAPPED_SPECULARMAPPED(
    "fwd_LD_T_E_SM",
    new KRenderingCapabilities(
      TextureCapability.TEXTURE_CAP_DIFFUSE,
      NormalCapability.NORMAL_CAP_VERTEX,
      SpecularCapability.SPECULAR_CAP_MAPPED,
      EnvironmentCapability.ENVIRONMENT_MAPPED)),

  /*
   * Directional with environment mapping, with normal mapping, with specular
   * terms.
   */

  KSP_FORWARD_LIT_DIRECTIONAL_ENVIRONMENTMAPPED_NORMALMAPPED(
    "fwd_LD_E_S_N",
    new KRenderingCapabilities(
      TextureCapability.TEXTURE_CAP_NONE,
      NormalCapability.NORMAL_CAP_MAPPED,
      SpecularCapability.SPECULAR_CAP_NONE,
      EnvironmentCapability.ENVIRONMENT_MAPPED)),

  KSP_FORWARD_LIT_DIRECTIONAL_TEXTURED_ENVIRONMENTMAPPED_NORMALMAPPED(
    "fwd_LD_T_E_S_N",
    new KRenderingCapabilities(
      TextureCapability.TEXTURE_CAP_DIFFUSE,
      NormalCapability.NORMAL_CAP_MAPPED,
      SpecularCapability.SPECULAR_CAP_NONE,
      EnvironmentCapability.ENVIRONMENT_MAPPED)),

  /*
   * Directional with environment mapping, with normal mapping, with specular
   * terms.
   */

  KSP_FORWARD_LIT_DIRECTIONAL_ENVIRONMENTMAPPED_SPECULAR_NORMALMAPPED(
    "fwd_LD_E_S_N",
    new KRenderingCapabilities(
      TextureCapability.TEXTURE_CAP_NONE,
      NormalCapability.NORMAL_CAP_MAPPED,
      SpecularCapability.SPECULAR_CAP_CONSTANT,
      EnvironmentCapability.ENVIRONMENT_MAPPED)),

  KSP_FORWARD_LIT_DIRECTIONAL_ENVIRONMENTMAPPED_SPECULARMAPPED_NORMALMAPPED(
    "fwd_LD_E_SM_N",
    new KRenderingCapabilities(
      TextureCapability.TEXTURE_CAP_NONE,
      NormalCapability.NORMAL_CAP_MAPPED,
      SpecularCapability.SPECULAR_CAP_MAPPED,
      EnvironmentCapability.ENVIRONMENT_MAPPED)),

  KSP_FORWARD_LIT_DIRECTIONAL_TEXTURED_ENVIRONMENTMAPPED_SPECULAR_NORMALMAPPED(
    "fwd_LD_T_E_S_N",
    new KRenderingCapabilities(
      TextureCapability.TEXTURE_CAP_DIFFUSE,
      NormalCapability.NORMAL_CAP_MAPPED,
      SpecularCapability.SPECULAR_CAP_CONSTANT,
      EnvironmentCapability.ENVIRONMENT_MAPPED)),

  KSP_FORWARD_LIT_DIRECTIONAL_TEXTURED_ENVIRONMENTMAPPED_SPECULARMAPPED_NORMALMAPPED(
    "fwd_LD_T_E_SM_N",
    new KRenderingCapabilities(
      TextureCapability.TEXTURE_CAP_DIFFUSE,
      NormalCapability.NORMAL_CAP_MAPPED,
      SpecularCapability.SPECULAR_CAP_MAPPED,
      EnvironmentCapability.ENVIRONMENT_MAPPED)),

  ;

  private final @Nonnull String                 name;
  private final @Nonnull KRenderingCapabilities required;

  private KSPForwardLitDirectionalPrograms(
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

  public static @Nonnull KShadingProgramLightDirectional make(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull FSCapabilityRead fs,
    final @Nonnull Log log,
    final @Nonnull KSPForwardLitDirectionalPrograms p)
    throws JCGLException,
      JCGLCompileException,
      JCGLUnsupportedException,
      FilesystemError,
      IOException,
      ConstraintError
  {
    switch (p) {
      case KSP_FORWARD_LIT_DIRECTIONAL:
        return new KSPF_LD(gc, fs, log);
      case KSP_FORWARD_LIT_DIRECTIONAL_NORMALMAPPED:
        return new KSPF_LD_NM(gc, fs, log);
      case KSP_FORWARD_LIT_DIRECTIONAL_SPECULAR:
        return new KSPF_LD_S(gc, fs, log);
      case KSP_FORWARD_LIT_DIRECTIONAL_SPECULARMAPPED:
        return new KSPF_LD_SM(gc, fs, log);
      case KSP_FORWARD_LIT_DIRECTIONAL_SPECULARMAPPED_NORMALMAPPED:
        return new KSPF_LD_SM_NM(gc, fs, log);
      case KSP_FORWARD_LIT_DIRECTIONAL_SPECULAR_NORMALMAPPED:
        return new KSPF_LD_S_NM(gc, fs, log);
      case KSP_FORWARD_LIT_DIRECTIONAL_TEXTURED:
        return new KSPF_LD_T(gc, fs, log);
      case KSP_FORWARD_LIT_DIRECTIONAL_TEXTURED_NORMALMAPPED:
        return new KSPF_LD_T_NM(gc, fs, log);
      case KSP_FORWARD_LIT_DIRECTIONAL_TEXTURED_SPECULAR:
        return new KSPF_LD_T_S(gc, fs, log);
      case KSP_FORWARD_LIT_DIRECTIONAL_TEXTURED_SPECULARMAPPED:
        return new KSPF_LD_T_SM(gc, fs, log);
      case KSP_FORWARD_LIT_DIRECTIONAL_TEXTURED_SPECULARMAPPED_NORMALMAPPED:
        return new KSPF_LD_T_SM_NM(gc, fs, log);
      case KSP_FORWARD_LIT_DIRECTIONAL_TEXTURED_SPECULAR_NORMALMAPPED:
        return new KSPF_LD_T_S_NM(gc, fs, log);
      case KSP_FORWARD_LIT_DIRECTIONAL_ENVIRONMENTMAPPED:
        return new KSPF_LD_E(gc, fs, log);
      case KSP_FORWARD_LIT_DIRECTIONAL_TEXTURED_ENVIRONMENTMAPPED:
        return new KSPF_LD_T_E(gc, fs, log);
      case KSP_FORWARD_LIT_DIRECTIONAL_ENVIRONMENTMAPPED_SPECULAR:
        return new KSPF_LD_E_S(gc, fs, log);
      case KSP_FORWARD_LIT_DIRECTIONAL_ENVIRONMENTMAPPED_SPECULARMAPPED:
        return new KSPF_LD_E_SM(gc, fs, log);
      case KSP_FORWARD_LIT_DIRECTIONAL_TEXTURED_ENVIRONMENTMAPPED_SPECULAR:
        return new KSPF_LD_T_E_S(gc, fs, log);
      case KSP_FORWARD_LIT_DIRECTIONAL_TEXTURED_ENVIRONMENTMAPPED_SPECULARMAPPED:
        return new KSPF_LD_T_E_SM(gc, fs, log);
      case KSP_FORWARD_LIT_DIRECTIONAL_ENVIRONMENTMAPPED_NORMALMAPPED:
        return new KSPF_LD_E_NM(gc, fs, log);
      case KSP_FORWARD_LIT_DIRECTIONAL_ENVIRONMENTMAPPED_SPECULARMAPPED_NORMALMAPPED:
        return new KSPF_LD_E_SM_NM(gc, fs, log);
      case KSP_FORWARD_LIT_DIRECTIONAL_ENVIRONMENTMAPPED_SPECULAR_NORMALMAPPED:
        return new KSPF_LD_E_S_NM(gc, fs, log);
      case KSP_FORWARD_LIT_DIRECTIONAL_TEXTURED_ENVIRONMENTMAPPED_NORMALMAPPED:
        return new KSPF_LD_T_E_NM(gc, fs, log);
      case KSP_FORWARD_LIT_DIRECTIONAL_TEXTURED_ENVIRONMENTMAPPED_SPECULARMAPPED_NORMALMAPPED:
        return new KSPF_LD_T_E_SM_NM(gc, fs, log);
      case KSP_FORWARD_LIT_DIRECTIONAL_TEXTURED_ENVIRONMENTMAPPED_SPECULAR_NORMALMAPPED:
        return new KSPF_LD_T_E_S_NM(gc, fs, log);
    }

    throw new UnreachableCodeException();
  }

  public @Nonnull KRenderingCapabilities getRequiredCapabilities()
  {
    return this.required;
  }
}
