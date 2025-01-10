package com.emmes.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.emmes.entity.Admin;
/**
 * 管理员Service接口
 */
public interface IAdminService extends IService<Admin> {

    /**
     * 修改
     * @param admin
     * @return
     */
    public Integer update(Admin admin);

}
