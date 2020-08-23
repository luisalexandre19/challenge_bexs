package br.flight.bex.challenge.resources.v1;

import br.flight.bex.challenge.exceptions.ChallengeException;
import br.flight.bex.challenge.exceptions.handler.GlobalExceptionHandler;
import br.flight.bex.challenge.models.dtos.RouteDTO;
import br.flight.bex.challenge.services.FlightService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.HttpServerErrorException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FlightResourceTest {

    @Mock
    private FlightService flightService;

    @InjectMocks
    private FlightResource flightResource;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        mockMvc = MockMvcBuilders.standaloneSetup(flightResource)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    public void givenRequestForBestRouteWhenSuccessThenReturnStatusOkAndPathAndCost() throws Exception {
        List<String> pathRoutes = Arrays.asList("GRU", "BRC", "SCL", "ORL", "CDG");
        Double cost = 40.0;

        when(flightService.bestRoute(anyString(), anyString())).thenReturn(pathRoutes);

        when(flightService.getCost(anyString())).thenReturn(cost);

        mockMvc.perform(get(FlightResource.BASE_URL + "/routes/source/GRU/target/CDG")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.path", equalTo(pathRoutes.stream().collect(Collectors.joining(" - ")))))
                .andExpect(jsonPath("$.cost", equalTo(cost)));
    }

    @Test
    public void givenRequestForBestRouteWhenNotFoundPathThenReturnStatusNotFound() throws Exception {
        when(flightService.bestRoute(anyString(), anyString()))
                .thenThrow(new ChallengeException("Not found a path!", HttpStatus.NOT_FOUND));

        mockMvc.perform(get(FlightResource.BASE_URL + "/routes/source/GRU/target/SAD")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Not found a path!"));
    }

    @Test
    public void givenRequestForBestRouteWhenNoRoutesInDatabaseThenReturnStatusNoContent() throws Exception {
        when(flightService.bestRoute(anyString(), anyString()))
                .thenThrow(new ChallengeException("Not found any routes in database!", HttpStatus.NO_CONTENT));

        mockMvc.perform(get(FlightResource.BASE_URL + "/routes/source/GRU/target/SAD")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(content().string("Not found any routes in database!"));
    }

    @Test
    public void givenRequestForAddRouteWhenSuccessThenReturnStatusCreated() throws Exception {
        RouteDTO routeDTO = new RouteDTO("GRU", "CDG", 40d);

        mockMvc.perform(post(FlightResource.BASE_URL + "/routes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsBytes(routeDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    public void givenRequestForAddRouteWithNullFieldsThenReturnBadRequest() throws Exception {
        RouteDTO routeDTO = new RouteDTO("GRU", null, 40d);

        mockMvc.perform(post(FlightResource.BASE_URL + "/routes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsBytes(routeDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]", equalTo("Task target must not be blank!")));
    }

    @Test
    public void givenRequestForAddRouteWithEmptyFieldsThenReturnBadRequest() throws Exception {
        RouteDTO routeDTO = new RouteDTO("GRU", "", 40d);

        mockMvc.perform(post(FlightResource.BASE_URL + "/routes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsBytes(routeDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]", equalTo("Task target must not be blank!")));
    }

    @Test
    public void givenRequestForAddRouteWithNegativeCostThenReturnBadRequest() throws Exception {
        RouteDTO routeDTO = new RouteDTO("GRU", "CDG", -40d);

        mockMvc.perform(post(FlightResource.BASE_URL + "/routes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsBytes(routeDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]", equalTo("Task cost must be positive or zero!")));
    }

    @Test
    public void givenRequestForBestRouteCallWhenServerErrorThenReturn500Error() throws Exception {
        when(flightService.bestRoute(anyString(), anyString()))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        mockMvc.perform(get(FlightResource.BASE_URL + "/routes/source/GRU/target/CDG")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }

    @Test
    public void givenRequestForInvalidPathWhenServerErrorThenReturn404() throws Exception {
        when(flightService.bestRoute(anyString(), anyString()))
                .thenThrow(new HttpServerErrorException(HttpStatus.NOT_FOUND));

        mockMvc.perform(get(FlightResource.BASE_URL + "/routes/source/GRU/target/")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

}
