package cn.edu.njupt.zx120.shiro.spring;

import net.sf.ehcache.Ehcache;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.util.CollectionUtils;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.support.SimpleValueWrapper;

import java.util.*;

/**
 * Created by wallance on 2/22/17.
 */
public class SpringCacheManagerWrapper implements CacheManager {

    private EhCacheCacheManager cacheManager;

    public void setCacheManager(EhCacheCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <K, V> Cache<K, V> getCache(String name) throws CacheException {
        org.springframework.cache.Cache springCache = cacheManager.getCache(name);
        if (springCache == null) {
            cacheManager.getCacheManager().addCacheIfAbsent(name);
        }
        return new SpringCacheWrapper(springCache);
    }

    public <K, V> String[] getCacheNames() throws CacheException {
        return cacheManager.getCacheManager().getCacheNames();
    }

    static class SpringCacheWrapper implements Cache {

        private org.springframework.cache.Cache springCache;

        public SpringCacheWrapper(org.springframework.cache.Cache springCache) {
            this.springCache = springCache;
        }

        @Override
        public Object get(Object key) throws CacheException {
            Object value = springCache.get(key);
            System.out.println("======================================");
            System.out.println("获取缓存: " + key + "    " + value);
            System.out.println("======================================");
            if (value instanceof SimpleValueWrapper) {
                return((SimpleValueWrapper) value).get();
            }
            return value;
        }

        @Override
        public Object put(Object key, Object value) throws CacheException {
            System.out.println("======================================");
            System.out.println("放入缓存: " + key + "    " + value);
            System.out.println("======================================");
            springCache.put(key, value);
            return value;
        }

        @Override
        public Object remove(Object key) throws CacheException {
            Object value = this.get(key);
            if (value != null) {
                springCache.evict(key);
            }
            System.out.println("======================================");
            System.out.println("移除缓存: " + key + "    " + value);
            System.out.println("======================================");
            return value;
        }

        @Override
        public void clear() throws CacheException {
            springCache.clear();
        }

        @Override
        public int size() {
            if (springCache.getNativeCache() instanceof Ehcache) {
                Ehcache ehcache = (Ehcache) springCache.getNativeCache();
                return ehcache.getSize();
            }

            throw new UnsupportedOperationException();
        }

        @Override
        public Set keys() {
            if (springCache.getNativeCache() instanceof Ehcache) {
                Ehcache ehcache = (Ehcache) springCache.getNativeCache();
                return new HashSet(ehcache.getKeys());
            }

            throw new UnsupportedOperationException();
        }

        @Override
        public Collection values() {
            if (springCache.getNativeCache() instanceof Ehcache) {
                Ehcache ehcache = (Ehcache) springCache.getNativeCache();
                List keys = ehcache.getKeys();
                if (!CollectionUtils.isEmpty(keys)) {
                    List values = new ArrayList(keys.size());
                    for (Object key : keys) {
                        Object value = get(key);
                        if (value != null) {
                            values.add(value);
                        }
                    }
                    return Collections.unmodifiableList(values);
                } else {
                    return Collections.emptyList();
                }
            }
            throw new UnsupportedOperationException();
        }
    }
}
