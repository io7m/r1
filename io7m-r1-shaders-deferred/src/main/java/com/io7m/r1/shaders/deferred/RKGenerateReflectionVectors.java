package com.io7m.r1.shaders.deferred;

public final class RKGenerateReflectionVectors
{
  public static void main(
    final String[] args)
  {
    if (args.length < 1) {
      System.err.println("usage: count");
      System.exit(1);
    }

    final int count = Integer.valueOf(args[0]).intValue();
    final double inc = (Math.PI * 2) / count;

    for (int index = 0; index < count; ++index) {
      final double a = inc * index;
      final double x = Math.cos(a);
      final double y = Math.sin(a);
      System.out.printf(
        "  value reflect_%d = new vector_2f (%f, %f);\n",
        index,
        x,
        y);
    }
  }
}
