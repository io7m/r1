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
-- FXAA
--

module PostprocessingFXAA is

  import com.io7m.parasol.Boolean   as B;
  import com.io7m.parasol.Float     as F;
  import com.io7m.parasol.Sampler2D as S2;
  import com.io7m.parasol.Vector2f  as V2;
  import com.io7m.parasol.Vector3f  as V3;

  import com.io7m.r1.core.VertexShaders;
  import com.io7m.r1.core.Viewport;

  --
  -- Debugging shader to show luminance.
  --

  function luminance (c : vector_3f) : float =
    V3.dot (c, new vector_3f (0.299, 0.587, 0.114));

  shader fragment luminance_f is
    parameter t_image       : sampler_2d;
    in        f_uv          : vector_2f;
    out       out_0         : vector_4f as 0;
  with
    value pixel =
      S2.texture (t_image, f_uv);
    value lum =
      luminance (pixel [x y z]);
    value rgba =
      new vector_4f (lum, lum, lum, 1.0);
  as
    out out_0 = rgba;
  end;

  shader program luminance is
    vertex   VertexShaders.standard_clip;
    fragment luminance_f;
  end;

  --
  -- FXAA shader
  --

  type t is record
    reduce_minimum : float,
    reduce_factor  : float,
    span_maximum   : float
  end;

  shader fragment antialias_f is
    parameter t_image       : sampler_2d;
    parameter fxaa          : t;
    parameter viewport      : Viewport.t;
    in        f_uv          : vector_2f;
    out       out_0         : vector_4f as 0;
  with
    value texel_size =
      new vector_2f (viewport.inverse_width, viewport.inverse_height);
  
    value nw = V2.add (f_uv, V2.multiply (new vector_2f (-1.0, -1.0), texel_size));
    value ne = V2.add (f_uv, V2.multiply (new vector_2f ( 1.0, -1.0), texel_size));
    value sw = V2.add (f_uv, V2.multiply (new vector_2f (-1.0,  1.0), texel_size));
    value se = V2.add (f_uv, V2.multiply (new vector_2f ( 1.0,  1.0), texel_size));
  
    value rgb_nw = S2.texture (t_image, nw) [x y z];
    value rgb_ne = S2.texture (t_image, ne) [x y z];
    value rgb_sw = S2.texture (t_image, sw) [x y z];
    value rgb_se = S2.texture (t_image, se) [x y z];
    value rgb_m  = S2.texture (t_image, f_uv) [x y z];

    value luma_nw = luminance (rgb_nw);
    value luma_ne = luminance (rgb_ne);
    value luma_sw = luminance (rgb_sw);
    value luma_se = luminance (rgb_se);
    value luma_m  = luminance (rgb_m);

    value luma_min =
      F.minimum (
        luma_m,
        F.minimum (
          F.minimum (luma_nw, luma_ne), 
          F.minimum (luma_sw, luma_se)
        )
      );
    
    value luma_max =
      F.maximum (
        luma_m,
        F.maximum (
          F.maximum (luma_nw, luma_ne),
          F.maximum (luma_sw, luma_se)
        )
      );

    value luma_sum =
      F.add (F.add (F.add (luma_nw, luma_ne), luma_sw), luma_se);

    value dir_x = F.negate (F.subtract (F.add (luma_nw, luma_ne), F.add (luma_sw, luma_se)));
    value dir_y = F.subtract (F.add (luma_nw, luma_sw), F.add (luma_ne, luma_se));
    value dir   = new vector_2f (dir_x, dir_y);

    value dir_factor  = F.multiply (0.25, fxaa.reduce_factor);
    value dir_reduce  = F.maximum (F.multiply (luma_sum, dir_factor), fxaa.reduce_minimum);
    value dir_min_abs = F.minimum (F.absolute (dir [x]), F.absolute (dir [y]));
    value dir_min     = F.add (dir_min_abs, dir_reduce);
    value dir_min_rcp = F.divide (1.0, dir_min);
    value dir_min_sc  = V2.multiply_scalar (dir, dir_min_rcp);

    value span_max =
      new vector_2f (fxaa.span_maximum, fxaa.span_maximum);
    value span_min =
      V2.negate (span_max);

    value dir_clamp =
      V2.minimum (span_max, V2.maximum (span_min, dir_min_sc));
    value dir_texels =
      V2.multiply (dir_clamp, texel_size);

    value factor_0 = F.subtract (F.divide (1.0, 3.0), 0.5);
    value factor_1 = F.subtract (F.divide (2.0, 3.0), 0.5);
    value dir_a0   = V2.multiply_scalar (dir_texels, factor_0);
    value dir_a1   = V2.multiply_scalar (dir_texels, factor_1);
    value rgb_a0   = S2.texture (t_image, V2.add (f_uv, dir_a0)) [x y z];
    value rgb_a1   = S2.texture (t_image, V2.add (f_uv, dir_a1)) [x y z];
    value rgb_a    = V3.multiply_scalar (V3.add (rgb_a0, rgb_a1), 0.5);

    value dir_b0 = V2.multiply_scalar (dir_texels, -0.5);
    value dir_b1 = V2.multiply_scalar (dir_texels, 0.5);
    value rgb_b0 = S2.texture (t_image, V2.add (f_uv, dir_b0)) [x y z];
    value rgb_b1 = S2.texture (t_image, V2.add (f_uv, dir_b1)) [x y z];
    value rgb_br = V3.add (rgb_b0, rgb_b1);

    value rgb_b = 
      V3.add (
        V3.multiply_scalar (rgb_a, 0.5),
        V3.multiply_scalar (rgb_br, 0.25)
      );

    value luma_b = luminance (rgb_b);
    value less = F.lesser (luma_b, luma_min);
    value more = F.greater (luma_b, luma_max);

    value rgba =
      if B.or (less, more) then
        new vector_4f (rgb_a, 1.0)
      else
        new vector_4f (rgb_b, 1.0)
      end;
  as
    out out_0 = rgba;
  end;

  shader program antialias is
    vertex   VertexShaders.standard_clip;
    fragment antialias_f;
  end;

end;
