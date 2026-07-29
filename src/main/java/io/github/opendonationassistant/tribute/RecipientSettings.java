package io.github.opendonationassistant.tribute;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import io.micronaut.data.model.DataType;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
@MappedEntity("recipient_settings")
public record RecipientSettings(
  @Id @MappedProperty(type = DataType.UUID) String id,
  String recipientId,
  boolean handleDonations,
  boolean handleSubscriptions,
  boolean handlePurchases,
  boolean triggerAlerts,
  boolean triggerDonaton,
  boolean triggerReel,
  boolean addToGoal,
  boolean countInTop,
  boolean subscriptionsTriggerAlerts,
  boolean subscriptionsTriggerDonaton,
  boolean subscriptionsTriggerReel,
  boolean subscriptionsAddToGoal,
  boolean subscriptionsCountInTop,
  boolean purchasesTriggerAlerts,
  boolean purchasesTriggerDonaton,
  boolean purchasesTriggerReel,
  boolean purchasesAddToGoal,
  boolean purchasesCountInTop
) {}
