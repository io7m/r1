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

package com.io7m.renderer.core;

--
-- Functions for calculating raw albedo terms.
--

module Albedo is

  import com.io7m.parasol.Float      as F;
  import com.io7m.parasol.Sampler2D  as S;
  import com.io7m.parasol.Vector3f   as V3;
  import com.io7m.parasol.Vector4f   as V4;

  type t is record
    color : vector_4f, -- The base surface color
    mix   : float      -- The linear mix between the color and texture, in the range [0, 1]
  end;

  function opaque (
    d : t
  ) : vector_4f =
    new vector_4f (d.color [x y z], 1.0);

  function translucent (
    d : t
  ) : vector_4f =
    d.color;

  function textured_opaque (
    t : sampler_2d,
    u : vector_2f,
    d : t
  ) : vector_4f =
    let
      value tc = S.texture (t, u);
      value m  = F.multiply (tc [w], d.mix);
      value c  = V3.interpolate (d.color [x y z], tc [x y z], m);
    in
      new vector_4f (c, 1.0)
    end;

  function textured_translucent (
    t : sampler_2d,
    u : vector_2f,
    d : t
  ) : vector_4f =
    let
      value tc = S.texture (t, u);
      value m  = F.multiply (tc [w], d.mix);
    in
      V4.interpolate (d.color, tc, m)
    end;

end;
