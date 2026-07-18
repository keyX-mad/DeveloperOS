package com.keyx.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 * MyBatis-Plus 配置
 *
 * 显式声明 SqlSessionFactory、Pagination 拦截器，
 * 解决 Spring Boot 3.5 + MyBatis-Plus 的 SqlSessionFactory 缺失问题。
 */
@Configuration
@MapperScan({
    "com.keyx.module.user.mapper",
    //"com.keyx.module.auth.mapper",
    "com.keyx.module.chat.mapper"
})
public class MybatisPlusConfig {

    /**
     * 显式声明 SqlSessionFactory
     *
     * 不显式声明的话，Spring Boot 3.5 + MyBatis-Plus 3.5.5+ 可能会找不到 SqlSessionFactory Bean
     */
    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPlugins(mybatisPlusInterceptor());

        // 设置 mapper XML 文件位置（V1 没有 XML，留着以后用）
        factory.setMapperLocations(
            new PathMatchingResourcePatternResolver()
                .getResources("classpath*:/mapper/**/*.xml")
        );

        return factory.getObject();
    }

    /**
     * 分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.POSTGRE_SQL));
        return interceptor;
    }
}