package com.campeat.app.model;

import java.util.List;
import java.util.Map;

public class CustomizeOptionModel {

    private String key;
    private String label;
    private String type; // "single" atau "multiple"
    private Map<String, ChoiceModel> choices;

    public CustomizeOptionModel() {}

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Map<String, ChoiceModel> getChoices() { return choices; }
    public void setChoices(Map<String, ChoiceModel> choices) { this.choices = choices; }
}