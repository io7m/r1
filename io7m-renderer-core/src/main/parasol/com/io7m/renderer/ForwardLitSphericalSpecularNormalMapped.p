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
-- Spherical light shaders, normal mapping, specular terms.
--

module ForwardLitSphericalSpecularNormalMapped is

  import com.io7m.parasol.Matrix3x3f        as M3;
  import com.io7m.parasol.Matrix4x4f        as M4;
  import com.io7m.parasol.Vector3f          as V3;
  import com.io7m.parasol.Sampler2D         as S;
  import com.io7m.parasol.Float             as F;
  import com.io7m.renderer.DirectionalLight as DL;
  import com.io7m.renderer.SphericalLight   as SL;
  import com.io7m.renderer.Normals          as N;
  import com.io7m.renderer.Materials        as M;
  
  import com.io7m.renderer.ForwardLitSphericalNormalMapped as FLSNM;
  
  --
  -- Untextured, normal mapped, constant specular.
  --
  
  shader fragment fwd_LS_S_N_fragment is
    in        f_normal    : vector_3f;
    in        f_tangent   : vector_3f;
    in        f_bitangent : vector_3f;
    in        f_uv        : vector_2f;
    in        f_position  : vector_4f;
    parameter material    : M.t;
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
      SL.diffuse_specular (light, n, f_position [x y z], material);
      
    value albedo =
      f_diffuse [x y z];
      
    value rgba =
      new vector_4f (V3.multiply (albedo, light_term), 1.0);
  as
    out out_0 = rgba;
  end;

  shader program fwd_LS_S_N is
    vertex   FLSNM.fwd_LS_N_vertex;
    fragment fwd_LS_S_N_fragment;
  end;

  --
  -- Textured, normal mapped, constant specular.
  --
  
  shader fragment fwd_LS_T_S_N_fragment is
    in        f_normal    : vector_3f;
    in        f_tangent   : vector_3f;
    in        f_bitangent : vector_3f;
    in        f_uv        : vector_2f;
    in        f_position  : vector_4f;
    parameter material    : M.t;
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
      SL.diffuse_specular (light, n, f_position [x y z], material);
      
    value albedo =
      S.texture (t_diffuse_0, f_uv) [x y z];
      
    value rgba =
      new vector_4f (V3.multiply (albedo, light_term), 1.0);
  as
    out out_0 = rgba;
  end;

  shader program fwd_LS_T_S_N is
    vertex   FLSNM.fwd_LS_N_vertex;
    fragment fwd_LS_T_S_N_fragment;
  end;

  --
  -- Untextured, normal mapped, specular mapped.
  --
  
  shader fragment fwd_LS_SM_N_fragment is
    in        f_normal    : vector_3f;
    in        f_tangent   : vector_3f;
    in        f_bitangent : vector_3f;
    in        f_uv        : vector_2f;
    in        f_position  : vector_4f;
    parameter material    : M.t;
    parameter light       : SL.t;
    parameter f_diffuse   : vector_4f;
    parameter t_specular  : sampler_2d;
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

    value m =
      record M.t {
        specular_exponent  = material.specular_exponent,
        specular_intensity = S.texture (t_specular, f_uv)[x]
      };

    value light_term =
      SL.diffuse_specular (light, n, f_position [x y z], m);
      
    value albedo =
      f_diffuse [x y z];
      
    value rgba =
      new vector_4f (V3.multiply (albedo, light_term), 1.0);
  as
    out out_0 = rgba;
  end;

  shader program fwd_LS_SM_N is
    vertex   FLSNM.fwd_LS_N_vertex;
    fragment fwd_LS_SM_N_fragment;
  end;

  --
  -- Textured, normal mapped, specular mapped.
  --
  
  shader fragment fwd_LS_T_SM_N_fragment is
    in        f_normal    : vector_3f;
    in        f_tangent   : vector_3f;
    in        f_bitangent : vector_3f;
    in        f_uv        : vector_2f;
    in        f_position  : vector_4f;
    parameter material    : M.t;
    parameter light       : SL.t;
    parameter t_diffuse_0 : sampler_2d;
    parameter t_specular  : sampler_2d;
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

    value m =
      record M.t {
        specular_exponent  = material.specular_exponent,
        specular_intensity = S.texture (t_specular, f_uv)[x]
      };

    value light_term =
      SL.diffuse_specular (light, n, f_position [x y z], m);
      
    value albedo =
      S.texture (t_diffuse_0, f_uv) [x y z];
      
    value rgba =
      new vector_4f (V3.multiply (albedo, light_term), 1.0);
  as
    out out_0 = rgba;
  end;

  shader program fwd_LS_T_SM_N is
    vertex   FLSNM.fwd_LS_N_vertex;
    fragment fwd_LS_T_SM_N_fragment;
  end;

end;
