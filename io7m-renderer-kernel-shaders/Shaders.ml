(*| Copyright © 2013 <code@io7m.com> http://io7m.com                         *)
(*|                                                                          *)
(*| Permission to use, copy, modify, and/or distribute this software for any *)
(*| purpose with or without fee is hereby granted, provided that the above   *)
(*| copyright notice and this permission notice appear in all copies.        *)
(*|                                                                          *)
(*| THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES *)
(*| WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF         *)
(*| MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR  *)
(*| ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES   *)
(*| WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN    *)
(*| ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF  *)
(*| OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.           *)

let vs_uv_attributes p =
  begin match Labels.label_implies_uv p with
  | true ->
      ["  in v_uv : vector_2f;\n";
      "  out f_uv : vector_2f;\n"]
  | false -> []
  end

let vs_uv_writes p =
  begin match Labels.label_implies_uv p with
  | true -> ["  out f_uv = v_uv;\n"]
  | false -> []
  end

let vs_normal_attributes n =
  begin match n with
  | Labels.LNormalsNone ->
      assert false
  | Labels.LNormalsVertex -> [
      "  in v_normal : vector_3f;\n";
      "  out f_normal_es : vector_3f;\n"]
  | Labels.LNormalsMapped -> [
      "  in v_normal : vector_3f;\n";
      "  in v_tangent4 : vector_4f;\n";
      "  out f_normal_os : vector_3f;\n";
      "  out f_tangent : vector_3f;\n";
      "  out f_bitangent : vector_3f;\n"]
  end

let vs_normal_parameters n =
  begin match n with
  | Labels.LNormalsNone -> assert false
  | Labels.LNormalsVertex -> ["  parameter m_normal : matrix_3x3f;\n"]
  | Labels.LNormalsMapped -> []
  end

let vs_normal_values n =
  begin match n with
  | Labels.LNormalsNone -> assert false
  | Labels.LNormalsVertex -> [
      "  value normal =\n";
      "    M3.multiply_vector (m_normal, v_normal);\n"
      ]
  | Labels.LNormalsMapped -> [
      "  value tangent =\n";
      "    v_tangent4 [x y z];\n";
      "  value bitangent =\n";
      "    N.bitangent (v_normal, v_tangent4);\n"]
  end

let vs_normal_writes n =
  begin match n with
  | Labels.LNormalsNone -> assert false
  | Labels.LNormalsVertex -> [
      "  out f_normal_es = normal;\n"
      ]
  | Labels.LNormalsMapped -> [
      "  out f_normal_os = v_normal;\n";
      "  out f_tangent = tangent;\n";
      "  out f_bitangent = bitangent;\n"
      ]
  end

let vs_standard_matrices = [
  "  parameter m_modelview : matrix_4x4f;\n";
  "  parameter m_projection : matrix_4x4f;\n"]

let vs_standard_positions = [
  "  value clip_position =\n";
  "    M4.multiply_vector (\n";
  "      M4.multiply (m_projection, m_modelview),\n";
  "      new vector_4f (v_position, 1.0)\n";
  "    );\n";
  "  value position =\n";
  "    M4.multiply_vector (\n";
  "      m_modelview,\n";
  "      new vector_4f (v_position, 1.0)\n";
  "    );\n"]

let vs_standard_writes =[
  "  out gl_Position = clip_position;\n";
  "  out f_position = position;\n"]

let vs_standard_io = [
  "  in v_position : vector_3f;\n";
  "  out f_position : vector_4f;\n"]

let fwd_vertex_shader = function
  | Labels.LUnlit _ as p ->
      ["shader vertex fwd_"; Labels.label_code p; "_vertex is\n"] @
      vs_standard_io @
      (vs_uv_attributes p) @
      vs_standard_matrices @
      ["with\n"] @
      vs_standard_positions @
      ["as\n"] @
      vs_standard_writes @
      (vs_uv_writes p) @
      ["end;\n"]
  | Labels.LLit (_, _, _, n, _, _, _) as p ->
      ["shader vertex fwd_"; Labels.label_code p; "_vertex is\n"] @
      vs_standard_io @
      (vs_uv_attributes p) @
      (vs_normal_attributes n) @
      (vs_normal_parameters n) @
      vs_standard_matrices @
      ["with\n"] @
      vs_standard_positions @
      (vs_normal_values n) @
      ["as\n"] @
      vs_standard_writes @
      (vs_uv_writes p) @
      (vs_normal_writes n) @
      ["end;\n"]

let fs_uv_attributes p =
  begin match Labels.label_implies_uv p with
  | true ->["  in f_uv : vector_2f;\n"]
  | false -> []
  end

let fs_normal_attributes n =
  begin match n with
  | Labels.LNormalsNone -> assert false
  | Labels.LNormalsVertex -> [
      "  in f_normal_es : vector_3f;\n"
      ]
  | Labels.LNormalsMapped -> [
      "  in f_normal_os : vector_3f;\n";
      "  in f_tangent : vector_3f;\n";
      "  in f_bitangent : vector_3f;\n"
      ]
  end

let fs_normal_parameters n =
  begin match n with
  | Labels.LNormalsNone -> assert false
  | Labels.LNormalsVertex -> []
  | Labels.LNormalsMapped -> [
      "  parameter m_normal : matrix_3x3f;\n";
      "  parameter t_normal : sampler_2d;\n"
      ]
  end

let fs_normal_value n =
  begin match n with
  | Labels.LNormalsNone -> assert false
  | Labels.LNormalsVertex -> [
      "  value n = f_normal_es;\n"
      ]
  | Labels.LNormalsMapped -> [
      "  value n = N.bump (\n";
      "    t_normal,\n";
      "    m_normal,\n";
      "    V3.normalize (f_normal_os),\n";
      "    V3.normalize (f_tangent),\n";
      "    V3.normalize (f_bitangent),\n";
      "    f_uv\n";
      "  );\n"
      ]
  end

let fs_standard_inputs = [
  "  in f_position : vector_4f;\n"]

let fs_standard_outputs = [
  "  out out_0 : vector_4f as 0;\n"]

let fs_albedo_parameters = function
  | Labels.LAlbedoColour -> []
  | Labels.LAlbedoTextured ->
      ["  parameter t_albedo : sampler_2d;\n"]

let fs_specular_parameters s =
  begin match s with
  | Labels.LSpecularNone -> []
  | Labels.LSpecularConstant -> []
  | Labels.LSpecularMapped ->
      ["  parameter t_specular : sampler_2d;\n"]
  end

let fs_emissive_parameters m =
  begin match m with
  | Labels.LEmissiveNone -> []
  | Labels.LEmissiveConstant -> []
  | Labels.LEmissiveMapped -> [
      "  parameter t_emissive : sampler_2d;\n"
      ]
  end

let fs_environment_parameters e =
  begin match e with
  | Labels.LEnvironmentNone -> []
  | Labels.LEnvironmentReflective
  | Labels.LEnvironmentRefractive
  | Labels.LEnvironmentReflectiveRefractive -> [
      "  parameter t_environment : sampler_cube;\n";
      "  parameter m_view_inv    : matrix_4x4f;\n"
      ]
  end

let fs_material_values m s =
  begin match (m, s) with
  | (Labels.LEmissiveNone, Labels.LSpecularNone)
  | (Labels.LEmissiveConstant, Labels.LSpecularNone)
  | (Labels.LEmissiveConstant, Labels.LSpecularConstant)
  | (Labels.LEmissiveNone, Labels.LSpecularConstant) -> [
      "  value m = material;\n"
      ]
  
  | (Labels.LEmissiveConstant, Labels.LSpecularMapped)
  | (Labels.LEmissiveNone, Labels.LSpecularMapped) -> [
      "  value m =\n";
      "    record M.t {\n";
      "      emissive    = material.emissive,\n";
      "      albedo      = material.albedo,\n";
      "      environment = material.environment,\n";
      "      specular    = record M.specular {\n";
      "        exponent  = material.specular.exponent,\n";
      "        intensity = S.texture (t_specular, f_uv)[x]\n";
      "      }\n";
      "    };\n";
      ]
  
  | (Labels.LEmissiveMapped, Labels.LSpecularMapped) -> [
      "  value m =\n";
      "    record M.t {\n";
      "      emissive   = record M.emissive {\n";
      "        emissive = S.texture (t_emissive, f_uv)[x]\n";
      "      },\n";
      "      albedo      = material.albedo,\n";
      "      environment = material.environment,\n";
      "      specular    = record M.specular {\n";
      "        exponent  = material.specular.exponent,\n";
      "        intensity = S.texture (t_specular, f_uv)[x]\n";
      "      }\n";
      "    };\n";
      ]
  
  | (Labels.LEmissiveMapped, Labels.LSpecularNone)
  | (Labels.LEmissiveMapped, Labels.LSpecularConstant) -> [
      "  value m =\n";
      "    record M.t {\n";
      "      emissive   = record M.emissive {\n";
      "        emissive = S.texture (t_emissive, f_uv)[x]\n";
      "      },\n";
      "      albedo      = material.albedo,\n";
      "      environment = material.environment,\n";
      "      specular    = material.specular\n";
      "    };\n";
      ]
  end

let fs_standard_parameters = function
  | Labels.LUnlit _ -> []
  | Labels.LLit (_, _, _, _, _, l, _) ->
      begin match l with
      | Labels.LLightDirectional ->
          ["  parameter light : DL.t;\n"]
      | Labels.LLightSpherical ->
          ["  parameter light : SL.t;\n"]
      | Labels.LLightDirectionalShadowMapped ->
          ["  parameter light : DL.t;\n"]
      | Labels.LLightSphericalShadowMapped ->
          ["  parameter light : SL.t;\n"]
      end
      @
      ["  parameter material : M.t;\n"]

let fs_environment_values = function
  | Labels.LEnvironmentNone -> []
  | Labels.LEnvironmentReflective -> [
      "  value env =\n";
      "    E.reflection (t_environment, f_position [x y z], n, m_view_inv);\n"
      ]
  | Labels.LEnvironmentRefractive -> [
      "  value env =\n";
      "    E.refraction (t_environment, f_position [x y z], n, m_view_inv, m.environment);\n"
      ]
  | Labels.LEnvironmentReflectiveRefractive -> [
      "  value env =\n";
      "    E.reflection_refraction (t_environment, f_position [x y z], n, m_view_inv, m.environment);\n"
      ]

let fs_light_values e l s =
  begin match (e, l, s) with
  | (Labels.LEmissiveNone, Labels.LLightDirectional, Labels.LSpecularNone) -> [
      "  value light_term =\n";
      "    DL.diffuse_only (light, n);\n"
      ]
  | (Labels.LEmissiveNone, Labels.LLightDirectional, Labels.LSpecularMapped)
  | (Labels.LEmissiveNone, Labels.LLightDirectional, Labels.LSpecularConstant) -> [
      "  value light_term =\n";
      "    DL.diffuse_specular (light, n, f_position [x y z], m);\n"
      ]
  
  | (Labels.LEmissiveNone, Labels.LLightSpherical, Labels.LSpecularNone) -> [
      "  value light_term =\n";
      "    SL.diffuse_only (light, n, f_position [x y z]);\n"
      ]
  | (Labels.LEmissiveNone, Labels.LLightSpherical, Labels.LSpecularConstant)
  | (Labels.LEmissiveNone, Labels.LLightSpherical, Labels.LSpecularMapped) -> [
      "  value light_term =\n";
      "    SL.diffuse_specular (light, n, f_position [x y z], m);\n"
      ]
  
  | (Labels.LEmissiveMapped, Labels.LLightDirectional, Labels.LSpecularNone)
  | (Labels.LEmissiveConstant, Labels.LLightDirectional, Labels.LSpecularNone) -> [
      "  value light_term =\n";
      "    DL.diffuse_only_emissive (light, n, m);\n"
      ]
  
  | (Labels.LEmissiveConstant, Labels.LLightDirectional, Labels.LSpecularConstant)
  | (Labels.LEmissiveConstant, Labels.LLightDirectional, Labels.LSpecularMapped)
  | (Labels.LEmissiveMapped, Labels.LLightDirectional, Labels.LSpecularConstant)
  | (Labels.LEmissiveMapped, Labels.LLightDirectional, Labels.LSpecularMapped) -> [
      "  value light_term =\n";
      "    DL.diffuse_specular_emissive (light, n, f_position [x y z], m);\n"
      ]
  
  | (Labels.LEmissiveMapped, Labels.LLightSpherical, Labels.LSpecularNone)
  | (Labels.LEmissiveConstant, Labels.LLightSpherical, Labels.LSpecularNone) -> [
      "  value light_term =\n";
      "    SL.diffuse_only_emissive (light, n, f_position [x y z], m);\n"
      ]
  
  | (Labels.LEmissiveConstant, Labels.LLightSpherical, Labels.LSpecularConstant)
  | (Labels.LEmissiveConstant, Labels.LLightSpherical, Labels.LSpecularMapped)
  | (Labels.LEmissiveMapped, Labels.LLightSpherical, Labels.LSpecularConstant)
  | (Labels.LEmissiveMapped, Labels.LLightSpherical, Labels.LSpecularMapped) -> [
      "  value light_term =\n";
      "    SL.diffuse_specular_emissive (light, n, f_position [x y z], m);\n"
      ]
  
  | (Labels.LEmissiveConstant, Labels.LLightSphericalShadowMapped, Labels.LSpecularNone)
  | (Labels.LEmissiveConstant, Labels.LLightSphericalShadowMapped, Labels.LSpecularConstant)
  | (Labels.LEmissiveConstant, Labels.LLightSphericalShadowMapped, Labels.LSpecularMapped)
  | (Labels.LEmissiveConstant, Labels.LLightDirectionalShadowMapped, Labels.LSpecularNone)
  | (Labels.LEmissiveConstant, Labels.LLightDirectionalShadowMapped, Labels.LSpecularConstant)
  | (Labels.LEmissiveConstant, Labels.LLightDirectionalShadowMapped, Labels.LSpecularMapped)
  
  | (Labels.LEmissiveMapped, Labels.LLightSphericalShadowMapped, Labels.LSpecularNone)
  | (Labels.LEmissiveMapped, Labels.LLightSphericalShadowMapped, Labels.LSpecularConstant)
  | (Labels.LEmissiveMapped, Labels.LLightSphericalShadowMapped, Labels.LSpecularMapped)
  | (Labels.LEmissiveMapped, Labels.LLightDirectionalShadowMapped, Labels.LSpecularNone)
  | (Labels.LEmissiveMapped, Labels.LLightDirectionalShadowMapped, Labels.LSpecularConstant)
  | (Labels.LEmissiveMapped, Labels.LLightDirectionalShadowMapped, Labels.LSpecularMapped)
  
  | (Labels.LEmissiveNone, Labels.LLightSphericalShadowMapped, Labels.LSpecularNone)
  | (Labels.LEmissiveNone, Labels.LLightSphericalShadowMapped, Labels.LSpecularConstant)
  | (Labels.LEmissiveNone, Labels.LLightSphericalShadowMapped, Labels.LSpecularMapped)
  | (Labels.LEmissiveNone, Labels.LLightDirectionalShadowMapped, Labels.LSpecularNone)
  | (Labels.LEmissiveNone, Labels.LLightDirectionalShadowMapped, Labels.LSpecularConstant)
  | (Labels.LEmissiveNone, Labels.LLightDirectionalShadowMapped, Labels.LSpecularMapped) -> [
      "  -- XXX: LIGHT TERM SHADOW MAPPING UNIMPLEMENTED\n"
      ]
  end

let fs_albedo a d =
  begin match (a, d) with
  | (Labels.LAlphaOpaque, Labels.LAlbedoColour) -> [
      "  value albedo =\n";
      "    A.opaque (m.diffuse);\n"
      ]
  
  | (Labels.LAlphaTranslucent, Labels.LAlbedoColour) -> [
      "  value albedo =\n";
      "    A.translucent (m.diffuse);\n"
      ]
  
  | (Labels.LAlphaTranslucent, Labels.LAlbedoTextured) -> [
      "  value albedo =\n";
      "    A.textured_translucent (t_albedo, f_uv, m.diffuse);\n"
      ]
  
  | (Labels.LAlphaOpaque, Labels.LAlbedoTextured) -> [
      "  value albedo =\n";
      "    A.textured_opaque (t_albedo, f_uv, m.albedo);\n"
      ]
  end

let fs_surface a e =
  begin match (a, e) with
  | (Labels.LAlphaOpaque, Labels.LEnvironmentNone) -> [
      "  value surface = albedo;\n"
      ]
  | (Labels.LAlphaOpaque, Labels.LEnvironmentReflective)
  | (Labels.LAlphaOpaque, Labels.LEnvironmentRefractive)
  | (Labels.LAlphaOpaque, Labels.LEnvironmentReflectiveRefractive) -> [
      "  value surface =\n";
      "    V3.interpolate (albedo [x y z], env [x y z], m.environment.mix);\n"
      ]
  
  | (Labels.LAlphaTranslucent, Labels.LEnvironmentNone) -> [
      "  value surface = albedo;\n"
      ]
  | (Labels.LAlphaTranslucent, Labels.LEnvironmentReflective)
  | (Labels.LAlphaTranslucent, Labels.LEnvironmentRefractive)
  | (Labels.LAlphaTranslucent, Labels.LEnvironmentReflectiveRefractive) -> [
      "  value surface =\n";
      "    V4.interpolate (albedo, env, m.environment.mix);\n"
      ]
  end

let fs_lit_rgba = [
  "  -- SURFACE NOT IMPLEMENTED\n";
  "  value rgba = new vector_4f (surface [x y z], 1.0);\n"]

let fwd_fragment_shader = function
  | Labels.LUnlit (a, d, m) as p ->
      ["shader fragment fwd_"; Labels.label_code p; "_fragment is\n"] @
      fs_standard_outputs @
      (fs_standard_parameters p) @
      (fs_uv_attributes p) @
      (fs_albedo_parameters d) @
      ["with\n"] @
      ["  -- XXX: EVERYTHING UNIMPLEMENTED\n"] @
      ["  value rgba = new vector_4f (1.0, 0.0, 1.0, 1.0);\n"] @
      ["as\n"] @
      ["  out out_0 = rgba;\n"] @
      ["end;\n"]
  
  | Labels.LLit (a, d, m, n, e, l, s) as p ->
      ["shader fragment fwd_"; Labels.label_code p; "_fragment is\n"] @
      fs_standard_inputs @
      fs_standard_outputs @
      (fs_standard_parameters p) @
      (fs_uv_attributes p) @
      (fs_albedo_parameters d) @
      (fs_normal_attributes n) @
      (fs_normal_parameters n) @
      (fs_environment_parameters e) @
      (fs_emissive_parameters m) @
      (fs_specular_parameters s) @
      ["with\n"] @
      (fs_normal_value n) @
      (fs_material_values m s) @
      (fs_environment_values e) @
      (fs_light_values m l s) @
      (fs_albedo a d) @
      (fs_surface a e) @
      fs_lit_rgba @
      ["as\n"] @
      ["  out out_0 = rgba;\n"] @
      ["end;\n"]

let fwd_program_shader p =
  ["shader program fwd_"; Labels.label_code p; " is\n"] @
  ["  vertex   fwd_"; Labels.label_code p; "_vertex;\n"] @
  ["  fragment fwd_"; Labels.label_code p; "_fragment;\n"] @
  ["end;\n"]

let module_start name = [
  "module "; name; " is\n";
  "\n";
  "import com.io7m.parasol.Matrix3x3f        as M3;\n";
  "import com.io7m.parasol.Matrix4x4f        as M4;\n";
  "import com.io7m.parasol.Vector3f          as V3;\n";
  "import com.io7m.parasol.Vector4f          as V4;\n";
  "import com.io7m.parasol.Sampler2D         as S;\n";
  "import com.io7m.parasol.Float             as F;\n";
  "\n";
  "import com.io7m.renderer.Albedo           as A;\n";
  "import com.io7m.renderer.CubeMap          as CM;\n";
  "import com.io7m.renderer.DirectionalLight as DL;\n";
  "import com.io7m.renderer.Environment      as E;\n";
  "import com.io7m.renderer.Materials        as M;\n";
  "import com.io7m.renderer.Normals          as N;\n";
  "import com.io7m.renderer.SphericalLight   as SL;\n";
  ]

let module_end =
  ["end;\n"]
