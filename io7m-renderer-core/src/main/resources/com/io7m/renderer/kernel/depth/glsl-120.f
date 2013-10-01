#version 120





void
main (void)
{
  vec4 rgba = vec4(gl_FragCoord.zzz, 1.0);
  gl_FragColor = rgba;
}
