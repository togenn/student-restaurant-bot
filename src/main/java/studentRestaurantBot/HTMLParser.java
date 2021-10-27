package studentRestaurantBot;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

//Parses restaurant info from https://www.kela.fi/ateriatuki-opiskelijaravintolahaku
public class HTMLParser {

    public ArrayList<Restaurant> getRestaurants() throws IOException {
        ArrayList<String> regionLinks = regionLinks();
        ArrayList<String> cityLinks = cityLinks(regionLinks);
        ArrayList<String> restaurantLinks = restaurantLinks(cityLinks);
        ArrayList<Restaurant> restaurantInfo = restaurantInfo(restaurantLinks);

        return restaurantInfo;
    }

    private ArrayList<String> regionLinks() throws IOException {
        Document doc = Jsoup.connect("https://www.kela.fi/ateriatuki-opiskelijaravintolahaku").get();

        ArrayList<String> regionLinks = new ArrayList<>();
        Elements links = doc.select("ul[class=layouts level-3]").select("a[href]");

        for (Element link : links) {
            regionLinks.add(link.attr("href"));
        }

        return regionLinks;
    }

    private ArrayList<String> cityLinks(ArrayList<String> regionLinks) throws IOException {
        ArrayList<String> cityLinks = new ArrayList<>();

        for (String regionLink : regionLinks) {
            Document doc = Jsoup.connect(regionLink).get();

            Elements links = doc.select("a[href^=https://asiointi.kela.fi/opiskelijaravintolahaku]");

            for (Element link : links) {
                cityLinks.add(link.attr("href"));
            }

        }

        return cityLinks;
    }

    private ArrayList<String> restaurantLinks(ArrayList<String> cityLinks) throws IOException {
        ArrayList<String> restaurantLinks = new ArrayList<>();

        for (String cityLink : cityLinks) {
            Document doc = Jsoup.connect(cityLink).get();

            if (doc.select("td[valign=top]").text().equals("Ei ole")) {
                continue;
            }

            Elements links = doc.select("td[valign=top]").select("a");

            for (Element link : links) {
                restaurantLinks.add(link.attr("abs:href"));
            }

        }

        return restaurantLinks;

    }

    private ArrayList<Restaurant> restaurantInfo(ArrayList<String> restaurantLinks) throws IOException {
        ArrayList<Restaurant> restaurants= new ArrayList<>();

        for (String restaurantLink : restaurantLinks) {
            Document doc = Jsoup.connect(restaurantLink).get();
            String name = doc.select("td").select("b").text();
            String city = doc.select("td:contains(Postitoimipaikka:) ~ td:eq(2)").text();
            String webAddress = doc.select("td:contains(Ravintolan web:) ~ td:eq(2)").select("a").attr("href");
            String address = doc.select("td:contains(Katuosoite:) ~ td:eq(2)").text();
            String ZIPCode = doc.select("td:contains(Postinumero:) ~ td:eq(2)").text();
            
            if (address.isEmpty()) {
                continue;
            }
            
            if (city.equals("HUS")) {
                city = "HELSINKI";
            }

            Restaurant restaurant = new Restaurant(name, city, webAddress, address, ZIPCode);

            restaurants.add(restaurant);

        }

        return restaurants;
    }

}
