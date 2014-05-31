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

package com.io7m.renderer.core;

--
-- Spherical lighting functions. All calculations are assumed to take
-- place in eye-space (where the observer is always at (0, 0, 0)).
--

module SphericalLight is

  import com.io7m.parasol.Vector3f as V3;
  import com.io7m.parasol.Float    as F;

  import com.io7m.renderer.core.Specular;
  import com.io7m.renderer.core.Light;

  --
  -- Given a spherical light [light], calculate the diffuse
  -- color based on [d], with minimum emission level [e].
  --

  function diffuse_color (
    light : Light.t,
    d     : Light.vectors,
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
  -- Given a spherical light [light], calculate the specular
  -- color based on [d].
  --

  function specular_color (
    light : Light.t,
    d     : Light.vectors,
    s     : Specular.t
  ) : vector_3f =
    let
      value factor =
        F.power (F.maximum (0.0, V3.dot (d.reflection, d.stl)), s.exponent);
      value color =
        V3.multiply_scalar (V3.multiply_scalar (light.color, light.intensity), factor);
    in
      V3.multiply (color, s.color)
    end;

end;
