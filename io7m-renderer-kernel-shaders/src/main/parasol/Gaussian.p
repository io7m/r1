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

module Gaussian is

  import com.io7m.parasol.Float     as F;
  import com.io7m.parasol.Sampler2D as S2;
  import com.io7m.parasol.Vector2f  as V2;
  import com.io7m.parasol.Vector4f  as V4;

  value gauss_offset0 = 0.0;
  value gauss_offset1 = 1.3846153846;
  value gauss_offset2 = 3.2307692308;

  value gauss_weight0 = 0.2270270270;
  value gauss_weight1 = 0.3162162162;
  value gauss_weight2 = 0.0702702703;

  function blur_horizontal (
    t    : sampler_2d,
    u    : vector_2f,
    size : float
  ) : vector_4f =
    let
      value size = F.divide (1.0, size);

      value sum = V4.multiply_scalar (S2.texture (t, u), gauss_weight0);

      value x   = F.add (u [x], F.multiply (size, gauss_offset1));
      value c   = new vector_2f (x, u [y]);
      value r   = V4.multiply_scalar (S2.texture (t, c), gauss_weight1);
      value sum = V4.add (r, sum);

      value x   = F.add (u [x], F.multiply (size, gauss_offset2));
      value c   = new vector_2f (x, u [y]);
      value r   = V4.multiply_scalar (S2.texture (t, c), gauss_weight2);
      value sum = V4.add (r, sum);
      
      value x   = F.subtract (u [x], F.multiply (size, gauss_offset1));
      value c   = new vector_2f (x, u [y]);
      value r   = V4.multiply_scalar (S2.texture (t, c), gauss_weight1);
      value sum = V4.add (r, sum);

      value x   = F.subtract (u [x], F.multiply (size, gauss_offset2));
      value c   = new vector_2f (x, u [y]);
      value r   = V4.multiply_scalar (S2.texture (t, c), gauss_weight2);
      value sum = V4.add (r, sum);
    in
      sum
    end;

  function blur_vertical (
    t    : sampler_2d,
    u    : vector_2f,
    size : float
  ) : vector_4f =
    let
      value size = F.divide (1.0, size);

      value sum = V4.multiply_scalar (S2.texture (t, u), gauss_weight0);

      value y   = F.add (u [y], F.multiply (size, gauss_offset1));
      value c   = new vector_2f (u [x], y);
      value r   = V4.multiply_scalar (S2.texture (t, c), gauss_weight1);
      value sum = V4.add (r, sum);

      value y   = F.add (u [y], F.multiply (size, gauss_offset2));
      value c   = new vector_2f (u [x], y);
      value r   = V4.multiply_scalar (S2.texture (t, c), gauss_weight2);
      value sum = V4.add (r, sum);
      
      value y   = F.subtract (u [y], F.multiply (size, gauss_offset1));
      value c   = new vector_2f (u [x], y);
      value r   = V4.multiply_scalar (S2.texture (t, c), gauss_weight1);
      value sum = V4.add (r, sum);

      value y   = F.subtract (u [y], F.multiply (size, gauss_offset2));
      value c   = new vector_2f (u [x], y);
      value r   = V4.multiply_scalar (S2.texture (t, c), gauss_weight2);
      value sum = V4.add (r, sum);
    in
      sum
    end;

end;