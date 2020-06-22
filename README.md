# PAS


PAS loads the .txt PAS database file from your SD Card and read it in order to import the data into the PAS system.
You should have SD Card in your phone before using this app.

## Guide


- Create a folder named "PAS" in your SD Card root directory
- Your database .txt file should be placed inside "PAS" older
- Create 2 folders named "MTR" and "KCR" inside "PAS" folder
- For audios inside "MTR", the file name is as follows:
    - [Category, 3 char][Id][SubId][Language].[Extension] Eg. EME01C.mp3
- For audios inside "KCR", the file name is as follows:
    - [Category]/[Id].[SubId][Language].[Extension] Eg. Emerg/01.1C.mp3
    

The database should code as follows:
````
01	Platform
02	Maximum
03	Category
04	Length
05	Is
06	8
#01	01C	壹
#01	01E	One
#01	01P	一
Platform	01.1C	#01	列車現正進入
Platform	01.2C	號月台，請勿超越黃綫
Platform	01.1E	#01	Train approaching PA: The train is approaching platform
Platform	01.2E	Please stand behind the yellow line
Platform	01.1P	#01	列車現正進入
Platform	01.2P	號月台，請不要超越黃綫
````
First part is the Category, it starts with the Id, followed by a TAB, then the Category name, maximum 8 characters

Second part is the Variable, it starts with a (#), followed by the Id and Lang, then what it represents

Third part is the PA, it starts with the Category name, followed by a TAB if the Category name less than 8 characters,

  then [Id].[SubId][Lang], then a TAB, then variable code if needed, then the (Title and)Messages. String before colon(:) will treat as Title,
  
  after colon(:) will treat as Message. If no colon(:) was found, the whole line will be the Title AND Message



## **Terms of Use**
- You are allowed to download the source code, compile and install on your own device.
- You are not allowed to redistribute any part of the code and claim that is your work.
