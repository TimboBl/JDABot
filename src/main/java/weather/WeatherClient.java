package weather;

import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import utils.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created by Timbo on 30.06.17.
 * This class queries OpenWeatherMap for the Weather Data on the requested location
 */
public class WeatherClient
{
    /**
     * The API Key for OpenWeatherMap
     */
    private static final String API_KEY = getApiKey();

    /**
     * The Path to the key file
     */
    private static final String PATH_TO_KEY = "src/main/res/apikey.txt";

    /**
     * The Base URL to query OpenWeatherMap's API
     */
    private static final String BASE_URL = "http://api.openweathermap.org/data/2.5/";

    /**
     * Gives back a nicely formatted Message Object
     * @param location The city the weather is being asked for
     * @return A Message Object holding all the relevant weather information
     * @throws IOException When OpenWeatherMap returns an error
     * @throws ParseException When the JSON Object cannot be parsed correctly
     */
    public Message getCompleteFormattedWeatherFor(String location) throws IOException, ParseException
    {
        MessageBuilder builder = new MessageBuilder();
        String[] data = getWeatherData(location);

        builder.append("The weather in " + location + "is currently:\n");
        builder.append(data[0]);
        builder.append("\nThe Temperature right now is at: ");
        builder.append(data[1]);
        builder.append(" °C\nToday you can expect at least: ");
        builder.append(data[2]);
        builder.append(" °C\nThe highest Temperatures today will be around: ");
        builder.append(data[3]);
        builder.append(" °C\nCurrent Windspeeds can be observed at about: ");
        builder.append(data[4]);
        builder.append(" km/h\n The direction of the Wind is at: ");
        builder.append(data[5]);
        builder.append(" degrees\n");


        return builder.build();
    }

    /**
     * Gives a nicely formatted Message Object with the Temperature information
     * @param location The city the temperature is being queried for
     * @return A Message Object with all the Temperature information in it
     * @throws IOException When OpenWeatherMap returns an error
     * @throws ParseException When the JSON Object cannot be parsed correctly
     */
    public Message getTemperatureDataFor(String location) throws IOException, ParseException
    {
        MessageBuilder builder = new MessageBuilder();
        String[] data = getWeatherData(location);

        builder.append("The Temperature right now is at: ");
        builder.append(data[1]);
        builder.append(" °C\nToday you can expect at least: ");
        builder.append(data[2]);
        builder.append(" °C\n and at most: ");
        builder.append(data[3]);
        builder.append(" °C");

        return builder.build();
    }

    /**
     * Queries OpenWeatherMap for the location's weather
     * @param location The location the weather is being queried for
     * @return A JSON Object in String format with the weather data in it
     * @throws IOException The Connection to the Website cannot be established
     */
    private String getWeatherDataFor(String location) throws IOException
    {
        URL weatherUrl = new URL(BASE_URL + "weather?q=" + location + "&APPID=" + API_KEY + "&units=metric");

        HttpURLConnection connection = (HttpURLConnection) weatherUrl.openConnection();

        connection.setRequestMethod("POST");

        connection.setDoOutput(true);

        int responseCode = connection.getResponseCode();

        Log.info("Response Code from OpenWeatherMap: " + responseCode);

        BufferedReader in = new BufferedReader(
		        new InputStreamReader(connection.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();



        return response.toString();
    }

    /**
     * Gives an Array with all the important weather information
     * @param location The city the weather is being queried for
     * @return An Array with weather condition (rain etc), temperature, min_temperature, max_temperature, wind speed and wind direction
     * @throws IOException When OpenWeatherMap returns an error
     * @throws ParseException When the JSON Object could not be parsed correctly
     */
    private String[] getWeatherData(String location) throws IOException, ParseException
    {
        String[] data = new String[6];

        data[0] = getJSONKeyValue("weather", location).get("main").toString();

        data[1] = getJSONKeyValue("main", location).get("temp").toString(); //Geht

        data[2] = getJSONKeyValue("main", location).get("temp_min").toString();

        data[3] = getJSONKeyValue("main", location).get("temp_max").toString();

        data[4] = getJSONKeyValue("wind", location).get("speed").toString();

        data[5] = getJSONKeyValue("wind", location).get("deg").toString();

        return data;
    }


    private JSONObject getJSONKeyValue(String key, String location) throws IOException, ParseException
    {
        JSONParser parser = new JSONParser();

        String json = getWeatherDataFor(location);
        Object array = parser.parse(json);

        JSONObject decodeArray = (JSONObject) array;

        Log.info(decodeArray.toString());

        return (JSONObject) decodeArray.get(key);
    }

    /**
     * Returns the API Key form the File specified in the field of this class
     * @return A String with the API Key in it
     */
    private static String getApiKey()
    {
        try
        {
            List<String> readAllLines = Files.readAllLines(Paths.get(PATH_TO_KEY));
            return readAllLines.get(0);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            Log.warning("The API Key for OpenWeatherMap could not be loaded!");
        }
        return null;
    }

}
