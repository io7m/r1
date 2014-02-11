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

module DepthVariance is

  import com.io7m.parasol.Matrix3x3f as M3;
  import com.io7m.parasol.Matrix4x4f as M4;
  import com.io7m.parasol.Vector3f   as V3;
  import com.io7m.parasol.Vector4f   as V4;
  import com.io7m.parasol.Sampler2D  as S;
  import com.io7m.parasol.Float      as F;
  import com.io7m.parasol.Fragment;

  import com.io7m.renderer.Albedo;
  import com.io7m.renderer.Depth;

  function variance (d : float) : vector_2f =
    new vector_2f (d, F.multiply (d, d));

  shader fragment depth_variance_C_f is
    in f_position : vector_4f;
    out out_0     : vector_4f as 0;
  with
    value rgba =
      new vector_4f (variance (Fragment.coordinate [z]), 0.0, 1.0);
  as
    out out_0 = rgba;
  end;

  shader program depth_variance_C is
    vertex   Depth.depth_simple_v;
    fragment depth_variance_C_f;
  end;

  shader fragment depth_variance_U_f is
    in f_position           : vector_4f;
    parameter p_albedo      : Albedo.t;
    parameter p_opacity     : float;
    parameter p_alpha_depth : float;
    out out_0               : vector_4f as 0;
  with
    value albedo : vector_4f = Albedo.translucent (p_albedo);
    value alpha = F.multiply (albedo [w], p_opacity);
    discard (F.lesser (alpha, p_alpha_depth));

    value rgba =
      new vector_4f (variance (Fragment.coordinate [z]), 0.0, 1.0);
  as
    out out_0 = rgba;
  end;

  shader program depth_variance_U is
    vertex   Depth.depth_simple_v;
    fragment depth_variance_U_f;
  end;

  shader fragment depth_variance_M_f is
    in f_uv                 : vector_2f;
    in f_position           : vector_4f;
    out out_0               : vector_4f as 0;
    parameter p_albedo      : Albedo.t;
    parameter p_opacity     : float;
    parameter p_alpha_depth : float;
    parameter t_albedo      : sampler_2d;
  with
    value albedo : vector_4f =
      Albedo.textured_translucent (
        t_albedo,
        f_uv,
        p_albedo
      );

    value alpha = F.multiply (albedo [w], p_opacity);
    discard (F.lesser (alpha, p_alpha_depth));
    
    value rgba =
      new vector_4f (variance (Fragment.coordinate [z]), 0.0, 1.0);
  as
    out out_0 = rgba;
  end;

  shader program depth_variance_M is
    vertex   Depth.depth_textured_v;
    fragment depth_variance_M_f;
  end;

end;
