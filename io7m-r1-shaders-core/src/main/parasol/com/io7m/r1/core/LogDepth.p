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
-- Functions for handling logarithmic depth buffers.
--

module LogDepth is

  import com.io7m.parasol.Float as F;

  --
  -- Calculate a new clip-space position suitable for use with a logarithmic
  -- depth buffer. The position's Z value is clamped to [0.000001, ∞]. The
  -- value is scaled by the given depth coefficient [depth_coefficient], which 
  -- should be given as: 
  --
  --  2.0 / log2 (far + 1.0)
  --
  -- ... where [far] is the value of the far clip plane.

  function make_position (
    position_clip     : vector_4f,
    depth_coefficient : float
  ) : vector_4f =
    let
      value alt_z =
        F.log2 (F.maximum (0.000001, F.add (position_clip [w], 1.0)));
      value alt_z_scale =
        F.subtract (F.multiply (alt_z, depth_coefficient), 1.0);
    in
      new vector_4f (
        position_clip [x],
        position_clip [y],
        alt_z_scale,
        position_clip [w]
      )
    end;

  --
  -- The interpolant to be written out by the vertex shader.
  --

  function make_vertex_interpolant (
    position_clip : vector_4f
  ) : float =
    F.add (position_clip [w], 1.0);

  --
  -- Produce the logarithmic depth value to be written out by
  -- the fragment shader.
  --

  function make_fragment_depth (
    interpolant       : float,
    depth_coefficient : float
  ) : float =
    F.multiply (
      F.log2 (interpolant),
      F.multiply (depth_coefficient, 0.5)
    );

  --
  -- Reconstruct the clip-space Z given a logarithmic depth
  -- value and the coefficient used to produce it.
  --

  function reconstruct (
    log_z             : float,
    depth_coefficient : float
  ) : float =
    let 
      value depth_coefficient_half = F.multiply (depth_coefficient, 0.5); 
      value unlog_z = F.power (2.0, F.divide (log_z, depth_coefficient_half));
    in
      F.subtract (unlog_z, 1.0)
    end;

end;
