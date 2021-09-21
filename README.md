# RunDo
I have developed app called "RunDo". This app tracks user physical activities(running, cycling, walking and so on) by saving geoposition, distance and time of activity. User can configure notifications to start physical activity. Data storing in SQLite database and server. Every user has his personal account and can log in and log out of it. Also user can view on map his activity.
I have developed app called "RunDo". This app tracks user’s physical activities(running, cycling, walking and so on) by saving geoposition, distance and time of activity. Users can configure notifications to start physical activity. Data storing in SQLite database and server. Every user has his personal account and can log in and log out. Also users can view his activity on map.
- SQLite database to local data storage.
-	API to save and get data about activity, login and registration.
-	Retrofit and Gson for work with API.
-	Bolts for multithreading.
-	Service for location tracking using LocationListener.
-	Broadcast receiver for getting results from service and check is gps enabled. If gps is disabled there is function of pause user’s activity.
-	If the Internet is enabled, the track will be saved to the database, otherwise it will be sent to the server and saved there
-	Creating, changing and deleting notification using alarm manager and pending intent.
-	Device rotating handling.
-	Splash screen to fetch data

# Tools
-	Android
-	Kotlin
-	Retrofit
-	Gson
-	Bolts
-	LocationListener
-	Google Maps API
-	SQLite
