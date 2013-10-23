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

  import com.io7m.renderer.Materials as M;

  type t is record
    color     : vector_3f,
    position  : vector_3f,
    intensity : float,
    distance  : float,
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
    distance   : float,     -- Distance between light and surface
    reflection : vector_3f  -- Reflection between observer and normal ("R")
  end;

  --
  -- Given a projective light [light], a surface normal [n],
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
  -- Given a projective light [light], at distance [distance]
  -- from the point on the surface, calculate the amount of attenuation.
  --

  function attenuation (
    light    : t,
    distance : float
  ) : float =
    let
      value nd           = F.subtract (0.0, distance);
      value inv_distance = F.divide (1.0, light.distance);
      value linear       = F.add (F.multiply (nd, inv_distance), 1.0);
      value exponential  = F.clamp (F.power (linear, light.falloff), 0.0, 1.0);
    in
      exponential
    end;

  --
  -- Given a projective light [light], calculate the diffuse
  -- color based on [d], with minimum emission level [e],
  -- sampling from texture [t] using coordinates [u].
  --

  function diffuse_color (
    light : t,
    d     : directions,
    t     : sampler_2d,
    u     : vector_4f,
    e     : float
  ) : vector_3f =
    let
      value factor =
        F.maximum (0.0, V3.dot (d.stl, d.normal));
      value factor_e =
        F.maximum (factor, e);

      value u_divided =
        new vector_2f (
          F.divide (u [x], u [w]),
          F.divide (u [y], u [w])
        );
      value u_added =
        V2.add_scalar (u_divided, 1.0);
      value u_scaled =
        V2.multiply_scalar (u_added, 0.5);

      value texel =
        S2.texture (t, u_scaled) [x y z];

      value light_color =
        V3.multiply (light.color, texel);
      value color =
        V3.multiply_scalar (V3.multiply_scalar (light_color, light.intensity), F.maximum(1.0, factor_e));
    in
      color
    end;

  --
  -- Given a projective light [light], a surface normal [n],
  -- and assuming the current point on the surface is at [p],
  -- with minimum emission level [e], calculate the diffuse 
  -- term for the surface.
  --

  function diffuse_only (
    light : t,
    n     : vector_3f,
    p     : vector_3f,
    t     : sampler_2d,
    u     : vector_4f
  ) : vector_3f =
    let
      value d = directions (light, p, n);
      value a = attenuation (light, d.distance);
      value c = diffuse_color (light, d, t, u, 0.0);
    in
      V3.multiply_scalar (c, a)
    end;

  function diffuse_only_emissive (
    light : t,
    n     : vector_3f,
    p     : vector_3f,
    m     : M.t,
    t     : sampler_2d,
    u     : vector_4f
  ) : vector_3f =
    new vector_3f (1.0, 0.0, 1.0);

  function diffuse_specular (
    light : t,
    n     : vector_3f,
    p     : vector_3f,
    m     : M.t,
    t     : sampler_2d,
    u     : vector_4f
  ) : vector_3f =
    new vector_3f (1.0, 0.0, 1.0);

  function diffuse_specular_emissive (
    light : t,
    n     : vector_3f,
    p     : vector_3f,
    m     : M.t,
    t     : sampler_2d,
    u     : vector_4f
  ) : vector_3f =
    new vector_3f (1.0, 0.0, 1.0);

end;