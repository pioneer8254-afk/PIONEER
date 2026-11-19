# PIONEER

PIONEER is a Persian-language Android football club management game.

## Current implementation

- Native Android application built with Kotlin and Jetpack Compose.
- Persian right-to-left interface.
- 2026/27 club catalog for all four English league tiers (92 clubs).
- One persistent active save, stored locally on the device.
- In-game day advances after each 24 hours of real elapsed time.
- League rules, double round-robin fixture generation, standings, and a first-pass match simulation engine.
- Initial transfer, player-contract, manager-salary, promotion/relegation, and FA Cup rule engines.
- GitHub Actions workflow to build and test a debug APK.

## Build

The GitHub Actions workflow builds the app-debug APK on pushes to main and on manual dispatch.

## Development status

This is an early implementation milestone, not the completed game. Verified player and manager databases, full match and tactics screens, persistent fixtures and league results, transfers UI, sponsor contracts, detailed finance ledger, FA Cup progression, manager replacement, season awards, and full gameplay integration remain to be implemented and tested.

The English league structure for 2026/27 is 20 clubs in the Premier League and 24 clubs in each of the three EFL divisions. This gives each EFL club 46 league matches.
