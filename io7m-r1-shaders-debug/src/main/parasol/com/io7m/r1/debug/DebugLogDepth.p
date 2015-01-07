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
  import com.io7m.r1.core.VertexShaders;

  shader fragment show_log_depths_f is
    out out_depth_log    : float as 0;
    out out_depth_linear : float as 1;
    out out_depth_recon  : float as 2;
    out out_depth_diff   : float as 3;

    in f_positive_eye_z : float;

    parameter depth_coefficient : float;
  with
    value depth_linear =
      f_positive_eye_z;
    value depth_log =
      LogDepth.encode_partial (depth_linear, depth_coefficient);
    value depth_recon =
      LogDepth.decode (depth_log, depth_coefficient);
    value depth_diff =
      F.absolute (F.subtract (depth_recon, depth_linear));
  as
    out out_depth_log    = depth_log;
    out out_depth_linear = depth_linear;
    out out_depth_recon  = depth_recon;
    out out_depth_diff   = depth_diff;
  end;

  shader program show_log_depths is
    vertex   VertexShaders.standard;
    fragment show_log_depths_f;
  end;
  
end;