--
-- Copyright © 2013 <code@io7m.com> http://io7m.com
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

module CubeMap is

  import com.io7m.parasol.Vector3f    as V3;
  import com.io7m.parasol.SamplerCube as SC;
  import com.io7m.parasol.Float       as F;
  import com.io7m.parasol.Matrix4x4f  as M4;

  --
  -- Access a texel in a right-handed cube map (as opposed to
  -- OpenGL's left-handed cube maps).
  --

  function texture (
    s : sampler_cube,
    u : vector_3f
  ) : vector_4f =
    SC.texture (s,
      new vector_3f (
        u [x],
        F.subtract (0.0, u [y]),
        F.subtract (0.0, u [z])
      ));

end;
