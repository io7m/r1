package com.io7m.r1.tests;

import net.java.quickcheck.Generator;
import net.java.quickcheck.generator.support.StringGenerator;

import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jcanephora.TextureFormat;
import com.io7m.jcanephora.TextureWrapS;
import com.io7m.jcanephora.TextureWrapT;
import com.io7m.jranges.RangeInclusiveL;

public final class RFakeTextures2DStaticGenerator implements
  Generator<Texture2DStaticUsableType>
{
  private final EnumGenerator<TextureFilterMagnification> filter_mag_gen;
  private final EnumGenerator<TextureFilterMinification>  filter_min_gen;
  private final Generator<TextureFormat>                  fmt_gen;
  private final StringGenerator                           name_gen;
  private final EnumGenerator<TextureWrapS>               wrap_s_gen;
  private final EnumGenerator<TextureWrapT>               wrap_t_gen;

  public RFakeTextures2DStaticGenerator()
  {
    this.fmt_gen = new EnumGenerator<TextureFormat>(TextureFormat.class);
    this.wrap_t_gen = new EnumGenerator<TextureWrapT>(TextureWrapT.class);
    this.wrap_s_gen = new EnumGenerator<TextureWrapS>(TextureWrapS.class);
    this.filter_min_gen =
      new EnumGenerator<TextureFilterMinification>(
        TextureFilterMinification.class);
    this.filter_mag_gen =
      new EnumGenerator<TextureFilterMagnification>(
        TextureFilterMagnification.class);
    this.name_gen = new StringGenerator();
  }

  @Override public Texture2DStaticUsableType next()
  {
    final int id = (int) (Math.random() * (Integer.MAX_VALUE - 1)) + 1;
    final int width = (int) (Math.random() * Integer.MAX_VALUE);
    final int height = (int) (Math.random() * Integer.MAX_VALUE);
    final TextureFormat format = this.fmt_gen.next();
    final RangeInclusiveL range_x = new RangeInclusiveL(0, width - 1);
    final RangeInclusiveL range_y = new RangeInclusiveL(0, height - 1);
    final AreaInclusive area = new AreaInclusive(range_x, range_y);
    final TextureWrapS wrap_s = this.wrap_s_gen.next();
    final TextureWrapT wrap_t = this.wrap_t_gen.next();
    final String name = this.name_gen.next();
    assert name != null;
    final TextureFilterMinification fmin = this.filter_min_gen.next();
    final TextureFilterMagnification fmag = this.filter_mag_gen.next();
    final long size = width * format.getBytesPerPixel() * height;

    return new Texture2DStaticUsableType() {
      @Override public int getGLName()
      {
        return id;
      }

      @Override public long resourceGetSizeBytes()
      {
        return size;
      }

      @Override public boolean resourceIsDeleted()
      {
        return false;
      }

      @Override public AreaInclusive textureGetArea()
      {
        return area;
      }

      @Override public TextureFormat textureGetFormat()
      {
        return format;
      }

      @Override public int textureGetHeight()
      {
        return height;
      }

      @Override public
        TextureFilterMagnification
        textureGetMagnificationFilter()
      {
        return fmag;
      }

      @Override public
        TextureFilterMinification
        textureGetMinificationFilter()
      {
        return fmin;
      }

      @Override public String textureGetName()
      {
        return name;
      }

      @Override public RangeInclusiveL textureGetRangeX()
      {
        return range_x;
      }

      @Override public RangeInclusiveL textureGetRangeY()
      {
        return range_y;
      }

      @Override public int textureGetWidth()
      {
        return width;
      }

      @Override public TextureWrapS textureGetWrapS()
      {
        return wrap_s;
      }

      @Override public TextureWrapT textureGetWrapT()
      {
        return wrap_t;
      }
    };
  }

}
