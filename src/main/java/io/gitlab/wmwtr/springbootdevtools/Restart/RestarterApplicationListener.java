package io.gitlab.wmwtr.springbootdevtools.Restart;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;

public class RestarterApplicationListener implements ApplicationListener<ApplicationEvent>, Ordered {
    private static final Log logger = LogFactory.getLog(RestarterApplicationListener.class);

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationStartingEvent) {
            this.onApplicationStartingEvent((ApplicationStartingEvent)event);
        }

        if (event instanceof ApplicationPreparedEvent) {
            this.onApplicationPreparedEvent((ApplicationPreparedEvent)event);
        }

        if (event instanceof ApplicationReadyEvent || event instanceof ApplicationFailedEvent) {
            Restarter.getInstance().finish();
        }

    }

    private void onApplicationStartingEvent(ApplicationStartingEvent event) {
        String[] args = event.getArgs();
        PatternClassPathStrategy initializer = new DefaultPatternClassPathStrategy();
        Restarter.initialize(args, initializer);
    }

    private void onApplicationPreparedEvent(ApplicationPreparedEvent event) {
        try{
            Restarter.getInstance().prepare(event.getApplicationContext());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
