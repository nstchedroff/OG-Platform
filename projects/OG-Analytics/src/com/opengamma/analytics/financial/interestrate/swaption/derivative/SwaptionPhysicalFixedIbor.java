/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.derivative;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.util.money.Currency;

/**
 * Class describing a European swaption on a vanilla swap.
 */
public final class SwaptionPhysicalFixedIbor extends EuropeanVanillaOption implements InstrumentDerivative {

  /**
   * List of calibration types for the physical swaption.
   */
  public enum SwaptionPhysicalFixedIborCalibrationType {
    /**
     * The calibration instruments are long swaptions with one maturity for each fixed coupon and strikes equal to the original strikes on the relevant periods.
     * The notional for all coupons is set to the first fixed leg coupon notional.
     * TODO: Should this be in the Definition or in a "Calculator"?
     */
    FIXEDLEG_STRIKE
  }

  /**
   * Swap underlying the swaption. The swap should be of vanilla type.
   */
  private final SwapFixedCoupon<? extends Payment> _underlyingSwap;
  /**
   * Flag indicating if the option is long (true) or short (false).
   */
  private final boolean _isLong;
  /**
   * The time (in years) to swap settlement.
   */
  private final double _settlementTime;

  /**
   * Constructor from the expiry date, the underlying swap and the long/short flqg.
   * @param expiryTime The expiry time.
   * @param strike The strike
   * @param underlyingSwap The underlying swap.
   * @param settlementTime Time to swap settlement.
   * @param isCall Call.
   * @param isLong The long (true) / short (false) flag.
   */
  private SwaptionPhysicalFixedIbor(double expiryTime, double strike, SwapFixedCoupon<? extends Payment> underlyingSwap, double settlementTime, boolean isCall, boolean isLong) {
    super(strike, expiryTime, isCall);
    Validate.notNull(underlyingSwap, "underlying swap");
    Validate.isTrue(isCall == underlyingSwap.getFixedLeg().isPayer(), "Call flag not in line with underlying");
    _underlyingSwap = underlyingSwap;
    _isLong = isLong;
    _settlementTime = settlementTime;
  }

  /**
   * Builder from the expiry date, the underlying swap and the long/short flag. The strike stored in the EuropeanVanillaOption should not be used for pricing as the 
   * strike can be different for each coupon and need to be computed at the pricing method level.
   * @param expiryTime The expiry time.
   * @param underlyingSwap The underlying swap.
   * @param settlementTime Time to swap settlement.
   * @param isLong The long (true) / short (false) flag.
   * @return The swaption.
   */
  public static SwaptionPhysicalFixedIbor from(double expiryTime, SwapFixedCoupon<? extends Payment> underlyingSwap, double settlementTime, boolean isLong) {
    Validate.notNull(underlyingSwap, "underlying swap");
    double strike = underlyingSwap.getFixedLeg().getNthPayment(0).getFixedRate();
    // Implementation comment: The strike is working only for swap with same rate on all coupons and standard conventions. The strike equivalent is computed in the pricing methods.
    return new SwaptionPhysicalFixedIbor(expiryTime, strike, underlyingSwap, settlementTime, underlyingSwap.getFixedLeg().isPayer(), isLong);
  }

  /**
   * Gets the underlying swap.
   * @return The underlying swap.
   */
  public SwapFixedCoupon<? extends Payment> getUnderlyingSwap() {
    return _underlyingSwap;
  }

  /**
   * Gets the _isLong flag.
   * @return The Long (true)/Short (false) flag.
   */
  public boolean isLong() {
    return _isLong;
  }

  /**
   * Gets the settlement time.
   * @return The settlement time
   */
  public double getSettlementTime() {
    return _settlementTime;
  }

  /**
   * Gets the time difference between the last fixed leg payment and the settlement.
   * @return The maturity time.
   */
  public double getMaturityTime() {
    return _underlyingSwap.getFixedLeg().getNthPayment(_underlyingSwap.getFixedLeg().getNumberOfPayments() - 1).getPaymentTime() - _settlementTime;
  }

  /**
   * Gets the swaption currency.
   * @return The currency.
   */
  public Currency getCurrency() {
    return _underlyingSwap.getFirstLeg().getCurrency();
  }

  @Override
  public String toString() {
    return "Swaption: Expiry=" + getTimeToExpiry() + ", is long=" + _isLong + "\n" + _underlyingSwap;
  }

  /**
   * Create a calibration basket for the swaption.
   * @param type The calibration type.
   * @return The basket.
   * TODO: Should this be in the Definition or in a "Calculator"?
   */
  public SwaptionPhysicalFixedIbor[] calibrationBasket(final SwaptionPhysicalFixedIborCalibrationType type) { //, final YieldCurveBundle curves
    SwaptionPhysicalFixedIbor[] calibration = new SwaptionPhysicalFixedIbor[0];
    switch (type) {
      case FIXEDLEG_STRIKE:
        AnnuityCouponFixed legFixed = getUnderlyingSwap().getFixedLeg();
        int nbCal = legFixed.getNumberOfPayments();
        calibration = new SwaptionPhysicalFixedIbor[nbCal];
        double notional = Math.abs(legFixed.getNthPayment(0).getNotional());
        for (int loopcal = 0; loopcal < nbCal; loopcal++) {
          double maturity = legFixed.getNthPayment(loopcal).getPaymentTime();
          SwapFixedCoupon<? extends Payment> swap = getUnderlyingSwap().trimAfter(maturity).withNotional(notional);
          calibration[loopcal] = SwaptionPhysicalFixedIbor.from(getTimeToExpiry(), swap, _settlementTime, true);
        }
        break;

      default:
        break;
    }
    return calibration;
  }

  @Override
  public <S, T> T accept(InstrumentDerivativeVisitor<S, T> visitor, S data) {
    return visitor.visitSwaptionPhysicalFixedIbor(this, data);
  }

  @Override
  public <T> T accept(InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitSwaptionPhysicalFixedIbor(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + (_isLong ? 1231 : 1237);
    long temp;
    temp = Double.doubleToLongBits(_settlementTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _underlyingSwap.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    SwaptionPhysicalFixedIbor other = (SwaptionPhysicalFixedIbor) obj;
    if (_isLong != other._isLong) {
      return false;
    }
    if (Double.doubleToLongBits(_settlementTime) != Double.doubleToLongBits(other._settlementTime)) {
      return false;
    }
    if (!ObjectUtils.equals(_underlyingSwap, other._underlyingSwap)) {
      return false;
    }
    return true;
  }

}
