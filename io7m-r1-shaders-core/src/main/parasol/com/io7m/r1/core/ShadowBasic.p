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

  import com.io7m.r1.core.Light;
  import com.io7m.r1.core.LogDepth;
  import com.io7m.r1.core.Transform;

  --
  -- Basic shadow mapping parameters.
  --

  type t is record
    depth_bias        : float,
    depth_coefficient : float,
    factor_min        : float
  end;

  --
  -- Given a shadow map [t_shadow], light-clip-coordinates [pos_light_clip], and
  -- light-eye-coordinates [pos_light_eye], return the amount of light that could 
  -- be reaching [pos_light_clip] (where 0.0 is fully shadowed and 1.0 is fully lit).
  --

  function factor (
    vectors        : Light.vectors,
    shadow         : t,
    t_shadow       : sampler_2d,
    pos_light_eye  : vector_4f,
    pos_light_clip : vector_4f
  ) : float =
    if F.lesser (pos_light_clip [w], 0.0) then
      0.0
    else
      let
        value current_tex =
          Transform.clip_to_texture3 (pos_light_clip);
        value current_depth =
          LogDepth.encode_partial (
            LogDepth.prepare_eye_z (pos_light_eye [z]),
            shadow.depth_coefficient
          );

        value map_depth_log =
          S2.texture (t_shadow, current_tex [x y]) [x];
        value map_depth =
          F.add (map_depth_log, shadow.depth_bias);
      in
        if F.lesser (current_depth, map_depth) then
          1.0
        else
          shadow.factor_min
        end
      end
    end;

end;
