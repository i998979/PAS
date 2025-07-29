# PAS
PAS is an Android app that replicates real-life Public Address system used in a railway. By selecting the folder containing the database `.txt`, the database will be loaded, and the public address will be played with specific configurations.


## Guide
- Create a folder
- Place the database `.txt` and public address file in `.mp3` in the same directory
- The file name is as follows:
    - `[Category][Id][SubId][Language].mp3` Eg. `EME01C.mp3`
    

The database should be coded as follows:
````
01	Platform
02	Maximum
03	Category
04	Length
05	Is
06	8
#01	01C	一
#01	01E	One
#01	01P	一
Platform01.1C	#01	列車現正進入
Platform01.2C	號月台，請勿超越黃綫
Platform01.1E	#01	Train approaching PA: The train is approaching platform
Platform01.2E	Please stand behind the yellow line
Platform01.1P	#01	列車現正進入
Platform01.2P	號月台，請不要超越黃綫
LLPA	01C	乘客上車時，請小心月台與車廂間之空隙
LLPA	01E	Please mind the platform gap when boarding
LLPA	01P	乘客上車的時候，請小心月台與車廂之間的空隙
````
- First part is the Category, it starts with the ID, followed by a TAB, then the Category name, maximum 8 characters
- Second part is the Variable, it starts with a `#`, followed by the ID and Lang, then what it represents
- Third part is the PA, it starts with the Category name, followed by a TAB if the Category name is less than 8 characters,
  - Then `[Id].[SubId][Lang]`, then a TAB, then variable code if needed, then the (Title and) Message. String before `:` will be treated as Title,
  - after `:` will be treated as Message. If no `:` was found, the whole line will be the Title



## **Terms of Use**
- You are allowed to download the source code, compile and install on your own device.
- You are not allowed to redistribute any part of the code and claim that is your work.
