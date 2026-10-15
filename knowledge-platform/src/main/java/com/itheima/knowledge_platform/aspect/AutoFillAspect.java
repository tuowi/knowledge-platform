package com.itheima.knowledge_platform.aspect;

import com.itheima.knowledge_platform.annotation.AutoFill;
import com.itheima.knowledge_platform.constant.AutoFillConstant;
import com.itheima.knowledge_platform.context.BaseContext;
import com.itheima.knowledge_platform.enums.TimeOperationEnum;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

//自定义切面，实现公共字段自动填充处理逻辑
@Slf4j
@Aspect
@Component
public class AutoFillAspect {

    //切入点
    @Pointcut("execution(* com.itheima.knowledge_platform.mapper.*.*(..)) && @annotation(com.itheima.knowledge_platform.annotation.AutoFill))")
    public void autoFillPointCut() {
    }

    //前置通知，在通知中进行公共字段赋值
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("开始进行公共字段赋值。。。");

        // 1. 拿到当前被拦截方法的「签名」（相当于方法的身份证：名字、参数、返回值）
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        // 2. 从方法上，拿到我们自定义的 @AutoFill 注解
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);

        // 3. 从注解里，取出数据库操作类型：INSERT / UPDATE
        TimeOperationEnum timeOperationEnum = autoFill.value();

        //获取到当前被拦截方法的参数实体对象
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return;
        }

        //得到实体对象
        Object entity = args[0];

        //准备赋值的数据
        LocalDateTime now = LocalDateTime.now();//当前时间
        Long currentId = BaseContext.getCurrentId();//当前操作者id

        //根据当前不同的操作类型，为对应的属性通过反射来赋值
        if (timeOperationEnum == TimeOperationEnum.INSERT) {
            //为四个公共字段赋值
            try {
                Method setCreatedAt = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setUpdatedAt = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);

                //通过反射为映射对象属性赋值
                setCreatedAt.invoke(entity, now);
                setUpdatedAt.invoke(entity, now);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (timeOperationEnum == TimeOperationEnum.UPDATE) {
            //为两个公共字段赋值
            try {
                Method setUpdatedAt = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);

                //通过反射为映射对象属性赋值
                setUpdatedAt.invoke(entity, now);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
