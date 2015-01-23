#include<pebble.h>						//Include the pebble header file
#define LIGHT 0
#define SOUND 1
#define MOTIONX 2
#define MOTIONY 3
#define MOTIONZ 4	
#define ALERT 5
#define MOTION_ALERT 6
#define LIGHT_ALERT 7
#define SOUND_ALERT 8
#define TIMESTAMP 9
#define TIMESTAMP_ALERT 10
#define STATUS 11
#define REASON 12
	
Window *window;																	//Define the layer and window handlers
TextLayer *alertLayer, *statusLayer, *reasonLayer, *timeLayer;
BitmapLayer *bitmapLayer;
GBitmap *bitMap;
DictionaryIterator *iterator;						//define the iterator used to read the dictionary
int symbolCount, first;
int dropCount=0;
char *symbolTable[]={"LINE","LAYN","AAPL","FNFG"};						//The symbol table that points to 4 different symbols
static void updateStock()
{
    DictionaryIterator *iterator;
    app_message_outbox_begin(&iterator);										//create a message to send to the phone
    Tuplet value = TupletCString(0, symbolTable[symbolCount]);
    dict_write_tuplet(iterator, &value);														//write value into the dictionary
    app_message_outbox_send();				// a call will be made to out_sent_handler or out_failed_handler after the message is sent to the phone  
}
static void tickHandler(struct tm *tick_time, TimeUnits units_changed)								//tick handler which is called after every second's tick
{
	if(first==0)														//If the tick is called for the first time, then update the stock
	{
		updateStock();
		symbolCount=(symbolCount==2)?(0):(symbolCount+1);
		first=1;														//Update the flag to indicate that the first call has been made
	}
  if((tick_time->tm_sec%10==0))										//conditional check to ensure that the stock is refreshed every 10 seconds
  {
		updateStock();
		symbolCount=(symbolCount==2)?(0):(symbolCount+1);						//update the symbol count to point to the next symbol
  }
}

//weather app message calls

void out_sent_handler(DictionaryIterator *sentIterator, void *context)						//called when the message to the phone is successful and an ACK is received
{
	APP_LOG(APP_LOG_LEVEL_INFO,"outbox message sent ");
	dropCount=0;
}

void out_failed_handler(DictionaryIterator *failedIterator, AppMessageResult reason, void *context)						//called when the message to phone is not delivered and a NACK is received
{
	APP_LOG(APP_LOG_LEVEL_ERROR,"outbox message failed ..");		//update the app log
	dropCount++;
	if(dropCount>2)
	{
		text_layer_set_text(statusLayer,"Phone Connection");			//Display an error message
  	text_layer_set_text(alertLayer,"Lost");						//Display an error message
	}
}

void in_received_handler(DictionaryIterator *receivedIterator, void *context)
{
  APP_LOG(APP_LOG_LEVEL_INFO,"inbox received handler");
	static char status[10];
	static char statusBuffer[20];
	/*static char motionX[10];
	static char motionXBuffer[20];
  static char motionY[10];
	static char motionYBuffer[20];
	static char motionZ[10];
	static char motionBuffer[20];
	static char light[10];
	static char lightBuffer[20];
	static char sound[10];
	static char soundBuffer[20];*/
	static char timestamp[20];
	static char alertBuffer[10];
	static char alertDisplayBuffer[20];
	static char reasonBuffer[20];
	static char reasonDisplayBuffer[40];
	static char timeBuffer[20];
	static char timeDisplayBuffer[30];
  Tuple *lightTuple = dict_find(receivedIterator, LIGHT);						//Search for the Symbol Key tuple
	Tuple *timestampAlertTuple = dict_find(receivedIterator, TIMESTAMP_ALERT);						//Search for the Symbol Key tuple
	Tuple *statusTuple = dict_find(receivedIterator, STATUS);						//Search for the Symbol Key tuple
  Tuple *soundTuple = dict_find(receivedIterator, SOUND);						//Search for the Fetch Key tuple
  Tuple *motionXTuple = dict_find(receivedIterator, MOTIONX);						//Search for the Price Key tuple
	Tuple *motionYTuple = dict_find(receivedIterator, MOTIONY);						//Search for the Change Key tuple
	Tuple *motionZTuple = dict_find(receivedIterator, MOTIONZ);						//Search for the Alert Key tuple
	Tuple *alertTuple = dict_find(receivedIterator, ALERT);						//Search for the Symbol Key tuple
  Tuple *timeTuple = dict_find(receivedIterator, TIMESTAMP_ALERT);						//Search for the Fetch Key tuple
  Tuple *motionAlertTuple = dict_find(receivedIterator, MOTION_ALERT);						//Search for the Price Key tuple
	Tuple *soundAlertTuple = dict_find(receivedIterator, SOUND_ALERT);						//Search for the Change Key tuple
	Tuple *lightAlertTuple = dict_find(receivedIterator, LIGHT_ALERT);						//Search for the Alert Key tuple
	Tuple *reasonTuple = dict_find(receivedIterator, 0);
	if(lightTuple!=NULL)
	{
		 // APP_LOG(APP_LOG_LEVEL_INFO,"inside");
			strcpy(status,statusTuple->value->cstring);						//copy the tuple value into the symbol buffer
			snprintf(statusBuffer,sizeof(statusBuffer),"Sensor : %s ",status);
			text_layer_set_text(statusLayer,statusBuffer);						//display the symbol
		
			strcpy(alertBuffer,alertTuple->value->cstring);						//copy the tuple value into the symbol buffer)
			snprintf(alertDisplayBuffer,sizeof(alertDisplayBuffer),"Alert : %s ",alertBuffer);
			text_layer_set_text(alertLayer,alertDisplayBuffer);						//display the symbol
		
			strcpy(reasonBuffer,reasonTuple->value->cstring);						//copy the tuple value into the symbol buffer)
			snprintf(reasonDisplayBuffer,sizeof(reasonDisplayBuffer),"Alert Type : \n%s ",reasonBuffer);
			text_layer_set_text(reasonLayer,reasonDisplayBuffer);						//display the symbol
			
			strcpy(timeBuffer,timeTuple->value->cstring);						//copy the tuple value into the symbol buffer)
			snprintf(timeDisplayBuffer,sizeof(timeDisplayBuffer),"Last Alert :\n%s ",timeBuffer);
			text_layer_set_text(timeLayer,timeDisplayBuffer);						//display the symbol
			
	}
}

void in_failed_handler(AppMessageResult reason, void *context)						//handler for an incoming message drop
{
	APP_LOG(APP_LOG_LEVEL_ERROR,"inbox message dropped!!");
}

static void windowLoad(Window *window)						//intializing the pebble window function
{
  Layer *windowLayer=window_get_root_layer(window);						//get the root window layer
  GRect bounds=layer_get_frame(windowLayer);						//get the bounds of the window layer
  
  bitmapLayer=bitmap_layer_create((GRect){.origin={0,0},.size={bounds.size.w,bounds.size.h}});						//create a blank bitmapLayer to display a blank image if needed
  bitmap_layer_set_background_color(bitmapLayer, GColorWhite);						//set the background colour as white
  //bitmap_layer_set_bitmap(bitmapLayer,bitMap);
  layer_add_child(windowLayer,bitmap_layer_get_layer(bitmapLayer));						//add the clear image onto the window

	statusLayer=text_layer_create((GRect){.origin={0,0},.size={bounds.size.w,bounds.size.h/4}});						//create a symbol layer to display the company symbol
  text_layer_set_text(statusLayer, "Sensor : ...");						//Set the company display
	text_layer_set_text_color(statusLayer,GColorBlack);
	text_layer_set_text_alignment(statusLayer,GTextAlignmentCenter);
	text_layer_set_font(statusLayer,fonts_get_system_font(FONT_KEY_GOTHIC_24_BOLD));
	layer_add_child(windowLayer,text_layer_get_layer(statusLayer));						//add the text to the window
	
  alertLayer=text_layer_create((GRect){.origin={0,bounds.size.h/4},.size={bounds.size.w,bounds.size.h/4}});						//create a layer to display theprices
  text_layer_set_text(alertLayer, "Alert : ...");						//set the initial text for the layer
  text_layer_set_text_color(alertLayer,GColorBlack);
  //text_layer_set_background_color(priceLayer,GColorBlack);
  text_layer_set_text_alignment(alertLayer,GTextAlignmentCenter);
  text_layer_set_font(alertLayer,fonts_get_system_font(FONT_KEY_GOTHIC_24_BOLD));
  layer_add_child(windowLayer,text_layer_get_layer(alertLayer));						//add the text to the window
  
  reasonLayer=text_layer_create((GRect){.origin={0,(2*bounds.size.h)/4},.size={bounds.size.w,bounds.size.h/4}});						//create a text layer to display the BUY/SELL alerts
  text_layer_set_text(reasonLayer,"Type : ...");							//set the initial text for the layer
  text_layer_set_text_color(reasonLayer,GColorBlack);
  text_layer_set_text_alignment(reasonLayer,GTextAlignmentCenter);
  //text_layer_set_background_color(priceStatusLayer,GColorClear);
	text_layer_set_font(reasonLayer,fonts_get_system_font(FONT_KEY_GOTHIC_14_BOLD));
  layer_add_child(windowLayer,text_layer_get_layer(reasonLayer));
	
	timeLayer=text_layer_create((GRect){.origin={0,(3*bounds.size.h)/4},.size={bounds.size.w,bounds.size.h/4}});						//create a text layer to display the BUY/SELL alerts
  text_layer_set_text(timeLayer,"Time : ...");							//set the initial text for the layer
  text_layer_set_text_color(timeLayer,GColorBlack);
  text_layer_set_text_alignment(timeLayer,GTextAlignmentCenter);
  //text_layer_set_background_color(priceStatusLayer,GColorClear);
	text_layer_set_font(timeLayer,fonts_get_system_font(FONT_KEY_GOTHIC_14_BOLD));
  layer_add_child(windowLayer,text_layer_get_layer(timeLayer));
}
static void windowUnload(Window *window)						//function to destroy any created resources
{
	text_layer_destroy(statusLayer);						//destroy the text layers
	text_layer_destroy(reasonLayer);
	text_layer_destroy(timeLayer);
		text_layer_destroy(alertLayer);

      //gbitmap_destroy(bitMapStatusHappy);						//destroy the bit maps
      //gbitmap_destroy(bitMapStatusSad);
     // bitmap_layer_destroy(bitmapLayerStatusHappy);						//destroy the bitmaplayers
     // bitmap_layer_destroy(bitmapLayerStatusSad);
      bitmap_layer_destroy(bitmapLayer);
}
void init()						//function to intialize the system
{
  window=window_create();						//reate a window which will contain the whole app
  window_set_window_handlers(window,(WindowHandlers){.load=windowLoad,.unload=windowUnload,});						//define the window handlers
  tick_timer_service_subscribe(SECOND_UNIT,tickHandler);						//register for a time tick event for each second
  window_stack_push(window,true);						//push the main window onto the window stack of the pebble
  app_message_register_inbox_received(in_received_handler);						//register the handler functions for App Messages
  app_message_register_inbox_dropped(in_failed_handler);
  app_message_register_outbox_sent(out_sent_handler);
  app_message_register_outbox_failed(out_failed_handler);
  app_message_open(app_message_inbox_size_maximum(), app_message_outbox_size_maximum());						//start the app messages with the maximum buffers
}
void deinit()						//function to destroy the window an unsubscribe from the tick event service
{
      tick_timer_service_unsubscribe();						//turn off the tick events
      window_destroy(window);						//destroy the main window
}
int main()
{
    init();						//initialize the system
    app_event_loop();						//create a loop for events
    deinit();						//clear the window and the event service
	return 0;
}
