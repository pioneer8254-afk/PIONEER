# PIONEER

PIONEER is a Persian-language Android football club management game.

## Current implementation

- Native Android application built with Kotlin and Jetpack Compose.
- Persian right-to-left interface.
- Official 2026/27 Premier League club selection.
- One persistent active save, stored locally on the device.
- In-game day advances after each 24 hours of real elapsed time.
- Initial league rules and a first-pass match simulation engine.
- GitHub Actions workflow to build and test a debug APK.

## Build

The GitHub Actions workflow builds the app-debug APK on pushes to main and on manual dispatch.

## Development status

This is the first implementation milestone, not the completed game. Player and manager databases, the three lower English divisions, full fixture generation, FA Cup, transfers, contracts, sponsors, finance, tactics UI, detailed match flow, and season awards remain to be implemented and tested.

Club membership is based on the Premier League's official 2026/27 club and squad publications.
