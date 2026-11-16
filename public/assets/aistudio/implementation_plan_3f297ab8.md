# Nitro Rush — Native Android Motorbike Highway Racer

Nitro Rush is a native Kotlin and Jetpack Compose pseudo-3D motorbike highway racer featuring real-world motorcycle models priced at their actual USD MSRPs, real-time GPS location via Android location services, live OpenStreetMap highway routes and mini-map radar, procedural audio synthesis, and offline persistence.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> This revised plan incorporates your new requirements for real-world USD bike rewards, real-time GPS location, and OpenStreetMap integration alongside all previously confirmed Phase 1 choices.

- **Confirmed Decision 1 — Real-World Bike Garage & USD Rewards**: Players start with a real entry sportbike and earn virtual USD prize payouts from every race (scaled by distance, near-miss combos, checkpoints, and difficulty) to purchase and equip iconic real-world motorcycles priced at their actual US MSRP.
- **Confirmed Decision 2 — Real-Time GPS & OpenStreetMap Routes**: Using `FusedLocationProviderClient` and OpenStreetMap (`osmdroid` + Overpass API, zero API keys required), the app locates the player in real time, discovers nearby real highways, converts real road geometry into playable 3D racing curves, and displays a live OpenStreetMap mini-map during runs.
- **Confirmed Decision 3 — Dual Steering & Adaptive Orientation**: Touch pads (`LEFT`, `BRAKE`, `NITRO`, `RIGHT`) and an orientation-aware Tilt Steering toggle remain active across both Portrait and Landscape layouts.
- **Confirmed Decision 4 — Offline Storage & Graceful Fallback**: Wallet balance, owned bikes, run history, and preferences persist locally via Room and DataStore; if GPS is denied or offline, curated real-world scenic highway GPS presets ensure uninterrupted play.

---

## 1. Overview & Core Concept

- **What It Does**: A high-speed 60 FPS arcade highway racer where real-world geography meets pseudo-3D outrun-style racing. Players locate nearby real highways on an interactive OpenStreetMap view, race through 3 lanes of dynamic traffic across *Sunset Highway*, *Neon Night*, and *Storm Run* stages, earn virtual USD rewards, and unlock faster real-world bikes in the Garage.
- **Real-World Motorbike Roster (Verified US Base MSRPs)**:
  1. **Kawasaki Ninja 500** — **$5,499 USD** *(Starter Bike · Owned)* — Balanced 451cc handling, 1.00× top speed baseline.
  2. **Honda CBR650R** — **$9,199 USD** — Smooth inline-4 acceleration and higher top-end pull.
  3. **Yamaha YZF-R7** — **$9,399 USD** — Razor cornering grip and rapid Nitro recharge efficiency.
  4. **BMW S 1000 RR** — **$18,995 USD** — Litre-class superbike with blistering top speed and sharp 6-gear response.
  5. **Suzuki Hayabusa** — **$19,499 USD** — Hyperbike velocity champion with extended Nitro reserve.
  6. **Ducati Panigale V4 S** — **$33,510 USD** — Flagship track weapon delivering peak speed, lean agility, and maximum race payout bonus.
- **Core Gameplay & Economy Loop**:
  1. **Locate & Select Route**: Acquire live GPS coordinates to pull nearby highways (e.g., local interstates/arterials) onto the OpenStreetMap viewer, or choose a built-in real-world highway coordinate preset.
  2. **Garage & Equip**: Spend earned virtual USD to buy new real-world bikes, inspect their USD price tag and performance bars, and preview their custom livery in the 3D bike sprite.
  3. **Race & Earn USD**: Beat the checkpoint clock, chain up to `×8` near-miss combos to charge Nitro, survive 3 lives, and cash out race earnings in USD (`+$`) directly to your local garage wallet.

---

## 2. User Experience & Visual Design

### Key User Flows

```
┌──────────────────┐   TAP TO RIDE    ┌─────────────────┐   3-2-1-GO   ┌──────────────────┐
│    Main Menu     │ ───────────────► │    Countdown    │ ───────────► │   Active Race    │
│ • Live Highway   │                  │  (Engine Rev)   │              │ • 3D Highway     │
│ • Wallet ($ USD) │                  └─────────────────┘              │ • Live OSM Radar │
│ • Route Badge    │                                                   │ • Multi-Touch +  │
└───┬──────────┬───┘                                                   │   Tilt Controls  │
    │          │                                                       └────────┬─────────┘
    │          └─────────► ┌───────────────────────────┐                        │
    │                      │   GPS & Real Map Sheet    │                        │ WRECK /
    ▼                      │ • Live GPS Pin & Recenter │                        │ TIME UP
┌────────────────────┐     │ • OSM Street Map View     │                        ▼
│  Bike Garage ($)   │     │ • Nearby Highway Selector │               ┌──────────────────┐
│ • 6 Real Models    │     └───────────────────────────┘               │   Run Over ($)   │
│ • Actual USD MSRP  │                                                 │ • USD Earned     │
│ • Buy / Equip      │                                                 │ • Stats & Retry  │
└────────────────────┘                                                 └──────────────────┘
```

1. **Main Menu & Wallet Header**: Displays the glowing **NITRO RUSH** title, current **USD Wallet Balance** pill, currently equipped bike badge, active real-world highway route pill (tap to open the **GPS & Live Map Sheet**), difficulty selector (`CHILL` / `PRO` / `INSANE`), **Tilt Steering** toggle, **Garage** button, and **Best Runs** history button.
2. **GPS & Real Map Sheet**:
   - Requests foreground location (`ACCESS_COARSE_LOCATION` / `ACCESS_FINE_LOCATION`) only when the player activates GPS discovery, with clear status feedback.
   - Displays a dark-themed interactive **OpenStreetMap** canvas centered on the player's real-time GPS position, highlighting discovered nearby highways in neon cyan/pink. Tapping any nearby highway converts its real lat/lon curves into the race track's curvature profile.
3. **Garage Sheet**:
   - Clean horizontal or adaptive two-column card showcase showing each bike's brand/model, verified **$ USD** price tag, custom painted silhouette, 4 stat meters (`Top Speed`, `Acceleration`, `Corner Grip`, `Nitro Boost`), and a `BUY $X,XXX` / `EQUIP` / `EQUIPPED` action button.
4. **Adaptive Racing HUD & Live Mini-Map**:
   - *Portrait*: Top HUD shows Score + Lives, glowing Countdown Timer, Distance + Best; a compact circular/rounded **Live OSM Mini-Map Radar** sits cleanly in the upper-right below the pause/mute buttons, rendering the real road polyline, live virtual bike position, and real GPS lock indicator without blocking traffic sightlines.
   - *Landscape*: Wide highway viewport with top-strip HUD, mini-map tucked in the upper-left/right safe area, and ergonomic thumb pads docked on left and right edges.

### Visual Identity & Theme

- **Aesthetic Direction**: Midnight Neon Synth-Racer (`#0B0F1A` obsidian base, `#FF2D78` electric pink, `#22E6FF` boost cyan, `#7EF07E` USD cash green, `#FFD34D` warning amber).
- **Stage Color Transitions**: Smooth cross-fades every 1,500 m between *Sunset Highway*, *Neon Night* (stars, moon, lit skyline, headlight cone), and *Storm Run* (angled rain, lightning flashes, thunder, reduced wet grip).
- **Typography**: Bundled local Google Fonts via `font-util`—bold racing display (`Orbitron`) for titles and bike names, tabular monospace (`JetBrains Mono`) for USD prices, GPS coordinates, timers, and speedometers, and `Plus Jakarta Sans` for UI copy.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1 — OpenStreetMap (`osmdroid`) + Overpass API vs. Google Maps SDK**
  - *Chosen Approach*: Use `osmdroid` (`6.1.20`) with a dark tile color-filter matrix for real-time map rendering and query OpenStreetMap's public **Overpass API** via Retrofit/OkHttp for nearby road waypoints (`motorway`, `trunk`, `primary`, `secondary`). Cache tile storage strictly inside app-private `filesDir`/`cacheDir` so zero storage permissions are needed.
  - *Why*: Works out of the box with zero proprietary API keys, supports real-time GPS centering and polyline overlays, and exposes raw road coordinate nodes needed to mathematically derive road turn curvature for the 3D racing engine.
  - *Alternatives Considered*: Google Maps SDK—requires user-supplied API keys and lacks direct vector road geometry export for procedural track curvature generation.

- **Decision 2 — Virtual USD Economy Tied to Real-World MSRP Values**
  - *Chosen Approach*: Prices reflect real 2025–2026 US base MSRPs (`$5,499` to `$33,510`), and race payouts are tuned (`~$800–$3,500` per solid run depending on distance, near misses, and difficulty) so players can unlock upgrades within a few exciting runs without tedious grinding.
  - *Why*: Keeps prices authentic to real motorcycles while making progression genuinely rewarding in arcade sessions.

- **Decision 3 — Foreground-Only Real-Time GPS & Safe Track Mapping**
  - *Chosen Approach*: Use `FusedLocationProviderClient` with foreground location permissions only (`ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`) to live-update the player's real-world coordinates on the map and fetch nearby highways; fall back seamlessly to built-in real-world GPS road coordinates if indoors/offline.
  - *Why*: Adheres strictly to Google Play privacy rules (no background location, no external storage permissions) while guaranteeing the game remains playable anywhere.

---

## 4. Technical Architecture & Data Strategy

### Architecture & Component Diagram

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                                MainActivity                                  │
│     (Edge-to-Edge, Foreground Location Launcher, Lifecycle Audio & Tilt)     │
└───────────────────────────────────┬──────────────────────────────────────────┘
                                    │
                                    ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                             NitroRushViewModel                               │
│  ┌─────────────────────┐  ┌────────────────────┐  ┌───────────────────────┐  │
│  │  GameEngine (60Hz)  │  │ RealWorldMapEngine │  │   SynthAudioEngine    │  │
│  │  • Bike Physics     │◄─┤ • GPS Tracker      │  │   • RPM Sawtooth/Sq   │  │
│  │  • Near-Miss & Cash │  │ • Overpass Client  │  │   • Wind/Skid/Nitro   │  │
│  │  • Dynamic Curvature│  │ • Bearing → Curve  │  │   • Cash/Crash/Storm  │  │
│  └──────────┬──────────┘  └─────────┬──────────┘  └───────────────────────┘  │
│             │ Frame + Mini-Map      │ Real Route Waypoints                   │
└─────────────┼───────────────────────┼────────────────────────────────────────┘
              │                       │                     ▲
              ▼                       ▼                     │ Persist / Load
┌───────────────────────────────────────┐   ┌───────────────┴──────────────────┐
│          Jetpack Compose UI           │   │        OfflineRepository         │
│  • HighwayCanvas + Live Mini-Map HUD  │   │  ┌────────────┐  ┌────────────┐  │
│  • OsmMapView (osmdroid AndroidView)  │   │  │ DataStore  │  │  Room DB   │  │
│  • GarageSheet (USD Bike Shop)        │   │  │ • Wallet $ │  │ • RunLog   │  │
│  • Adaptive Controls & Overlays       │   │  │ • OwnedIds │  │ • Cached   │  │
└───────────────────────────────────────┘   └──┴────────────┴──┴────────────┴──┘
                                                                 OSM Routes
```

### Data Model & State

- **`BikeModel` (Catalog)**: `id`, `manufacturer`, `modelName`, `year`, `msrpUsd`, `displacementCc`, `topSpeedMultiplier`, `accelMultiplier`, `gripMultiplier`, `nitroMultiplier`, `payoutBonus`, `primaryColor`, `accentColor`.
- **`RealRoadTrack` & `GeoWaypoint`**: `id`, `roadName`, `cityOrRegion`, `waypoints` (`lat`, `lon`), `totalLengthMeters`, `curvatureProfile` (computed from successive waypoint bearing deltas `Δθ` mapped into `TrackSegment.curve`), and `isGpsDerived`.
- **`GpsLocationState`**: `permissionGranted`, `latitude`, `longitude`, `accuracyMeters`, `liveGpsActive`, `nearbyRoutes`, `isFetchingRoads`, `statusMessage`.
- **`GarageState` & `RunRecord` (Room + DataStore)**: `usdBalance`, `ownedBikeIds`, `equippedBikeId`, plus Room-persisted `RunRecord` entries including `bikeModelName` and `usdEarned`.

### Interactive Component & State Mapping

- **Real-Time GPS & Route Pipeline**:
  1. Tapping **GPS MAP** requests `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` if not yet granted, starts `FusedLocationProviderClient` real-time updates, and centers the `osmdroid` `MapView` on the live GPS dot.
  2. `OverpassApiService` (Retrofit + OkHttp) fetches highways within a ~4 km radius around the live GPS point, computes road segment bearings (`atan2`), maps sharp/gentle turns into `TrackSegment` curves, and caches the route locally in Room so it stays playable offline.
  3. During racing, the **Live Mini-Map HUD** draws the scaled road polyline, pulses the player's current virtual distance along the real road shape, and displays the active road name & live GPS indicator.
- **Bike Purchase & Reward Payout**:
  1. Ending any run calculates `usdEarned` from distance, score, near-misses, checkpoints, difficulty multiplier, and the equipped bike's payout bonus, persisting the new `usdBalance` immediately.
  2. In the **Garage Sheet**, tapping `BUY` on an affordable bike deducts `msrpUsd` from `usdBalance`, adds the bike ID to `ownedBikeIds`, equips it, and immediately updates the 3D rider bike livery and physics multipliers.
