# Sydney TV Guide

A small Spring Boot application that serves a free-to-air TV guide for Sydney
and surfaces prime-time (6pm – 10pm) highlights for each night of the week.

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

Then open <http://localhost:8080/>.

## Endpoints

- `GET /` – Web UI showing today's prime-time guide with highlights.
- `GET /?day=FRIDAY` – Web UI for a specific day (`MONDAY` … `SUNDAY`).
- `GET /api/guide/today` – JSON guide for today.
- `GET /api/guide/week` – JSON guide for the full week.
- `GET /api/guide/day/{day}` – JSON guide for a named day.
- `GET /api/guide/highlights/{day}` – Just the highlights for a day.

## Highlights

The `HighlightsService` scores every program overlapping the 6pm–10pm window
using a transparent, editable rubric (premium genres, flagship Australian
titles, live sport, movie premieres) and returns up to five picks ordered by
start time, each annotated with a short reason to watch.

## Live data sources

Out of the box the app serves a hand-curated static schedule (`tvguide.epg.source=static`).
Flip to `xmltv` to pull real listings from any XMLTV-compatible feed. XMLTV is
the standard interchange format used by virtually every EPG provider.

Known sources you can point at:

| Source | URL pattern | Notes |
|--------|-------------|-------|
| [iptv-org EPG](https://github.com/iptv-org/epg) | `https://iptv-org.github.io/epg/guides/au/freeview.com.au.epg.xml.gz` | Community-scraped, free, daily refresh |
| [IceTV](https://www.icetv.com.au/) | Your API endpoint | Commercial, paid, high-quality |
| [OzTivo](https://www.oztivo.net/) | Rotating mirror URLs | Community, XMLTV format |
| Self-hosted `xmltv` grabber | Any URL you expose | Run `tv_grab_au_*` locally |

Configure via `application.properties`:

```properties
tvguide.epg.source=xmltv
tvguide.epg.xmltv-url=https://iptv-org.github.io/epg/guides/au/freeview.com.au.epg.xml.gz
tvguide.epg.refresh-interval=PT6H
tvguide.epg.channels[0].xmltv-id=abc1.abc.net.au
tvguide.epg.channels[0].number=2
tvguide.epg.channels[0].name=ABC TV
tvguide.epg.channels[0].network=ABC
# ... repeat for SBS, Seven, Nine, 10
```

`EpgService` caches the parsed schedule for `refresh-interval` and transparently
falls back to the static schedule if the upstream feed is unreachable on first
boot, so the UI never goes blank.

## Deploying to Render

The repo ships with a multi-stage `Dockerfile` and a `render.yaml` blueprint.

1. Push the branch to GitHub.
2. In Render, choose **New +** → **Blueprint** and point at this repo.
   Render will read `render.yaml` and provision a Docker web service.
3. Render injects a `PORT` env var; the container's entrypoint binds Spring
   Boot to it via `-Dserver.port=${PORT}`, so no extra config is required.
4. Health checks hit `/api/guide/today`.

To build and run locally against the same image:

```bash
docker build -t sydney-tv-guide .
docker run --rm -p 8080:8080 sydney-tv-guide
```

## Notes

The schedule is a representative, hand-curated snapshot — useful for demos and
testing. For a production build, plug in a real EPG (e.g. FreeView, the
networks' own schedule feeds, or Gracenote) behind `ScheduleRepository`.
