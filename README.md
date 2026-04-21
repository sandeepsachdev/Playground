# News Word Cloud

A Spring Boot application that pulls headlines from several live news feeds,
filters out stop words, and surfaces the trending words and two-word phrases
as an interactive word cloud.

## What it does

- Fetches RSS/Atom feeds configured under `newscloud.feeds`.
- Strips HTML, tokenises titles + descriptions, and drops stop words
  (`src/main/resources/stopwords.txt` — about 170 common English and
  news-wire terms like `a`, `this`, `and`, `said`, `year`).
- Counts unigrams and bigrams, ranks them by frequency, and returns the top
  `newscloud.top-n` (default 120) as a `TrendingSnapshot`.
- Renders the snapshot as a canvas word cloud using
  [wordcloud2.js](https://github.com/timdream/wordcloud2.js), with a side
  panel listing the top 25 terms and phrase badges.

## Running

```bash
./mvnw spring-boot:run
# or
mvn spring-boot:run
```

Open <http://localhost:8080/> for the cloud. The JSON snapshot is at
`GET /api/trending` and a manual refresh is `POST /api/trending/refresh`.

## Default feeds

Configured in `application.properties`:

- ABC News (Australia)
- BBC World
- The Guardian (Australia)
- Sydney Morning Herald
- NPR News
- Al Jazeera

Add, replace, or remove entries with the indexed-list form:

```properties
newscloud.feeds[6].name=Reuters Top News
newscloud.feeds[6].url=https://example.com/reuters.xml
```

## Tuning

| Property | Default | Meaning |
|----------|---------|---------|
| `newscloud.refresh-interval` | `PT15M` | How long a snapshot is cached |
| `newscloud.max-entries` | `500` | Articles pulled into each analysis (newest first) |
| `newscloud.min-token-length` | `3` | Shorter tokens are dropped |
| `newscloud.top-n` | `120` | Words + phrases returned by the API |
| `newscloud.include-phrases` | `true` | Include bigram phrases alongside words |

## Deploying to Render

The repo ships a multi-stage `Dockerfile` and `render.yaml`. Render will
auto-provision a Docker web service and bind Spring Boot to the injected
`PORT`. The health check hits `/api/trending`. See the Dockerfile for the
exact build steps.

```bash
docker build -t news-wordcloud .
docker run --rm -p 8080:8080 news-wordcloud
```
