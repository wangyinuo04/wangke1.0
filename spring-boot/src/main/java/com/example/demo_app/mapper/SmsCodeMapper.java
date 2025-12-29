package com.example.demo_app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo_app.entity.SmsCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SmsCodeMapper extends BaseMapper<SmsCode> {

    @Select("SELECT * FROM SMS_Code WHERE contact = #{contact} AND code = #{code} AND type = #{type} AND used = 0 AND expire_time > NOW() ORDER BY create_time DESC LIMIT 1")
    SmsCode findValidCode(String contact, String code, String type);

    @Update("UPDATE SMS_Code SET used = 1 WHERE id = #{id}")
    int markAsUsed(Integer id);

    @Select("SELECT COUNT(*) FROM SMS_Code WHERE contact = #{contact} AND type = #{type} AND create_time > DATE_SUB(NOW(), INTERVAL 1 MINUTE)")
    int countRecentCodes(String contact, String type);
}
