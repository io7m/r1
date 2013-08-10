#version 140

uniform sampler2D t_diffuse_0;
uniform vec3      l_color;
uniform vec3      l_position;
uniform float     l_intensity;
uniform float     l_radius;
uniform float     l_falloff;

in vec2 f_uv;
in vec3 f_normal;
in vec4 f_position;

out vec4 out_frag_color;

void
main (void)
{
  vec3 N  = normalize (f_normal);
  vec3 L  = f_position.xyz - l_position;
  float D = length (L);
  L = normalize (-L);

  float l_diffuse_factor = max (0, dot (L, N));
  vec3 l_diffuse         = l_color * l_intensity * l_diffuse_factor;

  vec3 surface = texture2D (t_diffuse_0, f_uv).rgb;
  
  float at_lin = -D * (1 / l_radius) + 1.0;
  float at_exp = clamp (pow (at_lin, l_falloff), 0.0, 1.0);

  vec3 light = l_diffuse;

  out_frag_color = vec4 (surface * (light * at_exp), 1.0);
}
