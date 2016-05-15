package org.utkuozdemir.watchdist.util;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.support.ConnectionSource;

import org.utkuozdemir.watchdist.app.Constants;
import org.utkuozdemir.watchdist.domain.Availability;
import org.utkuozdemir.watchdist.domain.Notes;
import org.utkuozdemir.watchdist.domain.Property;
import org.utkuozdemir.watchdist.domain.Soldier;
import org.utkuozdemir.watchdist.domain.Watch;
import org.utkuozdemir.watchdist.domain.WatchPoint;
import org.utkuozdemir.watchdist.domain.WatchValue;

import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class DbManager {
    private static Path dbPath = null;

    private static volatile DbManager instance;

    private ConnectionSource connectionSource;
    private TransactionManager transactionManager;

    private Dao<Availability, Integer> availabilityDao;
    private Dao<Soldier, Integer> soldierDao;
    private Dao<Watch, Integer> watchDao;
    private Dao<WatchPoint, Integer> watchPointDao;
    private Dao<WatchValue, Integer> watchValueDao;
    private Dao<Property, String> propertyDao;
    private Dao<Notes, String> notesDao;

    private DbManager() {
        try {
            connectionSource = new JdbcConnectionSource("jdbc:sqlite:" + dbPath.toString());
            transactionManager = new TransactionManager(connectionSource);
            availabilityDao = DaoManager.createDao(connectionSource, Availability.class);
            soldierDao = DaoManager.createDao(connectionSource, Soldier.class);
            watchDao = DaoManager.createDao(connectionSource, Watch.class);
            watchPointDao = DaoManager.createDao(connectionSource, WatchPoint.class);
            watchValueDao = DaoManager.createDao(connectionSource, WatchValue.class);
            propertyDao = DaoManager.createDao(connectionSource, Property.class);
            notesDao = DaoManager.createDao(connectionSource, Notes.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static DbManager getInstance() {
        if (dbPath == null)
            throw new RuntimeException("Cannot initialize DbManager: database path is null!");
        DbManager result = instance;
        if (result == null) { // First check (no locking)
            synchronized (DbManager.class) {
                result = instance;
                if (result == null) // Second check (with locking)
                    instance = result = new DbManager();
            }
        }
        return result;
    }

    public static void close() {
        DbManager dbManager = getInstance();
        if (dbManager.connectionSource != null) {
            dbManager.connectionSource.closeQuietly();
        }
    }

    public static List<Soldier> findAllActiveSoldiersOrderedByFullName() {
        try {
            return getInstance().soldierDao.queryBuilder().orderBy("full_name", true)
                    .where().eq("active", true).query();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Soldier> findAllActiveSoldiersOrdered() {
        try {
            DbManager dbManager = getInstance();
            PreparedQuery<Soldier> query = dbManager.soldierDao
                    .queryBuilder().orderBy("order", true).where().eq("active", true).prepare();
            return dbManager.soldierDao.query(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static int saveSoldier(final Soldier soldier) {
        try {
            return getInstance().transactionManager.callInTransaction(() -> {
                DbManager dbManager = getInstance();
                Dao.CreateOrUpdateStatus status = dbManager.soldierDao.createOrUpdate(soldier);

                if (status.isUpdated()) {
                    PreparedQuery<Availability> preparedDelete
                            = dbManager.availabilityDao.deleteBuilder()
                            .where().eq("soldier", soldier.getId()).prepare();
                    dbManager.availabilityDao.delete((PreparedDelete<Availability>) preparedDelete);
                }

                for (Availability availability : soldier.getAvailabilities()) {
                    dbManager.availabilityDao.create(availability);
                }
                return status.getNumLinesChanged();
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static int saveSoldiers(final Collection<Soldier> soldiers) {
        try {
            return getInstance().transactionManager.callInTransaction(() -> {
                int total = 0;
                for (Soldier soldier : soldiers) {
                    DbManager dbManager = getInstance();
                    Dao.CreateOrUpdateStatus status = dbManager.soldierDao.createOrUpdate(soldier);

                    if (status.isUpdated()) {
                        PreparedQuery<Availability> preparedDelete
                                = dbManager.availabilityDao.deleteBuilder()
                                .where().eq("soldier", soldier.getId()).prepare();
                        dbManager.availabilityDao.delete((PreparedDelete<Availability>) preparedDelete);
                    }

                    for (Availability availability : soldier.getAvailabilities()) {
                        dbManager.availabilityDao.create(availability);
                    }
                    total += status.getNumLinesChanged();
                }
                return total;
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static long countAllAvailabilities() {
        try {
            return getInstance().availabilityDao.countOf();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static int deleteSoldiers(final Collection<Soldier> soldiers) {
        try {
            DbManager dbManager = getInstance();
            return dbManager.transactionManager.callInTransaction(() -> {
                List<Integer> soldierIds
                        = soldiers.stream().mapToInt(input -> input == null ? 0 :
                        input.getId()).boxed().collect(Collectors.toList());

                PreparedDelete<Availability> preparedDelete
                        = (PreparedDelete<Availability>) dbManager.availabilityDao.deleteBuilder()
                        .where().in("soldier", soldierIds).prepare();
                dbManager.availabilityDao.delete(preparedDelete);

                return dbManager.soldierDao.delete(soldiers);

            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Optional<WatchPoint> findWatchPointById(int id) {
        try {
            return Optional.ofNullable(getInstance().watchPointDao.queryForId(id));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<WatchPoint> findAllActiveWatchPointsOrdered() {
        try {
            DbManager dbManager = getInstance();
            PreparedQuery<WatchPoint> query = dbManager.watchPointDao.queryBuilder()
                    .orderBy("order", true)
                    .where().eq("active", true).prepare();
            return dbManager.watchPointDao.query(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static int saveWatchPoint(WatchPoint watchPoint, SaveMode mode) {
        try {
            DbManager dbManager = getInstance();
            switch (mode) {
                case INSERT_OR_UPDATE: {
                    return dbManager.watchPointDao.createOrUpdate(watchPoint).getNumLinesChanged();
                }
                case REMOVE_OLD_CREATE_NEW: {
                    WatchPoint existing = dbManager.watchPointDao.queryForId(watchPoint.getId());
                    existing.setActive(false);
                    dbManager.watchPointDao.createOrUpdate(existing);
                    watchPoint.setId(null);
                    return dbManager.watchPointDao.createOrUpdate(watchPoint).getNumLinesChanged();
                }
                default:
                    throw new IllegalArgumentException("Invalid save mode: " + mode);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static int deleteWatchPoints(final Collection<WatchPoint> watchPoints) {
        try {
            DbManager dbManager = getInstance();
            return dbManager.transactionManager.callInTransaction(() -> {
                int count = 0;
                for (WatchPoint point : watchPoints) {
                    point.setActive(false);
                    dbManager.watchPointDao.update(point);
                    count++;
                }
                return count;
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static int saveWatchesAddPointsDeleteOldWatches(
            final LocalDate date,
            final Collection<Watch> watches) {
        try {
            DbManager dbManager = getInstance();
            return dbManager.transactionManager.callInTransaction(() -> {
                int deleted = dbManager.deleteWatchesByDate(date, WatchRemovalMode.UNDO_POINTS);

                int count = 0;
                for (Watch watch : watches) {
                    dbManager.watchDao.create(watch);

                    if (watch.getSoldier() != null) {
                        Soldier soldier = dbManager.soldierDao.queryForId(watch.getSoldier().getId());
                        double watchValue = dbManager.watchValueDao.queryForId(watch.getHour()).getValue();
                        soldier.setPoints(soldier.getPoints() + watchValue);
                        dbManager.soldierDao.update(soldier);
                    }
                    count++;
                }

                if (deleted == 0) {
                    List<Soldier> activeSergeants
                            = dbManager.soldierDao.queryForFieldValuesArgs(
                            Arrays.stream(new Object[][]{{"active", true}, {"sergeant", true}})
                                    .collect(Collectors.toMap(o -> String.valueOf(o[0]), o -> o[1]))
                    );
                    double sergeantPoints = Double.valueOf(getProperty(Constants.KEY_SERGEANT_DAILY_POINTS));
                    for (Soldier sergeant : activeSergeants) {
                        sergeant.setPoints(sergeant.getPoints() + sergeantPoints);
                        dbManager.soldierDao.update(sergeant);
                    }
                }

                return count;
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Watch> findWatchesByDate(LocalDate date) {
        try {
            return getInstance().watchDao.queryForFieldValues(
                    Collections.singletonMap("date", date.toString())
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Watch> findWatchesByDateAndHour(LocalDate date, int hour) {
        try {
            return getInstance().watchDao.queryForFieldValues(
                    Arrays.stream(new Object[][]{{"date", date.toString()}, {"hour", hour}})
                            .collect(Collectors.toMap(o -> String.valueOf(o[0]), o -> o[1]))
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<WatchValue> findAllWatchValues() {
        try {
            return getInstance().watchValueDao.queryForAll();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static int updateWatchValue(int hour, double value) {
        try {
            return getInstance().watchValueDao.update(new WatchValue(hour, value));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getProperty(String key) {
        try {
            Property property = getInstance().propertyDao.queryForId(key);
            return property != null ? property.getValue() : null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setProperty(String key, String value) {
        try {
            getInstance().propertyDao.createOrUpdate(new Property(key, value));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static int saveWatchValues(Collection<WatchValue> watchValues) {
        try {
            DbManager dbManager = getInstance();
            return dbManager.transactionManager.callInTransaction(() -> {
                int count = 0;
                for (WatchValue watchValue : watchValues) {
                    dbManager.watchValueDao.create(watchValue);
                    count++;
                }
                return count;
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static int saveNote(LocalDate date, String notes) {
        try {
            return getInstance().notesDao.createOrUpdate(new Notes(date, notes)).getNumLinesChanged();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getNote(LocalDate date) {
        try {
            Notes notes = getInstance().notesDao.queryForId(date.toString());
            return notes != null ? notes.getNotes() : "";
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized static void setDbPath(Path dbPath) {
        if (dbPath == null) throw new NullPointerException();
        DbManager.dbPath = dbPath;
        instance = null;
    }

    @SuppressWarnings("unchecked")
    private int deleteWatchesByDate(final LocalDate date, final WatchRemovalMode mode) {
        if (date == null) throw new NullPointerException();
        if (mode == null) throw new NullPointerException();
        try {
            return transactionManager.callInTransaction(() -> {
                PreparedQuery<Watch> preparedQuery
                        = watchDao.queryBuilder().where().eq("date", date.toString()).prepare();
                List<Watch> watches = watchDao.query(preparedQuery);

                if (mode == WatchRemovalMode.UNDO_POINTS) {
                    Map<Soldier, Double> soldierWonPoints = new HashMap<>();
                    for (Watch watch : watches) {
                        Soldier soldier = watch.getSoldier();
                        if (soldier != null) {
                            double valueBefore
                                    = soldierWonPoints.containsKey(soldier) ? soldierWonPoints.get(soldier) : 0;
                            soldierWonPoints.put(soldier, valueBefore + watch.getPointsWon());
                        }
                    }

                    for (Map.Entry<Soldier, Double> entry : soldierWonPoints.entrySet()) {
                        Soldier soldier = entry.getKey();
                        soldier.setPoints(soldier.getPoints() - entry.getValue());
                        soldierDao.update(soldier);
                    }
                }


                PreparedDelete<Watch> preparedDelete
                        = (PreparedDelete<Watch>) watchDao.deleteBuilder()
                        .where().eq("date", date.toString()).prepare();
                return watchDao.delete(preparedDelete);
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}