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

package com.io7m.r1.core;

module Pack is

  import com.io7m.parasol.Matrix4x4f as M4;
  import com.io7m.parasol.Float      as F;

  --
  -- Pack a value in the range [0, 1] to four four-bit cells.
  --

  function pack4444 (x : float) : vector_4f =
    let 
      value d  = F.multiply (x, 65536.0);

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
  -- Unpack a value in the range [0, 1] from four four-bit cells.
  --

  function unpack4444 (p : vector_4f) : float =
    let 
      value k0 = F.multiply (p [x], 61440.0);
      value k1 = F.multiply (p [y], 3840.0);
      value k2 = F.multiply (p [z], 240.0);
      value k3 = F.multiply (p [w], 15.0);
      value s  = F.add (F.add (F.add (k0, k1), k2), k3);
    in 
      F.divide (s, 65536.0)
    end;

end;
