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
-- place in eye-space (where the observer is always at (0, 0, 0)) unless
-- otherwise indicated.
--

module ProjectiveLight is

  import com.io7m.parasol.Vector2f   as V2;
  import com.io7m.parasol.Vector3f   as V3;
  import com.io7m.parasol.Float      as F;
  import com.io7m.parasol.Sampler2D  as S2;

  import com.io7m.renderer.Materials      as M;
  import com.io7m.renderer.SphericalLight as SL;
  import com.io7m.renderer.Transform      as T;

  type t is record
    colour    : vector_3f,
    position  : vector_3f,
    intensity : float,
    range     : float,
    falloff   : float
  end;

  --
  -- Sample a texel from the given texture [t], given clip-space 
  -- coordinates [u]. If [u [w]] is not greater than 0.0, then
  -- the coordinate is behind the viewer (the light) and so no lighting
  -- should be applied.
  --

  function light_texel (
    t : sampler_2d,
    u : vector_4f
  ) : vector_4f =
    if F.lesser (u [w], 0.0) then
      new vector_4f (0.0, 0.0, 0.0, 1.0)
    else
      S2.texture (t, T.clip_to_texture (u) [x y])
    end;

  --
  -- Given a shadow map [t_shadow], and clip-coordinates [p], return
  -- the amount of light that could be reaching the current point (where
  -- 0.0 is fully shadowed and 1.0 is fully lit).
  --

  value shadow_epsilon = 0.0005;

  function shadow_factor (
    t_shadow : sampler_2d,
    p        : vector_4f
  ) : float =
    if F.lesser (p [w], 0.0) then
      0.0
    else
      let
        value current_tex =
          T.clip_to_texture (p);
        value map_depth =
          S2.texture (t_shadow, current_tex [x y]) [x];
        value map_depth_adjusted =
          F.add (map_depth, shadow_epsilon);
      in
        if F.lesser (current_tex [z], map_depth_adjusted) then
          1.0
        else
          0.2
        end
      end
    end;

  --
  -- Given a projective light [light], calculate the diffuse
  -- colour based on the given light colour [light_colour], with
  -- minimum emission level [e].
  --

  function diffuse_colour (
    light        : t,
    d            : SL.directions,
    light_colour : vector_3f,
    e            : float
  ) : vector_3f =
    let
      value factor =
        F.maximum (0.0, V3.dot (d.stl, d.normal));
      value factor_e =
        F.maximum (factor, e);
      value colour =
        V3.multiply_scalar (V3.multiply_scalar (light_colour, light.intensity), factor_e);
    in
      colour
    end;

  --
  -- Given a projective light [light], calculate the specular
  -- colour based on the given light colour [light_colour].
  --

  function specular_colour (
    light        : t,
    d            : SL.directions,
    light_colour : vector_3f,
    s            : M.specular
  ) : vector_3f =
    let
      value factor =
        F.power (F.maximum (0.0, V3.dot (d.reflection, d.stl)), s.exponent);
      value colour =
        V3.multiply_scalar (V3.multiply_scalar (light_colour, light.intensity), factor);
    in
      V3.multiply_scalar (colour, s.intensity)
    end;

  --
  -- Given a projective light [light], calculate the diffuse
  -- colour based on [d], sampling the current light texel from [t].
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
      value lc = V3.multiply (light.colour, tx [x y z]);
      value c  = diffuse_colour (light, d, lc, 0.0);
    in
      V3.multiply_scalar (c, a)
    end;

  --
  -- Given a projective light [light], calculate the diffuse
  -- colour based on [d], with the minimum emission level given in
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
      value lc = V3.multiply (light.colour, tx [x y z]);
      value c  = diffuse_colour (light, d, lc, m.emissive.emissive);
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
      value lc = V3.multiply (light.colour, tx [x y z]);
      value dc = diffuse_colour (light, d, lc, 0.0);
      value sc = specular_colour (light, d, lc, m.specular);
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
      value lc = V3.multiply (light.colour, tx [x y z]);
      value dc = diffuse_colour (light, d, lc, m.emissive.emissive);
      value sc = specular_colour (light, d, lc, m.specular);
    in
      V3.multiply_scalar (V3.add (dc, sc), a)
    end;

  --
  -- Given a projective light [light], calculate the diffuse
  -- colour based on [d], sampling the current light texel from [t_light].
  --

  function diffuse_only_shadowed (
    light        : t,
    n            : vector_3f,
    p            : vector_3f,
    t_light      : sampler_2d,
    p_light_clip : vector_4f,
    t_shadow     : sampler_2d
  ) : vector_3f =
    let
      value d   = SL.directions (light.position, p, n);
      value sf  = shadow_factor (t_shadow, p_light_clip);
      value a   = SL.attenuation (light.range, light.falloff, d.distance);
      value sa  = F.multiply (sf, a);
      value tx  = light_texel (t_light, p_light_clip);
      value lc  = V3.multiply (light.colour, tx [x y z]);
      value c   = diffuse_colour (light, d, lc, 0.0);
    in
      V3.multiply_scalar (c, sa)
    end;

  --
  -- Given a projective light [light], calculate the diffuse
  -- colour based on [d], with the minimum emission level given in
  -- [m], sampling the current light texel from [t_light].
  --

  function diffuse_only_emissive_shadowed (
    light        : t,
    n            : vector_3f,
    p            : vector_3f,
    m            : M.t,
    t_light      : sampler_2d,
    p_light_clip : vector_4f,
    t_shadow     : sampler_2d
  ) : vector_3f =
    new vector_3f (1.0, 0.0, 1.0);

  --
  -- Given a projective light [light], calculate the diffuse and 
  -- specular terms for the surface.
  --

  function diffuse_specular_shadowed (
    light    : t,
    n        : vector_3f,
    p        : vector_3f,
    m        : M.t,
    t_light  : sampler_2d,
    u        : vector_4f,
    t_shadow : sampler_2d
  ) : vector_3f =
    new vector_3f (1.0, 0.0, 1.0);

  --
  -- Given a projective light [light], calculate the diffuse and 
  -- specular terms for the surface, with the minimum emission level
  -- given in [m].
  --

  function diffuse_specular_emissive_shadowed (
    light    : t,
    n        : vector_3f,
    p        : vector_3f,
    m        : M.t,
    t_light  : sampler_2d,
    u        : vector_4f,
    t_shadow : sampler_2d
  ) : vector_3f =
    new vector_3f (1.0, 0.0, 1.0);

end;
