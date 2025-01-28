# Match It

## Overview
**Match It** is an engaging and challenging Android puzzle game where players match similar items within a time limit. Designed for players of all ages, the game offers both single and multiplayer modes, as well as global leaderboards to track progress.

## Features
- **Leaderboard**: Track and compare the highest points in each game mode globally.
- **Game Sharing**: Create and download games with friends
- **Difficulty**: Allow the user to select different change and adapting the board size
- **Double Score**: Watch a ad and double your score
- **Achievements**: Collect achievements within the game
- **Offline Sync**: Play offline and sync your achievement progress when reconnected.

## Additional Features
- **Single & Multiplayer Modes**: Play solo or challenge friends online.
- **Login & Sign-up Options**: Log in with email/password or via Google.
- **Language Support**: Available in English, Afrikaans, isZulu, Chinese and Spanish
- **Notifications**: The application sends push notification to the user after a period of inactivity
- **Customizable Settings**: Change language preferences, biometric auth,dark/light mode, and more.
- **Help Screen**: Get detailed instructions on how to play the game.


## Prerequisites
- Android Studio Arctic Fox or higher.
- Retrofit2 for API calls.
- Google AdMob account and setup
- Internet access for firebase

## Installation
1. Clone the repository to your local machine:
   ```bash
   git clone https://github.com/VCDN-2024/opsc7312-part-poe-ST10144906.git
2. Open the project in Android Studio.
3. Sync the Gradle files to ensure all dependencies are installed.
4. Build and run the project on an Android device or emulator.
   
## Match It - User Guide

### Logging In
Upon opening **Match It**, you can log in using your **email and password** or through **Google**.

   - **Email and Password**: Enter your registered email and password. If you don’t have an account, tap **Sign Up** to create one.
   - **Google Login**: Tap **Log in with Google** to authenticate and be redirected to the main menu.

### Main Menu
After logging in, you will access the main menu with the following options:

   - **Single Player**: Play solo and aim for the best time.
  - **Multiplayer**: Challenge friends or other online players.
  - **Leaderboard**: View the highest scores for each game mode.
  - **Game Share & Creation**: Create and download user-generated games.
  - **Achievements**: View achievements collected within the game
  - **Settings**: Adjust language preferences, toggle dark mode, and modify other game settings.
  - **Help**: Access instructions on gameplay.

### Game Controls
   - **Tap to Match**: Select items by tapping to create matches.
   - **Timer**: Complete matches in the fastest time possible to get the best possible score.

### Game Over Screen
At the end of each game, the game over screen allows you to:
   - View your score.
  - Return to the main menu.
  - Option to double your score by watching an ad. Integrate advertisements to support the game and double your score.
  - Option to replay the game again.

### Help Screen
   - For detailed gameplay instructions, visit the **Help** section in the app.

### Language Preference
   - **Match It** supports **English**,**Afrikaans**,**isZulu**, **Chinese** and **Spanish**. Change your language preference in the **Settings** menu. Allow users to select their preferred language from the settings.

### Leaderboard
   - The leaderboard showcases the highest points achieved in each game mode for global competition and tracking.
   - The user can filter based on the game difficulty.


### Game Share & Creation
   - **Create Games**: Customize your own game using your own images.
   - **Download Games**: Access and play games created by other users using game code.

### Settings
In the **Settings** menu, you can:
   - Change language preference.
   - Enable/disable dark mode.
   - Enable/disable notifications.
   - Enable/disable biometric authorization. Enable users to log in using fingerprint
   - Logout

### Achievements
In the **Achievements** menu, you can:
   - View the achievements you have unlocked and the achievements you still have to unlock.
   - Through the use of an offline Room database, achievements can be stored when no internet connection is available. Once the user regains an internet connection, their achievement progress will sync online.

### Notifications
   - Provide users with updates and reminders related to the game.

### Biometrics 
   - This users the built in biometrics auth the user has on their device if they have it enabled.
  - If the user fails the biometrics auth or if they biometrics reader is damaged the user cna user the lock screen pin as an alternative.

## Furture Development
- API to manage the data transfer between the application and the database, it insures the leaderboard management. (To be developed further) 

## Youtube Demo 
https://youtu.be/-6cNUjU5BEI?si=f4iwxk2SfM80cgQw

## Contribution
- ST10144906 (Darius Govender)
- ST10204523 (Jaden Peramul)
- ST10069637 (Jordan Farrell  ~ Team Leader)
- ST10108175 (Cael Botha-Richards) 






