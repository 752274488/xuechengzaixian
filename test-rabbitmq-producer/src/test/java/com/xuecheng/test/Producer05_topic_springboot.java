package com.xuecheng.test;

import com.xuecheng.test.config.RabbitmqConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * 原始的rabbitmq ,与springboot结合版本)
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class Producer05_topic_springboot {
    @Resource
    RabbitTemplate rabbitTemplate;
    //使用rabbitTemplate发送消息
    @Test
    public void testrabblit()
    {
        String message = "send email message to user";
        /**
         * 参数：
         * 1、交换机名称
         * 2、routingKey
         * 3、消息内容
         */
        rabbitTemplate.convertAndSend(RabbitmqConfig.EXCHANGE_TOPICS_INFORM,"inform.email",message);
    }
}
