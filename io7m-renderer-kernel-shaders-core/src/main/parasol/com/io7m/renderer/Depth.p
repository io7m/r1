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

module Depth is

  import com.io7m.parasol.Matrix4x4f as M4;
  import com.io7m.parasol.Float      as F;

  --
  -- Pack a depth value in the range [0, 1] to four four-bit cells.
  --

  function pack4444 (depth : float) : vector_4f =
    let 
      value d  = F.multiply (depth, 65536.0);

      value k0 = F.floor (F.divide (d, 4096.0));
      value k1 = F.floor (F.divide (F.modulo (d, 4096.0), 256.0));
      value k2 = F.floor (F.divide (F.modulo (d, 256.0), 16.0));
      value k3 = F.floor (F.modulo (d, 16.0));

      value q0 = F.divide (k0, 15.0);
      value q1 = F.divide (k1, 15.0);
      value q2 = F.divide (k2, 15.0);
      value q3 = F.divide (k3, 15.0);
    in
      new vector_4f (q0, q1, q2, q3)
    end;

  --
  -- Unpack a depth value from four four-bit cells.
  --

  function unpack4444 (depth : vector_4f) : float =
    let 
      value k0 = F.multiply (depth [x], 61440.0);
      value k1 = F.multiply (depth [y], 3840.0);
      value k2 = F.multiply (depth [z], 240.0);
      value k3 = F.multiply (depth [w], 15.0);
      value s  = F.add (F.add (F.add (k0, k1), k2), k3);
    in 
      F.divide (s, 65536.0)
    end;

  --
  -- Depth-only program.
  --

  shader vertex depth_v is
    in v_position              : vector_3f;
    out vertex f_position_clip : vector_4f;
    parameter m_modelview      : matrix_4x4f;
    parameter m_projection     : matrix_4x4f;
  with
    value clip_position =
      M4.multiply_vector (
        M4.multiply (m_projection, m_modelview),
        new vector_4f (v_position, 1.0)
      );
  as
    out f_position_clip = clip_position;
  end;

  shader fragment depth_f is
    out out_0 : vector_4f as 0;
  with
    value rgba = new vector_4f (1.0, 1.0, 1.0, 1.0);
  as
    out out_0 = rgba;
  end;

  shader program depth is
    vertex   depth_v;
    fragment depth_f;
  end;

end;
