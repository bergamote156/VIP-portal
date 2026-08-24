package fr.insalyon.creatis.vip.application.models;

import com.fasterxml.jackson.annotation.JsonView;
import fr.insalyon.creatis.boutiques.model.Input;
import fr.insalyon.creatis.vip.application.models.boutiquesTools.BoutiquesInput;
import fr.insalyon.creatis.vip.core.server.inter.DataViews;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@JsonView(DataViews.User.class)
public class WorkflowInput {

    private List<String> values;
    @Size(max=3)
    private List<Double> interval;

    public static WorkflowInput ofList(List<String> values) {
        WorkflowInput workflowInput = new WorkflowInput();
        workflowInput.setValues(values);
        return workflowInput;
    }

    public static WorkflowInput ofValue(String value) {
        return ofList(List.of(value));
    }

    public static WorkflowInput ofinterval(List<Double> values) {
        WorkflowInput workflowInput = new WorkflowInput();
        workflowInput.setInterval(values);
        return workflowInput;
    }

    public boolean isInterval() {
        return interval != null;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public List<Double> getInterval() {
        return interval;
    }

    public void setInterval(List<Double> interval) {
        this.interval = interval;
    }
}
