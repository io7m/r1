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

package com.io7m.r1.debug;

module DebugLogDepth is

  import com.io7m.parasol.Matrix3x3f as M3;
  import com.io7m.parasol.Matrix4x4f as M4;
  import com.io7m.parasol.Vector3f   as V3;
  import com.io7m.parasol.Vector4f   as V4;
  import com.io7m.parasol.Sampler2D  as S;
  import com.io7m.parasol.Float      as F;
  import com.io7m.parasol.Fragment;
  
  import com.io7m.r1.core.LogDepth;

  shader vertex v is
    in v_position              : vector_3f;
    parameter m_modelview      : matrix_4x4f;
    parameter m_projection     : matrix_4x4f;
    out vertex f_position_clip : vector_4f;
    out f_positive_eye_depth   : float;
  with
    value position_clip =
      M4.multiply_vector (
        M4.multiply (m_projection, m_modelview),
        new vector_4f (v_position, 1.0)
      );
    value positive_eye_depth =
      position_clip [w];
  as
    out f_position_clip      = position_clip;
    out f_positive_eye_depth = positive_eye_depth;
  end;

  function encode (
    z                 : float,
    depth_coefficient : float  
  ) : float =
    let 
      value half_co = F.multiply (depth_coefficient, 0.5);
      value clamp_z = F.maximum (0.000001, F.add (z, 1.0));
    in
      F.multiply (F.log2 (clamp_z), half_co)
    end;

  function decode (
    z                 : float,
    depth_coefficient : float  
  ) : float =
    let value half_co = F.multiply (depth_coefficient, 0.5); in
      F.subtract (F.power (2.0, F.divide (z, half_co)), 1.0)
    end;

  shader fragment show_log_depths_f is
    out out_depth_log    : float as 0;
    out out_depth_linear : float as 1;
    out out_depth_recon  : float as 2;
    out out_depth_diff   : float as 3;

    in f_positive_eye_depth : float;

    parameter depth_coefficient : float;
  with
    value depth_linear =
      f_positive_eye_depth;
    value depth_log =
      encode (depth_linear, depth_coefficient);
    value depth_recon =
      decode (depth_log, depth_coefficient);
    value depth_diff =
      F.absolute (F.subtract (depth_recon, depth_linear));
  as
    out out_depth_log    = depth_log;
    out out_depth_linear = depth_linear;
    out out_depth_recon  = depth_recon;
    out out_depth_diff   = depth_diff;
  end;

  shader program show_log_depths is
    vertex   v;
    fragment show_log_depths_f;
  end;
  
end;