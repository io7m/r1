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

package com.io7m.renderer.postprocessing;

module Postprocessing is

  import com.io7m.parasol.Matrix3x3f;
  import com.io7m.parasol.Sampler2D;

  --
  -- A vertex shader that assumes the input vertices are already in clip space.
  --

  shader vertex screen_quad is
    parameter  m_uv            : matrix_3x3f;
    in         v_position      : vector_3f;
    in         v_uv            : vector_2f;
    out        f_uv            : vector_2f;
    out vertex f_position_clip : vector_4f;
  with
    value clip =
      new vector_4f (v_position, 1.0);
    value uv =
      Matrix3x3f.multiply_vector (m_uv, new vector_3f (v_uv, 1.0)) [x y];
  as
    out f_position_clip = clip;
    out f_uv            = uv;
  end;

  --
  -- A postprocessing shader that simply copies the input image to the output.
  --

  shader program identity is
    vertex   screen_quad;
    fragment identity_f;
  end;

  shader fragment identity_f is
    parameter t_image      : sampler_2d;
    in        f_uv         : vector_2f;
    out       out_0        : vector_4f as 0;
  with
    value rgba =
      Sampler2D.texture (t_image, f_uv);
  as
    out out_0 = rgba;
  end;

end;
