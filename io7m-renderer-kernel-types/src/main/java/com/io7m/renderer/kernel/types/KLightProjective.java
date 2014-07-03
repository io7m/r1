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
import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jfunctional.None;
import com.io7m.jfunctional.Option;
import com.io7m.jfunctional.OptionType;
import com.io7m.jfunctional.OptionVisitorType;
import com.io7m.jfunctional.Some;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RExceptionLightMissingTexture;
import com.io7m.renderer.types.RExceptionUserError;
import com.io7m.renderer.types.RSpaceRGBType;
import com.io7m.renderer.types.RSpaceWorldType;
import com.io7m.renderer.types.RVectorI3F;

/**
 * <p>
 * A projective light "projects" a texture into a scene from a given position,
 * according to the given projection matrix. The texture projected is
 * multiplied by the given color and attenuated according to the given range
 * and falloff values. The light may optionally cast shadows using a variety
 * of shadow mapping techniques, specified by the given shadow value.
 * </p>
 */

@EqualityReference public final class KLightProjective implements KLightType
{
  @SuppressWarnings("synthetic-access") @EqualityReference private static final class Builder implements
    KLightProjectiveBuilderType
  {
    private RVectorI3F<RSpaceRGBType>           color;
    private float                               falloff;
    private float                               intensity;
    private QuaternionI4F                       orientation;
    private RVectorI3F<RSpaceWorldType>         position;
    private KProjectionType                     projection;
    private float                               range;
    private OptionType<KShadowType>             shadow;
    private @Nullable Texture2DStaticUsableType texture;

    Builder(
      final KLightProjective in_original)
    {
      NullCheck.notNull(in_original, "Light");
      this.color = in_original.color;
      this.intensity = in_original.intensity;
      this.falloff = in_original.falloff;
      this.position = in_original.position;
      this.orientation = in_original.orientation;
      this.projection = in_original.projection;
      this.range = in_original.range;
      this.texture = in_original.texture;
      this.shadow = in_original.shadow;
    }

    Builder(
      final Texture2DStaticUsableType in_texture,
      final KProjectionType in_projection)
    {
      this.color = RVectorI3F.one();
      this.intensity = 1.0f;
      this.falloff = 1.0f;
      this.position = RVectorI3F.zero();
      this.orientation = QuaternionI4F.IDENTITY;
      this.projection = NullCheck.notNull(in_projection, "Projection");
      this.range = 8.0f;
      this.texture = NullCheck.notNull(in_texture, "Texture");
      this.shadow = Option.none();
    }

    @Override public KLightProjective build(
      final KGraphicsCapabilitiesType caps)
      throws RException,
        RExceptionUserError
    {
      NullCheck.notNull(caps, "Capabilities");

      final Texture2DStaticUsableType t = this.texture;
      if (t == null) {
        throw new RExceptionLightMissingTexture(
          "No texture specified for projective light");
      }

      final String c = KLightProjective.getCode(caps, this.shadow);
      return new KLightProjective(
        t,
        this.position,
        this.orientation,
        this.color,
        this.intensity,
        this.range,
        this.falloff,
        this.projection,
        this.shadow,
        c);
    }

    @Override public void setColor(
      final RVectorI3F<RSpaceRGBType> in_color)
    {
      this.color = NullCheck.notNull(in_color, "Color");
    }

    @Override public void setFalloff(
      final float in_exponent)
    {
      this.falloff = in_exponent;
    }

    @Override public void setIntensity(
      final float in_intensity)
    {
      this.intensity = in_intensity;
    }

    @Override public void setNoShadow()
    {
      this.shadow = Option.none();
    }

    @Override public void setOrientation(
      final QuaternionI4F in_orientation)
    {
      this.orientation = NullCheck.notNull(in_orientation, "Orientation");
    }

    @Override public void setPosition(
      final RVectorI3F<RSpaceWorldType> in_position)
    {
      this.position = NullCheck.notNull(in_position, "Position");
    }

    @Override public void setProjection(
      final KProjectionType in_projection)
    {
      this.projection = NullCheck.notNull(in_projection, "Projection");
    }

    @Override public void setRange(
      final float in_range)
    {
      this.range = in_range;
    }

    @Override public void setShadow(
      final KShadowType in_shadow)
    {
      this.shadow = Option.some(NullCheck.notNull(in_shadow, "Shadow"));
    }

    @Override public void setShadowOption(
      final OptionType<KShadowType> s)
    {
      this.shadow = NullCheck.notNull(s, "Shadow");
    }

    @Override public void setTexture(
      final Texture2DStaticUsableType in_texture)
    {
      this.texture = NullCheck.notNull(in_texture, "Texture");
    }
  }

  private static String getCode(
    final KGraphicsCapabilitiesType caps,
    final OptionType<KShadowType> in_shadow)
  {
    return in_shadow.accept(new OptionVisitorType<KShadowType, String>() {
      @Override public String none(
        final None<KShadowType> n)
      {
        return "LProj";
      }

      @Override public String some(
        final Some<KShadowType> s)
      {
        try {
          return s.get().shadowAccept(
            new KShadowVisitorType<String, RException>() {
              @Override public String shadowMappedBasic(
                final KShadowMappedBasic smb)
              {
                if (caps.getSupportsDepthTextures()) {
                  return "LProjSMBasic";
                }
                return "LProjSMBasic4444";
              }

              @Override public String shadowMappedVariance(
                final KShadowMappedVariance smv)
              {
                return "LProjSMVar";
              }
            });
        } catch (final JCGLException e) {
          throw new UnreachableCodeException(e);
        } catch (final RException e) {
          throw new UnreachableCodeException(e);
        }
      }
    });
  }

  /**
   * <p>
   * Create a builder for creating new projective lights.
   * </p>
   * 
   * @param in_texture
   *          The texture.
   * @param in_projection
   *          The projection.
   * @return A new light builder.
   */

  public static KLightProjectiveBuilderType newBuilder(
    final Texture2DStaticUsableType in_texture,
    final KProjectionType in_projection)
  {
    return new Builder(in_texture, in_projection);
  }

  /**
   * <p>
   * Create a builder for creating new spherical lights. The builder will be
   * initialized to values based on the given light.
   * </p>
   * 
   * @param p
   *          The initial light.
   * @return A new light builder.
   */

  public static KLightProjectiveBuilderType newBuilderFrom(
    final KLightProjective p)
  {
    return new Builder(p);
  }

  private final String                      code;
  private final RVectorI3F<RSpaceRGBType>   color;
  private final float                       falloff;
  private final float                       intensity;
  private final QuaternionI4F               orientation;
  private final RVectorI3F<RSpaceWorldType> position;
  private final KProjectionType             projection;
  private final float                       range;
  private final OptionType<KShadowType>     shadow;
  private final Texture2DStaticUsableType   texture;
  private final int                         textures;

  private KLightProjective(
    final Texture2DStaticUsableType in_texture,
    final RVectorI3F<RSpaceWorldType> in_position,
    final QuaternionI4F in_orientation,
    final RVectorI3F<RSpaceRGBType> in_color,
    final float in_intensity,
    final float in_range,
    final float in_falloff,
    final KProjectionType in_projection,
    final OptionType<KShadowType> in_shadow,
    final String in_code)
  {
    this.intensity = in_intensity;
    this.color = NullCheck.notNull(in_color, "Color");
    this.position = NullCheck.notNull(in_position, "Position");
    this.orientation = NullCheck.notNull(in_orientation, "Orientation");
    this.range = in_range;
    this.falloff = in_falloff;
    this.projection = NullCheck.notNull(in_projection, "Projection");
    this.texture = NullCheck.notNull(in_texture, "Texture");
    this.shadow = NullCheck.notNull(in_shadow, "Shadow");
    this.code = NullCheck.notNull(in_code, "Code");

    /**
     * One texture for the light, and at most one for the shadow.
     */

    this.textures = 1 + (this.shadow.isSome() ? 1 : 0);
  }

  @Override public
    <A, E extends Throwable, V extends KLightVisitorType<A, E>>
    A
    lightAccept(
      final V v)
      throws E,
        RException
  {
    return v.lightProjective(this);
  }

  @Override public String lightGetCode()
  {
    return this.code;
  }

  @Override public RVectorI3F<RSpaceRGBType> lightGetColor()
  {
    return this.color;
  }

  /**
   * @return The falloff exponent for the light
   */

  public float lightGetFalloff()
  {
    return this.falloff;
  }

  @Override public float lightGetIntensity()
  {
    return this.intensity;
  }

  /**
   * @return The orientation of the light
   */

  public QuaternionI4F lightGetOrientation()
  {
    return this.orientation;
  }

  /**
   * @return The position of the light
   */

  public RVectorI3F<RSpaceWorldType> lightGetPosition()
  {
    return this.position;
  }

  /**
   * @return The projection matrix for the light
   */

  public KProjectionType lightGetProjection()
  {
    return this.projection;
  }

  /**
   * @return The maximum range of the light
   */

  public float lightGetRange()
  {
    return this.range;
  }

  @Override public OptionType<KShadowType> lightGetShadow()
  {
    return this.shadow;
  }

  /**
   * @return The texture that will be projected into the scene
   */

  public Texture2DStaticUsableType lightGetTexture()
  {
    return this.texture;
  }

  @Override public boolean lightHasShadow()
  {
    return this.shadow.isSome();
  }

  @Override public int texturesGetRequired()
  {
    return this.textures;
  }

  @Override public String toString()
  {
    final StringBuilder b = new StringBuilder();
    b.append("[KLightProjective color=");
    b.append(this.color);
    b.append(" falloff=");
    b.append(this.falloff);
    b.append(" intensity=");
    b.append(this.intensity);
    b.append(" orientation=");
    b.append(this.orientation);
    b.append(" position=");
    b.append(this.position);
    b.append(" projection=");
    b.append(this.projection);
    b.append(" range=");
    b.append(this.range);
    b.append(" shadow=");
    b.append(this.shadow);
    b.append(" texture=");
    b.append(this.texture);
    b.append("]");
    final String r = b.toString();
    assert r != null;
    return r;
  }
}
