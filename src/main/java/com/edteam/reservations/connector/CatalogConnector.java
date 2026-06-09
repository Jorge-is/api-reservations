package com.edteam.reservations.connector;

import com.edteam.reservations.connector.configuration.EndpointConfiguration;
import com.edteam.reservations.connector.configuration.HostConfiguration;
import com.edteam.reservations.connector.configuration.HttpConnectorConfiguration;
import com.edteam.reservations.connector.response.CityDTO;
import com.edteam.reservations.enums.APIError;
import com.edteam.reservations.exception.ReservationException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.util.concurrent.TimeUnit;

@Component
public class CatalogConnector {

    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogConnector.class);

    private static final String HOST = "api-catalog";
    private static final String ENDPOINT = "get-city";

    private final HttpConnectorConfiguration configuration;
    private WebClient webClient;

    public CatalogConnector(HttpConnectorConfiguration configuration) {
        this.configuration = configuration;
    }

    @PostConstruct
    void initWebClient() {
        HostConfiguration hostConfig = configuration.getHosts().get(HOST);
        EndpointConfiguration endpointConfig = hostConfig.getEndpoints().get(ENDPOINT);

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Math.toIntExact(endpointConfig.getConnectionTimeout()))
                .doOnConnected(conn -> conn
                        .addHandler(new ReadTimeoutHandler(endpointConfig.getReadTimeout(), TimeUnit.MILLISECONDS))
                        .addHandler(new WriteTimeoutHandler(endpointConfig.getWriteTimeout(), TimeUnit.MILLISECONDS)));

        this.webClient = WebClient.builder()
                .baseUrl("http://" + hostConfig.getHost() + ":" + hostConfig.getPort() + endpointConfig.getUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(new ReactorClientHttpConnector(httpClient)).build();
    }

    @CircuitBreaker(name = "api-catalog", fallbackMethod = "fallbackGetCity")
    public CityDTO getCity(String code) {
        LOGGER.info("Calling to api-catalog");
        return webClient.get().uri(urlEncoder -> urlEncoder.build(code)).retrieve().bodyToMono(CityDTO.class).share()
                .block();
    }

    public CityDTO fallbackGetCity(String code, CallNotPermittedException ex) {
        LOGGER.debug("calling to fallbackGetCity-1");

        throw new ReservationException(APIError.CITY_NOT_FOUND);
    }

    public CityDTO fallbackGetCity(String code, Exception ex) {
        LOGGER.debug("calling to fallbackGetCity-2");

        throw new ReservationException(APIError.VALIDATION_ERROR);
    }
}
