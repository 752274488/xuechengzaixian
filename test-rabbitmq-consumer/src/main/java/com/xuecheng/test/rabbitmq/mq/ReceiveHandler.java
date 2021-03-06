package com.xuecheng.test.rabbitmq.mq;

import com.xuecheng.test.rabbitmq.config.RabbitmqConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ReceiveHandler {
    @RabbitListener(queues = {RabbitmqConfig.QUEUE_INFORM_EMAIL})
    public void accept(String msg)
    {
        System.out.println(msg);
    }
}
