package org.folio.rest.service.action;

import static io.vertx.core.CompositeFuture.all;
import static io.vertx.core.Future.succeededFuture;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.folio.rest.domain.FeeFineStatus.CLOSED;
import static org.folio.rest.persist.PostgresClient.getInstance;
import static org.folio.rest.tools.utils.TenantTool.tenantId;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.folio.rest.domain.Action;
import org.folio.rest.domain.BulkActionRequest;
import org.folio.rest.domain.MonetaryValue;
import org.folio.rest.jaxrs.model.Account;
import org.folio.rest.jaxrs.model.DefaultBulkActionRequest;
import org.folio.rest.jaxrs.model.Feefineaction;
import org.folio.rest.persist.PostgresClient;
import org.folio.rest.repository.AccountRepository;
import org.folio.rest.repository.FeeFineActionRepository;
import org.folio.rest.service.AccountUpdateService;
import org.folio.rest.service.PatronNoticeService;
import org.folio.rest.service.action.context.BulkActionContext;
import org.folio.rest.service.action.validation.ActionValidationService;
import org.folio.rest.utils.amountsplitter.BulkActionAmountSplitterStrategy;
import org.folio.rest.utils.amountsplitter.SplitEvenlyRecursively;

import io.vertx.core.Context;
import io.vertx.core.Future;

public abstract class BulkActionService {
  protected final Action action;
  protected final AccountRepository accountRepository;
  protected final FeeFineActionRepository feeFineActionRepository;
  protected final AccountUpdateService accountUpdateService;
  protected final ActionValidationService validationService;
  protected final PatronNoticeService patronNoticeService;
  protected final BulkActionAmountSplitterStrategy amountSplitterStrategy;

  public BulkActionService(Action action, ActionValidationService validationService,
    Map<String, String> headers, Context context) {

    PostgresClient postgresClient = getInstance(context.owner(), tenantId(headers));

    this.action = action;
    this.accountRepository = new AccountRepository(postgresClient);
    this.feeFineActionRepository = new FeeFineActionRepository(postgresClient);
    this.accountUpdateService = new AccountUpdateService(headers, context);
    this.patronNoticeService = new PatronNoticeService(context.owner(), headers);
    this.validationService = validationService;
    this.amountSplitterStrategy = new SplitEvenlyRecursively();
  }

  public Future<BulkActionContext> performAction(BulkActionRequest request) {
    return succeededFuture(new BulkActionContext(request))
      .compose(this::findAccounts)
      .compose(this::validateAction)
      .compose(this::createFeeFineActions)
      .compose(this::updateAccounts)
      .compose(this::sendPatronNotice);
  }

  private Future<BulkActionContext> findAccounts(BulkActionContext context) {
    return accountRepository.getAccountsByIdWithNulls(context.getRequest().getAccountIds())
      .map(context::withAccounts);
  }

  protected Future<BulkActionContext> validateAction(BulkActionContext context) {
    String requestedAmount = ((DefaultBulkActionRequest) context.getRequest()).getAmount();

    return validationService.validate(context.getAccounts(), requestedAmount)
      .map(ignored -> context.withRequestedAmount(new MonetaryValue(requestedAmount)));
  }

  protected Future<BulkActionContext> createFeeFineActions(BulkActionContext context) {
    final DefaultBulkActionRequest request = (DefaultBulkActionRequest) context.getRequest();
    final List<Account> accounts = new ArrayList<>(context.getAccounts().values());
    final MonetaryValue requestedAmount = context.getRequestedAmount();

    Map<String, MonetaryValue> actionableAmounts = accounts.stream()
      .collect(toMap(Account::getId, account -> new MonetaryValue(account.getRemaining())));

    Map<String, MonetaryValue> distributedAmounts = amountSplitterStrategy.split(
      requestedAmount, accounts, actionableAmounts);

    List<Feefineaction> feeFineActions = accounts.stream()
      .map(account -> createFeeFineActionAndUpdateAccount(
        account, distributedAmounts.get(account.getId()), request))
      .collect(toList());

    return all(feeFineActions.stream().map(feeFineActionRepository::save).collect(toList()))
      .map(context.withFeeFineActions(feeFineActions));
  }

  protected Feefineaction createFeeFineActionAndUpdateAccount(Account account, MonetaryValue amount,
    BulkActionRequest request) {

    final DefaultBulkActionRequest defaultRequest = (DefaultBulkActionRequest) request;
    final MonetaryValue remainingAmountAfterAction = new MonetaryValue(account.getRemaining())
      .subtract(amount);
    boolean isFullAction = remainingAmountAfterAction.isZero();
    String actionType = isFullAction ? action.getFullResult() : action.getPartialResult();

    final Feefineaction feeFineAction = new Feefineaction()
      .withAmountAction(amount.toDouble())
      .withComments(request.getComments())
      .withNotify(request.getNotifyPatron())
      .withTransactionInformation(((DefaultBulkActionRequest) request).getTransactionInfo())
      .withCreatedAt(request.getServicePointId())
      .withSource(request.getUserName())
      .withPaymentMethod(defaultRequest.getPaymentMethod())
      .withAccountId(account.getId())
      .withUserId(account.getUserId())
      .withBalance(remainingAmountAfterAction.toDouble())
      .withTypeAction(actionType)
      .withId(UUID.randomUUID().toString())
      .withDateAction(new Date());

    account.getPaymentStatus().setName(actionType);

    if (isFullAction) {
      account.getStatus().setName(CLOSED.getValue());
      account.setRemaining(0.0);
    } else {
      account.setRemaining(feeFineAction.getBalance());
    }

    return feeFineAction;
  }

  private Future<BulkActionContext> updateAccounts(BulkActionContext context) {
    return all(
      context.getAccounts()
        .values()
        .stream()
        .map(accountUpdateService::updateAccount)
        .collect(toList())
    ).map(context);
  }

  private Future<BulkActionContext> sendPatronNotice(BulkActionContext context) {
    if (isTrue(context.getRequest().getNotifyPatron())) {
      context.getFeeFineActions()
        .forEach(patronNoticeService::sendPatronNotice);
    }
    return succeededFuture(context);
  }

}
