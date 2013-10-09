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
-- Spherical light shaders, no specular terms.
--

module ForwardLitSphericalDiffuseOnly is

  import com.io7m.parasol.Matrix3x3f        as M3;
  import com.io7m.parasol.Matrix4x4f        as M4;
  import com.io7m.parasol.Vector3f          as V3;
  import com.io7m.parasol.Sampler2D         as S;
  import com.io7m.parasol.Float             as F;

  import com.io7m.renderer.CubeMap          as CM;
  import com.io7m.renderer.Diffuse          as D;
  import com.io7m.renderer.DirectionalLight as DL;
  import com.io7m.renderer.SphericalLight   as SL;
  import com.io7m.renderer.Normals          as N;
  import com.io7m.renderer.Materials        as M;

  --
  -- Diffuse-only, untextured.
  --

  shader vertex fwd_LS_vertex is
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

  shader fragment fwd_LS_fragment is
    in        f_normal   : vector_3f;
    in        f_position : vector_4f;
    parameter material   : M.t;
    parameter light      : SL.t;
    out       out_0      : vector_4f as 0;    
  with
    value n =
      V3.normalize (f_normal);
      
    value light_term =
      SL.diffuse_only (light, n, f_position [x y z]);
      
    value albedo =
      material.diffuse.colour [x y z];
      
    value rgba =
      new vector_4f (V3.multiply (albedo, light_term), 1.0);
  as
    out out_0 = rgba;
  end;

  shader program fwd_LS is
    vertex   fwd_LS_vertex;
    fragment fwd_LS_fragment;
  end;

  --
  -- Diffuse-only, textured.
  --

  shader vertex fwd_LS_T_vertex is
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

  shader fragment fwd_LS_T_fragment is
    in        f_uv        : vector_2f;
    in        f_normal    : vector_3f;
    in        f_position  : vector_4f;
    parameter material    : M.t;
    parameter light       : SL.t;
    parameter t_diffuse_0 : sampler_2d;
    out       out_0       : vector_4f as 0;    
  with
    value n =
      V3.normalize (f_normal);
      
    value light_term =
      SL.diffuse_only (light, n, f_position [x y z]);

    value albedo =
      D.diffuse (t_diffuse_0, f_uv, material.diffuse) [x y z];

    value rgba =
      new vector_4f (V3.multiply (albedo, light_term), 1.0);
  as
    out out_0 = rgba;
  end;

  shader program fwd_LS_T is
    vertex   fwd_LS_T_vertex;
    fragment fwd_LS_T_fragment;
  end;

  --
  -- Diffuse-only, untextured, environment mapped.
  --

  shader fragment fwd_LS_E_fragment is
    in        f_normal      : vector_3f;
    in        f_position    : vector_4f;
    parameter material      : M.t;
    parameter light         : SL.t;
    parameter t_environment : sampler_cube;
    parameter m_view_inv    : matrix_4x4f;
    out       out_0         : vector_4f as 0;    
  with
    value n =
      V3.normalize (f_normal);
      
    value light_term =
      SL.diffuse_only (light, n, f_position [x y z]);

    value env =
      CM.reflection (t_environment, f_position [x y z], f_normal, m_view_inv);

    value albedo =
      V3.interpolate (material.diffuse.colour [x y z], env [x y z], material.environment.mix);
      
    value rgba =
      new vector_4f (V3.multiply (albedo, light_term), 1.0);
  as
    out out_0 = rgba;
  end;

  shader program fwd_LS_E is
    vertex   fwd_LS_vertex;
    fragment fwd_LS_E_fragment;
  end;

  --
  -- Diffuse-only, textured, environment mapped.
  --

  shader fragment fwd_LS_T_E_fragment is
    in        f_normal      : vector_3f;
    in        f_position    : vector_4f;
    in        f_uv          : vector_2f;
    parameter material      : M.t;
    parameter light         : SL.t;
    parameter t_environment : sampler_cube;
    parameter t_diffuse_0   : sampler_2d;
    parameter m_view_inv    : matrix_4x4f;
    out       out_0         : vector_4f as 0;   
  with
    value n =
      V3.normalize (f_normal);
      
    value light_term =
      SL.diffuse_only (light, n, f_position [x y z]);

    value env =
      CM.reflection (t_environment, f_position [x y z], f_normal, m_view_inv);

    value diff =
      D.diffuse (t_diffuse_0, f_uv, material.diffuse) [x y z];

    value albedo =
      V3.interpolate (diff [x y z], env [x y z], material.environment.mix);

    value rgba =
      new vector_4f (V3.multiply (albedo, light_term), 1.0);
  as
    out out_0 = rgba;
  end;

  shader program fwd_LS_T_E is
    vertex   fwd_LS_T_vertex;
    fragment fwd_LS_T_E_fragment;
  end;

end;
