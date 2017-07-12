package test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class DummyRecords {

	static final String dbName = "hotel";
	static final String dbUrl = "jdbc:mysql://localhost:3306/" + dbName;

	// Database credentials
	static final String username = "root";
	static final String password = "";

	static Connection connection = null;
	static Statement statement = null;

	public static void main(String args[]) {

		try {

			Class.forName("com.mysql.jdbc.Driver");

			System.out.println("Connecting to database...");
			connection = DriverManager.getConnection(dbUrl, username, password);
			System.out.println("Connection to database successful...\n");

			statement = connection.createStatement();

			insertRecords();

		} catch (Exception e) {

			e.printStackTrace();
		} finally {

			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void insertRecords() throws SQLException {

		System.out.println("Inserting records ...");

		for (int i = 1; i < 1000; i++) {

			System.out.println("Processing: " + i);

			insertLogo(i);
			insertInfo(i);
			insertLocation(i);

			int companyId = getMaxId("hotel", "company");
			insertCompanes(companyId);

			insertSocialLink(companyId);

			insertImages(companyId);
			insertVideo(companyId);
			insertVacancy(companyId);
			insertUsers(companyId);

		}

		System.out.println("Records inserted successfully");

	}

	public static void insertLogo(int id) throws SQLException {

		StringBuilder builder = new StringBuilder("INSERT INTO logo VALUES (");
		builder.append("0,");
		builder.append("'" + id + ".png',");// name
		builder.append("null,");// url
		builder.append(true + ")");// enabled

		System.out.println("LOGO: " + builder.toString());

		statement.executeUpdate(builder.toString());

	}

	public static void insertInfo(int id) throws SQLException {

		StringBuilder builder = new StringBuilder("INSERT INTO info VALUES (");
		builder.append("0,");// id
		builder.append(id + ",");// phone1
		builder.append("null,");// phone2
		builder.append("null,");// phone3
		builder.append(id);// fax1
		builder.append(",null,");// fax2
		builder.append(getDate() + ",");// created date
		builder.append("'" + id + "@gmail.com'");// emailaddress
		builder.append(",null)");// postal address

		System.out.println("INFO: " + builder.toString());

		statement.executeUpdate(builder.toString());

	}

	public static void insertLocation(int id) throws SQLException {

		StringBuilder builder = new StringBuilder("INSERT INTO LOCATION VALUES (");
		builder.append("0,");
		builder.append("'" + getZone() + "',");
		builder.append("'" + getDistrict() + "',");
		builder.append("'" + getMunicipality() + "',");
		builder.append("'vdc" + id + "',");// vdc
		builder.append("'" + id + "',");// chowk
		builder.append("null,");// gps
		builder.append("'" + getWardNo() + "',");// ward_no
		builder.append("null)");// house_no

		System.out.println("LOCATION: " + builder.toString());

		statement.executeUpdate(builder.toString());
	}

	public static void insertCompanes(int id) throws SQLException {

		StringBuilder builder = new StringBuilder("INSERT INTO company VALUES (");

		int infoId = getMaxId("hotel", "info");
		int locationId = getMaxId("hotel", "location");

		builder.append(++id + ",");// id
		builder.append("'" + getName(id) + "',");// name
		builder.append("'www.myhotel.com/" + getName(id).replace(" ", "") + "',");// nameurl
		builder.append(1 + ",");// account_type_id
		builder.append("null,");// motto
		builder.append("2000,");// estd_year
		builder.append("null,");// url_name
		builder.append(infoId + ",");// info_id
		builder.append(locationId + ",");// location_id
		builder.append(1 + ",");// business_category_id
		builder.append("null,");// logos_id
		builder.append(getDate() + ",");// last_login
		builder.append(true + ",");// enabled
		builder.append(false + ",");// del_flag
		builder.append("null,");// deactivate_id
		builder.append("null,");// logo_name
		builder.append("false,");// deactivated
		builder.append("true)");// seen

		System.out.println("COMPANY: " + builder.toString());

		statement.executeUpdate(builder.toString());
	}

	public static void insertSocialLink(int id) throws SQLException {

		StringBuilder builder = new StringBuilder("INSERT INTO social_link VALUES (");
		builder.append("0,");
		builder.append(id);// company_id
		builder.append(",'www.facebook.com',");// facebook_link
		builder.append("'www.twitter.com',");// twitter_link
		builder.append("'www.google.com',");// google_link
		builder.append("'www.instagram.com',");// instagram_link
		builder.append("'www.youtube.com')");// youtube_link

		System.out.println("SOCIAL LINK: " + builder.toString());

		statement.executeUpdate(builder.toString());
	}

	public static void insertImages(int id) throws SQLException {

		int counter = getRandomNumber(1, 5);

		for (int i = 0; i < counter; i++) {

			StringBuilder builder = new StringBuilder("INSERT INTO IMAGE VALUES (");
			builder.append("0,");
			builder.append(id + ",");// company_id
			builder.append("'" + i + ".jpg',");// name
			builder.append("null,");// url
			builder.append(true + ")");// enabled

			System.out.println("IMAGE " + i + ": " + builder.toString());

			statement.executeUpdate(builder.toString());
		}
	}

	public static void insertVideo(int id) throws SQLException {

		int counter = getRandomNumber(1, 5);

		int maxId = getMaxId("hotel", "video");

		int start = maxId + 1;

		for (int i = start; i < (counter + start); i++) {

			StringBuilder builder = new StringBuilder("INSERT INTO video VALUES (");
			builder.append("0,");
			builder.append(id + ",");// company_id
			builder.append("'" + i + ".mp4',");// name
			builder.append("null,");// url
			builder.append(true + ")");// enabled

			System.out.println("VIDEO " + i + ": " + builder.toString());

			statement.executeUpdate(builder.toString());
		}
	}

	public static void insertUsers(int id) throws SQLException {

		int counter = getRandomNumber(1, 5);

		int maxId = getMaxId("hotel", "user");

		int start = maxId + 1;

		for (int i = start; i < (counter + start); i++) {

			StringBuilder builder = new StringBuilder("INSERT INTO user VALUES (");
			builder.append("0,");
			builder.append("'" + getName(id).replace(" ", "") + "',");// username
			builder.append("'" + getName(id).hashCode() + "',");// password
			builder.append(id + ",");// company_id
			builder.append("1,");// authority
			builder.append(getDate() + ",");// last_login
			builder.append(true + ",");// enabled
			builder.append(false + ",");// del_flag
			builder.append(i + ")");// total_login

			System.out.println("USER: " + builder.toString());

			statement.executeUpdate(builder.toString());
		}
	}

	public static void insertVacancy(int id) throws SQLException {

		int counter = getRandomNumber(1, 5);

		int maxId = getMaxId("hotel", "vacancy");

		int start = maxId + 1;

		for (int i = start; i < (counter + start); i++) {

			StringBuilder builder = new StringBuilder("INSERT INTO vacancy VALUES (");
			builder.append("0,");
			builder.append(id + ",");// company_id
			builder.append(getDate() + ",");// issued_date
			builder.append(getDate() + ",");// dead_line_date
			builder.append("null,");// description
			builder.append("null,");// note
			builder.append("null,");// offer
			builder.append("null,");// position
			builder.append(i + ",");// no_of_vacancy
			builder.append(true + ",");// enabled
			builder.append(false + ",");// del_flag
			builder.append(true + ")");// seen

			System.out.println("VACANCY: " + builder.toString());

			statement.executeUpdate(builder.toString());
		}
	}

	public static int getRandomNumber(int min, int max) {

		return ThreadLocalRandom.current().nextInt(min, max + 1);
	}

	public static String getZone() {
		List<String> zones = new ArrayList<>();
		zones.add("Gandaki");
		zones.add("Bagmati");
		zones.add("Narayani");
		zones.add("Lumbini");

		int random = getRandomNumber(0, zones.size() - 1);
		return zones.get(random);
	}

	public static String getDistrict() {
		List<String> districts = new ArrayList<>();
		districts.add("Kaski");
		districts.add("Baglung");
		districts.add("Chitwan");
		districts.add("Syangja");
		districts.add("Tanahun");
		districts.add("Bhaktapur");

		int random = getRandomNumber(0, districts.size() - 1);
		return districts.get(random);
	}

	public static String getMunicipality() {

		List<String> municipalities = new ArrayList<>();
		municipalities.add("Lekhnath");
		municipalities.add("Pokhara");
		municipalities.add("Kathmandu");
		municipalities.add("Narayngadh");
		municipalities.add("Butwal");

		int random = getRandomNumber(0, municipalities.size() - 1);
		return municipalities.get(random);
	}

	public static int getWardNo() {

		return getRandomNumber(1, 100);
	}

	public static int getMaxId(String dbName, String tableName) throws SQLException {

		String query = "SELECT MAX(id) FROM " + tableName;

		ResultSet rs = statement.executeQuery(query);
		rs.next();
		return rs.getInt(1);
	}

	public static String getDate() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		return "'" + format.format(new Date()) + "'";
	}

	public static String getName(int id) {
		String value = String.valueOf(id);

		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < value.length(); i++) {

			Character c = value.charAt(i);

			int data = Integer.parseInt(c.toString());

			if (data == 0) {
				builder.append("Zero ");
			} else if (data == 1) {
				builder.append("One ");
			} else if (data == 2) {
				builder.append("Two ");
			} else if (data == 3) {
				builder.append("Three ");
			} else if (data == 4) {
				builder.append("Four ");
			} else if (data == 5) {
				builder.append("Five ");
			} else if (data == 6) {
				builder.append("Six ");
			} else if (data == 7) {
				builder.append("Seven ");
			} else if (data == 8) {
				builder.append("Eight ");
			} else if (data == 9) {
				builder.append("Nine ");
			}
		}

		return builder.toString();

	}
}
