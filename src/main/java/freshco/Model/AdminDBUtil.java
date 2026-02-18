package freshco.Model;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import freshco.Beans.Admin; // Assuming you have an Admin bean class

public class AdminDBUtil {

	public static Admin validateAdmin(String email, String password) throws Exception {

		String sql = "SELECT * FROM admin WHERE email=? AND password=?";

		ResultSet rs = webDB.executeSearch(sql, email, password);

		if (rs.next()) {
			Admin adm = new Admin(
					rs.getInt("AID"),
					rs.getString("fName"),
					rs.getString("lName"),
					rs.getString("email"),
					rs.getString("lane"),
					rs.getString("city"),
					rs.getString("dob"),
					rs.getString("imgUrl"),
					rs.getString("phone"),
					rs.getString("password"));
			rs.close();
			return adm;
		} else {
			rs.close();
			return null;
		}
	}

	public static List<Admin> getAllAdmins() throws Exception {
		List<Admin> admins = new ArrayList<>();
		String query = "SELECT * FROM Admin";

		ResultSet rs = webDB.executeSearch(query);
		while (rs.next()) {
			Admin admin = new Admin(
					rs.getInt("AID"),
					rs.getString("fName"),
					rs.getString("lName"),
					rs.getString("email"),
					rs.getString("lane"),
					rs.getString("city"),
					rs.getString("dob"),
					rs.getString("imgUrl"),
					rs.getString("phone"),
					rs.getString("password"));
			admins.add(admin);
		}
		rs.close();
		return admins;
	}

	// Method to add a new admin record
	public static boolean addAdmin(String fName, String lName, String email, String address, String city, String dob,
			String imgUrl, String phone, String password) {
		boolean isSuccess = false;
		String sql = "INSERT INTO Admin (fName, lName, email, lane, city, dob, imgUrl, phone, password) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try {
			int rowsAffected = webDB.executeIUD(sql, fName, lName, email, address, city, dob, imgUrl, phone, password);
			isSuccess = rowsAffected > 0;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return isSuccess;
	}

	// Method to update an existing admin record
	public static boolean updateAdmin(int AID, String fName, String lName, String email, String imgUrl, String phone,
			String password) {
		boolean isSuccess = false;
		String sql = "UPDATE Admin SET fName=?, lName=?, email=?, imgUrl=?, phone=?, password=? WHERE AID=?";

		try {
			int rowsAffected = webDB.executeIUD(sql, fName, lName, email, imgUrl, phone, password, AID);
			isSuccess = rowsAffected > 0;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return isSuccess;
	}

	// Method to delete an admin record
	public static boolean deleteAdmin(int AID) {
		boolean isSuccess = false;
		String sql = "DELETE FROM Admin WHERE AID=?";

		try {
			int rowsAffected = webDB.executeIUD(sql, AID);
			isSuccess = rowsAffected > 0;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return isSuccess;
	}

	public static boolean updateAdminWithoutImage(int aID, String fName, String lName, String email, String phone,
			String password) {
		boolean isSuccess = false;
		String sql = "UPDATE Admin SET fName=?, lName=?, email=?, phone=?, password=? WHERE AID=?";

		try {
			int rowsAffected = webDB.executeIUD(sql, fName, lName, email, phone, password, aID);
			isSuccess = rowsAffected > 0;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return isSuccess;
	}
}
