package freshco.Model;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import freshco.Beans.Category;

public class CategoryDBUtil {

	// Get all categories from the database
	public static List<Category> getAllCategories() throws Exception {
		List<Category> categories = new ArrayList<>();// List to store Category objects
		String query = "SELECT * FROM Category";

		try {
			ResultSet rs = webDB.executeSearch(query); // Execute query to get categories from DB

			while (rs.next()) {
				Category category = new Category(
						rs.getInt("CID"),
						rs.getString("category_Name"),
						rs.getString("imgUrl")

				);
				categories.add(category);
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return categories;
	}

	// Insert a new category
	public static boolean insertCategory(String category_Name, String imgUrl, int ID) {
		boolean isSuccess = false;

		// SQL query to insert into the Category table
		String sqlCategory = "INSERT INTO Category (category_Name, ImgUrl) VALUES (?, ?)";

		try {
			// Execute the insert query for the Category
			int rowsAffected = webDB.executeIUD(sqlCategory, category_Name, imgUrl);

			if (rowsAffected > 0) {
				// Get the last inserted category ID
				ResultSet rs = webDB.executeSearch("SELECT LAST_INSERT_ID()");
				if (rs.next()) {
					int lastCategoryID = rs.getInt(1);

					String sqlCategoryEmployee = "INSERT INTO category_employee (CID, EmID) VALUES (?, ?)";
					int rowsAffected2 = webDB.executeIUD(sqlCategoryEmployee, lastCategoryID, ID);

					isSuccess = rowsAffected2 > 0;
				}
				rs.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return isSuccess;
	}

	// Update an existing category
	public static boolean updateCategory(int CID, String category_Name) {
		boolean isSuccess = false;
		String sql = "UPDATE Category SET category_Name=? WHERE CID=?";

		try {
			int rowsAffected = webDB.executeIUD(sql, category_Name, CID); // Execute the update query
			isSuccess = rowsAffected > 0; // Check if the update was successful
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isSuccess;
	}

	// Delete a category by ID
	public static boolean deleteCategory(int CID) {
		boolean isSuccess = false;
		String sql = "DELETE FROM Category WHERE CID=?";

		try {
			int rowsAffected = webDB.executeIUD(sql, CID); // Execute the delete query
			isSuccess = rowsAffected > 0; // Check if the deletion was successful
		} catch (Exception e) {
			e.printStackTrace();
		}

		return isSuccess;
	}
}
