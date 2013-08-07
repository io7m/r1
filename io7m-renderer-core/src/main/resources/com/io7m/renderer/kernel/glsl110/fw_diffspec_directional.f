#version 110

uniform sampler2D t_diffuse_0;

uniform vec3  l_color;
uniform float l_intensity;
uniform vec3  l_direction;

varying vec4 f_position;
varying vec2 f_uv;
varying vec3 f_normal;

void
main (void)
{
  vec4 V = normalize (f_position);
  vec4 N = normalize (vec4 (f_normal, 0.0));
  vec4 L = normalize (vec4 (-l_direction, 0.0));
  vec4 R = reflect (V, N);

  float l_diffuse_factor = max (0, dot (L, N));
  vec4 l_diffuse_color   = vec4 (l_color, 1.0) * l_intensity * l_diffuse_factor;

  float l_spec_factor = pow (max (dot (R, L), 0.0), 64);
  vec4 l_spec_color   = vec4 (l_color, 1.0) * l_intensity * l_spec_factor;

  gl_FragColor = texture2D(t_diffuse_0, f_uv) * (l_diffuse_color + l_spec_color);
}
