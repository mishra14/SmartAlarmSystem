<?php
	$username=$_POST["username"];
	$password=$_POST["password"];
	$senseStatus=$_POST["status"];
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
	$status=-1;
	$update=0;
	$validate=0;
	$sql="select username from user where username = '$username' and pass = '$password'";
	$queryResult=$conn->query($sql);
	//echo '<br>'.$queryResult->num_rows;
	if($queryResult->num_rows > 0)
	{
		$validate=1;
	}
	else
	{
		$validate=0;
	}
	if($validate==1)
	{
		if($senseStatus=="ON")
		{
			$light=$_POST["light"];
			$sound=$_POST["sound"];
			$motionX=$_POST["motionX"];
			$motionY=$_POST["motionY"];
			$motionZ=$_POST["motionZ"];
			$sql="update ".$username."_sense_status set status='OFF' where status='ON'";
			if($conn->query($sql)==true)
			{
				$result=$result+array("SensorUpdationOld"=>1);
				$status=0;
			}
			else
			{
				$result=$result+array("SensorUpdationOld"=>0);
				$result=$result+array("SQLError"=>$conn->error." in ".$sql);
			}
		
			$sql="insert into ".$username."_sense_status (tsStart,light, sound, motionX, motionY, motionZ, battery, status) values (NOW(),".$light.",".$sound.",".$motionX.",".$motionY.",".$motionZ.", 10, 'ON')";
			if($conn->query($sql)==true)
			{
				$result=$result+array("SensorInsertionON"=>1);
				$status=1;
				$update=1;
			}
			else
			{
				$result=$result+array("SensorInsertionON"=>0);
				$result=$result+array("SQLError"=>$conn->error." in ".$sql);
				$update=0;
			}
		}
		else
		{
			$sql="update ".$username."_sense_status set status='OFF' where status='ON'";
			if($conn->query($sql)==true)
			{
				$result=$result+array("SensorInsertionOFF"=>1);
				$status=0;
				$update=1;
			}
			else
			{
				$result=$result+array("SensorInsertionOFF"=>0);
				$update=1;
				$result=$result+array("SQLError"=>$conn->error." in ".$sql);
			}
		}
	}
	$conn->close();
	$result=$result+array("Validate"=>$validate);
	$result=$result+array("Update"=>$update);
	$result=$result+array("Status"=>$status);
	print json_encode($result);
?>
