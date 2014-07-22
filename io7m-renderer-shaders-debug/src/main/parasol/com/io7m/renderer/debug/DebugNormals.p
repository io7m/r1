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

package com.io7m.renderer.debug;

module DebugNormals is

  import com.io7m.parasol.Matrix3x3f as M3;
  import com.io7m.parasol.Matrix4x4f as M4;
  import com.io7m.parasol.Vector3f   as V3;
  import com.io7m.parasol.Vector4f   as V4;
  import com.io7m.parasol.Sampler2D  as S;
  import com.io7m.parasol.Float      as F;
  import com.io7m.parasol.Fragment;


  import com.io7m.renderer.core.Normals as N;

  --
  -- Visible object-space normals.
  --

  shader vertex show_normals_vertex_local_v is
    in v_position              : vector_3f;
    in v_normal                : vector_3f;
    out f_normal               : vector_3f;
    out vertex f_position_clip : vector_4f;
    parameter m_modelview      : matrix_4x4f;
    parameter m_projection     : matrix_4x4f;
  with
    value clip_position =
      M4.multiply_vector (
        M4.multiply (m_projection, m_modelview),
        new vector_4f (v_position, 1.0)
      );
  as
    out f_position_clip = clip_position;
    out f_normal        = v_normal;
  end;

  shader fragment show_normals_vertex_local_f is
    in f_normal : vector_3f;
    out out_0   : vector_4f as 0;
  with
    value rgba = N.to_rgba (f_normal);
  as
    out out_0 = rgba;
  end;

  shader program show_normals_vertex_local is
    vertex   show_normals_vertex_local_v;
    fragment show_normals_vertex_local_f;
  end;

  --
  -- Visible eye-space normals.
  --

  shader vertex show_normals_vertex_eye_v is
    in v_position              : vector_3f;
    in v_normal                : vector_3f;
    out f_normal               : vector_3f;
    out vertex f_position_clip : vector_4f;
    parameter m_normal         : matrix_3x3f;
    parameter m_modelview      : matrix_4x4f;
    parameter m_projection     : matrix_4x4f;
  with
    value clip_position =
      M4.multiply_vector (
        M4.multiply (m_projection, m_modelview),
        new vector_4f (v_position, 1.0)
      );
    value normal =
      M3.multiply_vector (m_normal, v_normal);
  as
    out f_position_clip = clip_position;
    out f_normal        = normal;
  end;

  shader fragment show_normals_vertex_eye_f is
    in f_normal : vector_3f;
    out out_0   : vector_4f as 0;
  with
    value rgba = N.to_rgba (f_normal);
  as
    out out_0 = rgba;
  end;

  shader program show_normals_vertex_eye is
    vertex   show_normals_vertex_eye_v;
    fragment show_normals_vertex_eye_f;
  end;

  --
  -- Visible object-space bitangents.
  --

  shader vertex show_bitangents_vertex_local_v is
    in v_position              : vector_3f;
    in v_normal                : vector_3f;
    in v_tangent4              : vector_4f;
    out f_bitangent            : vector_3f;
    out vertex f_position_clip : vector_4f;
    parameter m_modelview      : matrix_4x4f;
    parameter m_projection     : matrix_4x4f;
  with
    value clip_position =
      M4.multiply_vector (
        M4.multiply (m_projection, m_modelview),
        new vector_4f (v_position, 1.0)
      );
    value bitangent =
      N.bitangent (v_normal, v_tangent4);
  as
    out f_position_clip = clip_position;
    out f_bitangent     = bitangent;
  end;

  shader fragment show_bitangents_vertex_local_f is
    in f_bitangent : vector_3f;
    out out_0      : vector_4f as 0;
  with
    value rgba = N.to_rgba (f_bitangent);
  as
    out out_0 = rgba;
  end;

  shader program show_bitangents_vertex_local is
    vertex   show_bitangents_vertex_local_v;
    fragment show_bitangents_vertex_local_f;
  end;

  --
  -- Visible object-space tangents.
  --

  shader vertex show_tangents_vertex_local_v is
    in v_position              : vector_3f;
    in v_tangent4              : vector_4f;
    out f_tangent              : vector_3f;
    out vertex f_position_clip : vector_4f;
    parameter m_modelview      : matrix_4x4f;
    parameter m_projection     : matrix_4x4f;
  with
    value clip_position =
      M4.multiply_vector (
        M4.multiply (m_projection, m_modelview),
        new vector_4f (v_position, 1.0)
      );
    value tangent =
      v_tangent4 [x y z];
  as
    out f_position_clip = clip_position;
    out f_tangent       = tangent;
  end;

  shader fragment show_tangents_vertex_local_f is
    in f_tangent : vector_3f;
    out out_0    : vector_4f as 0;
  with
    value rgba = N.to_rgba (f_tangent);
  as
    out out_0 = rgba;
  end;

  shader program show_tangents_vertex_local is
    vertex   show_tangents_vertex_local_v;
    fragment show_tangents_vertex_local_f;
  end;

  --
  -- Visible eye-space bitangents.
  --

  shader vertex show_bitangents_vertex_eye_v is
    in v_position              : vector_3f;
    in v_normal                : vector_3f;
    in v_tangent4              : vector_4f;
    out f_bitangent            : vector_3f;
    out vertex f_position_clip : vector_4f;
    parameter m_normal         : matrix_3x3f;
    parameter m_modelview      : matrix_4x4f;
    parameter m_projection     : matrix_4x4f;
  with
    value clip_position =
      M4.multiply_vector (
        M4.multiply (m_projection, m_modelview),
        new vector_4f (v_position, 1.0)
      );
    value bitangent =
      M3.multiply_vector (m_normal, N.bitangent (v_normal, v_tangent4));
  as
    out f_position_clip = clip_position;
    out f_bitangent     = bitangent;
  end;

  shader fragment show_bitangents_vertex_eye_f is
    in f_bitangent : vector_3f;
    out out_0      : vector_4f as 0;
  with
    value rgba = N.to_rgba (f_bitangent);
  as
    out out_0 = rgba;
  end;

  shader program show_bitangents_vertex_eye is
    vertex   show_bitangents_vertex_eye_v;
    fragment show_bitangents_vertex_eye_f;
  end;

  --
  -- Visible object-space tangents.
  --

  shader vertex show_tangents_vertex_eye_v is
    in v_position              : vector_3f;
    in v_tangent4              : vector_4f;
    out f_tangent              : vector_3f;
    out vertex f_position_clip : vector_4f;
    parameter m_normal         : matrix_3x3f;
    parameter m_modelview      : matrix_4x4f;
    parameter m_projection     : matrix_4x4f;
  with
    value clip_position =
      M4.multiply_vector (
        M4.multiply (m_projection, m_modelview),
        new vector_4f (v_position, 1.0)
      );
    value tangent =
      M3.multiply_vector (m_normal, v_tangent4 [x y z]);
  as
    out f_position_clip = clip_position;
    out f_tangent       = tangent;
  end;

  shader fragment show_tangents_vertex_eye_f is
    in f_tangent : vector_3f;
    out out_0    : vector_4f as 0;
  with
    value rgba = N.to_rgba (f_tangent);
  as
    out out_0 = rgba;
  end;

  shader program show_tangents_vertex_eye is
    vertex   show_tangents_vertex_eye_v;
    fragment show_tangents_vertex_eye_f;
  end;

  --
  -- Visible object-space mapped normals.
  --

  shader vertex show_normals_map_local_v is
    in v_position              : vector_3f;
    in v_normal                : vector_3f;
    in v_tangent4              : vector_4f;
    in v_uv                    : vector_2f;
    out vertex f_position_clip : vector_4f;
    out f_normal               : vector_3f;
    out f_tangent              : vector_3f;
    out f_bitangent            : vector_3f;
    out f_uv                   : vector_2f;
    parameter m_modelview      : matrix_4x4f;
    parameter m_projection     : matrix_4x4f;
  with
    value clip_position =
      M4.multiply_vector (
        M4.multiply (m_projection, m_modelview),
        new vector_4f (v_position, 1.0)
      );
    value tangent =
      v_tangent4 [x y z];
    value bitangent =
      N.bitangent (v_normal, v_tangent4);
  as
    out f_position_clip = clip_position;
    out f_normal        = v_normal;
    out f_tangent       = tangent;
    out f_bitangent     = bitangent;
    out f_uv            = v_uv;
  end;

  shader fragment show_normals_map_local_f is
    in f_normal        : vector_3f;
    in f_tangent       : vector_3f;
    in f_bitangent     : vector_3f;
    in f_uv            : vector_2f;
    parameter t_normal : sampler_2d;
    out out_0          : vector_4f as 0;
  with
    value normal =
      N.bump_local (
        t_normal,
        V3.normalize (f_normal),
        V3.normalize (f_tangent),
        V3.normalize (f_bitangent),
        f_uv
      );
    value rgba = N.to_rgba (normal);
  as
    out out_0 = rgba;
  end;

  shader program show_normals_map_local is
    vertex   show_normals_map_local_v;
    fragment show_normals_map_local_f;
  end;

  --
  -- Visible eye-space mapped normals.
  --

  shader vertex show_normals_map_eye_v is
    in v_position              : vector_3f;
    in v_normal                : vector_3f;
    in v_tangent4              : vector_4f;
    in v_uv                    : vector_2f;
    out vertex f_position_clip : vector_4f;
    out f_normal               : vector_3f;
    out f_tangent              : vector_3f;
    out f_bitangent            : vector_3f;
    out f_uv                   : vector_2f;
    parameter m_modelview      : matrix_4x4f;
    parameter m_projection     : matrix_4x4f;
  with
    value clip_position =
      M4.multiply_vector (
        M4.multiply (m_projection, m_modelview),
        new vector_4f (v_position, 1.0)
      );
    value tangent =
      v_tangent4 [x y z];
    value bitangent =
      N.bitangent (v_normal, v_tangent4);
  as
    out f_position_clip = clip_position;
    out f_normal        = v_normal;
    out f_tangent       = tangent;
    out f_bitangent     = bitangent;
    out f_uv            = v_uv;
  end;

  shader fragment show_normals_map_eye_f is
    in f_normal        : vector_3f;
    in f_tangent       : vector_3f;
    in f_bitangent     : vector_3f;
    in f_uv            : vector_2f;
    parameter m_normal : matrix_3x3f;
    parameter t_normal : sampler_2d;
    out out_0          : vector_4f as 0;
  with
    value normal =
      N.bump (
        t_normal,
        m_normal,
        V3.normalize (f_normal),
        V3.normalize (f_tangent),
        V3.normalize (f_bitangent),
        f_uv
      );
    value rgba = N.to_rgba (normal);
  as
    out out_0 = rgba;
  end;

  shader program show_normals_map_eye is
    vertex   show_normals_map_eye_v;
    fragment show_normals_map_eye_f;
  end;

  --
  -- Visible tangent-space mapped normals.
  --

  shader vertex show_normals_map_tangent_v is
    in v_position              : vector_3f;
    in v_uv                    : vector_2f;
    out f_uv                   : vector_2f;
    out vertex f_position_clip : vector_4f;
    parameter m_modelview      : matrix_4x4f;
    parameter m_projection     : matrix_4x4f;
  with
    value clip_position =
      M4.multiply_vector (
        M4.multiply (m_projection, m_modelview),
        new vector_4f (v_position, 1.0)
      );
  as
    out f_position_clip = clip_position;
    out f_uv            = v_uv;
  end;

  shader fragment show_normals_map_tangent_f is
    in f_uv            : vector_2f;
    parameter t_normal : sampler_2d;
    out out_0          : vector_4f as 0;
  with
    value rgba = N.to_rgba (S.texture (t_normal, f_uv) [x y z]);
  as
    out out_0 = rgba;
  end;

  shader program show_normals_map_tangent is
    vertex   show_normals_map_tangent_v;
    fragment show_normals_map_tangent_f;
  end;

end;