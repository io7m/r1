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

import java.net.URI;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import nu.xom.Element;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jcanephora.ProjectionMatrix;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.jvvfs.PathVirtual;
import com.io7m.renderer.RMatrixI4x4F;
import com.io7m.renderer.RSpaceRGB;
import com.io7m.renderer.RSpaceWorld;
import com.io7m.renderer.RTransformProjection;
import com.io7m.renderer.RVectorI3F;
import com.io7m.renderer.RVectorI4F;
import com.io7m.renderer.kernel.KLight.KDirectional;
import com.io7m.renderer.kernel.KLight.KProjective;
import com.io7m.renderer.kernel.KLight.KSphere;
import com.io7m.renderer.kernel.KLight.Type;
import com.io7m.renderer.xml.RXMLException;
import com.io7m.renderer.xml.RXMLUtilities;

abstract class SBLightDescription
{
  @Immutable static final class SBLightDescriptionDirectional extends
    SBLightDescription
  {
    static @Nonnull SBLightDescriptionDirectional fromXML(
      final @Nonnull Element e)
      throws RXMLException,
        ConstraintError
    {
      final URI uri = SBSceneDescription.SCENE_XML_URI;
      RXMLUtilities.checkIsElement(e, "light-directional", uri);

      final Element ed = RXMLUtilities.getChild(e, "direction", uri);
      final Element ec = RXMLUtilities.getChild(e, "colour", uri);
      final Element ei = RXMLUtilities.getChild(e, "intensity", uri);
      final Element eid = RXMLUtilities.getChild(e, "id", uri);

      final RVectorI3F<RSpaceWorld> direction =
        RXMLUtilities.getElementVector3f(ed, uri);
      final RVectorI3F<RSpaceRGB> colour =
        RXMLUtilities.getElementRGB(ec, uri);
      final int id = RXMLUtilities.getElementInteger(eid);
      final float intensity = RXMLUtilities.getElementFloat(ei);

      return new SBLightDescriptionDirectional(new KLight.KDirectional(
        Integer.valueOf(id),
        direction,
        colour,
        intensity));
    }

    private final @Nonnull KLight.KDirectional actual;

    @SuppressWarnings("synthetic-access") SBLightDescriptionDirectional(
      final @Nonnull KLight.KDirectional actual)
      throws ConstraintError
    {
      super(KLight.Type.LIGHT_DIRECTIONAL);
      this.actual = actual;
    }

    @Override public Integer getID()
    {
      return this.actual.getID();
    }

    public KDirectional getLight()
    {
      return this.actual;
    }

    @Override Element toXML()
    {
      final String uri = SBSceneDescription.SCENE_XML_URI.toString();

      final Element eid = new Element("s:id", uri);
      eid.appendChild(this.actual.getID().toString());

      final Element ei = new Element("s:intensity", uri);
      ei.appendChild(Float.toString(this.actual.getIntensity()));

      final Element ec = new Element("s:colour", uri);
      RXMLUtilities.putElementAttributesRGB(
        ec,
        this.actual.getColour(),
        "s",
        SBSceneDescription.SCENE_XML_URI);

      final Element e = new Element("s:light-directional", uri);
      final Element ed = new Element("s:direction", uri);
      RXMLUtilities.putElementAttributesVector3f(
        ed,
        this.actual.getDirection(),
        "s",
        SBSceneDescription.SCENE_XML_URI);

      e.appendChild(eid);
      e.appendChild(ec);
      e.appendChild(ei);
      e.appendChild(ed);
      return e;
    }
  }

  @Immutable static final class SBLightDescriptionProjective extends
    SBLightDescription
  {
    static @Nonnull SBLightDescriptionProjective fromXML(
      final @Nonnull Element e)
      throws RXMLException,
        ConstraintError
    {
      final URI uri = SBSceneDescription.SCENE_XML_URI;
      RXMLUtilities.checkIsElement(e, "light-projective", uri);

      final Element ep = RXMLUtilities.getChild(e, "position", uri);
      final Element eo = RXMLUtilities.getChild(e, "orientation", uri);
      final Element ec = RXMLUtilities.getChild(e, "colour", uri);
      final Element ei = RXMLUtilities.getChild(e, "intensity", uri);
      final Element eid = RXMLUtilities.getChild(e, "id", uri);
      final Element er = RXMLUtilities.getChild(e, "distance", uri);
      final Element ef = RXMLUtilities.getChild(e, "falloff", uri);
      final Element efr = RXMLUtilities.getChild(e, "frustum", uri);
      final Element et = RXMLUtilities.getChild(e, "texture", uri);

      final RVectorI3F<RSpaceWorld> position =
        RXMLUtilities.getElementAttributesVector3f(ep, uri);
      final QuaternionI4F orientation =
        RXMLUtilities.getElementAttributesQuaternion4f(eo, uri);

      final RVectorI3F<RSpaceRGB> colour =
        RXMLUtilities.getElementAttributesRGB(ec, uri);
      final int id = RXMLUtilities.getElementInteger(eid);
      final float intensity = RXMLUtilities.getElementFloat(ei);
      final float distance = RXMLUtilities.getElementFloat(er);
      final float falloff = RXMLUtilities.getElementFloat(ef);
      final SBFrustum frustum = SBFrustum.fromXML(efr);
      final PathVirtual texture =
        PathVirtual.ofString(RXMLUtilities.getElementNonEmptyString(et));

      return new SBLightDescriptionProjective(
        orientation,
        position,
        distance,
        falloff,
        frustum,
        texture,
        colour,
        intensity,
        id);
    }

    private final @Nonnull QuaternionI4F                      orientation;
    private final @Nonnull RVectorI3F<RSpaceWorld>            position;
    private final float                                       distance;
    private final float                                       falloff;
    private final @Nonnull SBFrustum                          frustum;
    private final @Nonnull PathVirtual                        texture;
    private final @Nonnull RVectorI3F<RSpaceRGB>              colour;
    private final float                                       intensity;
    private final @Nonnull Integer                            id;

    private final @Nonnull RMatrixI4x4F<RTransformProjection> projection;

    @SuppressWarnings("synthetic-access") SBLightDescriptionProjective(
      final @Nonnull QuaternionI4F orientation,
      final @Nonnull RVectorI3F<RSpaceWorld> position,
      final float distance,
      final float falloff,
      final @Nonnull SBFrustum frustum,
      final @Nonnull PathVirtual texture,
      final @Nonnull RVectorI3F<RSpaceRGB> colour,
      final float intensity,
      final @Nonnull Integer id)
      throws ConstraintError
    {
      super(KLight.Type.LIGHT_PROJECTIVE);

      final MatrixM4x4F m = new MatrixM4x4F();
      ProjectionMatrix.makePerspective(
        m,
        1,
        100,
        frustum.getAspectRatio(),
        frustum.getHorizontalFOV());

      this.orientation = orientation;
      this.position = position;
      this.distance = distance;
      this.falloff = falloff;
      this.frustum = frustum;
      this.texture = texture;
      this.colour = colour;
      this.intensity = intensity;
      this.id = id;
      this.projection = new RMatrixI4x4F<RTransformProjection>(m);
    }

    public RVectorI3F<RSpaceRGB> getColour()
    {
      return this.colour;
    }

    public float getDistance()
    {
      return this.distance;
    }

    public float getFalloff()
    {
      return this.falloff;
    }

    public SBFrustum getFrustum()
    {
      return this.frustum;
    }

    public Integer getId()
    {
      return this.id;
    }

    @Override public Integer getID()
    {
      return this.id;
    }

    public float getIntensity()
    {
      return this.intensity;
    }

    KProjective getLight(
      final @Nonnull SBTexture2D<SBTexture2DKindAlbedo> t)
    {
      return new KLight.KProjective(
        this.id,
        t.getTexture(),
        this.position,
        this.orientation,
        this.colour,
        this.intensity,
        this.distance,
        this.falloff,
        this.projection);
    }

    public QuaternionI4F getOrientation()
    {
      return this.orientation;
    }

    public RVectorI3F<RSpaceWorld> getPosition()
    {
      return this.position;
    }

    @Nonnull PathVirtual getTexture()
    {
      return this.texture;
    }

    @Override Element toXML()
    {
      final String uri = SBSceneDescription.SCENE_XML_URI.toString();

      final Element eid = new Element("s:id", uri);
      eid.appendChild(this.id.toString());

      final Element ei = new Element("s:intensity", uri);
      ei.appendChild(Float.toString(this.intensity));

      final Element ec = new Element("s:colour", uri);
      RXMLUtilities.putElementAttributesRGB(
        ec,
        this.colour,
        "s",
        SBSceneDescription.SCENE_XML_URI);

      final Element e = new Element("s:light-projective", uri);

      final Element ep = new Element("s:position", uri);
      RXMLUtilities.putElementAttributesVector3f(
        ep,
        this.position,
        "s",
        SBSceneDescription.SCENE_XML_URI);

      final QuaternionI4F doo = this.orientation;
      final RVectorI4F<RSpaceWorld> ov =
        new RVectorI4F<RSpaceWorld>(
          doo.getXF(),
          doo.getYF(),
          doo.getZF(),
          doo.getWF());

      final Element eo = new Element("s:orientation", uri);
      RXMLUtilities.putElementAttributesVector4f(
        eo,
        ov,
        "s",
        SBSceneDescription.SCENE_XML_URI);

      final Element er = new Element("s:distance", uri);
      er.appendChild(Float.toString(this.distance));
      final Element ef = new Element("s:falloff", uri);
      ef.appendChild(Float.toString(this.falloff));

      final Element efr = this.frustum.toXML();

      final Element et = new Element("s:texture", uri);
      et.appendChild(this.texture.toString());

      e.appendChild(eid);
      e.appendChild(ec);
      e.appendChild(ei);
      e.appendChild(ep);
      e.appendChild(eo);
      e.appendChild(efr);
      e.appendChild(er);
      e.appendChild(ef);
      e.appendChild(et);
      return e;
    }

    public @Nonnull RMatrixI4x4F<RTransformProjection> getProjection()
    {
      return this.projection;
    }
  }

  @Immutable static final class SBLightDescriptionSpherical extends
    SBLightDescription
  {
    static @Nonnull SBLightDescriptionSpherical fromXML(
      final @Nonnull Element e)
      throws RXMLException,
        ConstraintError
    {
      final URI uri = SBSceneDescription.SCENE_XML_URI;
      RXMLUtilities.checkIsElement(e, "light-spherical", uri);

      final Element ep = RXMLUtilities.getChild(e, "position", uri);
      final Element ec = RXMLUtilities.getChild(e, "colour", uri);
      final Element ei = RXMLUtilities.getChild(e, "intensity", uri);
      final Element eid = RXMLUtilities.getChild(e, "id", uri);
      final Element er = RXMLUtilities.getChild(e, "distance", uri);
      final Element ef = RXMLUtilities.getChild(e, "falloff", uri);

      final RVectorI3F<RSpaceWorld> position =
        RXMLUtilities.getElementVector3f(ep, uri);
      final RVectorI3F<RSpaceRGB> colour =
        RXMLUtilities.getElementRGB(ec, uri);
      final int id = RXMLUtilities.getElementInteger(eid);
      final float intensity = RXMLUtilities.getElementFloat(ei);
      final float radius = RXMLUtilities.getElementFloat(er);
      final float falloff = RXMLUtilities.getElementFloat(ef);

      return new SBLightDescriptionSpherical(new KLight.KSphere(
        Integer.valueOf(id),
        colour,
        intensity,
        position,
        radius,
        falloff));
    }

    private final @Nonnull KLight.KSphere actual;

    @SuppressWarnings("synthetic-access") SBLightDescriptionSpherical(
      final @Nonnull KLight.KSphere actual)
      throws ConstraintError
    {
      super(KLight.Type.LIGHT_SPHERE);
      this.actual = actual;
    }

    @Override public Integer getID()
    {
      return this.actual.getID();
    }

    public KSphere getLight()
    {
      return this.actual;
    }

    @Override Element toXML()
    {
      final String uri = SBSceneDescription.SCENE_XML_URI.toString();

      final Element eid = new Element("s:id", uri);
      eid.appendChild(this.actual.getID().toString());

      final Element ei = new Element("s:intensity", uri);
      ei.appendChild(Float.toString(this.actual.getIntensity()));

      final Element ec = new Element("s:colour", uri);
      RXMLUtilities.putElementAttributesRGB(
        ec,
        this.actual.getColour(),
        "s",
        SBSceneDescription.SCENE_XML_URI);

      final Element e = new Element("s:light-spherical", uri);
      final Element ed = new Element("s:position", uri);
      RXMLUtilities.putElementAttributesVector3f(
        ed,
        this.actual.getPosition(),
        "s",
        SBSceneDescription.SCENE_XML_URI);
      final Element er = new Element("s:distance", uri);
      er.appendChild(Float.toString(this.actual.getRadius()));
      final Element ef = new Element("s:falloff", uri);
      ef.appendChild(Float.toString(this.actual.getFalloff()));

      e.appendChild(eid);
      e.appendChild(ec);
      e.appendChild(ei);
      e.appendChild(ed);
      e.appendChild(er);
      e.appendChild(ef);
      return e;
    }
  }

  private final @Nonnull KLight.Type type;

  private SBLightDescription(
    final @Nonnull KLight.Type type)
    throws ConstraintError
  {
    this.type = Constraints.constrainNotNull(type, "Type");
  }

  abstract public @Nonnull Integer getID();

  public @Nonnull Type getType()
  {
    return this.type;
  }

  abstract @Nonnull Element toXML();
}
