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

--
-- Ambient occlusion functions.
--

module BilateralBlur is

  import com.io7m.parasol.Float      as F;
  import com.io7m.parasol.Fragment;
  import com.io7m.parasol.Matrix3x3f as M3;
  import com.io7m.parasol.Matrix4x4f as M4;
  import com.io7m.parasol.Vector2f   as V2;
  import com.io7m.parasol.Vector3f   as V3;
  import com.io7m.parasol.Vector4f   as V4;
  import com.io7m.parasol.Sampler2D  as S;

  import com.io7m.r1.core.LogDepth;
  import com.io7m.r1.core.Normals;
  import com.io7m.r1.core.Reconstruction;
  import com.io7m.r1.core.Transform;
  import com.io7m.r1.core.Viewport;
  import com.io7m.r1.core.VertexShaders;

  type blur_process_1f is record
    color  : float,
    weight : float
  end;

  function conditionally_sample_1f (
    current         : blur_process_1f,
    image           : sampler_2d,
    uv              : vector_2f,
    occludee_depth  : float,
    occludee_normal : vector_3f,
    occluder_depth  : float,
    occluder_normal : vector_3f,
    weight          : float
  ) : blur_process_1f =
    let
      value depth_ok  = F.greater_or_equal (V3.dot (occluder_normal, occludee_normal), 0.8);
      value normal_ok = F.lesser_or_equal (F.absolute (F.subtract (occluder_depth, occludee_depth)), 0.2);
    in
      if depth_ok then
        if normal_ok then
          let
            value c = F.multiply (S.texture_with_lod (image, uv, 0.0) [x], weight);
            value w = weight;
          in
            record blur_process_1f {
              color  = F.add (c, current.color),
              weight = F.add (w, current.weight)
            }
          end
        else
          current
        end
      else
        current
      end
    end;

  shader fragment bilateral_blur_horizontal_1f_f is

    -- Logarithmic depth parameters
    in f_positive_eye_z         : float;
    parameter depth_coefficient : float;

    -- G-buffer components
    parameter t_map_normal : sampler_2d;
    parameter t_map_depth  : sampler_2d;
    parameter t_image      : sampler_2d;

    -- Standard declarations
    out out_0 : vector_4f as 0;

    -- Viewports
    parameter gbuffer_viewport : Viewport.t;
    parameter image_viewport   : Viewport.t;

  with
    value image_uv =
      Transform.screen_to_texture2 (
        image_viewport,
        Fragment.coordinate [x y]
      );

    -- Get surface normal
    value normal_sample =
      S.texture (t_map_normal, image_uv) [x y];
    value normal =
      V3.normalize (Normals.decompress (normal_sample));

    -- Get surface depth
    value log_depth =
      S.texture (t_map_depth, image_uv) [x];
    value eye_depth =
      F.negate (LogDepth.decode (log_depth, depth_coefficient));

    value gauss_offset0 = 0.0;
    value gauss_offset1 = 1.3846153846;
    value gauss_offset2 = 3.2307692308;

    value gauss_weight0 = 0.2270270270;
    value gauss_weight1 = 0.3162162162;
    value gauss_weight2 = 0.0702702703;

    value r_0 = record blur_process_1f {
      color  = F.multiply (S.texture_with_lod (t_image, image_uv, 0.0) [x], gauss_weight0),
      weight = gauss_weight0
    };

    value gbuffer_uv_x_p1 = F.add (image_uv [x], F.multiply (gbuffer_viewport.inverse_width, gauss_offset1));
    value gbuffer_uv_p1   = new vector_2f (gbuffer_uv_x_p1, image_uv [y]);
    value depth_p1        = F.negate (LogDepth.decode (S.texture (t_map_depth, gbuffer_uv_p1) [x], depth_coefficient));
    value normal_p1       = V3.normalize (Normals.decompress (S.texture (t_map_normal, gbuffer_uv_p1) [x y]));
    value image_uv_x_p1   = F.add (image_uv [x], F.multiply (image_viewport.inverse_width, gauss_offset1));
    value image_uv_p1     = new vector_2f (image_uv_x_p1, image_uv [y]);
    value r_p1            = conditionally_sample_1f (r_0, t_image, image_uv_p1, eye_depth, normal, depth_p1, normal_p1, gauss_weight1);

    value gbuffer_uv_x_p2 = F.add (image_uv [x], F.multiply (gbuffer_viewport.inverse_width, gauss_offset2));
    value gbuffer_uv_p2   = new vector_2f (gbuffer_uv_x_p2, image_uv [y]);
    value depth_p2        = F.negate (LogDepth.decode (S.texture (t_map_depth, gbuffer_uv_p2) [x], depth_coefficient));
    value normal_p2       = V3.normalize (Normals.decompress (S.texture (t_map_normal, gbuffer_uv_p2) [x y]));
    value image_uv_x_p2   = F.add (image_uv [x], F.multiply (image_viewport.inverse_width, gauss_offset2));
    value image_uv_p2     = new vector_2f (image_uv_x_p2, image_uv [y]);
    value r_p2            = conditionally_sample_1f (r_p1, t_image, image_uv_p2, eye_depth, normal, depth_p2, normal_p2, gauss_weight2);

    value gbuffer_uv_x_m1 = F.add (image_uv [x], F.multiply (gbuffer_viewport.inverse_width, gauss_offset1));
    value gbuffer_uv_m1   = new vector_2f (gbuffer_uv_x_m1, image_uv [y]);
    value depth_m1        = F.negate (LogDepth.decode (S.texture (t_map_depth, gbuffer_uv_m1) [x], depth_coefficient));
    value normal_m1       = V3.normalize (Normals.decompress (S.texture (t_map_normal, gbuffer_uv_m1) [x y]));
    value image_uv_x_m1   = F.add (image_uv [x], F.multiply (image_viewport.inverse_width, gauss_offset1));
    value image_uv_m1     = new vector_2f (image_uv_x_m1, image_uv [y]);
    value r_m1            = conditionally_sample_1f (r_p2, t_image, image_uv_m1, eye_depth, normal, depth_m1, normal_m1, gauss_weight1);

    value gbuffer_uv_x_m2 = F.add (image_uv [x], F.multiply (gbuffer_viewport.inverse_width, gauss_offset2));
    value gbuffer_uv_m2   = new vector_2f (gbuffer_uv_x_m2, image_uv [y]);
    value depth_m2        = F.negate (LogDepth.decode (S.texture (t_map_depth, gbuffer_uv_m2) [x], depth_coefficient));
    value normal_m2       = V3.normalize (Normals.decompress (S.texture (t_map_normal, gbuffer_uv_m2) [x y]));
    value image_uv_x_m2   = F.add (image_uv [x], F.multiply (image_viewport.inverse_width, gauss_offset2));
    value image_uv_m2     = new vector_2f (image_uv_x_m2, image_uv [y]);
    value r_m2            = conditionally_sample_1f (r_m1, t_image, image_uv_m2, eye_depth, normal, depth_m2, normal_m2, gauss_weight2);

    value rc =
      F.divide (r_m2.color, r_m2.weight);
    value r =
      new vector_4f (rc, 1.0, 1.0, 1.0);
  as
    out out_0 = r;
  end;

  shader program bilateral_blur_horizontal_1f is
    vertex   VertexShaders.standard_clip_eye;
    fragment bilateral_blur_horizontal_1f_f;
  end;

  shader fragment bilateral_blur_vertical_1f_f is

    -- Logarithmic depth parameters
    in f_positive_eye_z         : float;
    parameter depth_coefficient : float;

    -- G-buffer components
    parameter t_map_normal : sampler_2d;
    parameter t_map_depth  : sampler_2d;
    parameter t_image      : sampler_2d;

    -- Standard declarations
    out out_0 : vector_4f as 0;

    -- Viewports
    parameter gbuffer_viewport : Viewport.t;
    parameter image_viewport   : Viewport.t;

  with
    value image_uv =
      Transform.screen_to_texture2 (
        image_viewport,
        Fragment.coordinate [x y]
      );

    -- Get surface normal
    value normal_sample =
      S.texture (t_map_normal, image_uv) [x y];
    value normal =
      V3.normalize (Normals.decompress (normal_sample));

    -- Get surface depth
    value log_depth =
      S.texture (t_map_depth, image_uv) [x];
    value eye_depth =
      F.negate (LogDepth.decode (log_depth, depth_coefficient));

    value gauss_offset0 = 0.0;
    value gauss_offset1 = 1.3846153846;
    value gauss_offset2 = 3.2307692308;

    value gauss_weight0 = 0.2270270270;
    value gauss_weight1 = 0.3162162162;
    value gauss_weight2 = 0.0702702703;

    value r_0 = record blur_process_1f {
      color  = F.multiply (S.texture_with_lod (t_image, image_uv, 0.0) [x], gauss_weight0),
      weight = gauss_weight0
    };

    value gbuffer_uv_y_p1 = F.add (image_uv [y], F.multiply (gbuffer_viewport.inverse_height, gauss_offset1));
    value gbuffer_uv_p1   = new vector_2f (image_uv [x], gbuffer_uv_y_p1);
    value depth_p1        = F.negate (LogDepth.decode (S.texture (t_map_depth, gbuffer_uv_p1) [x], depth_coefficient));
    value normal_p1       = V3.normalize (Normals.decompress (S.texture (t_map_normal, gbuffer_uv_p1) [x y]));
    value image_uv_y_p1   = F.add (image_uv [y], F.multiply (image_viewport.inverse_height, gauss_offset1));
    value image_uv_p1     = new vector_2f (image_uv [x], image_uv_y_p1);
    value r_p1            = conditionally_sample_1f (r_0, t_image, image_uv_p1, eye_depth, normal, depth_p1, normal_p1, gauss_weight1);

    value gbuffer_uv_y_p2 = F.add (image_uv [y], F.multiply (gbuffer_viewport.inverse_height, gauss_offset2));
    value gbuffer_uv_p2   = new vector_2f (image_uv [x], gbuffer_uv_y_p2);
    value depth_p2        = F.negate (LogDepth.decode (S.texture (t_map_depth, gbuffer_uv_p2) [x], depth_coefficient));
    value normal_p2       = V3.normalize (Normals.decompress (S.texture (t_map_normal, gbuffer_uv_p2) [x y]));
    value image_uv_y_p2   = F.add (image_uv [y], F.multiply (image_viewport.inverse_height, gauss_offset2));
    value image_uv_p2     = new vector_2f (image_uv [x], image_uv_y_p2);
    value r_p2            = conditionally_sample_1f (r_p1, t_image, image_uv_p2, eye_depth, normal, depth_p2, normal_p2, gauss_weight2);

    value gbuffer_uv_y_m1 = F.add (image_uv [y], F.multiply (gbuffer_viewport.inverse_height, gauss_offset1));
    value gbuffer_uv_m1   = new vector_2f (image_uv [x], gbuffer_uv_y_m1);
    value depth_m1        = F.negate (LogDepth.decode (S.texture (t_map_depth, gbuffer_uv_m1) [x], depth_coefficient));
    value normal_m1       = V3.normalize (Normals.decompress (S.texture (t_map_normal, gbuffer_uv_m1) [x y]));
    value image_uv_y_m1   = F.add (image_uv [y], F.multiply (image_viewport.inverse_height, gauss_offset1));
    value image_uv_m1     = new vector_2f (image_uv [x], image_uv_y_m1);
    value r_m1            = conditionally_sample_1f (r_p2, t_image, image_uv_m1, eye_depth, normal, depth_m1, normal_m1, gauss_weight1);

    value gbuffer_uv_y_m2 = F.add (image_uv [y], F.multiply (gbuffer_viewport.inverse_height, gauss_offset2));
    value gbuffer_uv_m2   = new vector_2f (image_uv [x], gbuffer_uv_y_m2);
    value depth_m2        = F.negate (LogDepth.decode (S.texture (t_map_depth, gbuffer_uv_m2) [x], depth_coefficient));
    value normal_m2       = V3.normalize (Normals.decompress (S.texture (t_map_normal, gbuffer_uv_m2) [x y]));
    value image_uv_y_m2   = F.add (image_uv [y], F.multiply (image_viewport.inverse_height, gauss_offset2));
    value image_uv_m2     = new vector_2f (image_uv [x], image_uv_y_m2);
    value r_m2            = conditionally_sample_1f (r_m1, t_image, image_uv_m2, eye_depth, normal, depth_m2, normal_m2, gauss_weight2);

    value rc =
      F.divide (r_m2.color, r_m2.weight);
    value r =
      new vector_4f (rc, 1.0, 1.0, 1.0);
  as
    out out_0 = r;
  end;

  shader program bilateral_blur_vertical_1f is
    vertex   VertexShaders.standard_clip_eye;
    fragment bilateral_blur_vertical_1f_f;
  end;

end;