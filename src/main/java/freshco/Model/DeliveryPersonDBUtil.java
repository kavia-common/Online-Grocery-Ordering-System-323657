package freshco.Model;

import java.sql.ResultSet;

import freshco.Beans.DeliveryPerson;

public class DeliveryPersonDBUtil {
	// Method to validate User based on email and password
	// Used in UserValidation Servlet
	public static DeliveryPerson validateDeliveryPerson(String email, String password) throws Exception {
		// SQL query to validate delivery person credentials using JOIN
		String sql = "SELECT e.*, dp.vehicleNum, dp.drivingLicenseNum, dp.city " +
				"FROM employee e " +
				"JOIN deliveryPerson dp ON e.EmID = dp.EmID " +
				"WHERE e.email=? AND e.password=?";

		// Execute the query and get the result set
		ResultSet rs = webDB.executeSearch(sql, email, password);

		// Check if a result was returned
		if (rs.next()) {
			// Create a DeliveryPerson object with the retrieved data
			DeliveryPerson deliveryPerson = new DeliveryPerson(
					rs.getInt("EmID"),
					rs.getString("email"),
					rs.getString("nic"),
					rs.getString("dob"),
					rs.getString("imgUrl"),
					rs.getString("phone"),
					rs.getString("password"),
					rs.getString("vehicleNum"),
					rs.getString("drivingLicenseNum"),
					rs.getString("city"));

			rs.close();
			return deliveryPerson; // Return object
		} else {
			rs.close();
			return null; // Return null if no valid delivery person found
		}
	}

	// Method to CreateDP
	// Used in AddDeliveryPerson Servlet
	public static boolean insertDeliveryPerson(String email, String nic, String dob, String imgUrl, String phone,
			String password, String vehicleNum, String drivingLicenseNum, String city, int ID) {
		boolean isSuccess = false;

		String sqlEmployee = "INSERT INTO employee (email, nic, dob, imgUrl, phone, password) VALUES (?, ?, ?, ?, ?, ?)";

		try {
			int rowsAffected = webDB.executeIUD(sqlEmployee, email, nic, dob, imgUrl, phone, password);

			if (rowsAffected > 0) {
				ResultSet rs = webDB.executeSearch("SELECT LAST_INSERT_ID()");
				if (rs.next()) {
					int lastEmID = rs.getInt(1);

					String sqlDeliveryPerson = "INSERT INTO DeliveryPerson (EmID, vehicleNum, drivingLicenseNum, city) VALUES (?, ?, ?, ?)";
					int rowsAffected2 = webDB.executeIUD(sqlDeliveryPerson, lastEmID, vehicleNum, drivingLicenseNum,
							city);

					isSuccess = rowsAffected2 > 0;
				}
				rs.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return isSuccess;
	}

	// Method to UpdateDP
	// Used in UpdateDeliveryPerson Servlet
	public static boolean updateDP(int EmID, String vehicleNum, String city, String email, String imgUrl, String phone,
			String password) {
		boolean isSuccess = false;

		String sqlDeliveryPerson = "UPDATE deliveryperson SET vehicleNum=?, city=? WHERE EmID=?";

		try {
			int rowsAffected = webDB.executeIUD(sqlDeliveryPerson, vehicleNum, city, EmID);

			String sqlEmployee = "UPDATE employee SET email=?, imgUrl=?, phone=?, password=? WHERE EmID=?";

			int rowsAffected2 = webDB.executeIUD(sqlEmployee, email, imgUrl, phone, password, EmID);

			isSuccess = (rowsAffected > 0 && rowsAffected2 > 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return isSuccess;
	}

	// Method to DeleteDP
	// Used in DeleteDeliveryPerson Servlet
	public static boolean deleteDeliveryPerson(int EmID) {
		boolean isSuccess = false;

		String sqlDeliveryPerson = "DELETE FROM DeliveryPerson WHERE EmID = ?";
		String sqlEmployee = "DELETE FROM employee WHERE EmID = ?";

		try {
			int rowsAffected = webDB.executeIUD(sqlDeliveryPerson, EmID);
			int rowsAffected2 = webDB.executeIUD(sqlEmployee, EmID);

			isSuccess = (rowsAffected > 0 && rowsAffected2 > 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return isSuccess;
	}

	// Method to UpdateDP withoutImage
	// Used in UpdateDeliveryPerson Servlet
	public static boolean updateDPWithoutImage(int emID, String vehicleNum, String city, String email, String phone,
			String password) {
		boolean isSuccess = false;

		String sqlDeliveryPerson = "UPDATE deliveryperson SET vehicleNum=?, city=? WHERE EmID=?";

		try {
			int rowsAffected = webDB.executeIUD(sqlDeliveryPerson, vehicleNum, city, emID);

			String sqlEmployee = "UPDATE employee SET email=?, phone=?, password=? WHERE EmID=?";

			int rowsAffected2 = webDB.executeIUD(sqlEmployee, email, phone, password, emID);

			isSuccess = (rowsAffected > 0 && rowsAffected2 > 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return isSuccess;
	}
}
