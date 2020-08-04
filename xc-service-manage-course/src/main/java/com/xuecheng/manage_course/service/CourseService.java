package com.xuecheng.manage_course.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CategoryNode;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.domain.course.response.CourseCode;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.dao.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CourseService {
    @Resource
    TeachplanMapper teachplanMapper;
    @Autowired
    TeachplanRepository teachplanRepository;
    @Autowired
    CourseBaseRepository courseBaseRepository;
    @Resource
    CourseMapper courseMapper;
    @Autowired
    CourseMarketRepository courseMarketRepository;
    @Autowired
    CoursePicRepository coursePicRepository;
    @Resource
    CmsPageClient cmsPageClient;
    @Autowired
    CoursepubRepository coursepubRepository;
    @Autowired
    TeachplanMediaRepository teachplanMediaRepository;
    @Autowired
    TeachplanMediaPubRepository teachplanMediaPubRepository;
    @Value("${course-publish.dataUrlPre}")
    private String publish_dataUrlPre;
    @Value("${course-publish.pagePhysicalPath}")
    private String publish_page_physicalpath;
    @Value("${course-publish.pageWebPath}")
    private String publish_page_webpath;
    @Value("${course-publish.siteId}")
    private String publish_siteId;
    @Value("${course-publish.templateId}")
    private String publish_templateId;
    @Value("${course-publish.previewUrl}")
    private String previewUrl;
    public TeachplanNode findTeachplanList(String courseId)
    {
        TeachplanNode teachplanNode = teachplanMapper.selectList(courseId);
        return  teachplanNode;
    }
    //增删改一般都加事务
    @Transactional
    public ResponseResult addTeachplan(Teachplan teachplan) {
        if(teachplan==null || StringUtils.isEmpty(teachplan.getCourseid()) ||
                StringUtils.isEmpty(teachplan.getPname()))
        {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //获取课程id
        String courseid = teachplan.getCourseid();
        //获取parentid
        String parentid = teachplan.getParentid();
        //如果parentId为空
        if(StringUtils.isEmpty(parentid))
        {
            //获取课程的根结点
            parentid = getTeachplanRoot(courseid);
        }
        //创建一个新结点准备添加
        Teachplan teachplanNew = new Teachplan();
        //将teachplan的属性拷贝到teachplanNew中
        BeanUtils.copyProperties(teachplan,teachplanNew);
        //查找父级节点的grade
        Optional<Teachplan> optional = teachplanRepository.findById(parentid);
        Teachplan teachplan1 = optional.get();
        String grade = teachplan1.getGrade();
        //设置必要的属性
        teachplanNew.setParentid(parentid);
        if("1".equals(grade))
        {   teachplanNew.setGrade("2");

        }
        else {
            teachplanNew.setGrade("3");
        }
        teachplanNew.setStatus("0");//未发布
        teachplanRepository.save(teachplanNew);
        return new ResponseResult(CommonCode.SUCCESS);
    }
    //获取课程的根结点
    public String getTeachplanRoot(String courseId){
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if(!optional.isPresent())
        {
            return null;
        }
        CourseBase courseBase = optional.get();
        //获取根节点
        List<Teachplan> teachplanList =  teachplanRepository.findByCourseidAndParentid(courseId, "0");
        //如果根节点不存在
        if(teachplanList==null||teachplanList.size()<=0)
        {
            //添加一个新节点
            Teachplan teachplan= new Teachplan();
            teachplan.setCourseid(courseId);
            teachplan.setParentid("0");
            teachplan.setGrade("1");
            teachplan.setPname(courseBase.getName());
            teachplan.setStatus("0");
            teachplanRepository.save(teachplan);
            return teachplan.getId();

        }
        return teachplanList.get(0).getId();

    }
    //查询我的课程
    public QueryResponseResult<CourseInfo> findCourseList( String company_id,int page, int size, CourseListRequest courseListRequest)
    {
        if(courseListRequest==null)
        {
            courseListRequest=new CourseListRequest();
        }
        //将公司id参数传入dao
        courseListRequest.setCompanyId(company_id);
        if(page<=0)
        {
            page=0;
        }
        if (size<=0)
        {
            size=20;
        }
        PageHelper.startPage(page,size);
        //分页查询
        Page<CourseInfo> courseListPage = courseMapper.findCourseListPage(courseListRequest);
        List<CourseInfo> result = courseListPage.getResult();
        //总条数
        long total = courseListPage.getTotal();
        //查询结果集
        QueryResult<CourseInfo> courseInfoQueryResult= new QueryResult<>();
        courseInfoQueryResult.setList(result);
        courseInfoQueryResult.setTotal(total);
        return new QueryResponseResult<CourseInfo>(CommonCode.SUCCESS,courseInfoQueryResult);

    }
    //查询分类
    public CategoryNode findList(){

        return courseMapper.selectList();
    }
    //添加课程
    @Transactional
    public AddCourseResult addCourseBase(CourseBase courseBase){
        //添加的课程设置为未发布
        courseBase.setStatus("201001");
        CourseBase courseBase1 = courseBaseRepository.save(courseBase);
        return new AddCourseResult(CommonCode.SUCCESS,courseBase1.getId());
    }
    //查找课程基本信息
    public CourseBase getCoursebaseById(String courseid)
    {
        Optional<CourseBase> optional = courseBaseRepository.findById(courseid);
        if(optional.isPresent()){
            CourseBase courseBase = optional.get();
            return courseBase;
        }
        return null;
    }
    //更新课程基本信息
    @Transactional
    public ResponseResult updateCoursebase(String id,CourseBase courseBase){
        CourseBase one = getCoursebaseById(id);
        if(one==null){
            ExceptionCast.cast(CommonCode.SERVER_ERROR);
        }
        //修改课程信息
        one.setName(courseBase.getName());
        one.setMt(courseBase.getMt());
        one.setSt(courseBase.getSt());
        one.setGrade(courseBase.getGrade());
        one.setStudymodel(courseBase.getStudymodel());
        one.setUsers(courseBase.getUsers());
        one.setDescription(courseBase.getDescription());
        CourseBase courseBase1 = courseBaseRepository.save(one);
        return  new ResponseResult(CommonCode.SUCCESS);
    }
    //查询课程营销信息
    public CourseMarket getCourseMarketById(String courseId)
    {
        Optional<CourseMarket> optional = courseMarketRepository.findById(courseId);
        if(optional.isPresent())
        {
            return optional.get();
        }
        return null;
    }
    //修改课程营销信息
    @Transactional
    public CourseMarket  updateCourseMarket(String id,CourseMarket courseMarket){
        CourseMarket one = getCourseMarketById(id);
        if(one!=null)
        {
            one.setCharge(courseMarket.getCharge());
            one.setStartTime(courseMarket.getStartTime());
            one.setEndTime(courseMarket.getEndTime());
            one.setPrice(courseMarket.getPrice());
            one.setQq(courseMarket.getQq());
            one.setValid(courseMarket.getValid());
            courseMarketRepository.save(one);
        }else {

        one =new CourseMarket();
        BeanUtils.copyProperties(courseMarket,one);
        one.setId(id);
        courseMarketRepository.save(one);
        }
        return one;
    }
    //向课程管理数据添加课程与图片的关联信息
    @Transactional
     public ResponseResult addCoursePic(String courseId, String pic) {
        //课程图片信息
        CoursePic coursePic = null;
        Optional<CoursePic> optional = coursePicRepository.findById(courseId);
        if(optional.isPresent())
        {
            coursePic=optional.get();
        }
        if(coursePic==null)
        {
            coursePic= new CoursePic();
        }
        coursePic.setPic(pic);
        coursePic.setCourseid(courseId);
        coursePicRepository.save(coursePic);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    public CoursePic findCoursepic(String courseId) {
        Optional<CoursePic> optional = coursePicRepository.findById(courseId);
        if(optional.isPresent())
        {
            return optional.get();
        }
        return null;
    }
    @Transactional
    public ResponseResult deleteCoursePic(String courseId) {
        //执行删除，返回1表示删除成功，返回0表示删除失败
        long l = coursePicRepository.deleteByCourseid(courseId);
        if(l>0)
        {
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    public CourseView getCoruseView(String id) {
        CourseView courseView=new CourseView();
        //查询课程基本信息
        Optional<CourseBase> optional = courseBaseRepository.findById(id);
        if(optional.isPresent())
        {
            CourseBase courseBase = optional.get();
            courseView.setCourseBase(courseBase);
        }
        //查询图片信息
        Optional<CoursePic> picOptional = coursePicRepository.findById(id);
        if(picOptional.isPresent())
        {
            courseView.setCoursePic(picOptional.get());
        }
        //获取营销信息
        Optional<CourseMarket> marketOptional = courseMarketRepository.findById(id);
        if(marketOptional.isPresent())
        {
            CourseMarket courseMarket = marketOptional.get();
            courseView.setCourseMarket(courseMarket);
        }
        //获取教学计划
        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        courseView.setTeachplanNode(teachplanNode);
        return courseView;

    }
    //根据id查询课程基本信息
    public CourseBase findCourseBaseById(String courseId){
        Optional<CourseBase> baseOptional = courseBaseRepository.findById(courseId);
        if(baseOptional.isPresent()){
            CourseBase courseBase = baseOptional.get();
            return courseBase;
        }
        ExceptionCast.cast(CourseCode.COURSE_DENIED_DELETE);
        return null;
    }
    //课程预览
    public CoursePublishResult preview(String id) {
        //查询课程
        CourseBase courseBaseById = this.findCourseBaseById(id);
        //请求cms添加页面
        //准备cmsPage信息
        CmsPage cmsPage= new CmsPage();
        cmsPage.setSiteId(publish_siteId);//站点id
        cmsPage.setDataUrl(publish_dataUrlPre+id);//数据模型url
        cmsPage.setPageName(id+".html");//页面名称
        cmsPage.setPageAliase(courseBaseById.getName());//页面别名，就是课程名称
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);//页面物理路径
        cmsPage.setPageWebPath(publish_page_webpath);//页面webpath
        cmsPage.setTemplateId(publish_templateId);//页面模板id

        //远程调用cms
        CmsPageResult cmsPageResult = cmsPageClient.saveCms(cmsPage);
        if(!cmsPageResult.isSuccess())
        {
            return new CoursePublishResult(CommonCode.FAIL,null);
        }
        CmsPage cmsPage1 = cmsPageResult.getCmsPage();
        String pageId = cmsPage1.getPageId();
        //拼装页面预览的url
        String url = previewUrl+pageId;
        //返回CoursePublishResult对象（当中包含了页面预览的url）
        return new CoursePublishResult(CommonCode.SUCCESS,url);

    }
    //课程发布
    @Transactional
    public CoursePublishResult publish(String id) {
        //查询课程
        CourseBase courseBaseById = this.findCourseBaseById(id);

        //准备页面信息
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId(publish_siteId);//站点id
        cmsPage.setDataUrl(publish_dataUrlPre+id);//数据模型url
        cmsPage.setPageName(id+".html");//页面名称
        cmsPage.setPageAliase(courseBaseById.getName());//页面别名，就是课程名称
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);//页面物理路径
        cmsPage.setPageWebPath(publish_page_webpath);//页面webpath
        cmsPage.setTemplateId(publish_templateId);//页面模板id
        
        //调用一键发布
        CmsPostPageResult cmsPostPageResult = cmsPageClient.postPageQuick(cmsPage);
        if(!cmsPostPageResult.isSuccess()){
            return new CoursePublishResult(CommonCode.FAIL,null);
        }
        //如果发布成功则将保存课程的发布状态为“已发布”
        CourseBase courseBase = this.saveCoursePubState(id);
        if(courseBase==null)
        {
            return new CoursePublishResult(CommonCode.FAIL,null);
        }

        //保存课程索引信息
        //先创建一个coursePub对象
        CoursePub coursePub = createCoursePub(id);
        //将coursePub对象保存到数据库
         saveCoursePub(id, coursePub);
        //缓存课程的信息
        //...
        //得到页面的url
        String pageUrl = cmsPostPageResult.getPageUrl();
        //向teachplanMediaPub中保存课程媒资信息
        saveTeachplanMediaPub(id);
        return new CoursePublishResult(CommonCode.SUCCESS,pageUrl);

    }

    //向teachplanMediaPub中保存课程媒资信息
    private void saveTeachplanMediaPub(String courseId){
        //从teachplanMedia中查询
        List<TeachplanMedia> mediaList = teachplanMediaRepository.findByCourseId(courseId);
        List<TeachplanMediaPub> teachplanMediaPubs=new ArrayList<>();
        if(mediaList!=null){
            //先删除teachplanMediaPub中的数据
            teachplanMediaPubRepository.deleteByCourseId(courseId);
            for (TeachplanMedia teachplanMedia : mediaList) {
                        TeachplanMediaPub teachplanMediaPub=new TeachplanMediaPub();
                        BeanUtils.copyProperties(teachplanMedia,teachplanMediaPub);
                       //添加时间戳
                        teachplanMediaPub.setTimestamp(new Date());
                        teachplanMediaPubs.add(teachplanMediaPub);
            }
            //将teachplanMediaList插入到teachplanMediaPub
             teachplanMediaPubRepository.saveAll(teachplanMediaPubs);

        }
    }
    //更新课程状态为已发布 202002
    private CourseBase  saveCoursePubState(String courseId){
        CourseBase courseBase2 = this.findCourseBaseById(courseId);
        courseBase2.setStatus("202002");
        courseBaseRepository.save(courseBase2);
        return courseBase2;
    }
    //将coursePub对象保存到数据库
    private CoursePub saveCoursePub(String id,CoursePub coursePub){
        CoursePub coursePubNew = null;
        //根据课程id查询coursePub
        Optional<CoursePub> optionalCoursePub = coursepubRepository.findById(id);
        if(optionalCoursePub.isPresent())
        {
            coursePubNew=optionalCoursePub.get();
        }else {
            coursePubNew =new CoursePub();
        }
        //将coursePub对象中的信息保存到coursePubNew中
        BeanUtils.copyProperties(coursePub,coursePubNew);
        //在保存之前设置一些重要的信息
        coursePubNew.setId(id);
        //时间戳,给logstach使用
        coursePubNew.setTimestamp(new Date());
        //发布时间
        SimpleDateFormat simpleDateFormat =new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        String format = simpleDateFormat.format(new Date());
        coursePubNew.setPubTime(format);
        coursepubRepository.save(coursePubNew);
        return coursePubNew;

    }
    //创建coursePub对象
    private CoursePub createCoursePub(String id){
        CoursePub coursePub=new CoursePub();
        Optional<CourseBase> optionalCourseBase = courseBaseRepository.findById(id);
        if(optionalCourseBase.isPresent())
        {
            CourseBase courseBase = optionalCourseBase.get();
            //将courseBase属性拷贝到CoursePub中 第二个为目标参数
            BeanUtils.copyProperties(courseBase,coursePub);
        }
        //查询课程图片
        Optional<CoursePic> picOptional = coursePicRepository.findById(id);
        if(picOptional.isPresent()){
            CoursePic coursePic = picOptional.get();
            BeanUtils.copyProperties(coursePic, coursePub);
        }

        //课程营销信息
        Optional<CourseMarket> marketOptional = courseMarketRepository.findById(id);
        if(marketOptional.isPresent()){
            CourseMarket courseMarket = marketOptional.get();
            BeanUtils.copyProperties(courseMarket, coursePub);
        }

        //课程计划信息
        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        String jsonString = JSON.toJSONString(teachplanNode);
        coursePub.setTeachplan(jsonString);
        return  coursePub;

    }
    //保存课程计划与媒资文件的关联信息
    public ResponseResult savemedia(TeachplanMedia teachplanMedia) {
        if(teachplanMedia == null || StringUtils.isEmpty(teachplanMedia.getTeachplanId())){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
    }
        //校验课程计划是否是3级
        //课程计划
        String teachplanId = teachplanMedia.getTeachplanId();
        //查询到课程计划
        Optional<Teachplan> optional = teachplanRepository.findById(teachplanId);
        if(!optional.isPresent()){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //查询到教学计划
        Teachplan teachplan = optional.get();
        //取出等级
        String grade = teachplan.getGrade();
        if(StringUtils.isEmpty(grade) || !grade.equals("3")){
            //只允许选择第三级的课程计划关联视频
            ExceptionCast.cast(CourseCode.COURSE_MEDIA_TEACHPLAN_GRADEERROR);
        }
        //查询teachplanMedia
        Optional<TeachplanMedia> mediaOptional = teachplanMediaRepository.findById(teachplanId);
        TeachplanMedia one = null;
        if(mediaOptional.isPresent()){
            one = mediaOptional.get();
        }else{
            one = new TeachplanMedia();
        }

        //将one保存到数据库
        one.setCourseId(teachplan.getCourseid());//课程id
        one.setMediaId(teachplanMedia.getMediaId());//媒资文件的id
        one.setMediaFileOriginalName(teachplanMedia.getMediaFileOriginalName());//媒资文件的原始名称
        one.setMediaUrl(teachplanMedia.getMediaUrl());//媒资文件的url
        one.setTeachplanId(teachplanId);
        teachplanMediaRepository.save(one);

        return new ResponseResult(CommonCode.SUCCESS);


    }
}
