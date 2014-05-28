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

package com.io7m.renderer;

module Depth is

  import com.io7m.parasol.Matrix3x3f as M3;
  import com.io7m.parasol.Matrix4x4f as M4;
  import com.io7m.parasol.Vector3f   as V3;
  import com.io7m.parasol.Vector4f   as V4;
  import com.io7m.parasol.Sampler2D  as S;
  import com.io7m.parasol.Float      as F;
  import com.io7m.parasol.Fragment;

  import com.io7m.renderer.Albedo;
  import com.io7m.renderer.Pack;

  shader vertex depth_simple_v is
    in v_position              : vector_3f;
    out f_position             : vector_4f;
    out vertex f_position_clip : vector_4f;
    parameter m_modelview      : matrix_4x4f;
    parameter m_projection     : matrix_4x4f;
  with
    value clip_position =
      M4.multiply_vector (
        M4.multiply (m_projection, m_modelview),
        new vector_4f (v_position, 1.0)
      );
    value position =
      M4.multiply_vector (
        m_modelview,
        new vector_4f (v_position, 1.0)
      );
  as
    out f_position_clip = clip_position;
    out f_position      = position;
  end;

  --
  -- Rendering of the depth values of constant-depth objects into the depth buffer.
  -- The color value is expected to be ignored with glColorMask.
  --

  shader fragment depth_DepC_f is
    in f_position : vector_4f;
    out out_0     : vector_4f as 0;
  with
    value rgba =
      new vector_4f (0.0, Fragment.coordinate [z], 1.0, 1.0);
  as
    out out_0 = rgba;
  end;

  shader program depth_DepC is
    vertex   depth_simple_v;
    fragment depth_DepC_f;
  end;

  --
  -- Rendering of the depth values of constant-depth objects into the color buffer,
  -- packed into four 4-bit cells. This is for use on platforms that do not
  -- have depth textures.
  --

  shader fragment depth_DepC4444_f is
    in f_position : vector_4f;
    out out_0     : vector_4f as 0;
  with
    value rgba = Pack.pack4444 (Fragment.coordinate [z]);
  as
    out out_0 = rgba;
  end;

  shader program depth_DepC4444 is
    vertex   depth_simple_v;
    fragment depth_DepC4444_f;
  end;

  --
  -- Rendering of the depth values of mapped-depth objects into the
  -- depth buffer.
  --
  -- The color value is expected to be ignored with glColorMask.
  --

  shader vertex depth_textured_v is
    in v_position              : vector_3f;
    out f_position             : vector_4f;
    out vertex f_position_clip : vector_4f;
    parameter m_modelview      : matrix_4x4f;
    parameter m_projection     : matrix_4x4f;
    in v_uv                    : vector_2f;
    out f_uv                   : vector_2f;
    parameter m_uv             : matrix_3x3f;
  with
    value clip_position =
      M4.multiply_vector (
        M4.multiply (m_projection, m_modelview),
        new vector_4f (v_position, 1.0)
      );
    value position =
      M4.multiply_vector (
        m_modelview,
        new vector_4f (v_position, 1.0)
      );
    value uv =
      M3.multiply_vector (m_uv, new vector_3f (v_uv, 1.0)) [x y];
  as
    out f_position_clip = clip_position;
    out f_position      = position;
    out f_uv            = uv;
  end;
  
  shader fragment depth_DepM_f is
    in f_uv                 : vector_2f;
    in f_position           : vector_4f;
    out out_0               : vector_4f as 0;
    parameter p_albedo      : Albedo.t;
    parameter p_alpha_depth : float;
    parameter t_albedo      : sampler_2d;
  with
    value albedo : vector_4f =
      Albedo.textured_translucent (
        t_albedo,
        f_uv,
        p_albedo
      );
    discard (F.lesser (albedo [w], p_alpha_depth));
    value rgba =
      new vector_4f (0.0, Fragment.coordinate [z], 1.0, 1.0);
  as
    out out_0 = rgba;
  end;

  shader program depth_DepM is
    vertex   depth_textured_v;
    fragment depth_DepM_f;
  end;

  --
  -- Rendering of the depth values of mapped-depth objects 
  -- into the color buffer, packed into four 4-bit cells. This is for use on 
  -- platforms that do not have depth textures.
  --

  shader fragment depth_DepM4444_f is
    in f_uv                 : vector_2f;
    in f_position           : vector_4f;
    out out_0               : vector_4f as 0;
    parameter p_albedo      : Albedo.t;
    parameter p_alpha_depth : float;
    parameter t_albedo      : sampler_2d;
  with
    value albedo : vector_4f =
      Albedo.textured_translucent (
        t_albedo,
        f_uv,
        p_albedo
      );
    discard (F.lesser (albedo [w], p_alpha_depth));
    value rgba = Pack.pack4444 (Fragment.coordinate [z]);
  as
    out out_0 = rgba;
  end;

  shader program depth_DepM4444 is
    vertex   depth_textured_v;
    fragment depth_DepM4444_f;
  end;

end;
