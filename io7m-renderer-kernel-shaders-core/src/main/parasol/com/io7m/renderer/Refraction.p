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

module Refraction is

  import com.io7m.parasol.Float;
  import com.io7m.parasol.Fragment;
  import com.io7m.parasol.Sampler2D;
  import com.io7m.parasol.Vector2f;
  import com.io7m.parasol.Vector3f;
  import com.io7m.parasol.Vector4f;

  import com.io7m.renderer.Transform;

  --
  -- Refraction parameters.
  --

  type t is record
    scale : float
  end;

  function refraction (
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

end;
