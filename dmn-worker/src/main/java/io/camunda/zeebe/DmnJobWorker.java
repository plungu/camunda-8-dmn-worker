package io.camunda.zeebe;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.dmn.DmnRepository;
import io.camunda.zeebe.spring.client.annotation.JobWorker;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DmnJobWorker {

  private static Logger log = LoggerFactory.getLogger(DmnJobWorker.class);
  private static final String DECISION_ID_HEADER = "decisionRef";

  private ZeebeClient client;
  private DmnRepository repository;
  private DmnEngine dmnEngine;

  @Autowired
  public DmnJobWorker(ZeebeClient client, DmnRepository repository, DmnEngine dmnEngine) {
    this.repository = repository;
    this.dmnEngine = dmnEngine;
    this.client = client;
  }

  @JobWorker(type = "DMN", autoComplete = false) // autoComplete = true as default value
  public void handleDMNJobSync(final ActivatedJob job) {

    final DmnDecision decision = findDecisionForTask(job);
    final Map<String, Object> variables = job.getVariablesAsMap();

    final DmnDecisionResult decisionResult = dmnEngine.evaluateDecision(decision, variables);

    logJob(job, decisionResult);

    client.newCompleteCommand(job.getKey())
        .variables(Collections.singletonMap("dmnResult", decisionResult))
        .send().join();
  }

  private DmnDecision findDecisionForTask(ActivatedJob job) {

    final String decisionId = job.getCustomHeaders().get(DECISION_ID_HEADER);
    if (decisionId == null || decisionId.isEmpty()) {
      throw new RuntimeException(String.format("Missing header: '%d'", DECISION_ID_HEADER));
    }

    final DmnDecision decision = repository.findDecisionById(decisionId);
    if (decision == null) {
      throw new RuntimeException(String.format("No decision found with id: '%s'", decisionId));
    }
    return decision;
  }

  private static void logJob(final ActivatedJob job, Object parameterValue) {
    log.info(
      "complete job\n>>> [type: {}, key: {}, element: {}, workflow instance: {}]\n{deadline; {}]\n[headers: {}]\n[variable parameter: {}\n[variables: {}]",
      job.getType(),
      job.getKey(),
      job.getElementId(),
      job.getProcessInstanceKey(),
      Instant.ofEpochMilli(job.getDeadline()),
      job.getCustomHeaders(),
      parameterValue,
      job.getVariables());
  }

}
