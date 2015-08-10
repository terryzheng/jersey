package com.demo.jersey.common;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.exceptions.JedisException;

public class JedisTools {
	public static final Logger LOGGER = Logger.getLogger("JedisTools");

	private JedisPool jedisPool;
	private int dbIndex;

	public Jedis getJedis() throws JedisException {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
		} catch (JedisException e) {
			LOGGER.error("failed:jedisPool getResource.", e);
			if (jedis != null) {
				jedis.close();
			}
			e.printStackTrace();
		}
		return jedis;
	}

	public void release(Jedis jedis) {
		if (jedis != null)
			jedis.close();
	}

	public String addStringToJedis(String key, String value, int cacheSeconds) {
		Jedis jedis = null;
		String lastVal = null;
		try {
			jedis = this.getJedis();
			jedis.select(getDbIndex());
			lastVal = jedis.getSet(key, value);
			if (cacheSeconds != 0) {
				jedis.expire(key, cacheSeconds);
			}
			LOGGER.debug("succeed:" + key + "-" + value);
		} catch (Exception e) {
			LOGGER.error("failed:" + key + "-" + value, e);
		} finally {
			release(jedis);
		}
		return lastVal;
	}

	public void addStringToJedis(Map<String, String> batchData, int cacheSeconds) {
		Jedis jedis = null;
		try {
			jedis = this.getJedis();
			jedis.select(getDbIndex());
			Pipeline pipeline = jedis.pipelined();
			for (Map.Entry<String, String> element : batchData.entrySet()) {
				if (cacheSeconds != 0) {
					pipeline.setex(element.getKey(), cacheSeconds,
							element.getValue());
				} else {
					pipeline.set(element.getKey(), element.getValue());
				}
			}
			pipeline.sync();
			LOGGER.debug("succeed:" + batchData.size());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			release(jedis);
		}
	}

	public void addListToJedis(String key, List<String> list, int cacheSeconds) {
		if (list != null && list.size() > 0) {
			Jedis jedis = null;
			try {
				jedis = this.getJedis();
				jedis.select(getDbIndex());
				if (jedis.exists(key)) {
					jedis.del(key);
				}
				for (String aList : list) {
					jedis.rpush(key, aList);
				}
				if (cacheSeconds != 0) {
					jedis.expire(key, cacheSeconds);
				}
				LOGGER.debug("succeed:" + key + "-" + list.size());
			} catch (JedisException e) {
				LOGGER.error("failed:" + key + "-" + list.size(), e);
			} catch (Exception e) {
				LOGGER.error("failed:" + key + "-" + list.size(), e);
			} finally {
				release(jedis);
			}
		}
	}

	public void addToSetJedis(String key, String[] value) {
		Jedis jedis = null;
		try {
			jedis = this.getJedis();
			jedis.select(getDbIndex());
			jedis.sadd(key, value);
			LOGGER.debug("succeed:" + key + "-" + value);
		} catch (Exception e) {
			LOGGER.error("failed:" + key + "-" + value, e);
		} finally {
			release(jedis);
		}
	}

	public void removeSetJedis(String key, String value) {
		Jedis jedis = null;
		try {
			jedis = this.getJedis();
			jedis.select(getDbIndex());
			jedis.srem(key, value);
			LOGGER.debug("succeed:" + key + "-" + value);
		} catch (Exception e) {
			LOGGER.error("failed:" + key + "-" + value, e);
		} finally {
			release(jedis);
		}
	}

	public void pushDataToListJedis(String key, String data, int cacheSeconds) {
		Jedis jedis = null;
		try {
			jedis = this.getJedis();
			jedis.select(getDbIndex());
			jedis.rpush(key, data);
			if (cacheSeconds != 0) {
				jedis.expire(key, cacheSeconds);
			}
			LOGGER.debug("succeed:" + key + "-" + data);
		} catch (Exception e) {
			LOGGER.error("failed:" + key + "-" + data, e);
		} finally {
			release(jedis);
		}
	}

	public void pushDataToListJedis(String key, List<String> batchData,
			int cacheSeconds) {
		Jedis jedis = null;
		try {
			jedis = this.getJedis();
			jedis.select(getDbIndex());
			jedis.del(key);
			jedis.lpush(key, batchData.toArray(new String[batchData.size()]));
			if (cacheSeconds != 0)
				jedis.expire(key, cacheSeconds);
			LOGGER.debug("succeed:" + batchData != null ? batchData.size() : 0);
		} catch (Exception e) {
			LOGGER.error("failed:" + batchData != null ? batchData.size() : 0,
					e);
		} finally {
			release(jedis);
		}
	}

	/**
	 * 删除list中的元素
	 * 
	 * @param key
	 * @param values
	 * @param
	 */
	public void deleteDataFromListJedis(String key, List<String> values) {
		Jedis jedis = null;
		try {
			jedis = this.getJedis();
			jedis.select(getDbIndex());
			Pipeline pipeline = jedis.pipelined();
			if (values != null && !values.isEmpty()) {
				for (String val : values) {
					pipeline.lrem(key, 0, val);
				}
			}
			pipeline.sync();
			LOGGER.debug("succeed:" + values != null ? values.size() : 0);
		} catch (Exception e) {
			LOGGER.error("failed:" + values != null ? values.size() : 0, e);
		} finally {
			release(jedis);
		}
	}

	public void addHashMapToJedis(String key, Map<String, String> map,
			int cacheSeconds) {
		Jedis jedis = null;
		if (map != null && map.size() > 0) {
			try {
				jedis = this.getJedis();
				jedis.select(getDbIndex());
				jedis.hmset(key, map);
				if (cacheSeconds >= 0)
					jedis.expire(key, cacheSeconds);
				LOGGER.debug("succeed:" + key + "-" + map.size());
			} catch (Exception e) {
				LOGGER.error("failed:" + key + "-" + map.size(), e);
			} finally {
				release(jedis);
			}
		}
	}

	public void addHashMapToJedis(String key, String field, String value,
			int cacheSeconds) {
		Jedis jedis = null;
		try {
			jedis = this.getJedis();
			if (jedis != null) {
				jedis.select(getDbIndex());
				jedis.hset(key, field, value);
				if (cacheSeconds >= 0)
					jedis.expire(key, cacheSeconds);
				LOGGER.debug("succeed:" + key + "-" + field + "-" + value);
			}
		} catch (Exception e) {
			LOGGER.error("failed:" + key + "-" + field + "-" + value, e);
		} finally {
			release(jedis);
		}
	}

	public void updateHashMapToJedis(String key, String incrementField,
			long incrementValue, String dateField, String dateValue) {
		Jedis jedis = null;
		try {
			jedis = this.getJedis();
			jedis.select(getDbIndex());
			jedis.hincrBy(key, incrementField, incrementValue);
			jedis.hset(key, dateField, dateValue);
			LOGGER.debug("succeed:" + key + "-" + incrementField + "-"
					+ incrementValue);
		} catch (Exception e) {
			LOGGER.error("failed:" + key + "-" + incrementField + "-"
					+ incrementValue, e);
		} finally {
			release(jedis);
		}
	}

	public String getStringFromJedis(String key) {
		String value = null;
		Jedis jedis = null;
		try {
			jedis = this.getJedis();
			jedis.select(getDbIndex());
			if (jedis.exists(key)) {
				value = jedis.get(key);
				value = value != null && !"nil".equalsIgnoreCase(value) ? value
						: null;
				LOGGER.debug("succeed:" + key + "-" + value);
			}
		} catch (Exception e) {
			LOGGER.error("failed:" + key + "-" + value, e);
		} finally {
			release(jedis);
		}
		return value;
	}

	public List<String> getStringFromJedis(String[] keys) {
		Jedis jedis = null;
		try {
			jedis = this.getJedis();
			jedis.select(getDbIndex());
			return jedis.mget(keys);
		} catch (Exception e) {
			LOGGER.error("failed:" + Arrays.toString(keys), e);
		} finally {
			release(jedis);
		}
		return null;
	}

	public List<String> getListFromJedis(String key) {
		List<String> list = null;
		Jedis jedis = null;
		try {
			jedis = this.getJedis();
			jedis.select(getDbIndex());
			if (jedis.exists(key)) {
				list = jedis.lrange(key, 0, -1);
				LOGGER.debug("succeed:" + key + "-" + list != null ? list
						.size() : 0);
			}
		} catch (JedisException e) {
			LOGGER.error(
					"failed:" + key + "-" + list != null ? list.size() : 0, e);
		} finally {
			release(jedis);
		}
		return list;
	}

	public Set<String> getSetFromJedis(String key) {
		Set<String> list = null;
		Jedis jedis = null;
		try {
			jedis = this.getJedis();
			jedis.select(getDbIndex());
			if (jedis.exists(key)) {
				list = jedis.smembers(key);
				LOGGER.debug("succeed:" + key + "-" + list != null ? list
						.size() : 0);
			}
		} catch (Exception e) {
			LOGGER.error(
					"failed:" + key + "-" + list != null ? list.size() : 0, e);
		} finally {
			release(jedis);
		}
		return list;
	}

	public Map<String, String> getHashMapFromJedis(String key) {
		Map<String, String> hashMap = null;
		Jedis jedis = null;
		try {
			jedis = this.getJedis();
			jedis.select(getDbIndex());
			hashMap = jedis.hgetAll(key);
			LOGGER.debug("succeed:" + key + "-" + hashMap != null ? hashMap
					.size() : 0);
		} catch (Exception e) {
			LOGGER.error(
					"failed:" + key + "-" + hashMap != null ? hashMap.size()
							: 0, e);
		} finally {
			release(jedis);
		}
		return hashMap;
	}

	public String getHashMapValueFromJedis(String key, String field) {
		String value = null;
		Jedis jedis = null;
		try {
			jedis = this.getJedis();
			if (jedis != null) {
				jedis.select(getDbIndex());
				if (jedis.exists(key)) {
					value = jedis.hget(key, field);
					LOGGER.debug("succeed:" + key + "-" + field + "-" + value);
				}
			}
		} catch (Exception e) {
			LOGGER.error("failed:" + key + "-" + field + "-" + value, e);
		} finally {
			release(jedis);
		}
		return value;
	}

	public Long getIdentifyId(String identifyName) {
		Jedis jedis = null;
		Long identify = null;
		try {
			jedis = this.getJedis();
			if (jedis != null) {
				jedis.select(getDbIndex());
				identify = jedis.incr(identifyName);
				if (identify == 0) {
					return jedis.incr(identifyName);
				} else {
					return identify;
				}
			}
		} catch (Exception e) {
			LOGGER.error("failed:" + identifyName + "-" + identify, e);
		} finally {
			release(jedis);
		}
		return null;
	}

	public Long delKeyFromJedis(String key) {
		Jedis jedis = null;
		long result = 0;
		try {
			jedis = this.getJedis();
			jedis.select(getDbIndex());
			LOGGER.debug("succeed:delKeyFromJedis");
			return jedis.del(key);
		} catch (Exception e) {
			LOGGER.error("failed:delKeyFromJedis", e);
		} finally {
			release(jedis);
		}
		return result;
	}

	public void flushDBFromJedis(int dbIndex) {
		Jedis jedis = null;
		try {
			jedis = this.getJedis();
			jedis.select(dbIndex);
			jedis.flushDB();
			LOGGER.debug("succeed:flushDBFromJedis");
		} catch (Exception e) {
			LOGGER.error("failed:flushDBFromJedis", e);
		} finally {
			release(jedis);
		}
	}

	public boolean existKey(String key) {
		Jedis jedis = null;
		try {
			jedis = this.getJedis();
			jedis.select(getDbIndex());
			LOGGER.debug("succeed:" + key);
			return jedis.exists(key);
		} catch (Exception e) {
			LOGGER.error("failed:" + key, e);
		} finally {
			release(jedis);
		}
		return false;
	}

	public JedisPool getJedisPool() {
		return jedisPool;
	}

	public void setJedisPool(JedisPool jedisPool) {
		this.jedisPool = jedisPool;
	}

	public int getDbIndex() {
		return dbIndex;
	}

	public void setDbIndex(int dbIndex) {
		this.dbIndex = dbIndex;
	}

}