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

module Refraction is

  import com.io7m.parasol.Float;
  import com.io7m.parasol.Fragment;
  import com.io7m.parasol.Sampler2D;
  import com.io7m.parasol.Vector2f;
  import com.io7m.parasol.Vector3f;
  import com.io7m.parasol.Vector4f;
  import com.io7m.parasol.Matrix4x4f;

  import com.io7m.r1.core.Transform;

  --
  -- Refraction parameters.
  --

  type t is record
    scale : float
  end;

  --
  -- Functions to unpack delta values from textures.
  --

  function delta_unpack (v : vector_4f) : vector_2f =
    Vector2f.subtract_scalar (Vector2f.multiply_scalar (v [x y], 2.0), 1.0);
    
  function delta_get (
    t : sampler_2d,
    u : vector_2f,
    s : float
  ) : vector_2f =
    Vector2f.multiply_scalar (delta_unpack (Sampler2D.texture (t, u)), s);

  --
  -- Refraction functions that sample from the scene based on an offset
  -- that is calculated from the object's surface normal.
  --

  function refraction_masked (
    refraction   : t,
    t_scene      : sampler_2d,
    t_scene_mask : sampler_2d,
    n            : vector_3f,
    p            : vector_4f
  ) : vector_4f =
    let
      value n           = Vector3f.multiply_scalar (n, refraction.scale);
      value displaced   = new vector_4f (Vector3f.add (p [x y z], n), p [w]);
      value uv_there    = Transform.clip_to_texture (displaced) [x y];
      value uv_here     = Transform.clip_to_texture (p) [x y];
      value scene_there = Sampler2D.texture (t_scene, uv_there);
      value scene_here  = Sampler2D.texture (t_scene, uv_here);
      value mask_there  = Sampler2D.texture (t_scene_mask, uv_there) [x];
    in
      Vector4f.interpolate (scene_here, scene_there, mask_there)
    end;

  function refraction_unmasked (
    refraction  : t,
    t_scene     : sampler_2d,
    n           : vector_3f,
    p           : vector_4f
  ) : vector_4f =
    let
      value n           = Vector3f.multiply_scalar (n, refraction.scale);
      value displaced   = new vector_4f (Vector3f.add (p [x y z], n), p [w]);
      value uv_there    = Transform.clip_to_texture (displaced) [x y];
      value scene_there = Sampler2D.texture (t_scene, uv_there);
    in
      scene_there
    end;

  --
  -- Refraction functions that sample from the scene based on an offset
  -- that is read directly from a texture.
  --

  function refraction_masked_delta_textured (
    refraction   : t,
    t_scene      : sampler_2d,
    t_scene_mask : sampler_2d,
    t_delta      : sampler_2d,
    uv           : vector_2f,
    p            : vector_4f
  ) : vector_4f =
    let
      value delta       = delta_get (t_delta, uv, refraction.scale);
      value uv_here     = Transform.clip_to_texture (p) [x y];
      value uv_there    = Vector2f.add (uv_here, delta);
      value scene_here  = Sampler2D.texture (t_scene, uv_here);
      value scene_there = Sampler2D.texture (t_scene, uv_there);
      value mask_there  = Sampler2D.texture (t_scene_mask, uv_there) [x];
    in
      Vector4f.interpolate (scene_here, scene_there, mask_there)
    end;

  function refraction_unmasked_delta_textured (
    refraction  : t,
    t_scene     : sampler_2d,
    t_delta     : sampler_2d,
    uv          : vector_2f,
    p           : vector_4f
  ) : vector_4f =
    let
      value delta       = delta_get (t_delta, uv, refraction.scale);
      value uv_here     = Transform.clip_to_texture (p) [x y];
      value uv_there    = Vector2f.add (uv_here, delta);
      value scene_there = Sampler2D.texture (t_scene, uv_there);
    in
      scene_there
    end;

  --
  -- "Constant color" shader for refraction masks.
  --

  shader vertex mask_v is
    in v_position              : vector_3f;
    parameter m_modelview      : matrix_4x4f;
    parameter m_projection     : matrix_4x4f;
    out vertex f_position_clip : vector_4f;
  with
    value clip_position =
      Matrix4x4f.multiply_vector (
        Matrix4x4f.multiply (m_projection, m_modelview),
        new vector_4f (v_position, 1.0)
      );
  as
    out f_position_clip = clip_position;
  end;

  shader fragment mask_f is
    parameter f_ccolor : vector_4f;
    out       out_0    : vector_4f as 0;
  as
    out out_0 = f_ccolor;
  end;

  shader program mask is
    vertex   mask_v;
    fragment mask_f;
  end;

end;
