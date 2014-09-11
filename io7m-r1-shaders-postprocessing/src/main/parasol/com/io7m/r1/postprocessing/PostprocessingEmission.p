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

module PostprocessingEmission is

  import com.io7m.parasol.Sampler2D;
  import com.io7m.parasol.Vector3f;
  import com.io7m.parasol.Vector4f;
  import com.io7m.r1.core.VertexShaders;

  shader fragment emission_f is
    parameter t_map_albedo : sampler_2d;
    in        f_uv         : vector_2f;
    out       out_0        : vector_4f as 0;
  with
    value k =
      Sampler2D.texture (t_map_albedo, f_uv);
    value rgba =
      new vector_4f (
        Vector3f.multiply_scalar (k [x y z], k [w]),
        1.0
      );
  as
    out out_0 = rgba;
  end;

  shader program emission is
    vertex   VertexShaders.standard_clip;
    fragment emission_f;
  end;

  shader fragment emission_glow_f is
    parameter t_map_albedo : sampler_2d;
    parameter t_map_glow   : sampler_2d;
    parameter factor_glow  : float;
    in        f_uv         : vector_2f;
    out       out_0        : vector_4f as 0;
  with
    value k =
      Sampler2D.texture (t_map_albedo, f_uv);
    value r_glow =
      Vector4f.multiply_scalar (
        Sampler2D.texture (t_map_glow, f_uv),
        factor_glow
      );
    value r_emit =
      new vector_4f (
        Vector3f.multiply_scalar (k [x y z], k [w]),
        1.0
      );
    value rgba =
      Vector4f.add (r_emit, r_glow);
  as
    out out_0 = rgba;
  end;

  shader program emission_glow is
    vertex   VertexShaders.standard_clip;
    fragment emission_glow_f;
  end;

end;
