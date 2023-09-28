import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.StringJoiner;

public class DatabaseService {

	String url;
	String username;
	String password;
	String driver = "oracle.jdbc.driver.OracleDriver";

	public DatabaseService(String configFilePath) {
		DatabaseConfigReader configReader = new DatabaseConfigReader(configFilePath);
		this.url = configReader.getUrl();
		this.username = configReader.getUsername();
		this.password = configReader.getPassword();
	}

	public Connection connectToDatabase() throws SQLException {
		Connection connection = null;
		try {
			Class.forName(driver);

			// Create a connection to the database.
			connection = DriverManager.getConnection(url, username, password);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		return connection;
	}

	public Employee[] getEmployeNames() throws SQLException, ClassNotFoundException {
		// Connection to Db

		// Connection con = DriverManager.getConnection(url);
		Connection con = connectToDatabase();

		// Creating statement
		Statement stmt = con.createStatement();

		// getting the number of Rows
		String query = "select count(*) from employee";
		ResultSet rs = stmt.executeQuery(query);
		rs.next();
		int numberOfRows = rs.getInt(1);

		Employee[] employees = new Employee[numberOfRows];

		// getting full employees details
		query = "select * from employee order by name ASC";
		rs = stmt.executeQuery(query);

		// mapping employee table to employee class
		int index = 0;
		while (rs.next()) {

			String name = rs.getString(1);
			String signum = rs.getString(2);
			Employee e = new Employee(name, signum);
			employees[index] = e;
			index++;
		}

		con.close();
		return employees;

	}

	public Leave[] getEmployeeLeaves(String signum, boolean allLeaves) throws ClassNotFoundException, SQLException {
		// Connection to Db
		Connection con = connectToDatabase();
		// getting the number of Rows
		String query = "select count(*) from emp_leaves where signum in (?) and (MONTH(from_date)=MONTH(now()) or MONTH(to_date)=MONTH(now())) and (YEAR(from_date)=YEAR(now()) or YEAR(to_date)=YEAR(now()))";

		if (allLeaves) {
			query = "select count(*) from emp_leaves where signum in (?)";
		}
		PreparedStatement ps = con.prepareStatement(query);
		ps.setString(1, signum);

		ResultSet rs = ps.executeQuery();
		rs.next();
		int numberOfRows = rs.getInt(1);

		// getting the empName
		query = String.format("select name from employee where signumid in (%s)", signum);
		rs = ps.executeQuery();
		rs.next();
		String name = rs.getString(1);

		// getting the emp leaves list
		query = "select * from emp_leaves where signum in (?) and (MONTH(from_date)=MONTH(now()) or MONTH(to_date)=MONTH(now())) and (YEAR(from_date)=YEAR(now()) or YEAR(to_date)=YEAR(now()))";
		if (allLeaves) {
			query = "select * from emp_leaves where signum in (?)";
		}

		PreparedStatement ps1 = con.prepareStatement(query);
		ps1.setString(1, signum);

		rs = ps1.executeQuery();

		// creating the leaveslist
		Leave[] empLeaves = new Leave[numberOfRows];
		int index = 0;
		while (rs.next()) {
			empLeaves[index] = new Leave(rs.getInt("leaveid"), rs.getString("signum"),
					rs.getDate("from_date").toString(), rs.getDate("to_date").toString(), rs.getString("type"), name,
					rs.getString("mode"), rs.getString("reason"), rs.getDouble("number_of_days"));
			index++;
		}
		con.close();
		return empLeaves;

	}

	public Leave[] getLeavesInMonth(String signum, Date date) throws ClassNotFoundException, SQLException {
		// Connection to Db
		Connection con = connectToDatabase();
		int month = getMonthNumber(date);
		int year = getYearNumber(date);
		// getting the number of Rows
		String query = "SELECT COUNT(*) FROM emp_leaves WHERE signum IN (?) AND (EXTRACT(MONTH FROM from_date) = ? OR EXTRACT(MONTH FROM to_date) = ?) AND (EXTRACT(YEAR FROM from_date) = ? OR EXTRACT(YEAR FROM to_date) = ?)";

		PreparedStatement ps = con.prepareStatement(query);
		ps.setString(1, signum);
		ps.setInt(2, month);
		ps.setInt(3, month);
		ps.setInt(4, year);
		ps.setInt(5, year);

		ResultSet rs = ps.executeQuery();
		rs.next();
		int numberOfRows = rs.getInt(1);

		// getting the empName
		query = String.format("select name from employee where signumid in (%s)", signum);
		rs = ps.executeQuery();
		rs.next();
		String name = rs.getString(1);

		// getting the emp leaves list
		query = "SELECT * FROM emp_leaves WHERE signum IN (?) AND (EXTRACT(MONTH FROM from_date) = ? OR EXTRACT(MONTH FROM to_date) = ?) AND (EXTRACT(YEAR FROM from_date) = ? OR EXTRACT(YEAR FROM to_date) = ?)";

		PreparedStatement ps1 = con.prepareStatement(query);
		ps1.setString(1, signum);
		ps1.setInt(2, month);
		ps1.setInt(3, month);
		ps1.setInt(4, year);
		ps1.setInt(5, year);

		rs = ps1.executeQuery();

		// creating the leaveslist
		Leave[] empLeaves = new Leave[numberOfRows];
		int index = 0;
		while (rs.next()) {
			empLeaves[index] = new Leave(rs.getInt("leaveid"), rs.getString("signum"),
					rs.getDate("from_date").toString(), rs.getDate("to_date").toString(), rs.getString("type"), name,
					rs.getString("mode"), rs.getString("reason"), rs.getDouble("number_of_days"));
			index++;
		}
		con.close();
		return empLeaves;

	}

	public Employee getEmployeDetails(String signum) throws ClassNotFoundException, SQLException {

		System.out.println(signum);
		Connection con = connectToDatabase();

		// getting the number of Rows
		String query = "select * from employee where signumid in (?)";
		PreparedStatement ps = con.prepareStatement(query);
		ps.setString(1, signum);
		ResultSet rs = ps.executeQuery();
		rs.next();
		return new Employee(rs.getString("name"), rs.getString("signumid"));
	}

	public DBResponse addLeave(Leave leave) throws ClassNotFoundException, SQLException {
		Connection con = connectToDatabase();

		Date fromDate = Date.valueOf(leave.from_date);
		Date toDate = Date.valueOf(leave.to_date);
		if (this.isOverlapping(leave)) {
			return new DBResponse("Leave dates are overlapping with existing leaves", "dates-overlap");
		}

		// Creating statement
		String query = "INSERT INTO emp_leaves (signum, from_date, type, \"mode\" , reason, to_date, number_of_days) VALUES (?, TO_DATE(?, 'YYYY-MM-DD'), ?, ?, ?, TO_DATE(?, 'YYYY-MM-DD'), ?)";
		PreparedStatement ps = con.prepareStatement(query);
		ps.setString(1, leave.signum);
		ps.setString(2, fromDate.toString());
		ps.setString(3, leave.type);
		ps.setString(4, leave.mode);
		ps.setString(5, leave.reason);
		ps.setString(6, toDate.toString());

		if (isSameDay(fromDate, toDate) && getWorkingDays(fromDate, toDate) == 1
				&& this.getHolidays(fromDate, toDate) == 0) {
			if ("HALF".equals(leave.type)) {
				ps.setDouble(7, 0.5);
			} else {
				ps.setDouble(7, 1);
			}
		} else {
			int numberOfDays = calculateActualLeaveDays(fromDate, toDate);
			if (numberOfDays == 0) {
				return new DBResponse("No working days in the selected leave", "no-working-days");
			}
			ps.setDouble(7, numberOfDays);
		}
		ps.executeUpdate();

		String emails = getEmails(leave.getSignum());
		OutlookEmailSender.sendEmail(emails, "New leave added by " + leave.name + "(" + leave.signum + ")",
				"Name: " + leave.name + "\nSignum: " + leave.signum + "\nFrom Date: " + leave.from_date
						+ "\nTo Date: " + leave.to_date + "\nReason: " + leave.reason);

		// EmailService.sendLeaveEmail(leave, false);
		return new DBResponse("Leave added successfully", "leave-added");

	}

	public Leave[] getLeavesBetweenDates(String from, String to) throws SQLException, ClassNotFoundException {
		// Connection to Db
		Connection con = connectToDatabase();
		// getting the number of Rows
		String countQuery = "SELECT COUNT(*) FROM emp_leaves WHERE (from_date BETWEEN TO_DATE(?,'YYYY-MM-DD') AND TO_DATE(?,'YYYY-MM-DD')) OR (to_date BETWEEN TO_DATE(?,'YYYY-MM-DD') AND TO_DATE(?,'YYYY-MM-DD'))";
		PreparedStatement countPs = con.prepareStatement(countQuery);
		countPs.setString(1, from);
		countPs.setString(2, to);
		countPs.setString(3, from);
		countPs.setString(4, to);

		ResultSet countRs = countPs.executeQuery();
		countRs.next();
		int numberOfRows = countRs.getInt(1);
		Leave[] empLeaves = new Leave[numberOfRows];

		// getting the emp leaves list
		String query = "SELECT emp.signumid, emp.name, leaves.leaveid, leaves.from_date, leaves.to_date, leaves.type, leaves.\"mode\", leaves.reason, leaves.number_of_days FROM emp_leaves leaves INNER JOIN employee emp ON emp.signumid = leaves.signum WHERE leaves.from_date BETWEEN TO_DATE(?,'YYYY-MM-DD') AND TO_DATE(?,'YYYY-MM-DD') OR leaves.to_date BETWEEN TO_DATE(?,'YYYY-MM-DD') AND TO_DATE(?,'YYYY-MM-DD')";
		PreparedStatement ps1 = con.prepareStatement(query);
		ps1.setString(1, from);
		ps1.setString(2, to);
		ps1.setString(3, from);
		ps1.setString(4, to);

		ResultSet rs = ps1.executeQuery();

		// creating the leaves list
		int index = 0;
		while (rs.next()) {
			empLeaves[index] = new Leave(rs.getInt("leaveid"), rs.getString("signumid"),
					rs.getDate("from_date").toString(), rs.getDate("to_date").toString(), rs.getString("type"),
					rs.getString("name"), rs.getString("mode"), rs.getString("reason"), rs.getDouble("number_of_days"));
			index++;
		}

		con.close();
		return empLeaves;
	}

	public boolean login(String signum, String password) throws ClassNotFoundException, SQLException {
		Connection con = connectToDatabase();
		String role = "MGR";
		// Creating statement
		String query = "select * from employee where signumid in (?) and password in (?) and role in (?)";
		PreparedStatement ps = con.prepareStatement(query);
		ps.setString(1, signum);
		ps.setString(2, password);
		ps.setString(3, role);
		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			return true;
		}
		return false;
	}

	public Leave getLeave(int leaveId) throws SQLException, ClassNotFoundException {
		Connection con = connectToDatabase();
		String query = "select * from emp_leaves el inner join employee e on e.signumid = el.signum and el.leaveid in (?)";
		PreparedStatement ps = con.prepareStatement(query);
		ps.setInt(1, leaveId);
		ResultSet rs = ps.executeQuery();
		rs.next();
		return new Leave(rs.getInt("leaveid"), rs.getString("signum"), rs.getString("from_date"),
				rs.getString("to_date"), rs.getString("type"), rs.getString("name"), rs.getString("mode"),
				rs.getString("reason"), rs.getInt("number_of_days"));

	}

	public DBResponse deleteLeave(int leaveId) throws ClassNotFoundException, SQLException {
		Connection con = connectToDatabase();
		Leave leave = getLeave(leaveId);
		String query = "delete from emp_leaves where leaveid in (?)";
		PreparedStatement ps = con.prepareStatement(query);
		ps.setInt(1, leaveId);
		ps.executeUpdate();

		String emails = getEmails(leave.getSignum());
		OutlookEmailSender.sendEmail(emails, "Leave Deleted by " + leave.name + "(" + leave.signum + ")",
				"Name: " + leave.name + "\nSignum: " + leave.signum + "\nFrom Date: " + leave.from_date
						+ "\nTo Date: " + leave.to_date + "\nReason: " + leave.reason);

		return new DBResponse("Leave Deleted Successfully", "leave-deleted");

	}

	public DBResponse updateLeave(Leave leave) throws ClassNotFoundException, SQLException {
		Connection con = connectToDatabase();
		String query = "update emp_leaves set from_date=(?), to_date=(?), number_of_days=(?) where leaveid in (?)";
		PreparedStatement ps = con.prepareStatement(query);
		ps.setDate(1, Date.valueOf(leave.from_date));
		ps.setDate(2, Date.valueOf(leave.to_date));
		ps.setInt(3, calculateActualLeaveDays(Date.valueOf(leave.from_date), Date.valueOf(leave.to_date)));
		ps.setInt(4, leave.leaveId);
		ps.executeUpdate();
		String emails = getEmails(leave.getSignum());
		OutlookEmailSender.sendEmail(emails, "Leave update of " + leave.name + "(" + leave.signum + ")",
				"Name: " + leave.name + "\nSignum: " + leave.signum + "\nFrom Date: " + leave.from_date
						+ "\nTo Date: " + leave.to_date + "\nReason: " + leave.reason);
		// EmailService.sendLeaveEmail(leave, true);
		return new DBResponse("Leave updated Successfully", "leaved-updated");

	}

	public static int getMonthNumber(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);

		// get the month number (0-11) from the Calendar object
		return calendar.get(Calendar.MONTH) + 1;
	}

	public static int getYearNumber(Date date) {
		LocalDate localDate = date.toLocalDate();
		// get the year number from the java.time.LocalDate object
		return localDate.getYear();

	}

	public int calculateActualLeaveDays(Date from, Date to) throws ClassNotFoundException, SQLException {

		int c = getWorkingDays(from, to);
		System.out.println(c);
		int holidays = this.getHolidays(from, to);
		return c - holidays;
	}

	public int getHolidays(Date from, Date to) throws ClassNotFoundException, SQLException {
		Connection con = connectToDatabase();
		String query = "SELECT COUNT(*) AS num_holidays FROM holiday_calendar WHERE \"date\" >= TO_DATE(?, 'YYYY-MM-DD') AND \"date\" <= TO_DATE(?, 'YYYY-MM-DD') ";
		PreparedStatement ps = con.prepareStatement(query);
		ps.setDate(1, from);
		ps.setDate(2, to);
		ResultSet rs = ps.executeQuery();
		rs.next();
		return rs.getInt(1);
	}

	public Holiday[] getAllHolidays() throws ClassNotFoundException, SQLException {
		Connection con = connectToDatabase();
		String queryCount = "SELECT COUNT(*) FROM holiday_calendar";
		PreparedStatement ps1 = con.prepareStatement(queryCount);
		ResultSet rs1 = ps1.executeQuery();
		rs1.next();
		int n = rs1.getInt(1);
		String query = "SELECT * FROM holiday_calendar";
		PreparedStatement ps = con.prepareStatement(query);
		ResultSet rs = ps.executeQuery();

		Holiday[] holidays = new Holiday[n];
		int index = 0;
		while (rs.next()) {
			holidays[index] = new Holiday(rs.getDate("date").toString(), rs.getString("name"));
			index++;
		}
		System.out.println(index);
		return holidays;

	}

	public static int getWorkingDays(Date startDate, Date endDate) {
		LocalDate startLocalDate = LocalDate.parse(new SimpleDateFormat("yyyy-MM-dd").format(startDate));
		LocalDate endLocalDate = LocalDate.parse(new SimpleDateFormat("yyyy-MM-dd").format(endDate));

		int workDays = 0;

		// Return 0 if start and end are the same
		if (startLocalDate.equals(endLocalDate)) {
			if (!startLocalDate.getDayOfWeek().equals(DayOfWeek.SATURDAY)
					&& !startLocalDate.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
				return 1;
			} else {
				return 0;
			}

		}

		if (startLocalDate.isAfter(endLocalDate)) {
			LocalDate temp = startLocalDate;
			startLocalDate = endLocalDate;
			endLocalDate = temp;
		}

		LocalDate date = startLocalDate;
		while (date.isBefore(endLocalDate) || date.isEqual(endLocalDate)) {
			if (!date.getDayOfWeek().equals(DayOfWeek.SATURDAY) && !date.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
				workDays++;
			}
			date = date.plusDays(1);
		}
		return workDays;
	}

	public int countWorkingDays(Date startDate, Date endDate) {
		// create a Calendar object and set its time to the start date
		Calendar startCal = Calendar.getInstance();
		startCal.setTime(startDate);

		Calendar endCal = Calendar.getInstance();
		endCal.setTime(endDate);

		int workDays = 0;

		// Return 1 if start and end are the same
		if (startCal.getTimeInMillis() == endCal.getTimeInMillis()) {
			return 1;
		}

		// Count the number of workdays
		while (startCal.getTimeInMillis() <= endCal.getTimeInMillis()) {
			if (startCal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY
					&& startCal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
				++workDays;
			}
			startCal.add(Calendar.DAY_OF_MONTH, 1);
		}

		// Include the end date if it's a work day
		if (endCal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY
				&& endCal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
			++workDays;
		}

		return workDays;

	}

	public static boolean isSameDay(Date date1, Date date2) {
		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
		return fmt.format(date1).equals(fmt.format(date2));
	}

	public boolean isOverlapping(Leave leave) throws SQLException, ClassNotFoundException {
		Connection con = connectToDatabase();
		String query = "SELECT COUNT(*) FROM emp_leaves WHERE signum = ? AND ((from_date <= ? AND to_date >= ?) OR (from_date <= ? AND to_date >= ?) OR (from_date <= ? AND to_date >= ?))";
		PreparedStatement ps = con.prepareStatement(query);
		ps.setString(1, leave.signum);
		ps.setDate(2, Date.valueOf(leave.from_date));
		ps.setDate(3, Date.valueOf(leave.to_date));

		ps.setDate(4, Date.valueOf(leave.from_date));
		ps.setDate(5, Date.valueOf(leave.from_date));

		ps.setDate(6, Date.valueOf(leave.to_date));
		ps.setDate(7, Date.valueOf(leave.to_date));

		ResultSet rs = ps.executeQuery();
		rs.next();
		int count = rs.getInt(1);
		if (count > 0) {
			return true;
		} else {
			return false;
		}

	}

	String getEmails(String signum) throws ClassNotFoundException, SQLException {
		Connection con = connectToDatabase();
		String query = "SELECT email FROM employee WHERE role='MGR' OR signumid=(?)";
		PreparedStatement ps = con.prepareStatement(query);
		ps.setString(1, signum);

		ResultSet res = ps.executeQuery();

		StringJoiner emailJoiner = new StringJoiner(", ");

		while (res.next()) {
			String email = res.getString("email");
			emailJoiner.add(email);
		}

		String emails = emailJoiner.toString();
		System.out.println(emails);
		return emails;

	}

}