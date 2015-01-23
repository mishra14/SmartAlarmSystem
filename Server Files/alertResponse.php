<?php
	$username=$_POST["username"];
	$password=$_POST["password"];
	$alertResponse=$_POST["alertResponse"];
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
	$error=0;
	$validate=0;
	$alertStatus=1;
	$alertChanged=0;
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
		if($alertResponse==1)
		{
			$sql="update ".$username."_alert set status='OFF' where status='ON'";		//create query
			if($conn->query($sql)==true)							//ccheck if atleast 1 row is returned
			{
				while ($row = $queryResult->fetch_assoc())				//get the value of each row one by one
				{
					$sql="SELECT * FROM ".$username."_sensor ORDER BY id DESC LIMIT 1";		//create query
					$queryResult=$conn->query($sql);						
					if($queryResult->num_rows > 0)					//ccheck if atleast 1 row is returned
					{
						while ($row = $queryResult->fetch_assoc())		//get the value of each row one by one
						{
							$light=$row["light"];
							$sound=$row["sound"];
							$motionX=$row["motionX"];
							$motionY=$row["motionY"];
							$motionZ=$row["motionZ"];
							$battery=$row["battery"];
						}
						$error=0;
					}
					else
					{
						$error=1;			//return this if nothing is obtained from the query
					}
				}
				$alertChanged=1;
				$alertStatus=0;	
			}
			else
			{
				$alertChanged=0;
				$alertStatus=1;	
			}
			if(($error==0)&&($alertChanged==1))
			{
				$sql="select * from ".$username."_sense_status where status='ON'";
				$queryResult=$conn->query($sql);
				if($queryResult->num_rows>0)
				{
					if($alertChanged==1)
					{
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
		
						$sql="insert into ".$username."_sense_status (tsStart,light, sound, motionX, motionY, motionZ, status, battery) values (NOW(),".$light.",".$sound.",".$motionX.",".$motionY.",".$motionZ.", 'ON', 10)";
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
				}
			}
		}
		else
		{

		}
	}
	$conn->close();
	$result=$result+array("AlertStatus"=>$alertStatus);
	$result=$result+array("AlertChanged"=>$alertChanged);
	$result=$result+array("Validate"=>$validate);
	$result=$result+array("Error"=>$error);
	$result=$result+array("Update"=>$update);
	$result=$result+array("Status"=>$status);
	print json_encode($result);
?>
