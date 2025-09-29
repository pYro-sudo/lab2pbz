package by.losik.service;

import by.losik.configuration.LiquibaseFactory;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.RanChangeSet;
import liquibase.database.Database;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
@Slf4j
public class MigrationService {

    @Inject
    LiquibaseFactory liquibaseFactory;

    public Uni<String> runMigration(String contexts, String labels) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            Liquibase liquibase = null;
            try {
                liquibase = liquibaseFactory.createLiquibase();
                Contexts contextObj = new Contexts(contexts);
                LabelExpression labelExpr = new LabelExpression(labels);
                liquibase.update(contextObj, labelExpr);
                return "Migrations applied successfully with contexts: " + contexts + ", labels: " + labels;
            } catch (Exception e) {
                throw new RuntimeException("Migration failed: " + e.getMessage(), e);
            } finally {
                liquibaseFactory.closeLiquibase(liquibase);
            }
        }));
    }

    public Uni<String> rollbackLastChange(String contexts, String labels) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            Liquibase liquibase = null;
            try {
                liquibase = liquibaseFactory.createLiquibase();
                Contexts contextObj = new Contexts(contexts);
                LabelExpression labelExpr = new LabelExpression(labels);
                liquibase.rollback(1, contextObj, labelExpr);
                return "Last change rolled back successfully with contexts: " + contexts + ", labels: " + labels;
            } catch (Exception e) {
                throw new RuntimeException("Rollback failed: " + e.getMessage(), e);
            } finally {
                liquibaseFactory.closeLiquibase(liquibase);
            }
        }));
    }

    public Uni<String> rollbackToDate(Date date, String contexts, String labels) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            Liquibase liquibase = null;
            try {
                liquibase = liquibaseFactory.createLiquibase();
                Contexts contextObj = new Contexts(contexts);
                LabelExpression labelExpr = new LabelExpression(labels);
                liquibase.rollback(date, contextObj, labelExpr);
                return "Rolled back to date: " + date + " with contexts: " + contexts + ", labels: " + labels;
            } catch (Exception e) {
                throw new RuntimeException("Rollback to date failed: " + e.getMessage(), e);
            } finally {
                liquibaseFactory.closeLiquibase(liquibase);
            }
        }));
    }

    public Uni<String> rollbackToTag(String tag, String contexts, String labels) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            Liquibase liquibase = null;
            try {
                liquibase = liquibaseFactory.createLiquibase();
                Contexts contextObj = new Contexts(contexts);
                LabelExpression labelExpr = new LabelExpression(labels);
                liquibase.rollback(tag, contextObj, labelExpr);
                return "Rolled back to tag: " + tag + " with contexts: " + contexts + ", labels: " + labels;
            } catch (Exception e) {
                throw new RuntimeException("Rollback to tag failed: " + e.getMessage(), e);
            } finally {
                liquibaseFactory.closeLiquibase(liquibase);
            }
        }));
    }

    public Uni<String> rollbackCount(int count, String contexts, String labels) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            Liquibase liquibase = null;
            try {
                liquibase = liquibaseFactory.createLiquibase();
                Contexts contextObj = new Contexts(contexts);
                LabelExpression labelExpr = new LabelExpression(labels);
                liquibase.rollback(count, contextObj, labelExpr);
                return "Rolled back " + count + " change(s) with contexts: " + contexts + ", labels: " + labels;
            } catch (Exception e) {
                throw new RuntimeException("Rollback count failed: " + e.getMessage(), e);
            } finally {
                liquibaseFactory.closeLiquibase(liquibase);
            }
        }));
    }

    public Uni<String> tagDatabase(String tag) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            Liquibase liquibase = null;
            try {
                liquibase = liquibaseFactory.createLiquibase();
                liquibase.tag(tag);
                return "Database tagged with: " + tag;
            } catch (Exception e) {
                throw new RuntimeException("Tagging failed: " + e.getMessage(), e);
            } finally {
                liquibaseFactory.closeLiquibase(liquibase);
            }
        }));
    }

    public Uni<Map<String, Object>> getMigrationStatus(String contexts, String labels) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            Liquibase liquibase = null;
            try {
                liquibase = liquibaseFactory.createLiquibase();
                Database database = liquibase.getDatabase();
                Contexts contextObj = new Contexts(contexts);
                LabelExpression labelExpr = new LabelExpression(labels);

                List<RanChangeSet> ranChangeSets = database.getRanChangeSetList();
                List<ChangeSet> unrunChangeSets = liquibase.listUnrunChangeSets(contextObj, labelExpr);

                List<Map<String, String>> appliedChanges = ranChangeSets.stream()
                        .map(rcs -> Map.of(
                                "id", rcs.getId(),
                                "author", rcs.getAuthor(),
                                "stored", rcs.getStoredChangeLog(),
                                "dateExecuted", rcs.getDateExecuted().toString(),
                                "tag", rcs.getTag(),
                                "labels", rcs.getLabels() != null ? rcs.getLabels().toString() : ""
                        ))
                        .collect(Collectors.toList());

                List<Map<String, String>> pendingChanges = unrunChangeSets.stream()
                        .map(ucs -> Map.of(
                                "id", ucs.getId(),
                                "author", ucs.getAuthor(),
                                "path", ucs.getFilePath(),
                                "description", ucs.getDescription(),
                                "labels", ucs.getLabels() != null ? ucs.getLabels().toString() : ""
                        ))
                        .collect(Collectors.toList());

                return Map.of(
                        "database", database.getDatabaseProductName(),
                        "contexts", contexts,
                        "labels", labels,
                        "appliedChangesCount", appliedChanges.size(),
                        "pendingChangesCount", pendingChanges.size(),
                        "appliedChanges", appliedChanges,
                        "pendingChanges", pendingChanges
                );
            } catch (Exception e) {
                throw new RuntimeException("Failed to get migration status: " + e.getMessage(), e);
            } finally {
                liquibaseFactory.closeLiquibase(liquibase);
            }
        }));
    }

    public Uni<String> validateMigrations(String contexts, String labels) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            Liquibase liquibase = null;
            try {
                liquibase = liquibaseFactory.createLiquibase();
                liquibase.validate();
                return "Migrations validation passed for contexts: " + contexts + ", labels: " + labels;
            } catch (Exception e) {
                throw new RuntimeException("Validation failed: " + e.getMessage(), e);
            } finally {
                liquibaseFactory.closeLiquibase(liquibase);
            }
        }));
    }

    public Uni<List<Map<String, Object>>> getAppliedMigrations() {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            Liquibase liquibase = null;
            try {
                liquibase = liquibaseFactory.createLiquibase();
                Database database = liquibase.getDatabase();

                List<RanChangeSet> ranChangeSets = database.getRanChangeSetList();

                return ranChangeSets.stream()
                        .map(rcs -> Map.of(
                                "id", rcs.getId(),
                                "author", rcs.getAuthor(),
                                "changelog", rcs.getStoredChangeLog(),
                                "dateExecuted", rcs.getDateExecuted(),
                                "tag", rcs.getTag(),
                                "execType", rcs.getExecType(),
                                "checksum", rcs.getLastCheckSum(),
                                "labels", rcs.getLabels() != null ? rcs.getLabels().toString() : ""
                        ))
                        .collect(Collectors.toList());
            } catch (Exception e) {
                throw new RuntimeException("Failed to get applied migrations: " + e.getMessage(), e);
            } finally {
                liquibaseFactory.closeLiquibase(liquibase);
            }
        }));
    }

    public Uni<String> clearCheckSums() {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            Liquibase liquibase = null;
            try {
                liquibase = liquibaseFactory.createLiquibase();
                liquibase.clearCheckSums();
                return "CheckSums cleared successfully";
            } catch (Exception e) {
                throw new RuntimeException("Failed to clear checksums: " + e.getMessage(), e);
            } finally {
                liquibaseFactory.closeLiquibase(liquibase);
            }
        }));
    }
}