package org.example;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Properties;

public class Injector {
    public Object inject(Object object) {
        Class<?> clazz = object.getClass();
        Field[] fields = clazz.getDeclaredFields();

        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("src/main/resources/injector.properties")); // Load the properties file
        } catch (IOException e) {
            throw new RuntimeException("Failed to load injector.properties", e);
        }

        for (Field field : fields) {
            if (field.isAnnotationPresent(AutoInjectable.class)) {
                Class<?> fieldType = field.getType();
                String propertyKey = fieldType.getName();

                String implementationClassName = properties.getProperty(propertyKey);
                if (implementationClassName == null) {
                    throw new RuntimeException("No implementation specified for " + propertyKey);
                }

                try {
                    Class<?> implementationClass = Class.forName(implementationClassName);
                    Object implementationInstance = implementationClass.getDeclaredConstructor().newInstance();
                    field.setAccessible(true);
                    field.set(object, implementationInstance);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to inject field: " + field.getName(), e);
                }
            }
        }

        return object;
    }
}