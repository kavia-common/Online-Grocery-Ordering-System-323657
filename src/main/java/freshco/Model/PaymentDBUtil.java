package freshco.Model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import freshco.Beans.Payment;

public class PaymentDBUtil {

	public static List<Payment> getAllPayment() throws Exception {
		List<Payment> payment = new ArrayList<>();
		String query = "SELECT * FROM Payment";

		ResultSet rs = webDB.executeSearch(query);
		while (rs.next()) {
			Payment pay = new Payment(rs.getInt("PID"), rs.getString("payMethod"), rs.getBoolean("payStatus"));
			payment.add(pay);
		}
		rs.close();
		return payment;
	}

	public static boolean insertPayment(String payMethod, Boolean payStatus) {

		boolean isSuccess = false;

		String sql = "INSERT INTO payment (payMethod, payStatus) VALUES (?, ?)";

		try {
			int rowsAffected = webDB.executeIUD(sql, payMethod, payStatus);

			isSuccess = rowsAffected > 0;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return isSuccess;
	}

	public static boolean updatePayment(int PID, String payMethod, boolean payStatus) {

		boolean isSuccess = false;

		String sql = "UPDATE payment SET payMethod=?, payStatus=? WHERE PID=?";

		try {
			int rowsAffected = webDB.executeIUD(sql, payMethod, payStatus, PID);

			isSuccess = rowsAffected > 0;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return isSuccess;
	}

	public static boolean deletePayment(int PID) {

		boolean isSuccess = false;

		String sql = "DELETE FROM payment WHERE PID=?";

		try {
			int rowsAffected = webDB.executeIUD(sql, PID);

			isSuccess = rowsAffected > 0;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return isSuccess;
	}

	public static int createPayment(String paymentMethod) throws Exception {
		int status = paymentMethod.equals("card") ? 1 : 0; // Set status based on payment method
		String sql = "INSERT INTO Payment (payMethod, payStatus) VALUES (?, ?)";

		Integer affectedRows = webDB.executeIUD(sql, paymentMethod, status);
		if (affectedRows == 0) {
			throw new SQLException("Creating payment failed, no rows affected.");
		}

		// Get the last inserted ID (payment ID)
		ResultSet rs = webDB.executeSearch("SELECT LAST_INSERT_ID() AS paymentId");
		if (rs.next()) {
			int id = rs.getInt("paymentId");
			rs.close();
			return id;
		} else {
			rs.close();
			throw new SQLException("Creating payment failed, no ID obtained.");
		}
	}
}
