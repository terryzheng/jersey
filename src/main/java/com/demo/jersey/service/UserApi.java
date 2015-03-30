package com.demo.jersey.service;

import org.springframework.beans.factory.annotation.Autowired;
import com.demo.jersey.repository.UserRepository;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/user")
public class UserApi {

	@Autowired
	private UserRepository userRepository;

	@GET
	@Path("/{status}/count")
	@Produces(MediaType.TEXT_PLAIN)
	public int getUserCountByStatus(@PathParam("status") final Short status) {
		return userRepository.getUserCountByStatus(status);
	}
}
