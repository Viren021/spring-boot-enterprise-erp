package com.example.erp_service_ai.service;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

@Service
public class AiHrService {

    private final ChatModel chatModel;

    public AiHrService(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public String analyzeAttritionRisk(String employeeData) {
        String prompt = "You are an expert HR Director. Review the following employee data: " + employeeData + ". " +
                "Identify which employee is at the highest risk of quitting (attrition). " +
                "Briefly explain why in 2 to 3 sentences based on their lack of promotion and high sick days.";

        System.out.println("🧑‍💼 Sending HR data to Llama 3.2 for flight risk analysis...");

        return chatModel.call(prompt);
    }
}