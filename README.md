# TorrServe Flow

TorrServe Flow is a fork of the TorrServe MatriX Android client focused on clearer navigation and a cleaner, more standard interface. The name reflects the smoother navigation between torrents, files, playlists, and servers.

Android package: `io.github.khivus.torrserveflow`. Gradle project name: `TorrServeFlow`.

The new package installs separately from the original TorrServe app; settings and local server installations are not automatically migrated. The upstream Firebase integration has been removed. Client updates from the original app's release feed are disabled until this fork has its own feed; TorrServer updates remain available.

## What this fork changes

- Moves the main-menu button to the top-left corner and keeps it visible. The top bar displays the active page name.
- Adds explicit **Torrents**, **Servers**, and **TorrServer Settings** menu entries, replacing the hidden navigation actions on the server header.
- Opens **Playlist** as an in-app list of playable files. Playback starts only when a file or **Play All** is selected.
- Opens torrent contents as a full-screen file browser with a Back button, instead of a popup. Even single-file torrents open for browsing before playback.
- Shows the torrent name once above the file list and emphasizes distinct episode or filename labels. Separate subfolders remain visible when needed to distinguish files.
- Keeps the torrent file browser open when returning from an external player. Torrent information can be shown or hidden with the details button.
- Uses consistent menu styling, highlights the active destination, compacts torrent metadata, and keeps navigation consistent in portrait and landscape.

## AI disclosure

The fork-specific changes, including the usability improvements, app and package rename, and README updates, were implemented by **OpenAI Codex, an AI coding agent**, following the maintainer's instructions. The original TorrServe application and its upstream code were created by their respective authors.

## About TorrServe

The F-Droid link and screenshot below refer to the original TorrServe app, rather than a release of this fork.

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
     alt="Get it on F-Droid"
     height="80">](https://f-droid.org/packages/ru.yourok.torrserve/)

![torrserve](https://user-images.githubusercontent.com/53379023/140739930-4c41670f-f807-4a81-9462-e4beae8474d3.png)

Application for downloading torrents as `HTTP` files.
**If your provider blocks torrents, be careful**.
The cache is stored in `RAM`, so the internal memory is not damaged.
The application consists of two parts, a server and a client, this is a client for Android.
Server source: https://github.com/YouROK/TorrServer

On first launch, install TorrServer through **Update → Install / Update server**, or select a remote server through **Servers**.
