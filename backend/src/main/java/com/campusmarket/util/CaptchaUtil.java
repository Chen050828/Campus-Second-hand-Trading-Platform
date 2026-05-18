package com.campusmarket.util;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import java.util.Properties;

@Component
public class CaptchaUtil {
    @Bean
    public DefaultKaptcha defaultKaptcha() {
        DefaultKaptcha kaptcha = new DefaultKaptcha();
        Properties props = new Properties();
        props.setProperty("kaptcha.border", "yes");
        props.setProperty("kaptcha.border.color", "105,179,90");
        props.setProperty("kaptcha.textproducer.font.color", "blue");
        props.setProperty("kaptcha.image.width", "150");
        props.setProperty("kaptcha.image.height", "50");
        props.setProperty("kaptcha.textproducer.font.size", "35");
        props.setProperty("kaptcha.textproducer.char.length", "4");
        props.setProperty("kaptcha.textproducer.font.names", "Arial");
        props.setProperty("kaptcha.noise.impl", "com.google.code.kaptcha.impl.NoNoise");
        kaptcha.setConfig(new Config(props));
        return kaptcha;
    }
}
