#version 140

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
uniform samplerCube t_environment;
uniform mat4 m_view_inv;
in vec3 f_normal;
in vec4 f_position;
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

vec3
p_com_io7m_renderer_SphericalLight_diffuse_color (pt_com_io7m_renderer_SphericalLight_t light, pt_com_io7m_renderer_SphericalLight_directions d)
{
  float factor = max(0.0, dot(d.stl, d.normal));
  vec3 color = ((light.color * light.intensity) * factor);
  {
    return color;
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
p_com_io7m_renderer_SphericalLight_diffuse_only (pt_com_io7m_renderer_SphericalLight_t light, vec3 n, vec3 p)
{
  pt_com_io7m_renderer_SphericalLight_directions d = p_com_io7m_renderer_SphericalLight_directions(light, p, n);
  float a = p_com_io7m_renderer_SphericalLight_attenuation(light, d.distance);
  vec3 c = p_com_io7m_renderer_SphericalLight_diffuse_color(light, d);
  {
    return (c * a);
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
  vec3 n = normalize(f_normal);
  vec3 light_term = p_com_io7m_renderer_SphericalLight_diffuse_only(light, n, f_position.xyz);
  vec4 env = p_com_io7m_renderer_CubeMap_reflection(t_environment, f_position.xyz, f_normal, m_view_inv);
  vec3 albedo = mix(material.diffuse.colour.xyz, env.xyz, material.environment.mix);
  vec4 rgba = vec4((albedo * light_term), 1.0);
  out_0 = rgba;
}
