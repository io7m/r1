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

module Kernel is

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
  -- Trivial constant-color shader.
  --

  shader vertex c_colour_vertex is
    in        v_position   : vector_3f;
    parameter m_modelview  : matrix_4x4f;
    parameter m_projection : matrix_4x4f;
  with
    value position =
      M4.multiply_vector (
        M4.multiply (m_projection, m_modelview),
        new vector_4f (v_position, 1.0)
      );
  as
    out gl_Position = position;
  end;

  shader fragment c_colour_fragment is
    parameter f_ccolour : vector_4f;
    out       out_0     : vector_4f as 0;
  as
    out out_0 = f_ccolour;
  end;

  shader program c_colour is
    vertex   c_colour_vertex;
    fragment c_colour_fragment;
  end;

  --
  -- Trivial interpolated vertex-color shader.
  --

  shader vertex v_colour_vertex is
    in        v_position   : vector_3f;
    in        v_colour     : vector_4f;
    parameter m_modelview  : matrix_4x4f;
    parameter m_projection : matrix_4x4f;
    out       f_colour     : vector_4f;
  with
    value position =
      M4.multiply_vector (
        M4.multiply (m_projection, m_modelview),
        new vector_4f (v_position, 1.0)
      );
  as
    out gl_Position = position;
    out f_colour    = v_colour;
  end;

  shader fragment v_colour_fragment is
    in  f_colour : vector_4f;
    out out_0    : vector_4f as 0;
  as
    out out_0 = f_colour;
  end;

  shader program v_colour is
    vertex   v_colour_vertex;
    fragment v_colour_fragment;
  end;

  --
  -- Trivial depth-only shader.
  --

  shader vertex depth_vertex is
    in        v_position   : vector_3f;
    parameter m_modelview  : matrix_4x4f;
    parameter m_projection : matrix_4x4f;
  with
    value position =
      M4.multiply_vector (
        M4.multiply (m_projection, m_modelview),
        new vector_4f (v_position, 1.0)
      );
  as
    out gl_Position = position;
  end;

  shader fragment depth_fragment is
    out out_0 : vector_4f as 0;
  with
    value rgba = new vector_4f (gl_FragCoord [z z z], 1.0);
  as
    out out_0 = rgba;
  end;

  shader program depth is
    vertex   depth_vertex;
    fragment depth_fragment;
  end;

  --
  -- Trivial flat-texturing shader.
  --

  shader vertex flat_uv_vertex is
    in        v_position   : vector_3f;
    in        v_uv         : vector_2f;
    parameter m_modelview  : matrix_4x4f;
    parameter m_projection : matrix_4x4f;
    out       f_uv         : vector_2f;
  with
    value position =
      M4.multiply_vector (
        M4.multiply (m_projection, m_modelview),
        new vector_4f (v_position, 1.0)
      );
  as
    out gl_Position = position;
    out f_uv        = v_uv;
  end;

  shader fragment flat_uv_fragment is
    in        f_uv        : vector_2f;
    parameter t_diffuse_0 : sampler_2d;
    out       out_0       : vector_4f as 0;    
  with
    value rgba = S.texture (t_diffuse_0, f_uv);
  as
    out out_0 = rgba;
  end;

  shader program flat_uv is
    vertex   flat_uv_vertex;
    fragment flat_uv_fragment;
  end;

  --
  -- Simple diffuse-only shader for directional lights.
  --

  shader vertex forward_d_directional_vertex is
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

  shader fragment forward_d_directional_fragment is
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

  shader program forward_d_directional is
    vertex   forward_d_directional_vertex;
    fragment forward_d_directional_fragment;
  end;

  --
  -- Simple diffuse-only shader for spherical lights.
  --

  shader vertex forward_d_spherical_vertex is
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
    out f_uv        = v_uv;
    out f_normal    = normal;
  end;

  shader fragment forward_d_spherical_fragment is
    in        f_uv        : vector_2f;
    in        f_normal    : vector_3f;
    in        f_position  : vector_4f;
    parameter t_diffuse_0 : sampler_2d;
    parameter light       : SL.t;
    out       out_0       : vector_4f as 0;    
  with
    value n =
      V3.normalize (f_normal);
    value light_term =
      SL.diffuse_only (light, n, f_position [x y z]);
    value albedo =
      S.texture (t_diffuse_0, f_uv) [x y z];
    value rgba =
      new vector_4f (V3.multiply (albedo, light_term), 1.0);
  as
    out out_0 = rgba;
  end;

  shader program forward_d_spherical is
    vertex   forward_d_spherical_vertex;
    fragment forward_d_spherical_fragment;
  end;

  --
  -- Diffuse-specular shader for directional lights.
  --

  shader vertex forward_ds_directional_vertex is
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
    out f_uv        = v_uv;
    out f_normal    = normal;
  end;

  --
  -- Fragment shader without specular maps.
  --

  shader fragment forward_ds_directional_fragment is
    in        f_uv        : vector_2f;
    in        f_normal    : vector_3f;
    in        f_position  : vector_4f;
    parameter light       : DL.t;
    parameter material    : M.t;
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

  --
  -- Fragment shader with specular maps.
  --

  shader fragment forward_dsm_directional_fragment is
    in        f_uv        : vector_2f;
    in        f_normal    : vector_3f;
    in        f_position  : vector_4f;
    parameter light       : DL.t;
    parameter material    : M.t;
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

  shader program forward_ds_directional is
    vertex   forward_ds_directional_vertex;
    fragment forward_ds_directional_fragment;
  end;

  shader program forward_dsm_directional is
    vertex   forward_ds_directional_vertex;
    fragment forward_dsm_directional_fragment;
  end;

  --
  -- Diffuse-specular shader for spherical lights.
  --

  shader vertex forward_ds_spherical_vertex is
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
    out f_uv        = v_uv;
    out f_normal    = normal;
  end;

  --
  -- Fragment shader without specular maps
  --

  shader fragment forward_ds_spherical_fragment is
    in        f_uv        : vector_2f;
    in        f_normal    : vector_3f;
    in        f_position  : vector_4f;
    parameter light       : SL.t;
    parameter material    : M.t;
    parameter t_diffuse_0 : sampler_2d;
    out       out_0       : vector_4f as 0;    
  with
    value n =
      V3.normalize (f_normal);

    value light_term =
      SL.diffuse_specular (light, n, f_position [x y z], material);

    value albedo =
      S.texture (t_diffuse_0, f_uv) [x y z];

    value rgba =
      new vector_4f (V3.multiply (albedo, light_term), 1.0);
  as
    out out_0 = rgba;
  end;
  
  --
  -- Fragment shader with specular maps
  --

  shader fragment forward_dsm_spherical_fragment is
    in        f_uv        : vector_2f;
    in        f_normal    : vector_3f;
    in        f_position  : vector_4f;
    parameter light       : SL.t;
    parameter material    : M.t;
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
      SL.diffuse_specular (light, n, f_position [x y z], m);

    value albedo =
      S.texture (t_diffuse_0, f_uv) [x y z];

    value rgba =
      new vector_4f (V3.multiply (albedo, light_term), 1.0);
  as
    out out_0 = rgba;
  end;

  shader program forward_ds_spherical is
    vertex   forward_ds_spherical_vertex;
    fragment forward_ds_spherical_fragment;
  end;
  
  shader program forward_dsm_spherical is
    vertex   forward_ds_spherical_vertex;
    fragment forward_dsm_spherical_fragment;
  end;

  --
  -- Diffuse-specular shader, with normal mapping, for directional lights.
  --
  -- Vertex shader for provided bitangents.
  --

  shader vertex forward_dsn_pb_directional_vertex is
    in        v_normal     : vector_3f;
    in        v_tangent3   : vector_3f;
    in        v_bitangent  : vector_3f;
    in        v_position   : vector_3f;
    in        v_uv         : vector_2f;
    parameter m_normal     : matrix_3x3f;
    parameter m_modelview  : matrix_4x4f;
    parameter m_projection : matrix_4x4f;
    out       f_uv         : vector_2f;
    out       f_normal     : vector_3f;
    out       f_tangent    : vector_3f;
    out       f_bitangent  : vector_3f;
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
  as
    out gl_Position = clip_position;
    out f_position  = position;
    out f_uv        = v_uv;
    out f_normal    = v_normal;
    out f_tangent   = v_tangent3;
    out f_bitangent = v_bitangent;
  end;

  --
  -- Vertex shader for computed bitangents.
  --

  shader vertex forward_dsn_cb_directional_vertex is
    in        v_normal     : vector_3f;
    in        v_tangent4   : vector_4f;
    in        v_position   : vector_3f;
    in        v_uv         : vector_2f;
    parameter m_normal     : matrix_3x3f;
    parameter m_modelview  : matrix_4x4f;
    parameter m_projection : matrix_4x4f;
    out       f_uv         : vector_2f;
    out       f_normal     : vector_3f;
    out       f_tangent    : vector_3f;
    out       f_bitangent  : vector_3f;
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
    out f_uv        = v_uv;
    out f_normal    = v_normal;
    out f_tangent   = tangent;
    out f_bitangent = bitangent;
  end;

  --
  -- Fragment shader without specular maps.
  --

  shader fragment forward_dsn_directional_fragment is
    in        f_normal    : vector_3f;
    in        f_tangent   : vector_3f;
    in        f_bitangent : vector_3f;
    in        f_uv        : vector_2f;
    in        f_position  : vector_4f;
    parameter light       : DL.t;
    parameter material    : M.t;
    parameter t_diffuse_0 : sampler_2d;
    parameter t_normal    : sampler_2d;
    parameter m_normal    : matrix_3x3f;
    out       out_0       : vector_4f as 0;    
  with
    value m = N.unpack (t_normal, f_uv);
    value n = V3.normalize (f_normal);
    value t = V3.normalize (f_tangent);
    value b = V3.normalize (f_bitangent);
    value r = N.transform (m, t, b, n);
    value e = M3.multiply_vector (m_normal, r);

    value light_term =
      DL.diffuse_specular (light, e, f_position [x y z], material);

    value albedo =
      S.texture (t_diffuse_0, f_uv) [x y z];

    value rgba =
      new vector_4f (V3.multiply (albedo, light_term), 1.0);
  as
    out out_0 = rgba;
  end;

  --
  -- Fragment shader with specular maps.
  --

  shader fragment forward_dsmn_directional_fragment is
    in        f_normal    : vector_3f;
    in        f_tangent   : vector_3f;
    in        f_bitangent : vector_3f;
    in        f_uv        : vector_2f;
    in        f_position  : vector_4f;
    parameter light       : DL.t;
    parameter material    : M.t;
    parameter t_diffuse_0 : sampler_2d;
    parameter t_specular  : sampler_2d;
    parameter t_normal    : sampler_2d;
    parameter m_normal    : matrix_3x3f;
    out       out_0       : vector_4f as 0;    
  with
    value m = N.unpack (t_normal, f_uv);
    value n = V3.normalize (f_normal);
    value t = V3.normalize (f_tangent);
    value b = V3.normalize (f_bitangent);
    value r = N.transform (m, t, b, n);
    value e = M3.multiply_vector (m_normal, r);

    value mp =
      record M.t {
        specular_exponent  = material.specular_exponent,
        specular_intensity = S.texture (t_specular, f_uv)[x]
      };

    value light_term =
      DL.diffuse_specular (light, e, f_position [x y z], mp);

    value albedo =
      S.texture (t_diffuse_0, f_uv) [x y z];

    value rgba =
      new vector_4f (V3.multiply (albedo, light_term), 1.0);
  as
    out out_0 = rgba;
  end;

  shader program forward_dsn_pb_directional is
    vertex   forward_dsn_pb_directional_vertex;
    fragment forward_dsn_directional_fragment;
  end;

  shader program forward_dsn_cb_directional is
    vertex   forward_dsn_cb_directional_vertex;
    fragment forward_dsn_directional_fragment;
  end;
  
  shader program forward_dsmn_pb_directional is
    vertex   forward_dsn_pb_directional_vertex;
    fragment forward_dsmn_directional_fragment;
  end;

  shader program forward_dsmn_cb_directional is
    vertex   forward_dsn_cb_directional_vertex;
    fragment forward_dsmn_directional_fragment;
  end;

  --
  -- Diffuse-specular shader, with normal mapping, for spherical lights.
  --
  -- Vertex shader for provided bitangents.
  --

  shader vertex forward_dsn_pb_spherical_vertex is
    in        v_normal     : vector_3f;
    in        v_tangent3   : vector_3f;
    in        v_bitangent  : vector_3f;
    in        v_position   : vector_3f;
    in        v_uv         : vector_2f;
    parameter m_modelview  : matrix_4x4f;
    parameter m_projection : matrix_4x4f;
    out       f_uv         : vector_2f;
    out       f_normal     : vector_3f;
    out       f_tangent    : vector_3f;
    out       f_bitangent  : vector_3f;
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
  as
    out gl_Position = clip_position;
    out f_position  = position;
    out f_uv        = v_uv;
    out f_normal    = v_normal;
    out f_tangent   = v_tangent3;
    out f_bitangent = v_bitangent;
  end;

  --
  -- Vertex shader for computed bitangents.
  --

  shader vertex forward_dsn_cb_spherical_vertex is
    in        v_normal     : vector_3f;
    in        v_tangent4   : vector_4f;
    in        v_position   : vector_3f;
    in        v_uv         : vector_2f;
    parameter m_modelview  : matrix_4x4f;
    parameter m_projection : matrix_4x4f;
    out       f_uv         : vector_2f;
    out       f_normal     : vector_3f;
    out       f_tangent    : vector_3f;
    out       f_bitangent  : vector_3f;
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
    out f_uv        = v_uv;
    out f_normal    = v_normal;
    out f_tangent   = tangent;
    out f_bitangent = bitangent;
  end;

  --
  -- Fragment shader without specular maps.
  --

  shader fragment forward_dsn_spherical_fragment is
    in        f_normal    : vector_3f;
    in        f_tangent   : vector_3f;
    in        f_bitangent : vector_3f;
    in        f_uv        : vector_2f;
    in        f_position  : vector_4f;
    parameter light       : SL.t;
    parameter material    : M.t;
    parameter t_diffuse_0 : sampler_2d;
    parameter t_normal    : sampler_2d;
    parameter m_normal    : matrix_3x3f;
    out       out_0       : vector_4f as 0;    
  with
    value m = N.unpack (t_normal, f_uv);
    value n = V3.normalize (f_normal);
    value t = V3.normalize (f_tangent);
    value b = V3.normalize (f_bitangent);
    value r = N.transform (m, t, b, n);
    value e = M3.multiply_vector (m_normal, r);

    value light_term =
      SL.diffuse_specular (light, e, f_position [x y z], material);

    value albedo =
      S.texture (t_diffuse_0, f_uv) [x y z];

    value rgba =
      new vector_4f (V3.multiply (albedo, light_term), 1.0);
  as
    out out_0 = rgba;
  end;
  
  --
  -- Fragment shader with specular maps.
  --

  shader fragment forward_dsmn_spherical_fragment is
    in        f_normal    : vector_3f;
    in        f_tangent   : vector_3f;
    in        f_bitangent : vector_3f;
    in        f_uv        : vector_2f;
    in        f_position  : vector_4f;
    parameter light       : SL.t;
    parameter material    : M.t;
    parameter t_diffuse_0 : sampler_2d;
    parameter t_specular  : sampler_2d;
    parameter t_normal    : sampler_2d;
    parameter m_normal    : matrix_3x3f;
    out       out_0       : vector_4f as 0;    
  with
    value m = N.unpack (t_normal, f_uv);
    value n = V3.normalize (f_normal);
    value t = V3.normalize (f_tangent);
    value b = V3.normalize (f_bitangent);
    value r = N.transform (m, t, b, n);
    value e = M3.multiply_vector (m_normal, r);

    value mp =
      record M.t {
        specular_exponent  = material.specular_exponent,
        specular_intensity = S.texture (t_specular, f_uv)[x]
      };

    value light_term =
      SL.diffuse_specular (light, e, f_position [x y z], mp);

    value albedo =
      S.texture (t_diffuse_0, f_uv) [x y z];

    value rgba =
      new vector_4f (V3.multiply (albedo, light_term), 1.0);
  as
    out out_0 = rgba;
  end;

  shader program forward_dsn_pb_spherical is
    vertex   forward_dsn_pb_spherical_vertex;
    fragment forward_dsn_spherical_fragment;
  end;

  shader program forward_dsn_cb_spherical is
    vertex   forward_dsn_cb_spherical_vertex;
    fragment forward_dsn_spherical_fragment;
  end;
  
  shader program forward_dsmn_pb_spherical is
    vertex   forward_dsn_pb_spherical_vertex;
    fragment forward_dsn_spherical_fragment;
  end;

  shader program forward_dsmn_cb_spherical is
    vertex   forward_dsn_cb_spherical_vertex;
    fragment forward_dsmn_spherical_fragment;
  end;

end;
