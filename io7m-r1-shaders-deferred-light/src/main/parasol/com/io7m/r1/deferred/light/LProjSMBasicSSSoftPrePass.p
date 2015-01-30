package com.io7m.r1.deferred.light;

module LProjSMBasicSSSoftPrePass is

  import com.io7m.parasol.Float      as F;
  import com.io7m.parasol.Fragment;
  import com.io7m.parasol.Matrix3x3f as M3;
  import com.io7m.parasol.Matrix4x4f as M4;
  import com.io7m.parasol.Vector3f   as V3;
  import com.io7m.parasol.Vector4f   as V4;
  import com.io7m.parasol.Sampler2D  as S;

  import com.io7m.r1.core.Light;
  import com.io7m.r1.core.LogDepth;
  import com.io7m.r1.core.ProjectiveLight;
  import com.io7m.r1.core.Reconstruction;
  import com.io7m.r1.core.ShadowBasic;
  import com.io7m.r1.core.Transform;
  import com.io7m.r1.core.VectorAux;
  import com.io7m.r1.core.Viewport;
  import com.io7m.r1.core.ViewRays;
  import com.io7m.r1.core.VertexShaders;

  shader fragment f is

    -- The eye-space position of the current light volume fragment
    in f_position_eye : vector_4f;

    -- Logarithmic depth parameters
    in f_positive_eye_z         : float;
    parameter depth_coefficient : float;

    -- Matrices
    parameter m_view_inv   : matrix_4x4f;
    parameter m_projection : matrix_4x4f;

    -- G-buffer components
    parameter t_map_depth     : sampler_2d;

    -- Standard declarations
    out out_0           : float as 0;
    out depth out_depth : float;

    parameter view_rays : ViewRays.t;
    parameter viewport  : Viewport.t;

    -- Projective light parameters
    parameter light_projective   : Light.t;
    parameter m_eye_to_light_eye : matrix_4x4f;
    parameter m_light_projection : matrix_4x4f;

    -- Projective light (screen-space soft shadow mapping) parameters
    parameter t_shadow_basic_sssoft : sampler_2d;
    parameter shadow_basic_sssoft   : ShadowBasic.t;

  with
    value position_uv =
      Transform.screen_to_texture2 (
        viewport,
        Fragment.coordinate [x y]
      );

    value r_depth =
      LogDepth.encode_partial (f_positive_eye_z, depth_coefficient);

    -- Reconstruct eye-space position.
    value log_depth =
      S.texture (t_map_depth, position_uv) [x];
    value eye_depth =
      F.negate (LogDepth.decode (log_depth, depth_coefficient));
    value eye_position =
      Reconstruction.reconstruct_eye_with_eye_z (
        eye_depth,
        position_uv,
        m_projection,
        view_rays
      );

    -- Projective light-eye-space position
    value position_light_eye =
      M4.multiply_vector (
        m_eye_to_light_eye,
        eye_position
     );

    -- Projective light-clip-space position
    value position_light_clip =
      M4.multiply_vector (
        m_light_projection,
        position_light_eye
     );

    -- Projective light vectors/attenuation
    value light_vectors =
      Light.calculate (
        light_projective,
        eye_position [x y z],
        new vector_3f (0.0, 0.0, 1.0)
      );

    -- Basic shadow mapping
    value light_shadow =
      ShadowBasic.factor (
        shadow_basic_sssoft,
        t_shadow_basic_sssoft,
        position_light_eye,
        position_light_clip
      );
  as
    out out_0     = light_shadow;
    out out_depth = r_depth;
  end;

  shader program p is
    vertex VertexShaders.standard;
    fragment f;
  end;

end;

