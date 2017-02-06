package org.exastax.poiimporter.conf;


import org.exastax.poiimporter.IFileImporter;
import org.exastax.poiimporter.FileImporter;
import org.exastax.poiimporter.file.IFileManager;
import org.exastax.poiimporter.file.FileManager;
import org.exastax.poiimporter.file.PropertiesModel;
import org.exastax.poiimporter.es.IESManager;
import org.exastax.poiimporter.es.ESManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource("classpath:/${spring.profiles.active:default}.properties")
public class AppConfig {

    public static PropertiesModel properties;
    @Autowired
    Environment env;

    @Bean
    public IFileImporter loadCsvImpl() throws Exception {
        FileImporter load = new FileImporter ( );
        return load;
    }

    @Bean
    public IFileManager csvManagerImpl() throws Exception {
        return new FileManager ( env );
    }

    @Bean
    public IESManager ESManagerImple() {
        return new ESManager (env);
    }
}
