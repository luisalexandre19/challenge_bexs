package br.flight.bex.challenge;

import br.flight.bex.challenge.exceptions.ChallengeException;
import br.flight.bex.challenge.services.FlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@SpringBootApplication
public class ChallengeApplication extends SpringBootServletInitializer implements CommandLineRunner {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(ChallengeApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(ChallengeApplication.class, args);
    }

    @Autowired
    FlightService flightService;

    @Override
    public void run(String... args) {

        Scanner in = new Scanner(System.in);

        while (true) {

            try {

                System.out.println("please enter the route: SOURCE-TARGET Ex. GRU-CDG");

                String input = in.next();
                String[] route = input.split("-");
                String souce = route[0];
                String target = route[1];

                if (nonNull(souce)
                        && !souce.isEmpty()
                    && nonNull(target)
                    && !target.isEmpty()) {

                    List<String> path = flightService.bestRoute(souce.toUpperCase(), target.toUpperCase());

                    System.out.println("best route: " + path.stream().collect(Collectors.joining(" - ")) + " > $" + flightService.getCost(target));

                } else {
                    throw new ChallengeException("enter with a valid route!");
                }

            } catch (ChallengeException ex) {
                System.out.println(ex.getMessage());
            } catch (Throwable ex) {
                System.out.println("invalid input!");
            }
        }

    }


}
