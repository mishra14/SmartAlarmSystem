<?php
	$conn =new mysqli("localhost","root","mishra2014","smartsensor");
	// Check connection
	if ($conn->connect_error) 
	{
		$dbresult=array('DB Connection',0);
		print json_encode($dbresult);
		die("Connection failed: " . $conn->connect_error);
	} 
	else
	{
		$dbresult=array('DB Connection',1);
		print json_encode($dbresult);
	}
	echo '<br>';
	$sql="select username, pass from user";// where username = $username";
	$queryResult=$conn->query($sql);
	//echo '<br>'.$queryResult->num_rows;
	if($queryResult->num_rows > 0)
	{
		while ($row = $queryResult->fetch_assoc())
		{
			echo '<br>'."Username : ".$row["username"]."             Password : ".$row["pass"];
		}
	}
	else
	{
		$result=array('no user',0);
	}
	$queryResult->close();
	$conn->close();
?>
