package tsk.jgnk.watchdist;

import org.junit.Test;

public class Tests {
    @Test
    public void createSoldiers() throws Exception {
//        Random r = new Random();
//
//        String[] firstNames = {"Utku", "Ahmet", "Mustafa", "Mehmet", "Fatih", "Engin", "Mehmet", "Numan",
//                "Coşkun", "Ömer", "Ali", "Okan", "Şener", "Süleyman"};
//        String[] lastNames = {"Erkan", "Kahvecioğlu", "Demir", "Öztürk", "Elhan", "Uçar",
//                "Yiğit", "Torun", "Seyrek", "Erdoğan", "Kılıçdaroğlu"};
//        String[] duties = {"AR-GE", "Terzi", "Fast-Food", "Şoför", "Kazancı",
//                "Tören Mangası", "Mıntıkacı", "Bulaşıkçı", "Sıvacı", "Elektrikçi"};
//
//        InputStream dbInputStream = DbManager.class.getClassLoader().getResourceAsStream("clean_db.db");
//        checkNotNull(dbInputStream);
//        Path dbPath = FileManager.getDatabaseTemplate();
//        if (!Files.exists(dbPath)) {
//            Files.copy(dbInputStream, dbPath);
//        }
//
//        ConnectionSource connectionSource = new JdbcConnectionSource("jdbc:sqlite:" + dbPath.toString());
//        DbManager.initialize(connectionSource);
//        for (int i = 0; i < 300; i++) {
//            String fullName = firstNames[r.nextInt(firstNames.length)] + " " + lastNames[r.nextInt(lastNames.length)];
//            System.out.println(fullName);
//
//            boolean sergeant = r.nextInt(100) < 10;
//            Soldier soldier = new Soldier(fullName, duties[r.nextInt(duties.length)], !sergeant, sergeant);
//            DbManager.createSoldier(soldier);
//        }
    }
}
