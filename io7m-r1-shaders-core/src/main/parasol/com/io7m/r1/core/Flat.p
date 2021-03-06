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

module Flat is

  import com.io7m.r1.core.VertexShaders;
  import com.io7m.parasol.Sampler2D;

  shader fragment flat_f is
    parameter f_ccolor : vector_4f;
  
    out out_0 : vector_4f as 0;
  with
    value rgba = f_ccolor;
  as
    out out_0 = rgba;
  end;

  shader program flat_standard is
    vertex   VertexShaders.standard;
    fragment flat_f;
  end;

  shader program flat_clip is
    vertex   VertexShaders.standard_clip;
    fragment flat_f;
  end;

end;
