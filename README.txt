REQUIREMENTS
------------------------------------------------------------------------

In no particular order...

R.A) Projective texturing

  As in textured lighting; an example would be an overhead projector
  projecting a slide onto a wall.

R.B) Environment mapping

  Reflective objects (or some cheap approximation of them). Ideally
  the amount of reflection would be controllable on a per-material
  basis.

R.C) Emissive objects with glow maps

  It's not clear whether entire objects can be made emissive, or
  if each material has an associated emissive map so that only
  parts of an object appear to be emitting light.

  Glow mapping is added as a postprocessing effect by blending
  a blurred copy of only the emissive parts of a scene over the
  top of the rendered scene (prior to other effects).

R.D) Fog

R.E) Water

  Underwater effects. Surface effects with reflections.

R.F) Various fullscreen effects.

  Brightness/contrast. Hue/saturation/value. Colorize. Gaussian blur.

R.G) Shadows.

  Perhaps only hard-edged shadows, unless soft shadows are easy to
  implement.

R.L) Lighting.

  Directional
  Cone
  Point

DESIGN QUESTIONS
------------------------------------------------------------------------

DQ.1)

  What information needs to be placed into materials?

DQ.2)

  Is there a single gbuffer format that can store all the required
  information to achieve all of the required effects in deferred
  renderers?

DQ.3)

  What's actually required for materials in terms of control? For
  example, do materials always have diffuse textures? Are there going
  to be untextured, flat-colour objects anywhere? What kind of control
  is needed for specular lighting? Is a specular intensity map enough?

