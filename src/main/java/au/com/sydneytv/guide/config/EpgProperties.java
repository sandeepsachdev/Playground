package au.com.sydneytv.guide.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "tvguide.epg")
public class EpgProperties {

    /**
     * {@code static} (default, uses the bundled sample schedule) or {@code xmltv}
     * (fetches a live XMLTV feed and falls back to static on error).
     */
    private Source source = Source.STATIC;

    /** URL of the XMLTV feed (plain XML or gzipped). Required when source=xmltv. */
    private String xmltvUrl;

    /** How long to cache a successful fetch before refreshing. */
    private Duration refreshInterval = Duration.ofHours(6);

    /** Mapping of XMLTV channel ids to local channel metadata. */
    private List<ChannelMapping> channels = new ArrayList<>();

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public String getXmltvUrl() {
        return xmltvUrl;
    }

    public void setXmltvUrl(String xmltvUrl) {
        this.xmltvUrl = xmltvUrl;
    }

    public Duration getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(Duration refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    public List<ChannelMapping> getChannels() {
        return channels;
    }

    public void setChannels(List<ChannelMapping> channels) {
        this.channels = channels;
    }

    public enum Source {
        STATIC, XMLTV
    }

    public static class ChannelMapping {
        private String xmltvId;
        private String number;
        private String name;
        private String network;

        public ChannelMapping() {
        }

        public ChannelMapping(String xmltvId, String number, String name, String network) {
            this.xmltvId = xmltvId;
            this.number = number;
            this.name = name;
            this.network = network;
        }

        public String getXmltvId() {
            return xmltvId;
        }

        public void setXmltvId(String xmltvId) {
            this.xmltvId = xmltvId;
        }

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getNetwork() {
            return network;
        }

        public void setNetwork(String network) {
            this.network = network;
        }
    }
}
