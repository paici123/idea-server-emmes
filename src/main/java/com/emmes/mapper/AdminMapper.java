package com.emmes.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.emmes.entity.Admin;
/**
 * 管理员Mapper接口
 */
public interface AdminMapper extends BaseMapper<Admin> {

    /**
     * 修改
     * @param admin
     * @return
     */
    public Integer update(Admin admin);

}
