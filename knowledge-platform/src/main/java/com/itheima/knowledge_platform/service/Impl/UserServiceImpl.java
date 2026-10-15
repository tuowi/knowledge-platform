package com.itheima.knowledge_platform.service.Impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.itheima.knowledge_platform.constant.JwtClaimsConstant;
import com.itheima.knowledge_platform.constant.MessageConstant;
import com.itheima.knowledge_platform.constant.StatusConstant;
import com.itheima.knowledge_platform.context.BaseContext;
import com.itheima.knowledge_platform.enums.IdentityEnum;
import com.itheima.knowledge_platform.enums.TeamStatusEnum;
import com.itheima.knowledge_platform.exception.*;
import com.itheima.knowledge_platform.mapper.*;
import com.itheima.knowledge_platform.pojo.dto.UserDTO;
import com.itheima.knowledge_platform.pojo.dto.UserLoginDTO;
import com.itheima.knowledge_platform.pojo.dto.UserQueryRequestDTO;
import com.itheima.knowledge_platform.pojo.entity.PlatformAdmin;
import com.itheima.knowledge_platform.pojo.entity.Team;
import com.itheima.knowledge_platform.pojo.entity.TeamMember;
import com.itheima.knowledge_platform.pojo.entity.User;
import com.itheima.knowledge_platform.pojo.inner.document.*;
import com.itheima.knowledge_platform.pojo.inner.user.UserStatistics;
import com.itheima.knowledge_platform.pojo.inner.user.UserTeam;
import com.itheima.knowledge_platform.pojo.vo.UserAdminVO;
import com.itheima.knowledge_platform.pojo.vo.UserDetailVO;
import com.itheima.knowledge_platform.pojo.vo.UserLoginVO;
import com.itheima.knowledge_platform.pojo.vo.UserStatisticsOverviewVO;
import com.itheima.knowledge_platform.properties.JwtProperties;
import com.itheima.knowledge_platform.result.PageResult;
import com.itheima.knowledge_platform.service.UserService;
import com.itheima.knowledge_platform.utils.JwtUtil;
import com.itheima.knowledge_platform.utils.TokenUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private TeamMapper teamMapper;
    @Autowired
    private PlatformAdminMapper platformAdminMapper;
    @Autowired
    private DocumentMapper documentMapper;
    @Autowired
    private TeamMemberMapper teamMemberMapper;

    /**
     * 用户登录
     *
     * @param userLoginDTO
     * @return
     */
    @Override
    public UserLoginVO login(UserLoginDTO userLoginDTO) {
        //1.获取用户信息
        String username = userLoginDTO.getUsername();
        String password = userLoginDTO.getPassword();

        //2.根据用户账号查询用户是否存在
        User user = userMapper.getByUsername(username);

        //3.若账号不存在，抛出异常
        if (user == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //4.若账号存在，比对密码
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        if (!password.equals(user.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (user.getStatus().equals(StatusConstant.DISABLE)) {
            //账号禁用
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        claims.put(JwtClaimsConstant.USERNAME, user.getUsername());

        // 队长身份需要从团队表获取  团队需要非解散
        List<Team> teamLists = teamMapper.selectByOwnerUserId(user.getId());
        List<Team> teamList = teamLists.stream()
                .filter(team -> !team.getStatus().equals(TeamStatusEnum.DISBAND.getCode()))
                .toList();

        //检验是否有队长身份
        if (CollectionUtils.isEmpty(teamList)) {
            claims.put(JwtClaimsConstant.USER_IDENTITY, IdentityEnum.NORMAL.getCode());
        } else {
            claims.put(JwtClaimsConstant.USER_IDENTITY, IdentityEnum.TEAM_LEADER.getCode());
        }

        String token = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims
        );

        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .token(token)
                .build();

        return userLoginVO;
    }

    /**
     * 用户注册
     *
     * @param userDTO
     */
    @Override
    public void register(UserDTO userDTO) {
        //对象类型转换
        User user = new User();

        //密码加密
        String password = userDTO.getPassword();
        userDTO.setPassword(DigestUtils.md5DigestAsHex(password.getBytes()));

        //对象属性拷贝 原->目标，两者的属性名应该一致
        BeanUtils.copyProperties(userDTO, user);

        //属性设置
        user.setStatus(StatusConstant.ENABLE);
        user.setCreatedAt(LocalDateTime.now());

        userMapper.insert(user);
    }

    /**
     * 用户更改信息
     *
     * @param userDTO
     */
    @Override
    public void modify(UserDTO userDTO) {
        //1.校验用户是否存在
        User user = userMapper.getByUsername(userDTO.getUsername());

        if (user == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码加密转换
        String password = userDTO.getPassword();
        if (password != null) {
            password = DigestUtils.md5DigestAsHex(password.getBytes());
            userDTO.setPassword(password);
        }


        //2.对象类型转换
        User user1 = new User();
        BeanUtils.copyProperties(userDTO, user1);
        user1.setId(user.getId());
        userMapper.update(user1);
    }

    /**
     * 修改密码
     *
     * @param oldPassword
     * @param newPassword
     */
    @Override
    public void editPassword(String oldPassword, String newPassword) {
        Long currentUserId = BaseContext.getCurrentUserId();

        //通过用户id查询用户
        User user = userMapper.getById(currentUserId);
        if (user == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //验证旧密码是否正确
        if (!oldPassword.equals(user.getPassword())) {
            throw new PasswordErrorException(MessageConstant.OLD_PASSWORD_ERROR);
        }

        //验证新旧密码是否一致
        if (DigestUtils.md5DigestAsHex(newPassword.getBytes()).equals(user.getPassword())) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_EDIT_FAILED);
        }

        //设置新密码
        User user1 = User.builder().password(DigestUtils.md5DigestAsHex(newPassword.getBytes())).build();
        userMapper.update(user1);
    }

    /**
     * 获取所有用户列表
     *
     * @param userQueryRequestDTO
     * @return
     */
    @Override
    public PageResult<UserAdminVO> adminPage(UserQueryRequestDTO userQueryRequestDTO) {
        //权限校验：确认当前登录用户是超管或子管理员（有用户管理权限）
        Long currentAdminId = BaseContext.getCurrentAdminId();
        PlatformAdmin platformAdmin = platformAdminMapper.selectById(currentAdminId);
        if (platformAdmin == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //分页查询：从 user 表查询用户列表，按创建时间倒序
        //关键词搜索：如果传了 keyword，对 username 和 email 字段进行模糊匹配（LIKE '%keyword%'）
        //状态筛选：如果传了 status，按状态过滤
        PageHelper.startPage(userQueryRequestDTO.getPage(), userQueryRequestDTO.getSize());

        User user = User.builder()
                .username(userQueryRequestDTO.getUsername())
                .status(userQueryRequestDTO.getStatus())
                .build();
        List<User> users = userMapper.selectByUser(user);

        PageInfo<User> userPageInfo = new PageInfo<>(users);

        List<UserAdminVO> vos = users.stream().map(po -> {
            UserAdminVO userAdminVO = new UserAdminVO();
            BeanUtils.copyProperties(po, userAdminVO);

            //查询团队数目
            List<TeamMember> teamByUserId = teamMemberMapper.selectTeamByUserId(po.getId());
            userAdminVO.setTeamCount(teamByUserId.size());

            Integer sum = 0;
            List<String> list = new ArrayList<>();

            //查询团队名称 和 文档数目
            for (TeamMember member : teamByUserId) {
                Team team = teamMapper.selectById(member.getTeamId());
                list.add(team.getName());

                sum += documentMapper.selectByTeamId(team.getId()).size();
            }

            userAdminVO.setTeamNames(list);
            userAdminVO.setDocumentCount(sum);
            return userAdminVO;
        }).collect(Collectors.toList());

        return new PageResult<>(userPageInfo.getTotal(), userPageInfo.getPageNum(), userPageInfo.getPageSize(), vos);
    }

    /**
     * 禁用/启用用户
     *
     * @param id
     * @param status
     */
    @Override
    public void setUserStatus(Long id, Integer status) {
        //权限校验：确认当前管理员有用户管理权限
        Long currentAdminId = BaseContext.getCurrentAdminId();
        PlatformAdmin platformAdmin = platformAdminMapper.selectById(currentAdminId);
        if (platformAdmin == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //校验目标用户存在：用户ID必须存在
        User user = userMapper.getById(id);
        if (user == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //更新状态：更新 user 表的 status 字段
        user.setStatus(status);
        userMapper.update(user);

        /**
         * TODO 清理会话：如果禁用用户，需要：
         * 使该用户的所有登录Token失效（清空Redis中的Token或修改JWT黑名单）
         * 断开该用户的WebSocket连接（如果有）
         */
    }

    /**
     * 获取用户详情
     *
     * @param id
     * @return
     */
    @Override
    public UserDetailVO userDetail(Long id) {
        //权限校验：确认当前管理员有用户管理权限
        Long currentAdminId = BaseContext.getCurrentAdminId();
        PlatformAdmin platformAdmin = platformAdminMapper.selectById(currentAdminId);
        if (platformAdmin == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //查询用户基础信息，如果不存在返回404
        User user = userMapper.getById(id);
        if (user == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //类型转换
        UserDetailVO userDetailVO = new UserDetailVO();
        BeanUtils.copyProperties(user, userDetailVO);
        userDetailVO.setStatusDesc(user.getStatus() == StatusConstant.ENABLE ? "启用" : "禁用");

        //统计数据
        UserStatistics statistics = documentMapper.statisticsWithUser(user.getId());
        userDetailVO.setStatistics(statistics);
        //统计团队数据
        List<UserTeam> userTeam = teamMapper.selectUserTeamDetail(user.getId());
        userDetailVO.setTeams(userTeam);

        return userDetailVO;
    }

    /**
     * 获取用户文档统计
     * @param id
     * @return
     */
    @Override
    public UserStatisticsOverviewVO statisticsUser(Long id) {
        //权限校验：确认当前管理员有用户管理权限
        Long currentAdminId = BaseContext.getCurrentAdminId();
        PlatformAdmin platformAdmin = platformAdminMapper.selectById(currentAdminId);
        if (platformAdmin == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //查询用户基础信息，如果不存在返回404
        User user = userMapper.getById(id);
        if (user == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        UserStatisticsOverviewVO userStatisticsOverviewVO=new UserStatisticsOverviewVO();
        userStatisticsOverviewVO.setUserId(user.getId());
        userStatisticsOverviewVO.setUsername(user.getUsername());

        //从 document 表查询该用户的所有文档（author_id = userId）
        //收到的评论总数（自己文档下的评论数，不是自己发表的评论）
        Summary summary=documentMapper.summaryUser(user.getId());
        userStatisticsOverviewVO.setSummary(summary);

        //按状态分组：草稿、已发布、已归档
        ByStatus byStatus=documentMapper.byStatusUser(user.getId());
        userStatisticsOverviewVO.setByStatus(byStatus);

        //按审核状态分组：待审核、已通过、已驳回
        ByAuditStatus byAuditStatus=documentMapper.byAuditStatusUser(user.getId());
        userStatisticsOverviewVO.setByAuditStatus(byAuditStatus);

        //按可见性分组：私有、团队内、公开
        ByVisibility byVisibility=documentMapper.byVisibilityUser(user.getId());
        userStatisticsOverviewVO.setByVisibility(byVisibility);

        return userStatisticsOverviewVO;
    }
}
