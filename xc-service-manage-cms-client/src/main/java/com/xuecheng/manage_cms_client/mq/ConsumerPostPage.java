package com.xuecheng.manage_cms_client.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.manage_cms_client.service.PageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ConsumerPostPage {
    private static  final Logger LOGGER = LoggerFactory.getLogger(ConsumerPostPage.class);
    @Autowired
    PageService pageService;
    @RabbitListener(queues = {"${xuecheng.mq.queue}"})
    public void listen(String msg)
    {
        //解析消息
        Map map = JSON.parseObject(msg, Map.class);
        String pageId =(String) map.get("pageId");
        //检验页面合法性
        CmsPage cmsPage = pageService.findCmsPageById(pageId);
        if(cmsPage==null)
        {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //把页面从grids下载下来并保存到物理地址
        pageService.savePageToServerPath(pageId);
    }
}
