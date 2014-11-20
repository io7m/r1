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

package com.io7m.r1.image;

module ImageFilterFog is

  import com.io7m.parasol.Sampler2D;
  import com.io7m.parasol.Vector3f;
  import com.io7m.r1.core.VertexShaders;

  type t is record
    color : vector_3f
  end;

  shader fragment fog_f is
    parameter t_image       : sampler_2d;
    parameter t_image_depth : sampler_2d;
    parameter fog           : t;
    in        f_uv          : vector_2f;
    out       out_0         : vector_4f as 0;
  with
    value pixel =
      Sampler2D.texture (t_image, f_uv);
    value depth_sample =
      Sampler2D.texture (t_image_depth, f_uv) [x];
    value rgba =
      new vector_4f (
        Vector3f.interpolate (pixel [x y z], fog.color, depth_sample),
        1.0
      );
  as
    out out_0 = rgba;
  end;

  shader program fog is
    vertex   VertexShaders.standard_clip;
    fragment fog_f;
  end;

end;
