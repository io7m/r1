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

package com.io7m.r1.depth_only;

module Distance is

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

  --
  -- Rendering of the eye-space distance values of constant-depth 
  -- objects into the first channel of whatever is the first color
  -- attachment.
  --

  shader fragment distance_DepC_f is
    in f_position_eye           : vector_4f;
    in f_positive_eye_z         : float;
    parameter distance_range    : float;
    parameter depth_coefficient : float;
    out depth out_depth         : float;
    out out_0                   : vector_4f as 0;
  with
    value log_z =
      LogDepth.encode_partial (f_positive_eye_z, depth_coefficient);
    value d =
      F.divide (V3.magnitude (f_position_eye [x y z]), distance_range);
    value rgba =
      new vector_4f (d, 0.0, 0.0, 0.0);
  as
    out out_0     = rgba;
    out out_depth = log_z;
  end;

  shader program distance_DepC is
    vertex   VertexShaders.standard;
    fragment distance_DepC_f;
  end;

  --
  -- Rendering of the eye-space distance values of mapped-depth 
  -- objects into the first channel of whatever is the first color
  -- attachment.
  --

  shader fragment distance_DepA_f is
    in f_position_eye           : vector_4f;
    in f_positive_eye_z         : float;
    in f_uv                     : vector_2f;
    out depth out_depth         : float;
    out out_0                   : vector_4f as 0;
    parameter distance_range    : float;
    parameter depth_coefficient : float;
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

    value d =
      F.divide (V3.magnitude (f_position_eye [x y z]), distance_range);
    value rgba =
      new vector_4f (d, 0.0, 0.0, 0.0);
  as
    out out_0     = rgba;
    out out_depth = log_z;
  end;

  shader program distance_DepA is
    vertex   VertexShaders.standard;
    fragment distance_DepA_f;
  end;

end;
