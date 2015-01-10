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

package com.io7m.r1.depth_variance;

module DepthVariance is

  import com.io7m.parasol.Matrix3x3f as M3;
  import com.io7m.parasol.Matrix4x4f as M4;
  import com.io7m.parasol.Vector3f   as V3;
  import com.io7m.parasol.Vector4f   as V4;
  import com.io7m.parasol.Sampler2D  as S;
  import com.io7m.parasol.Float      as F;
  import com.io7m.parasol.Fragment;

  import com.io7m.r1.core.Albedo;
  import com.io7m.r1.core.LogDepth;
  import com.io7m.r1.core.VertexShaders;
  import com.io7m.r1.depth_only.Depth;

  function variance_rgba (z : float) : vector_4f =
    new vector_4f (variance (z), 1.0, 1.0);

  function variance (d : float) : vector_2f =
    new vector_2f (d, F.multiply (d, d));

  shader fragment depth_variance_C_f is
    in f_positive_eye_z         : float;
    parameter depth_coefficient : float;
    out depth out_depth         : float;
    out out_0                   : vector_4f as 0;
  with
    value log_z =
      LogDepth.encode_partial (f_positive_eye_z, depth_coefficient);
    value rgba =
      variance_rgba (log_z);
  as
    out out_0     = rgba;
    out out_depth = log_z;
  end;

  shader program depth_variance_C is
    vertex   VertexShaders.standard;
    fragment depth_variance_C_f;
  end;

  shader fragment depth_variance_U_f is
    in f_positive_eye_z         : float;
    parameter depth_coefficient : float;
    parameter p_albedo          : Albedo.t;
    parameter p_alpha_depth     : float;
    out depth out_depth         : float;
    out out_0                   : vector_4f as 0;
  with
    value log_z =
      LogDepth.encode_partial (f_positive_eye_z, depth_coefficient);

    value albedo : vector_4f = p_albedo.color;
    discard (F.lesser (albedo [w], p_alpha_depth));

    value rgba =
      variance_rgba (log_z);
  as
    out out_0     = rgba;
    out out_depth = log_z;
  end;

  shader program depth_variance_U is
    vertex   VertexShaders.standard;
    fragment depth_variance_U_f;
  end;

  shader fragment depth_variance_A_f is
    in f_uv                     : vector_2f;
    in f_positive_eye_z         : float;
    parameter depth_coefficient : float;
    out depth out_depth         : float;
    out out_0                   : vector_4f as 0;
    parameter p_albedo          : Albedo.t;
    parameter p_alpha_depth     : float;
    parameter t_albedo          : sampler_2d;
  with
    value log_z =
      LogDepth.encode_partial (f_positive_eye_z, depth_coefficient);

    value albedo : vector_4f =
      Albedo.textured (
        t_albedo,
        f_uv,
        p_albedo
      );

    discard (F.lesser (albedo [w], p_alpha_depth));

    value rgba =
      variance_rgba (log_z);
  as
    out out_0     = rgba;
    out out_depth = log_z;
  end;

  shader program depth_variance_A is
    vertex   VertexShaders.standard;
    fragment depth_variance_A_f;
  end;

end;
