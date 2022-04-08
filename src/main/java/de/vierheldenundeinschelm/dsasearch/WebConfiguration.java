package de.vierheldenundeinschelm.dsasearch;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Configuration
public class WebConfiguration extends WebMvcConfigurationSupport {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
            .addResourceLocations("classpath:/static/");

        registry.addResourceHandler("/**")
            .addResourceLocations(
                "file:d:/temp/dsasearch/preview/",
                "file:d:/dsa/drivethru/")
            .resourceChain(true)
            .addResolver(new CustomPathResourceResolver());
    }
}
