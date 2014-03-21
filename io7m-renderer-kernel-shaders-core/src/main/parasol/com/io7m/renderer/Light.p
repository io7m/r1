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

package com.io7m.renderer;

--
-- Generic lighting functions.
--

module Light is

  import com.io7m.parasol.Float    as F;
  import com.io7m.parasol.Vector3f as V3;

  --
  -- A basic coloured light with a position, maximum range, intensity,
  -- and falloff exponent.
  --

  type t is record
    colour    : vector_3f,
    position  : vector_3f,
    intensity : float,
    range     : float,
    falloff   : float
  end;

  --
  -- Given a light with range [light_range] and falloff exponent 
  -- [light_falloff], at [distance] from the current point on the 
  -- lit surface, calculate the amount of attenuation.
  --

  function attenuation (
    light_range   : float,
    light_falloff : float,
    distance      : float
  ) : float =
    let
      value inv_range   = F.divide (1.0, light_range);
      value linear      = F.multiply (distance, inv_range);
      value falloff_inv = F.divide (1.0, light_falloff);
      value exponential = F.power (linear, falloff_inv);
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
      value ots =
        V3.normalize (surface_position);
    in
      record vectors {
        ots        = ots,
        normal     = surface_normal,
        stl        = V3.normalize (V3.negate (position_diff)),
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
        attenuation = attenuation (k.range, k.falloff, v.distance)
      }
    end;

end;