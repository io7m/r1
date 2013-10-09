/*
 * Copyright © 2013 <code@io7m.com> http://io7m.com
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.renderer.kernel;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnimplementedCodeException;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Indeterminate;
import com.io7m.jaux.functional.Indeterminate.Failure;
import com.io7m.jaux.functional.Indeterminate.Success;
import com.io7m.jcanephora.BlendFunction;
import com.io7m.jcanephora.DepthFunction;
import com.io7m.jcanephora.FaceSelection;
import com.io7m.jcanephora.FaceWindingOrder;
import com.io7m.jcanephora.Framebuffer;
import com.io7m.jcanephora.JCGLCompileException;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jcanephora.JCGLInterfaceCommon;
import com.io7m.jcanephora.JCGLUnsupportedException;
import com.io7m.jlog.Log;
import com.io7m.jtensors.VectorM2I;
import com.io7m.jtensors.VectorM4F;
import com.io7m.jtensors.VectorReadable4F;
import com.io7m.jvvfs.FSCapabilityRead;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.renderer.kernel.KLight.KDirectional;
import com.io7m.renderer.kernel.KLight.KSphere;
import com.io7m.renderer.kernel.programs.KSPDepth;
import com.io7m.renderer.kernel.programs.KSPForwardLitDirectionalPrograms;
import com.io7m.renderer.kernel.programs.KSPForwardLitSphericalPrograms;
import com.io7m.renderer.kernel.programs.KSPForwardUnlitPrograms;

public final class KRendererForward implements KRenderer
{
  private static @Nonnull
    Indeterminate<KSPForwardLitDirectionalPrograms, KSPForwardUnlitPrograms>
    decideLitDirectionalShader(
      final @Nonnull KMeshInstance m)
  {
    final KRenderingCapabilities caps = m.getCapabilities();

    switch (caps.getTexture()) {
      case TEXTURE_CAP_DIFFUSE:
      {
        switch (caps.getNormal()) {
          case NORMAL_CAP_MAPPED:
          {
            switch (caps.getSpecular()) {
              case SPECULAR_CAP_CONSTANT:
              {
                switch (caps.getEnvironment()) {
                  case ENVIRONMENT_MAPPED:
                  {
                    return new Success<KSPForwardLitDirectionalPrograms, KSPForwardUnlitPrograms>(
                      KSPForwardLitDirectionalPrograms.KSP_FORWARD_LIT_DIRECTIONAL_TEXTURED_ENVIRONMENTMAPPED_SPECULAR_NORMALMAPPED);
                  }
                  case ENVIRONMENT_NONE:
                  {
                    return new Success<KSPForwardLitDirectionalPrograms, KSPForwardUnlitPrograms>(
                      KSPForwardLitDirectionalPrograms.KSP_FORWARD_LIT_DIRECTIONAL_TEXTURED_SPECULAR_NORMALMAPPED);
                  }
                }

                throw new UnreachableCodeException();
              }
              case SPECULAR_CAP_MAPPED:
              {
                switch (caps.getEnvironment()) {
                  case ENVIRONMENT_MAPPED:
                  {
                    return new Success<KSPForwardLitDirectionalPrograms, KSPForwardUnlitPrograms>(
                      KSPForwardLitDirectionalPrograms.KSP_FORWARD_LIT_DIRECTIONAL_TEXTURED_ENVIRONMENTMAPPED_SPECULARMAPPED_NORMALMAPPED);
                  }
                  case ENVIRONMENT_NONE:
                  {
                    return new Success<KSPForwardLitDirectionalPrograms, KSPForwardUnlitPrograms>(
                      KSPForwardLitDirectionalPrograms.KSP_FORWARD_LIT_DIRECTIONAL_TEXTURED_SPECULARMAPPED_NORMALMAPPED);
                  }
                }

                throw new UnreachableCodeException();
              }
              case SPECULAR_CAP_NONE:
              {
                switch (caps.getEnvironment()) {
                  case ENVIRONMENT_MAPPED:
                  {
                    return new Success<KSPForwardLitDirectionalPrograms, KSPForwardUnlitPrograms>(
                      KSPForwardLitDirectionalPrograms.KSP_FORWARD_LIT_DIRECTIONAL_TEXTURED_ENVIRONMENTMAPPED_NORMALMAPPED);
                  }
                  case ENVIRONMENT_NONE:
                  {
                    return new Success<KSPForwardLitDirectionalPrograms, KSPForwardUnlitPrograms>(
                      KSPForwardLitDirectionalPrograms.KSP_FORWARD_LIT_DIRECTIONAL_TEXTURED_NORMALMAPPED);
                  }
                }
              }
            }

            throw new UnreachableCodeException();
          }

          case NORMAL_CAP_NONE:
          {
            return new Failure<KSPForwardLitDirectionalPrograms, KSPForwardUnlitPrograms>(
              KSPForwardUnlitPrograms.KSP_FORWARD_UNLIT_TEXTURED);
          }

          case NORMAL_CAP_VERTEX:
          {
            switch (caps.getSpecular()) {
              case SPECULAR_CAP_CONSTANT:
              {
                switch (caps.getEnvironment()) {
                  case ENVIRONMENT_MAPPED:
                  {
                    return new Success<KSPForwardLitDirectionalPrograms, KSPForwardUnlitPrograms>(
                      KSPForwardLitDirectionalPrograms.KSP_FORWARD_LIT_DIRECTIONAL_TEXTURED_ENVIRONMENTMAPPED_SPECULAR);
                  }
                  case ENVIRONMENT_NONE:
                  {
                    return new Success<KSPForwardLitDirectionalPrograms, KSPForwardUnlitPrograms>(
                      KSPForwardLitDirectionalPrograms.KSP_FORWARD_LIT_DIRECTIONAL_TEXTURED_SPECULAR);
                  }
                }

                throw new UnreachableCodeException();
              }
              case SPECULAR_CAP_MAPPED:
              {
                switch (caps.getEnvironment()) {
                  case ENVIRONMENT_MAPPED:
                  {
                    return new Success<KSPForwardLitDirectionalPrograms, KSPForwardUnlitPrograms>(
                      KSPForwardLitDirectionalPrograms.KSP_FORWARD_LIT_DIRECTIONAL_TEXTURED_ENVIRONMENTMAPPED_SPECULARMAPPED);
                  }
                  case ENVIRONMENT_NONE:
                  {
                    return new Success<KSPForwardLitDirectionalPrograms, KSPForwardUnlitPrograms>(
                      KSPForwardLitDirectionalPrograms.KSP_FORWARD_LIT_DIRECTIONAL_TEXTURED_SPECULARMAPPED);
                  }
                }

                throw new UnreachableCodeException();
              }
              case SPECULAR_CAP_NONE:
              {
                switch (caps.getEnvironment()) {
                  case ENVIRONMENT_MAPPED:
                  {
                    return new Success<KSPForwardLitDirectionalPrograms, KSPForwardUnlitPrograms>(
                      KSPForwardLitDirectionalPrograms.KSP_FORWARD_LIT_DIRECTIONAL_TEXTURED_ENVIRONMENTMAPPED);
                  }
                  case ENVIRONMENT_NONE:
                  {
                    return new Success<KSPForwardLitDirectionalPrograms, KSPForwardUnlitPrograms>(
                      KSPForwardLitDirectionalPrograms.KSP_FORWARD_LIT_DIRECTIONAL_TEXTURED);
                  }
                }
              }
            }

            throw new UnreachableCodeException();
          }
        }

        throw new UnreachableCodeException();
      }

      case TEXTURE_CAP_NONE:
      {
        switch (caps.getNormal()) {
          case NORMAL_CAP_MAPPED:
          {
            switch (caps.getSpecular()) {
              case SPECULAR_CAP_CONSTANT:
              {
                switch (caps.getEnvironment()) {
                  case ENVIRONMENT_MAPPED:
                  {
                    return new Success<KSPForwardLitDirectionalPrograms, KSPForwardUnlitPrograms>(
                      KSPForwardLitDirectionalPrograms.KSP_FORWARD_LIT_DIRECTIONAL_ENVIRONMENTMAPPED_SPECULAR_NORMALMAPPED);
                  }
                  case ENVIRONMENT_NONE:
                  {
                    return new Success<KSPForwardLitDirectionalPrograms, KSPForwardUnlitPrograms>(
                      KSPForwardLitDirectionalPrograms.KSP_FORWARD_LIT_DIRECTIONAL_SPECULAR_NORMALMAPPED);
                  }
                }

                throw new UnreachableCodeException();
              }
              case SPECULAR_CAP_MAPPED:
              {
                switch (caps.getEnvironment()) {
                  case ENVIRONMENT_MAPPED:
                  {
                    return new Success<KSPForwardLitDirectionalPrograms, KSPForwardUnlitPrograms>(
                      KSPForwardLitDirectionalPrograms.KSP_FORWARD_LIT_DIRECTIONAL_ENVIRONMENTMAPPED_SPECULARMAPPED_NORMALMAPPED);
                  }
                  case ENVIRONMENT_NONE:
                  {
                    return new Success<KSPForwardLitDirectionalPrograms, KSPForwardUnlitPrograms>(
                      KSPForwardLitDirectionalPrograms.KSP_FORWARD_LIT_DIRECTIONAL_SPECULARMAPPED_NORMALMAPPED);
                  }
                }

                throw new UnreachableCodeException();
              }
              case SPECULAR_CAP_NONE:
              {
                switch (caps.getEnvironment()) {
                  case ENVIRONMENT_MAPPED:
                  {
                    return new Success<KSPForwardLitDirectionalPrograms, KSPForwardUnlitPrograms>(
                      KSPForwardLitDirectionalPrograms.KSP_FORWARD_LIT_DIRECTIONAL_ENVIRONMENTMAPPED_NORMALMAPPED);
                  }
                  case ENVIRONMENT_NONE:
                  {
                    return new Success<KSPForwardLitDirectionalPrograms, KSPForwardUnlitPrograms>(
                      KSPForwardLitDirectionalPrograms.KSP_FORWARD_LIT_DIRECTIONAL_NORMALMAPPED);
                  }
                }
              }
            }

            throw new UnreachableCodeException();
          }
          case NORMAL_CAP_NONE:
          {
            return new Failure<KSPForwardLitDirectionalPrograms, KSPForwardUnlitPrograms>(
              KSPForwardUnlitPrograms.KSP_FORWARD_UNLIT);
          }
          case NORMAL_CAP_VERTEX:
          {
            switch (caps.getSpecular()) {
              case SPECULAR_CAP_CONSTANT:
              {
                switch (caps.getEnvironment()) {
                  case ENVIRONMENT_MAPPED:
                  {
                    return new Success<KSPForwardLitDirectionalPrograms, KSPForwardUnlitPrograms>(
                      KSPForwardLitDirectionalPrograms.KSP_FORWARD_LIT_DIRECTIONAL_ENVIRONMENTMAPPED_SPECULAR);
                  }
                  case ENVIRONMENT_NONE:
                  {
                    return new Success<KSPForwardLitDirectionalPrograms, KSPForwardUnlitPrograms>(
                      KSPForwardLitDirectionalPrograms.KSP_FORWARD_LIT_DIRECTIONAL_SPECULAR);
                  }
                }

                throw new UnreachableCodeException();
              }
              case SPECULAR_CAP_MAPPED:
              {
                switch (caps.getEnvironment()) {
                  case ENVIRONMENT_MAPPED:
                  {
                    return new Success<KSPForwardLitDirectionalPrograms, KSPForwardUnlitPrograms>(
                      KSPForwardLitDirectionalPrograms.KSP_FORWARD_LIT_DIRECTIONAL_ENVIRONMENTMAPPED_SPECULARMAPPED);
                  }
                  case ENVIRONMENT_NONE:
                  {
                    return new Success<KSPForwardLitDirectionalPrograms, KSPForwardUnlitPrograms>(
                      KSPForwardLitDirectionalPrograms.KSP_FORWARD_LIT_DIRECTIONAL_SPECULARMAPPED);
                  }
                }

                throw new UnreachableCodeException();
              }
              case SPECULAR_CAP_NONE:
              {
                switch (caps.getEnvironment()) {
                  case ENVIRONMENT_MAPPED:
                  {
                    return new Success<KSPForwardLitDirectionalPrograms, KSPForwardUnlitPrograms>(
                      KSPForwardLitDirectionalPrograms.KSP_FORWARD_LIT_DIRECTIONAL_ENVIRONMENTMAPPED);
                  }
                  case ENVIRONMENT_NONE:
                  {
                    return new Success<KSPForwardLitDirectionalPrograms, KSPForwardUnlitPrograms>(
                      KSPForwardLitDirectionalPrograms.KSP_FORWARD_LIT_DIRECTIONAL);
                  }
                }
              }
            }

            throw new UnreachableCodeException();
          }
        }

        throw new UnreachableCodeException();
      }
    }

    throw new UnreachableCodeException();
  }

  private static @Nonnull
    Indeterminate<KSPForwardLitSphericalPrograms, KSPForwardUnlitPrograms>
    decideLitSphericalShader(
      final @Nonnull KMeshInstance m)
  {
    final KRenderingCapabilities caps = m.getCapabilities();

    switch (caps.getTexture()) {
      case TEXTURE_CAP_DIFFUSE:
      {
        switch (caps.getNormal()) {
          case NORMAL_CAP_MAPPED:
          {
            switch (caps.getSpecular()) {
              case SPECULAR_CAP_CONSTANT:
              {
                switch (caps.getEnvironment()) {
                  case ENVIRONMENT_MAPPED:
                  {
                    return new Success<KSPForwardLitSphericalPrograms, KSPForwardUnlitPrograms>(
                      KSPForwardLitSphericalPrograms.KSP_FORWARD_LIT_SPHERICAL_TEXTURED_ENVIRONMENTMAPPED_SPECULAR_NORMALMAPPED);
                  }
                  case ENVIRONMENT_NONE:
                  {
                    return new Success<KSPForwardLitSphericalPrograms, KSPForwardUnlitPrograms>(
                      KSPForwardLitSphericalPrograms.KSP_FORWARD_LIT_SPHERICAL_TEXTURED_SPECULAR_NORMALMAPPED);
                  }
                }

                throw new UnreachableCodeException();
              }
              case SPECULAR_CAP_MAPPED:
              {
                switch (caps.getEnvironment()) {
                  case ENVIRONMENT_MAPPED:
                  {
                    return new Success<KSPForwardLitSphericalPrograms, KSPForwardUnlitPrograms>(
                      KSPForwardLitSphericalPrograms.KSP_FORWARD_LIT_SPHERICAL_TEXTURED_ENVIRONMENTMAPPED_SPECULARMAPPED_NORMALMAPPED);
                  }
                  case ENVIRONMENT_NONE:
                  {
                    return new Success<KSPForwardLitSphericalPrograms, KSPForwardUnlitPrograms>(
                      KSPForwardLitSphericalPrograms.KSP_FORWARD_LIT_SPHERICAL_TEXTURED_SPECULARMAPPED_NORMALMAPPED);
                  }
                }

                throw new UnreachableCodeException();
              }
              case SPECULAR_CAP_NONE:
              {
                switch (caps.getEnvironment()) {
                  case ENVIRONMENT_MAPPED:
                  {
                    return new Success<KSPForwardLitSphericalPrograms, KSPForwardUnlitPrograms>(
                      KSPForwardLitSphericalPrograms.KSP_FORWARD_LIT_SPHERICAL_TEXTURED_ENVIRONMENTMAPPED_NORMALMAPPED);
                  }
                  case ENVIRONMENT_NONE:
                  {
                    return new Success<KSPForwardLitSphericalPrograms, KSPForwardUnlitPrograms>(
                      KSPForwardLitSphericalPrograms.KSP_FORWARD_LIT_SPHERICAL_TEXTURED_NORMALMAPPED);
                  }
                }
              }
            }

            throw new UnreachableCodeException();
          }

          case NORMAL_CAP_NONE:
          {
            return new Failure<KSPForwardLitSphericalPrograms, KSPForwardUnlitPrograms>(
              KSPForwardUnlitPrograms.KSP_FORWARD_UNLIT_TEXTURED);
          }

          case NORMAL_CAP_VERTEX:
          {
            switch (caps.getSpecular()) {
              case SPECULAR_CAP_CONSTANT:
              {
                switch (caps.getEnvironment()) {
                  case ENVIRONMENT_MAPPED:
                  {
                    return new Success<KSPForwardLitSphericalPrograms, KSPForwardUnlitPrograms>(
                      KSPForwardLitSphericalPrograms.KSP_FORWARD_LIT_SPHERICAL_TEXTURED_ENVIRONMENTMAPPED_SPECULAR);
                  }
                  case ENVIRONMENT_NONE:
                  {
                    return new Success<KSPForwardLitSphericalPrograms, KSPForwardUnlitPrograms>(
                      KSPForwardLitSphericalPrograms.KSP_FORWARD_LIT_SPHERICAL_TEXTURED_SPECULAR);
                  }
                }

                throw new UnreachableCodeException();
              }
              case SPECULAR_CAP_MAPPED:
              {
                switch (caps.getEnvironment()) {
                  case ENVIRONMENT_MAPPED:
                  {
                    return new Success<KSPForwardLitSphericalPrograms, KSPForwardUnlitPrograms>(
                      KSPForwardLitSphericalPrograms.KSP_FORWARD_LIT_SPHERICAL_TEXTURED_ENVIRONMENTMAPPED_SPECULARMAPPED);
                  }
                  case ENVIRONMENT_NONE:
                  {
                    return new Success<KSPForwardLitSphericalPrograms, KSPForwardUnlitPrograms>(
                      KSPForwardLitSphericalPrograms.KSP_FORWARD_LIT_SPHERICAL_TEXTURED_SPECULARMAPPED);
                  }
                }

                throw new UnreachableCodeException();
              }
              case SPECULAR_CAP_NONE:
              {
                switch (caps.getEnvironment()) {
                  case ENVIRONMENT_MAPPED:
                  {
                    return new Success<KSPForwardLitSphericalPrograms, KSPForwardUnlitPrograms>(
                      KSPForwardLitSphericalPrograms.KSP_FORWARD_LIT_SPHERICAL_TEXTURED_ENVIRONMENTMAPPED);
                  }
                  case ENVIRONMENT_NONE:
                  {
                    return new Success<KSPForwardLitSphericalPrograms, KSPForwardUnlitPrograms>(
                      KSPForwardLitSphericalPrograms.KSP_FORWARD_LIT_SPHERICAL_TEXTURED);
                  }
                }
              }
            }

            throw new UnreachableCodeException();
          }
        }

        throw new UnreachableCodeException();
      }

      case TEXTURE_CAP_NONE:
      {
        switch (caps.getNormal()) {
          case NORMAL_CAP_MAPPED:
          {
            switch (caps.getSpecular()) {
              case SPECULAR_CAP_CONSTANT:
              {
                switch (caps.getEnvironment()) {
                  case ENVIRONMENT_MAPPED:
                  {
                    return new Success<KSPForwardLitSphericalPrograms, KSPForwardUnlitPrograms>(
                      KSPForwardLitSphericalPrograms.KSP_FORWARD_LIT_SPHERICAL_ENVIRONMENTMAPPED_SPECULAR_NORMALMAPPED);
                  }
                  case ENVIRONMENT_NONE:
                  {
                    return new Success<KSPForwardLitSphericalPrograms, KSPForwardUnlitPrograms>(
                      KSPForwardLitSphericalPrograms.KSP_FORWARD_LIT_SPHERICAL_SPECULAR_NORMALMAPPED);
                  }
                }

                throw new UnreachableCodeException();
              }
              case SPECULAR_CAP_MAPPED:
              {
                switch (caps.getEnvironment()) {
                  case ENVIRONMENT_MAPPED:
                  {
                    return new Success<KSPForwardLitSphericalPrograms, KSPForwardUnlitPrograms>(
                      KSPForwardLitSphericalPrograms.KSP_FORWARD_LIT_SPHERICAL_ENVIRONMENTMAPPED_SPECULARMAPPED_NORMALMAPPED);
                  }
                  case ENVIRONMENT_NONE:
                  {
                    return new Success<KSPForwardLitSphericalPrograms, KSPForwardUnlitPrograms>(
                      KSPForwardLitSphericalPrograms.KSP_FORWARD_LIT_SPHERICAL_SPECULARMAPPED_NORMALMAPPED);
                  }
                }

                throw new UnreachableCodeException();
              }
              case SPECULAR_CAP_NONE:
              {
                switch (caps.getEnvironment()) {
                  case ENVIRONMENT_MAPPED:
                  {
                    return new Success<KSPForwardLitSphericalPrograms, KSPForwardUnlitPrograms>(
                      KSPForwardLitSphericalPrograms.KSP_FORWARD_LIT_SPHERICAL_ENVIRONMENTMAPPED_NORMALMAPPED);
                  }
                  case ENVIRONMENT_NONE:
                  {
                    return new Success<KSPForwardLitSphericalPrograms, KSPForwardUnlitPrograms>(
                      KSPForwardLitSphericalPrograms.KSP_FORWARD_LIT_SPHERICAL_NORMALMAPPED);
                  }
                }
              }
            }

            throw new UnreachableCodeException();
          }
          case NORMAL_CAP_NONE:
          {
            return new Failure<KSPForwardLitSphericalPrograms, KSPForwardUnlitPrograms>(
              KSPForwardUnlitPrograms.KSP_FORWARD_UNLIT);
          }
          case NORMAL_CAP_VERTEX:
          {
            switch (caps.getSpecular()) {
              case SPECULAR_CAP_CONSTANT:
              {
                switch (caps.getEnvironment()) {
                  case ENVIRONMENT_MAPPED:
                  {
                    return new Success<KSPForwardLitSphericalPrograms, KSPForwardUnlitPrograms>(
                      KSPForwardLitSphericalPrograms.KSP_FORWARD_LIT_SPHERICAL_ENVIRONMENTMAPPED_SPECULAR);
                  }
                  case ENVIRONMENT_NONE:
                  {
                    return new Success<KSPForwardLitSphericalPrograms, KSPForwardUnlitPrograms>(
                      KSPForwardLitSphericalPrograms.KSP_FORWARD_LIT_SPHERICAL_SPECULAR);
                  }
                }

                throw new UnreachableCodeException();
              }
              case SPECULAR_CAP_MAPPED:
              {
                switch (caps.getEnvironment()) {
                  case ENVIRONMENT_MAPPED:
                  {
                    return new Success<KSPForwardLitSphericalPrograms, KSPForwardUnlitPrograms>(
                      KSPForwardLitSphericalPrograms.KSP_FORWARD_LIT_SPHERICAL_ENVIRONMENTMAPPED_SPECULARMAPPED);
                  }
                  case ENVIRONMENT_NONE:
                  {
                    return new Success<KSPForwardLitSphericalPrograms, KSPForwardUnlitPrograms>(
                      KSPForwardLitSphericalPrograms.KSP_FORWARD_LIT_SPHERICAL_SPECULARMAPPED);
                  }
                }

                throw new UnreachableCodeException();
              }
              case SPECULAR_CAP_NONE:
              {
                switch (caps.getEnvironment()) {
                  case ENVIRONMENT_MAPPED:
                  {
                    return new Success<KSPForwardLitSphericalPrograms, KSPForwardUnlitPrograms>(
                      KSPForwardLitSphericalPrograms.KSP_FORWARD_LIT_SPHERICAL_ENVIRONMENTMAPPED);
                  }
                  case ENVIRONMENT_NONE:
                  {
                    return new Success<KSPForwardLitSphericalPrograms, KSPForwardUnlitPrograms>(
                      KSPForwardLitSphericalPrograms.KSP_FORWARD_LIT_SPHERICAL);
                  }
                }
              }
            }

            throw new UnreachableCodeException();
          }
        }

        throw new UnreachableCodeException();
      }
    }

    throw new UnreachableCodeException();
  }

  private final @Nonnull Log                                                                        log;
  private final @Nonnull JCGLImplementation                                                         gl;
  private final @Nonnull KSPDepth                                                                   program_depth;
  private final @Nonnull HashMap<KSPForwardLitDirectionalPrograms, KShadingProgramLightDirectional> programs_lit_directional;
  private final @Nonnull HashMap<KSPForwardLitSphericalPrograms, KShadingProgramLightSpherical>     programs_lit_spherical;
  private final @Nonnull HashMap<KSPForwardUnlitPrograms, KShadingProgram>                          programs_unlit;

  private final @Nonnull KMatrices                                                                  matrices;
  private final @Nonnull VectorM2I                                                                  viewport_size;
  private final @Nonnull VectorM4F                                                                  background;

  KRendererForward(
    final @Nonnull JCGLImplementation gl,
    final @Nonnull FSCapabilityRead fs,
    final @Nonnull Log log)
    throws JCGLException,
      JCGLCompileException,
      JCGLUnsupportedException,
      FilesystemError,
      IOException,
      ConstraintError
  {
    this.log = new Log(log, "krenderer-forward");
    this.gl = gl;

    this.background = new VectorM4F(0.0f, 0.0f, 0.0f, 0.0f);
    this.matrices = new KMatrices();
    this.viewport_size = new VectorM2I();

    final JCGLInterfaceCommon gc = gl.getGLCommon();
    this.program_depth = KSPDepth.make(gc, fs, this.log);

    /**
     * Initialize lit directional programs.
     */

    {
      this.programs_lit_directional =
        new HashMap<KSPForwardLitDirectionalPrograms, KShadingProgramLightDirectional>();

      final KSPForwardLitDirectionalPrograms[] names =
        KSPForwardLitDirectionalPrograms.values();

      for (final KSPForwardLitDirectionalPrograms name : names) {
        final KShadingProgramLightDirectional p =
          KSPForwardLitDirectionalPrograms.make(gc, fs, this.log, name);
        this.programs_lit_directional.put(name, p);
      }
    }

    /**
     * Initialize lit spherical programs.
     */

    {
      this.programs_lit_spherical =
        new HashMap<KSPForwardLitSphericalPrograms, KShadingProgramLightSpherical>();

      final KSPForwardLitSphericalPrograms[] names =
        KSPForwardLitSphericalPrograms.values();

      for (final KSPForwardLitSphericalPrograms name : names) {
        final KShadingProgramLightSpherical p =
          KSPForwardLitSphericalPrograms.make(gc, fs, this.log, name);
        this.programs_lit_spherical.put(name, p);
      }
    }

    /**
     * Initialize unlit programs.
     */

    {
      this.programs_unlit =
        new HashMap<KSPForwardUnlitPrograms, KShadingProgram>();

      final KSPForwardUnlitPrograms[] names =
        KSPForwardUnlitPrograms.values();

      for (final KSPForwardUnlitPrograms name : names) {
        final KShadingProgram p =
          KSPForwardUnlitPrograms.make(gc, fs, this.log, name);
        this.programs_unlit.put(name, p);
      }
    }
  }

  @Override public void render(
    final @Nonnull Framebuffer result,
    final @Nonnull KScene scene)
    throws JCGLException,
      ConstraintError
  {
    this.matrices.matricesBegin();
    this.matrices.matricesMakeFromCamera(scene.getCamera());

    final JCGLInterfaceCommon gc = this.gl.getGLCommon();

    try {
      gc.framebufferDrawBind(result.getFramebuffer());

      gc.colorBufferClearV4f(this.background);

      gc.cullingEnable(
        FaceSelection.FACE_BACK,
        FaceWindingOrder.FRONT_FACE_COUNTER_CLOCKWISE);

      /**
       * Pass 0: render all meshes into the depth buffer, without touching the
       * color buffer.
       */

      gc.depthBufferTestEnable(DepthFunction.DEPTH_LESS_THAN);
      gc.depthBufferWriteEnable();
      gc.depthBufferClear(1.0f);
      gc.colorBufferMask(false, false, false, false);
      gc.blendingDisable();
      this.renderDepthPassMeshes(scene, gc);

      /**
       * Pass 1 .. n: render all lit meshes, blending additively, into the
       * framebuffer.
       */

      gc.depthBufferTestEnable(DepthFunction.DEPTH_EQUAL);
      gc.depthBufferWriteDisable();
      gc.colorBufferMask(true, true, true, true);
      gc.blendingEnable(BlendFunction.BLEND_ONE, BlendFunction.BLEND_ONE);

      for (final KLight light : scene.getLights()) {
        switch (light.getType()) {
          case LIGHT_CONE:
          {
            throw new UnimplementedCodeException();
          }
          case LIGHT_SPHERE:
          {
            // XXX: Meshes should be per-light.
            final Collection<KMeshInstance> meshes = scene.getMeshes();
            final KSphere slight = (KLight.KSphere) light;
            this.renderLightPassMeshesSpherical(gc, slight, meshes);
            break;
          }
          case LIGHT_DIRECTIONAL:
          {
            // XXX: Meshes should be per-light.
            final Collection<KMeshInstance> meshes = scene.getMeshes();
            final KDirectional dlight = (KLight.KDirectional) light;
            this.renderLightPassMeshesDirectional(gc, dlight, meshes);
            break;
          }
        }
      }
    } finally {
      gc.framebufferDrawUnbind();
    }
  }

  /**
   * Render the given mesh into the depth buffer, without touching the color
   * buffer.
   */

  private void renderDepthPassMesh(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KMeshInstance instance)
    throws ConstraintError,
      JCGLException
  {
    this.matrices.matricesMakeFromTransform(instance.getTransform());

    try {
      this.program_depth
        .ksRenderWithMeshInstance(gc, this.matrices, instance);
    } catch (final JCGLException x) {
      throw x;
    } catch (final ConstraintError x) {
      throw x;
    } catch (final Exception x) {
      throw new UnreachableCodeException();
    }
  }

  /**
   * Render all meshes into the depth buffer, without writing anything to the
   * color buffer.
   */

  private void renderDepthPassMeshes(
    final @Nonnull KScene scene,
    final @Nonnull JCGLInterfaceCommon gc)
    throws ConstraintError,
      JCGLException
  {
    this.program_depth.ksPreparePass(gc, this.matrices.getMatrixProjection());

    for (final KMeshInstance mesh : scene.getMeshes()) {
      this.renderDepthPassMesh(gc, mesh);
    }
  }

  private void renderLightPassMeshDirectional(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KDirectional light,
    final @Nonnull KMeshInstance m,
    final @Nonnull KSPForwardLitDirectionalPrograms name)
    throws JCGLException,
      ConstraintError
  {
    final KShadingProgramLightDirectional s =
      this.programs_lit_directional.get(name);

    try {
      s.ksPreparePass(gc, this.matrices.getMatrixProjection());
      s.ksPrepareWithLightDirectional(gc, this.matrices, light);
      s.ksRenderWithMeshInstance(gc, this.matrices, m);
    } catch (final JCGLException e) {
      final StringBuilder x = new StringBuilder();
      x.append("Program ");
      x.append(name);
      x.append(" raised OpenGL error: ");
      x.append(e.getMessage());
      this.log.error(s.toString());
      throw e;
    } catch (final ConstraintError e) {
      final StringBuilder x = new StringBuilder();
      x.append("Program ");
      x.append(name);
      x.append(" raised constraint error: ");
      x.append(e.getMessage());
      this.log.error(s.toString());
      throw e;
    } catch (final Exception e) {
      throw new UnreachableCodeException();
    }
  }

  private void renderLightPassMeshesDirectional(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KDirectional light,
    final @Nonnull Collection<KMeshInstance> meshes)
    throws JCGLException,
      ConstraintError
  {
    for (final KMeshInstance m : meshes) {
      this.matrices.matricesMakeFromTransform(m.getTransform());

      final Indeterminate<KSPForwardLitDirectionalPrograms, KSPForwardUnlitPrograms> name_opt =
        KRendererForward.decideLitDirectionalShader(m);

      switch (name_opt.type) {
        case SUCCESS:
        {
          final KSPForwardLitDirectionalPrograms name =
            ((Success<KSPForwardLitDirectionalPrograms, KSPForwardUnlitPrograms>) name_opt).value;

          this.renderLightPassMeshDirectional(gc, light, m, name);
          break;
        }

        case FAILURE:
        {
          final KSPForwardUnlitPrograms name =
            ((Failure<KSPForwardLitDirectionalPrograms, KSPForwardUnlitPrograms>) name_opt).value;

          this.renderLightPassMeshUnlit(gc, m, name);
          break;
        }
      }
    }
  }

  private void renderLightPassMeshesSpherical(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KSphere light,
    final @Nonnull Collection<KMeshInstance> meshes)
    throws JCGLException,
      ConstraintError
  {
    for (final KMeshInstance m : meshes) {
      this.matrices.matricesMakeFromTransform(m.getTransform());

      final Indeterminate<KSPForwardLitSphericalPrograms, KSPForwardUnlitPrograms> name_opt =
        KRendererForward.decideLitSphericalShader(m);

      switch (name_opt.type) {
        case SUCCESS:
        {
          final KSPForwardLitSphericalPrograms name =
            ((Success<KSPForwardLitSphericalPrograms, KSPForwardUnlitPrograms>) name_opt).value;

          this.renderLightPassMeshSpherical(gc, light, m, name);
          break;
        }

        case FAILURE:
        {
          final KSPForwardUnlitPrograms name =
            ((Failure<KSPForwardLitSphericalPrograms, KSPForwardUnlitPrograms>) name_opt).value;

          this.renderLightPassMeshUnlit(gc, m, name);
          break;
        }
      }
    }
  }

  private void renderLightPassMeshSpherical(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KSphere light,
    final @Nonnull KMeshInstance m,
    final @Nonnull KSPForwardLitSphericalPrograms name)
    throws JCGLException,
      ConstraintError
  {
    final KShadingProgramLightSpherical s =
      this.programs_lit_spherical.get(name);

    try {
      s.ksPreparePass(gc, this.matrices.getMatrixProjection());
      s.ksPrepareWithLightSpherical(gc, this.matrices, light);
      s.ksRenderWithMeshInstance(gc, this.matrices, m);
    } catch (final JCGLException e) {
      final StringBuilder x = new StringBuilder();
      x.append("Program ");
      x.append(name);
      x.append(" raised OpenGL error: ");
      x.append(e.getMessage());
      this.log.error(s.toString());
      throw e;
    } catch (final ConstraintError e) {
      final StringBuilder x = new StringBuilder();
      x.append("Program ");
      x.append(name);
      x.append(" raised constraint error: ");
      x.append(e.getMessage());
      this.log.error(s.toString());
      throw e;
    } catch (final Exception e) {
      throw new UnreachableCodeException();
    }
  }

  private void renderLightPassMeshUnlit(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KMeshInstance m,
    final @Nonnull KSPForwardUnlitPrograms name)
    throws JCGLException,
      ConstraintError
  {
    final KShadingProgram s = this.programs_unlit.get(name);

    try {
      s.ksPreparePass(gc, this.matrices.getMatrixProjection());
      s.ksRenderWithMeshInstance(gc, this.matrices, m);
    } catch (final JCGLException e) {
      final StringBuilder x = new StringBuilder();
      x.append("Program ");
      x.append(name);
      x.append(" raised OpenGL error: ");
      x.append(e.getMessage());
      this.log.error(s.toString());
      throw e;
    } catch (final ConstraintError e) {
      final StringBuilder x = new StringBuilder();
      x.append("Program ");
      x.append(name);
      x.append(" raised constraint error: ");
      x.append(e.getMessage());
      this.log.error(s.toString());
      throw e;
    } catch (final Exception e) {
      throw new UnreachableCodeException();
    }
  }

  @Override public void setBackgroundRGBA(
    final @Nonnull VectorReadable4F rgba)
  {
    VectorM4F.copy(rgba, this.background);
  }
}
