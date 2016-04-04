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

  import com.io7m.r1.core.Viewport;

  --
  -- Transform normalized device coordinates ([-1.0, 1.0]) to 
  -- texture coordinates ([0.0, 1.0]).
  --

  function ndc_to_texture3 (
    p : vector_3f
  ) : vector_3f =
    V3.multiply_scalar (V3.add_scalar (p, 1.0), 0.5);

  function ndc_to_texture2 (
    p : vector_2f
  ) : vector_2f =
    V2.multiply_scalar (V2.add_scalar (p, 1.0), 0.5);

  --
  -- Transform NDC Z ([-1.0, 1.0]) to eye-space Z.
  --

  function ndc_to_eye_z (
    m_projection : matrix_4x4f,
    ndc          : float
  ) : float =
    let
      value c2  = column m_projection 2;
      value c3  = column m_projection 3;
      value m44 = c3 [w];
      value m43 = c3 [z];
      value m34 = c2 [w];
      value m33 = c2 [z];
      
      value z0 = F.multiply (ndc, m44);
      value z1 = F.multiply (ndc, m34);
      value num = F.subtract (z0, m43);
      value den = F.subtract (z1, m33);
    in
      F.subtract (0.0, F.divide (num, den))
    end;

  --
  -- Transform texture coordinates ([0.0, 1.0]) to normalized 
  -- device coordinates ([-1.0, 1.0]).
  --

  function texture_to_ndc3 (
    p : vector_3f
  ) : vector_3f =
    V3.subtract_scalar (V3.multiply_scalar (p, 2.0), 1.0);

  function texture_to_ndc2 (
    p : vector_2f
  ) : vector_2f =
    V2.subtract_scalar (V2.multiply_scalar (p, 2.0), 1.0);

  --
  -- Transform screen-space coordinates to UV coordinates.
  --
  
  function screen_to_texture2 (
    viewport        : Viewport.t,
    screen_position : vector_2f
  ) : vector_2f =
    new vector_2f (
      F.multiply (screen_position [x], viewport.inverse_width),
      F.multiply (screen_position [y], viewport.inverse_height)
    );

  --
  -- Transform screen-space depth ([0.0, 1.0]) to NDC Z ([-1.0, 1.0]).
  --

  function screen_depth_to_ndc (d : float) : float =
    F.subtract (F.multiply (d, 2.0), 1.0);

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
  -- Transform clip-space coordinates to texture coordinates.
  --

  function clip_to_texture3 (
    p : vector_4f
  ) : vector_3f =
    ndc_to_texture3 (clip_to_ndc (p));
 
  function clip_to_texture2 (
    p : vector_4f
  ) : vector_2f =
    ndc_to_texture2 (clip_to_ndc (p) [x y]);

end;
