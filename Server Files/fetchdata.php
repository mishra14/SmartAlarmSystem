<?php
	$username=$_POST["username"];			//testing only
	//date_default_timezone_set("America/New_York");
	$conn =new mysqli("localhost","root","mishra2014","smartsensor");		//connect to DB named smartsensor on localhost with id root and pass
	// Check connection
	if ($conn->connect_error) 
	{
		$result=array("DBConnection"=>0);
		print json_encode($result);			//print if DB failed
		die("Connection failed: " . $conn->connect_error);
	} 
	else
	{
		$result=array("DBConnection"=>1);			//print if DB connected
		//print json_encode($dbresult);
	}
	$alert=0;
	$motionAlert=0;
	$lightAlert=0;
	$soundAlert=0;
	$batteryAlert=0;
	$sql="SELECT * FROM ".$username."_sensor ORDER BY id DESC LIMIT 1";		//create query
	$queryResult=$conn->query($sql);							//un query and store into result
	if($queryResult->num_rows > 0)							//ccheck if atleast 1 row is returned
	{
		while ($row = $queryResult->fetch_assoc())				//get the value of each row one by one
		{
			$result=$result+array("Data" => array("username"=>$username,"timestamp"=>$row["ts"], "light" =>$row["light"], "sound"=>$row["sound"], "motionX" =>$row["motionX"], "motionY" =>$row["motionY"], "motionZ" =>$row["motionZ"], "battery"=>$row["battery"]));
		}
		$result=$result+array("Error"=>0);
	}
	else
	{
		$result=$result+array("Data"=>array("username"=>$username,"timestamp"=>-1, "light" =>-1, "sound"=>-1, "motionX" =>-1, "motionY" =>-1, "motionZ" =>-1, "battery"=>-1));
		$result=$result+array("Error"=>-1);						//return this if nothing is obtained from the query
	}
	$sql = "select * from ".$username."_alert where status='ON' ORDER BY tsStart DESC LIMIT 1";
	$queryResult=$conn->query($sql);
	if($queryResult->num_rows > 0)
	{
		$alert=1;
		while ($row = $queryResult->fetch_assoc())				//get the value of each row one by one
		{
			$result=$result+array("AlertData" => array("username"=>$username,"timestampStart"=>$row["tsStart"], "timestampUpdate" =>$row["tsUpdate"], "senseStatusID"=>$row["idSenseStatus"], "sensorID" =>$row["idSensor"], "status" =>$row["status"], "reason" =>$row["reason"], "reasonALL"=>$row["reasonALL"]));
			$alertReasonDB=$row["reason"];
			if(strpos($alertReasonDB,"Motion")!==false)
			{
				$motionAlert=1;
			}
			if(strpos($alertReasonDB,"Sound")!==false)
			{
				$soundAlert=1;
			}
			if(strpos($alertReasonDB,"Light")!==false)
			{
				$lightAlert=1;
			}
			if(strpos($alertReasonDB,"Battery")!==false)
			{
				$batteryAlert=1;
			}
		}
	}
	else
	{
		$alert=0;
	}
	$sql="select id,light,sound,motionX,motionY,motionZ,status from ".$username."_sense_status where status='ON'";
	$queryResult=$conn->query($sql);
	$senseStatus=0;
	if($queryResult->num_rows > 0)
	{
		$senseStatus=1;
	}
	$conn->close();
	$result=$result+array("SenseStatus"=>$senseStatus);
	$result=$result+array("Alert"=>$alert);
	$result=$result+array("BatteryAlert"=>$batteryAlert);
	$result=$result+array("MotionAlert"=>$motionAlert);
	$result=$result+array("SoundAlert"=>$soundAlert);
	$result=$result+array("LightAlert"=>$lightAlert);
	print json_encode($result);						//print result
?>
