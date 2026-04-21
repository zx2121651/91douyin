# Editor Timeline Model Documentation

## Overview
The DouyinLite Editor uses a unified timeline model to manage all media tracks, including video clips, audio tracks, and overlays (text, stickers). This model serves as the "source of truth" for the UI, state management, and the export engine.

## Core Models (`lib_media`)

Defined in `com.app.douyin.pro.lib.media.model.TimelineModels.kt`.

### `EditingTimeline`
The root container for all tracks.
- `videoMainTrack`: Sequential list of `VideoClip`.
- `pipTracks`: List of `VideoClip` representing Picture-in-Picture tracks.
- `audioTracks`: List of `AudioTrack`.
- `overlays`: List of `OverlayItem` (Text, Stickers, etc.).

### `VideoClip`
- `startInSourceMs`: Start time within the source media file.
- `endInSourceMs`: End time within the source media file.
- `sourceDurationMs`: Total duration of the source file.
- `speed`: Playback speed (affects timeline duration).
- `volume`: Audio volume for this clip.

### `AudioTrack`
- `timelineStartMs`: Where the audio begins on the global timeline.
- `startInSourceMs`: Start time within the source audio file.
- `endInSourceMs`: End time within the source audio file.

## Domain Models (`feature_edit`)

Defined in `com.app.douyin.pro.feature.edit.domain.model.EditModels.kt`.

### `EditProject`
The domain representation of the timeline, used by `EditViewModel`.
- `tracks`: List of `EditTrack`.

### `EditTrack`
- `type`: `VIDEO`, `PIP`, `AUDIO`, `TEXT`, `STICKER`.
- `clips`: List of `ClipItem`.

## Data Transfer Objects (DTOs)

Used for communication between the ViewModel and the `VideoExportWorker` (serialized as JSON).

- `EditingTimelineDto`
- `VideoClipDto`
- `AudioTrackDto`
- `TextOverlayDto` / `StickerOverlayDto`

## Unit Conventions
- All time-related values are in **milliseconds (ms)**.
- All coordinates are **normalized (0.0 to 1.0)** relative to the video frame size.

## Mapping Logic
- **Initialization**: `RecordSegment` (from recording) -> `ClipItem` (domain) -> `EditProject`.
- **Export**: `EditProject` (domain) -> `EditingTimelineDto` (JSON) -> `EditingTimeline` (core) -> `VideoEditorHelper` (Media3 Transformer).
