# ELI5 build instructions

This project builds exactly like Plant Water Tracker, but use a **new repository** so the two apps stay separate.

1. Extract `Gym-Rowing-Tracker.zip` on the computer.
2. On GitHub, create a new private repository named `gym-rowing-tracker`. Do not add starter files.
3. Open GitHub Desktop and choose **File > Clone repository**, then clone the new empty repository.
4. In GitHub Desktop choose **Repository > Show in Explorer**.
5. Copy everything *inside* the extracted `Gym-Rowing-Tracker` folder into the cloned repository folder. Include the hidden `.github` folder.
6. Return to GitHub Desktop. Enter `Add Fit Loop app` in Summary.
7. Click **Commit to main**, then **Push origin**.
8. In the repository website, open **Actions > Build Gym Rowing Tracker**.
9. Wait for the green tick. Open the run and download **Gym-Rowing-Tracker-APK** under Artifacts.
10. Extract the downloaded ZIP, transfer `app-debug.apk` to the phone inside a ZIP or by USB/Quick Share, and install it.

If Samsung asks, temporarily allow My Files to install unknown apps, then switch that permission off after installation.
