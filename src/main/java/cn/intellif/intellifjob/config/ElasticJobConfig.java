package cn.intellif.intellifjob.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;

public class ElasticJobConfig {


    @Bean(initMethod = "init")
    public ZookeeperRegistryCenter regCenter(@Value("${elaticjob.zookeeper.server-lists}") final String serverList, @Value("${elaticjob.zookeeper.namespace}") final String namespace) {
        return new ZookeeperRegistryCenter(new ZookeeperConfiguration(serverList, namespace));
    }
}