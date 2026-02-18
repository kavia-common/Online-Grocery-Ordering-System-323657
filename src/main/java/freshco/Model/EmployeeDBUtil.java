package freshco.Model;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import freshco.Beans.DeliveryPerson;
import freshco.Beans.Employee;
import freshco.Beans.Manager;

public class EmployeeDBUtil {
	// Method to validate User based on email and password
	// Used in UserValidation Servlet
	public static Employee validateEmployee(String email, String password) throws Exception {

		String sql = "SELECT * FROM employee WHERE email=? AND password=?";

		ResultSet rs = webDB.executeSearch(sql, email, password);

		if (rs.next()) {
			Employee emp = new Employee(rs.getInt("EmID"), rs.getString("email"), rs.getString("nic"),
					rs.getString("dob"), rs.getString("imgUrl"), rs.getString("phone"), rs.getString("password"));

			rs.close();
			return emp;
		} else {
			rs.close();
			return null;
		}
	}

	// Method to retrieve all the Data of the Employee and the Inheritence
	// Used in ViewEmployee Servlet
	public static List<Employee> getAllEmployees() throws Exception {
		List<Employee> employees = new ArrayList<>();
		String query = "SELECT EmID, email, nic, dob, imgUrl, phone, password, Role, vehicleNum, drivingLicenseNum, city FROM EmployeeRoleView";

		ResultSet rs = webDB.executeSearch(query);
		while (rs.next()) {
			// Fetch common employee fields
			int emID = rs.getInt("EmID");
			String email = rs.getString("email");
			String nic = rs.getString("nic");
			String dob = rs.getString("dob");
			String imgUrl = rs.getString("imgUrl");
			String phone = rs.getString("phone");
			String password = rs.getString("password");
			String role = rs.getString("Role");

			Employee emp;

			// Instantiate the appropriate subclass based on the role
			if ("Manager".equals(role)) {
				// Create Manager object
				emp = new Manager(emID, email, nic, dob, imgUrl, phone, password);
			} else if ("DeliveryPerson".equals(role)) {
				// Fetch delivery-specific fields
				String vehicleNum = rs.getString("vehicleNum");
				String drivingLicenseNum = rs.getString("drivingLicenseNum");
				String city = rs.getString("city");

				// Create DeliveryPerson object
				emp = new DeliveryPerson(emID, email, nic, dob, imgUrl, phone, password, vehicleNum, drivingLicenseNum,
						city);
			} else {
				// Create Normal Employee object
				emp = new Employee(emID, email, nic, dob, imgUrl, phone, password);
			}

			employees.add(emp);
		}
		rs.close();
		return employees;
	}

	// Method to Create Employee
	// Used in AddEmpMan Servlet
	public static boolean insertEmployee(String email, String nic, String dob, String phone, String imgUrl,
			String password, int ID) {

		boolean isSuccess = false;

		String sql = "INSERT INTO employee (email, nic, dob, phone, imgUrl, password) VALUES (?, ?, ?, ?, ?, ?)";

		try {
			int rowsAffected = webDB.executeIUD(sql, email, nic, dob, phone, imgUrl, password);

			isSuccess = rowsAffected > 0;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return isSuccess;
	}

	// Method to Update
	// Used in UpdateEmp Servlet
	public static boolean updateEmployee(int EmID, String email, String imgUrl, String phone, String password) {

		boolean isSuccess = false;

		String sql = "UPDATE employee SET email=?, phone=?, imgUrl=?, password=? WHERE EmID=?";

		try {
			int rowsAffected = webDB.executeIUD(sql, email, phone, imgUrl, password, EmID);

			isSuccess = rowsAffected > 0;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return isSuccess;
	}

	// Method to Delete
	// Used in DeleteEmployee Servlet
	public static boolean deleteEmployee(int EmID) {

		boolean isSuccess = false;

		String sql = "DELETE FROM employee WHERE EmID=?";

		try {
			int rowsAffected = webDB.executeIUD(sql, EmID);

			isSuccess = rowsAffected > 0;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return isSuccess;
	}

	// Method to update employee withoutImage
	// Used in UpdateEmp Servlet
	public static boolean updateEmployeeWithoutImage(int emID, String email, String phone, String password) {
		boolean isSuccess = false;

		String sql = "UPDATE employee SET email=?, phone=?, password=? WHERE EmID=?";

		try {
			int rowsAffected = webDB.executeIUD(sql, email, phone, password, emID);

			isSuccess = rowsAffected > 0;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return isSuccess;
	}
}
