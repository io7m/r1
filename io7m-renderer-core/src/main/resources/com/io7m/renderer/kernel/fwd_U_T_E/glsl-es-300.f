#version 300

precision highp float;
precision highp int;
struct pt_com_io7m_renderer_Materials_diffuse {
  vec4 colour;
  float mix;
};
struct pt_com_io7m_renderer_Materials_specular {
  float exponent;
  float intensity;
};
struct pt_com_io7m_renderer_Materials_environment {
  float mix;
};
struct pt_com_io7m_renderer_Materials_t {
  pt_com_io7m_renderer_Materials_diffuse diffuse;
  pt_com_io7m_renderer_Materials_specular specular;
  pt_com_io7m_renderer_Materials_environment environment;
};

uniform mat4 m_view_inv;
uniform pt_com_io7m_renderer_Materials_t material;
uniform samplerCube t_environment;
uniform sampler2D t_diffuse_0;
in vec4 f_position;
in vec3 f_normal;
in vec2 f_uv;
out vec4 out_0;

vec4
p_com_io7m_renderer_CubeMap_texture (samplerCube s, vec3 u)
{
  return texture(s, vec3(u.x, (0.0 - u.y), (0.0 - u.z)));
}

vec4
p_com_io7m_renderer_Diffuse_diffuse (sampler2D t, vec2 u, pt_com_io7m_renderer_Materials_diffuse m)
{
  vec4 tc = texture(t, u);
  vec3 c = mix(m.colour.xyz, tc.xyz, m.mix);
  {
    return vec4(c, 1.0);
  }
}

vec4
p_com_io7m_renderer_CubeMap_reflection (samplerCube t, vec3 v, vec3 n, mat4 m)
{
  vec3 vn = normalize(v.xyz);
  vec3 nn = normalize(n);
  vec3 r = reflect(vn, nn);
  vec4 ri = (m * vec4(r, 0.0));
  {
    return p_com_io7m_renderer_CubeMap_texture(t, ri.xyz);
  }
}


void
main (void)
{
  vec4 diff = p_com_io7m_renderer_Diffuse_diffuse(t_diffuse_0, f_uv, material.diffuse);
  vec4 env = p_com_io7m_renderer_CubeMap_reflection(t_environment, f_position.xyz, f_normal, m_view_inv);
  vec3 albedo = mix(diff.xyz, env.xyz, material.environment.mix);
  vec4 rgba = vec4(albedo, 1.0);
  out_0 = rgba;
}
