package cl.notara.ms_vocabulario.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;

/**
 * Configuración de Redis para el módulo de vocabulario.
 *
 * <p>
 * Define los caches disponibles y sus TTL (Time To Live) individuales:
 * </p>
 *
 * <ul>
 *   <li><b>ranking-global</b>: Top 10 usuarios por puntuación global — TTL 5 minutos.</li>
 *   <li><b>ranking-categoria</b>: Top 10 por categoría — TTL 5 minutos.</li>
 *   <li><b>estadisticas-usuario</b>: Rankings individuales de un usuario — TTL 10 minutos.</li>
 * </ul>
 *
 * <p>
 * Los caches se invalidan automáticamente con {@code @CacheEvict} al finalizar
 * una partida y actualizar el ranking correspondiente.
 * </p>
 *
 * @author Notara
 * @version 1.0
 */
@EnableCaching
@Configuration
public class RedisConfig {

    /**
     * Configura el {@link RedisCacheManager} con TTLs específicos por cache.
     *
     * @param factory conexión a Redis inyectada por Spring Boot
     * @return cache manager configurado
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration
                .defaultCacheConfig()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new GenericJackson2JsonRedisSerializer())
                )
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigs = Map.of(
                // Ranking global: cambia cuando alguien termina una partida — 5 min
                "ranking-global", defaultConfig.entryTtl(Duration.ofMinutes(5)),
                // Ranking por categoría: mismo ciclo de actualización — 5 min
                "ranking-categoria", defaultConfig.entryTtl(Duration.ofMinutes(5)),
                // Estadísticas de usuario: menos consultado, puede vivir más — 10 min
                "estadisticas-usuario", defaultConfig.entryTtl(Duration.ofMinutes(10))
        );

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig.entryTtl(Duration.ofMinutes(5)))
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}
