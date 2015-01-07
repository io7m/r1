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
-- The functions encode a negated (and therefore positive) eye-space Z + 1.0
-- value, giving it a logarithmic distribution in the depth buffer. Typically,
-- a vertex shader will use the [encode_full] function to replace the Z component
-- of the resulting clip-space position, and will also pass [add (negate (eye[z]), 1.0)]
-- to the fragment shader, which then uses [encode_partial] to assign an actual
-- value into the depth buffer.
--

module LogDepth is

  import com.io7m.parasol.Float as F;

  function encode_partial (
    eye_z_positive    : float,
    depth_coefficient : float  
  ) : float =
    let 
      value half_co = F.multiply (depth_coefficient, 0.5);
      value clamp_z = F.maximum (0.000001, eye_z_positive);
    in
      F.multiply (F.log2 (clamp_z), half_co)
    end;

  function encode_full (
    eye_z_positive    : float,
    depth_coefficient : float  
  ) : float =
    let 
      value half_co = F.multiply (depth_coefficient, 0.5);
      value clamp_z = F.maximum (0.000001, F.add (eye_z_positive, 1.0));
    in
      F.multiply (F.log2 (clamp_z), half_co)
    end;

  function decode (
    log_z             : float,
    depth_coefficient : float  
  ) : float =
    let value half_co = F.multiply (depth_coefficient, 0.5); in
      F.subtract (F.power (2.0, F.divide (log_z, half_co)), 1.0)
    end;

end;
