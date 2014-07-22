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

module Debug is

  import com.io7m.parasol.Matrix3x3f as M3;
  import com.io7m.parasol.Matrix4x4f as M4;
  import com.io7m.parasol.Vector3f   as V3;
  import com.io7m.parasol.Vector4f   as V4;
  import com.io7m.parasol.Sampler2D  as S;
  import com.io7m.parasol.Float      as F;
  import com.io7m.parasol.Fragment;

  --
  -- "Constant color" program.
  --

  shader vertex show_ccolor_v is
    in v_position              : vector_3f;
    parameter m_modelview      : matrix_4x4f;
    parameter m_projection     : matrix_4x4f;
    out vertex f_position_clip : vector_4f;
  with
    value clip_position =
      M4.multiply_vector (
        M4.multiply (m_projection, m_modelview),
        new vector_4f (v_position, 1.0)
      );
  as
    out f_position_clip = clip_position;
  end;

  shader fragment show_ccolor_f is
    parameter f_ccolor : vector_4f;
    out       out_0     : vector_4f as 0;
  as
    out out_0 = f_ccolor;
  end;

  shader program show_ccolor is
    vertex   show_ccolor_v;
    fragment show_ccolor_f;
  end;

  --
  -- Visible UV program.
  --

  shader vertex show_uv_v is
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

  shader fragment show_uv_f is
    in  f_uv  : vector_2f;
    out out_0 : vector_4f as 0;
  with
    value rgba = new vector_4f (f_uv [x y], 0.0, 1.0);
  as
    out out_0 = rgba;
  end;

  shader program show_uv is
    vertex   show_uv_v;
    fragment show_uv_f;
  end;

  --
  -- "Vertex color" program.
  --

  shader vertex show_vcolor_v is
    in v_position              : vector_3f;
    in v_color                : vector_4f;
    out f_color               : vector_4f;
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
    out f_color        = v_color;
  end;

  shader fragment show_vcolor_f is
    in  f_color : vector_4f;
    out out_0   : vector_4f as 0;
  as
    out out_0 = f_color;
  end;

  shader program show_vcolor is
    vertex   show_vcolor_v;
    fragment show_vcolor_f;
  end;

  --
  -- Flat textured UV program.
  --

  shader vertex show_flat_uv_v is
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
    out f_uv        = v_uv;
  end;

  shader fragment show_flat_uv_f is
    in        f_uv     : vector_2f;
    parameter t_albedo : sampler_2d;
    out       out_0    : vector_4f as 0;
  with
    value rgba = S.texture (t_albedo, f_uv);
  as
    out out_0 = rgba;
  end;

  shader program show_flat_uv is
    vertex   show_flat_uv_v;
    fragment show_flat_uv_f;
  end;

end;
