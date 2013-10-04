--
-- Copyright © 2013 <code@io7m.com> http://io7m.com
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

--
-- Spherical light shaders, normal mapping, no specular terms.
--

module ForwardLitSphericalNormalMapped is

  import com.io7m.parasol.Matrix3x3f        as M3;
  import com.io7m.parasol.Matrix4x4f        as M4;
  import com.io7m.parasol.Vector3f          as V3;
  import com.io7m.parasol.Sampler2D         as S;
  import com.io7m.parasol.Float             as F;
  import com.io7m.renderer.DirectionalLight as DL;
  import com.io7m.renderer.SphericalLight   as SL;
  import com.io7m.renderer.Normals          as N;
  import com.io7m.renderer.Materials        as M;

  --
  -- Diffuse-only, untextured, normal mapped.
  --

  shader vertex fwd_LS_N_vertex is
    in        v_uv         : vector_2f;
    in        v_normal     : vector_3f;
    in        v_position   : vector_3f;
    in        v_tangent4   : vector_4f;
    parameter m_modelview  : matrix_4x4f;
    parameter m_projection : matrix_4x4f;
    out       f_tangent    : vector_3f;
    out       f_bitangent  : vector_3f;
    out       f_uv         : vector_2f;
    out       f_normal     : vector_3f;
    out       f_position   : vector_4f;
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

    value tangent =
      v_tangent4 [x y z];

    value bitangent =
      N.bitangent (v_normal, v_tangent4);
  as
    out gl_Position = clip_position;
    out f_position  = position;
    out f_normal    = v_normal;
    out f_uv        = v_uv;
    out f_tangent   = tangent;
    out f_bitangent = bitangent;
  end;

  shader fragment fwd_LS_N_fragment is
    in        f_normal    : vector_3f;
    in        f_tangent   : vector_3f;
    in        f_bitangent : vector_3f;
    in        f_uv        : vector_2f;
    in        f_position  : vector_4f;
    parameter light       : SL.t;
    parameter f_diffuse   : vector_4f;
    parameter t_normal    : sampler_2d;
    parameter m_normal    : matrix_3x3f;
    out       out_0       : vector_4f as 0;    
  with
    value n = N.bump(
      t_normal,
      m_normal,
      V3.normalize (f_normal),
      V3.normalize (f_tangent),
      V3.normalize (f_bitangent),
      f_uv);
      
    value light_term =
      SL.diffuse_only (light, n, f_position [x y z]);
      
    value albedo =
      f_diffuse [x y z];
      
    value rgba =
      new vector_4f (V3.multiply (albedo, light_term), 1.0);
  as
    out out_0 = rgba;
  end;

  shader program fwd_LS_N is
    vertex   fwd_LS_N_vertex;
    fragment fwd_LS_N_fragment;
  end;

  --
  -- Diffuse-only, textured, normal mapped.
  --

  shader fragment fwd_LS_T_N_fragment is
    in        f_normal    : vector_3f;
    in        f_tangent   : vector_3f;
    in        f_bitangent : vector_3f;
    in        f_uv        : vector_2f;
    in        f_position  : vector_4f;
    parameter light       : SL.t;
    parameter t_diffuse_0 : sampler_2d;
    parameter t_normal    : sampler_2d;
    parameter m_normal    : matrix_3x3f;
    out       out_0       : vector_4f as 0;    
  with
    value n = N.bump(
      t_normal,
      m_normal,
      V3.normalize (f_normal),
      V3.normalize (f_tangent),
      V3.normalize (f_bitangent),
      f_uv);
      
    value light_term =
      SL.diffuse_only (light, n, f_position [x y z]);
      
    value albedo =
      S.texture (t_diffuse_0, f_uv) [x y z];
      
    value rgba =
      new vector_4f (V3.multiply (albedo, light_term), 1.0);
  as
    out out_0 = rgba;
  end;

  shader program fwd_LS_T_N is
    vertex   fwd_LS_N_vertex;
    fragment fwd_LS_T_N_fragment;
  end;

end;