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
-- Projective lighting functions. All calculations are assumed to take
-- place in eye-space (where the observer is always at (0, 0, 0)) unless
-- otherwise indicated.
--

module ProjectiveLight is

  import com.io7m.parasol.Vector2f   as V2;
  import com.io7m.parasol.Vector3f   as V3;
  import com.io7m.parasol.Float      as F;
  import com.io7m.parasol.Sampler2D  as S2;

  import com.io7m.r1.core.Light;
  import com.io7m.r1.core.ShadowBasic;
  import com.io7m.r1.core.ShadowVariance;
  import com.io7m.r1.core.Specular;
  import com.io7m.r1.core.SphericalLight;
  import com.io7m.r1.core.Transform;

  --
  -- Sample a texel from the given texture [t], given clip-space 
  -- coordinates [u].
  --

  function light_texel (
    t : sampler_2d,
    u : vector_4f
  ) : vector_4f =
    S2.texture (t, Transform.clip_to_texture3 (u) [x y]);

  --
  -- Given a projective light [light], calculate the diffuse
  -- color based on the given light color [light_color].
  --

  function diffuse_color (
    light        : Light.t,
    d            : Light.vectors,
    light_color  : vector_3f
  ) : vector_3f =
    let
      value factor =
        F.maximum (0.0, V3.dot (d.stl, d.normal));
      value color =
        V3.multiply_scalar (V3.multiply_scalar (light_color, light.intensity), factor);
    in
      color
    end;

  --
  -- Given a projective light [light], calculate the specular
  -- color based on the given light color [light_color].
  --

  function specular_color (
    light        : Light.t,
    d            : Light.vectors,
    light_color : vector_3f,
    s            : Specular.t
  ) : vector_3f =
    let
      value factor =
        F.power (F.maximum (0.0, V3.dot (d.reflection, d.stl)), s.exponent);
      value color =
        V3.multiply_scalar (V3.multiply_scalar (light_color, light.intensity), factor);
    in
      V3.multiply (color, s.color)
    end;

  --
  -- Given a projective light [light], calculate the diffuse
  -- color based on [d], sampling the current light texel from [t].
  --

  function diffuse_only (
    light : Light.t,
    n     : vector_3f,
    p     : vector_3f,
    t     : sampler_2d,
    u     : vector_4f
  ) : vector_3f =
    let
      value r  = Light.calculate (light, p, n);

      value tx = light_texel (t, u);
      value lc = V3.multiply (light.color, tx [x y z]);
      value c  = diffuse_color (light, r.vectors, lc);
    in
      V3.multiply_scalar (c, r.attenuation)
    end;

  --
  -- Given a projective light [light], calculate the diffuse and 
  -- specular terms for the surface.
  --

  function diffuse_specular (
    light : Light.t,
    n     : vector_3f,
    p     : vector_3f,
    s     : Specular.t,
    t     : sampler_2d,
    u     : vector_4f
  ) : vector_3f =
    let
      value r  = Light.calculate (light, p, n);

      value tx = light_texel (t, u);
      value lc = V3.multiply (light.color, tx [x y z]);
      value dc = diffuse_color (light, r.vectors, lc);
      value sc = specular_color (light, r.vectors, lc, s);
    in
      V3.multiply_scalar (V3.add (dc, sc), r.attenuation)
    end;

end;
