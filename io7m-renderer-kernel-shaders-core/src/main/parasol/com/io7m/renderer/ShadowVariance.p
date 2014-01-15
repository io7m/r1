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

module ShadowVariance is

  import com.io7m.parasol.Matrix3x3f as M3;
  import com.io7m.parasol.Matrix4x4f as M4;
  import com.io7m.parasol.Vector2f   as V2;
  import com.io7m.parasol.Vector3f   as V3;
  import com.io7m.parasol.Float      as F;
  import com.io7m.parasol.Sampler2D  as S2;

  import com.io7m.renderer.Albedo    as A;
  import com.io7m.renderer.Pack      as P;
  import com.io7m.renderer.Materials as M;
  import com.io7m.renderer.Transform as T;

  type t is record
    factor_min      : float,
    factor_max      : float,
    variance_min    : float,
    bleed_reduction : float
  end;

  function chebyshev_upper_bound (
    config  : t,
    moments : vector_2f,
    depth   : float
  ) : float =
    let
      value variance = F.subtract (moments [y], F.multiply (moments [x], moments [x]));
      value variance = F.maximum (config.variance_min, variance);
      value delta    = F.subtract (depth, moments [x]);
      value max      = F.divide (variance, F.add (variance, F.multiply (delta, delta)));
    in
      max
    end;

  function linear_step (
    min : float,
    max : float,
    x   : float
  ) : float =
    let
      value vsm = F.subtract (x, min);
      value msm = F.subtract (max, min);
    in
      F.clamp (F.divide (vsm, msm), 0.0, 1.0)
    end;

  function factor (
    config            : t,
    t_shadow_variance : sampler_2d,
    p                 : vector_4f
  ) : float =
    if F.lesser (p [w], 0.0) then
      0.0
    else
      let
        value current_tex =
          T.clip_to_texture (p);
        value moments =
          S2.texture (t_shadow_variance, current_tex [x y]) [x y];
        value p_max =
          chebyshev_upper_bound (config, moments, current_tex [z]);
      in
        F.clamp (
          linear_step (config.bleed_reduction, 1.0, p_max),
          config.factor_min,
          config.factor_max)
      end
    end;

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

  shader fragment shadow_VC_f is
    in f_position_scene_clip    : vector_4f;
    in f_position               : vector_4f;
    parameter t_shadow_variance : sampler_2d;
    parameter shadow_variance   : t;
    out out_0                   : vector_4f as 0;
  with
    value f =
      factor (
        shadow_variance,
        t_shadow_variance,
        f_position_scene_clip
      );
    value rgba =
      new vector_4f (f, f, f, 1.0);
  as
    out out_0 = rgba;
  end;

  shader program shadow_VC is
    vertex   shadow_simple_v;
    fragment shadow_VC_f;
  end;

end;
