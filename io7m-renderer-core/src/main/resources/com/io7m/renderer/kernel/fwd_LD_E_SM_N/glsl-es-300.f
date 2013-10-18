#version 300

precision highp float;
precision highp int;
struct pt_com_io7m_renderer_Materials_diffuse {
  vec4 colour;
  float mix;
};
struct pt_com_io7m_renderer_DirectionalLight_directions {
  vec3 ots;
  vec3 normal;
  vec3 stl;
  vec3 reflection;
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
struct pt_com_io7m_renderer_DirectionalLight_t {
  vec3 color;
  vec3 direction;
  float intensity;
};

uniform pt_com_io7m_renderer_DirectionalLight_t light;
uniform pt_com_io7m_renderer_Materials_t material;
uniform samplerCube t_environment;
uniform sampler2D t_normal;
uniform sampler2D t_specular;
uniform mat3 m_normal;
uniform mat4 m_view_inv;
in vec3 f_normal;
in vec3 f_tangent;
in vec3 f_bitangent;
in vec2 f_uv;
in vec4 f_position;
out vec4 out_0;

vec3
p_com_io7m_renderer_DirectionalLight_diffuse_color (pt_com_io7m_renderer_DirectionalLight_t light, pt_com_io7m_renderer_DirectionalLight_directions d)
{
  float factor = max(0.0, dot(d.stl, d.normal));
  vec3 color = ((light.color * light.intensity) * factor);
  {
    return color;
  }
}

vec4
p_com_io7m_renderer_CubeMap_texture (samplerCube s, vec3 u)
{
  return texture(s, vec3(u.x, (0.0 - u.y), (0.0 - u.z)));
}

vec3
p_com_io7m_renderer_Normals_unpack (sampler2D map, vec2 uv)
{
  vec3 rgb = texture(map, uv).xyz;
  {
    return ((rgb * 2.0) + -1.0);
  }
}

vec3
p_com_io7m_renderer_Normals_transform (vec3 m, vec3 t, vec3 b, vec3 n)
{
  mat3 mat = mat3(t, b, n);
  {
    return normalize((mat * m));
  }
}

vec3
p_com_io7m_renderer_Normals_bump (sampler2D t_normal, mat3 m_normal, vec3 n, vec3 t, vec3 b, vec2 uv)
{
  vec3 m = p_com_io7m_renderer_Normals_unpack(t_normal, uv);
  vec3 nn = normalize(n);
  vec3 nt = normalize(t);
  vec3 nb = normalize(b);
  vec3 r = p_com_io7m_renderer_Normals_transform(m, nt, nb, nn);
  {
    return (m_normal * r);
  }
}

pt_com_io7m_renderer_DirectionalLight_directions
p_com_io7m_renderer_DirectionalLight_directions (pt_com_io7m_renderer_DirectionalLight_t light, vec3 p, vec3 n)
{
  vec3 ots = normalize(p);
  {
    vec3 _tmp_1 = ots;
    vec3 _tmp_2 = n;
    vec3 _tmp_3 = normalize((-light.direction));
    vec3 _tmp_4 = reflect(ots, n);
    return pt_com_io7m_renderer_DirectionalLight_directions(_tmp_1, _tmp_2, _tmp_3, _tmp_4);
  }
}

vec3
p_com_io7m_renderer_DirectionalLight_specular_color (pt_com_io7m_renderer_DirectionalLight_t light, pt_com_io7m_renderer_DirectionalLight_directions d, pt_com_io7m_renderer_Materials_specular s)
{
  float factor = pow(max(0.0, dot(d.reflection, d.stl)), s.exponent);
  vec3 color = ((light.color * light.intensity) * factor);
  {
    return (color * s.intensity);
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

vec3
p_com_io7m_renderer_DirectionalLight_diffuse_specular (pt_com_io7m_renderer_DirectionalLight_t light, vec3 n, vec3 p, pt_com_io7m_renderer_Materials_t material)
{
  pt_com_io7m_renderer_DirectionalLight_directions d = p_com_io7m_renderer_DirectionalLight_directions(light, p, n);
  vec3 dc = p_com_io7m_renderer_DirectionalLight_diffuse_color(light, d);
  vec3 sc = p_com_io7m_renderer_DirectionalLight_specular_color(light, d, material.specular);
  {
    return (dc + sc);
  }
}

pt_com_io7m_renderer_Materials_specular
_tmp_10 (void)
{
  float _tmp_8 = material.specular.exponent;
  float _tmp_9 = texture(t_specular, f_uv).x;
  return pt_com_io7m_renderer_Materials_specular(_tmp_8, _tmp_9);
}

pt_com_io7m_renderer_Materials_t
_tmp_11 (void)
{
  pt_com_io7m_renderer_Materials_diffuse _tmp_5 = material.diffuse;
  pt_com_io7m_renderer_Materials_environment _tmp_6 = material.environment;
  pt_com_io7m_renderer_Materials_specular _tmp_7 = _tmp_10();
  return pt_com_io7m_renderer_Materials_t(_tmp_5, _tmp_7, _tmp_6);
}


void
main (void)
{
  vec3 n = p_com_io7m_renderer_Normals_bump(t_normal, m_normal, normalize(f_normal), normalize(f_tangent), normalize(f_bitangent), f_uv);
  pt_com_io7m_renderer_Materials_t m = _tmp_11();
  vec4 env = p_com_io7m_renderer_CubeMap_reflection(t_environment, f_position.xyz, n, m_view_inv);
  vec3 light_term = p_com_io7m_renderer_DirectionalLight_diffuse_specular(light, n, f_position.xyz, m);
  vec3 albedo = mix(m.diffuse.colour.xyz, env.xyz, (m.environment.mix * m.specular.intensity));
  vec4 rgba = vec4((albedo * light_term), 1.0);
  out_0 = rgba;
}