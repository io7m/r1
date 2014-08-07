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
-- Utility functions for transforming and/or reconstructing coordinates.
--

module Transform is

  import com.io7m.parasol.Float as F;
  import com.io7m.parasol.Vector2f as V2;
  import com.io7m.parasol.Vector3f as V3;
  import com.io7m.parasol.Vector4f as V4;

  --
  -- Transform clip-space coordinates to normalized device coordinates.
  --

  function clip_to_ndc (
    p : vector_4f
  ) : vector_3f =
    new vector_3f (
      F.divide (p [x], p [w]),
      F.divide (p [y], p [w]),
      F.divide (p [z], p [w])
    );

  --
  -- Transform normalized device coordinates coordinates to texture coordinates.
  --

  function ndc_to_texture (
    p : vector_3f
  ) : vector_3f =
    let
      value p_added =
        V3.add_scalar (p, 1.0);
      value p_scaled =
        V3.multiply_scalar (p_added, 0.5);
    in
      p_scaled
    end;

  --
  -- Transform screen-space coordinates to UV coordinates. So, for a (640, 480)
  -- screen:
  --
  -- screen_to_uv (640, 480) (0,   0)   = (0.0, 0.0)
  -- screen_to_uv (640, 480) (320, 240) = (0.5, 0.5)
  -- screen_to_uv (640, 480) (640, 480) = (1.0, 1.0)
  --
  
  function screen_to_texture (
    screen_size     : vector_2f,
    screen_position : vector_2f
  ) : vector_2f =
    V2.divide (screen_position, screen_size);

  --
  -- Transform clip-space coordinates to texture coordinates.
  --

  function clip_to_texture (
    p : vector_4f
  ) : vector_3f =
    ndc_to_texture (clip_to_ndc (p));

  --
  -- Calculate the normalized device coordinates of the given 
  -- screen-space coordinates, assuming a screen of the given 
  -- [screen_size].
  --

  function screen_to_ndc (
    screen_position : vector_2f,
    screen_size     : vector_2f
  ) : vector_2f =
    let
      value p =
        V2.divide (screen_position, screen_size);
    in
      V2.subtract_scalar (V2.multiply_scalar (p, 2.0), 1.0)
    end;

  --
  -- Given a view-space position [v_position_eye] (typically that of a light volume),
  -- the distance to the far clipping plane [far_clip_distance], and the normalized
  -- view-space depth of a pixel [normalized_depth], construct the original eye-space 
  -- position.
  --

  function reconstruct_eye (
    v_position_eye    : vector_4f,
    far_clip_distance : float,
    normalized_depth  : float
  ) : vector_4f =
    let
      value fcd_over_z =
        F.divide (far_clip_distance, v_position_eye [z]);

      value view_ray =
        new vector_3f (
          V2.multiply_scalar (v_position_eye [x y], fcd_over_z),
          far_clip_distance
        );
    in
      new vector_4f (
        V3.multiply_scalar (view_ray, normalized_depth),
        1.0
      )
    end;

end;
