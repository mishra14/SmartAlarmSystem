<?php
	$username=$_POST['username'];
  	$password=$_POST['password'];
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
		//print json_encode($dbresult);
	}
	$sql="select username from user where username = '$username' and pass = '$password'";
	$queryResult=$conn->query($sql);
	//echo '<br>'.$queryResult->num_rows;
	if($queryResult->num_rows > 0)
	{
		$result=$result+array("validate"=>1);
	}
	else
	{
		$result=$result+array("validate"=>0);
	}
	print json_encode($result);
	$queryResult->close();
	$conn->close();
?>
