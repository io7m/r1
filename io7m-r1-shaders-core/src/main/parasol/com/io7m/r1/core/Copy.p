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
-- Data copying shaders.
--

module Copy is

  import com.io7m.parasol.Sampler2D;
  import com.io7m.r1.core.VertexShaders;

  shader program copy_rgba is
    vertex   VertexShaders.standard_clip;
    fragment copy_rgba_f;
  end;

  shader fragment copy_rgba_f is
    parameter t_image      : sampler_2d;
    in        f_uv         : vector_2f;
    out       out_0        : vector_4f as 0;
  with
    value rgba =
      Sampler2D.texture (t_image, f_uv);
  as
    out out_0 = rgba;
  end;

  shader program copy_rgba_depth is
    vertex   VertexShaders.standard_clip;
    fragment copy_rgba_depth_f;
  end;

  shader fragment copy_rgba_depth_f is
    parameter t_image       : sampler_2d;
    parameter t_image_depth : sampler_2d;
    in        f_uv          : vector_2f;
    out       out_0         : vector_4f as 0;
    out depth out_d         : float;
  with
    value rgba =
      Sampler2D.texture (t_image, f_uv);
    value depth_sample =
      Sampler2D.texture (t_image_depth, f_uv) [x];
  as
    out out_0 = rgba;
    out out_d = depth_sample;
  end;

  --
  -- Copy depth to the [z] channel of an RGBA image.
  --

  shader program copy_depth_to_rgba is
    vertex   VertexShaders.standard_clip;
    fragment copy_depth_to_rgba_f;
  end;

  shader fragment copy_depth_to_rgba_f is
    parameter t_image_depth : sampler_2d;
    in        f_uv          : vector_2f;
    out       out_0         : vector_4f as 0;
  with
    value z =
      Sampler2D.texture (t_image_depth, f_uv) [z];
    value rgba =
      new vector_4f (z, z, z, 1.0);
  as
    out out_0 = rgba;
  end;

  --
  -- Copy the RG components of a depth-variance image to the output.
  --

  shader program copy_depth_variance_to_rgba is
    vertex   VertexShaders.standard_clip;
    fragment copy_depth_variance_to_rgba_f;
  end;

  shader fragment copy_depth_variance_to_rgba_f is
    parameter t_image : sampler_2d;
    in        f_uv    : vector_2f;
    out       out_0   : vector_4f as 0;
  with
    value rgba =
      new vector_4f (Sampler2D.texture (t_image, f_uv) [x y], 0.0, 1.0);
  as
    out out_0 = rgba;
  end;

end;
