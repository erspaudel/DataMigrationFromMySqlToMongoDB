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
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import com.utils.StringUtils;

public class TestDB2 {

	static final String dbName = "college";
	static final String dbUrl = "jdbc:mysql://localhost:3306/" + dbName;

	// Database credentials
	static final String username = "root";
	static final String password = "";

	static Connection connection = null;
	static Statement statement = null;

	static final String BACHELOR = "BACHELOR";
	static final String MASTER = "MASTER";

	static final int INSERT_STUDENTS_COUNT = 1000;
	static final int INSERT_TEACHER_COUNT = 100;
	static final int INSERT_ROUTINE_COUNT = 100;

	public static void main(String args[]) {

		try {

			Class.forName("com.mysql.jdbc.Driver");

			System.out.println("Connecting to database...");
			connection = DriverManager.getConnection(dbUrl, username, password);
			System.out.println("Connection to database successful...\n");

			statement = connection.createStatement();

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

				insertProgram(facultyId, facultyIndex);

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

				String queryProgram = "select id from program where faculty_id=" + facultyId;

				ResultSet rsProgram = connection.createStatement().executeQuery(queryProgram);

				while (rsProgram.next()) {

					int programId = rsProgram.getInt(1);

					insertStudent(programId);
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

		System.out.println("COLLEGE 1: " + builder.toString());

		statement.executeUpdate(builder.toString());

		builder = new StringBuilder("INSERT INTO college VALUES (");
		builder.append("0,");
		builder.append("'Nepal Engineering College',");// name
		builder.append("'NEC',");// code
		builder.append("'Changunarayan, Bhaktapur, Nepal',");// address
		builder.append("'016666054',");// contact
		builder.append("'016666054',");// fax
		builder.append("'info@nec.edu.np',");// email
		builder.append("'www.nec.edu.np')");// url

		System.out.println("COLLEGE 2: " + builder.toString());

		statement.executeUpdate(builder.toString());

	}

	public static void insertFaculty(int collegeId) throws SQLException {

		StringBuilder builder = new StringBuilder("INSERT INTO faculty VALUES (");
		builder.append("0,");
		builder.append("'Science and Technology',");// name
		builder.append(collegeId + ")");// collegeid

		System.out.println("FACULTY 1: " + builder.toString());

		statement.executeUpdate(builder.toString());

		builder = new StringBuilder("INSERT INTO faculty VALUES (");
		builder.append("0,");
		builder.append("'Management',");// name
		builder.append(collegeId + ")");// collegeid

		System.out.println("FACULTY 2: " + builder.toString());

		statement.executeUpdate(builder.toString());

	}

	public static void insertProgram(int facultyId, int facultyIndex) throws SQLException {

		StringBuilder builder = new StringBuilder("INSERT INTO program VALUES (");

		String[] programs = getProgram(facultyIndex).split("-");

		String code = programs[0];
		String name = programs[1];

		builder.append("0,'");
		builder.append(name + "',");// name
		builder.append(facultyId + ",'");// faculty_id
		builder.append(code + "')");// code

		System.out.println("PROGRAM : " + builder.toString());

		statement.executeUpdate(builder.toString());

		int programId = getMaxId(dbName, "program");

		insertCourse(programId);
	}

	public static void insertClass(int collegeId, int facultyId, int programId) throws SQLException {

		StringBuilder builder = null;

		String[] years = getYearsWithSemester();
		String[] sections = getSections();

		for (String yearSem : years) {

			String[] value = yearSem.split("-");
			String year = value[0];
			String sem = value[1];

			String name = getClassName(programId);

			for (String section : sections) {

				builder = new StringBuilder("INSERT INTO class VALUES (");

				builder.append("0,");// id
				builder.append(collegeId + ",");// collegeid
				builder.append(programId + ",'");// programid
				builder.append(section + "','");// section
				builder.append(name + "','");// name
				builder.append(year + "','");// batch
				builder.append(year + "-" + name + "-" + section + "',");// code
				builder.append(sem + ")");// code

				System.out.println("CLASS: " + builder.toString());

				statement.executeUpdate(builder.toString());

				int maxClassId = getMaxId(dbName, "class");

				for (int i = 0; i < INSERT_ROUTINE_COUNT; i++) {

					insertRoutine(facultyId, programId, maxClassId);
				}

			}
		}

	}

	public static void insertCourse(int programId) throws SQLException {

		Map<String, String[]> courseMap = CourseUtils.getCourseMap();

		String query = "select code from program where id=" + programId;

		ResultSet rs = connection.createStatement().executeQuery(query);

		System.out.println(query);

		rs.next();

		String[] levels = { "BACHELOR", "MASTER" };

		String code = rs.getString(1);

		String[] courses = null;

		for (String level : levels) {

			// loop for 8 sems
			for (int i = 1; i <= 8; i++) {

				// get bachelors courses first

				String mapCode = code + "_" + level + "_" + i;

				System.out.println(mapCode);

				courses = courseMap.get(mapCode);

				if (courses != null && courses.length > 0) {

					System.out.println("not empty");

					for (String course : courses) {

						StringBuilder builder = new StringBuilder("INSERT INTO course VALUES (");
						builder.append("0,'");
						builder.append(course + "',");// name
						builder.append(programId + ",");// program_id
						builder.append("null,");// code
						builder.append(getCourseCredit() + ",");// credit
						builder.append(i + ",'");// semester
						builder.append(level + "')");// level

						System.out.println("COURSE:" + builder.toString());

						connection.createStatement().executeUpdate(builder.toString());
					}

				}

			}
		}

	}

	public static void insertStudent(int programId) throws SQLException {

		// Here inner class is because we don't want the method-
		// insertClassStudent to be used by any other method

		class HandleStudent {
			public void insertClassStudent(int classId, int studentId) throws SQLException {

				StringBuilder builder = new StringBuilder("INSERT INTO class_student VALUES (");
				builder.append(classId + ",");// classid
				builder.append(studentId + ")");// studentid

				System.out.println("CLASS_STUDENT: " + builder.toString());

				connection.createStatement().executeUpdate(builder.toString());

			}

			public void insertStudentGrade(int programId, int semester, String level, int studentId)
					throws SQLException {

				// String query = "select c.id from student s, course c, program
				// p where s.program_id=p.id and c.program_id=p.id and
				// s.level=c.level";

				String query = "select distinct(c.id) from student s, course c, program p where s.program_id=p.id and c.program_id=p.id and s.level=c.level and c.semester=s.semester AND c.program_id="
						+ programId + " AND c.semester=" + semester + " AND c.level ='" + level + "'";

				ResultSet rs = connection.createStatement().executeQuery(query);

				while (rs.next()) {

					int courseId = rs.getInt(1);

					int marksTheory = getRandomNumber(0, 80);
					int marksPractical = getRandomNumber(0, 20);
					int marksTotal = marksTheory + marksPractical;

					String grade = getGrade(marksTotal);

					StringBuilder builder = new StringBuilder("INSERT INTO student_grade VALUES (");
					builder.append("0,");// id
					builder.append(studentId + ",");// studentid
					builder.append(courseId + ",'");// course_id
					builder.append(grade + "',");// grade
					builder.append(marksTheory + ",");// marks_theory
					builder.append(marksPractical + ",");// marks_practical
					builder.append(marksTotal + ",");// marks_total
					builder.append("null)");// remark

					System.out.println("CLASS_STUDENT: " + builder.toString());

					connection.createStatement().executeUpdate(builder.toString());
				}

			}
		}

		StringBuilder builder = null;

		HandleStudent handleClassStudent = new HandleStudent();

		for (int i = 0; i < INSERT_STUDENTS_COUNT; i++) {
			builder = new StringBuilder("INSERT INTO student VALUES (");

			String name = NameUtils.getName();
			String email = NameUtils.getEmail(name);
			String address = NameUtils.getAddress();

			String level = getLevel();
			String enrollDate = getEnrollDate(level);
			int semester = getSemester(enrollDate);
			String section = getSection();

			builder.append("0,");// id
			builder.append("1,");// college_id
			builder.append(programId + ",'");// program_id
			builder.append(name + "','");// name
			builder.append((i + 1) + "','");// roll no
			builder.append(level + "','");// level
			builder.append(enrollDate + "','");// enroll date
			builder.append(address + "','");// address
			builder.append(getContactNumber() + "','");// contact number
			builder.append(email + "',");// emailaddress
			builder.append(semester + ",'");// semester
			builder.append(section + "')");// section

			System.out.println("STUDENT " + i + ": " + builder.toString());

			statement.executeUpdate(builder.toString());

			String query = "SELECT * FROM class WHERE program_id=" + programId + " AND semester=" + semester
					+ " AND section = '" + section + "'";

			System.out.println(query);

			ResultSet rs = connection.createStatement().executeQuery(query);

			int studentId = getMaxId(dbName, "student");

			while (rs.next()) {
				int classId = rs.getInt(1);

				handleClassStudent.insertClassStudent(classId, studentId);
			}

			handleClassStudent.insertStudentGrade(programId, semester, level, studentId);

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

			System.out.println("TEACHER: " + builder.toString());

			statement.executeUpdate(builder.toString());

		}

	}

	public static void insertRoutine(int facultyId, int programId, int classId) throws SQLException {

		String query = "SELECT code FROM class where id=" + classId;

		ResultSet rs = connection.createStatement().executeQuery(query);
		rs.next();

		String classCode = rs.getString(1);

		String queryTeacher = "SELECT id FROM teacher where faculty_id=" + facultyId + " ORDER BY RAND() LIMIT 1";

		System.err.println(queryTeacher);

		ResultSet rsTeacher = connection.createStatement().executeQuery(queryTeacher);
		rsTeacher.next();
		int teacherId = rsTeacher.getInt(1);

		System.out.println(teacherId);

		String queryCourse = "SELECT c.id FROM course c, program p,class l WHERE c.program_id=p.id AND l.program_id=p.id AND p.faculty_id = "
				+ facultyId + " AND p.id = " + programId + " ORDER BY RAND() LIMIT 1;";

		ResultSet rsCourse = connection.createStatement().executeQuery(queryCourse);

		int courseId = 0;
		while (rsCourse.next()) {
			courseId = rsCourse.getInt(1);
		}

		if (courseId == 0) {
			return;
		}

		System.out.println("couseID: " + courseId);

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

		System.out.println("ROUTINE: " + builder.toString());

		statement.executeUpdate(builder.toString());

		int maxRoutineId = getMaxId(dbName, "routine");

		// String studentQuery = "select s.id from student s, class_student cs,
		// class c, routine r, program p where s.program_id = p.id and
		// cs.student_id=s.id and cs.class_id=c.id and c.program_id=p.id and
		// c.id=r.class_id";

		String studentQuery = "select s.id from student s, class_student cs, class c, routine r, program p where s.program_id = p.id and cs.student_id=s.id and cs.class_id=c.id and c.program_id=p.id and c.id=r.class_id and c.semester=s.semester";

		ResultSet studentRs = connection.createStatement().executeQuery(studentQuery);

		while (studentRs.next()) {

			builder = new StringBuilder("INSERT INTO student_attendance VALUES (");

			int studentId = studentRs.getInt(1);

			int randomValue = getRandomNumber(0, 1);

			boolean isPresent = false;

			if (randomValue == 1) {
				isPresent = true;
			}

			builder.append("0,");// id
			builder.append(maxRoutineId + ",");// routineid
			builder.append(studentId + ",");// student_id
			builder.append(isPresent + ",");// is_present
			builder.append("null)");// remark

			connection.createStatement().executeUpdate(builder.toString());

		}

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

	public static String getClassName(int programId) {
		if (programId == 1) {
			return "MECE";
		} else if (programId == 2) {
			return "MECvE";
		} else {
			return "MEEkE";
		}
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

		int randomNumber = TestDB2.getRandomNumber(1, 2);

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

	public static String[] getYearsWithSemester() {
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

	public static String getProgram(int index) {

		String[] programs = { "CmE-Computer Engineering", "CvE-Civil Engineering", "EkE-Elecrical Engineering" };
		return programs[index];
	}

	public static void truncateTables() throws SQLException {

		List<String> tables = getAllTables();

		for (String table : tables) {

			System.out.println("Truncating " + table + " ...");

			statement.execute("TRUNCATE " + table);
		}
	}

	public static List<String> getAllTables() {
		List<String> tables = new ArrayList<>();

		tables.add("student_attendance");
		tables.add("routine");
		tables.add("course");
		tables.add("class_student");
		tables.add("class");
		tables.add("student_grade");
		tables.add("teacher");
		tables.add("student");
		tables.add("program");
		tables.add("faculty");
		tables.add("college");

		return tables;

	}

}
