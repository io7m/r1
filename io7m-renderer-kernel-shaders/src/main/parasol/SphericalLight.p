--
-- Copyright © 2013 <code@io7m.com> http://io7m.com
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
-- Spherical lighting functions. All calculations are assumed to take
-- place in eye-space (where the observer is always at (0, 0, 0)).
--

module SphericalLight is

  import com.io7m.parasol.Vector3f   as V3;
  import com.io7m.parasol.Float      as F;
  import com.io7m.renderer.Materials as M;

  type t is record
    color     : vector_3f,
    position  : vector_3f,
    intensity : float,
    radius    : float,
    falloff   : float
  end;

  --
  -- The set of relevant direction vectors needed to calculate lighting
  -- for a surface.
  --

  type directions is record
    ots        : vector_3f, -- Direction from observer to surface ("V")
    normal     : vector_3f, -- Surface normal ("N")
    stl        : vector_3f, -- Direction from surface to light source ("L")
    distance   : float,     -- Distance to light from observer
    reflection : vector_3f  -- Reflection between observer and normal ("R")
  end;

  --
  -- Given a spherical light [light], a surface normal [n],
  -- and the point [p] on the surface to be lit, calculate all
  -- relevant directions for simulating lighting.
  --
  -- Note that calculations are in eye-space and therefore the
  -- observer is assumed to be at (0.0, 0.0, 0.0).
  --

  function directions (
    light : t,
    p     : vector_3f,
    n     : vector_3f
  ) : directions =
    let
      value position_diff =
        V3.subtract (p, light.position);
      value ots =
        V3.normalize (p);
    in
      record directions {
        ots        = ots,
        normal     = n,
        stl        = V3.normalize (V3.negate (position_diff)),
        distance   = V3.magnitude (position_diff),
        reflection = V3.reflect (ots, n)
      }
    end;

  --
  -- Given a spherical light [light], at distance [distance]
  -- from the point on the surface, calculate the amount of attenuation.
  --

  function attenuation (
    light    : t,
    distance : float
  ) : float =
    let
      value nd          = F.subtract (0.0, distance);
      value inv_radius  = F.divide (1.0, light.radius);
      value linear      = F.add (F.multiply (nd, inv_radius), 1.0);
      value exponential = F.clamp (F.power (linear, light.falloff), 0.0, 1.0);
    in
      exponential
    end; 

  --
  -- Given a spherical light [light], calculate the diffuse
  -- color based on [d], with minimum emission level [e].
  --

  function diffuse_color (
    light : t,
    d     : directions,
    e     : float
  ) : vector_3f =
    let
      value factor =
        F.maximum (0.0, V3.dot (d.stl, d.normal));
      value factor_e =
        F.maximum (factor, e);
      value color =
        V3.multiply_scalar (V3.multiply_scalar (light.color, light.intensity), factor_e);
    in
      color
    end;

  --
  -- Given a spherical light [light], a surface normal [n],
  -- and assuming the current point on the surface is at [p],
  -- with minimum emission level [e], calculate the diffuse 
  -- term for the surface.
  --

  function diffuse_only (
    light : t,
    n     : vector_3f,
    p     : vector_3f
  ) : vector_3f =
    let
      value d = directions (light, p, n);
      value a = attenuation (light, d.distance);
      value c = diffuse_color (light, d, 0.0);
    in
      V3.multiply_scalar (c, a)
    end;

  --
  -- Given a spherical light [light], calculate the specular
  -- color based on [d].
  --

  function specular_color (
    light : t,
    d     : directions,
    s     : M.specular
  ) : vector_3f =
    let
      value factor =
        F.power (F.maximum (0.0, V3.dot (d.reflection, d.stl)), s.exponent);
      value color =
        V3.multiply_scalar (V3.multiply_scalar (light.color, light.intensity), factor);
    in
      V3.multiply_scalar (color, s.intensity)
    end;

  --
  -- Given a spherical light [light], a surface normal [n],
  -- surface properties [material], and assuming an observer at [p],
  -- calculate the diffuse and specular terms for the surface.
  --

  function diffuse_specular (
    light    : t,
    n        : vector_3f,
    p        : vector_3f,
    material : M.t
  ) : vector_3f =
    let
      value d  = directions (light, p, n);
      value a  = attenuation (light, d.distance);
      value dc = diffuse_color (light, d, 0.0);
      value sc = specular_color (light, d, material.specular);
    in
      V3.multiply_scalar (V3.add (dc, sc), a)
    end;

  --
  -- Given a spherical light [light], a surface normal [n],
  -- surface properties [material], and assuming an observer at [p],
  -- calculate the diffuse and specular terms for the surface,
  -- including the emissive value as the minimum resulting
  -- (diffuse) light intensity.
  --

  function diffuse_specular_emissive (
    light    : t,
    n        : vector_3f,
    p        : vector_3f,
    material : M.t
  ) : vector_3f =
    let
      value d  = directions (light, p, n);
      value a  = attenuation (light, d.distance);
      value dc = diffuse_color (light, d, material.emissive.emissive);
      value sc = specular_color (light, d, material.specular);
    in
      V3.multiply_scalar (V3.add (dc, sc), a)
    end;

  --
  -- Given a spherical light [light], a surface normal [n],
  -- and assuming the current point on the surface is at [p],
  -- with minimum emission level [e], calculate the diffuse 
  -- term for the surface, including the emissive value as the 
  -- minimum resulting (diffuse) light intensity.
  --

  function diffuse_only_emissive (
    light    : t,
    n        : vector_3f,
    p        : vector_3f,
    material : M.t
  ) : vector_3f =
    let
      value d = directions (light, p, n);
      value a = attenuation (light, d.distance);
      value c = diffuse_color (light, d, material.emissive.emissive);
    in
      V3.multiply_scalar (c, a)
    end;

end;
