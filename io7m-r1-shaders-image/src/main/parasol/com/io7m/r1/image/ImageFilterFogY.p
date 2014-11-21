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

package com.io7m.r1.image;

module ImageFilterFogY is

  import com.io7m.parasol.Float      as F;
  import com.io7m.parasol.Fragment;
  import com.io7m.parasol.Matrix3x3f as M3;
  import com.io7m.parasol.Matrix4x4f as M4;
  import com.io7m.parasol.Vector3f   as V3;
  import com.io7m.parasol.Vector4f   as V4;
  import com.io7m.parasol.Sampler2D  as S;

  import com.io7m.r1.core.Reconstruction;
  import com.io7m.r1.core.Transform;
  import com.io7m.r1.core.VectorAux;
  import com.io7m.r1.core.VertexShaders;
  import com.io7m.r1.core.ViewRays;

  --
  -- Y fog: Fog that increases along the global Y axis.
  --

  type t is record
    color   : vector_3f,
    upper_y : float,
    lower_y : float
  end;

  function fog_factor_floor (
    y       : float,
    y_lower : float,
    y_upper : float
  ) : float =
    let
      value t0 = F.subtract (y, y_lower);
      value t1 = F.subtract (y_upper, y_lower);
      value r  = F.divide (t0, t1);
    in
      F.clamp (r, 0.0, 1.0)
    end;

  shader fragment fog_linear_y_floor_f is
    parameter fog          : t;
    parameter m_view_inv   : matrix_4x4f;
    parameter m_projection : matrix_4x4f;
    parameter t_map_depth  : sampler_2d;
    parameter t_image      : sampler_2d;
    parameter view_rays    : ViewRays.t;
    in f_uv                : vector_2f;
    out out_0              : vector_4f as 0;
  with
    value color_sample =
      S.texture (t_image, f_uv);

    -- Reconstruct eye-space position.
    value depth_sample = 
      S.texture (t_map_depth, f_uv) [x];
    value eye_position =
      Reconstruction.reconstruct_eye (
        depth_sample,
        f_uv,
        m_projection,
        view_rays
      );
    value world_position =
      M4.multiply_vector (m_view_inv, eye_position);

    value factor =
      fog_factor_floor (world_position [y], fog.lower_y, fog.upper_y);

    value rgba =
      V4.interpolate (
        color_sample,
        new vector_4f (fog.color, 1.0),
        factor
      );
  as
    out out_0 = rgba;
  end;

  shader fragment fog_exponential_y_floor_f is
    parameter fog          : t;
    parameter m_view_inv   : matrix_4x4f;
    parameter m_projection : matrix_4x4f;
    parameter t_map_depth  : sampler_2d;
    parameter t_image      : sampler_2d;
    parameter view_rays    : ViewRays.t;
    in f_uv                : vector_2f;
    out out_0              : vector_4f as 0;
  with
    value color_sample =
      S.texture (t_image, f_uv);

    -- Reconstruct eye-space position.
    value depth_sample = 
      S.texture (t_map_depth, f_uv) [x];
    value eye_position =
      Reconstruction.reconstruct_eye (
        depth_sample,
        f_uv,
        m_projection,
        view_rays
      );
    value world_position =
      M4.multiply_vector (m_view_inv, eye_position);

    value factor =
      fog_factor_floor (world_position [y], fog.lower_y, fog.upper_y);

    value rgba =
      V4.interpolate (
        color_sample,
        new vector_4f (fog.color, 1.0),
        F.multiply (factor, factor)
      );
  as
    out out_0 = rgba;
  end;

  shader fragment fog_logarithmic_y_floor_f is
    parameter fog          : t;
    parameter m_view_inv   : matrix_4x4f;
    parameter m_projection : matrix_4x4f;
    parameter t_map_depth  : sampler_2d;
    parameter t_image      : sampler_2d;
    parameter view_rays    : ViewRays.t;
    in f_uv                : vector_2f;
    out out_0              : vector_4f as 0;
  with
    value color_sample =
      S.texture (t_image, f_uv);

    -- Reconstruct eye-space position.
    value depth_sample = 
      S.texture (t_map_depth, f_uv) [x];
    value eye_position =
      Reconstruction.reconstruct_eye (
        depth_sample,
        f_uv,
        m_projection,
        view_rays
      );
    value world_position =
      M4.multiply_vector (m_view_inv, eye_position);

    value factor =
      fog_factor_floor (world_position [y], fog.lower_y, fog.upper_y);

    value rgba =
      V4.interpolate (
        color_sample,
        new vector_4f (fog.color, 1.0),
        F.square_root (factor)
      );
  as
    out out_0 = rgba;
  end;

  shader program fog_linear_y_floor is
    vertex   VertexShaders.standard_clip;
    fragment fog_linear_y_floor_f;
  end;

  shader program fog_exponential_y_floor is
    vertex   VertexShaders.standard_clip;
    fragment fog_exponential_y_floor_f;
  end;

  shader program fog_logarithmic_y_floor is
    vertex   VertexShaders.standard_clip;
    fragment fog_logarithmic_y_floor_f;
  end;

end;
