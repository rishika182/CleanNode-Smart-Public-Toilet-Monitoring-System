# CleanNode-Smart-Public-Toilet-Monitoring-System
CleanNode – An IoT and Machine Learning-based Smart Public Toilet Monitoring System that uses ESP32 sensors to monitor hygiene, detect anomalies, predict cleanliness levels (PCI), and automate cleaning alerts and task assignments through a web dashboard and Android app.

1. Project Overview
CleanNode is an IoT and Machine Learning-based public sanitation monitoring system designed to improve hygiene management and maintenance of public toilet facilities.
The system integrates sensor-based monitoring, an ESP32 microcontroller, cloud-based data storage, and machine learning techniques to assess sanitation conditions and support predictive maintenance.
Environmental and usage-related data collected through IoT sensors are transmitted to Firebase Realtime Database and made accessible through a web-based administrative dashboard and mobile application.
CleanNode aims to enable data-driven sanitation management, improve maintenance efficiency, and support the development of smarter and more hygienic public facilities.
2. Problem Statement
Public toilet facilities frequently face challenges related to hygiene, delayed maintenance, inefficient cleaning schedules, and the absence of real-time monitoring.
Traditional maintenance practices generally depend on manual inspections and user complaints, making it difficult to identify sanitation issues promptly.
CleanNode addresses these challenges by introducing an IoT-enabled monitoring approach that facilitates continuous data collection, cleanliness assessment, and predictive maintenance support.
3. Project Objectives
•	Develop an IoT-based system for monitoring public toilet conditions.
•	Collect real-time environmental and usage-related data using sensors.
•	Integrate sensor data with a cloud-based database.
•	Evaluate facility cleanliness using sensor readings and machine learning techniques.
•	Provide a centralized monitoring interface through a web dashboard.
•	Develop a mobile application for convenient access to facility information.
•	Support predictive maintenance and efficient sanitation management.
4. Key Features
4.1 Real-Time Monitoring
Collects and processes environmental and usage-related information through connected IoT sensors.
4.2 Cleanliness Assessment
Evaluates sanitation conditions using collected sensor readings and a composite cleanliness assessment approach.
4.3 Predictive Cleanliness Index
Uses machine learning techniques, including the Random Forest algorithm, to support cleanliness assessment and predictive analysis.
4.4 Environmental Monitoring
Monitors parameters such as temperature, humidity, air quality, and ammonia levels.
4.5 Occupancy Detection
Uses motion detection to identify restroom activity and support usage monitoring.
4.6 Water Usage Monitoring
Tracks water consumption using a water flow sensor.
4.7 Administrative Dashboard
Provides a centralized interface for viewing facility information, monitoring sensor readings, and reviewing sanitation conditions.
4.8 Mobile Application
Provides a mobile interface for accessing relevant facility information and monitoring functionalities.
4.9 Maintenance Support
Helps identify facilities that may require cleaning, inspection, or maintenance based on collected data and assessment results.
5. Technology Stack
Category	Technologies
Microcontroller	ESP32 DevKit V1
Programming Languages	Python, JavaScript, TypeScript
Web Frontend	React
Mobile Application	Android
Backend	Python, FastAPI
Database	Firebase Realtime Database
Machine Learning	Random Forest
Communication	Wi-Fi, HTTP/REST
Development Tools	Visual Studio Code, Arduino IDE
6. Hardware Components
Component	Purpose
ESP32 DevKit V1	Microcontroller and sensor data transmission
DHT22	Temperature and humidity monitoring
MQ135	Air quality monitoring
MQ137	Ammonia detection
PIR (HC-SR501)	Motion and occupancy detection
SPG-30	Gas and air quality sensing
YF-S201	Water flow measurement
Supporting Components	Power supply, resistors, and circuit connections
7. System Architecture
CleanNode follows a sensor-to-cloud architecture in which environmental and usage-related information is collected, transmitted, processed, and presented through connected applications.
Data Flow
1.	IoT sensors collect environmental and usage-related data.
2.	The ESP32 microcontroller receives sensor readings.
3.	Data is transmitted through Wi-Fi connectivity.
4.	Firebase Realtime Database stores the collected information.
5.	The backend retrieves and processes the relevant data.
6.	The web dashboard and mobile application provide access to monitoring information.
8. Project Structure
CleanNode/
│
├── admin_web-dashboard/
│   ├── backend/
│   └── frontend/
│
├── mobile_app/
│
├── images/
│
├── README.md
├── .gitignore
│
├── CleanNode_Presentation.pdf
├── CleanNode_Report_preview.pdf
├── CleanNode_IEEEPaper_Draft.pdf
│
└── Project Documentation
9. Installation and Setup
Prerequisites
•	Python 3.x
•	Node.js and npm
•	Android Studio
•	Arduino IDE
•	Firebase project configuration
•	Git
Repository Setup
Clone the repository:
git clone <repository-url>
Navigate to the project directory:
cd CleanNode-Smart-Public-Toilet-Monitoring-System
Backend Configuration
1.	Navigate to the backend directory.
2.	Create a Python virtual environment.
3.	Install the dependencies listed in requirements.txt.
4.	Configure the required Firebase environment variables.
5.	Start the backend server using the project's configured startup command.
Frontend Configuration
1.	Navigate to the frontend directory.
2.	Install the required dependencies using npm.
3.	Configure the necessary environment variables.
4.	Start the development server.
Mobile Application
Open the mobile_app directory using Android Studio and configure the required project dependencies and Firebase settings before running the application.
Note: Refer to the respective project directories for specific configuration requirements and execution instructions.
10. Security and Configuration
CleanNode uses environment variables for sensitive application configuration.
The following information should not be committed to a public repository:
•	Firebase service-account credentials.
•	Private API keys.
•	Database passwords and secrets.
•	Environment files containing sensitive configuration.
Use example configuration files to document the required variables without exposing actual credentials.
11. Future Enhancements
•	Integration with additional public toilet facilities.
•	Improved predictive cleanliness and maintenance models.
•	Automated cleaning task assignment and notifications.
•	Advanced sanitation analytics and reporting.
•	Real-time monitoring across multiple geographical locations.
•	Improved resource and water consumption management.
12. Project Information
Field	Details
Project Name	CleanNode – Smart Public Toilet Monitoring System
Project Domain	Internet of Things and Machine Learning
Institution	PES University
Project Type	Undergraduate Capstone Project
Repository	CleanNode
13. Documentation
Additional technical information, system design details, implementation documentation, and project presentation materials are available in this repository.
________________________________________
CleanNode – Towards smarter, cleaner, and more sustainable public sanitation infrastructure.

