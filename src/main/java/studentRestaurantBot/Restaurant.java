package studentRestaurantBot;

import java.io.Serializable;
import java.util.ArrayList;

import org.apache.lucene.util.SloppyMath;

public class Restaurant implements Comparable<Restaurant>, Serializable {

	private static final long serialVersionUID = 1L;
	private String name;
    private String city;
    private String webAddress;
    private String address;
    private String ZIPCode;
    private ArrayList<Double> coordinates;
    private double distanceFromUser;

    public Restaurant(String name, String city, String webAddress, String address, String ZIPCode) {
        this.name = name;
        this.city = city;
        this.webAddress = webAddress;
        this.address = address;
        this.ZIPCode = ZIPCode;
        this.coordinates = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    public ArrayList<Double> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(ArrayList<Double> coordinates) {
        this.coordinates = coordinates;
    }

    public String getWebAddress() {
        return webAddress;
    }

    public double getDistanceFromUser() {
        return distanceFromUser;
    }

    public String getAddress() {
        return address;
    }

    public String getZIPCode() {
        return ZIPCode;
    }
    
    public void setDistanceFromUser(double lat, double lon) {
       double distance = SloppyMath.haversinMeters(lat, lon, this.coordinates.get(0), this.coordinates.get(1));
       this.distanceFromUser = distance;
    }

    @Override
    public int compareTo(Restaurant r) {
        return Double.compare(this.distanceFromUser, r.getDistanceFromUser());
    }
    
    
    

}
