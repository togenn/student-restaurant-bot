package studentRestaurantBot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class fileHandler {

	public void saveRestaurants(ArrayList<Restaurant> restaurants) {

		File file = new File("restaurants.txt");
		try {
			FileOutputStream toFile = new FileOutputStream(file);
			ObjectOutputStream write = new ObjectOutputStream(toFile);

			for (Restaurant r : restaurants) {
				write.writeObject(r);
			}

			toFile.close();
			write.close();

		} catch (IOException e) {
			System.out.println("File not found");
		}
	}

	public ArrayList<Restaurant> readRestaurants() {
		ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
		FileInputStream fromFile = null;
		ObjectInputStream read = null;
		
		try {
			fromFile = new FileInputStream(new File("restaurants.txt"));
			read = new ObjectInputStream(fromFile);

			while (true) {
				restaurants.add((Restaurant) read.readObject());
			}

		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("All read");
		} finally {
			try {
				fromFile.close();
				read.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}

		return restaurants;
	}

}
