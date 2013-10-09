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
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ValidityException;

import org.pcollections.HashTreePMap;
import org.pcollections.HashTreePSet;
import org.pcollections.PMap;
import org.pcollections.PSet;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnimplementedCodeException;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jvvfs.PathVirtual;
import com.io7m.renderer.RSpaceRGB;
import com.io7m.renderer.RSpaceWorld;
import com.io7m.renderer.RVectorI3F;
import com.io7m.renderer.kernel.KLight.KDirectional;
import com.io7m.renderer.kernel.KLight.KSphere;
import com.io7m.renderer.xml.RXMLException;
import com.io7m.renderer.xml.RXMLUtilities;

@Immutable final class SBSceneDescription
{
  static final @Nonnull URI SCENE_XML_URI;
  static final int          SCENE_XML_VERSION;

  static {
    try {
      SCENE_XML_URI =
        new URI("http://schemas.io7m.com/renderer/1.0.0/sandbox-scene");
      SCENE_XML_VERSION = 7;
    } catch (final URISyntaxException e) {
      throw new UnreachableCodeException();
    }
  }

  public static @Nonnull SBSceneDescription empty()
  {
    final PSet<PathVirtual> textures_2d = HashTreePSet.empty();
    final PSet<PathVirtual> textures_cube = HashTreePSet.empty();
    final PSet<PathVirtual> meshes = HashTreePSet.empty();
    final PMap<Integer, KLight> lights = HashTreePMap.empty();
    final PMap<Integer, SBInstanceDescription> instances =
      HashTreePMap.empty();

    return new SBSceneDescription(
      textures_2d,
      textures_cube,
      meshes,
      lights,
      instances);
  }

  static @Nonnull SBSceneDescription fromXML(
    final @Nonnull Element e)
    throws RXMLException,
      ConstraintError
  {
    RXMLUtilities
      .checkIsElement(e, "scene", SBSceneDescription.SCENE_XML_URI);
    final Attribute a =
      RXMLUtilities.getAttribute(
        e,
        "version",
        SBSceneDescription.SCENE_XML_URI);

    final int version = RXMLUtilities.getAttributeInteger(a);
    if (version != SBSceneDescription.SCENE_XML_VERSION) {
      final StringBuilder message = new StringBuilder();
      message.append("Scene version is ");
      message.append(version);
      message.append(" but supported versions are: ");
      message.append(SBSceneDescription.SCENE_XML_VERSION);
      throw new RXMLException.RXMLExceptionValidityError(
        new ValidityException(message.toString()));
    }

    return new SBSceneDescription(
      SBSceneDescription.fromXMLTextures2D(e),
      SBSceneDescription.fromXMLTexturesCube(e),
      SBSceneDescription.fromXMLMeshes(e),
      SBSceneDescription.fromXMLLights(e),
      SBSceneDescription.fromXMLInstances(e));
  }

  private static @Nonnull
    PMap<Integer, SBInstanceDescription>
    fromXMLInstances(
      final @Nonnull Element e)
      throws RXMLException,
        ConstraintError
  {
    final HashMap<Integer, SBInstanceDescription> m =
      new HashMap<Integer, SBInstanceDescription>();

    final Element ec =
      RXMLUtilities
        .getChild(e, "instances", SBSceneDescription.SCENE_XML_URI);

    final Elements ecc =
      ec.getChildElements(
        "instance",
        SBSceneDescription.SCENE_XML_URI.toString());

    for (int index = 0; index < ecc.size(); ++index) {
      final Element ei = ecc.get(index);
      final SBInstanceDescription d = SBInstanceDescription.fromXML(ei);
      m.put(d.getID(), d);
    }

    return HashTreePMap.from(m);
  }

  private static @Nonnull PMap<Integer, KLight> fromXMLLights(
    final @Nonnull Element e)
    throws RXMLException,
      ConstraintError
  {
    final HashMap<Integer, KLight> m = new HashMap<Integer, KLight>();

    final Element ec =
      RXMLUtilities.getChild(e, "lights", SBSceneDescription.SCENE_XML_URI);

    final Elements ecc = ec.getChildElements();

    for (int index = 0; index < ecc.size(); ++index) {
      final Element ei = ecc.get(index);
      final KLight d = SBSceneDescription.loadLight(ei);
      m.put(d.getID(), d);
    }

    return HashTreePMap.from(m);
  }

  private static @Nonnull PSet<PathVirtual> fromXMLMeshes(
    final @Nonnull Element e)
    throws RXMLException,
      ConstraintError
  {
    final HashSet<PathVirtual> m = new HashSet<PathVirtual>();

    final Element ec =
      RXMLUtilities.getChild(e, "meshes", SBSceneDescription.SCENE_XML_URI);

    final Elements ecc =
      ec
        .getChildElements("mesh", SBSceneDescription.SCENE_XML_URI.toString());

    for (int index = 0; index < ecc.size(); ++index) {
      final Element me = ecc.get(index);
      final String mep = RXMLUtilities.getElementNonEmptyString(me);
      m.add(PathVirtual.ofString(mep));
    }

    return HashTreePSet.from(m);
  }

  private static @Nonnull PSet<PathVirtual> fromXMLTextures2D(
    final @Nonnull Element e)
    throws RXMLException,
      ConstraintError
  {
    final HashSet<PathVirtual> m = new HashSet<PathVirtual>();

    final Element ec =
      RXMLUtilities.getChild(
        e,
        "textures-2d",
        SBSceneDescription.SCENE_XML_URI);

    final Elements ecc =
      ec.getChildElements(
        "texture-2d",
        SBSceneDescription.SCENE_XML_URI.toString());

    for (int index = 0; index < ecc.size(); ++index) {
      final Element me = ecc.get(index);
      final String mep = RXMLUtilities.getElementNonEmptyString(me);
      m.add(PathVirtual.ofString(mep));
    }

    return HashTreePSet.from(m);
  }

  private static @Nonnull PSet<PathVirtual> fromXMLTexturesCube(
    final @Nonnull Element e)
    throws RXMLException,
      ConstraintError
  {
    final HashSet<PathVirtual> m = new HashSet<PathVirtual>();

    final Element ec =
      RXMLUtilities.getChild(
        e,
        "textures-cube",
        SBSceneDescription.SCENE_XML_URI);

    final Elements ecc =
      ec.getChildElements(
        "texture-cube",
        SBSceneDescription.SCENE_XML_URI.toString());

    for (int index = 0; index < ecc.size(); ++index) {
      final Element me = ecc.get(index);
      final String mep = RXMLUtilities.getElementNonEmptyString(me);
      m.add(PathVirtual.ofString(mep));
    }

    return HashTreePSet.from(m);
  }

  private static @Nonnull Element lightToXML(
    final @Nonnull KLight light)
  {
    final String uri = SBSceneDescription.SCENE_XML_URI.toString();

    final Element eid = new Element("s:id", uri);
    eid.appendChild(light.getID().toString());

    final Element ei = new Element("s:intensity", uri);
    ei.appendChild(Float.toString(light.getIntensity()));

    final Element ec = new Element("s:colour", uri);
    final Element ecr = new Element("s:r", uri);
    ecr.appendChild(Float.toString(light.getColour().getXF()));
    final Element ecg = new Element("s:g", uri);
    ecg.appendChild(Float.toString(light.getColour().getYF()));
    final Element ecb = new Element("s:b", uri);
    ecb.appendChild(Float.toString(light.getColour().getZF()));
    ec.appendChild(ecr);
    ec.appendChild(ecg);
    ec.appendChild(ecb);

    switch (light.getType()) {
      case LIGHT_CONE:
      {
        throw new UnimplementedCodeException();
      }
      case LIGHT_DIRECTIONAL:
      {
        final KDirectional d = (KLight.KDirectional) light;
        final Element e = new Element("s:light-directional", uri);

        final Element ed = new Element("s:direction", uri);
        final Element edx = new Element("s:x", uri);
        edx.appendChild(Float.toString(d.getDirection().getXF()));
        final Element edy = new Element("s:y", uri);
        edy.appendChild(Float.toString(d.getDirection().getYF()));
        final Element edz = new Element("s:z", uri);
        edz.appendChild(Float.toString(d.getDirection().getZF()));
        ed.appendChild(edx);
        ed.appendChild(edy);
        ed.appendChild(edz);

        e.appendChild(eid);
        e.appendChild(ec);
        e.appendChild(ei);
        e.appendChild(ed);
        return e;
      }
      case LIGHT_SPHERE:
      {
        final KSphere d = (KLight.KSphere) light;
        final Element e = new Element("s:light-spherical", uri);

        final Element ed = new Element("s:position", uri);
        final Element edx = new Element("s:x", uri);
        edx.appendChild(Float.toString(d.getPosition().getXF()));
        final Element edy = new Element("s:y", uri);
        edy.appendChild(Float.toString(d.getPosition().getYF()));
        final Element edz = new Element("s:z", uri);
        edz.appendChild(Float.toString(d.getPosition().getZF()));
        ed.appendChild(edx);
        ed.appendChild(edy);
        ed.appendChild(edz);

        final Element er = new Element("s:radius", uri);
        er.appendChild(Float.toString(d.getRadius()));
        final Element ef = new Element("s:falloff", uri);
        ef.appendChild(Float.toString(d.getExponent()));

        e.appendChild(eid);
        e.appendChild(ec);
        e.appendChild(ei);
        e.appendChild(ed);
        e.appendChild(er);
        e.appendChild(ef);
        return e;
      }
    }

    throw new UnreachableCodeException();
  }

  private static @Nonnull KLight loadLight(
    final @Nonnull Element e)
    throws RXMLException,
      ConstraintError
  {
    if (e.getLocalName().equals("light-directional")) {
      return SBSceneDescription.loadLightDirectional(e);
    }
    if (e.getLocalName().equals("light-spherical")) {
      return SBSceneDescription.loadLightSpherical(e);
    }

    throw new UnimplementedCodeException();
  }

  private static @Nonnull KLight.KDirectional loadLightDirectional(
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
    final RVectorI3F<RSpaceRGB> colour = RXMLUtilities.getElementRGB(ec, uri);
    final int id = RXMLUtilities.getElementInteger(eid);
    final float intensity = RXMLUtilities.getElementFloat(ei);

    return new KLight.KDirectional(
      Integer.valueOf(id),
      direction,
      colour,
      intensity);
  }

  private static @Nonnull KSphere loadLightSpherical(
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
    final Element er = RXMLUtilities.getChild(e, "radius", uri);
    final Element ef = RXMLUtilities.getChild(e, "falloff", uri);

    final RVectorI3F<RSpaceWorld> position =
      RXMLUtilities.getElementVector3f(ep, uri);
    final RVectorI3F<RSpaceRGB> colour = RXMLUtilities.getElementRGB(ec, uri);
    final int id = RXMLUtilities.getElementInteger(eid);
    final float intensity = RXMLUtilities.getElementFloat(ei);
    final float radius = RXMLUtilities.getElementFloat(er);
    final float falloff = RXMLUtilities.getElementFloat(ef);

    return new KLight.KSphere(
      Integer.valueOf(id),
      colour,
      intensity,
      position,
      radius,
      falloff);
  }

  private final @Nonnull PSet<PathVirtual>                    textures_2d;
  private final @Nonnull PSet<PathVirtual>                    textures_cube;
  private final @Nonnull PSet<PathVirtual>                    meshes;
  private final @Nonnull PMap<Integer, KLight>                lights;
  private final @Nonnull PMap<Integer, SBInstanceDescription> instances;

  public @Nonnull PSet<PathVirtual> getTextures2D()
  {
    return this.textures_2d;
  }

  public @Nonnull PSet<PathVirtual> getTexturesCube()
  {
    return this.textures_cube;
  }

  public @Nonnull PSet<PathVirtual> getMeshes()
  {
    return this.meshes;
  }

  private SBSceneDescription(
    final @Nonnull PSet<PathVirtual> textures_2d,
    final @Nonnull PSet<PathVirtual> textures_cube,
    final @Nonnull PSet<PathVirtual> models,
    final @Nonnull PMap<Integer, KLight> lights,
    final @Nonnull PMap<Integer, SBInstanceDescription> instances)
  {
    this.textures_2d = textures_2d;
    this.textures_cube = textures_cube;
    this.meshes = models;
    this.lights = lights;
    this.instances = instances;
  }

  @Override public boolean equals(
    final Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final SBSceneDescription other = (SBSceneDescription) obj;
    if (!this.instances.equals(other.instances)) {
      return false;
    }
    if (!this.lights.equals(other.lights)) {
      return false;
    }
    if (!this.meshes.equals(other.meshes)) {
      return false;
    }
    if (!this.textures_2d.equals(other.textures_2d)) {
      return false;
    }
    if (!this.textures_cube.equals(other.textures_cube)) {
      return false;
    }
    return true;
  }

  public @Nonnull Collection<SBInstanceDescription> getInstanceDescriptions()
  {
    return this.instances.values();
  }

  public @Nonnull Collection<KLight> getLights()
  {
    return this.lights.values();
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.instances.hashCode();
    result = (prime * result) + this.lights.hashCode();
    result = (prime * result) + this.meshes.hashCode();
    result = (prime * result) + this.textures_2d.hashCode();
    return result;
  }

  public @Nonnull SBSceneDescription instanceAdd(
    final @Nonnull SBInstanceDescription d)
  {
    return new SBSceneDescription(
      this.textures_2d,
      this.textures_cube,
      this.meshes,
      this.lights,
      this.instances.plus(d.getID(), d));
  }

  public @Nonnull SBSceneDescription lightAdd(
    final @Nonnull KLight l)
  {
    return new SBSceneDescription(
      this.textures_2d,
      this.textures_cube,
      this.meshes,
      this.lights.plus(l.getID(), l),
      this.instances);
  }

  public @Nonnull SBSceneDescription meshAdd(
    final @Nonnull PathVirtual d)
  {
    return new SBSceneDescription(
      this.textures_2d,
      this.textures_cube,
      this.meshes.plus(d),
      this.lights,
      this.instances);
  }

  public @Nonnull SBSceneDescription texture2DAdd(
    final @Nonnull PathVirtual t)
  {
    return new SBSceneDescription(
      this.textures_2d.plus(t),
      this.textures_cube,
      this.meshes,
      this.lights,
      this.instances);
  }

  public @Nonnull SBSceneDescription textureCubeAdd(
    final @Nonnull PathVirtual t)
  {
    return new SBSceneDescription(
      this.textures_2d,
      this.textures_cube.plus(t),
      this.meshes,
      this.lights,
      this.instances);
  }

  @Nonnull Element toXML()
  {
    final String uri = SBSceneDescription.SCENE_XML_URI.toString();
    final Element e = new Element("s:scene", uri);
    e.addAttribute(new Attribute("s:version", uri, Integer
      .toString(SBSceneDescription.SCENE_XML_VERSION)));

    final Element et2 = new Element("s:textures-2d", uri);
    for (final PathVirtual t : this.textures_2d) {
      final Element ete = new Element("s:texture-2d", uri);
      ete.appendChild(t.toString());
      et2.appendChild(ete);
    }

    final Element etc = new Element("s:textures-cube", uri);
    for (final PathVirtual t : this.textures_cube) {
      final Element ete = new Element("s:texture-cube", uri);
      ete.appendChild(t.toString());
      etc.appendChild(ete);
    }

    final Element em = new Element("s:meshes", uri);
    for (final PathVirtual m : this.meshes) {
      final Element eme = new Element("s:mesh", uri);
      eme.appendChild(m.toString());
      em.appendChild(eme);
    }

    final Element el = new Element("s:lights", uri);
    for (final KLight l : this.lights.values()) {
      el.appendChild(SBSceneDescription.lightToXML(l));
    }

    final Element ei = new Element("s:instances", uri);
    for (final SBInstanceDescription i : this.instances.values()) {
      ei.appendChild(i.toXML());
    }

    e.appendChild(et2);
    e.appendChild(etc);
    e.appendChild(em);
    e.appendChild(el);
    e.appendChild(ei);
    return e;
  }
}
