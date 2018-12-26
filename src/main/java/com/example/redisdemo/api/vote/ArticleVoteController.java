package com.example.redisdemo.api.vote;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author nsk
 * 2018/12/26 19:36
 */
@RestController
@RequestMapping("/articles")
public class ArticleVoteController {

    @Autowired
    private ArticleVoteService articleVoteService;

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity publishArticles(@RequestParam String username,
                                          @RequestParam String title,
                                          @RequestParam String link) {
        String articleId = articleVoteService.publishArticles(username, title, link);
        Map<String, String> criteria = new HashMap<>();
        criteria.put("articleId", articleId);
        return ResponseEntity.ok(criteria);
    }

    @RequestMapping(path = "/{articleId}", method = RequestMethod.GET)
    public ResponseEntity getArticleById(@PathVariable String articleId) {
        Map<String, String> article = articleVoteService.getArticleById(articleId);
        return ResponseEntity.ok(article);
    }

    @RequestMapping(path = "/{articleId}/vote", method = RequestMethod.PATCH)
    public ResponseEntity voteArticle(@PathVariable String articleId, @RequestParam String user) {
        Map<String, String> article = articleVoteService.voteArticle(articleId, user);
        return ResponseEntity.ok(article);
    }

    @RequestMapping(path = "/sort", method = RequestMethod.GET)
    public ResponseEntity sortArticles(@RequestParam(defaultValue = "time") String rule,
                                       @RequestParam(defaultValue = "1") Integer page) {
        List<Map<String, String>> articles = articleVoteService.getArticles(page, rule);
        return ResponseEntity.ok(articles);
    }

    @RequestMapping(path = "/{articleId}/groups", method = RequestMethod.POST)
    public ResponseEntity addArticleToGroups(@PathVariable String articleId,
                                             @RequestParam String groupIds) {
        articleVoteService.addArticleToGroups(articleId, groupIds);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(path = "/groups", method = RequestMethod.GET)
    public ResponseEntity getGroupArticles(@RequestParam String group,
                                           @RequestParam(defaultValue = "1") Integer page,
                                           @RequestParam(defaultValue = "time") String rule) {
        List<Map<String, String>> articles = articleVoteService.getGroupArticles(group, page,rule);
        return ResponseEntity.ok(articles);
    }
}
