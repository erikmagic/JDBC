package Project3;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.time.LocalDate;
import java.util.Date;
import java.util.Scanner;
import java.io.*;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.ChartFactory; 
import org.jfree.chart.JFreeChart; 
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class Bulk {
	
	// global variables 
	static private Scanner sc;
	
	static private String url = "jdbc:db2://comp421.cs.mcgill.ca:50000/cs421";
	static private Connection con;
	static private String user = "cs421g44";
	// put valid password before running
	static private String pwd = "Quebec.libre1!";	


	public static void main(String[] args) {
		
		sc = new Scanner(System.in);
		
		// register the driver
		try {
			DriverManager.registerDriver(new com.ibm.db2.jcc.DB2Driver());
		} catch (Exception cnfe) {
			System.out.println("Class not found");
		}
		
		
		// call default menu
		int choice = 1;
		do {
			choice = askOption();
			//System.out.println("** " + choice);
			if (choice == 1) {
				addAClient();
			}
			else if (choice == 2) {
				createNewBooking();
			}
			else if (choice == 3) {
				checkCheapFlights();
			}
			else if (choice ==4 ) {
				checkEverythingAtLocation();
			}
			else if (choice == 5) {
				getClientEmail();
			}
			else if (choice == 6) {
				visualise();
			}
			
			if (choice != 7 ) {
				System.out.println("Continue? [Press anything]");
				sc.nextLine();				
			}

			
		} while (choice != 7);
		
		
		System.out.println("Exiting...");
		
	}
	


	static void getClientEmail(){
		
		System.out.println("Enter a client's name");
		
		String name = sc.nextLine();
		
		String query = "select email from customer where name = '" + name + "'";
		
		
		// establish connection
		
		ResultSet rs = null;
		try {
			con = DriverManager.getConnection(url, user, pwd);
			Statement stat = con.createStatement();
			
			rs = stat.executeQuery(query);
			
			int count = 0;
			
			while (rs.next()) {
				String email = rs.getString(1);
				System.out.println("email:" + email);
				count++;
			}
			
			if (count == 0) {
				System.out.println("No client with the name " + name);
			}
			// close
			stat.close();
			con.close();
			
			
		} catch (SQLException e) {
			System.out.println("Error connecting to db");
			System.out.println(e.getMessage());
		}	
		
		return;	
		
	}
	
	static void addAClient(){
		
		System.out.println("Adding a client... Please enter client's name.");
		String name = sc.nextLine();
		System.out.println("Client's phone number?");
		String phone = sc.nextLine();
		boolean error = false;
		
		java.sql.Date dateFormatted = null;
		do {
			System.out.println("Client's date of birth? [yyyy-mm-dd]");
			String dateNotFormatted = sc.nextLine();
			try {
				dateFormatted = java.sql.Date.valueOf(dateNotFormatted);
				error = false;

			} catch (Exception e) {
				error = true;
				System.out.println("Invalid date, try again [yyyy-mm-dd]");
			}			
		} while (error);

		System.out.println("Client's email?");
		String email = sc.nextLine();
		System.out.println("Client's gender? [male/female]");
		String sex = sc.nextLine();
		System.out.println("Client's preferences?");
		String preferences = sc.nextLine();
		System.out.println("Client's favored way of communication? [email/phone]");
		String prefCom = sc.nextLine();
	
		
		// establish connection
		try {
			con = DriverManager.getConnection(url, user, pwd);
			Statement stat = con.createStatement();
			
			String query = "insert into customer values ("
					+"'" + name +  "',"
					+"'" + phone +  "',"
					+"'" + email +  "',"
					+"'" + dateFormatted +  "',"
					+"'" + sex +  "',"
					+"'" + preferences +  "',"
					+"'" + prefCom +  "')";
					
			
			// get current count of customer
			String getNumb = "select count(*) from customer";
			
			ResultSet initRs = stat.executeQuery(getNumb);
			initRs.next();
			int initCount = initRs.getInt(1);
			
			stat.executeUpdate(query);
			
			ResultSet endRs = stat.executeQuery(getNumb);
			endRs.next();
			int endCount = endRs.getInt(1);
			
			if (initCount == (endCount-1))
			{
				System.out.println("You've added customer "+ name );
			} else {
				System.out.println("There was an error adding customer " + name);
				System.out.println("Probably already existing email or fields too long");
			}
			
			
			// close
			stat.close();
			con.close();
			
			
		} catch (SQLException e) {
			System.out.println("Error connecting to db");
			System.out.println(e.getMessage());
		}	
		
		return;	
		
	}
	
	static void createNewBooking(){
		
		System.out.println("Creating a new booking... Please enter your agent ID.");
		String agentid = sc.nextLine();
		
		//today's date
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		String date = dateFormat.format(cal.getTime());
		
		// cost of the booking
		System.out.println("Price of the booking?");
		boolean error = false;
		double price = 0.0;
		do {
			String priceString = sc.nextLine();
			error = false;
			try {
				price = Double.parseDouble(priceString);
				
				if (price < 0.0) {
					error = true;
					System.out.println("Invalid price, retry.");
				}
			} catch (Exception e) {
				error = true;
				System.out.println("Invalid price, retry.");
			}
		} while (error);
		
		
		
		// establish connection
		try {
			con = DriverManager.getConnection(url, user, pwd);
			Statement stat = con.createStatement();
			
			
			// check if the agent id exists
			String queryAgentID = "select count(*) from travelagent where agentid = '"
					+ agentid + "'";
			
			ResultSet agentRs = stat.executeQuery(queryAgentID);
			agentRs.next();
			int agentCount = agentRs.getInt(1);
			
			if (agentCount == 0) {
				System.out.println("Invalid agentID !, Retry.");
				
				stat.close();
				con.close();
				return;
				
			}
			
			
			String query = "insert into booking (price, date, agentid) values ("
					+"'" + price +  "',"
					+"'" + date  +  "',"
					+"'" + agentid + "')";

					
			
			// get current count of customer
			String getNumb = "select count(*) from booking";
			
			ResultSet initRs = stat.executeQuery(getNumb);
			initRs.next();
			int initCount = initRs.getInt(1);
			
			stat.executeUpdate(query);
			
			ResultSet endRs = stat.executeQuery(getNumb);
			endRs.next();
			int endCount = endRs.getInt(1);
			
			if (initCount == (endCount-1))
			{
				System.out.println("You've successfully added the booking." );
			} else {
				System.out.println("There was an error adding the booking.");
			}
			
			
			// close
			stat.close();
			con.close();
			
			
		} catch (SQLException e) {
			System.out.println("Error connecting to db");
			System.out.println(e.getMessage());
		}	
		
		return;	
		
	}
	
	static void checkCheapFlights(){
		

		
		// establish connection
		ResultSet rs = null;
		try {
			con = DriverManager.getConnection(url, user, pwd);
			Statement stat = con.createStatement();
			
			// uses already existing view
			rs = stat.executeQuery("select price,flightnum,departingdate,departingtime from cheapflight");
			
			System.out.println("Price | Flight Number | Departure Date | Departure Time ");
			System.out.println("");
			int count = 0;
			
			while (rs.next()) {
				double price = rs.getDouble(1);
				String flightNum = rs.getString(2);
				String departingDate =  rs.getString(3);
				String departingTime = rs.getString(4);
				
				System.out.println(price + "," + flightNum + ", " + departingDate + ", " + departingTime);
				
				count++;
			}
			
			if (count == 0) {
				System.out.println("No flights cost under 100$");
			}
			// close
			stat.close();
			con.close();
			
			
		} catch (SQLException e) {
			System.out.println("Error connecting to db");
			System.out.println(e.getMessage());
		}	
		
		return;	
		
	}
	
	static void checkEverythingAtLocation(){
		
		// ask for the location
		System.out.println("Getting every listing at a location, please enter the country.");
		String country = sc.nextLine();
		
		System.out.println("Please enter the city (optional, press enter otherwise)");
		String city = sc.nextLine();
		
		String queryRestaurant = "select name,cuisine, chef, rating from restaurant "
				+ " where country = '" + country + "'";
		String queryHotel = "select hotelname, numroomsavail, pricepernight, starrating from hotel "
				+ " where country = '" + country + "'";
		String queryActivity = "select name, availability, time, date, price, minage from activity "
				+ " where country = '" + country + "'";
		
		if (!city.isEmpty()) {
			queryRestaurant += " and city = '" + city + "'";
			queryHotel += " and city = '" + city + "'";
			queryActivity += " and city = '" + city + "'";
		}
		
		
		
		// establish connection
		ResultSet rs = null;
		try {
			con = DriverManager.getConnection(url, user, pwd);
			Statement stat = con.createStatement();
			
			// start with restaurants
			rs = stat.executeQuery(queryRestaurant);
			
			int count = 0;
			
			System.out.println("Restaurants:");
			
			System.out.println("Name | Cuisine | Chef | Rating" );
			System.out.println("");
			
			while (rs.next()) {
				String name = rs.getString(1);
				String cuisine = rs.getString(2);
				String chef = rs.getString(3);
				String rating = rs.getString(4);
				
				System.out.println(name + ", " + cuisine + ", " + chef + ", " + rating );
				
				count++;
			}
			
			if (count == 0) {
				System.out.println("No Restaurant at specified location");
			}
			System.out.println("");
			count = 0;
			rs = stat.executeQuery(queryHotel);
			
			System.out.println("Hotels:");
			System.out.println("Name | Number of available rooms | Price/night | Rating ");
			System.out.println("");
			
			while (rs.next()) {
				String name = rs.getString(1);
				String avail = rs.getString(2);
				String price = rs.getString(3);
				String rating = rs.getString(4);
				
				System.out.println(name + ", " + avail + ", " + price + ", " + rating);
			}
			
			if (count == 0) {
				System.out.println("No Hotel at specified location");
			}
			System.out.println("");
			count = 0;
			rs = stat.executeQuery(queryActivity);
			
			System.out.println("Activities:");
			System.out.println("Name | Availability | Time | Date | Price | Minimum age");
			System.out.println("");
			
			while (rs.next()) {
				String name = rs.getString(1);
				String avail = rs.getString(2);
				String time = rs.getString(3);
				String date = rs.getString(4);
				String price = rs.getString(5);
				String minage = rs.getString(6);
				
				System.out.println(name + ", " + avail + ", " + time + ", " + date + ", " + price + ", " + minage);
			}
			
			if (count == 0) {
				System.out.println("No activity at specified location");
			}
			
			// close
			stat.close();
			con.close();
			
			
		} catch (SQLException e) {
			System.out.println("Error connecting to db");
			System.out.println(e.getMessage());
		}	
		
		return;	
		
	}

	static void visualise() {
		System.out.println("Your options are: ");
		System.out.println("1. Pie chart of hotel locations");
		System.out.println("2: Scatter Plot of flight costs against month");
		System.out.println("3: Exit");
		System.out.println("Please select an option between 1 and 3");
		int selected = -1;
		do {
			selected = visualiseHelper();

			if (selected == 1) {
				makePieChart();
			}
			else if (selected == 2) {
				makeScatterPlot();
				System.out.println("test1");
			}
			if (selected != 3 ) {
				System.out.println("Continue? [Press anything]");
				sc.nextLine();	
			}
		} while (selected !=3);
		System.out.println("Exiting...");

	}

	static int visualiseHelper() {
		boolean notified = false;
		int selected = -1;
		while (selected < 0)
		{
			try {
				selected = Integer.parseInt(sc.nextLine());

			} catch (Exception e) {
				selected = -1;
				System.out.println("Please select an option from integer 1 to 3");
				notified = true;
			}
			
			if ((selected < 1 || selected > 3) && !notified) {
				selected = -1;
				System.out.println("Please select an option between 1 and 3");
			}
			notified = false;
		}
		return selected;
	}

	static void makePieChart() {

		try {
			con = DriverManager.getConnection(url, user, pwd);
			Statement stat = con.createStatement();
			
			String query = "select country, count(*) as counts from hotel group by country";
			
			ResultSet resultSet = stat.executeQuery(query);
			// close
			stat.close();
			con.close();

			DefaultPieDataset dataset = new DefaultPieDataset( );
			
			while( resultSet.next( ) ) {
			dataset.setValue( 
			resultSet.getString( "city" ) ,
			Double.parseDouble( resultSet.getString( "counts" )));
			}
			
			JFreeChart chart = ChartFactory.createPieChart(
			"Hotel locations",   // chart title           
			dataset,          // data           
			true,             // include legend          
			true,           
			false );
	
			int width = 560;    /* Width of the image */
			int height = 370;   /* Height of the image */ 
			File pieChart = new File( "Pie_Chart.jpeg" );
			try {
				ChartUtilities.saveChartAsJPEG( pieChart , chart , width , height );
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
			
		} catch (SQLException e) {
			System.out.println("Error connecting to db");
			System.out.println(e.getMessage());
		}	
	}

	static void makeScatterPlot(){
		System.out.println("Test");
		try {
			con = DriverManager.getConnection(url, user, pwd);
			Statement stat = con.createStatement();
			
			String query = "select departingdate, price from flight";
			
			ResultSet resultSet = stat.executeQuery(query);
			// close
			stat.close();
			con.close();

			XYDataset dataset = createXYDataset(resultSet);
			
			JFreeChart chart = ChartFactory.createScatterPlot(
			"Flight cost against month",
			"X-Axis", "Y-Axis", dataset
			);

			int width = 560;    /* Width of the image */
			int height = 370;   /* Height of the image */ 
			File scatterPlot = new File( "Scatter_Plot.jpeg" );
			try {
				ChartUtilities.saveChartAsJPEG( scatterPlot , chart , width , height );
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		
		} catch (SQLException e) {
			System.out.println("Error connecting to db");
			System.out.println(e.getMessage());
		}	
	}

	static XYDataset createXYDataset(ResultSet resultSet) {
		XYSeriesCollection dataset = new XYSeriesCollection();
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		XYSeries series = new XYSeries("flights");

		try {
		while( resultSet.next( ) ) {
			series.add( 
			(df.parse(resultSet.getString( "departingdate" )).getMonth()),
			Double.parseDouble( resultSet.getString( "price" )));
		}
		dataset.addSeries(series);
		} catch(Exception e) {
			System.err.println(e);
		}

		return dataset;
	}
	
	static int askOption(){
		
		System.out.println("Your options are: ");
		System.out.println("1. Add a client");
		System.out.println("2: Create a new booking for a client");
		System.out.println("3: Check all flights below 100$");
		System.out.println("4: List hotels, restaurants and activities in a city");
		System.out.println("5: Get the email of a client");
		System.out.println("6: Print graphs ");
		System.out.println("7: Exit");
		System.out.println("Please select an option between 1 and 7");
		int selected = -1;
		
		boolean notified = false;
		
		while (selected < 0)
		{
			try {
				selected = Integer.parseInt(sc.nextLine());

			} catch (Exception e) {
				selected = -1;
				System.out.println("Please select an option from integer 1 to 7");
				notified = true;
			}
			
			if ((selected < 1 || selected > 7) && !notified) {
				selected = -1;
				System.out.println("Please select an option between 1 and 7");
			}
			notified = false;
		}
		
		
		return selected;
		
	}

}
