package io.github.poupeai.core.persistence.config;

import io.github.poupeai.core.persistence.mapper.BankAccountEntityMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperConfig {

    // Mapper bean registration removed to avoid duplicate bean with MapStruct-generated component.
    // MapStruct generates a Spring @Component for mappers when componentModel = "spring",
    // so manual registration is not necessary.

}
