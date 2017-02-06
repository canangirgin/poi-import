package org.exastax.poiimporter;

import org.exastax.poiimporter.conf.AppConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

public class MainProcess {

    public static void main(String[] args) throws Exception {
        ApplicationContext ctx = new AnnotationConfigApplicationContext ( AppConfig.class );
        ctx.getBean ( IFileImporter.class ).process ( );
        ((AbstractApplicationContext) ctx).close ( );
    }

}
