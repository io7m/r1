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

package com.io7m.renderer.kernel_shaders;

import javax.annotation.Nonnull;

public final class ShadowShaders
{
  private static void fragmentShaderAttributesUV(
    final @Nonnull StringBuilder b,
    final @Nonnull ShadowLabel label)
  {
    if (label.impliesUV()) {
      b.append("  in f_uv : vector_2f;\n");
    }
  }

  private static void fragmentShaderParametersAlbedo(
    final @Nonnull StringBuilder b,
    final @Nonnull ShadowLabel label)
  {
    switch (label) {
      case SHADOW_BASIC_OPAQUE:
      case SHADOW_BASIC_TRANSLUCENT:
      case SHADOW_BASIC_OPAQUE_PACKED4444:
      case SHADOW_BASIC_TRANSLUCENT_PACKED4444:
      {
        break;
      }
      case SHADOW_BASIC_TRANSLUCENT_TEXTURED:
      case SHADOW_BASIC_TRANSLUCENT_TEXTURED_PACKED4444:
      {
        b.append("  parameter t_albedo : sampler_2d;\n");
        break;
      }
    }
  }

  private static void fragmentShaderStandardIO(
    final @Nonnull StringBuilder b)
  {
    b.append("  in f_position : vector_4f;\n");
    b.append("  out out_0     : vector_4f as 0;\n");
  }

  private static void fragmentShaderStandardParameters(
    final @Nonnull StringBuilder b)
  {
    b.append("  parameter material : M.t;\n");
  }

  private static void fragmentShaderValuesAlbedo(
    final @Nonnull StringBuilder b,
    final @Nonnull ShadowLabel label)
  {
    switch (label) {
      case SHADOW_BASIC_OPAQUE_PACKED4444:
      case SHADOW_BASIC_OPAQUE:
      {
        b.append("  value albedo : vector_4f =\n");
        b.append("    new vector_4f (1.0, 1.0, 1.0, 1.0);\n");
        break;
      }
      case SHADOW_BASIC_TRANSLUCENT_PACKED4444:
      case SHADOW_BASIC_TRANSLUCENT:
      {
        b.append("  value albedo : vector_4f =\n");
        b.append("    A.translucent (m.albedo);\n");
        break;
      }
      case SHADOW_BASIC_TRANSLUCENT_TEXTURED_PACKED4444:
      case SHADOW_BASIC_TRANSLUCENT_TEXTURED:
      {
        b.append("  value albedo : vector_4f =\n");
        b.append("    A.textured_translucent (\n");
        b.append("      t_albedo,\n");
        b.append("      f_uv,\n");
        b.append("      m.albedo\n");
        b.append("    );\n");
        break;
      }
    }
  }

  private static void fragmentShaderValuesMaterial(
    final @Nonnull StringBuilder b)
  {
    b.append("  value m = material;\n");
  }

  private static void moduleEnd(
    final @Nonnull StringBuilder b)
  {
    b.append("end;\n");
  }

  private static void moduleFragmentShader(
    final @Nonnull StringBuilder b,
    final @Nonnull ShadowLabel label)
  {
    b.append("shader fragment f is\n");
    ShadowShaders.fragmentShaderStandardIO(b);
    ShadowShaders.fragmentShaderStandardParameters(b);
    ShadowShaders.fragmentShaderAttributesUV(b, label);
    ShadowShaders.fragmentShaderParametersAlbedo(b, label);

    b.append("with\n");
    ShadowShaders.fragmentShaderValuesMaterial(b);
    ShadowShaders.fragmentShaderValuesAlbedo(b, label);
    ShadowShaders.fragmentShaderValuesRGBA(b, label);

    switch (label) {
      case SHADOW_BASIC_OPAQUE:
      case SHADOW_BASIC_OPAQUE_PACKED4444:
      {
        break;
      }
      case SHADOW_BASIC_TRANSLUCENT:
      case SHADOW_BASIC_TRANSLUCENT_PACKED4444:
      case SHADOW_BASIC_TRANSLUCENT_TEXTURED:
      case SHADOW_BASIC_TRANSLUCENT_TEXTURED_PACKED4444:
      {
        b.append("  discard (F.lesser (alpha, 0.5));\n");
        break;
      }
    }

    b.append("as\n");
    b.append("  out out_0 = rgba;\n");
    b.append("end;\n");
  }

  private static void fragmentShaderValuesRGBA(
    final @Nonnull StringBuilder b,
    final @Nonnull ShadowLabel label)
  {
    switch (label) {
      case SHADOW_BASIC_OPAQUE:
      {
        b.append("  value rgba = albedo;\n");
        break;
      }
      case SHADOW_BASIC_OPAQUE_PACKED4444:
      {
        b.append("  -- Pack depth into colour buffer\n");
        b.append("  value rgba = D.pack4444 (Fragment.coordinate [z]);\n");
        break;
      }
      case SHADOW_BASIC_TRANSLUCENT:
      case SHADOW_BASIC_TRANSLUCENT_TEXTURED:
      {
        b.append("  -- Premultiply alpha\n");
        b
          .append("  value alpha = F.multiply (albedo [w], m.alpha.opacity);\n");
        b.append("  value rgba  = new vector_4f (0.0, 0.0, 0.0, alpha);\n");
        break;
      }
      case SHADOW_BASIC_TRANSLUCENT_PACKED4444:
      case SHADOW_BASIC_TRANSLUCENT_TEXTURED_PACKED4444:
      {
        b.append("  -- Premultiply alpha\n");
        b
          .append("  value alpha = F.multiply (albedo [w], m.alpha.opacity);\n");
        b.append("  -- Pack depth into colour buffer\n");
        b.append("  value rgba   = D.pack4444 (Fragment.coordinate [z]);\n");
        break;
      }
    }
  }

  private static void moduleProgram(
    final @Nonnull StringBuilder b)
  {
    b.append("shader program p is\n");
    b.append("  vertex   v;\n");
    b.append("  fragment f;\n");
    b.append("end;\n");
  }

  public static @Nonnull String moduleShadow(
    final @Nonnull ShadowLabel label)
  {
    final StringBuilder b = new StringBuilder();
    b.append("package com.io7m.renderer.kernel;\n");
    ShadowShaders.moduleStart(b, "Shadow_" + label.getCode());
    ShadowShaders.moduleVertexShader(b, label);
    b.append("\n");
    ShadowShaders.moduleFragmentShader(b, label);
    b.append("\n");
    ShadowShaders.moduleProgram(b);
    b.append("\n");
    ShadowShaders.moduleEnd(b);
    return b.toString();
  }

  private static void moduleStart(
    final @Nonnull StringBuilder b,
    final @Nonnull String name)
  {
    b.append("module ");
    b.append(name);
    b.append(" is\n");
    b.append("\n");
    b.append("import com.io7m.parasol.Matrix3x3f        as M3;\n");
    b.append("import com.io7m.parasol.Matrix4x4f        as M4;\n");
    b.append("import com.io7m.parasol.Vector3f          as V3;\n");
    b.append("import com.io7m.parasol.Vector4f          as V4;\n");
    b.append("import com.io7m.parasol.Sampler2D         as S;\n");
    b.append("import com.io7m.parasol.Float             as F;\n");
    b.append("import com.io7m.parasol.Fragment;\n");
    b.append("\n");
    b.append("import com.io7m.renderer.Albedo           as A;\n");
    b.append("import com.io7m.renderer.Depth            as D;\n");
    b.append("import com.io7m.renderer.Materials        as M;\n");
    b.append("\n");
  }

  private static void moduleVertexShader(
    final @Nonnull StringBuilder b,
    final @Nonnull ShadowLabel label)
  {
    b.append("shader vertex v is\n");
    ShadowShaders.vertexShaderStandardIO(b);
    ShadowShaders.vertexShaderStandardParametersMatrices(b);
    ShadowShaders.vertexShaderStandardAttributesUV(b, label);
    ShadowShaders.vertexShaderStandardParametersUV(b, label);

    b.append("with\n");
    ShadowShaders.vertexShaderStandardValuesPositions(b);
    ShadowShaders.vertexShaderStandardValuesUV(b, label);
    b.append("as\n");

    ShadowShaders.vertexShaderStandardWrites(b);
    ShadowShaders.vertexShaderStandardWritesUV(b, label);
    b.append("end;\n");
  }

  private static void vertexShaderStandardAttributesUV(
    final @Nonnull StringBuilder b,
    final @Nonnull ShadowLabel label)
  {
    if (label.impliesUV()) {
      b.append("  in v_uv  : vector_2f;\n");
      b.append("  out f_uv : vector_2f;\n");
    }
  }

  private static void vertexShaderStandardIO(
    final @Nonnull StringBuilder b)
  {
    b.append("  in v_position              : vector_3f;\n");
    b.append("  out f_position             : vector_4f;\n");
    b.append("  out vertex f_position_clip : vector_4f;\n");
  }

  private static void vertexShaderStandardParametersMatrices(
    final @Nonnull StringBuilder b)
  {
    b.append("  parameter m_modelview  : matrix_4x4f;\n");
    b.append("  parameter m_projection : matrix_4x4f;\n");
  }

  private static void vertexShaderStandardParametersUV(
    final @Nonnull StringBuilder b,
    final @Nonnull ShadowLabel label)
  {
    if (label.impliesUV()) {
      b.append("  parameter m_uv : matrix_3x3f;\n");
    }
  }

  private static void vertexShaderStandardValuesPositions(
    final @Nonnull StringBuilder b)
  {
    b.append("  value clip_position =\n");
    b.append("    M4.multiply_vector (\n");
    b.append("      M4.multiply (m_projection, m_modelview),\n");
    b.append("      new vector_4f (v_position, 1.0)\n");
    b.append("    );\n");
    b.append("  value position =\n");
    b.append("    M4.multiply_vector (\n");
    b.append("      m_modelview,\n");
    b.append("      new vector_4f (v_position, 1.0)\n");
    b.append("    );\n");
  }

  private static void vertexShaderStandardValuesUV(
    final StringBuilder b,
    final ShadowLabel label)
  {
    if (label.impliesUV()) {
      b
        .append("  value uv = M3.multiply_vector (m_uv, new vector_3f (v_uv, 1.0)) [x y];\n");
    }
  }

  private static void vertexShaderStandardWrites(
    final @Nonnull StringBuilder b)
  {
    b.append("  out f_position_clip = clip_position;\n");
    b.append("  out f_position      = position;\n");
  }

  private static void vertexShaderStandardWritesUV(
    final @Nonnull StringBuilder b,
    final @Nonnull ShadowLabel label)
  {
    if (label.impliesUV()) {
      b.append("  out f_uv = uv;\n");
    }
  }
}
