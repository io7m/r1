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
-- Position reconstruction for deferred rendering.
--

module Reconstruction is

  import com.io7m.parasol.Float as F;
  import com.io7m.parasol.Vector2f as V2;
  import com.io7m.parasol.Vector3f as V3;
  import com.io7m.parasol.Vector4f as V4;

  import com.io7m.r1.core.Projection;
  import com.io7m.r1.core.Viewport;

  --
  -- Reconstruct the eye-space position given by the NDC position [ndc],
  -- the screen-space depth in the range [0.0, 1.0] and the [projection].
  --

  function reconstruct_eye (
    ndc          : vector_2f,
    screen_depth : float,
    projection   : Projection.t
  ) : vector_4f =
    let
      --
      -- Eye Z is equal to:
      --
      --              near * far
      -- -----------------------------------
      -- (screen_depth * (far - near)) - far
      --

      value fmn =
        F.subtract (projection.far, projection.near);
      value db =
        F.multiply (screen_depth, fmn);
      value eye_z_num =
        F.multiply (projection.near, projection.far);
      value eye_z_den =
        F.subtract (db, projection.far);
      value eye_z =
        F.divide (eye_z_num, eye_z_den);

      --
      -- Eye-space x is equal to:
      --
      -- (-ndc.x * eye_z) * (right - left)   eye.z * (right + left)
      -- --------------------------------- - ----------------------
      --              2 * near                     2 * near
      --

      value near_2 =
        F.multiply (2.0, projection.near);

      value eye_x_l_num_l =
        F.multiply (F.subtract (0.0, ndc [x]), eye_z);
      value eye_x_l_num_r =
        F.subtract (projection.right, projection.left);
      value eye_x_l_num =
        F.multiply (eye_x_l_num_l, eye_x_l_num_r);
      value eye_x_r_num =
        F.multiply (eye_z, F.add (projection.right, projection.left));
      value eye_x_l =
        F.divide (eye_x_l_num, near_2);
      value eye_x_r =
        F.divide (eye_x_r_num, near_2);
      value eye_x =
        F.subtract (eye_x_l, eye_x_r);

      --
      -- Eye-space y is equal to:
      --
      -- (-ndc.y * eye_z) * (top - bottom)   eye.z * (top + bottom)
      -- --------------------------------- - ----------------------
      --             2 * near                       2 * near
      --

      value eye_y_l_num_l =
        F.multiply (F.subtract (0.0, ndc [y]), eye_z);
      value eye_y_l_num_r =
        F.subtract (projection.top, projection.bottom);
      value eye_y_l_num =
        F.multiply (eye_y_l_num_l, eye_y_l_num_r);
      value eye_y_r_num =
        F.multiply (eye_z, F.add (projection.top, projection.bottom));
      value eye_y_l =
        F.divide (eye_y_l_num, near_2);
      value eye_y_r =
        F.divide (eye_y_r_num, near_2);
      value eye_y =
        F.subtract (eye_y_l, eye_y_r);
    in
      new vector_4f (eye_x, eye_y, eye_z, 1.0)
    end;

end;
