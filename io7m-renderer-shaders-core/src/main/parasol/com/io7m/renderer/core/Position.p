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
-- Functions for reconstructing eye-space position values from
-- saved depth values.
--

module Position is

  import com.io7m.parasol.Vector2f as V2;
  import com.io7m.parasol.Float as F;

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
  -- [screen_width] and [screen_height].
  --

  function screen_to_ndc (
    screen_space  : vector_2f,
    screen_width  : float,
    screen_height : float
  ) : vector_2f =
    let
      value p =
        new vector_2f (
          F.divide (screen_space [x], screen_width),
          F.divide (screen_space [y], screen_height)
        );
    in
      V2.subtract_scalar (V2.multiply_scalar (p, 2.0), 1.0)
    end;

  --
  -- Calculate the eye-space coordinates of the given
  -- normalized device coordinates,  using the eye-space 
  -- depth [z_eye], and assuming a frustum with a near plane 
  -- given by [x_left, x_right], [y_top, y_bottom], [z_near], 
  -- and the eye-space depth value [z_eye].
  --

  function ndc_to_eye (
    ndc      : vector_2f,
    x_left   : float,
    x_right  : float,
    y_top    : float,
    y_bottom : float,
    z_near   : float,
    z_eye    : float
  ) : vector_3f =
    let
      value z_eye_m = F.subtract (0.0, z_eye);

      value rml   = F.subtract (x_right, x_left);
      value rpl   = F.add      (x_right, x_left);
      value tmb   = F.subtract (y_top, y_bottom);
      value tpb   = F.add      (y_top, y_bottom);
      value two_n = F.multiply (z_near, 2.0);
      
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

end;
