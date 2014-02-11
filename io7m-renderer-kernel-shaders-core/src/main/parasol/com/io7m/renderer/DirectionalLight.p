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
-- Directional lighting functions. All calculations are assumed to take
-- place in eye-space (where the observer is always at (0, 0, 0)).
--

module DirectionalLight is

  import com.io7m.parasol.Vector3f as V3;
  import com.io7m.parasol.Float    as F;

  import com.io7m.renderer.Emission;
  import com.io7m.renderer.Specular;

  type t is record
    colour    : vector_3f,
    direction : vector_3f,
    intensity : float
  end;

  --
  -- The set of relevant direction vectors needed to calculate lighting
  -- for a surface.
  --

  type vectors is record
    ots        : vector_3f, -- Direction from observer to surface ("V")
    normal     : vector_3f, -- Surface normal ("N")
    stl        : vector_3f, -- Direction from surface to light source ("L")
    reflection : vector_3f  -- Reflection between observer and normal ("R")
  end;

  --
  -- Given a directional light [light], a surface normal [n],
  -- and an observer at [p], calculate all relevant vectors
  -- for simulating lighting.
  --
  -- Note that calculations are in eye-space and therefore the
  -- observer is assumed to be at (0.0, 0.0, 0.0).
  --

  function vectors (
    light : t,
    p     : vector_3f,
    n     : vector_3f
  ) : vectors =
    let
      value ots = V3.normalize (p);
    in
      record vectors {
        ots        = ots,
        normal     = n,
        stl        = V3.normalize (V3.negate (light.direction)),
        reflection = V3.reflect (ots, n)
      }
    end;

  --
  -- Given a directional light [light], calculate the diffuse
  -- colour based on [d], with minimum emission [e].
  --

  function diffuse_colour (
    light : t,
    d     : vectors,
    e     : float
  ) : vector_3f =
    let
      value factor =
        F.maximum (0.0, V3.dot (d.stl, d.normal));
      value factor_e =
        F.maximum (factor, e);
      value colour =
        V3.multiply_scalar (V3.multiply_scalar (light.colour, light.intensity), factor_e);
    in
      colour
    end;

  --
  -- Given a directional light [light] and a surface normal [n],
  -- calculate the diffuse term for the surface.
  --

  function diffuse_only (
    light : t,
    n     : vector_3f
  ) : vector_3f =
    let
      value d = vectors (light, new vector_3f(0.0, 0.0, 0.0), n);
      value c = diffuse_colour (light, d, 0.0);
    in
      c
    end;

  --
  -- Given a directional light [light], calculate the specular
  -- colour based on [d] and surface properties [material].
  --

  function specular_colour (
    light : t,
    d     : vectors,
    s     : Specular.t
  ) : vector_3f =
    let
      value factor =
        F.power (F.maximum (0.0, V3.dot (d.reflection, d.stl)), s.exponent);
      value colour =
        V3.multiply_scalar (V3.multiply_scalar (light.colour, light.intensity), factor);
    in
      V3.multiply_scalar (colour, s.intensity)
    end;

  --
  -- Given a directional light [light], a surface normal [n],
  -- surface properties given by [s],
  -- and the current point [p] on the surface to be lit,
  -- calculate the diffuse and specular terms for the surface.
  --

  function diffuse_specular (
    light : t,
    n     : vector_3f,
    p     : vector_3f,
    s     : Specular.t
  ) : vector_3f =
    let
      value d  = vectors (light, p, n);
      value dc = diffuse_colour (light, d, 0.0);
      value sc = specular_colour (light, d, s);
    in
      V3.add (dc, sc)
    end;

  --
  -- Given a directional light [light], a surface normal [n],
  -- surface properties given by [m] and [s],
  -- and the current point [p] on the surface to be lit,
  -- calculate the diffuse and specular terms for the surface,
  -- including the emissive value as the minimum resulting
  -- light intensity.
  --

  function diffuse_specular_emissive (
    light : t,
    n     : vector_3f,
    p     : vector_3f,
    m     : Emission.t,
    s     : Specular.t
  ) : vector_3f =
    let
      value d  = vectors (light, p, n);
      value dc = diffuse_colour (light, d, m.amount);
      value sc = specular_colour (light, d, s);
    in
      V3.add (dc, sc)
    end;

  --
  -- Given a directional light [light] and a surface normal [n],
  -- calculate the diffuse term for the surface, including the
  -- emissive value as the minimum resulting light intensity.
  --

  function diffuse_only_emissive (
    light : t,
    n     : vector_3f,
    m     : Emission.t
  ) : vector_3f =
    let
      value d = vectors (light, new vector_3f(0.0, 0.0, 0.0), n);
      value c = diffuse_colour (light, d, m.amount);
    in
      c
    end;

end;
