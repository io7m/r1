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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.zip.ZipOutputStream;

import javax.annotation.Nonnull;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Pair;
import com.io7m.jlog.Level;
import com.io7m.jlog.Log;
import com.io7m.jvvfs.Filesystem;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.jvvfs.PathVirtual;
import com.io7m.renderer.xml.RXMLException;
import com.io7m.renderer.xml.cubemap.CubeMap;
import com.thoughtworks.xstream.XStream;

public final class SBSceneFilesystem
{
  public static final @Nonnull String       CUBE_MAP_POSITIVE_Z;
  public static final @Nonnull String       CUBE_MAP_POSITIVE_Y;
  public static final @Nonnull String       CUBE_MAP_POSITIVE_X;
  public static final @Nonnull String       CUBE_MAP_NEGATIVE_Z;
  public static final @Nonnull String       CUBE_MAP_NEGATIVE_Y;
  public static final @Nonnull String       CUBE_MAP_NEGATIVE_X;

  private static final @Nonnull PathVirtual PATH_TEXTURES_2D;
  private static final @Nonnull String      NAME_TEXTURES_2D;
  private static final @Nonnull String      NAME_TEXTURES_CUBE;
  private static final @Nonnull PathVirtual PATH_TEXTURES_CUBE;
  private static final @Nonnull String      NAME_MESHES;
  private static final @Nonnull PathVirtual PATH_MESHES;
  private static final int                  MAX_UNIQUE_NAME_TRIES = 1000;

  static {
    try {
      NAME_TEXTURES_2D = "textures";
      PATH_TEXTURES_2D =
        PathVirtual.ofString("/" + SBSceneFilesystem.NAME_TEXTURES_2D);

      NAME_TEXTURES_CUBE = "cubemaps";
      PATH_TEXTURES_CUBE =
        PathVirtual.ofString("/" + SBSceneFilesystem.NAME_TEXTURES_CUBE);

      NAME_MESHES = "meshes";
      PATH_MESHES = PathVirtual.ofString("/" + SBSceneFilesystem.NAME_MESHES);

      CUBE_MAP_POSITIVE_Z = "positive-z";
      CUBE_MAP_POSITIVE_Y = "positive-y";
      CUBE_MAP_POSITIVE_X = "positive-x";
      CUBE_MAP_NEGATIVE_Z = "negative-z";
      CUBE_MAP_NEGATIVE_Y = "negative-y";
      CUBE_MAP_NEGATIVE_X = "negative-x";

    } catch (final ConstraintError e) {
      throw new UnreachableCodeException();
    }
  }

  /**
   * Create a new empty scene.
   */

  public static @Nonnull SBSceneFilesystem filesystemEmpty(
    final @Nonnull Log log)
    throws ConstraintError,
      FilesystemError,
      IOException
  {
    final File td =
      SBIOUtilities.makeTemporaryDirectory(
        log,
        "sandbox/sandbox-scene-",
        SBSceneFilesystem.MAX_UNIQUE_NAME_TRIES);

    SBSceneFilesystem.initDirectories(td);
    final Filesystem fs = Filesystem.makeWithoutArchiveDirectory(log);
    fs.mountArchiveFromAnywhere(td, PathVirtual.ROOT);
    return new SBSceneFilesystem(log, td, fs);
  }

  /**
   * Load a scene from <code>file</code>.
   */

  public static @Nonnull SBSceneFilesystem filesystemLoadScene(
    final @Nonnull Log log,
    final @Nonnull File file)
    throws ConstraintError,
      FilesystemError,
      FileNotFoundException,
      IOException
  {
    final File td =
      SBZipUtilities.unzipToTemporary(
        log,
        file,
        "sandbox/scene-",
        SBSceneFilesystem.MAX_UNIQUE_NAME_TRIES);
    final Filesystem fs = Filesystem.makeWithoutArchiveDirectory(log);
    fs.mountArchiveFromAnywhere(td, PathVirtual.ROOT);
    return new SBSceneFilesystem(log, td, fs);
  }

  private static void initDirectories(
    final @Nonnull File td)
  {
    new File(td, SBSceneFilesystem.NAME_TEXTURES_2D).mkdirs();
    new File(td, SBSceneFilesystem.NAME_TEXTURES_CUBE).mkdirs();
    new File(td, SBSceneFilesystem.NAME_MESHES).mkdirs();
  }

  private static @Nonnull String insertRandomBytesIntoName(
    final @Nonnull byte[] bytes,
    final @Nonnull String name)
  {
    final StringBuilder b = new StringBuilder();
    if (name.contains(".")) {
      b.append(name.substring(0, name.lastIndexOf('.')));
      b.append("-");
      for (final byte x : bytes) {
        b.append(String.format("%x", Byte.valueOf(x)));
      }
      b.append(name.substring(name.lastIndexOf('.')));
    } else {
      b.append(name);
      b.append("-");
      for (final byte x : bytes) {
        b.append(String.format("%x", Byte.valueOf(x)));
      }
    }
    return b.toString();
  }

  private final @Nonnull Filesystem filesystem;

  private final @Nonnull Log        log;
  private final @Nonnull File       root;

  private SBSceneFilesystem(
    final @Nonnull Log log,
    final @Nonnull File actual_root,
    final @Nonnull Filesystem initial)
  {
    this.log = new Log(log, "scene-filesystem");
    this.filesystem = initial;
    this.root = actual_root;
  }

  private @Nonnull PathVirtual copyFileIn(
    final @Nonnull PathVirtual base,
    final @Nonnull File file)
    throws ConstraintError,
      FilesystemError,
      FileNotFoundException,
      IOException
  {
    final String name = file.getName();
    final Pair<PathVirtual, String> result =
      this.getUniqueName(base, name, SBSceneFilesystem.MAX_UNIQUE_NAME_TRIES);

    final File target = new File(this.root, result.first.toString());

    if (this.log.enabled(Level.LOG_DEBUG)) {
      final StringBuilder m = new StringBuilder();
      m.append("copy ");
      m.append(file);
      m.append(" to ");
      m.append(target);
      this.log.debug(m.toString());
    }

    SBIOUtilities.copyFile(file, target);
    return result.first;
  }

  public void filesystemClose()
    throws FilesystemError,
      ConstraintError
  {
    this.filesystem.close();
  }

  public @Nonnull PathVirtual filesystemCopyInMesh(
    final @Nonnull File file)
    throws ConstraintError,
      FileNotFoundException,
      FilesystemError,
      IOException
  {
    Constraints.constrainNotNull(file, "File");
    Constraints.constrainArbitrary(
      file.getName().isEmpty() == false,
      "File name not empty");

    return this.copyFileIn(SBSceneFilesystem.PATH_MESHES, file);
  }

  public @Nonnull PathVirtual filesystemCopyInTexture2D(
    final @Nonnull File file)
    throws ConstraintError,
      FileNotFoundException,
      FilesystemError,
      IOException
  {
    Constraints.constrainNotNull(file, "File");
    Constraints.constrainArbitrary(
      file.getName().isEmpty() == false,
      "File name not empty");

    return this.copyFileIn(SBSceneFilesystem.PATH_TEXTURES_2D, file);
  }

  public @Nonnull PathVirtual filesystemCopyInTextureCube(
    final @Nonnull File file)
    throws ConstraintError,
      ValidityException,
      ParsingException,
      IOException,
      RXMLException,
      FilesystemError
  {
    Constraints.constrainNotNull(file, "File");

    final Builder b = new Builder();
    final Document document = b.build(file);
    final CubeMap map = CubeMap.fromXML(document.getRootElement());

    final File file_parent = file.getParentFile();
    Constraints.constrainNotNull(file_parent, "File parent");
    Constraints.constrainArbitrary(
      file_parent.isDirectory(),
      "Parent of file is directory");

    /**
     * Create a new directory in the cube maps directory with a unique name
     * based on the name given in the cube map XML.
     */

    final Pair<PathVirtual, String> cube_map_directory =
      this.getUniqueName(
        SBSceneFilesystem.PATH_TEXTURES_CUBE,
        map.getName(),
        SBSceneFilesystem.MAX_UNIQUE_NAME_TRIES);

    final File cube_map_directory_real =
      new File(
        new File(this.root, SBSceneFilesystem.NAME_TEXTURES_CUBE),
        cube_map_directory.second);

    if (cube_map_directory_real.mkdirs() == false) {
      throw new IOException("Could not create directory: "
        + cube_map_directory_real);
    }

    /**
     * Copy in the cube map images to predictable filenames.
     */

    SBIOUtilities
      .copyFile(new File(file_parent, map.getPositiveZ()), new File(
        cube_map_directory_real,
        SBSceneFilesystem.CUBE_MAP_POSITIVE_Z));
    SBIOUtilities
      .copyFile(new File(file_parent, map.getPositiveY()), new File(
        cube_map_directory_real,
        SBSceneFilesystem.CUBE_MAP_POSITIVE_Y));
    SBIOUtilities
      .copyFile(new File(file_parent, map.getPositiveX()), new File(
        cube_map_directory_real,
        SBSceneFilesystem.CUBE_MAP_POSITIVE_X));
    SBIOUtilities
      .copyFile(new File(file_parent, map.getNegativeZ()), new File(
        cube_map_directory_real,
        SBSceneFilesystem.CUBE_MAP_NEGATIVE_Z));
    SBIOUtilities
      .copyFile(new File(file_parent, map.getNegativeY()), new File(
        cube_map_directory_real,
        SBSceneFilesystem.CUBE_MAP_NEGATIVE_Y));
    SBIOUtilities
      .copyFile(new File(file_parent, map.getNegativeX()), new File(
        cube_map_directory_real,
        SBSceneFilesystem.CUBE_MAP_NEGATIVE_X));

    return cube_map_directory.first;
  }

  public @Nonnull InputStream filesystemOpenFile(
    final @Nonnull PathVirtual path)
    throws FilesystemError,
      ConstraintError
  {
    return this.filesystem.openFile(path);
  }

  /**
   * Save a scene to the zip file <code>file</code>.
   */

  public void filesystemSave(
    final @Nonnull SBSceneDescription description,
    final @Nonnull File file)
    throws IOException
  {
    final ZipOutputStream fo =
      new ZipOutputStream(new FileOutputStream(file));
    fo.setLevel(9);

    this.sceneSaveSerializeXML(description);
    SBZipUtilities.zipToStream(this.log, fo, this.root);

    fo.finish();
    fo.flush();
    fo.close();
  }

  private @Nonnull Pair<PathVirtual, String> getUniqueName(
    final @Nonnull PathVirtual base,
    final @Nonnull String name,
    final int tries)
    throws ConstraintError,
      FilesystemError,
      IOException
  {
    final SecureRandom r = new SecureRandom();
    final byte[] bytes = new byte[8];
    final StringBuilder name_buffer = new StringBuilder(name);

    int count = 0;
    while (count < tries) {
      final PathVirtual dest = base.appendName(name_buffer.toString());
      if (this.filesystem.exists(dest)) {
        name_buffer.setLength(0);
        r.nextBytes(bytes);
        name_buffer.append(SBSceneFilesystem.insertRandomBytesIntoName(
          bytes,
          name));
        ++count;
      } else {
        final String new_name = name_buffer.toString();
        final PathVirtual path = base.appendName(new_name);
        return new Pair<PathVirtual, String>(path, new_name);
      }
    }

    throw new IOException("Could not create unique filename for " + name);
  }

  @Nonnull File root()
  {
    return this.root;
  }

  private void sceneSaveSerializeXML(
    final @Nonnull SBSceneDescription description)
    throws IOException
  {
    final FileOutputStream out =
      new FileOutputStream(new File(this.root, "scene.xml"));

    final XStream stream = new XStream();
    stream.toXML(description, out);
    out.close();
  }
}
