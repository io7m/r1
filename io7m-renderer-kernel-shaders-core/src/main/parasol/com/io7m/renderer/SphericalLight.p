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
-- Spherical lighting functions. All calculations are assumed to take
-- place in eye-space (where the observer is always at (0, 0, 0)).
--

module SphericalLight is

  import com.io7m.parasol.Vector3f as V3;
  import com.io7m.parasol.Float    as F;

  import com.io7m.renderer.Emission;
  import com.io7m.renderer.Specular;
  import com.io7m.renderer.Light;

  --
  -- Given a spherical light [light], calculate the diffuse
  -- colour based on [d], with minimum emission level [e].
  --

  function diffuse_colour (
    light : Light.t,
    d     : Light.vectors,
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
  -- Given a spherical light [light], a surface normal [n],
  -- and assuming the current point on the surface is at [p],
  -- with minimum emission level [e], calculate the diffuse 
  -- term for the surface.
  --

  function diffuse_only (
    light : Light.t,
    n     : vector_3f,
    p     : vector_3f
  ) : vector_3f =
    let
      value r = Light.calculate (light, p, n);
      value c = diffuse_colour (light, r.vectors, 0.0);
    in
      V3.multiply_scalar (c, r.attenuation)
    end;

  --
  -- Given a spherical light [light], calculate the specular
  -- colour based on [d].
  --

  function specular_colour (
    light : Light.t,
    d     : Light.vectors,
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
  -- Given a spherical light [light], a surface normal [n],
  -- surface properties [s], and assuming an observer at [p],
  -- calculate the diffuse and specular terms for the surface.
  --

  function diffuse_specular (
    light : Light.t,
    n     : vector_3f,
    p     : vector_3f,
    s     : Specular.t
  ) : vector_3f =
    let
      value r  = Light.calculate (light, p, n);
      value dc = diffuse_colour (light, r.vectors, 0.0);
      value sc = specular_colour (light, r.vectors, s);
    in
      V3.multiply_scalar (V3.add (dc, sc), r.attenuation)
    end;

  --
  -- Given a spherical light [light], a surface normal [n],
  -- surface properties [material], and assuming an observer at [p],
  -- calculate the diffuse and specular terms for the surface,
  -- including the emissive value as the minimum resulting
  -- (diffuse) light intensity.
  --

  function diffuse_specular_emissive (
    light : Light.t,
    n     : vector_3f,
    p     : vector_3f,
    m     : Emission.t,
    s     : Specular.t
  ) : vector_3f =
    let
      value r = Light.calculate (light, p, n);
      value dc = diffuse_colour (light, r.vectors, m.amount);
      value sc = specular_colour (light, r.vectors, s);
    in
      V3.multiply_scalar (V3.add (dc, sc), r.attenuation)
    end;

  --
  -- Given a spherical light [light], a surface normal [n],
  -- and assuming the current point on the surface is at [p],
  -- with minimum emission level [e], calculate the diffuse 
  -- term for the surface, including the emissive value as the 
  -- minimum resulting (diffuse) light intensity.
  --

  function diffuse_only_emissive (
    light : Light.t,
    n     : vector_3f,
    p     : vector_3f,
    m     : Emission.t
  ) : vector_3f =
    let
      value r = Light.calculate (light, p, n);
      value c = diffuse_colour (light, r.vectors, m.amount);
    in
      V3.multiply_scalar (c, r.attenuation)
    end;

end;
