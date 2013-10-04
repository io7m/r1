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
-- Directional light shaders with specular terms.
--

module ForwardLitDirectionalSpecular is

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
  -- Untextured, constant specular.
  --

  shader vertex fwd_LD_S_vertex is
    in        v_normal     : vector_3f;
    in        v_position   : vector_3f;
    parameter m_normal     : matrix_3x3f;
    parameter m_modelview  : matrix_4x4f;
    parameter m_projection : matrix_4x4f;
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

    value normal =
      M3.multiply_vector (m_normal, v_normal);
  as
    out gl_Position = clip_position;
    out f_position  = position;
    out f_normal    = normal;
  end;

  shader fragment fwd_LD_S_fragment is
    in        f_position : vector_4f;
    in        f_normal   : vector_3f;
    parameter material   : M.t;
    parameter light      : DL.t;
    parameter f_diffuse  : vector_4f;
    out       out_0      : vector_4f as 0;    
  with
    value n =
      V3.normalize (f_normal);
      
    value light_term =
      DL.diffuse_specular (light, n, f_position [x y z], material);
      
    value albedo =
      f_diffuse [x y z];
      
    value rgba =
      new vector_4f (V3.multiply (albedo, light_term), 1.0);
  as
    out out_0 = rgba;
  end;

  shader program fwd_LD_S is
    vertex   fwd_LD_S_vertex;
    fragment fwd_LD_S_fragment;
  end;

  --
  -- Untextured, mapped specular.
  --

  shader vertex fwd_LD_SM_vertex is
    in        v_normal     : vector_3f;
    in        v_position   : vector_3f;
    in        v_uv         : vector_2f;
    parameter m_normal     : matrix_3x3f;
    parameter m_modelview  : matrix_4x4f;
    parameter m_projection : matrix_4x4f;
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

    value normal =
      M3.multiply_vector (m_normal, v_normal);
  as
    out gl_Position = clip_position;
    out f_position  = position;
    out f_normal    = normal;
    out f_uv        = v_uv;
  end;

  shader fragment fwd_LD_SM_fragment is
    in        f_position : vector_4f;
    in        f_normal   : vector_3f;
    in        f_uv       : vector_2f;
    parameter material   : M.t;
    parameter light      : DL.t;
    parameter f_diffuse  : vector_4f;
    parameter t_specular : sampler_2d;
    out       out_0      : vector_4f as 0;    
  with
    value n =
      V3.normalize (f_normal);
      
    value m =
      record M.t {
        specular_exponent  = material.specular_exponent,
        specular_intensity = S.texture (t_specular, f_uv)[x]
      };
      
    value light_term =
      DL.diffuse_specular (light, n, f_position [x y z], m);
      
    value albedo =
      f_diffuse [x y z];
      
    value rgba =
      new vector_4f (V3.multiply (albedo, light_term), 1.0);
  as
    out out_0 = rgba;
  end;

  shader program fwd_LD_SM is
    vertex   fwd_LD_SM_vertex;
    fragment fwd_LD_SM_fragment;
  end;

  --
  -- Textured, constant specular.
  --

  shader fragment fwd_LD_T_S_fragment is
    in        f_position  : vector_4f;
    in        f_normal    : vector_3f;
    in        f_uv        : vector_2f;
    parameter material    : M.t;
    parameter light       : DL.t;
    parameter t_diffuse_0 : sampler_2d;
    out       out_0       : vector_4f as 0;    
  with
    value n =
      V3.normalize (f_normal);
      
    value light_term =
      DL.diffuse_specular (light, n, f_position [x y z], material);
      
    value albedo =
      S.texture (t_diffuse_0, f_uv) [x y z];
      
    value rgba =
      new vector_4f (V3.multiply (albedo, light_term), 1.0);
  as
    out out_0 = rgba;
  end;

  shader program fwd_LD_T_S is
    vertex   fwd_LD_SM_vertex; -- re-use SM vertex shader
    fragment fwd_LD_T_S_fragment;
  end;

  --
  -- Textured, mapped specular.
  --

  shader fragment fwd_LD_T_SM_fragment is
    in        f_position  : vector_4f;
    in        f_normal    : vector_3f;
    in        f_uv        : vector_2f;
    parameter material    : M.t;
    parameter light       : DL.t;
    parameter t_diffuse_0 : sampler_2d;
    parameter t_specular  : sampler_2d;
    out       out_0       : vector_4f as 0;    
  with
    value n =
      V3.normalize (f_normal);
      
    value m =
      record M.t {
        specular_exponent  = material.specular_exponent,
        specular_intensity = S.texture (t_specular, f_uv)[x]
      };
      
    value light_term =
      DL.diffuse_specular (light, n, f_position [x y z], m);
      
    value albedo =
      S.texture (t_diffuse_0, f_uv) [x y z];
      
    value rgba =
      new vector_4f (V3.multiply (albedo, light_term), 1.0);
  as
    out out_0 = rgba;
  end;

  shader program fwd_LD_T_SM is
    vertex   fwd_LD_SM_vertex; -- re-use SM vertex shader
    fragment fwd_LD_T_SM_fragment;
  end;

end;
