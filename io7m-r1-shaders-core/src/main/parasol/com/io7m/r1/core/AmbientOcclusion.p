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

package com.io7m.r1.core;

--
-- Ambient occlusion functions.
--

module AmbientOcclusion is

  import com.io7m.parasol.Float      as F;
  import com.io7m.parasol.Fragment;
  import com.io7m.parasol.Matrix3x3f as M3;
  import com.io7m.parasol.Matrix4x4f as M4;
  import com.io7m.parasol.Vector2f   as V2;
  import com.io7m.parasol.Vector3f   as V3;
  import com.io7m.parasol.Vector4f   as V4;
  import com.io7m.parasol.Sampler2D  as S;

  import com.io7m.r1.core.Light;
  import com.io7m.r1.core.LogDepth;
  import com.io7m.r1.core.Normals;
  import com.io7m.r1.core.Reconstruction;
  import com.io7m.r1.core.Transform;
  import com.io7m.r1.core.VectorAux;
  import com.io7m.r1.core.Viewport;
  import com.io7m.r1.core.ViewRays;
  import com.io7m.r1.core.VertexShaders;

  type t is record
    scale         : float,
    bias          : float,
    intensity     : float,
    sample_radius : float
  end;

  function eye_position_for_uv (
    map_depth         : sampler_2d,
    m_projection      : matrix_4x4f,
    view_rays         : ViewRays.t,
    uv                : vector_2f,
    depth_coefficient : float
  ) : vector_4f =
    let
      value log_depth =
        S.texture (map_depth, uv) [x];
      value eye_depth =
        F.negate (LogDepth.decode (log_depth, depth_coefficient));
      value eye_position =
        Reconstruction.reconstruct_eye_with_eye_z (
          eye_depth,
          uv,
          m_projection,
          view_rays
        );
     in
       eye_position
     end;

  function point_occlusion (
    config     : t,
    p_occludee : vector_4f,
    p_occluder : vector_4f,
    normal     : vector_3f
  ) : float =
    let
      value diff             = V4.subtract (p_occluder, p_occludee) [x y z];
      value diff_n           = V3.normalize (diff);
      value mag              = F.multiply (V3.magnitude (diff), config.scale);
      value contribution_raw = F.subtract (V3.dot (normal, diff_n), config.bias);
      value contribution     = F.maximum (0.0, contribution_raw);
      value scale            = F.divide (1.0, F.add (1.0, mag));
      value intensity        = F.multiply (scale, config.intensity);
    in
      F.multiply (contribution, intensity)
    end;

  function occlusion_iteration (
    config            : t,
    map_depth         : sampler_2d,
    m_projection      : matrix_4x4f,
    view_rays         : ViewRays.t,
    depth_coefficient : float,
    radius_scale      : float,
    p_occludee        : vector_4f,
    reflection        : vector_2f,
    random            : vector_2f,
    uv                : vector_2f,
    normal            : vector_3f
  ) : float =
    let
      value offset_0 =
        V2.multiply_scalar (
          V2.reflect (reflection, random),
          radius_scale
        );
      value sx =
        F.multiply (offset_0 [x], 0.707);
      value sy =
        F.multiply (offset_0 [y], 0.707);
      value offset_1 =
        new vector_2f (
          F.subtract (sx, sy),
          F.add (sx, sy)
        );

      value occluder_0 =
        eye_position_for_uv (
          map_depth,
          m_projection,
          view_rays,
          V2.add (uv, V2.multiply_scalar (offset_0, 0.25)),
          depth_coefficient
        );
      value occluder_1 =
        eye_position_for_uv (
          map_depth,
          m_projection,
          view_rays,
          V2.add (uv, V2.multiply_scalar (offset_0, 0.75)),
          depth_coefficient
        );
      value occluder_2 =
        eye_position_for_uv (
          map_depth,
          m_projection,
          view_rays,
          V2.add (uv, V2.multiply_scalar (offset_1, 0.5)),
          depth_coefficient
        );
      value occluder_3 =
        eye_position_for_uv (
          map_depth,
          m_projection,
          view_rays,
          V2.add (uv, offset_1),
          depth_coefficient
        );
        
      value a0 =
        point_occlusion (config, p_occludee, occluder_0, normal);
      value a1 =
        point_occlusion (config, p_occludee, occluder_1, normal);
      value a2 =
        point_occlusion (config, p_occludee, occluder_2, normal);
      value a3 =
        point_occlusion (config, p_occludee, occluder_3, normal);
    in
      F.add (F.add (F.add (a0, a1), a2), a3)
    end;

  shader fragment ssao_x16_f is

    -- Logarithmic depth parameters
    in f_positive_eye_z         : float;
    parameter depth_coefficient : float;

    -- Matrices
    parameter m_view_inv   : matrix_4x4f;
    parameter m_projection : matrix_4x4f;

    -- G-buffer components
    parameter t_map_normal : sampler_2d;
    parameter t_map_depth  : sampler_2d;

    -- Random texture
    parameter t_noise : sampler_2d;

    -- Standard declarations
    out out_0 : vector_4f as 0;

    parameter view_rays : ViewRays.t;
    parameter viewport  : Viewport.t;

    parameter ssao : t;

  with
    value position_uv =
      Transform.screen_to_texture2 (
        viewport,
        Fragment.coordinate [x y]
      );
    
    value noise_uv =
      V2.divide_scalar (Fragment.coordinate [x y], 256.0);

    -- Get surface normal
    value normal_sample =
      S.texture (t_map_normal, position_uv) [x y];
    value normal =
      V3.normalize (Normals.decompress (normal_sample));

    -- Get random sample in range [-1, 1]
    value random_sample =
      S.texture (t_noise, noise_uv) [x y];
    value random =
      V2.subtract_scalar (V2.multiply_scalar (random_sample, 2.0), 1.0);

    -- Reconstruct eye-space position.
    value eye_position =
      eye_position_for_uv (
        t_map_depth,
        m_projection,
        view_rays,
        position_uv,
        depth_coefficient
      );

    -- Scale the sampling radius by Z to account for perspective.
    value radius_scale =
      F.divide (ssao.sample_radius, eye_position [z]);

    -- Reflection vectors.
    value reflect_0 = new vector_2f (1.0, 0.0);
    value reflect_1 = new vector_2f (-1.0, 0.0);
    value reflect_2 = new vector_2f (0.0, 1.0);
    value reflect_3 = new vector_2f (0.0, -1.0);

    value occ0 =
      occlusion_iteration (
        ssao,
        t_map_depth,
        m_projection,
        view_rays,
        depth_coefficient,
        radius_scale,
        eye_position,
        reflect_0,
        random,
        position_uv,
        normal
      );
    value occ1 =
      occlusion_iteration (
        ssao,
        t_map_depth,
        m_projection,
        view_rays,
        depth_coefficient,
        radius_scale,
        eye_position,
        reflect_1,
        random,
        position_uv,
        normal
      );
    value occ2 =
      occlusion_iteration (
        ssao,
        t_map_depth,
        m_projection,
        view_rays,
        depth_coefficient,
        radius_scale,
        eye_position,
        reflect_2,
        random,
        position_uv,
        normal
      );
    value occ3 =
      occlusion_iteration (
        ssao,
        t_map_depth,
        m_projection,
        view_rays,
        depth_coefficient,
        radius_scale,
        eye_position,
        reflect_3,
        random,
        position_uv,
        normal
      );

    value occ =
      F.subtract (1.0, F.divide (F.add (F.add (F.add (occ0, occ1), occ2), occ3), 4.0));
    value r =
      new vector_4f (occ, 1.0, 1.0, 1.0);
  as
    out out_0 = r;
  end;

  shader fragment ssao_x8_f is

    -- Logarithmic depth parameters
    in f_positive_eye_z         : float;
    parameter depth_coefficient : float;

    -- Matrices
    parameter m_view_inv   : matrix_4x4f;
    parameter m_projection : matrix_4x4f;

    -- G-buffer components
    parameter t_map_normal : sampler_2d;
    parameter t_map_depth  : sampler_2d;

    -- Random texture
    parameter t_noise : sampler_2d;

    -- Standard declarations
    out out_0 : vector_4f as 0;

    parameter view_rays : ViewRays.t;
    parameter viewport  : Viewport.t;

    parameter ssao : t;

  with
    value position_uv =
      Transform.screen_to_texture2 (
        viewport,
        Fragment.coordinate [x y]
      );
    
    value noise_uv =
      V2.divide_scalar (Fragment.coordinate [x y], 256.0);

    -- Get surface normal
    value normal_sample =
      S.texture (t_map_normal, position_uv) [x y];
    value normal =
      V3.normalize (Normals.decompress (normal_sample));

    -- Get random sample in range [-1, 1]
    value random_sample =
      S.texture (t_noise, noise_uv) [x y];
    value random =
      V2.subtract_scalar (V2.multiply_scalar (random_sample, 2.0), 1.0);

    -- Reconstruct eye-space position.
    value eye_position =
      eye_position_for_uv (
        t_map_depth,
        m_projection,
        view_rays,
        position_uv,
        depth_coefficient
      );

    -- Scale the sampling radius by Z to account for perspective.
    value radius_scale =
      F.divide (ssao.sample_radius, eye_position [z]);

    -- Reflection vectors.
    value reflect_0 = new vector_2f (1.0, 0.0);
    value reflect_2 = new vector_2f (0.0, 1.0);

    value occ0 =
      occlusion_iteration (
        ssao,
        t_map_depth,
        m_projection,
        view_rays,
        depth_coefficient,
        radius_scale,
        eye_position,
        reflect_0,
        random,
        position_uv,
        normal
      );
    value occ2 =
      occlusion_iteration (
        ssao,
        t_map_depth,
        m_projection,
        view_rays,
        depth_coefficient,
        radius_scale,
        eye_position,
        reflect_2,
        random,
        position_uv,
        normal
      );

    value occ =
      F.subtract (1.0, F.divide (F.add (occ0, occ2), 2.0));
    value r =
      new vector_4f (occ, 1.0, 1.0, 1.0);
  as
    out out_0 = r;
  end;

  shader program ssao_x16 is
    vertex   VertexShaders.standard_clip_eye;
    fragment ssao_x16_f;
  end;

  shader program ssao_x8 is
    vertex   VertexShaders.standard_clip_eye;
    fragment ssao_x8_f;
  end;

end;