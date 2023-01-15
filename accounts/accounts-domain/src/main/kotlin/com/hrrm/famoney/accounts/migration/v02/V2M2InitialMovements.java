package com.hrrm.famoney.accounts.migration.v02;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonString;
import javax.json.JsonValue;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.migration.Context;
import org.flywaydb.core.api.migration.JavaMigration;
import org.osgi.service.log.Logger;
import org.osgi.service.log.LoggerFactory;

import com.hrrm.famoney.application.api.masterdata.dto.EntryCategoriesDTO;
import com.hrrm.famoney.application.api.masterdata.dto.EntryCategoryDTO;
import com.hrrm.famoney.domain.accounts.migrations.v01.V1M2Accounts;
import com.hrrm.famoney.function.throwing.ThrowingBiConsumer;
import com.hrrm.famoney.function.throwing.ThrowingConsumer;
import com.hrrm.famoney.function.throwing.ThrowingFunction;
import com.hrrm.famoney.function.throwing.ThrowingIntConsumer;
import com.hrrm.famoney.function.throwing.ThrowingRunnable;
import com.hrrm.famoney.infrastructure.persistence.migrations.MigrationException;

public class V2M2InitialMovements implements JavaMigration {

    private final Logger logger;

    private static final Map<String, String> MOVEMENT_TYPES = Map.of("check",
            "entry",
            "transfer",
            "transfer",
            "entry",
            "entry",
            "balance",
            "balance");
    private static final Map<String, String> ACCOUNTS = Map.of("Кошелек Пупсика",
            "entry",
            "transfer",
            "transfer",
            "entry",
            "entry",
            "balance",
            "balance");
    private Map<String, Integer> accountIds = new HashMap<>();
    private Map<String, Integer> categoryIds = new HashMap<>();
    private Supplier<EntryCategoriesDTO> entryCategoriesSuppier;
    private EntryCategoriesDTO entryCategories;

    private static final List<Pair<Pattern, String>> CATEGORY_PROCESSORS = List.of(Pair.of(Pattern.compile(
            "Питание: Продукты"),
            "Продукты"));

    public V2M2InitialMovements(final LoggerFactory loggerFactory,
            final Supplier<EntryCategoriesDTO> entryCategoriesSuppier) {
        super();
        this.logger = loggerFactory.getLogger(V1M2Accounts.class);
        this.entryCategoriesSuppier = entryCategoriesSuppier;
    }

    @Override
    public MigrationVersion getVersion() {
        return MigrationVersion.fromVersion("02.02");
    }

    @Override
    public String getDescription() {
        return "Initial account movements";
    }

    @Override
    public Integer getChecksum() {
        return 0xa1b14a2c;
    }

    @Override
    public boolean isUndo() {
        return false;
    }

    @Override
    public boolean canExecuteInTransaction() {
        return true;
    }

    @Override
    public void migrate(final Context context) throws MigrationException {
        logger.info("Starting migration: \"{} {}\".",
                getVersion(),
                getDescription());
        try (final var jdbcStatements = new InitialMovementsJdbcStatements(context.getConnection(),
            getClass().getClassLoader())) {
            entryCategories = entryCategoriesSuppier.get();
            insertAccountsMovements(jdbcStatements);
            updateAccountsMovemntsCountAndSum(jdbcStatements);
            logger.info("Migration: \"{} {}\" is successfully completed.",
                    getVersion(),
                    getDescription());
        } catch (final SQLException | MigrationException e) {
            final var message = MessageFormat.format("It is unable to perform migration: \"{0} {1}\".",
                    getVersion(),
                    getDescription());
            logger.error(message,
                    e);
            throw new MigrationException(message,
                e);
        }
    }

    private void insertAccountsMovements(final InitialMovementsJdbcStatements jdbcStatements) {
        logger.info("Inserting initial movements.");
        try (final var parser = Json.createParser(this.getClass()
            .getResourceAsStream("Pupsik_Wallet.json"))) {
            final var rootObject = parser.getObject();
            final var accounts = rootObject.getJsonObject("accounts");
            accounts.forEach(ThrowingBiConsumer.sneaky((accountName, accountValue) -> insertAccountMovements(
                    jdbcStatements,
                    accountName,
                    accountValue)));
            logger.info("Initial movements successfully inserted.");
        }
    }

    private void insertAccountMovements(final InitialMovementsJdbcStatements jdbcStatements, final String accountName,
            final JsonValue accountValue) throws MigrationException {
        logger.info("Inserting initial movements for account: \"{}\".",
                accountName);
        try {
            final var accountMovements = accountValue.asJsonArray();
            final var accountId = accountIds.computeIfAbsent(accountName,
                    ThrowingFunction.sneaky(key -> findAccountId(jdbcStatements.getAccountIdByNameSelect(),
                            key)));
            accountMovements.forEach(ThrowingConsumer.sneaky(movementValue -> {
                final var movementData = getMovementData(jdbcStatements,
                        accountId,
                        movementValue);
                final var movementId = insertMovement(jdbcStatements.getAccountMovementInsert(),
                        movementData);
                if (
                    movementData.getType()
                        .equals("entry") &&
                        !movementData.getEntryItems()
                            .isEmpty()
                ) {
                    insertEntryItems(jdbcStatements.getEntryItemInsert(),
                            movementData,
                            movementId);
                }
            }));
            insertMovmentSlices(jdbcStatements,
                    accountId);
            jdbcStatements.getConnection()
                .commit();
            logger.info("Initial movements for account: \"{}\" are successfully inserted.",
                    accountName);
            logger.debug("{} initial movements for account: \"{}\" are successfully inserted.",
                    accountMovements.size(),
                    accountName);
        } catch (MigrationException | SQLException e) {
            final var message = MessageFormat.format("It is unable to insert movements for account: \"{0}\"",
                    accountName);
            logger.error(message,
                    e);
            throw new MigrationException(message,
                e);
        }
    }

    private MovementData getMovementData(final InitialMovementsJdbcStatements jdbcStatements, final int accountId,
            final JsonValue movementValue) throws MigrationException {
        logger.trace("Combining data for account movement.");
        try {
            final var movement = movementValue.asJsonObject();
            final var movementDataBuilder = MovementDataImpl.builder()
                .accountId(accountId)
                .type(V2M2InitialMovements.MOVEMENT_TYPES.get(movement.getString("type")));
            final var date = DateTimeFormatter.ISO_DATE.parse(movement.getString("date"),
                    LocalDate::from);
            movementDataBuilder.date(findNextDate(jdbcStatements,
                    accountId,
                    date));
            final var bookingDateAttribute = Optional.ofNullable(movement.getString("bookingDate",
                    null))
                .map(attr -> DateTimeFormatter.ISO_DATE_TIME.parse(attr,
                        LocalDateTime::from));
            movementDataBuilder.bookingDate(bookingDateAttribute);
            final var amount = movement.getJsonNumber("amount")
                .bigDecimalValue();
            movementDataBuilder.amount(amount);
            Optional.ofNullable(movement.getJsonArray("items"))
                .map(ThrowingFunction.sneaky(this::jsonItemsToEntryItems))
                .ifPresent(movementDataBuilder::addAllEntryItems);
            Optional.ofNullable(movement.getJsonString("category"))
                .map(JsonString::getString)
                .map(category -> categoryIds.computeIfAbsent(category,
                        ThrowingFunction.sneaky(key -> findCategoryIdByCategoryFullName(key,
                                amount.signum()))))
                .ifPresent(movementDataBuilder::categoryId);
            Optional.ofNullable(movement.getJsonString("comments"))
                .map(JsonString::getString)
                .ifPresent(movementDataBuilder::comments);
            Optional.ofNullable(movement.getJsonString("oppositAccount"))
                .map(JsonString::getString)
                .map(account -> accountIds.computeIfAbsent(account,
                        ThrowingFunction.sneaky(accountName -> findAccountId(jdbcStatements.getAccountIdByNameSelect(),
                                accountName))))
                .ifPresent(movementDataBuilder::categoryId);
            final var accountMovementData = movementDataBuilder.build();
            logger.trace(l -> l.trace("Combined data for account movement: {}",
                    accountMovementData));
            return accountMovementData;
        } catch (final MigrationException e) {
            final var message = "Couldn't combine data for account movement.";
            logger.error(message);
            throw new MigrationException(message,
                e);
        }
    }

    private LocalDateTime findNextDate(final InitialMovementsJdbcStatements jdbcStatements, final int accountId,
            final LocalDate date) throws MigrationException {
        logger.trace("Searching for next free timestamp for account id: {} on date.",
                accountId,
                date);
        try {
            final var accountMovementsMaxDateBetweenDatesSelectStmt = jdbcStatements
                .getAccountMovementsMaxDateBetweenDatesSelect();
            accountMovementsMaxDateBetweenDatesSelectStmt.setTimestamp(1,
                    Timestamp.valueOf(date.atStartOfDay()));
            accountMovementsMaxDateBetweenDatesSelectStmt.setTimestamp(2,
                    Timestamp.valueOf(date.plusDays(1)
                        .atStartOfDay()));
            accountMovementsMaxDateBetweenDatesSelectStmt.setInt(3,
                    accountId);
            LocalDateTime dateTime = null;
            try (final var resultSet = accountMovementsMaxDateBetweenDatesSelectStmt.executeQuery()) {
                final var maxTimeStamp = resultSet.next() ? Optional.ofNullable(resultSet.getTimestamp(1))
                        : Optional.<Timestamp>empty();
                dateTime = maxTimeStamp.map(ts -> ts.toLocalDateTime()
                    .plusMinutes(5))
                    .orElse(date.atTime(12,
                            0));
            }
            logger.trace("Next free timestamp for account id: {} is {}.",
                    accountId,
                    date);
            return dateTime;
        } catch (MigrationException | SQLException e) {
            final var message = MessageFormat.format(
                    "It is unbale to find next free timestamp for account id: {0} on {1}.",
                    accountId,
                    date);
            logger.error(message,
                    e);
            throw new MigrationException(message,
                e);
        }
    }

    private List<EntryItemData> jsonItemsToEntryItems(JsonArray jsonItems) throws MigrationException {
        return jsonItems.getValuesAs(jsonItem -> {
            final var entryItem = jsonItem.asJsonObject();
            var entryItemBuilder = EntryItemDataImpl.builder();
            BigDecimal amount = entryItem.getJsonNumber("amount")
                .bigDecimalValue();
            entryItemBuilder.amount(amount);

            Optional.ofNullable(entryItem.getJsonString("category"))
                .map(JsonString::getString)
                .map(category -> categoryIds.computeIfAbsent(category,
                        ThrowingFunction.sneaky(key -> findCategoryIdByCategoryFullName(key,
                                amount.signum()))))
                .ifPresent(entryItemBuilder::categoryId);
            Optional.ofNullable(entryItem.getJsonString("comments"))
                .map(JsonString::getString)
                .ifPresent(entryItemBuilder::comments);
            return entryItemBuilder.build();
        });
    }

    private Integer findCategoryIdByCategoryFullName(final String categoryFullName, final int amountSignum)
            throws MigrationException {
        final var processedCategoryFullName = V2M2InitialMovements.CATEGORY_PROCESSORS.stream()
            .map(processor -> Pair.of(processor.getLeft()
                .matcher(categoryFullName),
                    processor.getRight()))
            .filter(processor -> processor.getLeft()
                .find())
            .findFirst()
            .map(processor -> processor.getLeft()
                .replaceAll(processor.getRight()))
            .orElse(categoryFullName);
        final var categoryLevels = new ArrayDeque<String>(List.of(StringUtils.split(processedCategoryFullName,
                ": ")));
        try {
            if (amountSignum > 0) {
                final var incomes = entryCategories.getIncomes();
                return findCategoryIdByCategoryLevels(categoryLevels,
                        incomes);
            } else {
                final var expenses = entryCategories.getExpenses();
                return findCategoryIdByCategoryLevels(categoryLevels,
                        expenses);
            }
        } catch (MigrationException e) {
            throw new MigrationException(MessageFormat.format("Cannot find category: \"{0}\".",
                    processedCategoryFullName),
                e);
        }
    }

    private <T extends EntryCategoryDTO<T>> Integer findCategoryIdByCategoryLevels(
            final ArrayDeque<String> categoryLevels, final List<T> incomes) throws MigrationException {
        final var categoryLevel = categoryLevels.poll();
        final var foundCategory = incomes.stream()
            .filter(value -> value.getName()
                .equals(categoryLevel))
            .findFirst()
            .orElseThrow(() -> new MigrationException(MessageFormat.format("Cannot find subcategory: \"{0}\".",
                    categoryLevel)));
        return findCategoryIdByCategory(foundCategory,
                categoryLevels);
    }

    private <T extends EntryCategoryDTO<T>> Integer findCategoryIdByCategory(final T foundCategory,
            final ArrayDeque<String> categoryLevels) throws MigrationException {
        if (categoryLevels.isEmpty()) {
            return foundCategory.getId();
        }
        final var categoryLevel = categoryLevels.poll();
        return foundCategory.getChildren()
            .stream()
            .filter(value -> value.getName()
                .equals(categoryLevel))
            .findFirst()
            .map(ThrowingFunction.sneaky(value -> findCategoryIdByCategory(value,
                    categoryLevels)))
            .orElse(foundCategory.getId());
    }

    private Integer insertMovement(final PreparedStatement insertMovementStatement,
            final MovementData accountMovementData) throws MigrationException {
        logger.trace(l -> l.trace("Inserting account movement: {}.",
                accountMovementData));
        try {
            final var generatedKeys = callInsertMovement(insertMovementStatement,
                    accountMovementData);
            if (generatedKeys.next()) {
                final int movementId = generatedKeys.getInt(1);
                logger.trace("Account movement is successfully inserted with id: {}.",
                        movementId);
                return movementId;
            }
            logger.trace("Account movement is successfully inserted.");
            return null;
        } catch (DateTimeParseException | SQLException ex) {
            final String message = MessageFormat.format("It was unable to insert an account movement for data: {0}.",
                    accountMovementData);
            logger.error(message);
            throw new MigrationException(message,
                ex);
        }
    }

    private ResultSet callInsertMovement(final PreparedStatement insertMovementStatement,
            final MovementData accountMovementData) throws SQLException {
        insertMovementStatement.setInt(1,
                accountMovementData.getAccountId());
        insertMovementStatement.setString(2,
                accountMovementData.getType());
        insertMovementStatement.setTimestamp(3,
                Timestamp.valueOf(accountMovementData.getDate()));
        accountMovementData.getBookingDate()
            .ifPresentOrElse(ThrowingConsumer.sneaky(value -> insertMovementStatement.setTimestamp(4,
                    Timestamp.valueOf(value))),
                    ThrowingRunnable.sneaky(() -> insertMovementStatement.setNull(4,
                            Types.TIMESTAMP)));
        accountMovementData.getBudgetPeriod()
            .ifPresentOrElse(ThrowingConsumer.sneaky(value -> insertMovementStatement.setDate(5,
                    Date.valueOf(value))),
                    ThrowingRunnable.sneaky(() -> insertMovementStatement.setNull(5,
                            Types.DATE)));
        accountMovementData.getCategoryId()
            .ifPresentOrElse(ThrowingIntConsumer.sneaky(value -> insertMovementStatement.setInt(6,
                    value)),
                    ThrowingRunnable.sneaky(() -> insertMovementStatement.setNull(6,
                            Types.INTEGER)));
        accountMovementData.getComments()
            .ifPresentOrElse(ThrowingConsumer.sneaky(value -> insertMovementStatement.setString(7,
                    value)),
                    ThrowingRunnable.sneaky(() -> insertMovementStatement.setNull(7,
                            Types.VARCHAR)));
        accountMovementData.getOppositAccountId()
            .ifPresentOrElse(ThrowingIntConsumer.sneaky(value -> insertMovementStatement.setInt(8,
                    value)),
                    ThrowingRunnable.sneaky(() -> insertMovementStatement.setNull(8,
                            Types.INTEGER)));
        insertMovementStatement.setBigDecimal(9,
                accountMovementData.getAmount());
        insertMovementStatement.executeUpdate();
        return insertMovementStatement.getGeneratedKeys();
    }

    private void insertEntryItems(final PreparedStatement insertEntryItemStmt, MovementData movementData,
            Integer movementId) {
        logger.debug("Inserting entry items.");
        final var entryItems = movementData.getEntryItems();
        entryItems.forEach(ThrowingConsumer.sneaky(entryItem -> {
            insertEntryItemStmt.setInt(1,
                    movementId);
            insertEntryItemStmt.setInt(2,
                    entryItem.getCategoryId());
            insertEntryItemStmt.setString(3,
                    entryItem.getComments());
            insertEntryItemStmt.setBigDecimal(4,
                    entryItem.getAmount());
            insertEntryItemStmt.executeUpdate();
        }));
        logger.debug("{} entry items are inserted.",
                entryItems.size());
    }

    private int findAccountId(final PreparedStatement findAccountIdByNameStmt, final String accountName)
            throws MigrationException {
        logger.debug("Searching for account with name: {}.",
                accountName);
        try {
            findAccountIdByNameStmt.setString(1,
                    accountName);
            findAccountIdByNameStmt.execute();
            final var accountId = getAccountId(findAccountIdByNameStmt,
                    accountName);
            logger.debug("Account with name: {} is found.",
                    accountName);
            logger.trace("Account with name: {} is found under id: {}.",
                    accountName,
                    accountId);
            return accountId;
        } catch (final SQLException e) {
            final var message = MessageFormat.format("Cant find account id by name: {0}.",
                    accountName);
            logger.error(message);
            throw new MigrationException(message,
                e);
        }
    }

    private int getAccountId(final PreparedStatement findAccountIdByNameStmt, final String accountName)
            throws MigrationException {
        try (final var accountIdResultSet = findAccountIdByNameStmt.getResultSet()) {
            if (!accountIdResultSet.first()) {
                throw new MigrationException(MessageFormat.format("Account by name: {0} is not found.",
                        accountName));
            }
            return accountIdResultSet.getInt(1);
        } catch (final SQLException e) {
            throw new MigrationException("Cant extract account id.",
                e);
        }
    }

    private void updateAccountsMovemntsCountAndSum(final InitialMovementsJdbcStatements jdbcStatements)
            throws MigrationException {
        final var accountsMovementsSumCountSelectStmt = jdbcStatements.getAccountsMovementsSumCountSelect();
        final var accountMovementsSumCountUpdateStmt = jdbcStatements.getAccountMovementsSumCountUpdate();
        try (final var countsAndSums = accountsMovementsSumCountSelectStmt.executeQuery()) {
            while (countsAndSums.next()) {
                accountMovementsSumCountUpdateStmt.setInt(1,
                        countsAndSums.getInt(1));
                accountMovementsSumCountUpdateStmt.setBigDecimal(2,
                        countsAndSums.getBigDecimal(2));
                accountMovementsSumCountUpdateStmt.setInt(3,
                        countsAndSums.getInt(3));
                accountMovementsSumCountUpdateStmt.executeUpdate();
            }
        } catch (final SQLException e) {
            throw new MigrationException("Cannot update account movements counts and sums.",
                e);
        }

    }

    private void insertMovmentSlices(final InitialMovementsJdbcStatements jdbcStatements, final int accountId)
            throws MigrationException {
        try {
            final var accountMovementsMinMaxDatesSelectStmt = jdbcStatements.getAccountMovementsMinMaxDatesSelect();
            final PreparedStatement accountMovementsSumCountBetweenMovementDatesSelectStmt = jdbcStatements
                .getAccountMovementsSumCountBetweenMovementDatesSelect();
            final PreparedStatement accountMovementsSumCountBetweenBookingDatesSelectStmt = jdbcStatements
                .getAccountMovementsSumCountBetweenBookingDatesSelect();
            final PreparedStatement insertMovementSliceStmt = jdbcStatements.getMovementSliceInsert();

            accountMovementsMinMaxDatesSelectStmt.setInt(1,
                    accountId);
            accountMovementsSumCountBetweenMovementDatesSelectStmt.setInt(3,
                    accountId);
            accountMovementsSumCountBetweenBookingDatesSelectStmt.setInt(3,
                    accountId);
            insertMovementSliceStmt.setInt(1,
                    accountId);
            try (final var resultSet = accountMovementsMinMaxDatesSelectStmt.executeQuery()) {
                if (!resultSet.next()) {
                    return;
                }
                final var minDateTimestamp = resultSet.getTimestamp(1);
                final var maxDateTimestamp = resultSet.getTimestamp(2);
                final var minBookingDateTimestamp = resultSet.getTimestamp(3);
                final var maxBookingDateTimestamp = resultSet.getTimestamp(4);
                if (
                    minDateTimestamp == null ||
                        maxDateTimestamp == null ||
                        minBookingDateTimestamp == null ||
                        maxBookingDateTimestamp == null
                ) {
                    return;
                }
                final var minTime = Math.min(minDateTimestamp.getTime(),
                        minBookingDateTimestamp.getTime());
                final var minDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(minTime),
                        TimeZone.getDefault()
                            .toZoneId())
                    .truncatedTo(ChronoUnit.DAYS);
                final var maxTime = Math.max(maxDateTimestamp.getTime(),
                        maxBookingDateTimestamp.getTime());
                final var maxDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(maxTime),
                        TimeZone.getDefault()
                            .toZoneId())
                    .truncatedTo(ChronoUnit.DAYS);
                var startDate = minDate.withDayOfMonth(1)
                    .plusMonths(1);
                var prevDate = minDate;
                var movementCount = 0;
                var movementSum = BigDecimal.ZERO;
                var bookingCount = 0;
                var bookingSum = BigDecimal.ZERO;
                while (startDate.compareTo(maxDate) < 0) {
                    accountMovementsSumCountBetweenMovementDatesSelectStmt.setTimestamp(1,
                            Timestamp.valueOf(prevDate));
                    accountMovementsSumCountBetweenMovementDatesSelectStmt.setTimestamp(2,
                            Timestamp.valueOf(startDate));
                    try (final var countAndSumByMovementDateResultSet = accountMovementsSumCountBetweenMovementDatesSelectStmt
                        .executeQuery()) {
                        if (countAndSumByMovementDateResultSet.next()) {
                            movementCount += Optional.ofNullable(countAndSumByMovementDateResultSet.getInt(1))
                                .orElse(0);
                            movementSum = movementSum.add(Optional.ofNullable(countAndSumByMovementDateResultSet
                                .getBigDecimal(2))
                                .orElse(BigDecimal.ZERO));
                        }
                    }
                    accountMovementsSumCountBetweenBookingDatesSelectStmt.setTimestamp(1,
                            Timestamp.valueOf(prevDate));
                    accountMovementsSumCountBetweenBookingDatesSelectStmt.setTimestamp(2,
                            Timestamp.valueOf(startDate));
                    try (final var countAndSumByBookingDateResultSet = accountMovementsSumCountBetweenBookingDatesSelectStmt
                        .executeQuery()) {
                        if (countAndSumByBookingDateResultSet.next()) {
                            bookingCount += Optional.ofNullable(countAndSumByBookingDateResultSet.getInt(1))
                                .orElse(0);
                            bookingSum = bookingSum.add(Optional.ofNullable(countAndSumByBookingDateResultSet
                                .getBigDecimal(2))
                                .orElse(BigDecimal.ZERO));
                        }
                    }
                    insertMovementSliceStmt.setTimestamp(2,
                            Timestamp.valueOf(startDate));
                    insertMovementSliceStmt.setInt(3,
                            movementCount);
                    insertMovementSliceStmt.setBigDecimal(4,
                            movementSum);
                    insertMovementSliceStmt.setInt(5,
                            bookingCount);
                    insertMovementSliceStmt.setBigDecimal(6,
                            bookingSum);
                    insertMovementSliceStmt.execute();
                    prevDate = startDate;
                    startDate = startDate.plusMonths(1);
                }
            }
        } catch (final SQLException e) {
            throw new MigrationException("Error adding movement slices.",
                e);
        }
    }

}
