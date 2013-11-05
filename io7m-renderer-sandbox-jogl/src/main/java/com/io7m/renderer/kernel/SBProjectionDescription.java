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
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jcanephora.ProjectionMatrix;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.renderer.RMatrixI4x4F;
import com.io7m.renderer.RTransformProjection;
import com.io7m.renderer.xml.RXMLException;
import com.io7m.renderer.xml.RXMLUtilities;

@Immutable public abstract class SBProjectionDescription
{
  @Immutable public static final class SBProjectionFrustum extends
    SBProjectionDescription
  {
    public static @Nonnull SBProjectionFrustum fromXML(
      final @Nonnull Element e)
      throws RXMLException,
        ConstraintError
    {
      final URI uri = SBSceneDescription.SCENE_XML_URI;
      RXMLUtilities.checkIsElement(e, "projection-frustum", uri);

      return new SBProjectionFrustum(
        RXMLUtilities.getElementDouble(RXMLUtilities.getChild(e, "left", uri)),
        RXMLUtilities.getElementDouble(RXMLUtilities
          .getChild(e, "right", uri)),
        RXMLUtilities.getElementDouble(RXMLUtilities.getChild(
          e,
          "bottom",
          uri)),
        RXMLUtilities.getElementDouble(RXMLUtilities.getChild(e, "top", uri)),
        RXMLUtilities.getElementDouble(RXMLUtilities.getChild(e, "near", uri)),
        RXMLUtilities.getElementDouble(RXMLUtilities.getChild(e, "far", uri)));
    }

    private final double left;
    private final double right;
    private final double bottom;
    private final double top;
    private final double near;
    private final double far;

    public SBProjectionFrustum()
      throws ConstraintError
    {
      this(-5.0, 5.0, -5.0, 5.0, 1.0, 100.0);
    }

    @SuppressWarnings("synthetic-access") public SBProjectionFrustum(
      final double left,
      final double right,
      final double bottom,
      final double top,
      final double near,
      final double far)
      throws ConstraintError
    {
      super(Type.PROJECTION_FRUSTUM);
      this.left = left;
      this.right = right;
      this.bottom = bottom;
      this.top = top;
      this.near = near;
      this.far = far;
    }

    public double getBottom()
    {
      return this.bottom;
    }

    @Override public double getFar()
    {
      return this.far;
    }

    public double getLeft()
    {
      return this.left;
    }

    public double getNear()
    {
      return this.near;
    }

    public double getRight()
    {
      return this.right;
    }

    public double getTop()
    {
      return this.top;
    }

    @Override public @Nonnull Element toXML()
    {
      final URI uri = SBSceneDescription.SCENE_XML_URI;
      final Element e = new Element("s:projection-frustum", uri.toString());
      RXMLUtilities.putElementDouble(e, "s", "left", this.left, uri);
      RXMLUtilities.putElementDouble(e, "s", "right", this.right, uri);
      RXMLUtilities.putElementDouble(e, "s", "top", this.top, uri);
      RXMLUtilities.putElementDouble(e, "s", "bottom", this.bottom, uri);
      RXMLUtilities.putElementDouble(e, "s", "near", this.near, uri);
      RXMLUtilities.putElementDouble(e, "s", "far", this.far, uri);
      return e;
    }

    @Override public RMatrixI4x4F<RTransformProjection> makeProjectionMatrix(
      final @Nonnull MatrixM4x4F temporary)
      throws ConstraintError
    {
      ProjectionMatrix.makeFrustumProjection(
        temporary,
        this.left,
        this.right,
        this.bottom,
        this.top,
        this.near,
        this.far);
      return new RMatrixI4x4F<RTransformProjection>(temporary);
    }
  }

  @Immutable public static final class SBProjectionOrthographic extends
    SBProjectionDescription
  {
    public static @Nonnull SBProjectionOrthographic fromXML(
      final @Nonnull Element e)
      throws RXMLException,
        ConstraintError
    {
      final URI uri = SBSceneDescription.SCENE_XML_URI;
      RXMLUtilities.checkIsElement(e, "projection-orthographic", uri);

      return new SBProjectionOrthographic(
        RXMLUtilities.getElementDouble(RXMLUtilities.getChild(e, "left", uri)),
        RXMLUtilities.getElementDouble(RXMLUtilities
          .getChild(e, "right", uri)),
        RXMLUtilities.getElementDouble(RXMLUtilities.getChild(
          e,
          "bottom",
          uri)),
        RXMLUtilities.getElementDouble(RXMLUtilities.getChild(e, "top", uri)),
        RXMLUtilities.getElementDouble(RXMLUtilities.getChild(e, "near", uri)),
        RXMLUtilities.getElementDouble(RXMLUtilities.getChild(e, "far", uri)));
    }

    private final double left;
    private final double right;
    private final double bottom;
    private final double top;
    private final double near;
    private final double far;

    public SBProjectionOrthographic()
      throws ConstraintError
    {
      this(-5.0, 5.0, -5.0, 5.0, 1.0, 100.0);
    }

    @SuppressWarnings("synthetic-access") public SBProjectionOrthographic(
      final double left,
      final double right,
      final double bottom,
      final double top,
      final double near,
      final double far)
      throws ConstraintError
    {
      super(Type.PROJECTION_ORTHOGRAPHIC);
      this.left = left;
      this.right = right;
      this.bottom = bottom;
      this.top = top;
      this.near = near;
      this.far = far;
    }

    public double getBottom()
    {
      return this.bottom;
    }

    @Override public double getFar()
    {
      return this.far;
    }

    public double getLeft()
    {
      return this.left;
    }

    public double getNear()
    {
      return this.near;
    }

    public double getRight()
    {
      return this.right;
    }

    public double getTop()
    {
      return this.top;
    }

    @Override public @Nonnull Element toXML()
    {
      final URI uri = SBSceneDescription.SCENE_XML_URI;
      final Element e =
        new Element("s:projection-orthographic", uri.toString());
      RXMLUtilities.putElementDouble(e, "s", "left", this.left, uri);
      RXMLUtilities.putElementDouble(e, "s", "right", this.right, uri);
      RXMLUtilities.putElementDouble(e, "s", "top", this.top, uri);
      RXMLUtilities.putElementDouble(e, "s", "bottom", this.bottom, uri);
      RXMLUtilities.putElementDouble(e, "s", "near", this.near, uri);
      RXMLUtilities.putElementDouble(e, "s", "far", this.far, uri);
      return e;
    }

    @Override public RMatrixI4x4F<RTransformProjection> makeProjectionMatrix(
      final @Nonnull MatrixM4x4F temporary)
      throws ConstraintError
    {
      ProjectionMatrix.makeOrthographicProjection(
        temporary,
        this.left,
        this.right,
        this.bottom,
        this.top,
        this.near,
        this.far);
      return new RMatrixI4x4F<RTransformProjection>(temporary);
    }
  }

  @Immutable public static final class SBProjectionPerspective extends
    SBProjectionDescription
  {
    public static @Nonnull SBProjectionPerspective fromXML(
      final @Nonnull Element e)
      throws RXMLException,
        ConstraintError
    {
      final URI uri = SBSceneDescription.SCENE_XML_URI;
      RXMLUtilities.checkIsElement(e, "projection-perspective", uri);

      return new SBProjectionPerspective(
        RXMLUtilities.getElementDouble(RXMLUtilities.getChild(e, "near", uri)),
        RXMLUtilities.getElementDouble(RXMLUtilities.getChild(e, "far", uri)),
        RXMLUtilities.getElementDouble(RXMLUtilities.getChild(
          e,
          "aspect",
          uri)),
        RXMLUtilities.getElementDouble(RXMLUtilities.getChild(
          e,
          "horizontal-fov",
          uri)));
    }

    private final double near;
    private final double far;
    private final double aspect;
    private final double horizontal_fov;

    public SBProjectionPerspective()
      throws ConstraintError
    {
      this(1.0, 100.0, 1.333333, Math.toRadians(90));
    }

    @SuppressWarnings("synthetic-access") public SBProjectionPerspective(
      final double near,
      final double far,
      final double aspect,
      final double horizontal_fov)
      throws ConstraintError
    {
      super(Type.PROJECTION_PERSPECTIVE);
      this.near = near;
      this.far = far;
      this.aspect = aspect;
      this.horizontal_fov = horizontal_fov;
    }

    public double getAspect()
    {
      return this.aspect;
    }

    @Override public double getFar()
    {
      return this.far;
    }

    public double getHorizontalFOV()
    {
      return this.horizontal_fov;
    }

    public double getNear()
    {
      return this.near;
    }

    @Override public @Nonnull Element toXML()
    {
      final URI uri = SBSceneDescription.SCENE_XML_URI;
      final Element e =
        new Element("s:projection-perspective", uri.toString());
      RXMLUtilities.putElementDouble(e, "s", "near", this.near, uri);
      RXMLUtilities.putElementDouble(e, "s", "far", this.far, uri);
      RXMLUtilities.putElementDouble(e, "s", "aspect", this.aspect, uri);
      RXMLUtilities.putElementDouble(
        e,
        "s",
        "horizontal-fov",
        this.horizontal_fov,
        uri);
      return e;
    }

    @Override public RMatrixI4x4F<RTransformProjection> makeProjectionMatrix(
      final @Nonnull MatrixM4x4F temporary)
      throws ConstraintError
    {
      ProjectionMatrix.makePerspectiveProjection(
        temporary,
        this.near,
        this.far,
        this.aspect,
        this.horizontal_fov);
      return new RMatrixI4x4F<RTransformProjection>(temporary);
    }
  }

  public static enum Type
  {
    PROJECTION_FRUSTUM("Frustum"),
    PROJECTION_PERSPECTIVE("Perspective"),
    PROJECTION_ORTHOGRAPHIC("Orthographic");

    private final @Nonnull String name;

    private Type(
      final @Nonnull String name)
    {
      this.name = name;
    }

    public @Nonnull String getName()
    {
      return this.name;
    }

    @Override public @Nonnull String toString()
    {
      return this.name;
    }
  }

  public static @Nonnull SBProjectionDescription fromXML(
    final @Nonnull Element e)
    throws RXMLException,
      ConstraintError
  {
    if (e.getLocalName().equals("projection-frustum")) {
      return SBProjectionFrustum.fromXML(e);
    } else if (e.getLocalName().equals("projection-perspective")) {
      return SBProjectionPerspective.fromXML(e);
    } else if (e.getLocalName().equals("projection-orthographic")) {
      return SBProjectionOrthographic.fromXML(e);
    }

    throw new UnreachableCodeException();
  }

  private final @Nonnull Type type;

  private SBProjectionDescription(
    final @Nonnull Type type)
    throws ConstraintError
  {
    this.type = Constraints.constrainNotNull(type, "Type");
  }

  public abstract double getFar();

  public @Nonnull Type getType()
  {
    return this.type;
  }

  public abstract @Nonnull
    RMatrixI4x4F<RTransformProjection>
    makeProjectionMatrix(
      final @Nonnull MatrixM4x4F temporary)
      throws ConstraintError;

  public abstract @Nonnull Element toXML();
}
