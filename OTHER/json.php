<?php
//Information regarding Database
$user_name = "houserea_admin";
$password = "password";
$database = "houserea_diss";
$server = "localhost";

// Attempt connection to MySQL
mysql_connect($server, $user_name, $password);

// Select database
$db_found = mysql_select_db($database);

// Check if database was found
if ($db_found) {

	//Store values from URL
	$lat = $_GET["lat"];
	$lon = $_GET["lon"];
	$diff = $_GET["dif"];
	$debug = $_GET["debug"];


	if(isset($diff) && $diff <= 0.4) {

//Do Nothing if set and less than 0.4
//0.4 degrees is around 30 miles
//Any more and will produce too larger result set

	}else{

     //Roughly 1 mile as 1 degree in Lat/Lon is around 69 miles
     //$diff = 1/69 = 0.0144928	
		$diff = 0.0145; 	
	}

	//Add/Subtract the distance difference
	$latS = $lat - $diff;
	$latL = $lat + $diff;
	$lonS = $lon - $diff;
	$lonL = $lon + $diff;


	//Find appropriate records using MySQL Query
	$sth = mysql_query("SELECT * FROM MastDB WHERE Latitude BETWEEN $latS AND $latL AND Longitude BETWEEN $lonS AND $lonL");
	$rows = array();

	if($debug){
		//Timing for Debug Analysis
		$start_time = (float) round(microtime(true) * 10000,0);
	}


	//Add results of query to $rows[]
	while($r = mysql_fetch_assoc($sth)) {
		$rows[] = $r;
	}
	
	
	if($debug){
		//Display information about query (debug)
		$size = sizeof($rows);
		echo "<html><head><title>Found $size Results</title></head><body>";
		echo "<a href=\"#time\" name=\"top\">Time for lat: $lat & lon: $lon with difference $diff</a> </ br><p>";
	}

    //Correct JSON Formatting
	echo "{\"Locations\": ";
	//print json results
	print json_encode($rows);
	echo "}";

	if($debug){
		//display timing information
		$finish_time = (float) round(microtime(true) * 10000,0);
		$time = ($finish_time - $start_time) / 10000;

		echo "</p></ br> </ br><a name=\"time\" href=\"#top\">Returned $size rows ($time s)</a></body></html>";
	}
}

else {

	//Database was not found. Act as no results returned to avoid errors in mobile app
	echo "{\"Locations\": []}";
	
}
?>