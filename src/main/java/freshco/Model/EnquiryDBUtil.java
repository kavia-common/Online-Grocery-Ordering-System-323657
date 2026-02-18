package freshco.Model;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import freshco.Beans.Enquiry;

public class EnquiryDBUtil {

	public static List<Enquiry> getAllEnquiry() throws Exception {
		List<Enquiry> enquiry = new ArrayList<>();
		String query = "SELECT * FROM Enquiry";

		ResultSet rs = webDB.executeSearch(query);
		while (rs.next()) {
			Enquiry enq = new Enquiry(rs.getInt("EnID"), rs.getString("email"), rs.getString("subject"),
					rs.getString("comments"), rs.getString("response"));
			enquiry.add(enq);
		}
		rs.close();
		return enquiry;
	}

	public static boolean insertEnquiry(String email, String subject, String comments) {

		boolean isSuccess = false;

		String sql = "INSERT INTO enquiry (email, subject, comments) VALUES (?, ?, ?)";

		try {
			int rowsAffected = webDB.executeIUD(sql, email, subject, comments);

			isSuccess = rowsAffected > 0;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return isSuccess;
	}

	public static boolean updateEnquiry(int EnID, String response) {

		boolean isSuccess = false;

		String sql = "UPDATE enquiry SET response=? WHERE EnID=?";

		try {
			int rowsAffected = webDB.executeIUD(sql, response, EnID);

			isSuccess = rowsAffected > 0;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return isSuccess;
	}

	public static boolean deleteEnquiry(int EnID) {

		boolean isSuccess = false;

		String sql = "DELETE FROM enquiry WHERE EnID=?";

		try {
			int rowsAffected = webDB.executeIUD(sql, EnID);

			isSuccess = rowsAffected > 0;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return isSuccess;
	}
}
