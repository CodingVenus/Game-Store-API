package com.company.queueservice;

import com.company.queueservice.util.feign.LevelUpClient;
import com.company.queueservice.util.messages.LevelUpMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageListener {

    private final LevelUpClient client;

    @Autowired
    public MessageListener(LevelUpClient client) {
        this.client = client;
    }

    @RabbitListener(queues = QueueServiceApplication.QUEUE_NAME)
    public void receiveMessage(LevelUpMessage msg) {

        if(msg.getLevelUpId()==0){
            LevelUpMessage customerAccount = client.getLevelUpByCustomerId(msg.getCustomerId());
            msg.setLevelUpId(customerAccount.getLevelUpId());
            msg.setMemberDate(customerAccount.getMemberDate());
            msg.setPoints(msg.getPoints()+customerAccount.getPoints());
        }

        // Sending to Level Up Client
        System.out.println("Sending to Level Up! Service to update points: " + msg.toString());
        client.updateLevelUp(msg, msg.getLevelUpId());

    }

}
