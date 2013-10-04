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
-- Directional light shaders, no specular terms.
--

module ForwardLitDirectionalDiffuseOnly is

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
  -- Diffuse-only.
  --

  shader vertex fwd_LD_vertex is
    in        v_normal     : vector_3f;
    in        v_position   : vector_3f;
    parameter m_normal     : matrix_3x3f;
    parameter m_modelview  : matrix_4x4f;
    parameter m_projection : matrix_4x4f;
    out       f_normal     : vector_3f;
  with
    value position =
      M4.multiply_vector (
        M4.multiply (m_projection, m_modelview),
        new vector_4f (v_position, 1.0)
      );
    value normal =
      M3.multiply_vector (m_normal, v_normal);
  as
    out gl_Position = position;
    out f_normal    = normal;
  end;

  shader fragment fwd_LD_fragment is
    in        f_normal  : vector_3f;
    parameter light     : DL.t;
    parameter f_diffuse : vector_4f;
    out       out_0     : vector_4f as 0;    
  with
    value n =
      V3.normalize (f_normal);
      
    value light_term =
      DL.diffuse_only (light, n);
      
    value albedo =
      f_diffuse [x y z];
      
    value rgba =
      new vector_4f (V3.multiply (albedo, light_term), 1.0);
  as
    out out_0 = rgba;
  end;

  shader program fwd_LD is
    vertex   fwd_LD_vertex;
    fragment fwd_LD_fragment;
  end;

  --
  -- Diffuse-only, textured.
  --

  shader vertex fwd_LD_T_vertex is
    in        v_normal     : vector_3f;
    in        v_position   : vector_3f;
    in        v_uv         : vector_2f;
    parameter m_normal     : matrix_3x3f;
    parameter m_modelview  : matrix_4x4f;
    parameter m_projection : matrix_4x4f;
    out       f_uv         : vector_2f;
    out       f_normal     : vector_3f;
  with
    value position =
      M4.multiply_vector (
        M4.multiply (m_projection, m_modelview),
        new vector_4f (v_position, 1.0)
      );
    value normal =
      M3.multiply_vector (m_normal, v_normal);
  as
    out gl_Position = position;
    out f_uv        = v_uv;
    out f_normal    = normal;
  end;

  shader fragment fwd_LD_T_fragment is
    in        f_uv        : vector_2f;
    in        f_normal    : vector_3f;
    parameter light       : DL.t;
    parameter t_diffuse_0 : sampler_2d;
    out       out_0       : vector_4f as 0;    
  with
    value n =
      V3.normalize (f_normal);
      
    value light_term =
      DL.diffuse_only (light, n);
      
    value albedo =
      S.texture (t_diffuse_0, f_uv) [x y z];
      
    value rgba =
      new vector_4f (V3.multiply (albedo, light_term), 1.0);
  as
    out out_0 = rgba;
  end;

  shader program fwd_LD_T is
    vertex   fwd_LD_T_vertex;
    fragment fwd_LD_T_fragment;
  end;

end;
