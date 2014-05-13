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

package com.io7m.renderer.kernel.sandbox;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.zip.ZipOutputStream;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import com.io7m.jfunctional.Pair;
import com.io7m.jlog.LogLevel;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.jvvfs.Filesystem;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.jvvfs.FilesystemType;
import com.io7m.jvvfs.PathVirtual;
import com.io7m.renderer.types.RXMLException;
import com.io7m.renderer.xml.cubemap.CubeMap;
import com.thoughtworks.xstream.XStream;

public final class SBSceneFilesystem
{
  public static final String       CUBE_MAP_POSITIVE_Z;
  public static final String       CUBE_MAP_POSITIVE_Y;
  public static final String       CUBE_MAP_POSITIVE_X;
  public static final String       CUBE_MAP_NEGATIVE_Z;
  public static final String       CUBE_MAP_NEGATIVE_Y;
  public static final String       CUBE_MAP_NEGATIVE_X;

  private static final PathVirtual PATH_TEXTURES_2D;
  private static final String      NAME_TEXTURES_2D;
  private static final String      NAME_TEXTURES_CUBE;
  private static final PathVirtual PATH_TEXTURES_CUBE;
  private static final String      NAME_MESHES;
  private static final PathVirtual PATH_MESHES;
  private static final int         MAX_UNIQUE_NAME_TRIES = 1000;

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

    } catch (final FilesystemError e) {
      throw new UnreachableCodeException();
    }
  }

  /**
   * Create a new empty scene.
   */

  public static SBSceneFilesystem filesystemEmpty(
    final LogUsableType log)
    throws FilesystemError,
      IOException
  {
    final File td =
      SBIOUtilities.makeTemporaryDirectory(
        log,
        "sandbox/sandbox-scene-",
        SBSceneFilesystem.MAX_UNIQUE_NAME_TRIES);

    SBSceneFilesystem.initDirectories(td);
    final FilesystemType fs = Filesystem.makeWithoutArchiveDirectory(log);
    fs.mountArchiveFromAnywhere(td, PathVirtual.ROOT);
    return new SBSceneFilesystem(log, td, fs);
  }

  /**
   * Load a scene from <code>file</code>.
   */

  public static SBSceneFilesystem filesystemLoadScene(
    final LogUsableType log,
    final File file)
    throws FilesystemError,
      FileNotFoundException,
      IOException
  {
    final File td =
      SBZipUtilities.unzipToTemporary(
        log,
        file,
        "sandbox/scene-",
        SBSceneFilesystem.MAX_UNIQUE_NAME_TRIES);
    final FilesystemType fs = Filesystem.makeWithoutArchiveDirectory(log);
    fs.mountArchiveFromAnywhere(td, PathVirtual.ROOT);
    return new SBSceneFilesystem(log, td, fs);
  }

  private static void initDirectories(
    final File td)
  {
    new File(td, SBSceneFilesystem.NAME_TEXTURES_2D).mkdirs();
    new File(td, SBSceneFilesystem.NAME_TEXTURES_CUBE).mkdirs();
    new File(td, SBSceneFilesystem.NAME_MESHES).mkdirs();
  }

  private static String insertRandomBytesIntoName(
    final byte[] bytes,
    final String name)
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

  private final FilesystemType filesystem;
  private final LogUsableType  log;
  private final File           root;

  private SBSceneFilesystem(
    final LogUsableType in_log,
    final File actual_root,
    final FilesystemType fs)
  {
    this.log = in_log.with("scene-filesystem");
    this.filesystem = fs;
    this.root = actual_root;
  }

  private PathVirtual copyFileIn(
    final PathVirtual base,
    final File file)
    throws FilesystemError,
      FileNotFoundException,
      IOException
  {
    final String name = file.getName();
    final Pair<PathVirtual, String> result =
      this.getUniqueName(base, name, SBSceneFilesystem.MAX_UNIQUE_NAME_TRIES);

    final File target = new File(this.root, result.getLeft().toString());

    if (this.log.wouldLog(LogLevel.LOG_DEBUG)) {
      final StringBuilder m = new StringBuilder();
      m.append("copy ");
      m.append(file);
      m.append(" to ");
      m.append(target);
      this.log.debug(m.toString());
    }

    SBIOUtilities.copyFile(file, target);
    return result.getLeft();
  }

  public void filesystemClose()
    throws FilesystemError
  {
    this.filesystem.close();
  }

  public PathVirtual filesystemCopyInMesh(
    final File file)
    throws FileNotFoundException,
      FilesystemError,
      IOException
  {
    NullCheck.notNull(file, "File");

    if (file.getName().isEmpty()) {
      throw new IllegalArgumentException("File name is empty");
    }

    return this.copyFileIn(SBSceneFilesystem.PATH_MESHES, file);
  }

  public PathVirtual filesystemCopyInTexture2D(
    final File file)
    throws FileNotFoundException,
      FilesystemError,
      IOException
  {
    NullCheck.notNull(file, "File");

    if (file.getName().isEmpty()) {
      throw new IllegalArgumentException("File name is empty");
    }

    return this.copyFileIn(SBSceneFilesystem.PATH_TEXTURES_2D, file);
  }

  public PathVirtual filesystemCopyInTextureCube(
    final File file)
    throws ValidityException,
      ParsingException,
      IOException,
      RXMLException,
      FilesystemError
  {
    NullCheck.notNull(file, "File");

    final Builder b = new Builder();
    final Document document = b.build(file);
    final CubeMap map = CubeMap.fromXML(document.getRootElement());

    final File file_parent = file.getParentFile();
    NullCheck.notNull(file_parent, "File parent");

    if (file_parent.isDirectory() == false) {
      throw new IllegalArgumentException("Parent of "
        + file
        + " is not a directory");
    }

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
        cube_map_directory.getRight());

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

    return cube_map_directory.getLeft();
  }

  public InputStream filesystemOpenFile(
    final PathVirtual path)
    throws FilesystemError
  {
    return this.filesystem.openFile(path);
  }

  /**
   * Save a scene to the zip file <code>file</code>.
   */

  public void filesystemSave(
    final SBSceneDescription description,
    final File file)
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

  private Pair<PathVirtual, String> getUniqueName(
    final PathVirtual base,
    final String name,
    final int tries)
    throws FilesystemError,
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
        return Pair.pair(path, new_name);
      }
    }

    throw new IOException("Could not create unique filename for " + name);
  }

  File root()
  {
    return this.root;
  }

  private void sceneSaveSerializeXML(
    final SBSceneDescription description)
    throws IOException
  {
    final FileOutputStream out =
      new FileOutputStream(new File(this.root, "scene.xml"));

    final XStream stream = new XStream();
    stream.toXML(description, out);
    out.close();
  }
}
