# ELI5 build instructions

This update builds like Plant Water Tracker and goes into your existing gym-app repository.

1. Extract `Gym-Rowing-Tracker-v2.zip` on the computer.
2. If version 1 is already in GitHub, open that existing `gym-rowing-tracker` repository in GitHub Desktop. Do not create a second repository.
3. In GitHub Desktop choose **Repository > Show in Explorer**.
4. Copy everything *inside* the extracted `Gym-Rowing-Tracker` folder into the existing repository folder. Include the hidden `.github` folder, and choose **Replace** when asked.
5. Return to GitHub Desktop. Enter `Update Fit Loop to version 2` in Summary.
6. Click **Commit to main**, then **Push origin**.
7. In the repository website, open **Actions > Build Gym Rowing Tracker**.
8. Wait for the green tick. Open the run and download **Gym-Rowing-Tracker-APK** under Artifacts.
9. Extract the downloaded ZIP, transfer `app-debug.apk` to the phone inside a ZIP or by USB/Quick Share, and open it. Samsung should show **Update**.

Do not uninstall version 1 first. Installing the APK built from the same GitHub repository preserves existing weigh-ins and app data. If Samsung does not offer **Update**, stop rather than uninstalling.

## First version-2 workout

The displayed weights are intentionally conservative starting suggestions, not tested maximums. Adjust them to match the available equipment and use controlled form. After the final set, tap **Easy +**, **Right**, or **Heavy −**. Fit Loop stores the result and changes the recommendation for the next workout. Week 3 automatically displays a 10% lighter recommendation.

If Samsung asks, temporarily allow My Files to install unknown apps, then switch that permission off after installation.
