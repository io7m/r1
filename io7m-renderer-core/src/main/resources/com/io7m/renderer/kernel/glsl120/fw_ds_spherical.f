#version 120

uniform sampler2D t_diffuse_0;
uniform vec3      l_color;
uniform vec3      l_position;
uniform float     l_intensity;
uniform float     l_radius;
uniform float     l_falloff;

varying vec2 f_uv;
varying vec3 f_normal;
varying vec4 f_position;

void
main (void)
{
  vec3 V  = normalize (f_position.xyz);
  vec3 N  = normalize (f_normal);
  vec3 L  = f_position.xyz - l_position;
  float D = length (L);
  L = normalize (-L);
  vec3 R = reflect (V, N);

  float l_diffuse_factor = max (0, dot (L, N));
  vec3 l_diffuse         = l_color * l_intensity * l_diffuse_factor;

  float l_spec_factor = pow (max (dot (R, L), 0.0), 64);
  vec3 l_specular     = l_color * l_intensity * l_spec_factor;

  vec3 surface = texture2D (t_diffuse_0, f_uv).rgb;
  
  float at_lin = -D * (1 / l_radius) + 1.0;
  float at_exp = clamp (pow (at_lin, l_falloff), 0.0, 1.0);

  vec3 light = l_diffuse + l_specular;

  gl_FragColor = vec4 (surface * (light * at_exp), 1.0);
}
