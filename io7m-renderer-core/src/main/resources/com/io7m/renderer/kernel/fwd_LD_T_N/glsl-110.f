#version 110

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
uniform sampler2D t_diffuse_0;
uniform sampler2D t_normal;
uniform mat3 m_normal;
varying vec3 f_normal;
varying vec3 f_tangent;
varying vec3 f_bitangent;
varying vec2 f_uv;


vec3
p_com_io7m_renderer_DirectionalLight_diffuse_color (pt_com_io7m_renderer_DirectionalLight_t light, pt_com_io7m_renderer_DirectionalLight_directions d)
{
  float factor = max(0.0, dot(d.stl, d.normal));
  vec3 color = ((light.color * light.intensity) * factor);
  {
    return color;
  }
}

vec3
p_com_io7m_renderer_Normals_unpack (sampler2D map, vec2 uv)
{
  vec3 rgb = texture2D(map, uv).xyz;
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
p_com_io7m_renderer_DirectionalLight_diffuse_only (pt_com_io7m_renderer_DirectionalLight_t light, vec3 n)
{
  pt_com_io7m_renderer_DirectionalLight_directions d = p_com_io7m_renderer_DirectionalLight_directions(light, vec3(0.0, 0.0, 0.0), n);
  vec3 c = p_com_io7m_renderer_DirectionalLight_diffuse_color(light, d);
  {
    return c;
  }
}


void
main (void)
{
  vec3 n = p_com_io7m_renderer_Normals_bump(t_normal, m_normal, normalize(f_normal), normalize(f_tangent), normalize(f_bitangent), f_uv);
  vec3 light_term = p_com_io7m_renderer_DirectionalLight_diffuse_only(light, n);
  vec3 albedo = texture2D(t_diffuse_0, f_uv).xyz;
  vec4 rgba = vec4((albedo * light_term), 1.0);
  gl_FragColor = rgba;
}
