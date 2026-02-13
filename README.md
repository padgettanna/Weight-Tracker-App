# Weight Tracker Android App

A mobile application that enables users to track weight goals, analyze historical data, and generate trend-based insights.

Originally developed for CS 360 (Mobile Architecture & Programming), this project was later enhanced during my Computer Science Capstone to incorporate structured data handling and algorithmic analysis.

## Features
- Set and update personal weight goals 
- Log daily weight entries  
- Edit and delete previous entries
- Chronological weight history log
- Local SQLite persistence
- Clean, mobile-optimized UI

## Capstone Enhancements (Algorithms & Data Structures)
This enhancement extended the app beyond simple data storage by introducing structured data processing and algorithmic insight generation:
- Refactored database queries to return structured collections instead of raw values
- Implemented chronological sorting logic prior to analysis
- Designed and implemented a 7-day rolling average algorithm
- Built trend detection logic based on changes in rolling averages
- Displayed calculated trend indicators (Upward / Downward) on dashboard
These changes transformed the app from a basic tracking tool into a data-driven progress analysis system.

## Tech Stack
- **Platform:** Android  
- **Language:** Java  
- **Database:** SQLite
- **Architecture Concepts:** Structured collections, sorting algorithms, rolling averages, trend analysis

## Installation
1. Clone the repository
2. Open the project in Android Studio
3. Build and run the app on an emulator or Android device

## Screenshots
<p align="center">
  <img src="screenshots/dashboard.png" width="200"/>
  <img src="screenshots/log.png" width="200"/>
  <img src="screenshots/update.png" width="200"/>
  <img src="screenshots/update-date.png" width="200"/>
</p>

