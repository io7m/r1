#version 120

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
uniform sampler2D t_normal;
uniform sampler2D t_diffuse_0;
uniform mat3 m_normal;
uniform mat4 m_view_inv;
varying vec3 f_normal;
varying vec3 f_tangent;
varying vec3 f_bitangent;
varying vec2 f_uv;
varying vec4 f_position;


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
  return textureCube(s, vec3(u.x, (0.0 - u.y), (0.0 - u.z)));
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

vec4
p_com_io7m_renderer_Diffuse_diffuse (sampler2D t, vec2 u, pt_com_io7m_renderer_Materials_diffuse m)
{
  vec4 tc = texture2D(t, u);
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


void
main (void)
{
  vec3 n = p_com_io7m_renderer_Normals_bump(t_normal, m_normal, normalize(f_normal), normalize(f_tangent), normalize(f_bitangent), f_uv);
  vec3 light_term = p_com_io7m_renderer_SphericalLight_diffuse_specular(light, n, f_position.xyz, material);
  vec4 env = p_com_io7m_renderer_CubeMap_reflection(t_environment, f_position.xyz, n, m_view_inv);
  vec4 diff = p_com_io7m_renderer_Diffuse_diffuse(t_diffuse_0, f_uv, material.diffuse);
  vec3 albedo = mix(diff.xyz, env.xyz, material.environment.mix);
  vec4 rgba = vec4((albedo * light_term), 1.0);
  gl_FragColor = rgba;
}
