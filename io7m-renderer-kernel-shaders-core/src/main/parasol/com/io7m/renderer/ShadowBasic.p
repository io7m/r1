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

module ShadowBasic is

  import com.io7m.parasol.Matrix3x3f as M3;
  import com.io7m.parasol.Matrix4x4f as M4;
  import com.io7m.parasol.Vector2f   as V2;
  import com.io7m.parasol.Vector3f   as V3;
  import com.io7m.parasol.Float      as F;
  import com.io7m.parasol.Sampler2D  as S2;

  import com.io7m.renderer.Albedo    as A;
  import com.io7m.renderer.Materials as M;
  import com.io7m.renderer.Pack      as P;
  import com.io7m.renderer.Transform as T;

  --
  -- Basic shadow mapping parameters.
  --

  type t is record
    depth_bias : float,
    factor_min : float,
    factor_max : float
  end;

  --
  -- Given a shadow map [t_shadow], and clip-coordinates [p], return
  -- the amount of light that could be reaching [p] (where
  -- 0.0 is fully shadowed and 1.0 is fully lit).
  --

  function factor (
    shadow   : t,
    t_shadow : sampler_2d,
    p        : vector_4f
  ) : float =
    if F.lesser (p [w], 0.0) then
      0.0
    else
      let
        value current_tex =
          T.clip_to_texture (p);
        value map_depth =
          S2.texture (t_shadow, current_tex [x y]) [x];
        value map_depth_adjusted =
          F.add (map_depth, shadow.depth_bias);
      in
        if F.lesser (current_tex [z], map_depth_adjusted) then
          shadow.factor_max
        else
          shadow.factor_min
        end
      end
    end;

  --
  -- Given a packed RGBA4444 shadow map [t_shadow], and clip-coordinates [p], 
  -- return the amount of light that could be reaching [p] (where
  -- 0.0 is fully shadowed and 1.0 is fully lit).
  --

  function factor_packed4444 (
    shadow   : t,
    t_shadow : sampler_2d,
    p        : vector_4f
  ) : float =
    if F.lesser (p [w], 0.0) then
      0.0
    else
      let
        value current_tex =
          T.clip_to_texture (p);
        value rgba =
          S2.texture (t_shadow, current_tex [x y]);
        value map_depth =
          P.unpack4444 (rgba);
        value map_depth_adjusted =
          F.add (map_depth, shadow.depth_bias);
      in
        if F.lesser (current_tex [z], map_depth_adjusted) then
          shadow.factor_max
        else
          shadow.factor_min
        end
      end
    end;

  --
  -- A vertex shader that transforms vertices such that the scene
  -- is rendered from the _perspective of the projective light_, and
  -- the clip-space position of the vertex _as it appeared in the
  -- original scene_ is passed to the fragment shader as f_position_clip.
  -- This is the exact opposite of what occurs when applying projective
  -- lighting to the scene.
  --

  shader vertex shadow_simple_v is
    in         v_position              : vector_3f;
    out        f_position              : vector_4f;
    out vertex f_position_light_clip   : vector_4f;
    out        f_position_scene_clip   : vector_4f;
    parameter  m_modelview             : matrix_4x4f;
    parameter  m_projection            : matrix_4x4f;
    parameter  m_projective_modelview  : matrix_4x4f;
    parameter  m_projective_projection : matrix_4x4f;
  with
    value light_clip_position =
      M4.multiply_vector (
        M4.multiply (m_projective_projection, m_projective_modelview),
        new vector_4f (v_position, 1.0)
      );
    value light_position =
      M4.multiply_vector (
        m_projective_modelview,
        new vector_4f (v_position, 1.0)
      );
    value scene_clip_position =
      M4.multiply_vector (
        M4.multiply (m_projection, m_modelview),
        new vector_4f (v_position, 1.0)
      );
  as
    out f_position_light_clip = light_clip_position;
    out f_position_scene_clip = scene_clip_position;
    out f_position            = light_position;
  end;

  --
  -- Shadow map rendering for constant-depth objects.
  --

  shader fragment shadow_BC_f is
    in f_position_scene_clip : vector_4f;
    in f_position            : vector_4f;
    parameter t_scene_depth  : sampler_2d;
    parameter shadow_basic   : t;
    out out_0                : vector_4f as 0;
  with
    value f =
      factor (shadow_basic, t_scene_depth, f_position_scene_clip);
    value rgba =
      new vector_4f (f, f, f, 1.0);
  as
    out out_0 = rgba;
  end;

  shader fragment shadow_BCP4_f is
    in f_position_scene_clip : vector_4f;
    in f_position            : vector_4f;
    parameter t_scene_depth  : sampler_2d;
    parameter shadow_basic   : t;
    out out_0                : vector_4f as 0;
  with
    value f =
      factor_packed4444 (shadow_basic, t_scene_depth, f_position_scene_clip);
    value rgba =
      new vector_4f (f, f, f, 1.0);
  as
    out out_0 = rgba;
  end;

  shader program shadow_BC is
    vertex   shadow_simple_v;
    fragment shadow_BC_f;
  end;

  shader program shadow_BCP4 is
    vertex   shadow_simple_v;
    fragment shadow_BCP4_f;
  end;

  --
  -- Shadow map rendering for uniform-depth objects.
  --

  shader fragment shadow_BU_f is
    in f_position_scene_clip : vector_4f;
    in f_position            : vector_4f;
    parameter t_scene_depth  : sampler_2d;
    parameter shadow_basic   : t;
    parameter material       : M.t;
    out out_0                : vector_4f as 0;
  with
    value albedo : vector_4f = A.translucent (material.albedo);
    value alpha = F.multiply (albedo [w], material.alpha.opacity);
    discard (F.lesser (alpha, material.alpha.depth_threshold));
  
    value f =
      factor (shadow_basic, t_scene_depth, f_position_scene_clip);
    value rgba =
      new vector_4f (f, f, f, 1.0);
  as
    out out_0 = rgba;
  end;

  shader fragment shadow_BUP4_f is
    in f_position_scene_clip : vector_4f;
    in f_position            : vector_4f;
    parameter t_scene_depth  : sampler_2d;
    parameter shadow_basic   : t;
    parameter material       : M.t;
    out out_0                : vector_4f as 0;
  with
    value albedo : vector_4f = A.translucent (material.albedo);
    value alpha = F.multiply (albedo [w], material.alpha.opacity);
    discard (F.lesser (alpha, material.alpha.depth_threshold));

    value f =
      factor_packed4444 (shadow_basic, t_scene_depth, f_position_scene_clip);
    value rgba =
      new vector_4f (f, f, f, 1.0);
  as
    out out_0 = rgba;
  end;

  shader program shadow_BU is
    vertex   shadow_simple_v;
    fragment shadow_BU_f;
  end;

  shader program shadow_BUP4 is
    vertex   shadow_simple_v;
    fragment shadow_BUP4_f;
  end;

  --
  -- Shadow map rendering for mapped-depth objects.
  --

  --
  -- A vertex shader that transforms vertices such that the scene
  -- is rendered from the _perspective of the projective light_, and
  -- the clip-space position of the vertex _as it appeared in the
  -- original scene_ is passed to the fragment shader as f_position_clip.
  -- This is the exact opposite of what occurs when applying projective
  -- lighting to the scene.
  --

  shader vertex shadow_textured_v is
    in         v_uv                    : vector_2f;
    in         v_position              : vector_3f;
    out        f_position              : vector_4f;
    out        f_uv                    : vector_2f;
    out vertex f_position_light_clip   : vector_4f;
    out        f_position_scene_clip   : vector_4f;
    parameter  m_modelview             : matrix_4x4f;
    parameter  m_projection            : matrix_4x4f;
    parameter  m_projective_modelview  : matrix_4x4f;
    parameter  m_projective_projection : matrix_4x4f;
    parameter  m_uv                    : matrix_3x3f;
  with
    value light_clip_position =
      M4.multiply_vector (
        M4.multiply (m_projective_projection, m_projective_modelview),
        new vector_4f (v_position, 1.0)
      );
    value light_position =
      M4.multiply_vector (
        m_projective_modelview,
        new vector_4f (v_position, 1.0)
      );
    value scene_clip_position =
      M4.multiply_vector (
        M4.multiply (m_projection, m_modelview),
        new vector_4f (v_position, 1.0)
      );
    value uv =
      M3.multiply_vector (m_uv, new vector_3f (v_uv, 1.0)) [x y];
  as
    out f_position_light_clip = light_clip_position;
    out f_position_scene_clip = scene_clip_position;
    out f_position            = light_position;
    out f_uv                  = uv;
  end;

  shader fragment shadow_BM_f is
    in f_position_scene_clip : vector_4f;
    in f_position            : vector_4f;
    in f_uv                  : vector_2f;
    parameter t_albedo       : sampler_2d;
    parameter t_scene_depth  : sampler_2d;
    parameter shadow_basic   : t;
    parameter material       : M.t;
    out out_0                : vector_4f as 0;
  with
    value albedo : vector_4f =
      A.textured_translucent (
        t_albedo,
        f_uv,
        material.albedo
      );
    value alpha = F.multiply (albedo [w], material.alpha.opacity);
    discard (F.lesser (alpha, material.alpha.depth_threshold));
  
    value f =
      factor (shadow_basic, t_scene_depth, f_position_scene_clip);
    value rgba =
      new vector_4f (f, f, f, 1.0);
  as
    out out_0 = rgba;
  end;

  shader fragment shadow_BMP4_f is
    in f_position_scene_clip : vector_4f;
    in f_position            : vector_4f;
    in f_uv                  : vector_2f;
    parameter t_albedo       : sampler_2d;
    parameter t_scene_depth  : sampler_2d;
    parameter shadow_basic   : t;
    parameter material       : M.t;
    out out_0                : vector_4f as 0;
  with
    value albedo : vector_4f =
      A.textured_translucent (
        t_albedo,
        f_uv,
        material.albedo
      );
    value alpha = F.multiply (albedo [w], material.alpha.opacity);
    discard (F.lesser (alpha, material.alpha.depth_threshold));

    value f =
      factor_packed4444 (shadow_basic, t_scene_depth, f_position_scene_clip);
    value rgba =
      new vector_4f (f, f, f, 1.0);
  as
    out out_0 = rgba;
  end;

  shader program shadow_BM is
    vertex   shadow_textured_v;
    fragment shadow_BM_f;
  end;

  shader program shadow_BMP4 is
    vertex   shadow_textured_v;
    fragment shadow_BMP4_f;
  end;

end;