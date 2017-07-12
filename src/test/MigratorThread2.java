package test;

public class MigratorThread2 implements Runnable {
/*
	private String tableName;
	private String dbName;
	private int migrationType;

	private final CountDownLatch latch;

	public MigratorThread2(String dbName, String tableName, int migrationType, CountDownLatch latch) {

		this.dbName = dbName;
		this.tableName = tableName;
		this.migrationType = migrationType;
		this.latch = latch;
	}
*/
	@Override
	public void run() {
/*
		Migrator migrator = null;

		System.out.println("Migrating \t" + tableName);

		if (migrationType == MigratorConstants.MIGRATION_DEFAULT) {

			migrator = new MySqlToMongoDb_Direct(dbName);

		} else if (migrationType == MigratorConstants.MIGRATION_FILE) {

			migrator = new MySqlToMongoDb_File(dbName);

		} else {

			try {

				throw new Exception("Migration type " + this.migrationType + " not supported!");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		try {

//			migrator.processTable(tableName);
		} catch (Exception e) {
			e.printStackTrace();
		}

		latch.countDown();*/
	}


}
