package io.github.opendonationassistant.tribute.handlers;

import com.fasterxml.uuid.Generators;
import io.github.opendonationassistant.events.AbstractMessageHandler;
import io.github.opendonationassistant.events.HasRecipientId;
import io.github.opendonationassistant.tribute.RecipientSettings;
import io.github.opendonationassistant.tribute.RecipientSettingsRepository;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.util.Map;

@Singleton
public class TokenSettingsChangedHandler
  extends AbstractMessageHandler<
    TokenSettingsChangedHandler.TokenSettingsChanged
  > {

  private final RecipientSettingsRepository repository;

  public TokenSettingsChangedHandler(
    ObjectMapper mapper,
    RecipientSettingsRepository repository
  ) {
    super(mapper);
    this.repository = repository;
  }

  @Serdeable
  public static record TokenSettingsChanged(
    String id,
    String type,
    String recipientId,
    String system,
    boolean enabled,
    boolean deleted,
    Map<String, Object> settings
  )
    implements HasRecipientId {}

  @Override
  public void handle(TokenSettingsChanged message) throws IOException {
    var settings = message.settings();
    var recipientSettings = new RecipientSettings(
      Generators.timeBasedEpochGenerator().generate().toString(),
      message.recipientId(),
      boolSetting(settings, "handleDonations"),
      boolSetting(settings, "handleSubscriptions"),
      boolSetting(settings, "handlePurchases"),
      boolSetting(settings, "triggerAlerts"),
      boolSetting(settings, "triggerDonaton"),
      boolSetting(settings, "triggerReel"),
      boolSetting(settings, "addToGoal"),
      boolSetting(settings, "countInTop"),
      boolSetting(settings, "subscriptionsTriggerAlerts"),
      boolSetting(settings, "subscriptionsTriggerDonaton"),
      boolSetting(settings, "subscriptionsTriggerReel"),
      boolSetting(settings, "subscriptionsAddToGoal"),
      boolSetting(settings, "subscriptionsCountInTop"),
      boolSetting(settings, "purchasesTriggerAlerts"),
      boolSetting(settings, "purchasesTriggerDonaton"),
      boolSetting(settings, "purchasesTriggerReel"),
      boolSetting(settings, "purchasesAddToGoal"),
      boolSetting(settings, "purchasesCountInTop")
    );
    repository.save(recipientSettings);
  }

  private boolean boolSetting(Map<String, Object> settings, String key) {
    var value = settings.get(key);
    if (value instanceof Boolean b) {
      return b;
    }
    return false;
  }
}
