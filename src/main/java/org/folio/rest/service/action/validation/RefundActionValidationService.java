package org.folio.rest.service.action.validation;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.folio.rest.domain.MonetaryValue;
import org.folio.rest.exception.FailedValidationException;
import org.folio.rest.jaxrs.model.Account;
import org.folio.rest.jaxrs.model.Feefineaction;
import org.folio.rest.repository.FeeFineActionRepository;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Context;
import io.vertx.core.Future;

public class RefundActionValidationService extends ActionValidationService {
  private final FeeFineActionRepository feeFineActionRepository;

  public RefundActionValidationService(Map<String, String> headers, Context context) {
    super(headers, context);
    this.feeFineActionRepository = new FeeFineActionRepository(headers, context);
  }

  @Override
  protected void validateAccountStatuses(List<Account> account) {
    // doing nothing as closed fee/fine can also be refunded
  }

  private Future<MonetaryValue> getRefundableAmount(List<Account> accounts) {
    // Sum of the refundable amounts of all accounts
    return CompositeFuture.all(
      accounts.stream()
        .map(this::getRefundableAmount)
        .collect(Collectors.toList()))
      .map(cf -> cf.list().stream()
        .filter(MonetaryValue.class::isInstance)
        .map(MonetaryValue.class::cast)
        .reduce(MonetaryValue::add)
        .orElse(new MonetaryValue(BigDecimal.ZERO)));
  }

  private Future<MonetaryValue> getRefundableAmount(Account account) {
    return feeFineActionRepository.findRefundableActionsForAccount(account.getId())
      .map(actions -> actions.stream()
        .mapToDouble(Feefineaction::getAmountAction)
        .sum())
      .map(MonetaryValue::new);
  }

  @Override
  protected Future<Void> validateAmountMaximum(List<Account> accounts,
    MonetaryValue requestedAmount) {

    return getRefundableAmount(accounts)
      .map(refundableAmount -> {
        if (requestedAmount.isGreaterThan(refundableAmount)) {
          throw new FailedValidationException(
            "Refund amount must be greater than zero and less than or equal to Selected amount");
        }
        return null;
      });
  }

  @Override
  protected Future<MonetaryValue> calculateRemainingBalance(List<Account> accounts,
    MonetaryValue requestedAmount) {

    return getRefundableAmount(accounts)
      .map(refundableAmount -> refundableAmount.subtract(requestedAmount));
  }

}
