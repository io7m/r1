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

module Normals is

  import com.io7m.parasol.Matrix3x3f as M3;
  import com.io7m.parasol.Vector3f   as V3;
  import com.io7m.parasol.Sampler2D  as S;

  --
  -- Convert a normal vector to "display" format.
  --

  function to_rgba (n : vector_3f) : vector_4f =
    let value xyz = V3.multiply_scalar (V3.add_scalar (n, 1.0), 0.5); in
      new vector_4f(V3.normalize (xyz), 1.0)
    end;

  --
  -- Compute a bitangent vector given a normal [n] and tangent vector [t],
  -- assuming a sign [[-1.0, 1.0]] stored in [t[w]].
  --

  function bitangent (
    n : vector_3f,
    t : vector_4f
  ) : vector_3f =
    let value p = V3.cross (n, t[x y z]); in
      V3.multiply_scalar (p, t[w])
    end;

  --
  -- Unpack a tangent-space normal vector from a texture.
  --

  function unpack (
    map : sampler_2d,
    uv  : vector_2f
  ) : vector_3f =
    let value rgb = S.texture (map, uv) [x y z]; in
      V3.add_scalar (V3.multiply_scalar (rgb, 2.0), -1.0)
    end;
    
  --
  -- Transform the tangent-space vector [m] into the coordinate
  -- system given by the orthnormal vectors [{t, b, n}].
  --

  function transform (
    m : vector_3f,
    t : vector_3f,
    b : vector_3f,
    n : vector_3f
  ) : vector_3f =
    let value mat = new matrix_3x3f (t, b, n); in
      V3.normalize (M3.multiply_vector (mat, m))
    end;

  --
  -- Given a texture consisting of normal vectors [t_normal], object-space
  -- normal [n], object-space tangent [t], object-space bitangent [b],
  -- and texture coordinates [uv], [unpack] and [transform] a
  -- vector from the texture, resulting in an object-space peturbed normal.
  --

  function bump_local (
    t_normal : sampler_2d,
    n        : vector_3f,
    t        : vector_3f,
    b        : vector_3f,
    uv       : vector_2f
  ) : vector_3f =
    let
      value m  = unpack (t_normal, uv);
      value nn = V3.normalize (n);
      value nt = V3.normalize (t);
      value nb = V3.normalize (b);
    in
      transform (m, nt, nb, nn)
    end;

  --
  -- Given a texture consisting of normal vectors [t_normal],
  -- an object-to-eye-space matrix [m_normal], object-space
  -- normal [n], object-space tangent [t], object-space bitangent [b],
  -- and texture coordinates [uv], [unpack] and [transform] a
  -- vector from the texture, and transform it to eye-space.
  --

  function bump (
    t_normal : sampler_2d,
    m_normal : matrix_3x3f,
    n        : vector_3f,
    t        : vector_3f,
    b        : vector_3f,
    uv       : vector_2f
  ) : vector_3f =
    M3.multiply_vector (
      m_normal,
      bump_local (t_normal, n, t, b, uv)
    );

end;
