package com.example.redisdemo.api.vote;

import com.example.redisdemo.exception.VoteException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisCluster;

import java.util.*;

/**
 * @author nsk
 * 2018/12/26 19:36
 */
@Service
public class ArticleVoteService {

    private static final int ONE_WEEK_IN_SECONDS = 7 * 86400;
    private static final int VOTE_SCORE = 432;
    private static final int ARTICLES_PER_PAGE = 1;
    private static final String ARTICLE = "article:";

    @Autowired
    private JedisCluster jedisCluster;

    /**
     * 文章发布信息
     *
     * @param user  作者
     * @param title 标题
     * @param link  相关链接
     * @return
     */
    public String publishArticles(String user, String title, String link) {
        String articleId = String.valueOf(jedisCluster.incr(ARTICLE));
        String voted = "voted:" + articleId;
        // 默认本人已投票
        jedisCluster.sadd(voted, user);
        // 设置投票有效时间为一周
        jedisCluster.expire(voted, ONE_WEEK_IN_SECONDS);
        double now = System.currentTimeMillis() / 1000d;
        String article = ARTICLE + articleId;
        Map<String, String> articleData = new HashMap<>();
        articleData.put("title", title);
        articleData.put("link", link);
        articleData.put("user", user);
        articleData.put("now", String.valueOf(now));
        articleData.put("votes", "1");
        // 保存文章相关信息
        jedisCluster.hmset(article, articleData);
        // 设置投票分数集合排序
        jedisCluster.zadd("score:", now + VOTE_SCORE, article);
        // 设置发布时间排序
        jedisCluster.zadd("time:", now, article);
        return articleId;
    }

    /**
     * 根据文章id获取文章信息
     *
     * @param articleId 文章id
     * @return
     */
    public Map<String, String> getArticleById(String articleId) {
        return jedisCluster.hgetAll(ARTICLE + articleId);
    }

    /**
     * 文章投票
     *
     * @param articleId 文章id
     * @param user      投票人
     * @return
     */
    public Map<String, String> voteArticle(String articleId, String user) {
        long cutoff = (System.currentTimeMillis() / 1000) - ONE_WEEK_IN_SECONDS;
        if (jedisCluster.zscore("time:", ARTICLE + articleId) < cutoff) {
            throw new VoteException("已过投票时间，不能继续投票");
        }
        if (jedisCluster.sadd("voted:" + articleId, user) == 1) {
            jedisCluster.zincrby("score:", VOTE_SCORE, ARTICLE + articleId);
            jedisCluster.hincrBy(ARTICLE + articleId, "votes", 1);
        }
        return getArticleById(articleId);
    }


    /**
     * 根据排序规则获取文章信息
     *
     * @param page 当前页
     * @param rule 排序规则
     * @return
     */
    public List<Map<String, String>> getArticles(int page, String rule) {
        String order = rule + ":";
        int start = (page - 1) * ARTICLES_PER_PAGE;
        int end = start + ARTICLES_PER_PAGE - 1;
        Set<String> ids = jedisCluster.zrevrange(order, start, end);
        List<Map<String, String>> articles = new ArrayList<>();
        for (String id : ids) {
            Map<String, String> articleData = jedisCluster.hgetAll(id);
            articleData.put("id", id);
            articles.add(articleData);
        }
        return articles;
    }

    /**
     * 对文章进行分组
     *
     * @param articleId
     * @param groupIds
     */
    public void addArticleToGroups(String articleId, String groupIds) {
        String[] groupIdArray = groupIds.split(",");
        for (String group : groupIdArray) {
            jedisCluster.sadd("group:" + group, ARTICLE + articleId);
        }
    }

    /**
     * 获取分组中所有文章信息
     *
     * @param groupName 分组
     * @param page      当前页数
     * @return
     */
    public List<Map<String, String>> getGroupArticles(String groupName, int page, String rule) {
        String order = rule + ":";
        int start = (page - 1) * ARTICLES_PER_PAGE;
        int end = start + ARTICLES_PER_PAGE;
        Set<String> ids = jedisCluster.zrevrange(order, 0, -1);
        List<Map<String, String>> articles = new ArrayList<>();
        Set<String> groupIds = jedisCluster.smembers("group:" + groupName);
        for (String id : ids) {
            if (groupIds.contains(id)) {
                Map<String, String> articleData = jedisCluster.hgetAll(id);
                articleData.put("id", id);
                articles.add(articleData);
            }
        }
        if (articles.size() <= start) {
            return new ArrayList<>();
        } else if (articles.size() >= end) {
            return articles.subList(start, end);
        } else {
            return articles.subList(start, articles.size());
        }
    }

}
