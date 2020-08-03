package org.folio.rest.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.folio.rest.jaxrs.model.Feefine;
import org.folio.rest.jaxrs.model.Feefineaction;
import org.folio.rest.jaxrs.model.Owner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(value = Parameterized.class)
public class FeeFineNoticeContextTest {
  private static final String DEFAULT_CHARGE_NOTICE_ID = "19a3a1c6-1717-4119-838f-298603ae2045";
  private static final String DEFAULT_ACTION_NOTICE_ID = "b0ff1736-262d-4c10-9a5c-b7d22ce0448c";
  private static final String FEEFINE_CHARGE_NOTICE_ID = "f361fc2d-dfbb-48a9-9f3f-a40c297411cf";
  private static final String FEEFINE_ACTION_NOTICE_ID = "500ebb63-0871-4fe7-a1da-fb3f1dbec5f2";

  private final Owner owner;
  private final Feefine feeFine;
  private final Feefineaction feeFineAction;
  private final String correctNoticeId;

  public FeeFineNoticeContextTest(Owner owner, Feefine feeFine, Feefineaction feeFineAction,
    String correctNoticeId) {
    this.owner = owner;
    this.feeFine = feeFine;
    this.feeFineAction = feeFineAction;
    this.correctNoticeId = correctNoticeId;
  }

  @Parameterized.Parameters
  public static Collection<Object[]> testParameters() {
    Owner owner = new Owner()
      .withOwner("Test owner")
      .withDefaultChargeNoticeId(DEFAULT_CHARGE_NOTICE_ID)
      .withDefaultActionNoticeId(DEFAULT_ACTION_NOTICE_ID);

    Owner ownerWithoutDefaultNoticeIds = new Owner()
      .withOwner("Owner without notice ids")
      .withDefaultChargeNoticeId(null)
      .withDefaultActionNoticeId(null);

    Feefine feeFineWithNoticeIds = new Feefine()
      .withFeeFineType("Test charge")
      .withChargeNoticeId(FEEFINE_CHARGE_NOTICE_ID)
      .withActionNoticeId(FEEFINE_ACTION_NOTICE_ID);

    Feefine feeFineWithNullNoticeIds = new Feefine()
      .withFeeFineType("Test charge")
      .withChargeNoticeId(null)
      .withActionNoticeId(null);

    Feefine feeFineWithEmptyNoticeIds = new Feefine()
      .withFeeFineType("Test charge")
      .withChargeNoticeId("")
      .withActionNoticeId("");

    Feefineaction charge = new Feefineaction()
      .withPaymentMethod(null)
      .withTypeAction("Test charge");

    Feefineaction paidFully = createActionWithType("Paid fully");
    Feefineaction paidPartially = createActionWithType("Paid partially");
    Feefineaction waivedFully = createActionWithType("Waived fully");
    Feefineaction waivedPartially = createActionWithType("Waived partially");
    Feefineaction transferredFully = createActionWithType("Transferred fully");
    Feefineaction transferredPartially = createActionWithType("Transferred partially");
    Feefineaction refundedFully = createActionWithType("Refunded fully");
    Feefineaction refundedPartially = createActionWithType("Refunded partially");
    Feefineaction cancelledAsError = createActionWithType("Cancelled as error");

    List<Object[]> parameters = new ArrayList<>();

    parameters.add(new Object[]
      {owner, feeFineWithNullNoticeIds, charge, DEFAULT_CHARGE_NOTICE_ID});
    parameters.add(new Object[]
      {owner, feeFineWithEmptyNoticeIds, charge, DEFAULT_CHARGE_NOTICE_ID});
    parameters.add(new Object[]
      {owner, feeFineWithNoticeIds, charge, FEEFINE_CHARGE_NOTICE_ID});

    parameters.add(new Object[]
      {owner, feeFineWithNullNoticeIds, paidFully, DEFAULT_ACTION_NOTICE_ID});
    parameters.add(new Object[]
      {owner, feeFineWithNullNoticeIds, paidPartially, DEFAULT_ACTION_NOTICE_ID});
    parameters.add(new Object[]
      {owner, feeFineWithEmptyNoticeIds, paidFully, DEFAULT_ACTION_NOTICE_ID});
    parameters.add(new Object[]
      {owner, feeFineWithEmptyNoticeIds, paidPartially, DEFAULT_ACTION_NOTICE_ID});
    parameters.add(new Object[]
      {owner, feeFineWithNoticeIds, paidFully, FEEFINE_ACTION_NOTICE_ID});
    parameters.add(new Object[]
      {owner, feeFineWithNoticeIds, paidPartially, FEEFINE_ACTION_NOTICE_ID});

    parameters.add(new Object[]
      {owner, feeFineWithNullNoticeIds, waivedFully, DEFAULT_ACTION_NOTICE_ID});
    parameters.add(new Object[]
      {owner, feeFineWithNullNoticeIds, waivedPartially, DEFAULT_ACTION_NOTICE_ID});
    parameters.add(new Object[]
      {owner, feeFineWithEmptyNoticeIds, waivedFully, DEFAULT_ACTION_NOTICE_ID});
    parameters.add(new Object[]
      {owner, feeFineWithEmptyNoticeIds, waivedPartially, DEFAULT_ACTION_NOTICE_ID});
    parameters.add(new Object[]
      {owner, feeFineWithNoticeIds, waivedFully, FEEFINE_ACTION_NOTICE_ID});
    parameters.add(new Object[]
      {owner, feeFineWithNoticeIds, waivedPartially, FEEFINE_ACTION_NOTICE_ID});

    parameters.add(new Object[]
      {owner, feeFineWithNullNoticeIds, transferredFully, DEFAULT_ACTION_NOTICE_ID});
    parameters.add(new Object[]
      {owner, feeFineWithNullNoticeIds, transferredPartially, DEFAULT_ACTION_NOTICE_ID});
    parameters.add(new Object[]
      {owner, feeFineWithEmptyNoticeIds, transferredFully, DEFAULT_ACTION_NOTICE_ID});
    parameters.add(new Object[]
      {owner, feeFineWithEmptyNoticeIds, transferredPartially, DEFAULT_ACTION_NOTICE_ID});
    parameters.add(new Object[]
      {owner, feeFineWithNoticeIds, transferredFully, FEEFINE_ACTION_NOTICE_ID});
    parameters.add(new Object[]
      {owner, feeFineWithNoticeIds, transferredPartially, FEEFINE_ACTION_NOTICE_ID});

    parameters.add(new Object[]
      {owner, feeFineWithNullNoticeIds, refundedFully, DEFAULT_ACTION_NOTICE_ID});
    parameters.add(new Object[]
      {owner, feeFineWithNullNoticeIds, refundedPartially, DEFAULT_ACTION_NOTICE_ID});
    parameters.add(new Object[]
      {owner, feeFineWithEmptyNoticeIds, refundedFully, DEFAULT_ACTION_NOTICE_ID});
    parameters.add(new Object[]
      {owner, feeFineWithEmptyNoticeIds, refundedPartially, DEFAULT_ACTION_NOTICE_ID});
    parameters.add(new Object[]
      {owner, feeFineWithNoticeIds, refundedFully, FEEFINE_ACTION_NOTICE_ID});
    parameters.add(new Object[]
      {owner, feeFineWithNoticeIds, refundedPartially, FEEFINE_ACTION_NOTICE_ID});

    parameters.add(new Object[]
      {owner, feeFineWithNullNoticeIds, cancelledAsError, DEFAULT_ACTION_NOTICE_ID});
    parameters.add(new Object[]
      {owner, feeFineWithEmptyNoticeIds, cancelledAsError, DEFAULT_ACTION_NOTICE_ID});
    parameters.add(new Object[]
      {owner, feeFineWithNoticeIds, cancelledAsError, FEEFINE_ACTION_NOTICE_ID});

    parameters.add(new Object[]
      {ownerWithoutDefaultNoticeIds, feeFineWithNullNoticeIds, charge, null});
    parameters.add(new Object[]
      {ownerWithoutDefaultNoticeIds, feeFineWithEmptyNoticeIds, charge, null});

    return parameters;
  }

  @Test
  public void getTemplateIdReturnsCorrectId() {
    FeeFineNoticeContext feeFineNoticeContext = new FeeFineNoticeContext()
      .withOwner(owner)
      .withFeefine(feeFine)
      .withFeefineaction(feeFineAction);

    String templateId = feeFineNoticeContext.getTemplateId();
    if (correctNoticeId == null) {
      assertNull(templateId);
    }
    else {
      assertEquals(correctNoticeId, templateId);
    }
  }

  private static Feefineaction createActionWithType(String type) {
    return new Feefineaction()
      .withPaymentMethod("Test payment method")
      .withTypeAction(type);
  }

}
