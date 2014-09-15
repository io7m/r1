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

module ShadowBasic is

  import com.io7m.parasol.Matrix3x3f as M3;
  import com.io7m.parasol.Matrix4x4f as M4;
  import com.io7m.parasol.Vector2f   as V2;
  import com.io7m.parasol.Vector3f   as V3;
  import com.io7m.parasol.Float      as F;
  import com.io7m.parasol.Sampler2D  as S2;

  import com.io7m.r1.core.Paraboloid;
  import com.io7m.r1.core.Transform;

  --
  -- Basic shadow mapping parameters.
  --

  type t is record
    depth_bias : float,
    factor_min : float
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
          Transform.clip_to_texture (p);
        value map_depth =
          S2.texture (t_shadow, current_tex [x y]) [x];
        value map_depth_adjusted =
          F.add (map_depth, shadow.depth_bias);
      in
        if F.lesser (current_tex [z], map_depth_adjusted) then
          1.0
        else
          shadow.factor_min
        end
      end
    end;

  --
  -- Given two paraboloid shadow map hemispheres [t_shadow_neg_z] and [t_shadow_pos_z], 
  -- and eye-coordinates [eye], return the amount of light that could be reaching [eye] 
  -- (where 0.0 is fully shadowed and 1.0 is fully lit).
  --

  function factor_paraboloid (
    shadow         : t,
    t_shadow_neg_z : sampler_2d,
    t_shadow_pos_z : sampler_2d,
    eye            : vector_4f,
    z_near         : float,
    z_far          : float
  ) : float =
  
    -- If the depth of the current eye space position is negative,
    -- then it means that the position is closer to positive Z than
    -- the observer (light), and therefore the positive hemisphere
    -- map must be inspected for depth values.

    if F.lesser (eye [z], 0.0) then
      let
        value pc =
          Paraboloid.calculate (eye, z_near, z_far);
        value tc =
          V2.multiply_scalar (V2.add_scalar (pc [x y], 1.0), 0.5);
        value map_depth =
          S2.texture (t_shadow_neg_z, tc) [x];
        value map_depth_adjusted =
          F.add (map_depth, shadow.depth_bias);
      in
        if F.lesser (pc [z], map_depth_adjusted) then
          1.0
        else
          shadow.factor_min
        end
      end
    else
      let
        value pc =
          Paraboloid.calculate (eye, z_near, z_far);
        value tc =
          V2.multiply_scalar (V2.add_scalar (pc [x y], 1.0), 0.5);
        value map_depth =
          S2.texture (t_shadow_neg_z, tc) [x];
        value map_depth_adjusted =
          F.add (map_depth, shadow.depth_bias);
      in
        if F.lesser (pc [z], map_depth_adjusted) then
          1.0
        else
          shadow.factor_min
        end
      end
    end; 

end;
