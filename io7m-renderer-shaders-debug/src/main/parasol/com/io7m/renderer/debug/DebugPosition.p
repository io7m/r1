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

package com.io7m.renderer.debug;

module DebugPosition is

  import com.io7m.parasol.Matrix3x3f as M3;
  import com.io7m.parasol.Matrix4x4f as M4;
  import com.io7m.parasol.Vector3f   as V3;
  import com.io7m.parasol.Vector4f   as V4;
  import com.io7m.parasol.Sampler2D  as S;
  import com.io7m.parasol.Float      as F;
  import com.io7m.parasol.Fragment;

  import com.io7m.renderer.core.Frustum;
  import com.io7m.renderer.core.Transform;

  --
  -- Eye space position reconstruction program.
  --

  shader vertex show_position_reconstruction_eye_v is
    in v_position              : vector_3f;
    parameter m_modelview      : matrix_4x4f;
    parameter m_projection     : matrix_4x4f;
    out vertex f_position_clip : vector_4f;
    out        f_position_eye  : vector_4f;
  with
    value eye_position =
      M4.multiply_vector (
        m_modelview,
        new vector_4f (v_position, 1.0)
      );
  
    value clip_position =
      M4.multiply_vector (
        M4.multiply (m_projection, m_modelview),
        new vector_4f (v_position, 1.0)
      );
  as
    out f_position_clip = clip_position;
    out f_position_eye  = eye_position;
  end;

  shader fragment show_position_reconstruction_eye_f is
    parameter screen_size : vector_2f;
    parameter frustum     : Frustum.t;

    in f_position_eye : vector_4f;

    out out_0 : vector_4f as 0;
  with
   -- value ndc =
   --   Transform.screen_to_ndc (
   --     Fragment.coordinate [x y],
   --     screen_size
   --   );

   -- value eye_z =
   --   Transform.eye_z_from_depth (
   --     Fragment.coordinate [z],
   --     frustum.z_near,
   --     frustum.z_far
   --   ); 

   -- value eye =
   --   new vector_4f (
   --     Transform.ndc_to_eye (ndc, frustum, eye_z),
   --     1.0
   --   );
      
    value eye =
      Transform.screen_to_eye (
        Fragment.coordinate [x y],
        Fragment.coordinate [z],
        screen_size,
        frustum
      );

    value diff =
      V4.subtract (f_position_eye, eye);
      
    value rgba =
      eye;
  as
    out out_0 = rgba;
  end;

  shader program show_position_reconstruction_eye is
    vertex   show_position_reconstruction_eye_v;
    fragment show_position_reconstruction_eye_f;
  end;

end;