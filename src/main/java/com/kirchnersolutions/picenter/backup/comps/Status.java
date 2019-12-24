package com.kirchnersolutions.picenter.backup.comps;

import org.hibernate.loader.custom.ConstructorReturn;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class Status {

    private static String status = "idle";
    private static Queue queue = new LinkedList();

    public synchronized String getStatus(){
        return status;
    }

    private synchronized void setStatus(String status){
        this.status = status;
    }

    public synchronized void add(String task){
        queue.add(task);
    }

    public synchronized String currentTask(){
        Object task = queue.peek();
        if(task == null){
            setStatus("idle");
            return "empty";
        }
        return (String)task;
    }

    public synchronized String nextTask(){
        Object task = queue.peek();
        if(task == null){
            setStatus("idle");
            return "empty";
        }
        queue.remove();
        status = currentTask();
        return status;
    }

}
