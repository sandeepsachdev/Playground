# Sydney TV Guide

A small Spring Boot application that serves a free-to-air TV guide for Sydney
and surfaces prime-time (6pm – 10pm) highlights for each night of the week.

The app has no bundled schedule: if no live EPG feed is configured, the UI
shows an explicit empty state. Plug in an XMLTV URL to get real listings.

## Coverage

Five networks, as broadcast in the Sydney market:

| Channel | Network        |
|---------|----------------|
| 2       | ABC TV         |
| 3       | SBS            |
| 7       | Seven          |
| 9       | Nine           |
| 10      | Network 10     |

## Running

```bash
./mvnw spring-boot:run
# or
mvn spring-boot:run
```

Then open <http://localhost:8080/>. Until you configure `tvguide.epg.xmltv-url`
(see below) the UI will render its empty state.

## Endpoints

The guide is keyed on ISO dates (`yyyy-MM-dd`) so you can request any day the
upstream feed provides.

- `GET /` – Web UI for today in Sydney.
- `GET /?date=2026-04-20` – Web UI for a specific date.
- `GET /api/guide/today` – JSON guide for today.
- `GET /api/guide/week` – JSON guide for today + the next 6 days.
- `GET /api/guide/date/{yyyy-MM-dd}` – JSON guide for a specific date.
- `GET /api/guide/highlights/{yyyy-MM-dd}` – Just the highlights.

## Highlights

The `HighlightsService` scores every program overlapping the 6pm–10pm window
using a transparent, editable rubric (premium genres, flagship Australian
titles, live sport, movie premieres) and returns up to five picks ordered by
start time, each annotated with a short reason to watch.

## Live data sources

The app reads any XMLTV-formatted feed. XMLTV is the standard interchange
format used by almost every EPG provider.

| Source | URL pattern | Notes |
|--------|-------------|-------|
| [iptv-org EPG](https://github.com/iptv-org/epg) | `https://iptv-org.github.io/epg/guides/au/freeview.com.au.epg.xml.gz` | Community-scraped, free, daily refresh |
| [IceTV](https://www.icetv.com.au/) | Your API endpoint | Commercial, paid, high-quality |
| [OzTivo](https://www.oztivo.net/) | Rotating mirror URLs | Community, XMLTV format |
| Self-hosted `xmltv` grabber | Any URL you expose | Run `tv_grab_au_*` locally |

Configure via `application.properties`:

```properties
tvguide.epg.xmltv-url=https://iptv-org.github.io/epg/guides/au/freeview.com.au.epg.xml.gz
tvguide.epg.refresh-interval=PT6H
tvguide.epg.channels[0].xmltv-id=abc1.abc.net.au
tvguide.epg.channels[0].number=2
tvguide.epg.channels[0].name=ABC TV
tvguide.epg.channels[0].network=ABC
# ... repeat for SBS, Seven, Nine, 10
```

`EpgService` caches the parsed schedule for `refresh-interval`. If a refresh
fails it keeps serving the previous cache; if nothing has ever loaded
successfully, the guide renders empty.

## Deploying to Render

The repo ships with a multi-stage `Dockerfile` and a `render.yaml` blueprint.

1. Push the branch to GitHub.
2. In Render, choose **New +** → **Blueprint** and point at this repo.
   Render will read `render.yaml` and provision a Docker web service.
3. Render injects a `PORT` env var; the container's entrypoint binds Spring
   Boot to it via `-Dserver.port=${PORT}`, so no extra config is required.
4. Health checks hit `/api/guide/today`.
5. Set `TVGUIDE_EPG_XMLTVURL` (and the channel mapping env vars) in the Render
   dashboard to point at your XMLTV feed.

To build and run locally against the same image:

```bash
docker build -t sydney-tv-guide .
docker run --rm -p 8080:8080 sydney-tv-guide
```
