package com.demo.springboot_vue.controller;

import com.demo.springboot_vue.entities.Activity;
import com.demo.springboot_vue.entities.HappyContent;
import com.demo.springboot_vue.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class JdbcController {

    @Autowired
    private MongoTemplate mongoTemplate;

    @RequestMapping("/initUser")
    public void init() {
        User user = new User();
        for (int count = 0; count < 10; count++) {
            user.setId(count + 1);
            user.setAccount("user" + count);
            user.setPassword("user" + count);
            user.setBirth("2000-10-10 10:10:10");
            user.setAdmin(false);
            mongoTemplate.insert(user);
        }
        user.setId(11);
        user.setAccount("admin");
        user.setPassword("admin");
        user.setBirth("2000-10-10 10:10:10");
        user.setAdmin(true);
        mongoTemplate.insert(user);
    }


    /**
     * 登录
     *
     * @param appUser
     * @return
     */
    @RequestMapping("/login")
    public User login(@RequestBody User appUser) {
        List<User> userList = mongoTemplate.find(new Query(Criteria.where("account").is(appUser.getAccount())), User.class);
        if (userList.size() == 0 || !appUser.getPassword().equals(userList.get(0).getPassword())) {
            return null;
        }
        return userList.get(0);
    }

    /**
     * admin登录
     * 默认登录admin 密码admin
     *
     * @param appUser
     * @return
     */
    @RequestMapping("/adminLogin")
    public Boolean adminLogin(@RequestBody User appUser) {
        if ("admin".equals(appUser.getAccount()) && "admin".equals(appUser.getPassword()) && appUser.getAdmin()) {
            return true;
        }
        return false;
    }

    /**
     * 注册用户
     *
     * @param appUser
     * @return
     */
    @RequestMapping("/createUser")
    public Boolean createUser(@RequestBody User appUser) {
        appUser.setId(mongoTemplate.findAll(User.class).size() + 1);
        mongoTemplate.insert(appUser);
        return true;
    }

    /**
     * 修改密码
     *
     * @param appUser
     * @return
     */
    @RequestMapping("/modifyPass")
    public Boolean modifyPass(@RequestBody User appUser) {
        Query query = new Query(Criteria.where("account").is(appUser.getAccount()));
        Update password = Update.update("password", appUser.getPassword());
        mongoTemplate.upsert(query, password, User.class);
        return true;
    }

    /**
     * 获取全部用户信息
     *
     * @return
     */
    @RequestMapping("/getAllUserInfo")
    public List<User> getAllUserInfo() {
        return mongoTemplate.find(new Query(Criteria.where("admin").is(false)), User.class);
    }

    /**
     * 获取用户信息
     *
     * @param appUser
     * @return
     */
    @RequestMapping("/getUserInfo")
    public User getUserInfo(@RequestBody User appUser) {
        List<User> userList = mongoTemplate.find(new Query(Criteria.where("account").is(appUser.getAccount())), User.class);
        return userList.get(0);
    }

    /**
     * 更改用户信息
     *
     * @param appUser
     * @return
     */
    @RequestMapping("/setUserInfo")
    public User setUserInfo(@RequestBody User appUser) {
        Query query = new Query(Criteria.where("account").is(appUser.getAccount()));
        Update update = Update.update("name", appUser.getName());
        mongoTemplate.upsert(query, update, User.class);
        update = Update.update("sex", appUser.getSex());
        mongoTemplate.upsert(query, update, User.class);
        update = Update.update("password", appUser.getPassword());
        mongoTemplate.upsert(query, update, User.class);
        update = Update.update("birth", appUser.getBirth());
        mongoTemplate.upsert(query, update, User.class);
        update = Update.update("image", appUser.getImage());
        mongoTemplate.upsert(query, update, User.class);
        return appUser;
    }

    /**
     * 管理员删除活动
     */
    @RequestMapping("/removeActivity")
    public Boolean removeActivity(@RequestBody Activity activity) {
        mongoTemplate.remove(new Query(Criteria.where("id").is(activity.getId())), Activity.class);
        return true;
    }

    /**
     * 查看所有发布的活动
     */
    @RequestMapping("/getAllActivity")
    public List<Activity> getAllActivity() {
        return mongoTemplate.findAll(Activity.class);
    }

    /**
     * 用户报名活动
     */
    @RequestMapping("/signActivity")
    public Boolean signActivity(@RequestBody Activity activity) {
        Boolean returnValue = true;
        //当前报名的用户
        User user = activity.getActivityUserList().get(0);
        Activity find = mongoTemplate.find(new Query(Criteria.where("id").is(activity.getId())), Activity.class).get(0);
        //已报名的user
        List<User> userList = find.getActivityUserList();
        if (userList == null || userList.size() == 0) {
            userList = new ArrayList<>();
        }
        for (User item : userList) {
            if (item.getId() == user.getId()) {
                returnValue = false;
                break;
            }
        }
        if (returnValue == false) {
            return false;
        }
        userList.add(user);
        Query query = new Query(Criteria.where("id").is(activity.getId()));
        Update update = Update.update("activityUserList", userList);
        mongoTemplate.upsert(query, update, Activity.class);
        User getUser = mongoTemplate.find(new Query(Criteria.where("id").is(user.getId())), User.class).get(0);
        List<Integer> signActivityList = getUser.getSignActivityList();
        if (signActivityList == null || signActivityList.size() == 0) {
            signActivityList = new ArrayList<>();
        }
        signActivityList.add(activity.getId());
        query = new Query(Criteria.where("account").is(user.getAccount()));
        update = Update.update("signActivityList", signActivityList);
        mongoTemplate.upsert(query, update, User.class);
        return true;
    }
    /**
     * 用户签到某次活动
     */
    @RequestMapping("/signOnActivity")
    public Boolean signOnActivity(@RequestBody Activity activity) {
        Query query = new Query(Criteria.where("id").is(activity.getId()));
        List<User> signUserList = activity.getSignUserList();
        int userId = signUserList.get(0).getId();
        activity = mongoTemplate.find(query, Activity.class).get(0);
        if (CollectionUtils.isEmpty(signUserList)) {
            signUserList = new ArrayList<>();
        }
        signUserList.add(signUserList.get(0));
        Update update = Update.update("signUserList", signUserList);
        mongoTemplate.upsert(query, update, Activity.class);
        List<User> activityUserList = activity.getActivityUserList();
        for (User item : activityUserList) {
            if (item.getId() == userId) {
                item.setSign(true);
            }
        }
        update = Update.update("activityUserList", activityUserList);
        mongoTemplate.upsert(query, update, Activity.class);
        query = new Query(Criteria.where("id").is(userId));
        User user = mongoTemplate.find(query, User.class).get(0);
        List<Integer> signOnActivityList = user.getSignOnActivityList();
        if (CollectionUtils.isEmpty(signOnActivityList)) {
            signOnActivityList = new ArrayList<>();
        }
        signOnActivityList.add(activity.getId());
        update = Update.update("signOnActivityList", signOnActivityList);
        mongoTemplate.upsert(query, update, User.class);
        return true;
    }

    /**
     * 用户签退活动（必须要传活动ID！！！）
     */
    @RequestMapping("/signOffActivity")
    public Boolean signOffActivity(@RequestBody Activity activity) {
        // 签退的用户
        int id = activity.getSignOffUser().getId();
        activity = mongoTemplate.find(new Query(Criteria.where("id").is(activity.getId())), Activity.class).get(0);
        List<User> userList = activity.getActivityUserList();
        List<User> newUserList = new ArrayList<>();
        for (User user : userList) {
            if (id != user.getId()) {
                newUserList.add(user);
            }
        }
        Query query = new Query(Criteria.where("id").is(activity.getId()));
        Update update = Update.update("activityUserList", newUserList);
        mongoTemplate.upsert(query, update, Activity.class);
        User user = mongoTemplate.find(new Query(Criteria.where("id").is(id)), User.class).get(0);
        List<Integer> signActivity = user.getSignActivityList();
        List<Integer> newList = new ArrayList<>();
        for (Integer activityId : signActivity) {
            if (activityId != activity.getId()) {
                newList.add(activityId);
            }
        }
        query = new Query(Criteria.where("id").is(id));
        update = Update.update("signActivityList", newList);
        mongoTemplate.upsert(query, update, User.class);
        return true;
    }
    /**
     * 管理员取消某次活动管理员
     */
    @RequestMapping("/removeActivityAdminUser")
    public List<User> removeActivityAdminUser(@RequestBody Activity activity) {
        Query query = new Query(Criteria.where("id").is(activity.getId()));
        Update update = Update.update("activityAdminUser", null);
        mongoTemplate.upsert(query, update, Activity.class);
        activity = mongoTemplate.find(query, Activity.class).get(0);
        List<User> accessList = activity.getAccessUserList();
        List<Integer> accessId = new ArrayList<>();
        for (User item : accessList) {
            accessId.add(item.getId());
        }
        List<User> activityUserList = activity.getActivityUserList();
        for (User user : activityUserList) {
            user.setActivityAdmin(false);
            if (!CollectionUtils.isEmpty(accessId) && accessId.indexOf(user.getId()) != -1) {
                user.setAccess(true);
                user.setSign(true);
            } else {
                user.setAccess(false);
                user.setSign(false);
            }
        }
        update = Update.update("activityAdminUser", null);
        mongoTemplate.upsert(query, update, Activity.class);
        return activityUserList;
    }
    /**
     * 管理员查看活动报名名单
     */
    @RequestMapping("/getUserListForActivity")
    public List<User> getUserListForActivity(@RequestBody Activity activity) {
        activity = mongoTemplate.find(new Query(Criteria.where("id").is(activity.getId())), Activity.class).get(0);
        List<User> userList = activity.getActivityUserList();
        List<User> accessList = activity.getAccessUserList();
        List<Integer> accessId = new ArrayList<>();
        if (!CollectionUtils.isEmpty(accessList)) {
            for (User access : accessList) {
                accessId.add(access.getId());
            }
        }
        int id = 0;
        if (activity.getActivityAdminUser() != null) {
            id = activity.getActivityAdminUser().getId();
        }
        for (User user : userList) {
            if (user.getId() == id) {
                user.setActivityAdmin(true);
            }
            if (!CollectionUtils.isEmpty(accessId) && accessId.indexOf(user.getId()) != -1) {
                user.setAccess(true);
            } else {
                user.setAccess(false);
            }
        }
        return userList;
    }

    /**
     * 管理员查看所有反馈 传活动id
     */
    @RequestMapping("/getAllSuggest")
    public List<String> getAllSuggest(@RequestBody Activity activity) {
        List<String> suggest = activity.getSuggestList();
        activity = mongoTemplate.find(new Query(Criteria.where("id").is(activity.getId())), Activity.class).get(0);
        return activity.getSuggestList();
    }
    /**
     * 管理员审核通过报名的用户
     */
    @RequestMapping("/accessUserForActivity")
    public Boolean accessUserForActivity(@RequestBody Activity activity) {
        List<User> accessUserList = activity.getAccessUserList();
        activity = mongoTemplate.find(new Query(Criteria.where("id").is(activity.getId())), Activity.class).get(0);
        // 寄存
        List<User> oldAccessUserList = activity.getAccessUserList();
        if (CollectionUtils.isEmpty(oldAccessUserList)) {
            oldAccessUserList = new ArrayList<>();
        }
        oldAccessUserList.add(accessUserList.get(0));
        for (User item : oldAccessUserList) {
            item.setAccess(true);
        }
        Query query = new Query(Criteria.where("id").is(activity.getId()));
        Update update = Update.update("accessUserList", oldAccessUserList);
        mongoTemplate.upsert(query, update, Activity.class);
        List<User> activityUserList = activity.getActivityUserList();
        for (User userItem : activityUserList) {
            if (userItem.getId() == accessUserList.get(0).getId()) {
                userItem.setAccess(true);
            }
        }
        update = Update.update("activityUserList", activityUserList);
        mongoTemplate.upsert(query, update, Activity.class);

        User user = mongoTemplate.find(new Query(Criteria.where("id").is(accessUserList.get(0).getId())), User.class).get(0);
        List<Integer> accessActivityList = user.getAccessActivityList();
        if (CollectionUtils.isEmpty(accessActivityList)) {
            accessActivityList = new ArrayList<>();
        }
        accessActivityList.add(activity.getId());
        query = new Query(Criteria.where("id").is(accessUserList.get(0).getId()));
        update = Update.update("accessActivityList", accessActivityList);
        mongoTemplate.upsert(query, update, User.class);
        return true;
    }


    /**
     * 用户签退后给活动提建议（活动id）
     */
    @RequestMapping("/addSuggest")
    public Boolean addSuggest(@RequestBody Activity activity) {
        List<String> suggest = activity.getSuggestList();
        activity = mongoTemplate.find(new Query(Criteria.where("id").is(activity.getId())), Activity.class).get(0);
        Query query = new Query(Criteria.where("id").is(activity.getId()));
        Update update = Update.update("suggestList", suggest);
        mongoTemplate.upsert(query, update, Activity.class);
        return true;
    }

    /**
     * 当前用户查看所有报名过的活动
     */
    @RequestMapping("/getAllActivityForUser")
    public List<Activity> getAllActivityForUser(@RequestBody User user) {
        user = mongoTemplate.find(new Query(Criteria.where("id").is(user.getId())), User.class).get(0);
        List<Integer> signActivityList = user.getSignActivityList();
        return mongoTemplate.find(new Query(Criteria.where("id").in(signActivityList)), Activity.class);
    }

    /**
     * 管理员发布活动
     */
    @RequestMapping("/setActivity")
    public Boolean setActivity(@RequestBody Activity activity) {
        activity.setId(mongoTemplate.findAll(Activity.class).size() + 1);
        mongoTemplate.insert(activity);
        return true;
    }

    /**
     * 管理员更新活动
     */
    @RequestMapping("/modifyActivity")
    public Boolean modifyActivity(@RequestBody Activity activity) {
        Query query = new Query(Criteria.where("id").is(activity.getId()));
        Update update = Update.update("image", activity.getImage());
        mongoTemplate.upsert(query, update, Activity.class);
        update = Update.update("content", activity.getContent());
        mongoTemplate.upsert(query, update, Activity.class);
        update = Update.update("title", activity.getTitle());
        mongoTemplate.upsert(query, update, Activity.class);
        return true;
    }

    /**
     * 管理员审核已经报名的用户（删除报名的用户）删除的用户给我放到list里面
     */
    @RequestMapping("/removeUserForActivity")
    public Boolean removeUserForActivity(@RequestBody Activity activity) {
        List<User> userList = activity.getActivityUserList();
        List<User> newUserList = new ArrayList<>();
        activity = mongoTemplate.find(new Query(Criteria.where("id").is(activity.getId())), Activity.class).get(0);
        for (User user : activity.getActivityUserList()) {
            if (user.getId() != userList.get(0).getId()) {
                newUserList.add(user);
            }
        }
        Query query = new Query(Criteria.where("id").is(activity.getId()));
        Update update = Update.update("activityUserList", newUserList);
        mongoTemplate.upsert(query, update, Activity.class);
        return true;
    }

    /**
     * 管理员设置某次活动管理员
     */
    @RequestMapping("/setActivityAdminUser")
    public Boolean setActivityAdminUser(@RequestBody Activity activity) {
        User user = activity.getActivityAdminUser();
        user.setActivityAdmin(true);
        Query query = new Query(Criteria.where("id").is(activity.getId()));
        Update update = Update.update("activityAdminUser", user);
        mongoTemplate.upsert(query, update, Activity.class);
        List<User> activityUserList = mongoTemplate.find(query, Activity.class).get(0).getActivityUserList();
        for (User item : activityUserList) {
            if (item.getId() == user.getId()) {
                item.setActivityAdmin(true);
            } else {
                item.setActivityAdmin(false);
            }
        }
        update = Update.update("activityUserList", activityUserList);
        mongoTemplate.upsert(query, update, Activity.class);
        return true;
    }

    /**
     * 管理员添加风采
     */
    @RequestMapping("/addHappyContent")
    public Boolean addHappyContent(@RequestBody HappyContent happyContent) {
        happyContent.setId(mongoTemplate.findAll(HappyContent.class).size() + 1);
        mongoTemplate.insert(happyContent);
        return true;
    }

    /**
     * 管理员删除风采
     */
    @RequestMapping("/removeHappyContent")
    public Boolean removeHappyContent(@RequestBody HappyContent happyContent) {
        mongoTemplate.remove(new Query(Criteria.where("id").is(happyContent.getId())));
        return true;
    }

    /**
     * 管理员修改风采
     */
    @RequestMapping("/modifyHappyContent")
    public HappyContent modifyHappyContent(@RequestBody HappyContent happyContent) {
        mongoTemplate.remove(new Query(Criteria.where("id").is(happyContent.getId())));
        mongoTemplate.insert(happyContent);
        return happyContent;
    }

    /**
     * 管理员查看风采
     */
    @RequestMapping("/getAllHappyContent")
    public List<HappyContent> getAllHappyContent() {
        return mongoTemplate.findAll(HappyContent.class);
    }
}
