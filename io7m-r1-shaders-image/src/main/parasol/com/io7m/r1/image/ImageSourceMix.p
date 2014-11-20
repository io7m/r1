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

module ImageSourceMix is

  import com.io7m.parasol.Matrix3x3f;
  import com.io7m.parasol.Sampler2D;
  import com.io7m.parasol.Vector3f;
  import com.io7m.parasol.Vector4f;
  import com.io7m.r1.core.VertexShaders;

  shader fragment mix_f is
    parameter t_image_0 : sampler_2d;
    parameter t_image_1 : sampler_2d;
    parameter alpha     : float;
    in        f_uv_0    : vector_2f;
    in        f_uv_1    : vector_2f;
    out       out_0     : vector_4f as 0;
  with
    value pixel_0 =
      Sampler2D.texture (t_image_0, f_uv_0);
    value pixel_1 =
      Sampler2D.texture (t_image_1, f_uv_1);
    value rgba =
      Vector4f.interpolate (pixel_0, pixel_1, alpha);      
  as
    out out_0 = rgba;
  end;

  shader vertex mix_v is
    parameter  m_uv_0          : matrix_3x3f;
    parameter  m_uv_1          : matrix_3x3f;
    in         v_position      : vector_3f;
    in         v_uv            : vector_2f;
    out vertex f_position_clip : vector_4f;
    out        f_uv_0          : vector_2f;
    out        f_uv_1          : vector_2f;
  with
    value position_clip =
      new vector_4f (v_position, 1.0);
    value uv_0 =
      Matrix3x3f.multiply_vector (m_uv_0, new vector_3f (v_uv, 1.0)) [x y];
    value uv_1 =
      Matrix3x3f.multiply_vector (m_uv_1, new vector_3f (v_uv, 1.0)) [x y];
  as
    out f_uv_0          = uv_0;
    out f_uv_1          = uv_1;
    out f_position_clip = position_clip;
  end;

  shader program mix is
    vertex   mix_v;
    fragment mix_f;
  end;

end;
