<?php
	$username=$_POST['username'];
  	$password=$_POST['password'];
	$conn =new mysqli("localhost","root","mishra2014","smartsensor");
	// Check connection
	if ($conn->connect_error) 
	{
		$result=array("DBConnection"=>0);
		print json_encode($result);
		//die("Connection failed: " . $conn->connect_error);
	}
	else
	{
		$result=array("DBConnection"=>1);
	}
	$sensorCreation=0;
	$sensorStatusCreation=0;
	$alertCreation=0;
	$userCreation=0;
	$sql="create table ".$username."_sensor (
		 ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
		 id int auto_increment, 
		 light float,
		 sound float,
		 motionX float,
		 motionY float,
		 motionZ float,
		 battery float,
		 primary key(id)
	)";
	if($conn->query($sql)==true)
	{
		$sensorCreation=1;
		//echo "1";
	}
	else
	{
		$sensorStatusCreation=0;
		$alertCreation=0;
		$userCreation=0;
		$result=$result+array("SQLError"=>$conn->error." in ".$sql);
	}
	if($sensorCreation==1)
	{
		$sql="create table ".$username."_sense_status
	       		(
				 tsStart TIMESTAMP NULL default NULL,
				 tsStop TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
				 id int auto_increment, 
				 light float,
				 sound float,
				 motionX float,
				 motionY float,
				 motionZ float,
				 battery float,
				 status varchar(3),
				 primary key(id)
			)";
		if($conn->query($sql)==true)
		{
			$sensorStatusCreation=1;
			//echo "2";
		}
		else
		{
			
			$sensorStatusCreation=0;
			$alertCreation=0;
			$userCreation=0;
			$result=$result+array("SQLError"=>$conn->error." in ".$sql);
		}
		if($sensorStatusCreation==1)
		{
			$sql="create table ".$username."_alert
				(
					 tsStart TIMESTAMP NULL default NULL,
				 	 tsUpdate TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
					 id int auto_increment, 
					 idSenseStatus int,
					 idSensor int,
					 status varchar(3),
					 reason varchar(30),
					 reasonALL varchar(30),
					 primary key(id),
					 foreign key(idSenseStatus) references ".$username."_sense_status(id),
					 foreign key(idSensor) references ".$username."_sensor(id)
				)";
			if($conn->query($sql)==true)
			{
				$alertCreation=1;
				//echo "3";
			}
			else
			{
				$alertCreation=0;
				$userCreation=0;
				$result=$result+array("SQLError"=>$conn->error." in ".$sql);
			}
			if($alertCreation==1)
			{
				$sql = " INSERT INTO user (username, pass) VALUES ('$username', '$password')";
				if($conn->query($sql)==true)
				{
					$userCreation=1;
					//echo "4";
				}
				else
				{
					$userCreation=0;
					$result=$result+array("SQLError"=>$conn->error." in ".$sql);
				}
			}	
		}
	}
	if(($userCreation==0)||($alertCreation==0)||($sensorCreation==0)||($sensorStatusCreation==0))
	{
		if($alertCreation==1)
		{
			$sql="drop table ".$username."_alert";
			if($conn->query($sql)==true)
			{
				$alertCreation=0;
			}
		}
		if($sensorStatusCreation==1)
		{
			$sql="drop table ".$username."_sense_status";
			if($conn->query($sql)==true)
			{
				$sensorStatusCreation=0;
			}
		}
		if($sensorCreation==1)
		{
			$sql="drop table ".$username."_sensor";
			if($conn->query($sql)==true)
			{
				$sensorCreation=0;
			}
		}
	}
	$result=$result+array("SensorCreation"=>$sensorCreation);
	$result=$result+array("SensorStatusCreation"=>$sensorStatusCreation);
	$result=$result+array("AlertCreation"=>$alertCreation);
	$result=$result+array("UserCreation"=>$userCreation);
	print json_encode($result);
	$conn->close();
?>

