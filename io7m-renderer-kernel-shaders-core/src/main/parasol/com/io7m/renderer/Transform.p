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

--
-- Utility functions for transforming coordinates.
--

module Transform is

  import com.io7m.parasol.Float      as F;
  import com.io7m.parasol.Vector2f   as V2;
  import com.io7m.parasol.Vector3f   as V3;
  import com.io7m.parasol.Vector4f   as V4;

  --
  -- Transform clip-space coordinates to normalized device coordinates.
  --

  function clip_to_ndc (
    p : vector_4f
  ) : vector_3f =
    new vector_3f (
      F.divide (p [x], p [w]),
      F.divide (p [y], p [w]),
      F.divide (p [z], p [w])
    );

  --
  -- Transform normalized device coordinates coordinates to texture coordinates.
  --

  function ndc_to_texture (
    p : vector_3f
  ) : vector_3f =
    let
      value p_added =
        V3.add_scalar (p, 1.0);
      value p_scaled =
        V3.multiply_scalar (p_added, 0.5);
    in
      p_scaled
    end;

  --
  -- Transform clip-space coordinates to texture coordinates.
  --

  function clip_to_texture (
    p : vector_4f
  ) : vector_3f =
    ndc_to_texture (clip_to_ndc (p));

end;
