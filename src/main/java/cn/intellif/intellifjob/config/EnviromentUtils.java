package cn.intellif.intellifjob.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import java.util.Iterator;

public class EnviromentUtils implements EnvironmentPostProcessor {
    private static ConfigurableEnvironment configurableEnvironment;
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        EnviromentUtils.configurableEnvironment = environment;
    }
    public static Object get(String name){
        MutablePropertySources propertySources = configurableEnvironment.getPropertySources();
        Iterator<PropertySource<?>> iterator = propertySources.iterator();
        while(iterator.hasNext()){
            PropertySource propertySource = iterator.next();
            if(propertySource.containsProperty(name))
                return  propertySource.getProperty(name);
        }
        return null;
    }
}
