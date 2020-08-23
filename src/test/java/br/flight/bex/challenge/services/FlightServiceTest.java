package br.flight.bex.challenge.services;

import br.flight.bex.challenge.entities.RouteEntity;
import br.flight.bex.challenge.exceptions.ChallengeException;
import br.flight.bex.challenge.models.dtos.RouteDTO;
import br.flight.bex.challenge.repositories.RouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.ApplicationArguments;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

public class FlightServiceTest {

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private ApplicationArguments applicationArguments;

    @InjectMocks
    private FlightService flightService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        flightService = new FlightService(routeRepository, applicationArguments);
    }

    @Test
    public void givenRequestForAddRouteThenSucessAndReturnNothing() throws IOException {
        RouteDTO routeDTO = getRoute();

        RouteEntity routeEntity = getRouteEntity(routeDTO);

        when(routeRepository.save(routeEntity)).thenReturn(routeEntity);

        File file = File.createTempFile("test" , ".csv");

        given(applicationArguments.getNonOptionArgs()).willReturn(Arrays.asList(file.getAbsolutePath()));

        flightService.addRoute(routeDTO);
    }

    @Test
    public void givenRequestForAddRouteWithOutFileThenSucessAndReturnNothing() {
        RouteDTO routeDTO = getRoute();

        RouteEntity routeEntity = getRouteEntity(routeDTO);

        when(routeRepository.save(routeEntity)).thenReturn(routeEntity);

        flightService.addRoute(routeDTO);
    }

    @Test
    public void givenRequestForBestRouteWithOutAnyRoutesInDatabaseThenExpectedAnExpection() {
        when(routeRepository.findAll()).thenReturn(Arrays.asList());

        ChallengeException challengeException = assertThrows(ChallengeException.class, () -> {
           flightService.bestRoute("GRU", "CDG");
        });

        assertTrue("Not found any routes in database!".contains(challengeException.getMessage()));
    }

    @Test
    public void givenRequestForBestRouteWithValidRouteThenSucessAndReturnList() {
        List<RouteEntity> path = getListRoutesDatabase();

        when(routeRepository.findAll()).thenReturn(path);

        List<String> strings = flightService.bestRoute("GRU", "CDG");

        assertArrayEquals(strings.toArray(new String[]{}), new String[]{"GRU", "BRC", "SCL", "ORL", "CDG"});
    }

    @Test
    public void givenRequestForBestRouteWithRouteNotInDatabaseThenExpectedAnExpection() {
        List<RouteEntity> path = getListRoutesDatabase();

        when(routeRepository.findAll()).thenReturn(path);

        ChallengeException challengeException = assertThrows(ChallengeException.class, () -> {
            flightService.bestRoute("GRU", "ABC");
        });

        assertTrue("Not found a path!".contains(challengeException.getMessage()));
    }

    private RouteEntity getRouteEntity(RouteDTO routeDTO) {
        return RouteEntity.from(routeDTO);
    }

    private RouteDTO getRoute() {
        return RouteDTO
                .builder()
                .source("GRU")
                .target("BRC")
                .cost(10.0)
                .build();
    }

    private List<RouteEntity> getListRoutesDatabase() {
        return Arrays.asList(
                getRouteEntity(getRoute()),
                RouteEntity
                        .builder()
                        .source("BRC")
                        .target("SCL")
                        .cost(5.0)
                        .build(),
                RouteEntity
                        .builder()
                        .source("GRU")
                        .target("CDG")
                        .cost(75.0)
                        .build(),
                RouteEntity
                        .builder()
                        .source("GRU")
                        .target("SCL")
                        .cost(20.0)
                        .build(),
                RouteEntity
                        .builder()
                        .source("GRU")
                        .target("ORL")
                        .cost(56.0)
                        .build(),
                RouteEntity
                        .builder()
                        .source("ORL")
                        .target("CDG")
                        .cost(5.0)
                        .build(),
                RouteEntity
                        .builder()
                        .source("SCL")
                        .target("ORL")
                        .cost(20.0)
                        .build()
        );
    }

}
