package com.redislabs.university.RU102J.dao;

import com.redislabs.university.RU102J.api.Site;
import com.redislabs.university.RU102J.core.KeyHelper;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Response;

import java.util.*;

public class SiteDaoRedisImpl implements SiteDao {
    private final JedisPool jedisPool;

    public SiteDaoRedisImpl(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    // When we insert a site, we set all of its values into a single hash.
    // We then store the site's id in a set for easy access.
    @Override
    public void insert(Site site) {
        try (Jedis jedis = jedisPool.getResource()) {
            String hashKey = RedisSchema.getSiteHashKey(site.getId());
            String siteIdKey = RedisSchema.getSiteIDsKey();
            jedis.hmset(hashKey, site.toMap());
            jedis.sadd(siteIdKey, hashKey);
        }
    }

    @Override
    public Site findById(long id) {
        try(Jedis jedis = jedisPool.getResource()) {
            String key = RedisSchema.getSiteHashKey(id);
            Map<String, String> fields = jedis.hgetAll(key);
            if (fields == null || fields.isEmpty()) {
                return null;
            } else {
                return new Site(fields);
            }
        }
    }

    // Challenge #1
    @Override
    public Set<Site> findAll() {
        // START Challenge #1
        Set<Site> result = new HashSet<>();

        try(Jedis jedis = jedisPool.getResource()) {
            Set<String> keys = jedis.keys(KeyHelper.getKey("sites:info:[0-9]*"));
            for(String key : keys)
                result.add(new Site(jedis.hgetAll(key)));
        }

        return result;
        // END Challenge #1
    }
}
