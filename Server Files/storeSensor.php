<?php
	$username=$_POST["username"];
	$light=$_POST["light"];
	$sound=$_POST["sound"];
	$motionX=$_POST["motionX"];
	$motionY=$_POST["motionY"];
	$motionZ=$_POST["motionZ"];
	$battery=$_POST["battery"];
	$conn =new mysqli("localhost","root","mishra2014","smartsensor");
	// Check connection
	if ($conn->connect_error) 
	{
		$result=array("DBConnection"=>0);
		print json_encode($result);
		die("Connection failed: " . $conn->connect_error);
	} 
	else
	{
		$result=array("DBConnection"=>1);
	}
	$sql="insert into ".$username."_sensor (light, sound, motionX, motionY, motionZ, battery) values (".$light.",".$sound.",".$motionX.",".$motionY.",".$motionZ.",".$battery.")";
	if($conn->query($sql)==true)
	{
		$result=$result+array("SensorInsertion"=>1);
	}
	else
	{
		$result=$result+array("SensorInsertion"=>0);
	}
	$sql="select id,light,sound,motionX,motionY,motionZ,status from ".$username."_sense_status where status='ON'";
	$queryResult=$conn->query($sql);
	$aboveThresholdLight=0;
	$aboveThresholdSound=0;
	$aboveThresholdMotion=0;
	$aboveThresholdBattery=0;
	$alertReason="None";
	$rowSenseID=-1;
	$alertReasonDB="";
	if($queryResult->num_rows == 1)
	{
		$result=$result+array("SenseStatus"=>1);
		while ($rowSense = $queryResult->fetch_assoc())
		{
			$rowSenseID=$rowSense["id"];
			$aboveThresholdLight=0;
			$aboveThresholdSound=0;
			$aboveThresholdMotion=0;
			if($light>($rowSense["light"]+5) || $light<($rowSense["light"]-5))
			{
				$aboveThresholdLight=1;
				if($alertReason=="None")
				{
					$alertReason="Light,";
				}
				else
				{
					$alertReason=$alertReason."Light,";
				}
			}
			if(($sound>($rowSense["sound"]+5))||($sound<($rowSense["sound"]-5)))
			{
				$aboveThresholdSound=1;
				if($alertReason=="None")
				{
					$alertReason="Sound,";
				}
				else
				{
					$alertReason=$alertReason."Sound,";
				}
			}
			if(($motionX>($rowSense["motionX"]+2))||($motionX<($rowSense["motionX"]-2))||($motionY>($rowSense["motionY"]+2))||($motionY<($rowSense["motionY"]-2))||($motionZ>($rowSense["motionZ"]+2))||($motionZ<($rowSense["motionZ"]-2)))
			{
				$aboveThresholdMotion=1;
				if($alertReason=="None")
				{
					$alertReason="Motion,";
				}
				else
				{
					$alertReason=$alertReason."Motion,";
				}
			}
			if($battery<10)
			{
				$aboveThresholdBattery=1;
				if($alertReason=="None")
				{
					$alertReason="Battery,";
				}
				else
				{
					$alertReason=$alertReason."Battery,";
				}
			}
			//$result=$result+array("battery"=>$battery);
			//add code here if the sensor needs to be notified of an alert;
		}
	}
	else
	{
		$result=$result+array("SenseStatus"=>0);
	}
	if(($aboveThresholdLight==1)||($aboveThresholdSound==1)||($aboveThresholdMotion==1)||($aboveThresholdBattery==1))
	{
		$result=$result+array("Alert"=>1);
		$result=$result+array("AlertReason"=>$alertReason);
		//alert has to be checked and generated
		$sql="SELECT * FROM ".$username."_sensor ORDER BY id DESC LIMIT 1";		//create query
		$queryResult=$conn->query($sql);							//run query and store into result
		if($queryResult->num_rows == 1)							//ccheck if atleast 1 row is returned
		{
			$rowSensor = $queryResult->fetch_assoc();
			$rowSensorID=$rowSensor["id"];
		}
		else
		{
			$rowSensorID=-1;
		}
		$sql="select id,idSenseStatus, idSensor, status, reason, reasonALL from ".$username."_alert where status='ON'";
		$queryResult=$conn->query($sql);
		if($queryResult->num_rows == 1)
		{
			while ($rowAlert = $queryResult->fetch_assoc())
			{
				$alertReasonDB=$rowAlert["reasonALL"];
				if($aboveThresholdMotion==1)
				{
					if(strpos($alertReasonDB,"Motion")===false)
					{
						$alertReasonDB=$alertReasonDB."Motion,";
					}
				}
				if($aboveThresholdLight==1)
				{
					if(strpos($alertReasonDB,"Light")===false)
					{
						$alertReasonDB=$alertReasonDB."Light,";
					}
				}
				if($aboveThresholdSound==1)
				{
					if(strpos($alertReasonDB,"Sound")===false)
					{
						$alertReasonDB=$alertReasonDB."Sound,";
					}
				}
				if($aboveThresholdBattery==1)
				{
					if(strpos($alertReasonDB,"Battery")===false)
					{
						$alertReasonDB=$alertReasonDB."Battery,";
					}
				}
				$sql="update ".$username."_alert set idSenseStatus = ".$rowSenseID.", idSensor = ".$rowSensorID.", reasonALL = '$alertReasonDB', reason='$alertReason' where status='ON'";
				if($conn->query($sql)==true)
				{
					$result=$result+array("UpdateSensorSense"=>1);
				}
				else
				{
					$result=$result+array("UpdateSensorSense"=>0);
				}
			}
		}
		else
		{
			$sql="insert into  ".$username."_alert (tsStart,idSenseStatus, idSensor, status, reason, reasonALL) values (NOW(),".$rowSenseID.",$rowSensorID,'ON','$alertReason','$alertReason')";
			if($conn->query($sql)==true)
			{
				$result=$result+array("UpdateAlert"=>1);
			}
			else
			{
				$result=$result+array("UpdateAlert"=>1);
			}
		}
		
	}
	else
	{
		$result=$result+array("Alert"=>0);
		$result=$result+array("AlertReason"=>$alertReason);
	}
	$queryResult->close();
	$conn->close();
	print json_encode($result);
?>
