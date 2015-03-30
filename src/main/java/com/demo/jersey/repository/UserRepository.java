package com.demo.jersey.repository;

import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository {
	@Select("SELECT COUNT(*) FROM user_info WHERE usr_status = #{status}")
	int getUserCountByStatus(@Param("status") short status);
}
