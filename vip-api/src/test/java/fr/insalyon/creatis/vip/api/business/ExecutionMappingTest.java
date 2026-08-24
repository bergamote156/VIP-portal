package fr.insalyon.creatis.vip.api.business;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import fr.insalyon.creatis.vip.api.model.Execution;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ExecutionMappingTest {

    @Test
    public void testExecutionDeserializing() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        Execution execution = mapper.readValue(getClass().getResource("/jsonObjects/execution1.json"), Execution.class);
        Assertions.assertTrue(execution.getInputValuesForDisplay().isEmpty());
        Assertions.assertEquals(1, execution.getInputValuesForInit().size());
        Assertions.assertEquals(2, execution.getInputValuesForInit().getFirst().size());
        execution = mapper.readValue(getClass().getResource("/jsonObjects/execution1-with-input-list.json"), Execution.class);
        Assertions.assertTrue(execution.getInputValuesForDisplay().isEmpty());
        Assertions.assertEquals(1, execution.getInputValuesForInit().size());
        Assertions.assertEquals(2, execution.getInputValuesForInit().getFirst().size());

        // small deserializing test
        String newContent = mapper.writeValueAsString(execution);
        System.out.println(newContent);
    }

    @Test
    public void testExecutionSerializing() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        Execution execution = new Execution();
        execution.getInputValuesForDisplay().put("testInput", "testValue");
        String serializedExecution = mapper.writeValueAsString(execution);
        System.out.println(serializedExecution);
        JsonNode jsonNode = mapper.readTree(serializedExecution);
        Assertions.assertTrue(jsonNode.hasNonNull("inputValues"));
    }

}
