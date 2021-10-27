package studentRestaurantBot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class StudentRestaurantBot extends TelegramLongPollingBot {
	private ArrayList<String> commands;
	private String helpMessage;
	private ArrayList<Restaurant> restaurants;
	private Iterator<Restaurant> restaurantsIterator;

	public StudentRestaurantBot() {
		super();
		ArrayList<String> commands = new ArrayList<>();
		commands.add("/help");
		commands.add("/etsi");
		this.commands = commands;
		this.helpMessage = "Kirjoita /etsi etsiäksesi opiskelijaravintoloita alueeltasi.";
		fileHandler fileHandler = new fileHandler();
		
		if (!new File("restaurants.txt").exists()) {
			System.out.println("Getting restaurant info...");
			HTMLParser parser = new HTMLParser();
			try {
				this.restaurants = parser.getRestaurants();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			System.out.println("Getting coordinates for restaurants...");
			Geocoder geocoder = new Geocoder();

			this.restaurants.stream().forEach(restaurant -> {
				ArrayList<Double> coordinates = new ArrayList<>();
				try {
					coordinates = geocoder.geocode(
							restaurant.getAddress() + " " + restaurant.getCity() + " " + restaurant.getZIPCode());
				} catch (IOException | InterruptedException e) {

					e.printStackTrace();
				}

				restaurant.setCoordinates(coordinates);
			});

			this.restaurants.removeIf(r -> r.getCoordinates().size() == 0);
			fileHandler.saveRestaurants(this.restaurants);
		} else {
			this.restaurants = fileHandler.readRestaurants();
		}

		System.out.println("Ready");
	}

	@Override
	public void onUpdateReceived(Update update) {

		if (update.hasCallbackQuery()) {
			handleCallbackQuery(update.getCallbackQuery());
			return;
		}

		Long chatId = update.getMessage().getChatId();
		String message = update.getMessage().getText();

		if (update.getMessage().hasLocation()) {
			this.restaurantsIterator = showNearby(chatId, update.getMessage().getLocation());
		} else if (!this.commands.contains(message)) {
			sendHelpMessage(chatId);

		} else {

			switch (message) {
			case "/help":
				sendHelpMessage(chatId);
				break;

			case "/etsi":
				askLocation(chatId);
				break;

			}
		}

	}

	private void handleCallbackQuery(CallbackQuery callbackQuery) {
		String str = callbackQuery.getData();
		
		if (str.equals("/more")) {
			showMoreRestaurants(callbackQuery.getMessage().getChatId());
			AnswerCallbackQuery answer = new AnswerCallbackQuery();
			answer.setCallbackQueryId(callbackQuery.getId());
			try {
				execute(answer);
			} catch (TelegramApiException e) {
				e.printStackTrace();
			}
		}
	}

	public String getBotUsername() {

		return "togennBot";
	}

	@Override
	public String getBotToken() {

		return "apikey";
	}

	private void askLocation(Long chatId) {
		ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
		keyboard.setResizeKeyboard(true);
		keyboard.setOneTimeKeyboard(true);

		KeyboardButton askLocation = new KeyboardButton("Jaa sijaintisi");
		KeyboardButton deny = new KeyboardButton("peruuta");
		askLocation.setRequestLocation(true);

		List<KeyboardRow> rows = new ArrayList<>();
		KeyboardRow row = new KeyboardRow();
		row.add(askLocation);
		row.add(deny);

		rows.add(row);

		keyboard.setKeyboard(rows);

		SendMessage message = new SendMessage();
		message.setChatId(chatId.toString());
		message.setText("anna lupa");
		message.setReplyMarkup(keyboard);

		try {
			execute(message);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}

	}

	private void sendHelpMessage(Long chatId) {
		sendMessage(this.helpMessage, chatId);
	}

	private void sendMessage(String text, Long chatId) {
		SendMessage message = new SendMessage();
		message.setChatId(chatId.toString());
		message.setText(text);

		try {
			execute(message);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}

	}

	private void sendMessage(String text, Long chatId, String parseMode) {
		SendMessage message = new SendMessage();
		message.setChatId(chatId.toString());
		message.setText(text);
		message.setParseMode(parseMode);

		try {
			execute(message);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}

	}

	private Iterator<Restaurant> showNearby(Long chatId, Location location) {
		sortRestaurantsByDistance(location);
		List<Restaurant> nearbyRestaurants = new ArrayList<Restaurant>();

		Iterator<Restaurant> restaurantsIterator = this.restaurants.iterator();
		Restaurant restaurant = restaurantsIterator.next();
		while (restaurant.getDistanceFromUser() < 10000 || nearbyRestaurants.size() < 3) {
			nearbyRestaurants.add(restaurantsIterator.next());
		}

		sendMessage("Tässä sinua lähimmät opiskelijaravintolat:", chatId);

		for (Restaurant r : nearbyRestaurants) {
			sendRestaurantInfo(r, chatId);

		}

		sendGetMoreRestaurantsKeyboard(chatId);

		return restaurantsIterator;

	}

	private void sendRestaurantInfo(Restaurant r, Long chatId) {
		double distance = Math.round(r.getDistanceFromUser() / 1000 * 10) / 10.0;
		StringBuilder message = new StringBuilder();

		message.append("ETÄISYYS: " + distance + " km\n");

		String mapLink = "<a href=\"http://www.google.com/maps/place/%s,%s\">Näytä kartalla</a>\n";
		message.append(String.format(mapLink, r.getCoordinates().get(0), r.getCoordinates().get(1)));

		message.append(r.getName() + "\n");
		message.append(r.getCity() + "\n");
		message.append(r.getAddress() + "\n");
		message.append(r.getWebAddress());

		sendMessage(message.toString(), chatId, "HTML");
	}

	private void sendGetMoreRestaurantsKeyboard(Long chatId) {
		InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();

		InlineKeyboardButton moreRestaurants = new InlineKeyboardButton("Lisää ravintoloita");
		moreRestaurants.setCallbackData("/more");
		
		List<List<InlineKeyboardButton>> rows = new ArrayList<>();
		List<InlineKeyboardButton> row = new ArrayList<>();
		row.add(moreRestaurants);

		rows.add(row);

		keyboard.setKeyboard(rows);

		SendMessage message = new SendMessage();
		message.setChatId(chatId.toString());
		message.setReplyMarkup(keyboard);
		message.setText("Näytä enemmän?");

		try {
			execute(message);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}

	}

	private void showMoreRestaurants(Long chatId) {
		try {
			for (int i = 0; i < 3; i++) {
				Restaurant restaurant = this.restaurantsIterator.next();
				sendRestaurantInfo(restaurant, chatId);
			}
		} catch (NoSuchElementException e) {
			sendMessage("Ei enempää ravintoloja", chatId);
			return;
		}
		
		sendGetMoreRestaurantsKeyboard(chatId);

	}

	private void sortRestaurantsByDistance(Location location) {
		double lat = location.getLatitude();
		double lon = location.getLongitude();
		for (Restaurant r : this.restaurants) {
			r.setDistanceFromUser(lat, lon);
		}

		Collections.sort(this.restaurants);
	}

}
