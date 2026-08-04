package io.github.opendonationassistant.tribute.webhook;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.opendonationassistant.commons.Amount;
import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.rabbit.RabbitClient;
import io.github.opendonationassistant.tribute.RecipientSettings;
import io.github.opendonationassistant.tribute.RecipientSettingsRepository;
import io.github.opendonationassistant.tribute.command.AddHistoryItemCommand;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.jspecify.annotations.Nullable;

@Controller
public class TributeWebhook {

  private final ODALogger log = new ODALogger(this);
  private final RabbitClient commandsFacade;
  private final ObjectMapper objectMapper;
  private final RecipientSettingsRepository settingsRepository;

  @Inject
  public TributeWebhook(
    @Named("commands") RabbitClient commandsFacade,
    RecipientSettingsRepository settingsRepository,
    ObjectMapper objectMapper
  ) {
    this.commandsFacade = commandsFacade;
    this.objectMapper = objectMapper;
    this.settingsRepository = settingsRepository;
  }

  @Post("/tribute/webhook/{recipientId}")
  @Secured(SecurityRule.IS_ANONYMOUS)
  public CompletableFuture<HttpResponse<Void>> handleWebhook(
    @PathVariable String recipientId,
    @Body TributePayload payload
  ) {
    log.info(
      "Received tribute webhook",
      Map.of("recipientId", recipientId, "payload", payload)
    );
    final Optional<RecipientSettings> settings = settingsRepository
      .findByRecipientId(recipientId)
      .stream()
      .findFirst();
    if (settings.isEmpty()) {
      return CompletableFuture.completedFuture(HttpResponse.noContent());
    }
    var command = convert(recipientId, payload, settings.get());
    if (command == null) {
      return CompletableFuture.completedFuture(HttpResponse.noContent());
    }
    return CompletableFuture.runAsync(() -> commandsFacade.sendCommand(command)
    ).thenApply(_ -> HttpResponse.ok());
  }

  private @Nullable AddHistoryItemCommand convert(
    String recipientId,
    TributePayload body,
    RecipientSettings settings
  ) {
    return switch (body.name()) {
      case "new_donation" -> convertDonation(recipientId, body, settings);
      case "new_subscription" -> convertSubscription(
        recipientId,
        body,
        settings
      );
      case "new_digital_product" -> convertPurchase(
        recipientId,
        body,
        settings
      );
      default -> {
        log.warn("Unknown tribute event", Map.of("name", body.name()));
        yield null;
      }
    };
  }

  private AddHistoryItemCommand convertDonation(
    String recipientId,
    TributePayload body,
    RecipientSettings settings
  ) {
    try {
      var p = objectMapper.convertValue(
        body.payload(),
        TributeDonationPayload.class
      );
      var amount = new Amount(p.amount() / 100, p.amount() % 100, p.currency());
      var id = String.valueOf(p.donationRequestId());
      return new AddHistoryItemCommand(
        id,
        p.telegramUsername(),
        recipientId,
        amount,
        p.message(),
        body.createdAt(),
        "tribute",
        id,
        List.of(),
        List.of(),
        List.of(),
        List.of(),
        List.of(),
        null,
        null,
        body.name(),
        null,
        null,
        null,
        settings.triggerAlerts(),
        settings.triggerReel(),
        settings.triggerDonaton(),
        settings.addToGoal(),
        settings.countInTop()
      );
    } catch (Exception e) {
      throw new RuntimeException("Failed to parse donation payload", e);
    }
  }

  private AddHistoryItemCommand convertSubscription(
    String recipientId,
    TributePayload body,
    RecipientSettings settings
  ) {
    try {
      var p = objectMapper.convertValue(
        body.payload(),
        TributeSubscriptionPayload.class
      );
      var amount = new Amount(p.amount() / 100, p.amount() % 100, p.currency());
      var id =
        String.valueOf(p.subscriptionId()) + String.valueOf(p.periodId());
      return new AddHistoryItemCommand(
        id,
        p.telegramUsername(),
        recipientId,
        amount,
        p.subscriptionName(),
        body.createdAt(),
        "tribute",
        id,
        List.of(),
        List.of(),
        List.of(),
        List.of(),
        List.of(),
        null,
        null,
        body.name(),
        null,
        null,
        null,
        settings.subscriptionsTriggerAlerts(),
        settings.subscriptionsTriggerReel(),
        settings.subscriptionsTriggerDonaton(),
        settings.subscriptionsAddToGoal(),
        settings.subscriptionsCountInTop()
      );
    } catch (Exception e) {
      throw new RuntimeException("Failed to parse subscription payload", e);
    }
  }

  private AddHistoryItemCommand convertPurchase(
    String recipientId,
    TributePayload body,
    RecipientSettings settings
  ) {
    try {
      var p = objectMapper.convertValue(
        body.payload(),
        TributeDigitalProductPayload.class
      );
      var amount = new Amount(p.amount() / 100, p.amount() % 100, p.currency());
      var id = String.valueOf(p.purchaseId());
      return new AddHistoryItemCommand(
        id,
        p.telegramUsername(),
        recipientId,
        amount,
        p.productName(),
        body.createdAt(),
        "tribute",
        id,
        List.of(),
        List.of(),
        List.of(),
        List.of(),
        List.of(),
        null,
        null,
        body.name(),
        null,
        null,
        null,
        settings.purchasesTriggerAlerts(),
        settings.purchasesTriggerReel(),
        settings.purchasesTriggerDonaton(),
        settings.purchasesAddToGoal(),
        settings.purchasesCountInTop()
      );
    } catch (Exception e) {
      throw new RuntimeException("Failed to parse digital product payload", e);
    }
  }

  @Serdeable
  public static record TributePayload(
    String name,
    @JsonProperty("created_at") Instant createdAt,
    @JsonProperty("sent_at") @Nullable Instant sentAt,
    Map<String, Object> payload
  ) {}

  @Serdeable
  public static record TributeDonationPayload(
    @JsonProperty("donation_request_id") Integer donationRequestId,
    @JsonProperty("donation_name") String donationName,
    String message,
    String period,
    Integer amount,
    String currency,
    Boolean anonymously,
    @JsonProperty("web_app_link") String webAppLink,
    String email,
    @JsonProperty("trb_user_id") String trbUserId,
    @JsonProperty("telegram_user_id") Integer telegramUserId,
    @JsonProperty("telegram_username") String telegramUsername
  ) {}

  @Serdeable
  public static record TributeSubscriptionPayload(
    @JsonProperty("subscription_name") String subscriptionName,
    @JsonProperty("subscription_id") Integer subscriptionId,
    @JsonProperty("period_id") Integer periodId,
    String period,
    String type,
    Integer price,
    Integer amount,
    String currency,
    @JsonProperty("trb_user_id") String trbUserId,
    @JsonProperty("telegram_user_id") Integer telegramUserId,
    @JsonProperty("telegram_username") String telegramUsername,
    @JsonProperty("channel_id") Integer channelId,
    @JsonProperty("channel_name") String channelName,
    @JsonProperty("expires_at") Instant expiresAt
  ) {}

  @Serdeable
  public static record TributeDigitalProductPayload(
    @JsonProperty("product_id") Integer productId,
    @JsonProperty("product_name") String productName,
    Integer amount,
    String currency,
    @JsonProperty("purchase_id") Integer purchaseId,
    @JsonProperty("transaction_id") Integer transactionId,
    @JsonProperty("purchase_created_at") Instant purchaseCreatedAt,
    @JsonProperty("trb_user_id") String trbUserId,
    @JsonProperty("telegram_user_id") Integer telegramUserId,
    @JsonProperty("telegram_username") String telegramUsername
  ) {}
}
