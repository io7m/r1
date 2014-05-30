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

module Environment is

  import com.io7m.parasol.Vector3f    as V3;
  import com.io7m.parasol.Vector4f    as V4;
  import com.io7m.parasol.SamplerCube as SC;
  import com.io7m.parasol.Float       as F;
  import com.io7m.parasol.Matrix4x4f  as M4;

  import com.io7m.renderer.CubeMap;

  --
  -- Material information relating to environment mapping
  --

  type t is record
    -- The linear mix factor between the surface albedo and the environment reflection
    mix : float
  end;

  --
  -- Calculate a reflection from the cube texture [t], assuming
  -- the view direction [v], surface normal [n], and matrix [m]
  -- transforming from eye-space to world-space.
  --

  function reflection (
    t : sampler_cube,
    v : vector_3f,
    n : vector_3f,
    m : matrix_4x4f
  ) : vector_4f =
    let
      value vn = V3.normalize (v);
      value nn = V3.normalize (n);
      value r  = V3.reflect (vn, nn);
      value ri = M4.multiply_vector (m, new vector_4f (r, 0.0));
    in 
      CubeMap.texture (t, ri [x y z])
    end;

end;
