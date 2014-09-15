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
-- Functions for paraboloid mappings.
--

module Paraboloid is

  import com.io7m.parasol.Vector3f as V3;
  import com.io7m.parasol.Vector4f as V4;
  import com.io7m.parasol.Float    as F;

  --
  -- Calculate paraboloid coordinates given the eye-space (from 
  -- the perspective of the light source, if the coordinates are 
  -- to be used in shadow mapping or other techniques) 
  -- coordinates [eye], with near and far clip distances [z_near]
  -- and [z_far].
  --

  function calculate (
    eye    : vector_4f,
    z_near : float,
    z_far  : float
  ) : vector_4f =
    let
      value clip = V4.divide_scalar (eye, eye [w]);
      value len  = V3.magnitude (clip [x y z]);
      value dir  = V4.divide_scalar (clip, len);
      value lmn  = F.subtract (len, z_near);
      value fmn  = F.subtract (z_far, z_near);
      value dist = F.divide (lmn, fmn);
      
      value nz   = F.add (dir [z], 1.0);
    in
      new vector_4f (
        F.divide (dir [x], nz),
        F.divide (dir [y], nz),
        dist,
        1.0
      )
    end;

end;
