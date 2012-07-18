/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.calculator;

import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.derivative.ForexNonDeliverableForward;
import com.opengamma.analytics.financial.forex.derivative.ForexSwap;
import com.opengamma.analytics.financial.forex.method.ForexDiscountingMethod;
import com.opengamma.analytics.financial.forex.method.ForexNonDeliverableForwardDiscountingMethod;
import com.opengamma.analytics.financial.forex.method.ForexSwapDiscountingMethod;
import com.opengamma.analytics.financial.forex.method.MultipleCurrencyInterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.AbstractInstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.cash.method.CashDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.fra.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.fra.method.ForwardRateAgreementDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFuture;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponOIS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponFixedDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponIborDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponIborSpreadDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponOISDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.method.PaymentFixedDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;

/**
 * Calculator of the present value curve sensitivity for Forex derivatives.
 */
public class PresentValueCurveSensitivityMCSCalculator extends AbstractInstrumentDerivativeVisitor<YieldCurveBundle, MultipleCurrencyInterestRateCurveSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueCurveSensitivityMCSCalculator s_instance = new PresentValueCurveSensitivityMCSCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueCurveSensitivityMCSCalculator getInstance() {
    return s_instance;
  }

  /**
   * Constructor.
   */
  public PresentValueCurveSensitivityMCSCalculator() {
  }

  /**
   * The methods used by the different instruments.
   */
  private static final CashDiscountingMethod METHOD_DEPOSIT = CashDiscountingMethod.getInstance();
  private static final PaymentFixedDiscountingMethod METHOD_PAY_FIXED = PaymentFixedDiscountingMethod.getInstance();
  private static final CouponFixedDiscountingMethod METHOD_CPN_FIXED = CouponFixedDiscountingMethod.getInstance();
  private static final CouponOISDiscountingMethod METHOD_CPN_OIS = CouponOISDiscountingMethod.getInstance();
  private static final CouponIborDiscountingMethod METHOD_CPN_IBOR = CouponIborDiscountingMethod.getInstance();
  private static final CouponIborSpreadDiscountingMethod METHOD_CPN_IBOR_SPREAD = CouponIborSpreadDiscountingMethod.getInstance();
  private static final ForwardRateAgreementDiscountingMethod METHOD_FRA = ForwardRateAgreementDiscountingMethod.getInstance();
  private static final InterestRateFutureDiscountingMethod METHOD_IR_FUTURES = InterestRateFutureDiscountingMethod.getInstance();
  private static final ForexDiscountingMethod METHOD_FOREX = ForexDiscountingMethod.getInstance();
  private static final ForexSwapDiscountingMethod METHOD_FXSWAP = ForexSwapDiscountingMethod.getInstance();
  private static final ForexNonDeliverableForwardDiscountingMethod METHOD_NDF = ForexNonDeliverableForwardDiscountingMethod.getInstance();

  @Override
  public MultipleCurrencyInterestRateCurveSensitivity visitCash(final Cash cash, final YieldCurveBundle curves) {
    return MultipleCurrencyInterestRateCurveSensitivity.of(cash.getCurrency(), METHOD_DEPOSIT.presentValueCurveSensitivity(cash, curves));
  }

  // -----     Coupon     ------

  @Override
  public MultipleCurrencyInterestRateCurveSensitivity visitFixedPayment(final PaymentFixed coupon, final YieldCurveBundle curves) {
    return MultipleCurrencyInterestRateCurveSensitivity.of(coupon.getCurrency(), METHOD_PAY_FIXED.presentValueCurveSensitivity(coupon, curves));
  }

  @Override
  public MultipleCurrencyInterestRateCurveSensitivity visitCouponFixed(final CouponFixed coupon, final YieldCurveBundle curves) {
    return MultipleCurrencyInterestRateCurveSensitivity.of(coupon.getCurrency(), METHOD_CPN_FIXED.presentValueCurveSensitivity(coupon, curves));
  }

  @Override
  public MultipleCurrencyInterestRateCurveSensitivity visitCouponOIS(final CouponOIS coupon, final YieldCurveBundle curves) {
    return MultipleCurrencyInterestRateCurveSensitivity.of(coupon.getCurrency(), METHOD_CPN_OIS.presentValueCurveSensitivity(coupon, curves));
  }

  @Override
  public MultipleCurrencyInterestRateCurveSensitivity visitCouponIbor(final CouponIbor coupon, final YieldCurveBundle curves) {
    return MultipleCurrencyInterestRateCurveSensitivity.of(coupon.getCurrency(), METHOD_CPN_IBOR.presentValueCurveSensitivity(coupon, curves));
  }

  @Override
  public MultipleCurrencyInterestRateCurveSensitivity visitCouponIborSpread(final CouponIborSpread coupon, final YieldCurveBundle curves) {
    return MultipleCurrencyInterestRateCurveSensitivity.of(coupon.getCurrency(), METHOD_CPN_IBOR_SPREAD.presentValueCurveSensitivity(coupon, curves));
  }

  @Override
  public MultipleCurrencyInterestRateCurveSensitivity visitForwardRateAgreement(final ForwardRateAgreement fra, final YieldCurveBundle curves) {
    return MultipleCurrencyInterestRateCurveSensitivity.of(fra.getCurrency(), METHOD_FRA.presentValueCurveSensitivity(fra, curves));
  }

  // -----     Futures     ------

  @Override
  public MultipleCurrencyInterestRateCurveSensitivity visitInterestRateFuture(final InterestRateFuture future, final YieldCurveBundle curves) {
    return MultipleCurrencyInterestRateCurveSensitivity.of(future.getCurrency(), METHOD_IR_FUTURES.presentValueCurveSensitivity(future, curves));
  }

  // -----     Annuity     ------

  @Override
  public MultipleCurrencyInterestRateCurveSensitivity visitGenericAnnuity(final Annuity<? extends Payment> annuity, final YieldCurveBundle data) {
    MultipleCurrencyInterestRateCurveSensitivity sensi = new MultipleCurrencyInterestRateCurveSensitivity();
    for (final Payment p : annuity.getPayments()) {
      sensi = sensi.plus(visit(p, data));
    }
    return sensi;
  }

  @Override
  public MultipleCurrencyInterestRateCurveSensitivity visitFixedCouponAnnuity(final AnnuityCouponFixed annuity, final YieldCurveBundle data) {
    return visitGenericAnnuity(annuity, data);
  }

  // -----     Swap     ------

  @Override
  public MultipleCurrencyInterestRateCurveSensitivity visitSwap(final Swap<?, ?> swap, final YieldCurveBundle curves) {
    final MultipleCurrencyInterestRateCurveSensitivity sensi1 = visit(swap.getFirstLeg(), curves);
    final MultipleCurrencyInterestRateCurveSensitivity sensi2 = visit(swap.getSecondLeg(), curves);
    return sensi1.plus(sensi2);
  }

  @Override
  public MultipleCurrencyInterestRateCurveSensitivity visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final YieldCurveBundle curves) {
    return visitSwap(swap, curves);
  }

  // -----     Forex     ------

  @Override
  public MultipleCurrencyInterestRateCurveSensitivity visitForex(final Forex derivative, final YieldCurveBundle data) {
    return METHOD_FOREX.presentValueCurveSensitivity(derivative, data);
  }

  @Override
  public MultipleCurrencyInterestRateCurveSensitivity visitForexSwap(final ForexSwap derivative, final YieldCurveBundle data) {
    return METHOD_FXSWAP.presentValueCurveSensitivity(derivative, data);
  }

  @Override
  public MultipleCurrencyInterestRateCurveSensitivity visitForexNonDeliverableForward(final ForexNonDeliverableForward derivative, final YieldCurveBundle data) {
    return METHOD_NDF.presentValueCurveSensitivity(derivative, data);
  }

}
