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
-- Projective lighting functions. All calculations are assumed to take
-- place in eye-space (where the observer is always at (0, 0, 0)) unless
-- otherwise indicated.
--

module ProjectiveLight is

  import com.io7m.parasol.Vector2f   as V2;
  import com.io7m.parasol.Vector3f   as V3;
  import com.io7m.parasol.Float      as F;
  import com.io7m.parasol.Sampler2D  as S2;

  import com.io7m.renderer.Pack           as P;
  import com.io7m.renderer.Materials      as M;
  import com.io7m.renderer.SphericalLight as SL;
  import com.io7m.renderer.Transform      as T;

  import com.io7m.renderer.Light;
  import com.io7m.renderer.ShadowBasic;
  import com.io7m.renderer.ShadowVariance;

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
  -- Given a projective light [light], calculate the diffuse
  -- colour based on the given light colour [light_colour], with
  -- minimum emission level [e].
  --

  function diffuse_colour (
    light        : Light.t,
    d            : Light.vectors,
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
    light        : Light.t,
    d            : Light.vectors,
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
    light : Light.t,
    n     : vector_3f,
    p     : vector_3f,
    t     : sampler_2d,
    u     : vector_4f
  ) : vector_3f =
    let
      value r  = Light.calculate (light, p, n);

      value tx = light_texel (t, u);
      value lc = V3.multiply (light.colour, tx [x y z]);
      value c  = diffuse_colour (light, r.vectors, lc, 0.0);
    in
      V3.multiply_scalar (c, r.attenuation)
    end;

  --
  -- Given a projective light [light], calculate the diffuse
  -- colour based on [d], with the minimum emission level given in
  -- [m], sampling the current light texel from [t].
  --

  function diffuse_only_emissive (
    light : Light.t,
    n     : vector_3f,
    p     : vector_3f,
    m     : M.t,
    t     : sampler_2d,
    u     : vector_4f
  ) : vector_3f =
    let
      value r  = Light.calculate (light, p, n);

      value tx = light_texel (t, u);
      value lc = V3.multiply (light.colour, tx [x y z]);
      value c  = diffuse_colour (light, r.vectors, lc, m.emissive.emissive);
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
    m     : M.t,
    t     : sampler_2d,
    u     : vector_4f
  ) : vector_3f =
    let
      value r  = Light.calculate (light, p, n);

      value tx = light_texel (t, u);
      value lc = V3.multiply (light.colour, tx [x y z]);
      value dc = diffuse_colour (light, r.vectors, lc, 0.0);
      value sc = specular_colour (light, r.vectors, lc, m.specular);
    in
      V3.multiply_scalar (V3.add (dc, sc), r.attenuation)
    end;

  --
  -- Given a projective light [light], calculate the diffuse and 
  -- specular terms for the surface, with the minimum emission level
  -- given in [m].
  --

  function diffuse_specular_emissive (
    light : Light.t,
    n     : vector_3f,
    p     : vector_3f,
    m     : M.t,
    t     : sampler_2d,
    u     : vector_4f
  ) : vector_3f =
    let
      value r  = Light.calculate (light, p, n);

      value tx = light_texel (t, u);
      value lc = V3.multiply (light.colour, tx [x y z]);
      value dc = diffuse_colour (light, r.vectors, lc, m.emissive.emissive);
      value sc = specular_colour (light, r.vectors, lc, m.specular);
    in
      V3.multiply_scalar (V3.add (dc, sc), r.attenuation)
    end;

  --
  -- Given a projective light [light], calculate the diffuse
  -- colour based on [d], sampling the current light texel from [t_light].
  --

  function diffuse_only_shadowed_basic (
    light        : Light.t,
    n            : vector_3f,
    p            : vector_3f,
    t_light      : sampler_2d,
    p_light_clip : vector_4f,
    t_shadow     : sampler_2d,
    shadow       : ShadowBasic.t
  ) : vector_3f =
    let
      value r  = Light.calculate (light, p, n);

      value tx  = light_texel (t_light, p_light_clip);
      value lc  = V3.multiply (light.colour, tx [x y z]);
      value dc  = diffuse_colour (light, r.vectors, lc, 0.0);

      value sf = ShadowBasic.factor (shadow, t_shadow, p_light_clip);
      value sa = F.multiply (sf, r.attenuation);
    in
      V3.multiply_scalar (dc, sa)
    end;

  function diffuse_only_shadowed_basic_packed4444 (
    light        : Light.t,
    n            : vector_3f,
    p            : vector_3f,
    t_light      : sampler_2d,
    p_light_clip : vector_4f,
    t_shadow     : sampler_2d,
    shadow       : ShadowBasic.t
  ) : vector_3f =
    let
      value r  = Light.calculate (light, p, n);

      value tx  = light_texel (t_light, p_light_clip);
      value lc  = V3.multiply (light.colour, tx [x y z]);
      value dc  = diffuse_colour (light, r.vectors, lc, 0.0);

      value sf = ShadowBasic.factor_packed4444 (shadow, t_shadow, p_light_clip);
      value sa = F.multiply (sf, r.attenuation);
    in
      V3.multiply_scalar (dc, sa)
    end;

  function diffuse_only_shadowed_variance (
    light        : Light.t,
    n            : vector_3f,
    p            : vector_3f,
    t_light      : sampler_2d,
    p_light_clip : vector_4f,
    t_shadow     : sampler_2d,
    shadow       : ShadowVariance.t
  ) : vector_3f =
    let
      value r  = Light.calculate (light, p, n);

      value tx  = light_texel (t_light, p_light_clip);
      value lc  = V3.multiply (light.colour, tx [x y z]);
      value dc  = diffuse_colour (light, r.vectors, lc, 0.0);

      value sf = ShadowVariance.factor (shadow, t_shadow, p_light_clip);
      value sa = F.multiply (sf, r.attenuation);
    in
      V3.multiply_scalar (dc, sa)
    end;

  --
  -- Given a projective light [light], calculate the diffuse
  -- colour based on [d], with the minimum emission level given in
  -- [m], sampling the current light texel from [t_light].
  --

  function diffuse_only_emissive_shadowed_basic (
    light        : Light.t,
    n            : vector_3f,
    p            : vector_3f,
    m            : M.t,
    t_light      : sampler_2d,
    p_light_clip : vector_4f,
    t_shadow     : sampler_2d,
    shadow       : ShadowBasic.t
  ) : vector_3f =
    let
      value r  = Light.calculate (light, p, n);

      value tx = light_texel (t_light, p_light_clip);
      value lc = V3.multiply (light.colour, tx [x y z]);
      value dc = diffuse_colour (light, r.vectors, lc, 0.0);
      value sc = specular_colour (light, r.vectors, lc, m.specular);

      value sf = ShadowBasic.factor (shadow, t_shadow, p_light_clip);
      value sa = F.multiply (sf, r.attenuation);
    in
      V3.multiply_scalar (V3.add (dc, sc), sa)
    end;

  function diffuse_only_emissive_shadowed_basic_packed4444 (
    light        : Light.t,
    n            : vector_3f,
    p            : vector_3f,
    m            : M.t,
    t_light      : sampler_2d,
    p_light_clip : vector_4f,
    t_shadow     : sampler_2d,
    shadow       : ShadowBasic.t
  ) : vector_3f =
    let
      value r  = Light.calculate (light, p, n);

      value tx = light_texel (t_light, p_light_clip);
      value lc = V3.multiply (light.colour, tx [x y z]);
      value dc = diffuse_colour (light, r.vectors, lc, 0.0);
      value sc = specular_colour (light, r.vectors, lc, m.specular);

      value sf = ShadowBasic.factor_packed4444 (shadow, t_shadow, p_light_clip);
      value sa = F.multiply (sf, r.attenuation);
    in
      V3.multiply_scalar (V3.add (dc, sc), sa)
    end;

  function diffuse_only_emissive_shadowed_variance (
    light        : Light.t,
    n            : vector_3f,
    p            : vector_3f,
    m            : M.t,
    t_light      : sampler_2d,
    p_light_clip : vector_4f,
    t_shadow     : sampler_2d,
    shadow       : ShadowVariance.t
  ) : vector_3f =
    let
      value r  = Light.calculate (light, p, n);

      value tx = light_texel (t_light, p_light_clip);
      value lc = V3.multiply (light.colour, tx [x y z]);
      value dc = diffuse_colour (light, r.vectors, lc, 0.0);
      value sc = specular_colour (light, r.vectors, lc, m.specular);

      value sf = ShadowVariance.factor (shadow, t_shadow, p_light_clip);
      value sa = F.multiply (sf, r.attenuation);
    in
      V3.multiply_scalar (V3.add (dc, sc), sa)
    end;

  --
  -- Given a projective light [light], calculate the diffuse and 
  -- specular terms for the surface.
  --

  function diffuse_specular_shadowed_basic (
    light        : Light.t,
    n            : vector_3f,
    p            : vector_3f,
    m            : M.t,
    t_light      : sampler_2d,
    p_light_clip : vector_4f,
    t_shadow     : sampler_2d,
    shadow       : ShadowBasic.t
  ) : vector_3f =
    let
      value r  = Light.calculate (light, p, n);

      value tx = light_texel (t_light, p_light_clip);
      value lc = V3.multiply (light.colour, tx [x y z]);
      value dc = diffuse_colour (light, r.vectors, lc, 0.0);
      value sc = specular_colour (light, r.vectors, lc, m.specular);

      value sf = ShadowBasic.factor (shadow, t_shadow, p_light_clip);
      value sa = F.multiply (sf, r.attenuation);
    in
      V3.multiply_scalar (V3.add (dc, sc), sa)
    end;

  function diffuse_specular_shadowed_basic_packed4444 (
    light        : Light.t,
    n            : vector_3f,
    p            : vector_3f,
    m            : M.t,
    t_light      : sampler_2d,
    p_light_clip : vector_4f,
    t_shadow     : sampler_2d,
    shadow       : ShadowBasic.t
  ) : vector_3f =
    let
      value r  = Light.calculate (light, p, n);

      value tx = light_texel (t_light, p_light_clip);
      value lc = V3.multiply (light.colour, tx [x y z]);
      value dc = diffuse_colour (light, r.vectors, lc, 0.0);
      value sc = specular_colour (light, r.vectors, lc, m.specular);

      value sf = ShadowBasic.factor (shadow, t_shadow, p_light_clip);
      value sa = F.multiply (sf, r.attenuation);
    in
      V3.multiply_scalar (V3.add (dc, sc), sa)
    end;

  function diffuse_specular_shadowed_variance (
    light        : Light.t,
    n            : vector_3f,
    p            : vector_3f,
    m            : M.t,
    t_light      : sampler_2d,
    p_light_clip : vector_4f,
    t_shadow     : sampler_2d,
    shadow       : ShadowVariance.t
  ) : vector_3f =
    let
      value r  = Light.calculate (light, p, n);

      value tx = light_texel (t_light, p_light_clip);
      value lc = V3.multiply (light.colour, tx [x y z]);
      value dc = diffuse_colour (light, r.vectors, lc, 0.0);
      value sc = specular_colour (light, r.vectors, lc, m.specular);

      value sf = ShadowVariance.factor (shadow, t_shadow, p_light_clip);
      value sa = F.multiply (sf, r.attenuation);
    in
      V3.multiply_scalar (V3.add (dc, sc), sa)
    end;

  --
  -- Given a projective light [light], calculate the diffuse and 
  -- specular terms for the surface, with the minimum emission level
  -- given in [m].
  --

  function diffuse_specular_emissive_shadowed_basic (
    light        : Light.t,
    n            : vector_3f,
    p            : vector_3f,
    m            : M.t,
    t_light      : sampler_2d,
    p_light_clip : vector_4f,
    t_shadow     : sampler_2d,
    shadow       : ShadowBasic.t
  ) : vector_3f =
    let
      value r  = Light.calculate (light, p, n);

      value tx = light_texel (t_light, p_light_clip);
      value lc = V3.multiply (light.colour, tx [x y z]);
      value dc = diffuse_colour (light, r.vectors, lc, m.emissive.emissive);
      value sc = specular_colour (light, r.vectors, lc, m.specular);

      value sf = ShadowBasic.factor (shadow, t_shadow, p_light_clip);
      value sa = F.multiply (sf, r.attenuation);
    in
      V3.multiply_scalar (V3.add (dc, sc), sa)
    end;

  function diffuse_specular_emissive_shadowed_basic_packed4444 (
    light        : Light.t,
    n            : vector_3f,
    p            : vector_3f,
    m            : M.t,
    t_light      : sampler_2d,
    p_light_clip : vector_4f,
    t_shadow     : sampler_2d,
    shadow       : ShadowBasic.t
  ) : vector_3f =
    let
      value r  = Light.calculate (light, p, n);

      value tx = light_texel (t_light, p_light_clip);
      value lc = V3.multiply (light.colour, tx [x y z]);
      value dc = diffuse_colour (light, r.vectors, lc, m.emissive.emissive);
      value sc = specular_colour (light, r.vectors, lc, m.specular);

      value sf = ShadowBasic.factor_packed4444 (shadow, t_shadow, p_light_clip);
      value sa = F.multiply (sf, r.attenuation);
    in
      V3.multiply_scalar (V3.add (dc, sc), sa)
    end;

  function diffuse_specular_emissive_shadowed_variance (
    light        : Light.t,
    n            : vector_3f,
    p            : vector_3f,
    m            : M.t,
    t_light      : sampler_2d,
    p_light_clip : vector_4f,
    t_shadow     : sampler_2d,
    shadow       : ShadowVariance.t
  ) : vector_3f =
    let
      value r  = Light.calculate (light, p, n);

      value tx = light_texel (t_light, p_light_clip);
      value lc = V3.multiply (light.colour, tx [x y z]);
      value dc = diffuse_colour (light, r.vectors, lc, m.emissive.emissive);
      value sc = specular_colour (light, r.vectors, lc, m.specular);

      value sf = ShadowVariance.factor (shadow, t_shadow, p_light_clip);
      value sa = F.multiply (sf, r.attenuation);
    in
      V3.multiply_scalar (V3.add (dc, sc), sa)
    end;

end;