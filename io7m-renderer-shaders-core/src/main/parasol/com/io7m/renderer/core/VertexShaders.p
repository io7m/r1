--
-- Copyright © 2014 <code@io7m.com> http://io7m.com
--
-- Permission to use, copy, modify, and/or distribute this software for any
-- purpose with or without fee is hereby granted, provided that the above
-- copyright notice and this permission notice appear in all copies.
--
-- THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
-- WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
-- MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
-- SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
-- WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
-- ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
-- IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
--

package com.io7m.renderer.core;

--
-- Standard vertex shaders.
--

module VertexShaders is

  import com.io7m.parasol.Matrix3x3f as M3;
  import com.io7m.parasol.Matrix4x4f as M4;
  import com.io7m.parasol.Vector3f   as V3;
  import com.io7m.parasol.Vector4f   as V4;
  import com.io7m.parasol.Sampler2D  as S;
  import com.io7m.parasol.Float      as F;

  import com.io7m.renderer.core.Albedo;
  import com.io7m.renderer.core.CubeMap;
  import com.io7m.renderer.core.DirectionalLight;
  import com.io7m.renderer.core.Emission;
  import com.io7m.renderer.core.Environment;
  import com.io7m.renderer.core.Light;
  import com.io7m.renderer.core.Normals;
  import com.io7m.renderer.core.ProjectiveLight;
  import com.io7m.renderer.core.Refraction;
  import com.io7m.renderer.core.ShadowBasic;
  import com.io7m.renderer.core.ShadowVariance;
  import com.io7m.renderer.core.Specular;
  import com.io7m.renderer.core.SphericalLight;
  import com.io7m.renderer.core.VectorAux;

  --
  -- Standard vertex shader with per-vertex normals.
  --

  shader vertex standard is
    -- Vertex position coordinates
    in v_position              : vector_3f;
    out f_position_eye         : vector_4f;
    out vertex f_position_clip : vector_4f;

    -- Standard matrices
    parameter m_modelview      : matrix_4x4f;
    parameter m_projection     : matrix_4x4f;

    -- UV coordinates
    in v_uv        : vector_2f;
    out f_uv       : vector_2f;
    parameter m_uv : matrix_3x3f;

    -- Vertex normal attributes and parameters
    in v_normal        : vector_3f;
    out f_normal_eye   : vector_3f;
    parameter m_normal : matrix_3x3f;
  with
    -- Position values
    value position_eye =
      M4.multiply_vector (
        m_modelview,
        new vector_4f (v_position, 1.0)
      );

    value position_clip =
      M4.multiply_vector (
        M4.multiply (m_projection, m_modelview),
        new vector_4f (v_position, 1.0)
      );

    -- Transformed UV coordinates
    value uv =
      M3.multiply_vector (
        m_uv,
        new vector_3f (v_uv, 1.0)
      ) [x y];

    -- Vertex normal values
    value normal_eye =
      M3.multiply_vector (m_normal, v_normal);
  as
    out f_normal_eye    = normal_eye;
    out f_position_clip = position_clip;
    out f_position_eye  = position_eye;
    out f_uv            = uv;
  end;

  --
  -- Standard vertex shader for mapped normals.
  --

  shader vertex standard_NorM is
    -- Vertex position coordinates
    in v_position              : vector_3f;
    out f_position_eye         : vector_4f;
    out vertex f_position_clip : vector_4f;

    -- Standard matrices
    parameter m_modelview      : matrix_4x4f;
    parameter m_projection     : matrix_4x4f;

    -- UV coordinates
    in v_uv        : vector_2f;
    out f_uv       : vector_2f;
    parameter m_uv : matrix_3x3f;

    -- Mapped normal attributes
    in v_normal        : vector_3f;
    in v_tangent4      : vector_4f;
    out f_normal_model : vector_3f;
    out f_tangent      : vector_3f;
    out f_bitangent    : vector_3f;
  with
    -- Position values
    value position_eye =
      M4.multiply_vector (
        m_modelview,
        new vector_4f (v_position, 1.0)
      );

    value position_clip =
      M4.multiply_vector (
        M4.multiply (m_projection, m_modelview),
        new vector_4f (v_position, 1.0)
      );

    -- Transformed UV coordinates
    value uv =
      M3.multiply_vector (
        m_uv,
        new vector_3f (v_uv, 1.0)
      ) [x y];

    -- Mapped normal values
    value tangent =
      v_tangent4 [x y z];
    value bitangent =
      Normals.bitangent (v_normal, v_tangent4);
  as
    out f_normal_model = v_normal;
    out f_tangent      = tangent;
    out f_bitangent    = bitangent;

    out f_position_clip = position_clip;
    out f_position_eye  = position_eye;
    out f_uv            = uv;
  end;

  --
  -- Standard vertex shader with per-vertex normals and projective lighting.
  --

  shader vertex standard_Proj is
    -- Vertex position coordinates
    in v_position              : vector_3f;
    out f_position_eye         : vector_4f;
    out vertex f_position_clip : vector_4f;

    -- Standard matrices
    parameter m_modelview      : matrix_4x4f;
    parameter m_projection     : matrix_4x4f;

    -- UV coordinates
    in v_uv        : vector_2f;
    out f_uv       : vector_2f;
    parameter m_uv : matrix_3x3f;

    -- Vertex normal attributes and parameters
    in v_normal        : vector_3f;
    out f_normal_eye   : vector_3f;
    parameter m_normal : matrix_3x3f;
    
    -- Projective light parameters
    parameter m_projective_modelview  : matrix_4x4f;
    parameter m_projective_projection : matrix_4x4f;

    -- Projective light (shadow mapped) outputs
    out f_position_light_eye  : vector_4f;
    out f_position_light_clip : vector_4f;

  with
    -- Position values
    value position_eye =
      M4.multiply_vector (
        m_modelview,
        new vector_4f (v_position, 1.0)
      );

    value position_clip =
      M4.multiply_vector (
        M4.multiply (m_projection, m_modelview),
        new vector_4f (v_position, 1.0)
      );

    -- Transformed UV coordinates
    value uv =
      M3.multiply_vector (
        m_uv,
        new vector_3f (v_uv, 1.0)
      ) [x y];

    -- Vertex normal values
    value normal_eye =
      M3.multiply_vector (m_normal, v_normal);
      
    -- Projective lighting coordinates
    value position_light_eye : vector_4f =
      M4.multiply_vector (
        m_projective_modelview,
        new vector_4f (v_position, 1.0)
      );
    value position_light_clip : vector_4f =
      M4.multiply_vector (
        m_projective_projection,
        position_light_eye
      );
  as
    out f_position_light_clip = position_light_clip;
    out f_position_light_eye  = position_light_eye;
    out f_normal_eye          = normal_eye;
    out f_position_clip       = position_clip;
    out f_position_eye        = position_eye;
    out f_uv                  = uv;
  end;

  --
  -- Standard vertex shader for mapped normals and projective lighting.
  --

  shader vertex standard_Proj_NorM is
    -- Vertex position coordinates
    in v_position              : vector_3f;
    out f_position_eye         : vector_4f;
    out vertex f_position_clip : vector_4f;

    -- Standard matrices
    parameter m_modelview      : matrix_4x4f;
    parameter m_projection     : matrix_4x4f;

    -- UV coordinates
    in v_uv        : vector_2f;
    out f_uv       : vector_2f;
    parameter m_uv : matrix_3x3f;

    -- Mapped normal attributes
    in v_normal        : vector_3f;
    in v_tangent4      : vector_4f;
    out f_normal_model : vector_3f;
    out f_tangent      : vector_3f;
    out f_bitangent    : vector_3f;
    
    -- Projective light parameters
    parameter m_projective_modelview  : matrix_4x4f;
    parameter m_projective_projection : matrix_4x4f;

    -- Projective light (shadow mapped) outputs
    out f_position_light_eye  : vector_4f;
    out f_position_light_clip : vector_4f;

  with
    -- Position values
    value position_eye =
      M4.multiply_vector (
        m_modelview,
        new vector_4f (v_position, 1.0)
      );

    value position_clip =
      M4.multiply_vector (
        M4.multiply (m_projection, m_modelview),
        new vector_4f (v_position, 1.0)
      );

    -- Transformed UV coordinates
    value uv =
      M3.multiply_vector (
        m_uv,
        new vector_3f (v_uv, 1.0)
      ) [x y];

    -- Mapped normal values
    value tangent =
      v_tangent4 [x y z];
    value bitangent =
      Normals.bitangent (v_normal, v_tangent4);
      
    -- Projective lighting coordinates
    value position_light_eye : vector_4f =
      M4.multiply_vector (
        m_projective_modelview,
        new vector_4f (v_position, 1.0)
      );
    value position_light_clip : vector_4f =
      M4.multiply_vector (
        m_projective_projection,
        position_light_eye
      );
  as
    out f_position_light_clip = position_light_clip;
    out f_position_light_eye  = position_light_eye;
    out f_normal_model        = v_normal;
    out f_tangent             = tangent;
    out f_bitangent           = bitangent;
    out f_position_clip       = position_clip;
    out f_position_eye        = position_eye;
    out f_uv                  = uv;
  end;

  --
  -- Standard vertex shader for vertex positions that are
  -- already specified in clip-space, and allow for modifying
  -- UV coordinates via a matrix.
  --

  shader vertex standard_clip is
    parameter  m_uv            : matrix_3x3f;
    in         v_position      : vector_3f;
    in         v_uv            : vector_2f;
    out vertex f_position_clip : vector_4f;
    out        f_uv            : vector_2f;
  with
    value position_clip =
      new vector_4f (v_position, 1.0);
    value uv =
      M3.multiply_vector (m_uv, new vector_3f (v_uv, 1.0)) [x y];
  as
    out f_uv            = uv;
    out f_position_clip = position_clip;
  end;

  --
  -- Standard vertex shader for vertex positions that are
  -- already specified in clip-space, and allow for modifying
  -- UV coordinates via a matrix, but provide eye-space positions
  -- to the fragment shader.
  --

  shader vertex standard_clip_eye is
    parameter  m_uv             : matrix_3x3f;
    parameter  m_projection_inv : matrix_4x4f;
    in         v_position       : vector_3f;
    in         v_uv             : vector_2f;
    out vertex f_position_clip  : vector_4f;
    out        f_position_eye   : vector_4f;
    out        f_uv             : vector_2f;
  with
    value position_clip =
      new vector_4f (v_position, 1.0);
    value position_eye =
      M4.multiply_vector (m_projection_inv, position_clip);
    value uv =
      M3.multiply_vector (m_uv, new vector_3f (v_uv, 1.0)) [x y];
  as
    out f_uv            = uv;
    out f_position_clip = position_clip;
    out f_position_eye  = position_eye;
  end;

end;
