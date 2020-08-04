package com.xuecheng.manage_cms_client.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import org.springframework.data.mongodb.repository.MongoRepository;

//参数类型为 第一个 与MongoDB对应的集合，第二个为对应的主键也就是是实体类中用@Id注解标注的
public interface CmsPageRepository extends MongoRepository<CmsPage,String> {
    //MongoRepository 提供了一些基本的方法,当然也可以自定义
    /*同Spring Data JPA一样Spring Data mongodb也提供自定义方法的规则，
    如下： 按照findByXXX，findByXXXAndYYY、countByXXXAndYYY等规则定义方法，实现查询操作。
    */
    //根据页面名称查询
    CmsPage findByPageName(String pageName);
    //根据页面名称、站点Id、页面webpath查询
    CmsPage findByPageNameAndSiteIdAndPageWebPath(String pageName, String siteId, String pageWebPath);

}
