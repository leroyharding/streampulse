# Prompt: Standalone Premium Media & IPTV Dashboard with TV D-Pad Navigation

Build a premium, single-file HTML/CSS/JavaScript standalone web application designed for Smart TVs, Firesticks, and Android TV WebViews. The UI should have a modern, dark, high-end streaming aesthetic (e.g., Netflix-style layout, smooth transitions, glassmorphism, and clear glowing focus indicators) but can lay out elements differently. The app must be fully navigable using a physical TV remote control D-pad and support direct video playback along with external player triggers.

---

## 1. Technology Stack & Core Architecture

1. **Self-Contained Frontend**:
   - Single standalone HTML file (`index.html`).
   - Use Tailwind CSS (via CDN) for styling. Do not use external CSS frameworks that require compilation.
   - Use Lucide Icons (via CDN) for clean icons.
   - Use Hls.js (via CDN) for HLS streaming (`.m3u8`) inside the standard `<video>` tag.
2. **Local Persistence**:
   - Store all configurations, favorites, search history, settings, and credentials inside `localStorage` keys prefixed to avoid collisions (e.g., `streampulse_`).
3. **Responsive TV Layout**:
   - Target a base 1080p layout. Ensure elements fit on standard screens. Use large, legible text, responsive columns, and card lists that do not wrap into multi-row structures unless explicitly paginated.

---

## 2. D-Pad TV Remote Navigation System

Implement a pure JavaScript D-pad navigation controller (`TVRemoteNav`) to support TV remote controls.

1. **Core Navigation Engine**:
   - Maintain a `currentElement` variable representing the currently focused DOM node.
   - Listen for global `keydown` events mapping physical remote buttons:
     - **ArrowUp / DPAD_UP** (Key Code 38)
     - **ArrowDown / DPAD_DOWN** (Key Code 40)
     - **ArrowLeft / DPAD_LEFT** (Key Code 37)
     - **ArrowRight / DPAD_RIGHT** (Key Code 39)
     - **Enter / DPAD_CENTER** (Key Code 13 / 83)
     - **Escape / Back** (Key Code 27 / 8)
2. **Focus Tracking & CSS Outlines**:
   - Append a `<style>` block to the document head defining a distinct focused state:
     ```css
     .tv-focused {
       outline: 4px solid #E50914 !important; /* Premium Red */
       outline-offset: 4px !important;
       transform: scale(1.05) !important;
       transition: all 0.18s cubic-bezier(0.16, 1, 0.3, 1) !important;
       box-shadow: 0 0 30px rgba(229, 9, 20, 0.8) !important;
       z-index: 50 !important;
     }
     ```
   - Ensure `setFocus(element)` removes the `.tv-focused` class from the previously focused element, adds it to the target element, and triggers `element.focus()`.
3. **Dynamic Scope Management**:
   - Implement `getCurrentScopeSelectors()` returning an array of query selector strings. The D-pad engine must restrict focus movement *only* to elements in the active scope to prevent focus traps and background navigation:
     - **Update Modal Open**: Restrict focus solely to `#update-download-btn` and the "Later" close button.
     - **Video Player Open**: Restrict focus solely to player overlay controls (play/pause, close, audio warning, external player trigger).
     - **Xtream/M3U Setup Modal Open**: Restrict focus to tab buttons, text input inputs (Server, User, Pass), and setup buttons.
     - **Details Modal Open**: Restrict focus to action buttons (Play, Trailer, List, Scrape), tab selectors, cast cards, recommendations, and episodes lists.
     - **Settings Screen Active**: Focus settings sidebar and settings form controls.
     - **Live TV Screen Active**: Focus search input, category filters, paginated channel cards, and "Load More" buttons.
     - **Home Catalog Active**: Focus navigation links, search bar, hero button actions, and movie/series grid cards.
4. **Spatial Directional Search**:
   - When a D-pad arrow key is pressed, compute the geometric distance from the center of `currentElement` to all visible, focusable elements in the current active scope selector list.
   - Filter candidate target elements based on the direction vector (e.g., if ArrowUp is pressed, target center `y` must be strictly less than current center `y`).
   - Calculate distance using a weighted formula prioritizing alignment:
     $$\text{Distance} = \Delta \text{primary\_axis}^2 + k \cdot \Delta \text{secondary\_axis}^2$$
     *(Where $k = 4$ penalizes diagonal offsets to keep navigation linear).*
   - Select the closest element and focus it.
5. **Auto-Focus Transitions**:
   - Automatically redirect focus to the primary button of any overlay (such as the Download button in the Update modal or watch button in Details) when it opens, and restore focus to safe fallback elements on close.

---

## 3. Premium Media Aggregator & Scraping Engine

1. **Multi-Scraper Parallelization**:
   - When scraping is requested, invoke 4 scraper functions in parallel using `Promise.allSettled()`:
     - **Torrentio**: Query `https://torrentio.strem.fun/providers=yts,eztg,rarbg,rarbg,glodls,kickasstorrents,torrentgalaxy/stream/{type}/{id}.json` (mapping TMDB IDs to IMDb IDs).
     - **NoTorrent**: Query your secondary Stremio scraper endpoints.
     - **StreamViX**: Query your custom stream resolver endpoint using encoded configuration options.
     - **HdHub**: Query your primary direct mirror links provider.
2. **Parsing, Scoring & Priority Sorting**:
   - Standardize all scraper responses into a unified structure:
     `{ name: string, title: string, url: string, resolution: string, audio: { type: 'hd'|'stereo', codec: string } }`
   - Classify audio codecs by parsing titles for keywords:
     - **HD Audio**: `DTS`, `TrueHD`, `Atmos`, `AC3`, `DDP`, `5.1`, `7.1`.
     - **Stereo**: `AAC`, `MP3`, `Opus`, `Stereo`, `2.0`.
   - Score each link by calculating weights:
     - **Provider Cache**: Real-Debrid Premium Cache (`rd`) = +100 weight; Direct HTTP (`hd`) = +80 weight; Free P2P/Torrent = +50 weight.
     - **Resolution**: `4K` = +30; `1080p` = +20; `720p` = +10.
   - Sort the streams list in descending order of score.
3. **Direct Streams Integration**:
   - Place a "Direct Streams" tab/button in the primary details modal button row. Clicking it must automatically slide/scroll the viewport to the active loaders and scrape results section.

---

## 4. Curated Stremio Collections Catalog

1. **Stremio Addon Catalog Parser**:
   - Dynamically fetch collections catalogs from a Vercel-hosted Stremio collections server:
     - Catalog: `https://ntl-collections-en.vercel.app/catalog/series/ntl_collections_catalog.json`
     - Metadata details: `https://ntl-collections-en.vercel.app/meta/series/{collectionId}.json`
2. **Metadata ID Resolution**:
   - When a collection item is clicked, check if it has an IMDb ID. If it is an IMDb ID, resolve the corresponding TMDB ID using TMDB `/find/{imdbId}?external_source=imdb_id` API call.
   - Open the resolved item in the standard media details view.

---

## 5. Real-Debrid OAuth Device Pairing Flow

1. **OAuth Pairing State Machine**:
   - Add a pairing block inside Settings. Clicking "Pair My Device" must fetch device code credentials from `https://api.real-debrid.com/oauth/v2/device/code`.
   - Display the pairing `user_code` and target URL (`real-debrid.com/device`) in bold letters.
   - Poll `https://api.real-debrid.com/oauth/v2/device/credentials?client_id=...&code=...` every 5 seconds.
   - Upon receiving credentials, request `oauth/v2/token` to fetch the client access token and store it.
2. **Profile Data Sync**:
   - Call `/rest/1.0/user` to fetch username, subscription tier, and subscription expiration date. Display these inside Settings.

---

## 6. Xtream Codes IPTV & VOD Engine

1. **Direct JSON API Queries**:
   - Implement an IPTV dashboard supporting two configuration modes: Custom M3U playlist URLs or direct Xtream Codes API logins (Server, User, Pass).
   - If Xtream Codes is active, query the server's native JSON endpoints to avoid loading bloated M3U files:
     - Categories: `/player_api.php?username=...&password=...&action=get_live_categories`
     - Live Streams: `/player_api.php?username=...&password=...&action=get_live_streams`
2. **CORS Proxy Fallbacks**:
   - Wrap fetch requests in a proxy fallback queue:
     1. Direct fetch.
     2. `https://api.allorigins.win/raw?url=` (Prioritized to bypass proxy filters).
     3. Alternative public CORS proxies.
3. **Paginated Channel Grid rendering**:
   - When rendering live channels, do NOT load all elements into the DOM at once (which crashes browser tabs on huge playlists).
   - Implement a page slider rendering 100 channels at a time (`pageSize = 100`).
   - Append a "Load More Channels" button below the grid that increments the page count, adding the next 100 channels. Reset the page count to 1 on category changes or search queries.

---

## 7. Playback Engine & Native Android Bridge

1. **Audio Warning Alert**:
   - If an internal web player (HTML5 `<video>`) attempts to play a stream flagged with **HD Audio** (`Atmos`, `DTS`, `AC3`), display a confirm prompt:
     *"This stream uses multi-channel HD audio which might be silent in web browsers. Would you like to play it anyway? (If sound is missing, please configure and use an External Player in Settings.)"*
2. **Exposing JS-to-Java WebView Bridge**:
   - Check if `window.LeeStreamTVBridge` or `window.LeePrimeBridge` is defined.
   - Expose the following method interfaces on the bridge:
     - `isAvailable(): Boolean`
     - `playInVLC(url, title)` -> Launches `org.videolan.vlc` with headers.
     - `playInMXPlayer(url, title)` -> Launches `com.mxtech.videoplayer.ad` or `.pro` with headers.
     - `playInJustPlayer(url, title)` -> Launches `com.brouken.player`.
     - `playInDefaultPlayer(url, title)` -> Launches system default chooser intent.
     - `openYoutubeTrailer(videoId)` -> Launches native trailer intent.
     - `downloadAndInstallAPK(apkUrl)` -> Enqueues native download manager task.
3. **Intent Injection Rules (Kotlin/Java Side)**:
   - For VLC, inject extra headers as a string array:
     `intent.putExtra("headers", new String[]{"User-Agent: Mozilla/5.0 ..."})`
   - For MX Player, inject headers as key-value string arrays:
     `intent.putExtra("headers", new String[]{"User-Agent", "Mozilla/5.0 ..."})`
   - For Just Player/System, inject a Bundle:
     `val bundle = Bundle().apply { putString("User-Agent", "Mozilla/5.0 ...") }`
     `intent.putExtra("extra_headers", bundle)`
4. **Playback Routing**:
   - If settings configuration defines an external player (VLC/MX/Just Player) and the native Android Bridge is available, call the corresponding bridge interface method.
   - Otherwise, fall back to Hls.js or direct `<video>` src playback in the web player overlay. Add error listeners to the video element to automatically clear loading screens and show recovery buttons on network or media errors.

---

## 8. App Update Checker

1. **Semantic Versioning Compare**:
   - Fetch a remote JSON descriptor (`update.json`) containing `version`, `url` (APK download path), and `changelog` fields.
   - Parse and compare using semantic rules: major, minor, patch (e.g., `1.0.2` is newer than `1.0.1`).
2. **Update Alerts**:
   - If a new version is found, show the custom alert modal. Focus D-pad navigation on the Download button. Trigger `downloadAndInstallAPK(url)` on click.
