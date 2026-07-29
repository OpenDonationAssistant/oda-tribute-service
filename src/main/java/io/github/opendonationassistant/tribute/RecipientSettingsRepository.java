package io.github.opendonationassistant.tribute;

import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import java.util.List;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface RecipientSettingsRepository
  extends CrudRepository<RecipientSettings, String> {
  List<RecipientSettings> findByRecipientId(String recipientId);
}
