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

--
-- Utility functions for transforming and/or reconstructing coordinates.
--

module Transform is

  import com.io7m.parasol.Float as F;
  import com.io7m.parasol.Vector2f as V2;
  import com.io7m.parasol.Vector3f as V3;
  import com.io7m.parasol.Vector4f as V4;

  import com.io7m.renderer.core.Frustum;

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
  -- Compute an eye-space Z coordinate from the given depth [d]
  -- value, assuming the depth value was produced with [near]
  -- and [far] planes. 
  --

  function eye_z_from_depth (
    d    : float,
    near : float,
    far  : float
  ) : float =
    let
      value zndc  = F.subtract (F.multiply (d, 2.0), 1.0);
      value fmn   = F.subtract (far, near);
      value fpn   = F.add (far, near);
      value f_num = F.multiply (F.multiply (2.0, far), near);
      value f_den = F.subtract (F.multiply (zndc, fmn), fpn);
    in
      F.divide (f_num, f_den)
    end;

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
  -- Calculate the eye-space coordinates of the given
  -- normalized device coordinates,  using the eye-space 
  -- depth [z_eye], and the given [frustum].
  --

  function ndc_to_eye (
    ndc      : vector_2f,
    frustum  : Frustum.t,
    z_eye    : float
  ) : vector_3f =
    let
      value z_eye_m = F.subtract (0.0, z_eye);

      value rml   = F.subtract (frustum.x_right, frustum.x_left);
      value rpl   = F.add      (frustum.x_right, frustum.x_left);
      value tmb   = F.subtract (frustum.y_top, frustum.y_bottom);
      value tpb   = F.add      (frustum.y_top, frustum.y_bottom);
      value two_n = F.multiply (frustum.z_near, 2.0);
      
      value vn = new vector_2f (
        F.add (F.multiply (ndc [x], rml), rpl),
        F.add (F.multiply (ndc [y], tmb), tpb)
      );
      
      value eye =
        V2.divide_scalar (
          V2.multiply_scalar (vn, z_eye_m), 
          two_n
        );
    in
      new vector_3f (eye, z_eye)
    end;

  --
  -- Given a screen-space position [screen_position] value, and a
  -- depth value [current_z], reconstruct the eye-space position
  -- based on the given [screen_size] and [frustum].
  --

  function screen_to_eye (
    screen_position : vector_2f,
    current_z       : float,
    screen_size     : vector_2f,
    frustum         : Frustum.t
  ) : vector_4f =
    let
      value ndc =
        screen_to_ndc (
          screen_position,
          screen_size
        );

      value eye_z =
        eye_z_from_depth (
          current_z,
          frustum.z_near,
          frustum.z_far
        );

      value eye =
        new vector_4f (
          ndc_to_eye (ndc, frustum, eye_z),
          1.0
        );
    in
      eye
    end;

  --
  -- Given a screen-space position [screen_position] value, and an (eye-space)
  -- depth value [current_z], reconstruct the eye-space position
  -- based on the given [screen_size] and [frustum].
  --

  function screen_to_eye_with_eye_depth (
    screen_position : vector_2f,
    eye_z           : float,
    screen_size     : vector_2f,
    frustum         : Frustum.t
  ) : vector_4f =
    let
      value ndc =
        screen_to_ndc (
          screen_position,
          screen_size
        );

      value eye =
        new vector_4f (
          ndc_to_eye (ndc, frustum, eye_z),
          1.0
        );
    in
      eye
    end;

end;
