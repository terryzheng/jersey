package com.demo.jersey.repository;

import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository {
	@Select("SELECT COUNT(*) FROM staff WHERE status = #{status}")
	int getUserCountByStatus(@Param("status") int status);
}
