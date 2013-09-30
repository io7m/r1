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

module Debug is

  import com.io7m.parasol.Vector3f   as V3;
  import com.io7m.parasol.Vector4f   as V4;
  import com.io7m.parasol.Matrix3x3f as M3;
  import com.io7m.parasol.Matrix4x4f as M4;
  import com.io7m.parasol.Sampler2D  as S;
  import com.io7m.renderer.Normals   as N;

  --
  -- Shader that shows the contents of the depth buffer.
  --

  shader vertex debug_depth_vertex is
    in        v_position   : vector_3f;
    parameter m_modelview  : matrix_4x4f;
    parameter m_projection : matrix_4x4f;
  with
    value position =
      M4.multiply_vector (
        M4.multiply (m_projection, m_modelview),
        new vector_4f (v_position, 1.0)
      );
  as
    out gl_Position = position;
  end;

  shader fragment debug_depth_fragment is
    out out_0 : vector_4f as 0;
  with
    value rgba = new vector_4f (gl_FragCoord [z z z], 1.0);
  as
    out out_0 = rgba;
  end;

  shader program debug_depth is
    vertex   debug_depth_vertex;
    fragment debug_depth_fragment;
  end;

  --
  -- Shader that shows UV coordinates as surface diffuse
  -- colour.
  --

  shader vertex debug_uv_vertex is
    in        v_position   : vector_3f;
    in        v_uv         : vector_2f;
    parameter m_modelview  : matrix_4x4f;
    parameter m_projection : matrix_4x4f;
    out       f_uv         : vector_2f;
  with
    value position =
      M4.multiply_vector (
        M4.multiply (m_projection, m_modelview),
        new vector_4f (v_position, 1.0)
      );
  as
    out gl_Position = position;
    out f_uv        = v_uv;
  end;

  shader fragment debug_uv_fragment is
    in  f_uv  : vector_2f;
    out out_0 : vector_4f as 0;
  with
    value rgba = new vector_4f (f_uv, 0.0, 1.0);
  as
    out out_0 = rgba;
  end;

  shader program debug_uv is
    vertex   debug_uv_vertex;
    fragment debug_uv_fragment;
  end;

  --
  -- Shader that shows vertex normals, in object local space,
  -- as surface diffuse colours.
  --

  shader vertex debug_normals_vertex_local_vertex is
    in        v_position   : vector_3f;
    in        v_normal     : vector_3f;
    parameter m_modelview  : matrix_4x4f;
    parameter m_projection : matrix_4x4f;
    out       f_normal     : vector_3f;
  with
    value position =
      M4.multiply_vector (
        M4.multiply (m_projection, m_modelview),
        new vector_4f (v_position, 1.0)
      );
  as
    out gl_Position = position;
    out f_normal    = v_normal;
  end;

  shader fragment debug_normals_vertex_local_fragment is
    in  f_normal : vector_3f;
    out out_0    : vector_4f as 0;
  with
    value rgba = N.to_rgba (f_normal);
  as
    out out_0 = rgba;
  end;

  shader program debug_normals_vertex_local is
    vertex   debug_normals_vertex_local_vertex;
    fragment debug_normals_vertex_local_fragment;
  end;

  --
  -- Shader that shows vertex normals, in eye space,
  -- as surface diffuse colours.
  --

  shader vertex debug_normals_vertex_eye_vertex is
    in        v_position   : vector_3f;
    in        v_normal     : vector_3f;
    parameter m_normal     : matrix_3x3f;
    parameter m_modelview  : matrix_4x4f;
    parameter m_projection : matrix_4x4f;
    out       f_normal     : vector_3f;
  with
    value position =
      M4.multiply_vector (
        M4.multiply (m_projection, m_modelview),
        new vector_4f (v_position, 1.0)
      );
    value normal =
      M3.multiply_vector (m_normal, v_normal);
  as
    out gl_Position = position;
    out f_normal    = normal;
  end;

  shader fragment debug_normals_vertex_eye_fragment is
    in  f_normal : vector_3f;
    out out_0    : vector_4f as 0;
  with
    value rgba = N.to_rgba (f_normal);
  as
    out out_0 = rgba;
  end;

  shader program debug_normals_vertex_eye is
    vertex   debug_normals_vertex_eye_vertex;
    fragment debug_normals_vertex_eye_fragment;
  end;

  --
  -- Shader that shows vertex tangents, where tangents are
  -- in three-dimensional form lacking a sign field, in object
  -- local space, as surface diffuse colours.
  --

  shader vertex debug_tangents3_vertex_local_vertex is
    in        v_position   : vector_3f;
    in        v_tangent3   : vector_3f;
    parameter m_modelview  : matrix_4x4f;
    parameter m_projection : matrix_4x4f;
    out       f_tangent    : vector_3f;
  with
    value position =
      M4.multiply_vector (
        M4.multiply (m_projection, m_modelview),
        new vector_4f (v_position, 1.0)
      );
  as
    out gl_Position  = position;
    out f_tangent    = v_tangent3;
  end;

  shader fragment debug_tangents3_vertex_local_fragment is
    in  f_tangent : vector_3f;
    out out_0     : vector_4f as 0;
  with
    value rgba = N.to_rgba (f_tangent);
  as
    out out_0 = rgba;
  end;

  shader program debug_tangents3_vertex_local is
    vertex   debug_tangents3_vertex_local_vertex;
    fragment debug_tangents3_vertex_local_fragment;
  end;
  
  --
  -- Shader that shows vertex tangents, where tangents are
  -- in four-dimensional form with a sign field, in object
  -- local space, as surface diffuse colours.
  --

  shader vertex debug_tangents4_vertex_local_vertex is
    in        v_position   : vector_3f;
    in        v_tangent4   : vector_4f;
    parameter m_modelview  : matrix_4x4f;
    parameter m_projection : matrix_4x4f;
    out       f_tangent    : vector_3f;
  with
    value position =
      M4.multiply_vector (
        M4.multiply (m_projection, m_modelview),
        new vector_4f (v_position, 1.0)
      );
    value tangent =
      v_tangent4 [x y z];
  as
    out gl_Position  = position;
    out f_tangent    = tangent;
  end;

  shader fragment debug_tangents4_vertex_local_fragment is
    in  f_tangent : vector_3f;
    out out_0     : vector_4f as 0;
  with
    value rgba = N.to_rgba (f_tangent);
  as
    out out_0 = rgba;
  end;

  shader program debug_tangents4_vertex_local is
    vertex   debug_tangents4_vertex_local_vertex;
    fragment debug_tangents4_vertex_local_fragment;
  end;

  --
  -- Shader that shows vertex tangents, where tangents are
  -- in three-dimensional form lacking a sign field, in
  -- eye-space, as surface diffuse colours.
  --

  shader vertex debug_tangents3_vertex_eye_vertex is
    in        v_position   : vector_3f;
    in        v_tangent3   : vector_3f;
    parameter m_normal     : matrix_3x3f;
    parameter m_modelview  : matrix_4x4f;
    parameter m_projection : matrix_4x4f;
    out       f_tangent    : vector_3f;
  with
    value position =
      M4.multiply_vector (
        M4.multiply (m_projection, m_modelview),
        new vector_4f (v_position, 1.0)
      );
    value tangent =
      M3.multiply_vector (m_normal, v_tangent3);
  as
    out gl_Position  = position;
    out f_tangent    = tangent;
  end;

  shader fragment debug_tangents3_vertex_eye_fragment is
    in  f_tangent : vector_3f;
    out out_0     : vector_4f as 0;
  with
    value rgba = N.to_rgba (f_tangent);
  as
    out out_0 = rgba;
  end;

  shader program debug_tangents3_vertex_eye is
    vertex   debug_tangents3_vertex_eye_vertex;
    fragment debug_tangents3_vertex_eye_fragment;
  end;
  
  --
  -- Shader that shows vertex tangents, where tangents are
  -- in four-dimensional form with a sign field, in object
  -- local space, as surface diffuse colours.
  --

  shader vertex debug_tangents4_vertex_eye_vertex is
    in        v_position   : vector_3f;
    in        v_tangent4   : vector_4f;
    parameter m_normal     : matrix_3x3f;
    parameter m_modelview  : matrix_4x4f;
    parameter m_projection : matrix_4x4f;
    out       f_tangent    : vector_3f;
  with
    value position =
      M4.multiply_vector (
        M4.multiply (m_projection, m_modelview),
        new vector_4f (v_position, 1.0)
      );
    value tangent3 =
      v_tangent4 [x y z];
    value tangent =
      M3.multiply_vector (m_normal, tangent3);
  as
    out gl_Position  = position;
    out f_tangent    = tangent;
  end;

  shader fragment debug_tangents4_vertex_eye_fragment is
    in  f_tangent : vector_3f;
    out out_0     : vector_4f as 0;
  with
    value rgba = N.to_rgba (f_tangent);
  as
    out out_0 = rgba;
  end;

  shader program debug_tangents4_vertex_eye is
    vertex   debug_tangents4_vertex_eye_vertex;
    fragment debug_tangents4_vertex_eye_fragment;
  end;

  --
  -- Shader that shows computed bitangents, in object local space,
  -- as surface diffuse colours.
  --

  shader vertex debug_bitangents_computed_local_vertex is
    in        v_position   : vector_3f;
    in        v_normal     : vector_3f;
    in        v_tangent4   : vector_4f;
    parameter m_modelview  : matrix_4x4f;
    parameter m_projection : matrix_4x4f;
    out       f_bitangent  : vector_3f;
  with
    value position =
      M4.multiply_vector (
        M4.multiply (m_projection, m_modelview),
        new vector_4f (v_position, 1.0)
      );
    value bitangent =
      N.bitangent (v_normal, v_tangent4);
  as
    out gl_Position = position;
    out f_bitangent = bitangent;
  end;

  shader fragment debug_bitangents_computed_local_fragment is
    in  f_bitangent : vector_3f;
    out out_0       : vector_4f as 0;
  with
    value rgba = N.to_rgba (f_bitangent);
  as
    out out_0 = rgba;
  end;

  shader program debug_bitangents_computed_local is
    vertex   debug_bitangents_computed_local_vertex;
    fragment debug_bitangents_computed_local_fragment;
  end;

  --
  -- Shader that shows provided bitangents, in object local space,
  -- as surface diffuse colours.
  --

  shader vertex debug_bitangents_provided_local_vertex is
    in        v_position   : vector_3f;
    in        v_bitangent  : vector_3f;
    parameter m_modelview  : matrix_4x4f;
    parameter m_projection : matrix_4x4f;
    out       f_bitangent  : vector_3f;
  with
    value position =
      M4.multiply_vector (
        M4.multiply (m_projection, m_modelview),
        new vector_4f (v_position, 1.0)
      );
  as
    out gl_Position = position;
    out f_bitangent = v_bitangent;
  end;

  shader fragment debug_bitangents_provided_local_fragment is
    in  f_bitangent : vector_3f;
    out out_0       : vector_4f as 0;
  with
    value rgba = N.to_rgba (f_bitangent);
  as
    out out_0 = rgba;
  end;

  shader program debug_bitangents_provided_local is
    vertex   debug_bitangents_provided_local_vertex;
    fragment debug_bitangents_provided_local_fragment;
  end;

  --
  -- Shader that shows computed bitangents, in eye space,
  -- as surface diffuse colours.
  --

  shader vertex debug_bitangents_computed_eye_vertex is
    in        v_position   : vector_3f;
    in        v_normal     : vector_3f;
    in        v_tangent4   : vector_4f;
    parameter m_normal     : matrix_3x3f;
    parameter m_modelview  : matrix_4x4f;
    parameter m_projection : matrix_4x4f;
    out       f_bitangent  : vector_3f;
  with
    value position =
      M4.multiply_vector (
        M4.multiply (m_projection, m_modelview),
        new vector_4f (v_position, 1.0)
      );
    value bitangent =
      M3.multiply_vector (m_normal, N.bitangent (v_normal, v_tangent4));
  as
    out gl_Position = position;
    out f_bitangent = bitangent;
  end;

  shader fragment debug_bitangents_computed_eye_fragment is
    in  f_bitangent : vector_3f;
    out out_0       : vector_4f as 0;
  with
    value rgba = N.to_rgba (f_bitangent);
  as
    out out_0 = rgba;
  end;

  shader program debug_bitangents_computed_eye is
    vertex   debug_bitangents_computed_eye_vertex;
    fragment debug_bitangents_computed_eye_fragment;
  end;

  --
  -- Shader that shows provided bitangents, in eye space,
  -- as surface diffuse colours.
  --

  shader vertex debug_bitangents_provided_eye_vertex is
    in        v_position   : vector_3f;
    in        v_bitangent  : vector_3f;
    parameter m_normal     : matrix_3x3f;
    parameter m_modelview  : matrix_4x4f;
    parameter m_projection : matrix_4x4f;
    out       f_bitangent  : vector_3f;
  with
    value position =
      M4.multiply_vector (
        M4.multiply (m_projection, m_modelview),
        new vector_4f (v_position, 1.0)
      );
    value bitangent =
      M3.multiply_vector (m_normal, v_bitangent);
  as
    out gl_Position = position;
    out f_bitangent = bitangent;
  end;

  shader fragment debug_bitangents_provided_eye_fragment is
    in  f_bitangent : vector_3f;
    out out_0       : vector_4f as 0;
  with
    value rgba = N.to_rgba (f_bitangent);
  as
    out out_0 = rgba;
  end;

  shader program debug_bitangents_provided_eye is
    vertex   debug_bitangents_provided_eye_vertex;
    fragment debug_bitangents_provided_eye_fragment;
  end;

  --
  -- Shader that shows raw tangent-space normals as
  -- read from a normal map.
  --

  shader vertex debug_normals_map_tangent_vertex is
    in        v_position   : vector_3f;
    in        v_uv         : vector_2f;
    parameter m_modelview  : matrix_4x4f;
    parameter m_projection : matrix_4x4f;
    out       f_uv         : vector_2f;
  with
    value position =
      M4.multiply_vector (
        M4.multiply (m_projection, m_modelview),
        new vector_4f (v_position, 1.0)
      );
  as
    out gl_Position = position;
    out f_uv        = v_uv;
  end;

  shader fragment debug_normals_map_tangent_fragment is
    in        f_uv     : vector_2f;
    parameter t_normal : sampler_2d;
    out       out_0    : vector_4f as 0;
  with
    value rgba = new vector_4f (S.texture (t_normal, f_uv) [x y z], 1.0);
  as
    out out_0 = rgba;
  end;

  shader program debug_normals_map_tangent is
    vertex   debug_normals_map_tangent_vertex;
    fragment debug_normals_map_tangent_fragment;
  end;

  --
  -- Shader that shows object-space normals computed from a tangent-space
  -- normal map, using computed bitangents, as surface diffuse
  -- colours.
  --

  shader vertex debug_normals_computed_bitangent_map_local_vertex is
    in        v_position   : vector_3f;
    in        v_normal     : vector_3f;
    in        v_tangent4   : vector_4f;
    in        v_uv         : vector_2f;
    parameter m_modelview  : matrix_4x4f;
    parameter m_projection : matrix_4x4f;
    out       f_uv         : vector_2f;
    out       f_normal     : vector_3f;
    out       f_tangent    : vector_3f;
    out       f_bitangent  : vector_3f;
  with
    value position =
      M4.multiply_vector (
        M4.multiply (m_projection, m_modelview),
        new vector_4f (v_position, 1.0)
      );

    value tangent =
      v_tangent4 [x y z];

    value bitangent =
      N.bitangent (v_normal, v_tangent4);
  as
    out gl_Position = position;
    out f_normal    = v_normal;
    out f_tangent   = tangent;
    out f_bitangent = bitangent;
    out f_uv        = v_uv;
  end;

  shader fragment debug_normals_computed_bitangent_map_local_fragment is
    in  f_normal       : vector_3f;
    in  f_tangent      : vector_3f;
    in  f_bitangent    : vector_3f;
    in  f_uv           : vector_2f;
    parameter t_normal : sampler_2d;
    out out_0          : vector_4f as 0;
  with
    value m = N.unpack (t_normal, f_uv);
    value n = V3.normalize (f_normal);
    value t = V3.normalize (f_tangent);
    value b = V3.normalize (f_bitangent);
    value r = N.transform (m, t, b, n);
    value rgba = N.to_rgba (r);
  as
    out out_0 = rgba;
  end;

  shader program debug_normals_computed_bitangent_map_local is
    vertex   debug_normals_computed_bitangent_map_local_vertex;
    fragment debug_normals_computed_bitangent_map_local_fragment;
  end;

  --
  -- Shader that shows object-space normals computed from a tangent-space
  -- normal map, using provided bitangents, as surface diffuse
  -- colours.
  --

  shader vertex debug_normals_provided_bitangent_map_local_vertex is
    in        v_position   : vector_3f;
    in        v_normal     : vector_3f;
    in        v_tangent3   : vector_3f;
    in        v_bitangent  : vector_3f;
    in        v_uv         : vector_2f;
    parameter m_modelview  : matrix_4x4f;
    parameter m_projection : matrix_4x4f;
    out       f_uv         : vector_2f;
    out       f_normal     : vector_3f;
    out       f_tangent    : vector_3f;
    out       f_bitangent  : vector_3f;
  with
    value position =
      M4.multiply_vector (
        M4.multiply (m_projection, m_modelview),
        new vector_4f (v_position, 1.0)
      );
  as
    out gl_Position = position;
    out f_normal    = v_normal;
    out f_tangent   = v_tangent3;
    out f_bitangent = v_bitangent;
    out f_uv        = v_uv;
  end;

  shader fragment debug_normals_provided_bitangent_map_local_fragment is
    in  f_normal       : vector_3f;
    in  f_tangent      : vector_3f;
    in  f_bitangent    : vector_3f;
    in  f_uv           : vector_2f;
    parameter t_normal : sampler_2d;
    out out_0          : vector_4f as 0;
  with
    value m = N.unpack (t_normal, f_uv);
    value n = V3.normalize (f_normal);
    value t = V3.normalize (f_tangent);
    value b = V3.normalize (f_bitangent);
    value r = N.transform (m, t, b, n);
    value rgba = N.to_rgba (r);
  as
    out out_0 = rgba;
  end;

  shader program debug_normals_provided_bitangent_map_local is
    vertex   debug_normals_provided_bitangent_map_local_vertex;
    fragment debug_normals_provided_bitangent_map_local_fragment;
  end;

  --
  -- Shader that shows eye-space normals computed from a tangent-space
  -- normal map, using computed bitangents, as surface diffuse
  -- colours.
  --

  shader vertex debug_normals_computed_bitangent_map_eye_vertex is
    in        v_position   : vector_3f;
    in        v_normal     : vector_3f;
    in        v_tangent4   : vector_4f;
    in        v_uv         : vector_2f;
    parameter m_modelview  : matrix_4x4f;
    parameter m_projection : matrix_4x4f;
    out       f_uv         : vector_2f;
    out       f_normal     : vector_3f;
    out       f_tangent    : vector_3f;
    out       f_bitangent  : vector_3f;
  with
    value position =
      M4.multiply_vector (
        M4.multiply (m_projection, m_modelview),
        new vector_4f (v_position, 1.0)
      );

    value tangent =
      v_tangent4 [x y z];

    value bitangent =
      N.bitangent (v_normal, v_tangent4);
  as
    out gl_Position = position;
    out f_normal    = v_normal;
    out f_tangent   = tangent;
    out f_bitangent = bitangent;
    out f_uv        = v_uv;
  end;

  shader fragment debug_normals_computed_bitangent_map_eye_fragment is
    in  f_normal       : vector_3f;
    in  f_tangent      : vector_3f;
    in  f_bitangent    : vector_3f;
    in  f_uv           : vector_2f;
    parameter m_normal : matrix_3x3f;
    parameter t_normal : sampler_2d;
    out out_0          : vector_4f as 0;
  with
    value m = N.unpack (t_normal, f_uv);
    value n = V3.normalize (f_normal);
    value t = V3.normalize (f_tangent);
    value b = V3.normalize (f_bitangent);
    value r = N.transform (m, t, b, n);
    value e = M3.multiply_vector (m_normal, r);
    value rgba = N.to_rgba (e);
  as
    out out_0 = rgba;
  end;

  shader program debug_normals_computed_bitangent_map_eye is
    vertex   debug_normals_computed_bitangent_map_eye_vertex;
    fragment debug_normals_computed_bitangent_map_eye_fragment;
  end;

  --
  -- Shader that shows eye-space normals computed from a tangent-space
  -- normal map, using provided bitangents, as surface diffuse
  -- colours.
  --

  shader vertex debug_normals_provided_bitangent_map_eye_vertex is
    in        v_position   : vector_3f;
    in        v_normal     : vector_3f;
    in        v_tangent3   : vector_3f;
    in        v_bitangent  : vector_3f;
    in        v_uv         : vector_2f;
    parameter m_modelview  : matrix_4x4f;
    parameter m_projection : matrix_4x4f;
    out       f_uv         : vector_2f;
    out       f_normal     : vector_3f;
    out       f_tangent    : vector_3f;
    out       f_bitangent  : vector_3f;
  with
    value position =
      M4.multiply_vector (
        M4.multiply (m_projection, m_modelview),
        new vector_4f (v_position, 1.0)
      );
  as
    out gl_Position = position;
    out f_normal    = v_normal;
    out f_tangent   = v_tangent3;
    out f_bitangent = v_bitangent;
    out f_uv        = v_uv;
  end;

  shader fragment debug_normals_provided_bitangent_map_eye_fragment is
    in  f_normal       : vector_3f;
    in  f_tangent      : vector_3f;
    in  f_bitangent    : vector_3f;
    in  f_uv           : vector_2f;
    parameter m_normal : matrix_3x3f;
    parameter t_normal : sampler_2d;
    out out_0          : vector_4f as 0;
  with
    value m = N.unpack (t_normal, f_uv);
    value n = V3.normalize (f_normal);
    value t = V3.normalize (f_tangent);
    value b = V3.normalize (f_bitangent);
    value r = N.transform (m, t, b, n);
    value e = M3.multiply_vector (m_normal, r);
    value rgba = N.to_rgba (e);
  as
    out out_0 = rgba;
  end;

  shader program debug_normals_provided_bitangent_map_eye is
    vertex   debug_normals_provided_bitangent_map_eye_vertex;
    fragment debug_normals_provided_bitangent_map_eye_fragment;
  end;

end;
