Code for PepperRobot can be found here: https://csil-git1.cs.surrey.sfu.ca/yunlongl/empathy-analysis-with-pepper-robot-ac/-/tree/4-pepperrobot/PepperRobot/app/src/main/java/com/example/pepperrobot/MainActivity.java
- the onResults function from onCreate shows the connection to the server, sending the user's message and getting the response, and the output of the emotions and gestures

Code for the dataset (pytorch_model.bin): https://drive.google.com/file/d/1JcgXKCmXaRSScz6dKIqIhL28AnU1hoiC/view

Animation files for the gestures: https://csil-git1.cs.surrey.sfu.ca/yunlongl/empathy-analysis-with-pepper-robot-ac/-/tree/4-pepperrobot/PepperRobot/app/src/main/res/raw

# Running the code
Prerequisite : 
1. Install the Pepper SDK on Android Studio (https://developer.softbankrobotics.com/pepper-qisdk/getting-started/installing-pepper-sdk-plug)
2. Ananconda is installed
3. The dataset (pytorch_model.bin) for the NLP model has been downloaded and is in Server/Model/checkpoint

How to run the code:
1. In Android Studio, open MainActivity.java.
2. Connect the Pepper robot and tablet to your PC (Note: You must have the router you are connected to set up with WiFi)
3. Open the Anaconda Prompt
4. Navigate to the Server folder
5. Type 'conda activate uvicorn'
6. Run 'python main.py' to start the Server
7. In Android Studio, run MainActivity.java while it is connected to the Pepper robot
8. In the robot app, enter the IPv4 address of the wifi network you are connected to on your PC in the text field (that WiFi network must also be the same WiFi as the tablet's wifi). To find the IPv4 address on your PC on Windows, type 'ipconfig' in the command line and look at the IPv4 address under 'Wireless LAN adapter Wi-Fi'
9. Press the mic button from the app and say something to Pepper robot that is related to the context file provided (.../Server/context.txt)
10. Wait for Pepper to respond. If it fails, try again.

# Self Evaluation
- We did the NLP portion and the robot as promised by our proposal. We were planning to incorporate a Computer Vision model as well, but we ended up not doing that due to time constraints.


