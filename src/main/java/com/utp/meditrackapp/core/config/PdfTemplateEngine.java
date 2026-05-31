package com.utp.meditrackapp.core.config;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

public class PdfTemplateEngine {
    private static TemplateEngine instance;

    private PdfTemplateEngine() {}

    public static synchronized TemplateEngine getInstance() {
        if (instance == null) {
            ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
            templateResolver.setPrefix("/templates/pdf/");
            templateResolver.setSuffix(".html");
            templateResolver.setTemplateMode(TemplateMode.XML);
            templateResolver.setCharacterEncoding("UTF-8");
            templateResolver.setCacheable(false); // Disable caching to reflect changes immediately

            instance = new TemplateEngine();
            instance.setTemplateResolver(templateResolver);
        }
        return instance;
    }
}
