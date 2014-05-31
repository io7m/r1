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

package com.io7m.renderer.core;

module ShadowVariance is

  import com.io7m.parasol.Matrix3x3f as M3;
  import com.io7m.parasol.Matrix4x4f as M4;
  import com.io7m.parasol.Vector2f   as V2;
  import com.io7m.parasol.Vector3f   as V3;
  import com.io7m.parasol.Float      as F;
  import com.io7m.parasol.Sampler2D  as S2;

  import com.io7m.renderer.core.Transform;

  type t is record
    factor_min      : float,
    variance_min    : float,
    bleed_reduction : float
  end;

  function chebyshev_upper_bound (
    config        : t,
    moments       : vector_2f,
    depth_current : float
  ) : float =
    let
      value p        = new float (F.lesser_or_equal (depth_current, moments [x]));
      value variance = F.subtract (moments [y], F.multiply (moments [x], moments [x]));
      value variance = F.maximum (config.variance_min, variance);
      value delta    = F.subtract (depth_current, moments [x]);
      value p_max    = F.divide (variance, F.add (variance, F.multiply (delta, delta)));
    in
      F.maximum (p, p_max)
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
    let
      value not_behind =
        new float (F.greater_or_equal (p [w], 0.0));
      value current_tex =
        Transform.clip_to_texture (p);
      value moments =
        S2.texture (t_shadow_variance, current_tex [x y]) [x y];
      value p_max =
        chebyshev_upper_bound (config, moments, current_tex [z]);
      value clamped =
        F.maximum (
          linear_step (config.bleed_reduction, 1.0, p_max),
          config.factor_min
        );
    in
      F.multiply (clamped, not_behind)
    end;

end;
