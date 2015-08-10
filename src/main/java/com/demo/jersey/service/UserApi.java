package com.demo.jersey.service;

import org.glassfish.jersey.server.mvc.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;
import com.demo.jersey.common.JedisTools;
import com.demo.jersey.repository.UserRepository;
import com.zhaopin.rabbitMQ.producer.ProducerMq;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/user")
public class UserApi {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserApi.class);

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ProducerMq producer;

	@Autowired
	private JedisTools jedisTools;

	@GET
	@Path("/{status}/count")
	@Produces(MediaType.TEXT_PLAIN)
	public int getUserCountByStatus(@PathParam("status") final int status) {
		jedisTools.addStringToJedis("helloworld-api", "test-jersey", 0);

		JSONObject object = new JSONObject();
		object.put("key", "helloworld-api");
		object.put("value", "test-jersey");
		object.put("ttl", 0);
		producer.convertAndSend("redistest", object);

		LOGGER.info("test jersey");

		return userRepository.getUserCountByStatus(status);
	}

	@GET
	@Path("/list")
	@Template()
	public String getUserList() {
		return "api";
	}
}
