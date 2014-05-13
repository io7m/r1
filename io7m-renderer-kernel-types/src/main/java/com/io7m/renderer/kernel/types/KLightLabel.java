/*
 * Copyright © 2014 <code@io7m.com> http://io7m.com
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

package com.io7m.renderer.kernel.types;

import com.io7m.jcanephora.JCGLException;
import com.io7m.jfunctional.Some;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.types.RException;

/**
 * Labels for lights.
 */

public enum KLightLabel
  implements
  KTexturesRequiredType,
  KLabelType
{
  /**
   * Directional lights.
   */

  LIGHT_LABEL_DIRECTIONAL("LD", 0),

  /**
   * Projective lights without shadow maps.
   */

  LIGHT_LABEL_PROJECTIVE("LP", 1),

  /**
   * Projective lights with basic shadow maps.
   */

  LIGHT_LABEL_PROJECTIVE_SHADOW_MAPPED_BASIC("LPSMB", 2),

  /**
   * Projective lights with basic packed4444 shadow maps (for implementations
   * that do not support rendering to depth textures).
   */

  LIGHT_LABEL_PROJECTIVE_SHADOW_MAPPED_BASIC_PACKED4444("LPSMBP4", 2),

  /**
   * Projective lights with variance shadow maps.
   */

  LIGHT_LABEL_PROJECTIVE_SHADOW_MAPPED_VARIANCE("LPSMV", 2),

  /**
   * Spherical lights.
   */

  LIGHT_LABEL_SPHERICAL("LS", 0);

  /**
   * Derive a label from the given capabilities and light.
   * 
   * @param caps
   *          The current capabilities
   * @param light
   *          The light
   * @return A label
   */

  public static KLightLabel fromLight(
    final KGraphicsCapabilitiesType caps,
    final KLightType light)
  {
    try {
      return light
        .lightAccept(new KLightVisitorType<KLightLabel, UnreachableCodeException>() {
          @Override public KLightLabel lightDirectional(
            final KLightDirectional l)
            throws RException
          {
            return LIGHT_LABEL_DIRECTIONAL;
          }

          @Override public KLightLabel lightProjective(
            final KLightProjective l)
            throws RException
          {
            if (l.lightHasShadow()) {
              final KShadowType shadow =
                ((Some<KShadowType>) l.lightGetShadow()).get();

              try {
                return shadow
                  .shadowAccept(new KShadowVisitorType<KLightLabel, UnreachableCodeException>() {
                    @Override public KLightLabel shadowMappedBasic(
                      final KShadowMappedBasic s)
                      throws JCGLException,
                        RException
                    {
                      if (caps.getSupportsDepthTextures()) {
                        return LIGHT_LABEL_PROJECTIVE_SHADOW_MAPPED_BASIC;
                      }
                      return LIGHT_LABEL_PROJECTIVE_SHADOW_MAPPED_BASIC_PACKED4444;
                    }

                    @Override public KLightLabel shadowMappedVariance(
                      final KShadowMappedVariance s)
                      throws JCGLException,
                        RException
                    {
                      return LIGHT_LABEL_PROJECTIVE_SHADOW_MAPPED_VARIANCE;
                    }
                  });
              } catch (final JCGLException e) {
                throw new UnreachableCodeException(e);
              }
            }
            return LIGHT_LABEL_PROJECTIVE;
          }

          @Override public KLightLabel lightSpherical(
            final KLightSphere l)
            throws RException
          {
            return LIGHT_LABEL_SPHERICAL;
          }
        });
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private final String code;
  private final int    textures_required;

  private KLightLabel(
    final String in_code,
    final int in_textures_required)
  {
    this.code = in_code;
    this.textures_required = in_textures_required;
  }

  @Override public String labelGetCode()
  {
    return this.code;
  }

  @Override public int texturesGetRequired()
  {
    return this.textures_required;
  }
}
