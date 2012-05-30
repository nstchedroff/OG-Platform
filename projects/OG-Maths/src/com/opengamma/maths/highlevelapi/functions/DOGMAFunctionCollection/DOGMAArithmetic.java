/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.highlevelapi.functions.DOGMAFunctionCollection;

import com.opengamma.maths.highlevelapi.datatypes.primitive.OGArraySuper;
import com.opengamma.maths.highlevelapi.datatypes.primitive.OGDoubleArray;
import com.opengamma.maths.highlevelapi.functions.DOGMAFunctions.DOGMAArithmetic.PlusAndMinus;
import com.opengamma.maths.highlevelapi.functions.DOGMAFunctions.DOGMAArithmetic.Rdivide;
import com.opengamma.maths.highlevelapi.functions.DOGMAFunctions.DOGMAArithmetic.Times;
import com.opengamma.maths.highlevelapi.functions.DOGMAFunctions.DOGMARearrangingMatrices.copy.Copy;
import com.opengamma.maths.highlevelapi.functions.DOGMAinterfaces.DOGMAArithmeticAPI;

/**
 * Basic Arithmetic 
 */
public class DOGMAArithmetic implements DOGMAArithmeticAPI {
  private final PlusAndMinus _plusMinus = new PlusAndMinus();
  private final Copy _copy = new Copy();
  private final Times _times = new Times();
  private Rdivide _rdivide = new Rdivide();

  @Override
  public OGArraySuper<Number> plus(OGArraySuper<Number>... array) {
    OGArraySuper<Number> tmp = _copy.copy(array[0]);
    for (int i = 1; i < array.length; i++) {
      tmp = _plusMinus.plus(tmp, array[i]);
    }
    return tmp;
  }

  @Override
  public OGArraySuper<Number> plus(OGArraySuper<Number> array1, OGArraySuper<Number> array2) {
    return _plusMinus.plus(array1, array2);
  }

  @Override
  public OGArraySuper<Number> minus(OGArraySuper<Number>... array) {
    OGArraySuper<Number> tmp = _copy.copy(array[0]);
    for (int i = 1; i < array.length; i++) {
      tmp = _plusMinus.minus(tmp, array[i]);
    }
    return tmp;
  }

  @Override
  public OGArraySuper<Number> minus(OGArraySuper<Number> array1, OGArraySuper<Number> array2) {
    return _plusMinus.minus(array1, array2);
  }

  @Override
  public OGArraySuper<Number> ldivide(OGArraySuper<Number> array1, OGArraySuper<Number> array2) {
    return null;
  }

  @Override
  public OGArraySuper<Number> mldivide(OGArraySuper<Number> array1, OGArraySuper<Number> array2) {
    return null;
  }

  @Override
  public OGArraySuper<Number> rdivide(OGArraySuper<Number> array1, OGArraySuper<Number> array2) {
    return _rdivide.rdivide(array1, array2);
  }

  @Override
  public OGArraySuper<Number> mrdivide(OGArraySuper<Number> matrixA, OGArraySuper<Number> vectorb) {
    return null;
  }

  @Override
  public OGArraySuper<Number> times(OGArraySuper<Number> array1, OGArraySuper<Number> array2) {
    OGArraySuper<Number> tmp = _copy.copy(array1);
    tmp = _times.times(tmp, array2);
    return tmp;
  }

  @Override
  public OGArraySuper<Number> times(OGArraySuper<Number>... array) {
    OGArraySuper<Number> tmp = _copy.copy(array[0]);
    for (int i = 1; i < array.length; i++) {
      tmp = _times.times(tmp, array[i]);
    }
    return tmp;
  }

  @Override
  public OGDoubleArray mtimes(OGArraySuper<Number>... array) {
    return null;
  }

  @Override
  public OGDoubleArray power(OGArraySuper<Number> array1, OGArraySuper<Number> array2) {
    return null;
  }

  @Override
  public OGDoubleArray mpower(OGArraySuper<Number> array1, OGArraySuper<Number> array2) {
    return null;
  }

  @Override
  public OGDoubleArray tranpose(OGArraySuper<Number> array) {
    return null;
  }

  /**
   * Adds a double to an array
   * @param array1 the array to add to
   * @param aNumber the double
   * @return the array plus element-wise a double
   */
  public OGArraySuper<Number> plus(OGArraySuper<Number> array1, double aNumber) {
    return plus(array1, new OGDoubleArray(aNumber));
  }

}