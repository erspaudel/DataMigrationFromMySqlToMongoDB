package test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import com.utils.StringUtils;

public class TestDB {

	static final String dbName = "college";
	static final String dbUrl = "jdbc:mysql://localhost:3306/" + dbName;

	// Database credentials
	static final String username = "root";
	static final String password = "";

	static Connection connection = null;
	// static Statement statement = null;

	static final String BACHELOR = "BACHELOR";
	static final String MASTER = "MASTER";

	static final int INSERT_STUDENTS_COUNT = 400;
	static final int INSERT_TEACHER_COUNT = 60;

	static final boolean debug = false;

	public static void main(String args[]) {

		try {

			Class.forName("com.mysql.jdbc.Driver");

			System.out.println("Connecting to database...");
			connection = DriverManager.getConnection(dbUrl, username, password);
			System.out.println("Connection to database successful...\n");

			// statement = connection.createStatement();

			truncateTables();

			insertRecords();

			// insertCourse(2);

			// insertRoutine(2, 1);

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
			//
			// if (statement != null) {
			// try {
			// statement.close();
			// } catch (SQLException e) {
			// e.printStackTrace();
			// }
			// }
		}
	}

	public static void insertRecords() throws SQLException {

		System.out.println("Inserting records ...");

		insertCollege();

		String queryCollege = "select id from college";

		ResultSet rsCollege = connection.createStatement().executeQuery(queryCollege);

		while (rsCollege.next()) {

			int collegeId = rsCollege.getInt(1);

			insertFaculty(collegeId);

			String queryFaculty = "select id from faculty where college_id=" + collegeId;

			ResultSet rsFaculty = connection.createStatement().executeQuery(queryFaculty);

			int facultyIndex = 0;

			while (rsFaculty.next()) {

				int facultyId = rsFaculty.getInt(1);

				insertTeacher(collegeId, facultyId);

				insertPrograms(facultyId, facultyIndex);

				String queryProgram = "select id from program where faculty_id=" + facultyId;

				ResultSet rsProgram = connection.createStatement().executeQuery(queryProgram);

				while (rsProgram.next()) {

					int programId = rsProgram.getInt(1);

					insertClass(collegeId, facultyId, programId);

					// insertStudent(programId);
				}

				facultyIndex++;

			}

		}

		rsCollege = connection.createStatement().executeQuery(queryCollege);

		while (rsCollege.next()) {

			int collegeId = rsCollege.getInt(1);

			String queryFaculty = "select id from faculty where college_id=" + collegeId;

			ResultSet rsFaculty = connection.createStatement().executeQuery(queryFaculty);

			while (rsFaculty.next()) {

				int facultyId = rsFaculty.getInt(1);

				String queryProgram = "select id,code from program where faculty_id=" + facultyId;

				ResultSet rsProgram = connection.createStatement().executeQuery(queryProgram);

				while (rsProgram.next()) {

					int programId = rsProgram.getInt(1);

					insertStudent(collegeId, programId);

					insertStudentAttendance(programId);
				}

			}

		}

		System.out.println("Records inserted successfully");

	}

	public static void insertCollege() throws SQLException {

		StringBuilder builder = new StringBuilder("INSERT INTO college VALUES (");
		builder.append("0,");
		builder.append("'Nepal College of Information Technology',");// name
		builder.append("'NCIT',");// code
		builder.append("'Balkumari, Lalitpur, Nepal',");// address
		builder.append("'015186054',");// contact
		builder.append("'015186054',");// fax
		builder.append("'info@ncit.edu.np',");// email
		builder.append("'www.ncit.edu.np')");// url

		debug("COLLEGE 1: " + builder.toString());

		connection.createStatement().executeUpdate(builder.toString());

		builder = new StringBuilder("INSERT INTO college VALUES (");
		builder.append("0,");
		builder.append("'Nepal Engineering College',");// name
		builder.append("'NEC',");// code
		builder.append("'Changunarayan, Bhaktapur, Nepal',");// address
		builder.append("'016666054',");// contact
		builder.append("'016666054',");// fax
		builder.append("'info@nec.edu.np',");// email
		builder.append("'www.nec.edu.np')");// url

		debug("COLLEGE 2: " + builder.toString());

		connection.createStatement().executeUpdate(builder.toString());

	}

	public static void insertFaculty(int collegeId) throws SQLException {

		StringBuilder builder = new StringBuilder("INSERT INTO faculty VALUES (");
		builder.append("0,");
		builder.append("'Science and Technology',");// name
		builder.append(collegeId + ")");// collegeid

		debug("FACULTY 1: " + builder.toString());

		connection.createStatement().executeUpdate(builder.toString());

		builder = new StringBuilder("INSERT INTO faculty VALUES (");
		builder.append("0,");
		builder.append("'Management',");// name
		builder.append(collegeId + ")");// collegeid

		debug("FACULTY 2: " + builder.toString());

		connection.createStatement().executeUpdate(builder.toString());

	}

	public static void insertPrograms(int facultyId, int facultyIndex) throws SQLException {

		String[] programs = getPrograms(facultyId, facultyIndex);

		for (String program : programs) {

			String[] codeAndProgram = program.split("-");

			StringBuilder builder = new StringBuilder("INSERT INTO program VALUES (");

			String code = codeAndProgram[0];
			String name = codeAndProgram[1];

			builder.append("0,'");
			builder.append(name + "',");// name
			builder.append(facultyId + ",'");// faculty_id
			builder.append(code + "')");// code

			debug("PROGRAM: " + builder.toString());

			connection.createStatement().executeUpdate(builder.toString());

			int programId = getMaxId(dbName, "program");

			insertCourse(programId);
		}
	}

	public static void insertClass(int collegeId, int facultyId, int programId) throws SQLException {

		String[] years = getBachelorYearsWithSemester();
		String[] sections = getSections();

		insertClass(collegeId, facultyId, programId, years, sections, BACHELOR);

		years = getMasterYearsWithSemester();

		insertClass(collegeId, facultyId, programId, years, sections, MASTER);
	}

	private static void insertClass(int collegeId, int facultyId, int programId, String[] years, String[] sections,
			String level) throws SQLException {
		StringBuilder builder;
		for (String yearSem : years) {

			String[] value = yearSem.split("-");
			String year = value[0];
			String sem = value[1];

			String query = "select code from program where id = " + programId;

			ResultSet rsClass = connection.createStatement().executeQuery(query);
			rsClass.next();
			String code = rsClass.getString(1);

			String name = getClassName(code, level);

			for (String section : sections) {

				builder = new StringBuilder("INSERT INTO class VALUES (");

				builder.append("0,");// id
				builder.append(collegeId + ",");// collegeid
				builder.append(programId + ",'");// programid
				builder.append(section + "','");// section
				builder.append(name + "','");// name
				builder.append(year + "','");// batch
				builder.append(year + "-" + name + "-" + section + "',");// code
				builder.append(sem + ",'");// semester
				builder.append(level + "')");// level

				debug("CLASS: " + builder.toString());

				connection.createStatement().executeUpdate(builder.toString());

				int maxClassId = getMaxId(dbName, "class");

				int randomRoutineCount = getRandomNumber(40, 70);

				for (int i = 0; i < randomRoutineCount; i++) {

					insertRoutine(facultyId, programId, maxClassId);
				}

			}
		}
	}

	public static void insertCourse(int programId) throws SQLException {

		Map<String, String[]> courseMap = CourseUtils.getCourseMap();

		String query = "select code from program where id=" + programId;

		ResultSet rs = connection.createStatement().executeQuery(query);

		rs.next();

		String[] levels = { "BACHELOR", "MASTER" };

		String code = rs.getString(1);

		String[] courses = null;

		for (String level : levels) {

			// loop for 8 sems
			for (int i = 1; i <= 8; i++) {

				// get bachelors courses first

				String mapCode = code + "_" + level + "_" + i;

				courses = courseMap.get(mapCode);

				if (courses != null && courses.length > 0) {

					for (String course : courses) {

						StringBuilder builder = new StringBuilder("INSERT INTO course VALUES (");
						builder.append("0,'");
						builder.append(course + "',");// name
						builder.append(programId + ",");// program_id
						builder.append("null,");// code
						builder.append(getCourseCredit() + ",");// credit
						builder.append(i + ",'");// semester
						builder.append(level + "')");// level

						debug("COURSE: " + builder.toString());

						connection.createStatement().executeUpdate(builder.toString());
					}

				}

			}
		}

	}

	public static void insertStudent(int collegeId, int programId) throws SQLException {

		StringBuilder builder = null;

		ResultSet rsClass = null;

		for (int i = 0; i < INSERT_STUDENTS_COUNT; i++) {

			String classQuery = "SELECT * FROM class where college_id= " + collegeId + " order by RAND() limit 1";

			rsClass = connection.createStatement().executeQuery(classQuery);

			rsClass.next();

			int classId = rsClass.getInt("id");
			String level = rsClass.getString("level");
			String enrollDate = getEnrollDate(level);
			int semester = rsClass.getInt("semester");

			builder = new StringBuilder("INSERT INTO student VALUES (");

			String name = NameUtils.getName();
			String email = NameUtils.getEmail(name);
			String address = NameUtils.getAddress();

			builder.append("0,");// id
			builder.append(classId + ",'");// class_id
			builder.append(name + "','");// name
			builder.append((i + 1) + "','");// roll no
			builder.append(enrollDate + "','");// enroll date
			builder.append(address + "','");// address
			builder.append(getContactNumber() + "','");// contact number
			builder.append(email + "')");// emailaddress

			debug("STUDENT: " + builder.toString());

			connection.createStatement().executeUpdate(builder.toString());

			// String query = "SELECT * FROM class WHERE program_id=" +
			// programId + " AND semester=" + semester
			// + " AND section = '" + section + "'";

			// System.out.println(query);

			// ResultSet rs = connection.createStatement().executeQuery(query);
			//
			int studentId = getMaxId(dbName, "student");
			//
			// while (rs.next()) {
			// int classId = rs.getInt(1);

			// handleClassStudent.insertClassStudent(classId, studentId);
			// }

			insertStudentCourse(programId, semester, level, studentId);

		}
	}

	public static void insertStudentCourse(int programId, int semester, String level, int studentId)
			throws SQLException {

		// String query = "select c.id from student s, course c, program
		// p where s.program_id=p.id and c.program_id=p.id and
		// s.level=c.level";

//		String query = "select distinct(c.id) from course c, program p , class cl where cl.program_id=p.id"
//				+ " and c.program_id=p.id and cl.level=c.level and c.semester=cl.semester" + " and p.id=" + programId
//				+ " AND c.semester IN (" + getAllSemesters(semester) + ") AND c.level ='" + level + "' and p.code = '"+programCode+"'";
//		

		String query = "select * from course c where c.semester in ("+getAllSemesters(semester)+") and c.program_id= "+programId+" and c.level = '"+level+"';";
		
//		System.out.println(query);
		
		ResultSet rs = connection.createStatement().executeQuery(query);

		StringBuilder builder = null;

		while (rs.next()) {

			int courseId = rs.getInt(1);

			int marksTheory = getRandomNumber(0, 80);
			int marksPractical = getRandomNumber(0, 20);
			int marksTotal = marksTheory + marksPractical;

			String grade = getGrade(marksTotal);

			builder = new StringBuilder("INSERT INTO course_student VALUES (");
			// builder.append("0,");// id
			builder.append(studentId + ",");// studentid
			builder.append(courseId + ",'");// course_id
			builder.append(grade + "',");// grade
			builder.append(marksTheory + ",");// marks_theory
			builder.append(marksPractical + ",");// marks_practical
			builder.append(marksTotal + ",");// marks_total
			builder.append("null)");// remark
			
//			System.out.println("\t"+builder.toString());

			debug("CLASS_STUDENT: " + builder.toString());

			connection.createStatement().executeUpdate(builder.toString());
		}

	}

	public static void insertStudentAttendance(int programId) throws SQLException {

		StringBuilder builder = new StringBuilder();

		String studentQuery = "select s.id, r.id from student s, class c, routine r, program p where c.program_id = p.id and s.class_id=c.id and c.id=r.class_id and p.id="
				+ programId + " and c.program_id=" + programId;

		ResultSet studentRs = connection.createStatement().executeQuery(studentQuery);

		while (studentRs.next()) {

			builder = new StringBuilder("INSERT INTO student_attendance VALUES (");

			int studentId = studentRs.getInt(1);

			int routineId = studentRs.getInt(2);

			int randomValue = getRandomNumber(0, 1);

			boolean isPresent = false;

			if (randomValue == 1) {
				isPresent = true;
			}

			builder.append("0,");// id
			builder.append(routineId + ",");// routineid
			builder.append(studentId + ",");// student_id
			builder.append(isPresent + ",");// is_present
			builder.append("null)");// remark

			connection.createStatement().executeUpdate(builder.toString());

			debug("STUDENT_ATTENDANCE: " + builder.toString());

		}
	}

	public static void insertTeacher(int collegeId, int facultyId) throws SQLException {

		StringBuilder builder = null;

		for (int i = 0; i < INSERT_TEACHER_COUNT; i++) {

			builder = new StringBuilder("INSERT INTO teacher VALUES (");

			String name = NameUtils.getName().replace("'", "");
			String email = NameUtils.getEmail(name);

			builder.append("0,");// id
			builder.append(collegeId + ",'");// collegeid
			builder.append(name + "',");// name
			builder.append(facultyId + ",'");// faculty
			builder.append(getContactNumber() + "','");// contactnumber
			builder.append(email + "')");// emailaddress

			debug("TEACHER: " + builder.toString());

			connection.createStatement().executeUpdate(builder.toString());

		}

	}

	public static void insertRoutine(int facultyId, int programId, int classId) throws SQLException {

		String query = "SELECT code FROM class where id=" + classId;

		ResultSet rs = connection.createStatement().executeQuery(query);
		rs.next();

		String classCode = rs.getString(1);

		String queryTeacher = "SELECT id FROM teacher where faculty_id=" + facultyId + " ORDER BY RAND() LIMIT 1";

		ResultSet rsTeacher = connection.createStatement().executeQuery(queryTeacher);
		rsTeacher.next();
		int teacherId = rsTeacher.getInt(1);

		String queryCourse = "SELECT c.id FROM course c, program p,class l WHERE c.program_id=p.id AND l.program_id=p.id AND p.faculty_id = "
				+ facultyId + " AND p.id = " + programId + " and c.program_id = " + programId + " and l.program_id="
				+ programId + " ORDER BY RAND() LIMIT 1;";

		ResultSet rsCourse = connection.createStatement().executeQuery(queryCourse);

		int courseId = 0;
		while (rsCourse.next()) {
			courseId = rsCourse.getInt(1);
		}

		if (courseId == 0) {
			return;
		}

		StringBuilder builder = new StringBuilder("INSERT INTO routine VALUES (");
		builder.append("0,");// id
		builder.append(teacherId + ",");// teacher_id
		builder.append(courseId + ",");// course_id
		builder.append(classId + ",'");// class_id
		builder.append(classCode + "','");// class_room
		builder.append(getRoutineDate(null) + "','");// class_date

		int timeFrom = getRandomNumber(7, 15);

		String amPmFrom = " AM";

		if (timeFrom >= 12) {
			amPmFrom = " PM";
		}

		int timeTo = timeFrom + 1;

		String amPmTo = " AM";

		if (timeTo >= 12) {
			amPmTo = " PM";
		}

		builder.append(timeFrom + amPmFrom + "','");// time_from
		builder.append(timeTo + amPmTo + "')");// class_to

		debug("ROUTINE: " + builder.toString());

		connection.createStatement().executeUpdate(builder.toString());

	}

	public static String getGrade(int marks) {

		if (marks >= 90) {
			return "A";
		} else if (marks >= 85) {
			return "A-";
		} else if (marks >= 80) {
			return "B+";
		} else if (marks >= 75) {
			return "B";
		} else if (marks >= 70) {
			return "B-";
		} else if (marks >= 65) {
			return "C+";
		} else if (marks >= 60) {
			return "C";
		} else if (marks >= 55) {
			return "C-";
		} else if (marks >= 50) {
			return "D+";
		} else if (marks >= 45) {
			return "D";
		} else if (marks >= 40) {
			return "D-";
		} else {
			return "F";
		}
	}

	public static int getProgramId() {
		int[] programIds = { 1, 2, 3 };
		int randomNumber = getRandomNumber(0, programIds.length - 1);
		return programIds[randomNumber];
	}

	public static int getCourseCredit() {
		return getRandomNumber(2, 4);
	}

	public static String getClassName(String code, String level) {

		if (StringUtils.equalsIgnoreCase(level, BACHELOR)) {

			if (StringUtils.equalsIgnoreCase(code, "CmE")) {
				return "BECE";
			}
			if (StringUtils.equalsIgnoreCase(code, "CvE")) {
				return "BECvE";
			}

			if (StringUtils.equalsIgnoreCase(code, "BBA")) {
				return "BBA";
			}
		}

		if (StringUtils.equalsIgnoreCase(level, MASTER)) {

			if (StringUtils.equalsIgnoreCase(code, "CmE")) {
				return "MECE";
			}
			if (StringUtils.equalsIgnoreCase(code, "CvE")) {
				return "MECvE";
			}

			if (StringUtils.equalsIgnoreCase(code, "MBA")) {
				return "MBA";
			}
		}

		return "";

	}

	public static String[] getSections() {
		return new String[] { "A", "B", "C" };
	}

	public static String getSection() {
		String[] sections = getSections();
		int randomNumber = getRandomNumber(0, sections.length - 1);
		return sections[randomNumber];
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

	public static String getEnrollDate(String level) {
		String[] years = null;

		if (StringUtils.isNotEmpty(level) && StringUtils.equals(MASTER, level)) {
			years = getMasterYears();
		} else {
			years = getYears();
		}

		String[] months = getMonths();
		String[] days = getDays();

		String year = years[getRandomNumber(0, years.length - 1)];
		String month = months[getRandomNumber(0, months.length - 1)];
		String day = days[getRandomNumber(0, days.length - 1)];

		return year + "-" + month + "-" + day;
	}

	public static String getRoutineDate(String level) {
		return getEnrollDate(level);
	}

	public static String[] getMonths() {
		return new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12" };
	}

	public static String[] getDays() {
		return new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16",
				"17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28" };
	}

	public static String[] getYears() {
		return new String[] { "2013", "2014", "2015", "2016" };
	}

	public static String[] getMasterYears() {
		return new String[] { "2015", "2016" };
	}

	public static String[] getMasterYearsWithSemester() {
		return new String[] { "2015-1", "2015-2", "2016-3", "2016-4", };
	}

	public static String[] getBachelorYearsWithSemester() {
		return new String[] { "2013-1", "2013-2", "2014-3", "2014-4", "2015-5", "2015-6", "2016-7", "2016-8" };
	}

	public static String getLevel() {
		String[] levels = { BACHELOR, MASTER };

		int randomNumber = getRandomNumber(0, 1);
		return levels[randomNumber];
	}

	public static String getContactNumber() {

		long randomNumber = getRandomNumber(9800000000l, 9999999999l);

		return String.valueOf(randomNumber);
	}

	public static int getCollegeId() {
		int[] collegeIds = { 1, 2, 3 };
		int randomNumber = getRandomNumber(0, collegeIds.length - 1);
		return collegeIds[randomNumber];
	}

	public static int getFacultyId() {
		int[] facultyIds = { 1, 2, 3 };
		int randomNumber = getRandomNumber(0, facultyIds.length - 1);
		return facultyIds[randomNumber];
	}

	public static int getRandomNumber(int min, int max) {

		return ThreadLocalRandom.current().nextInt(min, max + 1);
	}

	public static long getRandomNumber(long min, long max) {

		return ThreadLocalRandom.current().nextLong(min, max + 1);
	}

	public static int getMaxId(String dbName, String tableName) throws SQLException {

		String query = "SELECT MAX(id) FROM " + tableName;

		ResultSet rs = connection.createStatement().executeQuery(query);
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

	public static String[] getPrograms(int facultyId, int index) {

		if (facultyId % 2 != 0) {
			return getSciencePrograms();
		}
		return getManagementPrograms();
	}

	public static String[] getSciencePrograms() {
		return new String[] { "CmE-Computer Engineering", "CvE-Civil Engineering", "EkE-Elecrical Engineering",
				"MCIS-Master of Computer Information System" };
	}

	public static String[] getManagementPrograms() {
		return new String[] { "BBA-Bachelor in Business Administration" };
	}

	public static void truncateTables() throws SQLException {

		List<String> tables = getAllTables();

		for (String table : tables) {

			System.out.println("Truncating " + table + " ...");

			connection.createStatement().execute("TRUNCATE " + table);
		}
	}

	public static List<String> getAllTables() {
		List<String> tables = new ArrayList<>();

		tables.add("student_attendance");
		tables.add("routine");
		tables.add("course");
		tables.add("student");
		tables.add("class");
		tables.add("course_student");
		tables.add("teacher");
		tables.add("student");
		tables.add("program");
		tables.add("faculty");
		tables.add("college");

		return tables;

	}

	public static String getAllSemesters(int currentSemester) {

		String sems = "";

		for (int i = currentSemester; i > 0; i--) {
			sems += i + ",";
		}

		if (sems.endsWith(",")) {
			sems = sems.substring(0, sems.length() - 1);
		}

		return sems;
	}

	public static void debug(String msg) {

		if (debug) {

			System.out.println(msg);
		}
	}

}
