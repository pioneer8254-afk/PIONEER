# PIONEER

PIONEER is a Persian-language Android football club management game.

## Current implementation

- Native Android application built with Kotlin and Jetpack Compose.
- Persian right-to-left interface.
- 2026/27 club catalog for all four English league tiers (92 clubs).
- One persistent active save, stored locally on the device.
- In-game day advances after each 24 hours of real elapsed time.
- League rules, double round-robin fixture generation, standings, and a first-pass match simulation engine.
- Persistent starting-XI selection with validation for eleven squad members and exactly one goalkeeper.
- Match progression engine that records a user-club league result once, applies the existing win/draw/loss economy rules, and writes the financial ledger.
- Initial transfer, player-contract, manager-salary, promotion/relegation, and FA Cup rule engines.
- GitHub Actions workflow to build and test a debug APK.

## Build

The GitHub Actions workflow builds the app-debug APK on pushes to main and on manual dispatch.

## Development status

This is an early implementation milestone, not the completed game. Verified player and manager databases, opponent squad data, the playable match flow, full lineup-to-match integration, complete transfer market, FA Cup progression, manager replacement, season awards, and full gameplay integration remain to be implemented and tested. The lineup selector and match reward engine are currently groundwork; they do not yet make a match playable.

The English league structure for 2026/27 is 20 clubs in the Premier League and 24 clubs in each of the three EFL divisions. This gives each EFL club 46 league matches.


## Premier League player data

The 2026/27 Premier League player roster and biographical fields are bundled from the public `pl-catalogue` dataset (FPL API spine with linked public sources): https://github.com/oladeeayo/pl-catalogue . The upstream dataset is offered for non-commercial use; this project must retain attribution and must not use that compilation commercially without obtaining appropriate rights. Player gameplay attributes in PIONEER are estimates generated from market-value and season-points fields; they are not official ratings. The bundled roster contains registered squad and eligible U21 entries for the 20 Premier League clubs represented by the source dataset.
