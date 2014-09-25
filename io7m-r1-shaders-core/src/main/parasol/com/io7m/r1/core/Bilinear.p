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

module Bilinear is

  import com.io7m.parasol.Vector2f as V2;
  import com.io7m.parasol.Vector3f as V3;
  import com.io7m.parasol.Vector4f as V4;

  function interpolate_3f (
    x0y0 : vector_3f,
    x1y0 : vector_3f,
    x0y1 : vector_3f,
    x1y1 : vector_3f,
    p    : vector_2f
  ) : vector_3f =
    let
      value u0 = V3.interpolate (x0y0, x1y0, p [x]);
      value u1 = V3.interpolate (x0y1, x1y1, p [x]);
    in
      V3.interpolate (u0, u1, p [y])
    end;

end;
