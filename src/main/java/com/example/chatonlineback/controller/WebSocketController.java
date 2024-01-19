/*
package com.example.chatonlineback.controller;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;


@Controller
public class WebSocketController {

    private final SimpMessagingTemplate simpMessagingTemplate;

    public WebSocketController(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @MessageMapping("/notify/{username}")
    @SendTo("/topic/notifications/{username}")
    public String notifyUser(String message) {
        // Process the notification if needed (e.g., store it in the database)
        // You can also use a more complex object instead of a simple string for the message
        System.out.println("message ="+ message);

        return message;
    }
}
*/
