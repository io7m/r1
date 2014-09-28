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

  import com.io7m.r1.core.Bilinear;
  import com.io7m.r1.core.Transform;
  import com.io7m.r1.core.Projection;
  import com.io7m.r1.core.Viewport;
  import com.io7m.r1.core.ViewRays;

  function reconstruct_eye (
    screen_depth : float,
    screen_uv    : vector_2f,
    projection   : Projection.t,
    m_projection : matrix_4x4f,
    view_rays    : ViewRays.t
  ) : vector_4f =
    let
      value origin =
        Bilinear.interpolate_3f (
          view_rays.origin_x0y0,
          view_rays.origin_x1y0,
          view_rays.origin_x0y1,
          view_rays.origin_x1y1,
          screen_uv
        );

      value ray_normal =
        Bilinear.interpolate_3f (
          view_rays.ray_x0y0,
          view_rays.ray_x1y0,
          view_rays.ray_x0y1,
          view_rays.ray_x1y1,
          screen_uv
        );

      value linear_z =
        Transform.ndc_to_eye_z (
          m_projection,
          Transform.screen_depth_to_ndc (screen_depth)
        );
        
      value ray =
        V3.multiply_scalar (
          ray_normal,
          linear_z
        );
    in
      new vector_4f (V3.add (origin, ray), 1.0)
    end;

end;
