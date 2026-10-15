package com.itheima.knowledge_platform.service.Impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itheima.knowledge_platform.constant.JwtClaimsConstant;
import com.itheima.knowledge_platform.constant.MessageConstant;
import com.itheima.knowledge_platform.constant.StatusConstant;
import com.itheima.knowledge_platform.context.BaseContext;
import com.itheima.knowledge_platform.enums.PlatformAdminTypeEnum;
import com.itheima.knowledge_platform.exception.AccountLockedException;
import com.itheima.knowledge_platform.exception.AccountNotFoundException;
import com.itheima.knowledge_platform.exception.PasswordErrorException;
import com.itheima.knowledge_platform.exception.PlatformAdminErrorException;
import com.itheima.knowledge_platform.mapper.PlatformAdminMapper;
import com.itheima.knowledge_platform.pojo.dto.PlatformAdminAddDTO;
import com.itheima.knowledge_platform.pojo.dto.PlatformAdminLoginDTO;
import com.itheima.knowledge_platform.pojo.entity.PlatformAdmin;
import com.itheima.knowledge_platform.pojo.vo.PlatformAdminLoginVO;
import com.itheima.knowledge_platform.properties.JwtProperties;
import com.itheima.knowledge_platform.result.PageResult;
import com.itheima.knowledge_platform.service.PlatformAdminService;
import com.itheima.knowledge_platform.utils.JwtUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class PlatformAdminServiceImpl implements PlatformAdminService {

    @Autowired
    private PlatformAdminMapper platformAdminMapper;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 管理员登录
     *
     * @param platformAdminLoginDTO
     * @return
     */
    @Override
    public PlatformAdminLoginVO login(PlatformAdminLoginDTO platformAdminLoginDTO) {
        //1.验证账号密码
        String adminAccount = platformAdminLoginDTO.getAdminAccount();
        String adminPwd = platformAdminLoginDTO.getAdminPwd();

        PlatformAdmin platformAdmin = platformAdminMapper.getByAdminAccount(adminAccount);

        if (platformAdmin == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        //后期需要进行md5加密，然后再进行比对
        //对前端传过来的明文密码进行md5加密处理
        adminPwd = DigestUtils.md5DigestAsHex(adminPwd.getBytes());

        if (!adminPwd.equals(platformAdmin.getAdminPwd())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (platformAdmin.getStatus() == StatusConstant.DISABLE) {
            //账号禁用
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.ADMIN_ID, platformAdmin.getId());
        claims.put(JwtClaimsConstant.ADMIN_ACCOUNT, platformAdmin.getAdminAccount());
        claims.put(JwtClaimsConstant.ADMIN_TYPE, platformAdmin.getAdminType());

        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),  //获取管理员专用的 JWT 密钥（用于签名，保证token安全不被篡改）
                jwtProperties.getAdminTtl(),         //获取管理员 token 的有效时长（过期时间，单位一般是毫秒）
                claims
        );

        PlatformAdminLoginVO loginVO = PlatformAdminLoginVO.builder()
                .id(platformAdmin.getId())
                .adminAccount(platformAdmin.getAdminAccount())
                .adminType(platformAdmin.getAdminType())
                .token(token)
                .roleName(platformAdmin.getAdminType() == 1 ? "超级管理员" : "子管理员")
                .build();

        return loginVO;
    }

    /**
     * 添加子管理员
     *
     * @param platformAdminAddDTO
     * @return
     */
    @Override
    public PlatformAdmin add(PlatformAdminAddDTO platformAdminAddDTO) {
        //校验当前登录的管理员是否是超管（admin_type=1），不是则返回403
        Long currentAdminId = BaseContext.getCurrentAdminId();
        PlatformAdmin platformAdmin = platformAdminMapper.selectById(currentAdminId);

        if (!Objects.equals(platformAdmin.getAdminType(), PlatformAdminTypeEnum.SUPER_ADMIN.getCode())) {
            throw new PlatformAdminErrorException(MessageConstant.ADMIN_NO_PERMISSION_TO_MODIFY);
        }

        //校验 adminAccount 是否已存在，存在则返回400（账号已存在）
        PlatformAdmin oldAccount = platformAdminMapper.getByAdminAccount(platformAdminAddDTO.getAdminAccount());
        if (oldAccount != null) {
            throw new PlatformAdminErrorException(MessageConstant.ADMIN_HAS_BEEN_EXIST);
        }

        //对 adminPwd 进行加密
        platformAdminAddDTO.setAdminPwd(DigestUtils.md5DigestAsHex(platformAdminAddDTO.getAdminPwd().getBytes()));

        //插入管理员表，admin_type 固定为0，status 默认为1（启用）
        PlatformAdmin newAccount = new PlatformAdmin();
        BeanUtils.copyProperties(platformAdminAddDTO, newAccount);
        newAccount.setStatus(StatusConstant.ENABLE);
        newAccount.setCreatedAt(LocalDateTime.now());

        platformAdminMapper.insert(newAccount);

        newAccount.setAdminPwd("******");
        return newAccount;
    }

    /**
     * 启用/禁用子管理员
     *
     * @param id
     */
    @Override
    public void setStatus(Long id, Integer status) {
        //校验当前登录的管理员是否是超管（admin_type=1），不是则返回403
        Long currentAdminId = BaseContext.getCurrentAdminId();
        PlatformAdmin platformAdmin = platformAdminMapper.selectById(currentAdminId);

        if (!platformAdmin.getAdminType().equals(PlatformAdminTypeEnum.SUPER_ADMIN.getCode())) {
            throw new PlatformAdminErrorException(MessageConstant.ADMIN_NO_PERMISSION_TO_MODIFY);
        }

        //校验目标管理员是否存在，不存在则返回404
        PlatformAdmin admin = platformAdminMapper.selectById(id);
        if (admin == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //校验目标管理员不能是超管（不能禁用/启用另一个超管）
        if (admin.getAdminType().equals(PlatformAdminTypeEnum.SUPER_ADMIN.getCode())) {
            throw new PlatformAdminErrorException(MessageConstant.ADMIN_NO_PERMISSION_TO_MODIFY);
        }

        //更新该管理员的 status 字段
        admin.setStatus(status);
        platformAdminMapper.updateById(admin);
    }

    /**
     * 删除子管理员
     *
     * @param id
     */
    @Override
    public void delete(Long id) {
        //校验当前登录的管理员是否是超管（admin_type=1），不是则返回403
        Long currentAdminId = BaseContext.getCurrentAdminId();
        PlatformAdmin platformAdmin = platformAdminMapper.selectById(currentAdminId);

        if (!platformAdmin.getAdminType().equals(PlatformAdminTypeEnum.SUPER_ADMIN.getCode())) {
            throw new PlatformAdminErrorException(MessageConstant.ADMIN_NO_PERMISSION_TO_MODIFY);
        }

        //校验目标管理员是否存在，不存在则返回404
        PlatformAdmin admin = platformAdminMapper.selectById(id);
        if (admin == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //校验目标管理员不能是超管（不能删除另一个超管）
        if (admin.getStatus().equals(PlatformAdminTypeEnum.SUPER_ADMIN.getCode())) {
            throw new PlatformAdminErrorException(MessageConstant.ADMIN_NO_PERMISSION_TO_MODIFY);
        }

        //校验不能删除自己（当前登录的超管不能删除自己）
        if (id.equals(currentAdminId)) {
            throw new PlatformAdminErrorException(MessageConstant.ADMIN_NO_PERMISSION_TO_MODIFY);
        }

        //从数据库物理删除该子管理员记录
        platformAdminMapper.deleteById(id);
    }

    /**
     * 重置子管理员密码
     *
     * @param id
     */
    @Override
    public void updatePassword(Long id) {
        //校验当前登录的管理员是否是超管（admin_type=1），不是则返回403
        Long currentAdminId = BaseContext.getCurrentAdminId();
        PlatformAdmin platformAdmin = platformAdminMapper.selectById(currentAdminId);

        if (!platformAdmin.getAdminType().equals(PlatformAdminTypeEnum.SUPER_ADMIN.getCode())) {
            throw new PlatformAdminErrorException(MessageConstant.ADMIN_NO_PERMISSION_TO_MODIFY);
        }

        //校验目标管理员是否存在，不存在则返回404
        PlatformAdmin admin = platformAdminMapper.selectById(id);
        if (admin == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //校验目标管理员不能是超管（不能重置另一个超管）
        if (admin.getAdminType().equals(PlatformAdminTypeEnum.SUPER_ADMIN.getCode())) {
            throw new PlatformAdminErrorException(MessageConstant.ADMIN_NO_PERMISSION_TO_MODIFY);
        }

        String newPwd = "123456";
        newPwd = DigestUtils.md5DigestAsHex(newPwd.getBytes());

        admin.setAdminPwd(newPwd);
        platformAdminMapper.updateById(admin);
    }

    /**
     * 获取子管理员列表
     *
     * @param page
     * @param size
     * @param keyword
     * @return
     */
    @Override
    public PageResult<PlatformAdmin> page(Integer page, Integer size, String keyword) {
        //校验当前登录的管理员是否是超管（admin_type=1），不是则返回403
        Long currentAdminId = BaseContext.getCurrentAdminId();
        PlatformAdmin platformAdmin = platformAdminMapper.selectById(currentAdminId);

        if (!platformAdmin.getAdminType().equals(PlatformAdminTypeEnum.SUPER_ADMIN.getCode())) {
            throw new PlatformAdminErrorException(MessageConstant.ADMIN_NO_PERMISSION_TO_MODIFY);
        }

        //开始分页
        PageHelper.startPage(page, size);

        //查询管理员表，过滤 admin_type = 0（只查子管理员）
        //支持按 admin_account 模糊搜索（keyword参数）
        PlatformAdmin build = PlatformAdmin.builder()
                .adminAccount(keyword)
                .adminType(PlatformAdminTypeEnum.SUB_ADMIN.getCode())
                .build();
        List<PlatformAdmin> platformAdmins = platformAdminMapper.selectByPlatformAdmin(build);

        Page<PlatformAdmin> platformAdminPage=(Page<PlatformAdmin>) platformAdmins;

        for (PlatformAdmin admin : platformAdmins) {
            admin.setAdminPwd("******");
        }

        return new PageResult<>(platformAdminPage.getTotal(),platformAdminPage.getPageNum(),platformAdminPage.getPageSize(),platformAdmins);
    }
}
