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

import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ValidityException;

import org.pcollections.HashTreePMap;
import org.pcollections.PMap;

import com.io7m.jaux.UnimplementedCodeException;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.renderer.RSpaceRGB;
import com.io7m.renderer.RSpaceWorld;
import com.io7m.renderer.RVectorI3F;
import com.io7m.renderer.kernel.KLight.KDirectional;
import com.io7m.renderer.kernel.KLight.KSphere;
import com.io7m.renderer.kernel.SBZipUtilities.BaseDirectory;

@Immutable final class SBSceneDescription
{
  static final @Nonnull URI    SCENE_XML_URI;
  static final @Nonnull String SCENE_XML_VERSION;

  static {
    try {
      SCENE_XML_URI = new URI("http://io7m.com/software/renderer");
      SCENE_XML_VERSION = "1";
    } catch (final URISyntaxException e) {
      throw new UnreachableCodeException();
    }
  }

  public static @Nonnull SBSceneDescription empty()
  {
    final PMap<String, SBTextureDescription> textures = HashTreePMap.empty();
    final PMap<String, SBModelDescription> models = HashTreePMap.empty();
    final PMap<Integer, KLight> lights = HashTreePMap.empty();
    final PMap<Integer, SBInstanceDescription> instances =
      HashTreePMap.empty();

    return new SBSceneDescription(textures, models, lights, instances);
  }

  static @Nonnull SBSceneDescription fromXML(
    final @Nonnull BaseDirectory base,
    final @Nonnull Element e)
    throws ValidityException
  {
    return new SBSceneDescription(
      SBSceneDescription.fromXMLTextures(base, e),
      SBSceneDescription.fromXMLModels(base, e),
      SBSceneDescription.fromXMLLights(e),
      SBSceneDescription.fromXMLInstances(e));
  }

  private static @Nonnull
    PMap<Integer, SBInstanceDescription>
    fromXMLInstances(
      final @Nonnull Element e)
      throws ValidityException
  {
    final HashMap<Integer, SBInstanceDescription> m =
      new HashMap<Integer, SBInstanceDescription>();

    final Element ec =
      SBXMLUtilities.getChild(
        e,
        "instances",
        SBSceneDescription.SCENE_XML_URI);

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
    throws ValidityException
  {
    final HashMap<Integer, KLight> m = new HashMap<Integer, KLight>();

    final Element ec =
      SBXMLUtilities.getChild(e, "lights", SBSceneDescription.SCENE_XML_URI);

    final Elements ecc = ec.getChildElements();

    for (int index = 0; index < ecc.size(); ++index) {
      final Element ei = ecc.get(index);
      final KLight d = SBSceneDescription.loadLight(ei);
      m.put(d.getID(), d);
    }

    return HashTreePMap.from(m);
  }

  private static @Nonnull PMap<String, SBModelDescription> fromXMLModels(
    final @Nonnull BaseDirectory base,
    final @Nonnull Element e)
    throws ValidityException
  {
    final HashMap<String, SBModelDescription> m =
      new HashMap<String, SBModelDescription>();

    final Element ec =
      SBXMLUtilities.getChild(e, "models", SBSceneDescription.SCENE_XML_URI);

    final Elements ecc =
      ec.getChildElements(
        "model",
        SBSceneDescription.SCENE_XML_URI.toString());

    for (int index = 0; index < ecc.size(); ++index) {
      final Element ei = ecc.get(index);
      final SBModelDescription d = SBModelDescription.fromXML(base, ei);
      m.put(d.getName(), d);
    }

    return HashTreePMap.from(m);
  }

  private static @Nonnull PMap<String, SBTextureDescription> fromXMLTextures(
    final @Nonnull BaseDirectory base,
    final @Nonnull Element e)
    throws ValidityException
  {
    final HashMap<String, SBTextureDescription> m =
      new HashMap<String, SBTextureDescription>();

    final Element ec =
      SBXMLUtilities
        .getChild(e, "textures", SBSceneDescription.SCENE_XML_URI);

    final Elements ecc =
      ec.getChildElements(
        "texture",
        SBSceneDescription.SCENE_XML_URI.toString());

    for (int index = 0; index < ecc.size(); ++index) {
      final Element ei = ecc.get(index);
      final SBTextureDescription d = SBTextureDescription.fromXML(base, ei);
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
    throws ValidityException
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
    throws ValidityException
  {
    final URI uri = SBSceneDescription.SCENE_XML_URI;
    SBXMLUtilities.checkIsElement(e, "light-directional", uri);

    final Element ed = SBXMLUtilities.getChild(e, "direction", uri);
    final Element ec = SBXMLUtilities.getChild(e, "colour", uri);
    final Element ei = SBXMLUtilities.getChild(e, "intensity", uri);
    final Element eid = SBXMLUtilities.getChild(e, "id", uri);

    final RVectorI3F<RSpaceWorld> direction =
      SBXMLUtilities.getVector3f(ed, uri);
    final RVectorI3F<RSpaceRGB> colour = SBXMLUtilities.getRGB(ec, uri);
    final Integer id = SBXMLUtilities.getInteger(eid);
    final float intensity = SBXMLUtilities.getFloat(ei);

    return new KLight.KDirectional(id, direction, colour, intensity);
  }

  private static @Nonnull KSphere loadLightSpherical(
    final @Nonnull Element e)
    throws ValidityException
  {
    final URI uri = SBSceneDescription.SCENE_XML_URI;
    SBXMLUtilities.checkIsElement(e, "light-spherical", uri);

    final Element ep = SBXMLUtilities.getChild(e, "position", uri);
    final Element ec = SBXMLUtilities.getChild(e, "colour", uri);
    final Element ei = SBXMLUtilities.getChild(e, "intensity", uri);
    final Element eid = SBXMLUtilities.getChild(e, "id", uri);
    final Element er = SBXMLUtilities.getChild(e, "radius", uri);
    final Element ef = SBXMLUtilities.getChild(e, "falloff", uri);

    final RVectorI3F<RSpaceWorld> position =
      SBXMLUtilities.getVector3f(ep, uri);
    final RVectorI3F<RSpaceRGB> colour = SBXMLUtilities.getRGB(ec, uri);
    final Integer id = SBXMLUtilities.getInteger(eid);
    final float intensity = SBXMLUtilities.getFloat(ei);
    final float radius = SBXMLUtilities.getFloat(er);
    final float falloff = SBXMLUtilities.getFloat(ef);

    return new KLight.KSphere(
      id,
      colour,
      intensity,
      position,
      radius,
      falloff);
  }

  private final @Nonnull PMap<String, SBTextureDescription>   textures;
  private final @Nonnull PMap<String, SBModelDescription>     models;
  private final @Nonnull PMap<Integer, KLight>                lights;
  private final @Nonnull PMap<Integer, SBInstanceDescription> instances;

  private SBSceneDescription(
    final @Nonnull PMap<String, SBTextureDescription> textures,
    final @Nonnull PMap<String, SBModelDescription> models,
    final @Nonnull PMap<Integer, KLight> lights,
    final @Nonnull PMap<Integer, SBInstanceDescription> instances)
  {
    this.textures = textures;
    this.models = models;
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
    if (!this.models.equals(other.models)) {
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

  public @Nonnull Collection<SBModelDescription> getModelDescriptions()
  {
    return this.models.values();
  }

  public @Nonnull Collection<SBTextureDescription> getTextureDescriptions()
  {
    return this.textures.values();
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.instances.hashCode();
    result = (prime * result) + this.lights.hashCode();
    result = (prime * result) + this.models.hashCode();
    result = (prime * result) + this.textures.hashCode();
    return result;
  }

  public @Nonnull SBSceneDescription instanceAdd(
    final @Nonnull SBInstanceDescription d)
  {
    return new SBSceneDescription(
      this.textures,
      this.models,
      this.lights,
      this.instances.plus(d.getID(), d));
  }

  public @Nonnull SBSceneDescription lightAdd(
    final @Nonnull KLight l)
  {
    return new SBSceneDescription(
      this.textures,
      this.models,
      this.lights.plus(l.getID(), l),
      this.instances);
  }

  public @Nonnull SBSceneDescription modelAdd(
    final @Nonnull SBModelDescription d)
  {
    return new SBSceneDescription(this.textures, this.models.plus(
      d.getName(),
      d), this.lights, this.instances);
  }

  public @Nonnull SBSceneDescription textureAdd(
    final @Nonnull SBTextureDescription t)
  {
    return new SBSceneDescription(
      this.textures.plus(t.getName(), t),
      this.models,
      this.lights,
      this.instances);
  }

  @Nonnull Element toXML()
  {
    final String uri = SBSceneDescription.SCENE_XML_URI.toString();
    final Element e = new Element("s:scene", uri);

    final Element et = new Element("s:textures", uri);
    for (final SBTextureDescription t : this.textures.values()) {
      et.appendChild(t.toXML());
    }

    final Element em = new Element("s:models", uri);
    for (final SBModelDescription m : this.models.values()) {
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

  public @CheckForNull SBTextureDescription getTexture(
    final @Nonnull String name)
  {
    return this.textures.get(name);
  }

  public @CheckForNull SBModelDescription getModel(
    final @Nonnull String name)
  {
    return this.models.get(name);
  }
}
