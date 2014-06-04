package fr.insee.cspa.sa.content;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.freemarker.FreemarkerMvcFeature;
import org.glassfish.jersey.server.mvc.freemarker.FreemarkerProperties;

public class AppConfig extends ResourceConfig {

    public AppConfig() {
           
            // Package des ressources
            packages("org.romain.resources");
           
            // Ajout de l'extension Freemarker
            register(FreemarkerMvcFeature.class);
           
            // Support JSON
            register(JacksonFeature.class);
           
            // Chemin vers le dossier contenant les templates Freemarker (.jtl)
            property(FreemarkerProperties.TEMPLATES_BASE_PATH, "freemarker");
    }
}
