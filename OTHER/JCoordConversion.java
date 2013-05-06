package ed.george.jcoord;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
//Imported Third Party Library
import uk.me.jstott.jcoord.OSRef;


//Class used to convert between NGR and Lat/Lon from CSV and print new CSV to stdOut
//Example.csv is provided
public class JCoordConversion {

	public static void main(String[] args) {
		try {
			runMain();
		} catch (IOException e) {
			//Print any errors
			e.printStackTrace();
		}
	}

	public static void runMain() throws IOException{
		//Read CSV File from location (hard coded)
		FileReader fileReader = new FileReader("/Users/edgeorge/Documents/workspace/JCoordTest/src/ed/george/jcoord/test.csv");
		//Create new Buffered Reader
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		//Create string for the line of file currently being read
		String line;
		//Print header of CSV file
		System.out.println("Operator,Latitude,Longitude,Height,Type,Band");
		//Read file line by line
		while ((line = bufferedReader.readLine()) != null) {
			//Split the current line into sub strings by a comma
			//I.e. get columns of dataset
			String[] tokens = line.split(",");
			//3rd Column is the NGR
			//Create new OSRef object with Location
			OSRef x = new OSRef(tokens[2]);
			//Convert to LatLon object, then retrieve lat and lon
			String lat = Double.toString(x.toLatLng().getLat());
			String lon = Double.toString(x.toLatLng().getLng());
			//Print line to StdOut
			System.out.println(tokens[0] + "," + lat + "," + lon + "," + tokens[3] + "," + tokens[4] + "," + tokens[5]);
		}
		//Close Readers
		bufferedReader.close();
		fileReader.close();
	}
}
