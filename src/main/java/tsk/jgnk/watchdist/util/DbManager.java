package tsk.jgnk.watchdist.util;

import com.google.common.base.Optional;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.support.ConnectionSource;
import org.joda.time.LocalDate;
import tsk.jgnk.watchdist.domain.*;

import java.sql.SQLException;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class DbManager {
    private static TransactionManager transactionManager;

    private static Dao<Availability, Integer> availabilityDao;
    private static Dao<Soldier, Integer> soldierDao;
    private static Dao<Watch, Integer> watchDao;
    private static Dao<WatchPoint, Integer> watchPointDao;
    private static Dao<WatchValue, Integer> watchValueDao;
    private static Dao<Property, String> propertyDao;


    public static void initialize(ConnectionSource connectionSource) {
        try {
            DbManager.transactionManager = new TransactionManager(connectionSource);
            availabilityDao = DaoManager.createDao(connectionSource, Availability.class);
            soldierDao = DaoManager.createDao(connectionSource, Soldier.class);
            watchDao = DaoManager.createDao(connectionSource, Watch.class);
            watchPointDao = DaoManager.createDao(connectionSource, WatchPoint.class);
            watchValueDao = DaoManager.createDao(connectionSource, WatchValue.class);
            propertyDao = DaoManager.createDao(connectionSource, Property.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Soldier> findAllActiveSoldiersOrderedByFullName() {
        try {
            return soldierDao.queryBuilder().orderBy("full_name", true)
                    .where().eq("active", true).query();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Soldier> findAllActiveSoldiers() {
        try {
            return soldierDao.queryForFieldValues(Collections.singletonMap("active", (Object) true));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Soldier> findAllActiveSoldiersOrderedByPointsAsc() {
        try {
            return soldierDao.queryBuilder().orderBy("points", true)
                    .where().eq("active", true).query();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static int createSoldier(final Soldier soldier) {
        try {
            return transactionManager.callInTransaction(() -> {
                int count = soldierDao.create(soldier);
                for (Availability availability : soldier.getAvailabilities()) {
                    availabilityDao.create(availability);
                }
                return count;
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static int updateSoldier(final Soldier soldier) {
        try {
            return transactionManager.callInTransaction(() -> {
                int count = soldierDao.update(soldier);

                PreparedQuery<Availability> preparedDelete
                        = availabilityDao.deleteBuilder().where().eq("soldier", soldier.getId()).prepare();

                availabilityDao.delete((PreparedDelete<Availability>) preparedDelete);

                for (Availability availability : soldier.getAvailabilities()) {
                    availabilityDao.create(availability);
                }
                return count;
            });

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static int deleteSoldiers(final Collection<Soldier> soldiers) {
        try {
            return transactionManager.callInTransaction(() -> {
                Collection<Integer> soldierIds
                        = Collections2.transform(soldiers, input -> input == null ? 0 : input.getId());

                PreparedDelete<Availability> preparedDelete
                        = (PreparedDelete<Availability>) availabilityDao.deleteBuilder()
                        .where().in("soldier", soldierIds).prepare();
                availabilityDao.delete(preparedDelete);

                return soldierDao.delete(soldiers);

            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Optional<WatchPoint> findWatchPointById(int id) {
        try {
            return Optional.fromNullable(watchPointDao.queryForId(id));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<WatchPoint> findAllActiveWatchPoints() {
        try {
            return watchPointDao.queryForFieldValues(Collections.singletonMap("active", (Object) true));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static int createWatchPoint(WatchPoint watchPoint) {
        try {
            return watchPointDao.create(watchPoint);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static int updateWatchPoint(WatchPoint watchPoint) {
        try {
            return watchPointDao.update(watchPoint);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static int deleteWatchPoints(final Collection<WatchPoint> watchPoints) {
        try {
            return transactionManager.callInTransaction(() -> {
                int count = 0;
                for (WatchPoint point : watchPoints) {
                    point.setActive(false);
                    watchPointDao.update(point);
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
            final Collection<Watch> watches,
            final WatchRemovalMode removalMode) {
        try {
            return transactionManager.callInTransaction(() -> {
                int deleted = deleteWatchesByDate(date, removalMode);

                int count = 0;
                for (Watch watch : watches) {
                    watchDao.create(watch);
                    Soldier soldier = soldierDao.queryForId(watch.getSoldier().getId());

                    double watchValue = watchValueDao.queryForId(watch.getHour()).getValue();
                    soldier.setPoints(soldier.getPoints() + watchValue);
                    soldierDao.update(soldier);
                    count++;
                }

                if (deleted == 0) {
                    List<Soldier> activeSergeants
                            = soldierDao.queryForFieldValuesArgs(ImmutableMap.of("active", true, "sergeant", true));
                    double sergeantPoints = Double.valueOf(getProperty(Constants.SERGEANT_DAILY_POINTS));
                    for (Soldier sergeant : activeSergeants) {
                        sergeant.setPoints(sergeant.getPoints() + sergeantPoints);
                        soldierDao.update(sergeant);
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
            return watchDao.queryForFieldValues(
                    ImmutableMap.<String, Object>of("date", date.toString(Constants.DATE_FORMAT))
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Watch> findWatchesByDateAndHour(LocalDate date, int hour) {
        try {
            return watchDao.queryForFieldValues(
                    ImmutableMap.<String, Object>of("date", date.toString(Constants.DATE_FORMAT),
                            "hour", hour)
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static int deleteWatchesByDate(final LocalDate date, final WatchRemovalMode mode) {
        checkNotNull(date);
        checkNotNull(mode);
        try {
            return transactionManager.callInTransaction(() -> {
                PreparedQuery<Watch> preparedQuery
                        = watchDao.queryBuilder().where().eq("date", date.toString(Constants.DATE_FORMAT)).prepare();
                List<Watch> watches = watchDao.query(preparedQuery);

                if (mode == WatchRemovalMode.UNDO_POINTS) {
                    Map<Soldier, Double> soldierWonPoints = new HashMap<>();
                    for (Watch watch : watches) {
                        Soldier soldier = watch.getSoldier();
                        double valueBefore
                                = soldierWonPoints.containsKey(soldier) ? soldierWonPoints.get(soldier) : 0;
                        soldierWonPoints.put(soldier, valueBefore + watch.getPointsWon());
                    }

                    for (Map.Entry<Soldier, Double> entry : soldierWonPoints.entrySet()) {
                        Soldier soldier = entry.getKey();
                        soldier.setPoints(soldier.getPoints() - entry.getValue());
                        soldierDao.update(soldier);
                    }
                }


                PreparedDelete<Watch> preparedDelete
                        = (PreparedDelete<Watch>) watchDao.deleteBuilder()
                        .where().eq("date", date.toString(Constants.DATE_FORMAT)).prepare();
                return watchDao.delete(preparedDelete);
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<WatchValue> findAllWatchValues() {
        try {
            return watchValueDao.queryForAll();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static int updateWatchValue(int hour, double value) {
        checkArgument(hour >= 0 && hour <= 11, "Invalid hour!");
        checkArgument(value > 0, "Invalid value!");

        try {
            return watchValueDao.update(new WatchValue(hour, value));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getProperty(String key) {
        try {
            Property property = propertyDao.queryForId(key);
            return property != null ? property.getValue() : null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setProperty(String key, String value) {
        try {
            propertyDao.createOrUpdate(new Property(key, value));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}