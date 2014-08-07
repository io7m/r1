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

package com.io7m.r1.postprocessing;

--
-- Horizontal and vertical blur shaders.
--

module PostprocessingBlur is

  import com.io7m.parasol.Sampler2D;
  import com.io7m.r1.core.Gaussian;
  import com.io7m.r1.core.VertexShaders;

  shader fragment blur_horizontal_4f_f is
    parameter t_image     : sampler_2d;
    parameter image_width : float;
    in        f_uv        : vector_2f;
    out       out_0       : vector_4f as 0;
  with
    value rgba =
      Gaussian.blur_horizontal_4f (t_image, f_uv, image_width);
  as
    out out_0 = rgba;
  end;

  shader fragment blur_vertical_4f_f is
    parameter t_image      : sampler_2d;
    parameter image_height : float;
    in        f_uv         : vector_2f;
    out       out_0        : vector_4f as 0;
  with
    value rgba =
      Gaussian.blur_vertical_4f (t_image, f_uv, image_height);
  as
    out out_0 = rgba;
  end;

  shader program blur_horizontal_4f is
    vertex   VertexShaders.standard_clip;
    fragment blur_horizontal_4f_f;
  end;

  shader program blur_vertical_4f is
    vertex   VertexShaders.standard_clip;
    fragment blur_vertical_4f_f;
  end;

  shader fragment blur_horizontal_2f_f is
    parameter t_image     : sampler_2d;
    parameter image_width : float;
    in        f_uv        : vector_2f;
    out       out_0       : vector_4f as 0;
  with
    value rgba =
      new vector_4f (Gaussian.blur_horizontal_2f (t_image, f_uv, image_width), 0.0, 1.0);
  as
    out out_0 = rgba;
  end;

  shader fragment blur_vertical_2f_f is
    parameter t_image      : sampler_2d;
    parameter image_height : float;
    in        f_uv         : vector_2f;
    out       out_0        : vector_4f as 0;
  with
    value rgba =
      new vector_4f (Gaussian.blur_vertical_2f (t_image, f_uv, image_height), 0.0, 1.0);
  as
    out out_0 = rgba;
  end;

  shader program blur_horizontal_2f is
    vertex   VertexShaders.standard_clip;
    fragment blur_horizontal_2f_f;
  end;

  shader program blur_vertical_2f is
    vertex   VertexShaders.standard_clip;
    fragment blur_vertical_2f_f;
  end;

end;
