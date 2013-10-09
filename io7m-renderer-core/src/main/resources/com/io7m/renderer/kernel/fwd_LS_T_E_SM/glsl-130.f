#version 130

struct pt_com_io7m_renderer_SphericalLight_t {
  vec3 color;
  vec3 position;
  float intensity;
  float radius;
  float falloff;
};
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
struct pt_com_io7m_renderer_SphericalLight_directions {
  vec3 ots;
  vec3 normal;
  vec3 stl;
  float distance;
  vec3 reflection;
};

uniform pt_com_io7m_renderer_Materials_t material;
uniform pt_com_io7m_renderer_SphericalLight_t light;
uniform mat4 m_view_inv;
uniform sampler2D t_specular;
uniform sampler2D t_diffuse_0;
uniform samplerCube t_environment;
in vec3 f_normal;
in vec4 f_position;
in vec2 f_uv;
out vec4 out_0;

float
p_com_io7m_renderer_SphericalLight_attenuation (pt_com_io7m_renderer_SphericalLight_t light, float distance)
{
  float nd = (0.0 - distance);
  float inv_radius = (1.0 / light.radius);
  float linear = ((nd * inv_radius) + 1.0);
  float exponential = clamp(pow(linear, light.falloff), 0.0, 1.0);
  {
    return exponential;
  }
}

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

vec3
p_com_io7m_renderer_SphericalLight_specular_color (pt_com_io7m_renderer_SphericalLight_t light, pt_com_io7m_renderer_SphericalLight_directions d, pt_com_io7m_renderer_Materials_specular s)
{
  float factor = pow(max(0.0, dot(d.reflection, d.stl)), s.exponent);
  vec3 color = ((light.color * light.intensity) * factor);
  {
    return (color * s.intensity);
  }
}

pt_com_io7m_renderer_SphericalLight_directions
p_com_io7m_renderer_SphericalLight_directions (pt_com_io7m_renderer_SphericalLight_t light, vec3 p, vec3 n)
{
  vec3 position_diff = (p - light.position);
  vec3 ots = normalize(p);
  {
    vec3 _tmp_1 = ots;
    vec3 _tmp_2 = n;
    vec3 _tmp_3 = normalize((-position_diff));
    float _tmp_4 = length(position_diff);
    vec3 _tmp_5 = reflect(ots, n);
    return pt_com_io7m_renderer_SphericalLight_directions(_tmp_1, _tmp_2, _tmp_3, _tmp_4, _tmp_5);
  }
}

vec3
p_com_io7m_renderer_SphericalLight_diffuse_color (pt_com_io7m_renderer_SphericalLight_t light, pt_com_io7m_renderer_SphericalLight_directions d)
{
  float factor = max(0.0, dot(d.stl, d.normal));
  vec3 color = ((light.color * light.intensity) * factor);
  {
    return color;
  }
}

vec3
p_com_io7m_renderer_SphericalLight_diffuse_specular (pt_com_io7m_renderer_SphericalLight_t light, vec3 n, vec3 p, pt_com_io7m_renderer_Materials_t material)
{
  pt_com_io7m_renderer_SphericalLight_directions d = p_com_io7m_renderer_SphericalLight_directions(light, p, n);
  float a = p_com_io7m_renderer_SphericalLight_attenuation(light, d.distance);
  vec3 dc = p_com_io7m_renderer_SphericalLight_diffuse_color(light, d);
  vec3 sc = p_com_io7m_renderer_SphericalLight_specular_color(light, d, material.specular);
  {
    return ((dc + sc) * a);
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

pt_com_io7m_renderer_Materials_specular
_tmp_11 (void)
{
  float _tmp_9 = material.specular.exponent;
  float _tmp_10 = texture(t_specular, f_uv).x;
  return pt_com_io7m_renderer_Materials_specular(_tmp_9, _tmp_10);
}

pt_com_io7m_renderer_Materials_t
_tmp_12 (void)
{
  pt_com_io7m_renderer_Materials_diffuse _tmp_6 = material.diffuse;
  pt_com_io7m_renderer_Materials_environment _tmp_7 = material.environment;
  pt_com_io7m_renderer_Materials_specular _tmp_8 = _tmp_11();
  return pt_com_io7m_renderer_Materials_t(_tmp_6, _tmp_8, _tmp_7);
}


void
main (void)
{
  vec3 n = normalize(f_normal);
  pt_com_io7m_renderer_Materials_t m = _tmp_12();
  vec3 light_term = p_com_io7m_renderer_SphericalLight_diffuse_specular(light, n, f_position.xyz, m);
  vec4 diff = p_com_io7m_renderer_Diffuse_diffuse(t_diffuse_0, f_uv, m.diffuse);
  vec4 env = p_com_io7m_renderer_CubeMap_reflection(t_environment, f_position.xyz, f_normal, m_view_inv);
  vec3 albedo = mix(diff.xyz, env.xyz, (m.specular.intensity * m.environment.mix));
  vec4 rgba = vec4((albedo * light_term), 1.0);
  out_0 = rgba;
}
