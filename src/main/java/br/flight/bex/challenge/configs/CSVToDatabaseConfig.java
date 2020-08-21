package br.flight.bex.challenge.configs;

import br.flight.bex.challenge.entities.RouteEntity;
import br.flight.bex.challenge.models.dtos.RouteDTO;
import br.flight.bex.challenge.repositories.RouteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManagerFactory;
import java.util.List;

@EnableBatchProcessing
@Configuration
public class CSVToDatabaseConfig {

    @Autowired
    ApplicationArguments applicationArguments;

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    public EntityManagerFactory entityManagerFactory;

    @Autowired
    ResourceLoader resourceLoader;

    @Bean
    public FlatFileItemReader<RouteDTO> csvReader(){
        FlatFileItemReader<RouteDTO> reader = new FlatFileItemReader<>();
        reader.setResource(resourceLoader.getResource("file://" + applicationArguments.getNonOptionArgs().get(0)));
        reader.setLineMapper(new DefaultLineMapper<RouteDTO>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
                setNames(new String[] { "source", "target", "cost" });
            }});
            setFieldSetMapper(new BeanWrapperFieldSetMapper<RouteDTO>() {{
                setTargetType(RouteDTO.class);
            }});
        }});
        return reader;
    }

    @Bean
    public JpaItemWriter<RouteEntity> csvWriter() {
        JpaItemWriter<RouteEntity> itemWriter = new JpaItemWriter<>();
        itemWriter.setEntityManagerFactory(entityManagerFactory);
        return itemWriter;
    }

    @Bean
    public Step csvToDatabaseStep() {
        return stepBuilderFactory.get("csvToDatabaseStep")
                .<RouteDTO, RouteEntity>chunk(1)
                .reader(csvReader())
                .processor(new CvsProcessor())
                .writer(csvWriter())
                .build();
    }

    @Bean
    Job csvToDatabaseJob(JobCompletionNotificationListener listener) {
        return jobBuilderFactory.get("csvToDatabaseJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(csvToDatabaseStep())
                .end()
                .build();
    }


}

@Component
class JobCompletionNotificationListener extends JobExecutionListenerSupport {

    private static final Logger log = LoggerFactory.getLogger(JobCompletionNotificationListener.class);

    private final RouteRepository routeRepository;

    @Autowired
    public JobCompletionNotificationListener(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    @Override
    public void afterJob(JobExecution jobExecution) {

        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {

            log.info("============ JOB FINISHED ============");

            List<RouteEntity> results = routeRepository.findAll();

            for (RouteEntity route : results) {
                log.info("Discovered <" + route + "> in the database.");
            }

        } else {

            throw new RuntimeException("CSV File not found or malformed!");

        }
    }
}

class CvsProcessor implements ItemProcessor<RouteDTO, RouteEntity> {

    @Override
    public RouteEntity process(final RouteDTO routeDTO) {
        return new RouteEntity(routeDTO.getSource().toUpperCase(), routeDTO.getTarget().toUpperCase(), routeDTO.getCost());
    }

}