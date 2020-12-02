package org.folio.rest.service.action.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.folio.rest.domain.ActionRequest;
import org.folio.rest.domain.MonetaryValue;
import org.folio.rest.jaxrs.model.Account;
import org.folio.rest.jaxrs.model.Feefineaction;

public class ActionContext {
  private final ActionRequest request;
  private List<Feefineaction> feeFineActions;
  private MonetaryValue requestedAmount;
  private Map<String, Account> accounts;

  public ActionContext(ActionRequest request) {
    this.request = request;
    this.feeFineActions = new ArrayList<>();
    this.accounts = new HashMap<>();
  }

  public ActionContext withFeeFineAction(Feefineaction feeFineAction) {
    this.feeFineActions.add(feeFineAction);
    return this;
  }

  public ActionContext withFeeFineActions(List<Feefineaction> feeFineActions) {
    this.feeFineActions = feeFineActions;
    return this;
  }

  public ActionContext withRequestedAmount(MonetaryValue requestedAmount) {
    this.requestedAmount = requestedAmount;
    return this;
  }

  public ActionContext withAccounts(Map<String, Account> accounts) {
    this.accounts = accounts;
    return this;
  }

  public ActionRequest getRequest() {
    return request;
  }

  public Map<String, Account> getAccounts() {
    return accounts;
  }

  public List<Feefineaction> getFeeFineActions() {
    return feeFineActions;
  }

  public MonetaryValue getRequestedAmount() {
    return requestedAmount;
  }

}
