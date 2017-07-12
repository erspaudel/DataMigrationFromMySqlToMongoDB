package test;

import java.util.HashMap;
import java.util.Map;

public class CourseUtils {

	/*
	 * static final String[] CmE_MASTER_1 = { "Discrete Structure",
	 * "Advanced Problem Solving Technique",
	 * "Object Oriented Software Engineering", "Algorithmic Mathematics",
	 * "Digital System Design" };
	 * 
	 * static final String[] CmE_MASTER_2 = { "Theory of Computation",
	 * "Advanced Computer Architecture", "Distributed Operating System",
	 * "Computer Graphics", "Mobile and Wireless Communication" };
	 * 
	 * static final String[] CmE_MASTER_3 = { "Distributed Database",
	 * "Image Processing and Pattern Recognition", "Artificial Intelligence",
	 * "Network Security", "Elective-I" };
	 * 
	 * static final String[] CmE_MASTER_4 = { "Multimedia Computing",
	 * "Elective-II", "Thesis" };
	 * 
	 * static final String[] CmE_BACHELOR_1 = { "Chemistry",
	 * "Communication Technique", "Programming in C",
	 * "Basic Electrical Engineering", "Mechanical Workshop",
	 * "Engineering Mathematics I" };
	 * 
	 * static final String[] CmE_BACHELOR_2 = { "Engineering Mathematics II",
	 * "Physics", "Engineering Drawing", "Object Oriented Programming in C++",
	 * "Thermal Science", "Applied Mechanics" };
	 * 
	 * static final String[] CmE_BACHELOR_3 = { "Engineering Mathematics III",
	 * "Data Structure and Algorithm", "Electrical Engineering Materials",
	 * "Network Theory", "Electronic Devices & Circuits", "Logic Circuits" };
	 * 
	 * static final String[] CmE_BACHELOR_4 = { "Engineering Mathematics IV",
	 * "Instrumentation", "Database Management System",
	 * "Advanced Programming Technology", "Microprocessor", "Project I" };
	 * 
	 * static final String[] CmE_BACHELOR_5 = { "Engineering Mathematics V",
	 * "Numerical Methods", "Probability and Statistics", "Operating System",
	 * "Computer Graphics", "Computer Architecture", "Theory of Computation"
	 * ,"Elective" };
	 * 
	 * static final String[] CmE_BACHELOR_6 = { "Engineering Mathematics VI",
	 * "Embedded System", "Simulation and Modeling", "Data Communication",
	 * "Object Oriented Software Engineering", "Minor Project", "Elective I" };
	 * 
	 * static final String[] CmE_BACHELOR_7 = { "Engineering Economics",
	 * "Image Processing & Pattern Recognition", "Artifcial Intelligence",
	 * "Computer Network", "ICT Project Management", "Elective II" };
	 * 
	 * static final String[] CmE_BACHELOR_8 = { "Organization and Management",
	 * "Digital Signal Analysis & Processing",
	 * "Social and Professional Issues in IT", "Information Systems",
	 * "Final Project", "Elective III" };
	 * 
	 * static final String[] CvE_BACHELOR_1 = { "Chemistry",
	 * "Communication Technique", "Programming in C",
	 * "Basic Electrical Engineering", "Mechanical Workshop",
	 * "Engineering Mathematics I" };
	 * 
	 * static final String[] CvE_BACHELOR_2 = { "Engineering Mathematics II",
	 * "Physics", "Engineering Drawing", "Object Oriented Programming in C++",
	 * "Thermal Science", "Applied Mechanics I" };
	 * 
	 * static final String[] CvE_BACHELOR_3 = { "Engineering Mathematics III",
	 * "Civil Engineering Materials", "Engineering Geology", "Fluid Mechanics",
	 * "Strength Of Materials", "Applied Mechanics II" };
	 * 
	 * static final String[] CvE_BACHELOR_4 = { "Probability and Statistics",
	 * "Surveying", "Building Technology", "Numeric Methods", "Hydraulics",
	 * "Structural Analysis I" };
	 * 
	 * static final String[] CvE_BACHELOR_5 = { "Basic Electronics Engineering",
	 * "Engineering Hydrology", "Design of Steel and Timber Structure",
	 * "Foundation Engineering", "Transportation Engineering I",
	 * "Concrete Technology and Machinery Structure" };
	 * 
	 * static final String[] CvE_BACHELOR_6 = { "Survey Field Project",
	 * "Water Supply and Sanitary Engineering", "Soil Mechanics",
	 * "Structural Analysis II", "Irrigation Engineering", "Elective I",
	 * "Project II" };
	 * 
	 * static final String[] CvE_BACHELOR_7 = { "Design of RCC Structure",
	 * "Transportation Engineering II", "Hydropower Engineering",
	 * "Estimation, Valuation and Specification", "Elective III",
	 * "Major Project" };
	 * 
	 * static final String[] CvE_BACHELOR_8 = {
	 * "Construction Project Management ", "Engineering Professional Practice ",
	 * "Remote Sensing and GIS", "Elective II", "Engineering Economics" };
	 * 
	 * static final String[] BBA_BACHELOR_1 = { "English I",
	 * "Business Mathematics I", "Financial Accounting I",
	 * "Principles of Management", "Computer Application" };
	 * 
	 * static final String[] BBA_BACHELOR_2 = { "English II",
	 * "Business Mathematics II", "Financial Accounting II", "Microeconomics",
	 * "Programming Language" };
	 * 
	 * static final String[] BBA_BACHELOR_3 = { "Business Communication",
	 * "Business Statistics", "Sociology ACC", "Management Accounting",
	 * "Macroeconomics" };
	 * 
	 * static final String[] BBA_BACHELOR_4 = { "Fundamentals of Logic",
	 * "Data Analysis and Modeling", "Psychology", "Finance I ",
	 * "Research Methodology", "Summer Project" };
	 * 
	 * static final String[] BBA_BACHELOR_5 = { "Organization Relations",
	 * "Principles of Marketing", "Management Information System", "Finance II",
	 * "Nepalese Business Environmenty" };
	 * 
	 * static final String[] BBA_BACHELOR_6 = { "Human Resource Management",
	 * "Entrepreneurship", "International Business", "Operations Management",
	 * "Business Law", "Internship" };
	 * 
	 * static final String[] BBA_BACHELOR_7 = { "Strategic Management I",
	 * "Specialization I – 1st Paper", "Specialization I – 2nd Paper",
	 * "Specialization II – 1st Paper", "Specialization II – 2nd Paper" };
	 * 
	 * static final String[] BBA_BACHELOR_8 = { "Strategic Management II",
	 * "Specialization III – 1st Paper", "Specialization III – 2nd Paper",
	 * "Specialization IV – 1st Paper", "Specialization IV – 2nd Paper" };
	 * 
	 * static final String[] MCIS_MASTER_1 = { "Discrete Structure",
	 * "Ethical and Professional Issues in IT", "Software Project Management",
	 * "Distributed Database", "Elective-I" };
	 * 
	 * static final String[] MCIS_MASTER_2 = {
	 * "Organization Behavior and Human Resource management",
	 * "Operation Research", "Visual Programming", "Elective-II", "Project Work"
	 * };
	 * 
	 * static final String[] MCIS_MASTER_3 = { "Image Processing",
	 * "Artificial Intelligence", "Real Time System", "Directed Studies",
	 * "Elective-III" };
	 * 
	 * static final String[] MCIS_MASTER_4 = {
	 * "Data Mining and Data Warehousing", "Seminar & Presentations", "Thesis"
	 * };
	 */

	static final String[] CmE_MASTER_1 = { "Discrete Structure", "Object Oriented Software Engineering",
			"Algorithmic Mathematics", "Digital System Design" };

	static final String[] CmE_MASTER_2 = { "Theory of Computation", "Advanced Computer Architecture",
			"Distributed Operating System", "Computer Graphics", };

	static final String[] CmE_MASTER_3 = { "Distributed Database", "Image Processing and Pattern Recognition",
			"Artificial Intelligence", "Network Security", "Elective-I" };

	static final String[] CmE_MASTER_4 = { "Multimedia Computing", "Elective-II", "Thesis" };

	static final String[] CmE_BACHELOR_1 = { "Chemistry", "Communication Technique", "Programming in C",
			"Basic Electrical Engineering", "Mechanical Workshop", "Engineering Mathematics I" };

	static final String[] CmE_BACHELOR_2 = { "Engineering Mathematics II", "Physics", "Engineering Drawing",
			"Object Oriented Programming in C++", "Thermal Science", "Applied Mechanics" };

	static final String[] CmE_BACHELOR_3 = { "Engineering Mathematics III", "Data Structure and Algorithm",
			"Electrical Engineering Materials", "Network Theory", "Electronic Devices & Circuits", "Logic Circuits" };

	static final String[] CmE_BACHELOR_4 = { "Engineering Mathematics IV", "Instrumentation",
			"Database Management System", "Advanced Programming Technology", "Microprocessor", "Project I" };

	static final String[] CmE_BACHELOR_5 = { "Engineering Mathematics V", "Numerical Methods",
			"Probability and Statistics", "Operating System", "Computer Graphics", "Computer Architecture",
			"Theory of Computation", "Elective" };

	static final String[] CmE_BACHELOR_6 = { "Engineering Mathematics VI", "Embedded System", "Simulation and Modeling",
			"Data Communication", "Object Oriented Software Engineering", "Minor Project", "Elective I" };

	static final String[] CmE_BACHELOR_7 = { "Engineering Economics", "Image Processing & Pattern Recognition",
			"Artifcial Intelligence", "Computer Network", "ICT Project Management", "Elective II" };

	static final String[] CmE_BACHELOR_8 = { "Organization and Management", "Digital Signal Analysis & Processing",
			"Social and Professional Issues in IT", "Information Systems", "Final Project", "Elective III" };

	static final String[] CvE_BACHELOR_1 = { "Chemistry", "Communication Technique", "Programming in C",
			"Basic Electrical Engineering", "Engineering Mathematics I" };

	static final String[] CvE_BACHELOR_2 = { "Engineering Mathematics II", "Physics", "Engineering Drawing",
			"Object Oriented Programming in C++", "Thermal Science", "Applied Mechanics I" };

	static final String[] CvE_BACHELOR_3 = { "Engineering Mathematics III", "Civil Engineering Materials",
			"Engineering Geology", "Strength Of Materials", "Applied Mechanics II" };

	static final String[] CvE_BACHELOR_4 = { "Probability and Statistics", "Surveying", "Numeric Methods", "Hydraulics",
			"Structural Analysis I" };

	static final String[] CvE_BACHELOR_5 = { "Basic Electronics Engineering", "Engineering Hydrology",
			"Design of Steel and Timber Structure", "Foundation Engineering",
			"Concrete Technology and Machinery Structure" };

	static final String[] CvE_BACHELOR_6 = { "Survey Field Project", "Soil Mechanics", "Structural Analysis II",
			"Irrigation Engineering", "Elective I", "Project II" };

	static final String[] CvE_BACHELOR_7 = { "Transportation Engineering II", "Hydropower Engineering", "Elective III",
			"Major Project" };

	static final String[] CvE_BACHELOR_8 = { "Construction Project Management ", "Engineering Professional Practice ",
			"Remote Sensing and GIS", "Elective II", "Engineering Economics" };

	static final String[] BBA_BACHELOR_1 = { "English I", "Business Mathematics I", "Financial Accounting I",
			"Principles of Management", "Computer Application" };

	static final String[] BBA_BACHELOR_2 = { "English II", "Business Mathematics II", "Financial Accounting II",
			"Microeconomics", "Programming Language" };

	static final String[] BBA_BACHELOR_3 = { "Business Communication", "Sociology ACC", "Management Accounting",
			"Macroeconomics" };

	static final String[] BBA_BACHELOR_4 = { "Fundamentals of Logic", "Psychology", "Finance I ",
			"Research Methodology", "Summer Project" };

	static final String[] BBA_BACHELOR_5 = { "Organization Relations", "Management Information System", "Finance II",
			"Nepalese Business Environmenty" };

	static final String[] BBA_BACHELOR_6 = { "Human Resource Management", "International Business",
			"Operations Management", "Business Law", "Internship" };

	static final String[] BBA_BACHELOR_7 = { "Strategic Management I", "Specialization I – 1st Paper",
			"Specialization I – 2nd Paper", "Specialization II – 1st Paper", "Specialization II – 2nd Paper" };

	static final String[] BBA_BACHELOR_8 = { "Strategic Management II", "Specialization III – 1st Paper",
			"Specialization III – 2nd Paper", "Specialization IV – 1st Paper", "Specialization IV – 2nd Paper" };

	static final String[] MCIS_MASTER_1 = { "Discrete Structure", "Ethical and Professional Issues in IT",
			"Software Project Management", "Distributed Database", "Elective-I" };

	static final String[] MCIS_MASTER_2 = { "Organization Behavior and Human Resource management", "Operation Research",
			"Visual Programming", "Elective-II", "Project Work" };

	static final String[] MCIS_MASTER_3 = { "Image Processing", "Artificial Intelligence", "Real Time System",
			"Directed Studies", "Elective-III" };

	static final String[] MCIS_MASTER_4 = { "Data Mining and Data Warehousing", "Seminar & Presentations", "Thesis" };

	static Map<String, String[]> map = null;

	static String CmE_BACHELOR = "CmE_BACHELOR";
	static String CmE_MASTER = "CmE_MASTER";

	static {
		map = new HashMap<>();
		map.put("CmE_MASTER_1", CmE_MASTER_1);
		map.put("CmE_MASTER_2", CmE_MASTER_2);
		map.put("CmE_MASTER_3", CmE_MASTER_3);
		map.put("CmE_MASTER_4", CmE_MASTER_4);

		map.put("CmE_BACHELOR_1", CmE_BACHELOR_1);
		map.put("CmE_BACHELOR_2", CmE_BACHELOR_2);
		map.put("CmE_BACHELOR_3", CmE_BACHELOR_3);
		map.put("CmE_BACHELOR_4", CmE_BACHELOR_4);
		map.put("CmE_BACHELOR_5", CmE_BACHELOR_5);
		map.put("CmE_BACHELOR_6", CmE_BACHELOR_6);
		map.put("CmE_BACHELOR_7", CmE_BACHELOR_7);
		map.put("CmE_BACHELOR_8", CmE_BACHELOR_8);

		map.put("CvE_BACHELOR_1", CvE_BACHELOR_1);
		map.put("CvE_BACHELOR_2", CvE_BACHELOR_2);
		map.put("CvE_BACHELOR_3", CvE_BACHELOR_3);
		map.put("CvE_BACHELOR_4", CvE_BACHELOR_4);
		map.put("CvE_BACHELOR_5", CvE_BACHELOR_5);
		map.put("CvE_BACHELOR_6", CvE_BACHELOR_6);
		map.put("CvE_BACHELOR_7", CvE_BACHELOR_7);
		map.put("CvE_BACHELOR_8", CvE_BACHELOR_8);

		map.put("BBA_BACHELOR_1", BBA_BACHELOR_1);
		map.put("BBA_BACHELOR_2", BBA_BACHELOR_2);
		map.put("BBA_BACHELOR_3", BBA_BACHELOR_3);
		map.put("BBA_BACHELOR_4", BBA_BACHELOR_4);
		map.put("BBA_BACHELOR_5", BBA_BACHELOR_5);
		map.put("BBA_BACHELOR_6", BBA_BACHELOR_6);
		map.put("BBA_BACHELOR_7", BBA_BACHELOR_7);
		map.put("BBA_BACHELOR_8", BBA_BACHELOR_8);

		map.put("MCIS_BACHELOR_1", MCIS_MASTER_1);
		map.put("MCIS_BACHELOR_2", MCIS_MASTER_2);
		map.put("MCIS_BACHELOR_3", MCIS_MASTER_3);
		map.put("MCIS_BACHELOR_4", MCIS_MASTER_4);

	}

	public static Map<String, String[]> getCourseMap() {
		return map;
	}

}
