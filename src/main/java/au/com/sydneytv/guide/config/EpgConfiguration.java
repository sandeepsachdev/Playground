package au.com.sydneytv.guide.config;

import au.com.sydneytv.guide.service.EpgProvider;
import au.com.sydneytv.guide.service.ScheduleRepository;
import au.com.sydneytv.guide.service.XmltvEpgProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableConfigurationProperties(EpgProperties.class)
public class EpgConfiguration {

    private static final Logger log = LoggerFactory.getLogger(EpgConfiguration.class);

    @Bean
    public ScheduleRepository scheduleRepository() {
        return new ScheduleRepository();
    }

    @Bean
    @Primary
    public EpgProvider epgProvider(EpgProperties properties, ScheduleRepository staticProvider) {
        if (properties.getSource() == EpgProperties.Source.XMLTV
                && properties.getXmltvUrl() != null
                && !properties.getXmltvUrl().isBlank()) {
            log.info("EPG source: xmltv ({})", properties.getXmltvUrl());
            return new XmltvEpgProvider(properties);
        }
        log.info("EPG source: static (sample schedule)");
        return staticProvider;
    }
}
