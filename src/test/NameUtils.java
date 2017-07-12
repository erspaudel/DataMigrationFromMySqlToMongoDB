package test;

import java.util.concurrent.ThreadLocalRandom;

import org.fluttercode.datafactory.impl.DataFactory;

public class NameUtils {

//	private static DataFactory df = new DataFactory();

	private static final String[] FIRST_NAMES = { "Abigail", "Alexandra", "Alison", "Amanda", "Amelia", "Amy", "Andrea",
			"Angela", "Anna", "Anne", "Audrey", "Ava", "Bella", "Bernadette", "Carol", "Caroline", "Carolyn", "Chloe",
			"Claire", "Deirdre", "Diana", "Diane", "Donna", "Dorothy", "Elizabeth", "Ella", "Emily", "Emma", "Faith",
			"Felicity", "Fiona", "Gabrielle", "Grace", "Hannah", "Heather", "Irene", "Jan", "Jane", "Jasmine",
			"Jennifer", "Jessica", "Joan", "Joanne", "Julia", "Karen", "Katherine", "Kimberly", "Kylie", "Lauren",
			"Leah", "Lillian", "Lily", "Lisa", "Madeleine", "Maria", "Mary", "Megan", "Melanie", "Michelle", "Molly",
			"Natalie", "Nicola", "Olivia", "Penelope", "Pippa", "Rachel", "Rebecca", "Rose", "Ruth", "Sally",
			"Samantha", "Sarah", "Sonia", "Sophie", "Stephanie", "Sue", "Theresa", "Tracey", "Una", "Vanessa",
			"Victoria", "Virginia", "Wanda", "Wendy", "Yvonne", "Zoe", "Adam", "Adrian", "Alan", "Alexander", "Andrew",
			"Anthony", "Austin", "Benjamin", "Blake", "Boris", "Brandon", "Brian", "Cameron", "Carl", "Charles",
			"Christian", "Christopher", "Colin", "Connor", "Dan", "David", "Dominic", "Dylan", "Edward", "Eric", "Evan",
			"Frank", "Gavin", "Gordon", "Harry", "Ian", "Isaac", "Jack", "Jacob", "Jake", "James", "Jason", "Joe",
			"John", "Jonathan", "Joseph", "Joshua", "Julian", "Justin", "Keith", "Kevin", "Leonard", "Liam", "Lucas",
			"Luke", "Matt", "Max", "Michael", "Nathan", "Neil", "Nicholas", "Oliver", "Owen", "Paul", "Peter", "Phil",
			"Piers", "Richard", "Robert", "Ryan", "Sam", "Sean", "Sebastian", "Simon", "Stephen", "Steven", "Stewart",
			"Thomas", "Tim", "Trevor", "Victor", "Warren", "William" };

	private static final String[] LAST_NAMES = { "Abraham", "Allan", "Alsop", "Anderson", "Arnold", "Avery", "Bailey",
			"Baker", "Ball", "Bell", "Berry", "Black", "Blake", "Bond", "Bower", "Brown", "Buckland", "Burgess",
			"Butler", "Cameron", "Campbell", "Carr", "Chapman", "Churchill", "Clark", "Clarkson", "Coleman", "Cornish",
			"Davidson", "Davies", "Dickens", "Dowd", "Duncan", "Dyer", "Edmunds", "Ellison", "Ferguson", "Fisher",
			"Forsyth", "Fraser", "Gibson", "Gill", "Glover", "Graham", "Grant", "Gray", "Greene", "Hamilton",
			"Hardacre", "Harris", "Hart", "Hemmings", "Henderson", "Hill", "Hodges", "Howard", "Hudson", "Hughes",
			"Hunter", "Ince", "Jackson", "James", "Johnston", "Jones", "Kelly", "Kerr", "King", "Knox", "Lambert",
			"Langdon", "Lawrence", "Lee", "Lewis", "Lyman", "MacDonald", "Mackay", "Mackenzie", "MacLeod", "Manning",
			"Marshall", "Martin", "Mathis", "May", "McDonald", "McLean", "McGrath", "Metcalfe", "Miller", "Mills",
			"Mitchell", "Morgan", "Morrison", "Murray", "Nash", "Newman", "Nolan", "North", "Ogden", "Oliver", "Paige",
			"Parr", "Parsons", "Paterson", "Payne", "Peake", "Peters", "Piper", "Poole", "Powell", "Pullman", "Quinn",
			"Rampling", "Randall", "Rees", "Reid", "Roberts", "Robertson", "Ross", "Russell", "Rutherford", "Sanderson",
			"Scott", "Sharp", "Short", "Simpson", "Skinner", "Slater", "Smith", "Springer", "Stewart", "Sutherland",
			"Taylor", "Terry", "Thomson", "Tucker", "Turner", "Underwood", "Vance", "Vaughan", "Walker", "Wallace",
			"Walsh", "Watson", "Welch", "White", "Wilkins", "Wilson", "Wright", "Young" };

	@SuppressWarnings("unused")
	private static String getName2() {
		int firstNameRandom = getRandomNumber(0, FIRST_NAMES.length);
		int lastNameRandom = getRandomNumber(0, LAST_NAMES.length);

		return FIRST_NAMES[firstNameRandom] + " " + LAST_NAMES[lastNameRandom];
	}

	public static String getName() {
		DataFactory df = new DataFactory();
		String name = df.getFirstName() + " " + df.getLastName();
		return name.replace("'", "");
	}

	public static String getEmail(String name) {
		return name.replace(" ", "") + "@gmail.com";
	}

	public static String getAddress() {
		DataFactory df = new DataFactory();
		return df.getAddress() + "," + df.getCity() + "," + df.getNumberText(5);
	}

	public static int getRandomNumber(int min, int max) {

		return ThreadLocalRandom.current().nextInt(min, max + 1);
	}

	public static void main(String[] args) {
//		System.out.println(df.getEmailAddress());
	}

}
