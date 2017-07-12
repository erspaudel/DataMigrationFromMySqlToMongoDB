
package test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.utils.MySqlUtils;
import com.utils.StringUtils;

public class SushilTest {


	public static void main(String[] args) throws Exception {
	
//		MySqlUtils.init("college");
//		
//		String s = "student_grades";
//		
//		System.out.println(s.substring(0, s.length()-1));

		
		String sems = "";
		
		int c = 1;
		
		for (int i = c; i > 0; i--) {
			sems += i + ",";
		}
		
		System.out.println(sems);
		
//		List<String> pks = MySqlUtils.getPrimaryKeys("college", "class_student");
//		
//		for (String string : pks) {
//			
//			
//			System.out.println(string);
//			
//			System.out.println(MySqlUtils.isForeignKey("class_student", string));
//		}
		
//		String foriegnTable ="college";
//		
//		List<String> primaryKeys = MySqlUtils.getStaticPrimaryKeys(foriegnTable);
//		List<String> keys = new ArrayList<>();
//
//		for (String string : primaryKeys) {
//			System.out.println(foriegnTable + "_" + string);
//		}
//
//		String where = "";
//		
		
	}

	public static void t1(String tableName, int startLimit, String orderBy) throws SQLException {

		StringBuilder sb = new StringBuilder();

		sb.append("SELECT * FROM ");
		sb.append(tableName);
		sb.append(" ORDER BY ");

		sb.append(orderBy);

		sb.append(" ASC limit ");
		sb.append(startLimit);
		sb.append(",");
		sb.append(1);

		System.out.println(sb.toString());

	}

	public static void t3(String tableName, int startLimit, String orderBy, String lastId) throws SQLException {

		StringBuilder sb = new StringBuilder();

		if (StringUtils.isEmpty(lastId)) {

			sb.append("SELECT * FROM ");
			sb.append(tableName);
			sb.append(" ORDER BY ");

			sb.append(orderBy);

			sb.append(" ASC limit ");
			sb.append(startLimit);
			sb.append(",");
			sb.append(1);
		}
		
		sb.append("SELECT * FROM ");
		sb.append(tableName);
		sb.append(" WHERE ");

		sb.append(orderBy);

		sb.append(" > ");
		sb.append(lastId);
		
		
		sb.append(" ORDER BY ");
		sb.append(orderBy);
		sb.append(" ASC limit ");
		sb.append(0);
		sb.append(",");
		sb.append(1);

		System.out.println(sb.toString());

	}

	public static void t2(String tableName, int startLimit, String orderBy) throws SQLException {

		StringBuilder sb = new StringBuilder();

		sb.append("SELECT t.* FROM (");
		sb.append("SELECT ");
		sb.append(orderBy);
		sb.append(" FROM ");
		sb.append(tableName);
		sb.append(" ORDER BY ");
		sb.append(orderBy);

		sb.append(" ASC limit ");
		sb.append(startLimit);
		sb.append(",");
		sb.append(1);
		sb.append(") q JOIN ");
		sb.append(tableName);
		sb.append(" t ON t.");
		sb.append(orderBy);
		sb.append("=q.");
		sb.append(orderBy);

		System.out.println(sb.toString());

	}

	public static int getSemester(String date) {

		int year = Integer.parseInt(date.split("-")[0]);

		int currentYear = 2017;

		if (year == currentYear) {
			return 1;
		}

		int randomNumber = TestDB.getRandomNumber(1, 2);

		int sem = (((currentYear - year) - 1) * 2) + randomNumber;

		return sem;

	}

	public static String getOrderByQuery(String dbName, String tableName) throws SQLException {

		List<String> primaryKeys = MySqlUtils.getPrimaryKeys(dbName, tableName);

		StringBuilder builder = new StringBuilder();

		for (String string : primaryKeys) {

			if (StringUtils.isNotEmpty(builder.toString())) {
				builder.append(",");
			}

			builder.append(tableName);
			builder.append(".");
			builder.append(string);
		}

		return builder.toString();
	}
}