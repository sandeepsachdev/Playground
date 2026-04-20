package au.com.sydneytv.guide.config;

import au.com.sydneytv.guide.service.EmptyEpgProvider;
import au.com.sydneytv.guide.service.EpgProvider;
import au.com.sydneytv.guide.service.XmltvEpgProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(EpgProperties.class)
public class EpgConfiguration {

    private static final Logger log = LoggerFactory.getLogger(EpgConfiguration.class);

    @Bean
    public EpgProvider epgProvider(EpgProperties properties) {
        String url = properties.getXmltvUrl();
        if (url != null && !url.isBlank()) {
            log.info("EPG source: xmltv ({})", url);
            return new XmltvEpgProvider(properties);
        }
        log.warn("No EPG source configured (tvguide.epg.xmltv-url is blank) — guide will render empty");
        return new EmptyEpgProvider();
    }
}
