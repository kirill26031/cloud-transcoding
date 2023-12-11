package com.example.videotranscoder.service;

import io.swagger.models.auth.In;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;

@Service
public class ExecutorsService {
    private ArrayList<String> executorIds = new ArrayList<>();
    private HashMap<String, Integer> amountOfTasks = new HashMap<>();

    public ExecutorsService() {
        executorIds.add("i-02bf32802bd0323ac");
        for (String executorId : executorIds) {
            amountOfTasks.put(executorId, 0);
        }
    }

    public void addExecutor(String executorId){
        if (!executorIds.contains(executorId)){
            executorIds.add(executorId);
        }
    }

    public ArrayList<String> getExecutorIds() {
        return executorIds;
    }

    public String getAvailableExecutor() {
        Integer minTasksAmount = -1;
        String bestExecutor = executorIds.get(0);
        for (String executorId : executorIds) {
            Integer tasksAmount = amountOfTasks.get(executorId);
            if (minTasksAmount > tasksAmount) {
                minTasksAmount = tasksAmount;
                bestExecutor = executorId;
            }
        }
        amountOfTasks.put(bestExecutor, amountOfTasks.get(bestExecutor) + 1);
        return bestExecutor;
    }

    public void decrementTaskAmountForExecutor(String executor) {
        if (amountOfTasks.containsKey(executor)) {
            amountOfTasks.put(executor, amountOfTasks.get(executor) - 1);
        }
    }
}
