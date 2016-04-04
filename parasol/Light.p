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
-- Generic lighting functions.
--

module Light is

  import com.io7m.parasol.Float    as F;
  import com.io7m.parasol.Vector3f as V3;

  --
  -- A basic colored light with a position, maximum range r (given as 1 / r), 
  -- intensity, and falloff exponent f (given as 1 / f).
  --

  type t is record
    color           : vector_3f,
    position        : vector_3f,
    intensity       : float,
    inverse_range   : float,
    inverse_falloff : float
  end;

  --
  -- Given a light with inverse range [light_range_inverse] and inverse
  -- falloff exponent [light_falloff_inverse], at [distance] from the 
  -- current point on the lit surface, calculate the amount of attenuation.
  --

  function attenuation_from_inverses (
    light_range_inverse   : float,
    light_falloff_inverse : float,
    distance              : float
  ) : float =
    let
      value linear      = F.multiply (distance, light_range_inverse);
      value exponential = F.power (linear, light_falloff_inverse);
      value clamped     = F.clamp (exponential, 0.0, 1.0);
    in
      F.subtract (1.0, clamped)
    end;

  --
  -- The set of relevant direction vectors needed to calculate lighting
  -- for a surface.
  --

  type vectors is record
    ots        : vector_3f, -- Direction from observer to surface ("V")
    normal     : vector_3f, -- Surface normal ("N")
    lts        : vector_3f, -- Direction from light source ("L") to surface
    stl        : vector_3f, -- Direction from surface to light source ("L")
    distance   : float,     -- Distance between light and surface
    reflection : vector_3f  -- Reflection between observer and normal ("R")
  end;

  --
  -- Calculate all relevant vectors for simulating lighting.
  --
  -- Note that calculations are in eye-space and therefore the
  -- observer is assumed to be at (0.0, 0.0, 0.0).
  --

  function vectors (
    light_position   : vector_3f,
    surface_position : vector_3f,
    surface_normal   : vector_3f
  ) : vectors =
    let
      value position_diff =
        V3.subtract (surface_position, light_position);
      value lts =
        V3.normalize (position_diff);
      value ots =
        V3.normalize (surface_position);
    in
      record vectors {
        ots        = ots,
        normal     = surface_normal,
        lts        = lts,
        stl        = V3.negate (lts),
        distance   = V3.magnitude (position_diff),
        reflection = V3.reflect (ots, surface_normal)
      }
    end;

  --
  -- Calculate all light properties, given a surface position [p],
  -- surface normal [n], and light [k].
  --

  type r is record
    vectors     : vectors,
    attenuation : float
  end;

  function calculate (
    k : t,
    p : vector_3f,
    n : vector_3f
  ) : r =
    let value v = vectors (k.position, p, n); in
      record r {
        vectors     = v,
        attenuation = attenuation_from_inverses (
          k.inverse_range,
          k.inverse_falloff,
          v.distance
        )
      }
    end;

end;
