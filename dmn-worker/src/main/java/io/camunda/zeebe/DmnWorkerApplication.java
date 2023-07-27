package io.camunda.zeebe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.camunda.zeebe.spring.client.annotation.Deployment;

@SpringBootApplication
@Deployment(resources = "classpath:test-external-dmn.bpmn")
public class DmnWorkerApplication {

  public static void main(final String... args) {
    SpringApplication.run(DmnWorkerApplication.class, args);
  }

}
