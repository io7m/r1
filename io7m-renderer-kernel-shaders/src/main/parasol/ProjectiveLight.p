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
-- Projective lighting functions. All calculations are assumed to take
-- place in eye-space (where the observer is always at (0, 0, 0)).
--

module ProjectiveLight is

  import com.io7m.parasol.Vector2f   as V2;
  import com.io7m.parasol.Vector3f   as V3;
  import com.io7m.parasol.Float      as F;
  import com.io7m.parasol.Sampler2D  as S2;

  import com.io7m.renderer.Materials      as M;
  import com.io7m.renderer.SphericalLight as SL;

  type t is record
    color     : vector_3f,
    position  : vector_3f,
    intensity : float,
    range     : float,
    falloff   : float
  end;

  --
  -- Sample a texel from the given texture [t], given clip-space 
  -- coordinates [u].
  --

  function light_texel (
    t : sampler_2d,
    u : vector_4f
  ) : vector_4f =
    let
      -- Perform division-by-w to get coordinates into normalized-device space.
      value u_divided =
        new vector_2f (
          F.divide (u [x], u [w]),
          F.divide (u [y], u [w])
        );
      -- Scale and translate coordinates to get them into the range [0, 1] from [-1, 1].
      value u_added =
        V2.add_scalar (u_divided, 1.0);
      value u_scaled =
        V2.multiply_scalar (u_added, 0.5);
    in
      S2.texture (t, u_scaled)
    end;

  --
  -- Given a projective light [light], calculate the diffuse
  -- color based on the given light color [light_color], with
  -- minimum emission level [e].
  --

  function diffuse_color (
    light       : t,
    d           : SL.directions,
    light_color : vector_3f,
    e           : float
  ) : vector_3f =
    let
      value factor =
        F.maximum (0.0, V3.dot (d.stl, d.normal));
      value factor_e =
        F.maximum (factor, e);
      value color =
        V3.multiply_scalar (V3.multiply_scalar (light_color, light.intensity), factor_e);
    in
      color
    end;

  --
  -- Given a projective light [light], calculate the specular
  -- color based on the given light color [light_color].
  --

  function specular_color (
    light       : t,
    d           : SL.directions,
    light_color : vector_3f,
    s           : M.specular
  ) : vector_3f =
    let
      value factor =
        F.power (F.maximum (0.0, V3.dot (d.reflection, d.stl)), s.exponent);
      value color =
        V3.multiply_scalar (V3.multiply_scalar (light_color, light.intensity), factor);
    in
      V3.multiply_scalar (color, s.intensity)
    end;

  --
  -- Given a projective light [light], calculate the diffuse
  -- color based on [d], sampling the current light texel from [t].
  --

  function diffuse_only (
    light : t,
    n     : vector_3f,
    p     : vector_3f,
    t     : sampler_2d,
    u     : vector_4f
  ) : vector_3f =
    let
      value d  = SL.directions (light.position, p, n);
      value a  = SL.attenuation (light.range, light.falloff, d.distance);
      value tx = light_texel (t, u);
      value lc = V3.multiply (light.color, tx [x y z]);
      value c  = diffuse_color (light, d, lc, 0.0);
    in
      V3.multiply_scalar (c, a)
    end;

  --
  -- Given a projective light [light], calculate the diffuse
  -- color based on [d], with the minimum emission level given in
  -- [m], sampling the current light texel from [t].
  --

  function diffuse_only_emissive (
    light : t,
    n     : vector_3f,
    p     : vector_3f,
    m     : M.t,
    t     : sampler_2d,
    u     : vector_4f
  ) : vector_3f =
    let
      value d  = SL.directions (light.position, p, n);
      value a  = SL.attenuation (light.range, light.falloff, d.distance);
      value tx = light_texel (t, u);
      value lc = V3.multiply (light.color, tx [x y z]);
      value c  = diffuse_color (light, d, lc, m.emissive.emissive);
    in
      V3.multiply_scalar (c, a)
    end;

  --
  -- Given a projective light [light], calculate the diffuse and 
  -- specular terms for the surface.
  --

  function diffuse_specular (
    light : t,
    n     : vector_3f,
    p     : vector_3f,
    m     : M.t,
    t     : sampler_2d,
    u     : vector_4f
  ) : vector_3f =
    let
      value d  = SL.directions (light.position, p, n);
      value a  = SL.attenuation (light.range, light.falloff, d.distance);
      value tx = light_texel (t, u);
      value lc = V3.multiply (light.color, tx [x y z]);
      value dc = diffuse_color (light, d, lc, 0.0);
      value sc = specular_color (light, d, lc, m.specular);
    in
      V3.multiply_scalar (V3.add (dc, sc), a)
    end;

  --
  -- Given a projective light [light], calculate the diffuse and 
  -- specular terms for the surface, with the minimum emission level
  -- given in [m].
  --

  function diffuse_specular_emissive (
    light : t,
    n     : vector_3f,
    p     : vector_3f,
    m     : M.t,
    t     : sampler_2d,
    u     : vector_4f
  ) : vector_3f =
    let
      value d  = SL.directions (light.position, p, n);
      value a  = SL.attenuation (light.range, light.falloff, d.distance);
      value tx = light_texel (t, u);
      value lc = V3.multiply (light.color, tx [x y z]);
      value dc = diffuse_color (light, d, lc, m.emissive.emissive);
      value sc = specular_color (light, d, lc, m.specular);
    in
      V3.multiply_scalar (V3.add (dc, sc), a)
    end;

end;