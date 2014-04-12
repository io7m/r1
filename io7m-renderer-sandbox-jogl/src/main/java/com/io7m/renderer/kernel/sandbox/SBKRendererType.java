package com.io7m.renderer.kernel.sandbox;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.renderer.types.RException;

public interface SBKRendererType
{
  void accept(
    final @Nonnull SBKRendererVisitor v)
    throws RException,
      ConstraintError;
}
