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

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ValidityException;

import org.pcollections.HashTreePMap;
import org.pcollections.PMap;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnimplementedCodeException;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.renderer.RSpaceRGB;
import com.io7m.renderer.RSpaceWorld;
import com.io7m.renderer.RVectorI3F;
import com.io7m.renderer.kernel.KLight.KDirectional;
import com.io7m.renderer.kernel.KLight.KSphere;
import com.io7m.renderer.kernel.SBZipUtilities.BaseDirectory;
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
      SCENE_XML_VERSION = 5;
    } catch (final URISyntaxException e) {
      throw new UnreachableCodeException();
    }
  }

  public static @Nonnull SBSceneDescription empty()
  {
    final PMap<String, SBTexture2DDescription> textures =
      HashTreePMap.empty();
    final PMap<String, SBMeshDescription> meshes = HashTreePMap.empty();
    final PMap<Integer, KLight> lights = HashTreePMap.empty();
    final PMap<Integer, SBInstanceDescription> instances =
      HashTreePMap.empty();

    return new SBSceneDescription(textures, meshes, lights, instances);
  }

  static @Nonnull SBSceneDescription fromXML(
    final @Nonnull BaseDirectory base,
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
      SBSceneDescription.fromXMLTextures(base, e),
      SBSceneDescription.fromXMLMeshes(base, e),
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

  private static @Nonnull PMap<String, SBMeshDescription> fromXMLMeshes(
    final @Nonnull BaseDirectory base,
    final @Nonnull Element e)
    throws RXMLException,
      ConstraintError
  {
    final HashMap<String, SBMeshDescription> m =
      new HashMap<String, SBMeshDescription>();

    final Element ec =
      RXMLUtilities.getChild(e, "meshes", SBSceneDescription.SCENE_XML_URI);

    final Elements ecc =
      ec
        .getChildElements("mesh", SBSceneDescription.SCENE_XML_URI.toString());

    for (int index = 0; index < ecc.size(); ++index) {
      final Element ei = ecc.get(index);
      final SBMeshDescription d = SBMeshDescription.fromXML(base, ei);
      m.put(d.getName(), d);
    }

    return HashTreePMap.from(m);
  }

  private static @Nonnull
    PMap<String, SBTexture2DDescription>
    fromXMLTextures(
      final @Nonnull BaseDirectory base,
      final @Nonnull Element e)
      throws RXMLException,
        ConstraintError
  {
    final HashMap<String, SBTexture2DDescription> m =
      new HashMap<String, SBTexture2DDescription>();

    final Element ec =
      RXMLUtilities.getChild(e, "textures", SBSceneDescription.SCENE_XML_URI);

    final Elements ecc =
      ec.getChildElements(
        "texture",
        SBSceneDescription.SCENE_XML_URI.toString());

    for (int index = 0; index < ecc.size(); ++index) {
      final Element ei = ecc.get(index);
      final SBTexture2DDescription d =
        SBTexture2DDescription.fromXML(base, ei);
      m.put(d.getName(), d);
    }

    return HashTreePMap.from(m);
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

  private final @Nonnull PMap<String, SBTexture2DDescription> textures;
  private final @Nonnull PMap<String, SBMeshDescription>      meshes;
  private final @Nonnull PMap<Integer, KLight>                lights;
  private final @Nonnull PMap<Integer, SBInstanceDescription> instances;

  private SBSceneDescription(
    final @Nonnull PMap<String, SBTexture2DDescription> textures,
    final @Nonnull PMap<String, SBMeshDescription> models,
    final @Nonnull PMap<Integer, KLight> lights,
    final @Nonnull PMap<Integer, SBInstanceDescription> instances)
  {
    this.textures = textures;
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
    if (!this.textures.equals(other.textures)) {
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

  public @CheckForNull SBMeshDescription getMesh(
    final @Nonnull String name)
  {
    return this.meshes.get(name);
  }

  public @Nonnull Collection<SBMeshDescription> getMeshDescriptions()
  {
    return this.meshes.values();
  }

  public @CheckForNull SBTexture2DDescription getTexture(
    final @Nonnull String name)
  {
    return this.textures.get(name);
  }

  public @Nonnull Collection<SBTexture2DDescription> getTextureDescriptions()
  {
    return this.textures.values();
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.instances.hashCode();
    result = (prime * result) + this.lights.hashCode();
    result = (prime * result) + this.meshes.hashCode();
    result = (prime * result) + this.textures.hashCode();
    return result;
  }

  public @Nonnull SBSceneDescription instanceAdd(
    final @Nonnull SBInstanceDescription d)
  {
    return new SBSceneDescription(
      this.textures,
      this.meshes,
      this.lights,
      this.instances.plus(d.getID(), d));
  }

  public @Nonnull SBSceneDescription lightAdd(
    final @Nonnull KLight l)
  {
    return new SBSceneDescription(
      this.textures,
      this.meshes,
      this.lights.plus(l.getID(), l),
      this.instances);
  }

  public @Nonnull SBSceneDescription meshAdd(
    final @Nonnull SBMeshDescription d)
  {
    return new SBSceneDescription(this.textures, this.meshes.plus(
      d.getName(),
      d), this.lights, this.instances);
  }

  public @Nonnull SBSceneDescription textureAdd(
    final @Nonnull SBTexture2DDescription t)
  {
    return new SBSceneDescription(
      this.textures.plus(t.getName(), t),
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

    final Element et = new Element("s:textures", uri);
    for (final SBTexture2DDescription t : this.textures.values()) {
      et.appendChild(t.toXML());
    }

    final Element em = new Element("s:meshes", uri);
    for (final SBMeshDescription m : this.meshes.values()) {
      em.appendChild(m.toXML());
    }

    final Element el = new Element("s:lights", uri);
    for (final KLight l : this.lights.values()) {
      el.appendChild(SBSceneDescription.lightToXML(l));
    }

    final Element ei = new Element("s:instances", uri);
    for (final SBInstanceDescription i : this.instances.values()) {
      ei.appendChild(i.toXML());
    }

    e.appendChild(et);
    e.appendChild(em);
    e.appendChild(el);
    e.appendChild(ei);
    return e;
  }
}
