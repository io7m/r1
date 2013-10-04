#version 400

struct pt_com_io7m_renderer_SphericalLight_t {
  vec3 color;
  vec3 position;
  float intensity;
  float radius;
  float falloff;
};
struct pt_com_io7m_renderer_SphericalLight_directions {
  vec3 otl;
  vec3 normal;
  vec3 stl;
  float distance;
  vec3 reflection;
};

uniform pt_com_io7m_renderer_SphericalLight_t light;
uniform vec4 f_diffuse;
uniform sampler2D t_normal;
uniform mat3 m_normal;
in vec3 f_normal;
in vec3 f_tangent;
in vec3 f_bitangent;
in vec2 f_uv;
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
  vec3 otl = normalize(p);
  {
    vec3 _tmp_1 = otl;
    vec3 _tmp_2 = n;
    vec3 _tmp_3 = normalize((-position_diff));
    float _tmp_4 = length(position_diff);
    vec3 _tmp_5 = reflect(otl, n);
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


void
main (void)
{
  vec3 n = p_com_io7m_renderer_Normals_bump(t_normal, m_normal, normalize(f_normal), normalize(f_tangent), normalize(f_bitangent), f_uv);
  vec3 light_term = p_com_io7m_renderer_SphericalLight_diffuse_only(light, n, f_position.xyz);
  vec3 albedo = f_diffuse.xyz;
  vec4 rgba = vec4((albedo * light_term), 1.0);
  out_0 = rgba;
}
