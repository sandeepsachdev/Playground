package au.com.sydneytv.guide.service;

import au.com.sydneytv.guide.config.EpgProperties;
import au.com.sydneytv.guide.config.EpgProperties.ChannelMapping;
import au.com.sydneytv.guide.model.Channel;
import au.com.sydneytv.guide.model.Program;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Live EPG provider that fetches and parses an XMLTV feed.
 *
 * <p>XMLTV is the <i>de facto</i> standard format for TV listings and is served
 * by a number of Australian sources: iptv-org (community, free), IceTV
 * (commercial, API), OzTivo (community), plus any xmltv-grabber you run yourself.
 *
 * <p>The provider only keeps programmes whose XMLTV channel id is listed in
 * {@link EpgProperties#getChannels()}, converts each programme's start/stop
 * timestamps into Sydney local time, and buckets by {@link DayOfWeek}. Within a
 * day, channel programme lists are sorted by start time.
 */
public class XmltvEpgProvider implements EpgProvider {

    private static final Logger log = LoggerFactory.getLogger(XmltvEpgProvider.class);

    static final ZoneId SYDNEY = ZoneId.of("Australia/Sydney");
    private static final DateTimeFormatter XMLTV_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss Z", Locale.ROOT);
    private static final DateTimeFormatter XMLTV_FORMAT_NO_SECONDS =
            DateTimeFormatter.ofPattern("yyyyMMddHHmm Z", Locale.ROOT);

    private final EpgProperties properties;
    private final HttpClient httpClient;

    public XmltvEpgProvider(EpgProperties properties) {
        this(properties, HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build());
    }

    XmltvEpgProvider(EpgProperties properties, HttpClient httpClient) {
        this.properties = properties;
        this.httpClient = httpClient;
    }

    @Override
    public String name() {
        return "xmltv(" + properties.getXmltvUrl() + ")";
    }

    @Override
    public Map<DayOfWeek, List<Channel>> fetchSchedule() {
        String url = properties.getXmltvUrl();
        if (url == null || url.isBlank()) {
            throw new IllegalStateException("tvguide.epg.xmltv-url is not configured");
        }
        log.info("Fetching XMLTV feed from {}", url);
        try (InputStream stream = openStream(url)) {
            return parse(stream);
        } catch (IOException | InterruptedException | XMLStreamException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("Failed to load XMLTV feed from " + url, ex);
        }
    }

    private InputStream openStream(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "application/xml, text/xml, application/gzip, */*")
                .header("Accept-Encoding", "gzip")
                .GET()
                .build();
        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() / 100 != 2) {
            response.body().close();
            throw new IOException("XMLTV fetch returned HTTP " + response.statusCode());
        }
        InputStream body = response.body();
        boolean isGzip = url.endsWith(".gz")
                || response.headers().firstValue("Content-Encoding")
                        .map(v -> v.equalsIgnoreCase("gzip")).orElse(false);
        return isGzip ? new GZIPInputStream(body) : body;
    }

    Map<DayOfWeek, List<Channel>> parse(InputStream stream) throws XMLStreamException {
        Map<String, ChannelMapping> channelsById = indexChannelMappings();
        Map<String, Map<DayOfWeek, List<Program>>> programsByChannel = new HashMap<>();

        XMLInputFactory factory = XMLInputFactory.newFactory();
        factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        factory.setProperty("javax.xml.stream.isSupportingExternalEntities", Boolean.FALSE);
        XMLStreamReader reader = factory.createXMLStreamReader(stream);
        try {
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT
                        && "programme".equals(reader.getLocalName())) {
                    readProgramme(reader, channelsById, programsByChannel);
                }
            }
        } finally {
            reader.close();
        }

        return assemble(channelsById, programsByChannel);
    }

    private Map<String, ChannelMapping> indexChannelMappings() {
        Map<String, ChannelMapping> byId = new HashMap<>();
        for (ChannelMapping m : properties.getChannels()) {
            byId.put(m.getXmltvId(), m);
        }
        if (byId.isEmpty()) {
            throw new IllegalStateException(
                    "tvguide.epg.channels must list at least one XMLTV channel mapping");
        }
        return byId;
    }

    private void readProgramme(XMLStreamReader reader,
                               Map<String, ChannelMapping> channelsById,
                               Map<String, Map<DayOfWeek, List<Program>>> programsByChannel)
            throws XMLStreamException {
        String channelId = reader.getAttributeValue(null, "channel");
        if (channelId == null || !channelsById.containsKey(channelId)) {
            skipElement(reader);
            return;
        }
        ZonedDateTime start = parseTime(reader.getAttributeValue(null, "start"));
        ZonedDateTime stop = parseTime(reader.getAttributeValue(null, "stop"));
        if (start == null || stop == null) {
            skipElement(reader);
            return;
        }

        String title = null;
        String genre = null;
        String rating = null;
        String synopsis = null;

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                switch (reader.getLocalName()) {
                    case "title" -> title = firstNonNull(title, reader.getElementText());
                    case "category" -> genre = firstNonNull(genre, reader.getElementText());
                    case "desc" -> synopsis = firstNonNull(synopsis, reader.getElementText());
                    case "rating" -> rating = firstNonNull(rating, readRating(reader));
                    default -> skipElement(reader);
                }
            } else if (event == XMLStreamConstants.END_ELEMENT
                    && "programme".equals(reader.getLocalName())) {
                break;
            }
        }

        if (title == null || title.isBlank()) {
            return;
        }

        ZonedDateTime sydneyStart = start.withZoneSameInstant(SYDNEY);
        ZonedDateTime sydneyStop = stop.withZoneSameInstant(SYDNEY);
        DayOfWeek day = sydneyStart.getDayOfWeek();
        Program program = new Program(
                sydneyStart.toLocalTime(),
                clampEndTime(sydneyStart, sydneyStop),
                title.trim(),
                genre == null ? "General" : genre.trim(),
                rating == null ? "NC" : rating.trim(),
                synopsis == null ? "" : synopsis.trim()
        );
        programsByChannel
                .computeIfAbsent(channelId, k -> new EnumMap<>(DayOfWeek.class))
                .computeIfAbsent(day, k -> new ArrayList<>())
                .add(program);
    }

    private LocalTime clampEndTime(ZonedDateTime start, ZonedDateTime stop) {
        if (stop.toLocalDate().isAfter(start.toLocalDate())) {
            return LocalTime.of(23, 59);
        }
        return stop.toLocalTime();
    }

    private String readRating(XMLStreamReader reader) throws XMLStreamException {
        String value = null;
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT && "value".equals(reader.getLocalName())) {
                value = reader.getElementText();
            } else if (event == XMLStreamConstants.END_ELEMENT && "rating".equals(reader.getLocalName())) {
                break;
            }
        }
        return value;
    }

    private void skipElement(XMLStreamReader reader) throws XMLStreamException {
        int depth = 1;
        while (depth > 0 && reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                depth++;
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                depth--;
            }
        }
    }

    private Map<DayOfWeek, List<Channel>> assemble(
            Map<String, ChannelMapping> channelsById,
            Map<String, Map<DayOfWeek, List<Program>>> programsByChannel) {

        Map<DayOfWeek, List<Channel>> byDay = new EnumMap<>(DayOfWeek.class);
        for (DayOfWeek day : DayOfWeek.values()) {
            List<Channel> channels = new ArrayList<>();
            for (ChannelMapping mapping : properties.getChannels()) {
                List<Program> programs = programsByChannel
                        .getOrDefault(mapping.getXmltvId(), Map.of())
                        .getOrDefault(day, List.of())
                        .stream()
                        .sorted(Comparator.comparing(Program::getStartTime))
                        .toList();
                channels.add(new Channel(mapping.getNumber(), mapping.getName(),
                        mapping.getNetwork(), programs));
            }
            byDay.put(day, channels);
        }
        return byDay;
    }

    static ZonedDateTime parseTime(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String trimmed = raw.trim();
        try {
            return OffsetDateTime.parse(trimmed, XMLTV_FORMAT).toZonedDateTime();
        } catch (Exception first) {
            try {
                return OffsetDateTime.parse(trimmed, XMLTV_FORMAT_NO_SECONDS).toZonedDateTime();
            } catch (Exception second) {
                log.warn("Unparseable XMLTV timestamp: {}", raw);
                return null;
            }
        }
    }

    private static String firstNonNull(String existing, String candidate) {
        return existing != null ? existing : candidate;
    }
}
