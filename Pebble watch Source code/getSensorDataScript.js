var xhrRequest = function (url, type, callback) 						//create an XMLHttpRequest object with a url, call method and a callback function when the url responds
{
  var xhr = new XMLHttpRequest();
  xhr.onload = function () {
    callback(this.responseText);
  };
  xhr.open(type, url);
  xhr.send();						//send the xml request
};
var xhrRequestAlert = function (url, type, callback) 					//create an XMLHttpRequest object to hit the alert URL with a url, call method and a callback function when the url responds
{
  var xhrAlert = new XMLHttpRequest();
  xhrAlert.onload = function () {
    callback(this.responseText);
  };
  xhrAlert.open(type, url);
  xhrAlert.send();								//send the xml request
};
var changePercentage;								//variable to parse the chnage percentage
var symbol;								//symbol variable used to hit the market url
var dictionary;								//variable to store the dictionary
var motionAlertSent=0;
var soundAlertSent=0;
var lightAlertSent=0;
var lastAlertTimeStamp;
function sendAlert()								//function to send the alert in case the stock is up by more than 20%
{
	var url="http://stockpebble.herobo.com/TheftAlert.php?company="+symbol+"&change="+changePercentage;								//the url variable
	xhrRequestAlert(url,'GET',function(responseText)
		{
			var jsonObj=JSON.parse(responseText);
			if(jsonObj.AlertStatus==1)
			{
				console.log("Alert sent successfully!!");
			}
			else
			{
				console.log("Alert unsuccessful");
			}
		}
		);
}

function fetchSensorData()
{
	console.log("fetch data");
	var username="tess";	//TODO
	var url="http://mishra14.ddns.net/fetchDataPebble.php?username=tess";
	var light;
	var sound;
	var motionX;
	var motionY;
	var motionZ;
	var timestamp;
	var timestampAlert;
	var alert;
	var motionAlert;
	var soundAlert;
	var lightAlert;
	var status;
	var reason;
	xhrRequest(url,'GET',function(responseText)
            {
							console.log("response");
							var jsonObj=JSON.parse(responseText);
							console.log("response : "+responseText);
							if(jsonObj.Data)
								{
									light=jsonObj.Data.light;
									console.log("LIght : "+light);
									sound=jsonObj.Data.sound;
									console.log("Spund : "+sound);
									motionX=jsonObj.Data.motionX;
									console.log("X : "+motionX);
									motionY=jsonObj.Data.motionY;
									console.log("Y : "+motionY);
									motionZ=jsonObj.Data.motionZ;
									console.log("Z : "+motionZ);
									timestamp=jsonObj.Data.timestamp;
									console.log("time : "+timestamp);
									var date = new Date(timestamp.split(' ').join('T'));
									date.setHours(date.getHours()+5);
									console.log("Sensor Date : "+date+", timestamp : "+timestamp+", current date : "+new Date());
									if(((new Date().getTime()/1000)-(date.getTime()/1000))>300)
										{
											status="Offline";
										}
									else
										{
											status="Online";
										}
								}
							alert=jsonObj.Alert==1?"Yes":"No";
							if(jsonObj.Alert==1)
								{
									reason=jsonObj.AlertData.reason;
									/*var dateAlert = new Date(jsonObj.AlertData.timestampUpdate.split(' ').join('T'));
									console.log("last alert date : "+dateAlert);
									if(((new Date().getTime()/1000)-(dateAlert.getTime()/1000))<3600)
										{
											timestampAlert="Now";
										}
									else if(((new Date().getTime()/1000)-(dateAlert.getTime()/1000))<18000)
										{
											timestampAlert="Recent";
										}
									else
										{
											timestampAlert="Old"
										}*/
										timestampAlert=jsonObj.AlertData.timestampUpdate;
								}
							else
								{
									reason="N/A";
									timestampAlert="N/A";
								}
							console.log("Alert : "+alert);
								motionAlert=jsonObj.MotionAlert==1?"Yes":"No";
							console.log("motionAlert : "+motionAlert);
								soundAlert=jsonObj.SoundAlert==1?"Yes":"No";
							console.log("soundAlert : "+soundAlert);
								lightAlert=jsonObj.LightAlert==1?"Yes":"No";
							console.log("lightAlert : "+lightAlert);
								if(alert==1)
									{
										
										if(jsonObj.AlertData.timestampUpdate!=lastAlertTimeStamp)
											{
												//sendAlert();
											}
										
									}
							console.log("reason : "+reason);
                    dictionary =																	//create a dictionary
                    {
											//0: light,
											1: sound,
											2: motionX,
											3:motionY,
											4:motionZ,
											9:timestamp,
											5:alert,
											6:motionAlert,
											8:soundAlert,
											7:lightAlert,
											10:timestampAlert,
											11:status,
											0:reason
                    };
               
                    // Send to Pebble
                    Pebble.sendAppMessage(dictionary,								//send the dictionary to the pebble
                      function(e) 
                      {
                        console.log("Stock Price info sent to Pebble successfully!");
                      },
                      function(e) 
                      {
                        console.log("Error sending Stock Price info to Pebble!");
                      }
                    );
						});
	
}
Pebble.addEventListener								//add an event listener that is called when the pebble  is ready
("ready",
  function(e)
  {
      console.log('pebble app has started.....');
  }
);

Pebble.addEventListener								//add an event listener that is called when the pebble sends an app message to the phone
("appmessage",
function(e) 
 {
    console.log('message received...');
		fetchSensorData();
  }
);
