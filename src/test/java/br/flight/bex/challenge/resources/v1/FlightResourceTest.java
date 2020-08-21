package br.flight.bex.challenge.resources.v1;

import br.flight.bex.challenge.exceptions.handler.GlobalExceptionHandler;
import br.flight.bex.challenge.services.FlightService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    public void givenRequestForBestRouteWhenSuccessThenReturnStatusOkAndPath() throws Exception {
        List<String> pathRoutes = Arrays.asList("GRU", "BRC", "SCL", "ORL", "CDG");
        Double cost = 40.0;

        when(flightService.bestRoute("GRU", "CDG")).thenReturn(pathRoutes);

        when(flightService.getCost(anyString())).thenReturn(cost);

        mockMvc.perform(get(FlightResource.BASE_URL + "/routes/source/GRU/target/CDG")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.path", equalTo(pathRoutes.stream().collect(Collectors.joining(" - ")))))
                .andExpect(jsonPath("$.cost", equalTo(cost)));
    }

}
