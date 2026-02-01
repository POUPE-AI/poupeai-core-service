package io.github.poupeai.core.audit;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;


@Component
public class ApplicationContextProvider implements ApplicationContextAware {

    private static volatile ApplicationContext applicationContext;

    @Override
public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static <T> T getBean(Class<T> beanClass) {
        if (applicationContext == null) {
            return null;
        }
        try {
            return applicationContext.getBean(beanClass);
        } catch (BeansException e) {
            return null;
        }
    }
}
